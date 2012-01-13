/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ResultMessagesDmrActions extends DmrActions {

    private List<Message> messages = new ArrayList<Message>();

    public ResultMessagesDmrActions(OperationContext context, OperationStepRegistry stepRegistry) {
        super(context, stepRegistry);
    }

    public void addMessgesToContext() {
        ModelNode op = new ModelNode();
        op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
        context.addStep(op, new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) {
                doProcessMessages();
                context.completeStep();
            }
        }, OperationContext.Stage.MODEL);
    }

    private void doProcessMessages() {
        for (Message message : messages) {
            if (message.fireOnExecute()) {
                if (stepRegistry.areExecuted(message.getRequired())) {
                    context.getResult().add("INFO! " + message.getMessage());
                }
            }
            if (message.fireOnFailed()) {
                if (stepRegistry.areFailed(message.getRequired())) {
                    context.getResult().add("INFO! " + message.getMessage());
                }
            }
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
