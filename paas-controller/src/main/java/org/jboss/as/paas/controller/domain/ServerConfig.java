package org.jboss.as.paas.controller.domain;

import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerConfig {

    /**
     * example value
     * {"s0" => {
     *   "auto-start" => true,
     *   "group" => "sg1",
     *   "interface" => undefined,
     *   "jvm" => undefined,
     *   "name" => "s0",
     *   "path" => undefined,
     *   "socket-binding-group" => "ha-sockets",
     *   "socket-binding-port-offset" => 100,
     *   "status" => "STOPPED",
     *   "system-property" => undefined
     *  }
     */
    private ModelNode serverConfig;

    /**
     * @param serverConfig
     */
    public ServerConfig(ModelNode serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * @return
     */
    public Integer getPosition() {
        ModelNode values = serverConfig.asProperty().getValue();
        int portOffset;
        if (values.hasDefined("socket-binding-port-offset")) {
            portOffset = values.get("socket-binding-port-offset").asInt();
        } else {
            portOffset = 0;
        }
        return InstanceSlot.getSlotPosition(portOffset);
    }

    /**
     * @return
     */
    public String getName() {
        return serverConfig.asProperty().getValue().get("group").asString();
    }

    public String getStatus() {
        ModelNode values = serverConfig.asProperty().getValue();
        if (values.hasDefined("status")) {
            return values.get("status").asString();
        }
        return null;
    }
}
