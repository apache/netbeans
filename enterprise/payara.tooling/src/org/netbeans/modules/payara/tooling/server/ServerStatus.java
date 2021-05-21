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
package org.netbeans.modules.payara.tooling.server;

import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.CommandLocation;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.tooling.admin.CommandVersion;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.TaskState;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Check server status using administration commands <code>__locations</code>
 * and <code>version</code> and also verify if server is at least listening
 * on its administration port.
 * <p/>
 * Administration commands and port check are run in parallel to reduce delay.
 * <p/>
 * @author Tomas Kraus
 */
public class ServerStatus implements Closeable {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Individual server check status returned.
     */
    public static enum Status {
        /** Server status check passed. */
        SUCCESS,

        /** Server status check failed with <code>FAILED</code> result. */
        FAILED,

        /** Server status check failed on timeout. */
        TIMEOUT,

        /** Server status check failed on IO Exception. */
        EXCEPTION,

        /** Server status check failed because of invalid arguments. */
        INVALID,

        /** Server status check failed because of unexpected fatal issue. */
        FATAL;

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Convert <code>Status</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch(this) {
                case SUCCESS:   return "SUCCESS";
                case FAILED:    return "FAILED";
                case TIMEOUT:   return "TIMEOUT";
                case EXCEPTION: return "EXCEPTION";
                case INVALID:   return "INVALID";
                case FATAL:     return "FATAL";
                default:
                    throw new IllegalStateException("Unknown Status value");
            }
        }

    }

    /**
     * Individual server status result including additional information.
     */
    public static class Result {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Individual server status returned. */
        final Status status;

        /** IO Exception caught. */
        private final IOException ioe;

        /** Fatal issue Exception caught. */
        private final Exception ex;

        /** Task failure event. */
        private final TaskEvent failureEvent;

        /** Server name. */
        private final String serverName;

        /** Exception message. */
        private final String exceptionMeasage;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * IO Exception caught in asynchronous task is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ioe    IO Exception caught in asynchronous task.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        Result(final Status status, final IOException ioe,
                final TaskEvent failureEvent, final String serverName,
                final String exceptionMeasage) {
            this.status = status;
            this.ioe = ioe;
            this.ex = null;
            this.failureEvent = failureEvent;
            this.serverName = serverName;
            this.exceptionMeasage = exceptionMeasage;
        }

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * Common Exception caught is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ex     Common Exception caught.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        Result(final Status status, final Exception ex,
                final TaskEvent failureEvent, final String serverName,
                final String exceptionMeasage) {
            this.status = status;
            this.ioe = null;
            this.ex = ex;
            this.failureEvent = failureEvent;
            this.serverName = serverName;
            this.exceptionMeasage = exceptionMeasage;
        }

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * No additional value except result is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        Result(final Status status, final TaskEvent failureEvent,
                final String serverName, final String exceptionMeasage) {
            this.status = status;
            this.ioe = null;
            this.ex = null;
            this.failureEvent = failureEvent;
            this.serverName = serverName;
            this.exceptionMeasage = exceptionMeasage;
        }

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * IO Exception caught in asynchronous task is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ioe    IO Exception caught in asynchronous task.
         */
        Result(final Status status, final IOException ioe) {
            this(status, ioe, null, null, null);
        }

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * Common Exception caught is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ex     Common Exception caught.
         */
        Result(final Status status, final Exception ex) {
            this(status, ex, null, null, null);
        }

        /**
         * Creates an instance of individual server status result.
         * <p/>
         * No additional value except result is stored.
         * <p/>
         * @param status Individual server status returned.
         */
        Result(final Status status) {
            this(status, null, null, null);
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get individual check task status.
         * <p/>
         * @return Individual check task status.
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Get task failure event.
         * <p/>
         * @return Task failure event.
         */
        public TaskEvent getFailureEvent() {
            return failureEvent;
        }

        /**
         * Get server name.
         * <p/>
         * @return Server name.
         */
        public String getServerName() {
            return serverName;
        }

        /**
         * Get exception message.
         * <p/>
         * @return Exception message.
         */
        public String getExceptionMeasage() {
            return exceptionMeasage;
        }
    }

    /**
     * Server status task execution result for <code>__locations</code> command 
     * including additional information.
     * <p/>
     * This class stores task execution result only. Value <code>SUCCESS</code>
     * means that Locations command task execution finished successfully but it
     * does not mean that administration command itself returned with
     * <code>COMPLETED</code> status.
     * When <code>SUCCESS</code> status is set, stored <code>result</code> value
     * shall be examined too to see real administration command execution
     * result.
     */
    public static class ResultLocations extends Result {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Command <code>__locations</code> execution result. */
        final ResultMap<String, String> result;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of individual server status result
         * for <code>__locations</code> command.
         * <p/>
         * Command <code>__locations</code> result is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultLocations(final ResultMap<String, String> result,
                final Status status, final TaskEvent failureEvent,
                final String serverName, final String exceptionMeasage) {
            super(status, failureEvent, serverName, exceptionMeasage);
            this.result = result;
        }

        /**
         * Creates an instance of individual server status result
         * for <code>__locations</code> command.
         * <p/>
         * Common Exception caught is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ex     Common Exception caught.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultLocations(final  Status status, final Exception ex,
                final TaskEvent failureEvent, final String serverName,
                final String exceptionMeasage) {
            super(status, ex, failureEvent, serverName, exceptionMeasage);
            this.result = null;
        }

        /**
         * Creates an instance of individual server status result
         * for <code>__locations</code> command.
         * <p/>
         * No additional value except result is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultLocations(final Status status, final TaskEvent failureEvent,
                final String serverName, final String exceptionMeasage) {
            super(status, failureEvent, serverName, exceptionMeasage);
            this.result = null;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get <code>__locations</code> command execution result.
         * <p/>
         * @return <code>__locations</code> command execution result.
         */
        public ResultMap<String, String> getResult() {
            return result;
        }

    }

    /**
     * Individual server status result for <code>version</code> command 
     * including additional information.
     * <p/>
     * This class stores task execution result only. Value <code>SUCCESS</code>
     * means that Locations command task execution finished successfully but it
     * does not mean that administration command itself returned with
     * <code>COMPLETED</code> status.
     * When <code>SUCCESS</code> status is set, stored <code>result</code> value
     * shall be examined too to see real administration command execution
     * result.
     */
    public static class ResultVersion extends Result {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Command <code>version</code> execution result. */
        final ResultString result;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of individual server status result
         * for <code>version</code> command.
         * <p/>
         * Command <code>version</code> result is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultVersion(final ResultString result, final Status status,
                final TaskEvent failureEvent, final String serverName,
                final String exceptionMeasage) {
            super(status, failureEvent, serverName, exceptionMeasage);
            this.result = result;
        }

        /**
         * Creates an instance of individual server status result
         * for <code>version</code> command.
         * <p/>
         * Common Exception caught is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param ex     Common Exception caught.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultVersion(final Status status, final Exception ex,
                final TaskEvent failureEvent, final String serverName,
                final String exceptionMeasage) {
            super(status, ex, failureEvent, serverName, exceptionMeasage);
            this.result = null;
        }

        /**
         * Creates an instance of individual server status result
         * for <code>version</code> command.
         * <p/>
         * No additional value except result is stored.
         * <p/>
         * @param status Individual server status returned.
         * @param failureEvent     Failure cause.
         * @param serverName       Target Payara server name.
         * @param exceptionMeasage Exception message from command task.
         */
        ResultVersion(final  Status status, final TaskEvent failureEvent,
                final String serverName, final String exceptionMeasage) {
            super(status, failureEvent, serverName, exceptionMeasage);
            this.result = null;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get <code>version</code> command execution result.
         * <p/>
         * @return <code>version</code> command execution result.
         */
        public ResultString getResult() {
            return result;
        }

    }

    /**
     * Common individual server status check task.
     */
    private static abstract class Task implements TaskStateListener {

        ////////////////////////////////////////////////////////////////////////
        // Static methods                                                     //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Format time value in miliseconds to be printed as value in seconds
         * and miliseconds <code>s.ms<code>.
         * <p/>
         * @param tm Time value in miliseconds
         * @return Time string formated as econds and miliseconds
         *         <code>s.ms<code>.
         */
        static String tm(final long tm) {
            StringBuilder sb = new StringBuilder(8);
            sb.append(Long.toString(tm/1000));
            sb.append('.');
            sb.append(Long.toString(tm%1000));
            return sb.toString();
        }

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Payara server to be tested. */
        final PayaraServer server;

        /** Task start time. Used for logging purposes. Value of <code>-1</code>
         *  means that start time was not set.*/
        long tmStart;

        /** Task failure event filled by last state change. */
        TaskEvent failureEvent;

        /** Server name filled by last state change. */
        String serverName;

        /** Exception message filled by last state change. */
        String exceptionMeasage;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of common individual server status check.
         * <p/>
         * @param server Payara server to be checked.
         */
        Task(final PayaraServer server) {
            this.server = server;
            this.tmStart = -1;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters and setters                                                //
        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Compute task result waiting timeout based on task start time, actual
         * time and minimal timeout limit.
         * <p/>
         * @param startup Startup mode to select longer timeout.
         * @return Task result waiting timeout.
         */
        long timeout(final boolean startup) {
            long timeout = (startup ? COMAND_STARTUP_TIMEOUT : COMAND_TIMEOUT)
                    - System.currentTimeMillis() + tmStart;
            if (timeout > COMAND_TIMEOUT_MIN) {
                return timeout;
            } else {
                return COMAND_TIMEOUT_MIN;
            }
        }

        /**
         * Store event that caused task failure.
         * <p/>
         * @param newState Current task state.
         * @param event    Event that caused task change.
         * @param args     Unused interface parameter.
         */
        @Override
        public void operationStateChanged(
                final TaskState newState, final TaskEvent event,
                final String[] args) {
            if (args != null && args.length >= 3) {
                serverName = args[0];
                exceptionMeasage = args[2];
            } else {
                serverName = exceptionMeasage = null;
            }
            switch(newState) {
                case FAILED:
                    failureEvent = event;
            }
        }

    }

    /**
     * Individual server status check task to verify if server administration
     * port is alive.
     * <p/>
     * This task does not run in a separate thread but uses existing main thread
     * instead.
     */
    private static class AdminPortTask extends Task {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(AdminPortTask.class);

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Server administration interface host. */
        String host;
        
        /** Server administration interface port. */
        int port;

        /** Socked connecting timeout [ms]. */
        int timeout;

        /** Server administration port status check result. */
        private Result result;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of administration port status check.
         * <p/>
         * @param server  Payara server to be checked.
         * @param timeout Socked connecting timeout.
         */
        AdminPortTask(final PayaraServer server, final int timeout) {
            super(server);
            this.host = server.getHost();
            this.port = server.getAdminPort();
            this.timeout = timeout;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get server administration port status check result.
         * <p/>
         * @return Server administration port status check result.
         */
        Result getResult() {
            return result;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Close socket and handle <code>IOException</code> that could
         * be thrown.
         * <p/>
         * @param socket Socket to be closed.
         */
        private void closeSocket(final Socket socket) {
            try {
                socket.close();
            } catch (IOException ioe) {
                handleIOException(ioe, host, port,
                        "Socket closing failed when connecting to {0}:{1}: {2}");
            }
        }

        /**
         * Handle IO Exception caught in server administration port verification
         * task.
         * <p/>
         * @param ioe     <code>IOException</code> caught.
         * @param host    Server administration host.
         * @param port    Server administration port.
         * @param message Message to be logged. Shall not be <code>null</code>.
         */
        private void handleIOException(final IOException ioe,
                final String host, final int port, final String message) {
            final String METHOD = "handleIOException";
            String logMsg = MessageFormat.format(message, new Object[] {
                server.getName(), host, Integer.toString(port),
                ioe.getMessage()});
            if (tmStart >= 0 && LOGGER.isLoggable(Level.FINE)) {
                long tm = System.currentTimeMillis() - tmStart;
                LOGGER.log(Level.FINE, METHOD, "messageTm",
                        new Object[] {logMsg, tm(tm)});

            } else {
                LOGGER.log(Level.INFO, METHOD, "message", logMsg);
            }
        } 

        // Based on original code from
        // org.netbeans.modules.payara.common.CommonServerSupport.isRunning(...)
        /**
         * Parent thread task to verify if server administration port is alive.
         * <p/>
         * @return Returns <code>true</code> when server administration port
         *         is alive or <code>false</code> otherwise.
         */
        Result check() {
            final String METHOD = "check";
            if (port < 0 || host == null) {
                result = new Result(Status.INVALID);
                return result;
            }
            this.tmStart = System.currentTimeMillis();
            InetSocketAddress sa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            try {
                socket.connect(sa, timeout);
                socket.setSoTimeout(timeout);
            } catch (java.net.ConnectException ce) {
                handleIOException(ce, host, port,
                        "[{0}] Port check could not connect to {1}:{2}: {3}");
                result = new Result(Status.FAILED, ce);
                return result;
            } catch (java.net.SocketTimeoutException ste) {
                handleIOException(ste, host, port,
                        "[{0}] Port check timeout when connecting to {1}:{2}: {3}");
                result =  new Result(Status.TIMEOUT, ste);
                return result;
            } catch (IOException ioe) {
                handleIOException(ioe, host, port,
                        "[{0}] Port check caught IO exception when connecting to {1}:{2}: {3}");
                result =  new Result(Status.EXCEPTION, ioe);
                return result;
            } finally {
                closeSocket(socket);
            }
            if (tmStart >= 0 && LOGGER.isLoggable(Level.FINE)) {
                long tm = System.currentTimeMillis() - tmStart;
                LOGGER.log(Level.FINE, METHOD, "success",
                        new Object[] {tm(tm), server.getName()});
            }
            result = new Result(Status.SUCCESS);
            return result;
        }

    }

    /**
     * Individual server status check task with <code>__locations</code>
     * administration command execution.
     */
    private static class LocationsTask extends Task {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(LocationsTask.class);

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Locations command. */
        private final CommandLocation command;

        /** Locations command execution result; */
        private Future<ResultMap<String, String>> future;

        /** Locations command result; */
        ResultMap<String, String> taskResult;

        /** Locations command status check result. */
        private ResultLocations result;

        /** Startup mode. Triggers longer administration commands execution
         *  timeouts when <code>true</code>. */
        private final boolean startup;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of <code>__locations</code> server status check.
         * <p/>
         * @param server Payara server to be checked.
         * @param startup Trigger startup mode. Triggers longer administration
         *                commands execution timeouts when <code>true</code>.
         */
        LocationsTask(final PayaraServer server, final boolean startup) {           
            super(server);
            this.command = new CommandLocation();
            this.startup = startup;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get <code>__locations</code> command status check result.
         * <p/>
         * @return <code>__locations</code> command status check result.
         */
        ResultLocations getResult() {
            return result;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Start server Location task.
         */
        void start(final ExecutorService executor) {
            final String METHOD = "start";
            this.tmStart = System.currentTimeMillis();
            future = ServerAdmin.<ResultMap<String, String>>
                    exec(executor, server, command, this);
            if (tmStart >=0 && LOGGER.isLoggable(Level.FINE)) {
                long tm = System.currentTimeMillis() - tmStart;
                LOGGER.log(Level.FINE, METHOD, "started", tm(tm));
            }
        }

        /**
         * Log Exception caught on task join.
         * <p/>
         * @param ex Exception caught.
         */
        private void logExceptionOnJoin(final Exception ex) {
            final String METHOD = "logExceptionOnJoin";
            LOGGER.log(Level.FINE, METHOD, "failed",
                    new Object[] {tm(System.currentTimeMillis() - tmStart),
                        ex.getClass().getName(),
                        ex.getMessage() != null ? ex.getMessage() : ""});
        }

        /**
         * Wait for server Location task to finish.
         */
        void join() {
            final String METHOD = "join";
            try {
                taskResult = future.get(
                        timeout(startup), TimeUnit.MILLISECONDS);
                result = new ResultLocations(taskResult, Status.SUCCESS,
                        failureEvent, serverName, exceptionMeasage);
                LOGGER.log(Level.FINE, METHOD, "completed",
                        tm(System.currentTimeMillis() - tmStart));
            // This means administration port is not responding.
            } catch (TimeoutException te) {
                result = new ResultLocations(Status.TIMEOUT,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(te);
            // Expected exceptions are handled in call() method so this
            // is something serious.
            } catch (ExecutionException ee) {
                result = new ResultLocations(Status.FATAL, ee,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(ee);
            // Interrupted after administration port check failed.
            } catch (InterruptedException | CancellationException ie) {
                result = new ResultLocations(Status.FAILED, ie,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(ie);
            // Cancelled after administration port check failed.
            }
        }

        /**
         * Attempt to cancel execution of this task.
         */
        void cancel() {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }

    }

    /**
     * Individual server status check task with <code>version</code>
     * administration command execution.
     */
    private static class VersionTask extends Task {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** Logger instance for this class. */
        private static final Logger LOGGER = new Logger(VersionTask.class);

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Version command. */
        private final CommandVersion command;

        /** Version command execution result; */
        private Future<ResultString> future;

        /** Version command result; */
        ResultString taskResult;

        /** Version command status check result. */
        private ResultVersion result;

        /** Startup mode. Triggers longer administration commands execution
         *  timeouts when <code>true</code>. */
        private final boolean startup;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of <code>version</code> server status check.
         * <p/>
         * @param server Payara server to be checked.
         * @param startup Trigger startup mode. Triggers longer administration
         *                commands execution timeouts when <code>true</code>.
         */
        VersionTask(final PayaraServer server, final boolean startup) {           
            super(server);
            this.command = new CommandVersion();
            this.startup = startup;
        }


        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get <code>version</code> command status check result.
         * <p/>
         * @return <code>version</code> command status check result.
         */
        ResultVersion getResult() {
            return result;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Start server Version task.
         */
        void start(final ExecutorService executor) {
            final String METHOD = "start";
            this.tmStart = System.currentTimeMillis();
            future = ServerAdmin.<ResultString>
                    exec(executor, server, command, this);
            if (tmStart >=0 && LOGGER.isLoggable(Level.FINE)) {
                long tm = System.currentTimeMillis() - tmStart;
                LOGGER.log(Level.FINE, METHOD, "started", tm(tm));
            }
        }

        /**
         * Log Exception caught on task join.
         * <p/>
         * @param ex Exception caught.
         */
        private void logExceptionOnJoin(final Exception ex) {
            final String METHOD = "logExceptionOnJoin";
            LOGGER.log(Level.FINE, METHOD, "failed",
                    new Object[] {tm(System.currentTimeMillis() - tmStart),
                        ex.getClass().getName(),
                        ex.getMessage() != null ? ex.getMessage() : ""});
        }

        /**
         * Wait for server Version task to finish.
         */
        void join() {
            final String METHOD = "join";
            try {
                taskResult = future.get(
                        timeout(startup), TimeUnit.MILLISECONDS);
                result = new ResultVersion(taskResult, Status.SUCCESS,
                        failureEvent, serverName, exceptionMeasage);
                LOGGER.log(Level.FINE, "completed",
                        tm(System.currentTimeMillis() - tmStart));
            // This means administration port is not responding.
            } catch (TimeoutException te) {
                result = new ResultVersion(Status.TIMEOUT,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(te);
            // Expected exceptions are handled in call() method so this
            // is something serious.
            } catch (ExecutionException ee) {
                result = new ResultVersion(Status.FATAL, ee,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(ee);
            // Interrupted after administration port check failed.
            } catch (InterruptedException | CancellationException ie) {
                result = new ResultVersion(Status.FAILED, ie,
                        failureEvent, serverName, exceptionMeasage);
                logExceptionOnJoin(ie);
            // Cancelled after administration port check failed.
            }
        }

        /**
         * Attempt to cancel execution of this task.
         */
        void cancel() {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerStatus.class);

    /** Executor thread pool size (amount of threads to run in parallel). */
    private static final int EXECUTOR_POOL_SIZE = 2;

    /** Administration port connect timeout [ms]. */
    private static final int CONNECT_TIMEOUT = 15000;

    /** Minimal administration command execution timeout [ms]. */
    private static final int COMAND_TIMEOUT_MIN = 100;

    /** Administration command execution timeout [ms]. */
    private static final int COMAND_TIMEOUT = 30000;
    
    /** Administration command execution timeout [ms] in startup mode. */
    private static final int COMAND_STARTUP_TIMEOUT = 600000;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Executor used to run asynchronous server status checks in parallel. */
    private final ExecutorService executor;

    /** Server status check task to verify if server administration
     *  port is alive. */
    private final AdminPortTask adminPortTask;

    /** Server status check task with <code>version</code> administration
     *  command. */
    private final VersionTask versionTask;

    /** Server status check task with <code>__locations</code> administration
     *  command. */
    private final LocationsTask locationsTask;

    /** Asynchronous server status checks start time. Used for logging
     *  purposes. Value of <code>-1</code> means that start time was not set. */

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of server status check.
     * <p/>
     * Method {@link #close()} must be called at the end to release
     * system resources.
     * <p/>
     * @param server  Payara server to be checked.
     * @param startup Trigger startup mode. Triggers longer administration
     *                commands execution timeouts when <code>true</code>.
     */
    public ServerStatus(final PayaraServer server, final boolean startup) {
        this.executor = ServerAdmin.executor(EXECUTOR_POOL_SIZE);
        this.adminPortTask = new AdminPortTask(server, CONNECT_TIMEOUT);
        this.versionTask = new VersionTask(server, startup);
        this.locationsTask = new LocationsTask(server, startup);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get server administration port status check result.
     * <p/>
     * @return Server administration port status check result.
     */
    public Result getAdminPortResult() {
        return adminPortTask.result;
    }

    /**
     * Get <code>version</code> command status check result.
     * <p/>
     * @return <code>version</code> command status check result.
     */
    public ResultVersion getVersionResult() {
        return versionTask.result;
    }

    /**
     * Get <code>__locations</code> command status check result.
     * <p/>
     * @return <code>__locations</code> command status check result.
     */
    public ResultLocations getLocationsResult() {
        return locationsTask.result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Run asynchronous server status checks.
     * <p/>
     * Server administration port status check is run in parent thread. When
     * administration port has not been available, remaining command tasks
     * are canceled.
     */
    public void check() {
        versionTask.start(executor);
        locationsTask.start(executor);
        Result result = adminPortTask.check();
        if (result.status != Status.SUCCESS) {
            versionTask.cancel();
            locationsTask.cancel();
        }
        versionTask.join();
        locationsTask.join();
    }

    /**
     * Clean up all resources.
     * <p/>
     * Removes internal thread pool. 
     * 
     */
    @Override
    public void close() {
        executor.shutdownNow();
    }

}
