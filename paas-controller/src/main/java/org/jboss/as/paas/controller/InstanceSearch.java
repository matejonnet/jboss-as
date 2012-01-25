package org.jboss.as.paas.controller;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.InstanceSlot;
import org.jboss.as.paas.controller.domain.ServerConfig;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class InstanceSearch {

    // TODO make configurable per provider
    public static final int MAX_AS_PER_HOST = 3;

    private static final Logger log = Logger.getLogger(InstanceSearch.class);
    private PaasDmrActions paasDmrActions;
    private Instance instance;

    public InstanceSearch(PaasDmrActions paasDmrActions) {
        super();
        this.paasDmrActions = paasDmrActions;
    }

    /**
     * find first free slot on instance
     */
    private InstanceSlot findFreeSlot() {
        Set<Integer> usedPositions = new HashSet<Integer>();
        for (ServerConfig serverGroup : instance.getServerGroups()) {
            usedPositions.add(serverGroup.getPosition());
        }

        for (int i = 0; i < MAX_AS_PER_HOST; i++) {
            if (!usedPositions.contains(i)) {
                return new InstanceSlot(instance, i);
            }
        }
        throw new RuntimeException("There are no free slots on instance [" + instance.toString() + "].");
    }

    public InstanceSlot getFreeSlot(String group, String createOnProvider, String instanceId) {
        if (instanceId != null) {
            instance = paasDmrActions.getInstance(instanceId);
        } else {
            getInstanceWithFreeSlot(group, createOnProvider);
        }

        if (instance != null) {
            return findFreeSlot();
        }
        log.debug("No free slot found.");
        return null;
    }

    /**
     * loop thought instances which doesn't serve this group jet
     */
    private void getInstanceWithFreeSlot(String group, String createOnProvider) {
        Set<Instance> instances = paasDmrActions.getInstances();

        for (Instance instance : instances) {
            boolean hasFreeSlot = true;
            String providerName = instance.getProviderName();
            // if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                log.debugf("Skipping instance. Provider does not match, current [%s], requested [%s]", providerName, createOnProvider);
                continue;
            }

            Set<ServerConfig> serverGroups = instance.getServerGroups();

            if (serverGroups.size() > MAX_AS_PER_HOST) {
                log.debugf("All slots ocupied on instance [%s].", instance.toString());
                hasFreeSlot = false;
            }

            if (hasFreeSlot) {
                log.debugf("Instance [%s] has free slot.", instance.toString());
                if (!isServerGroupOnInstance(group, serverGroups)) {
                    log.debugf("Instance [%s] defined.", instance.toString());
                    this.instance = instance;
                    return;
                } else {
                    log.debugf("Instance [%s] already has group [%s].", instance.toString(), group);
                }
            }
        }
    }

    private boolean isServerGroupOnInstance(String group, Set<ServerConfig> instanceGroups) {
        for (ServerConfig serverGroup : instanceGroups) {
            //if server group is already on this instance don't allow another. Use first found
            if (group.equals(serverGroup.getName())) {
                return true;
            }
        }
        return false;
    }
}
