/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompositeDmrActions extends DmrActions {

    JbossDmrActions jbossDmrActions;
    PaasDmrActions paasDmrActions;

    /**
     * @param context
     */
    public CompositeDmrActions(OperationContext context) {
        super(context);
        jbossDmrActions = new JbossDmrActions(context);
        paasDmrActions = new PaasDmrActions(context);
    }

    /**
     * @param context
     * @param groupName
     * @param removeInstance
     *
     */
    public void removeHostFromServerGroup(String groupName, InstanceSlot slot) {

        ModelNode compositeRequest = new ModelNode();
        compositeRequest.get("operation").set("composite");
        compositeRequest.get("address").setEmptyList();
        ModelNode steps = compositeRequest.get("steps");

        jbossDmrActions.removeHostFromServerGroup(steps, groupName, slot);
        paasDmrActions.removeHostFromServerGroup(steps, groupName, slot);

        addStepToContext(compositeRequest);
    }

    /**
     * Add host to server group. If there is no host with free slots it creates new host.
     * New host creation is possible only with an IaaS provider.
     *
     * @param newInstance do not check for free slots, create new host
     * @param provider
     * @param context
     * @param groupName
     * @param slot
     */
    public void addHostToServerGroup(InstanceSlot slot, String groupName) {
        jbossDmrActions.addHostToServerGroup(slot, groupName);
        paasDmrActions.addHostToServerGroup(slot, groupName);
    }

    /**
     * @param context
     * @param groupName
     * @throws Exception
     *
     */
    public void removeHostsFromServerGroup(String groupName, boolean removeFromAll) throws Exception {
        Map<InstanceSlot,Integer> slotsToRemove = new HashMap<InstanceSlot,Integer>();

        outer:
        for (ResourceEntry instance : paasDmrActions.getInstances()) {
            Set<ResourceEntry> serverGroups = paasDmrActions.getServerGroups(instance);
            for (ResourceEntry serverGroup : serverGroups) {
                if (groupName.equals(serverGroup.getName())) {
                    String providerName = instance.getModel().get("provider").asString();
                    String hostIP = IaasController.getInstanceIp(providerName , instance.getName());
                    int slotPosition = serverGroup.getModel().get("position").asInt();

                    InstanceSlot slot = new InstanceSlot(hostIP, slotPosition, instance.getName());
                    removeHostFromServerGroup(groupName, slot);
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
