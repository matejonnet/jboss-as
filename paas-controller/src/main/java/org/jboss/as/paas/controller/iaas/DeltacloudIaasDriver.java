/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;
import org.jboss.as.paas.controller.domain.IaasProvider;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class DeltacloudIaasDriver implements IaasDriver {

    private static final Logger log = Logger.getLogger(DeltacloudIaasDriver.class);

    private DeltaCloudClientImpl driver;

    private IaasProvider iaasProvider;

    public DeltacloudIaasDriver(IaasProvider iaasProvider) throws MalformedURLException, DeltaCloudClientException {
        // TODO validate required params

        this.iaasProvider = iaasProvider;

        driver = new DeltaCloudClientImpl(iaasProvider.getUrl(), iaasProvider.getUsername(), iaasProvider.getPassword());
    }

    @Override
    public IaasInstance getInstance(String instanceId) {
        try {
            return IaasInstanceFactory.createInstance(driver.listInstances(instanceId));
        } catch (DeltaCloudClientException e) {
            log.errorf(e, "Cannot create instance %s.", instanceId);
            return null;
        }
    }

    @Override
    public IaasInstance createInstance(String imageId) {
        try {
            return IaasInstanceFactory.createInstance(driver.createInstance(imageId));
        } catch (DeltaCloudClientException e) {
            log.errorf(e, "Cannot craete instance from image with id: %s.", imageId);
            return null;
        }
    }

    @Override
    public void terminateInstance(String instanceId) {
        try {
            Instance instance = driver.listInstances(instanceId);
            instance.destroy(driver);
        } catch (DeltaCloudClientException e) {
            log.errorf(e, "Cannot terminate instance %s.", instanceId);
        }
    }

    @Override
    public IaasProvider getIaasProvider() {
        return iaasProvider;
    }
}
