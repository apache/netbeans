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
package org.netbeans.modules.cnd.modelimpl;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.NbBundle;

/**
 * start/stop csm model support.
 *
 */
public final class Installer {
    @OnStart
    public static final class Start implements Runnable {

        @Override
        public void run() {
            CndUtils.assertNonUiThread();
            if (TraceFlags.TRACE_MODEL_STATE) {
                System.err.println("=== Installer.Start"); // NOI18N
            }
            ModelSupport.instance().startup();
        }
    }

    @OnStop
    public static class Stop implements Runnable, Callable<Boolean> {

        @Override
        public void run() {
            CndUtils.assertNonUiThread();
            final Runnable runnable = new RunnableImpl();
            if (CndUtils.isStandalone() || CndUtils.isUnitTestMode() || !ModelSupport.instance().hasOpenedProjects()) {
                runnable.run();
            } else {
                ProgressUtils.showProgressDialogAndRun(runnable, NbBundle.getMessage(Installer.class, "CLOSE_PROJECT_DIALOG_MESSAGE")); //NOI18N
            }
        }

        @Override
        public Boolean call() throws Exception {
            if (TraceFlags.TRACE_MODEL_STATE) {
                System.err.println("=== Installer.AskStop"); // NOI18N
            }
            ModelSupport.instance().notifyClosing();
            return true;
        }

        private static final class RunnableImpl implements Runnable/*, org.openide.util.Cancellable*/ {

            public RunnableImpl() {
            }

            @Override
            public void run() {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.println("=== Installer.Stop"); // NOI18N
                }
                ModelSupport.instance().shutdown();
            }
        }
    }
}
