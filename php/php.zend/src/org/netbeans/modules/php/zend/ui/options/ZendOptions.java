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

package org.netbeans.modules.php.zend.ui.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.zend.ZendScript;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class ZendOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "zend"; // NOI18N

    private static final ZendOptions INSTANCE = new ZendOptions();

    // zend script
    private static final String ZEND = "zend"; // NOI18N
    // default params
    private static final String PARAMS_FOR_PROJECT = "default.params.project"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean zendSearched = false;

    private ZendOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static ZendOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public synchronized String getZend() {
        String zend = getPreferences().get(ZEND, null);
        if (zend == null && !zendSearched) {
            zendSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(ZendScript.SCRIPT_NAME, ZendScript.SCRIPT_NAME_LONG);
            if (!scripts.isEmpty()) {
                zend = scripts.get(0);
                setZend(zend);
            }
        }
        return zend;
    }

    public void setZend(String zend) {
        getPreferences().put(ZEND, zend);
    }

    public String getDefaultParamsForProject() {
        return getPreferences().get(PARAMS_FOR_PROJECT, ""); // NOI18N
    }

    public void setDefaultParamsForProject(String params) {
        getPreferences().put(PARAMS_FOR_PROJECT, params);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(ZendOptions.class).node(PREFERENCES_PATH);
    }
}
