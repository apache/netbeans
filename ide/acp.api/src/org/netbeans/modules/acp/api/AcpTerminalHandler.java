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
package org.netbeans.modules.acp.api;

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the ACP terminal capability by delegating to OS processes via
 * {@link ProcessBuilder}. Each {@code createTerminal} call spawns a subprocess;
 * subsequent calls read its accumulated output, wait for exit, kill, or
 * release it.
 * <p>
 * One instance is kept per {@link AcpConnection} and released when the
 * connection closes.
 */
public final class AcpTerminalHandler {

    private static final Logger LOG = Logger.getLogger(AcpTerminalHandler.class.getName());
    private static final int MAX_OUTPUT_BYTES = 1_000_000; // 1 MB cap

    private final Map<String, TerminalState> terminals = new ConcurrentHashMap<>();

    static final class TerminalState {
        final Process process;
        final StringBuilder output = new StringBuilder();
        volatile boolean outputDone;
        volatile Integer exitCode;
        final Object lock = new Object();

        TerminalState(Process process) {
            this.process = process;
        }
    }

    /**
     * Starts a subprocess for the agent's command and returns a terminal ID
     * that can be used in subsequent calls.
     */
    public AcpSchema.CreateTerminalResponse handleCreateTerminal(AcpSchema.CreateTerminalRequest request) {
        String terminalId = UUID.randomUUID().toString();
        try {
            List<String> cmd = new ArrayList<>();
            if (request.command() != null && !request.command().isBlank()) {
                cmd.add(request.command());
            }
            if (request.args() != null) {
                cmd.addAll(request.args());
            }
            if (cmd.isEmpty()) {
                LOG.log(Level.WARNING, "createTerminal: no command specified, terminal {0} will be empty", terminalId);
                TerminalState empty = new TerminalState(ProcessHandle.current().isAlive()
                        ? new ProcessBuilder("true").start() : new ProcessBuilder("true").start());
                empty.outputDone = true;
                empty.exitCode = 0;
                terminals.put(terminalId, empty);
                return new AcpSchema.CreateTerminalResponse(terminalId);
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // merge stderr into stdout
            if (request.cwd() != null && !request.cwd().isBlank()) {
                pb.directory(Path.of(request.cwd()).toFile());
            }
            if (request.env() != null) {
                for (AcpSchema.EnvVariable ev : request.env()) {
                    if (ev.name() != null && ev.value() != null) {
                        pb.environment().put(ev.name(), ev.value());
                    }
                }
            }

            Process process = pb.start();
            TerminalState state = new TerminalState(process);
            terminals.put(terminalId, state);

            Thread reader = new Thread(() -> readOutput(state, terminalId), "ACP-Terminal-" + terminalId);
            reader.setDaemon(true);
            reader.start();

            LOG.log(Level.FINE, "Terminal created: id={0}, cmd={1}", new Object[]{terminalId, cmd});
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to start terminal process for id=" + terminalId, ex);
        }
        return new AcpSchema.CreateTerminalResponse(terminalId);
    }

    /** Returns accumulated stdout+stderr and the exit status if the process has finished. */
    public AcpSchema.TerminalOutputResponse handleTerminalOutput(AcpSchema.TerminalOutputRequest request) {
        TerminalState state = terminals.get(request.terminalId());
        if (state == null) {
            LOG.log(Level.WARNING, "terminalOutput: unknown terminal {0}", request.terminalId());
            return new AcpSchema.TerminalOutputResponse("", true, null);
        }
        String output;
        boolean done;
        AcpSchema.TerminalExitStatus exitStatus = null;
        synchronized (state.lock) {
            output = state.output.toString();
            done = state.outputDone;
            if (done && state.exitCode != null) {
                exitStatus = new AcpSchema.TerminalExitStatus(state.exitCode, null);
            }
        }
        LOG.log(Level.FINE, "terminalOutput: id={0}, outputLen={1}, done={2}",
                new Object[]{request.terminalId(), output.length(), done});
        return new AcpSchema.TerminalOutputResponse(output, false, exitStatus);
    }

    /** Blocks until the subprocess exits (or the thread is interrupted). */
    public AcpSchema.WaitForTerminalExitResponse handleWaitForTerminalExit(AcpSchema.WaitForTerminalExitRequest request) {
        TerminalState state = terminals.get(request.terminalId());
        if (state == null) {
            LOG.log(Level.WARNING, "waitForTerminalExit: unknown terminal {0}", request.terminalId());
            return new AcpSchema.WaitForTerminalExitResponse(-1, null);
        }
        synchronized (state.lock) {
            while (!state.outputDone) {
                try {
                    state.lock.wait(30_000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return new AcpSchema.WaitForTerminalExitResponse(-1, "interrupted"); // NOI18N
                }
            }
        }
        LOG.log(Level.FINE, "waitForTerminalExit: id={0}, exitCode={1}",
                new Object[]{request.terminalId(), state.exitCode});
        return new AcpSchema.WaitForTerminalExitResponse(
                state.exitCode != null ? state.exitCode : -1, null);
    }

    /** Forcibly kills the subprocess. */
    public AcpSchema.KillTerminalCommandResponse handleKillTerminal(AcpSchema.KillTerminalCommandRequest request) {
        TerminalState state = terminals.get(request.terminalId());
        if (state != null) {
            state.process.destroyForcibly();
            LOG.log(Level.FINE, "killTerminal: id={0}", request.terminalId());
        } else {
            LOG.log(Level.WARNING, "killTerminal: unknown terminal {0}", request.terminalId());
        }
        return new AcpSchema.KillTerminalCommandResponse();
    }

    /** Kills the subprocess and removes it from the registry. */
    public AcpSchema.ReleaseTerminalResponse handleReleaseTerminal(AcpSchema.ReleaseTerminalRequest request) {
        TerminalState state = terminals.remove(request.terminalId());
        if (state != null) {
            state.process.destroyForcibly();
            LOG.log(Level.FINE, "releaseTerminal: id={0}", request.terminalId());
        } else {
            LOG.log(Level.WARNING, "releaseTerminal: unknown terminal {0}", request.terminalId());
        }
        return new AcpSchema.ReleaseTerminalResponse();
    }

    /** Kills all managed terminals; called when the connection closes. */
    public void releaseAll() {
        for (Map.Entry<String, TerminalState> entry : terminals.entrySet()) {
            entry.getValue().process.destroyForcibly();
            LOG.log(Level.FINE, "releaseAll: killing terminal {0}", entry.getKey());
        }
        terminals.clear();
    }

    private static void readOutput(TerminalState state, String terminalId) {
        try (InputStream is = state.process.getInputStream()) {
            byte[] buf = new byte[4096];
            int n;
            long totalBytes = 0;
            boolean truncated = false;
            while ((n = is.read(buf)) != -1) {
                if (!truncated) {
                    String chunk = new String(buf, 0, n, StandardCharsets.UTF_8);
                    synchronized (state.lock) {
                        state.output.append(chunk);
                        totalBytes += n;
                        if (totalBytes > MAX_OUTPUT_BYTES) {
                            state.output.append("\n[...output truncated...]"); // NOI18N
                            truncated = true;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Terminal reader ended: id=" + terminalId, ex);
        } finally {
            try {
                int code = state.process.waitFor();
                synchronized (state.lock) {
                    state.exitCode = code;
                    state.outputDone = true;
                    state.lock.notifyAll();
                }
                LOG.log(Level.FINE, "Terminal exited: id={0}, code={1}", new Object[]{terminalId, code});
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
