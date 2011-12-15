/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.ejb3.deployment.processors;

import org.jboss.as.ejb3.deployment.EjbDeploymentAttachmentKeys;
import org.jboss.as.ejb3.remote.TCCLBasedEJBClientContextSelector;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceController;

/**
 * A deployment processor which associates the {@link EJBClientContext}, belonging to a deployment unit,
 * with the deployment unit's classloader, so that the {@link TCCLBasedEJBClientContextSelector} can then
 * be used to return an appropriate {@link EJBClientContext} based on the classloader.
 *
 * @author Stuart Douglas
 * @author Jaikiran Pai
 */
public class EjbClientContextSetupProcessor implements DeploymentUnitProcessor {


    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final Module module = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.MODULE);
        if (module == null) {
            return;
        }
        final EJBClientContext ejbClientContext = deploymentUnit.getAttachment(EjbDeploymentAttachmentKeys.EJB_CLIENT_CONTEXT);
        final ServiceController<TCCLBasedEJBClientContextSelector> tcclEJBClientContextSelectorServiceController = (ServiceController<TCCLBasedEJBClientContextSelector>) phaseContext.getServiceRegistry().getService(TCCLBasedEJBClientContextSelector.TCCL_BASED_EJB_CLIENT_CONTEXT_SELECTOR_SERVICE_NAME);
        if (tcclEJBClientContextSelectorServiceController != null) {
            final TCCLBasedEJBClientContextSelector tcclBasedEJBClientContextSelector = tcclEJBClientContextSelectorServiceController.getValue();
            // associate the EJB client context with the deployment classloader
            tcclBasedEJBClientContextSelector.registerEJBClientContext(ejbClientContext, module.getClassLoader());
        }
    }

    @Override
    public void undeploy(final DeploymentUnit context) {

    }

}
