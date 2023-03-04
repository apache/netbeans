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

package org.netbeans.modules.profiler.v2.features;

import javax.swing.JPanel;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.v2.ProfilerFeature;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "MonitorFeature_name=Telemetry",
    "MonitorFeature_description=Monitor CPU and Memory usage, number of threads and loaded classes"
})
final class MonitorFeature extends ProfilerFeature.Basic {
    
    private MonitorFeature(ProfilerSession session) {
        super(Icons.getIcon(ProfilerIcons.MONITORING), Bundle.MonitorFeature_name(),
              Bundle.MonitorFeature_description(), 10, session);
    }
    
    
    // --- Settings ------------------------------------------------------------
    
    public void configureSettings(ProfilingSettings settings) {}
    
    
    // --- Toolbar & Results UI ------------------------------------------------
    
    private MonitorFeatureUI ui;
    
    public JPanel getResultsUI() {
        return getUI().getResultsUI();
    }
    
    public ProfilerToolbar getToolbar() {
        return getUI().getToolbar();
    }
    
    private MonitorFeatureUI getUI() {
        if (ui == null) ui = new MonitorFeatureUI() {
            int getSessionState() {
                return MonitorFeature.this.getSessionState();
            }
            Profiler getProfiler() {
                return MonitorFeature.this.getSession().getProfiler();
            }

            String readFlag(String flag, String defaultValue) {
                return MonitorFeature.this.readFlag("UI_" + flag, defaultValue); // NOI18N
            }

            void storeFlag(String flag, String value) {
                MonitorFeature.this.storeFlag("UI_" + flag, value); // NOI18N
            }
        };
        return ui;
    }
    
    
    // --- Session lifecycle ---------------------------------------------------
    
    public void notifyActivated() {
        getSession().getProfiler().getVMTelemetryManager().reset();
    }
    
    public void notifyDeactivated() {
        getSession().getProfiler().getVMTelemetryManager().reset();
        
        if (ui != null) {
            ui.cleanup();
            ui = null;
        }
    }
    
    
    protected void profilingStateChanged(int oldState, int newState) {
        if (newState == Profiler.PROFILING_STARTED)
            getSession().getProfiler().getVMTelemetryManager().reset();
        
        if (ui != null) ui.sessionStateChanged(getSessionState());
    }
    
    
    // --- Provider ------------------------------------------------------------
    
    @ServiceProvider(service=ProfilerFeature.Provider.class)
    public static final class Provider extends ProfilerFeature.Provider {
        public ProfilerFeature getFeature(ProfilerSession session) {
            return new MonitorFeature(session);
        }
    }
    
}
