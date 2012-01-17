package org.jboss.as.paas.controller.domain;

import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerGroup {

    private ModelNode serverGroup;

    /**
     * @param serverGroup
     */
    public ServerGroup(ModelNode serverGroup) {
        this.serverGroup = serverGroup;
    }

    /**
     * @return
     */
    public Integer getPosition() {
        //TODO position must be set - edit parser
        if (!serverGroup.asProperty().getValue().hasDefined("position")) {
            return 0;
        }
        return serverGroup.asProperty().getValue().get("position").asInt();
    }

    /**
     * @return
     */
    public String getName() {
        return serverGroup.asProperty().getName();
    }

}
