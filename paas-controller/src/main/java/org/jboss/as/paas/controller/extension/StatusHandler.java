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
import org.jboss.as.paas.controller.operations.StatusOperation;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class StatusHandler extends BaseHandler implements OperationStepHandler {

    public static final StatusHandler INSTANCE = new StatusHandler();
    public static final String OPERATION_NAME = "status";
    private static final String ATTRIBUTE_APP_NAME = "app-name";

    private StatusHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode request) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        final String appName = request.get(ATTRIBUTE_APP_NAME).isDefined() ? request.get(ATTRIBUTE_APP_NAME).asString() : null;

        StatusOperation status = new StatusOperation();

        ModelNode result = new ModelNode();
        result.setEmptyObject();
        if (appName == null) {
            status.getAppStatus(result);
        } else {
            status.getAppStatus(result, appName);
        }

        status.getInstancesStatus(result);

        context.getResult().set(result);
        context.completeStep();

    }

    public static DescriptionProvider DESC = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {

            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Deploy application.");

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, DESCRIPTION).set("Application name.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_APP_NAME, REQUIRED).set(false);

            return node;
        }
    };
}
