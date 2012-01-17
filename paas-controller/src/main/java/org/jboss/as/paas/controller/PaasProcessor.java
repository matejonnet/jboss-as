package org.jboss.as.paas.controller;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasProcessor {

    private static final Logger log = Logger.getLogger(PaasProcessor.class);

    private PaasDmrActions paasDmrActions;
    private JBossDmrActions jbossDmrActions;
    private CompositeDmrActions compositeDmrActions;

    private InstanceSlot slot;

    public PaasProcessor(JBossDmrActions jbossDmrActions, PaasDmrActions paasDmrActions, CompositeDmrActions compositeDmrActions) {
        this.jbossDmrActions = jbossDmrActions;
        this.paasDmrActions = paasDmrActions;
        this.compositeDmrActions = compositeDmrActions;
    }

    public void addHostToNewServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {
        addHostToServerGroup(serverGroupName, provider, newInstance, instanceId, true);
    }

    public void addHostToExisingServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId) {
        addHostToServerGroup(serverGroupName, provider, newInstance, instanceId, false);
    }

    private void addHostToServerGroup(String serverGroupName, String provider, boolean newInstance, String instanceId, boolean createNewSG) {

        //        findSlot(provider, newInstance, serverGroupName, instanceId);
        //        if (slot == null) {
        //            //TODO add message
        //            return;
        //        }
        //        log.debugf("Using free slot instanceId: [%s] host [%s] slot position [%s].", slot.getInstanceId(), slot.getHostIP(), slot.getSlotPosition());
        //        jbossDmrActions.validateHostRegistration(slot.getHostIP());
        //        if (createNewSG) {
        //            jbossDmrActions.createServerGroup(serverGroupName, new String[] { "validateHostRegistration" });
        //            dmrActionExecutor.process(op, "createServerGroup", requiredSteps);
        //        }
        //
        //        ModelNode op = jbossDmrActions.addHostToServerGroup(slot, serverGroupName);
        //        dmrActionExecutor.process(op, "addHostToServerGroup", new String[] { "validateHostRegistration" });
        //
        //        paasDmrActions.addHostToServerGroupPaas(slot.getInstanceId(), slot.getSlotPosition(), serverGroupName, new String[] { "validateHostRegistration" });

    }

    public void removeHostFromServerGroup(String serverGroupName, OperationContext context) {
        try {
            compositeDmrActions.removeHostsFromServerGroup(serverGroupName, false, context);
        } catch (Exception e) {
            // TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }
    }

    public InstanceSlot getSlot() {
        return slot;
    }
}
