/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.logging.Logger;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class JCloudIaasDriver implements IaasDriver {

    private static final Logger log = Logger.getLogger(JCloudIaasDriver.class);

    private ComputeServiceContext context;
    private IaasProvider iaasProvider;

    JCloudIaasDriver(IaasProvider iaasProvider) {
        // TODO validate required params

        this.iaasProvider = iaasProvider;

        String driverName = iaasProvider.getDriver();
        String provider = driverName.split("-")[1];

        // get a context with eucalyptus that offers the portable ComputeService api
        Properties overrides = new Properties();
        overrides.setProperty("eucalyptus.endpoint", iaasProvider.getUrl());

        context = new ComputeServiceContextFactory().createContext(provider, iaasProvider.getUsername(), iaasProvider.getPassword(), ImmutableSet.<Module> of(), overrides);
    }

    @Override
    public IaasInstance getInstance(String instanceId) {
        NodeMetadata node = context.getComputeService().getNodeMetadata(instanceId);
        return IaasInstanceFactory.createInstance(node);
    }

    @Override
    public IaasInstance createInstance(String imageId) {
        // pick the highest version of the RightScale CentOs template
        //Template template = context.getComputeService().templateBuilder().osFamily(OsFamily.CENTOS).build();

        Template template;
        try {
            //TODO define instance type (m1.small etc.)
            template = context.getComputeService().templateBuilder().imageId(imageId).minRam(250).build();
        } catch (NoSuchElementException e) {
            log.errorf(e, "Cannot build instance templete from image id %s.", imageId);
            return null;
        }

        template.getOptions().as(EC2TemplateOptions.class).securityGroups("default");
        //TODO use external setting for keypair name
        template.getOptions().as(EC2TemplateOptions.class).keyPair("test");

        try {
            Set<? extends NodeMetadata> nodes = context.getComputeService().createNodesInGroup("jboss-as", 1, template);
            //get first as we always create only one
            NodeMetadata node = nodes.iterator().next();
            return IaasInstanceFactory.createInstance(node);
        } catch (RunNodesException e) {
            log.errorf("Cannot create instance from templete.", e);
        }
        return null;
    }

    @Override
    public void terminateInstance(String instanceId) {
        context.getComputeService().destroyNode(instanceId);
    }

    @Override
    public IaasProvider getIaasProvider() {
        return iaasProvider;
    }

}
