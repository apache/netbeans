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
package org.netbeans.modules.java.openjdk.jtreg;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.modules.java.openjdk.common.BuildUtils.ExtraMakeTargets;
import org.netbeans.modules.java.openjdk.common.ShortcutUtils;
import org.netbeans.modules.java.openjdk.project.Settings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=ActionProvider.class, position=1_000_000)
public class ActionProviderImpl implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(ActionProviderImpl.class.getName());
    private static final RequestProcessor BACKGROUND = new RequestProcessor(ActionProviderImpl.class.getName(), 100, false, false);

    private static final String[] ACTIONS = new String[] {
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD
    };

    @Override
    public String[] getSupportedActions() {
        return ACTIONS;
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        createAndRunTest(context, command);
    }

    private static final String COMMAND_BUILD_FAST = "build-fast";
    private static final String COMMAND_BUILD_GENERIC_FAST = "build-generic-fast";

    //public for test
    @Messages({"# {0} - simple file name",
               "DN_Debugging=Debugging ({0})",
               "# {0} - simple file name",
               "DN_Running=Running ({0})",
               "LBL_IncorrectVersionSelectJTReg=Location of JTReg:",
               "TITLE_IncorrectVersionSelectJTReg=Version of JTReg appears to be incorrect, please select a correct version"})
    public static ExecutorTask createAndRunTest(Lookup context, String inputCommand) {
        FileObject file;
        String query;
        String command;

        if (SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(inputCommand) ||
            SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(inputCommand)) {
            SingleMethod singleMethod = context.lookup(SingleMethod.class);

            assert singleMethod != null;

            file = singleMethod.getFile();
            query = singleMethod.getMethodName();
            command = SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(inputCommand) ? COMMAND_TEST_SINGLE
                                                                                  : COMMAND_DEBUG_TEST_SINGLE;
        } else {
            file = context.lookup(FileObject.class);
            query = null;
            command = inputCommand;
        }

        ensureProjectsRegistered(file);

        String ioName = COMMAND_DEBUG_TEST_SINGLE.equals(command) ? Bundle.DN_Debugging(file.getName()) : Bundle.DN_Running(file.getName());
        StopAction newStop = new StopAction();
        ReRunAction newReRun = new ReRunAction(COMMAND_TEST_SINGLE);
        ReRunAction newReDebug = new ReRunAction(COMMAND_DEBUG_TEST_SINGLE);
        final InputOutput io = IOProvider.getDefault().getIO(ioName, false, new javax.swing.Action[] {newReRun, newReDebug, newStop}, null);
        final StopAction stop = StopAction.record(io, newStop);
        final ReRunAction rerun = ReRunAction.recordRun(io, newReRun);
        final ReRunAction redebug = ReRunAction.recordDebug(io, newReDebug);
        rerun.setFile(file);
        redebug.setFile(file);
        final File jtregOutput = Utilities.jtregOutputDir(file);
        final File jtregWork = new File(jtregOutput, "work");
        final File jtregReport = new File(jtregOutput, "report");
        final ActionProgress progress = ActionProgress.start(context);
        ProfileSupport profiler = COMMAND_PROFILE_TEST_SINGLE.equals(command) ? Lookup.getDefault().lookup(ProfileSupport.Factory.class).create() : null;
        File jtregJar = findJTReg(file);
        if (jtregJar == null) {
            return null;
        }
        return ExecutionEngine.getDefault().execute(ioName, new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                File jcovTempData = null;
                File jcovData = null;
                final ServerSocket[] debugSocket = new ServerSocket[1];
                try {
                    try {
                        io.getOut().reset();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    rerun.disable();
                    redebug.disable();
                    Project prj = FileOwnerQuery.getOwner(file);
                    ActionProvider prjAP = prj != null ? prj.getLookup().lookup(ActionProvider.class) : null;
                    if (prjAP != null) {
                        Lookup targetContext = Lookup.EMPTY;
                        Set<String> supported = new HashSet<>(Arrays.asList(prjAP.getSupportedActions()));
                        String toRun = null;
                        
                        for (String command : new String[] {idealBuildTarget(file), ActionProvider.COMMAND_BUILD}) {
                            if (supported.contains(command) && prjAP.isActionEnabled(command, targetContext)) {
                                toRun = command;
                                break;
                            }
                        }

                        if (toRun != null) {
                            String[] extraMakeTarget;
                            switch (inferTestType(prj)) {
                                case JDK:
                                    extraMakeTarget = new String[] {"build-test-jdk-jtreg-native"};
                                    break;
                                default:
                                    extraMakeTarget = new String[0];
                            }
                            final CountDownLatch wait = new CountDownLatch(1);
                            final boolean[] state = new boolean[1];
                            targetContext = Lookups.fixed(new ActionProgress() {
                                @Override
                                protected void started() {
                                    state[0] = true;
                                }
                                @Override
                                public void finished(boolean success) {
                                    state[0] = success;
                                    wait.countDown();
                                }
                            },
                            new ExtraMakeTargets() {
                                @Override
                                public String[] getExtraMakeTargets() {
                                    return extraMakeTarget;
                                }
                            });
                            prjAP.invokeAction(toRun, targetContext);

                            if (!state[0]) {
                                io.getErr().println("Cannot build project!");
                                return ;
                            }

                            try {
                                wait.await();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                                return ;
                            }

                            if (!state[0]) {
                                io.getErr().println("Cannot build project!");
                                return ;
                            }

                            io.select();
                        }
                    }
                    ClassPath testSourcePath = ClassPath.getClassPath(file, ClassPath.SOURCE);
                    ClassPath extraSourcePath = allSources(file);
                    final ClassPath fullSourcePath = ClassPathSupport.createProxyClassPath(testSourcePath, extraSourcePath);
                    List<String> options = new ArrayList<>();
                    File targetJavaHome = BuildUtils.findTargetJavaHome(file);
                    options.add(new File(new File(targetJavaHome, "bin"), "java").getAbsolutePath());
                    options.add("-jar");
                    options.add(jtregJar.getAbsolutePath());
                    options.add("-timeout:10");
                    options.add("-jdk:" + targetJavaHome.getAbsolutePath());
                    options.add("-retain:all");
                    options.add("-ignore:quiet");
                    options.add("-verbose:summary,nopass");
                    options.add("-w");
                    options.add(jtregWork.getAbsolutePath());
                    options.add("-r");
                    options.add(jtregReport.getAbsolutePath());
                    options.add("-xml:verify");
                    options.add("-javacoptions:-g");
                    File buildDir = BuildUtils.getBuildTargetDir(file);
                    options.add("-vmoption:-Djava.library.path=" + buildDir.getAbsolutePath() + "/support/test/jdk/jtreg/native/lib/");
                    Set<File> toRefresh = new HashSet<>();
                    if (hasXPatch(targetJavaHome)) {
                        CoverageSupport covSupp = Lookup.getDefault().lookup(CoverageSupport.class);
                        CoverageSupport.Result covResult = covSupp != null ? covSupp.coverage(jtregOutput, buildDir, io.getOut()) : null;
                        if (covResult != null) {
                            for (String opt : covResult.extraOptions) {
                                options.add("-vmoption:" + opt);
                            }

                            genXPatchForDir(file, covResult.classes, options);
                        } else if (!fullBuild(file)) {
                            File buildClasses = builtClassesDirsForXOverride(file);
                            
                            genXPatchForDir(file, buildClasses, options);
                        }
                    } else {
                        options.add("-Xbootclasspath/p:" + builtClassesDirsForBootClassPath(file));
                    }
                    switch (command) {
                        case COMMAND_RUN_SINGLE: break;
                        case COMMAND_DEBUG_TEST_SINGLE:
                            try {
                                InetAddress addr = InetAddress.getLocalHost();
                                debugSocket[0] = new ServerSocket(0, 1, addr);
                                BACKGROUND.post(() -> {
                                    try {
                                        while (true) {
                                            Socket server = debugSocket[0].accept();
                                            JPDAStart s = new JPDAStart(io, COMMAND_DEBUG_SINGLE); //XXX command
                                            s.setAdditionalSourcePath(fullSourcePath);
                                            try {
                                                Pair<String, Integer> connectTo = s.execute(prj);
                                                Socket clientSocket = new Socket(connectTo.first() != null ? connectTo.first() : InetAddress.getLocalHost().getHostName(), connectTo.second());
                                                BACKGROUND.post(new Copy(clientSocket.getInputStream(), server.getOutputStream(), clientSocket));
                                                BACKGROUND.post(new Copy(server.getInputStream(), clientSocket.getOutputStream(), clientSocket));
                                            } catch (Throwable ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                    } catch (IOException ex) {
                                        LOG.log(Level.FINE, null, ex);
                                    }
                                });
                                options.addAll(Arrays.asList("-debug:-agentlib:jdwp=transport=dt_socket,suspend=y,server=n,address=" + addr.getHostAddress() + ":" + debugSocket[0].getLocalPort()));
                            } catch (Throwable ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                        case COMMAND_PROFILE_TEST_SINGLE:
                            for (String opt : profiler.getCommandLineOptions()) {
                                options.add("-debug:" + opt);
                            }
                            break;
                    }
                    String testPath = FileUtil.toFile(file).getAbsolutePath();
                    if (query != null) {
                        testPath += "?" + query;
                    }
                    options.add(testPath);
                    try {
                        stop.started();
                        Process jtregProcess = new ProcessBuilder(options).start();
                        StringWriter errorOutput = new StringWriter();
                        Task outCopy = BACKGROUND.post(new CopyReaderWriter(new InputStreamReader(jtregProcess.getInputStream()), io.getOut()));
                        Task errCopy = BACKGROUND.post(new CopyReaderWriter(new InputStreamReader(jtregProcess.getErrorStream()), io.getErr(), errorOutput));
                        BACKGROUND.post(new CopyReaderWriter(io.getIn(), new OutputStreamWriter(jtregProcess.getOutputStream())));
                        int processResult = jtregProcess.waitFor();
                        outCopy.waitFinished();
                        errCopy.waitFinished();
                        switch (processResult) {
                            case 5: //error
                                //check if it is a version error:
                                if (errorOutput.toString().contains("jtreg version")) {
                                    Settings settings = prj.getLookup().lookup(Settings.class);
                                    String jtregLocation = settings.getJTregLocation();
                                    InputLine nd = new InputLine(Bundle.LBL_IncorrectVersionSelectJTReg(), Bundle.TITLE_IncorrectVersionSelectJTReg(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                                    nd.setInputText(jtregLocation);
                                    if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                                        settings.setJTregLocation(nd.getInputText());
                                        BACKGROUND.post(() -> {
                                            createAndRunTest(context, command);
                                        });
                                    }
                                }
                                break;
                            default:
                                printJTR(io, jtregWork, fullSourcePath, file);
                                break;
                        }
                        success = true;

                        for (File refresh : toRefresh) {
                            FileUtil.refreshFor(refresh);
                        }
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace(io.getErr());
                    } finally {
                        stop.finished();
                    }
                } finally {
                    if (jcovTempData != null) {
                        jcovTempData.renameTo(jcovData);
                    }
                    if (debugSocket[0] != null) {
                        try {
                            debugSocket[0].close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    io.getOut().close();
                    io.getErr().close();
                    try {
                        io.getIn().close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    progress.finished(success);
                    rerun.enable();
                    redebug.enable();
                }
            }
        }, io);
    }

    static ClassPath allSources(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        FileObject jdkRoot;
        List<String> sourceDirPaths;

        if (BuildUtils.getFileObject(prj.getProjectDirectory(), "../../../modules.xml") != null ||
            BuildUtils.getFileObject(prj.getProjectDirectory(), "share/classes/module-info.java") != null) {
            File buildTarget = BuildUtils.getBuildTargetDir(file);
            jdkRoot = buildTarget != null ? FileUtil.toFileObject(buildTarget.getParentFile().getParentFile()) : null;
            if (jdkRoot == null) {
                //should not happen, just last resort:
                jdkRoot = BuildUtils.getFileObject(prj.getProjectDirectory(), "../../..");
            }
            if (BuildUtils.getFileObject(jdkRoot, "src/java.base/share/classes/module-info.java") != null) {
                sourceDirPaths = Arrays.asList("src", "*", "*", "classes");
            } else {
                sourceDirPaths = Arrays.asList("*", "src", "*", "*", "classes");
            }
        } else {
            jdkRoot = BuildUtils.getFileObject(prj.getProjectDirectory(), "../../..");
            sourceDirPaths = Arrays.asList("src", "*", "*", "classes");
        }

        //find: */src/*/*/classes
        List<FileObject> roots = new ArrayList<>();

        listAllRoots(jdkRoot, new LinkedList<>(sourceDirPaths), roots);

        return ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
    }

    private static void listAllRoots(FileObject currentDir, List<String> remainders, List<FileObject> roots) {
        if (remainders.isEmpty() && currentDir.isFolder()) {
            roots.add(currentDir);
            return ;
        }

        String current = remainders.remove(0);

        if ("*".equals(current)) {
            for (FileObject c : currentDir.getChildren()) {
                listAllRoots(c, remainders, roots);
            }
        } else {
            FileObject child = BuildUtils.getFileObject(currentDir, current);

            if (child != null) {
                listAllRoots(child, remainders, roots);
            }
        }

        remainders.add(0, current);
    }

    private static boolean hasXPatch(File targetJavaHome) {
        return new File(targetJavaHome, "conf").isDirectory();
    }

    private static boolean newStyleXPatch(FileObject testFile) {
        Project prj = FileOwnerQuery.getOwner(testFile);

        if (BuildUtils.getFileObject(prj.getProjectDirectory(), "../../src/java.base/share/classes/java/lang/Object.java") != null &&
            BuildUtils.getFileObject(prj.getProjectDirectory(), "../../src/java.compiler/share/classes/javax/tools/ToolProvider.java") != null) {
            //consolidated repo:
            return true;
        }

        FileObject testRoot = BuildUtils.getFileObject(prj.getProjectDirectory(), "../../test/TEST.ROOT");

        if (testRoot == null)
            return false;

        try (InputStream in = testRoot.getInputStream()) {
            EditableProperties ep = new EditableProperties(true);
            ep.load(in);
            return "true".equals(ep.get("useNewOptions"));
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
            return false;
        }
    }

    static String builtClassesDirsForBootClassPath(FileObject testFile) {
        File buildDir = BuildUtils.getBuildTargetDir(testFile);
        Project prj = FileOwnerQuery.getOwner(testFile);
        Settings settings = prj.getLookup().lookup(Settings.class);
        boolean useLangtoolsBuild = settings == null || settings.isUseAntBuild();
        List<FileObject> roots = new ArrayList<>();

        if (buildDir != null) {
            FileObject repo = prj.getProjectDirectory().getParent().getParent();
            if (repo.getNameExt().equals("langtools") &&
                ShortcutUtils.getDefault().shouldUseCustomTest(repo.getNameExt(), FileUtil.getRelativePath(repo, testFile)) &&
                useLangtoolsBuild) {
                listAllRoots(BuildUtils.getFileObject(prj.getProjectDirectory(), "../.."), new LinkedList<>(Arrays.asList("build", "classes")), roots);
                listAllRoots(BuildUtils.getFileObject(prj.getProjectDirectory(), "../.."), new LinkedList<>(Arrays.asList("build", "*", "classes")), roots);
            } else {
                listAllRoots(FileUtil.toFileObject(buildDir), new LinkedList<>(Arrays.asList("jdk", "modules", "*")), roots);
            }
        } else {
            listAllRoots(BuildUtils.getFileObject(prj.getProjectDirectory(), "../../.."), new LinkedList<>(Arrays.asList("build", "classes")), roots);
            listAllRoots(BuildUtils.getFileObject(prj.getProjectDirectory(), "../../.."), new LinkedList<>(Arrays.asList("build", "*", "classes")), roots);
        }

        StringBuilder built = new StringBuilder();
        String sep = "";

        for (FileObject fo : roots) {
            built.append(sep);
            built.append(FileUtil.toFile(fo).getAbsoluteFile());

            sep = File.pathSeparator;
        }

        return built.toString();
    }

    static boolean fullBuild(FileObject testFile) {
        File buildDir = BuildUtils.getBuildTargetDir(testFile);
        Project prj = FileOwnerQuery.getOwner(testFile);
        Settings settings = prj.getLookup().lookup(Settings.class);
        boolean useLangtoolsBuild = settings == null || settings.isUseAntBuild();

        if (buildDir != null) {
            FileObject repo = prj.getProjectDirectory().getParent().getParent();
            String repoName = ShortcutUtils.getDefault().inferLegacyRepository(prj);
            return !("langtools".equals(repoName) &&
                    ShortcutUtils.getDefault().shouldUseCustomTest(repoName, FileUtil.getRelativePath(repo, testFile)) &&
                    useLangtoolsBuild);
        }

        return false;
    }

    private static File builtClassesDirsForXOverride(FileObject testFile) {
        Project prj = FileOwnerQuery.getOwner(testFile);
        FileObject buildClasses;

        FileObject repo = prj.getProjectDirectory().getParent().getParent();
        if (repo.getNameExt().equals("langtools") &&
            ShortcutUtils.getDefault().shouldUseCustomTest(repo.getNameExt(), FileUtil.getRelativePath(repo, testFile))) {
            buildClasses = BuildUtils.getFileObject(prj.getProjectDirectory(), "../../build/modules");
            if (buildClasses == null) {
                //old style:
                buildClasses = BuildUtils.getFileObject(prj.getProjectDirectory(), "../../build/classes");
            }
        } else {
            String inferredRepoName = ShortcutUtils.getDefault().inferLegacyRepository(prj);
            if ("langtools".equals(inferredRepoName) &&
                ShortcutUtils.getDefault().shouldUseCustomTest(inferredRepoName, FileUtil.getRelativePath(repo, testFile))) {
                File buildDir = BuildUtils.getBuildTargetDir(testFile);
                FileObject buildDirFO = FileUtil.toFileObject(buildDir);
                buildClasses = buildDirFO != null ? BuildUtils.getFileObject(buildDirFO, "../langtools/modules") : null;
            } else {
                File buildDir = BuildUtils.getBuildTargetDir(testFile);
                FileObject buildDirFO = FileUtil.toFileObject(buildDir);
                buildClasses = buildDirFO != null ? BuildUtils.getFileObject(buildDirFO, "jdk/modules") : null;
            }
        }

        return buildClasses != null ? FileUtil.toFile(buildClasses).getAbsoluteFile() : null;
    }

    static void printJTR(InputOutput io, File jtregWork, ClassPath fullSourcePath, FileObject testFile) {
        try {
            FileObject testRoot = testFile;
            while (testRoot != null && BuildUtils.getFileObject(testRoot, "TEST.ROOT") == null)
                testRoot = testRoot.getParent();
            if (testRoot != null) {
                String relPath = FileUtil.getRelativePath(testRoot, testFile);
                relPath = relPath.replaceAll(".java$", ".jtr");
                File jtr = new File(jtregWork, relPath);
                if (jtr.canRead()) {
                    FileUtil.refreshFor(jtr);
                    for (String line : FileUtil.toFileObject(jtr).asLines()) {
                        final StackTraceLine stl = matches(line);
                        if (stl != null) {
                            final FileObject source = fullSourcePath.findResource(stl.expectedFileName);
                            if (source != null) {
                                io.getOut().println(line, new OutputListener() {
                                    @Override
                                    public void outputLineSelected(OutputEvent ev) {}
                                    @Override
                                    public void outputLineAction(OutputEvent ev) {
                                        Mutex.EVENT.readAccess(new Runnable() {
                                            @Override public void run() {
                                                open(source, stl.lineNumber - 1);
                                            }
                                        });
                                    }
                                    @Override
                                    public void outputLineCleared(OutputEvent ev) {}
                                });
                            }
                        } else {
                            io.getOut().println(line);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static String idealBuildTarget(FileObject testFile) {
        Project prj = FileOwnerQuery.getOwner(testFile);
        FileObject repo = prj.getProjectDirectory().getParent().getParent();
        String repoName = ShortcutUtils.getDefault().inferLegacyRepository(prj);
        if (ShortcutUtils.getDefault().shouldUseCustomTest(repoName, FileUtil.getRelativePath(repo, testFile))) {
            return COMMAND_BUILD_FAST;
        } else {
            return COMMAND_BUILD_GENERIC_FAST;
        }
    }

    private static void open(FileObject file, int line) {
        LineCookie lc = file.getLookup().lookup(LineCookie.class);

        if (lc != null) {
            Line.Set ls = lc.getLineSet();
            try {
                Line originalLine = ls.getOriginal(line);
                originalLine.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            } catch (IndexOutOfBoundsException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        OpenCookie oc = file.getLookup().lookup(OpenCookie.class);

        if (oc != null) {
            oc.open();
        }
    }

    private static void genXPatchForDir(FileObject testFile, File dir, List<String> options) {
        boolean newStyleXPatch = newStyleXPatch(testFile);
        File[] modules = dir != null ? dir.listFiles() : null;
        if (modules != null) {
            for (File module : modules) {
                if (newStyleXPatch) {
                    options.add("-vmoption:--patch-module");
                    options.add("-vmoption:" + module.getName() + "=" + module.getAbsolutePath());
                } else {
                    options.add("-vmoption:-Xpatch:" + module.getName() + "=" + module.getAbsolutePath());
                }
            }
        }
    }

    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("\\s*at\\s*(([^/]*/[^/]*/)|([^/]*/))?(?<location>\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*(\\.<init>|\\.<clinit>)?)\\s*\\([^:)]*(:(?<line>[0-9]+))?\\)");

    static StackTraceLine matches(String line) {
        Matcher m = STACK_TRACE_PATTERN.matcher(line);
        if (m.matches()) {
            String className = m.group("location");
            className = className.substring(0, className.lastIndexOf('.'));
            int dollar = className.lastIndexOf('$');
            if (dollar != (-1))
                className = className.substring(0, dollar);
            className = className.replace('.', '/') + ".java";
            String lineNumber = m.group("line");
            return new StackTraceLine(className, lineNumber != null ? Integer.parseInt(lineNumber) : -1);
        } else {
            return null;
        }
    }

    static final class StackTraceLine {
        public final String expectedFileName;
        public final int lineNumber;
        public StackTraceLine(String expectedFileName, int lineNumber) {
            this.expectedFileName = expectedFileName;
            this.lineNumber = lineNumber;
        }
    }

    @Messages({
        "LBL_SelectJTReg=JTreg Location:",
        "TITLE_SelectJTReg=Please select JTReg location",
    })
    private static File findJTReg(FileObject file) {
        File buildDir = BuildUtils.getBuildTargetDir(file);
        File spec = new File(buildDir, "spec.gmk");
        if (spec.canRead()) {
            try (Stream<String> lines = Files.lines(spec.toPath())) {
                String jtHome = lines.filter(l -> l.startsWith(JT_HOME_KEY))
                                     .findAny()
                                     .orElse(JT_HOME_KEY)
                                     .substring(JT_HOME_KEY.length());
                File jtregJar = findJTRegJar(jtHome);
                if (jtregJar != null) {
                    return jtregJar;
                }
            } catch (IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        Project prj = FileOwnerQuery.getOwner(file);
        Settings settings = prj.getLookup().lookup(Settings.class);
        while (true) {
            String jtregLocation = settings.getJTregLocation();
            File jtregHome = jtregLocation != null ? new File(jtregLocation) : null;
            File jtregJar = jtregHome != null ? new File(new File(jtregHome, "lib"), "jtreg.jar") : null;
            if (jtregJar == null || !jtregJar.canRead()) {
                InputLine nd = new InputLine(Bundle.LBL_SelectJTReg(), Bundle.TITLE_SelectJTReg(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                nd.setInputText(jtregLocation);
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                    settings.setJTregLocation(nd.getInputText());
                    continue;
                } else {
                    return null;
                }
            }
            return jtregJar;
        }
    }
        private static final String JT_HOME_KEY = "JT_HOME:=";

    private static File findJTRegJar(String installDir) {
        File jtregHome = installDir != null ? new File(installDir) : null;
        File jtregJar = jtregHome != null ? new File(new File(jtregHome, "lib"), "jtreg.jar") : null;
        return jtregJar == null || !jtregJar.canRead() ? null : jtregJar;
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        FileObject file = context.lookup(FileObject.class);

        if (file == null) {
            SingleMethod singleMethod = context.lookup(SingleMethod.class);

            file = singleMethod != null ? singleMethod.getFile() : null;
        }

        if (file == null)
            return false;
        
        return findJDKRoot(file) != null;
    }

    private static FileObject findJDKRoot(FileObject file) {
        while (!file.isRoot()) {
            if (Utilities.isJDKRepository(file))
                return file;
            file = file.getParent();
        }

        return null;
    }

    private static void ensureProjectsRegistered(FileObject file) {
        if (FileOwnerQuery.getOwner(file) != null) {
            return ;
        }

        FileObject jdkRoot = findJDKRoot(file);

        if (jdkRoot == null) {
            return ;
        }

        for (String wellKnownProject : new String[] {"java.base", "java.compiler",
                                                     "java.xml", "jdk.scripting.nashorn"}) {
            for (String open : new String[] {"open/", ""}) {
                FileObject prjRoot = jdkRoot.getFileObject(open + "src/" + wellKnownProject);

                if (prjRoot == null) {
                    continue;
                }

                Project thisPrj = FileOwnerQuery.getOwner(prjRoot);

                if (thisPrj != null) {
                    //ensure external roots are registered:
                    ProjectUtils.getSources(thisPrj)
                                .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                }
            }
        }
    }

    private static TestType inferTestType(Project prj) {
        switch (prj.getProjectDirectory().getNameExt()) {
            case "java.base": return TestType.JDK;
            case "java.compiler": return TestType.LANGTOOLS;
            default: return TestType.OTHER;
        }
    }

    static enum TestType {
        JDK,
        LANGTOOLS,
        OTHER;
    }

    private static final class StopAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private static final Map<InputOutput, StopAction> actions = new WeakHashMap<>();

        public static StopAction record(InputOutput io, StopAction ifAbsent) {
            StopAction res = actions.get(io);

            if (res == null) {
                actions.put(io, res = ifAbsent);
            }

            return res;
        }

        private Thread executor;

        @Messages("DESC_Stop=Stop")
        public StopAction() {
            setEnabledEQ(false);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/java/openjdk/jtreg/resources/stop.png", true));
            putValue(SHORT_DESCRIPTION, Bundle.DESC_Stop());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executor.interrupt();
            setEnabledEQ(false);
        }

        private void started() {
            executor = Thread.currentThread();
            setEnabledEQ(true);
        }

        private void finished() {
            executor = null;
            setEnabledEQ(false);
        }

        private void setEnabledEQ(final boolean enabled) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(enabled);
                }
            });
        }
    }

    private static final class ReRunAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private static final Map<InputOutput, ReRunAction> runActions = new WeakHashMap<>();
        private static final Map<InputOutput, ReRunAction> debugActions = new WeakHashMap<>();

        public static ReRunAction recordRun(InputOutput io, ReRunAction ifAbsent) {
            return record(io, runActions, ifAbsent);
        }

        public static ReRunAction recordDebug(InputOutput io, ReRunAction ifAbsent) {
            return record(io, debugActions, ifAbsent);
        }

        private static ReRunAction record(InputOutput io, Map<InputOutput, ReRunAction> actions, ReRunAction ifAbsent) {
            ReRunAction res = actions.get(io);

            if (res == null) {
                actions.put(io, res = ifAbsent);
            }

            return res;
        }

        private FileObject file;
        private final String command;

        @Messages({
            "DESC_ReRun=Run test again",
            "DESC_ReDebug=Run test again under debugger"
        })
        public ReRunAction(String command) {
            setEnabledEQ(false);
            boolean debug = COMMAND_DEBUG_TEST_SINGLE.equals(command);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(debug ? "org/netbeans/modules/java/openjdk/jtreg/resources/redebug.png" : "org/netbeans/modules/java/openjdk/jtreg/resources/rerun.png", true));
            putValue(SHORT_DESCRIPTION, debug ? Bundle.DESC_ReDebug() : Bundle.DESC_ReRun());
            this.command = command;
        }

        public void setFile(FileObject file) {
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ActionProviderImpl.createAndRunTest(Lookups.singleton(file), command);
        }

        private void enable() {
            setEnabledEQ(true);
        }

        private void disable() {
            setEnabledEQ(false);
        }

        private void setEnabledEQ(final boolean enabled) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(enabled);
                }
            });
        }
    }

    private static final class Copy implements Runnable {
        private final InputStream in;
        private final OutputStream out;
        private final AutoCloseable toClose;

        public Copy(InputStream in, OutputStream out, AutoCloseable toClose) {
            this.in = in;
            this.out = out;
            this.toClose = toClose;
        }

        @Override
        public void run() {
            try {
                byte[] read = new byte[2048];
                int count;
                
                while ((count = in.read(read)) != (-1)) {
                    System.err.println("count=" + count);
                    out.write(read, 0, count);
                }
            } catch (IOException ex) {
                //ignore
            } finally {
                try {
                    toClose.close();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
    }

    private static final class CopyReaderWriter implements Runnable {
        private final Reader in;
        private final Writer out;
        private final Writer secondaryOut;

        public CopyReaderWriter(Reader in, Writer out) {
            this(in, out, null);
        }

        public CopyReaderWriter(Reader in, Writer out, Writer secondaryOut) {
            this.in = in;
            this.out = out;
            this.secondaryOut = secondaryOut;
        }

        @Override
        public void run() {
            try {
                char[] buf = new char[1024];
                int read;

                while ((read = in.read(buf)) != (-1)) {
                    out.write(buf, 0, read);
                    if (secondaryOut != null) {
                        secondaryOut.write(buf, 0, read);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
