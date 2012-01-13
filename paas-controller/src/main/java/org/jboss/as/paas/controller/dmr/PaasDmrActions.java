/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.as.paas.controller.operationqueue.DmrOperations;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasDmrActions extends DmrActions {

    public PaasDmrActions(OperationContext context) {
        super(context);
    }

    public PaasDmrActions(OperationContext context, OperationStepRegistry stepRegistry, DmrOperations dmrOperations) {
        super(context, stepRegistry, dmrOperations);
    }

    private static final Logger log = Logger.getLogger(DmrActions.class);

    public Set<Instance> getInstances() {
        PathAddress instancesAddr = PathAddress.pathAddress(PathElement.pathElement("profile", "paas-controller"), PathElement.pathElement("subsystem", "paas-controller"));
        Resource instancesResource = navigate(instancesAddr);

        Set<Instance> instances = new LinkedHashSet<Instance>();
        for (ResourceEntry instanceRe : instancesResource.getChildren("instance")) {
            instances.add(new Instance(instanceRe));
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

    public Set<ResourceEntry> getIaasProviders() {
        Resource rootResource = context.getRootResource();

        PathAddress instancesAddr = PathAddress.pathAddress(PathElement.pathElement("profile", "paas-controller"), PathElement.pathElement("subsystem", "paas-controller"));

        final Resource instancesResource = rootResource.navigate(instancesAddr);
        return instancesResource.getChildren("provider");
    }

    private ResourceEntry getIaasProvider(String providerName) {
        Set<ResourceEntry> providers = getIaasProviders();
        for (ResourceEntry provider : providers) {
            if (provider.getName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }

    public void addHostToServerGroupPaas(String instanceId, int slotPosition, String groupName, String[] requiredSteps) {
        ModelNode op = new ModelNode();
        op.get(OP).set("add");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");
        op.get(OP_ADDR).add("instance", instanceId);
        op.get(OP_ADDR).add("server-group", groupName);
        op.get("position").set(slotPosition);
        addStepToContext(op, "addHostToServerGroupPaas", requiredSteps);

    }

    public void addInstance(String instanceId, String provider, String ip) {
        ModelNode opAddInstance = new ModelNode();
        opAddInstance.get(OP).set("add");
        opAddInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddInstance.get(OP_ADDR).add("instance", instanceId);
        opAddInstance.get("provider").set(provider);
        opAddInstance.get("ip").set(ip);
        addStepToContext(opAddInstance);
    }

    public void removeHostFromServerGroup(String groupName, InstanceSlot slot) {
        ModelNode op = new ModelNode();
        op.get(OP).set("remove");
        op.get(OP_ADDR).add("profile", "paas-controller");
        op.get(OP_ADDR).add("subsystem", "paas-controller");
        op.get(OP_ADDR).add("instance", slot.getInstanceId());
        op.get(OP_ADDR).add("server-group", groupName);
        addStepToContext(op);
    }

    public Set<ResourceEntry> getServerGroups(ResourceEntry instance) {
        return instance.getChildren("server-group");
    }

    public void listApplications(ModelNode operation) {
        //        final ModelNode request = new ModelNode();
        //        request.get(OP_ADDR).setEmptyList();
        //        request.get(ClientConstants.OP).add("read-children-names");
        //        request.get("child-type").set("deployment");

        try {

            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.setOperationName("read-children-names");
            builder.addProperty("child-type", "deployment");

            ModelNode request = builder.buildRequest();
            ModelNode opHeaders = new ModelNode();

            if (operation.get("cord-true").isDefined()) {
                opHeaders.get("execute-for-coordinator").set(true);
                request.get(ModelDescriptionConstants.OPERATION_HEADERS).set(opHeaders);
            } else if (operation.get("cord-false").isDefined()) {
                opHeaders.get("execute-for-coordinator").set(false);
                request.get(ModelDescriptionConstants.OPERATION_HEADERS).set(opHeaders);
            }

            if (operation.get("exe-model").isDefined()) {
                System.out.println(">>>>>>>>> added step OperationContext.Stage.MODEL");
                addStepToContext(request);
            }

            if (operation.get("exe-domain").isDefined()) {
                System.out.println(">>>>>>>>> added step OperationContext.Stage.DOMAIN");
                context.addStep(request, new OperationStepHandler() {
                    @Override
                    public void execute(OperationContext context, ModelNode operation) {
                        executeStep(context, operation);
                        if (log.isDebugEnabled())
                            log.debug("Server group created. Oreration:" + operation);
                        System.out.println(">>>>>>>>> ListApplicationsHandle.execute: OperationContext.Stage.DOMAIN");
                    }
                }, OperationContext.Stage.DOMAIN);
            }
            //TODO narrow catch
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Cannot build request to create server group.", e);
        }
    }
}
