/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Properties;
import java.util.Set;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JCloudIaasDriver implements IaasDriver {

    ComputeServiceContext context;

    /**
     *
     * @param url eucalyptus url eg. "http://173.205.188.130:8773/services/Eucalyptus"
     * @param username
     * @param password
     */
    public JCloudIaasDriver(String provider, String url, String accesskeyid, String secretkey) {
     // get a context with eucalyptus that offers the portable ComputeService api
        Properties overrides = new Properties();
        overrides.setProperty("eucalyptus.endpoint", url);
        context = new ComputeServiceContextFactory()
                //.createContext("eucalyptus", accesskeyid ,secretkey,ImmutableSet.<Module> of(new Log4JLoggingModule(), new JschSshClientModule()), overrides);
                .createContext(provider, accesskeyid ,secretkey, ImmutableSet.<Module> of(), overrides);
    }


    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#getInstance(java.lang.String)
     */
    @Override
    public IaasInstance getInstance(String instanceId) {
        NodeMetadata node = context.getComputeService().getNodeMetadata(instanceId);
        return IaasInstance.Factory.createInstance(node);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#createInstance(java.lang.String)
     */
    @Override
    public IaasInstance createInstance(String imageId) {
        // pick the highest version of the RightScale CentOs template
        Template template = context.getComputeService().templateBuilder().osFamily(OsFamily.CENTOS).build();

        // specify your own groups which already have the correct rules applied
        //TODO-ML specify security group
        template.getOptions().as(EC2TemplateOptions.class).securityGroups("default");

        try {
            Set<? extends NodeMetadata> nodes = context.getComputeService().createNodesInGroup("jboss-as", 1, template);
            NodeMetadata node = nodes.iterator().next();
            return IaasInstance.Factory.createInstance(node);
        } catch (RunNodesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#terminateInstance(java.lang.String)
     */
    @Override
    public boolean terminateInstance(String instanceId) {
        context.getComputeService().destroyNode(instanceId);
        //TODO-ML validate ?
        return true;
    }

    @Override
    public void close() {
        context.close();
    }

}
