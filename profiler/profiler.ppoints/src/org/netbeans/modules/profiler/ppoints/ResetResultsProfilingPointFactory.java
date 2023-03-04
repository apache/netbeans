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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.modules.profiler.ppoints.ui.ResetResultsCustomizer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.Icon;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsIcons;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ResetResultsProfilingPointFactory_PpType=Reset Results",
    "ResetResultsProfilingPointFactory_PpDescr=Resets currently collected profiling results similarly to Reset Results action in Profiler UI. You may use this Profiling Point for collecting results deltas when combined with Take Snapshot Profiling Point.",
    "ResetResultsProfilingPointFactory_PpHint=", // #207680 Do not remove, custom brandings may provide wizard hint here!!!
//# Reset Results at Anagrams.java:32
    "ResetResultsProfilingPointFactory_PpDefaultName={0} at {1}:{2}"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.ppoints.ProfilingPointFactory.class)
public class ResetResultsProfilingPointFactory extends CodeProfilingPointFactory {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getDescription() {
        return Bundle.ResetResultsProfilingPointFactory_PpDescr();
    }
    
    public String getHint() {
        return Bundle.ResetResultsProfilingPointFactory_PpHint();
    }

    public Icon getIcon() {
        return Icons.getIcon(ProfilingPointsIcons.RESET_RESULTS);
    }
    
    public Icon getDisabledIcon() {
        return Icons.getIcon(ProfilingPointsIcons.RESET_RESULTS_DISABLED);
    }

    public int getScope() {
        return SCOPE_CODE;
    }

    public String getType() {
        return Bundle.ResetResultsProfilingPointFactory_PpType();
    }

    public ResetResultsProfilingPoint create(Lookup.Provider project) {
        if (project == null) {
            project = Utils.getCurrentProject(); // project not defined, will be detected from most active Editor or Main Project will be used
        }

        CodeProfilingPoint.Location location = Utils.getCurrentLocation(CodeProfilingPoint.Location.OFFSET_START);

        if (location.equals(CodeProfilingPoint.Location.EMPTY)) {
            String filename = ""; // NOI18N
            String name = Utils.getUniqueName(getType(), "", project); // NOI18N

            return new ResetResultsProfilingPoint(name, location, project, this);
        } else {
            File file = FileUtil.normalizeFile(new File(location.getFile()));
            String filename = FileUtil.toFileObject(file).getName();
            String name = Utils.getUniqueName(getType(),
                                              Bundle.ResetResultsProfilingPointFactory_PpDefaultName("", filename, location.getLine()),  // NOI18N
                                              project);

            return new ResetResultsProfilingPoint(name, location, project, this);
        }
    }

    public boolean supportsCPU() {
        return true;
    }

    public boolean supportsMemory() {
        return true;
    }

    public boolean supportsMonitor() {
        return false;
    }

    protected Class getProfilingPointsClass() {
        return ResetResultsProfilingPoint.class;
    }

    protected String getServerHandlerClassName() {
        return "org.netbeans.lib.profiler.server.ResetResultsProfilingPointHandler"; // NOI18N
    }

    protected ResetResultsCustomizer createCustomizer() {
        return new ResetResultsCustomizer(getType(), getIcon());
    }

    protected ProfilingPoint loadProfilingPoint(Lookup.Provider project, Properties properties, int index) {
        String name = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_NAME, null); // NOI18N
        String enabledStr = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_ENABLED, null); // NOI18N
        CodeProfilingPoint.Location location = CodeProfilingPoint.Location.load(project, index, properties);

        if ((name == null) || (enabledStr == null) || (location == null)) {
            return null;
        }

        ResetResultsProfilingPoint profilingPoint = null;

        try {
            profilingPoint = new ResetResultsProfilingPoint(name, location, project, this);
            profilingPoint.setEnabled(Boolean.parseBoolean(enabledStr));
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }

        return profilingPoint;
    }

    protected void storeProfilingPoint(ProfilingPoint profilingPoint, int index, Properties properties) {
        ResetResultsProfilingPoint resetResults = (ResetResultsProfilingPoint) profilingPoint;
        properties.put(index + "_" + ProfilingPoint.PROPERTY_NAME, resetResults.getName()); // NOI18N
        properties.put(index + "_" + ProfilingPoint.PROPERTY_ENABLED, Boolean.toString(resetResults.isEnabled())); // NOI18N
        resetResults.getLocation().store(resetResults.getProject(), index, properties);
    }
}
