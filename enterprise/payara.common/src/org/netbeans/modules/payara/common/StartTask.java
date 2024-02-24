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
package org.netbeans.modules.payara.common;

import org.netbeans.modules.payara.tooling.admin.ResultProcess;
import org.netbeans.modules.payara.tooling.admin.CommandException;
import org.netbeans.modules.payara.tooling.admin.CommandStartInstance;
import org.netbeans.modules.payara.tooling.admin.CommandVersion;
import org.netbeans.modules.payara.tooling.admin.CommandStartCluster;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import static org.netbeans.modules.payara.tooling.PayaraStatus.OFFLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.ONLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.PayaraStatus.STARTUP;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.StartupArgs;
import org.netbeans.modules.payara.tooling.data.StartupArgsEntity;
import org.netbeans.modules.payara.tooling.server.FetchLogSimple;
import org.netbeans.modules.payara.tooling.server.ServerTasks;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.api.extexecution.startup.StartupExtender;
import static org.netbeans.modules.payara.common.BasicTask.START_TIMEOUT;
import org.netbeans.modules.payara.common.ui.JavaSEPlatformPanel;
import org.netbeans.modules.payara.common.utils.AdminKeyFile;
import org.netbeans.modules.payara.common.utils.JavaUtils;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.netbeans.modules.payara.spi.Recognizer;
import org.netbeans.modules.payara.spi.VMIntrospector;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 * Asynchronous Payara server startup command execution.
 * <p/>
 * @author Ludovic Chamenois, Peter Williams, Tomas Kraus
 */
public class StartTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = PayaraLogger.get(StartTask.class);

    private static RequestProcessor NODE_REFRESHER
            = new RequestProcessor("nodes to refresh");

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    private static String[] removeEscapes(String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].replace("\\\"", ""); // NOI18N
        }
        return args;
    }

    private static StartupExtender.StartMode getMode(String gfMode) {
        if (PayaraModule.PROFILE_MODE.equals(gfMode)) {
            return StartupExtender.StartMode.PROFILE;
        } else if (PayaraModule.DEBUG_MODE.equals(gfMode)) {
            return StartupExtender.StartMode.DEBUG;
        } else {
            return StartupExtender.StartMode.NORMAL;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    private final CommonServerSupport support;
    private List<Recognizer> recognizers;
    private List<String> jvmArgs = null;
    private final VMIntrospector vmi;

    /** internal Java SE platform home cache. */
    private FileObject jdkHome;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of asynchronous Payara server startup command
     * execution support object.
     * <p/>
     * @param support       Common support object for the server instance being
     *                      started.
     * @param recognizers   Output recognizers to pass to log processors, if any.
     * @param stateListener State monitor to track start progress.
     */
    public StartTask(CommonServerSupport support, List<Recognizer> recognizers,
            VMIntrospector vmi,
            TaskStateListener... stateListener) {
        this(support, recognizers, vmi, null, stateListener);
    }

    /**
     * Constructs an instance of asynchronous Payara server startup command
     * execution support object.
     * <p/>
     * @param support       Common support object for the server instance being
     *                      started.
     * @param recognizers   Output recognizers to pass to log processors, if any.
     * @param jdkRoot       Java SE Development Kit to start server in profiling
     *                      mode.
     * @param jvmArgs       JVM arguments used to start server in profiling
     *                      mode.
     * @param stateListener State monitor to track start progress.
     */
    public StartTask(final CommonServerSupport support,
            List<Recognizer> recognizers, VMIntrospector vmi, String[] jvmArgs,
            TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        List<TaskStateListener> listeners = new ArrayList<>();
        listeners.addAll(Arrays.asList(stateListener));
        listeners.add(new TaskStateListener() {

            @Override
            public void operationStateChanged(TaskState newState,
                    TaskEvent event, String... args) {
                if (TaskState.COMPLETED.equals(newState)) {
                    // attempt to sync the comet support
                    RequestProcessor.getDefault().post(
                            new EnableComet(support.getInstance()));
                }
            }
        });
        this.stateListener = listeners.toArray(new TaskStateListener[0]);
        this.support = support;
        this.recognizers = recognizers;
        this.jvmArgs = (jvmArgs != null) ? Arrays.asList(removeEscapes(jvmArgs)) : null;
        this.vmi = vmi;
        this.jdkHome = null;
        LOGGER.log(Level.FINE, "VMI == {0}", vmi);
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Asynchronous task method started by {@link Executors}.
     * <p/>
     * @return Task execution result.
     */
    @Override
    public TaskState call() {
        // Save the current time so that we can deduct that the startup
        // Failed due to timeout
        LOGGER.log(Level.FINEST, "StartTask.call() called on thread \"{0}\"",
                Thread.currentThread().getName());
        final long start = System.currentTimeMillis();

        final String host = instance.getHost();
        final int adminPort = instance.getAdminPort();
        StateChange change;
        if ((change = validateAdminHostAndPort(host, adminPort)) != null) {
                return change.fireOperationStateChanged();
        }
        // Remote server.
        if (support.isRemote()) {
            if (PayaraState.isOnline(instance)) {
                if (Util.isDefaultOrServerTarget(instance.getProperties())) {
                    return restartDAS(host, adminPort, start);
                } else {
                    return startClusterOrInstance(host, adminPort);
                }
            } else {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_START_SERVER_FAILED_DASDOWN", instanceName);
            }
        // Local server.
        } else
        // Our server is offline.
        if (PayaraState.isOffline(instance)) {
            // But administrator port is occupied.
            if (ServerUtils.isAdminPortListening(
                    instance, NetUtils.PORT_CHECK_TIMEOUT)) {
                ResultString version;
                try {
                    version = CommandVersion.getVersion(instance);
                } catch (CommandException ce) {
                    version = null;
                }
                // Got version response from DAS.
                if (version != null) {
                    // There is server with matching version.
                    if (CommandVersion.verifyResult(version, instance)) {
                        fireOperationStateChanged(TaskState.RUNNING,
                                TaskEvent.CMD_COMPLETED,
                                "StartTask.call.matchVersion",
                                version.getValue());
                        return startClusterOrInstance(host, adminPort);
                    // There is server with non matching version.
                    } else {
                        if (!version.isAuth()) {
                            return fireOperationStateChanged(TaskState.FAILED,
                                    TaskEvent.CMD_FAILED,
                                    "StartTask.call.authFailed",
                                    instanceName, version.getValue());
                        } else {
                            return fireOperationStateChanged(TaskState.FAILED,
                                    TaskEvent.CMD_FAILED,
                                    "StartTask.call.anotherVersion",
                                    instanceName, version.getValue());
                        }
                    }
                // Got no version response from DAS.
                } else {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "StartTask.call.unknownVersion", instanceName);
                }
            } else {
                return startDAS(host, adminPort);
            }
            // Our server is online.
        } else {
            return startClusterOrInstance(host, adminPort);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Validate <code>host</code> and <code>port</code> values
     * for DAS listener.
     * <p/>
     * @return State change request data when server shall not be started
     *          and listeners should be notified about it or <code>null</code>
     *          otherwise.
     */
    private StateChange validateAdminHostAndPort(
            final String host, final int adminPort) {
        if (host == null || host.length() == 0) {
            return new StateChange(this, TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName);
        }
        if (adminPort < 0 || adminPort > 65535) {
            return new StateChange(this, TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName);
        }
        return null;
    }

    private TaskState restartDAS(String adminHost, int adminPort, final long start) {
        // deal with the remote case here...
        TaskStateListener[] listeners = {
                new TaskStateListener() {
                    // if the http command is successful, we are not done yet...
                    // The server still has to stop. If we signal success to the 'stateListener'
                    // for the task, it may be premature.
                    @SuppressWarnings("SleepWhileInLoop")
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        if (newState == TaskState.RUNNING) {
                            support.setServerState(ServerState.STARTING);
                        }
                        if (newState == TaskState.FAILED) {
                            fireOperationStateChanged(newState, event,
                                    instanceName, args);
                            support.setServerState(ServerState.STOPPED);
                            //support.refresh();
                        } else if (args != null && newState == TaskState.COMPLETED) {
                            for (String message : args) {
                                if (message.matches("[sg]et\\?.*\\=configs\\..*")) {
                                    return;
                                }
                            }
                            long startTime = System.currentTimeMillis();
                            TaskState state = TaskState.RUNNING;
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // no op
                            }
                            while (TaskState.RUNNING == state && System.currentTimeMillis() - start < START_TIMEOUT) {
                                // Send the 'completed' event and return when the server is running
                                boolean httpLive = PayaraState.isOnline(instance); //CommonServerSupport.isRunning(host, port,instance.getProperty(PayaraModule.DISPLAY_NAME_ATTR));

                                // Sleep for a little so that we do not make our checks too often
                                //
                                // Doing this before we check httpAlive also prevents us from
                                // pinging the server too quickly after the ports go live.
                                //
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    // no op
                                }

                                if (httpLive) {
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        // no op
                                    }
                                    state = TaskState.COMPLETED;
                                }
                            }
                            if (state == TaskState.COMPLETED) { //support.isReady(false, 120, TimeUnit.SECONDS)) {
                                support.setServerState(ServerState.RUNNING);
                            } else {
                                support.setServerState(ServerState.STOPPED);
                            }
                        }
                    }
                }};
        int debugPort = -1;
        if (PayaraModule.DEBUG_MODE.equals(instance.getProperty(PayaraModule.JVM_MODE))) {
            debugPort = instance.getDebugPort();
        }
        support.restartServer(debugPort,
                support.supportsRestartInDebug() && debugPort >= 0, listeners);
        return fireOperationStateChanged(
                TaskState.RUNNING, TaskEvent.CMD_FAILED,
                "StartTask.restartDAS.inProgress", instanceName);

    }

    /**
     * Reset administrator password when needed.
     */
    private void resetPassword() {
        AdminKeyFile keyFile = new AdminKeyFile(instance);
        keyFile.read();
        if (keyFile.isReset()) {
            String password = AdminKeyFile.randomPassword(
                    AdminKeyFile.RANDOM_PASSWORD_LENGTH);            
            instance.setAdminPassword(password);
            keyFile.setPassword(password);
            try {
                PayaraInstance.writeInstanceToFile(instance);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store Payara server attributes", ex);
            }
            keyFile.write();
        }
    }

    /**
     * Initialize JDK used to start Payara server.
     * <p/>
     * @return State change request data when JDK could not be initialized
     *         or <code>null</code> otherwise.
     */
    private StateChange initJDK() {
        try {
            if (null == jdkHome) {
                jdkHome = getJavaPlatformRoot();
                File jdkHomeFile = FileUtil.toFile(jdkHome);
                if (!JavaUtils.isJavaPlatformSupported(instance, jdkHomeFile)) {
                    jdkHome = JavaSEPlatformPanel.selectServerSEPlatform(
                            instance, jdkHomeFile);
                }
            }
            if (jdkHome == null) {
                return new StateChange(this, TaskState.FAILED,
                        TaskEvent.CMD_FAILED, "StartTask.initJDK.null",
                        instanceName);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex); // NOI18N
            return new StateChange(this, TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "StartTask.initJDK.exception",
                    new String[] {instanceName, ex.getLocalizedMessage()});
        }
        return null;
    }

    /**
     * Verify Payara server status before starting it.
     * <p/>
     * @return State change request data when server shall not be started
     *         and listeners should be notified about it or <code>null</code>
     *         otherwise.
     */
    private StateChange checkBeforeStart() {
        PayaraServerStatus status = PayaraState.getStatus(instance);
        String msgKey = null;
        switch (status.getStatus()) {
            case ONLINE:
                TaskState result;
                TaskEvent event;
                if (PayaraModule.PROFILE_MODE.equals(instance.getProperty(PayaraModule.JVM_MODE))) {
                    result = TaskState.FAILED;
                    event = TaskEvent.CMD_FAILED;
                } else {
                    result = TaskState.COMPLETED;
                    event = TaskEvent.CMD_COMPLETED;
                }
                return new StateChange(this, result, event,
                        "StartTask.startDAS.alreadyRunning");
            case OFFLINE:
                if (ServerUtils.isAdminPortListening(
                        instance, NetUtils.PORT_CHECK_TIMEOUT)) {
                    msgKey = "StartTask.startDAS.adminPortOccupied";
                } else {
                    final int httpPort = instance.getPort();
                    if (httpPort >= 0 && httpPort <= 65535
                            && NetUtils.isPortListeningLocal(
                            instance.getHost(), httpPort)) {
                        msgKey = "StartTask.startDAS.httpPortOccupied";
                    }
                }
                break;
            case SHUTDOWN:
                msgKey = "StartTask.startDAS.shutdown";
                break;
            case STARTUP:
                msgKey = "StartTask.startDAS.startup";
        }
        return msgKey != null
                ? new StateChange(this, TaskState.FAILED,
                TaskEvent.CMD_FAILED, msgKey, this.instance.getDisplayName())
                : null;
    }

    /**
     * Check for server status in profiling mode during startup.
     * <p/>
     * @return State change request about server startup success.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private StateChange profilingServerStatus() {
        LOGGER.log(Level.FINE,
                "Profiling mode status hack for {0}",
                new Object[]{instance.getName()});
        // Save process to be able to stop process waiting for profiler
        // to attach.
        support.setLocalStartProcess(instance.getProcess());
        // Try to sync the states after the profiler attaches.
        NODE_REFRESHER.post(new Runnable() {
            @Override
            public void run() {
                while (!PayaraState.isOnline(instance)) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        support.refresh();
                    }
                });
            }
        });
        return new StateChange(this, TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED,
                "MSG_SERVER_STARTED", instanceName);
    }

    private TaskState startDAS(String adminHost, int adminPort) {
        StateChange change;
        resetPassword();
        if ((change = initJDK()) != null) {
            return change.fireOperationStateChanged();
        }
        // Time must be measured after Java SE platform selection is done.
        long start = System.currentTimeMillis();
        StartStateListener listener;
        try {
            // This may be an autheticated server... so we will say it is
            // started. Other operations will fail if the process on the port
            // is not a GF v3 server.
            LOGGER.log(Level.FINEST,
                    "Checking if Payara {0} is running.",
                    instance.getName());
            if ((change = checkBeforeStart()) != null) {
                return change.fireOperationStateChanged();
            }
            // We should be listening for reaching ONLINE state before process
            // is started.
            listener = prepareStartMonitoring(
                    instance.getJvmMode() == PayaraJvmMode.PROFILE);
            if (listener == null) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "StartTask.startDAS.startupMonitoring", instanceName);                
            }
            Process process = createProcess();
            instance.setProcess(process);
            listener.setProcess(process);
        } catch (ProcessCreationException ex) {
            Logger.getLogger("payara").log(Level.INFO,
                    "Could not start process for " + instanceName, ex);
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_PASS_THROUGH",
                    ex.getLocalizedMessage());
        }

        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "StartTask.startDAS.inProgress", instanceName);
        // create a logger to the server's output stream so that a user
        // can observe the progress
        LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(PayaraModule.URL_ATTR));
        logger.readInputStreams(recognizers, true, null,
                new FetchLogSimple(instance.getProcess().getInputStream()),
                new FetchLogSimple(instance.getProcess().getErrorStream()));

        // Waiting for server to start
        LOGGER.log(Level.FINER, "Waiting for server to start for {0} ms",
                new Object[] {Integer.toString(START_TIMEOUT)});
        try {
            synchronized(listener) {
                while (!listener.isWakeUp()
                        && (System.currentTimeMillis()
                        - start < START_TIMEOUT)) {
                    listener.wait(System.currentTimeMillis() - start);
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO,
                    "Caught InterruptedException while waiting for {0} to start: {1}",
                    new Object[] {instance.getName(), ie.getLocalizedMessage()});
        } finally {
            PayaraStatus.removeListener(instance, listener);
        }
        // We need to lie about server status in profiling mode.
        if (null != jvmArgs && (change = profilingServerStatus()) != null) {
            return change.fireOperationStateChanged();
        }
        if (!PayaraState.isOnline(instance)) {
            PayaraStatus.suspend(instance);
            return fireOperationStateChanged(
                    TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "StartTask.startDAS.startFailed", instanceName);
        } else {
            return startClusterOrInstance(adminHost, adminPort);
        }
    }

    private TaskState startClusterOrInstance(String adminHost, int adminPort) {
        String target = Util.computeTarget(instance.getProperties());
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_STARTED", instanceName);
        } else {
            TaskState state;
            try {
                ResultString result
                        = CommandStartCluster.startCluster(instance, target);
                state = result.getState();
            } catch (PayaraIdeException gfie) {
                state = TaskState.FAILED;
                LOGGER.log(Level.INFO, gfie.getMessage(), gfie);
            }
            if (state == TaskState.FAILED) {
                try {
                    ResultString result
                            = CommandStartInstance.startInstance(instance, target);
                    state = result.getState();
                } catch (PayaraIdeException gfie) {
                    state = TaskState.FAILED;
                    LOGGER.log(Level.INFO, gfie.getMessage(), gfie);
                }
                if (state == TaskState.FAILED) {
                    // if start instance not suscessful fail
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_START_TARGET_FAILED", instanceName, target);
                }
            }
            support.updateHttpPort();
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_STARTED", instanceName);
        }
    }

    /**
     * Search for Java SE platform to be used for running Payara server.
     * <p/>
     * Payara instance Java SE platform property is checked first
     * and Java SE platform used to run NetBeans as a fallback option.
     * <p/>
     * @return Java SE platform to be used for running Payara server.
     * @throws IOException when Payara instance Java SE platform property
     *         does not point to existing directory.
     */
    private FileObject getJavaPlatformRoot() throws IOException {
        FileObject retVal;
        String javaHome = instance.getJavaHome();
        if (null == javaHome || javaHome.trim().length() < 1) {
            File dir = new File(getJdkHome());
            retVal = FileUtil.createFolder(FileUtil.normalizeFile(dir));
        } else {
            File f = new File(javaHome);
            if (f.exists()) {
                retVal = FileUtil.createFolder(FileUtil.normalizeFile(f));
            } else {
                throw new FileNotFoundException(
                        NbBundle.getMessage(StartTask.class,
                        "MSG_INVALID_JAVA", instanceName, javaHome));
            }
        }
        return retVal;
    }

    /**
     * Get Java SE platform used to run NetBeans.
     * <p/>
     * @return Java SE platform used to run NetBeans.
     */
    private String getJdkHome() {
        String result;
        result = System.getProperty("java.home");
        if (result.endsWith(File.separatorChar + "jre")) {
            result = result.substring(0, result.length() - 4);
        }
        return result;
    }

    private StartupArgs createProcessDescriptor() throws ProcessCreationException {
        List<String> payaraArgs = new ArrayList<>(2);
        String domainDir = Util.quote(getDomainFolder().getAbsolutePath());
        payaraArgs.add(ServerUtils.cmdLineArgument(
                ServerUtils.PF_DOMAIN_ARG, getDomainName()));
        payaraArgs.add(ServerUtils.cmdLineArgument(
                ServerUtils.PF_DOMAIN_DIR_ARG, domainDir));

        ArrayList<String> optList = new ArrayList<>();
        // append debug options
        if (PayaraModule.DEBUG_MODE.equals(instance.getProperty(PayaraModule.JVM_MODE))) {
            appendDebugOptions(optList);
        }
        
        // append other options from startup extenders, e.g. for profiling
        appendStartupExtenderParams(optList);

        return new StartupArgsEntity(
                payaraArgs,
                optList,
                (Map<String, String>) null,
                FileUtil.toFile(jdkHome).getAbsolutePath());
    }

    /**
     * Appends debug options for server start.
     * If the port read from instance properties is not valid (null or out of the range),
     * it offers to the user a different free port.
     * 
     * @param optList
     * @throws ProcessCreationException 
     */
    private void appendDebugOptions(List<String> optList) throws ProcessCreationException {
        String debugPortString = instance.getProperty(PayaraModule.DEBUG_PORT);
        String debugTransport = "dt_socket"; // NOI18N
        if ("true".equals(instance.getProperty(PayaraModule.USE_SHARED_MEM_ATTR))) { // NOI18N
            debugTransport = "dt_shmem";  // NOI18N
        } else {
            if (null != debugPortString && debugPortString.trim().length() > 0) {
                int t = Integer.parseInt(debugPortString);
                if (t != 0 && (t < PayaraInstance.LOWEST_USER_PORT
                        || t > 65535)) {
                    throw new NumberFormatException();
                }
            }
        }
        if (null == debugPortString
                || "0".equals(debugPortString) || "".equals(debugPortString)) {
            if ("true".equals(instance.getProperty(PayaraModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                debugPortString = Integer.toString(Math.abs((instance.getProperty(PayaraModule.PAYARA_FOLDER_ATTR)
                        + instance.getDomainsRoot()
                        + instance.getProperty(PayaraModule.DOMAIN_NAME_ATTR)).hashCode() + 1));
            } else {
                try {
                    debugPortString = selectDebugPort();
                } catch (IOException ioe) {
                    throw new ProcessCreationException(ioe,
                            "MSG_START_SERVER_FAILED_INVALIDPORT", instanceName, debugPortString); //NOI18N
                }
            }
        }
        support.setEnvironmentProperty(PayaraModule.DEBUG_PORT, debugPortString, true);
        StringBuilder opt = new StringBuilder();
        opt.append("-agentlib:jdwp=transport="); // NOI18N
        opt.append(debugTransport);
        opt.append(",address="); // NOI18N
        opt.append(debugPortString);
        opt.append(",server=y,suspend=n"); // NOI18N
        optList.add(opt.toString());
    }
    
    private void appendStartupExtenderParams(List<String> optList) {
        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(support.getInstanceProvider().getInstance(instance.getProperty("url"))), 
                getMode(instance.getProperty(PayaraModule.JVM_MODE)))) {
            for (String arg : args.getArguments()) {
                String[] argSplitted = arg.trim().split("\\s+(?=-)");
                optList.addAll(Arrays.asList(argSplitted));
            }
        }
    }

    private String selectDebugPort() throws IOException {
        int debugPort = 9009;
        ServerSocket t = null;
        try {
            // try to use the 'standard port'
            t = new ServerSocket(debugPort);
            return Integer.toString(debugPort);
        } catch (IOException ex) {
            // log this... but don't panic
            Logger.getLogger("payara").fine("9009 is in use... going random");
        } finally {
            if (null != t) {
                try {
                    t.close();
                } catch (IOException ioe) {
                }
            }
        }
        try {
            // try to find a different port... if this fails,
            //    it is a great time to panic.
            t = new ServerSocket(0);
            debugPort = t.getLocalPort();
            return Integer.toString(debugPort);
        } finally {
            if (null != t) {
                try {
                    t.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    private Process createProcess() throws ProcessCreationException {
        StartupArgs args = createProcessDescriptor();
        // JDK checks and Java VM process startup were moved to GF Tooling SDK.
        ResultProcess process = ServerTasks.startServer(instance, args);
        if (process.getState() != TaskState.COMPLETED) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_PD", instanceName);
        }
        return process.getValue().getProcess();
    }

    private File getDomainFolder() {
        return new File(instance.getDomainsRoot() + File.separatorChar + getDomainName());
    }

    private String getDomainName() {
        return instance.getProperty(PayaraModule.DOMAIN_NAME_ATTR);
    }

}
