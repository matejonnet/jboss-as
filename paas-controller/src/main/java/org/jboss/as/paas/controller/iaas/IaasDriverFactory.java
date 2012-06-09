/**
 *
 */
package org.jboss.as.paas.controller.iaas;

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
        if ("static".equals(driverName)) {
            driver = new StaticDriver(iaasProvider);
        } else if (driverName.startsWith("jcloud-")) {
            driver = new JCloudIaasDriver(iaasProvider);
        } else {
            throw new IllegalArgumentException("Unsuported driver [" + driver + "]");
        }

        return driver;
    }

}