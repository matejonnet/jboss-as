/**
 *
 */
package org.jboss.as.paas.controller.domain;

import java.util.List;

import org.jboss.as.paas.controller.iaas.IaasDriver;
import org.jboss.as.paas.controller.iaas.IaasDriverFactory;
import org.jboss.as.paas.controller.iaas.IaasInstance;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasProvider {

    private static Logger log = Logger.getLogger(IaasProvider.class);

    private String name;
    private String driver;
    private String url;
    private String username;
    private String password;
    private String imageId;

    private IaasDriver driverDeletage;

    public IaasProvider(String name, String driver, String url, String username, String password, String imageId) {
        super();
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.imageId = imageId;
    }

    public IaasProvider(String name, String driver) {
        super();
        this.name = name;
        this.driver = driver;
    }

    private IaasDriver getDeletage() throws Exception {
        if (driverDeletage == null) {
            driverDeletage = IaasDriverFactory.createDriver(this);
        }
        return driverDeletage;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getImageId() {
        return imageId;
    }

    public IaasInstance createInstance() throws Exception {
        log.debugf("Booting instance from image [%s]", imageId);
        return getDeletage().createInstance(imageId);
    }

    public void terminateInstance(String instanceId) throws Exception {
        getDeletage().terminateInstance(instanceId);
    }

    public List<String> getPublicAddresses(String instanceId) throws Exception {
        return getDeletage().getInstance(instanceId).getPublicAddresses();
    }

    public IaasInstance getInstance(String instanceId) throws Exception {
        return getDeletage().getInstance(instanceId);
    }

    public IaasInstance reloadInstanceMeta(IaasInstance instance) throws Exception {
        return getDeletage().getInstance(instance.getId());
    }

    public List<String> getPrivateAddresses(String instanceId) throws Exception {
        return getDeletage().getInstance(instanceId).getPrivateAddresses();
    }

}
