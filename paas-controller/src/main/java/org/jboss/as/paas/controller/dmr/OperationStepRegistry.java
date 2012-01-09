package org.jboss.as.paas.controller.dmr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OperationStepRegistry {

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

    public boolean areExecuted(String prefix, String[] required) {
        String[] requiredWithFrefix = new String[required.length];
        for (int i = 0; i < requiredWithFrefix.length; i++) {
            requiredWithFrefix[i] = "prefix" + required;
        }
        return areExecuted(requiredWithFrefix);
    }

}
