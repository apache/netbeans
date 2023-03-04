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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.nativeexecution.ExternalTerminalAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.TerminalProfile;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * NativeProcessBuilder can create a process that is executed in an external
 * terminal identified by ExternalTerminal object. The object can be obtained
 * with {@link ExternalTerminalProvider#getTerminal(java.lang.String)}.
 *
 * @see ExternalTerminalProvider
 */
public final class ExternalTerminal {

    private static final ConcurrentHashMap<TermEnvPair, String> execCache =
            new ConcurrentHashMap<>();
    private final TerminalProfile profile;
    private String title = null;
    private String workdir = null;

    private static final boolean CLOSE_TERMINAL = Boolean.getBoolean("org.netbeans.modules.nativeexecution.api.util.CloseTerminal"); // NOI18N
    private String prompt = CLOSE_TERMINAL ? "NO" : loc("Terminal.DefaultPrompt.text"); // NOI18N

    static {
        ExternalTerminalAccessor.setDefault(new ExternalTerminalAccessorImpl());
    }

    ExternalTerminal(TerminalProfile info) throws IllegalArgumentException {
        this.profile = info;
    }

    private ExternalTerminal(ExternalTerminal terminal) {
        profile = terminal.profile;
        title = terminal.title;
        workdir = terminal.workdir;
        prompt = terminal.prompt;
    }

    public boolean isAvailable(ExecutionEnvironment executionEnvironment) {
        return getExecutable(executionEnvironment) != null;
    }

    public String getID() {
        return profile.getID();
    }

    /**
     * Returnes an ExternalTerminal with configured prompt message that
     * appears in terminal after command execution is finished.
     *
     * @param prompt prompt to be used in external terminal
     * @return ExternalTerminal with configured prompt message
     */
    public ExternalTerminal setPrompt(String prompt) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.prompt = prompt;
        return result;
    }

    /**
     * Returnes an ExternalTerminal with configured title that
     * appears in terminal that executes a native process.
     *
     * @param title String to be displayed in a title of a terminal (if a
     *        terminal has capabilities to set a title)
     * 
     * @return ExternalTerminal with configured title
     */
    public ExternalTerminal setTitle(String title) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.title = title;
        return result;
    }
    
    /**
     * Returnes an ExternalTerminal with configured workdir that
     * is ued in terminal that executes a native process.
     *
     * @param workdir String to be used as a working dir in terminal (if a
     *        terminal has capabilities to set a workdir)
     * 
     * @return ExternalTerminal with configured title
     */
    public ExternalTerminal setWorkdir(String workdir) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.workdir = workdir;
        return result;
    }

    private static class ExternalTerminalAccessorImpl
            extends ExternalTerminalAccessor {
        private static final String ARG_TITLE = "$title"; //NOI18N
        private static final String ARG_WORKDIR = "$workdir"; //NOI18N

        @Override
        public TerminalProfile getTerminalProfile(ExternalTerminal terminal) {
            return terminal.profile;
        }

        @Override
        public List<String> wrapCommand(ExecutionEnvironment execEnv,
                ExternalTerminal terminal, List<String> args) {
            String exec = terminal.getExecutable(execEnv);

            if (exec == null) {
                return args;
            }

            ArrayList<String> result = new ArrayList<>();

            result.add(exec);

            for (String arg : terminal.profile.getArguments()) {
                if ("$@".equals(arg)) { // NOI18N
                    result.addAll(args);
                    continue;
                }

                if ("$shell".equals(arg)) { // NOI18N
                    try {
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                        result.add(hostInfo.getShell());
                        continue;
                    } catch (IOException ex) {
                    } catch (CancellationException ex) {
                    }
                }

                if (arg.contains(ARG_TITLE)) { // NOI18N
                    arg = arg.replace(ARG_TITLE, terminal.title); // NOI18N
                }
                
                if (arg.contains(ARG_WORKDIR)) { // NOI18N
                    arg = arg.replace(ARG_WORKDIR, terminal.workdir); // NOI18N
                }

                result.add(arg);
            }

            return result;
        }

        @Override
        public String getPrompt(ExternalTerminal terminal) {
            return terminal.prompt;
        }

        @Override
        public String getTitle(ExternalTerminal terminal) {
            return terminal.title;
        }

        @Override
        public String getExecutable(ExternalTerminal terminal, ExecutionEnvironment execEnv) {
            return terminal.getExecutable(execEnv);
        }
    }

    private String getExecutable(ExecutionEnvironment execEnv) {
        TermEnvPair key = new TermEnvPair(execEnv, profile.getCommand());

        String exec = execCache.get(key);

        if (exec == null) {
            if (execEnv.isLocal() && Utilities.isWindows()) {
                exec = profile.getCommand();
            } else {
                exec = HostInfoUtils.searchFile(execEnv,
                        profile.getSearchPaths(),
                        profile.getCommand(), true);
            }
            if (exec != null) {
                String execPath = execCache.putIfAbsent(key, exec);

                if (execPath != null) {
                    exec = execPath;
                }
            }
        }

        return exec;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ExternalTerminal.class, key, params);
    }

    /*package*/
    static final class TermEnvPair {

        public final ExecutionEnvironment env;
        public final String termexec;

        public TermEnvPair(ExecutionEnvironment env, String termexec) {
            this.env = env;
            this.termexec = termexec;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TermEnvPair)) {
                throw new IllegalArgumentException();
            }
            TermEnvPair that = (TermEnvPair) obj;

            return env.equals(that.env) && termexec.equals(that.termexec);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.env != null ? this.env.hashCode() : 0);
            hash = 79 * hash + (this.termexec != null ? this.termexec.hashCode() : 0);
            return hash;
        }
    }
}
