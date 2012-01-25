package org.jboss.as.paas.controller.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.as.paas.controller.dmr.DmrOperations;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.extension.ServerInstanceAddHandler;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Instance {

    private ModelNode instance;
    private DmrActionExecutor dmrActionExecutor;

    public Instance(ModelNode instance, DmrActionExecutor dmrActionExecutor) {
        this.instance = instance;
        this.dmrActionExecutor = dmrActionExecutor;
    }

    public Set<ServerConfig> getServerGroups() {
        ModelNode op = DmrOperations.getServerConfig(getHostIP());
        ModelNode nodeSGs = dmrActionExecutor.executeForResult(op);

        Set<ServerConfig> serverGroups = new LinkedHashSet<ServerConfig>();
        for (ModelNode serverConfigNode : nodeSGs.asList()) {
            serverGroups.add(new ServerConfig(serverConfigNode));
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

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("provider: ");
        buff.append(getProviderName());
        buff.append(" insance-id: ");
        buff.append(getInstanceId());
        buff.append(" host-ip:");
        buff.append(getHostIP());
        return buff.toString();
    }
}
