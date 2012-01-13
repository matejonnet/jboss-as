package org.jboss.as.paas.controller.dmr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OperationStepRegistry {

    private static final Logger log = Logger.getLogger(OperationStepRegistry.class);

    //private Deque<OperationStepEntry> queue = new ArrayDeque<OperationStepEntry>();
    private Map<String, OperationStepEntry> queue = new HashMap<String, OperationStepEntry>();
    private Set<String> executed = new HashSet<String>();
    private Set<String> failed = new HashSet<String>();

    public void addExecuted(String stepName) {
        executed.add(stepName);
    }

    public void addFailed(String stepName) {
        failed.add(stepName);
    }

    public boolean areExecuted(String[] required) {
        return executed.containsAll(Arrays.asList(required));
    }

    public boolean areFailed(String[] required) {
        return failed.containsAll(Arrays.asList(required));
    }

    public void addToQueue(String name, OperationStepEntry ose) {
        queue.put(name, ose);
    }

    public OperationStepEntry getFromQueue(String name) {
        return queue.get(name);
    }

    //    public void addToQueue(OperationStepEntry ose) {
    //        queue.addFirst(ose);
    //    }
    //
    //    public OperationStepEntry getFromQueue() {
    //        try {
    //            return queue.pop();
    //        } catch (NoSuchElementException e) {
    //            log.debug("No more elements in queue.");
    //            return null;
    //        }
    //    }
}
