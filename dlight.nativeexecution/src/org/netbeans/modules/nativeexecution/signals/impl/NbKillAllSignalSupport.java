/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
                    if (scope == SIGNAL_SCOPE.SIGNAL_BY_ENV) {
                        return false;
                    }
                    Shell activeShell = WindowsSupport.getInstance().getActiveShell();
                    return (activeShell != null && activeShell.type == Shell.ShellType.CYGWIN);
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
