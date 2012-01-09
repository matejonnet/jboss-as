/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ExpandHandler extends BaseHandler implements OperationStepHandler {
    public static final ExpandHandler INSTANCE = new ExpandHandler();
    public static final String OPERATION_NAME = "expand";
    private static final String ATTRIBUTE_APP_NAME = "name";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";
    private static final String ATTRIBUTE_INSTANCE_ID = "instance-id";

    private final Logger log = Logger.getLogger(ExpandHandler.class);

    private ExpandHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;
        final String instanceId = operation.get(ATTRIBUTE_INSTANCE_ID).isDefined() ? operation.get(ATTRIBUTE_INSTANCE_ID).asString() : null;

        // TODO validate required attributes
        // if(appName == null) {
        // throw new
        // OperationFormatException("Required argument name are missing.");
        // }

        PaasProcessor paasProcessor = new PaasProcessor(context, stepRegistry);

        String serverGroupName = getServerGroupName(appName);

        paasProcessor.addHostToServerGroup(serverGroupName, provider, newInstance, instanceId);

        context.completeStep();
    }

}
