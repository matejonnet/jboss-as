package org.jboss.as.paas.controller.operationqueue;

import org.jboss.as.paas.controller.domain.Instance;
import org.jboss.as.paas.controller.iaas.IaasController;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TerminateInstances implements Operation {

    private String providerName;
    private Instance instance;

    public TerminateInstances(String providerName, Instance instance) {
        super();
        this.providerName = providerName;
        this.instance = instance;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        try {
            IaasController.getInstance().terminateInstance(providerName, instance.getInstanceId());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
