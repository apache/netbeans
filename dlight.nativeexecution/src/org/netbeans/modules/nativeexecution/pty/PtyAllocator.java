/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.pty;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.JschSupport;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.PtyOpenUtility.PtyInfo;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
public final class PtyAllocator {

    private static final PtyAllocator instance = new PtyAllocator();

    private PtyAllocator() {
    }

    public static PtyAllocator getInstance() {
        return instance;
    }

    public Pty allocate(final ExecutionEnvironment env) throws IOException {
        PtyImplementation result = null;

        String ptyOpenUtilityPath = PtyOpenUtility.getInstance().getPath(env);

        if (ptyOpenUtilityPath == null) {
            throw new IOException("pty_open cannot be located"); // NOI18N
        }

        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoUtils.getHostInfo(env);
        } catch (CancellationException ex) {
            // TODO:CancellationException error processing
        }

        if (hostInfo == null) {
            throw new IOException("no hostinfo available for " + env.getDisplayName()); // NOI18N
        }

        ChannelStreams streams = null;
        try {
            if (env.isLocal()) {
                ProcessBuilder pb = new ProcessBuilder(hostInfo.getShell(), "-s"); // NOI18N

                if (Utilities.isWindows()) {
                    // Only works with cygwin...
                    if (hostInfo.getShell() == null || WindowsSupport.getInstance().getActiveShell().type != Shell.ShellType.CYGWIN) {
                        throw new IOException("terminal support requires Cygwin to be installed"); // NOI18N
                    }
                    ptyOpenUtilityPath = WindowsSupport.getInstance().convertToCygwinPath(ptyOpenUtilityPath);
                    String path = MacroMap.forExecEnv(env).get("PATH"); // NOI18N
                    pb.environment().put("Path", path); // NOI18N
                }

                Process pty = pb.start(); // no ProcessUtils, streams are attached below
                streams = new ChannelStreams(null, pty.getInputStream(), pty.getErrorStream(), pty.getOutputStream());
            } else {
                // Here I have faced with a problem that when
                // I'm trying to start ptyOpenUtilityPath directly - I'm fail
                // to read from it's output in some [64-bit linux, or ssh on
                // localhost (solaris/linux)] cases.
                // The workaround below is to use sh -s ...
                // It works, though I don't fully understand the reason...
                streams = JschSupport.startCommand(env, "/bin/sh -s", null); // NOI18N
            }

            streams.in.write(("exec \"" + ptyOpenUtilityPath + "\"\n").getBytes()); // NOI18N
            streams.in.flush();

            PtyInfo ptyInfo = PtyOpenUtility.getInstance().readSatelliteOutput(streams.out);

            if (ptyInfo == null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(streams.err));
                String errorLine;
                StringBuilder err_msg = new StringBuilder();
                while ((errorLine = br.readLine()) != null) {
                    err_msg.append(errorLine).append('\n');
                }
                throw new IOException(err_msg.toString());
            }

            result = new PtyImplementation(env, ptyInfo.tty, ptyInfo.pid, streams);
        } catch (Exception ex) {
            throw (ex instanceof IOException) ? (IOException) ex : new IOException(ex);
        } finally {
            if (result == null && streams != null) {
                if (streams.in != null) {
                    streams.in.close();
                }
                if (streams.out != null) {
                    streams.out.close();
                }
                if (streams.err != null) {
                    streams.err.close();
                }
                if (streams.channel != null) {
                    try {
                        ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(env, streams.channel);
                    } catch (JSchException ex) {
                    }
                }
            }
        }

        return result;
    }

    private final static class PtyImplementation implements Pty {

        private final String tty;
        private final int pid;
        private final ExecutionEnvironment env;
        private final ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        private final ChannelStreams streams;

        public PtyImplementation(ExecutionEnvironment env, String tty, int pid, ChannelStreams streams) throws IOException {
            this.tty = tty;
            this.pid = pid;
            this.streams = streams;
            this.env = env;
        }

        @Override
        public ExecutionEnvironment getEnv() {
            return env;
        }

        @Override
        public final void close() throws IOException {
            streams.in.close();
            streams.out.close();
            if (streams.channel != null) {
                try {
                    ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(env, streams.channel);
                } catch (JSchException ex) {
                }
            }
        }

        @Override
        public String toString() {
            return tty + " (" + pid + ")"; // NOI18N
        }

        @Override
        public InputStream getInputStream() {
            return streams.out;
        }

        @Override
        public OutputStream getOutputStream() {
            return streams.in;
        }

        @Override
        public InputStream getErrorStream() {
            return bis;
        }

        @Override
        public String getSlaveName() {
            return tty;
        }
    }
}
