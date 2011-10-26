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
public class ProviderDriverHandler implements OperationStepHandler {

	public static final ProviderDriverHandler INSTANCE = new ProviderDriverHandler(); 
	
	private ProviderDriverHandler() {
	}
	
	@Override
	public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
      //Update the model
      final String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
      final long driver = operation.require("value").asLong();
      ModelNode node = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel();
      node.get("driver").set(driver);

      //Add a step to perform the runtime update
      context.addStep(new OperationStepHandler() {
          @Override
          public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
         	 //TODO implement
//              TrackerService service = (TrackerService)context.getServiceRegistry(true).getRequiredService(TrackerService.createServiceName(suffix)).getValue();
//              service.setTick(tick);
//              context.completeStep();
          }
      }, Stage.RUNTIME);
      context.completeStep();
	}

}
