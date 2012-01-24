/**
 *
 */
package org.jboss.as.paas.configurator.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.jboss.as.paas.configurator.Main;
import org.jboss.as.paas.util.Util;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Server {

    private String pathToHostXml;
    private String pathToJbossRun;

    /**
     * @param pathToHostXml
     * @param pathToJbossRun
     */
    public Server(String pathToHostXml, String pathToJbossRun) {
        super();
        this.pathToHostXml = pathToHostXml;
        this.pathToJbossRun = pathToJbossRun;
    }

    /**
     * @throws IOException
     *
     */
    public void startServer() throws IOException {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            try {
                serverSocket = new ServerSocket(Main.CONFIGURATOR_PORT);
            } catch (IOException e) {
                System.out.println("Could not listen on port: " + Main.CONFIGURATOR_PORT);
                System.exit(-1);
            }

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Accept failed: " + Main.CONFIGURATOR_PORT);
                System.exit(-1);
            }

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;

            // remote client must send host controller ip address only
            inputLine = in.readLine();

            JBossConfig config = new JBossConfig(pathToHostXml);
            config.setDomainControllerIp(inputLine);

            out.println("Configuration done.");

            startJboss(pathToJbossRun);
            out.println("Jboss start executed [" + pathToJbossRun + "].");

        } finally {
            Util.safeClose(out);
            Util.safeClose(in);
            clientSocket.close();
            serverSocket.close();
        }

        // exit when jboss is configured and started
        System.exit(0);
    }

    private void startJboss(String path) throws IOException {
        Runtime.getRuntime().exec(path);
    }

}
