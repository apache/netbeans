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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import static org.netbeans.modules.payara.tooling.admin.Runner.PARAM_SEPARATOR;
import org.netbeans.modules.payara.tooling.admin.response.ResponseContentType;
import org.netbeans.modules.payara.tooling.admin.response.ResponseParserFactory;
import org.netbeans.modules.payara.tooling.admin.response.RestActionReport;
import org.netbeans.modules.payara.tooling.admin.response.RestResponseParser;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara server administration command execution using REST interface.
 * <p/>
 * Class implements Payara server administration functionality trough REST
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRest extends Runner {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Parser for returned response. Default response is XML. */
    RestResponseParser parser = ResponseParserFactory.getRestParser(getResponseType());

    /** Object representation of returned response.*/
    RestActionReport report;

    /**
     * Payara administration command result.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and receiveResult value
     * handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    protected ResultString result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerRest(final PayaraServer server, final Command command) {
        this(server, command, null);
    }


    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param query   Query string for this command.
     */
    public RunnerRest(final PayaraServer server, final Command command,
            final String query) {
        this(server, command, "/command/", query);
    }
    
    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param path    path which builds URL we speak to.
     * @param query   Query string for this command.
     */
    public RunnerRest(final PayaraServer server, final Command command,
            final String path, final String query) {
        super(server, command, path, query);
        silentFailureAllowed = false;
    }

    /**
     * Helper methods that appends java.util.Properties into
     * POST request body.
     * @param sb
     * @param properties
     * @param paramName
     * @param separator 
     */
    protected void appendProperties(StringBuilder sb,
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
    }
    
    protected void appendIfNotEmpty(StringBuilder sb, String paramName, String paramValue) {
        if ((paramValue != null) && (!paramValue.isEmpty())) {
            sb.append(paramName);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(paramValue);
        }
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
        return true;
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
     * Build Payara REST command URL.
     * <p/>
     * @return <code>String</code> value containing command URL.
     * @throws <code>CommandException</code> if there is a problem with building
     *         command URL.
     */
    @Override
    protected String constructCommandUrl() throws CommandException {
        String protocol = "http";
        URI uri;
        try {
            uri = new URI(protocol, null, server.getHost(),
                    server.getAdminPort(), path + command.getCommand(),
                    query, null);
        } catch (URISyntaxException use) {
            throw new CommandException(CommandException.RUNNER_HTTP_URL, use);
        }
        return uri.toASCIIString();
    }

    /**
     * Override to change the type of HTTP method used for this command.
     * Default is GET.
     * <p/>
     * @return HTTP method (GET, POST, etc.)
     */
    @Override
    protected String getRequestMethod() {
        return "POST"; // NOI18N
    }
    
   /*
     * Handle sending data to server using REST command interface.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        // Do nothing.
    }

    /**
     * Override this method to read response from provided input stream. <p/>
     * Override to read the response data sent by the server. Do not close the
     * stream parameter when finished. Caller will take care of that. <p/>
     * <p/>
     * @param in Stream to read data from.
     * @return <code>true</code> if response was read correctly.
     * @throws java.io.IOException in case of stream error.
     */
    @Override
    protected boolean readResponse(InputStream in, HttpURLConnection hconn) {
        report = parser.parse(in);
        return isSuccess();
    }

    /**
     * Override to parse, validate, and/or format any data read from the server
     * in readResponse() / readManifest().
     * <p/>
     * @return <code>true</code> if data was processed correctly.
     */
    @Override
    protected boolean processResponse() {
        result.value = report.getMessage();
        return isSuccess();
    }

    /**
     * Added to give descendants possibility to decide what report state has
     * successful meaning.
     * <p/>
     * Default is to delegate decision to report itself.
     * <p/>
     * @return Does report state have successful meaning?
     */
    protected boolean isSuccess() {
        return report.isSuccess();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Prepare headers for HTTP connection.
     * <p/>
     * @param conn Target HTTP connection.
     * @throws <code>CommandException</code> if there is a problem with setting
     *         the headers.
     */
    @Override
    protected void prepareHttpConnection(HttpURLConnection conn)
            throws CommandException {
        super.prepareHttpConnection(conn);
        // from gf4 every command has to be authenticated
        String adminUser = server.getAdminUser();
        String adminPassword = server.getAdminPassword();
        if (adminUser != null && adminUser.length() > 0) {
            adminPassword = (adminPassword != null
                    && adminPassword.length() > 0) ? adminPassword : "";
            String authCredentials = ServerUtils.basicAuthCredentials(
                    adminUser, adminPassword);
            conn.setRequestProperty(
                    "Authorization", "Basic " + authCredentials);
        }
        conn.setRequestProperty("Accept", getResponseType().toString());
        conn.addRequestProperty("X-Requested-By",
                "Payara REST HTML interface");
    }

    /**
     * Method returns content type in which the server is asked to return
     * the response.
     * <p/>
     * @return <code>ContentType</code> that runner wants to get from server.
     */
    protected ResponseContentType getResponseType() {
        return ResponseContentType.APPLICATION_JSON;
    }

}
