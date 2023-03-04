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
package org.netbeans.modules.css.prep.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.css.prep.ui.customizer.OptionsPanel;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private final CssPreprocessorType type;

    WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor> wizardPanel;
    // used in a background thread in instantiate() method
    private volatile BottomPanel bottomPanel;


    public NewFileWizardIterator(CssPreprocessorType type) {
        assert type != null;
        this.type = type;
    }

    @TemplateRegistration(folder = "ClientSide", category = "html5",
            content = "../resources/style.less",
            description = "../resources/NewLessFileDescription.html",
            position = 320,
            displayName = "#NewFileWizardIterator.less.template.displayName",
            scriptEngine = "freemarker")
    @NbBundle.Messages("NewFileWizardIterator.less.template.displayName=LESS File")
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createLessWizardIterator() {
        return new NewFileWizardIterator(CssPreprocessorType.LESS);
    }

    @TemplateRegistration(folder = "ClientSide", category = "html5",
            content = "../resources/style.scss",
            description = "../resources/NewSassFileDescription.html",
            position = 310,
            displayName = "#NewFileWizardIterator.scss.template.displayName",
            scriptEngine = "freemarker")
    @NbBundle.Messages("NewFileWizardIterator.scss.template.displayName=Sass File")
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createSassWizardIterator() {
        return new NewFileWizardIterator(CssPreprocessorType.SASS);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizardPanel = createWizardPanel();
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        getBottomPanel().save();

        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard));
        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
        wizardPanel = null;
        bottomPanel = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return wizardPanel;
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void previousPanel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getBottomPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getBottomPanel().removeChangeListener(listener);
    }

    private WizardDescriptor.Panel<WizardDescriptor> createWizardPanel() {
        Project project = getProject();
        assert project != null;
        // #233484
        ensureProperTargetFolder(project);
        return Templates.buildSimpleTargetChooser(project, getSourceGroups(project))
                .bottomPanel(getBottomPanel())
                .create();
    }

    private Project getProject() {
        return Templates.getProject(wizard);
    }

    private void ensureProperTargetFolder(Project project) {
        if (!project.getProjectDirectory().equals(Templates.getTargetFolder(wizard))) {
            // some folder set
            return;
        }
        FileObject webRoot = findWebRoot(project);
        if (webRoot == null) {
            // nothing we can do here
            return;
        }
        // prefer mappings if folder exists
        List<Pair<String, String>> mappings = type.getPreferences().getMappings(project);
        if (!mappings.isEmpty()) {
            File inputFolder = CssPreprocessorUtils.resolveInput(webRoot, mappings.get(0));
            if (inputFolder.isDirectory()) {
                FileObject fo = FileUtil.toFileObject(inputFolder);
                assert fo != null : "FileObject not found for existing directory: " + inputFolder;
                Templates.setTargetFolder(wizard, fo);
                return;
            }
        }
        // use web root
        Templates.setTargetFolder(wizard, webRoot);
    }

    @CheckForNull
    private FileObject findWebRoot(Project project) {
        Collection<FileObject> webRoots = ProjectWebRootQuery.getWebRoots(project);
        if (webRoots.isEmpty()) {
            return null;
        }
        // simply return the first one
        return webRoots.iterator().next();
    }

    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(Sources.TYPE_GENERIC);
    }

    private BottomPanel getBottomPanel() {
        if (bottomPanel != null) {
            return bottomPanel;
        }
        if (!type.getPreferences().isConfigured(getProject())) {
            bottomPanel = new BottomPanelImpl(getProject(), type);
        } else {
            bottomPanel = BottomPanel.EMPTY;
        }
        return bottomPanel;
    }

    //~ Inner classes

    private interface BottomPanel extends WizardDescriptor.Panel<WizardDescriptor> {

        BottomPanel EMPTY = new EmptyBottomPanel();

        void save() throws IOException;
    }

    private static final class EmptyBottomPanel implements BottomPanel {

        @Override
        public Component getComponent() {
            return new JPanel();
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void save() throws IOException {
        }

    }

    private static class BottomPanelImpl implements BottomPanel, PropertyChangeListener {

        private static final String ENABLED = "ENABLED"; // NOI18N
        private static final String MAPPINGS = "MAPPINGS"; // NOI18N
        private static final String COMPILER_OPTIONS = "COMPILER_OPTIONS"; // NOI18N

        private final Project project;
        private final CssPreprocessorType type;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private volatile OptionsPanel panel;
        private volatile WizardDescriptor settings = null;


        public BottomPanelImpl(Project project, CssPreprocessorType type) {
            assert project != null;
            assert type != null;

            this.project = project;
            this.type = type;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx("org.netbeans.modules.css.prep.ui.wizard.NewFileWizardIterator." + type.name()); // NOI18N
        }

        @Override
        public OptionsPanel getComponent() {
            if (panel == null) {
                assert EventQueue.isDispatchThread();
                CssPreprocessorPreferences preferences = type.getPreferences();
                panel = new OptionsPanel(type, project, preferences.isEnabled(project),
                        preferences.getMappings(project), preferences.getCompilerOptions(project));
                panel.showConfigureExecutableButton();
            }
            return panel;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.settings = settings;
            settings.putProperty(ENABLED, getComponent().isCompilationEnabled());
            settings.putProperty(MAPPINGS, getComponent().getMappings());
            settings.putProperty(COMPILER_OPTIONS, getComponent().getCompilerOptions());
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty(ENABLED, getComponent().isCompilationEnabled());
            settings.putProperty(MAPPINGS, getComponent().getMappings());
            settings.putProperty(COMPILER_OPTIONS, getComponent().getCompilerOptions());
        }

        @Override
        public boolean isValid() {
            if (settings == null) {
                // not displayed yet
                return false;
            }
            ValidationResult result = getValidationResult();
            String error = result.getFirstErrorMessage();
            if (error == null) {
                error = result.getFirstWarningMessage();
            }
            if (error != null) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                return false;
            }
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            getComponent().addChangeListener(listener);
            changeSupport.addChangeListener(listener);
            CssPrepOptions.getInstance().addPropertyChangeListener(this);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            getComponent().removeChangeListener(listener);
            changeSupport.removeChangeListener(listener);
            CssPrepOptions.getInstance().removePropertyChangeListener(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void save() throws IOException {
            CssPreprocessorPreferences preferences = type.getPreferences();
            // #230637
            boolean enabled = (boolean) settings.getProperty(ENABLED);
            if (enabled) {
                preferences.setConfigured(project, true);
            }
            preferences.setEnabled(project, enabled);
            preferences.setMappings(project, (List<Pair<String, String>>) settings.getProperty(MAPPINGS));
            preferences.setCompilerOptions(project, (String) settings.getProperty(COMPILER_OPTIONS));
        }

        private ValidationResult getValidationResult() {
            boolean compilationEnabled = getComponent().isCompilationEnabled();
            return type.getPreferencesValidator()
                    .validateMappings(CssPreprocessorUtils.getWebRoot(project), compilationEnabled, getComponent().getMappings())
                    .validateExecutable(compilationEnabled)
                    .getResult();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (CssPrepOptions.LESS_PATH_PROPERTY.equals(propertyName)
                    || CssPrepOptions.SASS_PATH_PROPERTY.equals(propertyName)) {
                changeSupport.fireChange();
            }
        }

    }

}
