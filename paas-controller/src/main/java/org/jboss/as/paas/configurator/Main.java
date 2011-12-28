/**
 *
 */
package org.jboss.as.paas.configurator;

import java.io.PrintStream;

import org.jboss.as.paas.configurator.client.RemoteConfigurator;
import org.jboss.as.paas.configurator.server.Server;

/**
 * Temporaly solution to configure and start remote jboss AS 7, as it cannot start in domain mode without host controller
 * remove this util once resolved https://issues.jboss.org/browse/AS7-379
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Main {

    public static final int CONFIGURATOR_PORT = 9995;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            if ("server".equals(args[0])) {
                String pathToHostXml = args[1];
                String pathToJbossRun = args[2];
                new Server(pathToHostXml, pathToJbossRun).startServer();
            } else if ("client".equals(args[0])) {
                String remoteIp = args[1];
                new RemoteConfigurator().reconfigureRemote(remoteIp);
            } else {
                usage();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            usage();
        }
    }

    /**
     *
     */
    private static void usage() {
        PrintStream out = System.out;
        out.println("Usage:");
        out.println("");
        out.println("param0: server/client");
        out.println("");
        out.println("server:");
        out.println("  param1: pathToHostXml");
        out.println("  param2: pathToJbossRun");
        out.println("");
        out.println("client:");
        out.println("  param1: remote server ip");
        out.println("");
    }

}
