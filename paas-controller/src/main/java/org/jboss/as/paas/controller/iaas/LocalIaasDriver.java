/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class LocalIaasDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(LocalIaasDriver.class);
    private IaasProvider iaasProvider;

    public LocalIaasDriver(IaasProvider iaasProvider) {
        // TODO validate required params

        this.iaasProvider = iaasProvider;
    }

    @Override
    public IaasInstance getInstance(String instanceId) {
        return new IaasInstanceLocalWrapper();
    }

    @Override
    public IaasInstance createInstance(String imageId) {
        throw new UnsupportedOperationException("Cannot call instantiate on local driver. Verify your configuration.");
    }

    @Override
    public void terminateInstance(String instanceId) {
        log.warn("Terminate instance called on local driver. Skipping termination.");
    }

    @Override
    public IaasProvider getIaasProvider() {
        return iaasProvider;
    }
}
