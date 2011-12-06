/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class VmIaasDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(VmIaasDriver.class);
    private OperationContext context;

    /**
     * @param context
     */
    public VmIaasDriver(OperationContext context) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#getInstance(java.lang.String)
     */
    @Override
    public IaasInstance getInstance(String instanceId) {
        PaasDmrActions paasDmrActions = new PaasDmrActions(context);

        ResourceEntry instance = paasDmrActions.getInstance(instanceId);
        String instanceIp = instance.getModel().get("ip").asString();
        List<String> publicAddresses = Arrays.asList(new String[]{instanceIp});
        return new IaasInstanceVmWrapper(publicAddresses, instanceId);
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
        log.warn("Terminate instance called on vm driver. Skipping termination.");
        return true;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#close()
     */
    @Override
    public void close() {}
}
