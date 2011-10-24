/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.net.MalformedURLException;
import java.util.Locale;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProviderAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

    public static final IaasProviderAddHandler INSTANCE = new IaasProviderAddHandler();

    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_DRIVER = "driver";
    private static final String ATTRIBUTE_URL = "url";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PASSWORD = "password";
    private static final String ATTRIBUTE_IMAGE_ID = "image-id";

    private IaasProviderAddHandler() {
        System.out.println(">>>>>>>>>>> IaasProviderAddHandler constructed.");
    }


    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        System.out.println(">>>>>>>>>>>>>> IaasProviderAddHandler.execute");
        super.execute(context, operation);

        //<iaas-provider provider="myprovider" driver="mock" url="http://localhost:3001/api" username="mockuser" password="mockpassword" image-id="i-12345"/>

        //String providerName = operation.get(ATTRIBUTE_PROVIDER).asString();
        String providerName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String driver = operation.get(ATTRIBUTE_DRIVER).asString();
        String url = operation.get(ATTRIBUTE_URL).asString();
        String username = operation.get(ATTRIBUTE_USERNAME).asString();
        String password = operation.get(ATTRIBUTE_PASSWORD).asString();
        String imageId = operation.get(ATTRIBUTE_IMAGE_ID).asString();

        try {
            IaasController.addProvider(providerName, driver, url, username, password, imageId, context);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DeltaCloudClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see org.jboss.as.controller.descriptions.DescriptionProvider#getModelDescription(java.util.Locale)
     */
    @Override
    public ModelNode getModelDescription(Locale locale) {
        System.out.println(">>>>>>>>>>> IaasProviderAddHandler.getModelDescription.");

        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds a tracked deployment type");

        node.get(REQUEST_PROPERTIES, ATTRIBUTE_DRIVER, DESCRIPTION).set("Deltacloud IaaS provider driver");
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_DRIVER, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_DRIVER, REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, ATTRIBUTE_URL, DESCRIPTION).set("Deltacloud IaaS provider url");
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_URL, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_URL, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, ATTRIBUTE_USERNAME, DESCRIPTION).set("Deltacloud IaaS provider username");
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_USERNAME, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_USERNAME, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, ATTRIBUTE_PASSWORD, DESCRIPTION).set("Deltacloud IaaS provider password");
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_PASSWORD, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_PASSWORD, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, ATTRIBUTE_IMAGE_ID, DESCRIPTION).set("Deltacloud IaaS provider image id.");
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_IMAGE_ID, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, ATTRIBUTE_IMAGE_ID, REQUIRED).set(false);

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
        if (operation.hasDefined(ATTRIBUTE_DRIVER)) {
            driver = operation.get(ATTRIBUTE_DRIVER).asString();
        }
        model.get(ATTRIBUTE_DRIVER).set(driver);

        String url = "";
        if (operation.hasDefined(ATTRIBUTE_URL)) {
            url = operation.get(ATTRIBUTE_URL).asString();
        }
        model.get(ATTRIBUTE_URL).set(url);

        String username = "";
        if (operation.hasDefined(ATTRIBUTE_USERNAME)) {
            username = operation.get(ATTRIBUTE_USERNAME).asString();
        }
        model.get(ATTRIBUTE_USERNAME).set(username);

        String password = "";
        if (operation.hasDefined(ATTRIBUTE_PASSWORD)) {
            password = operation.get(ATTRIBUTE_PASSWORD).asString();
        }
        model.get(ATTRIBUTE_PASSWORD).set(password);

        String imageId = "";
        if (operation.hasDefined(ATTRIBUTE_IMAGE_ID)) {
            imageId = operation.get(ATTRIBUTE_IMAGE_ID).asString();
        }
        model.get(ATTRIBUTE_IMAGE_ID).set(imageId);

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
