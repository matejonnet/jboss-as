package org.jboss.as.paas.controller.dmr.executor;

import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface DmrActionExecutor {

    void execute(ModelNode op);

    /**
     * Operation is not available on OperationContext implementation
     */
    ModelNode executeForResult(ModelNode op);

    ModelNode executeForResult(Operation operation);

}
