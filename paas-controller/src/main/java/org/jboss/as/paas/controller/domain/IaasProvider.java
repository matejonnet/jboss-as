/**
 *
 */
package org.jboss.as.paas.controller.domain;


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

}
