/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    protected JbossDmrActions jbossDmrActions;

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
        jbossDmrActions = new JbossDmrActions(context);

        if (!jbossDmrActions.isDomainController()) {
            context.completeStep();
            return false;
        }
        return true;
    }

}
