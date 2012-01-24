/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class LocalIaasDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(LocalIaasDriver.class);

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
    public void close() {}

}
