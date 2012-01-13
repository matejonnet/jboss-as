package org.jboss.as.paas.controller;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.configurator.client.RemoteConfigurator;
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

    private PaasDmrActions paasDmrActions;
    private JBossDmrActions jbossDmrActions;
    private CompositeDmrActions compositeDmrActions;

    private InstanceSlot slot;

    public PaasProcessor(OperationContext context, JBossDmrActions jbossDmrActions, PaasDmrActions paasDmrActions, CompositeDmrActions compositeDmrActions) {
        this.context = context;
        this.jbossDmrActions = jbossDmrActions;
        this.paasDmrActions = paasDmrActions;
        this.compositeDmrActions = compositeDmrActions;
    }

    /**
     * Search for new slot, if none found, create new instance.
     */
    public void findSlot(final String provider, final boolean newInstance, String serverGroupName, String instanceId) {
        slot = null;
        if (!newInstance) {
            if (log.isTraceEnabled())
                log.tracef("Searching for existing instance on provider [%s]: instanceId: [%s].", provider, instanceId);
            InstanceSearch instanceSearch = new InstanceSearch(paasDmrActions);
            slot = instanceSearch.getFreeSlot(serverGroupName, provider, instanceId);
        }

        if (slot == null) {
            addNewServerInstanceToDomain(provider, serverGroupName);
        }
    }

    public void addHostToNewServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {
        addHostToServerGroup(serverGroupName, provider, newInstance, instanceId, true);
    }

    public void addHostToExisingServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {
        addHostToServerGroup(serverGroupName, provider, newInstance, instanceId, false);
    }

    private void addHostToServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId, boolean createNewSG) {

        findSlot(provider, newInstance, serverGroupName, instanceId);
        if (slot == null) {
            context.getResult().set("Cannot get free slot.");
            return;
        }
        log.debugf("Using free slot instanceId: [%s] host [%s] slot position [%s].", slot.getInstanceId(), slot.getHostIP(), slot.getSlotPosition());
        jbossDmrActions.validateHostRegistration(slot.getHostIP());
        if (createNewSG) {
            jbossDmrActions.createServerGroup(serverGroupName, new String[] { "validateHostRegistration" });
        }

        jbossDmrActions.addHostToServerGroup(slot, serverGroupName, new String[] { "validateHostRegistration" });

        paasDmrActions.addHostToServerGroupPaas(slot.getInstanceId(), slot.getSlotPosition(), serverGroupName, new String[] { "validateHostRegistration" });

    }

    public void removeHostFromServerGroup(String serverGroupName) {
        try {
            compositeDmrActions.removeHostsFromServerGroup(serverGroupName, false);
        } catch (Exception e) {
            // TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }
    }

    private void addNewServerInstanceToDomain(String provider, String serverGroupName) {

        try {
            // TODO update config instances/instance
            String instanceId = IaasController.getInstance().createNewInstance(provider);
            String hostIp = IaasController.getInstance().getInstanceIp(provider, instanceId);

            paasDmrActions.addInstance(instanceId, provider, hostIp);

            //Add password for remote server
            AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
            clusterPaasMngmt.addRemoteServer(hostIp);

            configureInstance(hostIp);

            // TODO uncomment to replace external configurator
            // ControllerClient cc = new ControllerClient(username, password,
            // hostIp);
            // ModelControllerClient client = cc.getClient();
            // addHostToDomain(hostIp, client);
            log.debugf("New host [%s] added and configured.", hostIp);
            slot = new InstanceSlot(hostIp, 0, instanceId);
            log.tracef("Constructed new InstanceSlot: hostIp [%s], instanceId [%s]", hostIp, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void configureInstance(String remoteIp) {
        new RemoteConfigurator().reconfigureRemote(remoteIp);
    }

    public InstanceSlot getSlot() {
        return slot;
    }
}
