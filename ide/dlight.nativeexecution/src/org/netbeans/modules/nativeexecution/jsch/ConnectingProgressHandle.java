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
