/**
 * 
 */
package org.jboss.as.paas.controller;

import java.util.List;

import org.jboss.as.paas.controller.iaas.IaasDriver;
import org.jboss.as.paas.controller.iaas.IaasProvider;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Infrastructure {
    
    private List<ServerInstance> serverInstances;
    //TODO construct iaas
    private IaasDriver iaas;

    public boolean addServerInstance(ServerInstance instance, IaasProvider provider) {
        iaas.createInstance(instance.getImageId());
        
        return serverInstances.add(instance);
    }
}
