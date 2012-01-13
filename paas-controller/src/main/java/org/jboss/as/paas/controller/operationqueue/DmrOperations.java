package org.jboss.as.paas.controller.operationqueue;

import static org.jboss.as.controller.client.ControllerClientMessages.MESSAGES;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.paas.controller.ControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrOperations implements Operation {

    private static final Logger log = Logger.getLogger(DmrOperations.class);
    private List<ModelNode> queue = new ArrayList<ModelNode>();

    private ModelControllerClient client;

    @Override
    public void execute() {
        for (ModelNode op : queue) {
            executeOperation(op);
        }
        queue.clear();
        close();
    }

    /**
     * Add operation to queue
     */
    public void add(final ModelNode op) {
        queue.add(op);
    }

    private void executeOperation(ModelNode op) {
        log.debugf("Executing operation %s.", op);
        executeForResult(new OperationBuilder(op).build());
        log.debugf("Operation %s executed.", op);
    }

    ModelNode executeForResult(org.jboss.as.controller.client.Operation op) {
        try {
            ModelNode result = getDelegate().execute(op);
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
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

    public ModelNode execute(ModelNode operation) throws IOException {
        return client.execute(operation);
    }

    public ModelNode execute(org.jboss.as.controller.client.Operation operation) throws IOException {
        return client.execute(operation);
    }

    public void close() {
        try {
            getDelegate().close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the delegate
     */
    private ModelControllerClient getDelegate() {
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

}
