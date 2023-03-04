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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public final class NewFileWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 5846231213213L;

    private static final Logger LOGGER = Logger.getLogger(NewFileWizardIterator.class.getName());

    private final BottomPanel bottomPanel;
    private WizardDescriptor wizard;
    private PhpProject phpProject;
    private WizardDescriptor.Panel<WizardDescriptor>[] wizardPanels;
    private int index;

    private NewFileWizardIterator(BottomPanel bottomPanel) {
        this.bottomPanel = bottomPanel;
    }

    public static NewFileWizardIterator simple() {
        return new NewFileWizardIterator(null);
    }

    public static NewFileWizardIterator withNamespace() {
        return new NewFileWizardIterator(new NewFileNamespacePanel());
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard), getTemplateParams());

        // #187374
        try {
            FileUtils.reformatFile(createdFile);
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, exc.getMessage(), exc);
        }

        return Collections.singleton(createdFile.getPrimaryFile());
    }

    private Map<String, Object> getTemplateParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(CreateDescriptor.FREE_FILE_EXTENSION, true);
        params.put("namespace", wizard.getProperty(NewFileNamespacePanel.NAMESPACE)); // NOI18N
        return params;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        phpProject = getPhpProject();
        checkPhpProject();
        setTargetFolder();
        wizardPanels = getPanels();

        // Make sure list of steps is accurate.
        String[] beforeSteps = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        int beforeStepLength = beforeSteps.length - 1;
        String[] steps = createSteps(beforeSteps);
        for (int i = 0; i < wizardPanels.length; i++) {
            Component c = wizardPanels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i + beforeStepLength - 1)); // NOI18N
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    private void checkPhpProject() {
        if (phpProject == null) {
            // not php project
            return;
        }
        if (PhpProjectValidator.isFatallyBroken(phpProject)) {
            UiUtils.warnBrokenProject(phpProject.getPhpModule());
        }
    }

    private void setTargetFolder() {
        if (Templates.getTargetFolder(wizard) != null) {
            // already set
            return;
        }
        if (phpProject == null) {
            // not php project
            return;
        }
        FileObject srcDir = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
        if (srcDir != null && srcDir.isValid()) {
            Templates.setTargetFolder(wizard, srcDir);
        }
    }

    private PhpProject getPhpProject() {
        Project project = Templates.getProject(wizard);
        if (project == null) {
            // no project => ignore
            return null;
        }
        if (!(project instanceof PhpProject)) {
            // XXX check convertor project
            LOGGER.log(Level.WARNING, "PHP project expected but found {0}", project.getClass().getName());
            return null;
        }
        return (PhpProject) project;
    }

    private String[] createSteps(String[] beforeSteps) {
        int beforeStepLength = beforeSteps.length - 1;
        String[] res = new String[beforeStepLength + wizardPanels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeStepLength)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = wizardPanels[i - beforeStepLength].getComponent().getName();
            }
        }
        return res;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        wizardPanels = null;
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    /** Get the current panel.
     * @return the panel
     */
    @Override
    public Panel<WizardDescriptor> current() {
        return wizardPanels[index];
    }

    /** Test whether there is a next panel.
     * @return <code>true</code> if so
     */
    @Override
    public boolean hasNext() {
        return index < wizardPanels.length - 1;
    }

    /** Test whether there is a previous panel.
     * @return <code>true</code> if so
     */
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    /** Move to the next panel.
     * I.e. increment its index, need not actually change any GUI itself.
     * @throws NoSuchElementException if the panel does not exist
     */
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /** Move to the previous panel.
     * I.e. decrement its index, need not actually change any GUI itself.
     * @throws NoSuchElementException if the panel does not exist
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        BottomPanel bottomPanelForPhpProject = getBottomPanelForPhpProject();
        if (bottomPanelForPhpProject != null) {
            bottomPanelForPhpProject.addChangeListener(l);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        BottomPanel bottomPanelForPhpProject = getBottomPanelForPhpProject();
        if (bottomPanelForPhpProject != null) {
            bottomPanelForPhpProject.removeChangeListener(l);
        }
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        assert EventQueue.isDispatchThread();
        Project project = Templates.getProject(wizard);
        SourceGroup[] groups = PhpProjectUtils.getSourceGroups(project);
        // #218437
        if (phpProject != null) {
            // php project found
            if (groups.length == 0 && !PhpProjectValidator.isFatallyBroken(phpProject)) {
                // sources found but no source roots?!
                FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
                List<FileObject> tests = ProjectPropertiesSupport.getTestDirectories(phpProject, false);
                FileObject selenium = ProjectPropertiesSupport.getSeleniumDirectory(phpProject, false);
                SourceRoots sourceRoots = phpProject.getSourceRoots();
                SourceRoots testRoots = phpProject.getTestRoots();
                SourceRoots seleniumRoots = phpProject.getSeleniumRoots();

                StringBuilder sb = new StringBuilder(200);
                addDiagnosticForDirs(sb, phpProject, sources, tests, selenium);
                addDiagnosticForRoots(sb, sourceRoots, testRoots, seleniumRoots);
                LOGGER.log(Level.WARNING, sb.toString(),
                        new IllegalStateException("No source roots found (attach your IDE log to https://netbeans.org/bugzilla/show_bug.cgi?id=218437)"));

                // try to recover...
                sourceRoots.refresh();
                testRoots.refresh();
                seleniumRoots.refresh();
                sb = new StringBuilder(200);
                addDiagnosticForRoots(sb, sourceRoots, testRoots, seleniumRoots);
                LOGGER.log(Level.WARNING, sb.toString(),
                        new IllegalStateException("Trying to fire changes for all source roots"));

                groups = PhpProjectUtils.getSourceGroups(project);
            }
        }
        // fallback (e.g. convertor project)
        if (groups.length == 0) {
            // XXX check convertor project?
            groups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
        }
        final BottomPanel bottomPanelForPhpProject = getBottomPanelForPhpProject();
        Templates.SimpleTargetChooserBuilder targetChooserBuilder = Templates.buildSimpleTargetChooser(project, groups);
        if (bottomPanelForPhpProject != null) {
            targetChooserBuilder
                    .bottomPanel(bottomPanelForPhpProject);
        }
        final WizardDescriptor.Panel<WizardDescriptor> simpleTargetChooserPanel = targetChooserBuilder
                .freeFileExtension()
                .create();
        if (bottomPanelForPhpProject != null) {
            simpleTargetChooserPanel.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    // #240917 hack - it is not possible to listen on panel (name and location)
                    // GH #4099 ugly hack
                    Component component = simpleTargetChooserPanel.getComponent();
                    if (component instanceof JPanel) {
                        // SimpleTargetChooserPanelGUI
                        JPanel panel = (JPanel) component;
                        for (Component c : panel.getComponents()) {
                            if (c instanceof JTextField) {
                                JTextField textField = (JTextField) c;
                                if (!textField.isEditable()) {
                                    String text = textField.getText();
                                    if (text != null && text.endsWith(".php")) { // NOI18N
                                        File file = new File(text);
                                        File parent = file.getParentFile();
                                        if (parent == null || !parent.exists()) {
                                            return;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    WizardDescriptor descriptor = new DummyWizardDescriptor();
                    simpleTargetChooserPanel.storeSettings(descriptor);
                    bottomPanelForPhpProject.targetFolderChanged(Templates.getTargetFolder(descriptor));
                }
            });
        }
        @SuppressWarnings("unchecked") // Generic Array Creation
        WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                    simpleTargetChooserPanel
                };
        return panels;
    }

    @CheckForNull
    private BottomPanel getBottomPanelForPhpProject() {
        if (bottomPanel == null) {
            return null;
        }
        if (phpProject == null) {
            // unknown project type, return what we have
            return bottomPanel;
        }
        if (bottomPanel.isPresentForProject(phpProject)) {
            return bottomPanel;
        }
        return null;
    }

    private void addDiagnosticForDirs(StringBuilder sb, PhpProject project, FileObject sources, List<FileObject> tests, FileObject selenium) {
        sb.append("project directory equals sources: "); // NOI18N
        sb.append(project.getProjectDirectory().equals(sources));
        sb.append(";\n sources (not null, valid): "); // NOI18N
        sb.append(sources != null);
        sb.append(", "); // NOI18N
        sb.append(sources != null && sources.isValid());
        sb.append(";\n tests (not null, valid): "); // NOI18N
        for (FileObject test : tests) {
            sb.append("["); // NOI18N
            sb.append(test != null); // NOI18N
            sb.append(", "); // NOI18N
            sb.append(test != null && test.isValid());
            sb.append("]"); // NOI18N
        }
        sb.append(";\n selenium (not null, valid): "); // NOI18N
        sb.append(selenium != null);
        sb.append(", "); // NOI18N
        sb.append(selenium != null && selenium.isValid());
    }

    private void addDiagnosticForRoots(StringBuilder sb, SourceRoots sourceRoots, SourceRoots testRoots, SourceRoots seleniumRoots) {
        sb.append(";\n sourceRoots (fired changes): "); // NOI18N
        sb.append(Arrays.asList(sourceRoots.getRoots()));
        sb.append(" ("); // NOI18N
        sb.append(sourceRoots.getFiredChanges());
        sb.append(");\n testRoots (fired changes): "); // NOI18N
        sb.append(Arrays.asList(testRoots.getRoots()));
        sb.append(" ("); // NOI18N
        sb.append(testRoots.getFiredChanges());
        sb.append(");\n seleniumRoots (fired changes): "); // NOI18N
        sb.append(Arrays.asList(seleniumRoots.getRoots()));
        sb.append(" ("); // NOI18N
        sb.append(seleniumRoots.getFiredChanges());
        sb.append(")"); // NOI18N
    }

    //~ Inner classes

    // PropertyChangeListener should be used for listening on the main panel - not possible now so it is not used
    interface BottomPanel extends WizardDescriptor.Panel<WizardDescriptor> {

        void targetFolderChanged(@NullAllowed FileObject targetFolder);

        boolean isPresentForProject(PhpProject project);

    }

    private static final class DummyWizardDescriptor extends WizardDescriptor {

        public DummyWizardDescriptor() {
        }

    }

}
