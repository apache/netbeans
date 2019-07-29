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

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import static org.netbeans.modules.payara.tooling.PayaraStatus.OFFLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.ONLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.PayaraStatus.STARTUP;
import static org.netbeans.modules.payara.tooling.PayaraStatus.UNKNOWN;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.CommandRestartDAS;
import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.admin.CommandStopDAS;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import static org.netbeans.modules.payara.common.BasicTask.START_TIMEOUT;
import static org.netbeans.modules.payara.common.BasicTask.STOP_TIMEOUT;
import static org.netbeans.modules.payara.common.BasicTask.TIMEUNIT;
import static org.netbeans.modules.payara.common.PayaraState.getStatus;
import org.netbeans.modules.payara.spi.CommandFactory;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 *
 * @author Peter Williams
 * @author Vince Kraemer
 */
public class RestartTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = PayaraLogger.get(RestartTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** How long to wait after stopping server to let OS clean up resources. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private static final int RESTART_DELAY = 5000;

    /** Common support object for the server instance being restarted. */
    private final CommonServerSupport support;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of asynchronous Payara server restart command
     * execution support object.
     * <p/>
     * @param support       Common support object for the server instance being
     *                      restarted
     * @param stateListener State monitor to track restart progress.
     */
    public RestartTask(CommonServerSupport support, TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        this.support = support;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Start local server that is offline.
     * <p/>
     * @return State change request about offline remote server start request.
     */
    private StateChange localOfflineStart() {
        Future<TaskState> startTask
                = support.startServer(null, ServerState.RUNNING);
        TaskState startResult = TaskState.FAILED;
        try {
            startResult = startTask.get(START_TIMEOUT, TIMEUNIT);
        } catch (Exception ex) {
            LOGGER.log(Level.FINER,
                    ex.getLocalizedMessage(), ex);
        }
        if (startResult == TaskState.FAILED) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "RestartTask.localOfflineStart.failed", instanceName);
        }
        return new StateChange(this,
                TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                "RestartTask.localOfflineStart.completed", instanceName);
    }

    /**
     * Start remote server that is offline.
     * <p/>
     * This operation is not possible and will always fail.
     * <p/>
     * @return State change request about offline remote server start request.
     */
    private StateChange remoteOfflineStart() {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.remoteOfflineStart.failed", instanceName);
    }
   
    /**
     * Wait for local server currently shutting down and start it up.
     * <p/>
     * @return State change request about local server (that is shutting down)
     *         start request.
     */
    private StateChange localShutdownStart() {
        StateChange stateChange = waitShutDown();
        if (stateChange != null) {
            return stateChange;
        }
        PayaraServerStatus status = getStatus(instance);
        switch(status.getStatus()) {
            case UNKNOWN: case ONLINE: case SHUTDOWN: case STARTUP:
                return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localShutdownStart.notOffline",
                    instanceName);
            default:
                if (!ServerUtils.isAdminPortListening(
                        instance, NetUtils.PORT_CHECK_TIMEOUT)) {
                    return localOfflineStart();
                } else {
                return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localShutdownStart.portOccupied",
                    instanceName);                    
                }
        }
    }

    /**
     * Wait for remote server currently shutting down and start it up.
     * <p/>
     * This operation is not possible and will always fail.
     * <p/>
     * @return State change request about remote server (that is shutting down)
     *         start request.
     */
    private StateChange remoteShutdownStart() {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.remoteShutdownStart.failed", instanceName);        
    }

    /**
     * Wait for server to start up.
     * <p/>
     * @return State change request.
     */
    private StateChange startupWait() {
        StartStateListener listener = prepareStartMonitoring(true);
        if (listener == null) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.startupWait.listenerError",
                    instanceName);
        }
        long start = System.currentTimeMillis();
        try {
            synchronized(listener) {
                while (!listener.isWakeUp()
                        && (System.currentTimeMillis()
                        - start < START_TIMEOUT)) {
                    listener.wait(System.currentTimeMillis() - start);
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(RestartTask.class,
                    "RestartTask.startupWait.interruptedException",
                    new String[] {
                        instance.getName(), ie.getLocalizedMessage()}));
            
        } finally {
            PayaraStatus.removeListener(instance, listener);
        }
        if (PayaraState.isOnline(instance)) {
              return new StateChange(this,
                      TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                      "RestartTask.startupWait.completed", instanceName);
        } else {
              return new StateChange(this,
                      TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                      "RestartTask.startupWait.failed", instanceName);
        }
    }

    /**
     * Full restart of local online server.
     * <p/>
     * @return State change request.
     */
    private StateChange localRestart() {
        if (PayaraStatus.shutdown(instance)) {
            ResultString result = CommandStopDAS.stopDAS(instance);
            if (result.getState() == TaskState.COMPLETED) {
                return localShutdownStart();
            } else {
                // TODO: Reset server status monitoring
                return new StateChange(this,
                        TaskState.FAILED, TaskEvent.CMD_FAILED,
                        "RestartTask.localRestart.cmdFailed", instanceName);
            }
        } else {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localRestart.failed", instanceName);
        }
    }

    /**
     * Update server debug options before restart.
     */
    private boolean updateDebugOptions(final int debugPort) {
        boolean updateResult = false;
        try {
            ResultMap<String, String> result
                    = CommandGetProperty.getProperties(instance,
                    "configs.config.server-config.java-config.debug-options");
            if (result.getState() == TaskState.COMPLETED) {
                Map<String, String> values = result.getValue();
                if (values != null && !values.isEmpty()) {
                    CommandFactory commandFactory =
                            instance.getInstanceProvider().getCommandFactory();
                    String oldValue = values.get(
                            "configs.config.server-config.java-config.debug-options");
                    CommandSetProperty setCmd =
                            commandFactory.getSetPropertyCommand(
                            "configs.config.server-config.java-config.debug-options",
                            oldValue.replace("transport=dt_shmem", "transport=dt_socket").
                            replace("address=[^,]+", "address=" + debugPort));
                    try {
                        CommandSetProperty.setProperty(instance, setCmd);
                        updateResult = true;
                    } catch (PayaraIdeException gfie) {
                        LOGGER.log(Level.INFO, debugPort + "", gfie);
                    }
                }
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not retrieve property from server.", gfie);
        }
        return updateResult;
    }

    /**
     * Wait for debug port to become active.
     * <p/>
     * @return Value of <code>true</code> if port become active before timeout
     *         or <code>false</code> otherwise.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private boolean vaitForDebugPort(final String host, final int port) {
        boolean result = NetUtils.isPortListeningRemote(
                host, port, NetUtils.PORT_CHECK_TIMEOUT);
        if (!result) {
            long tmStart = System.currentTimeMillis();
            while (!result
                    && System.currentTimeMillis() - tmStart
                    < START_ADMIN_PORT_TIMEOUT) {
                try {
                    Thread.sleep(PORT_CHECK_IDLE);
                } catch (InterruptedException ex) {}
                result = NetUtils.isPortListeningRemote(
                        host, port, NetUtils.PORT_CHECK_TIMEOUT);
            }
        }
        return result;
    }

    /**
     * Full restart of remote online server.
     * <p/>
     * @return State change request.
     */
    private StateChange remoteRestart() {
        boolean debugMode = instance.getJvmMode() == PayaraJvmMode.DEBUG;
        // Wrong scenario as default.
        boolean debugPortActive = true;
        int debugPort = -1;
        if (debugMode) {
            debugPort = instance.getDebugPort();
            debugMode = updateDebugOptions(debugPort);
            debugPortActive = NetUtils.isPortListeningRemote(
                    instance.getHost(), debugPort, NetUtils.PORT_CHECK_TIMEOUT);
        }
        ResultString result
                = CommandRestartDAS.restartDAS(instance, debugMode);
        LogViewMgr.removeLog(instance);
        LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(PayaraModule.URL_ATTR));
        logger.stopReaders();                
        switch (result.getState()) {
            case COMPLETED:
                if (debugMode && !debugPortActive) {
                    vaitForDebugPort(instance.getHost(), debugPort);
                    waitStartUp(true, false);
// This probably won't be needed.
//                } else {
//                    try {
//                        Thread.sleep(RESTART_DELAY);
//                    } catch (InterruptedException ex) {}
                }
                return new StateChange(this,
                        result.getState(), TaskEvent.CMD_COMPLETED,
                        "RestartTask.remoteRestart.completed", instanceName);
            default:
                return new StateChange(this,
                        result.getState(), TaskEvent.CMD_COMPLETED,
                        "RestartTask.remoteRestart.failed", new String[] {
                            instanceName, result.getValue()});
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Restart Payara server.
     * <p/>
     * Possible states are <code>UNKNOWN</code>, <code>OFFLINE</code>,
     * <code>STARTUP</code>, <code>ONLINE</code> and <code>SHUTDOWN</code>:
     * <p/>
     * <code>UNKNOWN</code>:  Do nothing. UI shall not allow restarting while
     *                        server status is unknown.
     * <code>OFFLINE</code>:  Server is already offline, let's start it
     *                        if administrator port is not occupied.
     * <code>STARTUP</code>:  We are already in the middle of startup process.
     *                        Let's just wait for sever to start.
     * <code>ONLINE</code>:   Full restart is needed.
     * <code>SHUTDOWN</code>: Shutdown process has already started, let's wait
     *                        for it to finish. Server will be started after
     *                        that.
     */
    @Override
    public TaskState call() {
        PayaraStatus state = PayaraState.getStatus(instance).getStatus();
        StateChange change;
        switch (state) {
            case UNKNOWN:
                return fireOperationStateChanged(
                        TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                        "RestartTask.call.unknownState", instanceName);
            case OFFLINE:
                change = instance.isRemote()
                        ? remoteOfflineStart() : localOfflineStart();
                return change.fireOperationStateChanged();
            case STARTUP:
                change = startupWait();
                return change.fireOperationStateChanged();
            case ONLINE:
                change = instance.isRemote()
                        ? remoteRestart() : localRestart();
                return change.fireOperationStateChanged();
            case SHUTDOWN:
                change = instance.isRemote()
                        ? remoteShutdownStart() : localShutdownStart();
                return change.fireOperationStateChanged();
            // This shall be unrechable, all states should have
            // own case handlers.
            default:
                return fireOperationStateChanged(
                        TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                        "RestartTask.call.unknownState", instanceName);                
        }
    }

    /**
     * Restart operation:
     *
     * RUNNING -> stop server
     *            start server
     *
     * STARTING -> wait for state == STOPPED or RUNNING.
     *
     * STOPPED -> start server
     *
     * STOPPING -> wait for state == STOPPED
     *             start server
     *
     * For all of the above, command succeeds if state == RUNNING at the end.
     * 
     */
    @SuppressWarnings("SleepWhileInLoop")
//    @Override
    public TaskState call2() {
        Logger.getLogger("payara").log(Level.FINEST,
                "RestartTask.call() called on thread \"{0}\"",
                Thread.currentThread().getName());
        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "MSG_RESTART_SERVER_IN_PROGRESS", instanceName);

        //ServerState state = support.getServerState();
        PayaraStatus state = PayaraState.getStatus(instance).getStatus();

        if (state == PayaraStatus.STARTUP) {
            // wait for start to finish, we are done.
            PayaraStatus currentState = state;
            int steps = (START_TIMEOUT / DELAY);
            int count = 0;
            while (currentState == PayaraStatus.STARTUP && count++ < steps) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    Logger.getLogger("payara").log(Level.FINER,
                            ex.getLocalizedMessage(), ex);
                }
                currentState = PayaraState.getStatus(instance).getStatus();
            }

            if (!PayaraState.isOnline(instance)) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_RESTART_SERVER_FAILED_WONT_START", instanceName);
            }
        } else {
            boolean postStopDelay = true;
            if (state == PayaraStatus.ONLINE) {
                    Future<TaskState> stopTask = support.stopServer(null);
                    TaskState stopResult = TaskState.FAILED;
                    try {
                        stopResult = stopTask.get(STOP_TIMEOUT, TIMEUNIT);
                    } catch (Exception ex) {
                        Logger.getLogger("payara").log(Level.FINER,
                                ex.getLocalizedMessage(), ex);
                    }

                    if (stopResult == TaskState.FAILED) {
                        return fireOperationStateChanged(TaskState.FAILED,
                                TaskEvent.CMD_FAILED,
                                "MSG_RESTART_SERVER_FAILED_WONT_STOP",
                                instanceName);
                    }
            } else if (state == PayaraStatus.SHUTDOWN) {
                // wait for server to stop.
                PayaraStatus currentState = state;
                int steps = (STOP_TIMEOUT / DELAY);
                int count = 0;
                while (currentState == PayaraStatus.SHUTDOWN && count++ < steps) {
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException ex) {
                        Logger.getLogger("payara").log(Level.FINER,
                                ex.getLocalizedMessage(), ex);
                    }
                    currentState = PayaraState.getStatus(instance).getStatus();
                }

                if (!PayaraState.isOffline(instance)) {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_RESTART_SERVER_FAILED_WONT_STOP",
                            instanceName);
                }
            } else {
                postStopDelay = false;
            }
            
            if (postStopDelay) {
                // If we stopped the server (or it was already stopping), delay
                // start for a few seconds to let system clean up ports.
                support.setServerState(ServerState.STARTING);
                try {
                    Thread.sleep(RESTART_DELAY);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }

            // Server should be stopped. Start it.
            Object o = support.setEnvironmentProperty(PayaraModule.JVM_MODE,
                    PayaraModule.NORMAL_MODE, false);
            if (PayaraModule.PROFILE_MODE.equals(o)) {
                support.setEnvironmentProperty(PayaraModule.JVM_MODE,
                        PayaraModule.NORMAL_MODE, false);
            }
            Future<TaskState> startTask = support.startServer(null, ServerState.RUNNING);
            TaskState startResult = TaskState.FAILED;
            try {
                startResult = startTask.get(START_TIMEOUT, TIMEUNIT);
            } catch (Exception ex) {
                Logger.getLogger("payara").log(Level.FINER,
                        ex.getLocalizedMessage(), ex); // NOI18N
            }
            
            if (startResult == TaskState.FAILED) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_RESTART_SERVER_FAILED_WONT_START",
                        instanceName);
            }
            
            if (!support.isRemote()
                    && support.getServerState() != ServerState.RUNNING) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_RESTART_SERVER_FAILED_REASON_UNKNOWN",
                        instanceName);
            }
        }
        
        return fireOperationStateChanged(TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED,
                "MSG_SERVER_RESTARTED", instanceName);
    }
}
