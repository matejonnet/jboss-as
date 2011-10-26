/**
 *
 */
package org.jboss.as.paas.controller.extension;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    /**
     * @param appName
     * @return
     */
    public String getServerGroupName(String appName) {
        //return appName + "-SG";
        return appName;
    }
}
