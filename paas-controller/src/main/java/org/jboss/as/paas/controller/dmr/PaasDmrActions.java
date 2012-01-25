/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasDmrActions {

    private DmrActionExecutor dmrActionExecutor;

    public PaasDmrActions(DmrActionExecutor dmrActionExecutor) {
        this.dmrActionExecutor = dmrActionExecutor;
    }

    public Set<Instance> getInstances() {
        ModelNode op = new ModelNode();

        op.get(OP).set("read-children-resources");
        op.get("child-type").set("instance");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");

        ModelNode result = dmrActionExecutor.executeForResult(op);

        Set<Instance> instances = new LinkedHashSet<Instance>();
        for (ModelNode instance : result.asList()) {
            instances.add(new Instance(instance, dmrActionExecutor));
        }
        return instances;
    }

    public Instance getInstance(String instanceId) {
        Set<Instance> instances = getInstances();
        for (Instance instance : instances) {
            if (instance.getInstanceId().equals(instanceId)) {
                return instance;
            }
        }
        return null;
    }

}
