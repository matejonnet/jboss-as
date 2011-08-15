/**
 *
 */
package org.jboss.as.paas.controller;

import java.io.IOException;
import java.net.InetAddress;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;

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
        DefaultOperationRequestBuilder renameHostBuilder = new DefaultOperationRequestBuilder();
        renameHostBuilder.addNode("host", "newnode");
        renameHostBuilder.operationName("write-attribute");
        renameHostBuilder.addProperty("name", "name");
        renameHostBuilder.addProperty("value", hostIP);
        client.execute(renameHostBuilder.buildRequest());

        // /host=<hostIP>:write-remote-domain-controller(host=1.2.3.4, port=9999)
        DefaultOperationRequestBuilder setDcBuilder = new DefaultOperationRequestBuilder();
        renameHostBuilder.addNode("host", hostIP);
        renameHostBuilder.operationName("write-remote-domain-controller");
        renameHostBuilder.addProperty("host", dcIP);
        renameHostBuilder.addProperty("port", "9999");
        client.execute(renameHostBuilder.buildRequest());
    }
}
