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
package org.netbeans.modules.javascript.bower.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.util.FileUtils;
import org.openide.util.NbPreferences;

public final class BowerOptions {

    public static final String BOWER_PATH = "bower.path"; // NOI18N
    public static final String IGNORE_BOWER_COMPONENTS = "ignore.bower_components"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "bower"; // NOI18N

    private static final BowerOptions INSTANCE = new BowerOptions();

    private final Preferences preferences;

    private volatile boolean bowerSearched = false;

    private BowerOptions() {
        preferences = NbPreferences.forModule(BowerOptions.class).node(PREFERENCES_PATH);
    }

    public static BowerOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    @CheckForNull
    public String getBower() {
        String path = preferences.get(BOWER_PATH, null);
        if (path == null
                && !bowerSearched) {
            bowerSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(BowerExecutable.BOWER_NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setBower(path);
            }
        }
        return path;
    }

    public void setBower(String bower) {
        preferences.put(BOWER_PATH, bower);
    }

    public boolean isIgnoreBowerComponents() {
        return preferences.getBoolean(IGNORE_BOWER_COMPONENTS, true);
    }

    public void setIgnoreBowerComponents(boolean ignoreBowerComponents) {
        preferences.putBoolean(IGNORE_BOWER_COMPONENTS, ignoreBowerComponents);
    }

}
