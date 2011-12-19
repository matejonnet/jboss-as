/**
 *
 */
package org.jboss.as.paas.controller;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.security.auth.callback.CallbackHandler;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ControllerClient {

    private static final Logger log = Logger.getLogger(ControllerClient.class);

    private ModelControllerClient client;

    /**
     * @param username
     * @param password
     * @param hostIp
     * @throws UnknownHostException
     */
    public ControllerClient(String username, String password, String hostIp) throws UnknownHostException {
        super();
        CallbackHandler cbh = new UsernamePasswordHandler(username, password.toCharArray());
        client = ModelControllerClient.Factory.create(hostIp, 9999, cbh);
    }

    /**
     * @return
     * @throws UnknownHostException
     */
    public ModelControllerClient getClient() throws UnknownHostException {
        if (!isRemoteHostUp(client))
            waitRemoteHostBoot(client);

        return client;
    }

    /**
     * @param client
     *
     */
    private void waitRemoteHostBoot(ModelControllerClient client) {
        boolean serverUp = false;

        //TODO make configurable
        int maxWaitTime = 60000; //1min
        long started = System.currentTimeMillis();

        while (!serverUp) {
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Jboss AS hasn't boot in " + maxWaitTime / 1000 + " seconds.");
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (isRemoteHostUp(client)) {
                serverUp = true;
            }
        }
    }

    /**
     * @param hostIp
     * @return
     */
    private boolean isRemoteHostUp(ModelControllerClient client) {
        ModelNode operation = new ModelNode();
        operation.get(OP).set("ls");
        //operation.get(OP_ADDR).add("profile", "paas-controller");

        try {
            client.execute(operation);
            log.info("Server UP.");
            return true;
        } catch (IOException e) {
            if (log.isTraceEnabled()) log.trace("Server down.");
            return false;
        }
    }
}
