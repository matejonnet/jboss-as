/**
 *
 */
package org.jboss.as.paas.controller.dmr.executor;

import static org.jboss.as.controller.client.ControllerClientMessages.MESSAGES;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.paas.controller.ControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * Executes dmr operations using operation context
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrActionExecutorRemoteClient implements DmrActionExecutor {

    private static final Logger log = Logger.getLogger(DmrActionExecutorRemoteClient.class);

    private ModelControllerClient client;

    @Override
    public void execute(ModelNode op) {
        executeForResult(op);
    }

    @Override
    public ModelNode executeForResult(ModelNode op) {
        return executeForResult(new OperationBuilder(op).build());
    }

    @Override
    public void close() {
        try {
            getClient().close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the delegate
     */
    private ModelControllerClient getClient() {
        if (this.client == null) {
            try {
                log.debug("Connecting client to controller.");
                //                this.delegate = ModelControllerClient.Factory.create("127.0.0.1", 9999);
                ControllerClient cc = new ControllerClient();
                this.client = cc.getClient();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return this.client;
    }

    @Override
    public ModelNode executeForResult(org.jboss.as.controller.client.Operation op) {
        log.debugf("Executing operation %s.", op.getOperation());
        try {
            ModelNode result = getClient().execute(op);
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
                log.tracef("Operation %s executed.", op.getOperation());
                return result.get("result");
            } else if (result.hasDefined("failure-description")) {
                throw new RuntimeException(result.get("failure-description").toString());
            } else if (result.hasDefined("domain-failure-description")) {
                throw new RuntimeException(result.get("domain-failure-description").toString());
            } else if (result.hasDefined("host-failure-descriptions")) {
                throw new RuntimeException(result.get("host-failure-descriptions").toString());
            } else {
                throw MESSAGES.operationOutcome(result.get("outcome").asString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
