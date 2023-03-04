/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.projectimport.eclipse.web;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.projectimport.eclipse.core.spi.Facets.Facet;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.ProjectDescriptor;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO: current detection of whether NB project is uptodate with Eclipse or not
// is based just on .classpath/.project. For web support file
// ".settings/org.eclipse.wst.common.component" should be checked as well.

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.class, position=800)
public class WebProjectFactory implements ProjectTypeUpdater {

    private static final Logger LOG =
            Logger.getLogger(WebProjectFactory.class.getName());
    private static final String WEB_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature"; // NOI18N
    private static final Icon WEB_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif", false); //NOI18N
    
    private static final String MYECLIPSE_WEB_NATURE = "com.genuitec.eclipse.j2eedt.core.webnature"; // NOI18N
    
    public WebProjectFactory() {
    }
    
    public boolean canHandle(ProjectDescriptor descriptor) {
        // eclipse ganymede and europa are using facets:
        if (descriptor.getFacets() != null) {
            return descriptor.getFacets().hasInstalledFacet("jst.web"); //NOI18N
        }
        if (descriptor.getNatures().contains(WEB_NATURE)) {
            // this is perhaps case of older Eclipse versions??
            // TODO: perhaps not needed
            return true;
        }
        // accept MyEclipse web projects
        return descriptor.getNatures().contains(MYECLIPSE_WEB_NATURE);
    }

    private ServerSelectionWizardPanel findWizardPanel(ProjectImportModel model) {
        assert model.getExtraWizardPanels() != null;
        for (WizardDescriptor.Panel panel : model.getExtraWizardPanels()) {
            if (panel instanceof ServerSelectionWizardPanel) {
                return (ServerSelectionWizardPanel)panel;
            }
        }
        return null;
    }
    
    public Project createProject(final ProjectImportModel model, final List<String> importProblems) throws IOException {
        // create nb project location
        File nbProjectDir = model.getNetBeansProjectLocation();
        
        if (ProjectFactorySupport.areSourceRootsOwned(model, nbProjectDir, importProblems)) {
            return null;
        }
        
        WebContentData webData = parseWebContent(model.getEclipseProjectFolder());
        if (webData == null || /* #178018 */webData.webRoot == null/* || webData.contextRoot == null*/) {
            importProblems.add(org.openide.util.NbBundle.getMessage(WebProjectFactory.class, "MSG_MissingExtraWebFiles")); //NOI18N
            return null;
        }

        String serverID;
        if (model.getExtraWizardPanels() != null && findWizardPanel(model) != null) {
            ServerSelectionWizardPanel wizard = findWizardPanel(model);
            assert wizard != null;
            serverID = wizard.getServerID();
        } else {
            if (Deployment.getDefault().getServerInstanceIDs().length == 0) {
                importProblems.add(org.openide.util.NbBundle.getMessage(WebProjectFactory.class, "MSG_NoJ2EEServer")); //NOI18N
                return null;
            } else {
                serverID = Deployment.getDefault().getServerInstanceIDs()[0];
            }
        }
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(nbProjectDir);
        createData.setName(model.getProjectName());
        createData.setServerInstanceID(serverID);
        String  j2eeSpecVersion = null;
        if (model.getFacets() != null) {
            Facet f = model.getFacets().getFacet("jst.web"); //NOI18N
            if (f != null) {
                String servletAPIVersion = f.getVersion();
                if ("2.5".equals(servletAPIVersion)) {
                    j2eeSpecVersion = "1.5"; // NOI18N
                } else if ("2.4".equals(servletAPIVersion)) {
                    j2eeSpecVersion = "1.4"; // NOI18N
                } else if ("2.3".equals(servletAPIVersion)) {
                    j2eeSpecVersion = "1.3"; // NOI18N
                }
            }
        }
        if (j2eeSpecVersion == null && webData.j2eeSpecVersion != null) {
            j2eeSpecVersion = webData.j2eeSpecVersion;
        }
        if (j2eeSpecVersion == null) {
            j2eeSpecVersion = "1.5"; //NOI18N
        }
        createData.setJavaEEVersion(j2eeSpecVersion);
        createData.setSourceLevel(model.getSourceLevel());
        if (model.getJavaPlatform() != null) {
            createData.setJavaPlatformName(model.getJavaPlatform().getDisplayName());
        }

        FileObject root = FileUtil.toFileObject(model.getEclipseProjectFolder());
        if (root.getFileObject(webData.webRoot) == null) {
            importProblems.add(org.openide.util.NbBundle.getMessage(WebProjectFactory.class, "MSG_MissingDocRoot", webData.webRoot));
            return null;
        }
        createData.setWebModuleFO(root);
        createData.setSourceFolders(model.getEclipseSourceRootsAsFileArray());
        createData.setTestFolders(model.getEclipseTestSourceRootsAsFileArray());
        createData.setContextPath(webData.contextRoot);
        createData.setDocBase(root.getFileObject(webData.webRoot));
        createData.setLibFolder(root.getFileObject(webData.webRoot+"/WEB-INF/lib")); //NOI18N
        createData.setWebInfFolder(root.getFileObject(webData.webRoot+"/WEB-INF")); //NOI18N
        createData.setLibrariesDefinition(null);
        if (nbProjectDir.exists() && new File(nbProjectDir, "build.xml").exists()) { //NOI18N
            createData.setBuildfile("nb-build.xml"); //NOI18N
        } else {
            createData.setBuildfile("build.xml"); //NOI18N
        }
        
        AntProjectHelper helper = WebProjectUtilities.importProject(createData);
        WebProject nbProject = (WebProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        boolean changed = false;
        if (new File(nbProjectDir, "dist").exists()) { //NOI18N
            ep.setProperty("dist.dir", "nbdist"); //NOI18N
            changed = true;
        }
        if (new File(nbProjectDir, "build").exists()) { //NOI18N
            ep.setProperty("build.dir", "nbbuild"); //NOI18N
            changed = true;
        }
        if (changed) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        
        // set labels for source roots
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseSourceRoots(), nbProject.getSourceRoots());
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseTestSourceRoots(), nbProject.getTestSourceRoots());
        
        ProjectFactorySupport.setupSourceExcludes(helper, model, importProblems);

        setupCompilerProperties(helper, model);
        
        // update project classpath
        ProjectFactorySupport.updateProjectClassPath(helper, nbProject.getReferenceHelper(), model, importProblems);
        
        // save project
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }

    private static WebContentData parseWebContent(File eclipseProject) throws IOException {
        File f = new File(eclipseProject, ".settings/org.eclipse.wst.common.component"); // NOI18N
        if (!f.exists()) {
            f = new File(eclipseProject, ".settings/.component"); // NOI18N
        }
        if (f.exists()) {
            Document webContent;
            try {
                webContent = XMLUtil.parse(new InputSource(f.toURI().toString()), false, true, XMLUtil.defaultErrorHandler(), null);
            } catch (SAXException e) {
                IOException ioe = (IOException) new IOException(f + ": " + e.toString()).initCause(e); //NOI18N
                throw ioe;
            }
            Element modulesEl = webContent.getDocumentElement();
            if (!"project-modules".equals(modulesEl.getLocalName())) { // NOI18N
                return null;
            }
            WebContentData data = new WebContentData();
            Element moduleEl = XMLUtil.findElement(modulesEl, "wb-module", null); //NOI18N
            if (moduleEl != null) { // #175364
            for (Element el : XMLUtil.findSubElements(moduleEl)) {
                if ("wb-resource".equals(el.getNodeName())) { //NOI18N
                    if ("/".equals(el.getAttribute("deploy-path"))) { //NOI18N
                        data.webRoot = el.getAttribute("source-path"); //NOI18N
                    }
                }
                if ("property".equals(el.getNodeName())) { //NOI18N
                    if ("context-root".equals(el.getAttribute("name"))) { //NOI18N
                        data.contextRoot = el.getAttribute("value"); //NOI18N
                    }
                }
            }
            }
            return data;
        }
        f = new File(eclipseProject, ".mymetadata"); // NOI18N
        if (f.exists()) {
            Document webContent;
            try {
                webContent = XMLUtil.parse(new InputSource(f.toURI().toString()), false, true, XMLUtil.defaultErrorHandler(), null);
            } catch (SAXException e) {
                IOException ioe = (IOException) new IOException(f + ": " + e.toString()).initCause(e); //NOI18N
                throw ioe;
            }
            Element modulesEl = webContent.getDocumentElement();
            if (!"project-module".equals(modulesEl.getLocalName())) { // NOI18N
                return null;
            }
            WebContentData data = new WebContentData();
            data.contextRoot = modulesEl.getAttribute("context-root"); //NOI18N
            String specVer = modulesEl.getAttribute("j2ee-spec"); //NOI18N
            if ("5.0".equals(specVer)) {
                specVer = "1.5"; // NOI18N
            }
            Element attrsEl = XMLUtil.findElement(modulesEl, "attributes", null); //NOI18N
            if (attrsEl != null) {
                for (Element el : XMLUtil.findSubElements(attrsEl)) {
                    if ("attribute".equals(el.getNodeName())) { //NOI18N
                        if ("webrootdir".equals(el.getAttribute("name"))) { //NOI18N
                            data.webRoot = el.getAttribute("value"); //NOI18N
                            break;
                        }
                    }
                }
            }
            return data;
        }
        return null;
    }
    
    private static class WebContentData {
        private String contextRoot;
        private String webRoot;
        private String j2eeSpecVersion; // only initialized in case od MyEclipse

        @Override
        public String toString() {
            return "WebContentData[contextRoot="+contextRoot+", webRoot="+webRoot+"]"; // NOI18N
        }
        
    }

    public String calculateKey(ProjectImportModel model) {
        WebContentData webData;
        try {
            webData = parseWebContent(model.getEclipseProjectFolder());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            webData = new WebContentData();
            webData.contextRoot = "??"; //NOI18N
            webData.webRoot = "??"; //NOI18N
        }
        return ProjectFactorySupport.calculateKey(model) + "web=" + webData.webRoot + ";" + "context=" + webData.contextRoot + ";"; //NOI18N
    }

    public String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException {
        if (!(project instanceof WebProject)) {
            throw new IOException("is not web project: "+project.getClass().getName()); //NOI18N
        }
        String newKey = calculateKey(model);
        
        // update project classpath
        String actualKey = ProjectFactorySupport.synchronizeProjectClassPath(project, 
                ((WebProject)project).getAntProjectHelper(), 
                ((WebProject)project).getReferenceHelper(), model, oldKey, newKey, importProblems);
        
        setupCompilerProperties(((WebProject) project).getAntProjectHelper(), model);

        // TODO:
        // update source roots and platform and server and web root and context
        
        // save project
        ProjectManager.getDefault().saveProject(project);
        
        return actualKey;
    }

    public Icon getProjectTypeIcon() {
        return WEB_PROJECT_ICON;
    }

    public String getProjectTypeName() {
        return org.openide.util.NbBundle.getMessage(WebProjectFactory.class, "LABEL_Web_Application");
    }

    public List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalImportWizardPanels() {
        return Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(new ServerSelectionWizardPanel());
    }
    
    private void setupCompilerProperties(AntProjectHelper helper, ProjectImportModel model) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.JAVAC_SOURCE, model.getSourceLevel());
        ep.setProperty(WebProjectProperties.JAVAC_TARGET, model.getTargetLevel());
        ep.setProperty(WebProjectProperties.JAVAC_DEPRECATION, Boolean.toString(model.isDeprecation()));
        ep.setProperty(WebProjectProperties.JAVAC_COMPILER_ARG, model.getCompilerArgs());
        ep.setProperty(WebProjectProperties.SOURCE_ENCODING, model.getEncoding());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.JAVAC_DEBUG, Boolean.toString(model.isDebug()));
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
    }

    public File getProjectFileLocation(ProjectDescriptor descriptor, String token) {
        if (!token.equals(ProjectTypeFactory.FILE_LOCATION_TOKEN_WEBINF)) {
            return null;
        }
        WebContentData data;
        try {
            data = parseWebContent(descriptor.getEclipseProjectFolder());
        } catch (IOException ex) {
            LOG.log(Level.INFO, "cannot parse webmodule data", ex); //NOI18N
            return null;
        }
        if (data != null) {
            File f = new File(descriptor.getEclipseProjectFolder(), data.webRoot+File.separatorChar+"WEB-INF"+File.separator); // NOI18N
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

}
