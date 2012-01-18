/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.paas.controller.dmr.JBossDmrActions;
import org.jboss.as.paas.controller.dmr.OperationStepRegistry;
import org.jboss.as.paas.controller.dmr.PaasDmrActions;
import org.jboss.as.paas.controller.dmr.ResultMessagesDmrActions;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutor;
import org.jboss.as.paas.controller.dmr.executor.DmrActionExecutorInstance;
import org.jboss.as.paas.controller.operationqueue.DmrOperations;
import org.jboss.as.paas.controller.operationqueue.OperationQueue;
import org.jboss.as.paas.controller.operations.PaasOperation;
import org.jboss.as.paas.util.Util;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    private static final Logger log = Logger.getLogger(BaseHandler.class);

    protected JBossDmrActions jbossDmrActions;
    protected PaasDmrActions paasDmrActions;
    protected OperationStepRegistry stepRegistry;
    protected DmrOperations dmrOperations;
    protected ResultMessagesDmrActions messagesDmrActions;
    protected OperationQueue operationQueue;

    /**
     * Returns false if execution is not on domain controller
     *
     * @param context
     */
    public boolean execute(OperationContext context) {
        log.debug(">>>>>>>>> Handle.execute ");

        if (!isDomainController(context)) {
            context.completeStep();
            return false;
        }
        return true;
    }

    protected void completeStep(OperationContext context) {
        messagesDmrActions.addMessgesToContext();
        context.completeStep();
    }

    /**
     * used to execute operation after context.completeStep
     * method should be called just before return of execute method
     */
    protected void onReturn() {
        operationQueue.executeAsync();
        //remoteDmrActions.executeOnComplete();
    }

    protected void scheduleOperation(final PaasOperation operation) {

        //context.getServiceRegistry(modify)
        //serviceBuilder.addDependency(ThreadsServices.executorName(executorRef), Executor.class, service.getExecutor());

        new Thread(new Runnable() {

            @Override
            public void run() {
                operation.execute();
            }
        }).start();
    }

    private boolean isDomainController(OperationContext context) {
        Resource rootResource = context.getRootResource();

        String localIp = Util.getLocalIp();

        PathAddress addr = PathAddress.pathAddress(PathElement.pathElement("host", localIp));

        final Resource resource = rootResource.navigate(addr);
        String domainController = resource.getModel().get("domain-controller").asPropertyList().get(0).getName();
        return "local".equals(domainController);
    }

    protected DmrActionExecutor getActionExecutor() {
        return DmrActionExecutorInstance.get();
    }

}
