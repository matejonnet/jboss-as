/**
 *
 */
package org.jboss.as.paas.controller.test.IaaS;

import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.as.paas.controller.iaas.IaasController;
import org.jboss.as.paas.controller.iaas.IaasDriver;
import org.jboss.as.paas.controller.iaas.IaasInstance;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasOperations {

    IaasProvider provider;
    private String imageId = "Eucalyptus/emi-810916FF";
    private String instanceId = "i-383D0744";

    private IaasProvider getProvider() {
        if (provider == null) {
            provider = new IaasProvider(
                    "euca-provider",
                    "jcloud-eucalyptus",
                    "http://172.16.254.140:8773/services/Eucalyptus",
                    "WKy3rMzOWPouVOxK1p3Ar1C2uRBwa2FBXnCw",
                    "FzAhzP66RQn3OFHToSGvYygxACgvvzFNIdnxg",
                    imageId
                );
        }
        return provider;
    }

    @Test
    public void createInstance() throws Exception {
        IaasProvider provider = getProvider();

        IaasInstance instance = IaasController.createNewInstance(provider);

//        long startTime = System.currentTimeMillis();
//        while (!instance.isRunning()) {
//            if (System.currentTimeMillis() - startTime > 120000) {
//                throw new Exception("Instance hasn't boot in 2 min.");
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }

        Assert.assertNotNull(instance.getId());
        System.out.println("instance id: " + instance.getId());

        Assert.assertTrue("instance.getPrivateAddresses().size() > 0", instance.getPrivateAddresses().size() > 0);
        System.out.println("private ip: " + instance.getPrivateAddresses().get(0));
    }

   // @Test
    public void getLocalAddress() {
        IaasProvider provider = getProvider();
        IaasDriver driver = IaasDriver.Factory.createDriver(provider);
        IaasInstance instance = driver.getInstance(instanceId );
        Assert.assertTrue(instance.getPrivateAddresses().size() > 0);
        System.out.println("privateIp: " + instance.getPrivateAddresses().get(0));
    }
}
