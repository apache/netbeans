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

package org.netbeans.modules.spring.beans.completion;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Fousek
 */
public final class OptionCodeCompletionSettings {

    private static boolean caseSensitive;
    private static boolean inited;
    private static PreferenceChangeListener settingsListener = new SettingsListener();

    // prevent OptionCodeCompletionSettings instantiation
    private OptionCodeCompletionSettings() {}

    public static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static void setCaseSensitive(boolean b) {
        lazyInit();
        caseSensitive = b;
    }

    private static void lazyInit() {
        if (!inited) {
            inited = true;

            // correctly we should use a proper mime type for the document where the completion runs,
            // but at the moment this is enough, because completion settings are mainted globaly for all mime types
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, settingsListener, prefs));
            setCaseSensitive(prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false));
        }
    }

    private static class SettingsListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (SimpleValueNames.COMPLETION_CASE_SENSITIVE.equals(evt.getKey())) {
                setCaseSensitive(Boolean.valueOf(evt.getNewValue()));
            }
        }
    }
}
