package org.jboss.as.paas.controller.operations;

import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorInstance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class OperationBase {

    protected String appName;

    protected DmrActionExecutor dmrActionExecutor;

    private JBossDmrActions jBossDmrActions;
    private PaasDmrActions paasDmrAction;

    public OperationBase() {
        this.dmrActionExecutor = DmrActionExecutorInstance.get();
    }

    public String getServerGroupName() {
        return getAppName();
    }

    public String getAppName() {
        return appName;
    }

    protected JBossDmrActions getJBossDmrActions() {
        if (jBossDmrActions == null) {
            jBossDmrActions = new JBossDmrActions();
        }

        return jBossDmrActions;
    }

    protected PaasDmrActions getPaasDmrActions() {
        if (paasDmrAction == null) {
            paasDmrAction = new PaasDmrActions();
        }
        return paasDmrAction;
    }
}
