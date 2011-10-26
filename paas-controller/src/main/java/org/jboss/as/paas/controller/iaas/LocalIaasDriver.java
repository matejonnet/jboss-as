/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.apache.deltacloud.client.Instance;
import org.jboss.as.paas.controller.extension.UnDeployHandler;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class LocalIaasDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(LocalIaasDriver.class);


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

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#terminateInstance(java.lang.String)
     */
    @Override
    public boolean terminateInstance(String instanceId) {
        //throw new UnsupportedOperationException("Cannot call terminate on local driver. Verify your configuration.");
        log.warn("Terminate instance called on local driver. Skipping termination.");
        return true;
    }

    @Override
    public void close() {}

}
