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

import org.netbeans.modules.profiler.ppoints.ui.LoadGeneratorCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.Icon;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsIcons;


/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "LoadGenProfilingPointFactory_PpType=Load Generator",
    "LoadGenProfilingPointFactory_PpDescr=Starts and stops a load generator script at the given source code location",
    "LoadGenProfilingPointFactory_PpDefaultName={0} at {1}:{2}"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.ppoints.ProfilingPointFactory.class)
public class LoadGenProfilingPointFactory extends CodeProfilingPointFactory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String START_LOCATION_PREFIX = "start_"; // NOI18N
    private static final String END_LOCATION_PREFIX = "end_"; // NOI18N
    private static LoadGenProfilingPointFactory defaultInstance = null;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Lookup.Result loadGenResult;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent lookupEvent) {
            available = ((Lookup.Result) lookupEvent.getSource()).allInstances().size() > 0;
            firePropertyChange(new PropertyChangeEvent(LoadGenProfilingPointFactory.this,
                                                       ProfilingPointFactory.AVAILABILITY_PROPERTY, false, true));
        }
    };

    private boolean available = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LoadGenProfilingPointFactory() {
        loadGenResult = Lookup.getDefault().lookupResult(LoadGenPlugin.class);
        loadGenResult.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, loadGenResult));
        available = loadGenResult.allInstances().size() > 0;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isAvailable() {
        return available;
    }

    public String getDescription() {
        return Bundle.LoadGenProfilingPointFactory_PpDescr();
    }

    public Icon getIcon() {
        return Icons.getIcon(ProfilingPointsIcons.LOAD_GENERATOR);
    }
    
    public Icon getDisabledIcon() {
        return Icons.getIcon(ProfilingPointsIcons.LOAD_GENERATOR_DISABLED);
    }

    public int getScope() {
        return SCOPE_CODE;
    }

    public String getType() {
        return Bundle.LoadGenProfilingPointFactory_PpType();
    }

    public ProfilingPoint create(Lookup.Provider project) {
        if (project == null) {
            project = Utils.getCurrentProject(); // project not defined, will be detected from most active Editor or Main Project will be used
        }

        CodeProfilingPoint.Location[] selectionLocations = Utils.getCurrentSelectionLocations();

        if (selectionLocations.length != 2) {
            CodeProfilingPoint.Location location = Utils.getCurrentLocation(CodeProfilingPoint.Location.OFFSET_START);

            if (location.equals(CodeProfilingPoint.Location.EMPTY)) {
                String filename = ""; // NOI18N
                String name = Utils.getUniqueName(getType(), "", project); // NOI18N

                return new LoadGenProfilingPoint(name, location, null, project, this);
            } else {
                File file = FileUtil.normalizeFile(new File(location.getFile()));
                String filename = FileUtil.toFileObject(file).getName();
                String name = Utils.getUniqueName(getType(),
                                                  Bundle.LoadGenProfilingPointFactory_PpDefaultName("", filename, location.getLine()), project);  // NOI18N

                return new LoadGenProfilingPoint(name, location, null, project, this);
            }
        } else {
            CodeProfilingPoint.Location startLocation = selectionLocations[0];
            CodeProfilingPoint.Location endLocation = selectionLocations[1];
            File file = FileUtil.normalizeFile(new File(startLocation.getFile()));
            String filename = FileUtil.toFileObject(file).getName();
            String name = Utils.getUniqueName(getType(),
                                              Bundle.LoadGenProfilingPointFactory_PpDefaultName("", filename, startLocation.getLine()),  // NOI18N
                                              project);

            return new LoadGenProfilingPoint(name, startLocation, endLocation, project, this);
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
        return LoadGenProfilingPoint.class;
    }

    protected String getServerHandlerClassName() {
        return "org.netbeans.lib.profiler.server.ProfilingPointServerHandler"; // NOI18N
    }

    protected ValidityAwarePanel createCustomizer() {
        return new LoadGeneratorCustomizer(getType(), getIcon());
    }

    protected ProfilingPoint loadProfilingPoint(Lookup.Provider project, Properties properties, int index) {
        String name = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_NAME, null); // NOI18N
        String enabledStr = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_ENABLED, null); // NOI18N
        String scriptFile = properties.getProperty(index + "_" + LoadGenProfilingPoint.PROPERTY_SCRIPTNAME, null); // NOI18N
        CodeProfilingPoint.Location startLocation = CodeProfilingPoint.Location.load(project, index, START_LOCATION_PREFIX,
                                                                                     properties);
        CodeProfilingPoint.Location endLocation = CodeProfilingPoint.Location.load(project, index, END_LOCATION_PREFIX, properties);

        if ((name == null) || (enabledStr == null) || (startLocation == null)) {
            return null;
        }

        LoadGenProfilingPoint profilingPoint = null;

        try {
            profilingPoint = new LoadGenProfilingPoint(name, startLocation, endLocation, project, this);
            profilingPoint.setEnabled(Boolean.parseBoolean(enabledStr));
            profilingPoint.setSriptFileName(scriptFile);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }

        return profilingPoint;
    }

    protected void storeProfilingPoint(ProfilingPoint profilingPoint, int index, Properties properties) {
        LoadGenProfilingPoint loadgen = (LoadGenProfilingPoint) profilingPoint;
        properties.put(index + "_" + ProfilingPoint.PROPERTY_NAME, loadgen.getName()); // NOI18N
        properties.put(index + "_" + ProfilingPoint.PROPERTY_ENABLED, Boolean.toString(loadgen.isEnabled())); // NOI18N
        properties.put(index + "_" + LoadGenProfilingPoint.PROPERTY_SCRIPTNAME, loadgen.getScriptFileName()); // NOI18N
        loadgen.getStartLocation().store(loadgen.getProject(), index, START_LOCATION_PREFIX, properties);

        if (loadgen.usesEndLocation()) {
            loadgen.getEndLocation().store(loadgen.getProject(), index, END_LOCATION_PREFIX, properties);
        }
    }
}
