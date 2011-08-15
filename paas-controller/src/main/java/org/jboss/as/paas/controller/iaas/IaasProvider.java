/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.deltacloud.client.DeltaCloudClient;
import org.apache.deltacloud.client.DeltaCloudClientException;
import org.apache.deltacloud.client.DeltaCloudClientImpl;
import org.apache.deltacloud.client.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProvider {

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;
    private String imageId;

    private IaasDriver driverDeletage;

    /**
     * @param driver
     * @param url
     * @param username
     * @param password
     * @param imageId
     */
    public IaasProvider(String name, String driver, String url, String username, String password, String imageId) {
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.imageId = imageId;
    }

    private IaasDriver getDeletage() throws Exception, DeltaCloudClientException {
        if (driverDeletage == null) {
            driverDeletage = IaasDriver.Factory.createDriver(this);
        }
        return driverDeletage;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the imageId
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * @return
     * @throws Exception
     * @throws DeltaCloudClientException
     */
    public IaasInstance createInstance() throws DeltaCloudClientException, Exception {
        return getDeletage().createInstance(imageId);
    }

    /**
     * @param instanceId
     * @return
     * @throws Exception
     * @throws DeltaCloudClientException
     */
    public List<String> getPublicAddresses(String instanceId) throws DeltaCloudClientException, Exception {
        return getDeletage().getInstance(instanceId).getPublicAddresses();
    }


}
