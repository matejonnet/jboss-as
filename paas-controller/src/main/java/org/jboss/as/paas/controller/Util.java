/**
 *
 */
package org.jboss.as.paas.controller;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.extension.DeployHandle;
import org.jboss.as.paas.controller.extension.PaasExtension;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Util {

    private static final Logger log = Logger.getLogger(Util.class);

    public static void executeStep(OperationContext context, ModelNode operation) {
        String opName = operation.get(ModelDescriptionConstants.OP).asString();
        OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, opName);
        try {
            if (log.isDebugEnabled()) log.debug("Executing oreration:" + operation);
            opStep.execute(context, operation);
            if (log.isDebugEnabled()) log.debug("Oreration executed.");
        //} catch (OperationFailedException e) {
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            log.error("Can not execute operation [" + opName + "]", e);
        }
        context.completeStep();
    }

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
     * @param instance
     * @return
     */
    public static Set<ResourceEntry> getServerGroups(OperationContext context, ResourceEntry instance) {
        return instance.getChildren("server-group");
    }

    /**
     * @param appName
     * @return
     */
    public static String getServerGroupName(String appName) {
        //return appName + "-SG";
        return appName;
    }

    /**
     * Add host to server group. If there is no host with free slots it creates new host.
     * New host creation is possible only with an IaaS provider.
     *
     * @param newInstance do not check for free slots, create new host
     * @param provider
     * @param context
     * @param groupName
     */
    public static void addHostToServerGroup(boolean newInstance, String provider, OperationContext context, String groupName) {
        boolean newInstanceRequired = false;
        if (newInstance) {
            newInstanceRequired = true;
        }

        InstanceSlot slot = null;

        if (!newInstanceRequired) {
            slot = getFreeSlot(groupName, context, provider);
        }

        if (slot == null) {
            newInstanceRequired = true;
        }

        if (newInstanceRequired) {
            slot = addServerInstanceToDomain(provider);
        }

//        ModelNode compositeRequest = new ModelNode();
//        compositeRequest.get("operation").set("composite");
//        compositeRequest.get("address").setEmptyList();
//        ModelNode steps = compositeRequest.get("steps");

        //addHOST to SG
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets, socket-binding-port-offset=<portOffset>)
        ModelNode opAddHostToSg = new ModelNode();
        opAddHostToSg.get(OP).set("add");
        opAddHostToSg.get(OP_ADDR).add("host", slot.getHostIP());
        opAddHostToSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opAddHostToSg.get("group").set(groupName);
        opAddHostToSg.get("auto-start").set(true);
        opAddHostToSg.get("socket-binding-group").set("standard-sockets");
        opAddHostToSg.get("socket-binding-port-offset").set(slot.getPortOffset());
//        steps.add(opAddHostToSg);
        context.addStep(opAddHostToSg, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
                if (log.isDebugEnabled()) log.debug("Host added to server group. Oreration:" + operation);
            }
        }, OperationContext.Stage.MODEL);

        ModelNode opAddSgToInstance = new ModelNode();
        opAddSgToInstance.get(OP).set("add");
        opAddSgToInstance.get(OP_ADDR).add("profile", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opAddSgToInstance.get(OP_ADDR).add("instance", slot.getInstanceId());
        opAddSgToInstance.get(OP_ADDR).add("server-group", groupName);
        opAddSgToInstance.get("position").set(slot.getSlotPosition());
//      steps.add(opAddSgToInstance);
        context.addStep(opAddSgToInstance, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
                if (log.isDebugEnabled()) log.debug("Host added to server group. Oreration:" + operation);
            }
        }, OperationContext.Stage.MODEL);


//        context.addStep(compositeRequest, new OperationStepHandler() {
//            public void execute(OperationContext context, ModelNode operation) {
//                Util.executeStep(context, operation);
//                if (log.isDebugEnabled()) log.debug("Host added to server group. Oreration:" + operation);
//            }
//        }, OperationContext.Stage.MODEL);
    }

    /**
     * - creates new server instance
     * - join it to domain controller
     * - associate it with server group
     * - start the server instance
     * @return
     *
     */
    private static InstanceSlot addServerInstanceToDomain(String provider) {
        try {
            //TODO update config instances/instance
            String instanceId = IaasController.createNewInstance(provider);
            String hostIp = IaasController.getInstanceIp(provider, instanceId);
            DomainController.addHostToDomain(hostIp);
            return new InstanceSlot(hostIp, 0, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * loop throught instances which doesn't serve this group jet
     *
     * @param context
     * @param providerName
     *
     * @return
     */
    private static InstanceSlot getFreeSlot(String group, OperationContext context, String createOnProvider) {

        for (ResourceEntry instance : Util.getInstances(context)) {
            boolean hasFreeSlot = true;
            String providerName = instance.getModel().get("provider").asString();
            //if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                continue;
            }

            Set<Integer> usedPositions = new HashSet<Integer>();

            Set<ResourceEntry> serverGroups = instance.getChildren("server-group");

            if (serverGroups.size() > PaasExtension.MAX_AS_PER_HOST) {
                hasFreeSlot=false;
            }

            ResourceEntry iaasProvider = Util.getIaasProvider(context, providerName);
           // String iaasDriver = iaasProvider.getModel().get("driver").asString();

            if (hasFreeSlot)
            for (ResourceEntry serverGroup : serverGroups) {
                //if server group is already on this instance don't allow another
                if (group.equals(serverGroup.getName())) {
                    hasFreeSlot=false;
                }
                usedPositions.add(serverGroup.getModel().get("position").asInt());
            }

            if (hasFreeSlot) {
                //find first free slot
                for (int i = 0; i < PaasExtension.MAX_AS_PER_HOST ; i++) {
                    if (!usedPositions.contains(i)) {
                        String hostIP = null;
                        try {
                            hostIP = IaasController.getInstanceIp(providerName, instance.getName());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return new InstanceSlot(hostIP, i, instance.getName());
                    }
                }

            }
        }
        return null;
    }


    /**
     * @param context
     * @return
     */
    private static Set<ResourceEntry> getIaasProviders(OperationContext context) {
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

    /**
     * @param context
     * @param groupName
     * @throws Exception
     *
     */
    public static void removeHostsFromServerGroup(OperationContext context, String groupName, boolean removeFromAll) throws Exception {
        Map<InstanceSlot,Integer> slotsToRemove = new HashMap<InstanceSlot,Integer>();

        outer:
        for (ResourceEntry instance : Util.getInstances(context)) {
            Set<ResourceEntry> serverGroups = Util.getServerGroups(context, instance);
            for (ResourceEntry serverGroup : serverGroups) {
                if (groupName.equals(serverGroup.getName())) {
                    String providerName = instance.getModel().get("provider").asString();
                    String hostIP = IaasController.getInstanceIp(providerName , instance.getName());
                    int slotPosition = serverGroup.getModel().get("position").asInt();

                    InstanceSlot slot = new InstanceSlot(hostIP, slotPosition, instance.getName());
                    removeHostFromServerGroup(context, groupName, slot);
                    int slotsInOwningGroup = serverGroups.size();
                    if (slotsInOwningGroup < 2) {
                        IaasController.terminateInstance(providerName, slot.getInstanceId());
                    }
                    if (!removeFromAll) {
                        break outer;
                    }
                }
            }
        }
    }

    /**
     * @param context
     * @param groupName
     * @param removeInstance
     *
     */
    private static void removeHostFromServerGroup(OperationContext context, String groupName, InstanceSlot slot) {

        ModelNode compositeRequest = new ModelNode();
        compositeRequest.get("operation").set("composite");
        compositeRequest.get("address").setEmptyList();
        ModelNode steps = compositeRequest.get("steps");

        //rmeoveHOST from SG
        // /host=master/server-config=server-one:remove()
        ModelNode opRemoveHostFromSg = new ModelNode();
        opRemoveHostFromSg.get(OP).set("remove");

        opRemoveHostFromSg.get(OP_ADDR).add("host", slot.getHostIP());
        opRemoveHostFromSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opRemoveHostFromSg.get("group").set(groupName);
        steps.add(opRemoveHostFromSg);

        ModelNode opRemoveSgFromInstance = new ModelNode();
        opRemoveSgFromInstance.get(OP).set("remove");
        opRemoveSgFromInstance.get(OP_ADDR).add("profile", "paas-controller");
        opRemoveSgFromInstance.get(OP_ADDR).add("subsystem", "paas-controller");
        opRemoveSgFromInstance.get(OP_ADDR).add("instance", slot.getInstanceId());
        opRemoveSgFromInstance.get(OP_ADDR).add("server-group", groupName);
        steps.add(opRemoveSgFromInstance);

        context.addStep(compositeRequest, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);
    }

    public static boolean isDomainController(OperationContext context) {
        Resource rootResource = context.getRootResource();

        // /host=172.16.254.128:read-config-as-xml

        //rootResource.navigate(PathAddress.pathAddress(PathElement.pathElement("host")));
        //rootResource.getModel().get("host") //undefined
//        System.out.println(">>>>>>> Util.isDomainController: " + rootResource.getChildTypes().contains("server-group"));
//        return rootResource.getChildTypes().contains("server-group");



        PathAddress addr;

        addr = PathAddress.pathAddress(
                PathElement.pathElement("host", getLocalIp()));
        final Resource resource = rootResource.navigate(addr);
        String domainController = resource.getModel().get("domain-controller").asPropertyList().get(0).getName();
        return "local".equals(domainController);
    }

    private static String getLocalIp() {
        System.out.println(">>>>>>> local ip is: " + System.getProperty("local.address.ip"));
        return System.getProperty("local.address.ip");

//        String hostName;
//        try {
//            hostName = InetAddress.getLocalHost().getHostName();
//            InetAddress addrs[] = InetAddress.getAllByName(hostName);
//
//            String myIp = "UNKNOWN";
//            for (InetAddress addr: addrs) {
//                System.out.println ("addr.getHostAddress() = " + addr.getHostAddress());
//                System.out.println ("addr.getHostName() = " + addr.getHostName());
//                System.out.println ("addr.isAnyLocalAddress() = " + addr.isAnyLocalAddress());
//                System.out.println ("addr.isLinkLocalAddress() = " + addr.isLinkLocalAddress());
//                System.out.println ("addr.isLoopbackAddress() = " + addr.isLoopbackAddress());
//                System.out.println ("addr.isMulticastAddress() = " + addr.isMulticastAddress());
//                System.out.println ("addr.isSiteLocalAddress() = " + addr.isSiteLocalAddress());
//                System.out.println ("");
//
//                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
//                    return addr.getHostAddress();
//                }
//            }
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
    }

}
