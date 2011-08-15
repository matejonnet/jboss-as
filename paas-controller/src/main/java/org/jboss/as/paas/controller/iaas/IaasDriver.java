/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasDriver {

    /**
     * @param instanceId
     * @return
     */
    IaasInstance getInstance(String instanceId);

    /**
     * @param imageId
     * @return
     */
    IaasInstance createInstance(String imageId);

    public class Factory {

        /**
         * @param iaasProvider
         * @return
         */
        public static IaasDriver createDriver(IaasProvider iaasProvider) {
            String driverName = iaasProvider.getDriver();
            IaasDriver driver = null;
            if ("local".equals(driverName)) {
                driver = new LocalIaasDriver();
            } else if ("vm".equals(driverName)) {
                //TODO implement
            } else if (driverName.startsWith("delta-")) {
                //TODO validate required params
                try {
                    driver = new DeltacloudIaasDriver(iaasProvider.getUrl(), iaasProvider.getUsername(), iaasProvider.getPassword());
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (DeltaCloudClientException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException("Unsuported driver [" + driver + "]");
            }

            if (driver == null) {
                //TODO
            }
            return driver;
        }

    }
}
