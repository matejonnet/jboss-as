/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class IaasInstanceVmWrapper implements IaasInstance {

    private List<String> publicAddresses;
    private String instanceId;

    /**
     * @param publicAddresses
     * @param instanceId
     */
    IaasInstanceVmWrapper(List<String> publicAddresses, String instanceId) {
        super();
        this.publicAddresses = publicAddresses;
        this.instanceId = instanceId;
    }

    @Override
    public List<String> getPublicAddresses() {
        return publicAddresses;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public String getId() {
        return instanceId;
    }

    @Override
    public List<String> getPrivateAddresses() {
        //TODO add private address property to instance xml tag
        return publicAddresses;
    }

    @Override
    public InstanceState getState() {
        //TODO check if instance if really up and running
        return InstanceState.RUNNING;
    }

}
