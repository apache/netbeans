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
package org.netbeans.modules.php.symfony2.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.symfony2.commands.InstallerExecutable;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * Symfony 2/3 options.
 */
public final class SymfonyOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "symfony2"; // NOI18N

    private static final SymfonyOptions INSTANCE = new SymfonyOptions();

    // properties
    static final String INSTALLER = "installer"; // NOI18N
    static final String SANDBOX = "sandbox"; // NOI18N
    private static final String NEW_PROJECT_METHOD = "new.project.method"; // NOI18N
    private static final String IGNORE_CACHE = "ignore.cache"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean installerSearched = false;


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

    @CheckForNull
    public String getInstaller() {
        String path = getPreferences().get(INSTALLER, null);
        if (path == null
                && !installerSearched) {
            installerSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(InstallerExecutable.NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setInstaller(path);
            }
        }
        return path;
    }

    public void setInstaller(String installer) {
        getPreferences().put(INSTALLER, installer);
    }

    public String getSandbox() {
        return getPreferences().get(SANDBOX, null);
    }

    public void setSandbox(String sandbox) {
        getPreferences().put(SANDBOX, sandbox);
    }

    public boolean isUseInstaller() {
        return getPreferences().get(NEW_PROJECT_METHOD, INSTALLER).equals(INSTALLER);
    }

    public void setUseInstaller(boolean useInstaller) {
        getPreferences().put(NEW_PROJECT_METHOD, useInstaller ? INSTALLER : SANDBOX);
    }

    public boolean getIgnoreCache() {
        return getPreferences().getBoolean(IGNORE_CACHE, true);
    }

    public void setIgnoreCache(boolean ignoreCache) {
        getPreferences().putBoolean(IGNORE_CACHE, ignoreCache);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(SymfonyOptions.class).node(PREFERENCES_PATH);
    }

}
