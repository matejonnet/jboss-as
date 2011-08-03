/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import java.io.File;

import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployHandle implements OperationStepHandler {
    public static final DeployHandle INSTANCE = new DeployHandle();
    public static final String OPERATION_NAME = "deploy";
    private static final String ATTRIBUTE_PATH = "path";

    private final Logger log = Logger.getLogger(DeployHandle.class);
    private ModelControllerClient client;

    private DeployHandle() {}

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
        
        final String filePath = operation.get(ATTRIBUTE_PATH).asString();
        
        //TODO validate required attributes
        
        File f = new File(filePath);
        String appName = f.getName();
        
        createServerGroup(appName);
        
        //TODO create new thread to create new server instance and add deployment jobs to queue ?? can domain controlelr handle this without sleep ?
        addServerInstanceToGroup();
        
        deployApp(f, appName);
        
        context.completeStep();
    }
        
        
        
        
        
     /**
     * creates new server instance and add it to server group 
     */
    private void addServerInstanceToGroup() {
        // TODO Auto-generated method stub
        
        //
        
    }

        /**
     * @param file 
         * @param appName
     */
    private void deployApp(File file, String appName) {
        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        final ModelNode request;
        try {
            builder.operationName("deploy");
            builder.addProperty("", file.getAbsolutePath());
            builder.addProperty("server-groups", getServerGroupName(appName));
            request = builder.buildRequest();
        } catch (OperationFormatException e) {
            throw new IllegalStateException("Failed to build operation", e);
        }

        try {
            ModelNode outcome = client.execute(request);
            if (Util.isSuccess(outcome)) {
                //TODO handle error (print it and rollback)
            }
        } catch (Exception e) {
        }
    }

    /**
     * executes /server-group=[appName-SG]:add(profile=default, socket-binding-group=standard-sockets, socket-binding-port-offset=[portOffset])
     * @param appName
     */
    private void createServerGroup(String appName) {
                
        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        final ModelNode request;
        try {
            builder.operationName("/server-group=" + getServerGroupName(appName) + ":add");
            builder.addProperty("profile", "default");
            builder.addProperty("socket-binding-group", "standard-sockets");
            //TODO parametrize port offset, calculate it acording to deployment number per instance (1st=>0; 2nd=>100, ...)
            builder.addProperty("socket-binding-port-offset", "0");
            request = builder.buildRequest();
        } catch (OperationFormatException e) {
            throw new IllegalStateException("Failed to build operation", e);
        }

        try {
            ModelNode outcome = client.execute(request);
            if (Util.isSuccess(outcome)) {
                //TODO handle error (print it and rollback)
            }
        } catch (Exception e) {
        }

    }

    private String getServerGroupName(String appName) {
        return appName + "-SG";
    }

}
