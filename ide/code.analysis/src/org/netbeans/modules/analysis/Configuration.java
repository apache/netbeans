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
package org.netbeans.modules.analysis;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka
 */
public class Configuration {
    
    private String displayName;
    private String id;
    private final Preferences overlayPreferences;
    
    Configuration(String id, String displayName, Preferences overlayPreferences) {
        this.displayName = displayName;
        this.id = id;
        this.overlayPreferences = overlayPreferences;
        Preferences prefs = getPreferences();
        prefs.put("display.name", displayName);
    }
    
    public String getDisplayName() {
        return displayName;
        
    }
    
    public String id() {
        return id;
    }

    public void setDisplayName(String displayName) {
        Preferences oldPrefs = getPreferences();
        oldPrefs.put("display.name", displayName);
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
    
    public final Preferences getPreferences() {
        return overlayPreferences == null ? ConfigurationsManager.getConfigurationsRoot().node(id()) : overlayPreferences;
    }

}
