/**
 *
 */
package org.jboss.as.paas.controller.test.IaaS;

import java.io.IOException;

import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.paas.controller.dmr.executor.ControllerClient;
import org.junit.Assert;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasTest {

    private String remoteHostIp = "172.16.254.173";

    //@Test
    public void waitAsToBoot() throws OperationFormatException, IOException {

        ControllerClient cc = new ControllerClient("admin", 9999, "adminadmin", remoteHostIp);
        ModelControllerClient client = cc.getClient();
        Assert.assertNotNull(client);
    }
}
