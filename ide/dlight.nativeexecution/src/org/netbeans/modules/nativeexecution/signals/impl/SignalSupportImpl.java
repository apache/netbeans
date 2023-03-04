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
package org.netbeans.modules.nativeexecution.signals.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.signals.SignalSupport.SIGNAL_SCOPE;
import org.netbeans.modules.nativeexecution.signals.SignalSupportImplementation;
import org.netbeans.modules.nativeexecution.support.ShellSession;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SignalSupportImplementation.class, position = 200)
public final class SignalSupportImpl implements SignalSupportImplementation {

    private static final Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();

    @Override
    public boolean isSupported(ExecutionEnvironment env, SIGNAL_SCOPE scope) {
        if (!HostInfoUtils.isHostInfoAvailable(env)) {
            return false;
        }

        try {
            if (isWindows(env) && WindowsSupport.getInstance().getActiveShell() == null) {
                return false;
            }

            switch (scope) {
                case SIGNAL_BY_ENV:
                case SIGNAL_SESSION:
                case SIGQUEUE_PROCESS:
                    return false;
                case SIGNAL_PROCESS:
                case SIGNAL_GROUP:
                    return true;
            }
        } catch (Exception ex) {
            log.log(Level.FINE, "SignalSupportImpl.isSupported()", ex); // NOI18N
        }

        return false;
    }

    @Override
    public int sendSignal(ExecutionEnvironment env, SIGNAL_SCOPE scope, int id, Signal signal) {
        try {
            StringBuilder cmd = new StringBuilder("kill -"); // NOI18N
            cmd.append(signal == Signal.NULL ? "0" : signal.name().substring(3)); // NOI18N
            cmd.append(SIGNAL_SCOPE.SIGNAL_GROUP.equals(scope) ? " -" : " "); // NOI18N
            cmd.append(id);
            ExitStatus status = ShellSession.execute(env, cmd.toString());
            return status.exitCode;
        } catch (IOException ex) {
            log.log(Level.FINE, "SignalSupportImpl.sendSignal()", ex); // NOI18N
        }
        return -1;
    }

    @Override
    public int sendSignal(ExecutionEnvironment env, String environment, Signal signal) {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }

    @Override
    public int sigqueue(ExecutionEnvironment env, int id, Signal signal, int value) {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }

    private boolean isWindows(final ExecutionEnvironment env) throws IOException, CancellationException {
        HostInfo hinfo = HostInfoUtils.getHostInfo(env);
        return HostInfo.OSFamily.WINDOWS.equals(hinfo.getOSFamily());
    }
}
