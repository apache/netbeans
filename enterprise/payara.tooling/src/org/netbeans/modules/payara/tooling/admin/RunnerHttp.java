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
package org.netbeans.modules.payara.tooling.admin;

import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara server administration command execution using HTTP interface.
 * <p/>
 * Class implements Payara server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttp extends Runner {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Character used to separate individual items in libraries query
     *  parameter. */
    static final char LIBRARY_SEPARATOR = ',';

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Calculate properties length based on properties map. 
     * <p/>
     * @param properties Properties to be added into query string.
     * @param paramName  Properties parameter name in query string.
     * @return Size of properties query string to be added.
     */
    static int queryPropertiesLength(final Map<String,String> properties,
            final String paramName) {
        int propertyLength = 0;
        if (null != properties && properties.size() > 0) {
            Set<Map.Entry<String,String>> entrySet = properties.entrySet();
            boolean first = true;
            propertyLength += paramName.length() + 2;
            for (Map.Entry<String,String> entry : entrySet) {
                String key = entry.getKey();
                String val = entry.getValue();
                if (key == null)
                    throw new CommandException(
                            CommandException.ILLEGAL_NULL_VALUE);
                if (first) {
                    first = false;
                }
                else {
                    propertyLength++;
                }
                propertyLength += key.length() + 1 +
                        (val != null ? val.length() : 0);
            }
        }
        return propertyLength;
    }

    /**
     * Calculate libraries length based on libraries array. 
     * <p/>
     * @param libraries Libraries to be added into query string.
     * @param paramName Libraries parameter name in query string.
     * @return Size of libraries query string to be added.
     */
    static int queryLibrariesLength(final File[] libraries,
            final String paramName) {
        int propertyLength = 0;
        if (null != libraries && libraries.length > 0) {
            boolean first = true;
            propertyLength += paramName.length() + 2;
            for (File library : libraries) {
                if (first) {
                    first = false;
                }
                else {
                    propertyLength++;
                }
                propertyLength += library.getPath().length();
            }
        }
        return propertyLength;
    }

    /**
     * Append properties to existing query string stored in given
     * <code>StringBuilder</code>.
     * <p/>
     * @param sb         Target <code>StringBuilder</code> where to append
     *                   properties.
     * @param properties Properties to be added into query string.
     * @param paramName  Properties parameter name in query string.
     * @param separator  Prepend parameter separator if <code>true</code>
     * @return Target <code>StringBuilder</code> with properties appended.
     */
    static StringBuilder queryPropertiesAppend(StringBuilder sb,
            final Map<String,String> properties, final String paramName,
            final boolean separator) {
        if (null != properties && properties.size() > 0) {
            Set<Map.Entry<String,String>> entrySet = properties.entrySet();
            boolean first = true;
            if (separator) {
                sb.append(PARAM_SEPARATOR);
            }
            sb.append(paramName).append(PARAM_ASSIGN_VALUE);
            for (Map.Entry<String,String> entry : entrySet) {
                String key = entry.getKey();
                String val = entry.getValue();
                if (first) {
                    first = false;
                }
                else {
                    sb.append(ITEM_SEPARATOR);
                }
                sb.append(key);
                sb.append(PARAM_ASSIGN_VALUE);
                if (val != null) {
                    sb.append(val);
                }
            }
        }
        return sb;
    }

    /**
     * Append libraries to existing query string stored in given
     * <code>StringBuilder</code>.
     * <p/>
     * @param sb        Target <code>StringBuilder</code> where to append
     *                  properties.
     * @param libraries Libraries to be added into query string.
     * @param paramName Libraries parameter name in query string.
     * @param separator  Prepend parameter separator if <code>true</code>
     * @return Target <code>StringBuilder</code> with libraries appended.
     */
    static StringBuilder queryLibrariesAppend(StringBuilder sb,
            final File[] libraries, final String paramName,
            final boolean separator) {
        if (null != libraries && libraries.length > 0) {
            boolean first = true;
            if (separator) {
                sb.append(PARAM_SEPARATOR);
            }
            sb.append(paramName).append(PARAM_ASSIGN_VALUE);
            for (File library : libraries) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append(LIBRARY_SEPARATOR);
                }
                sb.append(library.getPath());
            }
        }
        return sb;
    }

    /**
     * Check <code>Manifest</code> <i>exit-code<i> attribute for result.
     * Missing value or any value differed to< code>"FAILED"</code>
     * is considered as success.
     * <p/>
     * Response processing helper method. Manifest must contain valid data.
     * <p/>
     * @param manifest Manifest from server response.
     * @return <code>true</code> for success and <code>false</code> for failure.
     */
    private static boolean successExitCode(Manifest manifest) {
        String exitCode = manifest.getMainAttributes().getValue("exit-code");
        return exitCode == null
                || !"FAILURE".equals(exitCode.toUpperCase(Locale.ENGLISH));
    }

    private static String getHttpListenerProtocol(String hostname, int port,
            String url) {
        String retVal = "http";  // NOI18N
        try {
            if (NetUtils.isSecurePort(hostname, port)) {
                retVal = "https"; // NOI18N
            }
        } catch (ConnectException ex) {
            Logger.log(Level.INFO, hostname + ":" + port + "::" + url, ex);
        } catch (SocketException ex) {
            Logger.log(Level.FINE, hostname + ":" + port + "::" + url, ex);
        } catch (SocketTimeoutException ex) {
            Logger.log(Level.INFO, hostname + ":" + port + "::" + url, ex);
        } catch (IOException ex) {
            Logger.log(Level.INFO, hostname + ":" + port + "::" + url, ex);
        }
        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Manifest from server response.
     * <p/>
     * <code>Manifest</code> instance is internal server response holder.
     * <code>Manifest</code> instance life cycle is started
     * in <code>readResponse</code> method where manifest returned from server
     * is read and stored internally.
     */
    Manifest manifest;

    /**
     * Payara administration command result.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and receiveResult value
     * handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultString result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttp(final PayaraServer server, final Command command) {
        this(server, command, (String)null);
    }

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * This constructor allows child classes to set their own path. 
     * <p/>
     * <b>For use in child classes only.</b>
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param query   Query string for this command.
     */
    RunnerHttp(final PayaraServer server, final Command command,
            final String query) {
        this(server, command, "/__asadmin/", query);
    }

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * This constructor allows child classes to set their own path. 
     * <p/>
     * <b>For use in child classes only.</b>
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param path    Server command path.
     * @param query   Query string for this command.
     */
    protected RunnerHttp(final PayaraServer server, final Command command,
            final String path, final String query) {
        super(server, command, path, query);
        silentFailureAllowed = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

   /**
     * Create <code>ResultString</code> object corresponding
     * to <code>String</code>command execution value to be returned.
     * <p/>
     * @return <code>String</code>command execution value to be returned.
     */
    @Override
    protected Result createResult() {
        return result = new ResultString();
    }

    /**
     * Do not send information to the server via HTTP POST by default.
     * <p/>
     * @return <code>true</code> if using HTTP POST to send to server
     *         or <code>false</code> otherwise
     */
    @Override
    public boolean getDoOutput() {
        return false;
    }

    /**
     * Inform whether this runner implementation accepts gzip format.
     * <p/>
     * @return <code>true</code> when gzip format is accepted,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean acceptsGzip() {
        return false;
    }

    /**
     * Build Payara HTTP command URL.
     * <o>
     * @return <code>String</code> value containing command URL.
     * @throws <code>CommandException</code> if there is a problem with building
     *         command URL.
     */
    @Override
    protected String constructCommandUrl() throws CommandException {
        String host = server.getHost();
        boolean useAdminPort = !"false"
                .equals(System.getProperty("payara.useadminport"));
        int port = useAdminPort ? server.getAdminPort() : server.getPort();
        String protocol = "http";
        String url = server.getUrl();
        String domainsDir = server.getDomainsFolder();
        if (null == url) {
            protocol = getHttpListenerProtocol(host, port, ":::"
                    + command.getCommand() + "?" + query);
        } else if (!(url.contains("ee6wc"))) {
            protocol = getHttpListenerProtocol(host, port, url + ":::"
                    + command.getCommand() + "?" + query);
        } else if (url.contains("ee6wc") && (null == domainsDir
                || "".equals(domainsDir))) {
            protocol = "https";
        }
        URI uri;
        try {
            uri = new URI(protocol, null, host, port,
                    path + command.getCommand(), query, null);
        } catch (URISyntaxException use) {
            throw new CommandException(CommandException.RUNNER_HTTP_URL, use);
        }
        // These characters don't get handled by GF correctly. Best I can tell.
        return uri.toASCIIString().replace("+", "%2b"); 
    }

    /**
     * Override to change the type of HTTP method used for this command.
     * Default is GET.
     * 
     * @return HTTP method (GET, POST, etc.)
     */
    @Override
    protected String getRequestMethod() {
        return "GET";
    }
    
    /**
     * Handle sending data to server using HTTP command interface.
     * <p/>
     * Does nothing by default.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
    }

    /**
     * Reads response from server and stores it into internal
     * <code>Manifest</code> object. Value of <i>exit-code<i> attribute
     * is verified to detect if command completed successfully. If not,
     * <i>message</i> value is checked for "please wait" <code>String</code>
     * to eventually set <code>retry</code> value to <code>true</code>.
     * <p/>
     * Override to read the response data sent by the server.  Do not close
     * the stream parameter when finished.  Caller will take care of that.
     * <p/>
     * @param in Stream to read data from.
     * @return true if response was read correctly.
     * @throws CommandException in case of stream error.
     */
    @Override
    protected boolean readResponse(InputStream in, HttpURLConnection hconn) {
        boolean readResult;
        manifest = new Manifest();
        try {
            Logger.log(Level.FINEST, "Reading response from {0}:{1}",
                    new Object[] {server.getHost(),
                        Integer.toString(server.getAdminPort())});
            manifest.read(in);
        } catch (IOException ioe) {
            throw new CommandException(CommandException.HTTP_RESP_IO_EXCEPTION,
                    ioe);
        }
        if (successExitCode(manifest)) {
            readResult = true;
        }
        else {
            readResult = false;
            String message = getMessage(manifest);
            if (message != null) {
                if (message.contains("please wait")) {
                    retry = true;
                } else if (message.contains(
                        "javax.security.auth.login.LoginException")) {
                    auth = false;
                }
            }            
        }
        return readResult;
    }

    /**
     * Extracts result value from internal <code>Manifest</code> object.
     * Value of <i>message</i> attribute in <code>Manifest</code> object is
     * stored as <i>value</i> into <code>ResultString</code> result object.
     * <p/>
     * @return true if result was extracted correctly. <code>null</code>
     *         <i>message</i>value is considered as failure.
     */
    @Override
    protected boolean processResponse() {
        result.value = getMessage(manifest);
        result.auth = auth;
        Logger.log(Level.FINEST, "Manifest message: {0}",
                new Object[] {result.value});
        return result.value != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Override to provide the lastModified date for data source whose
     * inputstream is returned by getInputStream.  Must not return null if
     * getInputStream() does not return null;
     *
     * @return String format of long integer from lastModified date of source.
     */
    public String getLastModified() {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve <i>message<i> from <code>Manifest</code> object.
     * <p/>
     * Response processing helper method. Manifest must contain valid data.
     * <code>Manifest</code> instance is always created in
     * <code>readResponse</code> method so no <code>null</code> check is
     * required here.
     * <p/>
     * @param manifest Manifest from server response.
     * @return Value of <i>message<i> attribute or <code>null</code> if
     *         no such attribute exists.
     */
    private String getMessage(Manifest manifest) {
        String message = null;
            String tmp = manifest.getMainAttributes().getValue("message");
        if (null != tmp) {
            try {
                message = URLDecoder.decode(
                        ServerUtils.manifestDecode(tmp), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                Logger.log(Level.WARNING, "Could not URL decode with UTF-8", uee);
            } catch (IllegalArgumentException iae) {
                Logger.log(Level.FINE, "Caught IllegalArgumentException", iae);
                message = tmp;
            }
        }
        return message;
    }

    /**
     * Get extra properties for ZIP entries.
     * <p/>
     * @return Extra properties for ZIP entries.
     */
    byte[] getExtraProperties() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Properties props = new Properties();
        props.setProperty("data-request-type", "file-xfer");
        props.setProperty("last-modified", getLastModified());
        props.put("data-request-name", "DEFAULT");
        props.put("data-request-is-recursive", "true");
        props.put("Content-Type", "application/octet-stream");
        props.list(new java.io.PrintStream(baos));
        return baos.toByteArray();
    }

    /**
     * Prepare headers for HTTP connection.
     * <p/>
     * @param conn Target HTTP connection.
     * @throws <code>CommandException</code> if there is a problem with setting
     *         the headers.
     */
    @Override
    protected void prepareHttpConnection(HttpURLConnection conn) throws CommandException {
        super.prepareHttpConnection(conn);
        String contentType = getContentType();
        if (contentType == null || contentType.length() <= 0) {
            // work around that helps prevent tickling the
            // GF issue that is the root cause of 195384.
            //
            // GF doesn't expect to get image content, so it doesn't
            // try to handle the content... which prevents the
            // exception, according to Tim Quinn.
            conn.setRequestProperty("Content-Type", "image/png");
        }
        conn.setRequestProperty("User-Agent", "hk2-agent");
    }

}
