/**
 * 
 */
package org.jboss.as.paas.controller.iaas;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface IaasDriver {
    public boolean createInstance(String imageId);

}
