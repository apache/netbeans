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
package org.netbeans.modules.nativeexecution;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * @author ak119685
 */
public final class JschSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();

    private JschSupport() {
    }

    /**
     * Starts the specified command (executable + params) on the specified ExecutionEnvironment.
     *
     * @param env - environment to execute in
     * @param command - executable + params to execute
     * @param params - (optional) channel params. May be null.
     * @return I/O streams and opened execution JSch channel. Never returns NULL.
     * @throws IOException - if unable to aquire an execution channel
     * @throws JSchException - if JSch exception occured
     * @throws InterruptedException - if the thread was interrupted
     */
    public static ChannelStreams startCommand(final ExecutionEnvironment env, final String command, final ChannelParams params)
            throws IOException, JSchException, InterruptedException {

        JSchWorker<ChannelStreams> worker = new JSchWorker<ChannelStreams>() {

            @Override
            public ChannelStreams call() throws JSchException, IOException, InterruptedException {
                ChannelExec echannel = (ChannelExec) ConnectionManagerAccessor.getDefault().openAndAcquireChannel(env, "exec", true); // NOI18N

                if (echannel == null) {
                    throw new IOException("Cannot open exec channel on " + env + " for " + command); // NOI18N
                }

                echannel.setCommand(command);
                echannel.setXForwarding(params == null ? false : params.x11forward);
                InputStream is = echannel.getInputStream();
                InputStream es = echannel.getErrStream();
                OutputStream os = new ProtectedOutputStream(echannel, echannel.getOutputStream());
                Authentication auth = Authentication.getFor(env);
                echannel.connect(auth.getTimeout() * 1000);
                return new ChannelStreams(echannel, is, es, os);
            }

            @Override
            public String toString() {
                return command;
            }
        };

        return start(worker, env, 2);
    }

    public static ChannelStreams startLoginShellSession(final ExecutionEnvironment env) throws IOException, JSchException, InterruptedException {
        JSchWorker<ChannelStreams> worker = new JSchWorker<ChannelStreams>() {

            @Override
            public ChannelStreams call() throws InterruptedException, JSchException, IOException {
                ChannelShell shell = (ChannelShell) ConnectionManagerAccessor.getDefault().openAndAcquireChannel(env, "shell", true); // NOI18N

                if (shell == null) {
                    throw new IOException("Cannot open shell channel on " + env); // NOI18N
                }

                shell.setPty(false);
                InputStream is = shell.getInputStream();
                InputStream es = new ByteArrayInputStream(new byte[0]);
                OutputStream os = shell.getOutputStream();
                Authentication auth = Authentication.getFor(env);
                shell.connect(auth.getTimeout() * 1000);
                return new ChannelStreams(shell, is, es, os);
            }

            @Override
            public String toString() {
                return "shell session for " + env.getDisplayName(); // NOI18N
            }
        };

        return start(worker, env, 2);
    }

    private static synchronized ChannelStreams start(final JSchWorker<ChannelStreams> worker, final ExecutionEnvironment env, final int attempts) throws IOException, JSchException, InterruptedException {
        int retry = attempts;

        while (retry-- > 0) {
            try {
                return worker.call();
            } catch (JSchException ex) {
                String message = ex.getMessage();
                Throwable cause = ex.getCause();
                if (cause instanceof NullPointerException) {
                    // Jsch bug... retry?
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Retrying", ex); // NOI18N
                } else if ("java.io.InterruptedIOException".equals(message)) { // NOI18N
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Retrying in 0.5 seconds", ex); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if ("channel is not opened.".equals(message)) { // NOI18N
                    log.log(Level.INFO, "JSch exception opening channel to " + env + ". Reconnecting and retrying", ex); // NOI18N
                    // Now reconnect disconnects old session and creates new, so this might help
                    ConnectionManagerAccessor.getDefault().reconnect(env);

                } else {
                    throw ex;
                }
            } catch (NullPointerException npe) {
                // Jsch bug... retry? ;)
                log.log(Level.FINE, "Exception from JSch", npe); // NOI18N
            }
        }

        throw new IOException("Failed to execute " + worker.toString()); // NOI18N
    }

    public static final class ChannelStreams {

        public final InputStream out;
        public final InputStream err;
        public final OutputStream in;
        public final Channel channel;

        public ChannelStreams(Channel channel, InputStream out,
                InputStream err, OutputStream in) {
            this.channel = channel;
            this.out = out;
            this.err = err;
            this.in = in;
        }
    }

    public static final class ChannelParams {

        private boolean x11forward = false;

        public void setX11Forwarding(boolean forward) {
            this.x11forward = forward;
        }
    }

    private static interface JSchWorker<T> {

        T call() throws InterruptedException, IOException, JSchException;
    }

    private static class ProtectedOutputStream extends OutputStream {

        private final ChannelExec channel;
        private final OutputStream stream;

        private ProtectedOutputStream(ChannelExec channel, OutputStream stream) {
            this.stream = stream;
            this.channel = channel;
        }

        @Override
        public void write(int b) throws IOException {
            checkAlive();
            stream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            checkAlive();
            stream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkAlive();
            stream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            checkAlive();
            stream.flush();
        }

        @Override
        public void close() throws IOException {
            if (!channel.isConnected()) {
                return;
            }
            stream.close();
        }

        private void checkAlive() throws IOException {
            if (!channel.isConnected()) {
                throw new IOException("Channel is already closed"); // NOI18N
            }
        }
    }
}
