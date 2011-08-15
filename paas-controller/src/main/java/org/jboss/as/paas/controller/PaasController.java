/**
 *
 */
package org.jboss.as.paas.controller;

import java.util.List;

import org.jboss.as.paas.controller.extension.IaasProviderAddHandler;
import org.jboss.as.paas.controller.iaas.IaasProvider;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @deprecated use IaasController
 */
@Deprecated
public class PaasController {

    public static final PaasController INSTANCE = new PaasController();

    private Infrastructure infrastructure = new Infrastructure();

    private PaasController() {}

    public void addIaasProvider() {

    }

    public void addServerInstance(List<String> serverGroups, IaasProvider provider) {
        ServerInstance instance = new ServerInstance();
        instance.setServerGroups(serverGroups);
        //instance.setImageId(provider);
        infrastructure.addServerInstance(instance, provider);
    }

    /**
     * Adds another server instance to serverGroup
     */
    public void expandServerGroup(String serverGroup) {

    }


}
