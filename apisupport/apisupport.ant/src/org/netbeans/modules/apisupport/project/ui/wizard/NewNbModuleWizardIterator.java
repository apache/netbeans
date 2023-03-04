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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard to create a new NetBeans Module project.
 *
 * @author Martin Krauskopf
 */
public class NewNbModuleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    
    private static final String FOLDER = "Project/AntJava/APISupport";
    private static final String MODULE_ICON = "org/netbeans/modules/apisupport/project/resources/module.png";
    private static final String SUITE_ICON = "org/netbeans/modules/apisupport/project/suite/resources/suite.png";

    enum Type {
        /** Either standalone module, suite component or NB.org module. */
        MODULE,
        /** Suite wizard. */
        SUITE,
        /** Application-mode suite. */
        APPLICATION,
        /** Library wrapper module wizard. */
        LIBRARY_MODULE,
        /** Pure suite component wizard. */
        SUITE_COMPONENT,
    }
    
    /**
     * Property under which a suite to be selected in the suite combo can be
     * stored.
     */
    static final String PREFERRED_SUITE_DIR = "preferredSuiteDir"; // NOI18N
    
    /** Tells whether the wizard should be run in a suite dedicate mode. */
    static final String ONE_SUITE_DEDICATED_MODE = "oneSuiteDedicatedMode"; // NOI18N
    
    private final NewModuleProjectData data;
    private int position;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private FileObject createdProjectFolder;
    
    /** See {@link #PREFERRED_SUITE_DIR}. */
    private String preferredSuiteDir;
    
    /** See {@link #ONE_SUITE_DEDICATED_MODE}. */
    private Boolean suiteDedicated = Boolean.FALSE; // default
    
    /** Create a new wizard iterator. */
    private NewNbModuleWizardIterator(NewNbModuleWizardIterator.Type type) {
        data = new NewModuleProjectData(type);
    }
    
    /**
     * Returns wizard for creating NetBeans module in general - i.e. either
     * standalone module, suite component or NB.org module.
     */
    @TemplateRegistration(folder = FOLDER, position = 100, displayName = "#template_module", iconBase = MODULE_ICON, description = "../../resources/emptyModule.html")
    @Messages("template_module=Module")
    public static NewNbModuleWizardIterator createModuleIterator() {
        return new NewNbModuleWizardIterator(Type.MODULE);
    }
    
    /**
     * Returns wizard for creating suite component <strong>only</strong>.
     */
    public static NewNbModuleWizardIterator createSuiteComponentIterator(final SuiteProject suite) {
        NewNbModuleWizardIterator iterator = new NewNbModuleWizardIterator(Type.SUITE_COMPONENT);
        iterator.preferredSuiteDir = suite.getProjectDirectoryFile().getAbsolutePath();
        iterator.suiteDedicated = Boolean.TRUE;
        return iterator;
    }
    
    @TemplateRegistration(folder = FOLDER, position = 200, displayName = "#template_suite", iconBase = SUITE_ICON, description = "../../resources/emptySuite.html")
    @Messages("template_suite=Module Suite")
    public static NewNbModuleWizardIterator createSuiteIterator() {
        return new NewNbModuleWizardIterator(Type.SUITE);
    }
    
    @TemplateRegistration(folder = FOLDER, position = 400, displayName = "#template_application", iconBase = SUITE_ICON, description = "../../resources/emptyApplication.html")
    @Messages("template_application=NetBeans Platform Application")
    public static NewNbModuleWizardIterator createApplicationIterator() {
        return new NewNbModuleWizardIterator(Type.APPLICATION);
    }
    
    /**
     * Returns wizard for creating library wrapper module
     * <strong>only</strong>. Given project <strong>must</strong> have an
     * instance of {@link SuiteProvider} in its lookup.
     */
    public static NewNbModuleWizardIterator createLibraryModuleIterator(final Project project) {
        NewNbModuleWizardIterator iterator = new NewNbModuleWizardIterator(Type.LIBRARY_MODULE);
        iterator.preferredSuiteDir = SuiteUtils.getSuiteDirectoryPath(project);
        assert iterator.preferredSuiteDir != null : project + " does not have a SuiteProvider in its lookup?"; // NOI18N
        iterator.suiteDedicated = Boolean.TRUE;
        return iterator;
    }
    
    @TemplateRegistration(folder = FOLDER, position = 300, displayName = "#template_library_module", iconBase = MODULE_ICON, description = "../../resources/libraryModule.html")
    @Messages("template_library_module=Library Wrapper Module")
    public static NewNbModuleWizardIterator createLibraryModuleIterator() {
        return new NewNbModuleWizardIterator(Type.LIBRARY_MODULE);
    }

    static boolean isSuiteWizard(Type type) {
        return type == Type.SUITE || type == Type.APPLICATION;
    }

    static boolean isSuiteComponentWizard(Type type) {
        return type == Type.SUITE_COMPONENT;
    }

    static boolean isLibraryWizard(Type type) {
        return type == Type.LIBRARY_MODULE;
    }

    public FileObject getCreateProjectFolder() {
        return createdProjectFolder;
    }
    
    public Set instantiate() throws IOException {
        final File projectFolder = new File(data.getProjectFolder());
        ModuleUISettings.getDefault().setLastUsedPlatformID(data.getPlatformID());
        WizardDescriptor settings = data.getSettings();
        switch (data.getWizardType()) {
            case SUITE:
                ModuleUISettings.getDefault().setNewSuiteCounter(data.getSuiteCounter());
                SuiteProjectGenerator.createSuiteProject(projectFolder, data.getPlatformID(), false);
                break;
            case APPLICATION:
                ModuleUISettings.getDefault().setNewSuiteCounter(data.getSuiteCounter());
                SuiteProjectGenerator.createSuiteProject(projectFolder, data.getPlatformID(), true);
                break;
            case MODULE:
            case SUITE_COMPONENT:
                ModuleUISettings.getDefault().setNewModuleCounter(data.getModuleCounter());
                if (data.isNetBeansOrg()) {
                    // create module within the netbeans.org source tree
                    NbModuleProjectGenerator.createNetBeansOrgModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), null, data.isOSGi());
                } else if (data.isStandalone()) {
                    // create standalone module
                    NbModuleProjectGenerator.createStandAloneModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), null, data.getPlatformID(), data.isOSGi(), false);
                } else {
                    // create suite-component module
                    NbModuleProjectGenerator.createSuiteComponentModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), null, new File(data.getSuiteRoot()), data.isOSGi(), false);
                }
                break;
            case LIBRARY_MODULE:
                // create suite-component module
                File[] jars = LibraryStartVisualPanel.convertStringToFiles((String) settings.getProperty(LibraryStartVisualPanel.PROP_LIBRARY_PATH));
                
                File license = null;
                String licPath = (String) settings.getProperty(LibraryStartVisualPanel.PROP_LICENSE_PATH);
                if (licPath != null && licPath.length() > 0) {
                    license = new File(licPath);
                }
                NbModuleProjectGenerator.createSuiteLibraryModule(projectFolder,
                        data.getCodeNameBase(), data.getProjectDisplayName(),
                        data.getBundle(), new File(data.getSuiteRoot()),
                        license, jars);
                break;
            default:
                throw new IllegalStateException("Uknown wizard type: " + data.getWizardType()); // NOI18N
        }
        
        this.createdProjectFolder = FileUtil.toFileObject(FileUtil.normalizeFile(projectFolder));
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(createdProjectFolder);

        if (!projectFolder.getParent().equals(data.getSuiteRoot())) { // #184830
            ApisupportAntUIUtils.setProjectChooserDirParent(projectFolder);
        }

        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        data.setSettings(wiz);
        if (preferredSuiteDir == null) {
            Project mainPrj = OpenProjects.getDefault().getMainProject();
            if (mainPrj != null) {
                preferredSuiteDir = SuiteUtils.getSuiteDirectoryPath(mainPrj);
            }
        }
        if (preferredSuiteDir != null) {
            wiz.putProperty(PREFERRED_SUITE_DIR, preferredSuiteDir);
            wiz.putProperty(ONE_SUITE_DEDICATED_MODE, suiteDedicated);
        }
        
        position = 0;
        String[] steps = null;
        switch (data.getWizardType()) {
            case MODULE:
            case SUITE_COMPONENT:
                steps = initModuleWizard();
                break;
            case SUITE:
            case APPLICATION:
                steps = initSuiteModuleWizard();
                break;
            case LIBRARY_MODULE:
                steps = initLibraryModuleWizard();
                break;
            default:
                assert false : "Should never get here. type: "  + data.getWizardType();
        }
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // step number
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // names of currently used steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                
                // Following is actually needed only by direct usage of this wizard.
                // Turn on subtitle creation on each step
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    private String[] initModuleWizard() {
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new BasicInfoWizardPanel(data));
        panels.add(new BasicConfWizardPanel(data));
        String[] steps = {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_BasicConfigPanel_Title") // NOI18N
        };
        return steps;
    }
    
    private String[] initSuiteModuleWizard() {
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new BasicInfoWizardPanel(data));
        String[] steps = {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
        };
        return steps;
    }
    
    private String[] initLibraryModuleWizard() {
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new LibraryStartWizardPanel(data));
        panels.add(new BasicInfoWizardPanel(data));
        panels.add(new LibraryConfWizardPanel(data));
        String[] steps = new String[] {
            getMessage("LBL_LibraryStartPanel_Title"), //NOi18N
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_PlatformSelectionPanel_Title") // NOI18N
        };
        return steps;
    }
    
    public String name() {
        // TemplateWizard internally does not use the value returned by this
        // method so we may return whatever (e.g. null) in the meantime. But it
        // would be resolved as "null" string by MessageFormat. So probably the
        // safest is to return empty string.
        return "";
    }
    
    public boolean hasNext() {
        return position < (panels.size() - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(position);
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    static String getMessage(String key) {
        return NbBundle.getMessage(NewNbModuleWizardIterator.class, key);
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
