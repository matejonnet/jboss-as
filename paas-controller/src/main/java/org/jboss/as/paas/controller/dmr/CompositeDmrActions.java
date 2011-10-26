/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.DomainController;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompositeDmrActions extends DmrActions {

    /**
     * @param context
     * @param groupName
     * @param removeInstance
     *
     */
    public static void removeHostFromServerGroup(OperationContext context, String groupName, InstanceSlot slot) {

        ModelNode compositeRequest = new ModelNode();
        compositeRequest.get("operation").set("composite");
        compositeRequest.get("address").setEmptyList();
        ModelNode steps = compositeRequest.get("steps");

        JbossDmrActions.removeHostFromServerGroup(steps, groupName, slot);
        PaasDmrActions.removeHostFromServerGroup(steps, groupName, slot);

        addStepToContext(context, compositeRequest);
    }

    /**
     * Add host to server group. If there is no host with free slots it creates new host.
     * New host creation is possible only with an IaaS provider.
     *
     * @param newInstance do not check for free slots, create new host
     * @param provider
     * @param context
     * @param groupName
     */
    public static void addHostToServerGroup(boolean newInstance, String provider, OperationContext context, String groupName) {
        boolean newInstanceRequired = false;
        if (newInstance) {
            newInstanceRequired = true;
        }

        InstanceSlot slot = null;

        if (!newInstanceRequired) {
            slot = PaasProcessor.getFreeSlot(groupName, context, provider);
        }

        if (slot == null) {
            newInstanceRequired = true;
        }

        if (newInstanceRequired) {
            slot = DomainController.addServerInstanceToDomain(provider);
        }

        JbossDmrActions.addHostToServerGroup(context, slot, groupName);
        PaasDmrActions.addHostToServerGroup(context, slot, groupName);
    }

    /**
     * @param context
     * @param groupName
     * @throws Exception
     *
     */
    public static void removeHostsFromServerGroup(OperationContext context, String groupName, boolean removeFromAll) throws Exception {
        Map<InstanceSlot,Integer> slotsToRemove = new HashMap<InstanceSlot,Integer>();

        outer:
        for (ResourceEntry instance : PaasDmrActions.getInstances(context)) {
            Set<ResourceEntry> serverGroups = PaasDmrActions.getServerGroups(context, instance);
            for (ResourceEntry serverGroup : serverGroups) {
                if (groupName.equals(serverGroup.getName())) {
                    String providerName = instance.getModel().get("provider").asString();
                    String hostIP = IaasController.getInstanceIp(providerName , instance.getName());
                    int slotPosition = serverGroup.getModel().get("position").asInt();

                    InstanceSlot slot = new InstanceSlot(hostIP, slotPosition, instance.getName());
                    removeHostFromServerGroup(context, groupName, slot);
                    int slotsInOwningGroup = serverGroups.size();
                    if (slotsInOwningGroup < 2) {
                        IaasController.terminateInstance(providerName, slot.getInstanceId());
                    }
                    if (!removeFromAll) {
                        break outer;
                    }
                }
            }
        }
    }

}
