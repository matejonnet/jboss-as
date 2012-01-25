package org.jboss.as.paas.controller.dmr.executor;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrActionExecutorInstance {

    private static DmrActionExecutor INSTANCE;

    private DmrActionExecutorInstance() {
        // TODO Auto-generated constructor stub
    }

    public static DmrActionExecutor get() {
        if (INSTANCE == null) {
            INSTANCE = new DmrActionExecutorRemoteClient();
        }
        return INSTANCE;
    }
}
