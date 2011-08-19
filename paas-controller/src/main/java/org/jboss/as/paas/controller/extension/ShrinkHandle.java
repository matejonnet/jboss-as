/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT_REMOVE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT_UNDEPLOY_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.ParsedArguments;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.DomainController;
import org.jboss.as.paas.controller.Util;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.MapEntry;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ShrinkHandle implements OperationStepHandler {
    public static final ShrinkHandle INSTANCE = new ShrinkHandle();
    public static final String OPERATION_NAME = "shrink";
    private static final String ATTRIBUTE_APP_NAME = "name";

    private final Logger log = Logger.getLogger(ShrinkHandle.class);

    private ShrinkHandle() {}

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

        try {
            Util.removeHostsFromServerGroup(context, Util.getServerGroupName(appName), false);
        } catch (Exception e) {
            //TODO throw new OperationFailedException(e);
            e.printStackTrace();
        }



        context.completeStep();
    }


}
