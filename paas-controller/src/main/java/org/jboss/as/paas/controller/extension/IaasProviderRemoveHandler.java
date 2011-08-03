/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;

import java.util.Locale;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.PaasController;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProviderRemoveHandler extends AbstractRemoveStepHandler implements DescriptionProvider {

   public static final IaasProviderRemoveHandler INSTANCE = new IaasProviderRemoveHandler();

   private IaasProviderRemoveHandler() {
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
       ModelNode node = new ModelNode();
       node.get(DESCRIPTION).set("Removes a IaaS provider.");
       return node;
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
//       String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
//       ServiceName name = PaasController.createServiceName(suffix);
//       context.removeService(name);
   }

}
