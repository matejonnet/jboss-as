package org.jboss.as.paas.controller.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.AttributeAccess.Storage;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PaasExtension implements Extension {

    private static final Logger log = Logger.getLogger(PaasExtension.class);

    /** The name space used for the {@code substystem} element */
    // public static final String NAMESPACE =
    // "urn:org.jboss.as.paas.controller:1.0";
    public static final String NAMESPACE = "urn:jboss:domain:paas-controller:1.0";

    /** The name of our subsystem within the model. */
    public static final String SUBSYSTEM_NAME = "paas-controller";
    // public static final String SUBSYSTEM_NAME =
    // "org.jboss.as.paas.controller";

    /** The parser used for parsing our subsystem */
    private final SubsystemParser parser = new SubsystemParser();

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(NAMESPACE, parser);
    }

    @Override
    public void initialize(ExtensionContext context) {

        log.info("Initializing PaasExtension...");

        // TODO register subsysetem outside the profile
        // custom operations must execute on domain controller only. Now there
        // is an if (isDomainController)
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(PaasProviders.SUBSYSTEM);
        // We always need to add an 'add' operation
        registration.registerOperationHandler(ADD, PaasAddHandler.INSTANCE, PaasProviders.SUBSYSTEM_ADD, false);

        // add module specific operations
        registration.registerOperationHandler(ListApplicationsHandler.OPERATION_NAME, ListApplicationsHandler.INSTANCE, PaasProviders.SUBSYSTEM_ADD, false);
        registration.registerOperationHandler(DeployHandler.OPERATION_NAME, DeployHandler.INSTANCE, DeployHandler.DESC, false);
        registration.registerOperationHandler(UnDeployHandler.OPERATION_NAME, UnDeployHandler.INSTANCE, UnDeployHandler.DESC, false);
        registration.registerOperationHandler(ScaleUpHandler.OPERATION_NAME, ScaleUpHandler.INSTANCE, ScaleUpHandler.DESC, false);
        registration.registerOperationHandler(ScaleDownHandler.OPERATION_NAME, ScaleDownHandler.INSTANCE, ScaleDownHandler.DESC, false);

        // We always need to add a 'describe' operation
        registration.registerOperationHandler(DESCRIBE, PaasDescribeHandler.INSTANCE, PaasDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);

        // Add the provider child
        ManagementResourceRegistration providerChild = registration.registerSubModel(PathElement.pathElement("provider"), PaasProviders.PROVIDER_CHILD);
        providerChild.registerOperationHandler(ModelDescriptionConstants.ADD, IaasProviderAddHandler.INSTANCE, IaasProviderAddHandler.INSTANCE);
        providerChild.registerOperationHandler(ModelDescriptionConstants.REMOVE, IaasProviderRemoveHandler.INSTANCE, IaasProviderRemoveHandler.INSTANCE);
        // TODO typeChild.registerReadWriteAttribute("tick", null,
        // TrackerTickHandler.INSTANCE, Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("driver", null,
        // ProviderDriverHandle.INSTANCE, Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("url", null,
        // Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("username", null,
        // Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("password", null,
        // Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("image-id", null,
        // Storage.CONFIGURATION);

        ManagementResourceRegistration serverInstanceChild = registration.registerSubModel(PathElement.pathElement("instance"), PaasProviders.INSTANCE_CHILD);
        serverInstanceChild.registerOperationHandler(ModelDescriptionConstants.ADD, ServerInstanceAddHandler.INSTANCE, ServerInstanceAddHandler.INSTANCE);
        serverInstanceChild.registerOperationHandler(ModelDescriptionConstants.REMOVE, ServerInstanceRemoveHandler.INSTANCE, ServerInstanceRemoveHandler.INSTANCE);

        // TODO read only ?
        providerChild.registerReadOnlyAttribute("provider", null, Storage.CONFIGURATION);
        // providerChild.registerReadWriteAttribute("provider", null,
        // InstanceProviderHandle.INSTANCE, Storage.CONFIGURATION);

        ManagementResourceRegistration serverGroupChildRegistration = serverInstanceChild.registerSubModel(PathElement.pathElement("server-group"), PaasProviders.SERVER_GROUP_CHILD);
        // ManagementResourceRegistration serverGroupChild =
        // registration.registerSubModel(PathElement.pathElement("server-group"),
        // PaasProviders.INSTANCE_CHILD);
        serverGroupChildRegistration.registerOperationHandler(ModelDescriptionConstants.ADD, ServerGroupAddHandler.INSTANCE, ServerGroupAddHandler.INSTANCE);
        serverGroupChildRegistration.registerOperationHandler(ModelDescriptionConstants.REMOVE, ServerGroupRemoveHandler.INSTANCE, ServerGroupRemoveHandler.INSTANCE);

        subsystem.registerXMLElementWriter(parser);
    }

    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /** {@inheritDoc} */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);

            // Add the main subsystem 'add' operation
            list.add(createAddSubsystemOperation());

            // Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (reader.getLocalName().equals("iaas-providers")) {
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readIaasProvider(reader, list);
                        }
                    }
                } else if (reader.getLocalName().equals("instances")) {
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readInstance(reader, list);
                        }
                    }
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
            // TODO remove: break point holder
            System.currentTimeMillis();
        }

        private void readIaasProvider(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals("iaas-provider")) {
                throw ParseUtils.unexpectedElement(reader);
            }

            String provider = null;
            String iaasDriver = null;
            String iaasUrl = null;
            String iaasUsername = null;
            String iaasPassword = null;
            String iaasImageId = null;

            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                if (attr.equals("provider")) {
                    provider = reader.getAttributeValue(i);
                } else if (attr.equals("driver")) {
                    iaasDriver = reader.getAttributeValue(i);
                } else if (attr.equals("url")) {
                    iaasUrl = reader.getAttributeValue(i);
                } else if (attr.equals("username")) {
                    iaasUsername = reader.getAttributeValue(i);
                } else if (attr.equals("password")) {
                    iaasPassword = reader.getAttributeValue(i);
                } else if (attr.equals("image-id")) {
                    iaasImageId = reader.getAttributeValue(i);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (provider == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("provider"));
            }

            // Add the 'add' operation for each 'type' child
            ModelNode addType = new ModelNode();
            addType.get(OP).set(ModelDescriptionConstants.ADD);
            PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement("provider", provider));
            addType.get(OP_ADDR).set(addr.toModelNode());

            if (iaasDriver != null)
                addType.get("driver").set(iaasDriver);
            if (iaasUrl != null)
                addType.get("url").set(iaasUrl);
            if (iaasUsername != null)
                addType.get("username").set(iaasUsername);
            if (iaasPassword != null)
                addType.get("password").set(iaasPassword);
            if (iaasImageId != null)
                addType.get("image-id").set(iaasImageId);

            list.add(addType);
        }

        private void readInstance(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals("instance")) {
                throw ParseUtils.unexpectedElement(reader);
            }

            String id = null;
            String provider = null;
            String ip = null;

            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                if (attr.equals("id")) {
                    id = reader.getAttributeValue(i);
                } else if (attr.equals("provider")) {
                    provider = reader.getAttributeValue(i);
                } else if (attr.equals("ip")) {
                    ip = reader.getAttributeValue(i);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }

            if (id == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("id"));
            }

            // Add the 'add' operation for each 'type' child
            ModelNode addType = new ModelNode();
            addType.get(OP).set(ModelDescriptionConstants.ADD);
            PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement("instance", id));
            addType.get(OP_ADDR).set(addr.toModelNode());

            if (provider != null)
                addType.get("provider").set(provider);

            if (ip != null)
                addType.get("ip").set(ip);

            list.add(addType);

            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (reader.getLocalName().equals("server-group")) {
                    readServerGroup(reader, list, id);
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }

        /**
         * @param reader
         * @param serverGroups
         * @throws XMLStreamException
         */
        private void readServerGroup(XMLExtendedStreamReader reader, List<ModelNode> list, String instanceId) throws XMLStreamException {
            if (!reader.getLocalName().equals("server-group")) {
                throw ParseUtils.unexpectedElement(reader);
            }

            String name = null;
            String position = null;

            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                if (attr.equals("name")) {
                    name = reader.getAttributeValue(i);
                } else if (attr.equals("position")) {
                    position = reader.getAttributeValue(i);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }

            assert (reader.nextTag() != END_ELEMENT);

            ParseUtils.requireNoContent(reader);

            if (name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("name"));
            }

            ModelNode addType = new ModelNode();
            addType.get(OP).set(ModelDescriptionConstants.ADD);
            PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement("instance", instanceId), PathElement.pathElement("server-group", name));
            addType.get(OP_ADDR).set(addr.toModelNode());

            if (position != null)
                addType.get("position").set(position);

            list.add(addType);
        }

        /** {@inheritDoc} */
        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
            // Write out the main subsystem element
            context.startSubsystemElement(PaasExtension.NAMESPACE, false);

            writer.writeStartElement("iaas-providers");
            ModelNode node = context.getModelNode();
            ModelNode iaasProvider = node.get("provider");
            for (Property property : iaasProvider.asPropertyList()) {
                // write each child element to xml
                writer.writeStartElement("iaas-provider");
                writer.writeAttribute("provider", property.getName());
                ModelNode entry = property.getValue();
                if (entry.hasDefined("driver")) {
                    writer.writeAttribute("driver", entry.get("driver").asString());
                }
                if (entry.hasDefined("url")) {
                    writer.writeAttribute("url", entry.get("url").asString());
                }
                if (entry.hasDefined("username")) {
                    writer.writeAttribute("username", entry.get("username").asString());
                }
                if (entry.hasDefined("password")) {
                    writer.writeAttribute("password", entry.get("password").asString());
                }
                if (entry.hasDefined("image-id")) {
                    writer.writeAttribute("image-id", entry.get("image-id").asString());
                }
                writer.writeEndElement();
            }
            // End providers
            writer.writeEndElement();

            writer.writeStartElement("instances");
            ModelNode instance = node.get("instance");
            for (Property property : instance.asPropertyList()) {
                // write each child element to xml
                writer.writeStartElement("instance");
                writer.writeAttribute("id", property.getName());
                ModelNode entry = property.getValue();
                if (entry.hasDefined("provider")) {
                    writer.writeAttribute("provider", entry.get("provider").asString());
                }
                if (entry.hasDefined("ip")) {
                    writer.writeAttribute("ip", entry.get("ip").asString());
                }

                ModelNode serverGroups = entry.get("server-group");
                if (serverGroups.isDefined())
                    for (Property serverGroup : serverGroups.asPropertyList()) {
                        // write each child element to xml
                        writer.writeStartElement("server-group");
                        writer.writeAttribute("name", serverGroup.getName());
                        ModelNode sgEntry = serverGroup.getValue();
                        if (sgEntry.hasDefined("position")) {
                            writer.writeAttribute("position", sgEntry.get("position").asString());
                        }
                        writer.writeEndElement();
                    }
                // End instance
                writer.writeEndElement();
            }
            // End instances
            writer.writeEndElement();

            // End subsystem
            writer.writeEndElement();
        }
    }

    /**
     * Recreate the steps to put the subsystem in the same state it was in.
     * This is used in domain mode to query the profile being used, in order to
     * get the steps needed to create the servers
     */
    private static class PaasDescribeHandler implements OperationStepHandler, DescriptionProvider {
        static final PaasDescribeHandler INSTANCE = new PaasDescribeHandler();

        // TODO
        @Override
        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            context.getResult().add(createAddSubsystemOperation());
            context.completeStep();
        }

        @Override
        public ModelNode getModelDescription(Locale locale) {
            return CommonDescriptions.getSubsystemDescribeOperation(locale);
        }
    }

}
