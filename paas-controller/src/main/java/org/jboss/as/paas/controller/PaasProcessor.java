package org.jboss.as.paas.controller;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasProcessor {

    // TODO make configurable per provider
    public static final int MAX_AS_PER_HOST = 3;

    private static final Logger log = Logger.getLogger(PaasProcessor.class);

    OperationContext context;

    /**
     *
     */
    public PaasProcessor(OperationContext context) {
        this.context = context;
    }

    /**
     * loop throught instances which doesn't serve this group jet
     * return null if none available
     *
     * @param context
     * @param providerName
     *
     * @return
     */
    private InstanceSlot getFreeSlot(String group, String createOnProvider) {

        PaasDmrActions paasDmrActions = new PaasDmrActions(context);

        for (ResourceEntry instance : paasDmrActions.getInstances()) {
            boolean hasFreeSlot = true;
            String providerName = instance.getModel().get("provider").asString();
            // if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                continue;
            }

            Set<Integer> usedPositions = new HashSet<Integer>();

            Set<ResourceEntry> serverGroups = instance.getChildren("server-group");

            if (serverGroups.size() > MAX_AS_PER_HOST) {
                hasFreeSlot = false;
            }

            // ResourceEntry iaasProvider = Util.getIaasProvider(context,
            // providerName);
            // String iaasDriver =
            // iaasProvider.getModel().get("driver").asString();

            if (hasFreeSlot)
                for (ResourceEntry serverGroup : serverGroups) {
                    // if server group is already on this instance don't allow
                    // another
                    if (group.equals(serverGroup.getName())) {
                        hasFreeSlot = false;
                    }
                    usedPositions.add(serverGroup.getModel().get("position").asInt());
                }

            if (hasFreeSlot) {
                // find first free slot
                for (int i = 0; i < MAX_AS_PER_HOST; i++) {
                    if (!usedPositions.contains(i)) {
                        String hostIP = null;
                        try {
                            hostIP = IaasController.getInstance().getInstanceIp(providerName, instance.getName());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return new InstanceSlot(hostIP, i, instance.getName());
                    }
                }

            }
        }
        return null;
    }

    /**
     * @param newInstance
     * @param context
     * @param provider
     * @param appName
     * @return
     */
    public InstanceSlot getSlot(boolean newInstance, String serverGroupName, String provider) {
        boolean newInstanceRequired;
        if (newInstance) {
            newInstanceRequired = true;
        } else {
            newInstanceRequired = false;
        }

        InstanceSlot slot = null;
        if (!newInstanceRequired) {
            slot = getFreeSlot(serverGroupName, provider);
        }

        if (slot == null) {
            newInstanceRequired = true;
        }

        if (newInstanceRequired) {
            slot = addNewServerInstanceToDomain(provider);
        }

        return slot;
    }

    /**
     * @param slot
     * @param serverGroupName
     * @param context
     * @param newInstance
     * @param provider
     */
    public void addHostToServerGroup(String serverGroupName, boolean newInstance, String provider) {
        InstanceSlot slot;

        try {
            slot = getSlot(newInstance, serverGroupName, provider);
        } catch (RuntimeException e) {
            context.setRollbackOnly();
            context.getResult().set("Cannot get free slot" + e);
            return;
        }

        JobQueue bj = new JobQueue();

        bj.add(JbossDmrActions.createServerGroupRequest(serverGroupName));

        bj.executeAsync(context, slot.getHostIP());

        // JbossDmrActions jbossDmrActions = new JbossDmrActions(context);
        // CompositeDmrActions compositeDmrActions = new
        // CompositeDmrActions(context);
        // jbossDmrActions.createServerGroup(serverGroupName);
        // compositeDmrActions.addHostToServerGroup(slot, serverGroupName);
        log.trace("addHostToServerGroup return");
    }

    public void removeHostFromServerGroup(String serverGroupName) {
        CompositeDmrActions compositeDmrActions = new CompositeDmrActions(context);

        try {
            compositeDmrActions.removeHostsFromServerGroup(serverGroupName, false);
        } catch (Exception e) {
            // TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }

    }

    /**
     * - creates new server instance
     * - join it to domain controller
     * - associate it with server group
     * - start the server instance
     *
     * @return
     *
     */
    private InstanceSlot addNewServerInstanceToDomain(String provider) {

        // System.out.println(">>>>>>> context:" +
        // context.getClass().getCanonicalName());
        // context.completeStep();

        try {
            // TODO update config instances/instance
            String instanceId = IaasController.getInstance().createNewInstance(provider);
            String hostIp = IaasController.getInstance().getInstanceIp(provider, instanceId);

            log.debugf("Waiting %s to register.", hostIp);

            // waitNewHostToRegisterToDC(hostIp);

            // TODO uncomment to replace external configurator
            // ControllerClient cc = new ControllerClient(username, password,
            // hostIp);
            // ModelControllerClient client = cc.getClient();
            // addHostToDomain(hostIp, client);
            return new InstanceSlot(hostIp, 0, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
