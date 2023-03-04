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
/*
 * Contributor(s): Craig MacKay
 */

package org.netbeans.modules.spring.beans.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;

public final class NewSpringXMLConfigWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(NewSpringXMLConfigWizardIterator.class.getName());
    private static final String DEFAULT_SPRING_VERSION = "3.2.18";
    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project p = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            ConfigFileManager manager = getConfigFileManager(p);
            List<ConfigFileGroup> configFileGroups = manager != null ? manager.getConfigFileGroups() : null;
            SpringXMLConfigGroupPanel configGroupPanel = configFileGroups != null && !configFileGroups.isEmpty() ? new SpringXMLConfigGroupPanel(configFileGroups) : null;
            WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups, configGroupPanel);

            panels = new WizardDescriptor.Panel[] {
                targetChooser,
                new SpringXMLConfigNamespacesPanel(),
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    @Override
    public Set instantiate() throws IOException {
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final String targetName = Templates.getTargetName(wizard);

        final FileObject[] createdFile = { null };

        String version="";
        boolean addSpringToClassPath = (Boolean) wizard.getProperty(SpringXMLConfigNamespacesPanel.ADD_SPRING_TO_CLASSPATH);
        if (addSpringToClassPath) {
            Library[] libraries = { (Library) wizard.getProperty(SpringXMLConfigNamespacesPanel.SPRING_LIBRARY) };
            version = SpringUtilities.getSpringLibraryVersion(libraries[0]);
            addLibrariesToClassPath(libraries);
        } else {
            //Retriving Spring library version from ClassPath
            FileObject artifact = getSourceGroupArtifact(Templates.getProject(wizard),targetFolder);
            ClassPath cp = ClassPath.getClassPath(artifact, ClassPath.COMPILE);
            FileObject resource = null;
            if (cp != null) {
                resource = cp.findResource(SpringUtilities.SPRING_CLASS_NAME.replace('.', '/')+".class");    //NOI18N
            }


            if ( resource != null ) {
                FileObject ownerRoot = cp.findOwnerRoot(resource);
                version = findSpringVersion(ownerRoot);
            }
        }
        if (version == null || "".equals(version)) {    //NOI18N
            version = DEFAULT_SPRING_VERSION;
        }

        HashMap<String, Object> params = new HashMap<>();
        if (version.startsWith("3.") || version.startsWith("4.")) { //NOI18N
            params.put("version", version); //NOI18N
        }

        params.put("namespaces", getNamespacesList(version));   //NOI18N
        
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        FileObject templateFO = Templates.getTemplate(wizard);
        DataObject templateDO = DataObject.find(templateFO);
        createdFile[0] = templateDO.createFromTemplate(dataFolder, targetName, params).getPrimaryFile();

        @SuppressWarnings("unchecked")
        Set<ConfigFileGroup> selectedGroups = (Set<ConfigFileGroup>) wizard.getProperty(SpringXMLConfigGroupPanel.CONFIG_FILE_GROUPS);
        addFileToConfigFileManager(selectedGroups != null ? selectedGroups : Collections.<ConfigFileGroup>emptySet(), FileUtil.toFile(createdFile[0]));
        
        return Collections.singleton(createdFile[0]);
    }

    private List<TemplateData> getNamespacesList(String version) {
        List<TemplateData> namespaces = new ArrayList<TemplateData>();
        String[] incNamespaces = (String[]) wizard.getProperty(SpringXMLConfigNamespacesPanel.INCLUDED_NAMESPACES);
        for (String ns : incNamespaces) {
            String prefix = ns.substring(0, ns.indexOf("-")).trim(); // NOI18N
            String schemaName = ns.substring(ns.indexOf("-") + 1).trim(); //NOI18N
            String fileName;
            int indexOfSchemaNameBegin = schemaName.lastIndexOf("/") + 1; //NOI18N
            if (schemaName.equals("http://www.springframework.org/schema/webflow-config")) { //NOI18N
                fileName = "spring-" + schemaName.substring(indexOfSchemaNameBegin) + "-2.0.xsd"; //NOI18N
            } else if (schemaName.equals("http://www.springframework.org/schema/osgi")) { //NOI18N
                fileName = "spring-" + schemaName.substring(indexOfSchemaNameBegin) + "-1.2.xsd"; //NOI18N
            } else {
                fileName = "spring-" + schemaName.substring(indexOfSchemaNameBegin) + "-" + version + ".xsd"; //NOI18N
            }

            // c and p namespaces doesn't have any scheme
            if ("c".equals(prefix) || "p".equals(prefix)) { //NOI18N
                namespaces.add(new TemplateData(prefix, schemaName, fileName, false));
            } else {
                namespaces.add(new TemplateData(prefix, schemaName, fileName));
            }

        }
        return namespaces;
    }

    private String findSpringVersion(FileObject ownerRoot) {
        try {
            if (ownerRoot !=null) { //NOI18N
                if (ownerRoot.getFileSystem() instanceof JarFileSystem) {
                    JarFileSystem jarFileSystem = (JarFileSystem) ownerRoot.getFileSystem();
                    String implementationVersion = SpringUtilities.getImplementationVersion(jarFileSystem);
                    if (implementationVersion != null) {
                        String[] splitedVersion = implementationVersion.split("[.]"); //NOI18N
                        if (splitedVersion.length < 2) {
                            LOG.log(Level.SEVERE, "Unknown syntax of implementation version={0}", implementationVersion);
                            return null;
                        }
                        return splitedVersion[0] + "." + splitedVersion[1];
                    }
                }
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }


    private void addLibrariesToClassPath(Library[] libraries) throws IOException {
        FileObject artifact = getSourceGroupArtifact(Templates.getProject(wizard), Templates.getTargetFolder(wizard));
        if (artifact != null) {
            ProjectClassPathModifier.addLibraries(libraries, artifact, ClassPath.COMPILE);
        }
    }
    
    private void addFileToConfigFileManager(final Set<ConfigFileGroup> selectedGroups, final File file) throws IOException {
        final ConfigFileManager manager = getConfigFileManager(Templates.getProject(wizard));
        try {
            manager.mutex().writeAccess(new ExceptionAction<Void>() {
                public Void  run() throws IOException {
                    List<File> origFiles = manager.getConfigFiles();
                    List<File> newFiles = new ArrayList<File>(origFiles);
                    if (!newFiles.contains(file)) {
                        newFiles.add(file);
                    }
                    List<ConfigFileGroup> origGroups = manager.getConfigFileGroups();
                    List<ConfigFileGroup> newGroups = null;
                    if (selectedGroups.size() > 0) {
                        newGroups = new ArrayList<ConfigFileGroup>(origGroups.size());
                        for (ConfigFileGroup group : origGroups) {
                            if (selectedGroups.contains(group)) {
                                ConfigFileGroup newGroup = addFileToConfigGroup(group, file);
                                newGroups.add(newGroup);
                            } else {
                                newGroups.add(group);
                            }
                        }
                    } else {
                        newGroups = origGroups;
                    }
                    manager.putConfigFilesAndGroups(newFiles, newGroups);
                    manager.save();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    private ConfigFileGroup addFileToConfigGroup(ConfigFileGroup group, File file) {
        List<File> files = group.getFiles();
        files.add(file);
        return ConfigFileGroup.create(group.getName(), files);
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (Templates.getTargetFolder(wizard) == null) {
            Project project = Templates.getProject(wizard);
            SpringConfigFileLocationProvider provider = project != null ? project.getLookup().lookup(SpringConfigFileLocationProvider.class) : null;
            FileObject location = provider != null ? provider.getLocation() : null;
            if (location != null) {
                Templates.setTargetFolder(wizard, location);
            }
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length; // NOI18N
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    static ConfigFileManager getConfigFileManager(Project p) {
        ProjectSpringScopeProvider scopeProvider = p.getLookup().lookup(ProjectSpringScopeProvider.class);
        return scopeProvider != null ?  scopeProvider.getSpringScope().getConfigFileManager() : null;
    }
    
    static FileObject getSourceGroupArtifact(Project project, FileObject preferredArtifact) {
        SourceGroup[] groups = SourceGroups.getJavaSourceGroups(project);
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            if (preferredArtifact.equals(root) || (FileUtil.isParentOf(root, preferredArtifact) && group.contains(preferredArtifact))) {
                return preferredArtifact;
            }
        }
        // Otherwise just get the first source group.
        for (SourceGroup group : groups) {
            return group.getRootFolder();
        }
        return null;
    }

    public static final class TemplateData {

        private String prefix;
        private String namespace;
        private String fileName;
        private boolean hasSchemeLocation;

        public boolean hasSchemeLocation() {
            return hasSchemeLocation;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public TemplateData(String prefix, String namespace, String fileName) {
            this(prefix, namespace, fileName, true);
        }

        public TemplateData(String prefix, String namespace, String fileName, boolean hasSchemeLocation) {
            this.prefix = prefix;
            this.namespace = namespace;
            this.fileName = fileName;
            this.hasSchemeLocation = hasSchemeLocation;
        }

    }
}
