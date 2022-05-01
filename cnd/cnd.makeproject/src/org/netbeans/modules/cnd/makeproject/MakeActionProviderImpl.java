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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeCommandFlagsProviderFactory;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.StepControllerProvider;
import org.netbeans.modules.cnd.makeproject.api.StepControllerProvider.StepController;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ComboStringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platform;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ResolveBuildToolsFactory;
import org.netbeans.modules.cnd.makeproject.uiapi.DefaultProjectOperationsImplementationUI;
import org.netbeans.modules.cnd.makeproject.uiapi.LongOperation;
import org.netbeans.modules.cnd.makeproject.uiapi.LongOperation.CanceledState;
import org.netbeans.modules.cnd.makeproject.uiapi.LongOperation.CancellableTask;
import org.netbeans.modules.cnd.makeproject.uiapi.RunActionSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.OSSComponentUsages;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProgress;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Action provider of the Make project. This is the place where to do
 * strange things to Make actions. E.g. compile-single.
 */
public final class MakeActionProviderImpl implements MakeActionProvider {

    private static final String[] supportedActions = {
        COMMAND_PRE_BUILD,
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DEBUG_SINGLE,
        COMMAND_BATCH_BUILD,
        COMMAND_BUILD_PACKAGE,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        COMMAND_CUSTOM_ACTION,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST,
        COMMAND_DEBUG_STEP_INTO_TEST,
    };
    // Project
    private final MakeProject project;
    // Project Descriptor
    //private MakeConfigurationDescriptor projectDescriptor = null;
    /** Map from commands to make targets */
    private final Map<String, Commands> commands;
    private boolean lastValidation = false;
    private static final String SAVE_STEP = "save"; // NOI18N
    private static final String PRE_BUILD_STEP = "pre-build"; // NOI18N
    private static final String BUILD_STEP = "build"; // NOI18N
    private static final String BUILD_PACKAGE_STEP = "build-package"; // NOI18N
    private static final String CLEAN_STEP = "clean"; // NOI18N
    private static final String RUN_STEP = "run"; // NOI18N
    private static final String DEBUG_STEP = "debug"; // NOI18N
    private static final String DEBUG_STEPINTO_STEP = "debug-stepinto"; // NOI18N
    private static final String RUN_SINGLE_STEP = "run-single"; // NOI18N
    private static final String DEBUG_SINGLE_STEP = "debug-single"; // NOI18N
    private static final String COMPILE_SINGLE_STEP = "compile-single"; // NOI18N
    private static final String CUSTOM_ACTION_STEP = "custom-action"; // NOI18N
    private static final String VALIDATE_TOOLCHAIN = "validate-toolchain"; // NOI18N
    private static final String BUILD_TESTS_STEP = "build-tests"; // NOI18N
    private static final String TEST_STEP = "test"; // NOI18N
    private static final String TEST_SINGLE_STEP = "test-single"; // NOI18N
    private static final String DEBUG_TEST_STEP = "debug-test"; // NOI18N
    private static final String DEBUG_STEPINTO_TEST_STEP = "debug-stepinto-test"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("Make Action RP", 1);// NOI18N
    private static final boolean PARALLEL_POST = "true".equals(System.getProperty("org.netbeans.modules.cnd.makeproject.parallel-build", "false"));    

    public MakeActionProviderImpl(MakeProject project) {
        this.project = project;
        commands = loadAcrionSteps("CND/BuildAction"); // NOI18N
    }

    private Map<String, Commands> loadAcrionSteps(String root) {
        Map<String, Commands> res = new HashMap<>();
        FileObject folder = FileUtil.getConfigFile(root);
        if (folder != null && folder.isFolder()) {
            for (FileObject subFolder : folder.getChildren()) {
                if (subFolder.isFolder()) {
                    TreeMap<Integer, Command> map = new TreeMap<>();
                    for (FileObject file : subFolder.getChildren()) {
                        Integer position = (Integer) file.getAttribute("position"); // NOI18N
                        String flag = (String) file.getAttribute("flag"); // NOI18N
                        map.put(position, new Command(file.getNameExt(),flag));
                    }
                    res.put(subFolder.getNameExt(), new Commands(subFolder.getNameExt(), map.values()));
                }
            }
        }
        return res;
    }

    private boolean isProjectDescriptorLoaded() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.gotDescriptor();
    }

    private MakeConfigurationDescriptor getProjectDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.getConfigurationDescriptor();
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    private void saveIfModified() {
        if (getProjectDescriptor() != null && getProjectDescriptor().isModified()) {
            getProjectDescriptor().save();
        }
    }

    @Override
    public void invokeAction(String command, final Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            try {
                // it's better to set deleted flag right here, otherwise we can start saving the project
                // #196501 - "Error synchronizing project to <login>@<host> null"
                ((MakeProjectImpl)project).setDeleting(true);  // can't set setDeleted here since user can answer "No"
                DefaultProjectOperationsImplementationUI.getDefaultProjectOperationsImplementationUI()
                        .deleteProject(project, new DefaultProjectOperationsImplementationExecutorImpl());
            } finally {
                ((MakeProjectImpl)project).setDeleting(false);                 
            }
            return;
        }

        if (COMMAND_COPY.equals(command)) {
            saveIfModified();
                DefaultProjectOperationsImplementationUI.getDefaultProjectOperationsImplementationUI()
                        .copyProject(project, new DefaultProjectOperationsImplementationExecutorImpl());
            return;
        }

        if (COMMAND_MOVE.equals(command)) {
            saveIfModified();
                DefaultProjectOperationsImplementationUI.getDefaultProjectOperationsImplementationUI()
                        .moveProject(project, new DefaultProjectOperationsImplementationExecutorImpl());
            return;
        }

        if (COMMAND_RENAME.equals(command)) {
            saveIfModified();
                DefaultProjectOperationsImplementationUI.getDefaultProjectOperationsImplementationUI()
                        .renameProject(project, new DefaultProjectOperationsImplementationExecutorImpl());
            return;
        }

        if (COMMAND_RUN_SINGLE.equals(command)) {
            RunActionSupport.geRunActionSupport().run(context);
            return;
        }

        // Basic info
        final MakeConfigurationDescriptor pd = getProjectDescriptor();
        MakeConfiguration activeConf = pd.getActiveConfiguration();
        if (activeConf == null) {
            return;
        }

        final List<MakeConfiguration> confs = new ArrayList<>();
        if (command.equals(COMMAND_BATCH_BUILD)) {
            ConfirmSupport.BatchConfigurationSelector selector = ConfirmSupport.getBatchConfigurationSelectorFactory().create(project, pd.getConfs().toArray());
            if (selector == null) {
                return;
            }
            command = selector.getCommand();
            Configuration[] confsArray = selector.getSelectedConfs();
            for (Configuration conf : confsArray) {
                confs.add((MakeConfiguration) conf);
            }
        } else {
            confs.add(activeConf);
        }
        final String finalCommand = command;
        ActionProgress progress;
        if (PARALLEL_POST) {
            progress = null;
        } else {
            progress = ActionProgress.start(context);
        }        

        CancellableTask actionWorker = new CancellableTask(progress) {

            @Override
            protected void runImpl() {
                final ArrayList<ProjectActionEvent> actionEvents = new ArrayList<>();
                for (MakeConfiguration conf : confs) {
                    if (!addAction(actionEvents, pd, conf, finalCommand, context, getCancelled())) {
                        if (!getCancelled().isCanceled()) {
                            getCancelled().getActionProgress().finished(false);
                        }
                        return;
                    }
                }
                // Execute actions
                if (!getCancelled().isCanceled()) {
                    if (actionEvents.size() > 0) {
                        RP.post(new NamedRunnable("Make Project Action Worker") { //NOI18N
                            @Override
                            protected void runImpl() {
                                ProjectActionSupport.getInstance().fireActionPerformed(actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]),null, getCancelled().getActionProgress());
                            }
                        });
                    } else {
                        getCancelled().getActionProgress().finished(false);
                    }
                }
            }
        };
        ConfigurationDescriptorProvider.recordActionMetrics(command, pd); //NOI18N
        runActionWorker(activeConf.getDevelopmentHost().getExecutionEnvironment(), actionWorker);
    }

    private static void runActionWorker(ExecutionEnvironment exeEnv, CancellableTask actionWorker) {
        ServerRecord record = ServerList.get(exeEnv);
        assert record != null;
        invokeLongAction(record, actionWorker);
    }

    public void invokeCustomAction(final MakeConfigurationDescriptor pd, final MakeConfiguration conf, final ProjectActionHandler customProjectActionHandler) {
        CancellableTask actionWorker = new CancellableTask(null) {

            @Override
            protected void runImpl() {
                ArrayList<ProjectActionEvent> actionEvents = new ArrayList<>();
                addAction(actionEvents, pd, conf, MakeActionProviderImpl.COMMAND_CUSTOM_ACTION, null, getCancelled());
                ProjectActionSupport.getInstance().fireActionPerformed(
                        actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]),
                        customProjectActionHandler);
            }
        };
        runActionWorker(conf.getDevelopmentHost().getExecutionEnvironment(), actionWorker);
    }

    private static void invokeLongAction(final ServerRecord record, final CancellableTask actionWorker) {
        CancellableTask wrapper;
        if (!record.isDeleted() && record.isOnline()) {
            wrapper = actionWorker;
        } else {
            String message;
            if (record.isDeleted()) {
                message = MessageFormat.format(getString("ERR_RequestingDeletedConnection"), record.getDisplayName()); //NOI18N
                String autoConfirm = message +"\n Confirm Yes"; //NOI18N
                if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                    new Exception(autoConfirm).printStackTrace(System.err);
                    ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                } else {
                    ConfirmSupport.AutoConfirm confirm = ConfirmSupport.getConfirmCreateConnectionFactory().createConnection(
                            getString("DLG_TITLE_DeletedConnection"), message, autoConfirm); //NOI18N
                    if (confirm == null) {
                        return;
                    }
                    ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                }
            }
            // start validation phase
            wrapper = new CancellableTask(actionWorker.getCancelled().getActionProgress()) {

                @Override
                public boolean cancel() {
                    return actionWorker.cancel();
                }

                @Override
                public void runImpl() {
                    try {
                        if (!ConnectionManager.getInstance().isConnectedTo(record.getExecutionEnvironment())) {
                            ConnectionManager.getInstance().connectTo(record.getExecutionEnvironment());
                        }
                        record.validate(true);
                        // initialize compiler sets for remote host if needed
                        CompilerSetManager csm = CompilerSetManager.get(record.getExecutionEnvironment());
                        csm.initialize(true, true, null);
                    } catch (CancellationException ex) {
                        cancel();
                    } catch (Exception e) {
                        final String message = MessageFormat.format(getString("ERR_Cant_Connect"), record.getDisplayName()); //NOI18N
                        if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                            new Exception(message).printStackTrace(System.err);
                        } else {
                            final String title = getString("DLG_TITLE_Cant_Connect"); //NOI18N
                            ConfirmSupport.getNotifyCantConnectFactory().showErrorLater(title, message);
                        }
                    }
                    if (record.isOnline()) {
                        actionWorker.run();
                    }
                }
            };
        }
        LongOperation.getLongOperation().executeLongOperation(wrapper,
                NbBundle.getMessage(MakeActionProviderImpl.class, "DLG_TITLE_Validate_Host"),
                NbBundle.getMessage(MakeActionProviderImpl.class, "MSG_Validate_Host", record.getDisplayName()));
    }

    private boolean addAction(ArrayList<ProjectActionEvent> actionEvents,
            MakeConfigurationDescriptor pd, MakeConfiguration conf, String command, Lookup context,
            CanceledState cancelled) throws IllegalArgumentException {

        if (cancelled.isCanceled()) {
            return false;
        }

        AtomicBoolean validated = new AtomicBoolean(false);
        lastValidation = false;

        String[] targetNames = getTargetNames(command, conf, context);
        if (targetNames == null || targetNames.length == 0) {
            return false;
        }

        for (int i = 0; i < targetNames.length; i++) {
            final String targetName = targetNames[i];
            List<String> tail = new ArrayList<>();
            for (int j = i + 1; j < targetNames.length; j++) {
                tail.add(targetNames[j]);
            }
            List<String> delegate = validateStep(targetName, tail);
            if (delegate != null) {
                for (String target : delegate) {
                    if (!addTarget(target, actionEvents, pd, conf, context, cancelled, validated)) {
                        break;
                    }
                }
            } else {
                if (!addTarget(targetName, actionEvents, pd, conf, context, cancelled, validated)) {
                    break;
                }
            }
        }
        return true;
    }

    private boolean addTarget(String targetName, ArrayList<ProjectActionEvent> actionEvents,
            MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, CanceledState cancelled, AtomicBoolean validated) throws IllegalArgumentException {
        if (cancelled.isCanceled()) {
            return false; // getPlatformInfo() might be costly for remote host
        }

        if (targetName.equals(SAVE_STEP)) {
            return onSaveStep();
        } else if (targetName.equals(VALIDATE_TOOLCHAIN)) {
            return onValidateToolchainStep(pd, conf, cancelled, validated, context);
        } else if (targetName.equals(PRE_BUILD_STEP)) {
            return onPreBuildStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.PRE_BUILD);
        } else if (targetName.equals(BUILD_STEP)) {
            return onBuildStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.BUILD);
        } else if (targetName.equals(BUILD_TESTS_STEP)) {
            return onBuildStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.BUILD_TESTS);
        } else if (targetName.equals(BUILD_PACKAGE_STEP)) {
            return onBuildPackageStep(actionEvents, conf, context, ProjectActionEvent.PredefinedType.BUILD);
        } else if (targetName.equals(CLEAN_STEP)) {
            return onCleanStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.CLEAN);
        } else if (targetName.equals(COMPILE_SINGLE_STEP)) {
            return onCompileSingleStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.COMPILE_SINGLE);
        } else if (targetName.equals(RUN_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.RUN);
        } else if (targetName.equals(TEST_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.TEST);
        } else if (targetName.equals(TEST_SINGLE_STEP)) {
            return onTestSingleStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.TEST);
        } else if (targetName.equals(RUN_SINGLE_STEP) || targetName.equals(DEBUG_SINGLE_STEP)) {
            return onRunSingleStep(conf, actionEvents, context, ProjectActionEvent.PredefinedType.RUN);
        } else if (targetName.equals(DEBUG_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG);
        } else if (targetName.equals(DEBUG_TEST_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG_TEST);
        } else if (targetName.equals(DEBUG_STEPINTO_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG_STEPINTO);
        } else if (targetName.equals(DEBUG_STEPINTO_TEST_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG_STEPINTO_TEST);
        } else if (targetName.equals(CUSTOM_ACTION_STEP)) {
            return onCustomActionStep(actionEvents, conf, context, ProjectActionEvent.PredefinedType.CUSTOM_ACTION);
        }
        return onExtendedStep(actionEvents, conf, context, targetName);
    }

    private boolean onSaveStep() {
        // Save all files and projects
        if (MakeOptions.getInstance().getSave()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!ProjectSupport.saveAllProjects(getString("NeedToSaveAllText"))) {// NOI18N
            return false;
        }
        return true;
    }

    private boolean onRunStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, CanceledState cancelled, AtomicBoolean validated, Lookup context, Type actionEvent) {
        PlatformInfo pi = conf.getPlatformInfo();
        validated.set(true);

        Folder targetFolder = context.lookup(Folder.class);
        if (targetFolder != null) { // DEBUG TEST
            String path = ""; // NOI18N
            if (targetFolder.isTest()) {
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet != null) {
                    path = targetFolder.getFolderConfiguration(conf).getLinkerConfiguration().getOutputValue();
                    path = conf.expandMacros(path);
                    path = CndPathUtilities.toAbsolutePath(conf.getBaseFSPath(), path);
                    
                    conf = conf.clone();    //  Replacing output path with test output path
                    StringConfiguration sc = new StringConfiguration(null, "OutputPath"); // NOI18N
                    sc.setValue(path);                    
                    
                    // Copied from MakeConfiguration.getOutputValue()
                    if (conf.isLinkerConfiguration()) {             // Binary or dynamic lib
                        conf.getLinkerConfiguration().setOutput(sc);
                    } else if (conf.isArchiverConfiguration()) {    // Static lib
                        conf.getArchiverConfiguration().setOutput(sc);
                    } else if (conf.isMakefileConfiguration()) {    // Unmanaged project
                        conf.getMakefileConfiguration().setOutput(sc);
                    } else if (conf.isQmakeConfiguration()) {       // QT
                    } else {
                        assert false;
                    }
                }
            }
            RunProfile runProfile = createRunProfile(conf, cancelled);
            if (runProfile == null) {
                if (cancelled.isCanceled()) {
                    return false; // getEnv() might be costly for remote host
                }
            }
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, runProfile, false);
            actionEvents.add(projectActionEvent);
        } else if (actionEvent == ProjectActionEvent.PredefinedType.TEST) { // RUN TEST
            if (conf.isCompileConfiguration() && !validateProject(conf)) {
                return true;
            }
            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
            String buildCommand;
            buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf), "test"); // NOI18N
            String args = "";
            int index = getArgsIndex(buildCommand);
            if (index > 0) {
                args = buildCommand.substring(index + 1);
                buildCommand = removeQuotes(buildCommand.substring(0, index));
            }
            RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
            profile.setArgs(args);
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
            actionEvents.add(projectActionEvent);
        } else if (conf.isMakefileConfiguration()) { // RUN UNMANAGED
            String path;
            if (actionEvent == ProjectActionEvent.PredefinedType.RUN) {
                path = conf.getMakefileConfiguration().getOutput().getValue();
                if (path.length() > 0 && !CndPathUtilities.isPathAbsolute(path)) {
                    // make path relative to run working directory
                    // path here should always be in unix style, see issue 149404
                    path = conf.getMakefileConfiguration().getAbsOutput();
                    path = CndPathUtilities.toRelativePath(conf.getProfile().getRunDirectory(), path);
                }
            } else {
                // Always absolute
                path = conf.getMakefileConfiguration().getAbsOutput();
                path = CndPathUtilities.normalizeSlashes(path);
            }
            RunProfile runProfile = conf.getProfile().clone(conf);
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, runProfile, false);
            actionEvents.add(projectActionEvent);
        } else if (conf.isLibraryConfiguration()) {
            String path;
            if (actionEvent == ProjectActionEvent.PredefinedType.RUN) {
                path = conf.getProfile().getRunCommand().getValue();
                if (path.length() > 0 && !CndPathUtilities.isPathAbsolute(path)) {
                    // make path relative to run working directory
                    // path here should always be in unix style, see issue 149404
                    path = conf.getMakefileConfiguration().getAbsOutput();
                    path = CndPathUtilities.toRelativePath(conf.getProfile().getRunDirectory(), path);
                }
            } else {
                // Always absolute
                path = conf.getMakefileConfiguration().getAbsOutput();
                path = CndPathUtilities.normalizeSlashes(path);
            }
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, null, false);
            actionEvents.add(projectActionEvent);
        } else if (conf.isApplicationConfiguration()) { // RUN MANAGED
            RunProfile runProfile = createRunProfile(conf, cancelled);
            if (runProfile == null) {
                if (cancelled.isCanceled()) {
                    return false; // getEnv() might be costly for remote host
                }
            }
            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
            String path;
            if (actionEvent == ProjectActionEvent.PredefinedType.RUN) {
                // naturalize if relative
                path = makeArtifact.getOutput();
                //TODO: we also need remote aware CndPathUtilities..........
                if (!CndPathUtilities.isPathAbsolute(path)) {
                    // make path relative to run working directory
                    path = makeArtifact.getWorkingDirectory() + "/" + path; // NOI18N
                    path = CndPathUtilities.naturalizeSlashes(path);
                    path = CndPathUtilities.toRelativePath(conf.getProfile().getRunDirectory(), path);
                    path = CndPathUtilities.naturalizeSlashes(path);
                }
            } else {
                // Always absolute
                path = CndPathUtilities.toAbsolutePath(conf.getBaseFSPath(), makeArtifact.getOutput());
            }
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, runProfile, false, context);
            actionEvents.add(projectActionEvent);
        } else {
            assert false;
        }
        return true;
    }

    private static RunProfile createRunProfile(MakeConfiguration conf, CanceledState cancelled) {
        RunProfile runProfile = null;
        PlatformInfo pi = conf.getPlatformInfo();
        int platform = conf.getDevelopmentHost().getBuildPlatform();
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            // On Windows we need to add paths to dynamic libraries from subprojects to PATH
            runProfile = conf.getProfile().clone(conf);
            Set<String> subProjectOutputLocations = conf.getSubProjectOutputLocations();
            StringBuilder path = new StringBuilder();
            // Add paths from subprojetcs
            Iterator<String> iter = subProjectOutputLocations.iterator();
            
            while (iter.hasNext()) {
                String location = CndPathUtilities.naturalizeSlashes(iter.next());
                if (path.length() > 0) {
                    path.append(';');
                }
                path.append(location);
            }
            
            // Add paths from -L option
            List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
            iter = list.iterator();
            while (iter.hasNext()) {
                String location = CndPathUtilities.naturalizeSlashes(iter.next());
                if (path.length() > 0) {
                    path.append(';');
                }
                path.append(location);
            }
            String userPath = runProfile.getEnvironment().getenv(pi.getPathName());
            if (userPath == null) {
                if (cancelled.isCanceled()) {
                    return null; // getEnv() might be costly for remote host
                }
                userPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get(pi.getPathName());
            }
            
            if (userPath != null && !userPath.isEmpty()) {
                if (path.length() > 0) {
                    path.append(';');
                }
                path.append(userPath);
            }
            
            runProfile.getEnvironment().putenv(pi.getPathName(), path.toString());
        } else if (platform == PlatformTypes.PLATFORM_MACOSX) {
            // On Mac OS X we need to add paths to dynamic libraries from subprojects to DYLD_LIBRARY_PATH
            StringBuilder path = new StringBuilder();
            Set<String> subProjectOutputLocations = conf.getSubProjectOutputLocations();
            // Add paths from subprojetcs
            Iterator<String> iter = subProjectOutputLocations.iterator();
            while (iter.hasNext()) {
                String location = CndPathUtilities.naturalizeSlashes(iter.next());
                if (path.length() > 0) {
                    path.append(":"); // NOI18N
                }
                path.append(location);
            }
            // Add paths from -L option
            List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
            iter = list.iterator();
            while (iter.hasNext()) {
                String location = CndPathUtilities.naturalizeSlashes(iter.next());
                if (path.length() > 0) {
                    path.append(":"); // NOI18N
                }
                path.append(location);
            }
            if (path.length() > 0) {
                runProfile = conf.getProfile().clone(conf);
                String extPath = runProfile.getEnvironment().getenv("DYLD_LIBRARY_PATH"); // NOI18N
                if (extPath == null) {
                    if (cancelled.isCanceled()) {
                        return null; // getEnv() might be costly for remote host
                    }
                    extPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("DYLD_LIBRARY_PATH"); // NOI18N
                }
                if (extPath != null) {
                    path.append(":").append(extPath); // NOI18N
                }
                runProfile.getEnvironment().putenv("DYLD_LIBRARY_PATH", path.toString()); // NOI18N
            }
        } else if (platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC || platform == PlatformTypes.PLATFORM_LINUX) {
            // Add paths from -L option
            StringBuilder path = new StringBuilder();
            List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
            Iterator<String> iter = list.iterator();
            while (iter.hasNext()) {
                String location = CndPathUtilities.naturalizeSlashes(iter.next());
                if (path.length() > 0) {
                    path.append(":"); // NOI18N
                }
                path.append(location);
            }
            if (path.length() > 0) {
                runProfile = conf.getProfile().clone(conf);
                String extPath = runProfile.getEnvironment().getenv("LD_LIBRARY_PATH"); // NOI18N
                if (extPath == null) {
                    if (cancelled.isCanceled()) {
                        return null; // NOI18N
                    }
                    extPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("LD_LIBRARY_PATH"); // NOI18N
                }
                if (extPath != null) {
                    path.append(":").append(extPath); // NOI18N
                }
                runProfile.getEnvironment().putenv("LD_LIBRARY_PATH", path.toString()); // NOI18N
            }
            // make sure OMP_NUM_THREADS is set to something reasonable
            // See 169981 for details
            String ont = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("OMP_NUM_THREADS"); // NOI18N
            if (ont == null) {
                ont = conf.getProfile().getEnvironment().getenv("OMP_NUM_THREADS"); // NOI18N
            }
            if (ont == null) {
                if (runProfile == null) {
                    runProfile = conf.getProfile().clone(conf);
                }
                runProfile.getEnvironment().putenv("OMP_NUM_THREADS", "2"); // NOI18N
            }
        }
        if (platform == PlatformTypes.PLATFORM_MACOSX || platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC || platform == PlatformTypes.PLATFORM_LINUX) {
            // Make sure DISPLAY variable has been set
            if (cancelled.isCanceled()) {
                return null; // getEnv() might be costly for remote host
            }
            if (conf.getDevelopmentHost().getExecutionEnvironment().isLocal()
                    && HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("DISPLAY") == null && // NOI18N
                    conf.getProfile().getEnvironment().getenv("DISPLAY") == null) {// NOI18N
                // DISPLAY hasn't been set
                if (runProfile == null) {
                    runProfile = conf.getProfile().clone(conf);
                }
                runProfile.getEnvironment().putenv("DISPLAY", ":0.0"); // NOI18N
            }
        }
        return runProfile;
    }

    private boolean onRunSingleStep(MakeConfiguration conf, ArrayList<ProjectActionEvent> actionEvents, Lookup context, Type actionEvent) {
        // FIXUP: not sure this is used...
        if (conf.isMakefileConfiguration()) {
            FileObject f = context.lookup(FileObject.class);
            String path = CndFileUtils.toFile(f).getPath();
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, null, false);
            actionEvents.add(projectActionEvent);
        } else {
            assert false;
        }
        return true;
    }

    private boolean onPreBuildStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {
//        if (conf.isCompileConfiguration() && !validateProject(conf)) {
//            return true;
//        }
//        Lookup lookup =  Lookup.EMPTY;
        String workingDir = conf.getPreBuildConfiguration().getAbsPreBuildCommandWorkingDir();
        String buildCommand = conf.getPreBuildConfiguration().getPreBuildCommand().getValue();
        String args = "";
        int index = getArgsIndex(buildCommand);
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = removeQuotes(buildCommand.substring(0, index));
        }
        if (buildCommand.equals(PreBuildSupport.CMAKE_MACRO)) {
            buildCommand = removeQuotes(getCMakeCommand(pd, conf));
        }
        try {
            FileSystem fs = FileSystemProvider.getFileSystem(conf.getFileSystemHost());
            FileObject root = fs.getRoot();
            if (workingDir.startsWith("\\\\")) { //NOI18N
                // fileUtil cannot create folder on network path. See IZ #268116
                String unixWorkingDir = workingDir.replace('\\', '/');
                FileObject[] children = root.getChildren();
                for (FileObject ch : root.getChildren()) {
                    String prefix = ch.getPath() + "/"; //NOI18N
                    if (unixWorkingDir.startsWith(prefix)) {
                        root = ch;
                        break;
                    }
                }
            }
            String toRelativePath = CndPathUtilities.toRelativePath(root, workingDir);
            FileUtil.createFolder(root, toRelativePath);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        RunProfile profile = new RunProfile(workingDir, conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onBuildStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {
        if (conf.isCompileConfiguration() && !validateProject(conf)) {
            return true;
        }
        Lookup ownLookup = null;
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_APPLICATION ||
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_DYNAMIC_LIB ||
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_STATIC_LIB) {
            for(ProjectActionEvent event : actionEvents) {
                SnapShot snapShot = event.getContext().lookup(SnapShot.class);
                if (snapShot != null) {
                    ownLookup = event.getContext();
                    break;
                }
            }
            if (ownLookup == null) {
                ConfigurationDescriptorProvider cdp = pd.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
                ownLookup = Lookups.fixed(cdp.startModifications());
            }
        }
        if (ownLookup == null) {
            ownLookup = Lookup.EMPTY;
        }
        Lookup proxyLookup = new ProxyLookup(context, ownLookup);
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String buildCommand;
        String makeCommand = getMakeCommand(pd, conf);
        if (actionEvent == ProjectActionEvent.PredefinedType.BUILD_TESTS) {
            buildCommand = makeArtifact.getBuildCommand(makeCommand, "build-tests"); // NOI18N
        } else {
            buildCommand = makeArtifact.getBuildCommand(makeCommand, ""); // NOI18N
        }
        String args = "";
        int index = getArgsIndex(buildCommand);
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = removeQuotes(buildCommand.substring(0, index));
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true, proxyLookup);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onBuildPackageStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Lookup context, Type actionEvent) {
        if (!validatePackaging(conf)) {
            actionEvents.clear();
            return true;
        }
        
        final String script = "nbproject/Package-" + conf.getName() + ".bash"; // NOI18N
        final RunProfile profile = new RunProfile(conf.getBaseDir(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        
        String buildCommand = null;

        if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
            HostInfo hostInfo;
            try {
                hostInfo = HostInfoUtils.getHostInfo(conf.getDevelopmentHost().getExecutionEnvironment());
                buildCommand = hostInfo.getShell();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                // don't report cancellation exception
            }

            if (buildCommand == null) {
                buildCommand = "sh.exe"; // NOI18N
            }
        } else {
            buildCommand = "bash"; // NOI18N
        }

        if (conf.getPackagingConfiguration().getVerbose().getValue()) {
            profile.setArgs(new String[] {"-x", script}); // NOI18N
        } else {
            profile.setArgs(new String[] {script});
        }

        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onCleanStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {
        Lookup ownLookup = null;
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_APPLICATION ||
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_DYNAMIC_LIB ||
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_STATIC_LIB) {
            for(ProjectActionEvent event : actionEvents) {
                SnapShot snapShot = event.getContext().lookup(SnapShot.class);
                if (snapShot != null) {
                    ownLookup = event.getContext();
                    break;
                }
            }
            if (ownLookup == null) {
                ConfigurationDescriptorProvider cdp = pd.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
                ownLookup = Lookups.fixed(cdp.startModifications());
            }
        }
        if (ownLookup == null) {
            ownLookup = Lookup.EMPTY;
        }
        Lookup proxyLookup = new ProxyLookup(context, ownLookup);
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String buildCommand = makeArtifact.getCleanCommand(getMakeCommand(pd, conf), ""); // NOI18N
        String args = ""; // NOI18N
        int index = getArgsIndex(buildCommand);
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = removeQuotes(buildCommand.substring(0, index));
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true, proxyLookup);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onExtendedStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Lookup context, final String extendedStep) {
        Type actionEvent = new MyType(extendedStep);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, null, conf, null, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onCompileSingleStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {
        Item item = getNoteItem(context);
        if (item == null) {
            return false;
        }
        ItemConfiguration itemConfiguration = item.getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return false;
        }
        if (itemConfiguration.getExcluded().getValue()) {
            Item bestComileUnit = getBestComileUnit(item, conf);
            if (bestComileUnit != null) {
                return onCompileSingleStepImpl(pd, conf, bestComileUnit, bestComileUnit.getItemConfiguration(conf), actionEvents, context, actionEvent);
            }
            return false;
        }
        if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified()) {
            Item bestComileUnit = getBestComileUnit(item, conf);
            if (bestComileUnit != null) {
                return onCompileSingleStepImpl(pd, conf, bestComileUnit, bestComileUnit.getItemConfiguration(conf), actionEvents, context, actionEvent);
            }
            return false;
        }
        return onCompileSingleStepImpl(pd, conf, item, itemConfiguration, actionEvents, context, actionEvent);
    }

    private boolean onCompileSingleStepImpl(MakeConfigurationDescriptor pd, MakeConfiguration conf, Item item, ItemConfiguration itemConfiguration, ArrayList<ProjectActionEvent> actionEvents, Lookup context, Type actionEvent) {
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String outputFile = getOutputFile(conf, item, itemConfiguration);
        if (conf.isMakefileConfiguration()) {
            return compileSingleUnmanage(actionEvents, conf, makeArtifact, context, actionEvent, item, itemConfiguration);
        } else {
            compileSingleManage(actionEvents, conf, makeArtifact, context, actionEvent, pd, outputFile);
            return true;
        }
    }
    
    private Item getBestComileUnit(Item item, MakeConfiguration conf) {
        CodeAssistance ca = Lookup.getDefault().lookup(CodeAssistance.class);
        Item bestCandidate = null;
        if (ca.hasCodeAssistance(item)) {
            String name = item.getName();
            if (name.indexOf('.') > 0) {
                name = name.substring(0, name.lastIndexOf('.'));
            }
            // included header
            List<NativeFileItem> listCU = ca.findHeaderCompilationUnit(item);
            for(NativeFileItem i : listCU) {
                if (Objects.equals(item.getNativeProject(), i.getNativeProject()) &&
                   (i instanceof Item)) {
                    if (checkItemCompileConfuguration((Item) i, conf)) {
                        String aName = i.getName();
                        if (aName.indexOf('.') > 0) {
                            aName = aName.substring(0, aName.lastIndexOf('.'));
                        }
                        bestCandidate = (Item) i;
                        if (aName.equals(name)) {
                            break;
                        }
                    }
                }
            }
        }
        return bestCandidate;
    }
    
    private String getOutputFile(MakeConfiguration conf, Item item, ItemConfiguration itemConfiguration) {
        String outputFile = null;
        if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
            CCompilerConfiguration cCompilerConfiguration = itemConfiguration.getCCompilerConfiguration();
            outputFile = cCompilerConfiguration.getOutputFile(item, conf, false);
            if(item.getFolder().isTest()) {
                outputFile = outputFile.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
            CCCompilerConfiguration ccCompilerConfiguration = itemConfiguration.getCCCompilerConfiguration();
            outputFile = ccCompilerConfiguration.getOutputFile(item, conf, false);
            if(item.getFolder().isTest()) {
                outputFile = outputFile.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.FortranCompiler) {
            FortranCompilerConfiguration fortranCompilerConfiguration = itemConfiguration.getFortranCompilerConfiguration();
            outputFile = fortranCompilerConfiguration.getOutputFile(item, conf, false);
            if(item.getFolder().isTest()) {
                outputFile = outputFile.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.Assembler) {
            AssemblerConfiguration assemblerConfiguration = itemConfiguration.getAssemblerConfiguration();
            outputFile = assemblerConfiguration.getOutputFile(item, conf, false);
            if(item.getFolder().isTest()) {
                outputFile = outputFile.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
            CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
            outputFile = customToolConfiguration.getOutputs().getValue();
        }
        return conf.expandMacros(outputFile);
    }
        
    private boolean compileSingleUnmanage(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, MakeArtifact makeArtifact, Lookup context, Type actionEvent, Item item, ItemConfiguration itemConfiguration) {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler ccCompiler = null;
        AllOptionsProvider optionProvider = null;
        if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
            ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
            optionProvider = itemConfiguration.getCCompilerConfiguration();
        } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
            ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
            optionProvider = itemConfiguration.getCCCompilerConfiguration();
        } 
        if (ccCompiler == null) {
            return false;
        }
        CompileConfiguration compileConfiguration = conf.getCompileConfiguration();
        if (CompileConfiguration.AUTO_COMPILE.equals(compileConfiguration.getCompileCommand().getValue())) {
            //auto
            AllOptionsProvider options = CompileOptionsProvider.getDefault().getOptions(item);
            if (options != null) {
                String compileLine = options.getAllOptions(ccCompiler);
                if (compileLine != null) {
                    int hasPath = compileLine.indexOf('#');// NOI18N
                    if (hasPath >= 0) {
                        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
                        profile.setRunDirectory(compileLine.substring(0, hasPath));
                        String command = compileLine.substring(hasPath+1).trim();
                        if (command.length() > 0 && command.charAt(0) != '-') {// NOI18N
                            int i = command.indexOf(' ');
                            if (i > 0) {
                                command = command.substring(i+1).trim();
                            }
                        }
                        profile.setArgs(command);
                        String compilerPath = convertPath(ccCompiler.getPath(), conf.getDevelopmentHost().getExecutionEnvironment());
                        ExecutionEnvironment ee = conf.getDevelopmentHost().getExecutionEnvironment();
                        if (ee.isLocal() && Utilities.isWindows()) {
                            try {
                                compilerPath = compilerPath.replace('\\', '/'); // NOI18N
                                command = escapeQuotes(command);
                                profile.setArgs(new String[]{"-c", "\"'"+compilerPath+"' "+command+"\""}); // NOI18N
                                HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                                compilerPath = hostInfo.getShell();
                            } catch (IOException ex) {
                                return false;
                            } catch (CancellationException ex) {
                                return false;
                            }
                        }
                        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, compilerPath, conf, profile, true, context);
                        actionEvents.add(projectActionEvent);
                        return true;
                    }
                }
            } else if (optionProvider != null) {
                String compileLine = optionProvider.getAllOptions(ccCompiler);
                if (compileLine != null) {
                    RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
                    profile.setRunDirectory(makeArtifact.getWorkingDirectory());
                    String command = compileLine.trim();
                    List<String> parseArgs = ImportUtils.parseArgs(command);
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < parseArgs.size(); i++) {
                        String s = parseArgs.get(i);
                        String s2 = CndPathUtilities.quoteIfNecessary(s);
                        if (s.equals(s2)) {
                            if (s.indexOf('"') > 0) {// NOI18N
                                int j = s.indexOf("\\\"");// NOI18N
                                if (j < 0) {
                                    s = s.replace("\"", "\\\"");// NOI18N
                                }
                            }
                        } else {
                            s = s2;
                        }
                        if (buf.length()>0) {
                            buf.append(' ');
                        }
                        buf.append(s);
                    }
                    command = buf.toString();
                    command = command+" -o "+getDevNull(conf.getDevelopmentHost().getExecutionEnvironment(), compilerSet); // NOI18N
                    String source = item.getAbsolutePath();
                    ExecutionEnvironment ee = conf.getDevelopmentHost().getExecutionEnvironment();
                    boolean isWindows = ee.isLocal() && Utilities.isWindows();
                    if (isWindows) {
                        source = source.replace('\\', '/'); // NOI18N
                        source = CppUtils.normalizeDriveLetter(compilerSet, source);
                        source = "'"+source+"'"; // NOI18N
                    }
                    command = command+" -c "+source; // NOI18N
                    profile.setArgs(command);
                    String compilerPath = convertPath(ccCompiler.getPath(), conf.getDevelopmentHost().getExecutionEnvironment());
                    if (isWindows) {
                        try {
                            HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                            compilerPath = compilerPath.replace('\\', '/'); // NOI18N
                            command = escapeQuotes(command);
                            profile.setArgs(new String[]{"-c", "\"'"+compilerPath+"' "+command+"\""}); // NOI18N
                            Shell shell = WindowsSupport.getInstance().getActiveShell();
                            String shellPath = hostInfo.getShell();
                            if (compilerSet.getCompilerFlavor().isMinGWCompiler() && shell != null && shell.type == Shell.ShellType.CYGWIN) {
                                Tool make = compilerSet.findTool(PredefinedToolKind.MakeTool);
                                if (make != null) {
                                    String path = make.getPath();
                                    String dir = CndPathUtilities.getDirName(path);
                                    if (dir != null && !dir.isEmpty()) {
                                        shellPath = dir+"/sh.exe"; // NOI18N
                                    }
                                }
                            }
                            if (shellPath != null) {
                                shellPath = shellPath.replace('\\', '/'); // NOI18N
                                compilerPath = shellPath;
                            }
                        } catch (IOException ex) {
                            return false;
                        } catch (CancellationException ex) {
                            return false;
                        }
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, compilerPath, conf, profile, true, context);
                    actionEvents.add(projectActionEvent);
                    return true;
                }
            }
        } else {
            // user command
            String command = compileConfiguration.getCompileCommand().getValue();
            if (command.contains(CompileConfiguration.AUTO_ITEM_PATH)) {
                command = command.replace(CompileConfiguration.AUTO_ITEM_PATH, item.getAbsolutePath());
            }
            if (command.contains(CompileConfiguration.AUTO_ITEM_NAME)) {
                String name = item.getName();
                if (name.indexOf('.') > 0) {
                    name = name.substring(0, name.lastIndexOf('.'));
                }
                command = command.replace(CompileConfiguration.AUTO_ITEM_NAME, name);
            }
            if (command.contains(CompileConfiguration.AUTO_MAKE)) {
                String make = "make"; // NOI18N
                Tool makeTool = compilerSet.findTool(PredefinedToolKind.MakeTool);
                if (makeTool != null && makeTool.getPath().length() > 0) {
                    make = makeTool.getPath();
                }
                command = command.replace(CompileConfiguration.AUTO_MAKE, make);
            }
            String workingDir = compileConfiguration.getCompileCommandWorkingDir().getValue();
            if (CompileConfiguration.AUTO_FOLDER.equals(workingDir)) {
                workingDir = CndPathUtilities.getDirName(item.getAbsolutePath());
            } else {
                if (!CndPathUtilities.isPathAbsolute(workingDir)) {
                    workingDir = conf.getBaseDir() + "/" + workingDir; // NOI18N
                }
                workingDir = FileSystemProvider.normalizeAbsolutePath(workingDir, conf.getSourceFileSystem());
            }
            RunProfile profile = new RunProfile(workingDir, conf.getDevelopmentHost().getBuildPlatform(), conf);
            profile.setRunDirectory(workingDir);
            profile.setRunCommand(new ComboStringConfiguration(null, command, new DefaultPicklistModel()));
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, workingDir, conf, profile, true, context);
            actionEvents.add(projectActionEvent);
            return true;
        }
        return false;
    }

    private String escapeQuotes(String command) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '\"') { // NOI18N
                buf.append("\\\""); // NOI18N
            } else if (c == '\\') { // NOI18N
                buf.append("\\\\"); // NOI18N
            } else {
                buf.append(c);
            }
        }
        command = buf.toString();
        return command;
    }
    
    private String getDevNull(ExecutionEnvironment execEnv, CompilerSet compilerSet) {
        if (execEnv.isLocal() && Utilities.isWindows()){
            if (!compilerSet.getCompilerFlavor().isCygwinCompiler()) {
                return "NUL"; // NOI18N
            }
        }
        return "/dev/null"; // NOI18N
    }
    
    private String convertPath(String path, ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            return LinkSupport.resolveWindowsLink(path);
        }
        return path;
    }

    private void compileSingleManage(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, MakeArtifact makeArtifact, Lookup context, Type actionEvent, MakeConfigurationDescriptor pd, String outputFile) {
        // Clean command
        String commandLine;
        String args;
        if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
            commandLine = "cmd.exe"; // NOI18N
            args = "/c rm -rf " + outputFile; // NOI18N
        } else {
            commandLine = "rm"; // NOI18N
            args = "-rf " + outputFile; // NOI18N
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, ProjectActionEvent.PredefinedType.CLEAN, commandLine, conf, profile, true);
        actionEvents.add(projectActionEvent);
        // Build commandLine
        commandLine = getMakeCommand(pd, conf) + " -f nbproject" + '/' + "Makefile-" + conf.getName() + ".mk " + outputFile; // Unix path // NOI18N
        args = ""; // NOI18N
        int index = commandLine.indexOf(' '); // NOI18N
        if (index > 0) {
            args = commandLine.substring(index + 1);
            commandLine = commandLine.substring(0, index);
        }
        profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        projectActionEvent = new ProjectActionEvent(project, actionEvent, commandLine, conf, profile, true, context);
        actionEvents.add(projectActionEvent);
    }

    private boolean onTestSingleStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {

        if (actionEvent == ProjectActionEvent.PredefinedType.TEST) {
            if (conf.isCompileConfiguration() && !validateProject(conf)) {
                return true;
            }

            Folder targetFolder = context.lookup(Folder.class);
            if (targetFolder == null) {
                return true;
            }

            List<Folder> list = targetFolder.getAllTests();
            if (targetFolder.isTest()) {
                list.add(targetFolder);
            }

            for (Folder folder : list) {
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                String target = folder.getFolderConfiguration(conf).getLinkerConfiguration().getOutputValue();
                target = conf.expandMacros(target);

                MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                String buildCommand;
                buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf), "test TEST=" + target); // NOI18N
                String args = "";
                int index = getArgsIndex(buildCommand);
                if (index > 0) {
                    args = buildCommand.substring(index + 1);
                    buildCommand = removeQuotes(buildCommand.substring(0, index));
                }
                RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
                profile.setArgs(args);
                ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
                actionEvents.add(projectActionEvent);

                break;
            }
        }

        return true;
    }

    private boolean onCustomActionStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Lookup context, Type actionEvent) {
        String exe = conf.getAbsoluteOutputValue();
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, exe, conf, null, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onValidateToolchainStep(MakeConfigurationDescriptor pd, MakeConfiguration conf, CanceledState cancelled, AtomicBoolean validated, Lookup context) {
        if (!validateBuildSystem(pd, conf, validated.get(), cancelled)) {
            return false; // Stop here
        }
        validated.set(true);
        return true;
    }

    private boolean validateProject(MakeConfiguration conf) {
        boolean ret = false;

        if (getProjectDescriptor().getProjectItems().length == 0) {
            ret = false;
        } else {
            for (int i = 0; i < getProjectDescriptor().getProjectItems().length; i++) {
                Item item = getProjectDescriptor().getProjectItems()[i];
                ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                if (itemConfiguration != null && !itemConfiguration.getExcluded().getValue()
                        && (itemConfiguration.getTool() != PredefinedToolKind.CustomTool || itemConfiguration.getCustomToolConfiguration().getCommandLine().getValue().length() > 0)) {
                    ret = true;
                    break;
                }
            }
        }

        if (!ret) {
            CndNotifier.getDefault().notifyErrorLater(getString("ERR_EMPTY_PROJECT"));
        }

        return ret;
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    private String[] getTargetNames(String command, MakeConfiguration conf, Lookup context) throws IllegalArgumentException {
        if (conf == null) {
            return null;
        }
        final Commands cmds = commands.get(command);
        if (cmds == null) {
            throw new IllegalArgumentException(command);
        }
        return cmds.getCommands(project, conf, context);
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        if (!isProjectDescriptorLoaded()) {
            return false;
        }
        MakeConfiguration conf = getProjectDescriptor().getActiveConfiguration();
        if (conf == null) {
            return false;
        }
        if (command.equals(COMMAND_CLEAN)) {
            return true;
        } else if (command.equals(COMMAND_PRE_BUILD)) {
            return conf.isMakefileConfiguration() && !conf.getPreBuildConfiguration().getPreBuildCommand().getValue().isEmpty();
        } else if (command.equals(COMMAND_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_BUILD_PACKAGE)) {
            return true;
        } else if (command.equals(COMMAND_BATCH_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_REBUILD)) {
            return true;
        } else if (command.equals(COMMAND_RUN)) {
            return true;//!conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG)) {
            return conf.hasDebugger() /*&& !conf.isLibraryConfiguration()*/;
        } else if (command.equals(COMMAND_DEBUG_STEP_INTO)) {
            return conf.hasDebugger() /*&& !conf.isLibraryConfiguration()*/;
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            return isCompileEnable(context, conf);
        } else if (command.equals(COMMAND_DELETE)
                || command.equals(COMMAND_RENAME)) {
            return true;
        } else if (command.equals(COMMAND_COPY)
                || command.equals(COMMAND_MOVE)) {
            // disable for full remote projects
            File file = FileUtil.toFile(project.getProjectDirectory());
            return file != null;
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            return RunActionSupport.geRunActionSupport().canRun(context);
        } else if (command.equals(COMMAND_TEST)) {
            Folder root = getProjectDescriptor().getLogicalFolders();
            Folder testRootFolder = null;
            for (Folder folder : root.getFolders()) {
                if (folder.isTestRootFolder()) {
                    testRootFolder = folder;
                    break;
                }
            }
            return testRootFolder != null;
        } else {
            return false;
        }
    }

    private boolean isCompileEnable(Lookup context, MakeConfiguration conf) {
        Item item = getNoteItem(context);
        if (item == null) {
            return false;
        }
        ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);//ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return false;
        }
        if (itemConfiguration.getExcluded().getValue()) {
            CodeAssistance ca = Lookup.getDefault().lookup(CodeAssistance.class);
            if (ca.hasCodeAssistance(item)) {
                // included header
                List<NativeFileItem> listCU = ca.findHeaderCompilationUnit(item);
                for(NativeFileItem i : listCU) {
                    if (Objects.equals(item.getNativeProject(), i.getNativeProject()) &&
                       (i instanceof Item)) {
                        if (checkItemCompileConfuguration((Item) i, conf)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified()) {
            CodeAssistance ca = Lookup.getDefault().lookup(CodeAssistance.class);
            if (ca != null && ca.hasCodeAssistance(item)) {
                // included header
                List<NativeFileItem> listCU = ca.findHeaderCompilationUnit(item);
                for(NativeFileItem i : listCU) {
                    if (Objects.equals(item.getNativeProject(), i.getNativeProject()) &&
                       (i instanceof Item)) {
                        if (checkItemCompileConfuguration((Item) i, conf)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        if (conf.isMakefileConfiguration()) {
            //AllOptionsProvider options = CompileOptionsProvider.getDefault().getOptions(item);
            //if (options != null) {
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                AbstractCompiler ccCompiler = null;
                if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                    ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                    ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                } 
                if (ccCompiler == null) {
                    return false;
                }
                return true;
            //}
            //return false;
        }
        return true;
    }
    
    private boolean checkItemCompileConfuguration(Item item, MakeConfiguration conf) {
        ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
        if (itemConfiguration == null) {
            return false;
        }
        if (itemConfiguration.getExcluded().getValue()) {
            return false;
        }
        if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified()) {
            return false;
        }
        if (conf.isMakefileConfiguration()) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            AbstractCompiler ccCompiler = null;
            if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
            } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
            } 
            if (ccCompiler == null) {
                return false;
            }
            return true;
        }
        return true;
    }
    
    private Item getNoteItem(Lookup context) {
        FileObject fo = context.lookup(FileObject.class);
        if (fo != null) {
            return getProjectDescriptor().findItemByFileObject(fo);
        }
        return null;
    }

    private static String getMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = cs.getTool(PredefinedToolKind.MakeTool).getPath();
        } else {
            CndUtils.assertFalse(true, "Null compiler collection"); //NOI18N
            cmd = "make"; // NOI18N
        }
        //cmd = cmd + " " + MakeOptions.getInstance().getMakeOptions(); // NOI18N
        return cmd;
    }

    private static String getCMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = PreBuildSupport.getCmakePath(cs);
        } else {
            CndUtils.assertFalse(true, "Null compiler collection"); //NOI18N
            cmd = "cmake"; // NOI18N
        }
        return cmd;
    }
    private List<String> validateStep(String id, List<String> tailSteps) {
        StepController validator = StepControllerProvider.getController(id);
        if (validator == null) {
            return null;
        }
        return validator.validate(project, id, tailSteps);
    }

    private boolean validateBuildSystem(MakeConfigurationDescriptor pd, MakeConfiguration conf,
            boolean validated, CanceledState cancelled) {
        CompilerSet2Configuration csconf = conf.getCompilerSet();
        ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(conf.getDevelopmentHost().getHostKey());
        ArrayList<String> errs = new ArrayList<>();
        CompilerSet cs;
        String csname;
        File file;
        boolean cRequired = conf.hasCFiles(pd);
        boolean cppRequired = conf.hasCPPFiles(pd);
        boolean fRequired = conf.hasFortranFiles(pd);
        boolean asRequired = conf.hasAssemblerFiles(pd);
        boolean runBTA = false;

        if (validated) {
            return lastValidation;
        }

        if (!conf.getDevelopmentHost().isLocalhost()) {
            ServerRecord record = ServerList.get(env);
            assert record != null;
            record.validate(false);
            if (cancelled.isCanceled()) {
                return false;
            }
            if (!record.isOnline()) {
                lastValidation = false;
                runBTA = true;
            }
            // TODO: all validation below works, but it may be more efficient to make a verifying script
        }

        // Check build/run/debug platform vs. host platform
        int buildPlatformId = conf.getDevelopmentHost().getBuildPlatform();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        int hostPlatformId = CompilerSetManager.get(execEnv).getPlatform();
        if (buildPlatformId != hostPlatformId) {
            if (!conf.isMakefileConfiguration()) {
                Platform buildPlatform = Platforms.getPlatform(buildPlatformId);
                Platform hostPlatform = Platforms.getPlatform(hostPlatformId);
                String errormsg = getString("WRONG_PLATFORM", hostPlatform.getDisplayName(), buildPlatform.getDisplayName());
                String autoConfirm = errormsg + "\n (build platform id =" + buildPlatformId + " host platform id = " + hostPlatformId + ")"; //NOI18N
                if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                    new Exception(autoConfirm).printStackTrace(System.err);
                } else {
                    ConfirmSupport.ConfirmPlatformMismatch confirm = ConfirmSupport.getConfirmPlatformMismatchFactory().createAndWait(errormsg, autoConfirm);
                    if (confirm == null) {
                        return false;
                    }
                }
            }
            conf.getDevelopmentHost().setBuildPlatform(hostPlatformId);
        }

        cs = csconf.getCompilerSet();
        csname = csconf.getOption();
        if (cs == null) {
            if (csconf.getFlavor() != null && csconf.getFlavor().equals("Unknown")) { // NOI18N
                // Confiiguration was created with unknown tool set. Use the now default one.
                cs = CompilerSetManager.get(env).getDefaultCompilerSet();
                // NB: cs == null still possible (see #219798), although I don't know how to reproduce
                String errMsg = NbBundle.getMessage(MakeActionProviderImpl.class, "ERR_UnknownCompiler", csname);
                errs.add(errMsg);
                runBTA = true;
            } else {
                CompilerFlavor flavor = null;
                if (csconf.getFlavor() != null) {
                    flavor = CompilerFlavor.toFlavor(csconf.getFlavor(), conf.getPlatformInfo().getPlatform());
                }
                if (flavor == null) {
                    flavor = CompilerFlavor.getUnknown(conf.getPlatformInfo().getPlatform());
                }
                cs = CompilerSetFactory.getCompilerSet(env, flavor, csname);
                String errMsg = NbBundle.getMessage(MakeActionProviderImpl.class, "ERR_INVALID_LOCAL_COMPILER_SET", csname);
                errs.add(errMsg);
                runBTA = true;
            }
        }

        if (cancelled.isCanceled()) {
            return false;
        }

        if (!runBTA) {
            Tool cTool = cs.getTool(PredefinedToolKind.CCompiler);
            Tool cppTool = cs.getTool(PredefinedToolKind.CCCompiler);
            Tool fTool = cs.getTool(PredefinedToolKind.FortranCompiler);
            Tool asTool = cs.getTool(PredefinedToolKind.Assembler);
            Tool makeTool = cs.getTool(PredefinedToolKind.MakeTool);
            Tool qmakeTool = cs.getTool(PredefinedToolKind.QMakeTool);

            PlatformInfo pi = conf.getPlatformInfo();

            // Check for a valid make program
            if (conf.getDevelopmentHost().isLocalhost()) {
                file = new File(makeTool.getPath());
                if ((!exists(makeTool.getPath(), pi) && Path.findCommand(makeTool.getPath()) == null) || ToolchainUtilities.isUnsupportedMake(file.getPath())) {
                    runBTA = true;
                }
            } else {
                if (!isValidExecutable(makeTool.getPath(), pi)) {
                    runBTA = true;
                }
            }

            // Check compilers
            List<Tool> tools2check = new ArrayList<>();
            if (cRequired) {
                tools2check.add(cTool);
            }
            if (cppRequired) {
                tools2check.add(cppTool);
            }
            if (fRequired) {
                tools2check.add(fTool);
            }
            if (asRequired) {
                tools2check.add(asTool);
            }
            if (conf.isQmakeConfiguration()) {
                tools2check.add(qmakeTool);
            }
            for (Tool tool : tools2check) {
                if (cancelled.isCanceled()) {
                    return false;
                }
                if (!exists(tool.getPath(), pi)) {
                    runBTA = true;
                }
            }
        }

        if (cancelled.isCanceled()) {
            return false;
        }

        // user counting mode
        if (!CndUtils.isUnitTestMode()) {
            OSSComponentUsages.countIDEUsage();
        }
        if (runBTA) {
            if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                // do not show any dialogs in unit test mode, just silently fail validation
                lastValidation = false;
            } else {
                String title = NbBundle.getMessage(MakeActionProviderImpl.class, "LBL_ResolveMissingTools_Title");
                ResolveBuildToolsFactory resolver = Lookup.getDefault().lookup(ResolveBuildToolsFactory.class);
                lastValidation = resolver.resolveTools(title, pd, conf, env, csname, cs, cRequired, cppRequired, fRequired, asRequired, errs);
                if (lastValidation) {
                    cs = conf.getCompilerSet().getCompilerSet();
                }
            }
        } else {
            lastValidation = true;
        }

        if (lastValidation == true) {
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                Shell activeShell = WindowsSupport.getInstance().getActiveShell();
                ShellValidationStatus shellValidationStatus = ShellValidationSupport.getValidationStatus(activeShell);
                boolean isOK = shellValidationStatus.isValid() && !shellValidationStatus.hasWarnings();

                if (!isOK) {
                    String binDir = cs.getDirectory();

                    if (activeShell == null || !binDir.equals(activeShell.bindir.getAbsolutePath())) {
                        // Perhaps one that is provided is better?
                        WindowsSupport.getInstance().init(binDir);
                        activeShell = WindowsSupport.getInstance().getActiveShell();
                        shellValidationStatus = ShellValidationSupport.getValidationStatus(activeShell);
                        isOK = shellValidationStatus.isValid() && !shellValidationStatus.hasWarnings();
                        if (isOK) {
                            try {
                                HostInfoUtils.updateHostInfo(execEnv);
                            } catch (IOException ex) {
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                }

                if (!isOK) {
                    lastValidation = ShellValidationSupport.confirm(shellValidationStatus);
                }
            } else if (cs.getCompilerFlavor().isMinGWCompiler()) {
                Shell activeShell = WindowsSupport.getInstance().getActiveShell();
                if (activeShell == null) {
                    String binDir = cs.getCommandFolder();
                    // Perhaps one that is provided is better?
                    WindowsSupport.getInstance().init(binDir);
                    activeShell = WindowsSupport.getInstance().getActiveShell();
                    if (activeShell == null) {
                        lastValidation = ShellValidationSupport.confirm(null);
                    } else {
                        try {
                            HostInfoUtils.updateHostInfo(execEnv);
                        } catch (IOException ex) {
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }

        return lastValidation;
    }

    private boolean validatePackaging(MakeConfiguration conf) {
        String errormsg = null;

        if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
            errormsg = getString("ERR_EMPTY_PACKAGE");
        }

        if (PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue()) == null) {
            errormsg = NbBundle.getMessage(MakeActionProviderImpl.class, "ERR_MISSING_TOOL4", conf.getPackagingConfiguration().getType().getValue()); // NOI18N
        }

        // Don;t verify the tool. It is too difficult to get right (148728)
//        if (errormsg == null) {
//            String tool = conf.getPackagingConfiguration().getToolValue();
//            if (conf.getDevelopmentHost().isLocalhost()) {
//                if (!CndPathUtilities.isPathAbsolute(tool) && Path.findCommand(tool) == null) {
//                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL1", tool); // NOI18N
//                } else if (CndPathUtilities.isPathAbsolute(tool) && !(new File(tool).exists())) {
//                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL2", tool); // NOI18N
//                }
//            } else {
//                ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
//                ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
//                if (serverList != null) {
//                    if (!serverList.isValidExecutable(execEnv, tool)) {
//                        errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL3", tool, execEnv.getHost()); // NOI18N
//                    }
//                }
//            }
//        }

        if (errormsg != null) {
            CndNotifier.getDefault().notifyError(errormsg);
            if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
                MakeCustomizerProvider makeCustomizerProvider = project.getLookup().lookup(MakeCustomizerProvider.class);
                if (makeCustomizerProvider != null) {
                    makeCustomizerProvider.showCustomizer("Packaging"); // NOI18N
                }
            }
            return false;
        }

        return true;
    }
    /** cache for file existence status. */
    private static final Map<String, Boolean> fileExistenceCache = new HashMap<>();
    /** cache for valid executables */
    private static final Map<String, Boolean> validExecutablesCache = new HashMap<>();

    private static boolean isValidExecutable(String path, PlatformInfo pi) {
        return existsImpl(path, pi, true);
    }

    private static boolean exists(String path, PlatformInfo pi) {
        return existsImpl(path, pi, false);
    }

    private static boolean existsImpl(String path, PlatformInfo pi, boolean checkExecutable) {
        ExecutionEnvironment execEnv = pi.getExecutionEnvironment();
        String key = path + ExecutionEnvironmentFactory.toUniqueID(execEnv);
        Map<String, Boolean> map = checkExecutable ? validExecutablesCache : fileExistenceCache;

        synchronized (map) {
            Boolean cached = map.get(key);
            if (cached != null && cached) {
                return true;
            }
        }

        boolean result;
        if (checkExecutable) {
            result = ServerList.isValidExecutable(execEnv, path);
        } else {
            result = pi.fileExists(path) || pi.isWindows() && pi.fileExists(path + ".lnk") || pi.findCommand(path) != null; // NOI18N
        }

        if (result) {
            synchronized (map) {
                map.put(key, Boolean.TRUE);
            }
        }
        return result;
    }

    private String removeQuotes(String command) {
        if (command.startsWith("\"") && command.endsWith("\"")) { // NOI18N
            return command.substring(1, command.length() - 1);
        } else if (command.startsWith("'") && command.endsWith("'")) { // NOI18N
            return command.substring(1, command.length() - 1);
        }
        return command;
    }

    private int getArgsIndex(String command) {
        boolean inQuote = false;
        int quote = 0;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            switch (c) {
                case ' ':
                    if (!inQuote) {
                        return i;
                    }
                    break;
                case '\'':
                case '"':
                    if (inQuote) {
                        if (quote == c) {
                            quote = 0;
                            inQuote = false;
                        }
                    } else {
                        quote = c;
                        inQuote = true;
                    }
                    break;
            }
        }
        return -1;
    }

    // Private methods -----------------------------------------------------
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeActionProviderImpl.class, s);
    }

    private static String getString(String s, String arg1, String arg2) {
        return NbBundle.getMessage(MakeActionProviderImpl.class, s, arg1, arg2);
    }

    private static final class MyType implements Type {

        private final String extendedStep;
        private String locName;

        private MyType(String extendedStep) {
            this.extendedStep = extendedStep;
            locName = extendedStep;
        }

        @Override
        public int ordinal() {
            return extendedStep.hashCode();
        }

        @Override
        public String name() {
            return extendedStep;
        }

        @Override
        public String getLocalizedName() {
            return locName;
        }

        @Override
        public void setLocalizedName(String name) {
            locName = name;
        }
    }
    
    private static final class Command {
        private final String command;
        private final String conditions;
        private Command(String command, String conditions) {
            this.command = command;
            this.conditions = conditions;
        }

        @Override
        public String toString() {
            return conditions == null ? command : command+"["+conditions+"]"; // NOI18N
        }
    }
    
    private static final class Commands {
        private final List<Command> commands;
        private final String id;
        private Commands(String id, Collection<Command> commands) {
            this.id = id;
            this.commands = new ArrayList<>(commands);
        }
        
        private String[] getCommands(Project project, MakeConfiguration conf, Lookup context) {
            List<String> res = new ArrayList<>();
            Loop:for(Command command : commands) {
                String conditions = command.conditions;
                if (conditions != null) {
                    HashMap<String,Boolean> flagValues = new HashMap<>();
                    for(String condition : conditions.split(",")) { //NOI18N
                        boolean can = false;
                        if (MakeCommandFlagsProviderFactory.DEFAULT.canHandle(condition, context, conf)) {
                            can = MakeCommandFlagsProviderFactory.DEFAULT.createProvider().flagValue(id, context, project, conf, condition, can);
                        }
                        for(MakeCommandFlagsProviderFactory factory : Lookup.getDefault().lookupAll(MakeCommandFlagsProviderFactory.class)) {
                            if (factory.canHandle(id, context, conf)) {
                                can = factory.createProvider().flagValue(id, context, project, conf, condition, can);
                            }
                        }
                        flagValues.put(condition, can);
                    }
                    for(boolean can : flagValues.values()) {
                        if (!can) {
                            continue Loop;
                        }
                    }
                }
                res.add(command.command);
            }
            return res.toArray(new String[res.size()]);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            for(Command command : commands) {
                if (buf.length()>0) {
                    buf.append('\n');
                }
                buf.append(command.command);
                if (command.conditions != null) {
                    buf.append('[').append(command.conditions).append(']');
                }
            }
            return buf.toString();
        }
    }
}
