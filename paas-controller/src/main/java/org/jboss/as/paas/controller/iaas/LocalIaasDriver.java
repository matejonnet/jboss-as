/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.apache.deltacloud.client.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class LocalIaasDriver implements IaasDriver {

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#listInstances(java.lang.String)
     */
    @Override
    public IaasInstance getInstance(String instanceId) {
        return new IaasInstanceLocalWrapper();
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#createInstance(java.lang.String)
     */
    @Override
    public IaasInstance createInstance(String imageId) {
        throw new UnsupportedOperationException("Cannot call instantiate on local driver. Verify your configuration.");
    }

}
