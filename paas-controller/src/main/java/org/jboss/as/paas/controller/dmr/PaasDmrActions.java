/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorOC;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasDmrActions {

    private static final Logger log = Logger.getLogger(DmrActionExecutorOC.class);

    public Set<Instance> getInstances(DmrActionExecutor dmrActionExecutor) {
        ModelNode op = new ModelNode();

        op.get(OP).set("read-children-resources");
        op.get("child-type").set("instance");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");

        ModelNode result = dmrActionExecutor.executeForResult(op);

        Set<Instance> instances = new LinkedHashSet<Instance>();
        for (ModelNode instance : result.asList()) {
            instances.add(new Instance(instance));
        }
        return instances;
    }

    public Instance getInstance(String instanceId, DmrActionExecutor dmrActionExecutor) {
        Set<Instance> instances = getInstances(dmrActionExecutor);
        for (Instance instance : instances) {
            if (instance.getInstanceId().equals(instanceId)) {
                return instance;
            }
        }
        return null;
    }

    public Set<ResourceEntry> getIaasProviders(OperationContext context) {
        Resource rootResource = context.getRootResource();

        PathAddress instancesAddr = PathAddress.pathAddress(PathElement.pathElement("profile", "paas-controller"), PathElement.pathElement("subsystem", "paas-controller"));

        final Resource instancesResource = rootResource.navigate(instancesAddr);
        return instancesResource.getChildren("provider");
    }

    private ResourceEntry getIaasProvider(String providerName, OperationContext context) {
        Set<ResourceEntry> providers = getIaasProviders(context);
        for (ResourceEntry provider : providers) {
            if (provider.getName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }

    public ModelNode addHostToServerGroupPaas(String instanceId, int slotPosition, String groupName) {
        ModelNode op = new ModelNode();
        op.get(OP).set("add");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");
        op.get(OP_ADDR).add("instance", instanceId);
        op.get(OP_ADDR).add("server-group", groupName);
        op.get("position").set(slotPosition);
        //addStepToContext(op, "addHostToServerGroupPaas", requiredSteps);
        return op;

    }

    public ModelNode addInstance(String instanceId, String provider, String ip) {
        ModelNode opAddInstance = new ModelNode();
        opAddInstance.get(OP).set("add");
        opAddInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddInstance.get(OP_ADDR).add("instance", instanceId);
        opAddInstance.get("provider").set(provider);
        opAddInstance.get("ip").set(ip);
        return opAddInstance;
    }

    public ModelNode removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        ModelNode op = new ModelNode();
        op.get(OP).set("remove");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");
        op.get(OP_ADDR).add("instance", slot.getInstanceId());
        op.get(OP_ADDR).add("server-group", groupName);
        return op;
    }

    public Set<ResourceEntry> getServerGroups(ResourceEntry instance) {
        return instance.getChildren("server-group");
    }

    public ModelNode listApplications(ModelNode operation) {
        ModelNode op = new ModelNode();
        op.get(OP_ADDR).setEmptyList();
        op.get(OP).add("read-children-names");
        op.get("child-type").set("deployment");
        return op;
    }

    private Resource navigate(PathAddress address, OperationContext context) {
        Resource rootResource = context.getRootResource();
        log.tracef("Navigating from rootResource to [%s] ...", address.toString());
        return rootResource.navigate(address);
    }

}
