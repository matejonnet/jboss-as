/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.io.InputStream;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OperationContextHelper {
    /**
     * create operation context with input stream
     *
     * @param context
     * @param operation
     * @return
     */
    static OperationContext addAttachmentToContext(final OperationContext context, final Operation operation) {
        return new OperationContext() {

            @Override
            public int getAttachmentStreamCount() {
                return operation.getInputStreams().size();
            }

            @Override
            public InputStream getAttachmentStream(int index) {
                if (operation.getInputStreams().size() <= index) {
                    throw new ArrayIndexOutOfBoundsException(index);
                }
                return operation.getInputStreams().get(index);
            }

            @Override
            public void setRollbackOnly() {
                context.setRollbackOnly();
            }

            @Override
            public void runtimeUpdateSkipped() {
                context.runtimeUpdateSkipped();
            }

            @Override
            public void revertRestartRequired() {
                context.revertRestartRequired();
            }

            @Override
            public void revertReloadRequired() {
                context.revertReloadRequired();
            }

            @Override
            public void restartRequired() {
                context.restartRequired();
            }

            @Override
            public void report(MessageSeverity severity, String message) {
                context.report(severity, message);
            }

            @Override
            public void removeService(ServiceController<?> controller) throws UnsupportedOperationException {
                context.removeService(controller);
            }

            @Override
            public ServiceController<?> removeService(ServiceName name) throws UnsupportedOperationException {
                return context.removeService(name);
            }

            @Override
            public Resource removeResource(PathAddress address) throws UnsupportedOperationException {
                return context.removeResource(address);
            }

            @Override
            public void reloadRequired() {
                context.reloadRequired();
            }

            @Override
            public Resource readResourceForUpdate(PathAddress address) {
                return context.readResourceForUpdate(address);
            }

            @Override
            public Resource readResource(PathAddress address) {
                return context.readResource(address);
            }

            @Override
            public ModelNode readModelForUpdate(PathAddress address) {
                return context.readModelForUpdate(address);
            }

            @Override
            public ModelNode readModel(PathAddress address) {
                return context.readModel(address);
            }

            @Override
            public boolean isRuntimeAffected() {
                return context.isResourceRegistryAffected();
            }

            @Override
            public boolean isRollbackOnly() {
                return context.isRollbackOnly();
            }

            @Override
            public boolean isResourceRegistryAffected() {
                return context.isResourceRegistryAffected();
            }

            @Override
            public boolean isModelAffected() {
                return context.isModelAffected();
            }

            @Override
            public boolean isBooting() {
                return context.isBooting();
            }

            @Override
            public boolean hasResult() {
                return context.hasResult();
            }

            @Override
            public boolean hasFailureDescription() {
                return context.hasFailureDescription();
            }

            @Override
            public Type getType() {
                return context.getType();
            }

            @Override
            public ServiceTarget getServiceTarget() throws UnsupportedOperationException {
                return context.getServiceTarget();
            }

            @Override
            public ServiceRegistry getServiceRegistry(boolean modify) throws UnsupportedOperationException {
                return context.getServiceRegistry(modify);
            }

            @Override
            public Resource getRootResource() {
                return context.getRootResource();
            }

            @Override
            public ModelNode getResult() {
                return context.getResult();
            }

            @Override
            public ManagementResourceRegistration getResourceRegistrationForUpdate() {
                return context.getResourceRegistrationForUpdate();
            }

            @Override
            public ImmutableManagementResourceRegistration getResourceRegistration() {
                return context.getResourceRegistration();
            }

            @Override
            public ModelNode getFailureDescription() {
                return context.getFailureDescription();
            }

            @Override
            public Stage getCurrentStage() {
                return context.getCurrentStage();
            }

            @Override
            public Resource createResource(PathAddress address) throws UnsupportedOperationException {
                return context.createResource(address);
            }

            @Override
            public ResultAction completeStep() {
                return context.completeStep();
            }

            @Override
            public void addStep(ModelNode response, ModelNode operation, OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(response, operation, step, stage);
            }

            @Override
            public void addStep(ModelNode operation, OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(operation, step, stage);
            }

            @Override
            public void addStep(OperationStepHandler step, Stage stage) throws IllegalArgumentException {
                context.addStep(step, stage);
            }

            @Override
            public void acquireControllerLock() {
                context.acquireControllerLock();
            }
        };
    }
}
