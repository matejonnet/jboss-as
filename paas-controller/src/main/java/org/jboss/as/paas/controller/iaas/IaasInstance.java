/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasInstance {

    /**
     * @return
     */
    List<String> getPublicAddresses();

    /**
     * @return
     */
    boolean isRunning();

    /**
     * @return
     */
    String getId();

    /**
     * @return
     */
    List<String> getPrivateAddresses();

    InstanceState getState();

}
