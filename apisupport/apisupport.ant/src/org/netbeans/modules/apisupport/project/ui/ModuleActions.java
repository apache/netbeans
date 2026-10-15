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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.ExecProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import static org.netbeans.modules.apisupport.project.ui.Bundle.*;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

public final class ModuleActions implements ActionProvider, ExecProject {
    private static final String RUN_ARGS_IDE = "run.args.ide";    // NOI18N

    private static final String COMMAND_NBM = "nbm";
    private static final String MODULE_ACTIONS_TYPE = "org-netbeans-modules-apisupport-project";
    private static final String MODULE_ACTIONS_PATH = "Projects/" + MODULE_ACTIONS_TYPE + "/Actions";

    private static final RequestProcessor RP = new RequestProcessor(ModuleActions.class);

    @Override
    public Task execute(String... args) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String r : args) {
            sb.append(r).append(' ');
        }
        Properties p = new Properties();
        p.setProperty("run.args", sb.substring(0, sb.length() - 1));

        return ActionUtils.runTarget(findBuildXml(project), new String[]{"run"}, p);
    }
    
    static Action[] getProjectActions(NbModuleProject project) {
        return CommonProjectActions.forType(MODULE_ACTIONS_TYPE);
    }
    
    private final NbModuleProject project;
    private final Map<String,String[]> globalCommands = new HashMap<String,String[]>();
    private String[] supportedActions = null;
    
    public ModuleActions(NbModuleProject project) {
        this.project = project;
        refresh();
    }

    public void refresh() {
        Set<String> supportedActionsSet = new HashSet<String>();
        globalCommands.put(ActionProvider.COMMAND_BUILD, new String[] {"build"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "build"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_RUN, new String[] {"run"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        globalCommands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc-nb"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_TEST, new String[] {"test-unit"}); // NOI18N
        globalCommands.put(COMMAND_NBM, new String[] {COMMAND_NBM});
        supportedActionsSet.addAll(globalCommands.keySet());
        supportedActionsSet.add(ActionProvider.COMMAND_COMPILE_SINGLE);
        supportedActionsSet.add(JavaProjectConstants.COMMAND_DEBUG_FIX); // #47012
        if (!project.supportedTestTypes().isEmpty()) {
            supportedActionsSet.add(ActionProvider.COMMAND_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_PROFILE_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_PROFILE_SINGLE);
            supportedActionsSet.add(SingleMethod.COMMAND_RUN_SINGLE_METHOD);
            supportedActionsSet.add(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD);
        }
        supportedActionsSet.add(ActionProvider.COMMAND_RENAME);
        supportedActionsSet.add(ActionProvider.COMMAND_MOVE);
        supportedActionsSet.add(ActionProvider.COMMAND_COPY);
        supportedActionsSet.add(ActionProvider.COMMAND_DELETE);
        supportedActions = supportedActionsSet.toArray(new String[0]);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    private static FileObject findBuildXml(NbModuleProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public boolean isActionEnabled(String command, Lookup context) {
        if (ActionProvider.COMMAND_DELETE.equals(command) ||
                ActionProvider.COMMAND_RENAME.equals(command) ||
                ActionProvider.COMMAND_MOVE.equals(command) ||
                ActionProvider.COMMAND_COPY.equals(command)) {
            return true;
        } else if (findBuildXml(project) == null) {
            // All other actions require a build script.
            return false;
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            return findSources(context) != null || findTestSources(context, true) != null;
        } else if (command.equals(COMMAND_TEST)) {
            return project.supportedTestTypes().contains("unit");
        } else if (command.equals(COMMAND_TEST_SINGLE)) {
            return findTestSourcesForSources(context) != null || findTestSources(context, true) != null || findTestSourcesForFiles(context) != null;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            TestSources testSources = findTestSourcesForSources(context);
            if (testSources == null)
                    testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            TestSources testSources = findTestSourcesForSources(context);
            if (testSources == null)
                    testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            return findTestSources(context, false) != null;
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(COMMAND_PROFILE_SINGLE)) {
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || 
                   command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)
                ) {
            NbPlatform plaf = project.getPlatform(false);
            if (plaf == null || plaf.getHarnessVersion().compareTo(HarnessVersion.V70) < 0) {
                return false;
            }
            return findTestMethodSources(context) != null;
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findSources(context);
            if (files != null && files.length == 1) {
                return true;
            }
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else {
            // other actions are global
            return true;
        }
    }
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    private static final String SUBSTNG = "NGTest.java"; // NOI18N
    
    private FileObject[] findSources(Lookup context) {
        return findSources(context, false, true);
    }
    
    private FileObject[] findSources(Lookup context, boolean findInPackages, boolean strict) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, findInPackages ? null : ".java", strict); // NOI18N
            //System.err.println("findSources: srcDir=" + srcDir + " files=" + (files != null ? java.util.Arrays.asList(files) : null) + " context=" + context);
            return files;
        } else {
            return null;
        }
    }
    
    static class TestSources {
        private final @NonNull FileObject[] sources;
        final @NonNull String testType;
        private final @NonNull FileObject sourceDirectory;
        final @NullAllowed String method;
        TestSources(@NonNull FileObject[] sources, @NonNull String testType, @NonNull FileObject sourceDirectory, String method) {
            this.sources = sources;
            this.testType = testType;
            this.sourceDirectory = sourceDirectory;
            this.method = method;
        }
        boolean isSingle() {
            return sources.length == 1;
        }
        @NonNull String includes() {
            return ActionUtils.antIncludesList(sources, sourceDirectory);
        }
        @Override public String toString() {
            return testType + ":" + includes() + (method != null ? ("#" + method) : "");
        }
    }
    @CheckForNull TestSources findTestSources(@NonNull Lookup context, boolean allowFolders) {
        TYPE: for (String testType : project.supportedTestTypes()) {
            FileObject testSrcDir = project.getTestSourceDirectory(testType);
            if (testSrcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, null, true);
                if (files != null) {
                    for (FileObject file : files) {
                        if (!(file.hasExt("java") || allowFolders && file.isFolder())) {
                            break TYPE;
                        }
                    }
                    return new TestSources(files, testType, testSrcDir, null);
                }
            }
        }
        return null;
    }
    
    @CheckForNull private FileObject[] findTestSourcesFOs(@NonNull Lookup context, boolean allowFolders, boolean strict) {
        TYPE: for (String testType : project.supportedTestTypes()) {
            FileObject testSrcDir = project.getTestSourceDirectory(testType);
            if (testSrcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, null, strict);
                if (files != null) {
                    for (FileObject file : files) {
                        if (!(file.hasExt("java") || allowFolders && file.isFolder())) {
                            break TYPE;
                        }
                    }
                    return files;
                }
            }
        }
        return null;
    }
    
    @CheckForNull private TestSources findTestMethodSources(@NonNull Lookup context) {
        SingleMethod meth = context.lookup(SingleMethod.class);
        if (meth != null) {
            FileObject file = meth.getFile();
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    if (FileUtil.isParentOf(testSrcDir, file)) {
                        return new TestSources(new FileObject[] {file}, testType, testSrcDir, meth.getMethodName());
                    }
                }
            }
        }
        return null;
    }

    private String getMainClass(Lookup context) {
        FileObject[] files = ActionUtils.findSelectedFiles(context, null, ".java", true); // NOI18N
        if (files.length == 1) {
            FileObject f = files[0];
            Collection<ElementHandle<TypeElement>> mcs = SourceUtils.getMainClasses(f);
            if (mcs.size() > 0) {
                ElementHandle<TypeElement> h = mcs.iterator().next();
                String qname = h.getQualifiedName();
                return qname;
            }
        }
        return null;
    }

    
    /** Find tests corresponding to selected sources.
     */
    TestSources findTestSourcesForSources(Lookup context) {
        String testType = "unit"; // NOI18N
        FileObject[] sourceFiles = findSources(context);
        if (sourceFiles == null) {
            // no source file selected. try folders
            sourceFiles = findSources(context, true, true);
            if (sourceFiles == null) {
                return null;
            }
        }
        FileObject testSrcDir = project.getTestSourceDirectory(testType);
        if (testSrcDir == null) {
            return null;
        }
        FileObject srcDir = project.getSourceDirectory();
        FileObject[] matches = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
        if (matches != null) {
            return new TestSources(matches, testType, testSrcDir, null);
        } else {
            matches = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcDir, SUBSTNG, true);
            if (matches != null) {
                return new TestSources(matches, testType, testSrcDir, null);
            } else {
                // no test files found. The selected FOs must be folders under source packages
                ArrayList<FileObject> testFOs = new ArrayList<FileObject>();
                for (FileObject file : sourceFiles) {
                    if (file.isFolder()) {
                        String relativePath = FileUtil.getRelativePath(srcDir, file);
                        if (relativePath != null && !relativePath.isEmpty()) {
                            FileObject testFO = FileUtil.toFileObject(new File(FileUtil.toFile(testSrcDir).getPath().concat(File.separator).concat(relativePath)));
                            if (testFO != null && testFO.getChildren().length != 0) {
                                testFOs.add(testFO);
                            }
                        }
                    }
                }
                if (testFOs.isEmpty()) {
                    return null;
                }
                return new TestSources(testFOs.toArray(new FileObject[0]), testType, testSrcDir, null);
            }
        }
    }
    
    /** Find tests corresponding to selected files. 
     * Selected files might be under Source and/or Test Packages
     */
    @CheckForNull
    TestSources findTestSourcesForFiles(Lookup context) {
        String testType = "unit"; // NOI18N
        FileObject[] sourcesFOs = findSources(context, false, false);
        FileObject[] testSourcesFOs = findTestSourcesFOs(context, false, false);
        HashSet<FileObject> testFiles = new HashSet<FileObject>();
        FileObject testRoot = project.getTestSourceDirectory(testType);
        if (testRoot == null) {
            return null;
        }
        if (testSourcesFOs == null) { // no test files were selected
            return findTestSources(context, true); // return tests which belong to selected source files, if any
        } else {
            if (sourcesFOs == null) { // only test files were selected
                return findTestSources(context, false);
            } else { // both test and source files were selected, do not return any dublicates
                testFiles.addAll(Arrays.asList(testSourcesFOs));
                //Try to find the test under the test roots
                FileObject srcRoot = project.getSourceDirectory();
                FileObject[] files2 = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBST, true);
                if (files2 != null) {
                    for (FileObject fo : files2) {
                        if (!testFiles.contains(fo)) {
                            testFiles.add(fo);
                        }
                    }
                }
                FileObject[] files2NG = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBSTNG, true);
                if (files2NG != null) {
                    for (FileObject fo : files2NG) {
                        if (!testFiles.contains(fo)) {
                            testFiles.add(fo);
                        }
                    }
                }
            }
        }
        return testFiles.isEmpty() ? null : new TestSources(testFiles.toArray(new FileObject[0]), testType, testRoot, null);
    }
    
    @Messages("MSG_no_source=No source to operate on.")
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        if (!canRunNoLock(command, project.getTestUserDirLockFile())) {
            return;
        }
        if (ActionProvider.COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        } else if (ActionProvider.COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        } else if (ActionProvider.COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        } else if (ActionProvider.COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }
        if (!verifySufficientlyNewHarness(project)) {
            return;
        }

        // XXX prefer to call just if and when actually starting target, but that is hard to calculate here
        final ActionProgress listener = ActionProgress.start(context);
        Runnable runnable = new Runnable() {
            ExecutorTask task;
            @Override public void run() {
                try {
                    doRun();
                } finally {
                    if (task != null) {
                        task.addTaskListener((Task t) -> listener.finished(task.result() == 0));
                    } else {
                        listener.finished(false);
                    }
                }
            }
            void doRun() {
                Properties p = new Properties();
                String[] targetNames;
                if (command.equals(COMMAND_COMPILE_SINGLE)) {
                    FileObject[] files = findSources(context);
                    if (files != null) {
                        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
                        targetNames = new String[]{"compile-single"}; // NOI18N
                    } else {
                        TestSources testSources = findTestSources(context, true);
                        p.setProperty("javac.includes", testSources.includes()); // NOI18N
                        p.setProperty("test.type", testSources.testType);
                        targetNames = new String[]{"compile-test-single"}; // NOI18N
                    }
                } else if (command.equals(COMMAND_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, true);
                        if (testSources == null) {
                            testSources = findTestSourcesForFiles(context);
                        }
                    }
                    p.setProperty("continue.after.failing.tests", "true");  //NOI18N
                    targetNames = setupTestSingle(p, testSources);
                } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, false);

                    }
                    targetNames = setupDebugTestSingle(p, testSources);
                } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, false);

                    }
                    targetNames = setupProfileTestSingle(p, testSources);
                } else if (command.equals(COMMAND_RUN_SINGLE)) {
                    TestSources testSources = findTestSources(context, false);
//       TODO CoS     String enableQuickTest = project.evaluator().getProperty("quick.test.single"); // NOI18N
//            if (    Boolean.parseBoolean(enableQuickTest)
//                 && "unit".equals(testSources.testType) // NOI18N
//                 && !hasTestUnitDataDir()) { // NOI18N
//                AtomicReference<ExecutorTask> _task = new AtomicReference<ExecutorTask>();
//                if (bypassAntBuildScript(command, testSources.sources, _task)) {
//                    task = _task.get();
//                    return ;
//                }
//            }
                    String clazz = getMainClass(context);
                    if (clazz != null) {
                        targetNames = setupRunMain(p, testSources, context, clazz);
                    } else {
                        // fallback to "old" run tests behavior
                        p.setProperty("continue.after.failing.tests", "true");  //NOI18N
                        targetNames = setupTestSingle(p, testSources);
                    }
                } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
                    TestSources testSources = findTestSources(context, false);
                    String clazz = getMainClass(context);
                    if (clazz != null) {
                        targetNames = setupDebugMain(p, testSources, context, clazz);
                    } else {
                        // fallback to "old" debug tests behavior
                        targetNames = setupDebugTestSingle(p, testSources);
                    }
                } else if (command.equals(COMMAND_PROFILE_SINGLE)) {
                    TestSources testSources = findTestSources(context, false);
                    String clazz = getMainClass(context);
                    if (clazz != null) {
                        targetNames = setupProfileMain(p, testSources, context, clazz);
                    } else {
                        // fallback to "old" debug tests behavior
                        targetNames = setupProfileTestSingle(p, testSources);
                    }
                } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD)) {
                    TestSources testSources = findTestMethodSources(context);
                    p.setProperty("test.class", testClassName(testSources)); // NOI18N
                    p.setProperty("test.type", testSources.testType); // NOI18N
                    p.setProperty("test.methods", testSources.method); // NOI18N
                    p.setProperty("continue.after.failing.tests", "true");  //NOI18N
                    targetNames = new String[] {"test-method"}; // NOI18N
                } else if (command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
                    TestSources testSources = findTestMethodSources(context);
                    p.setProperty("test.class", testClassName(testSources)); // NOI18N
                    p.setProperty("test.type", testSources.testType); // NOI18N
                    p.setProperty("test.methods", testSources.method); // NOI18N
                    targetNames = new String[] {"debug-test-single-nb"}; // NOI18N
                } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
                    FileObject[] files = findSources(context);
                    String path = null;
                    if (files != null) {
                        path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
                        assert path != null;
                        assert path.endsWith(".java");
                        targetNames = new String[]{"debug-fix-nb"}; // NOI18N
                    } else {
                        TestSources testSources = findTestSources(context, false);
                        if (testSources == null) {  // #174147
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(MSG_no_source());
                            DialogDisplayer.getDefault().notify(msg);
                            return;
                        }
                        p.setProperty("test.type", testSources.testType);
                        path = testSources.includes();
                        assert path.endsWith(".java");
                        targetNames = new String[]{"debug-fix-test-nb"}; // NOI18N
                    }
                    String clazzSlash = path.substring(0, path.length() - 5);
                    p.setProperty("fix.class", clazzSlash); // NOI18N
                } else if (command.equals(JavaProjectConstants.COMMAND_JAVADOC) && !project.supportsJavadoc()) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            promptForPublicPackagesToDocument();
                        }
                    });
                    return;
                } else {
                    // XXX consider passing PM.fP(FU.toFO(SuiteUtils.suiteDirectory(project))) instead for a suite component project:
                    if (command.equals(ActionProvider.COMMAND_REBUILD)) {
                        p.setProperty("do.not.clean.module.config.xml", "true"); // #196192
                    }
                    p.setProperty("continue.after.failing.tests", "true");  //NOI18N
                    targetNames = globalCommands.get(command);
                    if (targetNames == null) {
                        throw new IllegalArgumentException(command);
                    }
                    if (command.equals(ActionProvider.COMMAND_BUILD) || command.equals(ActionProvider.COMMAND_REBUILD)) {
                        try {
                            final Set<TargetLister.Target> targets = TargetLister.getTargets(AntScriptUtils.antProjectCookieFor(findBuildXml(project)));
                            TARGETS: for (int i = 0; i < targetNames.length; i++) {
                                for (TargetLister.Target t : targets) {
                                    if (t.getName().equals(targetNames[i])) {
                                        continue TARGETS;
                                    }
                                }
                                // target not found, replacing with default one
                                targetNames[i] = "netbeans"; // NOI18N
                                globalCommands.put(command, targetNames);
                            }
                        } catch (IOException ex) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                }
                try {
                    setRunArgsIde(project, project.evaluator(), command, p);
                    task = ActionUtils.runTarget(findBuildXml(project), targetNames, p);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
        RP.post(runnable);
    }

    /**
     * Checks if a given command can be run at the moment from the perspective of the test userdir lock file.
     * Cf. #63652, #72397, #141069, #207530.
     * @param command as in {@link ActionProvider}
     * @param lock from {@link NbModuleProject#getTestUserDirLockFile} or {@link SuiteProject#getTestUserDirLockFile}
     * @return true if the command is unrelated, there is no lock file, or the lock file is stale and can be safely deleted;
     *         false (after showing a warning dialog) if the command must not proceed
     */
    @Messages({
        "ERR_module_already_running=The application is already running within the test user directory. You must shut it down before trying to run it again.",
        "ERR_ModuleIsBeingRun=Cannot copy/move/rename/delete a module or suite while the application is running; shut it down first."
    })
    static boolean canRunNoLock(String command, File lock) {
        if (command.equals(ActionProvider.COMMAND_RUN) ||
                command.equals(ActionProvider.COMMAND_DEBUG) ||
                command.equals(ActionProvider.COMMAND_PROFILE)) {
            if (isLocked(lock)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERR_module_already_running()));
                return false;
            }
        } else if (ActionProvider.COMMAND_DELETE.equals(command) ||
                ActionProvider.COMMAND_RENAME.equals(command) ||
                ActionProvider.COMMAND_MOVE.equals(command) ||
                ActionProvider.COMMAND_COPY.equals(command)) {
            if (isLocked(lock)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERR_ModuleIsBeingRun()));
                return false;
            }
        }
        return true;
    }
    
    // Cf. org.netbeans.nbbuild.IsLocked
    private static boolean isLocked(File file) {
        if (!file.exists()) {
            return false;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            try {
                FileLock lock = raf.getChannel().tryLock();
                if (lock == null) {
                    return true;
                }
                lock.release();
                return false;
            } finally {
                raf.close();
            }
        } catch (IOException x) {
            return true;
        } catch (OverlappingFileLockException x) {
            return true; // ?
        }
    }

    private String[] setupProfileTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.includes", testSources.includes().replace("**", "**/*Test.java")); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"profile-test-single-nb"}; // NOI18N
    }

    static void setRunArgsIde(Project project, PropertyEvaluator eval, String command, Properties p) {
        StringBuilder runArgsIde = new StringBuilder();
        StartupExtender.StartMode mode;
        boolean isOsgi = command.equals("profile-osgi");
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE) || command.equals("profile-osgi")) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            mode = null;
        }
        
        boolean isTest = (EnumSet.of(
                            StartupExtender.StartMode.TEST_PROFILE, 
                            StartupExtender.StartMode.TEST_NORMAL, 
                            StartupExtender.StartMode.TEST_DEBUG).contains(mode) || 
                          command.equals(COMMAND_PROFILE_SINGLE));
        if (mode != null) {
            JavaPlatform plaf = ModuleProperties.getJavaPlatform(eval);
            Lookup context = Lookups.fixed(project, plaf != null ? plaf : JavaPlatformManager.getDefault().getDefaultPlatform());
            for (StartupExtender group : StartupExtender.getExtenders(context, mode)) {
                for (String arg : group.getArguments()) {
                    runArgsIde.append((isTest | isOsgi) ? "" : "-J").append(arg).append(' ');
                }
            }
        }
        if (runArgsIde.length() > 0) {
            p.setProperty(RUN_ARGS_IDE, runArgsIde.toString());
        }
    }

    @Messages({
        "TITLE_javadoc_disabled=No Public Packages",
        "ERR_javadoc_disabled=<html>Javadoc cannot be produced for this module.<br>It is not yet configured to export any packages to other modules.",
        "LBL_configure_pubpkg=Configure Public Packages..."
    })
    private void promptForPublicPackagesToDocument() {
        // #61372: warn the user, rather than disabling the action.
        if (ApisupportAntUIUtils.showAcceptCancelDialog(
                TITLE_javadoc_disabled(),
                ERR_javadoc_disabled(),
                LBL_configure_pubpkg(),
                null,
                NotifyDescriptor.WARNING_MESSAGE)) {
            CustomizerProviderImpl cpi = project.getLookup().lookup(CustomizerProviderImpl.class);
            cpi.showCustomizer(CustomizerProviderImpl.CATEGORY_VERSIONING, CustomizerProviderImpl.SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES);
        }
    }

    private boolean hasTestUnitDataDir() {
        String dataDir = project.evaluator().getProperty("test.unit.data.dir");
        return dataDir != null && project.getHelper().resolveFileObject(dataDir) != null;
    }
    
    private static final String SYSTEM_PROPERTY_PREFIX = "test-unit-sys-prop.";
    
    private void prepareSystemProperties(Map<String, Object> properties) {
        Map<String, String> evaluated = project.evaluator().getProperties();

        if (evaluated == null) {
            return ;
        }
        
        for (Entry<String, String> e : evaluated.entrySet()) {
            if (e.getKey().startsWith(SYSTEM_PROPERTY_PREFIX) && e.getValue() != null) {
                @SuppressWarnings("unchecked")
                Collection<String> systemProperties = (Collection<String>) properties.get(JavaRunner.PROP_RUN_JVMARGS);

                if (systemProperties == null) {
                    properties.put(JavaRunner.PROP_RUN_JVMARGS, systemProperties = new LinkedList<String>());
                }

                systemProperties.add("-D" + e.getKey().substring(SYSTEM_PROPERTY_PREFIX.length()) + "=" + e.getValue());
            }
        }
    }

    private static boolean verifySufficientlyNewHarness(NbModuleProject project) {
        NbPlatform plaf = project.getPlatform(false);
        if (plaf != null && plaf.getHarnessVersion() != HarnessVersion.UNKNOWN &&
                plaf.getHarnessVersion().compareTo(project.getMinimumHarnessVersion()) < 0) {
            promptForNewerHarness();
            return false;
        } else {
            return true;
        }
    }
    @Messages({
        "ERR_harness_too_old=You are attempting to build a module or suite project which uses a new metadata format with an old version of the module build harness which does not understand this format. You may either choose a newer NetBeans platform, or switch the harness used by the selected platform to use a newer harness (try using the harness supplied with the IDE).",
        "TITLE_harness_too_old=Harness Too Old"
    })
    static void promptForNewerHarness() {
        // #82388: warn the user that the harness version is too low.
        NotifyDescriptor d = new NotifyDescriptor.Message(ERR_harness_too_old(), NotifyDescriptor.ERROR_MESSAGE);
        d.setTitle(TITLE_harness_too_old());
        DialogDisplayer.getDefault().notify(d);
    }
    
    private String[] setupTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.includes", testSources.includes().replace("**", "**/*Test.java")); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupRunMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return  new String[] {"run-test-main"};    // NOI18N
    }

    private String[] setupDebugMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return  new String[] {"debug-test-main-nb"};    // NOI18N
    }
    
    private String[] setupProfileMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return  new String[] {"profile-test-main-nb"};    // NOI18N
    }
    
    private String testClassName(TestSources testSources) {
        String path = testSources.includes();
        assert path.endsWith(".java") && !path.contains(",") : path;
        // Convert foo/FooTest.java -> foo.FooTest
        return path.substring(0, path.length() - 5).replace('/', '.'); // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.class", testClassName(testSources)); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"debug-test-single-nb"}; // NOI18N
    }
    
    private boolean bypassAntBuildScript(String command, FileObject[] files, AtomicReference<ExecutorTask> task) throws IllegalArgumentException {
        FileObject toRun = null;

        if (COMMAND_RUN_SINGLE.equals(command) || 
            COMMAND_DEBUG_SINGLE.equals(command) ||
            COMMAND_PROFILE_SINGLE.equals(command)) {
            toRun = files[0];
        }
        
        if (toRun != null) {
            String commandToExecute = COMMAND_RUN_SINGLE.equals(command) ? 
                                        JavaRunner.QUICK_TEST : 
                                        (COMMAND_DEBUG_SINGLE.equals(command) ? 
                                            JavaRunner.QUICK_TEST_DEBUG :
                                            JavaRunner.QUICK_TEST_PROFILE);
            if (!JavaRunner.isSupported(commandToExecute, Collections.singletonMap(JavaRunner.PROP_EXECUTE_FILE, toRun))) {
                return false;
            }
            try {
                Map<String, Object> properties = new HashMap<String, Object>();

                prepareSystemProperties(properties);

                properties.put(JavaRunner.PROP_EXECUTE_FILE, toRun);

                task.set(JavaRunner.execute(commandToExecute, properties));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return true;
        }

        return false;
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.reload")
    @ActionRegistration(displayName="#ACTION_reload", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1400)
    @Messages("ACTION_reload=Reload in Target Platform")
    public static Action reload() {
        return reload(false);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.reloadInIde")
    @ActionRegistration(displayName="#ACTION_reload_in_ide", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1500)
    @Messages("ACTION_reload_in_ide=Install/Reload in Development IDE")
    public static Action reloadInIde() {
        return reload(true);
    }

    @Messages({
        "LBL_reload_in_ide_confirm=<html>Reloading a module in the running development IDE can be dangerous.<br>Errors in the module could corrupt your environment and force you to use a new user directory.<br>(In most cases it is wiser to use <b>Run</b> or <b>Reload in Target Platform</b>.)<br>Do you really want to reload this module in your own IDE?",
        "LBL_reload_in_ide_confirm_title=Confirm Install/Reload in Development IDE"
    })
    private static Action reload(final boolean inIDE) {
        return ProjectSensitiveActions.projectSensitiveAction(new ProjectActionPerformer() {
            @Override public boolean enable(Project _project) {
                if (!(_project instanceof NbModuleProject)) {
                    return false;
                }
                NbModuleProject project = (NbModuleProject) _project;
                if (findBuildXml(project) == null) {
                    return false;
                }
                if (!inIDE) {
                    return project.getTestUserDirLockFile().isFile();
                }
                if (Boolean.parseBoolean(project.evaluator().getProperty("is.autoload")) || Boolean.parseBoolean(project.evaluator().getProperty("is.eager"))) {
                    return false; // #86395 but #208415
                }
                NbModuleType type = project.getModuleType();
                if (type == NbModuleType.NETBEANS_ORG) {
                    return true;
                } else if (type == NbModuleType.STANDALONE) {
                    NbPlatform p = project.getPlatform(false);
                    return p != null && p.isDefault();
                } else {
                    assert type == NbModuleType.SUITE_COMPONENT : type;
                    try {
                        SuiteProject suite = SuiteUtils.findSuite(project);
                        if (suite == null) {
                            return false;
                        }
                        NbPlatform p = suite.getPlatform(false);
                        if (/* #67148 */p == null || !p.isDefault()) {
                            return false;
                        }
                        return SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.ENABLED_CLUSTERS_PROPERTY).length == 0 &&
                                SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY).length == 0 &&
                                SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY).length == 0;
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        return false;
                    }
                }
            }
            @Override public void perform(Project p) {
                NbModuleProject project = (NbModuleProject) p;
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                if (inIDE && ModuleUISettings.getDefault().getConfirmReloadInIDE()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            LBL_reload_in_ide_confirm(),
                            LBL_reload_in_ide_confirm_title(),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                    ModuleUISettings.getDefault().setConfirmReloadInIDE(false); // do not ask again
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), new String[] {inIDE ? "reload-in-ide" : "reload"}, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        }, inIDE ? ACTION_reload_in_ide() : ACTION_reload(), null);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.createNbm")
    @ActionRegistration(displayName="#ACTION_nbm", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1600)
    @Messages("ACTION_nbm=Create NBM")
    public static Action createNbm() {
        return ProjectSensitiveActions.projectCommandAction(COMMAND_NBM, ACTION_nbm(), null);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.arch")
    @ActionRegistration(displayName="#ACTION_arch", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1900)
    @Messages("ACTION_arch=Generate Architecture Description")
    public static Action arch() {
        return ProjectSensitiveActions.projectSensitiveAction(new ProjectActionPerformer() {
            @Override public boolean enable(Project p) {
                if (!(p instanceof NbModuleProject)) {
                    return false;
                }
                NbModuleProject project = (NbModuleProject) p;
                return findBuildXml(project) != null;
            }
            @Override public void perform(Project p) {
                final NbModuleProject project = (NbModuleProject) p;
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                    public Void run() {
                        String prop = "javadoc.arch"; // NOI18N
                        if (project.evaluator().getProperty(prop) == null) {
                            // User has not yet configured an arch desc. Assume we should just do it for them.
                            EditableProperties props = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.setProperty(prop, "${basedir}/arch.xml"); // NOI18N
                            project.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException e) {
                                Util.err.notify(e);
                            }
                        }
                        return null;
                    }
                });
                try {
                    ActionUtils.runTarget(findBuildXml(project), new String[] {"arch-nb"}, null); // NOI18N
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        }, ACTION_arch(), null);
    }

}
