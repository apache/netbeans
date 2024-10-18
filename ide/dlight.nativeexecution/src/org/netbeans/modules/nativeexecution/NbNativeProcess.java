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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.ProcessStatusEx;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.signals.SignalSupport;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * An implementation of NativeProcess that uses NbStartUtility as a trampoline.
 *
 * Supported platforms: MacOSX
 *
 * @author Andrew
 */
public abstract class NbNativeProcess extends AbstractNativeProcess {

    private final String nbStartPath;
    private volatile ProcessStatusEx statusEx;

    public NbNativeProcess(final NativeProcessInfo info) {
        super(new NativeProcessInfo(info, true));
        String _nbStartPath = null;
        try {
            _nbStartPath = NbStartUtility
                    .getInstance(getExecutionEnvironment().isLocal())
                    .getPath(getExecutionEnvironment());
        } catch (IOException ex) {
        } finally {
            nbStartPath = _nbStartPath;
        }
    }

    @Override
    protected final void create() throws Throwable {
        createProcessImpl(getCommand());
        readProcessInfo(getInputStream());
    }

    private List<String> getCommand() {
        List<String> command = new ArrayList<>();

        File wslPath = new File(System.getenv("windir"), "system32/wsl.exe");
        Shell activeShell = null;
        if(isWindows()) {
            activeShell = WindowsSupport.getInstance().getActiveShell();
        }
        if (activeShell != null && activeShell.type == Shell.ShellType.WSL) {
            command.add(wslPath.getAbsolutePath());
            command.add(WindowsSupport.getInstance().convertToWSL(nbStartPath));
        } else {
            command.add(nbStartPath);
        }


        String wdir = info.getWorkingDirectory(true);
        if (wdir != null && !wdir.isEmpty()) {
            command.add("--dir"); // NOI18N
            if (activeShell != null && activeShell.type == Shell.ShellType.WSL) {
                command.add(WindowsSupport.getInstance().convertToWSL(wdir));
            } else {
                command.add(fixForWindows(wdir));
            }
        }

        if (!info.isPtyMode()) {
            command.add("--no-pty"); // NOI18N
        } else {
            Pty pty = info.getPty();
            if (FIX_ERASE_KEY_IN_TERMINAL) {
                command.add("--set-erase-key"); // NOI18N;
            }
            if (pty != null) {
                command.add("-p"); // NOI18N
                command.add(pty.getSlaveName());
            }
        }

        if (!info.isPtyMode() && info.isUnbuffer()) {
            try {
                UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), info.getEnvironment());
            } catch (IOException ex) {
                Logger.getInstance().log(Level.FINE, "initUnbuffer failed", ex); // NOI18N
            }
        }

        if (info.getInitialSuspend()) {
            command.add("-w"); // NOI18N
        }

        boolean getStatus = info.isStatusEx();
        if (getStatus) {
            command.add("--report"); // NOI18N
            // hostInfo.getTempDir() is already in 'shell' format for Windows
            command.add(hostInfo.getTempDir() + "/status"); // NOI18N
        }

        String envFile = hostInfo.getEnvironmentFile();
        if (envFile != null) {
            // envFile is already in 'shell' format for Windows
            command.add("--readenv"); // NOI18N
            command.add(envFile);
        }

        if (info.isRedirectError()) {
            command.add("--redirect-error"); // NOI18N
        }

        MacroMap userEnv = info.getEnvironment();
        if (userEnv != null) {
            Map<String, String> userDefinedMap = userEnv.getUserDefinedMap();

            for (Map.Entry<String, String> entry : userDefinedMap.entrySet()) {
                if (isWindows() && entry.getKey().equalsIgnoreCase("PATH") && WindowsSupport.getInstance().getActiveShell().type != Shell.ShellType.WSL) { // NOI18N
                    command.add("--env"); // NOI18N
                    command.add(entry.getKey() + "=" + WindowsSupport.getInstance().convertToAllShellPaths(entry.getValue())); // NOI18N
                    continue;
                }
                command.add("--env"); // NOI18N
                String setCall = entry.getKey() + "=" + entry.getValue();
                if (isWindows() && WindowsSupport.getInstance().getActiveShell().type == Shell.ShellType.WSL) {
                    // the environment must be escaped so that it is passed
                    // to the inner shell
                    command.add(
                            "\""
                            + setCall
                                    .replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    .replace("$", "\\$")
                            + "\""); // NOI18N
                } else {
                    command.add(setCall);
                }
            }
        }

        if (info.isCommandLineDefined()) {
            command.add(hostInfo.getShell());
            command.add("-c"); // NOI18N
            final String origCommand = info.getCommandLineForShell();
            command.add("exec " + origCommand); // NOI18N
        } else {
            command.add(fixForWindows(info.getExecutable()));
            command.addAll(info.getArguments());
        }
        return command;
    }

    private void readProcessInfo(InputStream fromProcessStream) throws IOException {
        String line;

        while (!(line = readLine(fromProcessStream).trim()).isEmpty()) {
            addProcessInfo(line);
        }

        String pidProperty = getProcessInfo("PID"); // NOI18N

        if (pidProperty == null) {
            InputStream error = getErrorStream();
            while (!(line = readLine(error).trim()).isEmpty()) {
                LOG.info(line);
            }
            throw new InternalError("Failed to get process PID"); // NOI18N
        }

        setPID(Integer.parseInt(pidProperty)); // NOI18N    
    }

    @Override
    protected final int waitResult() throws InterruptedException {
        int result = waitResultImpl();

        String reportFile = getProcessInfo("REPORT"); // NOI18N

        if (reportFile != null) {
            // NbNativeProcess works in either *nix or cygwin environment;
            // So it is safe to call /bin/sh here in any case
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(info.getExecutionEnvironment());
            npb.setExecutable("/bin/sh"); // NOI18N
            npb.setArguments("-c", "cat " + reportFile + " && rm " + reportFile); // NOI18N
            ExitStatus st = ProcessUtils.execute(npb);
            if (st.isOK()) {
                statusEx = ProcessStatusAccessor.getDefault().create(st.getOutputString().split("\n")); // NOI18N
                result = statusEx.getExitCode();
            }
        }

        return result;
    }

    @Override
    public ProcessStatusEx getExitStatusEx() {
        // Ensure that process is finished
        exitValue();
        return statusEx;
    }

    private String readLine(final InputStream is) throws IOException {
        int c;
        StringBuilder sb = new StringBuilder(20);

        while (!isInterrupted()) {
            c = is.read();

            if (c < 0 || c == '\n') {
                break;
            }

            sb.append((char) c);
        }

        return sb.toString().trim();
    }

    protected abstract int waitResultImpl() throws InterruptedException;

    protected abstract void createProcessImpl(List<String> command) throws Throwable;

    @Override
    protected int destroyImpl() {
        if (destroyed()) {
            return 0;
        }

        // signal using env
        String env = getProcessInfo("NBMAGIC"); // NOI18N
        if (env != null) {
            String magicEnv = "NBMAGIC=" + env; // NOI18N
            SignalSupport.signalProcessesByEnv(info.getExecutionEnvironment(), magicEnv, Signal.SIGTERM);
        }

        return 0;
    }

    protected boolean isWindows() {
        return HostInfo.OSFamily.WINDOWS.equals(hostInfo.getOSFamily());
    }

    protected String fixForWindows(String path) {
        if(isWindows() && WindowsSupport.getInstance().getActiveShell().type == Shell.ShellType.CYGWIN) {
            return WindowsSupport.getInstance().convertToCygwinPath(path);
        } else {
            return path;
        }
    }

    private boolean destroyed() {
        try {
            exitValue();
            return true;
        } catch (IllegalThreadStateException ex) {
            return false;
        }
    }
}
