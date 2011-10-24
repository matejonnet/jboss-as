/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.paas.controller.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ListApplicationsHandle implements OperationStepHandler {
    public static final ListApplicationsHandle INSTANCE = new ListApplicationsHandle();
    public static final String OPERATION_NAME = "list-applications";

    private final Logger log = Logger.getLogger(ListApplicationsHandle.class);
    private ModelControllerClient client;

    private ListApplicationsHandle() {
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

        if (!Util.isDomainController(context)) {
            context.completeStep();
            return;
        }

        System.out.println(">>>>>>>>> ListApplicationsHandle.execute: continue");

        //        final ModelNode request = new ModelNode();
        //        request.get(OP_ADDR).setEmptyList();
        //        request.get(ClientConstants.OP).add("read-children-names");
        //        request.get("child-type").set("deployment");

        try {

            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.setOperationName("read-children-names");
            builder.addProperty("child-type", "deployment");

            ModelNode request = builder.buildRequest();
            ModelNode opHeaders = new ModelNode();

            if (operation.get("cord-true").isDefined()) {
                opHeaders.get("execute-for-coordinator").set(true);
                request.get(ModelDescriptionConstants.OPERATION_HEADERS).set(opHeaders);
            } else if (operation.get("cord-false").isDefined()) {
                opHeaders.get("execute-for-coordinator").set(false);
                request.get(ModelDescriptionConstants.OPERATION_HEADERS).set(opHeaders);
            }

            if (operation.get("exe-model").isDefined()) {
                System.out.println(">>>>>>>>> added step OperationContext.Stage.MODEL");
                context.addStep(request, new OperationStepHandler() {
                    public void execute(OperationContext context, ModelNode operation) {
                        Util.executeStep(context, operation);
                        if (log.isDebugEnabled()) log.debug("Server group created. Oreration:" + operation);
                        System.out.println(">>>>>>>>> ListApplicationsHandle.execute: OperationContext.Stage.MODEL");
                    }
                }, OperationContext.Stage.MODEL);
            }


            if (operation.get("exe-domain").isDefined()) {
                System.out.println(">>>>>>>>> added step OperationContext.Stage.DOMAIN");
                context.addStep(request, new OperationStepHandler() {
                    public void execute(OperationContext context, ModelNode operation) {
                        Util.executeStep(context, operation);
                        if (log.isDebugEnabled()) log.debug("Server group created. Oreration:" + operation);
                        System.out.println(">>>>>>>>> ListApplicationsHandle.execute: OperationContext.Stage.DOMAIN");
                    }
                }, OperationContext.Stage.DOMAIN);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Cannot build request to create server group.", e);
        }



        //        final ModelNode request
        //        try {
        //            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
        //
        //
        //            builder.operationName("read-children-names");
        //            builder.addProperty("child-type", "deployment");
        //            request = builder.buildRequest();
        //
        //            context.addStep(request, new OperationStepHandler() {
        //                public void execute(OperationContext context, ModelNode operation) {
        //
        //                    String operationName = "read-children-names";
        //                    OperationStepHandler opStep = context.getResourceRegistration().getOperationHandler(PathAddress.EMPTY_ADDRESS, operationName);
        //
        //                    try {
        //                        opStep.execute(context, operation);
        //                    } catch (OperationFailedException e) {
        //                        // TODO Auto-generated catch block
        //                        e.printStackTrace();
        //                    }
        //
        //                    context.completeStep();
        //                }
        //            }, OperationContext.Stage.MODEL);
        //        } catch (OperationFormatException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }

        context.completeStep();
    }
}
