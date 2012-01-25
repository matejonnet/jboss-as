package org.jboss.as.paas.controller.iaas;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public enum InstanceState {

    PENDING,

    TERMINATED,

    SUSPENDED,

    RUNNING,

    ERROR,

    UNRECOGNIZED;

}
