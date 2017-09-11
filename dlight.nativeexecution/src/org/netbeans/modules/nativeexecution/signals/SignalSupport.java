/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
