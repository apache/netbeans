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
package org.netbeans.modules.payara.tooling.admin.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.admin.Command;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara cloud administration command execution using REST interface.
 * <p/>
 * Class implements Payara cloud administration functionality trough REST
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestCloudDeploy extends RunnerRestCloud {

    private static final String NEWLINE = "\r\n";

    private String multipartBoundary = Long.toHexString(System.
            currentTimeMillis());

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandCloudDeploy command;

    /**
     * Constructs an instance of administration command executor using
     * REST interface for deploy command.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerRestCloudDeploy(final PayaraServer server,
            final Command command) {
        super(server, command);
        this.command = (CommandCloudDeploy)command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

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
        writeParam(wr, "file", command.path.getAbsolutePath());
        if (command.account != null) {
            writeParam(wr, "account", command.account);
        }
        writeBinaryFile(wr, hconn.getOutputStream(), command.path);
        wr.append("--" + multipartBoundary + "--").append(NEWLINE);
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

    private void writeBinaryFile(OutputStreamWriter writer, OutputStream output,
            File file) throws IOException {
        writer.append("--" + multipartBoundary).append(NEWLINE);
        writer.
                append("Content-Disposition: form-data; name=\"warFile\"; filename=\""
                + file.getAbsolutePath() + "\"").append(NEWLINE);
        writer.append("Content-Type: application/octet-stream").append(NEWLINE);
        writer.append("Content-Transfer-Encoding: binary").append(NEWLINE);
        writer.append(NEWLINE).flush();

        InputStream input = null;
        try {
            input = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024];
            for (int length = 0 ; (length = input.read(buffer)) > 0 ;) {
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
}
