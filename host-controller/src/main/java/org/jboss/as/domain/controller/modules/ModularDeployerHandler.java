package org.jboss.as.domain.controller.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationContext.Stage;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.repository.ContentRepository;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.dmr.ModelNode;
import org.jboss.vfs.VirtualFile;

class ModularDeployerHandler implements OperationStepHandler {

    private final ContentRepository contentRepository;
    private final AtomicInteger portOffset;

    public ModularDeployerHandler(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;

        portOffset = new AtomicInteger();
    }

    @Override
    public void execute(OperationContext context, ModelNode operation)
            throws OperationFailedException {

        File deployment = null;

        List<ModelNode> content = operation.get("content").asList();
        ModelNode contentHashNode = getListValue(content, "hash");
        if (contentHashNode.isDefined()) {
            byte[] contentHash = contentHashNode.asBytes();
            VirtualFile vfDeployment = contentRepository.getContent(contentHash);
            try {
                deployment = vfDeployment.getPhysicalFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (deployment != null) {
            File extractedEar = new File("/tmp/ear"); //TODO
            //extractedEar.delete();
            FileUtil.recursiveDelete(extractedEar);
            extractedEar.mkdir();

            try {
                FileUtil.extractArchive(deployment, extractedEar);
                if (isModularDeployment(extractedEar)) {
                    doExecute(context, extractedEar);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        context.stepCompleted();
    }

    private void doExecute(OperationContext context, File extractedEar) {
        //todo war location
        try {
            File warsDestination = new File("/tmp/wars"); //TODO
            FileUtil.recursiveDelete(warsDestination);
            warsDestination.mkdir();

            EarExtractor earExtractor = new EarExtractor(extractedEar, warsDestination);

            addDeployModulesOperations(earExtractor.getWebModules(), context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ModelNode getListValue(List<ModelNode> list, String key) {
        for (ModelNode modelNode : list) {
            ModelNode obj = modelNode.get(key);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    private void addDeployModulesOperations(List<Module> modules, OperationContext context) throws Exception {
        //ManagementResourceRegistration registration = context.getResourceRegistrationForUpdate();
        ImmutableManagementResourceRegistration registration = context.getRootResourceRegistration();

        for (Module module : modules) {
            ModelNode opCreateSG = createServerGroupOperation(module.name());
            addOperation(opCreateSG, registration, context);
            //TODO host selection
            String host = "master";
            ModelNode opAddServerToSG = addServerToGroupOperation(module.name(), host);
            addOperation(opAddServerToSG, registration, context);

            ModelNode opDeployToSG = deployToServerGroupOperation(module);
            addOperation(opDeployToSG, registration, context);
        }
    }

    private int getPortOffset() {
        return portOffset.getAndAdd(100);
    }

    private void addOperation(ModelNode operation,
            ImmutableManagementResourceRegistration registration,
            OperationContext context) {
        OperationStepHandler handler = operationHandler(operation, registration);
        context.addStep(operation, handler, Stage.MODEL);
    }

    private OperationStepHandler operationHandler(ModelNode op,
            ImmutableManagementResourceRegistration registration) {
        PathAddress pathAddress = PathAddress.pathAddress(Operations.getOperationAddress(op));
        String operationName = Operations.getOperationName(op);
        return registration.getOperationHandler(pathAddress, operationName);
    }

    private ModelNode createServerGroupOperation(String name) throws DeploymentUnitProcessingException {
        // /server-group=newg:add(profile=default,socket-binding-group=standard-sockets,socket-binding-port-offset=)
        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(ClientConstants.SERVER_GROUP, name));

        ModelNode add = Operations.createAddOperation(address.toModelNode());
        add.get("profile").set("default");
        add.get(ClientConstants.SOCKET_BINDING_GROUP).set("standard-sockets");
        add.get("socket-binding-port-offset").set(getPortOffset());
        return add;
    }

    private ModelNode addServerToGroupOperation(String name, String host) throws DeploymentUnitProcessingException {
        // /host=master/server-config=server-01:add(auto-start=, group=)
        PathAddress address = PathAddress.pathAddress(
                PathElement.pathElement(ClientConstants.HOST, host),
                PathElement.pathElement(ClientConstants.SERVER_CONFIG, name));

        ModelNode add = Operations.createAddOperation(address.toModelNode());
        add.get(ClientConstants.GROUP).set(name);
        add.get(ClientConstants.AUTO_START).set(true);
        return add;
    }

    private ModelNode deployToServerGroupOperation(Module module) throws IOException {
        ModelNode deploy = Operations.createCompositeOperation();
        ModelNode steps = deploy.get(ClientConstants.STEPS);
        steps.add(deploymentContentOperation(module));
        steps.add(deployOperation(module.name()));
        return deploy;
    }

    private ModelNode deployOperation(String name) {
        PathAddress address = PathAddress.pathAddress(
                PathElement.pathElement(ClientConstants.SERVER_GROUP, name),
                PathElement.pathElement(ClientConstants.DEPLOYMENT, name));
        ModelNode add = Operations.createAddOperation(address.toModelNode());
        ModelNode deploy = Operations.createOperation(ClientConstants.DEPLOYMENT_DEPLOY_OPERATION, address.toModelNode());

        ModelNode composite = Operations.createCompositeOperation();
        ModelNode steps = composite.get(ClientConstants.STEPS);
        steps.add(add);
        steps.add(deploy);

        return composite;
    }

    private ModelNode deploymentContentOperation(Module module) throws FileNotFoundException {
        /*{
        "steps" => [
            {
                "operation" => "add",
                "address" => {"deployment" => "hello-servlet-noEJBnoDist.war"},
                "content" => [{"input-stream-index" => 0}]
            },
        ]
        }*/

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(ClientConstants.DEPLOYMENT, module.name()));
        ModelNode add = Operations.createAddOperation(address.toModelNode());
//        OperationBuilder builder = OperationBuilder.create(add, true);
//        builder.addInputStream(module.getInputStream());
//        add.get(ClientConstants.CONTENT).get(0).get(ClientConstants.INPUT_STREAM_INDEX).set(0);
//        steps.add(builder.build().getOperation());

        add.get(ClientConstants.CONTENT).get(0).get(ClientConstants.PATH).set(module.getWarArchive().getAbsolutePath());
        add.get(ClientConstants.CONTENT).get(0).get("archive").set(true);
        return add;
    }

    private boolean isModularDeployment(File extractedEar) throws IOException {
        //File appEngineApp = extractedEar.getChild("META-INF/appengine-application.xml").getPhysicalFile();
        File appEngineApp = new File(extractedEar, "META-INF/appengine-application.xml");
        return appEngineApp.isFile();
    }


}