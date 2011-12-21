/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationContext.Stage;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class InstanceProviderHandler implements OperationStepHandler {

    public static final InstanceProviderHandler INSTANCE = new InstanceProviderHandler();

    private InstanceProviderHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        // Update the model
        final String id = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        final long provider = operation.require("value").asLong();
        ModelNode node = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel();
        node.get("provider").set(provider);

        // Add a step to perform the runtime update
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                // TODO implement
                // TrackerService service =
                // (TrackerService)context.getServiceRegistry(true).getRequiredService(TrackerService.createServiceName(suffix)).getValue();
                // service.setTick(tick);
                // context.completeStep();
            }
        }, Stage.RUNTIME);
        context.completeStep();
    }
}
