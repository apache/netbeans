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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import static org.netbeans.modules.payara.tooling.PayaraStatus.OFFLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.ONLINE;
import static org.netbeans.modules.payara.tooling.PayaraStatus.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.PayaraStatus.STARTUP;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraStatusTask;
import org.netbeans.modules.payara.common.status.WakeUpStateListener;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.util.NbBundle;

/**
 * Basic common functionality of commands execution.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public abstract class BasicTask<V> implements Callable<V> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Notification about server state check results while waiting for server
     * to start.
     * <p/>
     * Handles initial period of time after starting server.
     * At least port checks are being executed periodically so this class will
     * be called back in any situation.
     */
    protected static class StartStateListener extends WakeUpStateListener {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Is server starting in profiling mode? */
        private final boolean profile;

        /** Payara process being started. */
        private volatile Process process;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of state check results notification.
         * <p/>
         * @param profile Server is starting in profiling mode when
         *                <code>true</code>.
         */
        protected StartStateListener(final boolean profile) {
            super();
            this.profile = profile;
            this.process = null;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters and setters                                                //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Set Payara process being started.
         * <p/>
         * @param process Payara process being started.
         */
        void setProcess(final Process process) {
            this.process = process;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Callback to notify about current server status after every check
         * when enabled.
         * <p/>
         * Wake up startup thread when administrator port is active
         * in profiling mode or when illegal state was detected.
         * <p/>
         * @param server Payara server instance being monitored.
         * @param status Current server status.
         * @param task   Last Payara server status check task details.
         */
        @Override
        public void currentState(final PayaraServer server,
                final PayaraStatus status, final PayaraStatusTask task) {
            switch(status) {
                // Consider server as ready when at least process exists
                // when running in profiling mode.
                case OFFLINE: case STARTUP:
                    if (profile && process != null) {
                        wakeUp();
                    }
                    break;
                // Interrupt waiting for illegal states.
                case ONLINE: case SHUTDOWN:
                    wakeUp();
                    break;
            }
        }

    }

    /**
     * Notification about server state check results while waiting for server
     * to shut down.
     * <p/>
     * Handles period of time until server shuts down completely.
     * At least port checks are being executed periodically so this class will
     * be called back in any situation.
     */
    protected static class ShutdownStateListener extends WakeUpStateListener {

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of state check results notification.
         */
        protected ShutdownStateListener() {
            super();
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Callback to notify about current server status after every check
         * when enabled.
         * <p/>
         * Wake up restart thread when server is not in <code>SHUTDOWN</code>
         * state.
         * <p/>
         * @param server Payara server instance being monitored.
         * @param status Current server status.
         * @param task   Last Payara server status check task details.
         */
        @Override
        public void currentState(final PayaraServer server,
                final PayaraStatus status, final PayaraStatusTask task) {
            if (status != SHUTDOWN) {
                wakeUp();
            }
        }

    }

    /**
     * State change request data.
     */
    protected static class StateChange {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Command execution task. */
        private final BasicTask<?> task;

        /** New state of current command execution. */
        private final TaskState result;

        /** Event that caused  state change. */
        private final TaskEvent event;

        /** Message bundle key. */
        private final String msgKey;

        /** Message arguments. */
        private final String[] msgArgs;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of state change request data.
         * <p/>
         * @param task   Command execution task.
         * @param result New state of current command execution.
         * @param event  Event that caused  state change.
         * @param msgKey Message bundle key.
         */
        protected StateChange(final BasicTask<?> task, final TaskState result,
                final TaskEvent event, final String msgKey) {
            this.task = task;
            this.result = result;
            this.event = event;
            this.msgKey = msgKey;
            this.msgArgs = null;
        }

        /**
         * Constructs an instance of state change request data.
         * <p/>
         * @param task    Command execution task.
         * @param result  New state of current command execution.
         * @param event   Event that caused  state change.
         * @param msgKey  Message bundle key.
         * @param msgArgs Message arguments.
         */
        protected StateChange(final BasicTask<?> task, final TaskState result,
                final TaskEvent event, final String msgKey,
                final String... msgArgs) {
            this.task = task;
            this.result = result;
            this.event = event;
            this.msgKey = msgKey;
            this.msgArgs = msgArgs;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Call all registered callback listeners to inform about state change.
         * <p/>
         * @return Passed new state of current command.
         */
        protected TaskState fireOperationStateChanged() {
            return task.fireOperationStateChanged(
                    result, event, msgKey, msgArgs);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = PayaraLogger.get(BasicTask.class);

    /** Wait duration (ms) between server status checks. */
    public static final int DELAY = 250;
    
    /** Maximum amount of time (in ms) to wait for server to start. */
    public static final int START_TIMEOUT = 300000;
    
    /** Maximum amount of time (in ms) to wait for server to stop. */
    public static final int STOP_TIMEOUT = 180000;

    /** Maximum amount of time (in ms) to wait for server to open debug port
     *  during startup. */
    public static final int START_ADMIN_PORT_TIMEOUT = 120000;

    /** Delay why waiting server to shut down (in ms). */
    public static final int RESTART_DELAY = 5000;

    /** Port check idle (in ms). */
    public static final int PORT_CHECK_IDLE = 500;

    /** Unit (ms) for the DELAY and START_TIMEOUT constants. */
    public static final TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara instance accessed in this task. */
    PayaraInstance instance;

    /** Callback to retrieve state changes. */
    protected TaskStateListener [] stateListener;

    /** Name of Payara instance accessed in this task. */
    protected String instanceName;

    /** Task thread when inside <code>call</code> method. */
    protected volatile Thread taskThread;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Command execution is implemented as <code>call()</code> method in child
     * classes.
     * <p/>
     * @return Command execution result.
     */
    @Override
    public abstract V call();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>BasicTask</code> class.
     * <p/>
     * @param instance Payara instance accessed in this task.
     * @param stateListener Callback listeners used to retrieve state changes.
     */
    protected BasicTask(PayaraInstance instance,
            TaskStateListener... stateListener) {
        this.instance = instance;
        this.stateListener = stateListener;
        this.instanceName = instance.getProperty(PayaraModule.DISPLAY_NAME_ATTR);
        this.taskThread = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize task thread when <code>call</code> method is started.
     * <p/>
     * This should be called immediately after <code>call</code> method
     * is started from inside.
     */
    protected void setTaskThread() {
        taskThread = Thread.currentThread();
    }

    /**
     * Clear task thread when <code>call</code> method is exiting.
     * <p/>
     * This should be called when <code>call</code> method
     * is exiting.
     */
    protected void clearTaskThread() {
        taskThread = null;
    }

    /**
     * Initialize GlassFisg server startup monitoring.
     * <p/>
     * Creates and registers listener to monitor server status during startup.
     * Switches server status monitoring into startup mode.
     * <p/>
     * @param profile Server is starting in profiling mode when
     *                <code>true</code>.
     * @return Listener instance when server startup monitoring was successfully
     *         initialized or  <code>null</code> when something failed.
     */
    protected StartStateListener prepareStartMonitoring(final boolean profile) {
        StartStateListener listener = new StartStateListener(profile);
        if (PayaraStatus.start(instance, false, listener,
                PayaraStatus.ONLINE, PayaraStatus.SHUTDOWN)) {
            PayaraStatus.addCheckListener(instance, listener);
            return listener;
        } else {
            PayaraStatus.removeListener(instance, listener);
            return null;
        }
    }

    /**
     * Force initialization of GlassFisg server startup monitoring.
     * <p/>
     * Creates and registers listener to monitor server status during startup.
     * Switches server status monitoring into startup mode.
     * <p/>
     * @param profile Server is starting in profiling mode when
     *                <code>true</code>.
     * @return Listener instance when server startup monitoring was successfully
     *         initialized or  <code>null</code> when something failed.
     */
    protected StartStateListener forceStartMonitoring(final boolean profile) {
        StartStateListener listener = new StartStateListener(profile);
        if (PayaraStatus.start(instance, true, listener,
                PayaraStatus.OFFLINE, PayaraStatus.ONLINE,
                PayaraStatus.SHUTDOWN, PayaraStatus.UNKNOWN)) {
            PayaraStatus.addCheckListener(instance, listener);
            return listener;
        } else {
            return null;
        }
    }

    /**
     * Initialize GlassFisg server startup monitoring.
     * <p/>
     * Creates and registers listener to monitor server status during shutdown.
     * <p/>
     * @return Listener instance when server startup monitoring was successfully
     *         initialized or  <code>null</code> when something failed.
     */
    protected ShutdownStateListener prepareShutdownMonitoring() {
        ShutdownStateListener listener
                = new ShutdownStateListener();
        if (PayaraStatus.addListener(instance, listener, true,
                PayaraStatus.OFFLINE)) {
            return listener;
        } else {
            PayaraStatus.removeListener(instance, listener);
            return null;
        }
    }

    /**
     * Wait for server to start up.
     * <p/>
     * Wait until server starts.
     * <p/>
     * @param force Force server startup mode.
     * @param profile Server is starting in profiling mode when
     *                <code>true</code>.
     * @return {@see StateChange} request on failure or null on success.
     */
    protected StateChange waitStartUp(final boolean force,
            final boolean profile) {
        StartStateListener listener = force
                ? forceStartMonitoring(profile)
                : prepareStartMonitoring(profile);
        if (listener == null) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "BasicTask.waitShutDown.listenerError",
                    instanceName);
        }
        long start = System.currentTimeMillis();
        LOGGER.log(Level.FINEST, NbBundle.getMessage(RestartTask.class,
                "BasicTask.waitShutDown.waitingTime",
                new Object[] {instanceName, Integer.toString(START_TIMEOUT)}));
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
                    "BasicTask.waitShutDown.interruptedException",
                    new Object[] {
                        instance.getName(), ie.getLocalizedMessage()}));
            
        } finally {
            PayaraStatus.removeListener(instance, listener);
        }
        if (!listener.isWakeUp()) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "BasicTask.waitShutDown.timeout", new String[]
                    {instanceName, Integer.toString(STOP_TIMEOUT)});
        } else {
            return null;
        }
    }

    /**
     * Wait for server to shut down.
     * <p/>
     * Wait until server stops. Stop server log readers.
     * <p/>
     * @return {@see StateChange} request on failure or null on success.
     */
    protected StateChange waitShutDown() {
        ShutdownStateListener listener = prepareShutdownMonitoring();
        if (listener == null) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "BasicTask.waitShutDown.listenerError",
                    instanceName);
        }
        long start = System.currentTimeMillis();
        LOGGER.log(Level.FINEST, NbBundle.getMessage(RestartTask.class,
                "BasicTask.waitShutDown.waitingTime",
                new Object[] {instanceName, Integer.toString(STOP_TIMEOUT)}));
        try {
            synchronized(listener) {
                while (!listener.isWakeUp()
                        && (System.currentTimeMillis()
                        - start < STOP_TIMEOUT)) {
                    listener.wait(System.currentTimeMillis() - start);
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(RestartTask.class,
                    "BasicTask.waitShutDown.interruptedException",
                    new Object[] {
                        instance.getName(), ie.getLocalizedMessage()}));
            
        } finally {
            PayaraStatus.removeListener(instance, listener);
        }
        LogViewMgr.removeLog(instance);
        LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(PayaraModule.URL_ATTR));
        logger.stopReaders();
        if (!listener.isWakeUp()) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "BasicTask.waitShutDown.timeout", new String[]
                    {instanceName, Integer.toString(STOP_TIMEOUT)});
        } else {
            return null;
        }
    }

    /**
     * Call all registered callback listeners to inform about state change.
     * <p/>
     * @param stateType New state of current command execution sent
     *        to listeners. This value will be returned by this method.
     * @param resName Name of the resource to look for message.
     * @param args Additional arguments passed to message.
     * @return Passed new state of current command.
     */
    protected final TaskState fireOperationStateChanged(
            TaskState stateType, TaskEvent te, String resName, String... args) {
        if(stateListener != null && stateListener.length > 0) {
            String msg = NbBundle.getMessage(BasicTask.class, resName, args);
            for(int i = 0; i < stateListener.length; i++) {
                if(stateListener[i] != null) {
                    stateListener[i].operationStateChanged(stateType, te, msg);
                }
            }
        }
        return stateType;
    }
}
