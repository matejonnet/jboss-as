/**
 *
 */
package org.jboss.as.paas.configurator.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.jboss.as.paas.configurator.Main;
import org.jboss.as.paas.configurator.sys.SysUtil;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RemoteConfigurator {

    /**
     * @param remoteIp
     * @throws IOException
     */
    public void reconfigureRemote(String remoteIp) {
        Socket remoteConfigurator = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            //remoteConfigurator = new Socket(remoteIp, Main.CONFIGURATOR_PORT);
            remoteConfigurator = new Socket();
            SocketAddress endpointAddress = new InetSocketAddress(remoteIp, Main.CONFIGURATOR_PORT);
            remoteConfigurator.connect(endpointAddress, 30000);
            out = new PrintWriter(remoteConfigurator.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(remoteConfigurator.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + remoteIp);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + remoteIp);
        }

        try {
            // String hostControllerIp =
            // InetAddress.getLocalHost().getHostAddress();
            String hostControllerIp = SysUtil.getLocalIp();
            out.println(hostControllerIp);
            System.out.println("response: " + in.readLine());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            out.close();
            try {
                in.close();
                remoteConfigurator.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
