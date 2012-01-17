/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.ServerGroup;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompositeDmrActions {

    JBossDmrActions jbossDmrActions;
    PaasDmrActions paasDmrActions;
    private DmrActionExecutor dmrActionExecutor;

    public CompositeDmrActions(JBossDmrActions jbossDmrActions, PaasDmrActions paasDmrActions, DmrActionExecutor dmrActionExecutor) {
        this.jbossDmrActions = jbossDmrActions;
        this.paasDmrActions = paasDmrActions;
        this.dmrActionExecutor = dmrActionExecutor;
    }

    public void removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        jbossDmrActions.removeHostFromServerGroup(groupName, slot);
        paasDmrActions.removeHostFromServerGroup(groupName, slot);
    }

    public void removeHostsFromServerGroup(String groupName, boolean removeFromAll, OperationContext context) throws Exception {

        //        for (Instance instance : paasDmrActions.getInstances()) {
        Set<Instance> instances = paasDmrActions.getInstances(dmrActionExecutor);
        outer:
        for (int i = instances.size() - 1; i > -1; i--) {
            Instance instance = (Instance) instances.toArray()[i];
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
                        //TODO XXX uncomment
                        //operationQueue.add(new TerminateInstances(providerName, instance));
                    }
                    if (!removeFromAll) {
                        break outer;
                    }
                }
            }
        }
    }

}
