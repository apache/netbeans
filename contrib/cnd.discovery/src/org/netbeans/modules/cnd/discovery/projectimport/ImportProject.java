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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.actions.CMakeAction;
import org.netbeans.modules.cnd.actions.QMakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.BuildTraceSupport;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.remote.api.RfsListenerSupport;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class ImportProject implements PropertyChangeListener {

    private static final String BUILD_COMMAND = MakeArtifact.MAKE_MACRO+" -f Makefile";  // NOI18N
    private static final String CLEAN_COMMAND = MakeArtifact.MAKE_MACRO+" -f Makefile clean";  // NOI18N
    static final boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    public static final Logger logger;
    static {
        logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
    }

    private static final RequestProcessor RP = new RequestProcessor(ImportProject.class.getName(), 2);
    private static final RequestProcessor RPR = new RequestProcessor(ImportProject.class.getName(), 1);
    private final String nativeProjectPath;
    private final FileObject nativeProjectFO;
    private final FSPath projectFolder;
    private String projectName;
    private String makefilePath;
    private String configurePath;
    private String configureRunFolder;
    private String configureArguments;
    private String configureCommand;
    private boolean runConfigure = false;
    private boolean manualCA = false;
    private boolean buildArifactWasAnalyzed = false;
    private final String hostUID;
    private final ExecutionEnvironment executionEnvironment;
    private final ExecutionEnvironment fileSystemExecutionEnvironment;
    private final MakeProjectOptions.PathMode pathMode;
    private CompilerSet toolchain;
    private boolean defaultToolchain;
    private String workingDir;
    private String buildCommand = BUILD_COMMAND;
    private String cleanCommand = CLEAN_COMMAND;
    private String buildResult = "";  // NOI18N
    private FileObject dwarfSource;
    private Project makeProject;
    private boolean runMake;
    private String includeDirectories = ""; // NOI18N
    private String macros = ""; // NOI18N
    private Iterator<? extends SourceFolderInfo> sources;
    private Iterator<? extends SourceFolderInfo> tests;
    private String sourceFoldersFilter = null;
    private FileObject configureFileObject;
    private final Map<Step, State> importResult = new EnumMap<>(Step.class);
    private final CountDownLatch waitSources = new CountDownLatch(1);
    private final AtomicInteger openState = new AtomicInteger(0);
    private Interrupter interrupter;
    private final boolean isFullRemoteProject;
    //
    private volatile boolean isFinished = false;
    //Build artifacts
    private DoubleFile makeLog = null;
    private DoubleFile execLog = null;
    private DoubleFile expectedCmakeLog = null;
    private DoubleFile existingBuildLog;
    private File configureLog = null;
    private boolean resolveSymLinks;
    private boolean useBuildAnalyzer;

    public ImportProject(WizardDescriptor wizard) {
        isFullRemoteProject = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizard) != null;
        hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizard);
        if (hostUID == null) {
            executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        } else {
            executionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        if (isFullRemoteProject) {
            fileSystemExecutionEnvironment = executionEnvironment;
        } else {
            fileSystemExecutionEnvironment = ExecutionEnvironmentFactory.getLocal();
        }
        pathMode = MakeProjectOptions.getPathMode();
        projectFolder = WizardConstants.PROPERTY_PROJECT_FOLDER.get(wizard);
        nativeProjectPath = WizardConstants.PROPERTY_NATIVE_PROJ_DIR.get(wizard);
        assert nativeProjectPath != null;
        if (isFullRemoteProject) {
            FileObject npfo = WizardConstants.PROPERTY_NATIVE_PROJ_FO.get(wizard);
            // #230539 NPE while creation a full remote project
            // IMHO we duplicate information here: nativeProjectFO and pair (nativeProjectPath, executionEnvironment);
            // but I'm not sure I understand all project creation nuances in minute details, so I left this as is, just added a check
            if (npfo == null) {
                npfo = FileSystemProvider.getFileObject(executionEnvironment, nativeProjectPath);
                if (logger.isLoggable(Level.INFO)) {
                    String warning = "Null file object for " + nativeProjectPath + " at " + executionEnvironment + //NOI18N
                            ((npfo== null) ? " NOT " : "") + " found at 2-nd attempt"; //NOI18N
                    logger.log(Level.INFO, warning, new Exception(warning));
                }
            } else {
                FileObject npfo2 = FileSystemProvider.getFileObject(executionEnvironment, nativeProjectPath);
                if (!npfo.equals(npfo2)) {
                    String warning = "Inconsistent file objects when creating a project: " + npfo + " vs " + npfo2; //NOI18N
                    logger.log(Level.INFO, warning, new Exception(warning));
                }
            }
            nativeProjectFO = npfo;
        } else {
            nativeProjectFO = WizardConstants.PROPERTY_NATIVE_PROJ_FO.get(wizard);
        }
        if (Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizard))) { // NOI18N
            simpleSetup(wizard);
        } else {
            customSetup(wizard);
        }
    }

    private void simpleSetup(WizardDescriptor wizard) {
        projectName = CndPathUtilities.getBaseName(projectFolder.getPath());
        workingDir = nativeProjectPath;
        runConfigure = Boolean.TRUE.equals(WizardConstants.PROPERTY_RUN_CONFIGURE.get(wizard));
        if (runConfigure) {
            configurePath = WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.get(wizard);
            configureArguments = WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.get(wizard);
            configureRunFolder = WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.get(wizard);
            configureCommand = WizardConstants.PROPERTY_CONFIGURE_COMMAND.get(wizard);
        }
        runMake = Boolean.TRUE.equals(WizardConstants.PROPERTY_RUN_REBUILD.get(wizard));
        if (runMake) {
            makefilePath = WizardConstants.PROPERTY_USER_MAKEFILE_PATH.get(wizard);
            if (makefilePath == null) {
                makefilePath = nativeProjectPath + "/Makefile"; // NOI18N;
            }
            buildCommand = WizardConstants.PROPERTY_BUILD_COMMAND.get(wizard);
            cleanCommand = WizardConstants.PROPERTY_CLEAN_COMMAND.get(wizard);
        }
        toolchain = WizardConstants.PROPERTY_TOOLCHAIN.get(wizard);
        defaultToolchain = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wizard));

        List<SourceFolderInfo> list = new ArrayList<>();
        list.add(new SourceFolderInfo() {

            @Override
            public FileObject getFileObject() {
                return nativeProjectFO;
            }

            @Override
            public String getFolderName() {
                return nativeProjectFO.getNameExt();
            }

            @Override
            public boolean isAddSubfoldersSelected() {
                return true;
            }
        });
        sources = list.iterator();
        sourceFoldersFilter = MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT;
        resolveSymLinks = MakeProjectOptions.getResolveSymbolicLinks();
        useBuildAnalyzer = WizardConstants.PROPERTY_USE_BUILD_ANALYZER.get(wizard);
    }

    private void customSetup(WizardDescriptor wizard) {
        projectName = WizardConstants.PROPERTY_NAME.get(wizard);
        workingDir = WizardConstants.PROPERTY_WORKING_DIR.get(wizard);
        buildCommand = WizardConstants.PROPERTY_BUILD_COMMAND.get(wizard);
        cleanCommand = WizardConstants.PROPERTY_CLEAN_COMMAND.get(wizard);
        buildResult = WizardConstants.PROPERTY_BUILD_RESULT.get(wizard);
        includeDirectories = WizardConstants.PROPERTY_INCLUDES.get(wizard);
        macros = WizardConstants.PROPERTY_MACROS.get(wizard);
        makefilePath = WizardConstants.PROPERTY_USER_MAKEFILE_PATH.get(wizard);
        configurePath = WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.get(wizard);
        configureRunFolder = WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.get(wizard);
        configureArguments = WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.get(wizard);
        configureCommand = WizardConstants.PROPERTY_CONFIGURE_COMMAND.get(wizard);
        runConfigure = Boolean.TRUE.equals(WizardConstants.PROPERTY_RUN_CONFIGURE.get(wizard));
        sources = WizardConstants.PROPERTY_SOURCE_FOLDERS.get(wizard);
        tests = WizardConstants.PROPERTY_TEST_FOLDERS.get(wizard);
        sourceFoldersFilter = WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER.get(wizard);
        runMake = Boolean.TRUE.equals(WizardConstants.PROPERTY_RUN_REBUILD.get(wizard));
        String path = WizardConstants.PROPERTY_BUILD_LOG.get(wizard);
        if (path != null && !path.isEmpty()) {
            FileObject fo = RemoteFileUtil.getFileObject(path, fileSystemExecutionEnvironment);
            if (fo != null && fo.isValid()) {
                existingBuildLog = DoubleFile.createFile("make", new FSPath(FileSystemProvider.getFileSystem(fileSystemExecutionEnvironment), path)); // NOI18N
            }
        }
        manualCA = Boolean.TRUE.equals(WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE.get(wizard));
        toolchain = WizardConstants.PROPERTY_TOOLCHAIN.get(wizard);
        defaultToolchain = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wizard));
        Boolean resolve = WizardConstants.PROPERTY_RESOLVE_SYM_LINKS.get(wizard);
        if (resolve != null) {
            resolveSymLinks = resolve;
        } else {
            resolveSymLinks = MakeProjectOptions.getResolveSymbolicLinks();
        }
        useBuildAnalyzer = WizardConstants.PROPERTY_USE_BUILD_ANALYZER.get(wizard);
    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<>();
        MakeConfiguration extConf;
        String aHostUID = hostUID;
        if (isFullRemoteProject) {
            aHostUID = ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal());
            extConf = MakeConfiguration.createMakefileConfiguration(projectFolder, "Default", aHostUID, toolchain, defaultToolchain); // NOI18N
            int platform = CompilerSetManager.get(executionEnvironment).getPlatform();
            extConf.getDevelopmentHost().setBuildPlatform(platform);
        } else {
            extConf = MakeConfiguration.createConfiguration(projectFolder, "Default", MakeConfiguration.TYPE_MAKEFILE, null, aHostUID, toolchain, defaultToolchain); // NOI18N
        }
        if (runConfigure) {
            if (configureRunFolder != null && !configureRunFolder.isEmpty()){
                String workingDirRel = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(configureRunFolder), pathMode);
                workingDirRel = CndPathUtilities.normalizeSlashes(workingDirRel);
                extConf.getPreBuildConfiguration().getPreBuildCommandWorkingDir().setValue(workingDirRel);
                extConf.getPreBuildConfiguration().getPreBuildCommand().setValue(configureCommand);
            }
        }
        String workingDirRel = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(workingDir), pathMode);
        workingDirRel = CndPathUtilities.normalizeSlashes(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            FileObject fo = RemoteFileUtil.getFileObject(buildResult, fileSystemExecutionEnvironment);
            if (fo != null && fo.isValid()) {
                dwarfSource = fo;
            }
            if (fo != null && fo.isValid() && fo.isFolder()) {
                // do not set build result
            } else {
                buildResult = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(buildResult), pathMode);
                buildResult = CndPathUtilities.normalizeSlashes(buildResult);
                extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
            }
        }
        extConf.getProfile().setRunDirectory(workingDirRel);
        extConf.getProfile().setBuildFirst(false);
        // Include directories
        if (includeDirectories != null && includeDirectories.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
            List<String> includeDirectoriesVector = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                String includeDirectory = tokenizer.nextToken();
                includeDirectory = CndPathUtilities.toRelativePath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(includeDirectory));
                includeDirectory = CndPathUtilities.normalizeSlashes(includeDirectory);
                includeDirectoriesVector.add(includeDirectory);
            }
            extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
            extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(new ArrayList<>(includeDirectoriesVector));
        }
        extConf.getCodeAssistanceConfiguration().getResolveSymbolicLinks().setValue(resolveSymLinks);
        extConf.getCodeAssistanceConfiguration().getBuildAnalyzer().setValue(useBuildAnalyzer);
        
        // Macros
        if (macros != null && macros.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(macros, "; "); // NOI18N
            ArrayList<String> list = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
            // FIXUP
            extConf.getCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
            extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<>();
        if (makefilePath != null && makefilePath.length() > 0) {
            makefilePath = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(makefilePath), pathMode);
            makefilePath = CndPathUtilities.normalizeSlashes(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            String normPath = RemoteFileUtil.normalizeAbsolutePath(configurePath, fileSystemExecutionEnvironment);
            configureFileObject = RemoteFileUtil.getFileObject(normPath, fileSystemExecutionEnvironment);
            configurePath = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(configurePath), pathMode);
            configurePath = CndPathUtilities.normalizeSlashes(configurePath);
            importantItems.add(configurePath);
        }
        {
            String launcher = projectFolder.getPath()+"/nbproject/private/launcher.properties"; //NOI18N
            launcher = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(launcher), pathMode);
            launcher = CndPathUtilities.normalizeSlashes(launcher);
            importantItems.add(launcher);
        }
        Iterator<String> importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams
                .setConfiguration(extConf)
                .setSourceFolders(Collections.<SourceFolderInfo>emptyList().iterator())
//                .setSourceFolders(sources)
                .setSourceFoldersFilter(sourceFoldersFilter)
                .setTestFolders(tests)
                .setImportantFiles(importantItemsIterator)
                .setFullRemoteNativeProjectPath(nativeProjectPath)
                .setHostUID(aHostUID);
        if (makefilePath != null) {
            prjParams.setMakefileName(makefilePath);
        } else {
            prjParams.setMakefileName(""); //NOI18N
        }
        makeProject = ProjectGenerator.getDefault().createProject(prjParams);
        FileObject dir = projectFolder.getFileObject();
        importResult.put(Step.Project, State.Successful);
        switchModel(false);
        resultSet.add(dir);
        OpenProjects.getDefault().addPropertyChangeListener(this);
        return resultSet;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (openState.get() == 0) {
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                if (evt.getNewValue() instanceof Project[]) {
                    Project[] projects = (Project[])evt.getNewValue();
                    if (projects.length == 0) {
                        return;
                    }
                    interrupter = new Interrupter() {

                        @Override
                        public boolean cancelled() {
                            return !isProjectOpened();
                        }
                    };
                    openState.incrementAndGet();
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            doWork();
                        }
                    });
                }
            }
        } else if (openState.get() == 1) {
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                if (evt.getNewValue() instanceof Project[]) {
                    Project[] projects = (Project[])evt.getNewValue();
                    for(Project p : projects) {
                        if (p == makeProject) {
                            return;
                        }
                    }
                    openState.incrementAndGet();
                    OpenProjects.getDefault().removePropertyChangeListener(this);
                }
            }
        }
    }

    boolean isProjectOpened() {
        return openState.get() == 1;
    }

    private void doWork() {
        try {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.getConfigurationDescriptor();
            if (pdp.gotDescriptor()) {
                final MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
                if (sources != null) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportProject.class, "ImportProject.Progress.AnalyzeRoot"));
                            handle.start();
                            while(sources.hasNext()) {
                                SourceFolderInfo next = sources.next();
                                configurationDescriptor.addFilesFromRoot(configurationDescriptor.getLogicalFolders(), next.getFileObject(), handle, interrupter, true, Folder.Kind.SOURCE_DISK_FOLDER, null);
                            }
                            handle.finish();
                            waitSources.countDown();
                        }
                    });
                } else {
                    waitSources.countDown();
                }
                if (configurationDescriptor.getActiveConfiguration() != null) {
                    if (runConfigure &&
                        (configurePath != null && configurePath.length() > 0 && configureFileObject != null && configureFileObject.isValid() ||
                        configureCommand != null)) {
                        waitSources.await(); // or should it be waitConfigurationDescriptor() ?
                        configureProject();
                    } else {
                        if (runMake) {
                            makeProject(null);
                        } else {
                            discovery(MakeResult.Skipped, existingBuildLog, null, null);
                        }
                    }
                } else {
                    isFinished = true;
                }
            } else {
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    static File createTempFile(String prefix) {
        try {
            File file = File.createTempFile(prefix, ".log"); // NOI18N
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            return null;
        }
    }

    private void configureProject() {
        ExecutionListener listener = new ExecutionListener() {
            private RfsListenerImpl listener;

            @Override
            public void executionStarted(int pid) {
                if (executionEnvironment.isRemote()) {
                    listener = new RfsListenerImpl(executionEnvironment);
                    RfsListenerSupport.addListener(executionEnvironment, listener);
                }
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.Configure, State.Successful);
                } else {
                    importResult.put(Step.Configure, State.Fail);
                }
                if (listener != null) {
                    listener.download();
                    RfsListenerSupport.removeListener(executionEnvironment, listener);
                }
                if (runMake && rc == 0) {
                    //parseConfigureLog(configureLog);
                    // when run scripts we do full "clean && build" to
                    // remove old build artifacts as well
                    makeProject(configureLog);
                } else {
                    discovery(MakeResult.Skipped, existingBuildLog, null, null);
                }
            }
        };
        if (configurePath != null && configurePath.length() > 0 && configureFileObject != null && configureFileObject.isValid()) {
            try {
                DataObject dObj = DataObject.find(configureFileObject);
                Node node = dObj.getNodeDelegate();
                postConfigure(node, listener);
            } catch (Throwable e) {
                logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
                importResult.put(Step.Configure, State.Fail);
                importResult.put(Step.MakeClean, State.Skiped);
                discovery(MakeResult.Skipped, existingBuildLog, null, null);
            }
        } else {
            try {
                BuildTraceSupport.BuildTrace buldTraceSupport = getBuildTraceSupport();
                ExecuteCommand ec = new ExecuteCommand(makeProject, workingDir, configureCommand);
                String name = NbBundle.getMessage(ImportProject.class, "CONFIGURE_LABEL"); // NOI18N
                String tabName = ec.getExecutionEnvironment().isLocal() ? name :
                                 NbBundle.getMessage(ExecuteCommand.class, "CONFIGURE_REMOTE_LABEL", ec.getExecutionEnvironment().getDisplayName()); // NOI18N
                ec.setName(name, tabName);
                Future<Integer> task = ec.performAction(listener, null, null, buldTraceSupport);
                if (task == null) {
                    logger.log(Level.INFO, "Cannot execute configure command"); // NOI18N
                    isFinished = true;
                }
            } catch (Throwable ex) {
                isFinished = true;
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void postConfigure(Node node, ExecutionListener listener) throws Exception {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (configureLog == null) {
            configureLog = createTempFile("configure"); // NOI18N
        }
        Writer outputListener = null;
        try {
            outputListener =  Files.newBufferedWriter(configureLog.toPath(), Charset.forName("UTF-8")); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        String mime = FileUtil.getMIMEType(configureFileObject);
        // Add arguments to configure script?
        BuildTraceSupport.BuildTrace buldTraceSupport = getBuildTraceSupport();
        if (configureArguments != null) {
            Map<String, String> map = new HashMap<>();
            if (buldTraceSupport != null && buldTraceSupport.getKind() == BuildTraceSupport.BuildTraceKind.Wrapper) {
                buldTraceSupport.modifyEnv(map);
            }
            configureArguments = PreBuildSupport.expandMacros(configureArguments, toolchain, map);
            if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                ShellExecSupport ses = node.getLookup().lookup(ShellExecSupport.class);
                try {
                    // Keep user arguments as is in args[0]
                    ses.setArguments(new String[]{configureArguments});
                    // duplicate configure variables in environment
                    List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                    String wrapper = map.get(BuildTraceSupport.CND_TOOL_WRAPPER);
                    if (wrapper != null) {
                        vars.add(BuildTraceSupport.CND_TOOL_WRAPPER+"="+wrapper); //NOI18N
                    }
                    ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                    if (configureRunFolder != null) {
                        FileObject createdFolder = mkDir(configureFileObject.getParent(), CndPathUtilities.toRelativePath(configureFileObject.getParent(), configureRunFolder));
                        if (createdFolder != null) {
                            ses.setRunDirectory(createdFolder.getPath());
                        }
                    } else {
                        ses.setRunDirectory(configureFileObject.getParent().getPath());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                CMakeExecSupport ses = node.getLookup().lookup(CMakeExecSupport.class);
                try {
                    // extract configure variables in environment
                    List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                    for (String s : ImportUtils.quoteList(vars)) {
                        int i = configureArguments.indexOf(s);
                        if (i >= 0){
                            configureArguments = configureArguments.substring(0, i) + configureArguments.substring(i + s.length());
                        }
                    }
                    ses.setArguments(new String[]{configureArguments});
                    String wrapper = map.get(BuildTraceSupport.CND_TOOL_WRAPPER);
                    if (wrapper != null) {
                        vars.add(BuildTraceSupport.CND_TOOL_WRAPPER+"="+wrapper); //NOI18N
                    }
                    ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                    if (configureRunFolder != null) {
                        FileObject createdFolder = mkDir(configureFileObject.getParent(), CndPathUtilities.toRelativePath(configureFileObject.getParent(), configureRunFolder));
                        if (createdFolder != null) {
                            ses.setRunDirectory(createdFolder.getPath());
                            expectedCmakeLog = DoubleFile.createFile("json", new FSPath(createdFolder.getFileSystem(), createdFolder.getPath()+"/compile_commands.json")); // NOI18N
                        }
                    } else {
                        ses.setRunDirectory(configureFileObject.getParent().getPath());
                        expectedCmakeLog = DoubleFile.createFile("json", new FSPath(configureFileObject.getFileSystem(), configureFileObject.getParent().getPath()+"/compile_commands.json")); // NOI18N
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                QMakeExecSupport ses = node.getLookup().lookup(QMakeExecSupport.class);
                try {
                    ses.setArguments(new String[]{configureArguments});
                    List<String> vars = new ArrayList<>();
                    String wrapper = map.get(BuildTraceSupport.CND_TOOL_WRAPPER);
                    if (wrapper != null) {
                        vars.add(BuildTraceSupport.CND_TOOL_WRAPPER+"="+wrapper); //NOI18N
                    }
                    ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                    if (configureRunFolder != null) {
                        FileObject createdFolder = mkDir(configureFileObject.getParent(), CndPathUtilities.toRelativePath(configureFileObject.getParent(), configureRunFolder));
                        if (createdFolder != null) {
                            ses.setRunDirectory(createdFolder.getPath());
                        }
                    } else {
                        ses.setRunDirectory(configureFileObject.getParent().getPath());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        // If no makefile, create empty one so it shows up in Interesting Files
        //if (!makefileFile.exists()) {
        //    makefileFile.createNewFile();
        //}
        //final File configureLog = createTempFile("configure");
        if (TRACE) {
            logger.log(Level.INFO, "#{0} {1}", new Object[]{configureFileObject, configureArguments}); // NOI18N
        }
        if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
            Future<Integer> task = ShellRunAction.performAction(node, listener, outputListener, makeProject, null);
            if (task == null) {
                throw new Exception("Cannot execute configure script"); // NOI18N
            }
        } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
            Future<Integer> task = CMakeAction.performAction(node, listener, null, makeProject, null);
            if (task == null) {
                throw new Exception("Cannot execute cmake"); // NOI18N
            }
        } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
            Future<Integer> task = QMakeAction.performAction(node, listener, null, makeProject, null);
            if (task == null) {
                throw new Exception("Cannot execute qmake"); // NOI18N
            }
        } else {
            if (TRACE) {
                logger.log(Level.INFO, "#Configure script does not supported"); // NOI18N
            }
            importResult.put(Step.Configure, State.Fail);
            importResult.put(Step.MakeClean, State.Skiped);
            discovery(MakeResult.Skipped, existingBuildLog, null, null);
        }
    }

    private BuildTraceSupport.BuildTrace getBuildTraceSupport() {
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = makeConfigurationDescriptor.getActiveConfiguration();
        if(BuildTraceSupport.useBuildTrace(conf)) {
            return BuildTraceSupport.supportedPlatforms(executionEnvironment, conf, makeProject);
        }
        return null;
    }

    private FileObject mkDir(FileObject parent, String relative) {
        if (relative != null) {
            try {
                relative = relative.replace('\\', '/'); // NOI18N
                for (String segment : relative.split("/")) { // NOI18N
                    if (parent == null) {
                        return null;
                    }
                    if (segment.isEmpty()) {
                        continue;
                    } else if (".".equals(segment)) { // NOI18N
                        continue;
                    } else if ("..".equals(segment)) { // NOI18N
                        parent = parent.getParent();
                    } else {
                        FileObject test = parent.getFileObject(segment, null);
                        if (test != null) {
                            parent = test;
                        } else {
                            parent = parent.createFolder(segment);
                        }

                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            return parent;
        }
        return null;
    }

    private void downloadRemoteFile(File file){
        if (file != null && !file.exists()) {
            if (executionEnvironment.isRemote()) {
                String remoteFile = HostInfoProvider.getMapper(executionEnvironment).getRemotePath(file.getAbsolutePath());
                try {
                    if (HostInfoUtils.fileExists(executionEnvironment, remoteFile)){
                        Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, executionEnvironment, file.getAbsolutePath(), null);
                        if (TRACE) {
                            logger.log(Level.INFO, "#download file {0}->{1}", new Object[]{remoteFile, file.getAbsolutePath()}); // NOI18N
                        }
                        /*int rc =*/ task.get();
                    }
                } catch (Throwable ex) {
                    logger.log(Level.INFO, "Cannot download file {0}->{1}. Exception {2}", new Object[]{remoteFile, file.getAbsolutePath(), ex.getMessage()}); // NOI18N
                }
            }
        }
    }

    private void scanConfigureLog(File logFile){
        String configureCteatePattern = " creating "; // NOI18N
        if (logFile != null && logFile.exists() && logFile.canRead()){
            BufferedReader in = null;
            try {
                in = Files.newBufferedReader(logFile.toPath(), Charset.forName("UTF-8")); // NOI18N
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    int i = line.indexOf(configureCteatePattern);
                    if (i > 0) {
                        String f = line.substring(i+configureCteatePattern.length()).trim();
                        //if (f.endsWith(".h")) { // NOI18N
                            downloadRemoteFile(CndFileUtils.createLocalFile(projectFolder.getPath(), f)); // NOI18N
                        //}
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    private void makeProject(File logFile) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        FileObject makeFileObject = null;
        if (makefilePath != null && makefilePath.length() > 0) {
            makeFileObject = CndFileUtils.toFileObject(FileUtil.normalizePath(CndPathUtilities.toAbsolutePath(projectFolder.getFileObject(), makefilePath)));
        }
        if (makeFileObject != null) {
            downloadRemoteFile(CndFileUtils.createLocalFile(makeFileObject.getPath())); // FileUtil.toFile SIC! - always local
            makeFileObject = CndFileUtils.toFileObject(FileUtil.normalizePath(CndPathUtilities.toAbsolutePath(projectFolder.getFileObject(), makefilePath)));
        }
        scanConfigureLog(logFile);
        postClean();
    }

    private void postClean() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.MakeClean, State.Successful);
                } else {
                    importResult.put(Step.MakeClean, State.Fail);
                }
                postMake();
            }
        };
        if (TRACE) {
            logger.log(Level.INFO, "#{0}", cleanCommand); // NOI18N
        }
        try {
            BuildTraceSupport.BuildTrace buldTraceSupport = getBuildTraceSupport();
            ExecuteCommand ec = new ExecuteCommand(makeProject, workingDir, cleanCommand);
            String name = NbBundle.getMessage(ImportProject.class, "CLEAN_LABEL"); // NOI18N
            String tabName = ec.getExecutionEnvironment().isLocal() ? name :
                             NbBundle.getMessage(ExecuteCommand.class, "CLEAN_REMOTE_LABEL", ec.getExecutionEnvironment().getDisplayName()); // NOI18N
            ec.setName(name, tabName);
            Future<Integer> task = ec.performAction(listener, null, null, buldTraceSupport);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute clean command"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private ExecutionListener createMakeExecutionListener() {
        if (makeLog == null) {
            makeLog = DoubleFile.createTmpFile("make", executionEnvironment); // NOI18N
        }
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = makeConfigurationDescriptor.getActiveConfiguration();
        if(BuildTraceSupport.useBuildTrace(conf)) {
            BuildTraceSupport.BuildTrace support = BuildTraceSupport.supportedPlatforms(executionEnvironment, conf, makeProject);
            if (support != null) {
                execLog = DoubleFile.createTmpFile("exec", executionEnvironment); // NOI18N
            }
        }

        return new ExecutionListener() {
            private RfsListenerImpl listener;

            @Override
            public void executionStarted(int pid) {
                if (!isFullRemoteProject) {
                    if (executionEnvironment.isRemote()) {
                        listener = new RfsListenerImpl(executionEnvironment);
                        RfsListenerSupport.addListener(executionEnvironment, listener);
                    }
                }
            }

            @Override
            public void executionFinished(int rc) {
                if (listener != null) {
                    listener.download();
                    RfsListenerSupport.removeListener(executionEnvironment, listener);
                }
                if (rc == 0) {
                    importResult.put(Step.Make, State.Successful);
                } else {
                    importResult.put(Step.Make, State.Fail);
                }
                if (execLog != null) {
                    if (executionEnvironment.isRemote()) {
                        execLog.download();
                    }
                    if (execLog.existLocalFile()) {
                        FileObject fo = execLog.getLocalFileObject();
                        try {
                              FileUtil.copyFile(fo, projectFolder.getFileObject().getFileObject("nbproject/private"), "Default-exec"); // NOI18N
                          } catch (IOException ex) {
                              ex.printStackTrace(System.err);
                          }
                    }
                }
                if (makeLog != null) {
                    if (isFullRemoteProject) {
                        makeLog.upload();
                    }
                    if (makeLog.existLocalFile()) {
                        FileObject fo = makeLog.getLocalFileObject();
                        try {
                              FileUtil.copyFile(fo, projectFolder.getFileObject().getFileObject("nbproject/private"), "Default-build"); // NOI18N
                          } catch (IOException ex) {
                              ex.printStackTrace(System.err);
                          }
                    }
                }
                if (expectedCmakeLog != null) {
                    if (isFullRemoteProject) {
                        expectedCmakeLog.download();
                    }
                    if (!expectedCmakeLog.existLocalFile()) {
                        expectedCmakeLog = null;
                    }
                }
                if (rc == 0) {
                    discovery(MakeResult.Successful, makeLog, execLog, expectedCmakeLog);
                } else {
                    discovery(MakeResult.Failed, makeLog, execLog, expectedCmakeLog);
                }
            }
        };
    }

    private void postMake() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = createMakeExecutionListener();
        Writer outputListener = null;
        if (makeLog != null) {
            try {
                outputListener =  Files.newBufferedWriter(makeLog.getLocalFile().toPath(), Charset.forName("UTF-8")); //NOI18N 
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        BuildTraceSupport.BuildTrace buldTraceSupport = getBuildTraceSupport();
        List<String> vars;
        if (configureArguments != null) {
            Map<String, String> map = new HashMap<>();
            if (buldTraceSupport != null && buldTraceSupport.getKind() == BuildTraceSupport.BuildTraceKind.Wrapper) {
                buldTraceSupport.modifyEnv(map);
            }
            configureArguments = PreBuildSupport.expandMacros(configureArguments, toolchain, map);
            vars = ImportUtils.parseEnvironment(configureArguments);
        } else {
            vars = new ArrayList<>();
        }
        if (execLog != null) {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            vars.add(BuildTraceSupport.CND_TOOLS+"="+BuildTraceSupport.getTools(makeConfigurationDescriptor.getActiveConfiguration(), executionEnvironment)); // NOI18N
            if (executionEnvironment.isLocal()) {
                vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+execLog.getLocalPath()); // NOI18N
            } else {
                vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+execLog.getRemotePath()); // NOI18N
            }
        }
        if (TRACE) {
            logger.log(Level.INFO, "#{0}", buildCommand); // NOI18N
        }
        try {
            ExecuteCommand ec = new ExecuteCommand(makeProject, workingDir, buildCommand);
            String name = NbBundle.getMessage(ImportProject.class, "BUILD_LABEL"); // NOI18N
            String tabName = ec.getExecutionEnvironment().isLocal() ? name :
                             NbBundle.getMessage(ExecuteCommand.class, "BUILD_REMOTE_LABEL", ec.getExecutionEnvironment().getDisplayName()); // NOI18N
            ec.setName(name, tabName);
            Future<Integer> task = ec.performAction(listener, outputListener, vars, buldTraceSupport);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute build command"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void waitConfigurationDescriptor() {
        // Discovery require a fully completed project
        // Make sure that descriptor was stored and readed
        ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        provider.getConfigurationDescriptor();
        try {
            waitSources.await();
        } catch (InterruptedException ex) {
        }
        refreshAfterBuild(interrupter);
    }

    private void refreshAfterBuild(final Interrupter interrupter) {
        RequestProcessor.Task refresh = RPR.post(new Runnable() {
            @Override
            public void run() {
                ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                Folder rootFolder = provider.getConfigurationDescriptor().getLogicalFolders();
                for(Folder sub : rootFolder.getFolders()) {
                    if (sub.isDiskFolder()) {
                        sub.forceDiskFolderRefreshAndWait();
                        sub.refreshDiskFolder(interrupter);
                    }
                }
            }
        });
        refresh.waitFinished();
    }

    private void discovery(MakeResult rc, DoubleFile makeLog, DoubleFile execLog, DoubleFile expectedCmakeLog) {
        try {
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            waitConfigurationDescriptor();
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            boolean done = false;
            boolean exeLogDone = false;
            boolean makeLogDone = false;
            if (!manualCA) {
                if (expectedCmakeLog != null) {
                    done = discoveryByExecLog(expectedCmakeLog, done);
                    exeLogDone = true;
                }
                if (rc == MakeResult.Successful) {
                    // build successful
                    if (!done && execLog != null) {
                        done = discoveryByExecLog(execLog, done);
                        exeLogDone = true;
                    }
                    if (!done) {
                        if (!isProjectOpened()) {
                            isFinished = true;
                            return;
                        }
                        done = discoveryByDwarfOrBuildLog(done);
                        buildArifactWasAnalyzed = true;
                        if (done && makeLog != null) {
                            if (!isProjectOpened()) {
                                isFinished = true;
                                return;
                            }
                            discoveryMacrosByBuildLog(makeLog);
                        }
                    }
                } else if (rc == MakeResult.Skipped) {
                    // build skiped
                    if (!done) {
                        if (isFullRemoteProject) {
                            if (makeLog != null) {
                                if (TRACE) {
                                    logger.log(Level.INFO, "#start remote discovery by log file {0}", makeLog); // NOI18N
                                }
                                // TODO detect real return code
                                /*done = */updateRemoteProjectImpl(makeLog);
                                done = true;
                                buildArifactWasAnalyzed = true;
                                // TODO reload configuration descriptor
                            }
                        } else {
                            if (makeLog != null) {
                                // have a build log
                                done = dicoveryByBuildLog(makeLog, done);
                                makeLogDone = true;
                            } else {
                                done = discoveryByDwarfOrBuildLog(done);
                                buildArifactWasAnalyzed = true;
                            }
                        }
                    }
                } else if (rc == MakeResult.Failed) {
                    // build faled
                    if (isFullRemoteProject) {
                        // TODO detect real return code
                        /*done = */updateRemoteProjectImpl(makeLog);
                        done = true;
                        buildArifactWasAnalyzed = true;
                        // TODO reload configuration descriptor
                    } else {
                        if (!done && execLog != null && !exeLogDone) {
                            done = discoveryByExecLog(execLog, done);
                            exeLogDone = true;
                        }
                        if (!isProjectOpened()) {
                            isFinished = true;
                            return;
                        }
                        if (!done && makeLog != null && !makeLogDone) {
                            done = dicoveryByBuildLog(makeLog, done);
                            makeLogDone = true;
                        }
                    }
                }
            }
            if (!done) {
                if (!manualCA && !buildArifactWasAnalyzed) {
                    done = discoveryByDwarf(done);
                }
            }
            postModelDiscovery();
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void fixMacros(List<ProjectConfiguration> confs) {
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        SnapShot delta = pdp.startModifications();
        boolean changed = false;
        for (ProjectConfiguration conf : confs) {
            List<FileConfiguration> files = conf.getFiles();
            for (FileConfiguration fileConf : files) {
                if (fileConf.getUserMacros().size() > 0) {
                    Item item = findByNormalizedName(new File(fileConf.getFilePath()));
                    if (item != null) {
                        if (TRACE) {
                            logger.log(Level.FINE, "#fix macros for file {0}", fileConf.getFilePath()); // NOI18N
                        }
                        changed |= ProjectBridge.setSourceStandard(item, fileConf.getLanguageStandard(), false);
                        changed |= ProjectBridge.fixFileMacros(fileConf.getUserMacros(), item);
                    }
                }
            }
        }
        if (changed) {
            DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, delta);
        } else {
            pdp.endModifications(delta, false, null);
        }
    }

    private boolean updateRemoteProjectImpl(DoubleFile makeLog) {
        ProgressHandle createHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportProject.class, "CONFIGURING_PROJECT_CREATOR",executionEnvironment.getDisplayName()));
        createHandle.start();
        try {
            FileObject projectCreator = findProjectCreator();
            if (projectCreator == null) {
                if (TRACE) {
                    logger.log(Level.INFO, NbBundle.getMessage(ImportProject.class, "ERROR_FIND_PROJECT_CREATOR",executionEnvironment.getDisplayName())); // NOI18N
                }
                return false;
            }
            if (TRACE) {
                logger.log(Level.INFO, "#{0} --netbeans-project={1} --project-reconfigure build-log={2}", // NOI18N
                            new Object[]{projectCreator.getPath(), projectFolder.getPath(), makeLog.getRemotePath()});
            }
            DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
            FileObject conf1 = projectFolder.getFileObject().getFileObject("nbproject/configurations.xml"); //NOI18N
            ExitStatus execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                         , "--netbeans-project="+projectFolder.getPath() // NOI18N
                                         , "--project-reconfigure", "build-log="+makeLog.getRemotePath() // NOI18N
                                         );
            if (TRACE) {
                logger.log(Level.INFO, "#exitCode={0}", execute.exitCode); // NOI18N
                logger.log(Level.INFO, execute.getErrorString());
                logger.log(Level.INFO, execute.getOutputString());
            }
            if (!execute.isOK()) {
                // probably java does not found an
                // try to find java in environment variables
                String java = null;
                try {
                    java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JDK_HOME"); // NOI18N
                    if (java == null || java.isEmpty()) {
                        java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JAVA_HOME"); // NOI18N
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // don't report CancellationException
                }
                if (java != null) {
                    execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                         , "--netbeans-project="+projectFolder.getPath() // NOI18N
                                         , "--project-reconfigure", "build-log="+makeLog.getRemotePath() // NOI18N
                                         );
                }
                if (!execute.isOK()) {
                    if (TRACE) {
                        logger.log(Level.INFO, NbBundle.getMessage(ImportProject.class, "ERROR_RUN_PROJECT_CREATOR",executionEnvironment.getDisplayName())); // NOI18N
                    }
                    return false;
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            makeProject.getProjectDirectory().refresh(true);
            conf1.getParent().refresh(true);
            ConfigurationDescriptorProvider cdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            Type cdpClass = cdp.getClass().getGenericSuperclass();
            for(Method method : ((Class)cdpClass).getDeclaredMethods()) {
                if ("resetConfiguration".equals(method.getName())) { // NOI18N
                    try {
                        method.setAccessible(true);
                        method.invoke(cdp);
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    break;
                }
            }
            waitConfigurationDescriptor();
            return true;
        } finally {
            createHandle.finish();
        }
    }

    private FileObject findProjectCreator() {
        FileSystem fileSystem = FileSystemProvider.getFileSystem(fileSystemExecutionEnvironment);
        for(CompilerSet set : CompilerSetManager.get(fileSystemExecutionEnvironment).getCompilerSets()) {
            if (set.getCompilerFlavor().isSunStudioCompiler()) {
                String directory = set.getDirectory();
                FileObject projectCreator = fileSystem.findResource(directory+"/../lib/ide_project/bin/ide_project"); // NOI18N
                if (projectCreator != null && projectCreator.isValid()) {
                    return projectCreator;
                }
            }
        }
        return null;
    }

    private void postModelDiscovery() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(final CsmProject project) {
                    final Object id = project.getPlatformProject();
                    if (id != null && id.equals(np)) {
                        ImportProject.listeners.remove(np);
                        CsmListeners.getDefault().removeProgressListener(this); // ignore java warning "usage of this in anonymous class"
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                postModelDiscovery(np, project);
                            }
                        });
                    }
                }

            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportProject.listeners.put(np, listener);
            switchModel(true);
        } else {
            isFinished = true;
        }
    }

    private void postModelDiscovery(NativeProject np, CsmProject p) {
        try {
            if (TRACE) {
                logger.log(Level.INFO, "#model ready, explore model"); // NOI18N
            }
            fixExcludedHeaderFiles();
            showFollwUp(np);
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean isFinished(){
        return isFinished;
    }

    public Map<Step, State> getState(){
        return new EnumMap<>(importResult);
    }

    public Project getProject(){
        return makeProject;
    }

    private boolean isUILessMode = false;
    public void setUILessMode(){
        isUILessMode = true;
    }

    public void setConfigureLog(File configureLog) {
        this.configureLog = configureLog;
    }

    public DoubleFile getMakeLog() {
        return makeLog;
    }
    
    public void setMakeLog(File log) {
        this.makeLog = DoubleFile.createFile(log, executionEnvironment);
    }

    private void showFollwUp(final NativeProject project) {
        isFinished = true;
        if (isUILessMode) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                FollowUp.showFollowUp(ImportProject.this, project);
            }
        });
    }

    Project getMakeProject() {
        return makeProject;
    }

    Map<Step, State> getImportResult() {
        return importResult;
    }

    // remove wrong "exclude from project" flags
    private void fixExcludedHeaderFiles() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (TRACE) {
            logger.log(Level.INFO, "#start fixing excluded header files by model"); // NOI18N
        }
        if (DiscoveryProjectGenerator.fixExcludedHeaderFiles(makeProject, logger)) {
            importResult.put(Step.FixExcluded, State.Successful);
        }
    }

    private Map<String,Item> normalizedItems;
    private Item findByNormalizedName(File file){
        if (normalizedItems == null) {
            normalizedItems = DiscoveryProjectGenerator.initNormalizedNames(makeProject);
        }
        String path = CndFileUtils.normalizeFile(file).getAbsolutePath();
        return normalizedItems.get(path);
    }

    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                if (TRACE) {
                    logger.log(Level.INFO, "#enable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                model.enableProject(np);
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#disable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                model.disableProject(np);
            }
        }
    }

    private static final Map<NativeProject, CsmProgressListener> listeners = new WeakHashMap<>();

    private boolean discoveryByExecLog(DoubleFile execLog, boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<>();
            DiscoveryDescriptor.ROOT_FOLDER.toMap(map, nativeProjectPath);
            DiscoveryDescriptor.EXEC_LOG_FILE.toMap(map, execLog.getLocalPath());
            DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymLinks);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start discovery by exec log file {0}", execLog.getLocalPath()); // NOI18N
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    setBuildResults(DiscoveryDescriptor.BUILD_ARTIFACTS.fromMap(map));
                    validateBuildTools(DiscoveryDescriptor.BUILD_TOOLS.fromMap(map));
                    DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
                    importResult.put(Step.DiscoveryLog, State.Successful);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by exec log file {0}", execLog.getLocalPath()); // NOI18N
                }
            }
            DiscoveryDescriptor.EXEC_LOG_FILE.toMap(map, null);
        }
        return done;
    }

    private void setBuildResults(List<String> buildArtifacts) {
        if (buildArtifacts == null || buildArtifacts.isEmpty()) {
            return;
        }
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (buildArtifacts.size() == 1) {
            MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
            if (activeConfiguration != null) {
                String value = activeConfiguration.getMakefileConfiguration().getOutput().getValue();
                if (value == null || value.isEmpty()) {
                    buildResult = buildArtifacts.get(0);
                    buildResult = ProjectSupport.toProperPath(projectFolder, CndPathUtilities.naturalizeSlashes(buildResult), pathMode);
                    buildResult = CndPathUtilities.normalizeSlashes(buildResult);
                    activeConfiguration.getMakefileConfiguration().getOutput().setValue(buildResult);
                }
            }
        }
        //Folder externalFileItems = makeConfigurationDescriptor.getExternalFileItems();
        //for(String binary : buildArtifacts) {
        //    externalFileItems.addItem(Item.createInFileSystem(makeConfigurationDescriptor.getBaseDirFileSystem(),binary));
        //}
     }

    private void validateBuildTools(Map<LanguageKind, Map<String, Integer>> buildTools) {
        if (buildTools == null || buildTools.isEmpty()) {
            return;
        }
        String cToolPath = coutPath(buildTools.get(LanguageKind.C));
        String cppToolPath = coutPath(buildTools.get(LanguageKind.CPP));
        String fortranToolPath = coutPath(buildTools.get(LanguageKind.CPP));
        if (cToolPath == null && cppToolPath == null) {
            return;
        }
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
        if (activeConfiguration == null) {
            return;
        }
        BuildTraceSupport.BuildTrace buldTraceSupport = getBuildTraceSupport();
        if (buldTraceSupport != null && buldTraceSupport.getKind() == BuildTraceSupport.BuildTraceKind.Wrapper) {
            Tool wrapperC = buldTraceSupport.getToolsWrapper().getTool(PredefinedToolKind.CCompiler);
            if (wrapperC != null && cToolPath != null) {
                if (cToolPath.equals(wrapperC.getPath())) {
                    return;
                }
                if (activeConfiguration.getCompilerSet().getCompilerSet() != null &&
                    activeConfiguration.getCompilerSet().getCompilerSet().getCompilerFlavor().isCygwinCompiler()) {
                    String converted = WindowsSupport.getInstance().convertFromCygwinPath(cToolPath);
                    if (converted != null) {
                        converted = converted.replace('\\', '/');
                        if (converted.equals(wrapperC.getPath())) {
                            return;
                        }
                    }
                }
            }
            Tool wrapperCpp = buldTraceSupport.getToolsWrapper().getTool(PredefinedToolKind.CCCompiler);
            if (wrapperCpp != null && cppToolPath != null) {
                if (cppToolPath.equals(wrapperCpp.getPath())) {
                    return;
                }
                if (activeConfiguration.getCompilerSet().getCompilerSet() != null &&
                    activeConfiguration.getCompilerSet().getCompilerSet().getCompilerFlavor().isCygwinCompiler()) {
                    String converted = WindowsSupport.getInstance().convertFromCygwinPath(cppToolPath);
                    if (converted != null) {
                        converted = converted.replace('\\', '/');
                        if (converted.equals(wrapperCpp.getPath())) {
                            return;
                        }
                    }
                }
            }
        }
        String cProjectToolPath = null;
        String cppProjectToolPath = null;
        String fortranProjectToolPath = null;
        CompilerSet2Configuration compilerSet = activeConfiguration.getCompilerSet();
        CompilerSet cs = compilerSet.getCompilerSet();
        if (cs != null) {
            Tool tool = cs.getTool(PredefinedToolKind.CCompiler);
            if (tool != null) {
                cProjectToolPath = tool.getPath().replace('\\', '/'); //NOI18N
            }
            tool = cs.getTool(PredefinedToolKind.CCCompiler);
            if (tool != null) {
                cppProjectToolPath = tool.getPath().replace('\\', '/'); //NOI18N
            }
            tool = cs.getTool(PredefinedToolKind.FortranCompiler);
            if (tool != null) {
                fortranProjectToolPath = tool.getPath().replace('\\', '/'); //NOI18N
            }
        }
        
        CompilerSetManager csm = CompilerSetManager.get(executionEnvironment);
        if (cToolPath != null) {
            if (!cToolPath.equals(cProjectToolPath)) {
                for(CompilerSet c : csm.getCompilerSets()) {
                    Tool tool = c.getTool(PredefinedToolKind.CCompiler);
                    if (tool != null) {
                        String path = tool.getPath().replace('\\', '/'); //NOI18N
                        if (cToolPath.equals(path)) {
                            // change tool collection in project
                            activeConfiguration.getCompilerSet().setValue(c.getName());
                            return;
                        }
                    }
                }
                if (cProjectToolPath == null) {
                    // add path to compiler in tool collection ?
                } else {
                    // create new tool collection
                    newToolColletionDialog(activeConfiguration, cToolPath);
                    return;
                }
            }
        }
        if (cppToolPath != null) {
            if (!cppToolPath.equals(cppProjectToolPath)) {
                for(CompilerSet c : csm.getCompilerSets()) {
                    Tool tool = c.getTool(PredefinedToolKind.CCCompiler);
                    if (tool != null) {
                        String path = tool.getPath().replace('\\', '/'); //NOI18N
                        if (cppToolPath.equals(path)) {
                            // change tool collection in project
                            activeConfiguration.getCompilerSet().setValue(c.getName());
                            return;
                        }
                    }
                }
                if (cppProjectToolPath == null) {
                    // add path to compiler in tool collection ?
                } else {
                    // create new tool collection
                    newToolColletionDialog(activeConfiguration, cppToolPath);
                    return;
                }
            }
        }
    }

    private void newToolColletionDialog(final MakeConfiguration activeConfiguration, String toolPath) {
        int i = toolPath.lastIndexOf('/');
        if (i > 0) {
            final String basePath = toolPath.substring(0, i);
            if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                logger.log(Level.INFO, NbBundle.getMessage(ImportProject.class, "TOOL_COLLECTION_MISMATCH_EXPLANATION", toolPath));
                logger.log(Level.INFO, "Confirm - No");
                return;
            } else {
                if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ImportProject.class, "TOOL_COLLECTION_MISMATCH_EXPLANATION", toolPath), // NOI18N    
                    NbBundle.getMessage(ImportProject.class, "TOOL_COLLECTION_MISMATCH_TITLE"), // NOI18N
                    NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION){
                    return;
                }
            }
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        Future<CompilerSet> future = ToolsPanelSupport.invokeNewCompilerSetWizard(executionEnvironment, basePath);
                        if (future != null) {
                            try {
                                CompilerSet cs = future.get();
                                if (cs != null) {
                                    activeConfiguration.getCompilerSet().setValue(cs.getName());
                                }
                            } catch (InterruptedException ex) {
                            } catch (ExecutionException ex) {
                            }
                        }
                    }
                });
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }
    
    
    private String coutPath(Map<String, Integer> tools) {
        String toolPath = null;
        if (tools != null) {
            int max = -1;
            for(Map.Entry<String, Integer> e : tools.entrySet()) {
                if (e.getValue() > max) {
                    toolPath = e.getKey();
                    max = e.getValue();
                }
            }
        }
        return toolPath;
    }

    private boolean discoveryByDwarfOrBuildLog(boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<>();
            DiscoveryDescriptor.ROOT_FOLDER.toMap(map, nativeProjectPath);
            DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymLinks);
            if (dwarfSource != null) {
                if (dwarfSource.isFolder()) {
                    DiscoveryDescriptor.BUILD_FOLDER.toMap(map, dwarfSource.getPath());
                } else {
                    DiscoveryDescriptor.BUILD_RESULT.toMap(map, dwarfSource.getPath());
                }
            }

            if (extension.canApply(map, makeProject, interrupter)) {
                DiscoveryProvider provider = DiscoveryDescriptor.PROVIDER.fromMap(map);
                if (provider != null && DiscoveryExtension.MAKE_LOG_PROVIDER.equals(provider.getID())) {
                    if (TRACE) {
                        logger.log(Level.INFO, "#start discovery by log file {0}", ProviderPropertyType.MakeLogPropertyType.getProperty(provider)); // NOI18N
                    }
                } else {
                    if (TRACE) {
                        logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                    }
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    if (provider != null && DiscoveryExtension.MAKE_LOG_PROVIDER.equals(provider.getID())) {
                        importResult.put(Step.DiscoveryLog, State.Successful);
                    } else {
                        importResult.put(Step.DiscoveryDwarf, State.Successful);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                }
            }
        }
        return done;
    }

    private boolean dicoveryByBuildLog(DoubleFile makeLog, boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<>();
            DiscoveryDescriptor.ROOT_FOLDER.toMap(map, nativeProjectPath);
            DiscoveryDescriptor.LOG_FILE.toMap(map, makeLog.getLocalPath());
            DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymLinks);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    DiscoveryProvider provider = DiscoveryDescriptor.PROVIDER.fromMap(map);
                    if (provider != null && DiscoveryExtension.MAKE_LOG_PROVIDER.equals(provider.getID())) {
                        logger.log(Level.INFO, "#start discovery by build log file {0}", makeLog.getLocalPath()); // NOI18N
                    } else {
                        logger.log(Level.INFO, "#start discovery by exec log file {0}", makeLog.getLocalPath()); // NOI18N
                    }
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    setBuildResults(DiscoveryDescriptor.BUILD_ARTIFACTS.fromMap(map));
                    DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
                    importResult.put(Step.DiscoveryLog, State.Successful);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by log file {0}", makeLog.getLocalPath()); // NOI18N
                }
            }
        }
        return done;
    }

    private void discoveryMacrosByBuildLog(DoubleFile makeLog) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<>();
            DiscoveryDescriptor.ROOT_FOLDER.toMap(map, nativeProjectPath);
            DiscoveryDescriptor.LOG_FILE.toMap(map, makeLog.getLocalPath());
            DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymLinks);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start fix macros by log file {0}", makeLog.getLocalPath()); // NOI18N
                }
                List<ProjectConfiguration> confs = DiscoveryDescriptor.CONFIGURATIONS.fromMap(map);
                fixMacros(confs);
                importResult.put(Step.FixMacros, State.Successful);
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#fix macros cannot be done by log file {0}", makeLog.getLocalPath()); // NOI18N
                }
            }
        }
    }

    private boolean discoveryByDwarf(boolean does) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            Map<String, Object> map = new HashMap<>();
            DiscoveryDescriptor.ROOT_FOLDER.toMap(map, nativeProjectPath);
            DiscoveryDescriptor.INVOKE_PROVIDER.toMap(map, Boolean.TRUE);
            DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymLinks);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                }
                try {
                    extension.apply(map, makeProject, interrupter);
                    importResult.put(Step.DiscoveryDwarf, State.Successful);
                    does = true;
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                }
            }
        }
        return does;
    }

    public static enum State {

        Successful, Fail, Skiped
    }

    public static enum Step {

        Project, Configure, MakeClean, Make, DiscoveryDwarf, DiscoveryLog, FixMacros, DiscoveryModel, FixExcluded
    }

    private static enum MakeResult {

        Successful, Failed, Skipped
    };
}
