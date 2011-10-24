/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DeltacloudIaasDriver implements IaasDriver {

    private DeltaCloudClientImpl driver;

    public DeltacloudIaasDriver(String url, String username, String password) throws MalformedURLException, DeltaCloudClientException {
        driver = new DeltaCloudClientImpl(url, username, password);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#listInstances(java.lang.String)
     */
    @Override
    public IaasInstance getInstance(String instanceId) {
        try {
            return IaasInstance.Factory.createInstance(driver.listInstances(instanceId));
        } catch (DeltaCloudClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#createInstance(java.lang.String)
     */
    @Override
    public IaasInstance createInstance(String imageId) {
        try {
            return IaasInstance.Factory.createInstance(driver.createInstance(imageId));
        } catch (DeltaCloudClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#terminateInstance(java.lang.String)
     */
    @Override
    public boolean terminateInstance(String instanceId) {
        try {
            Instance instance = driver.listInstances(instanceId);
            instance.destroy(driver);
            return true;
        } catch (DeltaCloudClientException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasDriver#close()
     */
    @Override
    public void close() {

    }

}
