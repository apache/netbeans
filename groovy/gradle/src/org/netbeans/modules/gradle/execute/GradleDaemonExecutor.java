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

import org.netbeans.modules.gradle.GradleDaemon;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.GradleProgressListenerProvider;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Path;
import java.util.Collection;
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
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.io.ReaderInputStream;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleDaemonExecutor extends AbstractGradleExecutor {

    private CancellationTokenSource cancelTokenSource;
    private static final Logger LOGGER = Logger.getLogger(GradleDaemonExecutor.class.getName());

    private final ProgressHandle handle;
    private InputStream inStream;
    private OutputStream outStream;
    private OutputStream errStream;
    private boolean cancelling;

    @SuppressWarnings("LeakingThisInConstructor")
    public GradleDaemonExecutor(RunConfig config) {
        super(config);
        handle = ProgressHandleFactory.createHandle(config.getTaskDisplayName(), this, new AbstractAction() {

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
        "GRADLE_IO_ERROR=Gradle internal IO problem has been detected.\nThe running build may or may not have finished succesfully."
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

        ProjectConnection pconn = null;
        final InputOutput ioput = getInputOutput();
        actionStatesAtStart();
        handle.start();
        try {

            BuildExecutionSupport.registerRunningItem(item);
            if (GradleSettings.getDefault().isAlwaysShowOutput()) {
                ioput.select();
            }

            GradleConnector gconn = GradleConnector.newConnector();
            cancelTokenSource = GradleConnector.newCancellationTokenSource();
            File gradleInstall = RunUtils.evaluateGradleDistribution(config.getProject(), false);
            if (gradleInstall != null) {
                gconn.useInstallation(gradleInstall);
            } else {
                gconn.useBuildDistribution();
            }

            File projectDir = FileUtil.toFile(config.getProject().getProjectDirectory());
            //TODO: GradleUserHome
            pconn = gconn.forProjectDirectory(projectDir).connect();

            BuildLauncher buildLauncher = pconn.newBuild();
            GradleCommandLine cmd = config.getCommandLine();
            if (RunUtils.isAugmentedBuildEnabled(config.getProject())) {
                cmd = new GradleCommandLine(cmd);
                cmd.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, GradleDaemon.INIT_SCRIPT);
                cmd.addSystemProperty(GradleDaemon.PROP_TOOLING_JAR, GradleDaemon.TOOLING_JAR);
            }
            cmd.configure(buildLauncher, projectDir);

            printCommandLine();

            Pair<String, JavaPlatform> activePlatform = RunUtils.getActivePlatform(config.getProject());
            if (activePlatform.second() == null || !activePlatform.second().isValid()) {
                io.getErr().println(Bundle.NO_PLATFORM(activePlatform.first()));
                return;
            }
            if (!activePlatform.second().getInstallFolders().isEmpty()) {
                File javaHome = FileUtil.toFile(activePlatform.second().getInstallFolders().iterator().next());
                buildLauncher.setJavaHome(javaHome);
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
                handle.progress(pe.getDescription());
            });

            buildLauncher.withCancellationToken(cancelTokenSource.token());
            if (config.getProject() != null) {
                Collection<? extends GradleProgressListenerProvider> providers = config.getProject().getLookup().lookupAll(GradleProgressListenerProvider.class);
                for (GradleProgressListenerProvider provider : providers) {
                    buildLauncher.addProgressListener(provider.getProgressListener(), provider.getSupportedOperationTypes());
                }
            }
            buildLauncher.run();
            StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_SUCCESS(getProjectName()));
        } catch (BuildCancelledException ex) {
            showAbort();
        } catch (UncheckedException | BuildException ex) {
            if (!cancelling) {
                StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_FAILED(getProjectName()));
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
            if (pconn != null) {
                pconn.close();
            }
            closeInOutErr();
            checkForExternalModifications();
            handle.finish();
            markFreeTab();
            actionStatesAtFinish();
        }
    }

    private String getProjectName() {
        ProjectInformation info = ProjectUtils.getInformation(config.getProject());
        return info.getDisplayName();
    }

    private void printCommandLine() {
        StringBuilder commandLine = new StringBuilder(1024);

        JavaPlatform activePlatform = RunUtils.getActivePlatform(config.getProject()).second();
        if ((activePlatform != null) && activePlatform.isValid() && !activePlatform.getInstallFolders().isEmpty()) {
            File javaHome = FileUtil.toFile(activePlatform.getInstallFolders().iterator().next());
            commandLine.append("JAVA_HOME=\"").append(javaHome.getAbsolutePath()).append("\"\n"); //NOI18N
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
                commandLine.append(relRoot).append("/gradlew"); //NOI18N
            } else {
                File gradleDistribution = RunUtils.evaluateGradleDistribution(null, false);
                if (gradleDistribution != null) {
                    File gradle = new File(gradleDistribution, "bin/gradle"); //NOI18N
                    commandLine.append(gradle.getAbsolutePath());
                }
            }

        for (String arg : config.getCommandLine().getSupportedCommandLine()) {
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

}
