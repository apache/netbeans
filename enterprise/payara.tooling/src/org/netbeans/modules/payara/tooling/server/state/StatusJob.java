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
package org.netbeans.modules.payara.tooling.server.state;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.admin.CommandLocation;
import org.netbeans.modules.payara.tooling.admin.CommandVersion;
import org.netbeans.modules.payara.tooling.admin.Result;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.Runner;
import org.netbeans.modules.payara.tooling.TaskState;
import static org.netbeans.modules.payara.tooling.TaskState.COMPLETED;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheck;
import org.netbeans.modules.payara.tooling.data.PayaraStatusCheckResult;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.logging.Logger;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.NO_CHECK;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.UNKNOWN;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.UNKNOWN_PORT;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.OFFLINE;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.OFFLINE_PORT;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.ONLINE;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.SHUTDOWN;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.SHUTDOWN_PORT;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.STARTUP;
import static org.netbeans.modules.payara.tooling.server.state.StatusJobState.STARTUP_PORT;
import org.netbeans.modules.payara.tooling.utils.LinkedList;
import org.netbeans.modules.payara.tooling.PayaraStatusListener;
import org.netbeans.modules.payara.tooling.data.PayaraStatusTask;

/**
 * Server status check internal data for individual Payara server instance.
 * <p/>
 * @author Tomas Kraus
 */
public class StatusJob {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Common server status check command execution state listener.
     */
    abstract static class Listener implements TaskStateListener {

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(Listener.class);

        /** Should contain {@link Runner} object when it's {@link Runner#call()}
         *  method is executed. */
        Runner runner;

        /** Server status check internal data for individual Payara
         *  server instance. */
        final StatusJob job;

        /**
         * Constructs an instance of common command execution
         * state listener.
         * <p/>
         * @param job Server status check internal data for individual Payara
         *            server instance. 
         */
        private Listener(final StatusJob job) {
            this.runner = null;
            this.job = job;
        }

        /**
         * Set {@link Runner} object instance before it's {@link Runner#call()}
         * method is executed.
         * <p/>
         * @param runner Payara server command execution runner.
         */
        void setRunner(final Runner runner) {
            this.runner = runner;
        }

        /**
         * Clear {@link Runner} object instance after {@link Runner#call()}
         * method is finished.
         */
        void clearRunner() {
            this.runner = null;
        }
        
    }

    /**
     * Locations command execution state listener.
     */
     static class ListenerLocations extends Listener {

        /** Logger instance for this class. */
        private static final Logger LOGGER
                = new Logger(ListenerLocations.class);

        /**
         * Constructs an instance of <code>__locations</code> command execution
         * state listener.
         * <p/>
         * @param job Server status check internal data for individual Payara
         *            server instance. 
         */
        private ListenerLocations(final StatusJob job) {
            super(job);
        }

        /**
         * Evaluate <code>__locations</code> response to verify Payara server
         * home and domain directories.
         * <p/>
         * @return Returns <code>true</code> when <code>__locations</code>
         *         response matches domain directory in Payara server entity
         *         or <code>false</code> otherwise.
         */
        private boolean verifyResult() {
            Result result = runner.getResult();
            if (result instanceof ResultMap) {
                ResultMap<String, String> resultMap
                        = (ResultMap<String, String>)result;
                return CommandLocation.verifyResult(
                        resultMap, job.status.getServer());
            } else {
                return false;
            }
        }

        /**
         * Get notification about state change in {@link Runner} task.
         * <p/>
         * This is being called in {@link Runner#call()} method execution
         * context.
         * <p/>
         * <code>String</codce> arguments passed to state listener:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(final TaskState newState,
        final TaskEvent event, final String... args) {
            final String METHOD = "operationStateChanged";
            switch (newState) {
                case COMPLETED: case FAILED:
                    String serverName;
                    String exceptionMeasage;
                    ResultMap<String, String> taskResult
                            = (ResultMap<String, String>)runner.getResult();
                    if (args != null && args.length >= 3) {
                        serverName = args[0];
                        exceptionMeasage = args[2];
                    } else {
                        serverName = null;
                        exceptionMeasage = null;
                    }
                    // Store task result into job task object
                    boolean notifyError = false;
                    synchronized (job.locations) {
                        switch (newState) {
                            case COMPLETED:
                                // Breaks only for true result. False result
                                // is handeld by FAILED case.
                                if (verifyResult()) {
                                    job.locations.setResult(new StatusResultLocations(
                                            taskResult,
                                            PayaraStatusCheckResult.SUCCESS,
                                            event));
                                    break;
                                }
                            case FAILED:
                                job.locations.setResult(new StatusResultLocations(taskResult,
                                        PayaraStatusCheckResult.FAILED,
                                        event));
                                notifyError = true;
                                break;
                        }
                    }
                    LOGGER.log(Level.FINE, METHOD, "result",
                            job.locations.getResult().status.toString());
                    if (notifyError) {
                        synchronized (job) {
                            job.notifyErrorListeners(job.locations);
                        }
                    }
                    commandTransition(job, job.locations);
            }

        }
        
    }
   
    /**
     * Locations command execution state listener.
     */
     static class ListenerVersion extends Listener {

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(ListenerVersion.class);

        /**
         * Constructs an instance of <code>version</code> command execution
         * state listener.
         * <p/>
         * @param job Server status check internal data for individual Payara
         *            server instance. 
         */
        private ListenerVersion(final StatusJob job) {
            super(job);
        }

        /**
         * Evaluate <code>version</code> response to verify Payara server
         * version.
         * <p/>
         * @return Returns <code>true</code> when <code>version</code>
         *         response matches version in Payara server entity
         *         or <code>false</code> otherwise.
         */
        private boolean verifyResult() {
            Result result = runner.getResult();
            if (result instanceof ResultString) {
                ResultString resultString = (ResultString)result;
                return CommandVersion.verifyResult(
                        resultString, job.status.getServer());
            } else {
                return false;
            }
        }

        /**
         * Get notification about state change in {@link Runner} task.
         * <p/>
         * This is being called in {@link Runner#call()} method execution
         * context.
         * <p/>
         * <code>String</codce> arguments passed to state listener:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(final TaskState newState,
        final TaskEvent event, final String... args) {
            final String METHOD = "operationStateChanged";
            switch (newState) {
                case COMPLETED: case FAILED:
                    String serverName;
                    String exceptionMeasage;
                    ResultString taskResult = (ResultString)runner.getResult();
                    if (args != null && args.length >= 3) {
                        serverName = args[0];
                        exceptionMeasage = args[2];
                    } else {
                        serverName = null;
                        exceptionMeasage = null;
                    }
                    // Store task result into job task object
                    boolean notifyError = false;
                    synchronized (job.version) {
                        switch (newState) {
                            case COMPLETED:
                                // Breaks only for true result. False result
                                // is handeld by FAILED case.
                                if (verifyResult()) {
                                    job.version.setResult(new StatusResultVersion(
                                            taskResult,
                                            PayaraStatusCheckResult.SUCCESS,
                                            event));
                                    break;
                                }
                            case FAILED:
                                job.version.setResult(new StatusResultVersion(taskResult,
                                        PayaraStatusCheckResult.FAILED,
                                        event));
                                notifyError = true;
                                break;
                        }
                    }
                    LOGGER.log(Level.FINE, METHOD, "result",
                            job.version.getResult().status.toString());
                    if (notifyError) {
                        synchronized (job) {
                            job.notifyErrorListeners(job.version);
                        }
                    }
                    commandTransition(job, job.version);
            }
        }
        
    }

     /**
     * Administrator port check task state listener.
     */
     static class ListenerPortCheck extends Listener {

        /** Logger instance for this class. */
        private static final Logger LOGGER
                = new Logger(ListenerPortCheck.class);

        /**
         * Constructs an instance of <code>version</code> command execution
         * state listener.
         * <p/>
         * @param job Server status check internal data for individual Payara
         *            server instance. 
         */
        private ListenerPortCheck(final StatusJob job) {
            super(job);
        }

        /**
         * Get notification about state change in running task.
         * <p/>
         * This is being called in {@link Runnable#run()} method execution
         * context.
         * <p/>
         * <code>String</codce> arguments passed to state listener:<ul>
         *   <li><code>args[0]</code> host name</li>
         *   <li><code>args[1]</code> port check task name</li>
         *   <li><code>args[2]</code> exception message</li>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(final TaskState newState,
        final TaskEvent event, final String... args) {
            final String METHOD = "operationStateChanged";
            // Store task result into job task object
            AdminPortTask task = (AdminPortTask)job.portCheck.getTask();
            job.portCheck.setResult(task.getResult());
            LOGGER.log(Level.FINE, METHOD, "result",
                    job.portCheck.getResult().status.toString());
            // Evaluate taks result
            portCheckTransition(job, job.portCheck);
        }

     }

     /**
      * Individual status check task data.
      */
     static class Task implements PayaraStatusTask {

        /** Server status task execution listener for asynchronous
         * command execution. */
        private Listener listener;

        /** Last command task execution result. */
        StatusResult result;
        
        /** Task thread currently being executed. */
        AbstractTask task;

        /** Task execution handler. */
        private ScheduledFuture future;

        /** All task listeners. */
        private TaskStateListener[] listeners;

        /** Server status check type. */
        private final PayaraStatusCheck type;

        /**
         * Constructs an instance of individual job task.
         * <p/>
         * @param type     Server status check type.
         * @param listener Server status task execution listener
         *                 for asynchronous command execution.
         */
        private Task(final PayaraStatusCheck type,
                final Listener listener) {
            this.listener = listener;
            this.type = type;
            this.result = null;
            this.task = null;
            this.future = null;
            this.listeners = new TaskStateListener[] {listener};
        }

        /**
         * Get server status task execution listener for asynchronous
         * command execution.
         * <p/>
         * @return Server status task execution listener
         *         for asynchronous command execution.
         */
        Listener getListener() {
            return listener;
        }

        /**
         * Set server status task execution listener for asynchronous
         * command execution.
         * <p/>
         * @param listener Server status task execution listener
         *                 for asynchronous command execution.
         */
        void setListener(final Listener listener) {
            this.listener = listener;
        }

        /**
         * Get server status check type.
         * <p/>
         * @return Server status check type.
         */
        @Override
        public PayaraStatusCheck getType() {
            return type;
        }

        /**
         * Get last command task execution result.
         * <p/>
         * @return Last command task execution result.
         */
        StatusResult getResult() {
            return result;
        }

        /**
         * Set last command task execution result.
         * <p/>
         * @param result Last command task execution result.
         */
        void setResult(final StatusResult result) {
            this.result = result;
        }

        /**
         * Get last command task execution status.
         * <p/>
         * @return Last command task execution status.
         */
        @Override
        public PayaraStatusCheckResult getStatus() {
            return result != null ? result.status : null;
        }

        /**
         * Get last command task execution status.
         * <p/>
         * @return Last command task execution status.
         */
        @Override
        public TaskEvent getEvent() {
            return result != null ? result.event : null;
        }

        /**
         * Get task thread currently being executed.
         * <p/>
         * @return Task thread currently being executed.
         */
        AbstractTask getTask() {
            return task;
        }

        /**
         * Set task thread currently being executed.
         * <p/>
         * @param task Task thread currently being executed.
         */
        void setTask(final AbstractTask task) {
            this.task = task;
        }

        /**
         * Get all task listeners.
         * <p/>
         * @return All task listeners.
         */
        TaskStateListener[] getListeners() {
            return listeners;
        }

        /**
         * Get task execution handler.
         * <p/>
         * @return Task execution handler.
         */
        ScheduledFuture getFuture() {
            return future;
        }

        /**
         * Set task execution handler.
         * <p/>
         * @param future Task execution handler.
         */
        void setFuture(final ScheduledFuture future) {
            this.future = future;
        }

        /**
         * Set task execution handler and thread currently being executed.
         * <p/>
         * @param task Task thread currently being executed.
         * @param future Task execution handler.
         */
        void setTaskFuture(final AbstractTask task,
                final ScheduledFuture future) {
            this.task = task;
            this.future = future;
        }

        /**
         * Clear task execution handler and thread currently being executed.
         */
        void clearTaskFuture() {
            this.task = null;
            this.future = null;
        }

        /**
         * Evaluate task execution result.
         * <p/>
         * Task will fail only when task is scheduled for execution
         * (<code>task</code> value is not null) and stored task result
         * is <code>FAILED</code> or does not exist.
         * <p/>
         * @return Task execution result evaluation.
         */
        PayaraStatusCheckResult evalResult() {
            return task == null
                    ? PayaraStatusCheckResult.SUCCESS : result == null
                    ? PayaraStatusCheckResult.FAILED : result.status;
        }

     }

     /**
      * Individual administrator command status check task data.
      */
     static class RunnerTask extends Task {

         /** Server administration command to be executed. */
         private final Command command;

        /**
         * Constructs an instance of individual job runner task.
         * <p/>
         * @param type     Server status check type.
         * @param cmd      Server administration command to be executed.
         * @param listener Server status task execution listener
         *                 for asynchronous command execution.
         */
        private RunnerTask(final PayaraStatusCheck type,
                final Command cmd, final Listener listener) {
            super(type, listener);
            this.command = cmd;
        }

        /**
         * Get server administration command to be executed.
         * <p/>
         * @return Server administration command to be executed.
         */
        Command getCommand() {
            return command;
        }

     }

     /**
      * Individual administrator command status check task data using
      * <code>version</code> command.
      */
     static class RunnerTaskLocations extends RunnerTask {

        /**
         * Constructs an instance of individual job runner task running
         * <code>__locations</code> command.
         * <p/>
         * @param listener Server status task execution listener
         *                 for asynchronous command execution.
         */
        private RunnerTaskLocations(final Listener listener) {
            super(PayaraStatusCheck.LOCATIONS,
                    new CommandLocation(), listener);
        }
         
     }

     /**
      * Individual administrator command status check task data using
      * <code>version</code> command.
      */
     static class RunnerTaskVersion extends RunnerTask {

        /**
         * Constructs an instance of individual job runner task running
         * <code>version</code> command.
         * <p/>
         * @param listener Server status task execution listener
         *                 for asynchronous command execution.
         */
        private RunnerTaskVersion(final Listener listener) {
            super(PayaraStatusCheck.VERSION,
                    new CommandVersion(), listener);
        }
         
     }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(StatusJob.class);

    /** State transition depending on administrator port check result . */
    private static final StatusJobState[][] portCheckTransition = {
       //       SUCCESS   FAILED
        {      NO_CHECK,      NO_CHECK }, // NO_CHECK
        {  UNKNOWN_PORT,       OFFLINE }, // UNKNOWN
        {  UNKNOWN_PORT,       OFFLINE }, // UNKNOWN_PORT
        {  OFFLINE_PORT,       OFFLINE }, // OFFLINE
        {  OFFLINE_PORT,       OFFLINE }, // OFFLINE_PORT
        {  STARTUP_PORT,       STARTUP }, // STARTUP
        {  STARTUP_PORT,       OFFLINE }, // STARTUP_PORT
        {        ONLINE,  OFFLINE_PORT }, // ONLINE
        {      SHUTDOWN, SHUTDOWN_PORT }, // SHUTDOWN
        { SHUTDOWN_PORT,       OFFLINE }  // SHUTDOWN_PORT
    };

    /** State transition depending on administrator command execution result. */
    private static final StatusJobState[][] commandTransition = {
       //       SUCCESS   FAILED
        {      NO_CHECK,      NO_CHECK }, // NO_CHECK
        {        ONLINE,       UNKNOWN }, // UNKNOWN
        {        ONLINE,       OFFLINE }, // UNKNOWN_PORT
        {        ONLINE,       OFFLINE }, // OFFLINE
        {        ONLINE,  OFFLINE_PORT }, // OFFLINE_PORT
        {        ONLINE,       STARTUP }, // STARTUP
        {        ONLINE,  STARTUP_PORT }, // STARTUP_PORT
        {        ONLINE,  OFFLINE_PORT }, // ONLINE
        {      SHUTDOWN, SHUTDOWN_PORT }, // SHUTDOWN
        {      SHUTDOWN, SHUTDOWN_PORT }  // SHUTDOWN_PORT
    };
   
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Evaluate check result depending on running tasks and their result.
     * <p/>
     * @param job Server status job.
     * @return Check result depending on running tasks and their result.
     */
    private static PayaraStatusCheckResult checkResult(final StatusJob job) {
        return PayaraStatusCheckResult.and(job.portCheck.evalResult(),
                job.version.evalResult(), job.locations.evalResult());
    }

    /**
     * Handles state transition and server status check tasks reschedule
     * for administrator port check result evaluation.
     * <p/>
     * @param job  Server status job.
     * @param task Payara server status check task details.
     */
    private static void portCheckTransition(
            final StatusJob job, final PayaraStatusTask task) {
        final String METHOD = "portCheckTransition";
        PayaraStatus oldState;
        PayaraStatus newState;
        synchronized (job) {
            StatusJobState oldInternalState = job.state;
            oldState = job.status.getStatus();
            job.state = portCheckTransition[job.state.ordinal()]
                    [job.portCheck.result.status.ordinal()];
            LOGGER.log(Level.FINE, METHOD, "transition",
                    new String[] {job.portCheck.result.status.toString(),
                oldInternalState.toString(), job.state.toString()});
            if (oldInternalState != job.state) {
                StatusScheduler scheduler = StatusScheduler.getInstance();
                scheduler.remove(job);
                scheduler.reschedule(job);
            }
            newState = job.state.toPayaraStatus();
            if (oldState != newState) {
                job.status.setStatus(newState);                
            }
        }
        if (job.portCheck.result.status == PayaraStatusCheckResult.FAILED) {
            job.notifyErrorListeners(job.portCheck);
        }
        if (oldState != newState) {
            job.notifyNewStatusListeners(job.status.getStatus(), task);
        }
        job.notifyCurrStatusListeners(job.status.getStatus(), task);
    }

    /**
     * Handles state transition and server status check tasks reschedule
     * for administrator command execution result evaluation.
     * <p/>
     * @param job  Server status job.
     * @param task Payara server status check task details.
     */
    private static void commandTransition(
            final StatusJob job, final PayaraStatusTask task) {
        final String METHOD = "commandLocationsTransition";
        PayaraStatusCheckResult status = checkResult(job);
        PayaraStatus oldState;
        PayaraStatus newState;
        synchronized (job) {
            StatusJobState oldInternalState = job.state;
            oldState = job.status.getStatus();
            job.state = commandTransition[job.state.ordinal()]
                    [status.ordinal()];
            LOGGER.log(Level.FINE, METHOD, "transition", new String[] {status.
                toString(), oldInternalState.toString(), job.state.toString()});
            if (oldInternalState != job.state) {
                StatusScheduler scheduler = StatusScheduler.getInstance();
                scheduler.remove(job);
                scheduler.reschedule(job);
            }
            newState = job.state.toPayaraStatus();
            if (oldState != newState) {
                job.status.setStatus(newState);                
            }
        }
        if (oldState != newState) {
            job.notifyNewStatusListeners(job.status.getStatus(), task);
        }
        job.notifyCurrStatusListeners(job.status.getStatus(), task);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server status entity. */
    private final PayaraStatusEntity status;

    /** Server status job internal state. */
    private StatusJobState state;

    /** Administrator port check asynchronous task. */
    private final Task portCheck;

    /** Command <code>__locations</code> asynchronous task. */
    private final RunnerTask locations;

    /** Command <code>version</code> asynchronous task. */
    private final RunnerTask version;

    /** Listeners to be notified about server status change. */
    private final LinkedList<PayaraStatusListener>[] newStatusListeners;

    /** Listeners to be notified about every server status check result. */
    private final LinkedList<PayaraStatusListener> currStatusListeners;

    /** Listeners to be notified about every server status check error. */
    private final LinkedList<PayaraStatusListener> errorListeners;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server status check internal data.
     * <p/>
     * @param status Payara server status entity.
     */
    StatusJob(final PayaraStatusEntity status) {
        this.status = status;
        state = StatusJobState.UNKNOWN;
        portCheck = new Task(
                PayaraStatusCheck.PORT, new ListenerPortCheck(this));
        locations = new RunnerTaskLocations(new ListenerLocations(this));
        version = new RunnerTaskVersion(new ListenerVersion(this));
        newStatusListeners = new LinkedList[PayaraStatus.length];
        for (int i = 0; i < PayaraStatus.length; i++) {
            newStatusListeners[i] = new LinkedList<>();
        }
        currStatusListeners = new LinkedList<>();
        errorListeners = new LinkedList<>();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * get Payara server status entity.
     * <p/>
     * @return Payara server status entity.
     */
    PayaraStatusEntity getStatus() {
        return status;
    }

    /**
     * Get server status job internal state.
     * <p/>
     * @return Server status job internal state.
     */
    StatusJobState getState() {
        return state;
    }

    /**
     * Get server status job internal state.
     * <p/>
     * @param state Server status job internal state.
     */
    void setState(final StatusJobState state) {
        this.state = state;
    }

    /**
     * Get administrator port check asynchronous task.
     * <p/>
     * @return Administrator port check asynchronous task.
     */
    Task getPortCheck() {
        return portCheck;
    }

    /**
     * Get command <code>__locations</code> asynchronous task.
     * <p/>
     * @return Command <code>__locations</code> asynchronous task.
     */
    RunnerTask getLocations() {
        return locations;
    }

    /**
     * Get command <code>version</code> asynchronous task.
     * <p/>
     * @return Command <code>version</code> asynchronous task.
     */
    RunnerTask getVersion() {
        return version;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Attempts to restart job switching it into <code>UNKNOWN_PORT</code> state
     * to restart status checking with fastest possible check.
     * <p/>
     * Job can be switched into <code>UNKNOWN_PORT</code> state only when it's
     * in <code>NO_CHECK</code> state. This is equivalent to state transition
     * methods.
     * If listener is provided, it will be registered to receive any state
     * change notification following status checking restart. This listener
     * won't be unregistered automatically so caller should handle it properly.
     * <p/>
     * Called by job tasks scheduler.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     * @param listener  Server status listener to be registered when status
     *                  checking is being restarted.
     * @return Value of <code>true</code> when job was successfully switched
     *         into startup mode or false otherwise.
     */
    boolean restartJob(final StatusScheduler scheduler,
            final PayaraStatusListener listener) {
        boolean result;
        synchronized (this) {
            switch (state) {
                case NO_CHECK:
                    state = UNKNOWN_PORT;
                    status.setStatus(state.toPayaraStatus());
                    scheduler.remove(this);
                    scheduler.reschedule(this);
                    result = true;
                    if (listener != null) {
                        addNewStatusListener(listener, PayaraStatus.values());
                    }
                    break;
                default:
                    result = false;
            }
        }
        return result;
    }

    /**
     * Stops job switching it into <code>NO_CHECK</code> state.
     * <p/>
     * Job can be switched into <code>NO_CHECK</code> state from any state.
     * This is equivalent to state transition methods.
     * <p/>
     * Called by job tasks scheduler.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     */
    void stopJob(StatusScheduler scheduler) {
        synchronized (this) {
            PayaraStatus oldStatus = state.toPayaraStatus();
            state = NO_CHECK;
            status.setStatus(state.toPayaraStatus());
            scheduler.remove(this);
            scheduler.reschedule(this);
            if (oldStatus != status.getStatus()) {
                notifyNewStatusListeners(status.getStatus(), null);
            }
        }
    }

    /**
     * Switches job into <code>STARTUP</code> state.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     * @param listener  Server status listener to be registered together with
     *                  switching into startup mode.
     * @param newState  Notify about server status change for new states
     *                  provided as this argument.
     */
    private void startStateImpl(StatusScheduler scheduler,
            final PayaraStatusListener listener,
            final PayaraStatus... newState) {
        PayaraStatus oldStatus = status.getStatus();
        state = STARTUP;
        status.setStatus(state.toPayaraStatus());
        scheduler.remove(this);
        scheduler.reschedule(this);
        if (oldStatus != status.getStatus()) {
            notifyNewStatusListeners(status.getStatus(), null);
        }
        if (listener != null) {
            addNewStatusListener(listener, newState);
        }
    }

    /**
     * Attempts to switch job into <code>STARTUP</code> state.
     * <p/>
     * Job can be switched into <code>STARTUP</code> state only when it's
     * in <code>OFFLINE</code> or <code>OFFLINE_PORT</code> state. This is
     * equivalent to state transition methods.
     * <p/>
     * Called by job tasks scheduler.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     * @param force     Force startup mode for Payara server instance
     *                  from any state then <code>true</code>.
     * @param listener  Server status listener to be registered together with
     *                  switching into startup mode.
     * @param newState  Notify about server status change for new states
     *                  provided as this argument.
     * @return Value of <code>true</code> when job was successfully switched
     *         into startup mode or false otherwise.
     */
    boolean startState(StatusScheduler scheduler, final boolean force,
            final PayaraStatusListener listener,
            final PayaraStatus... newState) {
        boolean result;
        synchronized (this) {
            switch (state) {
                case OFFLINE:
                case OFFLINE_PORT:
                    startStateImpl(scheduler, listener, newState);
                    result = true;
                    break;
                case STARTUP:
                    result = false;
                    break;
                default:
                    if (force) {
                        startStateImpl(scheduler, listener, newState);
                        result = true;
                    } else {
                        result = false;
                    }
            }
        }
        return result;
    }

    /**
     * Attempts to switch job into <code>SHUTDOWN</code> state.
     * <p/>
     * Job can be switched into <code>SHUTDOWN</code> state only when it's
     * in <code>ONLINE</code> state. This is equivalent to state transition
     * methods.
     * <p/>
     * Called by job tasks scheduler.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     * @return Value of <code>true</code> when job was successfully switched
     *         into shutdown mode or false otherwise.
     */
    boolean shutdownState(StatusScheduler scheduler) {
        boolean result;
        synchronized (this) {
            switch (state) {
                case ONLINE:
                    state = SHUTDOWN;
                    status.setStatus(state.toPayaraStatus());
                    scheduler.remove(this);
                    scheduler.reschedule(this);
                    result = true;
                    notifyNewStatusListeners(status.getStatus(), null);
                    break;
                default:
                    result = false;
            }
        }
        return result;
    }

    /**
     * Initial server status task schedule.
     * <p/>
     * Called by job tasks scheduler.
     * <p/>
     * @param scheduler Job tasks scheduler calling this method.
     */
    void scheduleNew(StatusScheduler scheduler) {
        synchronized (this) {
            scheduler.scheduleNew(this);
        }
    }

    /**
     * Notify server status change listeners about state change.
     * <p/>
     * Listeners list access is synchronized but they are just copied into
     * temporary array and executed outside synchronized block to avoid
     * deadlocks.
     * <p/>
     * @param status Current server status.
     * @param task   Payara server status check task details.
     */
    public void notifyNewStatusListeners(
            final PayaraStatus status, final PayaraStatusTask task) {
        PayaraStatusListener[] call;
        // Copy registered status change listeners.
        int i = 0;
        boolean isElement;
        LinkedList<PayaraStatusListener> listeners;
        synchronized (newStatusListeners) {
            listeners = newStatusListeners[status.ordinal()];
            call = new PayaraStatusListener[listeners.size()];
            isElement = listeners.first();
            while(isElement) {
                call[i++] = listeners.getCurrent();
                isElement = listeners.next();
            }
        }
        // Execute them outside synchronized block.
        for (PayaraStatusListener listener : call) {
            listener.newState(this.status.getServer(), status, task);
        }
    }

    /**
     * Notify server status change listeners about current server status
     * after every check.
     * <p/>
     * Listeners list access is synchronized but they are just copied into
     * temporary array and executed outside synchronized block to avoid
     * deadlocks.
     * <p/>
     * @param status Current server status.
     * @param task   Payara server status check task details.
     */
    public void notifyCurrStatusListeners(
            final PayaraStatus status, final PayaraStatusTask task) {
        PayaraStatusListener[] call;
        // Copy registered status change listeners.
        int i = 0;
        boolean isElement;
        synchronized (currStatusListeners) {
            call = new PayaraStatusListener[currStatusListeners.size()];
            isElement = currStatusListeners.first();
            while(isElement) {
                call[i++] = currStatusListeners.getCurrent();
                isElement = currStatusListeners.next();
            }
        }
        // Execute them outside synchronized block.
        for (PayaraStatusListener listener : call) {
            listener.currentState(this.status.getServer(), status, task);
        }
    }

    /**
     * Notify server status check error listeners about every check error.
     * <p/>
     * Listeners list access is synchronized but they are just copied into
     * temporary array and executed outside synchronized block to avoid
     * deadlocks.
     * <p/>
     * @param task   Payara server status check task details.
     */
    public void notifyErrorListeners(final PayaraStatusTask task) {
        PayaraStatusListener[] call;
        // Copy registered status change listeners.
        int i = 0;
        boolean isElement;
        synchronized (errorListeners) {
            call = new PayaraStatusListener[errorListeners.size()];
            isElement = errorListeners.first();
            while(isElement) {
                call[i++] = errorListeners.getCurrent();
                isElement = errorListeners.next();
            }
        }
        // Execute them outside synchronized block.
        for (PayaraStatusListener listener : call) {
            listener.error(this.status.getServer(), task);
        }
    }

    /**
     * Register server status listener to be notified about current server
     * status after every check.
     * <p/>
     * @param listener Server status listener to be registered.
     * @return Value of <code>true</code> when listener was added
     *         or <code>false</code> when this listener was already registered.
     */
    public boolean addCurrStatusListener(
            final PayaraStatusListener listener) {
        boolean added = false;
        boolean exists = false;
        boolean isElement;
        synchronized (currStatusListeners) {
            isElement = currStatusListeners.first();
            while(isElement) {
                if (listener.equals(currStatusListeners.getCurrent())) {
                    exists = true;
                }
                isElement = currStatusListeners.next();
            }
            if (!exists) {
                currStatusListeners.addLast(listener);
                added = true;
            }
        }
        if (added && listener != null) {
            listener.added();
        }
        return added;
    }

    /**
     * Register server status listener to be notified about server status
     * change.
     * <p/>
     * @param listener Server status listener to be registered.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when listener was added in at least
     *         one list or <code>false</code> when this listener was already
     *         registered in all requested lists.
     */
    public boolean addNewStatusListener(
            final PayaraStatusListener listener,
            final PayaraStatus... newState) {
        boolean added = false;
        boolean exists;
        boolean isElement;
        LinkedList<PayaraStatusListener> listeners;
        synchronized (newStatusListeners) {
            for (PayaraStatus stateToAdd : newState) {
                listeners = newStatusListeners[stateToAdd.ordinal()];
                exists = false;
                isElement = listeners.first();
                while(isElement) {
                    if (listener.equals(listeners.getCurrent())) {
                        exists = true;
                    }
                    isElement = listeners.next();
                }
                if (!exists) {
                    listeners.addLast(listener);
                    added = true;
                }

            }
        }
        if (added && listener != null) {
            listener.added();
        }
        return added;
    }

    /**
     * Register server status listener to be notified about server status
     * check errors.
     * <p/>
     * @param listener Server status listener to be registered.
     * @return Value of <code>true</code> when listener was added
     *         or <code>false</code> when this listener was already registered.
     */
    public boolean addErrorListener(
            final PayaraStatusListener listener) {
        boolean added = false;
        boolean exists = false;
        boolean isElement;
        synchronized (errorListeners) {
            isElement = errorListeners.first();
            while(isElement) {
                if (listener.equals(errorListeners.getCurrent())) {
                    exists = true;
                }
                isElement = errorListeners.next();
            }
            if (!exists) {
                errorListeners.addLast(listener);
                added = true;
            }
        }
        if (added && listener != null) {
            listener.added();
        }
        return added;
    }

    /**
     * Register server status listener.
     * <p/>
     * @param listener Server status listener to be registered.
     * @param currentState Notify about current server status after every check
     *                     when <code>true</code>.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when listener was added in at least
     *         one list or <code>false</code> when this listener was already
     *         registered in all requested lists.
     */
    public boolean addStatusListener(
            final PayaraStatusListener listener,
            final boolean currentState, final PayaraStatus... newState) {
        boolean added;
        // Notify about current server status after every check.
        if (currentState) {
            added = addCurrStatusListener(listener);
        } else {
            added = false;
        }
        // Notify about server status change.
        if (newState != null) {
            added = addNewStatusListener(listener, newState) || added;
        }
        if (added && listener != null) {
            listener.added();
        }
        return added;
    }

    /**
     * Unregister server status listener.
     * <p/>
     * @param listener Server status listener to be unregistered.
     * @return Value of <code>true</code> when listener was found and removed
     *         or <code>false</code> when listener was not found among
     *         registered listeners.
     */
    public boolean removeStatusListener(
            final PayaraStatusListener listener) {
        boolean removed = false;
        // Remove from current server status after every check list.
        boolean isElement;
        synchronized (currStatusListeners) {
            isElement = currStatusListeners.first();
            while(isElement) {
                if (listener.equals(currStatusListeners.getCurrent())) {
                    currStatusListeners.removeAndNext();
                    isElement = currStatusListeners.isCurrent();
                    removed = true;
                } else {
                    isElement = currStatusListeners.next();
                }
            }
        }
        // Remove from server status change lists.
        LinkedList<PayaraStatusListener> listeners;
        synchronized (newStatusListeners) {
            for (PayaraStatus stateToRemove : PayaraStatus.values()) {
                listeners = newStatusListeners[stateToRemove.ordinal()];
                isElement = listeners.first();
                while(isElement) {
                    if (listener.equals(listeners.getCurrent())) {
                        listeners.removeAndNext();
                        isElement = listeners.isCurrent();
                        removed = true;
                    } else {
                        isElement = listeners.next();
                    }
                }

            }
        }
        // Remove from server status check errors list.
        synchronized (errorListeners) {
            isElement = errorListeners.first();
            while(isElement) {
                if (listener.equals(errorListeners.getCurrent())) {
                    errorListeners.removeAndNext();
                    isElement = errorListeners.isCurrent();
                    removed = true;
                } else {
                    isElement = errorListeners.next();
                }
            }
        }
        if (removed && listener != null) {
            listener.removed();
        }
        return removed;
    }

}
