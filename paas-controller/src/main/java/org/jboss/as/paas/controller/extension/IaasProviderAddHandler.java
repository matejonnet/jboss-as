/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProviderAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

    private static final Logger log = Logger.getLogger(IaasProviderAddHandler.class);

    public static final IaasProviderAddHandler INSTANCE = new IaasProviderAddHandler();

    private static final String ATTRIBUTE_DRIVER = "driver";
    private static final String ATTRIBUTE_URL = "url";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PASSWORD = "password";
    private static final String ATTRIBUTE_IMAGE_ID = "image-id";

    private IaasProviderAddHandler() {
        log.trace("IaasProviderAddHandler constructed.");
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        super.execute(context, operation);
        log.trace("IaasProviderAddHandler.execute");

        String providerName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String driver = operation.get(ATTRIBUTE_DRIVER).asString();
        String url = operation.get(ATTRIBUTE_URL).asString();
        String username = operation.get(ATTRIBUTE_USERNAME).asString();
        String password = operation.get(ATTRIBUTE_PASSWORD).asString();
        String imageId = operation.get(ATTRIBUTE_IMAGE_ID).asString();

        IaasController.getInstance().addProvider(providerName, driver, url, username, password, imageId);

    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        log.trace("IaasProviderAddHandler.getModelDescription.");

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

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        log.trace("IaasProviderAddHandler.populateModel");

        String driver = "";
        // Read the value from the operation
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
}
