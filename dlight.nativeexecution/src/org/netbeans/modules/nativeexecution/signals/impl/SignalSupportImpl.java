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
