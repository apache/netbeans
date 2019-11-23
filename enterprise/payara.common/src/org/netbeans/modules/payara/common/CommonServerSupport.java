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

import org.netbeans.modules.payara.tooling.admin.CommandRedeploy;
import org.netbeans.modules.payara.tooling.admin.CommandRestartDAS;
import org.netbeans.modules.payara.tooling.admin.CommandUndeploy;
import org.netbeans.modules.payara.tooling.admin.CommandDeploy;
import org.netbeans.modules.payara.tooling.admin.CommandDisable;
import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.CommandEnable;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.CommandListComponents;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import static org.netbeans.modules.payara.tooling.PayaraStatus.OFFLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.PayaraStatus.STARTUP;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.utils.Utils;
import org.netbeans.modules.payara.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.AppDesc;
import org.netbeans.modules.payara.spi.CommandFactory;
import static org.netbeans.modules.payara.spi.PayaraModule.HOSTNAME_ATTR;
import static org.netbeans.modules.payara.spi.PayaraModule.PROPERTIES_FETCH_TIMEOUT;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.netbeans.modules.payara.spi.Recognizer;
import org.netbeans.modules.payara.spi.RecognizerCookie;
import org.netbeans.modules.payara.spi.ResourceDesc;
import org.netbeans.modules.payara.spi.VMIntrospector;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule3;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 * Payara server support API.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public class CommonServerSupport
        implements PayaraModule3, RefreshModulesCookie {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Task state listener watching __locations command execution.
     */
    private static class LocationsTaskStateListener
            implements TaskStateListener {

        /** Payara server support object instance. */
        final CommonServerSupport css;

        /**
         * Creates an instance of task state listener watching __locations
         * command execution.
         * <p/>
         * @param css Payara server support object instance.
         */
        LocationsTaskStateListener(CommonServerSupport css) {
            this.css = css;
        }

        private String adminCommandFailedMsg(String resName, String[] args) {
            String serverName = args[0];
            String command = args[1];
            String exMessage = args.length > 2 ? args[2] : null;
            return args.length > 2
                    ? NbBundle.getMessage(CommonServerSupport.class, resName,
                    args[0], args[1], args[2])
                    : NbBundle.getMessage(CommonServerSupport.class, resName,
                    args[0], args[1]);
        }

        /**
         * Callback to notify about Payara __locations command execution
         * state change.
         * <p/>
         * <code>String</codce> arguments passed to state listener
         * from runner:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     <code>String</codce> arguments passed to state
         *                 listener.
         */
        @Override
        public void operationStateChanged(
                TaskState newState, TaskEvent event,
                String[] args) {
            // Server name and command are mandatory.
            if (args.length > 1) {
                String exMessage = args.length > 2 ? args[2] : null;
                boolean display = args.length > 3
                        ? Boolean.parseBoolean(args[3]) : false;
                if (display) {
                    long lastDisplayed = css.getLatestWarningDisplayTime();
                    long currentTime = System.currentTimeMillis();
                    if (TaskState.FAILED == newState
                            && currentTime - lastDisplayed > 5000) {
                        String message;

                        switch (event) {
                            case EXCEPTION:
                                if (exMessage != null
                                        && exMessage.length() > 0) {
                                    message = adminCommandFailedMsg(
                                            "MSG_ADMIN_EXCEPTION", args);
                                } else {
                                    message = adminCommandFailedMsg(
                                            "MSG_ADMIN_FAILED", args);
                                }
                                break;
                            case AUTH_FAILED: case AUTH_FAILED_HTTP:
                                message = adminCommandFailedMsg(
                                        "MSG_ADMIN_AUTH_FAILED", args);
                                break;
                            default:
                                message = adminCommandFailedMsg(
                                        "MSG_ADMIN_FAILED", args);
                        }
                        displayPopUpMessage(css, message);
                    }
                }
            }
        }
    }

    class StartOperationStateListener implements TaskStateListener {
        private ServerState endState;

        StartOperationStateListener(ServerState endState) {
            this.endState = endState;
        }

        @Override
        public void operationStateChanged(TaskState newState, TaskEvent event,
                String... args) {
            if(newState == TaskState.RUNNING) {
                setServerState(ServerState.STARTING);
            } else if(newState == TaskState.COMPLETED) {
                startedByIde = isRemote
                        ? false : PayaraState.isOnline(instance);
                setServerState(endState);
            } else if(newState == TaskState.FAILED) {
                setServerState(ServerState.STOPPED);
                // Open a warning dialog here...
                NotifyDescriptor nd = new NotifyDescriptor.Message(Utils.concatenate(args));
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }
    class StopOperationStateListener implements TaskStateListener {

        @Override
        public void operationStateChanged(
                TaskState newState, TaskEvent event, String... args) {
            if (newState == TaskState.RUNNING) {
                setServerState(ServerState.STOPPING);
            } else if (newState == TaskState.COMPLETED) {
                setServerState(ServerState.STOPPED);
            } else if (newState == TaskState.FAILED) {
                // Possible bug: What if server was started in other mode
                // than RUNNING.
                setServerState(ServerState.RUNNING);
            }
        }
    };

    class KillOperationStateListener implements TaskStateListener {

        @Override
        public void operationStateChanged(final TaskState newState,
                final TaskEvent event, final String... args) {
            if (newState == TaskState.RUNNING) {
                setServerState(ServerState.STOPPING);
            } else if (newState == TaskState.COMPLETED) {
                setServerState(ServerState.STOPPED);
            } else if (newState == TaskState.FAILED) {
                // Registered process has already finished.
                if (event == TaskEvent.PROCESS_NOT_RUNNING) {
                    setServerState(ServerState.STOPPED);
                }
                // Otherwise do nothing for TaskEvent.PROCESS_NOT_EXISTS.
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(CommonServerSupport.class);

    /** Local host name (DNS). */
    private static final String LOCALHOST = "localhost";

    /** String to return for failed {@see getHttpHostFromServer()} search. */
    private static final String FAILED_HTTP_HOST = LOCALHOST + "FAIL";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display pop up window with given message.
     * <p/>
     * Method is thread safe.
     * <p/>
     * @param css     Payara server support object.
     * @param message Message to be displayed.
     */
    public static void displayPopUpMessage(final CommonServerSupport css,
            final String message) {
        synchronized (css) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            css.setLatestWarningDisplayTime(System.currentTimeMillis());
            LOGGER.log(Level.INFO, message);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Managed Payara instance. */
    private final PayaraInstance instance;

    private volatile ServerState serverState = ServerState.UNKNOWN;
    private final Object stateMonitor = new Object();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private FileObject instanceFO;

    private volatile boolean startedByIde = false;

    /** Cache local/remote test for instance. */
    private transient boolean isRemote = false;

    // prevent j2eeserver from stopping an authenticated domain that
    // the IDE did not start.
    private boolean stopDisabled = false;

    private Process localStartProcess;

    /** Last executed start task. */
    private volatile FutureTask<TaskState> startTask;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    CommonServerSupport(PayaraInstance instance) {
        this.instance = instance;
        this.isRemote = instance.isRemote();
        // !PW FIXME temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
        startTask = null;
    }

    /**
     * Get <code>PayaraInstance</code> object associated with this object.
     * <p/>
     * @return <code>PayaraInstance</code> object associated with this object.
     */
    @Override
    public PayaraInstance getInstance() {
        return this.instance;
    }

    private FileObject getInstanceFileObject() {
        FileObject dir = FileUtil.getConfigFile(
                instance.getInstanceProvider().getInstancesDirFirstName());
        if(dir != null) {
            String instanceFN = instance
                    .getProperty(PayaraInstance.INSTANCE_FO_ATTR);
            if(instanceFN != null) {
                return dir.getFileObject(instanceFN);
            }
        }
        return null;
    }

    @Override
    public String getPassword() {
        return instance.getPassword();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getInstallRoot() {
        return instance.getInstallRoot();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getPayaraRoot() {
        return instance.getPayaraRoot();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getDisplayName() {
        return instance.getDisplayName();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getDeployerUri() {
        return instance.getDeployerUri();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getUserName() {
        return instance.getUserName();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getAdminPort() {
        return instance.getHttpAdminPort();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getHttpPort() {
        return instance.getHttpPort();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public int getHttpPortNumber() {
        return instance.getPort();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public int getAdminPortNumber() {
        return instance.getAdminPort();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getHostName() {
        return instance.getProperty(HOSTNAME_ATTR);
    }

   /** @deprecated Use in <code>PayaraInstance</code> context. */
   @Deprecated
   public String getDomainsRoot() {
        return instance.getDomainsRoot();
    }

    /** @deprecated Use in <code>PayaraInstance</code> context. */
    @Deprecated
    public String getDomainName() {
        return instance.getDomainName();
    }

    public void setServerState(final ServerState newState) {
        // Synchronized on private monitor to serialize changes in state.
        // Storage of serverState is volatile to facilitate readability of
        // current state regardless of lock status.
        boolean fireChange = false;

        synchronized (stateMonitor) {
            if(serverState != newState) {
                serverState = newState;
                fireChange = true;
            }
        }

        if(fireChange) {
            changeSupport.fireChange();
        }
    }

    boolean isStartedByIde() {
        return startedByIde;
    }

    // ------------------------------------------------------------------------
    // PayaraModule interface implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, String> getInstanceProperties() {
        // force the domains conversion
        getDomainsRoot();
        return Collections.unmodifiableMap(instance.getProperties());
    }

    @Override
    public PayaraInstanceProvider getInstanceProvider() {
        return instance.getInstanceProvider();
    }

    @Override
    public boolean isRemote() {
        return isRemote;
    }

    private static final RequestProcessor RP = new RequestProcessor("CommonServerSupport - start/stop/refresh",5); // NOI18N

    @Override
    public Future<TaskState> startServer(
            final TaskStateListener stateListener, ServerState endState) {
        LOGGER.log(Level.FINEST,
                "CSS.startServer called on thread \"{0}\"",
                Thread.currentThread().getName());
        TaskStateListener startServerListener = new StartOperationStateListener(endState);
        VMIntrospector vmi = Lookups.forPath(Util.PF_LOOKUP_PATH).lookup(VMIntrospector.class);
        FutureTask<TaskState> task = new FutureTask<TaskState>(
                new StartTask(this, getRecognizers(), vmi,
                (String[]) (endState == ServerState.STOPPED_JVM_PROFILER
                ? new String[]{""} : null),
                startServerListener, stateListener));
        startTask = task;
        RP.post(task);
        return task;
    }

    private List<Recognizer> getRecognizers() {
        List<Recognizer> recognizers;
        Collection<? extends RecognizerCookie> cookies = 
                instance.localLookup().lookupAll(RecognizerCookie.class);
        if(!cookies.isEmpty()) {
            recognizers = new LinkedList<Recognizer>();
            for(RecognizerCookie cookie: cookies) {
                recognizers.addAll(cookie.getRecognizers());
            }
            recognizers = Collections.unmodifiableList(recognizers);
        } else {
            recognizers = Collections.emptyList();
        }
        return recognizers;
    }


    @Override
    public Future<TaskState> stopServer(final TaskStateListener stateListener) {
        LOGGER.log(Level.FINEST, "CSS.stopServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        TaskStateListener stopServerListener = new StopOperationStateListener();
        FutureTask<TaskState> task;
        if (!isRemote() || !Util.isDefaultOrServerTarget(instance.getProperties())) {
            if (getServerState() == ServerState.STOPPED_JVM_PROFILER) {
                task = new FutureTask<TaskState>(
                        new StopProfilingTask(this, stateListener));
            } else {
                task = new FutureTask<TaskState>(
                        new StopTask(this, stopServerListener, stateListener));
            }
        // prevent j2eeserver from stopping a server it did not start.
        } else {
            task = new FutureTask<TaskState>(
                    new NoopTask(this,stopServerListener,stateListener));
        }
        if (stopDisabled) {
            stopServerListener.operationStateChanged(
                    TaskState.COMPLETED, TaskEvent.CMD_COMPLETED, "");
            if (null != stateListener) {
                stateListener.operationStateChanged(
                        TaskState.COMPLETED, TaskEvent.CMD_COMPLETED, "");
            }
            return task;
        }
        RP.post(task);
        return task;
    }

    /** Delay between checking task state while waiting it
     *  to die [ms]. */
    private static final int WAIT_TASK_TO_DIE_SLEEP = 300;

    /** Maximum time to wait for task to die [ms]. */
    private static final int WAIT_TASK_TO_DIE_MAX = 3000;

     /**
     * Terminates local Payara server process when started from UI.
     * <p/>
     * @param stateListener External state listener to register.
     * @return Asynchronous Payara server termination task when 
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public Future<TaskState> killServer(final TaskStateListener stateListener) {
        TaskStateListener killServerListener = new KillOperationStateListener();
        FutureTask<TaskState> task;
        boolean isStateListener = stateListener != null;
        if (!isRemote() && instance.getProcess() != null) {
            FutureTask<TaskState> stTask = this.startTask;
            // Stop running StartTask and wait for it to die.
            if (stTask != null && !stTask.isDone()) {
                stTask.cancel(true);
                long startTime = System.currentTimeMillis();
                while(!stTask.isDone()
                        && System.currentTimeMillis() - startTime
                        < WAIT_TASK_TO_DIE_MAX) {
                    try {
                        Thread.sleep(WAIT_TASK_TO_DIE_SLEEP);
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO,
                                "Caught InterruptedException while waiting for "
                                + "{0} start task to die.",
                                instance.getName());
                    }
                }
                if (!stTask.isDone()) {
                        LOGGER.log(Level.INFO,
                                "Start task for {0} did not finish within {1} ms.",
                                new String[] {instance.getName(),
                                    Integer.toString(WAIT_TASK_TO_DIE_MAX)});
                    
                }
            }
            TaskStateListener[] listeners
                    = new TaskStateListener[isStateListener ? 2 : 1];
            listeners[0] = killServerListener;
            if (isStateListener) {
                listeners[1] = stateListener;
            }
            task = new FutureTask<TaskState>(new KillTask(instance, listeners));
        } else {
            task = new FutureTask<TaskState>(
                    new NoopTask(this, null, stateListener));
        }
        RP.post(task);
        return task;
    }

    @Override
    public Future<TaskState> restartServer(TaskStateListener stateListener) {
        LOGGER.log(Level.FINEST,
                "CSS.restartServer called on thread \"{0}\"",
                Thread.currentThread().getName());
        FutureTask<TaskState> task = new FutureTask<TaskState>(
                new RestartTask(this, stateListener));
        RP.post(task);
        return task;
    }

    /**
     * Sends restart-domain command to server (asynchronous)
     *
     */
    public Future<ResultString> restartServer(final int debugPort,
            boolean debug, TaskStateListener[] listeners) {
        if (-1 == debugPort) {
            Command command = new CommandRestartDAS(false);
            return ServerAdmin.<ResultString>exec(
                    instance, command, null, listeners);
        }
        TaskState state = null;
        try {
            ResultMap<String, String> result
                    = CommandGetProperty.getProperties(instance,
                    "configs.config.server-config.java-config.debug-options");
            if (result.getState() == TaskState.COMPLETED) {
                Map<String, String> values = result.getValue();
                if (values != null && !values.isEmpty()) {
                    String oldValue = values.get(
                            "configs.config.server-config.java-config.debug-options");
                    CommandSetProperty setCmd =
                            getCommandFactory().getSetPropertyCommand(
                            "configs.config.server-config.java-config.debug-options",
                            oldValue.replace("transport=dt_shmem", "transport=dt_socket").
                            replace("address=[^,]+", "address=" + debugPort));
                    try {
                        CommandSetProperty.setProperty(instance, setCmd);
                        debug = true;
                    } catch (PayaraIdeException gfie) {
                        debug = false;
                        LOGGER.log(Level.INFO, debugPort + "", gfie);
                    }
                }
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not retrieve property from server.", gfie);
        }
        Command command = new CommandRestartDAS(debug);
        return ServerAdmin.<ResultString>exec(
                instance, command, null, listeners);
    }

    @Override
    public Future<ResultString> deploy(final TaskStateListener stateListener,
            final File application, final String name) {
        return deploy(stateListener, application, name, null);
    }

    @Override
    public Future<ResultString> deploy(final TaskStateListener stateListener,
            final File application, final String name,
            final String contextRoot) {
        return deploy(stateListener, application, name, contextRoot, null);
    }

    @Override
    public Future<ResultString> deploy(final TaskStateListener stateListener,
            final File application, final String name, final String contextRoot,
            final Map<String,String> properties) {
        return deploy(stateListener, application, name, contextRoot, null, new File[0]);
    }

    @Override
    public Future<ResultString> deploy(final TaskStateListener stateListener,
            final File application, final String name, final String contextRoot,
            final Map<String, String> properties, final File[] libraries) {
        try {
            return ServerAdmin.<ResultString>exec(instance, new CommandDeploy(
                    name, Util.computeTarget(instance.getProperties()),
                    application, contextRoot, properties, libraries
            ), null, new TaskStateListener[]{stateListener});
        } finally {
            refreshChildren();
        }
    }

    @Override
    public Future<ResultString> redeploy(
            final TaskStateListener stateListener,
            final String name, boolean resourcesChanged) {
        return redeploy(stateListener, name, null, resourcesChanged);
    }

    @Override
    public Future<ResultString> redeploy(
            final TaskStateListener stateListener,
            final String name, final String contextRoot,
            boolean resourcesChanged) {
        return redeploy(stateListener, name, contextRoot, new File[0],
                resourcesChanged);
    }

    @Override
    public Future<ResultString> redeploy(TaskStateListener stateListener,
    String name, String contextRoot, File[] libraries,
    boolean resourcesChanged) {
        Map<String, String> properties = new HashMap<String, String>();
        String url = instance.getProperty(PayaraModule.URL_ATTR);
        String sessionPreservationFlag = instance.getProperty(PayaraModule.SESSION_PRESERVATION_FLAG);
        if (sessionPreservationFlag == null) {
            // If there isn't a value stored for the instance, use the value of
            // the command-line flag.
            sessionPreservationFlag = System.getProperty(
                    "glassfish.session.preservation.enabled", "false");
        }
        if (Boolean.parseBoolean(sessionPreservationFlag)) {
            properties.put("keepSessions", "true");
        }
        if (resourcesChanged) {
            properties.put("preserveAppScopedResources", "true");
        }
        try {
            return ServerAdmin.<ResultString>exec(instance, new CommandRedeploy(
                    name, Util.computeTarget(instance.getProperties()),
                    contextRoot, properties, libraries,
                    url != null && url.contains("ee6wc")), stateListener);
        } finally {
            refreshChildren();
        }
    }

    @Override
    public Future<ResultString> undeploy(
            final TaskStateListener stateListener, final String name) {
        return ServerAdmin.<ResultString>exec(
                instance, new CommandUndeploy(name, Util.computeTarget(
                instance.getProperties())), null,
                new TaskStateListener[]{stateListener});
    }

    @Override
    public Future<ResultString> enable(
            final TaskStateListener stateListener, final String name) {
        return ServerAdmin.<ResultString>exec(instance, new CommandEnable(
                name,  Util.computeTarget(instance.getProperties())), null,
                new TaskStateListener[] {stateListener});
    }

    @Override
    public Future<ResultString> disable(
            final TaskStateListener stateListener, final String name) {
        return ServerAdmin.<ResultString>exec(instance, new CommandDisable(
                name,  Util.computeTarget(instance.getProperties())), null,
                new TaskStateListener[] {stateListener});
    }

    @Override
    public AppDesc [] getModuleList(String container) {
        int total = 0;
        Map<String, List<AppDesc>> appMap = getApplications(container);
        Collection<List<AppDesc>> appLists = appMap.values();
        for(List<AppDesc> appList: appLists) {
            total += appList.size();
        }
        AppDesc [] result = new AppDesc[total];
        int index = 0;
        for(List<AppDesc> appList: appLists) {
            for(AppDesc app: appList) {
                result[index++] = app;
            }
        }
        return result;
    }

    @Override
    public Map<String, ResourceDesc> getResourcesMap(String type) {
        Map<String, ResourceDesc> resourcesMap
                = new HashMap<String, ResourceDesc>();
        List<ResourceDesc> resourcesList
                = ResourceDesc.getResources(instance, type);
        for (ResourceDesc resource : resourcesList) {
            resourcesMap.put(resource.getName(), resource);
        }
        return resourcesMap;
    }

    @Override
    public ServerState getServerState() {
        if (serverState == ServerState.UNKNOWN) {
            RequestProcessor.Task task = refresh();
            if (task != null) {
                task.waitFinished();
            }
        }
        return serverState;
    }

    @Override
    public void addChangeListener(final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public String setEnvironmentProperty(final String name, final String value,
            final boolean overwrite) {
        String result;

        synchronized (instance.getProperties()) {
            result = instance.getProperty(name);
            if(result == null || overwrite == true) {
                instance.putProperty(name, value);
                setInstanceAttr(name, value);
                result = value;
            }
        }

        return result;
    }

    // ------------------------------------------------------------------------
    // bookkeeping & impl managment, not exposed via interface.
    // ------------------------------------------------------------------------
    void setProperty(final String key, final String value) {
        instance.putProperty(key, value);
    }

    void getProperty(String key) {
        instance.getProperty(key);
    }

    boolean setInstanceAttr(String name, String value) {
        boolean retVal = false;
        if(instanceFO == null || !instanceFO.isValid()) {
            instanceFO = getInstanceFileObject();
        }
        if(instanceFO != null && instanceFO.canWrite()) {
            try {
                Object currentValue = instanceFO.getAttribute(name);
                if (null != currentValue && currentValue.equals(value)) {
                    // do nothing
                } else {
                    instanceFO.setAttribute(name, value);
                }
                retVal = true;
            } catch(IOException ex) {
                LOGGER.log(Level.WARNING,
                        "Unable to save attribute " + name + " in " + instanceFO.getPath() + " for " + getDeployerUri(), ex); // NOI18N
            }
        } else {
            if (null == instanceFO)
                LOGGER.log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), false, "null"}); // NOI18N
            else
                LOGGER.log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), instanceFO.canWrite(), instanceFO.getPath()}); // NOI18N
        }
        return retVal;
    }

    void setFileObject(FileObject fo) {
        instanceFO = fo;
    }

   
    public static boolean isRunning(final String host, final int port,
            final String name, final int timeout) {
        if(null == host)
            return false;

        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            Logger.getLogger("payara-socket-connect-diagnostic").log(
                    Level.FINE, "Using socket.connect", new Exception());
            socket.connect(isa, timeout);
            socket.setSoTimeout(timeout);
            try {
                socket.close();
            } catch (IOException ioe) {
                LOGGER.log(
                        Level.INFO, "Socket closing failed: {0}",
                        ioe.getMessage());
            }
            return true;
        } catch (java.net.ConnectException | java.net.SocketTimeoutException ex) {
            return false;
        } catch (IOException ioe) {
            String message = NbBundle.getMessage(CommonServerSupport.class,
                    name == null || "".equals(name.trim())
                    ? "MSG_FLAKEY_NETWORK" : "MSG_FLAKEY_NETWORK2",
                    host, Integer.toString(port), ioe.getLocalizedMessage());
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            LOGGER.log(Level.INFO,
                    "Evidence of network flakiness: {0}", ioe.getMessage());
            return false;
        }
    }

    public static boolean isRunning(final String host, final int port,
            final String name) {
        return isRunning(host, port, name, 2000);
    }

    // ------------------------------------------------------------------------
    //  RefreshModulesCookie implementation (for refreshing server state)
    // ------------------------------------------------------------------------
    private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    @Override
    public final RequestProcessor.Task refresh() {
        return refresh(null,null);
    }

    @Override
    public RequestProcessor.Task refresh(String expected, String unexpected) {
        
        if(refreshRunning.compareAndSet(false, true)) {
            return RP.post(new Runnable() {
                @Override
                public void run() {
                    // Get current server status.
                    PayaraServerStatus status
                            = PayaraStatus.get(instance);
                    // Start monitoring of this Payara server instance
                    // when status is null and refresh status.
                    if (status == null) {
                        PayaraState.monitor(instance);
                        status = PayaraStatus.get(instance);
                    }
                    ServerState currentState = serverState;
                    switch(currentState) {
                        case UNKNOWN:
                            switch(status.getStatus()) {
                                case ONLINE:
                                    setServerState(ServerState.RUNNING);
                                    break;
                                case STARTUP:
                                    setServerState(ServerState.STARTING);
                                    break;
                                case SHUTDOWN:
                                    setServerState(ServerState.STOPPING);
                                    break;
                                case OFFLINE:
                                    setServerState(ServerState.STOPPED);
                            }
                            break;
                        case STOPPED:
                            if (status.getStatus() == PayaraStatus.ONLINE) {
                                setServerState(ServerState.RUNNING);
                            }
                            break;
                        case RUNNING:
                            switch(status.getStatus()) {
                                case STARTUP:
                                    setServerState(ServerState.STARTING);
                                    break;
                                case SHUTDOWN:
                                    setServerState(ServerState.STOPPING);
                                    break;
                                case OFFLINE:
                                    setServerState(ServerState.STOPPED);
                            }
                            break;
                        case STOPPED_JVM_PROFILER:
                            if (status.getStatus() == PayaraStatus.ONLINE) {
                                setServerState(ServerState.RUNNING);
                            }
                    }
                    refreshRunning.set(false);
                }
            });
        } else {
            return null;
        }
    }

    void disableStop() {
        stopDisabled = true;
    }
    
    void setLocalStartProcess(Process process) {
        this.localStartProcess = process;
    }
    
    Process getLocalStartProcess() {
        return localStartProcess;
    }
    
    void stopLocalStartProcess() {
        localStartProcess.destroy();
        localStartProcess = null;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return instance.getInstanceProvider().getCommandFactory();
    }

    @Override
    public String getResourcesXmlName() {
        return org.netbeans.modules.payara.spi.Utils
                .useGlassFishPrefix(getDeployerUri()) ?
                "glassfish-resources" : "sun-resources"; // NOI18N
    }

    @Override
    public boolean supportsRestartInDebug() {
        return getDeployerUri().contains(PayaraInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isRestfulLogAccessSupported() {
        return getDeployerUri().contains(PayaraInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isWritable() {
        return (null == instanceFO) ? false : instanceFO.canWrite();
    }

    private long latestWarningDisplayTime = System.currentTimeMillis();
    
    private long getLatestWarningDisplayTime() {
        return latestWarningDisplayTime;
    }

    private void setLatestWarningDisplayTime(long currentTime) {
        latestWarningDisplayTime = currentTime;
    }

    /**
     * Update HTTP port value from server properties.
     */
    void updateHttpPort() {
        String target = Util.computeTarget(instance.getProperties());
        String gpc;
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            gpc = "*.server-config.*.http-listener-1.port";
            setEnvironmentProperty(PayaraModule.HTTPHOST_ATTR, 
                    instance.getProperty(PayaraModule.HOSTNAME_ATTR), true); // NOI18N
        } else {
            String server = getServerFromTarget(target);
            String adminHost = instance.getProperty(PayaraModule.HOSTNAME_ATTR);
            setEnvironmentProperty(PayaraModule.HTTPHOST_ATTR,
                    getHttpHostFromServer(server,adminHost), true);
            gpc = "servers.server."+server+".system-property.HTTP_LISTENER_PORT.value";
        }
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    instance, gpc, PayaraModule.PROPERTIES_FETCH_TIMEOUT);
            boolean didSet = false;
            if (result.getState() == TaskState.COMPLETED) {
                Map<String, String> values = result.getValue();
                for (Entry<String, String> entry : values.entrySet()) {
                    String val = entry.getValue();
                    try {
                        if (null != val && val.trim().length() > 0) {
                            Integer.parseInt(val);
                            setEnvironmentProperty(PayaraModule.HTTPPORT_ATTR, val, true);
                            didSet = true;
                        }
                    } catch (NumberFormatException nfe) {
                        LOGGER.log(Level.FINEST,
                                "Property value {0} was not a number", val);
                    }
                }
            }
            if (!didSet && !Util.isDefaultOrServerTarget(instance.getProperties())) {
                setEnvironmentProperty(PayaraModule.HTTPPORT_ATTR, "28080", true); // NOI18N
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO, "Could not get http port value.", gfie);
        }
    }

    /**
     * Sends list-applications command to server (synchronous)
     *
     * @param container
     * @return String array of names of deployed applications.
     */
    public Map<String, List<AppDesc>> getApplications(String container) {
        Map<String, List<AppDesc>> result = Collections.emptyMap();
            Map<String, List<String>> apps = Collections.emptyMap();
        try {
            ResultMap<String, List<String>> resultMap
                    = CommandListComponents.listComponents(instance,
                    Util.computeTarget(instance.getProperties()));
            if (resultMap.getState() == TaskState.COMPLETED) {
                apps = resultMap.getValue();
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not retrieve components server.", gfie);
        }
        if (null == apps || apps.isEmpty()) {
            return result;
        }
        try {
            ResultMap<String, String> appPropsResult = CommandGetProperty
                    .getProperties(instance, "applications.application.*");
            if (appPropsResult.getState() == TaskState.COMPLETED) {
                ResultMap<String, String> appRefResult
                        = CommandGetProperty.getProperties(
                        instance, "servers.server.*.application-ref.*");
                if (appRefResult.getState() == TaskState.COMPLETED) {
                    result = processApplications(apps,
                            appPropsResult.getValue(),
                            appRefResult.getValue());
                }
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not retrieve property from server.", gfie);
        }
        return result;
    }

    private Map<String, List<AppDesc>> processApplications(Map<String,
            List<String>> appsList, Map<String, String> properties,
            Map<String, String> refProperties){
        Map<String, List<AppDesc>> result = new HashMap<String, List<AppDesc>>();
        Iterator<String> appsItr = appsList.keySet().iterator();
        while (appsItr.hasNext()) {
            String engine = appsItr.next();
            List<String> apps = appsList.get(engine);
            for (int i = 0; i < apps.size(); i++) {
                String name = apps.get(i).trim();
                String appname = "applications.application." + name; // NOI18N
                String contextKey = appname + ".context-root"; // NOI18N
                String pathKey = appname + ".location"; // NOI18N

                String contextRoot = properties.get(contextKey);
                if (contextRoot == null) {
                    contextRoot = name;
                }
                if (contextRoot.startsWith("/")) {  // NOI18N
                    contextRoot = contextRoot.substring(1);
                }

                String path = properties.get(pathKey);
                if (path == null) {
                    path = "unknown"; //NOI18N
                }
                if (path.startsWith("file:")) {  // NOI18N
                    path = path.substring(5);
                    path = (new File(path)).getAbsolutePath();
                }

                String enabledKey = "servers.server.server.application-ref."
                        +name+ ".enabled";  //NOI18N
                // This needs to be more focused. Does it need to list
                // of servers that are associated with the target?
                for (String possibleKey : refProperties.keySet()) {
                    if (possibleKey.endsWith(".application-ref."
                            + name + ".enabled")) { // NOI18N
                        enabledKey = possibleKey;
                    }
                }
                String enabledValue = refProperties.get(enabledKey);
                if (null != enabledValue) {
                    boolean enabled = Boolean.parseBoolean(enabledValue);

                    List<AppDesc> appList = result.get(engine);
                    if(appList == null) {
                        appList = new ArrayList<AppDesc>();
                        result.put(engine, appList);
                    }
                    appList.add(new AppDesc(name, path, contextRoot, enabled));
                }
            }
        }
        return result;
    }

    /**
     * Retrieve server name using target name from server properties.
     * <p/>
     * @param target Server target name.
     * @return Name of server having this target.
     */
    private String getServerFromTarget(String target) {
        String retVal = target; // NOI18N
        String gpc = "clusters.cluster."+target+".server-ref.*.ref";
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    instance, gpc, PROPERTIES_FETCH_TIMEOUT);
            if (result.getState() == TaskState.COMPLETED) {
                Map<String, String> values = result.getValue();
                for (Entry<String, String> entry : values.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            retVal = val;
                            break;
                        }
                }
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO, "Could not get server value from target.", gfie);
        }

        return retVal;
    }

    /**
     * Retrieve HTTP host name for server from server properties.
     * <p/>
     * @param server          Server name.
     * @param nameOfLocalhost Local host DNS name.
     * @return HTTP host name for server.
     */
    private String getHttpHostFromServer(
            String server, String nameOfLocalhost) {
        String retVal = FAILED_HTTP_HOST;
        String refVal = null;
        String gpc = "servers.server."+server+".node-ref";
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    instance, gpc, PROPERTIES_FETCH_TIMEOUT);
            if (result.getState() == TaskState.COMPLETED) {
                for (Entry<String, String> entry 
                        : result.getValue().entrySet()) {
                    String val = entry.getValue();
                    if (null != val && val.trim().length() > 0) {
                        refVal = val;
                        break;
                    }
                }
                if (refVal != null) {
                    gpc = "nodes.node." + refVal + ".node-host";
                    result = CommandGetProperty.getProperties(
                            instance, gpc, PROPERTIES_FETCH_TIMEOUT);
                    if (result.getState() == TaskState.COMPLETED) {
                        for (Entry<String, String> entry
                                : result.getValue().entrySet()) {
                            String val = entry.getValue();
                            if (null != val && val.trim().length() > 0) {
                                retVal = val;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO, "Could not get http host value.", gfie);
        }
        return LOCALHOST.equals(retVal) ? nameOfLocalhost : retVal; // NOI18N
    }

    private void refreshChildren() {
        PayaraInstance instance = getInstance();
        RefreshModulesCookie cookie = instance.getFullNode().getLookup().lookup(RefreshModulesCookie.class);
        if (cookie != null) {
            cookie.refresh(null, null);
        }
    }

}
