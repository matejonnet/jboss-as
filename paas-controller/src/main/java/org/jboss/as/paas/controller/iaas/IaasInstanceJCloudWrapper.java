/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasInstanceJCloudWrapper implements IaasInstance {

    private NodeMetadata instance;

    /**
     * @param instance
     */
    public IaasInstanceJCloudWrapper(NodeMetadata instance) {
        this.instance = instance;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPublicAddresses()
     */
    @Override
    public List<String> getPublicAddresses() {
        String[] addresses = instance.getPublicAddresses().toArray(new String[0]);
        return Arrays.asList(addresses);
    }

    @Override
    public List<String> getPrivateAddresses() {
        String[] addresses = instance.getPrivateAddresses().toArray(new String[0]);
        return Arrays.asList(addresses);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#isRunning()
     */
    @Override
    public boolean isRunning() {
        return instance.getState().equals(NodeState.RUNNING);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getId()
     */
    @Override
    public String getId() {
        return instance.getId();
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getState()
     */
    @Override
    public InstanceState getState() {
        NodeState state = instance.getState();
        return InstanceState.valueOf(state.toString());
    }

}
