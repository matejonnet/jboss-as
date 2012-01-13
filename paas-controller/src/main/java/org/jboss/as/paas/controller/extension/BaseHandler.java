/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.OperationStepRegistry;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.ResultMessagesDmrActions;
import org.jboss.as.paas.controller.operationqueue.DmrOperations;
import org.jboss.as.paas.controller.operationqueue.OperationQueue;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    protected JBossDmrActions jbossDmrActions;
    protected PaasDmrActions paasDmrActions;
    protected CompositeDmrActions compositeDmrActions;
    protected OperationStepRegistry stepRegistry;
    protected DmrOperations dmrOperations;
    protected ResultMessagesDmrActions messagesDmrActions;
    protected OperationQueue operationQueue;

    /**
     * @param appName
     * @return
     */
    public String getServerGroupName(String appName) {
        //return appName + "-SG";
        return appName;
    }

    /**
     * Returns false if execution is not on domain controller
     *
     * @param context
     */
    public boolean execute(OperationContext context) {
        System.out.println(">>>>>>>>> Handle.execute ");
        stepRegistry = new OperationStepRegistry();
        dmrOperations = new DmrOperations();
        jbossDmrActions = new JBossDmrActions(context, stepRegistry, dmrOperations);

        if (!jbossDmrActions.isDomainController()) {
            context.completeStep();
            return false;
        }

        paasDmrActions = new PaasDmrActions(context, stepRegistry, dmrOperations);
        compositeDmrActions = new CompositeDmrActions(context, jbossDmrActions, paasDmrActions, stepRegistry, dmrOperations, operationQueue);
        messagesDmrActions = new ResultMessagesDmrActions(context, stepRegistry);
        operationQueue = new OperationQueue();
        operationQueue.add(dmrOperations);
        return true;
    }

    protected void completeStep(OperationContext context) {
        messagesDmrActions.addMessgesToContext();
        context.completeStep();
    }

    /**
     * used to execute operation after context.completeStep
     * method should be called just before return of execute method
     */
    protected void onReturn() {
        operationQueue.executeAsync();
        //remoteDmrActions.executeOnComplete();
    }

}
