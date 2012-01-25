/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorInstance;
import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class StaticDriver implements IaasDriver {

    private final Logger log = Logger.getLogger(StaticDriver.class);
    private DmrActionExecutor dmrActionExecutor;
    private IaasProvider iaasProvider;

    public StaticDriver(IaasProvider iaasProvider) {
        // TODO validate required params

        this.dmrActionExecutor = DmrActionExecutorInstance.get();
        this.iaasProvider = iaasProvider;
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
    public IaasProvider getIaasProvider() {
        return iaasProvider;
    }
}
