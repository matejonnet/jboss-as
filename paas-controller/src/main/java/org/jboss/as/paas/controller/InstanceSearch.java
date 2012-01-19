package org.jboss.as.paas.controller;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.ServerGroup;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class InstanceSearch {

    // TODO make configurable per provider
    public static final int MAX_AS_PER_HOST = 3;

    private static final Logger log = Logger.getLogger(InstanceSearch.class);

    //private Set<Integer> usedPositions = new HashSet<Integer>();

    //private OperationContext context;

    private PaasDmrActions paasDmrActions;

    private Instance instance;

    private DmrActionExecutor dmrActionExecutor;

    //private Set<Integer> usedPositions;

    /**
     * @param context
     * @param paasDmrActions
     * @param dmrActionExecutor
     */
    public InstanceSearch(PaasDmrActions paasDmrActions, DmrActionExecutor dmrActionExecutor) {
        super();
        this.paasDmrActions = paasDmrActions;
        this.dmrActionExecutor = dmrActionExecutor;
    }

    /**
     * find first free slot on instance
     */
    private InstanceSlot findFreeSlot() {
        Set<Integer> usedPositions = new HashSet<Integer>();
        for (ServerGroup serverGroup : instance.getServerGroups()) {
            usedPositions.add(serverGroup.getPosition());
        }

        for (int i = 0; i < MAX_AS_PER_HOST; i++) {
            if (!usedPositions.contains(i)) {
                return new InstanceSlot(instance, i);
            }
        }
        throw new RuntimeException("There are no free slots on instance [" + instance.toString() + "].");
    }

    /**
     *
     * @param group
     * @param createOnProvider
     * @param instanceId
     * @return InstanceSlot from an existing instance
     */
    public InstanceSlot getFreeSlot(String group, String createOnProvider, String instanceId) {
        if (instanceId != null) {
            instance = paasDmrActions.getInstance(instanceId, dmrActionExecutor);
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
     * loop throught instances which doesn't serve this group jet
     * @return null if none available
     */
    private void getInstanceWithFreeSlot(String group, String createOnProvider) {
        Set<Instance> instances = paasDmrActions.getInstances(dmrActionExecutor);

        for (Instance instance : instances) {
            boolean hasFreeSlot = true;
            String providerName = instance.getProviderName();
            // if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                log.debugf("Skipping instance. Provider does not match, current [%s], requested [%s]", providerName, createOnProvider);
                continue;
            }

            Set<ServerGroup> serverGroups = instance.getServerGroups();

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

    private boolean isServerGroupOnInstance(String group, Set<ServerGroup> instanceGroups) {
        for (ServerGroup serverGroup : instanceGroups) {
            //if server group is already on this instance don't allow another. Use first found
            if (group.equals(serverGroup.getName())) {
                return true;
            }
        }
        return false;
    }
}
