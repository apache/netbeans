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
package org.netbeans.modules.php.twig.editor.ui.options;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.openide.util.WeakListeners;

/**
 * Based on PHP. Options for Auto Completion.
 */
public final class OptionsUtils {

    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    public static final String AUTO_COMPLETION_SMART_QUOTES = "twigAutoCompletionSmartQuotes"; // NOI18N
    public static final String AUTO_COMPLETION_SMART_DELIMITERS = "twigAutoCompletionSmartDelimiters"; // NOI18N

    private static Boolean autoCompletionSmartQuotes = null;
    private static Boolean autoCompletionSmartDelimiters = null;

    // default values
    public static final boolean AUTO_COMPLETION_SMART_QUOTES_DEFAULT = true;
    public static final boolean AUTO_COMPLETION_SMART_DELIMITERS_DEFAULT = true;

    private static Preferences PREFERENCES;

    private static final PreferenceChangeListener PREFERENCES_TRACKER = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();

            if (settingName == null || AUTO_COMPLETION_SMART_QUOTES.equals(settingName)) {
                autoCompletionSmartQuotes = PREFERENCES.getBoolean(
                        AUTO_COMPLETION_SMART_QUOTES,
                        AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
            }
            if (settingName == null || AUTO_COMPLETION_SMART_DELIMITERS.equals(settingName)) {
                autoCompletionSmartDelimiters = PREFERENCES.getBoolean(
                        AUTO_COMPLETION_SMART_DELIMITERS,
                        AUTO_COMPLETION_SMART_DELIMITERS_DEFAULT);
            }
        }
    };

    private OptionsUtils() {
    }

    public static boolean autoCompletionSmartQuotes() {
        lazyInit();
        assert autoCompletionSmartQuotes != null;
        return autoCompletionSmartQuotes;
    }

    public static boolean autoCompletionSmartDelimiters() {
        lazyInit();
        assert autoCompletionSmartDelimiters != null;
        return autoCompletionSmartDelimiters;
    }

    private static void lazyInit() {
        if (INITED.compareAndSet(false, true)) {
            PREFERENCES = MimeLookup.getLookup(TwigLanguage.TWIG_MIME_TYPE).lookup(Preferences.class);
            PREFERENCES.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, PREFERENCES));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
