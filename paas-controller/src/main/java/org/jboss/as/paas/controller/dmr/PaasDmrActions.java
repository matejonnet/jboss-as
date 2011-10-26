/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.util.Set;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasDmrActions extends DmrActions {

    private static final Logger log = Logger.getLogger(DmrActions.class);

    public static Set<ResourceEntry> getInstances(OperationContext context) {
        Resource rootResource = context.getRootResource();

        PathAddress instancesAddr = PathAddress.pathAddress(
                PathElement.pathElement("profile", "paas-controller"),
                PathElement.pathElement("subsystem", "paas-controller"));

        final Resource instancesResource = rootResource.navigate(instancesAddr);
        return instancesResource.getChildren("instance");
    }

    public static ResourceEntry getInstance(OperationContext context, String instanceId) {
        Set<ResourceEntry> instances = getInstances(context);
        for (ResourceEntry instance : instances) {
            if (instance.getName().equals(instanceId)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * @param context
     * @return
     */
    public static Set<ResourceEntry> getIaasProviders(OperationContext context) {
        Resource rootResource = context.getRootResource();

        PathAddress instancesAddr = PathAddress.pathAddress(
                PathElement.pathElement("profile", "paas-controller"),
                PathElement.pathElement("subsystem", "paas-controller"));

        final Resource instancesResource = rootResource.navigate(instancesAddr);
        return instancesResource.getChildren("provider");
    }

    private static ResourceEntry getIaasProvider(OperationContext context, String providerName) {
        Set<ResourceEntry> providers = getIaasProviders(context);
        for (ResourceEntry provider : providers) {
            if (provider.getName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }

    public static void addHostToServerGroup(OperationContext context, InstanceSlot slot, String groupName) {
        ModelNode opAddSgToInstance = new ModelNode();
        opAddSgToInstance.get(OP).set("add");
        opAddSgToInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("instance", slot.getInstanceId());
        opAddSgToInstance.get(OP_ADDR).add("server-group", groupName);
        opAddSgToInstance.get("position").set(slot.getSlotPosition());
        addStepToContext(context, opAddSgToInstance);
    }

    public static void removeHostFromServerGroup(ModelNode steps, String groupName, InstanceSlot slot) {
        ModelNode opRemoveSgFromInstance = new ModelNode();
        opRemoveSgFromInstance.get(OP).set("remove");
        opRemoveSgFromInstance.get(OP_ADDR).add("profile", "paas-controller");
        opRemoveSgFromInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opRemoveSgFromInstance.get(OP_ADDR).add("instance", slot.getInstanceId());
        opRemoveSgFromInstance.get(OP_ADDR).add("server-group", groupName);
        steps.add(opRemoveSgFromInstance);
    }

    /**
     * @param context
     * @param instance
     * @return
     */
    public static Set<ResourceEntry> getServerGroups(OperationContext context, ResourceEntry instance) {
        return instance.getChildren("server-group");
    }

    /**
     * create a server group on DC
     *
     * @param context
     * @param serverGroupName
     */
    public static void createServerGroup(OperationContext context, String serverGroupName) {

        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        builder.setOperationName("add");
        builder.addNode("server-group", serverGroupName);
        builder.addProperty("profile", "default");
        builder.addProperty("socket-binding-group", "standard-sockets");

        try {
            ModelNode request = builder.buildRequest();
            addStepToContext(context, request);
        } catch (OperationFormatException e) {
            // TODO Auto-generated catch block
            log.error("Cannot build request to create server group.", e);
        }
    }

    public static void listApplications(OperationContext context, ModelNode operation) {
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
                addStepToContext(context, request);
            }

            if (operation.get("exe-domain").isDefined()) {
                System.out.println(">>>>>>>>> added step OperationContext.Stage.DOMAIN");
                context.addStep(request, new OperationStepHandler() {
                    public void execute(OperationContext context, ModelNode operation) {
                        executeStep(context, operation);
                        if (log.isDebugEnabled()) log.debug("Server group created. Oreration:" + operation);
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
