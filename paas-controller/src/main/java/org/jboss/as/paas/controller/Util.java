/**
 *
 */
package org.jboss.as.paas.controller;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.extension.DeployHandle;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Util {

    private static final Logger log = Logger.getLogger(Util.class);

    public static void executeStep(OperationContext context, ModelNode operation) {
        String opName = operation.get(ModelDescriptionConstants.OP).asString();
        OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, opName);
        try {
            opStep.execute(context, operation);
        } catch (OperationFailedException e) {
            // TODO Auto-generated catch block
            log.error("Can not execute operation [" + opName + "]", e);
        }
        context.completeStep();
    }
}
