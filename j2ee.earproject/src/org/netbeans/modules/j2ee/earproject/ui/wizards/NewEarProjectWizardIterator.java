/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectCreateData;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerRun;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Enterprise Application project.
 * @author Jesse Glick
 */
public class NewEarProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    transient WizardDescriptor wiz;
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectLocationWizardPanel(J2eeModule.EAR, 
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "TXT_NewProject"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName")), // NOI18N
            new ProjectServerWizardPanel(J2eeModule.EAR, 
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "NewEarProjectWizardIterator.secondStep"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "TXT_NewProject"),
                    false, false, false, true, false, true),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "NewEarProjectWizardIterator.secondStep"), 
        };
    }
    
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(9);
        handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty(ProjectLocationWizardPanel.NAME);
        String serverInstanceID = (String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID);
        Profile j2eeLevel = (Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL);
        Boolean createWAR = (Boolean) wiz.getProperty(ProjectServerWizardPanel.CREATE_WAR);
        String warName = null;
        if (createWAR.booleanValue()) {
            warName = (String) wiz.getProperty(ProjectServerWizardPanel.WAR_NAME);
        }
        Boolean createJAR = (Boolean) wiz.getProperty(ProjectServerWizardPanel.CREATE_JAR);
        String ejbJarName = null;
        if (createJAR.booleanValue()) {
            ejbJarName = (String) wiz.getProperty(ProjectServerWizardPanel.JAR_NAME);
        }
        Boolean createCAR = (Boolean) wiz.getProperty(ProjectServerWizardPanel.CREATE_CAR);
        String carName = null;
        String mainClass = null;
        if (createCAR.booleanValue()) {
            carName = (String) wiz.getProperty(ProjectServerWizardPanel.CAR_NAME);
            mainClass = (String) wiz.getProperty(ProjectServerWizardPanel.MAIN_CLASS);
        }
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        Boolean cdi = (Boolean)wiz.getProperty(ProjectServerWizardPanel.CDI);
        // remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(serverInstanceID);
        
        String librariesDefinition =
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES));
        SharableLibrariesUtils.setLastProjectSharable(librariesDefinition != null);
        return testableInstantiate(dirF,name,j2eeLevel, serverInstanceID, warName,
                ejbJarName, carName, mainClass, platformName, sourceLevel, handle, 
                librariesDefinition, cdi);
    }
    
    /** <strong>Package private for unit test only</strong>. */
    static Set<FileObject> testableInstantiate(File dirF, String name, Profile j2eeProfile,
            String serverInstanceID, String warName, String ejbJarName, String carName,
            String mainClass, String platformName, String sourceLevel, ProgressHandle handle,
            String librariesDefinition, Boolean cdi) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        AntProjectHelper h = EarProjectGenerator.createProject(dirF, name, j2eeProfile,
                serverInstanceID, sourceLevel, librariesDefinition);
        if (handle != null)
            handle.progress(2);
        FileObject dir = FileUtil.toFileObject(dirF);
        Project p = ProjectManager.getDefault().findProject(dir);
        EarProject earProject =  p.getLookup().lookup(EarProject.class);
        if (null != earProject) {
            Application app = null;
            try {
                app = earProject.getAppModule().getApplication();
                if (app != null && EarProjectUtil.isDDWritable(earProject)) {
                    app.setDisplayName(name);
                    app.write(earProject.getAppModule().getDeploymentDescriptor());
                }
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            }
        }
        
        resultSet.add(dir);
        
        if (librariesDefinition != null) {
            File libLocation = new File(librariesDefinition);
            if (!libLocation.isAbsolute()) {
                librariesDefinition = ".." + File.separatorChar + librariesDefinition; // NOI18N
            }
        }
        
        AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
        Project webProject = null;
        if (null != warName) {
            File webAppDir = FileUtil.normalizeFile(new File(dirF, warName));

            WebProjectCreateData createData = new WebProjectCreateData();
            createData.setProjectDir(webAppDir);
            createData.setName(warName);
            createData.setServerInstanceID(serverInstanceID);
            createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
            createData.setJavaEEProfile(EarProjectGenerator.getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.WAR));
            createData.setContextPath('/' + warName); //NOI18N
            createData.setJavaPlatformName(platformName);
            createData.setSourceLevel(sourceLevel);
            createData.setLibrariesDefinition(librariesDefinition);
            createData.setCDIEnabled(cdi);

            if (handle != null) {
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_WAR"), 3);
            }
            AntProjectHelper webHelper = WebProjectUtilities.createProject(createData);
            if (handle != null) {
                handle.progress(4);
            }

            FileObject webAppDirFO = FileUtil.toFileObject(webAppDir);
            webProject = ProjectManager.getDefault().findProject(webAppDirFO);
            WebModule wm = WebModule.getWebModule(webAppDirFO);
            WebProjectUtilities.ensureWelcomePage(wm.getDocumentBase(), wm.getDeploymentDescriptor(), j2eeProfile);
            
            EarProjectProperties.addJ2eeSubprojects(earProject, new Project[] { webProject });
            resultSet.add(webAppDirFO);
        }
        Project appClient = null;
        if (null != carName) {
            File carDir = FileUtil.normalizeFile(new File(dirF,carName));

            AppClientProjectCreateData createData = new AppClientProjectCreateData();
            createData.setProjectDir(carDir);
            createData.setName(carName);
            createData.setMainClass(mainClass);
            createData.setJavaEEProfile(EarProjectGenerator.getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.CAR));
            createData.setServerInstanceID(serverInstanceID);
            createData.setLibrariesDefinition(librariesDefinition);
            createData.setCDIEnabled(cdi);

            if (handle != null)
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_AppClient"), 5);
            AntProjectHelper clientHelper = AppClientProjectGenerator.createProject(createData);
            if (handle != null)
                handle.progress(6);

            if (platformName != null || sourceLevel != null) {
                AppClientProjectGenerator.setPlatform(clientHelper, platformName, sourceLevel);
            }
            FileObject carDirFO = FileUtil.toFileObject(carDir);
            appClient = ProjectManager.getDefault().findProject(carDirFO);
            
            EarProjectProperties.addJ2eeSubprojects(earProject, new Project[] { appClient });
            resultSet.add(carDirFO);
        }
        if (null != ejbJarName) {
            File ejbJarDir = FileUtil.normalizeFile(new File(dirF,ejbJarName));

            EjbJarProjectCreateData createData = new EjbJarProjectCreateData();
            createData.setProjectDir(ejbJarDir);
            createData.setName(ejbJarName);
            createData.setJavaEEProfile(EarProjectGenerator.getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.EJB));
            createData.setServerInstanceID(serverInstanceID);
            createData.setLibrariesDefinition(librariesDefinition);
            createData.setCDIEnabled(cdi);

            if (handle != null)
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_EJB"), 7);
            AntProjectHelper ejbHelper = EjbJarProjectGenerator.createProject(createData);
            if (handle != null)
                handle.progress(8);

            if (platformName != null || sourceLevel != null) {
                EjbJarProjectGenerator.setPlatform(ejbHelper, platformName, sourceLevel);
            }
            FileObject ejbJarDirFO = FileUtil.toFileObject(ejbJarDir);
            Project ejbJarProject = ProjectManager.getDefault().findProject(ejbJarDirFO);
            EarProjectProperties.addJ2eeSubprojects(earProject, new Project[] { ejbJarProject });
            resultSet.add(ejbJarDirFO);
            EarProjectGenerator.addEJBToClassPaths(ejbJarProject, appClient, webProject); // #74123
        }
        CustomizerRun.ApplicationUrisComboBoxModel.initializeProperties(earProject, warName, carName);
        NewEarProjectWizardIterator.setProjectChooserFolder(dirF);
        
        if (handle != null)
            handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_PreparingToOpen"), 9);
        
        return resultSet;
    }
    
    static void setProjectChooserFolder(final File dirF) {
        File parentF = (dirF != null) ? dirF.getParentFile() : null;
        if (parentF != null && parentF.exists()) {
            ProjectChooser.setProjectsFolder(parentF);
        }
    }
    
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
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR,null);
            this.wiz.putProperty(ProjectLocationWizardPanel.NAME,null);
        }
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_WizardStepsCount"),
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
    
    // helper methods, finds indexJSP's FileObject
    FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // XXX: ignore unvalid mainClass?
        return webRoot.getFileObject(indexJSP.replace('.', '/'), "jsp"); // NOI18N
    }
    
}
