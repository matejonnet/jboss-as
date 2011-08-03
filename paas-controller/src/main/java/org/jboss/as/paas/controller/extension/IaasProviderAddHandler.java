/**
 * 
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;

import java.util.List;
import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.PaasController;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProviderAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

    public static final IaasProviderAddHandler INSTANCE = new IaasProviderAddHandler();

    private IaasProviderAddHandler() {
        System.out.println(">>>>>>>>>>> IaasProviderAddHandler constructed.");
    }


    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        System.out.println(">>>>>>>>>>>>>> IaasProviderAddHandler.execute");
        super.execute(context, operation);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription(java.util.Locale)
     */
    @Override
    public ModelNode getModelDescription(Locale locale) {
        System.out.println(">>>>>>>>>>> IaasProviderAddHandler.getModelDescription.");

        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds a tracked deployment type");

        node.get(REQUEST_PROPERTIES, "driver", DESCRIPTION).set("Deltacloud IaaS provider driver");
        node.get(REQUEST_PROPERTIES, "driver", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "driver", REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, "url", DESCRIPTION).set("Deltacloud IaaS provider url");
        node.get(REQUEST_PROPERTIES, "url", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "url", REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, "username", DESCRIPTION).set("Deltacloud IaaS provider username");
        node.get(REQUEST_PROPERTIES, "username", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "username", REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, "password", DESCRIPTION).set("Deltacloud IaaS provider password");
        node.get(REQUEST_PROPERTIES, "password", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "password", REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, "image-id", DESCRIPTION).set("Deltacloud IaaS provider image id.");
        node.get(REQUEST_PROPERTIES, "image-id", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "image-id", REQUIRED).set(true);

        return node;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode)
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        System.out.println(">>>>>>>>>>> IaasProviderAddHandler.populateModel");

        String driver = "";
        //Read the value from the operation
        if (operation.hasDefined("driver")) {
            driver = operation.get("driver").asString();
        }
        model.get("driver").set(driver);

        String url = "";
        if (operation.hasDefined("url")) {
            url = operation.get("url").asString();
        }
        model.get("url").set(url);

        String username = "";
        if (operation.hasDefined("username")) {
            username = operation.get("username").asString();
        }
        model.get("username").set(username);

        String password = "";
        if (operation.hasDefined("password")) {
            password = operation.get("password").asString();
        }
        model.get("password").set(password);

        String imageId = "";
        if (operation.hasDefined("image-id")) {
            imageId = operation.get("image-id").asString();
        }
        model.get("image-id").set(imageId);

    }


//
//    /* (non-Javadoc)
//     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
//     */
//    @Override
//    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
//        System.out.println(">>>>>>>>>>>>>> IaasProviderAddHandler.performRuntime");
//        // TODO Auto-generated method stub
//        //super.performRuntime(context, operation, model, verificationHandler, newControllers);
//        String provider = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
//        //TODO add all parameters
//        PaasService service = new PaasService(provider, model.get("driver").asString());
//        ServiceName name = PaasService.createServiceName(provider);
//        ServiceController<PaasService> controller = context.getServiceTarget()
//                .addService(name, service)
//                .addListener(verificationHandler)
//                .setInitialMode(ServiceController.Mode.ACTIVE)
//                .install();
//        newControllers.add(controller);
//
//    }


}
