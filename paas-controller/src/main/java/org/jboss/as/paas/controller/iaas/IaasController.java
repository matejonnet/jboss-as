/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.paas.configurator.client.RemoteConfigurator;
import org.jboss.as.paas.controller.AsClusterPassManagement;
import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.logging.Logger;

/**
 * Singlethon that holds list of IaaS providers
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasController {
    private static final Logger log = Logger.getLogger(IaasController.class);

    private static final IaasController INSTANCE = new IaasController();
    private final Map<String, IaasProvider> providers = new HashMap<String, IaasProvider>();

    private IaasController() {
        // hide public constructor
    }

    public static IaasController getInstance() {
        return INSTANCE;
    }

    public void addProvider(String name, String driver, String url, String user, String password, String imageId) {
        IaasProvider provier;
        if ("vm".equals(driver)) {
            provier = new IaasProvider(name, driver);
        } else {
            provier = new IaasProvider(name, driver, url, user, password, imageId);
        }
        INSTANCE.providers.put(name, provier);
    }

    public String createNewInstance(String providerName) throws Exception {
        IaasProvider provider = INSTANCE.getProvider(providerName);
        return createNewInstance(provider).getId();
    }

    public IaasInstance createNewInstance(IaasProvider provider) throws Exception {
        log.infof("Creating new server instance using %s provider", provider.getName());

        IaasInstance instance = provider.createInstance();

        log.debug("Waiting instance to boot ...");

        waitInstanceToBoot(provider, instance);

        return instance;
    }

    private void waitInstanceToBoot(IaasProvider provider, IaasInstance instance) throws Exception {
        // TODO make configurable
        int maxWaitTime = 120000; // 2min
        long started = System.currentTimeMillis();

        // wait for instance boot up
        while (!instance.isRunning() || instance.getPrivateAddresses().size() == 0) {
            if (instance.isRunning()) {
                instance = provider.reloadInstanceMeta(instance);
            }

            if (System.currentTimeMillis() - started > maxWaitTime) {
                throw new RuntimeException("Instance hasn't boot in " + maxWaitTime / 1000 + "seconds.");
            }
            try {
                log.debug("Waiting instance to boot. Going to sleep for 1000.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Waiting instance to boot. Interupted.");
            }
        }
    }

    public void terminateInstance(String providerName, String instanceId) throws Exception {
        IaasProvider provider = INSTANCE.getProvider(providerName);

        String hostIp = provider.getPrivateAddresses(instanceId).get(0);

        AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
        clusterPaasMngmt.removeRemoteSerer(hostIp);

        provider.terminateInstance(instanceId);
    }

    public InstanceState getInstanceStatus(String providerName, String instanceId) {
        IaasProvider provider = INSTANCE.getProvider(providerName);
        IaasInstance instance;
        try {
            instance = provider.getInstance(instanceId);
            return instance.getState();
        } catch (Exception e) {
            log.warn("Cannot get instance state.");
            return InstanceState.UNRECOGNIZED;
        }
    }

    private IaasProvider getProvider(String providerName) {
        return providers.get(providerName);
    }

    public String getInstanceIp(String providerName, String instanceId) throws Exception {
        return INSTANCE.getProvider(providerName).getPrivateAddresses(instanceId).get(0);
    }

    public void configureInstance(String remoteIp) {
        new RemoteConfigurator().reconfigureRemote(remoteIp);
    }
}
