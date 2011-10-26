package org.jboss.as.paas.controller;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasProcessor {

    //TODO make configurable
    public static final int MAX_AS_PER_HOST = 3;

    /**
     * loop throught instances which doesn't serve this group jet
     *
     * @param context
     * @param providerName
     *
     * @return
     */
    public static InstanceSlot getFreeSlot(String group, OperationContext context, String createOnProvider) {

        for (ResourceEntry instance : PaasDmrActions.getInstances(context)) {
            boolean hasFreeSlot = true;
            String providerName = instance.getModel().get("provider").asString();
            //if defined createOnProvider, allow only defined provider
            if (createOnProvider != null && !createOnProvider.equals(providerName)) {
                continue;
            }

            Set<Integer> usedPositions = new HashSet<Integer>();

            Set<ResourceEntry> serverGroups = instance.getChildren("server-group");

            if (serverGroups.size() > MAX_AS_PER_HOST) {
                hasFreeSlot=false;
            }

            //ResourceEntry iaasProvider = Util.getIaasProvider(context, providerName);
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
                for (int i = 0; i < MAX_AS_PER_HOST ; i++) {
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


}
