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
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.Utils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Server <code>deploy</code> Administration Command Execution
 * using HTTP interface.
 * <p/>
 * Class implements Payara server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpDeploy extends RunnerHttp {


    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerHttpDeploy.class);

    /** Deploy command <code>DEFAULT</code> parameter name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /** Deploy command <code>target</code> parameter name. */
    private static final String TARGET_PARAM = "target";

    /** Deploy command <code>name</code> parameter name. */
    private static final String NAME_PARAM = "name";

    /** Deploy command <code>contextroot</code> parameter name. */
    private static final String CTXROOT_PARAM = "contextroot";

    /** Deploy command <code>force</code> parameter name. */
    private static final String FORCE_PARAM = "force";

    /** Deploy command <code>properties</code> parameter name. */
    private static final String PROPERTIES_PARAM = "properties";

    /** Deploy command <code>libraries</code> parameter name. */
    private static final String LIBRARIES_PARAM = "libraries";

    /** Deploy command <code>force</code> parameter value. */
    private static final boolean FORCE_VALUE = true;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds deploy query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;path&gt; <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; '&' "force" '=' true | false <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "name" '=' &lt;name&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "target" '=' &lt;target&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "contextroot" '=' &lt;contextRoot&gt; ] <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     *                                                  { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "libraries" '=' &lt;lname&gt; '=' &lt;lvalue&gt;
     *                                                  { ':' &lt;lname&gt; '=' &lt;lvalue&gt;} ]</code>
     * <p/>
     * @param command Payara server administration deploy command entity.
     * @return Deploy query string for given command.
     */
    private static String query(final Command command) {
        // Prepare values
        String name; 
        String path;
        String target;
        String ctxRoot;
        String force = Boolean.toString(FORCE_VALUE);
        if (command instanceof CommandDeploy) {
            if (((CommandDeploy)command).path == null) {
                throw new CommandException(CommandException.ILLEGAL_NULL_VALUE);
            }
            name = Utils.sanitizeName(((CommandDeploy)command).name);
            path = ((CommandDeploy)command).path.getAbsolutePath();
            target =((CommandDeploy)command).target;
            ctxRoot = ((CommandDeploy)command).contextRoot;
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                DEFAULT_PARAM.length() + 1 + path.length() +
                1 + FORCE_PARAM.length() + 1 + force.length()
                + queryPropertiesLength(
                        ((CommandDeploy)command).properties, PROPERTIES_PARAM)
                + queryLibrariesLength(
                        ((CommandDeploy)command).libraries, LIBRARIES_PARAM)
                + ( name != null && name.length() > 0
                        ? 1 + NAME_PARAM.length() + 1 + name.length()
                        : 0
                ) + ( target != null
                        ? 1 + TARGET_PARAM.length() + 1 + target.length()
                        : 0
                ) + ( ctxRoot != null && ctxRoot.length() > 0
                        ? 1 + CTXROOT_PARAM.length() + 1 + ctxRoot.length()
                        : 0
                ));
        // Build query string
        sb.append(DEFAULT_PARAM).append(PARAM_ASSIGN_VALUE).append(path);
        sb.append(PARAM_SEPARATOR);
        sb.append(FORCE_PARAM).append(PARAM_ASSIGN_VALUE).append(force);
        if (name != null && name.length() > 0) {
            sb.append(PARAM_SEPARATOR);
            sb.append(NAME_PARAM).append(PARAM_ASSIGN_VALUE).append(name);
        }
        if (target != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(TARGET_PARAM).append(PARAM_ASSIGN_VALUE).append(target);            
        }
        if (ctxRoot != null && ctxRoot.length() > 0) {
            sb.append(PARAM_SEPARATOR);
            sb.append(CTXROOT_PARAM).append(PARAM_ASSIGN_VALUE).append(ctxRoot);
        }
        // Add properties into query string.
        queryPropertiesAppend(sb, ((CommandDeploy)command).properties,
                PROPERTIES_PARAM, true);
        queryLibrariesAppend(sb, ((CommandDeploy)command).libraries,
                LIBRARIES_PARAM, true);
        
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandDeploy command;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttpDeploy(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
        this.command = (CommandDeploy)command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Send deployed file to the server via HTTP POST when it's not
     * a directory deployment.
     * <p/>
     * @return <code>true</code> if using HTTP POST to send to server
     *         or <code>false</code> otherwise
     */
    @Override
    public boolean getDoOutput() {
        return !command.dirDeploy;
    }

    /**
     * HTTP request method used for this command is <code>POST</code> for
     * file deployment and <code>GET</code> for directory deployment.
     * 
     * @return HTTP request method used for this command.
     */
    @Override
    public String getRequestMethod() {
        return command.dirDeploy ? super.getRequestMethod() : "POST";
    }

    /**
     * Handle sending data to server using HTTP command interface.
     * <p/>
     * This is based on reading the code of <code>CLIRemoteCommand.java</code>
     * from the server's code repository. Since some asadmin commands
     * need to send multiple files, the server assumes the input is a ZIP
     * stream.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        final String METHOD = "handleSend";
        InputStream istream = getInputStream();
        if(istream != null) {
            ZipOutputStream ostream = null;
            try {
                ostream = new ZipOutputStream(new BufferedOutputStream(
                        hconn.getOutputStream(), 1024*1024));
                ZipEntry e = new ZipEntry(command.path.getName());
                e.setExtra(getExtraProperties());
                ostream.putNextEntry(e);
                byte buffer[] = new byte[1024*1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.closeEntry();
                ostream.flush();
            } finally {
                try {
                    istream.close();
                } catch(IOException ex) {
                    LOGGER.log(Level.INFO, METHOD, "ioException", ex);
                }
                if(ostream != null) {
                    try {
                        ostream.close();
                    } catch(IOException ex) {
                        LOGGER.log(Level.INFO, METHOD, "ioException", ex);
                    }
                }
            }
        } else if("POST".equalsIgnoreCase(getRequestMethod())) {
            LOGGER.log(Level.INFO, METHOD, "noData");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set the content-type of information sent to the server.
     * Returns <code>application/zip</code> for file deployment
     * and <code>null</code> (not set) for directory deployment.
     *
     * @return content-type of data sent to server via HTTP POST
     */
    @Override
    public String getContentType() {
        return command.dirDeploy ? null : "application/zip";
    }

    /**
     * Provide the lastModified date for data source whose
     * <code>InputStream</code> is returned by getInputStream.
     * <p/>
     * @return String format of long integer from lastModified date of source.
     */
    @Override
    public String getLastModified() {
        return Long.toString(command.path.lastModified());
    }

    /**
     * Get <code>InputStream</code> object for deployed file.
     * <p/>
     * @return <code>InputStream</code> object for deployed file
     *         or <code>null</code> for directory deployment.
     */
    public InputStream getInputStream() {
        final String METHOD = "getInputStream";
        if (command.dirDeploy) {
            return null;
        } else {
            try {
                return new FileInputStream(command.path);
            } catch (FileNotFoundException fnfe) {
                LOGGER.log(Level.INFO, METHOD, "fileNotFound", fnfe);
                return null;
            }
        }
    }

}
