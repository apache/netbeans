/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.openide.util.WeakListeners;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssPreferences {

    public static final String FIND_IN_UNRELATED_FILES = "cssFindInUnrelatedFiles"; //NOI18N
    public static final boolean FIND_IN_UNRELATED_FILES_DEFAULT = true;

    private static boolean findInUnrelatedFiles;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || FIND_IN_UNRELATED_FILES.equals(settingName)) {
                findInUnrelatedFiles = preferences.getBoolean(FIND_IN_UNRELATED_FILES, FIND_IN_UNRELATED_FILES_DEFAULT);
            }
        }
    };

    private static void lazyIntialize() {
        if (initialized.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(CssTokenId.language().mimeType()).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    private CssPreferences() {
        //do not instantiate
    }

    /**
     * Should the find usages/refactorings work also in unrelated files?
     *
     * @return true if enabled
     */
    public static boolean findInUnrelatedFiles() {
        lazyIntialize();
        return findInUnrelatedFiles;
    }

    public static void setFindInUnrelatedFiles(boolean value) {
        lazyIntialize();
        preferences.putBoolean(FIND_IN_UNRELATED_FILES, value);
    }
}
