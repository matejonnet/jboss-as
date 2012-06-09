package org.jboss.as.paas.controller.iaas;

import java.util.List;

import org.jclouds.compute.domain.NodeMetadata;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class IaasInstanceFactory {

    static IaasInstance createInstance(List<String> publicAddresses, String instanceId) {
        return new IaasInstanceVmWrapper(publicAddresses, instanceId);
    }

    static IaasInstance createInstance(NodeMetadata instance) {
        return new IaasInstanceJCloudWrapper(instance);
    }

}
