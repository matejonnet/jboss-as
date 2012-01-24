package org.jboss.as.paas.controller.operations;

import java.io.File;
import java.io.FileInputStream;

import org.jboss.as.cli.Util;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorInstance;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Deployment {

    private DmrActionExecutor dmrActionExecutor;

    public Deployment() {
        this.dmrActionExecutor = DmrActionExecutorInstance.get();
    }

    /**
     * @see org.jboss.as.cli.handlers.DeployHandler.addDeployment
     */
    public void addDeployment(final File f, String name, final String runtimeName) {
        ModelNode request = new ModelNode();
        request.get(Util.OPERATION).set(Util.ADD);
        request.get(Util.ADDRESS, Util.DEPLOYMENT).set(name);
        if (runtimeName != null) {
            request.get(Util.RUNTIME_NAME).set(runtimeName);
        }

        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            OperationBuilder op = new OperationBuilder(request);
            op.addInputStream(is);
            request.get(Util.CONTENT).get(0).get(Util.INPUT_STREAM_INDEX).set(0);
            dmrActionExecutor.executeForResult(op.build());
        } catch (Exception e) {
            return;
        } finally {
            org.jboss.as.paas.util.Util.safeClose(is);
        }
    }

    /**
     * @see org.jboss.as.cli.handlers.DeployHandler.replaceDeployment
     */
    public void replaceDeployment(final File f, String name, final String runtimeName) {

        // replace
        final ModelNode request = new ModelNode();
        request.get(Util.OPERATION).set(Util.FULL_REPLACE_DEPLOYMENT);
        request.get(Util.NAME).set(name);
        if (runtimeName != null) {
            request.get(Util.RUNTIME_NAME).set(runtimeName);
        }

        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            OperationBuilder op = new OperationBuilder(request);
            op.addInputStream(is);
            request.get(Util.CONTENT).get(0).get(Util.INPUT_STREAM_INDEX).set(0);
            dmrActionExecutor.executeForResult(op.build());
        } catch (Exception e) {
            return;
        } finally {
            org.jboss.as.paas.util.Util.safeClose(is);
        }
    }
}
