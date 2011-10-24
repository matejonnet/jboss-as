/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.domain.IaasProvider;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasController {

    private static final IaasController INSTANCE = new IaasController();
    private Map<String, IaasProvider> providers = new HashMap<String, IaasProvider>();

    public static void addProvider(String name, String driver, String url, String user, String password, String imageId, OperationContext context) throws MalformedURLException, DeltaCloudClientException {
        IaasProvider provier;
        if ("vm".equals(driver)) {
            provier = new IaasProvider(name, driver, context);
        } else {
            provier = new IaasProvider(name, driver, url, user, password, imageId);
        }
        INSTANCE.providers.put(name, provier);
    }

    /**
     * @param imageId
     * @return
     * @throws Exception
     */
    public static String createNewInstance(String providerName) throws Exception {
        //TODO create new thread to create new server instance and add deployment jobs to queue ?? can domain controller handle this without sleep ?

        IaasProvider provider = INSTANCE.getProvider(providerName);
        IaasInstance instance = provider.createInstance();

        //wait for instance boot up
        while (!instance.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return instance.getId();
    }

    /**
     * @param provider
     * @param instanceId
     * @throws Exception
     * @throws DeltaCloudClientException
     */
    public static boolean terminateInstance(String providerName, String instanceId) throws DeltaCloudClientException, Exception {
        IaasProvider provider = INSTANCE.getProvider(providerName);
        return provider.terminateInstance(instanceId);
    }


    /**
     * @param providerName
     * @return
     */
    private IaasProvider getProvider(String providerName) {
        return providers.get(providerName);
    }

    /**
     * @param instanceId
     * @return
     * @throws Exception
     */
    public static String getInstanceIp(String providerName, String instanceId) throws Exception {
        return INSTANCE.getProvider(providerName).getPublicAddresses(instanceId).get(0);
    }

}
