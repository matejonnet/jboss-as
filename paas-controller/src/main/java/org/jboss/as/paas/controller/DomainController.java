/**
 *
 */
package org.jboss.as.paas.controller;

import java.io.IOException;
import java.net.InetAddress;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DomainController {

    /**
     * @param hostIP
     * @throws IOException
     * @throws OperationFormatException
     */
    public static void addHostToDomain(String hostIP) throws IOException, OperationFormatException {
        String dcIP = InetAddress.getLocalHost().getHostAddress();

        ModelControllerClient client = ModelControllerClient.Factory.create(hostIP, 9999);

        // /host=newnode:write-attribute(name=name, value=<hostIP>)
        DefaultOperationRequestBuilder renameDefaultHost = new DefaultOperationRequestBuilder();
        //default node name is newnode
        renameDefaultHost.addNode("host", "newnode");
        renameDefaultHost.setOperationName("write-attribute");
        renameDefaultHost.addProperty("name", "name");
        renameDefaultHost.addProperty("value", hostIP);
        client.execute(renameDefaultHost.buildRequest());

        // /host=<hostIP>:write-remote-domain-controller(host=1.2.3.4, port=9999)
        DefaultOperationRequestBuilder setDc = new DefaultOperationRequestBuilder();
        setDc.addNode("host", hostIP);
        setDc.setOperationName("write-remote-domain-controller");
        setDc.addProperty("host", dcIP);
        setDc.addProperty("port", "9999");
        client.execute(setDc.buildRequest());
    }

    /**
     * - creates new server instance
     * - join it to domain controller
     * - associate it with server group
     * - start the server instance
     * @return
     *
     */
    public static InstanceSlot addServerInstanceToDomain(String provider) {
        try {
            //TODO update config instances/instance
            String instanceId = IaasController.createNewInstance(provider);
            String hostIp = IaasController.getInstanceIp(provider, instanceId);
            addHostToDomain(hostIp);
            return new InstanceSlot(hostIp, 0, instanceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
