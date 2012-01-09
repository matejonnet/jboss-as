/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import static org.jboss.as.controller.client.ControllerClientMessages.MESSAGES;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.paas.controller.iaas.InstanceSlot;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBossRemoteDmrActions {

    private static final Logger log = Logger.getLogger(JBossRemoteDmrActions.class);

    private final ModelControllerClient delegate;

    protected OperationContext context;

    /**
     * @param context
     */
    //    public JBossRemoteDmrActions(OperationContext context) {
    //        this.context = context;
    //    }

    public JBossRemoteDmrActions(InetAddress address, int port) {
        this.delegate = ModelControllerClient.Factory.create(address, port);
    }

    /**
     * @param hostIP
     * @param port
     * @throws UnknownHostException
     */
    public JBossRemoteDmrActions(String hostIP, int port) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(hostIP);
        this.delegate = ModelControllerClient.Factory.create(address, port);
    }

    public void addHostToServerGroup(InstanceSlot slot, String groupName) {
        log.debugf("adding instance [%s] with ip [%s] to server group [%s]", slot.getInstanceId(), slot.getHostIP(), groupName);

        // addHOST to SG
        // /host=master/server-config=server-one:add(socket-binding-group=standard-sockets,
        // socket-binding-port-offset=<portOffset>)
        ModelNode opAddHostToSg = new ModelNode();
        opAddHostToSg.get(OP).set("add");
        opAddHostToSg.get(OP_ADDR).add("host", slot.getHostIP());
        opAddHostToSg.get(OP_ADDR).add("server-config", "server" + slot.getSlotPosition());
        opAddHostToSg.get("group").set(groupName);
        opAddHostToSg.get("auto-start").set(true);
        //TODO validate if it is a bug, that server throws an exception on boot if ref attribute is included
        //opAddHostToSg.get("socket-binding-group").set("standard-sockets");
        opAddHostToSg.get("socket-binding-port-offset").set(slot.getPortOffset());

        ModelNode result = executeForResult(new OperationBuilder(opAddHostToSg).build());

    }

    ModelNode executeForResult(Operation op) {
        try {
            ModelNode result = delegate.execute(op);
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
                return result.get("result");
            } else if (result.hasDefined("failure-description")) {
                throw new RuntimeException(result.get("failure-description").toString());
            } else if (result.hasDefined("domain-failure-description")) {
                throw new RuntimeException(result.get("domain-failure-description").toString());
            } else if (result.hasDefined("host-failure-descriptions")) {
                throw new RuntimeException(result.get("host-failure-descriptions").toString());
            } else {
                throw MESSAGES.operationOutcome(result.get("outcome").asString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelNode execute(ModelNode operation) throws IOException {
        return delegate.execute(operation);
    }

    public ModelNode execute(Operation operation) throws IOException {
        return delegate.execute(operation);
    }

    public void close() throws IOException {
        delegate.close();
    }
}