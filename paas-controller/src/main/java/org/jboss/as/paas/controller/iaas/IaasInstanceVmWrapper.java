/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasInstanceVmWrapper implements IaasInstance {

    private List<String> publicAddresses;
    private String instanceId;

    /**
     * @param publicAddresses
     * @param instanceId
     */
    public IaasInstanceVmWrapper(List<String> publicAddresses, String instanceId) {
        super();
        this.publicAddresses = publicAddresses;
        this.instanceId = instanceId;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPublicAddresses()
     */
    @Override
    public List<String> getPublicAddresses() {
        return publicAddresses;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#isRunning()
     */
    @Override
    public boolean isRunning() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getId()
     */
    @Override
    public String getId() {
        return instanceId;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPrivateAddresses()
     */
    @Override
    public List<String> getPrivateAddresses() {
        //TODO warning
        return publicAddresses;
    }

}
