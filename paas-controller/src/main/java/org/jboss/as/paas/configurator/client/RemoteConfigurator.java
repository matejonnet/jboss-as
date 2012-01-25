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
import org.jboss.as.paas.util.Util;

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
            remoteConfigurator = new Socket();
            connect(remoteConfigurator, remoteIp);
            out = new PrintWriter(remoteConfigurator.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(remoteConfigurator.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + remoteIp);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + remoteIp);
        }

        try {
            String hostControllerIp = Util.getLocalIp();
            out.println(hostControllerIp);
            System.out.println("response: " + in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.safeClose(out);
            Util.safeClose(in);
            Util.safeClose(remoteConfigurator);
        }
    }

    private void connect(Socket remoteConfigurator, String remoteIp) throws IOException {
        SocketAddress endpointAddress = new InetSocketAddress(remoteIp, Main.CONFIGURATOR_PORT);

        int retry = 10;

        while (retry > 0) {
            try {
                System.out.println("Connecting to: " + remoteIp + " Retries left: " + retry);
                remoteConfigurator.connect(endpointAddress, 3000);
                System.out.println("Connected to: " + remoteIp);
                return;
            } catch (IOException e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    System.err.println("Waiting to retry connecting interupted.");
                }
                retry--;
            }
        }
        throw new IOException("");
    }

}
