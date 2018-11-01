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
import org.netbeans.modules.gradle.options.GradleDistributionManager;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.GradleProgressListenerProvider;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleDaemonExecutor extends AbstractGradleExecutor {

    CancellationTokenSource cancelTokenSource;
    private static final Logger LOGGER = Logger.getLogger(GradleDaemonExecutor.class.getName());

    private final ProgressHandle handle;

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
        "BUILD_FAILED=Building {0} failed."
    })
    @Override
    public void run() {
        synchronized (taskSemaphore) {
            if (task == null) {
                try {
                    taskSemaphore.wait();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, "interrupted", ex);
                }
            }
        }

        ProjectConnection pconn = null;
        final InputOutput ioput = getInputOutput();
        actionStatesAtStart();
        handle.start();
        OutputStream outStream = null;
        OutputStream errStream = null;
        try {

            BuildExecutionSupport.registerRunningItem(item);
            if (GradleSettings.getDefault().isAlwaysShowOutput()) {
                ioput.select();
            }

            GradleConnector gconn = GradleConnector.newConnector();
            cancelTokenSource = GradleConnector.newCancellationTokenSource();

            File gradleDistribution = GradleDistributionManager.evaluateGradleDistribution();
            if (!GradleSettings.getDefault().isWrapperPreferred()) {
                gconn.useInstallation(gradleDistribution);
            }

            File projectDir = FileUtil.toFile(config.getProject().getProjectDirectory());
            //TODO: GradleUserHome
            pconn = gconn.forProjectDirectory(projectDir).connect();
            boolean useRichOutput = GradleSettings.getDefault().isUseRichOutput();

            // Unfortunately Rich output leaks a thread on Gradle 2.13
            String gradleVersion = GradleDistributionManager.getDistributionVersion(gradleDistribution);
            if (gradleVersion != null && gradleVersion.startsWith("2.13")) {
                useRichOutput = false;
            }

            BuildLauncher buildLauncher = pconn.newBuild();
            GradleCommandLine cmd = config.getCommandLine();
            if (RunUtils.isAugmentedBuildEnabled(config.getProject())) {
                cmd = new GradleCommandLine(cmd);
                cmd.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, GradleDaemon.INIT_SCRIPT);
                cmd.addSystemProperty(GradleDaemon.PROP_TOOLING_JAR, GradleDaemon.TOOLING_JAR);
            }
            cmd.configure(buildLauncher);
            JavaPlatform activePlatform = RunUtils.getActivePlatform(config.getProject());
            if (!activePlatform.getInstallFolders().isEmpty()) {
                File javaHome = FileUtil.toFile(activePlatform.getInstallFolders().iterator().next());
                buildLauncher.setJavaHome(javaHome);
            }
            buildLauncher.setColorOutput(useRichOutput);
            if (useRichOutput) {
                outStream = new EscapeProcessingOutputStream(new GradleColorEscapeProcessor(io, handle, config));
                buildLauncher.setStandardOutput(outStream);
            } else {
                outStream = new EscapeProcessingOutputStream(new GradlePlainEscapeProcessor(io, config, false));
                errStream = new EscapeProcessingOutputStream(new GradlePlainEscapeProcessor(io, config, true));
                buildLauncher.setStandardOutput(outStream);
                buildLauncher.setStandardError(errStream);
                buildLauncher.addProgressListener(new ProgressListener() {
                    @Override
                    public void statusChanged(ProgressEvent pe) {
                        handle.progress(pe.getDescription());
                    }
                });
            }
            buildLauncher.withCancellationToken(cancelTokenSource.token());
            if (config.getProject() != null) {
                Collection<? extends GradleProgressListenerProvider> providers = config.getProject().getLookup().lookupAll(GradleProgressListenerProvider.class);
                for (GradleProgressListenerProvider provider : providers) {
                    buildLauncher.addProgressListener(provider.getProgressListener(), provider.getSupportedOperationTypes());
                }
            }
            printCommandLine();
            buildLauncher.run();
            StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_SUCCESS(getProjectName()));
        } catch (BuildCancelledException ex) {
            try {
                IOColorPrint.print(io, "\nBUILD ABORTED\n", IOColors.getColor(io, IOColors.OutputType.ERROR)); //NOI18N
            } catch (IOException iex) {
            }
        } catch (UncheckedException | BuildException ex) {
            StatusDisplayer.getDefault().setStatusText(Bundle.BUILD_FAILED(getProjectName()));
            //TODO: Handle Cancelled builds
            // We just swallow BUILD FAILED exception silently
        } finally {
            if (pconn != null) {
                pconn.close();
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException iox) {
                }
            }
            if (errStream != null) {
                try {
                    errStream.close();
                } catch (IOException iox) {
                }
            }
            checkForExternalModifications();
            handle.finish();
            markFreeTab();
            actionStatesAtFinish();
            BuildExecutionSupport.registerFinishedItem(item);
        }
    }

    private String getProjectName() {
        ProjectInformation info = ProjectUtils.getInformation(config.getProject());
        return info.getDisplayName();
    }

    private void printCommandLine() {
        StringBuilder commandLine = new StringBuilder(1024);

        JavaPlatform activePlatform = RunUtils.getActivePlatform(config.getProject());
        if (!activePlatform.getInstallFolders().isEmpty()) {
            File javaHome = FileUtil.toFile(activePlatform.getInstallFolders().iterator().next());
            commandLine.append("JAVA_HOME=\"").append(javaHome.getAbsolutePath()).append("\"\n"); //NOI18N
        }
        File dir = FileUtil.toFile(config.getProject().getProjectDirectory());
        if (dir != null) {
            commandLine.append("cd ").append(dir.getAbsolutePath()).append("; "); //NOI18N
        }

        if (GradleSettings.getDefault().isWrapperPreferred()) {
            //TODO: Do a better job with wrapper.
            GradleBaseProject gbp = GradleBaseProject.get(config.getProject());
            Path rootPath = gbp.getRootDir().toPath();
            Path projectPath = gbp.getProjectDir().toPath();

            String relRoot = projectPath.relativize(rootPath).toString();
            relRoot = relRoot.isEmpty() ? "." : relRoot;
            commandLine.append(relRoot).append("/gradlew");
        } else {
            File gradleDistribution = GradleDistributionManager.evaluateGradleDistribution();
            File gradle = new File(gradleDistribution, "bin/gradle"); //NOI18N
            commandLine.append(gradle.getAbsolutePath());
        }

        for (String arg : config.getCommandLine().getSupportedCommandLine()) {
            commandLine.append(' ');
            if (arg.contains(" ")) { //NOI18N
                commandLine.append('"').append(arg).append('"');
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
            //TODO: Shall not happen...
        }
    }

    @Messages("LBL_ABORTING_BUILD=Aborting Build...")
    @Override
    public boolean cancel() {
        if (cancelTokenSource != null) {
            handle.switchToIndeterminate();
            handle.setDisplayName(Bundle.LBL_ABORTING_BUILD());
            cancelTokenSource.cancel();
        }
        return true;
    }

}
