package org.jboss.as.paas.controller.domain;

import org.jboss.as.controller.registry.Resource.ResourceEntry;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerGroup {

    private ResourceEntry serverGroup;

    /**
     * @param serverGroup
     */
    public ServerGroup(ResourceEntry serverGroup) {
        this.serverGroup = serverGroup;
    }

    /**
     * @return
     */
    public Integer getPosition() {
        return serverGroup.getModel().get("position").asInt();
    }

    /**
     * @return
     */
    public String getName() {
        return serverGroup.getName();
    }

}
