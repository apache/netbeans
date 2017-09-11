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
package org.netbeans.modules.nativeexecution.jsch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
//import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * Aid class to avoid multiple ProgressHandles...
 *
 * @author akrasny
 */
public final class ConnectingProgressHandle {

    private static final Object lock = new Object();
    private static final HashMap<ExecutionEnvironment, ProgressHandle> envToHandle = new HashMap<>();
    private static final HashMap<ProgressHandle, List<Cancellable>> phToCancelList = new HashMap<>();

    private ConnectingProgressHandle() {
    }

    public static void startHandle(final ExecutionEnvironment env, Cancellable cancel) {
        ProgressHandle ph;

        synchronized (lock) {
            if (envToHandle.containsKey(env)) {
                ProgressHandle h = envToHandle.get(env);
                phToCancelList.get(h).add(cancel);
                return;
            }

            ph = ProgressHandle.createHandle(
                    NbBundle.getMessage(ConnectingProgressHandle.class, "ConnectingProgressHandle.Connecting", // NOI18N
                    env.toString()), new Cancellable() {

                @Override
                public boolean cancel() {
                    List<Cancellable> cl;

                    synchronized (lock) {
                        ProgressHandle h = envToHandle.get(env);
                        cl = phToCancelList.remove(h);
                        stopHandle(env);
                    }

                    for (Cancellable c : cl) {
                        c.cancel();
                    }

                    return true;
                }
            });

            envToHandle.put(env, ph);
            List<Cancellable> cl = new LinkedList<>();
            cl.add(cancel);
            phToCancelList.put(ph, cl);
        }

        ph.setInitialDelay(500);
        ph.start();
    }

    public static void stopHandle(ExecutionEnvironment env) {
        ProgressHandle ph;

        synchronized (lock) {
            ph = envToHandle.remove(env);
        }

        if (ph != null) {
            ph.finish();
        }
    }
}
