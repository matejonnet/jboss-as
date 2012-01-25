/**
 *
 */
package org.jboss.as.paas.controller.dmr.executor;

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
class ControllerClient {

    private static final Logger log = Logger.getLogger(ControllerClient.class);

    private ModelControllerClient client;

    ControllerClient() throws UnknownHostException {
        client = ModelControllerClient.Factory.create("127.0.0.1", 9999);
    }

    ControllerClient(String hostIp, int port, String username, String password) throws UnknownHostException {
        CallbackHandler cbh = new UsernamePasswordHandler(username, password.toCharArray());
        client = ModelControllerClient.Factory.create(hostIp, port, cbh);
    }

    ModelControllerClient getClient() throws UnknownHostException {
        if (!isRemoteHostUp(client))
            waitRemoteHostBoot(client);
        //TODO try to reconnect if no connection

        return client;
    }

    private void waitRemoteHostBoot(ModelControllerClient client) {
        boolean serverUp = false;

        //TODO make configurable
        int maxWaitTime = 60000; //1min
        long started = System.currentTimeMillis();

        while (!serverUp) {
            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Could not connect in " + maxWaitTime / 1000 + " seconds.");
            }
            try {
                log.debug("Waiting to connect. Going to sleep for 500ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Waiting to connect interrupted.", e);
            }
            if (isRemoteHostUp(client)) {
                serverUp = true;
            }
        }
    }

    private boolean isRemoteHostUp(ModelControllerClient client) {
        ModelNode operation = new ModelNode();
        operation.get(OP).set("ls");

        try {
            client.execute(operation);
            log.info("Server UP.");
            return true;
        } catch (IOException e) {
            if (log.isTraceEnabled())
                log.trace("Server down.");
            return false;
        }
    }
}
