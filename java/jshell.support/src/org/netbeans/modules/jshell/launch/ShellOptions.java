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
package org.netbeans.modules.jshell.launch;

import java.util.prefs.Preferences;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbPreferences;

/**
 *
 * @author sdedic
 */
public final class ShellOptions {
    public static final SpecificationVersion MIN_SPEC_VERSION = new SpecificationVersion("1.6"); // NOI18N
    
    private static final String PREF_PLATFORM = "launchPlatformId"; // NOI18N
    private static final String PREF_OPEN_CONSOLE = "openConsoleOnLaunch"; // NOI18N
    private static final String PREF_REUSE_CONSOLES = "reuseDeadConsoles"; // NOI18N
    private static final String PREF_HISTORY_LINES = "historyLines"; // NOI18N
    private Preferences prefs;
    
    private Preferences prefs() {
        if (prefs != null) {
            return prefs;
        }
        prefs = NbPreferences.forModule(ShellOptions.class);
        return prefs;
    }
    
    public int getHistoryLines() {
        return prefs().getInt(PREF_HISTORY_LINES, 50);
    }
    
    public void setHistoryLines(int val) {
        prefs().putInt(PREF_HISTORY_LINES, val);
    }
    
    public void setOpenConsole(boolean open) {
        prefs().putBoolean(PREF_OPEN_CONSOLE, open);
    }
    
    public void setReuseDeadConsoles(boolean reuse) {
        prefs().putBoolean(PREF_REUSE_CONSOLES, reuse);
    }
    
    public boolean isOpenConsole() {
        return prefs().getBoolean(PREF_OPEN_CONSOLE, false);
    }
    
    public boolean isReuseDeadConsoles() {
        return prefs().getBoolean(PREF_REUSE_CONSOLES, true);
    }
    
    public boolean isPlatformSet() {
        return prefs().get(PREF_PLATFORM, null) != null;
    }
    
    public void setSelectedPlatform(JavaPlatform p) {
        prefs().put(PREF_PLATFORM, p.getDisplayName());
    }

    public boolean setSelectedPlatform(String name) {
        JavaPlatform[] candidates;
        candidates = JavaPlatformManager.getDefault().getPlatforms(name, null);
        if (candidates != null && candidates.length == 1) {
            prefs().put(PREF_PLATFORM, name);
            return true;
        } else {
            return false;
        }
    }
    
    public JavaPlatform getSelectedPlatform() {
        String platformId = prefs().get(PREF_PLATFORM, null);
        if (platformId == null) {
            return getDefaultPlatform();
        }
        JavaPlatform[] candidates;
        candidates = JavaPlatformManager.getDefault().getPlatforms(platformId, null);
        if (candidates == null || candidates.length == 0) {
            return getDefaultPlatform();
        } else {
            return candidates[0];
        }
    }
    
    public JavaPlatform getDefaultPlatform() {
        JavaPlatform check = JavaPlatform.getDefault();
        if (check.getSpecification().getVersion().compareTo(MIN_SPEC_VERSION) >= 0) {
            return check;
        }
        JavaPlatform[] candidates = JavaPlatformManager.getDefault().getPlatforms(null, 
            new Specification("J2SE", MIN_SPEC_VERSION));
        return candidates == null || candidates.length == 0 ? null : candidates[0];
    }
    
    public static ShellOptions get() {
        return new ShellOptions();
    }
    
    public boolean accepts(JavaPlatform p) {
        return p.getSpecification().getVersion().compareTo(MIN_SPEC_VERSION) >= 0;
    }
}
