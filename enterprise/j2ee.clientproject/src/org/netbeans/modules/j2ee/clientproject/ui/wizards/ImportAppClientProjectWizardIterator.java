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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectCreateData;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectImportLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Application Client project for an existing one.
 * @author Pavel Buzek
 */
public class ImportAppClientProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
//    private boolean imp = true;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[]{
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_IW_ImportTitle"), //NOI18N
        NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "NewAppClientProjectWizardIterator.secondStep"), // NOI18N
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LAB_ConfigureSourceRoots") //NOI18N
    };
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectImportLocationWizardPanel(J2eeModule.CLIENT, 
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "LBL_IW_ImportTitle"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_ImportAppClientModule"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_JavaApplication"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "LBL_IW_LocationSrcDesc")),
            new ProjectServerWizardPanel(J2eeModule.CLIENT, 
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "NewAppClientProjectWizardIterator.secondStep"),
                    NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "TXT_ImportAppClientModule"),
                    true, true, false, false, true, false),
            new PanelSourceFolders.Panel()
        };
    }

    public Set<FileObject> instantiate() throws IOException/*, IllegalStateException*/ {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }

        AppClientProjectCreateData createData = new AppClientProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName((String) wiz.getProperty(ProjectLocationWizardPanel.NAME));
        createData.setSourceFolders((File[]) wiz.getProperty(WizardProperties.JAVA_ROOT));
        createData.setTestFolders((File[]) wiz.getProperty(WizardProperties.TEST_ROOT));
        createData.setConfFolder((File) wiz.getProperty(WizardProperties.CONFIG_FILES_FOLDER));
        createData.setLibFolder((File) wiz.getProperty(WizardProperties.LIB_FOLDER));
        createData.setServerInstanceID((String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID));
        createData.setJavaEEProfile((Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL));
        createData.setLibrariesDefinition(
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES)));

        AntProjectHelper h = AppClientProjectGenerator.importProject(createData);
        
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject (dirF);

        Project earProject = (Project) wiz.getProperty(ProjectServerWizardPanel.EAR_APPLICATION);
        AppClientProject createdAppClientProject = (AppClientProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdAppClientProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addCarModule(createdAppClientProject.getAPICar());
            }
        }
        
        resultSet.add (dir);

        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
        
        // remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(createData.getServerInstanceID());
        
        // downgrade the Java platform or src level to 1.4
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
                
        handle.progress(NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        Utils.setSteps(panels, STEPS);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR, null);
        this.wiz.putProperty(ProjectLocationWizardPanel.NAME, null);
        this.wiz.putProperty(WizardProperties.SOURCE_ROOT, null);
        this.wiz.putProperty(WizardProperties.JAVA_ROOT, null);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, null);
        this.wiz.putProperty(WizardProperties.CONFIG_FILES_FOLDER, null);
        this.wiz.putProperty(WizardProperties.LIB_FOLDER, null);
        this.wiz.putProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID, null);
        this.wiz.putProperty(ProjectServerWizardPanel.J2EE_LEVEL, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_WizardStepsCount"),
                index + 1, panels.length);
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
    
}

