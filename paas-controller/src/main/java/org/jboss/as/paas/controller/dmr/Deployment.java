package org.jboss.as.paas.controller.dmr;

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
     * @see org.jboss.as.cli.handlers.addDeployment
     */
    public void addDeployment(final File f, String name, final String runtimeName) {
        ModelNode request = new ModelNode();
        request.get(Util.OPERATION).set(Util.ADD);
        request.get(Util.ADDRESS, Util.DEPLOYMENT).set(name);
        if (runtimeName != null) {
            request.get(Util.RUNTIME_NAME).set(runtimeName);
        }

        ModelNode result;
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            OperationBuilder op = new OperationBuilder(request);
            op.addInputStream(is);
            request.get(Util.CONTENT).get(0).get(Util.INPUT_STREAM_INDEX).set(0);
            result = dmrActionExecutor.executeForResult(op.build());
        } catch (Exception e) {
            return;
        } finally {
            org.jboss.as.paas.util.Util.safeClose(is);
        }

        //TODO
        //        if (!Util.isSuccess(result)) {
        //            return;
        //        }
    }

    /**
     * @see org.jboss.as.cli.handlers.replaceDeployment
     */
    public void replaceDeployment(final File f, String name, final String runtimeName) {

        ModelNode result;

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
            result = dmrActionExecutor.executeForResult(op.build());
        } catch (Exception e) {
            //ctx.printLine("Failed to replace the deployment: " + e.getLocalizedMessage());
            return;
        } finally {
            org.jboss.as.paas.util.Util.safeClose(is);
        }
        //        if(!Util.isSuccess(result)) {
        //            ctx.printLine(Util.getFailureDescription(result));
        //            return;
        //        }
    }
}
