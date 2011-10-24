/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.List;
import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerGroupAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

   public static final ServerGroupAddHandler INSTANCE = new ServerGroupAddHandler();

   private ServerGroupAddHandler() {}


   @Override
   public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
       super.execute(context, operation);
   }

   /* (non-Javadoc)
    * @see org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription(java.util.Locale)
    */
   @Override
   public ModelNode getModelDescription(Locale locale) {
      ModelNode node = new ModelNode();
      node.get(DESCRIPTION).set("Adds a server group to instance.");

      node.get(REQUEST_PROPERTIES, "position", DESCRIPTION).set("Server group slot position.");
      node.get(REQUEST_PROPERTIES, "position", TYPE).set(ModelType.INT);
      node.get(REQUEST_PROPERTIES, "position", REQUIRED).set(true);

      return node;
   }

   /* (non-Javadoc)
    * @see org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode)
    */
   @Override
   protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

      int position = 0;

      //Read the value from the operation
      if (operation.hasDefined("position")) {
          position = operation.get("position").asInt();
      }
      model.get("position").set(position);
   }

   /* (non-Javadoc)
    * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
    */
   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
   }


}
