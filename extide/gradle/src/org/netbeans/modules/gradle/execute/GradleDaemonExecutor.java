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

package org.netbeans.modules.gradle.execute;

import org.netbeans.modules.gradle.loaders.GradleDaemon;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.GradleProgressListenerProvider;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.gradle.internal.UncheckedException;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.GradleNetworkProxySupport.ProxyResult;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.execute.GradleDistributionProvider;
import org.netbeans.modules.gradle.spi.execute.GradleJavaPlatformProvider;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleDaemonExecutor extends AbstractGradleExecutor {
    private static final boolean DEBUG_GRADLE_BUILD_ACTION = Boolean.getBoolean("netbeans.debug.gradle.build.action"); //NOI18N
    private CancellationTokenSource cancelTokenSource;
    private static final Logger LOGGER = Logger.getLogger(GradleDaemonExecutor.class.getName());
    private static final String JAVA_HOME = "JAVA_HOME";    // NOI18N

    private final ProgressHandle handle;
    private InputStream inStream;
    private OutputStream outStream;
    private OutputStream errStream;
    private boolean cancelling;
    private GradleTask gradleTask;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public GradleDaemonExecutor(RunConfig config) {
        super(config);
        handle = ProgressHandle.createHandle(config.getTaskDisplayName(), this, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getInputOutput().select();
            }
        });
    }
    
    @NbBundle.Messages({
        "# {0} - Project name",
        "BUILD_SUCCESS=Building {0} was success.",
        "# {0} - Project name",
        "BUILD_FAILED=Building {0} failed.",
        "# {0} - Platform Key",
        "NO_PLATFORM=No valid Java Platform found for key: ''{0}''",
        "GRADLE_IO_ERROR=Gradle internal IO problem has been detected.\nThe running build may or may not have finished succesfully.",
        "# {0} - Gradle Version",
        "DOWNLOAD_GRADLE=Downloading Gradle {0}...",
        "# {0} - Gradle Version",
        "# {1} - Gradle Distribution URI",
        "DOWNLOAD_GRADLE_FAILED=Failed Downloading Gradle {0} from {1}",
    })
    @Override
    public void run() {
        synchronized (taskSemaphore) {
            if (task == null) {
                try {
                    taskSemaphore.wait();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, "interrupted", ex); //NOI18N
                }
            }
        }

        final InputOutput ioput = getInputOutput();
        actionStatesAtStart();
        handle.start();

        // BuildLauncher creates its own threads, need to note the effective Lookup and re-establish it in the listeners
        final Lookup execLookup = Lookup.getDefault();

        class ProgressLookupListener implements org.gradle.tooling.events.ProgressListener {
            private final org.gradle.tooling.events.ProgressListener delegate;

            public ProgressLookupListener(ProgressListener delegate) {
                this.delegate = delegate;
            }

            @Override
            public void statusChanged(org.gradle.tooling.events.ProgressEvent event) {
                Lookups.executeWith(execLookup, () -> delegate.statusChanged(event));
            }
        }

        try {

            BuildExecutionSupport.registerRunningItem(item);
            if (GradleSettings.getDefault().isAlwaysShowOutput()) {
                ioput.select();
            }
            cancelTokenSource = GradleConnector.newCancellationTokenSource();

            GradleDistributionProvider distProvider = config.getProject().getLookup().lookup(GradleDistributionProvider.class);
            GradleDistribution dist = distProvider != null ? distProvider.getGradleDistribution() : null;
            if ((dist != null) && !dist.isAvailable()) {
                try {
                    IOColorPrint.print(io, Bundle.DOWNLOAD_GRADLE(dist.getVersion()) + "\n",IOColors.getColor(io, IOColors.OutputType.LOG_WARNING));
                    try {
                        dist.install().get();
                    } catch(InterruptedException | ExecutionException ex) {
                        IOColorPrint.print(io, Bundle.DOWNLOAD_GRADLE_FAILED(dist.getVersion(), dist.getDistributionURI()),IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE));
                        throw new BuildException(Bundle.DOWNLOAD_GRADLE_FAILED(dist.getVersion(), dist.getDistributionURI()), ex);
                    }
                } catch (IOException ex) {}
            }
            ProjectConnection pconn = config.getProject().getLookup().lookup(ProjectConnection.class);

            BuildLauncher buildLauncher = pconn.newBuild();

            GradleCommandLine cmd = config.getCommandLine();

            GradleExecConfiguration cfg = config.getExecConfig();
            if (cfg == null) {
                cfg = ProjectConfigurationSupport.getEffectiveConfiguration(config.getProject(), Lookup.EMPTY);
            }
            if (cfg != null) {
                GradleCommandLine addConfigParts = null;
                
                if (cfg.getCommandLineArgs() != null && !cfg.getCommandLineArgs().isEmpty()) {
                    addConfigParts = new GradleCommandLine(cfg.getCommandLineArgs());
                }
                for (Map.Entry<String, String> pe : cfg.getProjectProperties().entrySet()) {
                    if (addConfigParts == null) {
                        addConfigParts = new GradleCommandLine();
                    }
                    addConfigParts.addProjectProperty(pe.getKey(), pe.getValue());
                }
                if (addConfigParts != null) {
                    cmd = GradleCommandLine.combine(addConfigParts, cmd);
                }
            }

            cmd = new GradleCommandLine(dist, cmd);
            
            // will not show augmented in the output
            GradleCommandLine augmented = cmd;

            if (RunUtils.isAugmentedBuildEnabled(config.getProject())) {
                augmented = new GradleCommandLine(cmd);
                augmented.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, GradleDaemon.initScript());
            }
            GradleBaseProject gbp = GradleBaseProject.get(config.getProject());
            augmented.configure(buildLauncher, gbp != null ? gbp.getRootDir() : null);

            printCommandLine(cmd);
            GradleJavaPlatformProvider platformProvider = config.getProject().getLookup().lookup(GradleJavaPlatformProvider.class);
            String runEnvironment = cmd.getProperty(GradleCommandLine.Property.PROJECT, "runEnvironment");
            boolean success = setPlatformAndEnv(buildLauncher, platformProvider, runEnvironment);
            if (!success) {
                return;
            }

            outStream = new EscapeProcessingOutputStream(new GradlePlainEscapeProcessor(io, config, false));
            errStream = new EscapeProcessingOutputStream(new GradlePlainEscapeProcessor(io, config, true));
            try {
                inStream = new ReaderInputStream(io.getIn(), "UTF-8"); //NOI18N
                buildLauncher.setStandardInput(inStream);
            } catch (IOException ex) {
                // Unlikely but worst case we do not have the input set.
            }
            buildLauncher.setStandardOutput(outStream);
            buildLauncher.setStandardError(errStream);
            buildLauncher.addProgressListener((ProgressEvent pe) -> {
                Lookups.executeWith(execLookup, () -> handle.progress(pe.getDescription()));
            });

            buildLauncher.withCancellationToken(cancelTokenSource.token());
            if (config.getProject() != null) {
                Collection<? extends GradleProgressListenerProvider> providers = config.getProject().getLookup().lookupAll(GradleProgressListenerProvider.class);
                for (GradleProgressListenerProvider provider : providers) {
                    buildLauncher.addProgressListener(new ProgressLookupListener(provider.getProgressListener()), provider.getSupportedOperationTypes());
                }
            }
            GradleExecAccessor.instance().configureGradleHome(buildLauncher);
            GradleNetworkProxySupport proxySupport = config.getProject().getLookup().lookup(GradleNetworkProxySupport.class);
            if (proxySupport != null) {
                try {
                    ProxyResult result = proxySupport.checkProxySettings().get();
                    if (result.getStatus() == GradleNetworkProxySupport.Status.ABORT) {
                        showAbort();
                        return;
                    }
                    buildLauncher = result.configure(buildLauncher);
                } catch (InterruptedException | ExecutionException ex) {
                    throw new BuildCancelledException("Interrupted", ex);
                }
            }
            if (DEBUG_GRADLE_BUILD_ACTION) {
                buildLauncher.addJvmArguments("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5008");
            }
            buildLauncher.run();
            StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_SUCCESS(getProjectName()));
            gradleTask.finish(0);
        } catch (BuildCancelledException ex) {
            showAbort();
        } catch (UncheckedException | BuildException ex) {
            if (!cancelling) {
                StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_FAILED(getProjectName()));
                gradleTask.finish(1);
            } else {
                // This can happen if cancelling a Gradle build which is running
                // an external aplication
                showAbort();
            }
        } catch (GradleConnectionException ex) {
            Throwable th = ex.getCause();
            boolean handled = false;
            while (th != null && !handled) {
                if (th instanceof StreamCorruptedException) {
                    LOGGER.log(Level.INFO, "Suspecting Gradle Serialization IO Error:", ex);
                    try {
                        IOColorPrint.print(io, Bundle.GRADLE_IO_ERROR(), IOColors.getColor(io, IOColors.OutputType.LOG_WARNING));
                    } catch (IOException iex) {
                    }
                    handled = true;
                }
                th = th.getCause();
            }
            if (!handled) throw ex;
        } finally {
            BuildExecutionSupport.registerFinishedItem(item);
            closeInOutErr();
            checkForExternalModifications();
            handle.finish();
            markFreeTab();
            actionStatesAtFinish();
        }
    }

    @NbBundle.Messages({"# {0} - JAVA_HOME", "# {1} - Java platform path", "MSG_JAVA_HOME_EnvWarning=Warning: {0} environment variable is replaced with the current Java platform path {1}."})
    private boolean setPlatformAndEnv(BuildLauncher buildLauncher, GradleJavaPlatformProvider platformProvider, String runEnvironment) {
        String javaHome = null;
        if (platformProvider != null) {
            try {
                javaHome = platformProvider.getJavaHome().getCanonicalPath();
            } catch (IOException ex) {
                io.getErr().println(Bundle.NO_PLATFORM(ex.getMessage()));
                gradleTask.finish(1);
                return false;
            }
        }
        if (javaHome != null || runEnvironment != null) {
            Map<String, String> envs = new HashMap<>(System.getenv());
            if (runEnvironment != null) {
                // Quoted space-separated expressions of <ENV_VAR>=<ENV_VALUE>
                // to set environment variables,
                // or !<ENV_VAR> to remove environment variables
                for (String env : BaseUtilities.parseParameters(runEnvironment)) {
                    String name = null;
                    if (env.startsWith("!")) {  // NOI18N
                        name = env.substring(1);
                        envs.remove(name);
                    } else {
                        int i = env.indexOf('=');   // NOI18N
                        if (i > 0) {
                            name = env.substring(0, i);
                            envs.put(name, env.substring(i + 1));
                        }
                    }
                    if (javaHome != null && JAVA_HOME.equals(name)) {
                        io.getErr().println(Bundle.MSG_JAVA_HOME_EnvWarning(JAVA_HOME, javaHome));
                    }
                }
            }
            if (javaHome != null) {
                envs.put(JAVA_HOME, javaHome);    // NOI18N
            }
            buildLauncher.setEnvironmentVariables(envs);
        }
        return true;
    }

    private String getProjectName() {
        ProjectInformation info = ProjectUtils.getInformation(config.getProject());
        return info.getDisplayName();
    }

    private void printCommandLine(GradleCommandLine cmd) {
        StringBuilder commandLine = new StringBuilder(1024);

        String userHome = GradleSettings.getDefault().getPreferences().get(GradleSettings.PROP_GRADLE_USER_HOME, null);
        if (userHome != null) {
            commandLine.append("GRADLE_USER_HOME=\"").append(userHome).append("\"\n"); //NOI18N
        }
        GradleJavaPlatformProvider platformProvider = config.getProject().getLookup().lookup(GradleJavaPlatformProvider.class);
        if (platformProvider != null) {
            try {
                File javaHome = platformProvider.getJavaHome();
                commandLine.append("JAVA_HOME=\"").append(javaHome.getAbsolutePath()).append("\"\n"); //NOI18N
            } catch (FileNotFoundException ex) {}
        }
        File dir = FileUtil.toFile(config.getProject().getProjectDirectory());
        if (dir != null) {
            commandLine.append("cd ").append(dir.getAbsolutePath()).append("; "); //NOI18N
        }

        GradleBaseProject gbp = GradleBaseProject.get(config.getProject());
        if (gbp != null
                && new GradleFiles(gbp.getProjectDir(), true).hasWrapper()
                && GradleSettings.getDefault().isWrapperPreferred()) {

                Path rootPath = gbp.getRootDir().toPath();
                Path projectPath = gbp.getProjectDir().toPath();

                String relRoot = projectPath.relativize(rootPath).toString();
                relRoot = relRoot.isEmpty() ? "." : relRoot;
                commandLine.append(relRoot).append(gradlewExecutable());
            } else {
                GradleDistributionProvider pvd = config.getProject().getLookup().lookup(GradleDistributionProvider.class);
                GradleDistribution dist = pvd != null ? pvd.getGradleDistribution() : null;
                if (dist != null) {
                    File gradle = new File(dist.getDistributionDir(), gradleExecutable());
                    commandLine.append(gradle.getAbsolutePath());
                }
            }

        for (String arg : cmd.getSupportedCommandLine()) {
            commandLine.append(' ');
            if (arg.contains(" ") || arg.contains("*")) { //NOI18N
                commandLine.append('\'').append(arg).append('\'');
            } else {
                commandLine.append(arg);
            }
        }
        commandLine.append('\n');
        try {
            if (IOColorPrint.isSupported(io)) {
                IOColorPrint.print(io, commandLine, IOColors.getColor(io, IOColors.OutputType.INPUT));
            } else {
                io.getOut().print(commandLine);
            }
        } catch (IOException ex) {
            // Shall not happen...
        }
    }

    private synchronized void closeInOutErr() {
        if (inStream != null) try {inStream.close();} catch (IOException ex) {}
        if (outStream != null) try {outStream.close();} catch (IOException ex) {}
        if (errStream != null) try {errStream.close();} catch (IOException ex)  {}
    }

    @NbBundle.Messages("TXT_BUILD_ABORTED=\nBUILD ABORTED\n")
    private void showAbort() {
        try {
            IOColorPrint.print(io, Bundle.TXT_BUILD_ABORTED(), IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
        } catch (IOException ex) {
        }
    }

    @Messages("LBL_ABORTING_BUILD=Aborting Build...")
    @Override
    public boolean cancel() {
        if (!cancelling && (cancelTokenSource != null)) {
            handle.switchToIndeterminate();
            handle.setDisplayName(Bundle.LBL_ABORTING_BUILD());
            // Closing out and err streams to prevent ambigous output NETBEANS-2038
            closeInOutErr();
            cancelling = true;
            cancelTokenSource.cancel();
            return true;
        }
        return false;
    }

    private static String gradlewExecutable() {
        return Utilities.isWindows() ? "\\gradlew.bat" : "/gradlew"; //NOI18N
    }

    private static String gradleExecutable() {
        return Utilities.isWindows() ? "bin\\gradle.bat" : "bin/gradle"; //NOI18N
    }

    // TODO one of the two methods can likely go from the API
    // setTask is called first and signals the runnable to start
    // the started runnable requires (!) the gradleTask so it can't be created in createTask
    @Override
    public void setTask(ExecutorTask task) {
        assert gradleTask == null;
        gradleTask = new GradleTask(task);
        super.setTask(task);
    }

    @Override
    public final ExecutorTask createTask(ExecutorTask process) {
        assert gradleTask != null;
        assert task == process;
        return gradleTask;
    }

    // task which can finish early, like a CompletableFuture
    private static final class GradleTask extends ExecutorTask {

        private final ExecutorTask delegate;
        private volatile Integer result;
        // is 0 when wrapper or delegate finished
        private final CountDownLatch doneSignal = new CountDownLatch(1);
        
        GradleTask(ExecutorTask delegate) {
            super(() -> {});
            this.delegate = delegate;
            this.delegate.addTaskListener(t -> doneSignal.countDown());
        }

        @Override
        public void stop() {
            delegate.stop();
        }

        @Override
        public int result() {
            waitFinished();
            return result != null ? result : delegate.result();
        }

        @Override
        public InputOutput getInputOutput() {
            return delegate.getInputOutput();
        }

        // FIXME isFinished() is final... this is still broken

        @Override
        public boolean waitFinished(long milliseconds) throws InterruptedException {
            return doneSignal.await(milliseconds, TimeUnit.MILLISECONDS);
        }

        @Override
        public void waitFinished() {
            try {
                doneSignal.await();
            } catch (InterruptedException ex) {}
        }

        public void finish(int result) {
            this.result = result;
            doneSignal.countDown();
            notifyFinished();
        }
    }
}
