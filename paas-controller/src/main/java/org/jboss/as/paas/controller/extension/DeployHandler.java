/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.io.File;
import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.paas.controller.PaasProcessor;
import org.jboss.as.paas.controller.dmr.CompositeDmrActions;
import org.jboss.as.paas.controller.dmr.JbossDmrActions;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeployHandler extends BaseHandler implements OperationStepHandler {
    public static final DeployHandler INSTANCE = new DeployHandler();
    public static final String OPERATION_NAME = "deploy";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_PROVIDER = "provider";
    private static final String ATTRIBUTE_NEW_INSTANCE = "new-instance";

    private final Logger log = Logger.getLogger(DeployHandler.class);


    private DeployHandler() {}

    /* (non-Javadoc)
     * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
     */
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {

        PaasDmrActions paasDmrActions = new PaasDmrActions(context);
        JbossDmrActions jbossDmrActions = new JbossDmrActions(context);
        CompositeDmrActions compositeDmrActions = new CompositeDmrActions(context);
        PaasProcessor paasProcessor = new PaasProcessor();

        System.out.println(">>>>>>>>> DeployHandle.execute ");
        //TODO create interceptor annotation
        if (!jbossDmrActions.isDomainController()) {
            context.completeStep();
            return;
        }
        System.out.println(">>>>>>>>> DeployHandle.execute: continue ... ");

        final String filePath = operation.get(ATTRIBUTE_PATH).asString();
        final String provider = operation.get(ATTRIBUTE_PROVIDER).asString();
        final boolean newInstance = operation.get(ATTRIBUTE_NEW_INSTANCE).isDefined() ? operation.get(ATTRIBUTE_NEW_INSTANCE).asBoolean() : false;


        //TODO validate required attributes

        final File f;
        if(filePath != null) {
            f = new File(filePath);
            if(!f.exists()) {
                String message = "Path " + f.getAbsolutePath() + " doesn't exist. File must be located on localhost.";
                context.getResult().add(message);
                log.warn(message);
                //TODO remove
                System.out.println(message);
                return;
            }
            if(f.isDirectory()) {
                String message = f.getAbsolutePath() + " is a directory.";
                context.getResult().add(message);
                log.warn(message);
                //TODO remove
                System.out.println(message);
                return;
            }
        } else {
            f = null;
        }

        String appName = f.getName();
        String serverGroupName = getServerGroupName(appName);


        paasDmrActions.createServerGroup(serverGroupName);

        InstanceSlot slot = paasProcessor.getSlot(newInstance, getServerGroupName(appName), context, provider);
        compositeDmrActions.addHostToServerGroup(slot, getServerGroupName(appName));

        jbossDmrActions.deployToServerGroup(f, appName, serverGroupName);

        context.completeStep();
    }

    /**
     * DescriptionProvider.
     */
    public static DescriptionProvider DESC = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {

            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Deploy application.");

            node.get(ATTRIBUTES, ATTRIBUTE_PATH, DESCRIPTION).set("Path to file.");
            node.get(ATTRIBUTES, ATTRIBUTE_PATH, TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, ATTRIBUTE_PATH, REQUIRED).set(true);

            node.get(ATTRIBUTES, ATTRIBUTE_PROVIDER, DESCRIPTION).set("Provider name.");
            node.get(ATTRIBUTES, ATTRIBUTE_PROVIDER, TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, ATTRIBUTE_PROVIDER, REQUIRED).set(true);

            node.get(ATTRIBUTES, ATTRIBUTE_NEW_INSTANCE, DESCRIPTION).set("Force new instance creation. Default: false");
            node.get(ATTRIBUTES, ATTRIBUTE_NEW_INSTANCE, TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, ATTRIBUTE_NEW_INSTANCE, REQUIRED).set(false);

            return node;
        }
     };









}
