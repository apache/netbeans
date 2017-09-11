/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    private static final Set<ProfilingPointReport> openReports = new HashSet();
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
