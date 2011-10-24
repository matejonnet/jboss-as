/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.List;

import org.apache.deltacloud.client.Instance;
import org.jclouds.compute.domain.NodeMetadata;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasInstance {

    public class Factory {

        /**
         * @param listInstances
         * @return
         */
        public static IaasInstance createInstance(Instance instance) {
            return new IaasInstanceDeltaClWrapper(instance);
        }

        public static IaasInstance createInstance(List<String> publicAddresses, String instanceId) {
            return new IaasInstanceVmWrapper(publicAddresses, instanceId);
        }

        public static IaasInstance createInstance(NodeMetadata instance) {
            return new IaasInstanceJCloudWrapper(instance);
        }



    }

    /**
     * @return
     */
    public List<String> getPublicAddresses();


    /**
     * @return
     */
    public boolean isRunning();


    /**
     * @return
     */
    public String getId();


}
