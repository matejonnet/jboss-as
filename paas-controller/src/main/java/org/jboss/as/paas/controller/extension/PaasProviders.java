package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MAX_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Contains the description providers. The description providers are what print out the
 * information when you execute the {@code read-resource-description} operation.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class PaasProviders {

    /**
     * Used to create the description of the subsystem
     */
    public static DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            final ModelNode subsystem = new ModelNode();
            subsystem.get(DESCRIPTION).set("PaaS controller subsystem.");
            subsystem.get(HEAD_COMMENT_ALLOWED).set(true);
            subsystem.get(TAIL_COMMENT_ALLOWED).set(true);
            subsystem.get(NAMESPACE).set(PaasExtension.NAMESPACE);

            //Add information about the children
            subsystem.get(CHILDREN, "provider", DESCRIPTION).set("List of IaaS providers.");
            subsystem.get(CHILDREN, "provider", MIN_OCCURS).set(0);
            subsystem.get(CHILDREN, "provider", MAX_OCCURS).set(Integer.MAX_VALUE);
            subsystem.get(CHILDREN, "provider", MODEL_DESCRIPTION);

            subsystem.get(CHILDREN, "instance", DESCRIPTION).set("List of server instances.");
            subsystem.get(CHILDREN, "instance", MIN_OCCURS).set(0);
            subsystem.get(CHILDREN, "instance", MAX_OCCURS).set(Integer.MAX_VALUE);
            subsystem.get(CHILDREN, "instance", MODEL_DESCRIPTION);

            return subsystem;
        }
    };

    /**
     * Used to create the description of the subsystem add method
     */
    public static DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {

            final ModelNode subsystem = new ModelNode();
            subsystem.get(DESCRIPTION).set("Adds PaaS controller subsystem");

            return subsystem;
        }
    };

    public static DescriptionProvider PROVIDER_CHILD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Iaas provider.");

            //Add information about the child
            node.get(ATTRIBUTES, "driver", DESCRIPTION).set("Deltacloud IaaS driver.");
            node.get(ATTRIBUTES, "driver", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "driver", REQUIRED).set(true);

            node.get(ATTRIBUTES, "url", DESCRIPTION).set("Deltacloud IaaS driver.");
            node.get(ATTRIBUTES, "url", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "url", REQUIRED).set(true);

            node.get(ATTRIBUTES, "username", DESCRIPTION).set("Deltacloud IaaS driver.");
            node.get(ATTRIBUTES, "username", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "username", REQUIRED).set(true);

            node.get(ATTRIBUTES, "password", DESCRIPTION).set("Deltacloud IaaS driver.");
            node.get(ATTRIBUTES, "password", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "password", REQUIRED).set(true);

            node.get(ATTRIBUTES, "image-id", DESCRIPTION).set("Deltacloud IaaS driver.");
            node.get(ATTRIBUTES, "image-id", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "image-id", REQUIRED).set(true);

            return node;
        }
    };

    public static DescriptionProvider INSTANCE_CHILD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            final ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Server instance.");

            //Add information about the child
            node.get(ATTRIBUTES, "provider", DESCRIPTION).set("IaaS provider.");
            node.get(ATTRIBUTES, "provider", TYPE).set(ModelType.STRING);
            node.get(ATTRIBUTES, "provider", REQUIRED).set(true);

            return node;
        }
    };

}
