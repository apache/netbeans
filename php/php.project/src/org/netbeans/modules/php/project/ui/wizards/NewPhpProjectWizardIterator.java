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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.BasePathSupport.Item;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.sync.TimeStamps;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.actions.DownloadCommand;
import org.netbeans.modules.php.project.ui.actions.RemoteCommand;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectGenerator;
import org.netbeans.modules.php.project.util.PhpProjectGenerator.ProjectProperties;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender.ExtendingException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 * Minor note to frameworks - the 1st framework "wins", it means that e.g. web root sets
 * the 1st framework from the list, other frameworks are ignored.
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    public static enum WizardType {
        NEW,
        EXISTING,
        REMOTE,
    }

    private static final Logger LOGGER = Logger.getLogger(NewPhpProjectWizardIterator.class.getName());

    private final WizardType wizardType;
    private final List<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender> phpModuleExtenders = new CopyOnWriteArrayList<>();
    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;

    public NewPhpProjectWizardIterator() {
        this(WizardType.NEW, createPhpModuleExtenders());
    }

    private NewPhpProjectWizardIterator(WizardType wizardType, Collection<? extends org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender> phpModuleExtenders) {
        assert wizardType != null;
        assert phpModuleExtenders != null;
        this.wizardType = wizardType;
        this.phpModuleExtenders.addAll(phpModuleExtenders);
    }

    public static NewPhpProjectWizardIterator existing() {
        return new NewPhpProjectWizardIterator(WizardType.EXISTING, Collections.<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender>emptyList());
    }

    public static NewPhpProjectWizardIterator remote() {
        return new NewPhpProjectWizardIterator(WizardType.REMOTE, Collections.<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender>emptyList());
    }

    // workaround for wizards api
    static boolean areAllStepsValid(WizardDescriptor descriptor) {
        // first step does not need to be checked, just the 2nd and more
        Boolean isValid = (Boolean) descriptor.getProperty(RunConfigurationPanel.VALID);
        if (isValid != null && !isValid) {
            return false;
        }
        isValid = (Boolean) descriptor.getProperty(PhpFrameworksPanel.VALID);
        if (isValid != null && !isValid) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Map<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender, Boolean> validity = (Map<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender, Boolean>) descriptor.getProperty(PhpExtenderPanel.VALID);
        if (validity != null) {
            for (Boolean extenderValid : validity.values()) {
                if (!extenderValid) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender> createPhpModuleExtenders() {
        Collection<? extends org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.Factory> factories = Lookups
                .forPath(org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.Factory.EXTENDERS_PATH)
                .lookupAll(org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.Factory.class);
        List<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender> phpModuleExtenders = new ArrayList<>(factories.size());
        for (org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.Factory factory : factories) {
            phpModuleExtenders.add(factory.create());
        }
        return phpModuleExtenders;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // normally we would do it in uninitialize but we have listener on ide options (=> NPE)
        initDescriptor(wizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        Panel<WizardDescriptor> current = current();
        // #158483
        if (current instanceof CancelablePanel) {
            ((CancelablePanel) current).cancel();
        }
        panels = null;
        descriptor = null;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @NbBundle.Messages("NewPhpProjectWizardIterator.project.alreadyExists=Project was not created because it already exists (maybe only in memory).")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        final Set<FileObject> resultSet = new HashSet<>();

        final Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders = getFrameworkExtenders();

        // #207493
        final PhpVersion phpVersion = (PhpVersion) descriptor.getProperty(ConfigureProjectPanel.PHP_VERSION);
        if (wizardType == WizardType.NEW) {
            PhpOptions.getInstance().setDefaultPhpVersion(phpVersion);
        }

        final PhpProjectGenerator.ProjectProperties createProperties = new PhpProjectGenerator.ProjectProperties()
                .setProjectDirectory(getProjectDirectory())
                .setSourcesDirectory(getSources(descriptor))
                .setName((String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME))
                .setRunAsType(wizardType == WizardType.REMOTE ? RunAsType.REMOTE : getRunAsType())
                .setPhpVersion(phpVersion)
                .setCharset((Charset) descriptor.getProperty(ConfigureProjectPanel.ENCODING))
                .setUrl(getUrl())
                .setIndexFile(wizardType == WizardType.REMOTE ? null : getIndexFile(frameworkExtenders))
                .setDescriptor(descriptor)
                .setCopySources(isCopyFiles())
                .setCopySourcesTarget(getCopySrcTarget())
                .setCopySourcesOnOpen(getCopySrcOnOpen())
                .setRemoteConfiguration((RemoteConfiguration) descriptor.getProperty(RunConfigurationPanel.REMOTE_CONNECTION))
                .setRemoteDirectory((String) descriptor.getProperty(RunConfigurationPanel.REMOTE_DIRECTORY))
                .setUploadFiles(wizardType == WizardType.REMOTE ? UploadFiles.ON_SAVE : (UploadFiles) descriptor.getProperty(RunConfigurationPanel.REMOTE_UPLOAD))
                .setHostname((String) descriptor.getProperty(RunConfigurationPanel.HOSTNAME))
                .setPort(getPort())
                .setRouter((String) descriptor.getProperty(RunConfigurationPanel.ROUTER))
                .setFrameworkExtenders(frameworkExtenders);

        PhpProjectGenerator.Monitor monitor = null;
        switch (wizardType) {
            case NEW:
            case EXISTING:
                monitor = new LocalProgressMonitor(handle, frameworkExtenders, phpModuleExtenders);
                break;
            case REMOTE:
                monitor = new RemoteProgressMonitor(handle);
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
        }

        AntProjectHelper projectHelper;
        try {
            projectHelper = PhpProjectGenerator.createProject(createProperties, monitor);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            warnUser(Bundle.NewPhpProjectWizardIterator_project_alreadyExists());
            File projectDirectory = createProperties.getProjectDirectory();
            if (projectDirectory == null) {
                projectDirectory = createProperties.getSourcesDirectory();
            }
            FileObject projDir = FileUtil.toFileObject(projectDirectory);
            if (projDir != null && projDir.isValid()) {
                resultSet.add(projDir);
            }
            return resultSet;
        }
        final AntProjectHelper helper = projectHelper;
        resultSet.add(helper.getProjectDirectory());

        final Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        assert project instanceof PhpProject;
        final PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
        assert phpModule != null : "PHP module must exist!";
        FileObject sources = FileUtil.toFileObject(createProperties.getSourcesDirectory());
        resultSet.add(sources);

        // post process
        switch (wizardType) {
            case NEW:
                if (!frameworkExtenders.isEmpty()
                        || !phpModuleExtenders.isEmpty()) {
                    assert monitor instanceof LocalProgressMonitor;
                    LocalProgressMonitor localMonitor = (LocalProgressMonitor) monitor;
                    localMonitor.startingExtending();
                    extendPhpModule(phpModule, frameworkExtenders, monitor, resultSet);
                    extendPhpModule(phpModule, monitor, resultSet);
                    localMonitor.finishingExtending();
                }
                break;
            case REMOTE:
                downloadRemoteFiles(createProperties, monitor);
                setSyncTimestamp((PhpProject) project, sources);
                break;
            case EXISTING:
                // noop
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
        }

        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws MutexException {
                    try {
                        // update project properties
                        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                        List<PhpModuleProperties> phpModuleProperties = getPhpModuleProperties(phpModule, frameworkExtenders);

                        FileObject indexFile = setIndexFile(createProperties, projectProperties, privateProperties, phpModuleProperties);
                        if (indexFile != null && indexFile.isValid()) {
                            resultSet.add(indexFile);
                        }
                        setWebRoot(createProperties, projectProperties, privateProperties, phpModuleProperties);
                        setTests(createProperties, projectProperties, privateProperties, phpModuleProperties);
                        setIncludePath((PhpProject) project, createProperties, projectProperties, privateProperties, phpModuleProperties);

                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
                        ProjectManager.getDefault().saveProject(project);

                    } catch (IOException ioe) {
                        throw new MutexException(ioe);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            Exception ie = e.getException();
            if (ie instanceof IOException) {
                throw (IOException) ie;
            }
            Exceptions.printStackTrace(e);
        }
        return resultSet;
    }

    @Override
    public String name() {
        return NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_IteratorName", index + 1, panels.length);
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        setTitle();
        return panels[index];
    }

    private void setTitle() {
        // #158483
        if (descriptor != null) {
            // wizard title
            String msgKey = null;
            switch (wizardType) {
                case NEW:
                    msgKey = "TXT_PhpProject"; // NOI18N
                    break;
                case EXISTING:
                    msgKey = "TXT_ExistingPhpProject"; // NOI18N
                    break;
                case REMOTE:
                    msgKey = "TXT_RemotePhpProject"; // NOI18N
                    break;
                default:
                    throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
            }

            descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(NewPhpProjectWizardIterator.class, msgKey)); // NOI18N
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        // step names
        String step2 = null;
        String step3 = null;
        List<String> extenderSteps = new ArrayList<>();
        switch (wizardType) {
            case NEW:
                step2 = "LBL_RunConfiguration"; // NOI18N
                step3 = "LBL_Frameworks"; // NOI18N
                for (org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender extender : phpModuleExtenders) {
                    extenderSteps.add(extender.getDisplayName());
                }
                break;
            case EXISTING:
                step2 = "LBL_RunConfiguration"; // NOI18N
                break;
            case REMOTE:
                step2 = "LBL_RemoteConfiguration"; // NOI18N
                step3 = "LBL_RemoteConfirmation"; // NOI18N
                break;
            default:
                throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
        }
        List<String> steps = new ArrayList<>();
        steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_ProjectNameLocation"));
        steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, step2));
        if (step3 != null) {
            steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, step3));
        }
        steps.addAll(extenderSteps);
        String[] stepsArray = steps.toArray(new String[0]);
        // panels
        WizardDescriptor.Panel<WizardDescriptor> panel3 = null;
        List<WizardDescriptor.Panel<WizardDescriptor>> extenderPanels = new ArrayList<>();
        switch (wizardType) {
            case NEW:
                panel3 = new PhpFrameworksPanel(stepsArray);
                int i = 3;
                for (org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender extender : phpModuleExtenders) {
                    extenderPanels.add(new PhpExtenderPanel(extender, stepsArray, i++));
                }
                break;
            case EXISTING:
                break;
            case REMOTE:
                panel3 = new RemoteConfirmationPanel(stepsArray);
                break;
            default:
                throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
        }
        ConfigureProjectPanel configureProjectPanel = new ConfigureProjectPanel(stepsArray, wizardType);

        List<WizardDescriptor.Panel<WizardDescriptor>> pnls = new ArrayList<>(steps.size());
        pnls.add(configureProjectPanel);
        pnls.add(new RunConfigurationPanel(stepsArray, configureProjectPanel, wizardType));
        if (panel3 != null) {
            pnls.add(panel3);
        }
        pnls.addAll(extenderPanels);
        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor>[] pnlsArray = (WizardDescriptor.Panel<WizardDescriptor>[]) Array.newInstance(WizardDescriptor.Panel.class, pnls.size());
        return pnls.toArray(pnlsArray);
    }

    // prevent incorrect default values (empty project => back => existing project)
    private void initDescriptor(WizardDescriptor settings) {
        settings.putProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_DIR, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_NAME, null);
        settings.putProperty(ConfigureProjectPanel.SOURCES_FOLDER, null);
        settings.putProperty(ConfigureProjectPanel.LOCAL_SERVERS, null);
        settings.putProperty(ConfigureProjectPanel.PHP_VERSION, null);
        settings.putProperty(ConfigureProjectPanel.ENCODING, null);
        settings.putProperty(RunConfigurationPanel.VALID, null);
        settings.putProperty(RunConfigurationPanel.RUN_AS, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_FILES, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGET, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGETS, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_ON_OPEN, null);
        settings.putProperty(RunConfigurationPanel.URL, null);
        settings.putProperty(RunConfigurationPanel.INDEX_FILE, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_CONNECTION, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_DIRECTORY, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_UPLOAD, null);
        settings.putProperty(RunConfigurationPanel.HOSTNAME, null);
        settings.putProperty(RunConfigurationPanel.PORT, null);
        settings.putProperty(RunConfigurationPanel.ROUTER, null);
        settings.putProperty(PhpFrameworksPanel.VALID, null);
        settings.putProperty(PhpFrameworksPanel.EXTENDERS, null);
        settings.putProperty(PhpExtenderPanel.VALID, null);
        settings.putProperty(RemoteConfirmationPanel.REMOTE_FILES, null);
        settings.putProperty(RemoteConfirmationPanel.REMOTE_CLIENT, null);
    }

    private File getProjectDirectory() {
        Boolean isProjectDirUsed = (Boolean) descriptor.getProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED);
        if (isProjectDirUsed != null && isProjectDirUsed) {
            return (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
        }
        return null;
    }

    static File getSources(WizardDescriptor descriptor) {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        if (localServer != null) {
            return new File(localServer.getSrcRoot());
        }
        return null;
    }

    private RunAsType getRunAsType() {
        return (RunAsType) descriptor.getProperty(RunConfigurationPanel.RUN_AS);
    }

    private String getUrl() {
        String url = (String) descriptor.getProperty(RunConfigurationPanel.URL);
        if (url == null) {
            // #146882
            url = RunConfigurationPanel.getUrlForSources(wizardType, descriptor);
        }
        return url;
    }

    private String getIndexFile(Map<PhpFrameworkProvider, PhpModuleExtender> frameworkProviders) {
        if (frameworkProviders != null && !frameworkProviders.isEmpty()) {
            // no index for php framework
            return null;
        }
        String indexName = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
        if (indexName == null) {
            // run configuration panel not shown at all
            indexName = RunConfigurationPanel.DEFAULT_INDEX_FILE;
        }
        return indexName;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Null means that it is not set")
    private Boolean isCopyFiles() {
        PhpProjectProperties.RunAsType runAs = getRunAsType();
        if (runAs == null) {
            return null;
        }
        boolean copyFiles = false;
        switch (runAs) {
            case LOCAL:
                Boolean tmp = (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_FILES);
                if (tmp != null && tmp) {
                    copyFiles = true;
                }
                break;
            default:
                // noop
                break;
        }
        return copyFiles;
    }

    private File getCopySrcTarget() {
        if (getRunAsType() == null) {
            return null;
        }
        LocalServer localServer = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        if (StringUtils.hasText(localServer.getSrcRoot())) {
            return new File(localServer.getSrcRoot());
        }
        return null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Null means that it is not set")
    @CheckForNull
    private Boolean getCopySrcOnOpen() {
        if (getRunAsType() == null) {
            return null;
        }
        return (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_ON_OPEN);
    }

    private Integer getPort() {
        String port = (String) descriptor.getProperty(RunConfigurationPanel.PORT);
        if (port == null) {
            return null;
        }
        return Integer.valueOf(port);
    }

    private void extendPhpModule(PhpModule phpModule, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders,
            PhpProjectGenerator.Monitor monitor, Set<FileObject> filesToOpen) {
        assert wizardType == WizardType.NEW : "Extending not allowed for: " + wizardType;
        assert monitor instanceof LocalProgressMonitor;

        LocalProgressMonitor localMonitor = (LocalProgressMonitor) monitor;
        for (Entry<PhpFrameworkProvider, PhpModuleExtender> entry : frameworkExtenders.entrySet()) {
            PhpFrameworkProvider frameworkProvider = entry.getKey();
            assert frameworkProvider != null;

            localMonitor.extending(frameworkProvider.getName());
            PhpModuleExtender phpModuleExtender = entry.getValue();
            if (phpModuleExtender != null) {
                try {
                    Set<FileObject> newFiles = phpModuleExtender.extend(phpModule);
                    assert newFiles != null;
                    filesToOpen.addAll(newFiles);
                } catch (ExtendingException ex) {
                    warnUser(ex.getFailureMessage());
                }
            }
        }
    }

    private void extendPhpModule(PhpModule phpModule, PhpProjectGenerator.Monitor monitor, Set<FileObject> filesToOpen) {
        assert wizardType == WizardType.NEW : "Extending not allowed for: " + wizardType;
        assert monitor instanceof LocalProgressMonitor;

        LocalProgressMonitor localMonitor = (LocalProgressMonitor) monitor;
        for (org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender extender : phpModuleExtenders) {
            assert extender != null;
            localMonitor.extending(extender.getDisplayName());
            try {
                Set<FileObject> newFiles = extender.extend(phpModule);
                assert newFiles != null;
                filesToOpen.addAll(newFiles);
            } catch (org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.ExtendingException ex) {
                warnUser(ex.getFailureMessage());
            }
        }
    }

    private void warnUser(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    @SuppressWarnings("unchecked")
    private Set<TransferFile> getRemoteFiles() {
        return (Set<TransferFile>) descriptor.getProperty(RemoteConfirmationPanel.REMOTE_FILES);
    }

    private RemoteClient getRemoteClient() {
        return (RemoteClient) descriptor.getProperty(RemoteConfirmationPanel.REMOTE_CLIENT);
    }

    private void downloadRemoteFiles(ProjectProperties projectProperties, PhpProjectGenerator.Monitor monitor) {
        assert wizardType == WizardType.REMOTE : "Download not allowed for: " + wizardType;
        assert monitor instanceof RemoteProgressMonitor : "RemoteProgressMonitor expected but is: " + monitor;

        Set<TransferFile> forDownload = getRemoteFiles();
        assert forDownload != null;
        assert !forDownload.isEmpty();

        RemoteClient remoteClient = getRemoteClient();
        assert remoteClient != null;
        // be sure that it is not cancelled
        remoteClient.reset();

        RemoteProgressMonitor remoteMonitor = (RemoteProgressMonitor) monitor;
        remoteMonitor.startingDownload();

        FileObject sources = FileUtil.toFileObject(projectProperties.getSourcesDirectory());
        InputOutput remoteLog = RemoteCommand.getRemoteLog(projectProperties.getRemoteConfiguration().getDisplayName());
        DownloadCommand.download(remoteClient, remoteLog, projectProperties.getName(), sources, forDownload);

        remoteMonitor.finishingDownload();
    }

    private void setSyncTimestamp(PhpProject project, FileObject sources) {
        File sourceDir = FileUtil.toFile(sources);
        TransferFile transferFile = TransferFile.fromFile(getRemoteClient().createRemoteClientImplementation(sourceDir.getAbsolutePath()),
                null, sourceDir);
        TimeStamps timeStamps = new TimeStamps(project);
        timeStamps.setSyncTimestamp(transferFile);
    }

    private Map<PhpFrameworkProvider, PhpModuleExtender> getFrameworkExtenders() {
        @SuppressWarnings("unchecked")
        Map<PhpFrameworkProvider, PhpModuleExtender> frameworkProviders = (Map<PhpFrameworkProvider, PhpModuleExtender>) descriptor.getProperty(PhpFrameworksPanel.EXTENDERS);
        if (frameworkProviders == null) {
            frameworkProviders = Collections.emptyMap();
        }
        return frameworkProviders;
    }

    private List<PhpModuleProperties> getPhpModuleProperties(PhpModule phpModule, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders) {
        if (frameworkExtenders.isEmpty()) {
            return Collections.emptyList();
        }
        List<PhpModuleProperties> phpModuleProperties = new ArrayList<>(frameworkExtenders.size());
        for (PhpFrameworkProvider frameworkProvider : frameworkExtenders.keySet()) {
            phpModuleProperties.add(frameworkProvider.getPhpModuleProperties(phpModule));
        }
        return phpModuleProperties;
    }

    private FileObject setIndexFile(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        String indexFile = createProperties.getIndexFile();
        switch (wizardType) {
            case NEW:
                if (indexFile == null) {
                    for (PhpModuleProperties properties : phpModuleProperties) {
                        FileObject frameworkIndex = properties.getIndexFile();
                        if (frameworkIndex != null) {
                            indexFile = PropertyUtils.relativizeFile(createProperties.getSourcesDirectory(), FileUtil.toFile(frameworkIndex));
                            assert indexFile != null && !indexFile.startsWith("../") : "Unexpected index file: " + indexFile;
                            break; // 1st wins
                        }
                    }
                }
                break;
            case REMOTE:
                // try to find index file for downloaded files
                indexFile = getIndexFile(null);
                break;
            case EXISTING:
                // noop
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
        }

        if (indexFile == null) {
            return null;
        }

        if (!RunAsType.INTERNAL.equals(getRunAsType())) {
            // XXX do not store index file for internal web otherwise run/debug project will not work
            privateProperties.setProperty(PhpProjectProperties.INDEX_FILE, indexFile);
        }
        return FileUtil.toFileObject(createProperties.getSourcesDirectory()).getFileObject(indexFile);
    }

    private void setWebRoot(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        for (PhpModuleProperties properties : phpModuleProperties) {
            FileObject webRoot = properties.getWebRoot();
            if (webRoot != null) {
                String relPath = PropertyUtils.relativizeFile(createProperties.getSourcesDirectory(), FileUtil.toFile(webRoot));
                assert relPath != null && !relPath.startsWith("../") : "WebRoot must be underneath Sources";
                projectProperties.setProperty(PhpProjectProperties.WEB_ROOT, relPath);
                break; // 1st wins
            }
        }
    }

    private void setTests(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        if (phpModuleProperties.isEmpty()) {
            return;
        }

        File projectDir = createProperties.getProjectDirectory();
        if (projectDir == null) {
            projectDir = createProperties.getSourcesDirectory();
        }
        assert projectDir != null;

        for (PhpModuleProperties properties : phpModuleProperties) {
            FileObject tests = properties.getTests();
            if (tests != null) {
                File testDir = FileUtil.toFile(tests);
                // relativize path
                String testPath = PropertyUtils.relativizeFile(projectDir, testDir);
                if (testPath == null) {
                    // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
                    testPath = testDir.getAbsolutePath();
                }
                projectProperties.setProperty(PhpProjectProperties.TEST_SRC_DIR, testPath);
                break; // 1st wins
            }
        }
    }

    private void setIncludePath(PhpProject project, PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        if (phpModuleProperties.isEmpty()) {
            return;
        }

        for (PhpModuleProperties properties : phpModuleProperties) {
            List<String> customIncludePath = properties.getIncludePath();
            if (customIncludePath != null && !customIncludePath.isEmpty()) {
                Set<String> includePath = new LinkedHashSet<>();
                String current = projectProperties.getProperty(PhpProjectProperties.INCLUDE_PATH);
                if (StringUtils.hasText(current)) {
                    includePath.add(current);
                }
                includePath.addAll(customIncludePath);
                IncludePathSupport includePathSupport = new IncludePathSupport(
                        ProjectPropertiesSupport.getPropertyEvaluator(project), project.getRefHelper(), project.getHelper());
                Iterator<Item> itemsIterator = includePathSupport.itemsIterator(
                        StringUtils.implode(new ArrayList<>(includePath), ":")); // NOI18N
                String[] encoded = includePathSupport.encodeToStrings(itemsIterator);
                projectProperties.setProperty(PhpProjectProperties.INCLUDE_PATH, encoded);
            }
        }
    }

    private static final class LocalProgressMonitor implements PhpProjectGenerator.Monitor {
        private final ProgressHandle handle;
        private final int units;
        private int unit = 0;

        private LocalProgressMonitor(ProgressHandle handle, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders, List<org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender> phpModuleExtenders) {
            assert handle != null;
            assert frameworkExtenders != null;
            assert phpModuleExtenders != null;

            this.handle = handle;
            units = 5 + 2 * frameworkExtenders.size() + 2 * phpModuleExtenders.size();
        }

        @Override
        public void starting() {
            handle.start(units);

            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
            handle.progress(msg, 2);
        }

        @Override
        public void creatingIndexFile() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, 4);
        }

        @Override
        public void finishing() {
        }

        public void startingExtending() {
            unit = 5;
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_StartingExtending");
            handle.progress(msg, unit);
        }

        public void extending(String framework) {
            unit += 2;
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_Extending", framework);
            handle.progress(msg, unit);
        }

        public void finishingExtending() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
            handle.progress(msg, units);
        }
    }

    private static final class RemoteProgressMonitor implements PhpProjectGenerator.Monitor {
        private final ProgressHandle handle;

        public RemoteProgressMonitor(ProgressHandle handle) {
            assert handle != null;
            this.handle = handle;
        }

        @Override
        public void starting() {
            handle.start(10);

            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
            handle.progress(msg, 2);
        }

        @Override
        public void creatingIndexFile() {
            assert false : "Should not get here";
        }

        @Override
        public void finishing() {
        }

        public void startingDownload() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_StartingDownload");
            handle.progress(msg, 5);
        }

        public void finishingDownload() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
            handle.progress(msg, 10);
        }
    }
}
