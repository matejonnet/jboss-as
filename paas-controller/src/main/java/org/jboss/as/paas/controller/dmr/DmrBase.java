package org.jboss.as.paas.controller.dmr;

import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DmrBase {

    protected ModelNode createCompositeOperation() {
        ModelNode op = new ModelNode();
        op.get("operation").set("composite");
        op.get("address").setEmptyList();
        return op.get("steps");
    }

}
