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

package org.netbeans.modules.j2ee.clientproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectCreateData;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Application Client project.
 */
public class NewAppClientProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    public static NewAppClientProjectWizardIterator library() {
        return new NewAppClientProjectWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectLocationWizardPanel(J2eeModule.CLIENT, 
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "LAB_ConfigureProject"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_NewAppClient"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_JavaApplication")), // NOI18N
            new ProjectServerWizardPanel(J2eeModule.CLIENT, 
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "NewAppClientProjectWizardIterator.secondStep"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_NewAppClient"),
                    true, true, false, false, false, true),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"LAB_ConfigureProject"), // NOI18N
            NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "NewAppClientProjectWizardIterator.secondStep"), // NOI18N
        };
    }
    
    
    public Set<FileObject> instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File)wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }

        String mainClass = (String)wiz.getProperty(ProjectServerWizardPanel.MAIN_CLASS);
        AppClientProjectCreateData createData = new AppClientProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName((String)wiz.getProperty(ProjectLocationWizardPanel.NAME));
        createData.setMainClass(mainClass);
        createData.setServerInstanceID((String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID));
        createData.setJavaEEProfile((Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL));
        createData.setLibrariesDefinition(
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES)));
        createData.setCDIEnabled((Boolean)wiz.getProperty(ProjectServerWizardPanel.CDI));
        
        AntProjectHelper h = AppClientProjectGenerator.createProject(createData);
        
        handle.progress(2);
        
        if (mainClass != null && mainClass.length() > 0) {
            try {
                //String sourceRoot = "src"; //(String)j2seProperties.get (J2SEProjectProperties.SRC_DIR);
                FileObject sourcesRoot = h.getProjectDirectory().getFileObject("src/java");        //NOI18N
                FileObject mainClassFo = getMainClassFO(sourcesRoot, mainClass);
                assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass;        //NOI18N
                // Returning FileObject of main class, will be called its preferred action
                resultSet.add(mainClassFo);
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
            }
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        
        Project earProject = (Project) wiz.getProperty(ProjectServerWizardPanel.EAR_APPLICATION);
        AppClientProject createdAppClientProject = (AppClientProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdAppClientProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addCarModule(createdAppClientProject.getAPICar());
            }
        }
        
        // remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(createData.getServerInstanceID());
        SharableLibrariesUtils.setLastProjectSharable(createData.getLibrariesDefinition() != null);
        
        // downgrade the Java platform or src level to 1.4
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
        
        // Returning FileObject of project diretory.
        // Project will be open and set as main
        resultSet.add(dir);
        
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder(dirF);
        }

        // Usages statistics
        Object[] parameters = new Object[2];
        parameters[0] = ""; //NOI18N
        try {
            if (createData.getServerInstanceID() != null) {
                parameters[0] = Deployment.getDefault().getServerInstance(createData.getServerInstanceID()).getServerDisplayName();
            }
        } catch (InstanceRemovedException ire) {
            // ignore
        }
        parameters[1] = createData.getJavaEEProfile();
        Utils.logUsage(NewAppClientProjectWizardIterator.class, "USG_PROJECT_CREATE_APPCLIENT", parameters); //NOI18N

        handle.progress(NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);

        return resultSet;
    }
    
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty(WizardProperties.SOURCE_ROOT, new File[0]);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, new File[0]);
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) { // #74316
            this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR, null);
            this.wiz.putProperty(ProjectLocationWizardPanel.NAME, null);
            this.wiz.putProperty(ProjectServerWizardPanel.MAIN_CLASS, null);
            this.wiz = null;
        }
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"LAB_IteratorName"),
                new Object[] {index + 1, panels.length});
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO(FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
        mainClass = mainClass.replace('.', '/');
        
        // ignore unvalid mainClass ???
        
        return sourcesRoot.getFileObject(mainClass+ ".java"); // NOI18N
    }
    
    static String getPackageName(String displayName) {
        StringBuffer builder = new StringBuffer();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart(c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
            }
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
}
