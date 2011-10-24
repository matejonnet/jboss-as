/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.paas.controller.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ExpandHandle implements OperationStepHandler {
    public static final ExpandHandle INSTANCE = new ExpandHandle();
    public static final String OPERATION_NAME = "expand";
    private static final String ATTRIBUTE_APP_NAME = "name";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";

    private final Logger log = Logger.getLogger(ExpandHandle.class);

    private ExpandHandle() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!Util.isDomainController(context)) {
            context.completeStep();
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;
//TODO validate required attributes
//        if(appName == null) {
//            throw new OperationFormatException("Required argument name are missing.");
//        }

        Util.addHostToServerGroup(newInstance, provider, context, Util.getServerGroupName(appName));



        context.completeStep();
    }


}
