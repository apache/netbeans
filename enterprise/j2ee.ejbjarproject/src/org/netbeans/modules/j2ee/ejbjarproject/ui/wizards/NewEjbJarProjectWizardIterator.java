/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

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
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewEjbJarProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[] {
        NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
        NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "NewEjbJarProjectWizardIterator.secondpanel"),
    };

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectLocationWizardPanel(J2eeModule.EJB, 
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "TXT_NewWebApp"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName")), // NOI18N
            new ProjectServerWizardPanel(J2eeModule.EJB, 
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "NewEjbJarProjectWizardIterator.secondpanel"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "TXT_NewWebApp"),
                    true, false, false, false, false, true),
        };
    }

    @Override
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NewEjbJarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        
        EjbJarProjectCreateData createData = new EjbJarProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName((String) wiz.getProperty(ProjectLocationWizardPanel.NAME));
        createData.setServerInstanceID((String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID));
        createData.setJavaEEProfile((Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL));
        createData.setLibrariesDefinition(
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES)));
        createData.setCDIEnabled((Boolean)wiz.getProperty(ProjectServerWizardPanel.CDI));
        
        AntProjectHelper h = EjbJarProjectGenerator.createProject(createData);
        
        handle.progress(2);
        FileObject dir = FileUtil.toFileObject(dirF);
        
        Project earProject = (Project) wiz.getProperty(ProjectServerWizardPanel.EAR_APPLICATION);
        EjbJarProject createdEjbJarProject = (EjbJarProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdEjbJarProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addEjbJarModule(createdEjbJarProject.getAPIEjbJar());
            }
        }
        
        // remember last used server
    	UserProjectSettings.getDefault().setLastUsedServer(createData.getServerInstanceID());
        SharableLibrariesUtils.setLastProjectSharable(createData.getLibrariesDefinition() != null);
        
        // downgrade the Java platform or src level to 1.4        
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            sourceLevel = adaptSourceLevelToJavaEEProfile(createData.getJavaEEProfile(), sourceLevel);
            EjbJarProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
        
        handle.progress(NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NewEjbJarProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        
        resultSet.add(dir);
        
        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
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
        Utils.logUsage(NewEjbJarProjectWizardIterator.class, "USG_PROJECT_CREATE_EJB", parameters); //NOI18N

        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        Utils.setSteps(panels, STEPS);
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR,null);
        this.wiz.putProperty(ProjectLocationWizardPanel.NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(NewEjbJarProjectWizardIterator.class,
                "LBL_WizardStepsCount", //NOI18N
                index + 1, panels.length);
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}

    @Override
    public final void removeChangeListener(ChangeListener l) {}

    private static String adaptSourceLevelToJavaEEProfile(Profile javaEEProfile, String defaultSourceLevel) {
        if (javaEEProfile.isAtLeast(Profile.JAKARTA_EE_11_WEB)) {
            return "21"; //NOI18N
        } else if (javaEEProfile.isAtLeast(Profile.JAKARTA_EE_9_1_WEB)) {
            return "11"; //NOI18N
        } else if (javaEEProfile.isAtLeast(Profile.JAVA_EE_8_WEB)) {
            return "1.8"; //NOI18N
        } else if (javaEEProfile.isAtLeast(Profile.JAVA_EE_7_WEB)) {
            return "1.7"; //NOI18N
        } else if (javaEEProfile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
            return "1.6"; //NOI18N
        } else if (javaEEProfile.isAtLeast(Profile.JAVA_EE_5)) {
            return "1.5"; //NOI18N
        }

        return defaultSourceLevel;
    }
    
}
