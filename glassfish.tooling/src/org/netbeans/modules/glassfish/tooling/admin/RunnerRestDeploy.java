/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

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
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestDeploy(final GlassFishServer server,
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
            throw new GlassFishIdeException("The path attribute of deploy command"
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
