package org.jboss.as.paas.controller.test.configurator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.jboss.as.paas.configurator.Main;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RemoteConnection {

    @Test
    public void connect() {
        String remoteIp = "172.16.254.159";

        Socket remoteConfigurator = null;

        try {
            //remoteConfigurator = new Socket(remoteIp, Main.CONFIGURATOR_PORT);
            remoteConfigurator = new Socket();
            SocketAddress endpointAddress = new InetSocketAddress(remoteIp, Main.CONFIGURATOR_PORT);
            remoteConfigurator.connect(endpointAddress, 30000);

            Assert.assertTrue(remoteConfigurator.isConnected());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + remoteIp);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + remoteIp);
        }
    }
}
