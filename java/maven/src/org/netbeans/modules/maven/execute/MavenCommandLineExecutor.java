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

package org.netbeans.modules.maven.execute;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.cos.CosChecker;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.cmd.Constructor;
import org.netbeans.modules.maven.execute.cmd.ShellConstructor;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.runjar.MavenExecuteUtils;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.NotificationDisplayer;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * support for executing maven, externally on the command line.
 * <b>Since 2/1.144</b>, the {@link LateBoundPrerequisitesChecker} registered in Maven projects for JAR packaging by default supports 
 * {@link ExplicitProcessParameters} API. The caller of the execute-type action can request to append or replace VM or user
 * application parameters. The parameters recorded in the POM.xml or NetBeans action mappings are augmented according to that
 * instructions:
 * <ul>
 * <li><b>launcherArgs</b> are mapped to VM arguments (precede main class name)
 * <li><b>args</b> are mapped to user application arguments (after main class name)
 * </ul>
 * VM parameters injected by {@link StartupExtender} API are not affected by this feature. 
 * <p>
 * Example use:
 * {@snippet file="org/netbeans/modules/maven/execute/MavenExecutionTestBase.java" region="samplePassAdditionalVMargs"}
 * The example will <b>append</b> <code>-DvmArg2=2</code> to VM arguments and <b>replaces</b> all user
 * program arguments with <code>"paramY"</code>. Append mode can be controlled using {@link ExplicitProcessParameters.Builder#appendArgs} or
 * {@link ExplicitProcessParameters.Builder#appendPriorityArgs}.
 *
 * @author  Milos Kleint (mkleint@codehaus.org)
 * @author  Svata Dedic (svatopluk.dedic@gmail.com)
 */
public class MavenCommandLineExecutor extends AbstractMavenExecutor {
    static final String ENV_PREFIX = MavenExecuteUtils.ENV_PREFIX;
    static final String INTERNAL_PREFIX = "NbIde."; //NOI18N
    static final String ENV_JAVAHOME = ENV_PREFIX + "JAVA_HOME"; //NOI18N

    private static final String KEY_UUID = "NB_EXEC_MAVEN_PROCESS_UUID"; //NOI18N

    private static final String NETBEANS_MAVEN_COMMAND_LINE = "NETBEANS_MAVEN_COMMAND_LINE"; //NOI18N

    private Process process;
    private String processUUID;
    private Process preProcess;
    private String preProcessUUID;
    private static final SpecificationVersion VER18 = new SpecificationVersion("1.8"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(MavenCommandLineExecutor.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(MavenCommandLineExecutor.class.getName(),1);

    private static final RequestProcessor UPDATE_INDEX_RP = new RequestProcessor(RunUtils.class.getName(), 5);

    private static final String ICON_MAVEN_PROJECT = "org/netbeans/modules/maven/resources/Maven2Icon.gif"; // NOI18N

    /**
     * Execute maven build in NetBeans execution engine.
     * Most callers should rather use {@link #run} as this variant does no (non-late-bound) prerequisite checks.
     * It is mostly suitable for cases where you need full control by the caller over the config, or want to rerun a previous execution.
     * @param config
     * @param io <code>null</code> or InputOutput to reuse for output of the execution
     * @param tc tab context to use or <code>null</code>
     * @return
     * @since 2.113
     */
    public static ExecutorTask executeMaven(final RunConfig config, InputOutput io, TabContext tc) {
        ExecuteMaven runner = Lookup.getDefault().lookup(ExecuteMaven.class);
        if (runner == null) {
            runner = new ExecuteMaven();
        }
        return runner.execute(config, io, tc);
    }

    /**
     * Hooks for tests to mock the Maven execution.
     */
    public static class ExecuteMaven {
        // tests only
        MavenExecutor createCommandLineExecutor(RunConfig config, InputOutput io, TabContext tc) {
            return new MavenCommandLineExecutor(config, io, tc);
        }
        
        public ExecutorTask execute(RunConfig config, InputOutput io, TabContext tc) {
            LifecycleManager.getDefault().saveAll();
            MavenExecutor exec = createCommandLineExecutor(config, io, tc);
            ExecutorTask task = ExecutionEngine.getDefault().execute(config.getTaskDisplayName(), exec, new ProxyNonSelectableInputOutput(exec.getInputOutput()));
            exec.setTask(task);
            task.addTaskListener((Task t) -> {
                MavenProject mp = config.getMavenProject();
                if (mp == null) {
                    return;
                }
                final List<Artifact> arts = new ArrayList<>();
                Artifact main = mp.getArtifact();
                if (main != null) {
                    arts.add(main);
                }
                arts.addAll(mp.getArtifacts());
                UPDATE_INDEX_RP.post(() -> {
                    RepositoryIndexer.updateIndexWithArtifacts(RepositoryPreferences.getInstance().getLocalRepository(), arts);
                });
            });
            return task;
        }
    }

    public MavenCommandLineExecutor(RunConfig conf, InputOutput io, TabContext tc) {
        super(conf, tc);
        this.io = io;
    }

    @NbBundle.Messages({
        "# {0} - original message",
        "ERR_CannotOverrideProxy=Could not override the proxy: {0}",
        "ERR_BuildCancelled=Build cancelled by the user"
    })
    /**
     * not to be called directly.. use execute();
     */
    @Override
    public void run() {
        synchronized (SEMAPHORE) {
            if (task == null) {
                try {
                    SEMAPHORE.wait();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, "interrupted", ex);
                }
            }
        }

        final BeanRunConfig clonedConfig = new BeanRunConfig(this.config);
        clonedConfig.reassignMavenProjectFromParent();
        if (clonedConfig.getPreExecution() != null) {
            final BeanRunConfig preconf = new BeanRunConfig(clonedConfig.getPreExecution());
            preconf.reassignMavenProjectFromParent();
            clonedConfig.setPreExecution(preconf);
        }
        int executionresult = -10;
        final InputOutput ioput = getInputOutput();

        // TODO: maybe global instance for project-less operation ?
        MavenProxySupport mps = (clonedConfig.getProject() == null) ? null : clonedConfig.getProject().getLookup().lookup(MavenProxySupport.class);
        if (mps != null) {
            boolean ok = false;
            try {
                MavenProxySupport.ProxyResult res = mps.checkProxySettings().get();
                
                if (res != null) {
                    res.configure(clonedConfig);
                }
                if (res.getStatus() == MavenProxySupport.Status.ABORT) {
                    IOException ex = res.getException();
                    
                    if (ex == null) {
                        ioput.getErr().append(Bundle.ERR_BuildCancelled());
                    } else {
                        throw ex;
                    }
                    
                } else {
                    ok = true;
                }
            } catch (IOException ex) {
                NotificationDisplayer.getDefault().notify(Bundle.TITLE_ProxyUpdateFailed(),
                        ImageUtilities.loadImageIcon(ICON_MAVEN_PROJECT, false),
                        ex.getLocalizedMessage(), null, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.ERROR);
                ioput.getErr().append(Bundle.ERR_CannotOverrideProxy(ex.getLocalizedMessage()));
                // FIXME: log exception
            } catch (ExecutionException | InterruptedException ex) {
                LOGGER.log(Level.WARNING, "could not determine proxy settings", ex);
            } finally {
                if (!ok) {
                    ioput.getOut().close();
                    ioput.getErr().close();
                    actionStatesAtFinish(null, null);
                    markFreeTab();
                }
            }
        }
        
        final ProgressHandle handle = ProgressHandle.createHandle(clonedConfig.getTaskDisplayName(), this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ioput.select();
            }
        });
        actionStatesAtStart();
        ExecutionContext exCon = ActionToGoalUtils.ACCESSOR.createContext(ioput, handle);
        // check the prerequisites
        if (clonedConfig.getProject() != null) {
            Lookup.Result<LateBoundPrerequisitesChecker> result = clonedConfig.getProject().getLookup().lookupResult(LateBoundPrerequisitesChecker.class);
            for (LateBoundPrerequisitesChecker elem : result.allInstances()) {
                if (!elem.checkRunConfig(clonedConfig, exCon)) {
                    //#238360 when the check says don't execute, we still need to close the output and mark it for reuse
                    ioput.getOut().close();
                    ioput.getErr().close();
                    actionStatesAtFinish(null, null);
                    markFreeTab();
                    //#238360 - end
                    return;
                }
                if (clonedConfig.getPreExecution() != null) {
                    if (!elem.checkRunConfig(clonedConfig.getPreExecution(), exCon)) {
                        //#238360 when the check says don't execute, we still need to close the output and mark it for reuse
                        ioput.getOut().close();
                        ioput.getErr().close();
                        actionStatesAtFinish(null, null);
                        markFreeTab();
                        //#238360 - end
                        return;
                    }
                }
            }
        }

//        final Properties originalProperties = clonedConfig.getProperties();
        handle.start();
        processInitialMessage();
        boolean isMaven3 = !isMaven2();
        boolean singlethreaded = !isMultiThreaded(clonedConfig);
        boolean eventSpyCompatible = isEventSpyCompatible(clonedConfig);
        if (isMaven3 && singlethreaded && eventSpyCompatible) {
            injectEventSpy( clonedConfig );
            if (clonedConfig.getPreExecution() != null) {
                injectEventSpy( (BeanRunConfig) clonedConfig.getPreExecution());
            }
        }

        CommandLineOutputHandler out = new CommandLineOutputHandler(ioput, clonedConfig.getProject(), handle, clonedConfig, isMaven3 && singlethreaded);
        try {
            BuildExecutionSupport.registerRunningItem(item);
            if (MavenSettings.getDefault().isAlwaysShowOutput()) {
                ioput.select();
            }
            if (clonedConfig.getPreExecution() != null) {
                ProcessBuilder builder = constructBuilder(clonedConfig.getPreExecution(), ioput);
                preProcessUUID = UUID.randomUUID().toString();
                builder.environment().put(KEY_UUID, preProcessUUID);
                if (executeProcess(out, builder, (p) -> preProcess = p) != 0) {
                    return;
                }
            }

//debugging..            
//            Map<String, String> env = builder.environment();
//            for (String key : env.keySet()) {
//                ioput.getOut().println(key + ":" + env.get(key));
//            }
            ProcessBuilder builder = constructBuilder(clonedConfig, ioput);
            printCoSWarning(clonedConfig, ioput);
            processUUID = UUID.randomUUID().toString();
            builder.environment().put(KEY_UUID, processUUID);
            executionresult = executeProcess(out, builder, (p) -> process = p);
        } catch (IOException x) {
            if (Utilities.isWindows()) { //#153101
                processIssue153101(x, ioput);
            } else {
                ioput.getErr().println(x.getMessage());
            }
        } catch (InterruptedException x) {
            cancel();
        } catch (ThreadDeath death) {
            cancel();
            throw death;
        } finally {
            BuildExecutionSupport.registerFinishedItem(item);

            try { //defend against badly written extensions..
                out.buildFinished();
                if (clonedConfig.getProject() != null) {
                    Lookup.Result<ExecutionResultChecker> result = clonedConfig.getProject().getLookup().lookupResult(ExecutionResultChecker.class);
                    for (ExecutionResultChecker elem : result.allInstances()) {
                        elem.executionResult(clonedConfig, exCon, executionresult);
                    }
                }
            }
            finally {
                //MEVENIDE-623 re add original Properties
//                clonedConfig.setProperties(originalProperties);

                handle.finish();
                ioput.getOut().close();
                ioput.getErr().close();
                actionStatesAtFinish(out.createResumeFromFinder(), out.getExecutionTree());
                markFreeTab();
                final Project prj = clonedConfig.getProject();
                if (prj != null) {
                    NbMavenProjectImpl impl = prj.getLookup().lookup(NbMavenProjectImpl.class);
                    if (impl != null) {
                        // this reload must not wait for blockers.
                        RequestProcessor.Task reloadTask = impl.fireProjectReload();
                        reloadTask.waitFinished();
                    }
                }
            }
        }
    }

    private boolean isEventSpyCompatible(final BeanRunConfig clonedConfig) {
        // EventSpy cannot work on jdk < 8
        if (clonedConfig.getProject() != null) {
            ActiveJ2SEPlatformProvider javaprov = clonedConfig.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            JavaPlatform platform = javaprov.getJavaPlatform();
            return (platform.getSpecification().getVersion().compareTo(VER18) >= 0);
        } else {
            return true;
        }
    }

    /**
     * Overridable by tests.
     */
    int executeProcess(CommandLineOutputHandler out, ProcessBuilder builder, Consumer<Process> processSetter) throws IOException, InterruptedException {
        Process p = builder.start();
        processSetter.accept(p);
        out.setStdOut(p.getInputStream());
        out.setStdIn(p.getOutputStream());
        int executionresult = p.waitFor();
        out.waitFor();
        return executionresult;
    }
    
    private void kill(Process prcs, String uuid) {
        Map<String, String> env = new HashMap<>();
        env.put(KEY_UUID, uuid);
        Processes.killTree(prcs, env);
    }

    @Override
    public boolean cancel() {
        final Process pre = preProcess;
        preProcess = null;
        final Process pro = process;
        process = null;
        RP.post(() -> {
            if (pre != null) {
                kill(pre, preProcessUUID);
            }
            if (pro != null) {
                kill(pro, processUUID);
            }
        });
        return true;
    }

    private static List<String> createMavenExecutionCommand(RunConfig config, Constructor base) {
        List<String> toRet = new ArrayList<>(base.construct());

        if (Utilities.isUnix()) { // #198997 - defend against symlinks
            File basedir = config.getExecutionDirectory();
            try {
                if (basedir != null && !basedir.equals(basedir.getCanonicalFile())) {
                    File pom = new File(basedir, "pom.xml");
                    if (pom.isFile()) { // #201400
                        toRet.add("-f");
                        toRet.add(pom.getAbsolutePath());
                    }
                }
            } catch (IOException x) {
                LOGGER.log(Level.FINE, "Could not canonicalize " + basedir, x);
            }
        }

        //#164234
        //if maven.bat file is in space containing path, we need to quote with simple quotes.
        String quote = "\"";
        // the command line parameters with space in them need to be quoted and escaped to arrive
        // correctly to the java runtime on windows
        for (Map.Entry<? extends String, ? extends String> entry : config.getProperties().entrySet()) {
            String k = entry.getKey();
            // filter out env vars AND internal properties.
            if (k.startsWith(ENV_PREFIX) || k.startsWith(INTERNAL_PREFIX)) {
                continue;
            }
            //skip envs, these get filled in later.
            //#228901 since u21 we need to use cmd /c to execute on windows, quotes get escaped and when there is space in value, the value gets wrapped in quotes.
            String value = quote2apos(entry.getValue());
            String p = "-D" + entry.getKey() + "=" + value;
            String s = (Utilities.isWindows() && value.contains(" ") ? quote + p + quote : p);
            toRet.add(s);
        }

        //TODO based on a property? or UI option? can this backfire?
        //#224526 
        //execute in encoding that is based on project.build.sourceEncoding to have the output of exec:exec, surefire:test and others properly encoded.
        if (config.getMavenProject() != null) {
            String enc = config.getMavenProject().getProperties().getProperty(Constants.ENCODING_PROP);
            if (enc != null && !enc.equals(Charset.defaultCharset().name())) {
                toRet.add("-Dfile.encoding=" + enc);
            }
        }

        if (config.isOffline() != null && config.isOffline()) {
            toRet.add("--offline");//NOI18N
        }
        if (!config.isInteractive()) {
            toRet.add("--batch-mode"); //NOI18N
        }

        if (!config.isRecursive()) {
            toRet.add("--non-recursive");//NOI18N
        }
        if (config.isShowDebug()) {
            toRet.add("--debug");//NOI18N
        }
        if (config.isShowError()) {
            toRet.add("--errors");//NOI18N
        }
        if (config.isUpdateSnapshots()) {
            toRet.add("--update-snapshots");//NOI18N
        }
        if (config.getReactorStyle() != RunConfig.ReactorStyle.NONE) {
            File basedir = config.getExecutionDirectory();
            MavenProject mp = NbMavenProject.getPartialProject(config.getMavenProject());
            File projdir = mp == null || NbMavenProject.isErrorPlaceholder(mp) ? basedir : mp.getBasedir();
            String rel = basedir != null && projdir != null ? FileUtilities.relativizeFile(basedir, projdir) : null;
            if (!".".equals(rel)) {
                toRet.add(config.getReactorStyle() == RunConfig.ReactorStyle.ALSO_MAKE ? "--also-make" : "--also-make-dependents");
                toRet.add("--projects");
                toRet.add(rel != null ? rel : mp.getGroupId() + ':' + mp.getArtifactId());
            }
        }
        Object o = config.getInternalProperties().get("NbIde.configOverride"); // NOI18N
        if (o instanceof String) {
            toRet.add("--settings");
            toRet.add(o.toString());
        }
        String opts = MavenSettings.getDefault().getDefaultOptions();
        if (opts != null) {
            try {
                String[] s = CommandLineUtils.translateCommandline(opts);
                for (String one : s) {
                    one = one.trim();
                    if (one.startsWith("-D")) {
                        //check against the config.getProperties
                    } else {
                        if (!config.isShowDebug() && (one.equals("-X") || one.equals("--debug"))) {
                            continue;
                        }
                        if (!config.isShowError() && (one.equals("-e") || one.equals("--errors"))) {
                            continue;
                        }
                        if (!config.isUpdateSnapshots() && (one.equals("--update-snapshots") || one.equals("-U"))) {
                            continue;
                        }
                        if (config.isInteractive() && (one.equals("--batch-mode") || one.equals("-B"))) {
                            continue;
                        }
                        if ((config.isOffline() != null && !config.isOffline()) && (one.equals("--offline") || one.equals("-o"))) {
                            continue;
                        }
                    }
                    toRet.add(one);
                }

            } catch (Exception ex1) {
                Logger.getLogger(MavenSettings.class.getName()).log(Level.FINE, "Error parsing global options:{0}", opts);
            }

        }

        String profiles = "";//NOI18N

        for (Object profile : config.getActivatedProfiles()) {
            profiles = profiles + "," + profile;//NOI18N
        }
        if (profiles.length() > 0) {
            profiles = profiles.substring(1);
            toRet.add("-P" + profiles);//NOI18N
        }

        for (String goal : config.getGoals()) {
            toRet.add(goal);
        }

        return toRet;
    }
   
    /**
     * Quotes the parameter string using apostrohphes. As Maven does not understand \' escape in quoted string, work around by terminating single-quote
     * and pass the apostrophe as double-quoted single-character string, then open single-quote again.
     * @param s
     * @return quoted string
     */
    private static String quote2apos(String s) {
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i < s.length()) {
                    char c2 = s.charAt(i);
                    if (inQuote) {
                        if (c2 == '\'') {
                            sb.append("'\"'\"'");
                            continue;
                        } else if (c2 == '\"') {
                            sb.append(c2);
                            continue;
                        }
                    }
                    sb.append(c);
                    sb.append(c2);
                } else {
                    sb.append(c);
                }
            } else if (c == '\'') {
                if (inQuote) {
                    sb.append("'\"'\"'");
                } else {
                    inQuote = !inQuote;
                    sb.append('\'');
                }
            } else if (c == '"') {
                inQuote = !inQuote;
                sb.append('\'');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // tests only
    ProcessBuilder constructBuilder(final RunConfig clonedConfig, InputOutput ioput) {
        File javaHome = null;
        Map<String, String> envMap = new LinkedHashMap<>();
        for (Map.Entry<? extends String,? extends String> entry : clonedConfig.getProperties().entrySet()) {
            if (entry.getKey().startsWith(ENV_PREFIX)) {
                String env = entry.getKey().substring(ENV_PREFIX.length());
                envMap.put(env, entry.getValue());
                if (entry.getKey().equals(ENV_JAVAHOME)) {
                    javaHome = new File(entry.getValue());
                }
            }
        }
        if (javaHome == null) {
            if (clonedConfig.getProject() != null) {
                //TODO somehow use the config.getMavenProject() call rather than looking up the
                // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                ActiveJ2SEPlatformProvider javaprov = clonedConfig.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                File path;
                FileObject java = javaprov.getJavaPlatform().findTool("java"); //NOI18N
                if (java != null) {
                    Collection<FileObject> objs = javaprov.getJavaPlatform().getInstallFolders();
                    for (FileObject fo : objs) {
                        if (FileUtil.isParentOf(fo, java)) {
                            path = FileUtil.toFile(fo);
                            if (path != null) {
                                javaHome = path;
                                envMap.put(ENV_JAVAHOME.substring(ENV_PREFIX.length()), path.getAbsolutePath());
                            }
                            break;
                        }
                    }
                }
            }
            //#151559
            if (javaHome == null) {
                String envJH = System.getenv("JAVA_HOME");
                if (envJH != null) {
                    File f = new File(envJH);
                    if (!f.exists() || !new File(f, "bin" + File.separator + "java" + (Utilities.isWindows() ? ".exe" : "")).exists()) {
                        envJH = null; //#233452  ignore non existing JAVA_HOME
                    }
                }
                if (envJH == null) {
                    //NOI18N
                    javaHome = new File(System.getProperty("java.home"));
                    envMap.put("JAVA_HOME", javaHome.getAbsolutePath()); //NOI18N
                } else {
                    javaHome = new File(envJH);
                    envMap.put("JAVA_HOME", javaHome.getAbsolutePath()); //NOI18N
                }
            }
        }

        Constructor constructeur;
        File mavenHome = null;
        File wrapper = null;
        if (MavenSettings.getDefault().isPreferMavenWrapper()) {
            wrapper = searchMavenWrapper(config);
        }
        if (wrapper != null) {
            constructeur = new WrapperShellConstructor(wrapper);
        } else {
            mavenHome = EmbedderFactory.getEffectiveMavenHome();
            constructeur = new ShellConstructor(mavenHome);
        }
        
        List<String> cmdLine = createMavenExecutionCommand(clonedConfig, constructeur);

        //#228901 on windows, since u21 we must use cmd /c
        // the working format is ""C:\Users\mkleint\space in path\apache-maven-3.0.4\bin\mvn.bat"
        //-Dexec.executable=java -Dexec.args="-jar
        //C:\Users\mkleint\Documents\NetBeansProjects\JavaApplication13\dist\JavaApplication13.jar
        //-Dxx=\"space path\" -Dfoo=bar" exec:exec""
        if (cmdLine.get(0).equals("cmd")) {
            //merge all items after cmd /c into one string and quote it.
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = cmdLine.iterator();
            //sb.append("cmd.exe /c ");
            it.next(); //cmd
            it.next(); //c
            String m = it.next();

            sb.append(m);
            while (it.hasNext()) {
                sb.append(" ").append(it.next());
            }

            // NETBEANS-3251, NETBEANS-3254: 
            // JDK-8221858 (non public) / CVE-2019-2958 changed the way cmd 
            // command lines are verified and made it "difficult" to have embedded 
            // quotes in it, quotes that are needed for the mvn.bat and some
            // parameters of the goals being run (particularly exec:exec).
            // Setting the Maven command as an environment variable and
            // using the cmd.exe variables extention mechanism when launching
            // the command allows to bypass the new JDK check locally without 
            // resorting to using the global jdk.lang.Process.allowAmbiguousCommands flag
            envMap.put(NETBEANS_MAVEN_COMMAND_LINE, sb.toString());
            cmdLine = Arrays.asList(new String[] {
                "cmd", "/c", "%" + NETBEANS_MAVEN_COMMAND_LINE + "%"
            });
        }

        ProcessBuilder builder = new ProcessBuilder(cmdLine);
        builder.redirectErrorStream(true);
        builder.directory(clonedConfig.getExecutionDirectory());
        StringBuilder display = new StringBuilder("cd ").append(clonedConfig.getExecutionDirectory()).append("; "); // NOI18N
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String env = entry.getKey();
            String val = entry.getValue();
            if ("M2_HOME".equals(env.toUpperCase(Locale.ENGLISH))) {
                continue;// #191374: would prevent bin/mvn from using selected installation
            }
            // TODO: do we really put *all* the env vars there? maybe filter, M2_HOME and JDK_HOME?
            if (MavenExecuteUtils.isEnvRemovedValue(val)) {
                builder.environment().remove(env);
            } else {
                builder.environment().put(env, val);
            }
            if (!env.equals(CosChecker.NETBEANS_PROJECT_MAPPINGS)
                    && !env.equals(NETBEANS_MAVEN_COMMAND_LINE)) { //don't show to user
                display.append(Utilities.escapeParameters(new String[] {env + "=" + val})).append(' '); // NOI18N
            }
        }

        if (mavenHome != null) {
            //#195039
            builder.environment().put("M2_HOME", mavenHome.getAbsolutePath());
            if (!mavenHome.equals(EmbedderFactory.getDefaultMavenHome())) {
                //only relevant display when using the non-default maven installation.
                display.append(Utilities.escapeParameters(new String[] {"M2_HOME=" + mavenHome.getAbsolutePath()})).append(' '); // NOI18N
            }
        }

        // hide the bypass command and output the command as it used to be (before the bypass command was added)
        if (envMap.containsKey(NETBEANS_MAVEN_COMMAND_LINE)) {
            display.append(Utilities.escapeParameters(new String[] {"cmd", "/c", envMap.get("NETBEANS_MAVEN_COMMAND_LINE")}));
        }
        else {
            //very hacky here.. have a way to remove
            List<String> command = new ArrayList<>(builder.command());
            command.removeIf(s -> s.startsWith("-D" + CosChecker.MAVENEXTCLASSPATH + "="));
            display.append(Utilities.escapeParameters(command.toArray(new String[0])));
        }

        printGray(ioput, display.toString());

        return builder;
    }

    private static void printGray(InputOutput io, String text) {
        if (IOColorLines.isSupported(io)) {
            try {
                IOColorLines.println(io, text, IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            io.getOut().println(text);
        }
    }

    private void processIssue153101(IOException x, InputOutput ioput) {
        //#153101
        if (x.getMessage() != null && x.getMessage().contains("CreateProcess error=5")) {
            System.setProperty("maven.run.cmd", "true");
            LOGGER.log(Level.INFO, "Cannot create Process, next time we will run the build with 'cmd /c'", x); //NOI18N
            ioput.getErr().println("Cannot execute the mvn.bat executable directly due to wrong access rights, switching to execution via 'cmd.exe /c mvn.bat'."); //NOI18N - in maven output
            try {
                ioput.getErr().println("  See issue http://www.netbeans.org/issues/show_bug.cgi?id=153101 for details.", new OutputListener() {                    //NOI18N - in maven output
                    @Override
                    public void outputLineSelected(OutputEvent ev) {}
                    @Override
                    public void outputLineCleared(OutputEvent ev) {}
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        try {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.netbeans.org/issues/show_bug.cgi?id=153101")); //NOI18N - in maven output
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            ioput.getErr().println("  This message will show on the next start of the IDE again, to skip it, add -J-Dmaven.run.cmd=true to your etc/netbeans.conf file in your NetBeans installation."); //NOI18N - in maven output
            ioput.getErr().println("The detailed exception output is printed to the IDE's log file."); //NOI18N - in maven output
            RP.post(() -> {
                RunConfig newConfig = new BeanRunConfig(config);
                RunUtils.executeMaven(newConfig);
            });
        } else {
            ioput.getErr().println(x.getMessage());
        }
    }

    private void printCoSWarning(BeanRunConfig clonedConfig, InputOutput ioput) {
        if (clonedConfig.getProperties().containsKey(CosChecker.ENV_NETBEANS_PROJECT_MAPPINGS)) {
            printGray(ioput, "Running NetBeans Compile On Save execution. Phase execution is skipped and output directories of dependency projects (with Compile on Save turned on) will be used instead of their jar artifacts.");
            if (isMaven2()) {
                printGray(ioput, "WARNING: Using Maven 2.x for execution, NetBeans cannot establish links between current project and output directories of dependency projects with Compile on Save turned on. Only works with Maven 3.0+.");
            }

        }
        if (clonedConfig.getProperties().containsKey(ModelRunConfig.EXEC_MERGED)) {
            printGray(ioput, "\nDefault '" + clonedConfig.getActionName() + "' action exec.args merged with maven-exec-plugin arguments declared in pom.xml.");
        }

    }

    boolean isMaven2() {
        File mvnHome = EmbedderFactory.getEffectiveMavenHome();
        String version = MavenSettings.getCommandLineMavenVersion(mvnHome);
        return version != null && version.startsWith("2");
    }

    private boolean isMavenDaemon() {
        File mvnHome = EmbedderFactory.getEffectiveMavenHome();
        return MavenSettings.isMavenDaemon(Paths.get(mvnHome.getPath()));
    }

    private void injectEventSpy(final BeanRunConfig clonedConfig) {
        //TEMP 
        String mavenPath = clonedConfig.getProperties().get(CosChecker.MAVENEXTCLASSPATH);
        File jar = InstalledFileLocator.getDefault().locate("maven-nblib/netbeans-eventspy.jar", "org.netbeans.modules.maven", false);
        if (jar == null) {
            return;
        }
        if (mavenPath == null) {
            mavenPath = "";
        } else {
            String delimiter = Utilities.isWindows() ? ";" : ":";
            if(mavenPath.contains(jar + delimiter)) {
                // invoked by output view > rerun? see also issue #249971
                return;
            }
            mavenPath = delimiter + mavenPath;
        }
        //netbeans-eventspy.jar comes first on classpath
        mavenPath = jar.getAbsolutePath() + mavenPath;
        clonedConfig.setProperty(CosChecker.MAVENEXTCLASSPATH, mavenPath);
    }

    private boolean isMultiThreaded(BeanRunConfig clonedConfig) {

        List<String> params = new ArrayList<>();
        params.addAll(Arrays.asList(MavenSettings.getDefault().getDefaultOptions().split(" ")));
        params.addAll(clonedConfig.getGoals());
        if (clonedConfig.getPreExecution() != null) {
            params.addAll(clonedConfig.getPreExecution().getGoals());
        }

        return isMavenDaemon() ? isMultiThreadedMvnd(params)
                               : isMultiThreadedMaven(params);
    }

    // mvnd is MT by default
    static boolean isMultiThreadedMvnd(List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String p = params.get(i);
            if (p.equals("-1") || p.equals("--serial") || p.equals("-Dmvnd.serial")) { // "behave like standard maven" mode
                return false;
            }
            if (i + 1 < params.size() && (p.equals("-T") || p.equals("--threads"))) {
                if (params.get(i+1).equals("1")) {
                    return false;
                }
            }
            try {
                if (p.startsWith("-Dmvnd.threads=") && Integer.parseInt(p.substring(15)) == 1)  {
                    return false;
                }
            } catch (NumberFormatException ignored) {} 
        }
        return true;
    }

    // mvn is ST by default
    static boolean isMultiThreadedMaven(List<String> params) {
        for (int i = 0; i < params.size() - 1; i++) {
            String p = params.get(i);
            if ((p.equals("-T") || p.equals("--threads")) && !params.get(i+1).equals("1")) {
                return true;
            }
        }
        return false;
    }
    
    private File searchMavenWrapper(RunConfig config) {
        String fileName = Utilities.isWindows() ? "mvnw.cmd" : "mvnw"; //NOI18N
        MavenProject project = NbMavenProject.getPartialProject(config.getMavenProject());
        while (project != null) {
            File baseDir = project.getBasedir();
            if (baseDir != null) {
                File mvnw = new File(baseDir, fileName);
                if (mvnw.exists()) {
                    return mvnw;
                }
            }
            project = project.getParent();
        }
        return null;
    }

    // part copied from ShellConstructor - @TODO consolidate here
    private static class WrapperShellConstructor implements Constructor {

        private final @NonNull File wrapper;

        WrapperShellConstructor(@NonNull File wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public List<String> construct() {
            //#164234
            //if maven.bat file is in space containing path, we need to quote with simple quotes.
            String quote = "\"";
            List<String> toRet = new ArrayList<>();
            toRet.add(quoteSpaces(wrapper.getAbsolutePath(), quote));

            if (Utilities.isWindows()) { //#153101, since #228901 always on windows use cmd /c
                toRet.add(0, "/c"); //NOI18N
                toRet.add(0, "cmd"); //NOI18N
            }
            return toRet;
        }

        // we run the shell/bat script in the process, on windows we need to quote any spaces
        //once/if we get rid of shell/bat execution, we might need to remove this
        //#164234
        private static String quoteSpaces(String val, String quote) {
            if (Utilities.isWindows()) {
                //since #228901 always quote
                //#208065 not only space but a few other characters are to be quoted..
                //if (val.indexOf(' ') != -1 || val.indexOf('=') != -1 || val.indexOf(";") != -1 || val.indexOf(",") != -1) { //NOI18N
                return quote + val + quote;
                //}
            }
            return val;
        }

    }
}
