/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.paas.configurator.sys.SysUtil;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBossDmrActions extends DmrActions {

    /**
     * @param context
     */
    public JBossDmrActions(OperationContext context) {
        super(context);
    }

    public JBossDmrActions(OperationContext context, OperationStepRegistry stepRegistry) {
        super(context, stepRegistry);
    }

    private static final Logger log = Logger.getLogger(JBossDmrActions.class);

    public boolean isDomainController() {
        Resource rootResource = context.getRootResource();
        // /host=172.16.254.128:read-config-as-xml
        // rootResource.navigate(PathAddress.pathAddress(PathElement.pathElement("host")));
        // rootResource.getModel().get("host") //undefined
        // System.out.println(">>>>>>> Util.isDomainController: " +
        // rootResource.getChildTypes().contains("server-group"));
        // return rootResource.getChildTypes().contains("server-group");

        String localIp = SysUtil.getLocalIp();

        PathAddress addr = PathAddress.pathAddress(PathElement.pathElement("host", localIp));

        final Resource resource = rootResource.navigate(addr);
        String domainController = resource.getModel().get("domain-controller").asPropertyList().get(0).getName();
        return "local".equals(domainController);
    }

    /**
     * configure remote host to connect to domain controller
     * @param required
     */
    public void addHostToServerGroup(InstanceSlot slot, String groupName, String[] requiredSteps) {
        log.debugf("Adding step addHostToServerGroup. Instance [%s] with ip [%s] to server group [%s].", slot.getInstanceId(), slot.getHostIP(), groupName);

        // addHOST to SG
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets,
        // socket-binding-port-offset=<portOffset>)
        ModelNode op = new ModelNode();
        op.get(OP).set("add");
        op.get(OP_ADDR).add("host", slot.getHostIP());
        op.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        op.get("group").set(groupName);
        op.get("auto-start").set(true);
        //TODO validate if it is a bug, that server throws an exception on boot if ref attribute is included
        //opAddHostToSg.get("socket-binding-group").set("standard-sockets");
        op.get("socket-binding-port-offset").set(slot.getPortOffset());

        //addStepToContext(op, OperationContext.Stage.DOMAIN, "addHostToServerGroup", requiredSteps); //same problem, execution out of order
        addStepToContext(op, OperationContext.Stage.MODEL, "addHostToServerGroup", requiredSteps);
    }

    void removeHostFromServerGroup(ModelNode steps, String groupName, InstanceSlot slot) {
        // rmeoveHOST from SG
        // /host=master/server-config=server-one:remove()
        ModelNode opRemoveHostFromSg = new ModelNode();
        opRemoveHostFromSg.get(OP).set("remove");

        opRemoveHostFromSg.get(OP_ADDR).add("host", slot.getHostIP());
        opRemoveHostFromSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opRemoveHostFromSg.get("group").set(groupName);
        steps.add(opRemoveHostFromSg);
    }

    /**
     * deploy application content and associate deployment with server group
     * @param requiredSteps
     */
    public void deployToServerGroup(final File f, String appName, String serverGroup, String[] requiredSteps) {
        if (!stepRegistry.areExecuted(requiredSteps)) {
            log.debug("Skipping execution of step deployToServerGroup.");
            return;
        }
        // Deployment process extracted from
        // org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        final ModelNode request;

        //TODO replace deployment

        // add deployment
        ModelNode opAddDeployment = new ModelNode();
        opAddDeployment.get(OP).set("add");
        opAddDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);

        try {
            context.addStep(opAddDeployment, new OperationStepHandler() {
                @Override
                public void execute(OperationContext context, ModelNode operation) {
                    OperationContext ctxWthStream = null;
                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(f);
                        operation.get("content").get(0).get("input-stream-index").set(0);
                        OperationBuilder opb = new OperationBuilder(operation);
                        opb.addInputStream(is);
                        Operation opWthStream = opb.build();
                        ctxWthStream = OperationContextHelper.addAttachmentToContext(context, opWthStream);
                        OperationStepHandler opStep = ctxWthStream.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, "add");
                        opStep.execute(ctxWthStream, opWthStream.getOperation());
                    } catch (Throwable t) {
                        // TODO
                        log.error("Deployment failed.", t);
                    } finally {
                        try {
                            is.close();
                        } catch (Throwable t) {
                            log.errorf(t, "Failed to close resource %s", is);
                        }
                        context.completeStep();
                    }
                }
            }, OperationContext.Stage.MODEL);
        } catch (Exception e) {
            context.getResult().add("Failed to add the deployment content to the repository: " + e.getLocalizedMessage());
            log.error("Deployment failed while adding step.", e);
            return;
        }

        // add deployment to server group
        // prepare composite operation
        request = new ModelNode();
        request.get("operation").set("composite");
        request.get("address").setEmptyList();
        ModelNode steps = request.get("steps");

        // deploy app - step add
        ModelNode opAdd = new ModelNode();
        opAdd.get(OP).set("add");
        opAdd.get(OP_ADDR).add("server-group", serverGroup);
        opAdd.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opAdd);

        // deploy app - step deploy
        ModelNode opDeploy = new ModelNode();
        opDeploy.get(OP).set("deploy");
        opDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opDeploy);

        addStepToContext(request);
        // TODO verify result
    }

    /**
     * Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)
     */
    public void undeployFromServerGroup(String appName, String serverGroup) {
        final ModelNode request;

        // prepare composite operation
        request = new ModelNode();
        request.get("operation").set("composite");
        request.get("address").setEmptyList();
        ModelNode steps = request.get("steps");

        // undeploy app - step undeploy
        ModelNode opUnDeploy = new ModelNode();
        opUnDeploy.get(OP).set("undeploy");
        opUnDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opUnDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opUnDeploy);

        // undeploy app - step remove
        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("server-group", serverGroup);
        opRemove.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemove);

        // remove deployment
        ModelNode opRemoveDeployment = new ModelNode();
        opRemoveDeployment.get(OP).set("remove");
        opRemoveDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemoveDeployment);

        addStepToContext(request);

        // TODO verify result
    }

    public void removeServerGroup(String serverGroupName) {
        ModelNode request = new ModelNode();
        request.get(OP).set("remove");
        request.get(OP_ADDR).add("server-group", serverGroupName);

        addStepToContext(request);
    }

    public void addHostToDomain(String hostIp, ModelControllerClient client) throws IOException, OperationFormatException {
        String dcIP = SysUtil.getLocalIp();

        // /host=<hostIP>:write-remote-domain-controller(host=1.2.3.4,
        // port=9999)
        DefaultOperationRequestBuilder setDc = new DefaultOperationRequestBuilder();
        setDc.addNode("host", hostIp);
        setDc.setOperationName("write-remote-domain-controller");
        setDc.addProperty("host", dcIP);
        setDc.addProperty("port", "9999");
        client.execute(setDc.buildRequest());
    }

    /**
     * create a server group on DC
     *
     * @param context
     * @param serverGroupName
     * @param requiredSteps
     */
    public void createServerGroup(String serverGroupName, String[] requiredSteps) {
        log.tracef("Adding step createServerGroup [%s].", serverGroupName);

        ModelNode opHeaders = new ModelNode();
        opHeaders.get("execute-for-coordinator").set(true);

        ModelNode op = new ModelNode();
        if (false)
            op.get(OPERATION_HEADERS).set(opHeaders);
        op.get(OP).set("add");
        op.get(OP_ADDR).add("server-group", serverGroupName);
        op.get("profile").set("default");
        op.get("socket-binding-group").set("standard-sockets");

        addStepToContext(op, "createServerGroup", requiredSteps);
    }

    public void validateHostRegistration(final String hostName) {
        // :read-children-names(child-type=host)

        ModelNode op = new ModelNode();
        op.get(OP).set("read-children-names");
        op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
        op.get("child-type").set("host");

        context.addStep(op, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStepValidateHostReg(context, operation, "validateHostRegistration", hostName);
            }
        }, OperationContext.Stage.MODEL);
    }

    private void executeStepValidateHostReg(OperationContext context, ModelNode operation, String stepName, String hostName) {
        log.tracef("Executing step %s ...", stepName);

        doExecuteStep(context, operation);

        ModelNode result = context.getResult();
        List<ModelNode> resultList = result.asList();
        boolean success = false;
        for (ModelNode modelNode : resultList) {
            if (modelNode.asString().equals(hostName)) {
                log.tracef("Step %s executed successfully.", stepName);
                stepRegistry.addExecuted(stepName);
                success = true;
                break;
            }
        }

        if (!success) {
            log.tracef("Step %s did not execute success. Result [%s].", stepName, result);
            stepRegistry.addFailed(stepName);
        }
        //context.getResult().setEmptyObject();
        //XXX        context.completeStep();
    }

    public void reloadHost(String hostIP) {
        // /host=172.16.254.233:reload
        ModelNode request = new ModelNode();
        request.get(OP).set("reload");
        request.get(OP_ADDR).add("host", hostIP);

        addStepToContext(request);
    }

    public void startServer(InstanceSlot slot) {
        // /host=172.16.254.233/server-config=server0:start
        ModelNode request = new ModelNode();
        request.get(OP).set("start");
        request.get(OP_ADDR).add("host", slot.getHostIP());
        request.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        addStepToContext(request);

    }
}