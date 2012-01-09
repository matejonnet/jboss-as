/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.io.File;
import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployHandler extends BaseHandler implements OperationStepHandler {
    public static final DeployHandler INSTANCE = new DeployHandler();
    public static final String OPERATION_NAME = "deploy";
    // TODO rename attribute to PROPERTY
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";
    private static final String ATTRIBUTE_INSTANCE_ID = "instance-id";

    private final Logger log = Logger.getLogger(DeployHandler.class);

    private DeployHandler() {}

    /**
     * (non-Javadoc)
     *
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!super.execute(context)) {
            return;
        }

        log.trace("Executing deploy handle ...");

        PaasProcessor paasProcessor = new PaasProcessor(context, jbossDmrActions, paasDmrActions, compositeDmrActions);

        final String filePath = operation.get(ATTRIBUTE_PATH).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() && operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean();
        final String instanceId = operation.get(ATTRIBUTE_INSTANCE_ID).isDefined() ? operation.get(ATTRIBUTE_INSTANCE_ID).asString() : null;

        // TODO validate required attributes

        final File f;
        if (filePath != null) {
            f = new File(filePath);
            if (!f.exists()) {
                String message = "Path " + f.getAbsolutePath() + " doesn't exist. File must be located on localhost.";
                context.getResult().add(message);
                log.warn(message);
                return;
            }
            if (f.isDirectory()) {
                String message = f.getAbsolutePath() + " is a directory.";
                context.getResult().add(message);
                log.warn(message);
                return;
            }
        } else {
            f = null;
        }

        String appName = f.getName();
        String serverGroupName = getServerGroupName(appName);

        paasProcessor.addHostToServerGroup(serverGroupName, provider, newInstance, instanceId);

        //log.debug("deploying ...");
        //jbossDmrActions.deployToServerGroup(f, appName, serverGroupName, new String[] { "addHostToServerGroupPaas" });

        context.completeStep();

        //        if (!stepRegistry.areExecuted(JBossDmrActions.class.getName(), new String[] { "validateHostRegistration" })) {
        //            context.getResult().add("Instance [" + paasProcessor.getSlot().getInstanceId() + "] is not registered in domain controller jet. Re-run deploy command with instance-id parameter.");
        //        }

    }

    /**
     * DescriptionProvider.
     */
    public static DescriptionProvider DESC = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {

            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Deploy application.");

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PATH, DESCRIPTION).set("Path to file.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PATH, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PATH, REQUIRED).set(true);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, DESCRIPTION).set("Provider name.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_PROVIDER, REQUIRED).set(true);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, DESCRIPTION).set("Force new instance creation. Default: false");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_NEW_INSTANCE, REQUIRED).set(false);

            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, DESCRIPTION).set("Host where to look for free slot.");
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, TYPE).set(ModelType.STRING);
            node.get(REQUEST_PROPERTIES, ATTRIBUTE_INSTANCE_ID, REQUIRED).set(false);

            return node;
        }
    };

}
