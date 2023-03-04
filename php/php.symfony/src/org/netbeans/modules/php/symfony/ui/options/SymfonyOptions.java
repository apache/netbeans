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

package org.netbeans.modules.php.symfony.ui.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class SymfonyOptions {
    public static final String DEFAULT_SECRET = "UniqueSecret"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "symfony"; // NOI18N

    private static final SymfonyOptions INSTANCE = new SymfonyOptions();

    // symfony script
    private static final String SYMFONY = "symfony"; // NOI18N
    // ignore cache
    private static final String IGNORE_CACHE = "ignore.cache"; // NOI18N
    // default params
    private static final String PARAMS_FOR_PROJECT = "default.params.project"; // NOI18N
    private static final String PARAMS_FOR_APPS = "default.params.apps"; // NOI18N
    private static final String DEFAULT_PARAMS_FOR_APPS = "--escaping-strategy=on --csrf-secret=" + DEFAULT_SECRET; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean symfonySearched = false;

    private SymfonyOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static SymfonyOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public synchronized String getSymfony() {
        String symfony = getPreferences().get(SYMFONY, null);
        if (symfony == null && !symfonySearched) {
            symfonySearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(SymfonyScript.SCRIPT_NAME, SymfonyScript.SCRIPT_NAME_LONG);
            if (!scripts.isEmpty()) {
                symfony = scripts.get(0);
                setSymfony(symfony);
            }
        }
        return symfony;
    }

    public void setSymfony(String symfony) {
        getPreferences().put(SYMFONY, symfony);
    }

    public boolean getIgnoreCache() {
        return getPreferences().getBoolean(IGNORE_CACHE, true);
    }

    public void setIgnoreCache(boolean ignoreCache) {
        getPreferences().putBoolean(IGNORE_CACHE, ignoreCache);
    }

    public String getDefaultParamsForProject() {
        return getPreferences().get(PARAMS_FOR_PROJECT, ""); // NOI18N
    }

    public void setDefaultParamsForProject(String params) {
        getPreferences().put(PARAMS_FOR_PROJECT, params);
    }

    public String getDefaultParamsForApps() {
        return getPreferences().get(PARAMS_FOR_APPS, DEFAULT_PARAMS_FOR_APPS);
    }

    public void setDefaultParamsForApps(String params) {
        getPreferences().put(PARAMS_FOR_APPS, params);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(SymfonyOptions.class).node(PREFERENCES_PATH);
    }
}
