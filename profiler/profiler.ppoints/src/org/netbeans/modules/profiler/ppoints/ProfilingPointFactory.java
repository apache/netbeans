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

import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.openide.ErrorManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import javax.swing.Icon;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsIcons;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilingPointFactory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final String AVAILABILITY_PROPERTY = ProfilingPointFactory.class.getName() + "#AVAILABILITY"; // NOI18N
    private static final String PROFILING_POINT_STORAGE_EXT = "pp"; // NOI18N
    public static final int SCOPE_CODE = 1; // Scope of the Profiling Point: Code (see CodeProfilingPoint)
    public static final int SCOPE_GLOBAL = 2; // Scope of the Profiling Point: Global (see GlobalProfilingPoint)
    public static final Icon SCOPE_CODE_ICON = Icons.getIcon(ProfilingPointsIcons.CODE);
    public static final Icon SCOPE_GLOBAL_ICON = Icons.getIcon(ProfilingPointsIcons.GLOBAL);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ValidityAwarePanel customizer = null;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public boolean isAvailable() {
        return true;
    } // subclasses will override this method if they eg. depend on external modules

    public abstract String getDescription();
    
    public String getHint() { return null; };

    public abstract Icon getIcon();
    
    public abstract Icon getDisabledIcon();

    // Defines scope of the Profiling Point: Code or Global (see CodeProfilingPoint or GlobalProfilingPoint)
    public abstract int getScope();

    public abstract String getType();

    public ProfilingPoint create() {
        return create(null);
    }

    public abstract ProfilingPoint create(Lookup.Provider project);

    public abstract boolean supportsCPU();

    public abstract boolean supportsMemory();

    // Support for each profiling type
    public abstract boolean supportsMonitor();

    public Icon getScopeIcon() {
        switch (getScope()) {
            case SCOPE_CODE:
                return SCOPE_CODE_ICON;
            case SCOPE_GLOBAL:
                return SCOPE_GLOBAL_ICON;
            default:
                return null;
        }
    }

    protected abstract Class getProfilingPointsClass();

    protected abstract ValidityAwarePanel createCustomizer(); // Creates an instance of ValidityAwarePanel (called once)

    protected abstract ProfilingPoint loadProfilingPoint(Lookup.Provider project, Properties properties, int index);

    protected abstract void storeProfilingPoint(ProfilingPoint profilingPoint, int index, Properties properties);

    protected void firePropertyChange(PropertyChangeEvent event) {
        pcs.firePropertyChange(event);
    }

    ValidityAwarePanel getCustomizer() {
        if (customizer == null) {
            customizer = createCustomizer(); // Created new customizer (shared instance)
        }

        ValidityAwarePanel safeCustomizer = ProfilingPointsManager.getDefault().safeGetCustomizer(customizer); // Check if any customizer is already showing

        return safeCustomizer;
    }

    ProfilingPoint[] loadProfilingPoints(Lookup.Provider project)
                                  throws IOException, InvalidPropertiesFormatException {
        Properties properties = new Properties();
        ProfilerStorage.loadProjectProperties(properties, project, getProfilingPointsStorage());

        int index = 0;
        List<ProfilingPoint> profilingPoints = new ArrayList<>();
        while (properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_NAME) != null) { // NOI18N
            ProfilingPoint profilingPoint = loadProfilingPoint(project, properties, index);

            if (profilingPoint != null) {
                profilingPoints.add(profilingPoint);
            } else {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "Invalid " + getType() + // NOI18N
                                             " Profiling Point format at index " + index +  // NOI18N
                                             " in project " + ProjectUtilities.getDisplayName(project)); // NOI18N
            }

            index++;
        }

        return profilingPoints.toArray(new ProfilingPoint[0]);
    }

    void saveProfilingPoints(Lookup.Provider project) throws IOException {
        List<ProfilingPoint> profilingPoints = ProfilingPointsManager.getDefault().getProfilingPoints(getProfilingPointsClass(), project, false);
        saveProfilingPoints(profilingPoints.toArray(new ProfilingPoint[0]), project);
    }
    
    private void saveProfilingPoints(ProfilingPoint[] profilingPoints, Lookup.Provider project)
                              throws IOException {
        String storage = getProfilingPointsStorage();
        if (profilingPoints.length > 0) {
                Properties properties = new Properties();
                for (int i = 0; i < profilingPoints.length; i++)
                    storeProfilingPoint(profilingPoints[i], i, properties);
                ProfilerStorage.saveProjectProperties(properties, project, storage);
        } else {
            ProfilerStorage.deleteProjectProperties(project, storage);
        }
    }
    
    private String getProfilingPointsStorage() {
        String fullClass = getProfilingPointsClass().getName();
        return fullClass.substring(fullClass.lastIndexOf('.') + 1); // NOI18N
    }
    
}
