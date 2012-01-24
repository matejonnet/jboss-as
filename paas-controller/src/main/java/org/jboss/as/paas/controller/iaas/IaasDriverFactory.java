/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class IaasDriverFactory {

    private static final Logger log = Logger.getLogger(IaasDriverFactory.class);

    static IaasDriver createDriver(IaasProvider iaasProvider) {
        String driverName = iaasProvider.getDriver();
        IaasDriver driver = null;
        if ("local".equals(driverName)) {
            driver = new LocalIaasDriver(iaasProvider);
        } else if ("vm".equals(driverName)) {
            driver = new VmIaasDriver(iaasProvider);
        } else if (driverName.startsWith("jcloud-")) {
            driver = new JCloudIaasDriver(iaasProvider);
        } else if (driverName.startsWith("delta-")) {
            try {
                driver = new DeltacloudIaasDriver(iaasProvider);
            } catch (MalformedURLException e) {
                log.error("Cannot crate DeltacloudIaasDriver.", e);
            } catch (DeltaCloudClientException e) {
                log.error("Cannot crate DeltacloudIaasDriver.", e);
            }
        } else {
            throw new IllegalArgumentException("Unsuported driver [" + driver + "]");
        }

        return driver;
    }

}