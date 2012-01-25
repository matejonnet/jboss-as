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

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPublicAddresses()
     */
    @Override
    public List<String> getPublicAddresses() {
        return instance.getPublicAddresses();
    }

    @Override
    public List<String> getPrivateAddresses() {
        return instance.getPrivateAddresses();
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#isRunning()
     */
    @Override
    public boolean isRunning() {
        return instance.isRunning();
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getId()
     */
    @Override
    public String getId() {
        return instance.getId();
    }

}
