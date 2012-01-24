/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.List;

import org.apache.deltacloud.client.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasInstanceDeltaClWrapper implements IaasInstance {

    private Instance instance;

    /**
     * @param instance
     */
    public IaasInstanceDeltaClWrapper(Instance instance) {
        this.instance = instance;
    }

    @Override
    public List<String> getPublicAddresses() {
        return instance.getPublicAddresses();
    }

    @Override
    public List<String> getPrivateAddresses() {
        return instance.getPrivateAddresses();
    }

    @Override
    public boolean isRunning() {
        return instance.isRunning();
    }

    @Override
    public String getId() {
        return instance.getId();
    }

    @Override
    public InstanceState getState() {
        // TODO Auto-generated method stub
        return null;
    }

}
