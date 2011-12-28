/**
 *
 */
package org.jboss.as.paas.configurator.sys;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class SysUtil {

    public static String getLocalIp() {
        return System.getProperty("address.local.ip");
    }
}
