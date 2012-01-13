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
import org.jboss.as.paas.controller.operationqueue.DmrOperations;
import org.jboss.as.paas.controller.operationqueue.OperationQueue;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompositeDmrActions extends DmrActions {

    JBossDmrActions jbossDmrActions;
    PaasDmrActions paasDmrActions;
    OperationQueue operationQueue;

    //    public CompositeDmrActions(OperationContext context) {
    //        super(context);
    //        jbossDmrActions = new JBossDmrActions(context);
    //        paasDmrActions = new PaasDmrActions(context);
    //    }

    public CompositeDmrActions(OperationContext context, JBossDmrActions jbossDmrActions, PaasDmrActions paasDmrActions, OperationStepRegistry stepRegistry, DmrOperations dmrOperations, OperationQueue operationQueue) {
        super(context, stepRegistry, dmrOperations);
        this.jbossDmrActions = jbossDmrActions;
        this.paasDmrActions = paasDmrActions;
        this.operationQueue = operationQueue;
    }

    public void removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        //        ModelNode compositeRequest = new ModelNode();
        //        compositeRequest.get("operation").set("composite");
        //        compositeRequest.get("address").setEmptyList();
        //        ModelNode steps = compositeRequest.get("steps");

        jbossDmrActions.removeHostFromServerGroup(groupName, slot);
        paasDmrActions.removeHostFromServerGroup(groupName, slot);
    }

    public void removeHostsFromServerGroup(String groupName, boolean removeFromAll) throws Exception {

        //        for (Instance instance : paasDmrActions.getInstances()) {
        Set<Instance> instances = paasDmrActions.getInstances();
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

    protected void terminateInstance(final Instance instance, final String providerName) {
        //TODO schedule on complete
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IaasController.getInstance().terminateInstance(providerName, instance.getInstanceId());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
