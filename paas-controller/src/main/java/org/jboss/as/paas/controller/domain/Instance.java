package org.jboss.as.paas.controller.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.paas.controller.extension.ServerInstanceAddHandler;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Instance {

    private ModelNode instance;

    //private String instanceId;

    public Instance(ModelNode instance) {
        this.instance = instance;
        //instanceId = instance.keys().iterator().next();
    }

    public Set<ServerGroup> getServerGroups() {
        Set<ServerGroup> serverGroups = new LinkedHashSet<ServerGroup>();
        if (!instance.asProperty().getValue().hasDefined("server-group")) {
            return serverGroups;
        }
        for (ModelNode serverGroupNode : instance.asProperty().getValue().get("server-group").asList()) {
            serverGroups.add(new ServerGroup(serverGroupNode));
        }
        return serverGroups;
    }

    public String getProviderName() {
        return instance.asProperty().getValue().get("provider").asString();
    }

    public String getInstanceId() {
        return instance.asProperty().getName();
    }

    public String getHostIP() {
        return instance.asProperty().getValue().get(ServerInstanceAddHandler.ATTRIBUTE_INSTANCE_IP).asString();
    }
}
