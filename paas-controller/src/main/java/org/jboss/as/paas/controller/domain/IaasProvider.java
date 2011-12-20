/**
 *
 */
package org.jboss.as.paas.controller.domain;

import java.util.List;

import org.apache.deltacloud.client.DeltaCloudClientException;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.paas.controller.iaas.IaasDriver;
import org.jboss.as.paas.controller.iaas.IaasInstance;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
//TODO extract abstract and remove OperationContext from non VM provider. create different implementations
public class IaasProvider {

    private static Logger log = Logger.getLogger(IaasProvider.class);

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



    private IaasDriver getDeletage() throws Exception {
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
    public IaasInstance createInstance() throws Exception {
        log.debugf("Booting instance from image [%s]", imageId);
        return getDeletage().createInstance(imageId);
    }

    /**
     * @param instanceId
     * @return
     * @throws Exception
     * @throws DeltaCloudClientException
     */
    public boolean terminateInstance(String instanceId) throws Exception {
        return getDeletage().terminateInstance(instanceId);
    }

    /**
     * @param instanceId
     * @return
     * @throws Exception
     * @throws DeltaCloudClientException
     */
    public List<String> getPublicAddresses(String instanceId) throws Exception {
        return getDeletage().getInstance(instanceId).getPublicAddresses();
    }

    /**
     * @param instance
     * @return reloaded instance
     * @throws Exception
     */
    public IaasInstance reloadInstanceMeta(IaasInstance instance) throws Exception {
        return getDeletage().getInstance(instance.getId());
    }

    /**
     * @param instanceId
     * @return
     * @throws Exception
     */
    public List<String> getPrivateAddresses(String instanceId) throws Exception {
        return getDeletage().getInstance(instanceId).getPrivateAddresses();
    }



}
