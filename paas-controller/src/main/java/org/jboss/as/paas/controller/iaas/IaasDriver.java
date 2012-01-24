/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.jboss.as.paas.controller.domain.IaasProvider;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasDriver {

    IaasInstance getInstance(String instanceId);

    IaasInstance createInstance(String imageId);

    void terminateInstance(String instanceId);

    IaasProvider getIaasProvider();

}
