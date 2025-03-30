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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.modules.profiler.ppoints.ui.TriggeredTakeSnapshotCustomizer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import java.util.Properties;
import javax.swing.Icon;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsIcons;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "TriggeredTakeSnapshotProfilingPointFactory_PpType=Triggered Take Snapshot",
    "TriggeredTakeSnapshotProfilingPointFactory_PpDescr=Takes snapshot of currently collected profiling results similarly to Take Snapshot action in Profiler UI. This Profiling Point is defined globally for the profiling session and is invoked when defined condition is met during profiling.",
    "TriggeredTakeSnapshotProfilingPointFactory_PpHint=", // #207680 Do not remove, custom brandings may provide wizard hint here!!!
    "TriggeredTakeSnapshotProfilingPointFactory_PpDefaultName={0} in {1}"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.ppoints.ProfilingPointFactory.class)
public class TriggeredTakeSnapshotProfilingPointFactory extends CodeProfilingPointFactory {    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getDescription() {
        return Bundle.TriggeredTakeSnapshotProfilingPointFactory_PpDescr();
    }
    
    public String getHint() {
        return Bundle.TriggeredTakeSnapshotProfilingPointFactory_PpHint();
    }

    public Icon getIcon() {
        return Icons.getIcon(ProfilingPointsIcons.TAKE_SNAPSHOT_TRIGGERED);
    }
    
    public Icon getDisabledIcon() {
        return ImageUtilities.createDisabledIcon(getIcon());
    }

    public int getScope() {
        return SCOPE_GLOBAL;
    }

    public String getType() {
        return Bundle.TriggeredTakeSnapshotProfilingPointFactory_PpType();
    }

    public TriggeredTakeSnapshotProfilingPoint create(Lookup.Provider project) {
        if (project == null) {
            project = Utils.getCurrentProject(); // project not defined, will be detected from most active Editor or Main Project will be used
        }

        String name = Utils.getUniqueName(getType(),
                                          Bundle.TriggeredTakeSnapshotProfilingPointFactory_PpDefaultName(
                                                "", ProjectUtilities.getDisplayName(project)), // NOI18N
                                          project);

        return new TriggeredTakeSnapshotProfilingPoint(name, project, this);
    }

    public boolean supportsCPU() {
        return true;
    }

    public boolean supportsMemory() {
        return true;
    }

    public boolean supportsMonitor() {
        return true;
    }

    protected Class getProfilingPointsClass() {
        return TriggeredTakeSnapshotProfilingPoint.class;
    }

    protected String getServerHandlerClassName() {
        throw new UnsupportedOperationException();
    }

    protected TriggeredTakeSnapshotCustomizer createCustomizer() {
        return new TriggeredTakeSnapshotCustomizer(getType(), getIcon());
    }

    protected ProfilingPoint loadProfilingPoint(Lookup.Provider project, Properties properties, int index) {
        String name = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_NAME, null); // NOI18N
        String enabledStr = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_ENABLED, null); // NOI18N
        String type = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TYPE, null); // NOI18N
        String target = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TARGET, null); // NOI18N
        String file = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_CUSTOM_FILE, null); // NOI18N
        String resetResultsStr = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_RESET_RESULTS,
                                                        null); // NOI18N
        TriggeredGlobalProfilingPoint.TriggerCondition condition = TriggeredGlobalProfilingPoint.TriggerCondition.load(project,
                                                                                                                       index,
                                                                                                                       properties);

        if ((name == null) || (enabledStr == null) || (condition == null) || (type == null) || (target == null) || (file == null)
                || (resetResultsStr == null)) {
            return null;
        }

        TriggeredTakeSnapshotProfilingPoint profilingPoint = null;

        try {
            profilingPoint = new TriggeredTakeSnapshotProfilingPoint(name, project, this);
            profilingPoint.setEnabled(Boolean.parseBoolean(enabledStr));
            profilingPoint.setSnapshotType(type);
            profilingPoint.setSnapshotTarget(target);
            profilingPoint.setSnapshotFile(file);
            profilingPoint.setResetResults(Boolean.parseBoolean(resetResultsStr));
            profilingPoint.setCondition(condition);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }

        return profilingPoint;
    }

    protected void storeProfilingPoint(ProfilingPoint profilingPoint, int index, Properties properties) {
        TriggeredTakeSnapshotProfilingPoint takeSnapshot = (TriggeredTakeSnapshotProfilingPoint) profilingPoint;
        properties.put(index + "_" + ProfilingPoint.PROPERTY_NAME, takeSnapshot.getName()); // NOI18N
        properties.put(index + "_" + ProfilingPoint.PROPERTY_ENABLED, Boolean.toString(takeSnapshot.isEnabled())); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TYPE, takeSnapshot.getSnapshotType()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TARGET, takeSnapshot.getSnapshotTarget()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_CUSTOM_FILE,
                       (takeSnapshot.getSnapshotFile() == null) ? "" : takeSnapshot.getSnapshotFile()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_RESET_RESULTS,
                       Boolean.toString(takeSnapshot.getResetResults())); // NOI18N
        takeSnapshot.getCondition().store(takeSnapshot.getProject(), index, properties);
    }
}
