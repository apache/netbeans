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

package org.netbeans.modules.profiler.v2;

import java.util.Properties;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.v2.impl.WeakProcessor;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
public final class SessionStorage {
    
    private static final String SETTINGS_FILENAME = "settings"; // NOI18N
    
    private static final WeakProcessor PROCESSOR = new WeakProcessor("Profiler Storage Processor"); // NOI18N
    
    private boolean dirty;
    private Properties properties;
    private final Lookup.Provider project;
    
    
    SessionStorage(Lookup.Provider project) {
        this.project = project;
    }
    
    
    public synchronized void storeFlag(String flag, String value) {
        if (properties == null) loadProperties();
        
        boolean _dirty;
        if (value != null) _dirty = !value.equals(properties.put(flag, value));
        else _dirty = properties.remove(flag) != null;
        
        dirty |= _dirty;
    }
    
    public synchronized String readFlag(String flag, String defaultValue) {
        if (properties == null) loadProperties();
        
        return properties.getProperty(flag, defaultValue);
    }
    
    
    synchronized void persist(boolean immediately) {
        if (dirty) {
            if (immediately) {
                synchronized(PROCESSOR) { saveProperties(properties); }
            } else {
                final Properties _properties = new Properties();
                for (String key : properties.stringPropertyNames())
                    _properties.setProperty(key, properties.getProperty(key));
                PROCESSOR.post(new Runnable() {
                    public void run() { synchronized(PROCESSOR) { saveProperties(_properties); } }
                });
            }
            dirty = false;
        }
    }
    
    
    private void loadProperties() {
        properties = new Properties();

        assert !SwingUtilities.isEventDispatchThread();
        try {
            ProfilerStorage.loadProjectProperties(properties, project, SETTINGS_FILENAME);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveProperties(Properties _properties) {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            ProfilerStorage.saveProjectProperties(_properties, project, SETTINGS_FILENAME);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
    
}
