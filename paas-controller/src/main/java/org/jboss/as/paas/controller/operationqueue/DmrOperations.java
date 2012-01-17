package org.jboss.as.paas.controller.operationqueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorOC;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrOperations implements Operation {

    private static final Logger log = Logger.getLogger(DmrOperations.class);
    private List<ModelNode> queue = new ArrayList<ModelNode>();

    private ModelControllerClient client;
    private DmrActionExecutor dmrActionExecutor;

    /**
     * @param context
     *
     */
    public DmrOperations(OperationContext context) {
        dmrActionExecutor = new DmrActionExecutorOC(context);
    }

    @Override
    public void execute() {
        //executeCommands();
        executeAsBatch();
    }

    private void executeCommands() {
        for (ModelNode op : queue) {
            dmrActionExecutor.execute(op);
        }
        queue.clear();
        dmrActionExecutor.close();
    }

    private void executeAsBatch() {
        ModelNode composite = new ModelNode();
        composite.get("operation").set("composite");
        composite.get("address").setEmptyList();
        ModelNode steps = composite.get("steps");

        for (ModelNode op : queue) {
            steps.add(op);
        }

        dmrActionExecutor.execute(composite);
        queue.clear();
        dmrActionExecutor.close();
    }

    /**
     * Add operation to queue
     */
    public void add(final ModelNode op) {
        queue.add(op);
    }

    public ModelNode execute(ModelNode operation) throws IOException {
        return client.execute(operation);
    }

    public ModelNode execute(org.jboss.as.controller.client.Operation operation) throws IOException {
        return client.execute(operation);
    }

}
