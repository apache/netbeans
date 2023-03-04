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
package org.netbeans.modules.profiler.ppoints.ui;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.modules.profiler.ProfilerTopComponent;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
        "ProfilingPointReport_NoHitsString=no hits", // NOI18N
        "ProfilingPointReport_NoMonitorString=Monitor mode not supported by this profiling point.", // NOI18N
        "ProfilingPointReport_NoSampledCpuString=Sampled Methods profiling not supported by this profiling point. Switch to instrumented profiling to start collecting data.", // NOI18N
        "ProfilingPointReport_NoCpuString=Methods profiling not supported by this profiling point.", // NOI18N
        "ProfilingPointReport_NoMemoryString=Objects profiling not supported by this profiling point.", // NOI18N
        "ProfilingPointReport_NoCurrentString=Current profiling mode not supported by this profiling point." // NOI18N
    })
public abstract class ProfilingPointReport extends ProfilerTopComponent {
    private static final String HELP_CTX_KEY = "ProfilingPointReport.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    
    private static final ProfilingSettings REF_CPU_INSTR =
            ProfilingSettingsPresets.createCPUPreset(ProfilingSettings.PROFILE_CPU_ENTIRE);
    
    private static final Set<ProfilingPointReport> openReports = new HashSet<>();
    private static boolean profilingRunning;
    private static ProfilingSettings currentSettings;
    
    
    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }
    
    public final int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected final String preferredID() {
        return this.getClass().getName();
    }
    
    protected abstract void refresh();
    
    
    public static String getNoDataHint(ProfilingPoint profilingPoint) {
        if (!profilingRunning || profilingPoint.supportsProfilingSettings(currentSettings))
                return "&nbsp;&nbsp;&lt;" + Bundle.ProfilingPointReport_NoHitsString() + "&gt;"; // NOI18N
        if (ProfilingSettings.isMonitorSettings(currentSettings)) {
            return "&nbsp;&nbsp;" + Bundle.ProfilingPointReport_NoMonitorString(); // NOI18N
        } else if (ProfilingSettings.isCPUSettings(currentSettings)) {
            if (!profilingPoint.supportsProfilingSettings(REF_CPU_INSTR))
                return "&nbsp;&nbsp;" + Bundle.ProfilingPointReport_NoCpuString(); // NOI18N
            else
                return "&nbsp;&nbsp;" + Bundle.ProfilingPointReport_NoSampledCpuString(); // NOI18N
        } else if (ProfilingSettings.isMemorySettings(currentSettings)) {
            return "&nbsp;&nbsp;" + Bundle.ProfilingPointReport_NoMemoryString(); // NOI18N
        } else {
            return "&nbsp;&nbsp;" + Bundle.ProfilingPointReport_NoCurrentString(); // NOI18N
        }
    }
    
    
    protected void componentOpened() {
        super.componentOpened();
        openReports.add(this);
    }
    
    protected void componentClosed() {
        openReports.remove(this);
        super.componentClosed();
    }
    
    
    public static void refreshOpenReports() {
        Profiler profiler = Profiler.getDefault();
        profilingRunning = profiler.profilingInProgress();
        currentSettings = profiler.getLastProfilingSettings();
        
        if (profiler.getProfilingState() != Profiler.PROFILING_RUNNING &&
            profiler.getProfilingState() != Profiler.PROFILING_INACTIVE) return;
        
        for (ProfilingPointReport report : openReports)
            report.refresh();
    }
    
}
