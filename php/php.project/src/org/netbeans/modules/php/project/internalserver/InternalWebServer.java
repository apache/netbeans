/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.project.internalserver;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigInternal;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigInternalValidator;
import org.netbeans.modules.php.project.ui.actions.support.FileRunner;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Manager of internal web server (available in PHP 5.4+)
 * for the given {@link PhpProject}.
 */
public final class InternalWebServer implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(InternalWebServer.class.getName());

    // @GuardedBy("InternalWebServer.class")
    private static InternalWebServer runningInstance;

    private static final String WEB_SERVER_PARAM = "-S"; // NOI18N
    private static final String DOCUMENT_ROOT_PARAM = "-t"; // NOI18N

    private static final Set<String> RELATED_EVENT_NAMES = new HashSet<>(Arrays.asList(
            PhpProject.PROP_WEB_ROOT,
            PhpProjectProperties.HOSTNAME,
            PhpProjectProperties.PORT,
            PhpProjectProperties.ROUTER));

    private final PhpProject project;

    // @GuardedBy(this)
    private Future<Integer> process = null;


    private InternalWebServer(PhpProject project) {
        this.project = project;
    }

    public static InternalWebServer createForProject(PhpProject project) {
        InternalWebServer server = new InternalWebServer(project);
        // listen to changes in project.properties
        ProjectPropertiesSupport.getPropertyEvaluator(project).addPropertyChangeListener(server);
        // listen to changes in webroot
        ProjectPropertiesSupport.addProjectPropertyChangeListener(project, server);
        return server;
    }

    // #207763
    @NbBundle.Messages("InternalWebServer.error.stop=Timeout occured while stopping PHP built-in web server.")
    static synchronized void startingInstance(InternalWebServer serverToBeStarted) {
        if (runningInstance != null
                && runningInstance != serverToBeStarted) {
            InternalWebServer instanceRef = runningInstance;
            if (!ensureServerStopped(instanceRef)) {
                warnUser(instanceRef.project.getName(), Bundle.InternalWebServer_error_stop());
            }
        }
        runningInstance = serverToBeStarted;
    }

    static synchronized void stoppingInstance(InternalWebServer serverToBeStopped) {
        if (serverToBeStopped == runningInstance) {
            runningInstance = null;
        }
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "InternalWebServer.stopping=Stopping PHP built-in web server for project {0}..."
    })
    @SuppressWarnings("SleepWhileHoldingLock")
    private static boolean ensureServerStopped(InternalWebServer instance) {
        assert !EventQueue.isDispatchThread();
        ProgressHandle progressHandle = ProgressHandle.createHandle(Bundle.InternalWebServer_stopping(instance.project.getName()));
        try {
            progressHandle.start();
            // stop server
            instance.stop();
            // wait for shutdown
            RunConfigInternal runConfig = RunConfigInternal.forProject(instance.project);
            String host = runConfig.getHostname();
            int port = Integer.parseInt(runConfig.getPort());
            for (int i = 0; i < 20; ++i) {
                try {
                    Socket socket = new Socket(host, port);
                    socket.close();
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (UnknownHostException ex) {
                    return true;
                } catch (IOException ex) {
                    return true;
                }
            }
            return false;
        } finally {
            progressHandle.finish();
        }
    }

    private static void warnUser(String title, String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION));
    }

    public synchronized boolean isRunning() {
        return process != null && !process.isDone();
    }

    public synchronized boolean start() {
        if (isRunning()) {
            LOGGER.log(Level.FINE, "Internal web server already running for project {0}", project.getName());
            return true;
        }
        process = createProcess();
        return isRunning();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "InternalWebServer.error.cancelProcess=Cannot cancel running internal web server for project {0}."
    })
    public synchronized void stop() {
        if (isRunning()
                && !process.cancel(true)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    Bundle.InternalWebServer_error_cancelProcess(project.getName()),
                    NotifyDescriptor.WARNING_MESSAGE));
        }
        stoppingInstance(this);
        reset();
    }

    public synchronized void restart() {
        stop();
        start();
    }

    void reset() {
        assert Thread.holdsLock(this);
        process = null;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "InternalWebServer.output.title=Internal WebServer ({0})"
    })
    private Future<Integer> createProcess() {
        // validate
        PhpInterpreter phpInterpreter;
        try {
            phpInterpreter = PhpInterpreter.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            return null;
        }
        RunConfigInternal runConfig = RunConfigInternal.forProject(project);
        if (RunConfigInternalValidator.validateCustomizer(runConfig) != null) {
            PhpProjectUtils.openCustomizerRun(project);
            return null;
        }
        // run
        return new PhpExecutable(phpInterpreter.getInterpreter())
                .viaAutodetection(false)
                .viaPhpInterpreter(false)
                .workDir(runConfig.getWorkDir())
                .additionalParameters(getParameters(runConfig))
                .displayName(Bundle.InternalWebServer_output_title(project.getName()))
                .run(getDescriptor());
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (!isRunning()) {
            return;
        }
        if (RELATED_EVENT_NAMES.contains(evt.getPropertyName())) {
            restart();
        }
    }

    @Override
    public String toString() {
        return "InternalWebServer[" + project.getName() + "]"; // NOI18N
    }

    private List<String> getParameters(RunConfigInternal runConfig) {
        List<String> params = new ArrayList<>(3);
        params.add(WEB_SERVER_PARAM);
        params.add(runConfig.getServer());
        String relativeDocumentRoot = runConfig.getRelativeDocumentRoot();
        if (relativeDocumentRoot != null) {
            params.add(DOCUMENT_ROOT_PARAM);
            params.add(relativeDocumentRoot);
        }
        String routerRelativePath = runConfig.getRouterRelativePath();
        if (StringUtils.hasText(routerRelativePath)) {
            params.add(routerRelativePath);
        }
        return params;
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(UiUtils.OPTIONS_PATH + "/" + UiUtils.GENERAL_OPTIONS_SUBCATEGORY) // NOI18N
                .outConvertorFactory(FileRunner.PHP_LINE_CONVERTOR_FACTORY)
                .preExecution(new Runnable() {
                    @Override
                    public void run() {
                        // needs to be called even from the output window (rerun button)
                        startingInstance(InternalWebServer.this);
                    }
                });
    }

}
