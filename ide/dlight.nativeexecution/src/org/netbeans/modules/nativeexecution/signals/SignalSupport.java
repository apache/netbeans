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
package org.netbeans.modules.nativeexecution.signals;

import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.openide.util.Lookup;

public final class SignalSupport {

    public enum SIGNAL_SCOPE {

        SIGNAL_PROCESS,
        SIGNAL_GROUP,
        SIGNAL_SESSION,
        SIGNAL_BY_ENV,
        SIGQUEUE_PROCESS
    }

    public static int signalProcess(ExecutionEnvironment env, int pid, Signal signal) {
        final Collection<? extends SignalSupportImplementation> impls = Lookup.getDefault().lookupAll(SignalSupportImplementation.class);
        for (SignalSupportImplementation impl : impls) {
            if (impl.isSupported(env, SIGNAL_SCOPE.SIGNAL_PROCESS)) {
                try {
                    return impl.sendSignal(env, SIGNAL_SCOPE.SIGNAL_PROCESS, pid, signal);
                } catch (UnsupportedOperationException ex) {
                    // try next
                }
            }
        }

        throw new UnsupportedOperationException("Sending signal to a pid is not supported on " + env.getDisplayName()); // NOI18N
    }

    public static int signalProcessGroup(ExecutionEnvironment env, int gid, Signal signal) {
        final Collection<? extends SignalSupportImplementation> impls = Lookup.getDefault().lookupAll(SignalSupportImplementation.class);
        for (SignalSupportImplementation impl : impls) {
            if (impl.isSupported(env, SIGNAL_SCOPE.SIGNAL_GROUP)) {
                try {
                    return impl.sendSignal(env, SIGNAL_SCOPE.SIGNAL_GROUP, gid, signal);
                } catch (UnsupportedOperationException ex) {
                    // try next
                }
            }
        }

        throw new UnsupportedOperationException("Sending signal to a group of processes is not supported on " + env.getDisplayName()); // NOI18N
    }

    public static int signalProcessSession(ExecutionEnvironment env, int psid, Signal signal) {
        final Collection<? extends SignalSupportImplementation> impls = Lookup.getDefault().lookupAll(SignalSupportImplementation.class);
        for (SignalSupportImplementation impl : impls) {
            if (impl.isSupported(env, SIGNAL_SCOPE.SIGNAL_SESSION)) {
                try {
                    return impl.sendSignal(env, SIGNAL_SCOPE.SIGNAL_SESSION, psid, signal);
                } catch (UnsupportedOperationException ex) {
                    // try next
                }
            }
        }

        throw new UnsupportedOperationException("Sending signal to a session of processes is not supported on " + env.getDisplayName()); // NOI18N
    }

    public static int signalProcessesByEnv(ExecutionEnvironment env, String environment, Signal signal) {
        final Collection<? extends SignalSupportImplementation> impls = Lookup.getDefault().lookupAll(SignalSupportImplementation.class);
        for (SignalSupportImplementation impl : impls) {
            if (impl.isSupported(env, SIGNAL_SCOPE.SIGNAL_BY_ENV)) {
                try {
                    return impl.sendSignal(env, environment, signal);
                } catch (UnsupportedOperationException ex) {
                    // try next
                }
            }
        }

        throw new UnsupportedOperationException("Sending signal to processes by env is not supported on " + env.getDisplayName()); // NOI18N
    }

    public static int sigqueue(ExecutionEnvironment env, int pid, Signal signal, int sigvalue) {
        final Collection<? extends SignalSupportImplementation> impls = Lookup.getDefault().lookupAll(SignalSupportImplementation.class);
        int result = 0;

        for (SignalSupportImplementation impl : impls) {
            if (impl.isSupported(env, SIGNAL_SCOPE.SIGQUEUE_PROCESS)) {
                try {
                    result = impl.sigqueue(env, pid, signal, sigvalue);
                    if (result >= 0) {
                        return result;
                    }
                } catch (UnsupportedOperationException ex) {
                    // try next
                }
            }
        }

        if (result == 0) {
            throw new UnsupportedOperationException("Sigqueue is not supported on " + env.getDisplayName()); // NOI18N
        }

        return result;
    }
}
