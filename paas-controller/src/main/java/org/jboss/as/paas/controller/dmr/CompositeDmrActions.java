/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.ServerGroup;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompositeDmrActions extends DmrActions {

    JBossDmrActions jbossDmrActions;
    PaasDmrActions paasDmrActions;

    public CompositeDmrActions(OperationContext context) {
        super(context);
        jbossDmrActions = new JBossDmrActions(context);
        paasDmrActions = new PaasDmrActions(context);
    }

    public void removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        ModelNode compositeRequest = new ModelNode();
        compositeRequest.get("operation").set("composite");
        compositeRequest.get("address").setEmptyList();
        ModelNode steps = compositeRequest.get("steps");

        jbossDmrActions.removeHostFromServerGroup(steps, groupName, slot);
        paasDmrActions.removeHostFromServerGroup(steps, groupName, slot);

        addStepToContext(compositeRequest);
    }

    public void removeHostsFromServerGroup(String groupName, boolean removeFromAll) throws Exception {

        outer:
        for (Instance instance : paasDmrActions.getInstances()) {
            Set<ServerGroup> serverGroups = instance.getServerGroups();
            for (ServerGroup serverGroup : serverGroups) {
                if (groupName.equals(serverGroup.getName())) {
                    String providerName = instance.getProviderName();
                    String hostIP = IaasController.getInstance().getInstanceIp(providerName, instance.getInstanceId());
                    int slotPosition = serverGroup.getPosition();

                    InstanceSlot slot = new InstanceSlot(hostIP, slotPosition, instance.getInstanceId());
                    removeHostFromServerGroup(groupName, slot);
                    int slotsInOwningGroup = serverGroups.size();
                    //if this is the only group on instance
                    if (slotsInOwningGroup < 2) {
                        IaasController.getInstance().terminateInstance(providerName, instance.getInstanceId());
                    }
                    if (!removeFromAll) {
                        break outer;
                    }
                }
            }
        }
    }
}
