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
package org.netbeans.modules.glassfish.tooling.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.net.ssl.*;
import org.netbeans.modules.glassfish.tooling.GlassFishToolsConfig;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;

/**
 * Abstract GlassFish server administration command execution.
 * <p/>
 * Abstract class implements common GlassFish server administration
 * functionality
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Runner implements Callable<Result> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * State change request data.
     */
    protected static class StateChange {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** GlassFish server administration command runner. */
        private final Runner runner;

        /** New task execution state. */
        private final TaskState taskState;

        /** Event related to execution state change. */
        private final TaskEvent taskEvent;

        /** Additional arguments. */
        private final String[] args;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of state change request data.
         * <p/>
         * @param runner       GlassFish server administration command runner. 
         * @param taskState    New task execution state.
         * @param taskEvent    Event related to execution state change.
         * @param args         Additional state change request arguments.
         */
        protected StateChange(final Runner runner, final TaskState taskState,
            final TaskEvent taskEvent, final String... args) {
            this.runner = runner;
            this.taskState = taskState;
            this.taskEvent = taskEvent;
            this.args = args;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Update task state value in <code>Result</code> object and notify
         * all registered command execution state listeners about command
         * execution state change.
         * <p/>
         * @return GlassFish administration command receiveResult with updated
         *         task execution state.
         */
        protected Result handleStateChange() {
            return runner.handleStateChange(taskState, taskEvent, args);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(Runner.class);

    /** Socket connection timeout (in miliseconds). */
    public static final int HTTP_CONNECTION_TIMEOUT = 3000;

    /** Delay before administration command execution will be retried. */
    public static final int HTTP_RETRY_DELAY = 3000;

    /** Character used to separate query string from list of parameters. */
    static final char QUERY_SEPARATOR = '?';

    /** Character used to separate individual parameters. */
    static final char PARAM_SEPARATOR = '&';

    /** Character used to separate individual items in parameters. */
    static final char ITEM_SEPARATOR = ':';

    /** Character used to assign value to parameter. */
    static final char PARAM_ASSIGN_VALUE = '=';

    /** <code>String</code> value for <code>true</code>. */
    static final String TRUE_VALUE = "true";

    /** <code>String</code> value for <code>false</code>. */
    static final String FALSE_VALUE = "false";

    /** Executor used to serialize administration commands passed to GlassFish
     *  server. */
    private static volatile ExecutorService executor;

    /* Global password authenticator for GlassFish servers. */
    private static volatile Authenticator authenticator;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns shared <code>Executor</code> instance to serialize administration
     * commands passed to GlassFish server.
     * <p/>
     * <code>Executor</code> instance is handled internally.
     * <p/>
     * @return Shared <code>Executor</code> instance.
     */
    static ExecutorService serializedExecutor() {
        if (executor != null) {
            return executor;
        }
        synchronized (Runner.class) {
            if (executor == null) {
                executor = Executors.newFixedThreadPool(1);
            }
        }
        return executor;
    }
 
    /**
     * Allows to initialize this class to use external
     * <code>Authenticator</code>.
     * <p/>
     * This method must be called before first usage of <code>call()</code>
     * method.
     * <p/>
     * @param authenticator External authenticator for GlassFish servers
     *                      to be supplied.
     */
    public static void init(final Authenticator authenticator) {
        synchronized (Runner.class) {
            if (Runner.authenticator == null) {
                Runner.authenticator = authenticator;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Get external <code>Authenticator</code> if set.
     * @return External <code>Authenticator</code> for GlassFish servers
     *         or <code>null</code> if no external <code>Authenticator</code>
     *         was set.
     */
    private static Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Returns individual <code>Executor</code> instance to run administration
     * commands passed to GlassFish server in parallel.
     * <p/>
     * @param size Thread pool size.
     * @return Individual <code>Executor</code> instance.
     */
    static ExecutorService parallelExecutor(final int size) {
        return Executors.newFixedThreadPool(size);
    }

    /**
     * Convert <code>boolean</code> value to <code>String</code> constant.
     * <p/>
     * @param value <code>boolean</code> value to be converted.
     * @return <code>String</code> constant.
     */
    static String toString(final boolean value) {
        return value ? TRUE_VALUE : FALSE_VALUE;
    }

    /**
     * Convert <code>String</code> constant representing <code>boolean</code>
     * value to it's original <code>boolean</code> value.
     * <p/>
     * @param constant Constant representing <code>boolean</code> value.
     * @return Original <code>boolean</code> value.
     * @throws CommandException when constant representing <code>boolean</code>
     *         value is not recognized.
     */
    static boolean toBoolean(final String constant) throws CommandException {
        if (constant == null || constant.length() < 1)
            throw new CommandException(
                    CommandException.INVALID_BOOLEAN_CONSTANT);
        switch(constant.charAt(0)) {
            case 'T': case 't':
                if (constant.regionMatches(true, 1, "rue", 1, 3))
                    return true;
                else
                    throw new CommandException(
                            CommandException.INVALID_BOOLEAN_CONSTANT);
            case 'F': case 'f':
                if (constant.regionMatches(true, 1, "alse", 1, 4))
                    return false;
                else
                    throw new CommandException(
                            CommandException.INVALID_BOOLEAN_CONSTANT);
            default:
                throw new CommandException(
                        CommandException.INVALID_BOOLEAN_CONSTANT);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Allow commands to fail without a big message. */
    protected boolean silentFailureAllowed = false;

    /** Server command path (e.g. "/__asadmin/").*/
    protected String path;

    /**
     * Sometimes (e.g. during startup), the server does not accept commands. In
     * such cases, it will block for 20 seconds and then return with the message
     * "V3 cannot process this command at this time, please wait".
     * <p/>
     * In such cases, we set a flag and have the option to reissue the command.
     */
    boolean retry = false;

    /** Authentication success or failure. */
    boolean auth = true;

    /** Holding GlassFish server for command execution. */
    protected GlassFishServer server;

    /** Holding data for command execution. */
    final Command command;

    /**
     * GlassFish administration command execution result.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and receiveResult value
     * handling.
     */
    Result<?> result;

    /** Query string for this command. */
    final String query;

    /** Listeners that want to know about command state. */
    protected TaskStateListener[] stateListeners;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>Result</code> object corresponding to command execution
     * value to be returned.
     */
    protected abstract Result<?> createResult();

    /**
     * Send information to the server via HTTP POST.
     * <p/>
     * @return <code>true</code> if using HTTP POST to send to server
     *         or <code>false</code> otherwise
     */
    public abstract boolean getDoOutput();

    /**
     * Inform whether this runner implementation accepts gzip format.
     * <p/>
     * @return <code>true</code> when gzip format is accepted,
     *         <code>false</code> otherwise.
     */
    public abstract boolean acceptsGzip();

    /**
     * Template method to allow descendants to implement creation of command
     * url.
     * <p/>
     * @return url to connect to
     */
    protected abstract String constructCommandUrl() throws CommandException;

    /**
     * Override to change the type of HTTP method used for this command. Default
     * is GET.
     * <p/>
     * @return HTTP method (GET, POST, etc.)
     */
    protected abstract String getRequestMethod();

    /**
     * Override this method to handle sending data to server.
     * <p/>
     * In some cases command has to send data to server. To do that, override
     * this method.
     * <p/>
     * @param hconn
     * @throws IOException
     */
    protected abstract void handleSend(final HttpURLConnection hconn)
            throws IOException;

    /**
     * Override this method to read response from provided input stream.
     * <p/>
     * Override to read the response data sent by the server. Do not close the
     * stream parameter when finished. Caller will take care of that.
     * <p/>
     * @param in Stream to read data from.
     * @return true if response was read correctly.
     * @throws java.io.IOException in case of stream error.
     */
    protected abstract boolean readResponse(final InputStream in,
            final HttpURLConnection hconn);

    /**
     * Override to parse, validate, and/or format any data read from the server
     * in readResponse() / readManifest().
     * <p/>
     * @return true if data was processed correctly.
     */
    protected abstract boolean processResponse();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish Server HTTP Administration Command Entity.
     * @param path    Server command path.
     */
    Runner(final GlassFishServer server, final Command command,
            final String path) {
        this(server, command, path, null);
    }

    /**
     * Constructs an instance of administration command executor using HTTP
     * interface.
     * <p/>
     * @param server GlassFish server entity object.
     * @param command GlassFish Server HTTP Administration Command Entity.
     * @param path    Server command path.
     * @param query   Query string for this command.
     */
    Runner(final GlassFishServer server, final Command command,
            final String path, final String query) {
        this.server = server;
        this.command = command;
        this.path = path;
        this.query = query;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the query string for this command. Value is set in constructor.
     * <p/>
     * @return query string for this command.
     */
    String getQuery() {
        return query;
    }

    /**
     * Get GlassFish server administration command entity.
     * <p/>
     * @return GlassFish server administration command entity.
     */
    Command getCommand() {
        return command;
    }

    /**
     * Get command execution result after task is finished.
     * <p/>
     * @return GlassFish administration command execution result.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Do we allow commands to fail without a big message?
     * <p/>
     * SDK Exception functional test.
     * <p/>
     * @return The silentFailureAllowed.
     */
    protected boolean isSilentFailureAllowed() {
        return silentFailureAllowed;
    }

    /**
     * Set whether we allow commands to fail without a big message.
     * <p/>
     * @param silentFailureAllowed The silentFailureAllowed to set.
     */
    public void setSilentFailureAllowed(final boolean silentFailureAllowed) {
        this.silentFailureAllowed = silentFailureAllowed;
    }

    /**
     * Set listeners to notify about task state changes.
     * <p/>
     * Listeners must be set before task is executed.
     * <p/>
     * @param listeners Listeners to notify about task state changes.
     */
    public void setStateListeners(final TaskStateListener[] listeners) {
        this.stateListeners = listeners;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fake getters                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Override to set the content-type of information sent to the server.
     * Default is null (not set).
     * <p/>
     * @return content-type of data sent to server via HTTP POST
     */
    public String getContentType() {
        return null;
    }

    /**
     * Construct string containing <code>Command</code> string with
     * <code>query</code> parameters appended.
     * <p/>
     * @return <code>Command</code> string with <code>query</code>
     *         parameters appended.
     */
    String getCommandWithQuery() {
        String commandString = command.getCommand();
        if (query == null) {
            return commandString;
        } else {
            StringBuilder sb = new StringBuilder(commandString.length()
                    + 1 + query.length());
            sb.append(commandString);
            sb.append(QUERY_SEPARATOR);
            sb.append(query);
            return sb.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build arguments for operationStateChanged method listening for state
     * changes.
     * <p/>
     * <code>String</codce> arguments passed to state listener:<ul>
     *   <li><code>args[0]</code> server name</li>
     *   <li><code>args[1]</code> administration command</li>
     *   <li><code>args[2]</code> exception message</li>
     *   <li><code>args[3]</code> display message in GUI</li></ul>
     * <p/>
     * @param exMessage Exception message.
     * @param display   Display this event in GUI (Transformed
     *                  to <code>String</code> value containing
     *                  <code>"true"</code> value when user should be notified
     *                  or <code>"true"</code> or <code>null</code> otherwise.
     * @return Arguments for operationStateChanged method listening for state
     * changes.
     */
    String[] stateChangeArgs(final String exMessage, final boolean display) {
        return new String[] {
            server.getName(), command.getCommand(),
            exMessage, Boolean.toString(display)
        };
    }

    /**
     * Build arguments for operationStateChanged method listening for state
     * changes.
     * <p/>
     * Send <code>false</code> display argument to operationStateChanged method
     * to not display GUI message about this event.
     * <p/>
     * @param exMessage Exception message.
     * @return Arguments for operationStateChanged method listening for state
     * changes.
     */
    String[] stateChangeArgs(final String exMessage) {
        return new String[] {
            server.getName(), command.getCommand(),
            exMessage, Boolean.toString(false)
        };
    }

    /**
     * Update task state value in <code>Result</code> object and notify
     * all registered command execution state listeners about command execution
     * state change.
     * <p/>
     * This method is not responsible for setting receiveResult value returned
     * by GlassFish. This method is used after task is submitted into
     * <code>ExecutorService</code>'s queue and Result object is initialized.
     * <p/>
     * @param newTaskState New task execution state.
     * @param taskEvent    Event related to execution state change.
     * @param args         Additional arguments.
     * @return GlassFish administration command receiveResult with updated task
     *         execution state.
     */
    Result handleStateChange(final TaskState newTaskState,
            final TaskEvent taskEvent, final String... args) {
        result.state = newTaskState;
        if (stateListeners != null) {
            for (int i = 0; i < stateListeners.length; i++) {
                if (stateListeners[i] != null) {
                    stateListeners[i].operationStateChanged(newTaskState,
                            taskEvent, args);
                }
            }
        }
        return result;
    }

    /**
     * Used with external execution code to prepare initial task state before
     * task is passed to executor.
     */
    public void setReadyState() {
        result = createResult();
        handleStateChange(TaskState.READY, TaskEvent.SUBMIT,
                stateChangeArgs(null));
    }

    /**
     * Execute an arbitrary server command.
     * <p/>
     * @return <code>Future</code> object to retrieve receiveResult of
     * asynchronous execution.
     */
    Future<? extends Result> execute() {
        setReadyState();
        return serializedExecutor().submit(this);
    }

    /**
     * Execute an arbitrary server command using provided executor.
     * <p/>
     * @param executor {@link ExecutorService} instance used to run this task.
     * @return <code>Future</code> object to retrieve receiveResult of
     * asynchronous execution.
     */
    Future<? extends Result> execute(final ExecutorService executor) {
        setReadyState();
        return executor.submit(this);
    }

    /**
     * Prepare headers for HTTP connection. This handles all common headers for
     * all implemented command interfaces (REST, HTTP, ...).
     * <p/>
     * @param conn Target HTTP connection.
     * @throws <code>CommandException</code> if there is a problem with setting
     *         the headers.
     */
    protected void prepareHttpConnection(final HttpURLConnection conn)
            throws CommandException {
        final String METHOD = "prepareHttpConnection";
        // Set up standard connection characteristics
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
        String adminUser = server.getAdminUser();
        String adminPassword = server.getAdminPassword();
        LOGGER.log(Level.FINEST, METHOD, "setting", new Object[] {
            HTTP_CONNECTION_TIMEOUT, adminUser,
                    adminPassword});
        try {
            conn.setRequestMethod(getRequestMethod());
        } catch (ProtocolException pe) {
            throw new CommandException(CommandException.RUNNER_HTTP_HEADERS,
                    pe);
        }
        conn.setDoOutput(getDoOutput());
        String contentType = getContentType();
        if (contentType != null && contentType.length() > 0) {
            conn.setRequestProperty("Content-Type", contentType);
            conn.setChunkedStreamingMode(0);
        }
        if (adminPassword != null && adminPassword.length() > 0) {
            String authString = ServerUtils.basicAuthCredentials(
                    adminUser, adminPassword);
            LOGGER.log(Level.FINEST, METHOD, "using");
            conn.setRequestProperty("Authorization", "Basic " + authString);
        }
        if (acceptsGzip()) {
            conn.setRequestProperty("Accept-Encoding", "gzip");
        }
    }

    protected void handleSecureConnection(final HttpsURLConnection conn) {
        final String METHOD = "handleSecureConnection";
        // let's just trust any server that we connect to...
        // we aren't send them money or secrets...
        TrustManager[] tm = new TrustManager[]{
            new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
        };

        SSLContext context;
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, tm, null);
            conn.setSSLSocketFactory(context.getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            // if there is an issue here... there will be another exception
            // later which will take care of the user interaction...
            LOGGER.log(Level.INFO, METHOD, "issue", conn.getURL());
            LOGGER.log(Level.INFO, METHOD, "exception", ex);
        }
    }

    protected boolean handleReceive(final HttpURLConnection hconn)
            throws IOException {
        final String METHOD = "handleReceive";
        boolean receiveResult = false;
        InputStream httpInputStream = hconn.getInputStream();
        try {
            receiveResult = readResponse(httpInputStream, hconn);
        } finally {
            try {
                httpInputStream.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, METHOD, "exception", ioe);
            }
        }
        return receiveResult;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method attributes                               
    // Do not use those attributes outside call() method execution context!   //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server administration interface URL. */
    private URL urlToConnectTo;

    /** GlassFish server administration interface URL connection. */
    private URLConnection conn;

    /** GlassFish server administration interface HTTP URL connection. */
    private HttpURLConnection hconn;

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method helpers                                  //
    // Do not use those methods outside call() method execution context!      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize class instance attributes for <code>call()</code> method.
     */
    private void initCallAttributes() {
        urlToConnectTo = null;
        conn = null;
        hconn = null;
    }

    /**
     * Creates {@link URLConnection} instance that represents a connection
     * to GlassFish server administration interface.
     * <p/>
     * Depending on GlassFisg Tooling Library configuration proxy usage for
     * loopback addresses may be suppressed.
     * <p/>
     * @param urlToConnectTo GlassFish server administration interface URL.
     * @return lassFish server administration interface URL connection.
     * @throws IOException IOException if an I/O error occurs while opening the
     *                     connection.
     */
    private static URLConnection openURLConnection(
            final URL urlToConnectTo) throws IOException {
        if (!GlassFishToolsConfig.getProxyForLoopback()) {
            InetAddress addr;
            try {
                addr = InetAddress.getByName(urlToConnectTo.getHost());
            } catch (UnknownHostException ex) {
                addr = null;
            }
            if (addr != null && addr.isLoopbackAddress()) {
                return urlToConnectTo.openConnection(Proxy.NO_PROXY);
            }
        }
        return urlToConnectTo.openConnection();
    }

    /**
     * Handle HTTP connections to server.
     * <p/>
     * @return State change request when <code>call()</code> method should exit.
     */
    private StateChange handleHTTPConnection() throws IOException {
        final String METHOD = "handleHTTPConnection";
        URL oldUrlToConnectTo;
        do { // deal with possible redirects from 3.1
            oldUrlToConnectTo = urlToConnectTo;
            hconn = (HttpURLConnection)conn;
            if (conn instanceof HttpsURLConnection) {
                handleSecureConnection((HttpsURLConnection)conn);
            }
            prepareHttpConnection(hconn);
            LOGGER.log(Level.FINEST, METHOD, "connect",
                    new Object[] {server.getHost(),
                Integer.toString(server.getAdminPort())});
            // Use external Authenticator if supplied
            Authenticator extAuth = Runner.getAuthenticator();
            if (extAuth != null) {
                Authenticator.setDefault(extAuth);
            }
            // Connect to server.
            hconn.connect();
            // Send data to server if necessary.
            handleSend(hconn);
            int respCode = hconn.getResponseCode();
            StateChange change = handleHTTPResponse(respCode);
            if (change != null) {
                return change;
            }
        } while (urlToConnectTo != oldUrlToConnectTo);
        return null;
    }

    /**
     * Handle HTTP response from server.
     * <p/>
     * @param responseCode HTTP Response code.
     * @return State change request when <code>call()</code> method should exit.
     */
    private StateChange handleHTTPResponse(
            final int responseCode) throws IOException {
        final String METHOD = "handleHTTPResponse";
        LOGGER.log(Level.FINE, METHOD, "response", responseCode);
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            result.auth = auth = false;
            return new StateChange(this, TaskState.FAILED,
                    TaskEvent.AUTH_FAILED_HTTP, stateChangeArgs(null, true));
        } else if (responseCode == HttpURLConnection.HTTP_BAD_GATEWAY) {
            // signals proxy configuration problem
            return new StateChange(this, TaskState.FAILED,
                    TaskEvent.BAD_GATEWAY, stateChangeArgs(null, true));
        } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
            String newUrl = hconn.getHeaderField("Location");
            if (null == newUrl || "".equals(newUrl.trim())) {
                LOGGER.log(Level.SEVERE, METHOD,
                        "invalidRedirect", urlToConnectTo.toString());
            } else {
                LOGGER.log(Level.FINE, METHOD, "locationMoved", newUrl);
                urlToConnectTo = new URL(newUrl);
                conn = openURLConnection(urlToConnectTo);
                hconn.disconnect();
            }
        }
        return null;
    }

    /**
     * Log administration command failure.
     * <p/>
     * @param method Method component of log message key.
     */
    private void logCommandFailure(final String method) {
        LOGGER.log(Level.FINE, method, "failure", new Object[] {
            hconn.toString(), hconn.getContentType(),
            hconn.getContentEncoding()});
        Map<String, List<String>> ms2ls = hconn.getHeaderFields();
        LOGGER.log(Level.FINE, method, "headerFields");
        for (Map.Entry<String, List<String>> e : ms2ls.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (String v : e.getValue()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(v);
            }
            sb.append("]");
            LOGGER.log(Level.FINE, method, "headerField", new String[] {
                e.getKey(), sb.toString()});
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish server administration command execution call.
     * This is an entry point from <code>executor<code>'s serialization queue.
     * <p/>
     * @return Task execution state.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public Result call() {
        final String METHOD = "call";
        boolean httpSucceeded = false;
        boolean commandSucceeded = false;
        String commandUrl;
        initCallAttributes();

        handleStateChange(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                stateChangeArgs(null));
        try {
            commandUrl = constructCommandUrl();
        } catch (CommandException ce) {
            return handleStateChange(TaskState.FAILED, TaskEvent.CMD_EXCEPTION,
                    stateChangeArgs(ce.getLocalizedMessage()));
        }
        // disable ("version".equals(cmd) || "__locations".equals(cmd)) ? 1 : 3;
        int retries = 1;
        LOGGER.log(Level.FINEST, METHOD, "thread", new Object[] {
            commandUrl, Thread.currentThread().getName()});
        try {
            urlToConnectTo = new URL(commandUrl);
            while (!httpSucceeded && retries-- > 0) {
                try {
                    conn = openURLConnection(urlToConnectTo);
                    if (conn instanceof HttpURLConnection) {
                        StateChange change = handleHTTPConnection();
                        if (change != null) {
                            return change.handleStateChange();
                        }
                        // Process the response message.
                        // In Glassfish v4 HTTP interface authentication failure
                        // can only be found in readResponse() method where
                        // response message contains
                        // "javax.security.auth.login.LoginException".
                        boolean receiveResult = handleReceive(hconn);
                        boolean processResult = processResponse();
                        commandSucceeded = receiveResult && processResult;
                        if (!auth) {
                            return handleStateChange(TaskState.FAILED,
                                    TaskEvent.AUTH_FAILED,
                                    stateChangeArgs(null, true));
                        } else if (!commandSucceeded
                                && !isSilentFailureAllowed()
                                && LOGGER.isLoggable(Level.FINE)) {
                            logCommandFailure(METHOD);
                        }
                        httpSucceeded = true;
                    } else {
                        LOGGER.log(Level.INFO, METHOD,
                                "unexpectedConnection", urlToConnectTo);
                    }
                } catch (ProtocolException ex) {
                    handleStateChange(TaskState.FAILED, TaskEvent.EXCEPTION,
                            stateChangeArgs(ex.getLocalizedMessage(), true));
                    retries = 0;
                } catch (ConnectException ce) {
                    return handleStateChange(TaskState.FAILED,
                            TaskEvent.EXCEPTION,
                            stateChangeArgs(ce.getLocalizedMessage()));
                } catch (IOException ex) {
                    if (retries <= 0) {
                        return handleStateChange(TaskState.FAILED,
                                TaskEvent.EXCEPTION, stateChangeArgs(
                                ex.getLocalizedMessage()));
                    }
                } catch (RuntimeException ex) {
                    return handleStateChange(TaskState.FAILED,
                            TaskEvent.EXCEPTION, stateChangeArgs(
                                    ex.getLocalizedMessage()));
                } finally {
                    if (null != hconn) {
                        hconn.disconnect();
                    }
                }

                if (!httpSucceeded && retries > 0) {
                    try {
                        Thread.sleep(HTTP_RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        LOGGER.log(Level.INFO, METHOD, "sleepInterrupted", ie);
                    }
                }
            } // while
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, METHOD, "malformedURLException", ex);
        }

        if (commandSucceeded) {
            return handleStateChange(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED, stateChangeArgs(null));
        } else {
            return handleStateChange(TaskState.FAILED, TaskEvent.CMD_FAILED,
                    stateChangeArgs(null));
        }
    }

}
