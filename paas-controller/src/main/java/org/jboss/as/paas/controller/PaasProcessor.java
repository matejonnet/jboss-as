package org.jboss.as.paas.controller;

import java.util.NoSuchElementException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasProcessor {

    private static final Logger log = Logger.getLogger(PaasProcessor.class);

    OperationContext context;

    private CompositeDmrActions compositeDmrActions;
    private PaasDmrActions paasDmrActions;

    /**
     *
     */
    public PaasProcessor(OperationContext context) {
        this.context = context;
        compositeDmrActions = new CompositeDmrActions(context);
        paasDmrActions = new PaasDmrActions(context);
    }

    public InstanceSlot getSlot(final String provider, final boolean newInstance, String serverGroupName, String instanceId) {
        InstanceSlot slot = null;
        if (!newInstance) {
            InstanceSearch instanceSearch = new InstanceSearch(context);
            slot = instanceSearch.getFreeSlot(serverGroupName, provider, instanceId);
        }

        if (slot == null) {
            slot = addNewServerInstanceToDomain(provider, serverGroupName);
        }

        return slot;
    }

    /**
     * @param serverGroupName
     * @param slot
     * @param provider
     * @param newInstance
     * @param instanceId
     */
    public void addHostToServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {

        InstanceSlot slot = getSlot(provider, newInstance, serverGroupName, instanceId);
        if (slot == null) {
            context.getResult().set("Cannot get free slot.");
        }

        if (!isHostRegistredToDc(slot.getHostIP())) {
            context.getResult().add("Instance [" + slot.getInstanceId() + "] is not registered in domain controller jet. Re-run deploy command with instance-id parameter.");
            context.completeStep();
            return;
        }

        JBossDmrActions jbossDmrActions = new JBossDmrActions(context);
        jbossDmrActions.createServerGroup(serverGroupName);
        jbossDmrActions.addHostToServerGroup(slot, serverGroupName);
        if (log.isTraceEnabled())
            log.trace("addHostToServerGroup return");
    }

    public void removeHostFromServerGroup(String serverGroupName) {

        try {
            compositeDmrActions.removeHostsFromServerGroup(serverGroupName, false);
        } catch (Exception e) {
            // TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }

    }

    private InstanceSlot addNewServerInstanceToDomain(String provider, String serverGroupName) {

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
            paasDmrActions.addHostToServerGroup(instanceId, 0, serverGroupName);
            return new InstanceSlot(hostIp, 0, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param hostIp
     * @return true if host is registered to domain controller
     */
    public boolean isHostRegistredToDc(String hostIp) {
        try {
            // try to navigate to host, to see if it is already registered
            JBossDmrActions jbossDmrActions = new JBossDmrActions(context);
            jbossDmrActions.navigateToHostName(hostIp);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * @param hostIp
     * @param context
     *
     */
    private void waitNewHostToRegisterToDC(String hostIp) {
        // TODO make configurable
        int maxWaitTime = 30000; // 30sec
        long started = System.currentTimeMillis();

        // wait remote as to register
        while (true) {
            if (isHostRegistredToDc(hostIp)) {
                break;
            }
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Instance hasn't registered in " + maxWaitTime / 1000 + " seconds.");
            }
            try {
                log.debug("Waiting remote as to register. Going to sleep for 1000.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
