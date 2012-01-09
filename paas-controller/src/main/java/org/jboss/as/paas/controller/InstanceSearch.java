package org.jboss.as.paas.controller;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.paas.controller.dmr.PaasDmrActions;
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

    private Set<Integer> usedPositions = new HashSet<Integer>();

    //private OperationContext context;

    private PaasDmrActions paasDmrActions;

    /**
     * @param context
     * @param paasDmrActions
     */
    InstanceSearch(PaasDmrActions paasDmrActions) {
        super();
        //this.context = context;
        this.paasDmrActions = paasDmrActions;
        //paasDmrActions = new PaasDmrActions(context);
    }

    /**
     * find first free slot on instance
     */
    private InstanceSlot findFreeSlot(Instance instance, Set<Integer> usedPositions) {
        for (int i = 0; i < MAX_AS_PER_HOST; i++) {
            if (!usedPositions.contains(i)) {
                return new InstanceSlot(instance, i);
            }
        }
        throw new RuntimeException("There are no free slots on " + instance.getInstanceId() + " - " + instance.getHostIP() + ".");
    }

    /**
     *
     * @param group
     * @param createOnProvider
     * @param instanceId
     * @return InstanceSlot from an existing instance
     */
    InstanceSlot getFreeSlot(String group, String createOnProvider, String instanceId) {
        Instance instance = null;

        if (instanceId != null) {
            instance = paasDmrActions.getInstance(instanceId);
        } else {
            instance = getInstanceWithFreeSlot(group, createOnProvider);
        }

        if (instance != null) {
            return findFreeSlot(instance, usedPositions);
        }
        log.debug("No free slot found.");
        return null;
    }

    /**
     * loop throught instances which doesn't serve this group jet
     * @return null if none available
     */
    private Instance getInstanceWithFreeSlot(String group, String createOnProvider) {

        Instance instanceWithFreeSlot = null;

        for (Instance instance : paasDmrActions.getInstances()) {
            boolean hasFreeSlot = true;
            String providerName = instance.getProviderName();
            // if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                continue;
            }

            Set<ServerGroup> serverGroups = instance.getServerGroups();

            if (serverGroups.size() > MAX_AS_PER_HOST) {
                hasFreeSlot = false;
            }

            if (hasFreeSlot) {
                for (ServerGroup serverGroup : serverGroups) {
                    //if server group is already on this instance don't allow another. Use first found
                    if (instanceWithFreeSlot == null && !group.equals(serverGroup.getName())) {
                        instanceWithFreeSlot = instance;
                    }
                    usedPositions.add(serverGroup.getPosition());
                }
            }
        }
        return instanceWithFreeSlot;
    }
}
