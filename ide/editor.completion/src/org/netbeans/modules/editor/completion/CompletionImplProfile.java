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
package org.netbeans.modules.editor.completion;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.sampler.Sampler;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

final class CompletionImplProfile {
    private static final Logger LOG = Logger.getLogger(CompletionImplProfile.class.getName());
    static final RequestProcessor RP = new RequestProcessor("Completion Slowness"); // NOI18N

    private final Sampler profiler;
    private boolean profiling;
    private final long time;

    CompletionImplProfile(long when) {
        time = when;

        this.profiler = Sampler.createSampler("completion");  // NOI18N
        this.profiling = true;
        
        if (profiler != null) {
            profiler.start();
            LOG.log(Level.FINE, "Profiling started {0} at {1}", new Object[] { profiler, time });
        }
    }

    final synchronized void stop() {
        if (!profiling) {
            return;
        }
        final long now = System.currentTimeMillis();
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    stopImpl(now);
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "Cannot stop profiling", ex);
                }
            }
        });
        profiling = false;
    }

    private void stopImpl(long now) throws Exception {
        long delta = now - time;
        LOG.log(Level.FINE, "Profiling stopped at {0}", now);
        int report = Integer.getInteger("org.netbeans.modules.editor.completion.slowness.report", 2000); // NOI18N
        if (delta < report) {
            LOG.log(Level.FINE, "Cancel profiling of {0}. Profiling {1}. Time {2} ms.", new Object[] { profiler, profiling, delta });
            if (profiler != null) {
                profiler.cancel();
            }
            return;
        }
        try {
            LOG.log(Level.FINE, "Obtaining snapshot for {0} ms.", delta);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            if (profiler != null) {
                profiler.stopAndWriteTo(dos);
            }
            dos.close();
            if (dos.size() > 0) {
                Object[] params = new Object[]{out.toByteArray(), delta, "CodeCompletion"};
                Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params);
                LOG.log(Level.FINE, "Snapshot sent to UI logger. Size {0} bytes.", dos.size());
            } else {
                LOG.log(Level.WARNING, "No snapshot taken"); // NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
