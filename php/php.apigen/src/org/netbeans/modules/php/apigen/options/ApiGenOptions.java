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
package org.netbeans.modules.php.apigen.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * ApiGen options.
 */
public final class ApiGenOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "apigen"; // NOI18N

    private static final ApiGenOptions INSTANCE = new ApiGenOptions();

    // apigen script
    private static final String APIGEN = "apigen.path"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean apigenSearched = false;


    private ApiGenOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static ApiGenOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getApiGen() {
        String apiGen = getPreferences().get(APIGEN, null);
        if (apiGen == null && !apigenSearched) {
            apigenSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(ApiGenScript.SCRIPT_NAME, ApiGenScript.SCRIPT_NAME_LONG);
            if (!scripts.isEmpty()) {
                apiGen = scripts.get(0);
                setApiGen(apiGen);
            }
        }
        return apiGen;
    }

    public void setApiGen(String apiGen) {
        getPreferences().put(APIGEN, apiGen);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(ApiGenOptions.class).node(PREFERENCES_PATH);
    }

}
