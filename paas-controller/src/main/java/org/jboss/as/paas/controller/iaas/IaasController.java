/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.controller.OperationContext;
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

    public void addProvider(String name, String driver, String url, String user, String password, String imageId, OperationContext context) throws MalformedURLException, DeltaCloudClientException {
        IaasProvider provier;
        if ("vm".equals(driver)) {
            provier = new IaasProvider(name, driver, context);
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

        // TODO create new thread to create new server instance and add
        // deployment jobs to queue ?? can domain controller handle this without
        // sleep ?

        IaasInstance instance = provider.createInstance();

        // TODO make configurable
        int maxWaitTime = 120000; // 2min
        long started = System.currentTimeMillis();

        log.debug("Waiting instance to boot ...");
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return instance;
    }

    public boolean terminateInstance(String providerName, String instanceId) throws Exception {
        IaasProvider provider = INSTANCE.getProvider(providerName);

        String hostIp = provider.getPrivateAddresses(instanceId).get(0);

        AsClusterPassManagement clusterPaasMngmt = new AsClusterPassManagement();
        clusterPaasMngmt.removeRemoteSerer(hostIp);

        return provider.terminateInstance(instanceId);
    }

    private IaasProvider getProvider(String providerName) {
        return providers.get(providerName);
    }

    public String getInstanceIp(String providerName, String instanceId) throws Exception {
        return INSTANCE.getProvider(providerName).getPrivateAddresses(instanceId).get(0);
    }

}
