package org.jboss.as.paas.controller.operationqueue;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OperationQueue {

    private static final Logger log = Logger.getLogger(OperationQueue.class);

    private List<Operation> operations = new ArrayList<Operation>();

    public void add(Operation operation) {
        operations.add(operation);
    }

    public void execute() {
        for (Operation operation : operations) {
            log.debugf("Executing operation %s.", operation.getClass());
            operation.execute();
            log.tracef("Operation %s executed.", operation.getClass());
        }
    }

    public void executeAsync() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                execute();
            }
        }).start();
    }
}
