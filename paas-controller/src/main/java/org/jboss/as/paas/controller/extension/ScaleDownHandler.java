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
public class ScaleDownHandler extends BaseHandler implements OperationStepHandler {
    public static final ScaleDownHandler INSTANCE = new ScaleDownHandler();
    public static final String OPERATION_NAME = "scale-down";
    private static final String ATTRIBUTE_APP_NAME = "name";

    private final Logger log = Logger.getLogger(ScaleDownHandler.class);

    private ScaleDownHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        // TODO validate required attributes
        // if(appName == null) {
        // throw new
        // OperationFormatException("Required argument name are missing.");
        // }

        PaasProcessor paasProcessor = new PaasProcessor(context, jbossDmrActions, paasDmrActions, compositeDmrActions);

        String serverGroupName = getServerGroupName(appName);
        paasProcessor.removeHostFromServerGroup(serverGroupName);

        completeStep(context);

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

            return node;
        }
    };
}
