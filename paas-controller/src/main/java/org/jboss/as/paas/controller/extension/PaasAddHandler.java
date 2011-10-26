package org.jboss.as.paas.controller.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.paas.controller.extension.deployment.PaasDeploymentProcessor;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class PaasAddHandler extends AbstractBoottimeAddStepHandler {

    public static final PaasAddHandler INSTANCE = new PaasAddHandler();

    private final Logger log = Logger.getLogger(PaasAddHandler.class);

    private PaasAddHandler() {
   	 System.out.println(">>>>>>>>>>> PaasAdd constructed.");
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

   	 System.out.println(">>>>>>>>>>> PaasAdd.populateModel");

   	 log.info("Populating the model");
        //model.setEmptyObject();

        //Initialize the 'type' child node
        model.get("provider").setEmptyObject();
        model.get("instance").setEmptyObject();

    }


    /** {@inheritDoc} */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

   	 System.out.println(">>>>>>>>>>> PaasAdd.performBoottime");

        //Add deployment processors here
        //Remove this if you don't need to hook into the deployers, or you can add as many as you like
        //see SubDeploymentProcessor for explanation of the phases
        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(PaasDeploymentProcessor.PHASE, PaasDeploymentProcessor.PRIORITY, new PaasDeploymentProcessor());

            }
        }, OperationContext.Stage.RUNTIME);
    }
}
