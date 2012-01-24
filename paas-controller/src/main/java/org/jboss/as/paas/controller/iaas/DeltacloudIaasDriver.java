/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeltacloudIaasDriver implements IaasDriver {

    private static final Logger log = Logger.getLogger(DeltacloudIaasDriver.class);

    private DeltaCloudClientImpl driver;

    public DeltacloudIaasDriver(String url, String username, String password) throws MalformedURLException, DeltaCloudClientException {
        driver = new DeltaCloudClientImpl(url, username, password);
    }

    @Override
    public IaasInstance getInstance(String instanceId) {
        try {
            return IaasInstance.Factory.createInstance(driver.listInstances(instanceId));
        } catch (DeltaCloudClientException e) {
            log.errorf(e, "Cannot create instance %s.", instanceId);
            return null;
        }
    }

    @Override
    public IaasInstance createInstance(String imageId) {
        try {
            return IaasInstance.Factory.createInstance(driver.createInstance(imageId));
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
    public void close() {

    }

}
