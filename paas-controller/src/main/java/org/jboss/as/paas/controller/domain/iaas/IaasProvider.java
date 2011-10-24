/**
 *
 */
package org.jboss.as.paas.controller.domain.iaas;

import java.util.List;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.iaas.IaasDriver;
import org.jboss.as.paas.controller.iaas.IaasInstance;

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
    private OperationContext context;

    private IaasDriver driverDeletage;

    /**
     * @param driver
     * @param url
     * @param username
     * @param password
     * @param imageId
     */
    public IaasProvider(String name, String driver, String url, String username, String password, String imageId) {
        super();
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.imageId = imageId;
    }

    /**
     * @param context
     */
    public IaasProvider(String name, String driver, OperationContext context) {
        super();
        this.name = name;
        this.driver = driver;
        this.context = context;
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
     * @return the context
     */
    public OperationContext getContext() {
        return context;
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
    public boolean terminateInstance(String instanceId) throws DeltaCloudClientException, Exception {
        return getDeletage().terminateInstance(instanceId);
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
