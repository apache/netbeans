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
package org.netbeans.modules.nativeexecution.sps.impl;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner.BufferedLineProcessor;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public final class SPSRemoteImpl extends SPSCommonImpl {

    private final ExecutionEnvironment execEnv;
    private String pid = null;

    private SPSRemoteImpl(ExecutionEnvironment execEnv) {
        super(execEnv);
        this.execEnv = execEnv;
    }

    public static SPSCommonImpl getNewInstance(ExecutionEnvironment execEnv) {
        return new SPSRemoteImpl(execEnv);
    }

    @Override
    synchronized String getPID() {
        if (pid != null) {
            return pid;
        }

        BufferedLineProcessor blp = new BufferedLineProcessor();
        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, "/bin/ptree $$", blp); // NOI18N

        try {
            if (scriptRunner.execute() != 0) {
                throw new IOException("Unable to get sshd pid"); // NOI18N
            }
        } catch (IOException ex) {
            Logger.getInstance().fine(ex.toString());
        } catch (CancellationException ex) {
            Logger.getInstance().fine(ex.toString()); // TODO:CancellationException error processing
        }

        String pidCandidate = null;

        for (String line : blp.getBuffer()) {
            line = line.trim();
            if (line.endsWith("sshd")) { // NOI18N
                try {
                    pidCandidate = line.substring(0, line.indexOf(' '));
                } catch (NumberFormatException ex) {
                }
            }
        }

        pid = pidCandidate;
        return pid;
    }

    @Override
    public synchronized void invalidate() {
        super.invalidate();
        pid = null;
    }

    @Override
    public synchronized boolean requestPrivileges(Collection<String> requestedPrivileges, String user, char[] passwd) throws NotOwnerException, InterruptedException {
        // Construct privileges list
        StringBuilder sb = new StringBuilder();

        for (String priv : requestedPrivileges) {
            sb.append(priv).append(","); // NOI18N
        }

        String requestedPrivs = sb.toString();

        OutputStream out = null;
        InputStream in = null;

        String script = "/usr/bin/ppriv -s I+" + // NOI18N
                requestedPrivs + " " + getPID(); // NOI18N

        StringBuilder cmd = new StringBuilder("/sbin/su - "); // NOI18N
        cmd.append(user).append(" -c \""); // NOI18N
        cmd.append(script).append("\"; echo ExitStatus:$?\n"); // NOI18N

        ChannelShell channel = null;
        PrintWriter w = null;
        int status = 1;

        try {
            channel = (ChannelShell) ConnectionManagerAccessor.getDefault().openAndAcquireChannel(execEnv, "shell", true); // NOI18N+
            if (channel == null) {
                return false;
            }

            channel.setPty(true);
            channel.setPtyType("ldterm"); // NOI18N

            out = channel.getOutputStream();
            in = channel.getInputStream();

            channel.connect();

            w = new PrintWriter(out);
            w.write(cmd.toString());
            w.flush();

            expect(in, "Password:"); // NOI18N

            w.println(passwd);
            w.flush();

            String exitStatus = expect(in, "ExitStatus:%"); // NOI18N
            status = Integer.parseInt(exitStatus);

            return status == 0;
        } catch (InterruptedIOException ex) {
            // TODO:CancellationException error processing
            // was: throw new CancellationException();
            throw new InterruptedException(ex.getMessage());
        } catch (IOException ex) {
            Logger.getInstance().log(Level.FINE, "", ex); // NOI18N
        } catch (JSchException ex) {
            Logger.getInstance().log(Level.FINE, "", ex); // NOI18N
            if (MiscUtils.isJSCHTooLongException(ex)) {
                MiscUtils.showJSCHTooLongNotification(execEnv.getDisplayName());
            }
        } finally {
            if (status != 0) {
                if (!Boolean.getBoolean("nativeexecution.mode.unittest") && !"true".equals(System.getProperty("cnd.command.line.utility"))) { // NOI18N)
                    NativeExecutionUserNotification.getDefault().
                            notify(NbBundle.getMessage(SPSRemoteImpl.class, 
                                    "TaskPrivilegesSupport_GrantPrivileges_Failed"));//NOI18N
//                    NotifyDescriptor dd =
//                            new NotifyDescriptor.Message(NbBundle.getMessage(SPSRemoteImpl.class, "TaskPrivilegesSupport_GrantPrivileges_Failed"));
//                    DialogDisplayer.getDefault().notify(dd);
                }
            }

            // DO NOT CLOSE A CHANNEL HERE... (Why?)
            // channel.disconnect();

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getInstance().log(Level.FINE, "", ex); // NOI18N
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getInstance().log(Level.FINE, "", ex); // NOI18N
                }
            }

            if (w != null) {
                w.close();
            }
        }

        return false;
    }

    /**
     * Expects some predefined string to appear in reader's stream
     * @param r - reader to use
     * @param expectedString
     * @return
     */
    private static String expect(
            final InputStream in,
            final String expectedString) throws IOException {

        int pos = 0;
        int len = expectedString.length();
        char[] cbuf = new char[2];
        StringBuilder sb = new StringBuilder();

        // LATER: shouldn't it use ProcessUtils.getReader?
        Reader r = new InputStreamReader(in);

        while (pos != len && r.read(cbuf, 0, 1) != -1) {
            char currentChar = expectedString.charAt(pos);
            if (currentChar == '%') {
                pos++;
                sb.append(cbuf[0]);
            } else if (currentChar == cbuf[0]) {
                pos++;
            } else {
                pos = 0;
            }
        }

        return sb.toString();
    }
}
