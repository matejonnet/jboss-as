package org.jboss.as.domain.controller.modules;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationContext.ResultAction;
import org.jboss.as.controller.OperationContext.Stage;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.domain.controller.operations.deployment.DeploymentAddHandler;
import org.jboss.as.repository.ContentRepository;
import org.jboss.dmr.ModelNode;

public class ModularDeploymentAddHandler implements OperationStepHandler {

    private final DeploymentAddHandler deploymentAddHandler;
    private final ContentRepository contentRepository;

    public ModularDeploymentAddHandler(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
        deploymentAddHandler = new DeploymentAddHandler(contentRepository);
    }

    @Override
    public void execute(OperationContext context, ModelNode operation)
            throws OperationFailedException {
        OperationStepHandler handler = new ModularDeployerHandler(contentRepository);
        context.addStep(operation, handler, Stage.MODEL);

        deploymentAddHandler.execute(context, operation);
        context.completeStep(new OperationContext.ResultHandler() {
            @Override
            public void handleResult(ResultAction resultAction, OperationContext context, ModelNode operation) {
                if (resultAction == ResultAction.KEEP) {
                    //TODO
                }
            }
        });
    }

}
