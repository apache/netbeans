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
package org.netbeans.modules.parsing.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.implspi.ProfilerSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Self profiling Support
 * @author Tomas Zezula
 */
final class SelfProfile {
    private static final Logger LOG = Logger.getLogger(SelfProfile.class.getName());
    
    private final ProfilerSupport profiler;
    private final long time;
    private volatile boolean profiling;

    SelfProfile (long when) {
        time = when;
        
        ProfilerSupport.Factory f = Lookup.getDefault().lookup(ProfilerSupport.Factory.class);
        if (f != null) {
            this.profiler = f.create("taskcancel"); // NOI18N
        } else {
            this.profiler = null;
        }
        this.profiling = true;
    
        LOG.finest("STARTED");  //NOI18N
        if (profiler != null) {
            profiler.start();
            LOG.log(
                Level.FINE,
                "Profiling started {0} at {1}", //NOI18N
                new Object[] {
                    profiler,
                    time
                });
        }
    }

    final synchronized void stop() {
        if (!profiling) {
            return;
        }
        try {
            stopImpl();
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Cannot stop profiling", ex);
        } finally {
            profiling = false;
        }
    }

    private void stopImpl() throws Exception {
        final long now = System.currentTimeMillis();
        long delta = now - time;
        LOG.log(Level.FINE, "Profiling stopped at {0}", now);
        int report = Integer.getInteger("org.netbeans.modules.parsing.api.taskcancel.slowness.report", 1000); // NOI18N
        if (delta < report) {
            LOG.finest("CANCEL");  //NOI18N
            if (profiler != null) {
                profiler.cancel();
                LOG.log(
                    Level.FINE,
                    "Cancel profiling of {0}. Profiling {1}. Time {2} ms.",     //NOI18N
                    new Object[] {
                        profiler,
                        profiling,
                        delta
                    });
            }
            return;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            LOG.finest("LOGGED");  //NOI18N
            if (profiler != null) {
                profiler.stopAndSnapshot(dos);
                LOG.log(
                    Level.FINE,
                    "Obtaining snapshot for {0} ms.",   //NOI18N
                    delta);
            }
            dos.close();
            if (dos.size() > 0) {
                Object[] params = new Object[]{out.toByteArray(), delta, "ParserResultTask-cancel"};    //NOI18N
                Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params); //NOI18N
                LOG.log(Level.FINE, "Snapshot sent to UI logger. Size {0} bytes.", dos.size()); //NOI18N
            } else {
                LOG.log(Level.WARNING, "No snapshot taken"); // NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
