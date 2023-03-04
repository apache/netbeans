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
package org.netbeans.modules.php.composer.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.composer.commands.Composer;
import org.openide.util.NbPreferences;

/**
 * Composer options.
 */
public final class ComposerOptions {

    public static final String COMPOSER_PATH = "composer.path"; // NOI18N
    public static final String VENDOR = "vendor"; // NOI18N
    public static final String AUTHOR_NAME = "author.name"; // NOI18N
    public static final String AUTHOR_EMAIL = "author.email"; // NOI18N
    public static final String IGNORE_VENDOR = "ignore.vendor"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "composer"; // NOI18N

    private static final ComposerOptions INSTANCE = new ComposerOptions();

    private final Preferences preferences;

    private volatile boolean composerSearched = false;


    private ComposerOptions() {
        preferences = NbPreferences.forModule(ComposerOptions.class).node(PREFERENCES_PATH);
    }

    public static ComposerOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    public String getComposerPath() {
        String composerPath = preferences.get(COMPOSER_PATH, null);
        if (composerPath == null && !composerSearched) {
            composerSearched = true;
            List<String> paths = FileUtils.findFileOnUsersPath(Composer.COMPOSER_FILENAMES.toArray(new String[0]));
            if (!paths.isEmpty()) {
                composerPath = paths.get(0);
                setComposerPath(composerPath);
            }
        }
        return composerPath;
    }

    public void setComposerPath(String composerPath) {
        preferences.put(COMPOSER_PATH, composerPath);
    }

    public String getVendor() {
        return preferences.get(VENDOR, "vendor"); // NOI18N
    }

    public void setVendor(String vendor) {
        preferences.put(VENDOR, vendor);
    }

    public String getAuthorName() {
        return preferences.get(AUTHOR_NAME, System.getProperty("user.name")); // NOI18N
    }

    public void setAuthorName(String authorName) {
        preferences.put(AUTHOR_NAME, authorName);
    }

    public String getAuthorEmail() {
        return preferences.get(AUTHOR_EMAIL, "your@email.here"); // NOI18N
    }

    public void setAuthorEmail(String authorEmail) {
        preferences.put(AUTHOR_EMAIL, authorEmail);
    }

    public boolean isIgnoreVendor() {
        return preferences.getBoolean(IGNORE_VENDOR, true);
    }

    public void setIgnoreVendor(boolean ignoreVendor) {
        preferences.putBoolean(IGNORE_VENDOR, ignoreVendor);
    }

}
