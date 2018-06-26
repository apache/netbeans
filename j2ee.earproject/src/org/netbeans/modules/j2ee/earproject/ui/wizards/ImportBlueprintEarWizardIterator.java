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
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectImportLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard for importing a new Enterprise Application project.
 * @author Jesse Glick
 */
public class ImportBlueprintEarWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    transient WizardDescriptor wiz;
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectImportLocationWizardPanel(J2eeModule.EAR, 
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "TXT_ImportProject"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_ImportInstructions1")),
            new ProjectServerWizardPanel(J2eeModule.EAR, 
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "NewEarProjectWizardIterator.secondStep"),
                    NbBundle.getMessage(NewEarProjectWizardIterator.class, "TXT_ImportProject"),
                    false, false, false, false, true, false),
            new PanelModuleDetection()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "NewEarProjectWizardIterator.secondStep"), 
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_IW_ApplicationModulesStep")
        };
    }
    
    public Set<FileObject> instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        File srcF = (File) wiz.getProperty(WizardProperties.SOURCE_ROOT);
        if (srcF != null) {
            srcF = FileUtil.normalizeFile(srcF);
        }
        String name = (String) wiz.getProperty(ProjectLocationWizardPanel.NAME);
        Profile j2eeProfile = (Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL);
        //        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        String serverInstanceID = (String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID);
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        @SuppressWarnings("unchecked")
        Map<FileObject, ModuleType> userModules = (Map<FileObject, ModuleType>)
                wiz.getProperty(WizardProperties.USER_MODULES);
        String librariesDefinition =
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES));
        return testableInstantiate(platformName, sourceLevel, j2eeProfile, dirF,
                srcF, serverInstanceID, name, userModules, handle, librariesDefinition);
    }
    
    /** <strong>Package private for unit test only</strong>. */
    static Set<FileObject> testableInstantiate(final String platformName,
            final String sourceLevel, final Profile j2eeProfile, final File dirF,
            final File srcF, final String serverInstanceID, final String name,
            final Map<FileObject, ModuleType> userModules, ProgressHandle handle,
            String librariesDefinition) throws IOException {
        
        EarProjectGenerator.importProject(dirF, srcF, name, j2eeProfile,
                serverInstanceID, platformName, sourceLevel, userModules, 
                librariesDefinition);
        if (handle != null) {
            handle.progress(2);
        }

        FileObject dir = FileUtil.toFileObject(dirF);
        
        // remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(serverInstanceID);
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(dir);
        
        NewEarProjectWizardIterator.setProjectChooserFolder(dirF);
        
        if (handle != null) {
            handle.progress(NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        }

        // Returning set of FileObject of project diretory.
        // Project will be open and set as main
        return resultSet;
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
                NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_WizardStepsCount"),
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
