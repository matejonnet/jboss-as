/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

import org.jboss.as.paas.configurator.sys.SysUtil;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasInstanceLocalWrapper implements IaasInstance {

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPublicAddresses()
     */
    @Override
    public List<String> getPublicAddresses() {
        return Arrays.asList(new String[] { SysUtil.getLocalIp() });
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#isRunning()
     */
    @Override
    public boolean isRunning() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getId()
     */
    @Override
    public String getId() {
        return "localhost-instance";
    }

    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPrivateAddresses()
     */
    @Override
    public List<String> getPrivateAddresses() {
        return Arrays.asList(new String[] { SysUtil.getLocalIp() });
    }

}
