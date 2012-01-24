/**
 *
 */
package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;

import java.util.Locale;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerInstanceRemoveHandler extends AbstractRemoveStepHandler implements DescriptionProvider {

    public static final ServerInstanceRemoveHandler INSTANCE = new ServerInstanceRemoveHandler();

    private ServerInstanceRemoveHandler() {}

    @Override
    public ModelNode getModelDescription(Locale locale) {
        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Removes a server instance.");
        return node;
    }

}
