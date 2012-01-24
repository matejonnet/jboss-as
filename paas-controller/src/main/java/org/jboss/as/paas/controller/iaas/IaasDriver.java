/**
 *
 */
package org.jboss.as.paas.controller.iaas;


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasDriver {

    IaasInstance getInstance(String instanceId);

    IaasInstance createInstance(String imageId);

    void terminateInstance(String instanceId);

    void close();

}
