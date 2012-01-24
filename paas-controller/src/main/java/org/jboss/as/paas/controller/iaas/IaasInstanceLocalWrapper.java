/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jboss.as.paas.util.Util;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class IaasInstanceLocalWrapper implements IaasInstance {

    @Override
    public List<String> getPublicAddresses() {
        return Arrays.asList(new String[] { Util.getLocalIp() });
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public String getId() {
        return "localhost-instance";
    }

    @Override
    public List<String> getPrivateAddresses() {
        return Arrays.asList(new String[] { Util.getLocalIp() });
    }

    @Override
    public InstanceState getState() {
        return InstanceState.RUNNING;
    }

}
