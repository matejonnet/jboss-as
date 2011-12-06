/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UnDeployHandler extends BaseHandler implements OperationStepHandler {
    public static final UnDeployHandler INSTANCE = new UnDeployHandler();
    public static final String OPERATION_NAME = "undeploy";
    private static final String ATTRIBUTE_APP_NAME = "name";

    private final Logger log = Logger.getLogger(UnDeployHandler.class);


    private UnDeployHandler() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        JbossDmrActions jbossDmrActions = new JbossDmrActions(context);
        CompositeDmrActions compositeDmrActions = new CompositeDmrActions(context);

        if (!jbossDmrActions.isDomainController()) {
            context.completeStep();
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
        //TODO validate required attributes
        //        if(appName == null) {
        //            throw new OperationFormatException("Required argument name are missing.");
        //        }

        String serverGroupName = getServerGroupName(appName);

        jbossDmrActions.undeployFromServerGroup(appName, serverGroupName);

        try {
            compositeDmrActions.removeHostsFromServerGroup(serverGroupName, true);
        } catch (Exception e) {
            //TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }

        jbossDmrActions.removeServerGroup(serverGroupName);

        context.completeStep();
    }










}
