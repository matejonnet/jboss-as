/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class IaasInstanceLocalWrapper implements IaasInstance {


    /* (non-Javadoc)
     * @see org.jboss.as.paas.controller.iaas.IaasInstance#getPublicAddresses()
     */
    @Override
    public List<String> getPublicAddresses() {
//        try {
            //TODO problem with multiple eth interfaces
            //return Arrays.asList(new String[]{InetAddress.getLocalHost().getHostAddress()});
            return Arrays.asList(new String[]{"127.0.0.1"});
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return null;
//        }
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
        // TODO Auto-generated method stub
        return Arrays.asList(new String[]{"127.0.0.1"});
    }

}
