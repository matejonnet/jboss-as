package org.jboss.as.paas.controller.domain;

import static org.jboss.as.paas.controller.extension.ServerInstanceAddHandler.ATTRIBUTE_INSTANCE_IP;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.controller.registry.Resource.ResourceEntry;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Instance {

    private ResourceEntry instance;

    public Instance(ResourceEntry instance) {
        this.instance = instance;
    }

    public Set<ServerGroup> getServerGroups() {
        Set<ServerGroup> serverGroups = new LinkedHashSet<ServerGroup>();
        for (ResourceEntry serverGroup : instance.getChildren("server-group")) {
            serverGroups.add(new ServerGroup(serverGroup));
        }
        return serverGroups;
    }

    public String getProviderName() {
        return instance.getModel().get("provider").asString();
    }

    public String getInstanceId() {
        return instance.getName();
    }

    public String getHostIP() {
        return instance.getModel().get(ATTRIBUTE_INSTANCE_IP).asString();
    }
}
