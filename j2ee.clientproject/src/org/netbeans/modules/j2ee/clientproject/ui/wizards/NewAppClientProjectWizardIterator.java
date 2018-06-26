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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
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
                new Object[] {new Integer(index + 1), new Integer(panels.length) });
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
