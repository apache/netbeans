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
package org.netbeans.modules.php.doctrine2.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.doctrine2.commands.Doctrine2Script;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * Doctrine2 options.
 */
public final class Doctrine2Options {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "doctrine2"; // NOI18N

    private static final Doctrine2Options INSTANCE = new Doctrine2Options();

    // properties
    private static final String SCRIPT = "script"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean scriptSearched = false;


    private Doctrine2Options() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static Doctrine2Options getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getScript() {
        String script = getPreferences().get(SCRIPT, null);
        if (script == null && !scriptSearched) {
            scriptSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(Doctrine2Script.SCRIPT_NAME, Doctrine2Script.SCRIPT_NAME_LONG);
            if (!scripts.isEmpty()) {
                script = scripts.get(0);
                setScript(script);
            }
        }
        return script;
    }

    public void setScript(String script) {
        getPreferences().put(SCRIPT, script);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(Doctrine2Options.class).node(PREFERENCES_PATH);
    }

}
