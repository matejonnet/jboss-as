package org.jboss.as.paas.controller.operations;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ScaleDownOperation extends UnDeployOperation implements PaasOperation {

    public ScaleDownOperation(String appName) {
        super(appName);
    }

    @Override
    public void execute() {
        removeHostsFromServerGroup(getServerGroupName(), false);
    }

}
