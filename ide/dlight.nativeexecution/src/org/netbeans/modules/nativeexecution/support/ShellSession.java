/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.RequestProcessor;

/**
 * This class holds a single shell session per environment to run small and
 * quick tasks.
 *
 * It is synchronized. Use with care! Failed/closed session will be restored
 * automatically when needed.
 *
 * UTF-8 charset is used for I/O.
 *
 * @author ak119685
 */
public final class ShellSession {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final RequestProcessor RP = new RequestProcessor("ShellSession I/O", 10); // NOI18N
    private static final HashMap<ExecutionEnvironment, ShellProcess> processes =
            new HashMap<>();
    private static final String csName = "UTF-8"; // NOI18N
    private static final String eop = "ShellSession.CMDDONE"; // NOI18N

    private ShellSession() {
    }

    public static void shutdown(final ExecutionEnvironment env) {
        ShellProcess process;

        synchronized (processes) {
            process = processes.put(env, null);
        }

        if (process != null) {
            log.log(Level.FINE, "{0} - shutdown", process); // NOI18N
            ProcessUtils.destroy(process.process);
        }
    }

    private static ShellProcess startProcessIfNeeded(final ExecutionEnvironment env) throws IOException, CancellationException {
        ShellProcess process;

        synchronized (processes) {
            process = processes.get(env);
            if (process != null && ProcessUtils.isAlive(process.process)) {
                return process;
            } else {
                process = null;
                processes.put(env, null);
            }
            try {
                String shell;

                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (HostInfo.OSFamily.WINDOWS.equals(hostInfo.getOSFamily())) {
                    shell = hostInfo.getShell();
                } else {
                    shell = "/bin/sh"; // NOI18N
                }

                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                npb.setExecutable(shell).setArguments("-s"); // NOI18N
                npb.getEnvironment().put("LC_ALL", "C"); // NOI18N
                NativeProcess sh = npb.call();

                if (sh.getState() == State.RUNNING) {
                    process = new ShellProcess(sh);
                    processes.put(env, process);
                } else {
                    process = null;
                    ProcessUtils.readProcessError(sh);
                    ProcessUtils.readProcessOutput(sh);
                }
            } catch (ConnectionManager.CancellationException ex) {
                throw new CancellationException(ex.getMessage());
            }
        }

        if (process == null) {
            throw new IOException("Failed to start shell session on " + env.getDisplayName()); // NOI18N
        }

        log.log(Level.FINE, "{0} - started", process); // NOI18N

        return process;
    }

    public static ExitStatus execute(final ExecutionEnvironment env, final String command) throws IOException, CancellationException {
        while (true) {
            final ShellProcess process = startProcessIfNeeded(env);
            if (process == null) {
                continue;
            }
            synchronized (process) {
                if (ProcessUtils.isAlive(process.process)) {
                    try {
                        ExitStatus result = executeSync(process, env, command);
                        if (result != null) {
                            return result;
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return new ExitStatus(-1, null, MiscUtils.getMessageAsList(ex));
                    }
                }
            }
        }
    }

    private static ExitStatus executeSync(final ShellProcess process, final ExecutionEnvironment env, final String command) throws IOException, CancellationException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(command).append(')'); // NOI18N
        sb.append("; echo ").append(eop).append("$?; echo ").append(eop).append(" 1>&2\n"); // NOI18N
        log.log(Level.FINE, "{0} - \"{1}\"", new Object[]{process, sb.toString()}); // NOI18N
        OutputStream os = process.process.getOutputStream();

        if (!ProcessUtils.isAlive(process.process)) {
            return null;
        }

        try {
            os.write(sb.toString().getBytes(csName));
            os.flush();
        } catch (IOException ex) {
            log.log(Level.FINE, "{0} - FAILED TO WRITE TO OUTPUT - {1}", new Object[]{process, ex.getMessage()}); // NOI18N
            return null;
        }

        final AtomicInteger rc = new AtomicInteger(-1);
        Future<List<String>> out = RP.submit(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                List<String> result = new ArrayList<>();
                String line;
                while ((line = process.out.readLine()) != null) {
                    if (line.startsWith(eop)) {
                        rc.set(Integer.parseInt(line.substring(eop.length())));
                        break;
                    } else {
                        result.add(line);
                    }
                }
                return result;
            }
        });

        Future<List<String>> err = RP.submit(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                List<String> result = new ArrayList<>();
                String line;
                while ((line = process.err.readLine()) != null) {
                    if (line.startsWith(eop)) {
                        break;
                    } else {
                        result.add(line);
                    }
                }
                return result;
            }
        });

        List<String> output, error;

        try {
            output = out.get();
        } catch (ExecutionException ex) {
            return null;
        }

        try {
            error = err.get();
        } catch (ExecutionException ex) {
            return null;
        }

        ExitStatus result = new ExitStatus(rc.get(), output, error);
        log.log(Level.FINE, "{0} - \"{1}\" result is {2}", new Object[]{process, sb.toString(), result}); // NOI18N
        return result;
    }

    private static class ShellProcess {

        private final NativeProcess process;
        private final BufferedReader out;
        private final BufferedReader err;
        private final String displayName;

        public ShellProcess(NativeProcess process) {
            String _displayName;
            try {
                _displayName = "ShellProcess@" + process.getExecutionEnvironment().getDisplayName() + " [" + process.getPID() + "]"; // NOI18N
            } catch (IOException ex) {
                _displayName = "ShellProcess@" + process.getExecutionEnvironment().getDisplayName() + " [broken]"; // NOI18N
            }
            this.displayName = _displayName;
            this.process = process;
            // On Ubuntu when tried to use ReadableByteChannel got unexpected
            // behavior - error was not read from process's error stream...
            // Is it a 'dup2' call side-effect? ...
            //
            // ReadableByteChannel ochannel = Channels.newChannel(process.getInputStream());
            // out = new BufferedReader(Channels.newReader(ochannel, csName));
            // ReadableByteChannel echannel = Channels.newChannel(process.getErrorStream());
            // err = new BufferedReader(Channels.newReader(echannel, csName));

            BufferedReader bout = null;
            try {
                bout = new BufferedReader(new InputStreamReader(process.getInputStream(), csName));
            } catch (UnsupportedEncodingException ex) {
                bout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } finally {
                out = bout;
            }

            BufferedReader berr = null;
            try {
                berr = new BufferedReader(new InputStreamReader(process.getErrorStream(), csName));
            } catch (UnsupportedEncodingException ex) {
                berr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            } finally {
                err = berr;
            }
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
