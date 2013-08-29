package org.jboss.as.domain.controller.modules;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.vfs.VirtualFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EarExtractor {

    List<Module> modules = new ArrayList<Module>();

    public EarExtractor(File extractedEar, File modulesLocation) throws Exception {
        modules = extractModules(extractedEar, modulesLocation);
        mergeLib(modules, extractedEar);
    }

    public List<Module> getWebModules() {
        return modules;
    }

    private List<Module> extractModules(File extractedEar, File modulesLocation) throws Exception {
        List<Module> modules = new ArrayList<Module>();

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".war");
            }
        };

        for (File war : extractedEar.listFiles(filter)) {
            String moduleName = extractedEar.getName() + "-" + war.getName();
            modules.add(new Module(war, modulesLocation, moduleName));
        }
        return modules;
    }

    private List<VirtualFile> getEarModules(VirtualFile ear) throws Exception {
        List<VirtualFile> modules = new ArrayList<VirtualFile>();
        List<VirtualFile> children = ear.getChildren();
        return modules;
    }

    private void mergeLib(List<Module> modules, File extractedEar) throws Exception {
        String libFolderPath = getLibFolder(new File(extractedEar, "META-INF/application.xml"));
        File libFolder = new File(extractedEar, libFolderPath);
        for (Module module : modules) {
            try {
                if (libFolder.exists()) {
                    module.addLibs(libFolder.listFiles());
                }
            } catch (IOException e) {
                throw new DeploymentUnitProcessingException("Cannot add merge module libs.", e);
            }
        }
    }

    private String getLibFolder(File applicationXml) throws Exception {
        //TODO optimize parsing
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(applicationXml);
        NodeList libFolderTags = doc.getElementsByTagName("library-directory");
        if (libFolderTags.getLength() == 1) {
            Node libFolder = libFolderTags.item(0);
            return libFolder.getTextContent();
        } else {
            throw new Exception("Invalid ear structure, missing library-directory metadata.");
        }
    }
}
