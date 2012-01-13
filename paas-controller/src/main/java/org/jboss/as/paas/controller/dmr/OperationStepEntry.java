package org.jboss.as.paas.controller.dmr;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationContext.Stage;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OperationStepEntry {
    private ModelNode request;
    private OperationContext.Stage opStage;
    private String stepName;
    //step to execute on success execution
    private String onSuccess;

    /**
     * @param request
     * @param opStage
     * @param stepName
     * @param onSuccess
     */
    public OperationStepEntry(ModelNode request, Stage opStage, String stepName, String onSuccess) {
        super();
        this.request = request;
        this.opStage = opStage;
        this.stepName = stepName;
        this.onSuccess = onSuccess;
    }

    /**
     * @return the request
     */
    public ModelNode getRequest() {
        return request;
    }

    /**
     * @return the opStage
     */
    public OperationContext.Stage getOpStage() {
        return opStage;
    }

    /**
     * @return the stepName
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * @return the onSuccess
     */
    public String getOnSuccess() {
        return onSuccess;
    }

}
