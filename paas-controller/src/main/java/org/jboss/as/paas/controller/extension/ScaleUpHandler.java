/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ScaleUpHandler extends BaseHandler implements OperationStepHandler {
    public static final ScaleUpHandler INSTANCE = new ScaleUpHandler();
    public static final String OPERATION_NAME = "scale-up";
    private static final String ATTRIBUTE_APP_NAME = "name";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";
    private static final String ATTRIBUTE_INSTANCE_ID = "instance-id";

    private final Logger log = Logger.getLogger(ScaleUpHandler.class);

    private ScaleUpHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).isDefined() ? operation.get(ATTRIBUTE_PROVIDER).asString() : null;
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;
        final String instanceId = operation.get(ATTRIBUTE_INSTANCE_ID).isDefined() ? operation.get(ATTRIBUTE_INSTANCE_ID).asString() : null;

        PaasProcessor paasProcessor = new PaasProcessor(context, jbossDmrActions, paasDmrActions, compositeDmrActions);

        String serverGroupName = getServerGroupName(appName);

        paasProcessor.addHostToExisingServerGroup(serverGroupName, provider, newInstance, instanceId);

        completeStep(context);

        if (stepRegistry.areExecuted(new String[] { "validateHostRegistration" })) {
            jbossDmrActions.reloadHost(paasProcessor.getSlot().getHostIP());
        }

        onReturn();
    }

    public static DescriptionProvider DESC = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {

            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Deploy application.");

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, DESCRIPTION).set("Application name.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, REQUIRED).set(true);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, DESCRIPTION).set("Provider name.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, REQUIRED).set(false);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, DESCRIPTION).set("Force new instance creation. Default: false");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, REQUIRED).set(false);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, DESCRIPTION).set("Host where to look for free slot.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, REQUIRED).set(false);

            return node;
        }
    };
}
