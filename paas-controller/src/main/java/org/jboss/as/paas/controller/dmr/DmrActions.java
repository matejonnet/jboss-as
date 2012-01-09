/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrActions {

    protected OperationContext context;
    protected OperationStepRegistry stepRegistry;

    public DmrActions(OperationContext context) {
        super();
        this.context = context;
    }

    public DmrActions(OperationContext context, OperationStepRegistry stepRegistry) {
        super();
        this.context = context;
        this.stepRegistry = stepRegistry;
    }

    private static final Logger log = Logger.getLogger(DmrActions.class);

    protected void executeStep(OperationContext context, ModelNode operation) {
        doExecuteStep(context, operation);
        //XXX context.completeStep();
    }

    protected void executeStep(OperationContext context, ModelNode operation, String stepName, String... required) {
        if (required != null && required.length > 0) {
            if (!stepRegistry.areExecuted(required)) {
                log.debugf("Skipping execution of %s step.", stepName);
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

        //context.getResult().add(operation);
        //XXX context.completeStep();

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

    public void addStepToContext(ModelNode request, final String stepName) {
        addStepToContext(request, OperationContext.Stage.MODEL, stepName, null);
    }

    public void addStepToContext(ModelNode request, final String stepName, String[] requiredSteps) {
        addStepToContext(request, OperationContext.Stage.MODEL, stepName, requiredSteps);
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

    public void addStepToContext(ModelNode request, OperationContext.Stage opStage, final String stepName, final String[] requiredSteps) {
        log.tracef("Adding step to context %s.", request.toString());
        context.addStep(request, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                executeStep(context, operation, stepName, requiredSteps);
            }
        }, opStage);
    }

    protected Resource navigate(PathAddress address) {
        Resource rootResource = context.getRootResource();
        log.tracef("Navigating from rootResource to [%s] ...", address.toString());
        return rootResource.navigate(address);
    }

}
