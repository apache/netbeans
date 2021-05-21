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
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Command runner for deploying directory or file.
 * <p>
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestDeploy extends RunnerRest {

    private static final String NEWLINE = "\r\n";

    private String multipartBoundary = Long.toHexString(System.
            currentTimeMillis());

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandDeploy command;

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerRestDeploy(final PayaraServer server,
            final Command command) {
        super(server, command);
        this.command = (CommandDeploy)command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void prepareHttpConnection(HttpURLConnection conn) throws CommandException {
        super.prepareHttpConnection(conn);
        if (!command.dirDeploy) {
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + multipartBoundary);
        }
    }

    /**
     * Handle sending data to server using HTTP command interface.
     * <p/>
     * This is based on reading the code of
     * <code>CLIRemoteCommand.java</code>
     * from the server's code repository. Since some asadmin commands
     * need to send multiple files, the server assumes the input is a ZIP
     * stream.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        //InputStream istream = getInputStream();
        if (command.path == null) {
            throw new PayaraIdeException("The path attribute of deploy command"
                    + " has to be non-empty!");
        }
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        if (!command.dirDeploy) {
            writeParam(wr, "path", command.path.getAbsolutePath());
            if (command.name != null) {
                writeParam(wr, "name", command.name);
            }
            if (command.contextRoot != null) {
                writeParam(wr, "contextroot", command.contextRoot);
            }
            if (command.target != null) {
                writeParam(wr, "target", command.target);
            }

            writeBinaryFile(wr, hconn.getOutputStream(), command.path);
            wr.append("--" + multipartBoundary + "--").append(NEWLINE);
        } else {
            wr.write("path=" + command.path.toString());
            if (command.name != null) {
                wr.write("&");
                wr.write("name=" + command.name);
            }
            if (command.contextRoot != null) {
                wr.write("&");
                wr.write("contextroot=" + command.name);
            }
            if (command.target != null) {
                wr.write("&");
                wr.write("target=" + command.target);
            }
        }

        wr.close();
    }

    private void writeParam(OutputStreamWriter writer, String paramName,
            String paramValue) throws IOException {
        writer.append("--" + multipartBoundary).append(NEWLINE);
        writer.append("Content-Disposition: form-data; name=\"").append(
                paramName).append("\"").append(NEWLINE);
        writer.append("Content-Type: text/plain;").append(NEWLINE);
        writer.append(NEWLINE);
        writer.append(paramValue).append(NEWLINE).flush();
    }

    private void writeBinaryFile(OutputStreamWriter writer, OutputStream output, File file) throws IOException {
        writer.append("--" + multipartBoundary).append(NEWLINE);
//        writer.append("Content-Disposition: form-data; name=\"warFile\"; filename=\""
//                + file.getAbsolutePath() + "\"").append(NEWLINE);
        writer.append("Content-Type: application/octet-stream").append(NEWLINE);
        writer.append("Content-Transfer-Encoding: binary").append(NEWLINE);
        writer.append(NEWLINE).flush();
        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            byte[] buffer = new byte[1024*1024];
            for (int length ; (length = input.read(buffer)) > 0 ;) {
                output.write(buffer, 0, length);
            }
            output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }
        writer.append(NEWLINE).flush();
    }
    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set the content-type of information sent to the server.
     * Returns
     * <code>application/zip</code> for file deployment
     * and
     * <code>null</code> (not set) for directory deployment.
     *
     * @return content-type of data sent to server via HTTP POST
     */
    @Override
    public String getContentType() {
        return command.dirDeploy ? null : "application/zip";
    }

//    /**
//     * Provide the lastModified date for data source whose
//     * <code>InputStream</code> is returned by getInputStream.
//     * <p/>
//     * @return String format of long integer from lastModified date of source.
//     */
//    @Override
//    public String getLastModified() {
//        return Long.toString(command.path.lastModified());
//    }
    /**
     * Get
     * <code>InputStream</code> object for deployed file.
     * <p/>
     * @return <code>InputStream</code> object for deployed file
     *         or <code>null</code> for directory deployment.
     */
    public InputStream getInputStream() {
        if (command.dirDeploy) {
            return null;
        } else {
            try {
                return new FileInputStream(command.path);
            } catch (FileNotFoundException fnfe) {
                Logger.log(Level.INFO, command.path.getPath(), fnfe);
                return null;
            }
        }
    }

}
