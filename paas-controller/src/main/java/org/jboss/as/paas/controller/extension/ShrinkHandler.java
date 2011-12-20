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
public class ShrinkHandler extends BaseHandler implements OperationStepHandler {
    public static final ShrinkHandler INSTANCE = new ShrinkHandler();
    public static final String OPERATION_NAME = "shrink";
    private static final String ATTRIBUTE_APP_NAME = "name";

    private final Logger log = Logger.getLogger(ShrinkHandler.class);

    private ShrinkHandler() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        //TODO validate required attributes
        //        if(appName == null) {
        //            throw new OperationFormatException("Required argument name are missing.");
        //        }

        PaasProcessor paasProcessor = new PaasProcessor();

        String serverGroupName = getServerGroupName(appName);
        paasProcessor.removeHostFromServerGroup(serverGroupName, context);

        context.completeStep();
    }


}
