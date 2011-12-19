/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
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

    private final Logger log = Logger.getLogger(ExpandHandler.class);

    private ExpandHandler() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        CompositeDmrActions compositeDmrActions = new CompositeDmrActions(context);

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;
//TODO validate required attributes
//        if(appName == null) {
//            throw new OperationFormatException("Required argument name are missing.");
//        }

        PaasProcessor paasProcessor = new PaasProcessor();

        InstanceSlot slot = paasProcessor.getSlot(newInstance, getServerGroupName(appName), context, provider);
        compositeDmrActions.addHostToServerGroup(slot, getServerGroupName(appName));

        context.completeStep();
    }


}
