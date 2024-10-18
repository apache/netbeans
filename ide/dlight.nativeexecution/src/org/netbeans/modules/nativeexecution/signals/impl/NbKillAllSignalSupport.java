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
package org.netbeans.modules.nativeexecution.signals.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HelperUtility;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.signals.SignalSupport.SIGNAL_SCOPE;
import org.netbeans.modules.nativeexecution.signals.SignalSupportImplementation;
import org.netbeans.modules.nativeexecution.support.ShellSession;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SignalSupportImplementation.class, position = 100)
public final class NbKillAllSignalSupport extends HelperUtility implements SignalSupportImplementation {

    public NbKillAllSignalSupport() {
        super("bin/nativeexecution/${osname}-${platform}${_isa}/killall"); // NOI18N
    }

    @Override
    public boolean isSupported(ExecutionEnvironment env, SIGNAL_SCOPE scope) {
        if (!HostInfoUtils.isHostInfoAvailable(env)) {
            return false;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);

            if (getPath(env, hostInfo) == null) {
                return false;
            }

            switch (hostInfo.getOSFamily()) {
                case LINUX:
                case SUNOS:
                    return true;
                case FREEBSD:
                    return false;
                case WINDOWS:
                    Shell activeShell = WindowsSupport.getInstance().getActiveShell();
                    if (scope == SIGNAL_SCOPE.SIGNAL_BY_ENV && (activeShell == null || activeShell.type != Shell.ShellType.WSL)) {
                        return false;
                    }
                    return (activeShell != null && (activeShell.type == Shell.ShellType.CYGWIN || activeShell.type == Shell.ShellType.WSL));
                case MACOSX:
                    if (scope == SIGNAL_SCOPE.SIGQUEUE_PROCESS) {
                        return false;
                    }
                    return true;
                default:
                    return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int sendSignal(ExecutionEnvironment env, SIGNAL_SCOPE scope, int id, Signal signal) {
        String param;
        switch (scope) {
            case SIGNAL_PROCESS:
                param = "-p"; // NOI18N
                break;
            case SIGNAL_GROUP:
                param = "-g"; // NOI18N
                break;
            case SIGNAL_SESSION:
                param = "-s"; // NOI18N
                break;
            case SIGNAL_BY_ENV:
            case SIGQUEUE_PROCESS:
            default:
                throw new IllegalArgumentException();
        }
        return doSignal(env, param, signal, Integer.toString(id));
    }

    @Override
    public int sendSignal(ExecutionEnvironment env, String environment, Signal signal) {
        return doSignal(env, "-e", signal, environment); // NOI18N
    }

    @Override
    public int sigqueue(ExecutionEnvironment env, int id, Signal signal, int sigdata) {
        return doSignal(env, "-q", signal, Integer.toString(id), Integer.toString(sigdata)); // NOI18N
    }

    private int doSignal(ExecutionEnvironment env, String scope, Signal signal, String... args) {
        try {
            String path = getPath(env);
            if (path == null) {
                return -1;
            }

            StringBuilder cmd = new StringBuilder();
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (HostInfo.OSFamily.WINDOWS.equals(hostInfo.getOSFamily())) {
                    path = WindowsSupport.getInstance().convertToShellPath(path);
                }
                if (path == null) {
                    return -1;
                }
            } catch (ConnectionManager.CancellationException ex) {
                return -1;
            }

            cmd.append('"').append(path).append('"').append(' '); // NOI18N
            cmd.append(scope).append(' ');
            cmd.append(signal == Signal.NULL ? "NULL" : signal.name().substring(3)).append(' '); // NOI18N
            for (String arg : args) {
                cmd.append(arg).append(' ');
            }

            ExitStatus status = ShellSession.execute(env, cmd.toString());

            if (!status.getErrorLines().isEmpty()) {
                log.log(Level.FINE, "doSignal: {0}", status.toString()); // NOI18N
            }

            return status.exitCode;
        } catch (IOException ex) {
            log.log(Level.FINE, "attempt to send signal " + signal.name() // NOI18N
                    + " to " + scope + " " + Arrays.toString(args) + " failed:", ex); // NOI18N
            return -1;
        }
    }
}
