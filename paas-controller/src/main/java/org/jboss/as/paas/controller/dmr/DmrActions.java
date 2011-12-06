/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class DmrActions {

    protected OperationContext context;

    public DmrActions(OperationContext context) {
        super();
        this.context = context;
    }

    private static final Logger log = Logger.getLogger(DmrActions.class);

    protected void executeStep(OperationContext context, ModelNode operation) {
        String opName = operation.get(ModelDescriptionConstants.OP).asString();
        OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, opName);
        try {
            if (log.isTraceEnabled()) log.trace("Executing oreration:" + operation);
            opStep.execute(context, operation);
            if (log.isTraceEnabled()) log.trace("Operation executed.");
        //} catch (OperationFailedException e) {
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            log.error("Can not execute operation [" + opName + "]", e);
        }
        context.completeStep();
    }

    /**
     * @param context
     * @param request
     */
    protected void addStepToContext(ModelNode request) {
        context.addStep(request, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);
    }

}
