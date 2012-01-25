package org.jboss.as.paas.controller.operations;

import java.util.Set;

import org.jboss.as.paas.controller.dmr.DmrOperations;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.InstanceSlot;
import org.jboss.as.paas.controller.domain.ServerConfig;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UnDeployOperation extends OperationBase implements Operation {

    private static final Logger log = Logger.getLogger(UnDeployOperation.class);

    public UnDeployOperation(String appName) {
        super();
        this.appName = appName;
    }

    @Override
    public void execute() {
        String serverGroupName = getServerGroupName();

        ModelNode opUdeploy = DmrOperations.undeployFromServerGroup(getAppName(), serverGroupName);
        dmrActionExecutor.execute(opUdeploy);

        removeHostsFromServerGroup(serverGroupName, true);

        ModelNode opRemoveSG = DmrOperations.removeServerGroup(serverGroupName);
        dmrActionExecutor.execute(opRemoveSG);
    }

    void removeHostsFromServerGroup(String groupName, boolean removeFromAll) {

        Set<Instance> instances = getPaasDmrActions().getInstances();
        outer:
        for (int i = instances.size() - 1; i > -1; i--) {
            Instance instance = (Instance) instances.toArray()[i];
            Set<ServerConfig> serverGroups = instance.getServerGroups();

            for (ServerConfig serverGroup : serverGroups) {
                if (groupName.equals(serverGroup.getName())) {
                    String providerName = instance.getProviderName();
                    String hostIP;
                    try {
                        hostIP = IaasController.getInstance().getInstanceIp(providerName, instance.getInstanceId());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    int slotPosition = serverGroup.getPosition();

                    InstanceSlot slot = new InstanceSlot(hostIP, slotPosition, instance.getInstanceId());
                    removeHostFromServerGroup(groupName, slot);
                    int slotsInOwningGroup = serverGroups.size();
                    //if this is the only group on instance
                    if (slotsInOwningGroup < 2) {
                        removeInstance(instance);
                    }
                    if (!removeFromAll) {
                        break outer;
                    }
                }
            }
        }
    }

    protected void removeInstance(Instance instance) {
        try {
            String instanceId = instance.getInstanceId();

            ModelNode opShutdown = DmrOperations.getShutdown(instance.getHostIP());
            dmrActionExecutor.execute(opShutdown);

            IaasController.getInstance().terminateInstance(instance.getProviderName(), instanceId);

            ModelNode opRemove = DmrOperations.removeInstance(instanceId);
            dmrActionExecutor.execute(opRemove);
        } catch (Exception e) {
            log.errorf("Cannot terminate instance [%s]", instance);
        }
    }

    private void removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        ModelNode opJBossStop = DmrOperations.removeHostFromServerGroupStop(groupName, slot);
        dmrActionExecutor.execute(opJBossStop);

        waitServerToStop(slot);

        ModelNode opJBossRemove = DmrOperations.removeHostFromServerGroupRemove(groupName, slot);
        dmrActionExecutor.execute(opJBossRemove);
    }

    private void waitServerToStop(InstanceSlot slot) {
        ModelNode opServerRunning = DmrOperations.getServerResorce(slot);

        ModelNode result;
        int maxWaitTime = 15000; // 30sec
        long started = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Server hasn't stopped in " + maxWaitTime / 1000 + "seconds.");
            }
            result = dmrActionExecutor.executeForResult(opServerRunning);

            if (result.hasDefined("status") && "STOPPED".equals(result.get("status").asString())) {
                break;
            }
            try {
                log.debugf("Waiting server %s to stop. Going to sleep for 500.", slot.toString());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
