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

import java.util.List;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.wizards.NewPhpProjectWizardIterator.WizardType;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>,
        SourcesFolderProvider, ChangeListener, CancelablePanel {

    static final String PROJECT_NAME = "projectName"; // NOI18N
    static final String PROJECT_DIR = "projectDir"; // NOI18N
    static final String IS_PROJECT_DIR_USED = "isProjectDirUsed"; // NOI18N
    static final String SOURCES_FOLDER = "sourcesFolder"; // NOI18N
    static final String LOCAL_SERVERS = "localServers"; // NOI18N
    static final String PHP_VERSION = "phpVersion"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N
    static final String ROOTS = "roots"; // NOI18N

    private final String[] steps;
    private final NewPhpProjectWizardIterator.WizardType wizardType;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigurableProjectPanel configureProjectPanelVisual = null;
    private WizardDescriptor descriptor = null;
    private String originalProjectName = null;
    private String originalSources = null;
    private volatile boolean canceled;

    public ConfigureProjectPanel(String[] steps, NewPhpProjectWizardIterator.WizardType wizardType) {
        this.steps = steps.clone();
        this.wizardType = wizardType;
    }

    @Override
    public Component getComponent() {
        if (configureProjectPanelVisual == null) {
            switch (wizardType) {
                case NEW:
                case REMOTE:
                    configureProjectPanelVisual = new ConfigureNewProjectPanelVisual(this);
                    break;
                case EXISTING:
                    configureProjectPanelVisual = new ConfigureExistingProjectPanelVisual(this);
                    break;
                default:
                    assert false : "Unknown wizard type: " + wizardType;
                    break;
            }
            addListeners();
        }
        return configureProjectPanelVisual;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName() + "." + wizardType);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        // project
        switch (wizardType) {
            case NEW:
            case REMOTE:
                // sources - we need them first because of free project name
                MutableComboBoxModel<LocalServer> localServers = getLocalServers();
                if (localServers != null) {
                    configureProjectPanelVisual.setLocalServerModel(localServers);
                } else {
                    configureProjectPanelVisual.setLocalServerModel(new LocalServer.ComboBoxModel(LocalServer.PENDING_LOCAL_SERVER));
                    configureProjectPanelVisual.setState(false);
                    canceled = false;
                    PhpEnvironment.get().readDocumentRoots(new PhpEnvironment.ReadDocumentRootsNotifier() {
                        @Override
                        public void finished(final List<DocumentRoot> documentRoots) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    initLocalServers(documentRoots);
                                }
                            });
                        }
                    });
                }
                LocalServer sourcesLocation = getLocalServer();
                if (sourcesLocation != null) {
                    configureProjectPanelVisual.selectSourcesLocation(sourcesLocation);
                }
                break;
            case EXISTING:
                // noop
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }
        configureProjectPanelVisual.setProjectFolder(getProjectFolder().getAbsolutePath());

        // php version
        PhpVersion phpVersion = getPhpVersion();
        if (phpVersion != null) {
            configureProjectPanelVisual.setPhpVersion(phpVersion);
        }

        // encoding
        configureProjectPanelVisual.setEncoding(getEncoding());
    }

    private void addListeners() {
        configureProjectPanelVisual.addConfigureProjectListener(this);
    }

    private void removeListeners() {
        configureProjectPanelVisual.removeConfigureProjectListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        // project - we have to save it as it is because one can navigate back and forward
        //  => the project folder equals to sources
        File projectDir = getProjectFolderFile();
        if (projectDir != null) {
            projectDir = FileUtil.normalizeFile(projectDir);
        }
        settings.putProperty(IS_PROJECT_DIR_USED, configureProjectPanelVisual.isProjectFolderUsed());
        settings.putProperty(PROJECT_DIR, projectDir);
        settings.putProperty(PROJECT_NAME, configureProjectPanelVisual.getProjectName());

        // sources
        settings.putProperty(SOURCES_FOLDER, configureProjectPanelVisual.getSourcesLocation());
        settings.putProperty(LOCAL_SERVERS, configureProjectPanelVisual.getLocalServerModel());

        // php version
        settings.putProperty(PHP_VERSION, configureProjectPanelVisual.getPhpVersion());

        // encoding
        settings.putProperty(ENCODING, configureProjectPanelVisual.getEncoding());
    }

    /**
     * @return <b>non-normalized</b> {@link File file} for project folder or <code>null</code> if no text is present.
     */
    public File getProjectFolderFile() {
        String projectFolder = configureProjectPanelVisual.getProjectFolder();
        if (!StringUtils.hasText(projectFolder)) {
            return null;
        }
        return new File(projectFolder);
    }

    @Override
    public boolean isFinishPanel() {
        if (wizardType == NewPhpProjectWizardIterator.WizardType.REMOTE) {
            return false;
        }
        return NewPhpProjectWizardIterator.areAllStepsValid(descriptor);
    }

    @Override
    public boolean isValid() {
        getComponent();
        if (!configureProjectPanelVisual.getState()) {
            return false;
        }
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        String error = null;
        // different order of validation for each wizard type
        switch (wizardType) {
            case NEW:
            case REMOTE:
                // first check whether document roots are read already
                if (descriptor.getProperty(ROOTS) == null) {
                    return false;
                }
                error = validateProject();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                    return false;
                }
                error = validateSources(false);
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                    return false;
                }
                error = validateProjectDirectory();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                    return false;
                }
                break;
            case EXISTING:
                String sourcesFolder = configureProjectPanelVisual.getSourcesFolder();
                if (sourcesFolder == null || sourcesFolder.trim().length() == 0) {
                    descriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                            NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_EmptySources"));
                    return false;
                }
                error = validateSources(true);
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                    return false;
                }
                error = validateProject();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error); // NOI18N
                    return false;
                }
                error = validateProjectDirectory();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                    return false;
                }
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }

        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public File getSourcesFolder() {
        getComponent();
        return FileUtil.normalizeFile(new File(configureProjectPanelVisual.getSourcesLocation().getSrcRoot()));
    }

    @Override
    public String getSourcesFolderName() {
        getComponent();
        return configureProjectPanelVisual.getProjectName();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    String[] getSteps() {
        return steps;
    }

    public WizardType getWizardType() {
        return wizardType;
    }

    String getProjectName() {
        String projectName = (String) descriptor.getProperty(PROJECT_NAME);
        if (projectName == null) {
            // this can happen only for the first time and ideally only for NEW project wizard (see NewPhpProjectWizardIterator#getPreferredDocumentRoot)
            projectName = getDefaultFreeName(ProjectChooser.getProjectsFolder());
            descriptor.putProperty(PROJECT_NAME, projectName);
        }
        return projectName;
    }

    private File getProjectFolder(String projectName) {
        return new File(getProjectFolder().getParentFile(), projectName);
    }

    private File getProjectFolder() {
        File projectFolder = (File) descriptor.getProperty(PROJECT_DIR);
        if (projectFolder == null) {
            projectFolder = new File(ProjectChooser.getProjectsFolder(), getProjectName());
            descriptor.putProperty(PROJECT_DIR, projectFolder);
        }
        return projectFolder;
    }

    private String getDefaultFreeName(File projectFolder) {
        int i = 1;
        String projectName;
        do {
            projectName = validFreeProjectName(projectFolder, i++);
        } while (projectName == null);
        return projectName;
    }

    private PhpVersion getPhpVersion() {
        return (PhpVersion) descriptor.getProperty(PHP_VERSION);
    }

    private Charset getEncoding() {
        Charset enc = (Charset) descriptor.getProperty(ENCODING);
        if (enc == null) {
            // #136917
            enc = FileEncodingQuery.getDefaultEncoding();
        }
        return enc;
    }

    private LocalServer getLocalServer() {
        return (LocalServer) descriptor.getProperty(SOURCES_FOLDER);
    }

    @SuppressWarnings("unchecked")
    private MutableComboBoxModel<LocalServer> getLocalServers() {
        return (MutableComboBoxModel<LocalServer>) descriptor.getProperty(LOCAL_SERVERS);
    }

    private void initLocalServers(List<DocumentRoot> documentRoots) {
        if (canceled) {
            return;
        }
        // first, get preferred document root because we need to find free folder name for project
        File preferredRoot = ProjectChooser.getProjectsFolder();
        for (DocumentRoot root : documentRoots) {
            if (root.isPreferred()) {
                preferredRoot = new File(root.getDocumentRoot());
                break;
            }
        }
        // store document roots
        descriptor.putProperty(ROOTS, documentRoots);

        // find free folder name
        String projectName = getDefaultFreeName(preferredRoot);
        descriptor.putProperty(PROJECT_NAME, projectName);

        // prepare copy-to-folder targets
        int size = documentRoots.size();
        List<LocalServer> localServers = new ArrayList<>(size);
        for (DocumentRoot root : documentRoots) {
            String srcRoot = new File(root.getDocumentRoot(), projectName).getAbsolutePath();
            LocalServer ls = new LocalServer(null, root.getUrl(), root.getDocumentRoot(), srcRoot, true);
            localServers.add(ls);
        }
        descriptor.putProperty(RunConfigurationPanel.COPY_SRC_TARGETS, new LocalServer.ComboBoxModel(localServers.toArray(new LocalServer[size])));

        // create & set a new model for document roots
        File projectFolder = FileUtil.normalizeFile(getProjectFolder(projectName));
        LocalServer selected = new LocalServer(projectFolder.getAbsolutePath());
        MutableComboBoxModel<LocalServer> model = new LocalServer.ComboBoxModel(selected);
        for (DocumentRoot root : documentRoots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot() + File.separator + projectName);
            ls.setHint(root.getHint());
            model.addElement(ls);
            if (root.isPreferred()) {
                selected = ls;
            }
        }
        model.setSelectedItem(selected);
        // store settings
        descriptor.putProperty(SOURCES_FOLDER, selected);
        descriptor.putProperty(LOCAL_SERVERS, model);
        descriptor.putProperty(PROJECT_DIR, projectFolder);
        // update UI
        configureProjectPanelVisual.setLocalServerModel(model);
        configureProjectPanelVisual.setProjectName(projectName);
        configureProjectPanelVisual.setProjectFolder(projectFolder.getAbsolutePath());

        configureProjectPanelVisual.setState(true);
        fireChangeEvent();
    }

    private String validFreeProjectName(File parentFolder, int index) {
        String name = MessageFormat.format(NbBundle.getMessage(ConfigureProjectPanel.class, "TXT_DefaultProjectName"),
                new Object[] {index});
        File file = new File(parentFolder, name);
        if (file.exists()) {
            return null;
        }
        return name;
    }

    private String validateProject() {
        String projectName = configureProjectPanelVisual.getProjectName();
        if (projectName.trim().length() == 0) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectName");
        }
        if (!configureProjectPanelVisual.isProjectFolderUsed()) {
            return null;
        }
        File projectFolder = getProjectFolderFile();
        if (projectFolder == null
                || !Utils.isValidFileName(projectFolder)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectFolder");
        }
        String err = Utils.validateProjectDirectory(projectFolder, "Project", true, false);
        if (err != null) {
            return err;
        }
        if (PhpProjectUtils.isProject(projectFolder)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectAlreadyProject");
        }
        warnIfNotEmpty(projectFolder.getAbsolutePath(), "Project"); // NOI18N
        return null;
    }

    @NbBundle.Messages("ConfigureProjectPanel.error.sources.homeDir=Sources cannot be your home directory.")
    private String validateSources(boolean children) {
        String err = null;
        LocalServer localServer = configureProjectPanelVisual.getSourcesLocation();
        String sourcesLocation = localServer.getSrcRoot();

        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (sourcesLocation.trim().length() == 0
                || !Utils.isValidFileName(sources)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalSourcesName");
        }

        if (isHomeDir(sources)) {
            return Bundle.ConfigureProjectPanel_error_sources_homeDir();
        }

        err = Utils.validateProjectDirectory(sourcesLocation, "Sources", true, true); // NOI18N
        if (err != null) {
            return err;
        }

        if (children) {
            if (!sources.isDirectory()) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalSourcesName");
            }
            // #196811
            if (sources.listFiles() == null) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesCannotBeRead");
            }
        }

        if (!configureProjectPanelVisual.isProjectFolderUsed()) {
            // project folder not used => validate sources as project folder
            if (PhpProjectUtils.isProject(sources)) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesAlreadyProject");
            }
        }

        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }

        switch (wizardType) {
            case NEW:
            case REMOTE:
                warnIfNotEmpty(sourcesLocation, "Sources"); // NOI18N
                break;
            case EXISTING:
                warnIfEmptySources(sourcesLocation);
                break;
        }

        return null;
    }

    private boolean isHomeDir(File folder) {
        return folder.equals(new File(System.getProperty("user.home"))); // NOI18N
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        if (!NewPhpProjectWizardIterator.areAllStepsValid(descriptor)) {
            // some error there, need to be fixed, so do not compare
            return null;
        }
        Boolean copyFiles = (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_FILES);
        if (copyFiles == null || !copyFiles) {
            return null;
        }
        LocalServer sources = configureProjectPanelVisual.getSourcesLocation();
        String sourcesSrcRoot = sources.getSrcRoot();
        LocalServer copyTarget = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        File normalized = FileUtil.normalizeFile(new File(copyTarget.getSrcRoot()));
        String cpTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, cpTarget);
    }

    // #154874
    private String validateProjectDirectory() {
        File[] fsRoots = File.listRoots();
        if (fsRoots == null || fsRoots.length == 0) {
            // definitely should not happen
            return null;
        }
        File projectDirectory = configureProjectPanelVisual.isProjectFolderUsed() ? getProjectFolderFile() : getSourcesFolder();
        assert projectDirectory != null;
        if (Arrays.asList(fsRoots).contains(projectDirectory)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderIsRoot");
        }
        // #196811
        if (projectDirectory.isDirectory() && projectDirectory.listFiles() == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderCannotBeRead");
        }
        return null;
    }

    // type - Project | Sources
    private void warnIfNotEmpty(String location, String type) {
        // warn if the folder is not empty
        File destFolder = new File(location);
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // folder exists and is not empty - but just warning
            String warning = NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "NotEmpty");
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, warning); // NOI18N
        }
    }

    private void warnIfEmptySources(String location) {
        File destFolder = new File(location);
        assert destFolder.isDirectory() : "Sources directory must exist: " + location;
        File[] kids = destFolder.listFiles();
        assert kids != null : "Sources directory should have children: " + location;
        if (kids.length == 0) {
            // folder is empty - but just warning
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesEmpty"));
        }
    }

    // we will do this only if the name equals to the project directory and not vice versa
    private void projectNameChanged() {
        String projectName = configureProjectPanelVisual.getProjectName();
        if (projectName.length() == 0) {
            // invalid situation, do not change anything
            return;
        }
        if (originalProjectName == null) {
            originalProjectName = projectName;
        }
        if (projectName.equals(originalProjectName)) {
            // no change in project name
            return;
        }

        adjustProjectFolder(originalProjectName, projectName);
        adjustSources(originalProjectName, projectName);

        originalProjectName = projectName;
    }

    private void adjustProjectFolder(String originalProjectName, String projectName) {
        File projectFolderFile = getProjectFolderFile();
        if (projectFolderFile == null) {
            // invalid folder given, just ignore it
            return;
        }
        String projectFolder = projectFolderFile.getName();
        if (!originalProjectName.equals(projectFolder)) {
            // already "disconnected"
            return;
        }

        File newProjecFolder = new File(projectFolderFile.getParentFile(), projectName);
        configureProjectPanelVisual.setProjectFolder(newProjecFolder.getAbsolutePath());
    }

    private void adjustSources(String originalProjectName, String projectName) {
        LocalServer.ComboBoxModel model = (LocalServer.ComboBoxModel) configureProjectPanelVisual.getLocalServerModel();
        boolean fire = false;
        for (int i = 0; i < model.getSize(); ++i) {
            LocalServer ls = model.getElementAt(i);
            File src = new File(ls.getSrcRoot());
            if (originalProjectName.equals(src.getName())) {
                File newSrc = new File(src.getParentFile(), projectName);
                ls.setSrcRoot(newSrc.getAbsolutePath());
                fire = true;
            }
        }
        if (fire) {
            model.fireContentsChanged();
        }
    }

    private void sourceFolderChanged() {
        String sources = configureProjectPanelVisual.getSourcesLocation().getSrcRoot();
        if (sources.length() == 0) {
            // invalid situation, do not change anything
            return;
        }
        if (sources.equals(originalSources)) {
            // no change in sources
            return;
        }
        adjustProjectName(originalSources, sources);
        String projectName = new File(sources).getName();
        String originalName = null;
        if (originalSources == null) {
            // only for the first time => project folder *must* be valid
            assert getProjectFolderFile() != null;
            originalName = getProjectFolderFile().getName();
        } else {
            originalName = new File(originalSources).getName();
        }
        adjustProjectFolder(originalName, projectName);
        originalSources = sources;
    }

    private void adjustProjectName(String originalSources, String sources) {
        if (originalSources != null) {
            String sourcesFolder = new File(originalSources).getName();
            String projectName = configureProjectPanelVisual.getProjectName();
            if (!sourcesFolder.equals(projectName)) {
                // already "disconnected"
                return;
            }
        }
        String newProjectName = new File(sources).getName();
        configureProjectPanelVisual.setProjectName(newProjectName);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // because JTextField.setText() calls document.remove() and then document.insert() (= 2 events!), just remove and readd the listener
        removeListeners();
        switch (wizardType) {
            case NEW:
            case REMOTE:
                projectNameChanged();
                break;
            case EXISTING:
                sourceFolderChanged();
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }
        addListeners();
        fireChangeEvent();
    }

    @Override
    public void cancel() {
        canceled = true;
    }
}
