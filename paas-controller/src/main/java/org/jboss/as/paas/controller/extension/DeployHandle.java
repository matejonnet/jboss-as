/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.OperationRequestAddress;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestAddress;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.threads.AsyncFuture;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployHandle implements OperationStepHandler {
    public static final DeployHandle INSTANCE = new DeployHandle();
    public static final String OPERATION_NAME = "deploy";
    private static final String ATTRIBUTE_PATH = "path";

    private final Logger log = Logger.getLogger(DeployHandle.class);

    private DeployHandle() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        
        final String filePath = operation.get(ATTRIBUTE_PATH).asString();
        
        //TODO validate required attributes
        
        File f = new File(filePath);
        String appName = f.getName();
        
        createServerGroup(appName, context);
        
        //TODO create new thread to create new server instance and add deployment jobs to queue ?? can domain controller handle this without sleep ?
//        addServerInstanceToGroup(appName);
//        
//        deployApp(f, appName);
        
        context.completeStep();
    }
        
        
     /**
     * - creates new server instance 
     * - join it to domain controller
     * - associate it with server group
     * - start the server instance
     *  
     * @param appName 
     */
    private void addServerInstanceToGroup(String appName) {
        // TODO Auto-generated method stub
        
        //
        
    }

        /**
     * @param file 
         * @param appName
     */
//    private void deployApp(File file, String appName) {
//        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
//        final ModelNode request;
//        try {
//            builder.operationName("deploy");
//            builder.addProperty("", file.getAbsolutePath());
//            builder.addProperty("name", appName);
//            builder.addProperty("server-groups", getServerGroupName(appName));
//            request = builder.buildRequest();
//        } catch (OperationFormatException e) {
//            throw new IllegalStateException("Failed to build operation", e);
//        }
//
//        try {
//            ModelNode outcome = client.execute(request);
//            if (Util.isSuccess(outcome)) {
//                //TODO handle error (print it and rollback)
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * executes /server-group=[appName-SG]:add(profile=default, socket-binding-group=standard-sockets, socket-binding-port-offset=[portOffset])
     * @param appName
     * @param context 
     */
    private void createServerGroup(final String appName, OperationContext context) {
        //final ModelNode request = new ModelNode();
      final ModelNode request;
      
      try {
          DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
          builder.addNode("server-group", getServerGroupName(appName));
          builder.operationName("add");
          builder.addProperty("profile", "default");
          builder.addProperty("socket-binding-group", "standard-sockets");
          //TODO parametrize port offset, calculate it acording to deployment number per instance (1st=>0; 2nd=>100, ...)
          builder.addProperty("socket-binding-port-offset", "0");
          request = builder.buildRequest();


        context.addStep(request, new OperationStepHandler() {
//        context.addStep(context.getResult(), request, new OperationStepHandler() {

            public void execute(OperationContext context, ModelNode operation) {

                String operationName = "add";
                OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, operationName);
//OpStep = null                OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, null);
//                PathAddress address = PathAddress.pathAddress(PathElement.pathElement("server-group", getServerGroupName(appName)));
//                OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(address, operationName);


                try {  
//                    final ModelNode request;
//                    DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
//                    builder.addNode("server-group", getServerGroupName(appName));
//                    builder.operationName("add");
//                    builder.addProperty("profile", "default");
//                    builder.addProperty("socket-binding-group", "standard-sockets");
//                    //TODO parametrize port offset, calculate it acording to deployment number per instance (1st=>0; 2nd=>100, ...)
//                    builder.addProperty("socket-binding-port-offset", "0");
//                    request = builder.buildRequest();
//                    opStep.execute(context, request);
                    opStep.execute(context, operation);
                    
                    //TODO set outcome success
                    
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                
                context.completeStep();
            }
        }, OperationContext.Stage.IMMEDIATE);

        
        
      } catch (OperationFormatException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
      }

        
        
        
        
        
        
        
//        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
//        final ModelNode request;
//        try {
//            builder.addNode("server-group", getServerGroupName(appName));
//            builder.operationName("add");
//            builder.addProperty("profile", "default");
//            builder.addProperty("socket-binding-group", "standard-sockets");
//            //TODO parametrize port offset, calculate it acording to deployment number per instance (1st=>0; 2nd=>100, ...)
//            builder.addProperty("socket-binding-port-offset", "0");
//            request = builder.buildRequest();
//        } catch (OperationFormatException e) {
//            throw new IllegalStateException("Failed to build operation", e);
//        }
//
//        try {
//          
//            ModelNode outcome = client.execute(request);
//            //ModelNode outcome = client.execute(request, OperationMessageHandler.logging);
//            
////            OperationBuilder op = new OperationBuilder(request);
////            ModelNode outcome = client.execute(op.build());
//
//            //AsyncFuture<ModelNode> future = client.executeAsync(request, OperationMessageHandler.logging);
//            //Future<ModelNode> future = client.executeAsync(request, OperationMessageHandler.logging);
//            //ModelNode outcome = future.get(5, TimeUnit.SECONDS);
//            //ModelNode outcome = future.get();
//            
//            if (Util.isSuccess(outcome)) {
//                //TODO handle error (print it and rollback)
//                context.getFailureDescription().set("Cannot create server group.");
//            }
//            
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        
        
        
        
//        try {
//            builder.operationName("read-children-names");
//            builder.addProperty("child-type", "deployment");
//            request = builder.buildRequest();
//        } catch (OperationFormatException e) {
//            throw new IllegalStateException("Failed to build operation", e);
//        }
//
//        try {
//            ModelNode outcome = client.execute(request);
//            if (Util.isSuccess(outcome)) {
//                
//                List<String> deployments = Util.getList(outcome);
//                
//                ModelNode deploymentsNode = new ModelNode();
//                for (String deployment : deployments) {
//                    deploymentsNode.add(deployment);
//                }
//
//                
//                context.getResult().set("deployments", deploymentsNode);
//            }
//        } catch (Exception e) {
//        }        
        
        
        
        
        
        
    }

    private String getServerGroupName(String appName) {
        return appName + "-SG";
    }

}
