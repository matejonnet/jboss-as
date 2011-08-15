/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.DomainController;
import org.jboss.as.paas.controller.Util;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployHandle implements OperationStepHandler {
    public static final DeployHandle INSTANCE = new DeployHandle();
    public static final String OPERATION_NAME = "deploy";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";

    //TODO make configurable
    private static final int MAX_AS_PER_HOST = 3;

    private final Logger log = Logger.getLogger(DeployHandle.class);


    private DeployHandle() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {

        final String filePath = operation.get(ATTRIBUTE_PATH).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;


        //TODO validate required attributes

        final File f;
        if(filePath != null) {
            f = new File(filePath);
            if(!f.exists()) {
                context.getResult().add("Path " + f.getAbsolutePath() + " doesn't exist. File must be located on localhost.");
                return;
            }
            if(f.isDirectory()) {
                context.getResult().add(f.getAbsolutePath() + " is a directory.");
                return;
            }
        } else {
            f = null;
        }

        String appName = f.getName();

        createServerGroup(context, appName);

        addHostToServerGroup(newInstance, provider, context, getServerGroupName(appName));

        deployToServerGroup(context, f, appName);

        context.completeStep();
    }

    /**
     * @param newInstance
     * @param provider
     * @param context
     * @param groupName
     *
     */
    private void addHostToServerGroup(boolean newInstance, String provider, OperationContext context, String groupName) {
        boolean newInstanceRequired = false;
        if (newInstance) {
            newInstanceRequired = true;
        }

        InstanceSlot slot = null;

        if (!newInstanceRequired) {
            slot = getFreeSlot(groupName, context, provider);
        }

        if (slot == null) {
            newInstanceRequired = true;
        }

        if (newInstanceRequired) {
            slot = addServerInstanceToDomain(provider);
        }

        ModelNode compositeRequest = new ModelNode();
        compositeRequest.get("operation").set("composite");
        compositeRequest.get("address").setEmptyList();
        ModelNode steps = compositeRequest.get("steps");

        //addHOST to SG
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets, socket-binding-port-offset=<portOffset>)
        ModelNode opAddHostToSg = new ModelNode();
        opAddHostToSg.get(OP).set("add");
        opAddHostToSg.get(OP_ADDR).add("host", slot.getHostIP());
        opAddHostToSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opAddHostToSg.get("group").set(groupName);
        opAddHostToSg.get("auto-start").set(true);
        opAddHostToSg.get("socket-binding-group").set("standard-sockets");
        opAddHostToSg.get("socket-binding-port-offset").set(slot.getPortOffset()); //TODO verify
        steps.add(opAddHostToSg);

        ModelNode opAddSgToInstance = new ModelNode();
        opAddSgToInstance.get(OP).set("add");
        opAddSgToInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("instance", slot.getInstanceId());
        opAddSgToInstance.get(OP_ADDR).add("server-group", groupName);
        //opAddSgToInstance.get("name").set(groupName);
        opAddSgToInstance.get("position").set(slot.getSlotPosition());


//        ModelNode serverGroup = new ModelNode();
//        serverGroup.get("name").set(groupName);
//        serverGroup.get("position").set(slot.getSlotPosition());
//        opAddSgToInstance.get("serverGroups").add(serverGroup);

        //<server-group name="other-server-group" position="0"/>
        //opAddSgToInstance.get("position").set(slot.getSlotPosition());

        steps.add(opAddSgToInstance);


        context.addStep(compositeRequest, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);
    }

    /**
     * loop throught instances which doesn't serve this group jet
     * @param context
     * @param providerName
     *
     * @return
     */
    private InstanceSlot getFreeSlot(String group, OperationContext context, String providerName) {
        Resource rootResource = context.getRootResource();
        ModelNode operationAddr = new ModelNode();

        PathAddress instancesAddr = PathAddress.pathAddress(
                PathElement.pathElement("profile", "paas-controller"),
                PathElement.pathElement("subsystem", "paas-controller"));

        final Resource instancesResource = rootResource.navigate(instancesAddr);
        //Set<String> instances = instancesResource.getChildrenNames("instance");
        Set<ResourceEntry> instances = instancesResource.getChildren("instance");

        for (ResourceEntry instance : instances) {
            boolean hasFreeSlot = true;
            Set<Integer> usedPositions = new HashSet<Integer>();

//            List<ModelNode> serverGroups = instance.getModel().get("server-group").asList();
//            Resource serverGroups = instance.getChild(PathElement.pathElement("server-group"));
            Set<ResourceEntry> serverGroups = instance.getChildren("server-group");


            if (serverGroups.size() > MAX_AS_PER_HOST) {
                hasFreeSlot=false;
            }

            if (hasFreeSlot)
            for (ResourceEntry serverGroup : serverGroups) {
                //if server group is already on this instance don't allow another
                if (group.equals(serverGroup.getName())) {
                    hasFreeSlot=false;
                }
                usedPositions.add(serverGroup.getModel().get("position").asInt());
            }

            if (hasFreeSlot) {
                //find first free slot
                for (int i = 0; i < MAX_AS_PER_HOST ; i++) {
                    if (!usedPositions.contains(i)) {
                        String hostIP = null;
                        try {
                            hostIP = IaasController.getInstanceIp(providerName, instance.getName());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return new InstanceSlot(hostIP, i, instance.getName());
                    }
                }

            }
        }
        return null;
    }

    /**
     * @param context
     * @param appName
     */
    private void createServerGroup(OperationContext context, String appName) {

        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        builder.setOperationName("add");
        builder.addNode("server-group", getServerGroupName(appName));
        builder.addProperty("profile", "default");
        builder.addProperty("socket-binding-group", "standard-sockets");
        //TODO if this is required ? parametrize port offset, calculate it acording to deployment number per instance (1st=>0; 2nd=>100, ...)
        //builder.addProperty("socket-binding-port-offset", "0");
        try {
            ModelNode request = builder.buildRequest();

            context.addStep(request, new OperationStepHandler() {
                public void execute(OperationContext context, ModelNode operation) {
                    Util.executeStep(context, operation);
                }
            }, OperationContext.Stage.MODEL);
        } catch (OperationFormatException e) {
            // TODO Auto-generated catch block
            log.error("Cannot build request to create server group.", e);
        }
    }


    private void deployToServerGroup(OperationContext context, final File f, String appName) {
        //Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        String serverGroup = getServerGroupName(appName);

        final ModelNode request;

        //add deployment
        ModelNode opAddDeployment = new ModelNode();
        opAddDeployment.get(OP).set("add");
        opAddDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);

        try {
            context.addStep(opAddDeployment, new OperationStepHandler() {
                public void execute(OperationContext context, ModelNode operation) {
                    OperationContext ctxWthStream = null;
                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(f);
                        operation.get("content").get(0).get("input-stream-index").set(0);
                        OperationBuilder opb = new OperationBuilder(operation);
                        opb.addInputStream(is);
                        Operation opWthStream = opb.build();
                        ctxWthStream = addAttachmentToContext(context, opWthStream);
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
                        //ctxWthStream.completeStep();
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


        context.addStep(request, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);


        //TODO verify result
    }


    /**
     * - creates new server instance
     * - join it to domain controller
     * - associate it with server group
     * - start the server instance
     * @return
     *
     */
    private InstanceSlot addServerInstanceToDomain(String provider) {
        try {
            //TODO update config instances/instance
            String instanceId = IaasController.createNewInstance(provider);
            String hostIp = IaasController.getInstanceIp(provider, instanceId);
            DomainController.addHostToDomain(hostIp);
            return new InstanceSlot(hostIp, 0, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }



    private String getServerGroupName(String appName) {
        //return appName + "-SG";
        return appName;
    }


    /**
     * create operation context with input stream
     *
     * @param context
     * @param operation
     * @return
     */
    protected OperationContext addAttachmentToContext(final OperationContext context, final Operation operation) {
        return new OperationContext() {

            @Override
            public int getAttachmentStreamCount() {
                return operation.getInputStreams().size();
            }

            @Override
            public InputStream getAttachmentStream(int index) {
                if (operation.getInputStreams().size() <= index) {
                    throw new ArrayIndexOutOfBoundsException(index);
                }
                return operation.getInputStreams().get(index);
            }

            @Override
            public void setRollbackOnly() {
                context.setRollbackOnly();
            }

            @Override
            public void runtimeUpdateSkipped() {
                context.runtimeUpdateSkipped();
            }

            @Override
            public void revertRestartRequired() {
                context.revertRestartRequired();
            }

            @Override
            public void revertReloadRequired() {
                context.revertReloadRequired();
            }

            @Override
            public void restartRequired() {
                context.restartRequired();
            }

            @Override
            public void report(MessageSeverity severity, String message) {
                context.report(severity, message);
            }

            @Override
            public void removeService(ServiceController<?> controller) throws UnsupportedOperationException {
                context.removeService(controller);
            }

            @Override
            public ServiceController<?> removeService(ServiceName name) throws UnsupportedOperationException {
                return context.removeService(name);
            }

            @Override
            public Resource removeResource(PathAddress address) throws UnsupportedOperationException {
                return context.removeResource(address);
            }

            @Override
            public void reloadRequired() {
                context.reloadRequired();
            }

            @Override
            public Resource readResourceForUpdate(PathAddress address) {
                return context.readResourceForUpdate(address);
            }

            @Override
            public Resource readResource(PathAddress address) {
                return context.readResource(address);
            }

            @Override
            public ModelNode readModelForUpdate(PathAddress address) {
                return context.readModelForUpdate(address);
            }

            @Override
            public ModelNode readModel(PathAddress address) {
                return context.readModel(address);
            }

            @Override
            public boolean isRuntimeAffected() {
                return context.isResourceRegistryAffected();
            }

            @Override
            public boolean isRollbackOnly() {
                return context.isRollbackOnly();
            }

            @Override
            public boolean isResourceRegistryAffected() {
                return context.isResourceRegistryAffected();
            }

            @Override
            public boolean isModelAffected() {
                return context.isModelAffected();
            }

            @Override
            public boolean isBooting() {
                return context.isBooting();
            }

            @Override
            public boolean hasResult() {
                return context.hasResult();
            }

            @Override
            public boolean hasFailureDescription() {
                return context.hasFailureDescription();
            }

            @Override
            public Type getType() {
                return context.getType();
            }

            @Override
            public ServiceTarget getServiceTarget() throws UnsupportedOperationException {
                return context.getServiceTarget();
            }

            @Override
            public ServiceRegistry getServiceRegistry(boolean modify) throws UnsupportedOperationException {
                return context.getServiceRegistry(modify);
            }

            @Override
            public Resource getRootResource() {
                return context.getRootResource();
            }

            @Override
            public ModelNode getResult() {
                return context.getResult();
            }

            @Override
            public ManagementResourceRegistration getResourceRegistrationForUpdate() {
                return context.getResourceRegistrationForUpdate();
            }

            @Override
            public ImmutableManagementResourceRegistration getResourceRegistration() {
                return context.getResourceRegistration();
            }

            @Override
            public ModelNode getFailureDescription() {
                return context.getFailureDescription();
            }

            @Override
            public Stage getCurrentStage() {
                return context.getCurrentStage();
            }

            @Override
            public Resource createResource(PathAddress address) throws UnsupportedOperationException {
                return context.createResource(address);
            }

            @Override
            public ResultAction completeStep() {
                return context.completeStep();
            }

            @Override
            public void addStep(ModelNode response, ModelNode operation, OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(response, operation, step, stage);
            }

            @Override
            public void addStep(ModelNode operation, OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(operation, step, stage);
            }

            @Override
            public void addStep(OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(step, stage);
            }

            @Override
            public void acquireControllerLock() {
                context.acquireControllerLock();
            }
        };
    }

}
