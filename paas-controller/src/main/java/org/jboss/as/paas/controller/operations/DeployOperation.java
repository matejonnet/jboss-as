package org.jboss.as.paas.controller.operations;

import java.io.File;
import java.util.List;

import org.jboss.as.paas.controller.AsClusterPassManagement;
import org.jboss.as.paas.controller.InstanceSearch;
import org.jboss.as.paas.controller.dmr.DmrOperations;
import org.jboss.as.paas.controller.domain.InstanceSlot;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.util.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployOperation extends OperationBase implements Operation {

    private static final Logger log = Logger.getLogger(DeployOperation.class);

    private File f;
    private String provider;
    private boolean newInstance;
    private String instanceId;
    private InstanceSlot slot;
    private boolean createNewSG;

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

        findSlot(provider, newInstance, serverGroupName, instanceId);
        if (slot == null) {
            //TODO add message
            return;
        }

        log.debugf("Using free slot instance: [%s].", slot.toString());

        waitRemoteHostToRegister(slot.getHostName());

        if (createNewSG) {
            ModelNode opCSG = DmrOperations.createServerGroup(serverGroupName);
            dmrActionExecutor.execute(opCSG);

            //TODO redeploy
            Deployment deployment = new Deployment();
            deployment.addDeployment(f, appName, appName);

            ModelNode opADSGa = DmrOperations.addDeploymentToServerGroupStepAdd(f, appName, serverGroupName);
            dmrActionExecutor.execute(opADSGa);

            ModelNode opADSGd = DmrOperations.addDeploymentToServerGroupStepDeploy(f, appName, serverGroupName);
            dmrActionExecutor.execute(opADSGd);

        }

        ModelNode opHTSG = DmrOperations.addHostToServerGroup(slot, serverGroupName);
        dmrActionExecutor.execute(opHTSG);

        ModelNode opStart = DmrOperations.startServer(slot.getHostName(), slot.getSlotPosition());
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
            InstanceSearch instanceSearch = new InstanceSearch(getPaasDmrActions());
            slot = instanceSearch.getFreeSlot(serverGroupName, provider, instanceId);
        }

        if (slot == null) {
            addNewServerInstanceToDomain(provider, serverGroupName);
        }
    }

    private void addNewServerInstanceToDomain(String provider, String serverGroupName) {

        if (provider == null) {
            // TODO if provider is null, loop through providers
            // ModelNode op = paasDmrActions.getIaasProviders();
            // ModelNode result = dmrActionExecutor.executeForResult(op);
            // result.asPropertyList();
        }

        try {
            String instanceId = IaasController.getInstance().createNewInstance(provider);
            String hostIp = IaasController.getInstance().getInstanceIp(provider, instanceId);

            ModelNode op = DmrOperations.addInstance(instanceId, provider, hostIp);
            dmrActionExecutor.execute(op);

            //Add password for remote server
            AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
            clusterPaasMngmt.addRemoteServer(Util.getHostName(hostIp));

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

    private void waitRemoteHostToRegister(String hostName) {

        // TODO make configurable
        int maxWaitTime = 45000; // 45sec
        long started = System.currentTimeMillis();

        log.debugf("Waiting host %s to register ...", hostName);
        while (!validateHostRegistration(hostName)) {
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Host hasn't registered in " + maxWaitTime / 1000 + "seconds.");
            }
            try {
                log.debugf("Waiting host %s to register. Going to sleep for 1000.", hostName);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private boolean validateHostRegistration(String hostName) {
        ModelNode op = DmrOperations.getRegistedHosts();
        ModelNode hosts = dmrActionExecutor.executeForResult(op);
        return isInResult(hostName, hosts.asList());
    }
}
