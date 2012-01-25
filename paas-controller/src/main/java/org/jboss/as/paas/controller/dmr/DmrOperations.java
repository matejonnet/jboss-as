/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.paas.controller.domain.InstanceSlot;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrOperations {

    private DmrOperations() {}

    public static ModelNode addHostToServerGroup(InstanceSlot slot, String groupName) {
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets,
        // socket-binding-port-offset=<portOffset>)
        ModelNode op = new ModelNode();
        op.get(OP).set("add");
        op.get(OP_ADDR).add("host", slot.getHostIP());
        op.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        op.get("group").set(groupName);
        op.get("auto-start").set(true);
        op.get("socket-binding-group").set("standard-sockets");
        op.get("socket-binding-port-offset").set(slot.getPortOffset());

        return op;
    }

    public static ModelNode removeHostFromServerGroupStop(String groupName, InstanceSlot slot) {
        // /host=master/server-config=server-one:stop()
        ModelNode opStop = new ModelNode();
        opStop.get(OP).set("stop");
        opStop.get(OP_ADDR).add("host", slot.getHostIP());
        opStop.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());

        return opStop;
    }

    public static ModelNode removeHostFromServerGroupRemove(String groupName, InstanceSlot slot) {
        // /host=master/server-config=server-one:remove()
        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("host", slot.getHostIP());
        opRemove.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());

        return opRemove;
    }

    public static ModelNode undeployFromServerGroup(String appName, String serverGroup) {
        // Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        // prepare composite operation
        final ModelNode composite = new ModelNode();
        composite.get("operation").set("composite");
        composite.get("address").setEmptyList();
        ModelNode steps = composite.get("steps");

        // undeploy app - step undeploy
        ModelNode opUnDeploy = new ModelNode();
        opUnDeploy.get(OP).set("undeploy");
        opUnDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opUnDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opUnDeploy);

        // undeploy app - step remove
        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("server-group", serverGroup);
        opRemove.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemove);

        // remove deployment
        ModelNode opRemoveDeployment = new ModelNode();
        opRemoveDeployment.get(OP).set("remove");
        opRemoveDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemoveDeployment);

        return composite;
    }

    public static ModelNode removeServerGroup(String serverGroupName) {
        ModelNode op = new ModelNode();
        op.get(OP).set("remove");
        op.get(OP_ADDR).add("server-group", serverGroupName);

        return op;
    }

    public static ModelNode createServerGroup(String serverGroupName) {
        ModelNode op = new ModelNode();
        op.get(OP).set("add");
        op.get(OP_ADDR).add("server-group", serverGroupName);
        op.get("profile").set("default");
        op.get("socket-binding-group").set("standard-sockets");
        return op;
    }

    public static ModelNode getRegistedHosts() {
        // :read-children-names(child-type=host)
        ModelNode op = new ModelNode();
        op.get(OP).set("read-children-names");
        op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
        op.get("child-type").set("host");
        return op;
    }

    public static ModelNode startServer(String hostName, int slotPosition) {
        // /host=172.16.254.233:reload
        //skip reload on domain controller
        ModelNode op = new ModelNode();
        op.get(OP).set("start");
        op.get(OP_ADDR).add("host", hostName);
        op.get(OP_ADDR).add("server-config", "server" + slotPosition);
        return op;
    }

    public static ModelNode addDeploymentToServerGroupStepAdd(final File f, String appName, String serverGroup) {
        // Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)
        // deploy app - step add
        ModelNode opAdd = new ModelNode();
        opAdd.get(OP).set("add");
        opAdd.get(OP_ADDR).add("server-group", serverGroup);
        opAdd.get(OP_ADDR).add(DEPLOYMENT, appName);

        return opAdd;
    }

    public static ModelNode addDeploymentToServerGroupStepDeploy(final File f, String appName, String serverGroup) {
        // Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)
        // deploy app - step deploy
        ModelNode opDeploy = new ModelNode();
        opDeploy.get(OP).set("deploy");
        opDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        return opDeploy;
    }

    public static ModelNode getServerResorce(InstanceSlot slot) {
        return getServerResorce(slot.getHostIP(), slot.getSlotPosition());
    }

    public static ModelNode getServerResorce(String hostIp, int slotPosition) {
        // /host=172.16.254.134/server-config=server0:read-resource(include-runtime=true)
        ModelNode op = new ModelNode();
        op.get(OP).set("read-resource");
        op.get("include-runtime").set("true");
        op.get(OP_ADDR).add("host", hostIp);
        op.get(OP_ADDR).add("server-config", "server" + slotPosition);
        return op;
    }

    /**
     * expected result
     * "result" => {"s0" => {
     *   "auto-start" => true,
     *   "group" => "sg1",
     *   "interface" => undefined,
     *   "jvm" => undefined,
     *   "name" => "s0",
     *   "path" => undefined,
     *   "socket-binding-group" => "ha-sockets",
     *   "socket-binding-port-offset" => 100,
     *   "status" => "STOPPED",
     *   "system-property" => undefined
     * }}
     *
    */
    public static ModelNode getServerConfig(String hostIp) {
        // /host=172.16.254.160:read-children-resources(child-type=server-config, include-runtime=true)
        ModelNode op = new ModelNode();
        op.get(OP).set("read-children-resources");
        op.get("child-type").set("server-config");
        op.get("include-runtime").set("true");
        op.get(OP_ADDR).add("host", hostIp);
        return op;
    }

    public static ModelNode getDeployments() {
        // /:read-children-resources(child-type=deployment)
        ModelNode op = new ModelNode();
        op.get(OP).set("read-children-resources");
        op.get("child-type").set("deployment");
        op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
        return op;
    }

    public static ModelNode getShutdown(String hostIp) {
        // /host=172.16.254.173:shutdown
        ModelNode op = new ModelNode();
        op.get(OP).set("shutdown");
        op.get(OP_ADDR).add("host", hostIp);
        return op;
    }

    public static ModelNode getIaasProviders() {
        ///profile=paas-controller/subsystem=paas-controller:read-children-resources(child-type=provider)
        ModelNode op = new ModelNode();

        op.get(OP).set("read-children-resources");
        op.get("child-type").set("provider");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");

        return op;
    }

    public static ModelNode addInstance(String instanceId, String provider, String ip) {
        ModelNode opAddInstance = new ModelNode();
        opAddInstance.get(OP).set("add");
        opAddInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddInstance.get(OP_ADDR).add("instance", instanceId);
        opAddInstance.get("provider").set(provider);
        opAddInstance.get("ip").set(ip);
        return opAddInstance;
    }

    public static ModelNode removeInstance(String instanceId) {
        ModelNode opAddInstance = new ModelNode();
        opAddInstance.get(OP).set("remove");
        opAddInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddInstance.get(OP_ADDR).add("instance", instanceId);
        return opAddInstance;
    }
}
