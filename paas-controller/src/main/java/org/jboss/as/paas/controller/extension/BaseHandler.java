/**
 *
 */
package org.jboss.as.paas.controller.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.paas.controller.operations.Operation;
import org.jboss.as.paas.util.Util;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
abstract class BaseHandler {

    private static final Logger log = Logger.getLogger(BaseHandler.class);

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

    protected void scheduleOperation(final Operation operation) {
        //TODO replace with  serviceBuilder.addDependency(ThreadsServices.executorName(executorRef), Executor.class, service.getExecutor());
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

}
