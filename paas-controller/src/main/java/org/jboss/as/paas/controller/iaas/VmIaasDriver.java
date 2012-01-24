/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorInstance;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class VmIaasDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(VmIaasDriver.class);
    private DmrActionExecutor dmrActionExecutor;

    public VmIaasDriver() {
        this.dmrActionExecutor = DmrActionExecutorInstance.get();
    }

    @Override
    public IaasInstance getInstance(String instanceId) {
        PaasDmrActions paasDmrActions = new PaasDmrActions(dmrActionExecutor);

        Instance instance = paasDmrActions.getInstance(instanceId);
        List<String> publicAddresses = Arrays.asList(new String[] { instance.getHostIP() });
        return new IaasInstanceVmWrapper(publicAddresses, instanceId);
    }

    @Override
    public IaasInstance createInstance(String imageId) {
        throw new UnsupportedOperationException("Cannot call instantiate on local driver. Verify your configuration.");
    }

    @Override
    public void terminateInstance(String instanceId) {
        log.warn("Terminate instance called on vm driver. Skipping termination.");
    }

    @Override
    public void close() {}
}
