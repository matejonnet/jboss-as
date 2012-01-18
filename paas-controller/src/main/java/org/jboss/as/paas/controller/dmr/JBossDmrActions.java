/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.as.paas.util.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBossDmrActions extends DmrBase {

    private static final Logger log = Logger.getLogger(JBossDmrActions.class);

    /**
     * configure remote host to connect to domain controller
     */
    public ModelNode addHostToServerGroup(InstanceSlot slot, String groupName) {
        //log.debugf("Adding step addHostToServerGroup. Instance [%s] with ip [%s] to server group [%s].", slot.getInstanceId(), slot.getHostIP(), groupName);

        // addHOST to SG
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

    public ModelNode removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        // rmeoveHOST from SG
        // /host=master/server-config=server-one:stop()
        // /host=master/server-config=server-one:remove()
        // prepare composite operation

        final ModelNode composite = new ModelNode();
        composite.get("operation").set("composite");
        composite.get("address").setEmptyList();
        ModelNode steps = composite.get("steps");

        steps.add(removeHostFromServerGroupStop(groupName, slot));
        steps.add(removeHostFromServerGroupRemove(groupName, slot));

        return composite;
    }

    public ModelNode removeHostFromServerGroupStop(String groupName, InstanceSlot slot) {

        ModelNode opStop = new ModelNode();
        opStop.get(OP).set("stop");
        opStop.get(OP_ADDR).add("host", slot.getHostIP());
        opStop.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());

        return opStop;
    }

    public ModelNode removeHostFromServerGroupRemove(String groupName, InstanceSlot slot) {

        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("host", slot.getHostIP());
        opRemove.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());

        return opRemove;
    }

    /**
     * Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)
     */
    public ModelNode undeployFromServerGroup(String appName, String serverGroup) {
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

        //addStepToContext(request);
        return composite;

        // TODO verify result
    }

    public ModelNode removeServerGroup(String serverGroupName) {
        ModelNode op = new ModelNode();
        op.get(OP).set("remove");
        op.get(OP_ADDR).add("server-group", serverGroupName);

        return op;
    }

    /**
     * create a server group on DC
     *
     * @param serverGroupName
     * @param onSuccess
     * @param requiredSteps
     */
    public ModelNode createServerGroup(String serverGroupName) {
        log.tracef("Adding step createServerGroup [%s].", serverGroupName);

        ModelNode opHeaders = new ModelNode();
        opHeaders.get("execute-for-coordinator").set(true);

        ModelNode op = new ModelNode();
        if (false) //TODO
            op.get(OPERATION_HEADERS).set(opHeaders);
        op.get(OP).set("add");
        op.get(OP_ADDR).add("server-group", serverGroupName);
        op.get("profile").set("default");
        op.get("socket-binding-group").set("standard-sockets");

        return op;
        //return remoteDmrActions.executeOperation(op);
    }

    public ModelNode getRegistedHosts(final String hostName) {
        // :read-children-names(child-type=host)

        ModelNode op = new ModelNode();
        op.get(OP).set("read-children-names");
        op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
        op.get("child-type").set("host");

        return op;
    }

    public ModelNode reloadHost(String hostName) {
        // /host=172.16.254.233:reload
        //skip reload on domain controller
        ModelNode op = new ModelNode();
        if (Util.isLocalHost(hostName)) {
            op.get(OP).set("start");
        } else {
            //TODO reload should not be required
            op.get(OP).set("reload");
        }
        op.get(OP_ADDR).add("host", hostName);
        //addStepToContext(request);
        //dmrOperations.add(op);
        return op;
    }

    public ModelNode startServer(String hostName, int slotPosition) {
        // /host=172.16.254.233:reload
        //skip reload on domain controller
        ModelNode op = new ModelNode();
        op.get(OP).set("start");
        op.get(OP_ADDR).add("host", hostName);
        op.get(OP_ADDR).add("server-config", "server" + slotPosition);
        return op;
    }

    public ModelNode addDeploymentToServerGroupStepAdd(final File f, String appName, String serverGroup) {

        // Deployment process extracted from
        // org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        // deploy app - step add
        ModelNode opAdd = new ModelNode();
        opAdd.get(OP).set("add");
        opAdd.get(OP_ADDR).add("server-group", serverGroup);
        opAdd.get(OP_ADDR).add(DEPLOYMENT, appName);

        return opAdd;
    }

    public ModelNode addDeploymentToServerGroupStepDeploy(final File f, String appName, String serverGroup) {

        // Deployment process extracted from
        // org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        // deploy app - step deploy
        ModelNode opDeploy = new ModelNode();
        opDeploy.get(OP).set("deploy");
        opDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);

        return opDeploy;
    }

    public ModelNode getServerResorce(InstanceSlot slot) {
        // /host=172.16.254.134/server-config=server0:read-resource(include-runtime=true)

        ModelNode op = new ModelNode();
        op.get(OP).set("read-resource");
        op.get("include-runtime").set("true");
        op.get(OP_ADDR).add("host", slot.getHostIP());
        op.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        return op;
    }
}
