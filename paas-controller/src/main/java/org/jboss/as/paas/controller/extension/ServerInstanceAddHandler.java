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
public class ServerInstanceAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

    public static final ServerInstanceAddHandler INSTANCE = new ServerInstanceAddHandler();

    public static final String ATTRIBUTE_INSTANCE_IP = "ip";

    private ServerInstanceAddHandler() {
        System.out.println(">>>>>>>>>>> ServerInstanceAddHandler constructed.");
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        System.out.println(">>>>>>>>>>>>>> ServerInstanceAddHandler.execute");
        super.execute(context, operation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription
     * (java.util.Locale)
     */
    @Override
    public ModelNode getModelDescription(Locale locale) {
        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds a server instance");

        node.get(REQUEST_PROPERTIES, "provider", DESCRIPTION).set("Deltacloud IaaS provider name.");
        node.get(REQUEST_PROPERTIES, "provider", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "provider", REQUIRED).set(true);

        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss
     * .dmr.ModelNode, org.jboss.dmr.ModelNode)
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        System.out.println(">>>>>>>>>>> ServerInstanceAddHandler.populateModel");

        String provider = "";
        String ip = "";
        //TODO used typed server groups ?
        //String serverGroups = "";

        //Read the value from the operation
        if (operation.hasDefined("provider")) {
            provider = operation.get("provider").asString();
        }
        model.get("provider").set(provider);

        if (operation.hasDefined(ATTRIBUTE_INSTANCE_IP)) {
            ip = operation.get(ATTRIBUTE_INSTANCE_IP).asString();
        }
        model.get(ATTRIBUTE_INSTANCE_IP).set(ip);

        //      if (operation.hasDefined("serverGroups")) {
        //          ModelNode sererGroups = operation.get("serverGroups");
        //          model.get("serverGroups").set(sererGroups);
        //      } else {
        //          model.get("serverGroups").setEmptyList();
        //      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss
     * .as.controller.OperationContext, org.jboss.dmr.ModelNode,
     * org.jboss.dmr.ModelNode,
     * org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
     */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        System.out.println(">>>>>>>>>>>>>> ServerInstanceAddHandler.performRuntime");
        // TODO Auto-generated method stub
        //super.performRuntime(context, operation, model, verificationHandler, newControllers);
        //      String provider = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        //      //TODO add all parameters
        //      PaasService service = new PaasService(provider, model.get("driver").asString());
        //      ServiceName name = PaasService.createServiceName(provider);
        //      ServiceController<PaasService> controller = context.getServiceTarget()
        //            .addService(name, service)
        //            .addListener(verificationHandler)
        //            .setInitialMode(ServiceController.Mode.ACTIVE)
        //            .install();
        //      newControllers.add(controller);

    }

}
