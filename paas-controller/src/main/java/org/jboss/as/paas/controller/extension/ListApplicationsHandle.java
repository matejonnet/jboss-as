/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.paas.controller.deployment.PaasDeploymentProcessor;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.logmanager.Level;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ListApplicationsHandle implements OperationStepHandler {
    public static final ListApplicationsHandle INSTANCE = new ListApplicationsHandle();
    public static final String OPERATION_NAME = "list-applications";

    private final Logger log = Logger.getLogger(ListApplicationsHandle.class);
    private ModelControllerClient client;

    private ListApplicationsHandle() {
     System.out.println(">>>>>>>>>>> PaasAdd constructed.");
    }

    public void init(ModelControllerClient client) {
        this.client = client;
    }
    
    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (client == null) {
            ModelNode error = new ModelNode();
            error.set("ModelControllerClient is null. Singleton must be initialized.");
            throw new OperationFailedException(error);
        }
        
        //final ModelNode model = context.readModelForUpdate(PathAddress.EMPTY_ADDRESS);
            //final String level = operation.get(CommonAttributes.LEVEL).asString();
            //model.get(CommonAttributes.ROOT_LOGGER, CommonAttributes.LEVEL).set(level);

//            if (context.getType() == OperationContext.Type.SERVER) {
//                context.addStep(new OperationStepHandler() {
//                    public void execute(OperationContext context, ModelNode operation) {
//                        final ServiceRegistry serviceRegistry = context.getServiceRegistry(false);
//                        final ServiceController<Logger> controller = (ServiceController<Logger>) serviceRegistry.getService(LogServices.ROOT_LOGGER);
//                        if (controller != null) {
//                            controller.getValue().setLevel(Level.parse(level));
//                        }
//                        context.completeStep();
//                    }
//                }, OperationContext.Stage.RUNTIME);
//            }
        
        List<String> deployments = Util.getDeployments(client);
        
        ModelNode deploymentsNode = new ModelNode();
        for (String deployment : deployments) {
            deploymentsNode.add(deployment);
        }
        //context.getResult().setEmptyList();
        context.getResult().set("deployments", deploymentsNode);
        
//            ModelNode result = operation.get("result");
//            if (result==null) {
//                result = new ModelNode();
//                operation.set(result);
//            }
//            result.add("a", "1111");
//            result.add("b", "2222");

        context.completeStep();
    }
}
