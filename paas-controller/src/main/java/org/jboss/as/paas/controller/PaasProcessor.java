package org.jboss.as.paas.controller;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.configurator.client.RemoteConfigurator;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.OperationStepRegistry;
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

    @Deprecated
    public PaasProcessor(OperationContext context, OperationStepRegistry stepRegistry) {
        this.context = context;
        paasDmrActions = new PaasDmrActions(context, stepRegistry);
        jbossDmrActions = new JBossDmrActions(context);
        compositeDmrActions = new CompositeDmrActions(context, jbossDmrActions, paasDmrActions, stepRegistry);
    }

    public PaasProcessor(OperationContext context, JBossDmrActions jbossDmrActions, PaasDmrActions paasDmrActions, CompositeDmrActions compositeDmrActions) {
        this.context = context;
        this.jbossDmrActions = jbossDmrActions;
        this.paasDmrActions = paasDmrActions;
        this.compositeDmrActions = compositeDmrActions;
    }

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

    public void addHostToServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {

        findSlot(provider, newInstance, serverGroupName, instanceId);
        if (slot == null) {
            context.getResult().set("Cannot get free slot.");
            return;
        }
        log.debugf("Using free slot instanceId: [%s] host [%s] slot position [%s].", slot.getInstanceId(), slot.getHostIP(), slot.getSlotPosition());
        jbossDmrActions.validateHostRegistration(slot.getHostIP());

        jbossDmrActions.createServerGroup(serverGroupName, new String[] { "validateHostRegistration" });
        //        jbossDmrActions.createServerGroup(serverGroupName, new String[] {});
        //        jbossDmrActions.addHostToServerGroup(slot, serverGroupName, new String[] { "createServerGroup" });

        //        if (true)
        //            return;

        //jbossDmrActions.startServer(slot);
        //jbossDmrActions.reloadHost(slot.getHostIP());

        paasDmrActions.addHostToServerGroupPaas(instanceId, slot.getSlotPosition(), serverGroupName, new String[] { "addHostToServerGroup" });

        if (log.isTraceEnabled())
            log.trace("Completed adding steps for addHostToServerGroup.");

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
