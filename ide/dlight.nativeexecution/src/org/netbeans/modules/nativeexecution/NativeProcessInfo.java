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
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Utilities;

/**
 *
 */
// @NotThreadSafe
public final class NativeProcessInfo {
    private static final String DEFAULT_CHARSET = "UTF-8"; // NOI18N

    public final MacroExpander macroExpander;
    private final ExecutionEnvironment execEnv;
    private final boolean isWindows;
    private final MacroMap environment;
    private final List<String> arguments = new ArrayList<>();
    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<>();
    private String executable;
    private String commandLine;
    private String workingDirectory;
    private boolean unbuffer;
    private boolean redirectError;
    private boolean x11forwarding;
    private boolean suspend;
    private Pty pty = null;
    private boolean runInPty;
    private boolean expandMacros = true;
    private Charset charset;
    private boolean statusEx;

    public NativeProcessInfo(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        this.executable = null;
        this.unbuffer = false;
        this.workingDirectory = null;
        this.macroExpander = MacroExpanderFactory.getExpander(execEnv);
        this.environment = MacroMap.forExecEnv(execEnv);
        isWindows = execEnv.isLocal() && Utilities.isWindows();
        redirectError = false;
    }

    NativeProcessInfo(NativeProcessInfo info, boolean copyListeners) {
        this.macroExpander = info.macroExpander;
        this.execEnv = info.execEnv;
        this.isWindows = info.isWindows;
        this.environment = info.environment.clone();
        this.arguments.addAll(info.arguments);
        this.executable = info.executable;
        this.commandLine = info.commandLine;
        this.workingDirectory = info.workingDirectory;
        this.unbuffer = info.unbuffer;
        this.redirectError = info.redirectError;
        this.x11forwarding = info.x11forwarding;
        this.suspend = info.suspend;
        if (copyListeners) {
            this.listeners.addAll(info.getListenersSnapshot());
        }
        this.pty = info.pty;
        this.runInPty = info.runInPty;
        this.expandMacros = info.expandMacros;
        this.charset = info.charset;
        this.statusEx = info.statusEx;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.addIfAbsent(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void redirectError(boolean redirectError) {
        this.redirectError = redirectError;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @Deprecated
    public void setCommandLine(String commandLine) {
        if (isWindows && commandLine != null) {
            // Until we use java ProcessBuilder on Windows,
            // we cannot pass a single line to it [IZ#170748]
            String[] cmdAndArgs = Utilities.parseParameters(commandLine);
            if (cmdAndArgs.length == 0) {
                return;
            }

            String execFile = cmdAndArgs[0];
            setExecutable(execFile);
            if (cmdAndArgs.length == 1) {
                return;
            }

            List<String> args = new ArrayList<>(cmdAndArgs.length - 1);
            for (int i = 1; i < cmdAndArgs.length; i++) {
                args.add(cmdAndArgs[i]);
            }

            setArguments(args.toArray(new String[0]));
        } else {
            this.commandLine = commandLine;
        }
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setUnbuffer(boolean unbuffer) {
        this.unbuffer = unbuffer;
    }

    public boolean isUnbuffer() {
        return unbuffer;
    }

    public void setX11Forwarding(boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

    public boolean getX11Forwarding() {
        return x11forwarding;
    }

    public void setInitialSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    public boolean getInitialSuspend() {
        return suspend;
    }

    public void setArguments(String... arguments) {
        if (commandLine != null) {
            throw new IllegalStateException("commandLine is already defined. No additional parameters can be set"); // NOI18N
        }

        this.arguments.clear();

        if (arguments != null) {
            for (String arg : arguments) {
                this.arguments.add(arg.trim());
            }
        }
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getExecutable() {
        return executable;
    }

    public List<String> getCommand() {
        if (executable == null && commandLine == null) {
            return null;
        }

        List<String> result = new ArrayList<>();

        String cmd;

        if (commandLine != null) {
            try {
                if (isExpandMacros()) {
                    cmd = macroExpander.expandPredefinedMacros(commandLine);
                } else {
                    cmd = executable;
                }
            } catch (Exception ex) {
                cmd = executable;
            }

            result.add(cmd);
        } else {
            try {
                if (isExpandMacros()) {
                    cmd = macroExpander.expandPredefinedMacros(executable);
                } else {
                    cmd = executable;
                }
            } catch (Exception ex) {
                cmd = executable;
            }

            if (execEnv.isLocal()) {
                cmd = findFullPathToExceutable(cmd);
            }

            result.add(cmd);

            for (String arg : arguments) {
                if (isExpandMacros()) {
                    arg = Utilities.escapeParameters(new String[]{arg});
                    if ((arg.startsWith("'") && arg.endsWith("'")) || // NOI18N
                            (arg.startsWith("\"") && arg.endsWith("\""))) { // NOI18N
                        arg = arg.substring(1, arg.length() - 1);
                    }
                    result.add('"' + arg + '"'); // NOI18N
                } else {
                    result.add(arg);
                }
            }
        }

        return result;
    }

    private String quoteSpecialChars(String orig) {
        StringBuilder sb = new StringBuilder();
        String escapeChars = (isWindows) ? " &\"'()" : " &\"'()!"; // NOI18N

        for (char c : orig.toCharArray()) {
            if (escapeChars.indexOf(c) >= 0) { // NOI18N
                sb.append('\\');
            }
            sb.append(c);
        }

        return sb.toString();
    }

    public void setStatusEx(boolean getStatus) {
        this.statusEx = getStatus;
    }

    public String getCommandLineForShell() {
        if (commandLine == null && executable == null) {
            return null;
        }

        /**
         * See IZ#168186 - Wrongly interpreted "$" symbol in arguments
         *
         * The magic below is all about making run/debug act identically in case
         * of ExternalTerminal
         */
        if (commandLine != null) {
            return commandLine;
        }

        StringBuilder sb = new StringBuilder();

        List<String> cmd = getCommand();

        String exec = cmd.get(0);

        if (isWindows) {
            exec = WindowsSupport.getInstance().convertToShellPath(exec);

            if (exec == null) {
                return null;
            }
        }

        sb.append(quoteSpecialChars(exec)).append(' ');

        String[] sarg = new String[1];

        boolean escape;

        for (String arg : arguments) {
            if(!arg.contains(" ") && !arg.contains("$")) { // NOI18N
                // This condition avoids quoting --login, [NETBEANS-4143]
                sb.append(arg).append(' ');
                continue;
            }

            escape = false;
            sarg[0] = arg;
            arg = Utilities.escapeParameters(sarg);

            sb.append('"');

            if ((arg.startsWith("'") && arg.endsWith("'")) || // NOI18N
                    (arg.startsWith("\"") && arg.endsWith("\""))) { // NOI18N
                arg = arg.substring(1, arg.length() - 1);
                escape = true;
            }

            if (isWindows || escape) {
                char pc = 'x';

                for (char c : arg.toCharArray()) {
                    if (c == '$' && pc != '\\') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    pc = c;
                }
            } else {
                sb.append(arg);
            }

            sb.append("\" "); // NOI18N
        }

        return sb.toString().trim();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    /* package */ Collection<ChangeListener> getListenersSnapshot() {
        return new LinkedList<>(listeners);
    }

    public String getWorkingDirectory(boolean expandMacros) {
        String result = workingDirectory;
        if (expandMacros && macroExpander != null) {
            try {
                result = macroExpander.expandPredefinedMacros(workingDirectory);
            } catch (ParseException ex) {
                // nothing
            }
        }
        return result;
    }

    public MacroMap getEnvironment() {
        return environment;
    }

    public void setPty(Pty pty) {
        this.pty = pty;
        runInPty = (pty != null);
    }

    public Pty getPty() {
        return pty;
    }

    public void setPtyMode(boolean ptyMode) {
        this.runInPty = ptyMode;
        if (!ptyMode) {
            pty = null;
        }
    }

    public boolean isPtyMode() {
        return runInPty || getPty() != null;
    }

    /**
     * @return the expandMacros
     */
    public boolean isExpandMacros() {
        return expandMacros;
    }

    /**
     * @param expandMacros the expandMacros to set
     */
    public void setExpandMacros(boolean expandMacros) {
        this.expandMacros = expandMacros;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    private String findFullPathToExceutable(String cmd) {
        if (execEnv.isRemote()) {
            // Not going to search on remote ...
            return cmd;
        }
        File f;
        if ((!isWindows && cmd.startsWith("/"))) { // NOI18N
            f = new File(cmd);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        if ((isWindows && cmd.length() > 2 && cmd.charAt(1) == ':')) {
            f = new File(cmd);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
            f = new File(cmd + ".exe"); // NOI18N
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        f = new File(workingDirectory, cmd);

        if (f.exists()) {
            return f.getAbsolutePath();
        }

        if (isWindows) {
            f = new File(workingDirectory, cmd + ".exe"); // NOI18N

            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        return cmd;
    }

    boolean isStatusEx() {
        return statusEx;
    }

    boolean isCommandLineDefined() {
        return commandLine != null;
    }

    public boolean isRedirectError() {
        return redirectError;
    }
    
    public static String getCharset(NativeProcess process) {
        String res = null;
        if (process instanceof AbstractNativeProcess) {
            res = ((AbstractNativeProcess) process).getCharset();
        }
        return res == null ? DEFAULT_CHARSET : res;
    }    
}
