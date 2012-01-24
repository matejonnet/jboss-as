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
    private final Map<String, IaasDriver> drivers = new HashMap<String, IaasDriver>();

    private IaasController() {
        // hide public constructor
    }

    public static IaasController getInstance() {
        return INSTANCE;
    }

    public void addProvider(String name, String driver, String url, String user, String password, String imageId) {
        IaasProvider provider;
        if ("vm".equals(driver)) {
            provider = new IaasProvider(name, driver);
        } else {
            provider = new IaasProvider(name, driver, url, user, password, imageId);
        }
        IaasDriver iaasDriver = IaasDriverFactory.createDriver(provider);
        INSTANCE.drivers.put(name, iaasDriver);
    }

    private IaasProvider getProvider(String providerName) {
        return getDriver(providerName).getIaasProvider();
    }

    private IaasDriver getDriver(String providerName) {
        return INSTANCE.drivers.get(providerName);
    }

    public String createNewInstance(String providerName) throws Exception {
        IaasProvider provider = getProvider(providerName);
        IaasInstance instance = createNewInstance(provider);
        return instance.getId();
    }

    private IaasInstance createNewInstance(IaasProvider provider) throws Exception {
        log.infof("Creating new server instance using %s provider", provider.getName());

        //        InstanceController ic = new InstanceController(getDriver(provider.getName()));
        //        IaasInstance instance = ic.createInstance(provider.getImageId());

        IaasDriver iaasDriver = getDriver(provider.getName());
        IaasInstance instance = iaasDriver.createInstance(provider.getImageId());

        log.debug("Waiting instance to boot ...");

        waitInstanceToBoot(iaasDriver, instance);

        return instance;
    }

    private void waitInstanceToBoot(IaasDriver driver, IaasInstance instance) throws Exception {
        // TODO make configurable
        int maxWaitTime = 120000; // 2min
        long started = System.currentTimeMillis();

        // wait for instance boot up
        while (!instance.isRunning() || instance.getPrivateAddresses().size() == 0) {
            if (instance.isRunning()) {
                instance = reloadInstanceMeta(driver, instance);
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
        IaasDriver driver = getDriver(providerName);

        String hostIp = driver.getInstance(instanceId).getPrivateAddresses().get(0);

        AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
        clusterPaasMngmt.removeRemoteSerer(hostIp);

        driver.terminateInstance(instanceId);
    }

    public String getInstanceIp(String providerName, String instanceId) throws Exception {
        IaasDriver driver = getDriver(providerName);
        return getIaasInstance(driver, instanceId).getPrivateAddresses().get(0);
    }

    public InstanceState getInstanceStatus(String providerName, String instanceId) {
        IaasDriver driver = getDriver(providerName);
        try {
            IaasInstance instance = getIaasInstance(driver, instanceId);
            return instance.getState();
        } catch (Exception e) {
            log.warn("Cannot get instance state.");
            return InstanceState.UNRECOGNIZED;
        }
    }

    public void configureInstance(String remoteIp) {
        new RemoteConfigurator().reconfigureRemote(remoteIp);
    }

    private IaasInstance getIaasInstance(IaasDriver driver, String instanceId) {
        return driver.getInstance(instanceId);
    }

    private IaasInstance reloadInstanceMeta(IaasDriver driver, IaasInstance instance) {
        return getIaasInstance(driver, instance.getId());
    }

}
