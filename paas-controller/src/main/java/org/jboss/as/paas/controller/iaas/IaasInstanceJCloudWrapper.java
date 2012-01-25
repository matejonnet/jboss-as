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
class IaasInstanceJCloudWrapper implements IaasInstance {

    private NodeMetadata instance;

    /**
     * @param instance
     */
    IaasInstanceJCloudWrapper(NodeMetadata instance) {
        this.instance = instance;
    }

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

    @Override
    public boolean isRunning() {
        return instance.getState().equals(NodeState.RUNNING);
    }

    @Override
    public String getId() {
        return instance.getId();
    }

    @Override
    public InstanceState getState() {
        NodeState state = instance.getState();
        return InstanceState.valueOf(state.toString());
    }

}
