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

package org.netbeans.modules.db.sql.editor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.db.sql.editor.options.ui.CodeCompletionPanel;
import org.openide.util.WeakListeners;

/**
 * 
 * @author Arthur Sadykov
 * 
 * @since 1.47
 */
public class SQLPreferences {

    private static boolean autoPopupCompletionWindow;
    private static String autoPopupTriggers;
    private static Preferences preferences;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || CodeCompletionPanel.SQL_COMPLETION_AUTOPOPUP_WINDOW.equals(settingName)) {
                autoPopupCompletionWindow = preferences.getBoolean(
                        CodeCompletionPanel.SQL_COMPLETION_AUTOPOPUP_WINDOW, true);
                autoPopupTriggers = preferences.get(
                        CodeCompletionPanel.SQL_AUTOPOPUP_TRIGGERS, CodeCompletionPanel.DEFAULT_AUTOPOPUP_TRIGGERS);
            }
        }
    };

    private SQLPreferences() {
    }

    /**
     * Returns whether the automatic code completion should be invoked while typing.
     *
     * @return {@code true} if the automatic code completion should be invoked while typing,
     *         {@code false} otherwise.
     * 
     * @since 1.47
     */
    public static boolean autoPopupCompletionWindow() {
        lazyIntialize();
        return autoPopupCompletionWindow;
    }
    
    /**
     * Returns a string consisting of characters each of which acts as an auto popup trigger.
     * 
     * @return a string consisting of characters each of which acts as an auto popup trigger.
     * 
     * @since 1.47
     */
    public static String autoPopupTriggers() {
        lazyIntialize();
        return autoPopupTriggers;
    }

    private static void lazyIntialize() {
        if (initialized.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup("text/x-sql").lookup(Preferences.class); //NOI18N
            preferences.addPreferenceChangeListener(
                    WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }
    
}
