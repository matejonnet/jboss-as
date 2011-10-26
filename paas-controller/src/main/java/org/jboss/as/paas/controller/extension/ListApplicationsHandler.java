/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ListApplicationsHandler implements OperationStepHandler {
    public static final ListApplicationsHandler INSTANCE = new ListApplicationsHandler();
    public static final String OPERATION_NAME = "list-applications";

    private final Logger log = Logger.getLogger(ListApplicationsHandler.class);
    private ModelControllerClient client;

    private ListApplicationsHandler() {
        System.out.println(">>>>>>>>>>> PaasAdd constructed.");
    }

    public void init(ModelControllerClient client) {
        this.client = client;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        System.out.println(">>>>>>>>> ListApplicationsHandle.execute.");

        if (!JbossDmrActions.isDomainController(context)) {
            context.completeStep();
            return;
        }

        System.out.println(">>>>>>>>> ListApplicationsHandle.execute: continue");

        PaasDmrActions.listApplications(context, operation);

        context.completeStep();
    }
}
