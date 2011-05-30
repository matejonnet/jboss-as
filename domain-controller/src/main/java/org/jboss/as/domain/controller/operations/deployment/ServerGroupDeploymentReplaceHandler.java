/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.domain.controller.operations.deployment;

import java.util.Locale;
import org.jboss.as.controller.NewOperationContext;
import org.jboss.as.controller.NewStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLACE_DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TO_REPLACE;
import org.jboss.as.controller.descriptions.common.DeploymentDescription;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.domain.controller.FileRepository;
import org.jboss.dmr.ModelNode;

/**
 * Handles replacement in the runtime of one deployment by another.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class ServerGroupDeploymentReplaceHandler implements NewStepHandler, DescriptionProvider {

    public static final String OPERATION_NAME = REPLACE_DEPLOYMENT;

    static final ModelNode getOperation(ModelNode address) {
        return Util.getEmptyOperation(OPERATION_NAME, address);
    }

    private final FileRepository fileRepository;
    private final ParametersValidator validator = new ParametersValidator();

    public ServerGroupDeploymentReplaceHandler(final FileRepository fileRepository) {
        if (fileRepository == null) {
            throw new IllegalArgumentException("fileRepository is null");
        }
        this.fileRepository = fileRepository;
        this.validator.registerValidator(NAME, new StringLengthValidator(1));
        this.validator.registerValidator(TO_REPLACE, new StringLengthValidator(1));
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        return DeploymentDescription.getDeployDeploymentOperation(locale);
    }

    public void execute(NewOperationContext context, ModelNode operation) throws OperationFailedException {
        validator.validate(operation);

        String name = operation.require(NAME).asString();
        String toReplace = operation.require(TO_REPLACE).asString();

        if (name.equals(toReplace)) {
            throw operationFailed(String.format("Cannot use %s with the same value for parameters %s and %s. " +
                    "Use %s to redeploy the same content or %s to replace content with a new version with the same name.",
                    OPERATION_NAME, NAME, TO_REPLACE, ServerGroupDeploymentRedeployHandler.OPERATION_NAME,
                    DeploymentFullReplaceHandler.OPERATION_NAME));
        }

        final PathElement deploymentPath = PathElement.pathElement(DEPLOYMENT, name);
        final PathElement replacePath = PathElement.pathElement(DEPLOYMENT, toReplace);

        Resource domainDeployment;
        try {
            // check if the domain deployment exists
            domainDeployment = context.getRootResource().requireChild(deploymentPath);
        } catch (Exception e) {
            throw operationFailed(String.format("No deployment with name %s found", name));
        }

        final ModelNode deployment = domainDeployment.getModel();
        for (ModelNode content : deployment.require(CONTENT).asList()) {
            if ((content.hasDefined(HASH))) {
                byte[] hash = content.require(HASH).asBytes();
                // Ensure the local repo has the files
                fileRepository.getDeploymentFiles(hash);
            }
        }

        final Resource serverGroup = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS);
        if (! serverGroup.hasChild(replacePath)) {
            throw operationFailed(String.format("No deployment with name %s found", toReplace));
        }
        final Resource replaceResource = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS.append(replacePath));
        //
        final Resource deploymentResource;
        if(! serverGroup.hasChild(deploymentPath)) {
            final Resource resource = context.createResource(PathAddress.EMPTY_ADDRESS.append(deploymentPath));
            final ModelNode deployNode = resource.getModel();
            deployNode.set(deployment); // Get the information from the domain deployment
            deployNode.remove("content"); // Prune the content information
            deployNode.get(ENABLED).set(true); // Enable
        } else {
            deploymentResource = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS.append(deploymentPath));
            if(deploymentResource.getModel().get(ENABLED).asBoolean()) {
                throw operationFailed(String.format("Deployment %s is already started", toReplace));
            }
        }
        //
        replaceResource.getModel().get(ENABLED).set(false);
        context.completeStep();
    }

    private static OperationFailedException operationFailed(String msg) {
        return new OperationFailedException(new ModelNode().set(msg));
    }
}
