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

package org.netbeans.modules.apisupport.project.ui;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage for settings used by a module's UI (wizards, properties, ...)
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class ModuleUISettings {

    private static final String LAST_CHOSEN_LIBRARY_LOCATION = "lastChosenLibraryLocation"; // NOI18N
    private static final String LAST_USED_NB_PLATFORM_LOCATION = "lastUsedNbPlatformLocation"; // NOI18N
    private static final String NEW_MODULE_COUNTER = "newModuleCounter";  //NOI18N
    private static final String NEW_SUITE_COUNTER = "newSuiteCounter";  //NOI18N
    private static final String NEW_APPLICATION_COUNTER = "newApplicationCounter";  //NOI18N
    private static final String CONFIRM_RELOAD_IN_IDE = "confirmReloadInIDE"; // NOI18N
    private static final String LAST_USED_PLATFORM_ID = "lastUsedPlatformID"; // NOI18N
    private static final String HARNESSES_UPGRADED = "harnessesUpgraded"; // NOI18N
    private static final String LAST_USED_CLUSTER_LOCATION = "lastUsedClusterLocation";    // NOI18N

    public static ModuleUISettings getDefault() {
        return new ModuleUISettings(); // stateless
    }

    private Preferences prefs() {
        return NbPreferences.forModule(ModuleUISettings.class);
    }

    public int getNewModuleCounter() {
        return prefs().getInt(NEW_MODULE_COUNTER, 0);
    }

    public void setNewModuleCounter(int count) {
        prefs().putInt(NEW_MODULE_COUNTER, count);
    }

    public int getNewSuiteCounter() {
        return prefs().getInt(NEW_SUITE_COUNTER, 0);
    }

    public void setNewSuiteCounter(int count) {
        prefs().putInt(NEW_SUITE_COUNTER, count);
    }
    
    public int getNewApplicationCounter() {
        return prefs().getInt(NEW_APPLICATION_COUNTER, 0);
    }

    public void setNewApplicationCounter(int count) {
        prefs().putInt(NEW_APPLICATION_COUNTER, count);
    }

    public String getLastUsedNbPlatformLocation() {
        return prefs().get(LAST_USED_NB_PLATFORM_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastUsedNbPlatformLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_USED_NB_PLATFORM_LOCATION, location);
    }

    public String getLastUsedClusterLocation() {
        return prefs().get(LAST_USED_CLUSTER_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastUsedClusterLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_USED_CLUSTER_LOCATION, location);
    }

    public boolean getConfirmReloadInIDE() {
        return prefs().getBoolean(CONFIRM_RELOAD_IN_IDE, true);
    }

    public void setConfirmReloadInIDE(boolean b) {
        prefs().putBoolean(CONFIRM_RELOAD_IN_IDE, b);
    }

    public String getLastChosenLibraryLocation() {
        return prefs().get(LAST_CHOSEN_LIBRARY_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastChosenLibraryLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_CHOSEN_LIBRARY_LOCATION, location);
    }

    public String getLastUsedPlatformID() {
        return prefs().get(LAST_USED_PLATFORM_ID, "default"); // NOI18N
    }

    public void setLastUsedPlatformID(String id) {
        assert id != null : "Platform ID can not be null"; // NOI18N
        prefs().put(LAST_USED_PLATFORM_ID, id);
    }

    public boolean getHarnessesUpgraded() {
        return prefs().getBoolean(HARNESSES_UPGRADED, false);
    }

    public void setHarnessesUpgraded(boolean b) {
        prefs().putBoolean(HARNESSES_UPGRADED, b);
    }

}
