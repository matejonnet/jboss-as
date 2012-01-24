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
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerInstanceAddHandler extends AbstractAddStepHandler implements DescriptionProvider {

    public static final ServerInstanceAddHandler INSTANCE = new ServerInstanceAddHandler();

    public static final String ATTRIBUTE_INSTANCE_IP = "ip";

    private ServerInstanceAddHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        super.execute(context, operation);
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds a server instance");

        node.get(REQUEST_PROPERTIES, "provider", DESCRIPTION).set("Deltacloud IaaS provider name.");
        node.get(REQUEST_PROPERTIES, "provider", TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, "provider", REQUIRED).set(true);

        return node;
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        System.out.println(">>>>>>>>>>> ServerInstanceAddHandler.populateModel");

        String provider = "";
        String ip = "";
        //Read the value from the operation
        if (operation.hasDefined("provider")) {
            provider = operation.get("provider").asString();
        }
        model.get("provider").set(provider);

        if (operation.hasDefined(ATTRIBUTE_INSTANCE_IP)) {
            ip = operation.get(ATTRIBUTE_INSTANCE_IP).asString();
        }
        model.get(ATTRIBUTE_INSTANCE_IP).set(ip);

    }

}
