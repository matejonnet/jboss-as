/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.registry.Resource;
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

        final ModelNode request;

        try {
            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();

            
            builder.operationName("read-children-names");
            builder.addProperty("child-type", "deployment");
            request = builder.buildRequest();

            context.addStep(request, new OperationStepHandler() {
                public void execute(OperationContext context, ModelNode operation) {

                    String operationName = "read-children-names";
                    OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, operationName);

                    try {
                        opStep.execute(context, operation);
                    } catch (OperationFailedException e) {
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
        
        context.completeStep();
    }
}
