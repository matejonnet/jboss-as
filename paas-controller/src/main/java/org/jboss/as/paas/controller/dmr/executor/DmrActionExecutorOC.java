/**
 *
 */
package org.jboss.as.paas.controller.dmr.executor;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.dmr.OperationStepEntry;
import org.jboss.as.paas.controller.dmr.OperationStepRegistry;
import org.jboss.as.paas.controller.operationqueue.DmrOperations;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * Executes dmr operations using operation context
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrActionExecutorOC implements DmrActionExecutor {

    protected OperationContext context;
    protected OperationStepRegistry stepRegistry;
    protected DmrOperations dmrOperations;

    public DmrActionExecutorOC(OperationContext context) {
        super();
        this.context = context;
    }

    public DmrActionExecutorOC(OperationContext context, OperationStepRegistry stepRegistry) {
        super();
        this.context = context;
        this.stepRegistry = stepRegistry;
    }

    public DmrActionExecutorOC(OperationContext context, OperationStepRegistry stepRegistry, DmrOperations dmrOperations) {
        super();
        this.context = context;
        this.stepRegistry = stepRegistry;
        this.dmrOperations = dmrOperations;
    }

    private static final Logger log = Logger.getLogger(DmrActionExecutorOC.class);

    protected void executeStep(OperationContext context, ModelNode operation) {
        doExecuteStep(context, operation);
        context.completeStep();
    }

    protected void executeStep(OperationContext context, ModelNode operation, String stepName, String[] required, String onSuccess) {
        if (required != null && required.length > 0) {
            if (!stepRegistry.areExecuted(required)) {
                log.debugf("Skipping execution of %s step.", stepName);
                context.completeStep();
                return;
            }
        }
        log.tracef("Executing step %s ...", stepName);

        doExecuteStep(context, operation);

        //ModelNode result = context.getResult(); //undefined
        //String failure = context.getFailureDescription().asString(); //causes failure

        if (!context.hasFailureDescription()) {
            log.tracef("Step %s executed successfully.", stepName);
            stepRegistry.addExecuted(stepName);
        } else {
            log.tracef("Step %s did not execute successfully. Failure [%s].", stepName, context.getFailureDescription());
            stepRegistry.addFailed(stepName);
        }

        if (onSuccess != null && !context.hasFailureDescription()) {
            OperationStepEntry next = stepRegistry.getFromQueue(onSuccess);
            executeNextStep(next);
        }

        //context.getResult().add(operation);
        context.completeStep();

        /*fails
            if (required != null && required.length > 0) {
                if (!stepRegistry.areExecuted(required)) {
                    log.debugf("Skipping execution of %s step.", stepName);
                    return;
                }
            }
            log.tracef("Executing step %s ...", stepName);

            doExecuteStep(context, operation);

            //ModelNode result = context.getResult();
            //if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
            if ("undefined".equals(context.getFailureDescription().asString())) {
                log.tracef("Step %s executed successfully.", stepName);
                stepRegistry.addExecuted(stepName);
            } else {
                //log.tracef("Step %s did not execute success. Result [%s].", stepName, result);
                log.tracef("Step %s did not execute successfully. Failure [%s].", stepName, context.getFailureDescription());
                stepRegistry.addFailed(stepName);
            }
            //context.getResult().add(operation);
            context.completeStep();
         */

    }

    protected void doExecuteStep(OperationContext context, ModelNode operation) {
        String opName = operation.get(ModelDescriptionConstants.OP).asString();
        OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, opName);
        //        PathAddress stepAddress = PathAddress.pathAddress(operation.get(OP_ADDR));
        //        OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(stepAddress, opName); //err: Duplicate path element
        try {
            if (log.isTraceEnabled())
                log.tracef("Executing oreration: %s", operation);

            opStep.execute(context, operation);

            if (log.isTraceEnabled())
                log.tracef("Operation executed %s.", operation);
            // } catch (OperationFailedException e) {
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            log.error("Can not execute operation [" + opName + "]", e);
        }
    }

    public void addStepToContext(ModelNode request) {
        addStepToContext(request, OperationContext.Stage.MODEL);
    }

    public void addStepToContext(ModelNode request, OperationContext.Stage opStage) {
        log.tracef("Adding step to context %s.", request.toString());
        context.addStep(request, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStep(context, operation);
            }
        }, opStage);
    }

    /*public void addStepToContext(ModelNode request, final String stepName) {
        addStepToContext(request, OperationContext.Stage.MODEL, stepName, null);
    }*/

    public void addStepToContext(ModelNode request, final String stepName, String onSuccess) {
        addStepToContext(request, OperationContext.Stage.MODEL, stepName, null, onSuccess);
    }

    public void process(ModelNode request, final String stepName, final String[] requiredSteps) {
        addStepToContext(request, OperationContext.Stage.MODEL, stepName, requiredSteps);
    }

    public void addStepToContext(ModelNode request, OperationContext.Stage opStage, final String stepName, final String[] requiredSteps) {
        log.tracef("Adding step %s to context.", request.toString());
        context.addStep(request, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStep(context, operation, stepName, requiredSteps, null);
            }
        }, opStage);
    }

    public void addStepToContext(ModelNode request, OperationContext.Stage opStage, final String stepName, final String[] requiredSteps, String onSuccess) {
        log.tracef("Adding step to queue %s.", request.toString());

        OperationStepEntry ose = new OperationStepEntry(request, opStage, stepName, onSuccess);
        stepRegistry.addToQueue(stepName, ose);

    }

    protected void executeNextStep(final OperationStepEntry step) {
        context.addStep(step.getRequest(), new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStep(context, operation, step.getStepName(), null, step.getOnSuccess());
            }
        }, step.getOpStage());
    }

    @Override
    public void execute(ModelNode op) {
        // TODO Auto-generated method stub

    }

    @Override
    public ModelNode executeForResult(ModelNode op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor#executeForResult(org.jboss.as.controller.client.Operation)
     */
    @Override
    public ModelNode executeForResult(Operation build) {
        // TODO Auto-generated method stub
        return null;
    }
}
