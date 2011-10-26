/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;
import java.io.FileInputStream;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JbossDmrActions extends DmrActions {

    private static final Logger log = Logger.getLogger(JbossDmrActions.class);

    public static boolean isDomainController(OperationContext context) {
        Resource rootResource = context.getRootResource();
        // /host=172.16.254.128:read-config-as-xml
        //rootResource.navigate(PathAddress.pathAddress(PathElement.pathElement("host")));
        //rootResource.getModel().get("host") //undefined
//        System.out.println(">>>>>>> Util.isDomainController: " + rootResource.getChildTypes().contains("server-group"));
//        return rootResource.getChildTypes().contains("server-group");

        String localIp = System.getProperty("local.address.ip");

        PathAddress addr = PathAddress.pathAddress(
                PathElement.pathElement("host", localIp));
        final Resource resource = rootResource.navigate(addr);
        String domainController = resource.getModel().get("domain-controller").asPropertyList().get(0).getName();
        return "local".equals(domainController);
    }

    static void addHostToServerGroup(OperationContext context, InstanceSlot slot, String groupName) {
        //addHOST to SG
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets, socket-binding-port-offset=<portOffset>)
        ModelNode opAddHostToSg = new ModelNode();
        opAddHostToSg.get(OP).set("add");
        opAddHostToSg.get(OP_ADDR).add("host", slot.getHostIP());
        opAddHostToSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opAddHostToSg.get("group").set(groupName);
        opAddHostToSg.get("auto-start").set(true);
        opAddHostToSg.get("socket-binding-group").set("standard-sockets");
        opAddHostToSg.get("socket-binding-port-offset").set(slot.getPortOffset());

        addStepToContext(context, opAddHostToSg);
    }

    static void removeHostFromServerGroup(ModelNode steps, String groupName, InstanceSlot slot) {
        //rmeoveHOST from SG
        // /host=master/server-config=server-one:remove()
        ModelNode opRemoveHostFromSg = new ModelNode();
        opRemoveHostFromSg.get(OP).set("remove");

        opRemoveHostFromSg.get(OP_ADDR).add("host", slot.getHostIP());
        opRemoveHostFromSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opRemoveHostFromSg.get("group").set(groupName);
        steps.add(opRemoveHostFromSg);
    }

    /**
     * deploy application content and associate deployment with servr group
     *
     * @param context
     * @param f
     * @param appName
     */
    public static void deployToServerGroup(OperationContext context, final File f, String appName, String serverGroup) {
        //Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        final ModelNode request;

        //add deployment
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
                        //TODO
                        t.printStackTrace();
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
            return;
        }

        //add deployment to server group
        //prepare composite operation
        request = new ModelNode();
        request.get("operation").set("composite");
        request.get("address").setEmptyList();
        ModelNode steps = request.get("steps");

        //deploy app - step add
        ModelNode opAdd = new ModelNode();
        opAdd.get(OP).set("add");
        opAdd.get(OP_ADDR).add("server-group", serverGroup);
        opAdd.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opAdd);

        //deploy app - step deploy
        ModelNode opDeploy = new ModelNode();
        opDeploy.get(OP).set("deploy");
        opDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opDeploy);

        addStepToContext(context, request);
        //TODO verify result
    }

    /**
     * Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)
     *
     * @param context
     * @param appName
     * @param serverGroup
     */
    public static void undeployFromServerGroup(OperationContext context, String appName, String serverGroup) {
        final ModelNode request;

        //prepare composite operation
        request = new ModelNode();
        request.get("operation").set("composite");
        request.get("address").setEmptyList();
        ModelNode steps = request.get("steps");

        //undeploy app - step undeploy
        ModelNode opUnDeploy = new ModelNode();
        opUnDeploy.get(OP).set("undeploy");
        opUnDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opUnDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opUnDeploy);

        //undeploy app - step remove
        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("server-group", serverGroup);
        opRemove.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemove);

        //remove deployment
        ModelNode opRemoveDeployment = new ModelNode();
        opRemoveDeployment.get(OP).set("remove");
        opRemoveDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemoveDeployment);

        addStepToContext(context, request);

        //TODO verify result
    }

    /**
     * @param context
     * @param appName
     */
    public static void removeServerGroup(OperationContext context, String serverGroupName) {
        ModelNode request = new ModelNode();
        request.get(OP).set("remove");
        request.get(OP_ADDR).add("server-group", serverGroupName);

        addStepToContext(context, request);
    }



}