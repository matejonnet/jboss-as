/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.OperationStepRegistry;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    protected JBossDmrActions jbossDmrActions;
    protected PaasDmrActions paasDmrActions;
    protected CompositeDmrActions compositeDmrActions;
    protected OperationStepRegistry stepRegistry;

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
        jbossDmrActions = new JBossDmrActions(context, stepRegistry);

        if (!jbossDmrActions.isDomainController()) {
            context.completeStep();
            return false;
        }
        paasDmrActions = new PaasDmrActions(context, stepRegistry);
        //compositeDmrActions = new CompositeDmrActions(context, jbossDmrActions, paasDmrActions, stepRegistry);
        return true;
    }

}
