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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
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
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * support for executing maven, externally on the command line.
 * @author  Milos Kleint (mkleint@codehaus.org)
 */
public class MavenCommandLineExecutor extends AbstractMavenExecutor {
    static final String ENV_PREFIX = "Env."; //NOI18N
    static final String ENV_JAVAHOME = "Env.JAVA_HOME"; //NOI18N

    private static final String KEY_UUID = "NB_EXEC_MAVEN_PROCESS_UUID"; //NOI18N
    
    private static final String NETBEANS_MAVEN_COMMAND_LINE = "NETBEANS_MAVEN_COMMAND_LINE"; //NOI18N
    
    private Process process;
    private String processUUID;
    private Process preProcess;
    private String preProcessUUID;
    private static final SpecificationVersion VER17 = new SpecificationVersion("1.7"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(MavenCommandLineExecutor.class.getName());
    
    private static final RequestProcessor RP = new RequestProcessor(MavenCommandLineExecutor.class.getName(),1);
    
    private final static RequestProcessor UPDATE_INDEX_RP = new RequestProcessor(RunUtils.class.getName(), 5);
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
        public ExecutorTask execute(RunConfig config, InputOutput io, TabContext tc) {
            LifecycleManager.getDefault().saveAll();
            MavenExecutor exec = new MavenCommandLineExecutor(config, io, tc);
            ExecutorTask task = ExecutionEngine.getDefault().execute(config.getTaskDisplayName(), exec, new ProxyNonSelectableInputOutput(exec.getInputOutput()));
            exec.setTask(task);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task t) {
                    MavenProject mp = config.getMavenProject();
                    if (mp == null) {
                        return;
                    }
                    final List<Artifact> arts = new ArrayList<Artifact>();
                    Artifact main = mp.getArtifact();
                    if (main != null) {
                        arts.add(main);
                    }
                    arts.addAll(mp.getArtifacts());
                    UPDATE_INDEX_RP.post(new Runnable() {
                        @Override
                        public void run() {
                            RepositoryIndexer.updateIndexWithArtifacts(RepositoryPreferences.getInstance().getLocalRepository(), arts);
                        }
                    });
                }
            });
            return task;
        }
    }
    
    public MavenCommandLineExecutor(RunConfig conf, InputOutput io, TabContext tc) {
        super(conf, tc);
        this.io = io;
    }
    
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
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(clonedConfig.getTaskDisplayName(), this, new AbstractAction() {

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
                preProcess = builder.start();
                out.setStdOut(preProcess.getInputStream());
                out.setStdIn(preProcess.getOutputStream());
                executionresult = preProcess.waitFor();
                out.waitFor();
                if (executionresult != 0) {
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
            process = builder.start();
            out.setStdOut(process.getInputStream());
            out.setStdIn(process.getOutputStream());
            executionresult = process.waitFor();
            out.waitFor();
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
                RP.post(new Runnable() { //#103460
                    @Override
                    public void run() {
                        //TODO we eventually know the coordinates of all built projects via EventSpy.
                        if (clonedConfig.getProject() != null) {
                            NbMavenProject.fireMavenProjectReload(clonedConfig.getProject());
                        }
                    }
                });
                
            }
            
        }
    }

    private boolean isEventSpyCompatible(final BeanRunConfig clonedConfig) {
        // EventSpy cannot work on jdk < 7
        if (clonedConfig.getProject() != null) {
            ActiveJ2SEPlatformProvider javaprov = clonedConfig.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            JavaPlatform platform = javaprov.getJavaPlatform();
            return (platform.getSpecification().getVersion().compareTo(VER17) >= 0);
        } else {
            return true;
        }
    }

    private void kill(Process prcs, String uuid) {
        Map<String, String> env = new HashMap<String, String>();
        env.put(KEY_UUID, uuid);
        Processes.killTree(prcs, env);
    }
    
    @Override
    public boolean cancel() {
        final Process pre = preProcess;
        preProcess = null;
        final Process pro = process;
        process = null;
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (pre != null) {
                    kill(pre, preProcessUUID);
                }
                if (pro != null) {
                    kill(pro, processUUID);
                }
            }
        });
        return true;
    }
        
    private static List<String> createMavenExecutionCommand(RunConfig config, Constructor base) {
        List<String> toRet = new ArrayList<String>(base.construct());

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
        String escaped = "\\" + quote;        
        for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
            if (!entry.getKey().startsWith(ENV_PREFIX)) {
                //skip envs, these get filled in later.
                //#228901 since u21 we need to use cmd /c to execute on windows, quotes get escaped and when there is space in value, the value gets wrapped in quotes.
                String value = (Utilities.isWindows() ? entry.getValue().replace(quote, escaped) : entry.getValue().replace(quote, "'"));
                if (Utilities.isWindows() && value.endsWith("\"")) {
                    //#201132 property cannot end with 2 double quotes, add a space to the end after our quote to prevent the state
                    value = value + " ";
                }
                String s = "-D" + entry.getKey() + "=" + (Utilities.isWindows() && value.contains(" ") ? quote + value + quote : value);            
                toRet.add(s);
            }
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

        if (config.isOffline() != null && config.isOffline().booleanValue()) {
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
            MavenProject mp = config.getMavenProject();
            File projdir = NbMavenProject.isErrorPlaceholder(mp) ? basedir : mp.getBasedir();
            String rel = basedir != null && projdir != null ? FileUtilities.relativizeFile(basedir, projdir) : null;
            if (!".".equals(rel)) {
                toRet.add(config.getReactorStyle() == RunConfig.ReactorStyle.ALSO_MAKE ? "--also-make" : "--also-make-dependents");
                toRet.add("--projects");
                toRet.add(rel != null ? rel : mp.getGroupId() + ':' + mp.getArtifactId());
            }
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
                        if ((config.isOffline() != null && !config.isOffline().booleanValue()) && (one.equals("--offline") || one.equals("-o"))) {
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

    private ProcessBuilder constructBuilder(final RunConfig clonedConfig, InputOutput ioput) {
        File javaHome = null;
        Map<String, String> envMap = new LinkedHashMap<String, String>();
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

        File mavenHome = EmbedderFactory.getEffectiveMavenHome();
        if (MavenSettings.getDefault().isUseBestMaven()) {
            File n = guessBestMaven(clonedConfig, ioput);
            if (n != null) {
                mavenHome = n;
            }
        }
        Constructor constructeur = new ShellConstructor(mavenHome);

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
            builder.environment().put(env, val);
            if (!env.equals(CosChecker.NETBEANS_PROJECT_MAPPINGS)
                && !env.equals(NETBEANS_MAVEN_COMMAND_LINE)) { //don't show to user
                display.append(Utilities.escapeParameters(new String[] {env + "=" + val})).append(' '); // NOI18N
            }
        }
       
        //#195039
        builder.environment().put("M2_HOME", mavenHome.getAbsolutePath());
        if (!mavenHome.equals(EmbedderFactory.getDefaultMavenHome())) {
            //only relevant display when using the non-default maven installation.
            display.append(Utilities.escapeParameters(new String[] {"M2_HOME=" + mavenHome.getAbsolutePath()})).append(' '); // NOI18N
        }

        // hide the bypass command and output the command as it used to be (before the bypass command was added)
        if (envMap.containsKey(NETBEANS_MAVEN_COMMAND_LINE)) {
            display.append(Utilities.escapeParameters(new String[] {"cmd", "/c", envMap.get("NETBEANS_MAVEN_COMMAND_LINE")}));
        }
        else {
            //very hacky here.. have a way to remove
            List<String> command = new ArrayList<String>(builder.command());
            for (Iterator<String> it = command.iterator(); it.hasNext();) {
                String s = it.next();
                if (s.startsWith("-D" + CosChecker.MAVENEXTCLASSPATH + "=")) {
                    it.remove();
                }
            }
            display.append(Utilities.escapeParameters(command.toArray(new String[command.size()])));
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
            RP.post(new Runnable() {
                @Override
                public void run() {
                    RunConfig newConfig = new BeanRunConfig(config);
                    RunUtils.executeMaven(newConfig);
                }
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

    private void injectEventSpy(final BeanRunConfig clonedConfig) {
        //TEMP 
        String mavenPath = clonedConfig.getProperties().get(CosChecker.MAVENEXTCLASSPATH);
        File jar = InstalledFileLocator.getDefault().locate("maven-nblib/netbeans-eventspy.jar", "org.netbeans.modules.maven", false);
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
        String list = MavenSettings.getDefault().getDefaultOptions();
        for (String s : clonedConfig.getGoals()) {
            list = list + " " + s;
        }
        if (clonedConfig.getPreExecution() != null) {
            for (String s : clonedConfig.getPreExecution().getGoals()) {
                list = list + " " + s;
            }
        }
        return list.contains("-T") || list.contains("--threads");
    } 

    private File guessBestMaven(RunConfig clonedConfig, InputOutput ioput) {
        MavenProject mp = clonedConfig.getMavenProject();
        if (mp != null) {
            if (mp.getPrerequisites() != null) {
                Prerequisites pp = mp.getPrerequisites();
                String ver = pp.getMaven();
                if (ver != null) {
                    return checkAvailability(ver, null, ioput);
                }
            }
            String value = PluginPropertyUtils.getPluginPropertyBuildable(clonedConfig.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, "maven-enforcer-plugin", "enforce", new PluginPropertyUtils.ConfigurationBuilder<String>() {
                @Override
                public String build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
                    if(configRoot != null) {
                        Xpp3Dom rules = configRoot.getChild("rules");
                        if (rules != null) {
                            Xpp3Dom rmv = rules.getChild("requireMavenVersion");
                            if (rmv != null) {
                                Xpp3Dom v = rmv.getChild("version");
                                if (v != null) {
                                    return v.getValue();
                                }
                            }
                        }
                    }
                    return null;
                }
            });
            if (value != null) {
                if (value.contains("[") || value.contains("(")) {
                    try {
                        VersionRange vr = VersionRange.createFromVersionSpec(value);
                        return checkAvailability(null, vr, ioput);
                    } catch (InvalidVersionSpecificationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    return checkAvailability(value, null, ioput);
                }
            }
        }
        return null;
    }

    private File checkAvailability(String ver, VersionRange vr, InputOutput ioput) {
        ArrayList<String> all = new ArrayList(MavenSettings.getDefault().getUserDefinedMavenRuntimes());
        //TODO this could be slow? but is it slower than downloading stuff?
        //is there a faster way? or can we somehow log the findings after first attempt?
        DefaultArtifactVersion candidate = null;
        File candidateFile = null;
        for (String one : all) {
            File f = FileUtil.normalizeFile(new File(one));
            String oneVersion = MavenSettings.getCommandLineMavenVersion(f);
            if(oneVersion == null) {
                continue;
            }
            if (ver != null && ver.equals(oneVersion)) {
                return f;
            }
            DefaultArtifactVersion dav = new DefaultArtifactVersion(oneVersion);
            if (vr != null && vr.containsVersion(dav)) {
                if (candidate != null) {
                    if (candidate.compareTo(dav) < 0) {
                        candidate = dav;
                        candidateFile = f;
                    }
                } else {
                    candidate = new DefaultArtifactVersion(oneVersion);
                    candidateFile = f;
                }
            }
        }
        if (candidateFile != null) {
            return candidateFile;
        } else if (vr != null) {
            ver = vr.getRecommendedVersion() != null ? vr.getRecommendedVersion().toString() : null;
            if (ver == null) {
                //TODO can we figure out which version to get without hardwiring a list of known versions?
                ioput.getOut().println("NetBeans: No match and no recommended version for version range " + vr.toString());
                return null;
            }
        }
        if (ver == null) {
            return null;
        }
        
        File f = getAltMavenLocation(); 
        File child = FileUtil.normalizeFile(new File(f, "apache-maven-" + ver));
        if (child.exists()) {
            return child;
        } else {
            f.mkdirs();
            ioput.getOut().println("NetBeans: Downloading and unzipping Maven version " + ver);
            ZipInputStream str = null;
            try {
                //this url pattern works for all versions except the last one 3.2.3
                //which is only under <mirror>/apache/maven/maven-3/3.2.3/binaries/
                URL[] urls = new URL[] {new URL("http://archive.apache.org/dist/maven/binaries/apache-maven-" + ver + "-bin.zip"),
                                        new URL("http://archive.apache.org/dist/maven/maven-3/" + ver + "/binaries/apache-maven-" + ver + "-bin.zip")};
                InputStream is = null;
                for (URL u : urls) {
                    try {
                        is = u.openStream();
                        break;
                    } catch (FileNotFoundException e) {
                        // try next url
                    }
                }
                if(is == null) {
                    LOGGER.log(Level.WARNING, "wasn''t able to download maven binaries, version {0}", ver);
                    return null;
                }
                str = new ZipInputStream(is);
                ZipEntry entry;
                while ((entry = str.getNextEntry()) != null) {
                    //base it of f not child as the zip contains the maven base folder
                    File fileOrDir = new File(f,  entry.getName());
                    if (entry.isDirectory()) {
                        fileOrDir.mkdirs();
                    } else {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(fileOrDir);
                            FileUtil.copy(str, fos);
                        } finally {
                            IOUtil.close(fos);
                        }
                        // correct way to set executable flag?
                        if ("bin".equals(fileOrDir.getParentFile().getName()) && !fileOrDir.getName().endsWith(".conf")) {
                            fileOrDir.setExecutable(true);
                        }
                    }
                }
                if (!all.contains(child.getAbsolutePath())) {
                    all.add(child.getAbsolutePath());
                    MavenSettings.getDefault().setMavenRuntimes(all);
                }
                return child;
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                try {
                    FileUtils.deleteDirectory(child);
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                try {
                    FileUtils.deleteDirectory(child);
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            } finally {
                IOUtil.close(str);
            }
        }
        return null;
    }

    private File getAltMavenLocation() {
        if (MavenSettings.getDefault().isUseBestMavenAltLocation()) {
            String s = MavenSettings.getDefault().getBestMavenAltLocation();
            if (s != null && s.trim().length() > 0) {
                return FileUtil.normalizeFile(new File(s));
            }
        }
        return Places.getCacheSubdirectory("downloaded-mavens");
    }
}
