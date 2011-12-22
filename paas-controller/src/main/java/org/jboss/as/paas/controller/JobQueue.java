package org.jboss.as.paas.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.DmrActions;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JobQueue {
    private static final Logger log = Logger.getLogger(JobQueue.class);

    private List<ModelNode> queue = new ArrayList<ModelNode>();

    /**
     * @param job
     */
    public void add(ModelNode job) {
        queue.add(job);
        log.tracef("Job %s added to queue.", job.toString());
    }

    private void executeQueue(OperationContext context) {
        DmrActions dmr = new DmrActions(context);
        for (ModelNode job : queue) {
            dmr.addStepToContext(job);
            log.tracef("Job %s added to context.", job.toString());
        }
        context.completeStep();
        log.debug("context.completeStep");
    }

    /**
     * @param context
     */
    public void executeAsync(final OperationContext context, final String hostIp) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
        final Future future = es.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                waitNewHostToRegisterToDC(context, hostIp);
                executeQueue(context);
                return null;
            }
        });
    }

    /**
     * @param hostIp
     * @param context
     *
     */
    private void waitNewHostToRegisterToDC(OperationContext context, String hostIp) {
        // TODO make configurable
        int maxWaitTime = 30000; // 30sec
        long started = System.currentTimeMillis();

        // wait remote as to register
        while (true) {
            boolean registred = false;

            try {
                // try to navigate to host, to see if it is already registered
                JbossDmrActions jbossDmrActions = new JbossDmrActions(context);
                jbossDmrActions.navigateToHostName(hostIp);
                registred = true;
            } catch (NoSuchElementException e) {}

            if (registred) {
                break;
            }

            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Instance hasn't registered in " + maxWaitTime / 1000 + " seconds.");
            }
            try {
                log.debug("Waiting remote as to register. Going to sleep for 1000.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
