package org.jboss.as.paas.controller.operations;

import java.util.Set;

import org.jboss.as.paas.controller.dmr.DmrOperations;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.domain.ServerConfig;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceState;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class StatusOperation extends OperationBase {

    /**
     * @param context
     */
    public StatusOperation() {}

    public void getAppStatus(final ModelNode result) {
        ModelNode opDeployments = DmrOperations.getDeployments();
        ModelNode deployments = dmrActionExecutor.executeForResult(opDeployments);

        result.get("applications").setEmptyList();

        for (String deploymentName : deployments.keys()) {
            ModelNode appResultNode = new ModelNode();
            getAppStatus(appResultNode, deploymentName);
            result.get("applications").add(appResultNode);
        }
    }

    public void getAppStatus(final ModelNode result, String appName) {
        result.get("name").set(appName);
        result.get("hosts").setEmptyList();

        ModelNode opHosts = DmrOperations.getRegistedHosts();
        ModelNode hosts = dmrActionExecutor.executeForResult(opHosts);

        for (ModelNode modelNode : hosts.asList()) {
            String hostIp = modelNode.asString();
            ModelNode op = DmrOperations.getServerConfig(hostIp);
            ModelNode serverConfigsNode = dmrActionExecutor.executeForResult(op);
            for (ModelNode serverConfigNode : serverConfigsNode.asList()) {
                ServerConfig serverConfig = new ServerConfig(serverConfigNode);
                if (appName.equals(serverConfig.getName())) {
                    ModelNode config = new ModelNode();
                    config.get("host").set(hostIp);
                    config.get("slot-position").set(serverConfig.getPosition());
                    config.get("status").set(serverConfig.getStatus());

                    result.get("hosts").add(config);
                }
            }
        }
    }

    public void getInstancesStatus(final ModelNode result) {
        result.get("instances").setEmptyList();

        Set<Instance> instances = getPaasDmrActions().getInstances();
        for (Instance instance : instances) {
            ModelNode instanceData = new ModelNode();
            instanceData.get("instance-id").set(instance.getInstanceId());
            instanceData.get("provider").set(instance.getProviderName());

            InstanceState status = IaasController.getInstance().getInstanceStatus(instance.getProviderName(), instance.getInstanceId());
            instanceData.get("status").set(status.toString());

            result.get("instances").add(instanceData);
        }
    }
}
