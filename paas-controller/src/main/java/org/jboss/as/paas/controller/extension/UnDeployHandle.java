/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.paas.controller.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UnDeployHandle implements OperationStepHandler {
    public static final UnDeployHandle INSTANCE = new UnDeployHandle();
    public static final String OPERATION_NAME = "undeploy";
    private static final String ATTRIBUTE_APP_NAME = "name";

    private final Logger log = Logger.getLogger(UnDeployHandle.class);


    private UnDeployHandle() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!Util.isDomainController(context)) {
            context.completeStep();
            return;
        }

        final String appName = operation.get(ATTRIBUTE_APP_NAME).asString();
//TODO validate required attributes
//        if(appName == null) {
//            throw new OperationFormatException("Required argument name are missing.");
//        }



        undeployFromServerGroup(context, appName);

        try {
            Util.removeHostsFromServerGroup(context, Util.getServerGroupName(appName), true);
        } catch (Exception e) {
            //TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }

        removeServerGroup(context, appName);

        context.completeStep();
    }



    /**
     * @param context
     * @param appName
     */
    private void removeServerGroup(OperationContext context, String appName) {
        ModelNode request = new ModelNode();
        request.get(OP).set("remove");
        request.get(OP_ADDR).add("server-group", Util.getServerGroupName(appName));

        context.addStep(request, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);
    }


    private void undeployFromServerGroup(OperationContext context, String appName) {
        //Deployment process extracted from org.jboss.as.cli.handlers.DeployHandler.doHandle(CommandContext)

        String serverGroup = Util.getServerGroupName(appName);

        final ModelNode request;

        //prepare composite operation
        request = new ModelNode();
        request.get("operation").set("composite");
        request.get("address").setEmptyList();
        ModelNode steps = request.get("steps");

        //undeploy app - step undeploy
        ModelNode opUnDeploy = new ModelNode();
        opUnDeploy.get(OP).set("undeploy");
        opUnDeploy.get(OP_ADDR).add("server-group", serverGroup);
        opUnDeploy.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opUnDeploy);

        //undeploy app - step remove
        ModelNode opRemove = new ModelNode();
        opRemove.get(OP).set("remove");
        opRemove.get(OP_ADDR).add("server-group", serverGroup);
        opRemove.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemove);

        //remove deployment
        ModelNode opRemoveDeployment = new ModelNode();
        opRemoveDeployment.get(OP).set("remove");
        opRemoveDeployment.get(OP_ADDR).add(DEPLOYMENT, appName);
        steps.add(opRemoveDeployment);

        context.addStep(request, new OperationStepHandler() {
            public void execute(OperationContext context, ModelNode operation) {
                Util.executeStep(context, operation);
            }
        }, OperationContext.Stage.MODEL);

        //TODO verify result
    }



}
