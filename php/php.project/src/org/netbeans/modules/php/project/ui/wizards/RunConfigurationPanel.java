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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.runconfigs.RunConfigInternal;
import org.netbeans.modules.php.project.runconfigs.RunConfigLocal;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigLocalValidator;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigRemoteValidator;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigScriptValidator;
import org.netbeans.modules.php.project.runconfigs.validation.BaseRunConfigValidator;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigInternalValidator;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServer.ComboBoxModel;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.modules.php.project.ui.wizards.NewPhpProjectWizardIterator.WizardType;

/**
 * @author Tomas Mysik
 */
public class RunConfigurationPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener, CancelablePanel {

    static final String VALID = "RunConfigurationPanel.valid"; // NOI18N // used in the previous step while validating sources - copy-folder
    static final String RUN_AS = PhpProjectProperties.RUN_AS; // this property is used in RunAsPanel... yeah, ugly
    static final String URL = "url"; // NOI18N
    static final String INDEX_FILE = "indexFile"; // NOI18N
    static final String DEFAULT_INDEX_FILE = "index.php"; // NOI18N
    static final String COPY_SRC_FILES = "copySrcFiles"; // NOI18N
    static final String COPY_SRC_TARGET = "copySrcTarget"; // NOI18N
    static final String COPY_SRC_TARGETS = "copySrcTargets"; // NOI18N
    static final String COPY_SRC_ON_OPEN = "copySrcOnOpen"; // NOI18N
    static final String REMOTE_CONNECTION = "remoteConnection"; // NOI18N
    static final String REMOTE_DIRECTORY = "remoteDirectory"; // NOI18N
    static final String REMOTE_UPLOAD = "remoteUpload"; // NOI18N
    static final String HOSTNAME = "hostname"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String ROUTER = "router"; // NOI18N

    static final String[] CFG_PROPS = new String[] {
        RUN_AS,
        URL,
        INDEX_FILE,
        REMOTE_CONNECTION,
        REMOTE_DIRECTORY,
        REMOTE_UPLOAD,
        HOSTNAME,
        PORT,
        ROUTER,
    };

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final SourcesFolderProvider sourcesFolderProvider;
    private final NewPhpProjectWizardIterator.WizardType wizardType;
    private WizardDescriptor descriptor = null;
    private PropertyChangeListener phpInterpreterListener;

    private ConfigManager configManager;

    private RunConfigurationPanelVisual runConfigurationPanelVisual = null;
    private RunAsLocalWeb runAsLocalWeb = null;
    private RunAsRemoteWeb runAsRemoteWeb = null;
    private RunAsScript runAsScript = null;
    private RunAsInternalServer runAsInternalServer = null;
    private String defaultLocalUrl = null;
    private String originalProjectName = null;

    private volatile boolean readingDocumentRoots = false;
    private volatile boolean canceled;

    public RunConfigurationPanel(String[] steps, SourcesFolderProvider sourcesFolderProvider, NewPhpProjectWizardIterator.WizardType wizardType) {
        this.sourcesFolderProvider = sourcesFolderProvider;
        this.steps = steps.clone();
        this.wizardType = wizardType;
    }

    String[] getSteps() {
        return steps;
    }

    @Override
    public Component getComponent() {
        if (runConfigurationPanelVisual == null) {
            configManager = new ConfigManager(new WizardConfigProvider());

            runAsLocalWeb = new RunAsLocalWeb(configManager, sourcesFolderProvider);
            runAsRemoteWeb = new RunAsRemoteWeb(configManager, sourcesFolderProvider);
            runAsScript = new RunAsScript(configManager, sourcesFolderProvider);
            runAsInternalServer = new RunAsInternalServer(configManager, sourcesFolderProvider);
            switch (wizardType) {
                case NEW:
                    runAsLocalWeb.setIndexFile(DEFAULT_INDEX_FILE);
                    runAsRemoteWeb.setIndexFile(DEFAULT_INDEX_FILE);
                    runAsScript.setIndexFile(DEFAULT_INDEX_FILE);
                    runAsLocalWeb.hideIndexFile();
                    runAsRemoteWeb.hideIndexFile();
                    runAsScript.hideIndexFile();
                    runAsInternalServer.hideRouter();
                    break;
                case REMOTE:
                    runAsRemoteWeb.setIndexFile(DEFAULT_INDEX_FILE);
                    runAsRemoteWeb.setUploadFiles(PhpProjectProperties.UploadFiles.ON_SAVE);
                    runAsRemoteWeb.hideRunAs();
                    runAsRemoteWeb.hideIndexFile();
                    runAsRemoteWeb.hideUploadFiles();
                    break;
                case EXISTING:
                    // noop
                    break;
                default:
                    assert false : "Unknown wizard type: " + wizardType;
            }

            RunAsPanel.InsidePanel[] insidePanels = null;
            switch (wizardType) {
                case NEW:
                case EXISTING:
                    insidePanels = new RunAsPanel.InsidePanel[] {
                        runAsLocalWeb,
                        runAsRemoteWeb,
                        runAsScript,
                        runAsInternalServer,
                    };
                    break;
                case REMOTE:
                    insidePanels = new RunAsPanel.InsidePanel[] {
                        runAsRemoteWeb,
                    };
                    break;
                default:
                    assert false : "Unknown wizard type: " + wizardType;
            }

            runConfigurationPanelVisual = new RunConfigurationPanelVisual(this, sourcesFolderProvider, configManager, insidePanels);

            // listen to the changes in php interpreter
            phpInterpreterListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (PhpOptions.PROP_PHP_INTERPRETER.equals(evt.getPropertyName())) {
                        runAsScript.loadPhpInterpreter();
                    }
                }
            };
            PhpOptions phpOptions = PhpOptions.getInstance();
            phpOptions.addPropertyChangeListener(WeakListeners.propertyChange(phpInterpreterListener, phpOptions));

            addListeners();
        }
        return runConfigurationPanelVisual;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(RunConfigurationPanel.class.getName() + "." + wizardType);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        //  must be done every time because user can go back, select another sources and return back
        switch (wizardType) {
            case EXISTING:
                findIndexFile();
                break;
            case REMOTE:
                setUrl();
                break;
            case NEW:
                // noop
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
        }

        MutableComboBoxModel<LocalServer> localServerModel = getLocalServerModel();
        if (localServerModel != null) {
            runAsLocalWeb.setLocalServerModel(localServerModel);
        } else {
            runAsLocalWeb.setLocalServerModel(new LocalServer.ComboBoxModel(LocalServer.PENDING_LOCAL_SERVER));
            readingDocumentRoots = true;
            runAsLocalWeb.setCopyFilesState(false);
            canceled = false;
            PhpEnvironment.get().readDocumentRoots(new PhpEnvironment.ReadDocumentRootsNotifier() {
                @Override
                public void finished(final List<DocumentRoot> documentRoots) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            initLocalServerModel(documentRoots);
                        }
                    });
                }
            });
        }
        runAsLocalWeb.setCopyFiles(getCopyFiles());
        runAsLocalWeb.setCopyFilesOnOpen(getCopyFilesOnOpen());

        runAsRemoteWeb.setUploadDirectory(getUploadDirectory());

        runAsInternalServer.setHostname(getHostname());
        runAsInternalServer.setPort(getPort());
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        // first remove all the properties
        for (String s : CFG_PROPS) {
            settings.putProperty(s, null);
        }
        // and put only the valid ones
        RunAsType runAs = getRunAsType();
        settings.putProperty(RUN_AS, runAs);
        settings.putProperty(COPY_SRC_FILES, runAsLocalWeb.isCopyFiles());
        settings.putProperty(COPY_SRC_TARGET, runAsLocalWeb.getLocalServer());
        settings.putProperty(COPY_SRC_TARGETS, runAsLocalWeb.getLocalServerModel());
        settings.putProperty(COPY_SRC_ON_OPEN, runAsLocalWeb.isCopyFilesOnOpen());

        switch (runAs) {
            case LOCAL:
                storeRunAsLocalWeb(settings);
                break;
            case REMOTE:
                storeRunAsRemoteWeb(settings);
                break;
            case SCRIPT:
                storeRunAsScript(settings);
                break;
            case INTERNAL:
                storeRunAsInternalServer(settings);
                break;
            default:
                assert false : "Unhandled RunAsType type: " + runAs;
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private MutableComboBoxModel<LocalServer> getLocalServerModel() {
        return (MutableComboBoxModel<LocalServer>) descriptor.getProperty(COPY_SRC_TARGETS);
    }

    void initLocalServerModel(List<DocumentRoot> documentRoots) {
        if (canceled) {
            return;
        }
        int size = documentRoots.size();
        List<LocalServer> localServers = new ArrayList<>(size);
        LocalServer selected = null;
        for (DocumentRoot root : documentRoots) {
            String srcRoot = new File(root.getDocumentRoot(), sourcesFolderProvider.getSourcesFolderName()).getAbsolutePath();
            LocalServer ls = new LocalServer(null, root.getUrl(), root.getDocumentRoot(), srcRoot, true);
            localServers.add(ls);
            if (selected == null) {
                selected = ls;
            }
        }

        ComboBoxModel model = new LocalServer.ComboBoxModel(localServers.toArray(new LocalServer[0]));
        // store settings
        if (selected != null) {
            model.setSelectedItem(selected);
            descriptor.putProperty(COPY_SRC_TARGET, selected);
        }
        descriptor.putProperty(COPY_SRC_TARGETS, model);
        // update UI
        runAsLocalWeb.setLocalServerModel(model);
        runAsLocalWeb.setCopyFilesState(true);
        readingDocumentRoots = false;
        fireChangeEvent();
    }

    private boolean getCopyFiles() {
        Boolean copyFiles = (Boolean) descriptor.getProperty(COPY_SRC_FILES);
        if (copyFiles != null) {
            return copyFiles;
        }
        return false;
    }

    private boolean getCopyFilesOnOpen() {
        Boolean copyFilesOnOpen = (Boolean) descriptor.getProperty(COPY_SRC_ON_OPEN);
        if (copyFilesOnOpen != null) {
            return copyFilesOnOpen;
        }
        return false;
    }

    private String getUploadDirectory() {
        String uploadDirectory = (String) descriptor.getProperty(REMOTE_DIRECTORY);
        if (uploadDirectory != null) {
            return uploadDirectory;
        }
        return "/" + getProjectName(); // NOI18N
    }

    private String getHostname() {
        String hostname = (String) descriptor.getProperty(HOSTNAME);
        if (hostname != null) {
            return hostname;
        }
        return RunConfigInternal.DEFAULT_HOSTNAME;
    }

    private String getPort() {
        String port = (String) descriptor.getProperty(PORT);
        if (port != null) {
            return port;
        }
        return String.valueOf(RunConfigInternal.DEFAULT_PORT);
    }

    private void findIndexFile() {
        // index file for existing sources - if index file is empty, try to find existing index.php
        String indexFile = (String) descriptor.getProperty(INDEX_FILE);
        if (indexFile == null || indexFile.length() == 0) {
            FileObject fo = FileUtil.toFileObject(sourcesFolderProvider.getSourcesFolder()).getFileObject(DEFAULT_INDEX_FILE);
            if (fo != null && fo.isValid()) {
                runAsLocalWeb.setIndexFile(DEFAULT_INDEX_FILE);
                runAsRemoteWeb.setIndexFile(DEFAULT_INDEX_FILE);
                runAsScript.setIndexFile(DEFAULT_INDEX_FILE);
            }
        }
    }

    private void storeRunAsLocalWeb(WizardDescriptor settings) {
        RunConfigLocal config = runAsLocalWeb.createRunConfig();
        settings.putProperty(URL, config.getUrl());
        settings.putProperty(INDEX_FILE, config.getIndexRelativePath());
    }

    private void storeRunAsRemoteWeb(WizardDescriptor settings) {
        RunConfigRemote config = runAsRemoteWeb.createRunConfig();
        settings.putProperty(URL, config.getUrl());
        settings.putProperty(INDEX_FILE, config.getIndexRelativePath());
        settings.putProperty(REMOTE_CONNECTION, config.getRemoteConfiguration());
        settings.putProperty(REMOTE_DIRECTORY, config.getSanitizedUploadDirectory());
        settings.putProperty(REMOTE_UPLOAD, config.getUploadFilesType());
    }

    private void storeRunAsScript(WizardDescriptor settings) {
        settings.putProperty(INDEX_FILE, runAsScript.createRunConfig().getIndexRelativePath());
    }

    private void storeRunAsInternalServer(WizardDescriptor settings) {
        RunConfigInternal config = runAsInternalServer.createRunConfig();
        settings.putProperty(URL, config.getUrlHint());
        settings.putProperty(HOSTNAME, config.getHostname());
        settings.putProperty(PORT, config.getPort());
        settings.putProperty(ROUTER, config.getRouterRelativePath());
    }

    @Override
    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        String error = null;
        String indexFile = null;
        switch (getRunAsType()) {
            case LOCAL:
                if (readingDocumentRoots) {
                    descriptor.putProperty(VALID, false);
                    return false;
                }
                error = validateRunAsLocalWeb();
                indexFile = runAsLocalWeb.createRunConfig().getIndexRelativePath();
                break;
            case REMOTE:
                error = validateRunAsRemoteWeb();
                indexFile = runAsRemoteWeb.createRunConfig().getIndexRelativePath();
                break;
            case SCRIPT:
                error = validateRunAsScript();
                indexFile = runAsScript.createRunConfig().getIndexRelativePath();
                break;
            case INTERNAL:
                error = validateRunAsInternalServer();
                indexFile = null;
                break;
            default:
                assert false : "Unhandled RunAsType type: " + getRunAsType();
                break;
        }
        if (error != null) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
            descriptor.putProperty(VALID, false);
            return false;
        }
        // index file is just warning
        String warning = null;
        if (indexFile != null) {
            warning = BaseRunConfigValidator.validateIndexFile(sourcesFolderProvider.getSourcesFolder(), indexFile);
        }
        if (wizardType == WizardType.EXISTING
                && warning != null) {
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warning);
        } else {
            validateAsciiTexts();
        }

        descriptor.putProperty(VALID, true);
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
    public boolean isFinishPanel() {
        switch (wizardType) {
            case REMOTE:
                return false;
            default:
                return NewPhpProjectWizardIterator.areAllStepsValid(descriptor);
        }
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private void addListeners() {
        runAsLocalWeb.addRunAsLocalWebListener(this);
        runAsRemoteWeb.addRunAsRemoteWebListener(this);
        runAsScript.addRunAsScriptListener(this);
        runAsInternalServer.addRunAsInternalServerListener(this);
    }

    private PhpProjectProperties.RunAsType getRunAsType() {
        String runAs = configManager.currentConfiguration().getValue(RUN_AS);
        if (runAs == null) {
            switch (wizardType) {
                case REMOTE:
                    return PhpProjectProperties.RunAsType.REMOTE;

                default:
                    return PhpProjectProperties.RunAsType.LOCAL;
            }
        }
        return PhpProjectProperties.RunAsType.valueOf(runAs);
    }

    private String validateRunAsLocalWeb() {
        String error = RunConfigLocalValidator.validateNewProject(runAsLocalWeb.createRunConfig());
        if (error != null) {
            return error;
        }
        error = validateServerLocation();
        if (error != null) {
            return error;
        }
        return null;
    }

    private String validateRunAsRemoteWeb() {
        return RunConfigRemoteValidator.validateNewProject(runAsRemoteWeb.createRunConfig());
    }

    private String validateRunAsScript() {
        return RunConfigScriptValidator.validateNewProject(runAsScript.createRunConfig());
    }

    private String validateRunAsInternalServer() {
        return RunConfigInternalValidator.validateNewProject(runAsInternalServer.createRunConfig());
    }

    private String validateServerLocation() {
        if (!runAsLocalWeb.isCopyFiles()) {
            return null;
        }

        LocalServer copyTarget = runAsLocalWeb.getLocalServer();
        String sourcesLocation = copyTarget.getSrcRoot();
        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (!Utils.isValidFileName(sources)) {
            return NbBundle.getMessage(RunConfigurationPanel.class, "MSG_IllegalFolderName");
        }

        String err = Utils.validateProjectDirectory(sourcesLocation, "Folder", false, true); // NOI18N
        if (err != null) {
            return err;
        }
        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }
        // warn about visibility of source folder
        String url = runAsLocalWeb.createRunConfig().getUrl();
        String warning = NbBundle.getMessage(RunConfigurationPanel.class, "MSG_TargetFolderVisible", url);
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, warning); // NOI18N
        return null;
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        LocalServer sources = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        assert sources != null;
        String sourcesSrcRoot = sources.getSrcRoot();
        File normalized = FileUtil.normalizeFile(new File(runAsLocalWeb.getLocalServer().getSrcRoot()));
        String copyTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, copyTarget);
    }

    // #127088
    private void validateAsciiTexts() {
        String url = null;
        String indexFile = null;
        switch (getRunAsType()) {
            case LOCAL:
                RunConfigLocal configLocal = runAsLocalWeb.createRunConfig();
                url = configLocal.getUrl();
                indexFile = configLocal.getIndexRelativePath();
                break;
            case REMOTE:
                RunConfigRemote configRemote = runAsRemoteWeb.createRunConfig();
                url = configRemote.getUrl();
                indexFile = configRemote.getIndexRelativePath();
                break;
            case SCRIPT:
                // do not validate anything
                return;
            case INTERNAL:
                RunConfigInternal configInternal = runAsInternalServer.createRunConfig();
                url = configInternal.getUrlHint();
                break;
        }

        String warning = Utils.validateAsciiText(url, NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_ProjectUrlPure"));
        if (warning != null) {
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warning);
            return;
        }
        warning = Utils.validateAsciiText(indexFile, NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_IndexFilePure"));
        if (warning != null) {
            descriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, warning);
            return;
        }
    }

    private void adjustUrl() {
        String currentUrl = runAsLocalWeb.createRunConfig().getUrl();
        if (defaultLocalUrl == null) {
            defaultLocalUrl = currentUrl;
        }
        if (!defaultLocalUrl.equals(currentUrl)) {
            return;
        }
        String url = null;
        if (runAsLocalWeb.isCopyFiles()) {
            LocalServer ls = runAsLocalWeb.getLocalServer();
            String documentRoot = ls.getDocumentRoot();
            assert documentRoot != null;
            String srcRoot = ls.getSrcRoot();
            String urlSuffix = getUrlSuffix(documentRoot, srcRoot);
            if (urlSuffix == null) {
                // user changed path to a different place => use the name of the directory
                urlSuffix = new File(srcRoot).getName();
            }
            String urlPrefix = ls.getUrl() != null ? ls.getUrl() : "http://localhost/"; // NOI18N
            url = urlPrefix + urlSuffix;
        } else {
            url = getUrlForSources(wizardType, descriptor);
        }
        // we have to do it here because we need correct url BEFORE the following comparison [!defaultLocalUrl.equals(url)]
        if (url != null && !url.endsWith("/")) { // NOI18N
            url += "/"; // NOI18N
        }
        if (url != null && !defaultLocalUrl.equals(url)) {
            defaultLocalUrl = url;
            runAsLocalWeb.setUrl(url);
        }
    }

    static String getUrlForSources(NewPhpProjectWizardIterator.WizardType wizardType, WizardDescriptor descriptor) {
        // /var/www or similar => check source folder name and url
        String url = null;
        LocalServer sources = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        assert sources != null;
        String srcRoot = sources.getSrcRoot();
        switch (wizardType) {
            case NEW:
                // we can check doucment roots only for new wizard; for existing sources we don't have any source roots
                @SuppressWarnings("unchecked")
                List<DocumentRoot> srcRoots = (List<DocumentRoot>) descriptor.getProperty(ConfigureProjectPanel.ROOTS);
                assert srcRoots != null;
                for (DocumentRoot root : srcRoots) {
                    String urlSuffix = getUrlSuffix(root.getDocumentRoot(), srcRoot);
                    if (urlSuffix != null) {
                        url = root.getUrl() + urlSuffix;
                        break;
                    }
                }
                break;
        }
        if (url == null) {
            // not found => get the name of the sources
            url = "http://localhost/" + new File(srcRoot).getName(); // NOI18N
        }
        if (!url.endsWith("/")) { // NOI18N
            url += "/"; // NOI18N
        }
        return url;
    }

    private static String getUrlSuffix(String documentRoot, String srcRoot) {
        if (!documentRoot.endsWith(File.separator)) {
            documentRoot += File.separator;
        }
        if (!srcRoot.startsWith(documentRoot)) {
            return null;
        }
        // handle situations like: /var/www///// or c:\\apache\htdocs\aaa\bbb
        srcRoot = srcRoot.replaceAll(Pattern.quote(File.separator) + "+", "/");
        return srcRoot.substring(documentRoot.length());
    }

    private void adjustUploadDirectoryAndCopyFiles() {
        if (originalProjectName == null) {
            originalProjectName = getProjectName();
            return;
        }
        String newProjectName = getProjectName();
        if (newProjectName.equals(originalProjectName)) {
            // no change in project name
            return;
        }

        adjustUploadDirectory(originalProjectName, newProjectName);
        adjustCopyFiles(originalProjectName, newProjectName);

        originalProjectName = newProjectName;
    }

    private String getProjectName() {
        return (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME);
    }

    private void adjustUploadDirectory(String originalProjectName, String newProjectName) {
        String uploadDirectory = runAsRemoteWeb.createRunConfig().getUploadDirectory();
        if (!uploadDirectory.equals(TransferFile.REMOTE_PATH_SEPARATOR + originalProjectName)) {
            // already disconnected
            return;
        }
        runAsRemoteWeb.setUploadDirectory(TransferFile.REMOTE_PATH_SEPARATOR + newProjectName);
    }

    private void adjustCopyFiles(String originalProjectName, String projectName) {
        LocalServer.ComboBoxModel model = (LocalServer.ComboBoxModel) runAsLocalWeb.getLocalServerModel();
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
            // needed because text field is changed => combobox is editable (see LocalServer.ComboBoxEditor#processUpdate)
            runAsLocalWeb.setCopyFiles(runAsLocalWeb.isCopyFiles());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        switch (getRunAsType()) {
            case LOCAL:
                adjustUrl();
                adjustUploadDirectoryAndCopyFiles();
                break;
            case REMOTE:
                adjustUploadDirectoryAndCopyFiles();
                break;
            case INTERNAL:
            case SCRIPT:
                // noop
                break;
            default:
                assert false : "Unknown run type: " + getRunAsType();
        }
        fireChangeEvent();
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    private void setUrl() {
        assert wizardType == NewPhpProjectWizardIterator.WizardType.REMOTE;
        if (descriptor.getProperty(URL) == null) {
            runAsRemoteWeb.setUrl("http://"); // NOI18N
        }
    }

    private static final class WizardConfigProvider implements ConfigManager.ConfigProvider {
        final Map<String, Map<String, String>> configs;

        public WizardConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
            // we will be using the default configuration (=> no bold labels)
            configs.put(null, new HashMap<String, String>());
        }

        @Override
        public String[] getConfigProperties() {
            return CFG_PROPS;
        }

        @Override
        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }
    }
}
