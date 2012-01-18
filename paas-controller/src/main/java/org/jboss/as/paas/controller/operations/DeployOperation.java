package org.jboss.as.paas.controller.operations;

import java.io.File;
import java.util.List;

import org.jboss.as.paas.controller.AsClusterPassManagement;
import org.jboss.as.paas.controller.InstanceSearch;
import org.jboss.as.paas.controller.dmr.Deployment;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployOperation extends OperationBase implements PaasOperation {

    private static final Logger log = Logger.getLogger(DeployOperation.class);

    private File f;
    private String provider;
    private boolean newInstance;
    private String instanceId;
    private InstanceSlot slot;
    boolean createNewSG;

    public DeployOperation(String appName, String provider, boolean newInstance, String instanceId) {
        super();
        this.appName = appName;
        this.provider = provider;
        this.newInstance = newInstance;
        this.instanceId = instanceId;
        this.createNewSG = false;
    }

    public DeployOperation(File f, String provider, boolean newInstance, String instanceId, boolean createNewSG) {
        super();
        this.f = f;
        this.provider = provider;
        this.newInstance = newInstance;
        this.instanceId = instanceId;
        this.createNewSG = createNewSG;
    }

    @Override
    public void execute() {
        String appName = getAppName();
        String serverGroupName = getServerGroupName();

        JBossDmrActions jbossDmrActions = getJBossDmrActions();
        PaasDmrActions paasDmrActions = getPaasDmrActions();

        //        PaasProcessor paasProcessor = new PaasProcessor(jbossDmrActions, paasDmrActions, compositeDmrActions);

        findSlot(provider, newInstance, serverGroupName, instanceId);
        if (slot == null) {
            //TODO add message
            return;
        }

        log.debugf("Using free slot instanceId: [%s] host [%s] slot position [%s].", slot.getInstanceId(), slot.getHostIP(), slot.getSlotPosition());
        //boolean isHostRegistered = validateHostRegistration(slot.getHostIP());
        waitRemoteHostToRegister(slot.getHostIP());

        if (createNewSG) {
            ModelNode opCSG = jbossDmrActions.createServerGroup(serverGroupName);
            dmrActionExecutor.execute(opCSG);

            //TODO redeploy
            Deployment deployment = new Deployment();
            deployment.addDeployment(f, appName, appName);

            ModelNode opADSGa = jbossDmrActions.addDeploymentToServerGroupStepAdd(f, appName, serverGroupName);
            dmrActionExecutor.execute(opADSGa);

            ModelNode opADSGd = jbossDmrActions.addDeploymentToServerGroupStepDeploy(f, appName, serverGroupName);
            dmrActionExecutor.execute(opADSGd);

        }

        ModelNode opHTSG = jbossDmrActions.addHostToServerGroup(slot, serverGroupName);
        dmrActionExecutor.execute(opHTSG);

        ModelNode opHTSGP = paasDmrActions.addHostToServerGroupPaas(slot.getInstanceId(), slot.getSlotPosition(), serverGroupName);
        dmrActionExecutor.execute(opHTSGP);

        //        jbossDmrActions.reloadHost(paasProcessor.getSlot().getHostIP());
        //        ModelNode opStart = jbossDmrActions.reloadHost(slot.getHostIP());
        ModelNode opStart = jbossDmrActions.startServer(slot.getHostIP(), slot.getSlotPosition());
        dmrActionExecutor.execute(opStart);

        //dmrActionExecutor.close();
    }

    /**
     * Search for new slot, if none found, create new instance.
     */
    public void findSlot(final String provider, final boolean newInstance, String serverGroupName, String instanceId) {
        slot = null;
        if (!newInstance) {
            if (log.isTraceEnabled())
                log.tracef("Searching for existing instance on provider [%s]: instanceId: [%s].", provider, instanceId);
            InstanceSearch instanceSearch = new InstanceSearch(getPaasDmrActions(), dmrActionExecutor);
            slot = instanceSearch.getFreeSlot(serverGroupName, provider, instanceId);
        }

        if (slot == null) {
            addNewServerInstanceToDomain(provider, serverGroupName);
        }
    }

    private void addNewServerInstanceToDomain(String provider, String serverGroupName) {

        PaasDmrActions paasDmrActions = getPaasDmrActions();

        try {
            // TODO update config instances/instance
            String instanceId = IaasController.getInstance().createNewInstance(provider);
            String hostIp = IaasController.getInstance().getInstanceIp(provider, instanceId);

            paasDmrActions.addInstance(instanceId, provider, hostIp);

            //Add password for remote server
            AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
            clusterPaasMngmt.addRemoteServer(hostIp);

            IaasController.getInstance().configureInstance(hostIp);

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

    @Override
    public String getAppName() {
        if (appName == null) {
            appName = f.getName();
        }
        return appName;
    }

    private boolean isInResult(String resultEntry, List<ModelNode> resultList) {
        boolean success = false;
        for (ModelNode modelNode : resultList) {
            if (modelNode.asString().equals(resultEntry)) {
                success = true;
                break;
            }
        }
        return success;
    }

    private void waitRemoteHostToRegister(String hostIP) {

        // TODO make configurable
        int maxWaitTime = 30000; // 30sec
        long started = System.currentTimeMillis();

        log.debugf("Waiting host %s to register ...", hostIP);
        while (!validateHostRegistration(hostIP)) {
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Host hasn't registered in " + maxWaitTime / 1000 + "seconds.");
            }
            try {
                log.debugf("Waiting host %s to register. Going to sleep for 500.", hostIP);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private boolean validateHostRegistration(String hostIP) {
        ModelNode op = getJBossDmrActions().getRegistedHosts(hostIP);
        ModelNode hosts = dmrActionExecutor.executeForResult(op);
        return isInResult(hostIP, hosts.asList());
    }

    //    private PaasProcessor getPaasProcessor() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

}
