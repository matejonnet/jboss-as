/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.jboss.as.paas.controller.domain.IaasProvider;
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

        context = new ComputeServiceContextFactory()
        //.createContext("eucalyptus", accesskeyid ,secretkey,ImmutableSet.<Module> of(new Log4JLoggingModule(), new JschSshClientModule()), overrides);
        .createContext(provider, iaasProvider.getUsername(), iaasProvider.getPassword(), ImmutableSet.<Module> of(), overrides);
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
            // TODO: handle exception; return message ( _imageId_ not found) to CLI
            // TODO: handle exception; org.jclouds.aws.AWSResponseException: request POST http://10.30.1.3:8773/services/Eucalyptus/ HTTP/1.1 failed with code 400, error: AWSError{requestId='3df2cdb1-efce-4259-8585-beabc0d7d16c', requestToken='null', code='FinishedVerify', message='Not enough resources (0 < 1: vm instances.
            // [Host Controller] 14:51:57,856 ERROR [stderr] (Remoting "192.168.37.66:MANAGEMENT" task-1) Not enough resources (0 < 1: vm instances.', context='{Response=, Errors=}'}

            return null;
        }

        // specify your own groups which already have the correct rules applied
        //TODO-ML specify security group
        template.getOptions().as(EC2TemplateOptions.class).securityGroups("default");

        try {
            Set<? extends NodeMetadata> nodes = context.getComputeService().createNodesInGroup("jboss-as", 1, template);
            //get first as we always create only one
            NodeMetadata node = nodes.iterator().next();
            return IaasInstanceFactory.createInstance(node);
        } catch (RunNodesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
