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
package org.netbeans.modules.php.blade.editor.preferences;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.openide.util.WeakListeners;

/**
 * this will be moved to PHP Options
 * 
 * @author bogdan
 */
public final class GeneralPreferencesUtils {

    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    public static final String ENABLE_FORMATTING = "enable-blade-format"; // NOI18N
    public static final String ENABLE_INDENTATION = "enable-blade-indent"; // NOI18N
    public static final String ENABLE_AUTO_TAG_COMPLETION = "enable-auto-tag-completion"; // NOI18N

    private static Boolean enableFormatting = null;
    private static Boolean enableIndentation = null;
    private static Boolean enableAutoTagCompletion = null;

    // default values
    private static Preferences PREFERENCES;

    private static final PreferenceChangeListener PREFERENCES_TRACKER = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            enableFormatting = PREFERENCES.getBoolean(ENABLE_FORMATTING, false);
            enableIndentation = PREFERENCES.getBoolean(ENABLE_INDENTATION, false);
            enableAutoTagCompletion = PREFERENCES.getBoolean(ENABLE_AUTO_TAG_COMPLETION, false);
        }
    };

    private GeneralPreferencesUtils() {
    }

    public static boolean isFormattingEnabled(){
        lazyInit();
        return enableFormatting;
    }

    public static boolean isIndentationEnabled(){
        lazyInit();
        return enableIndentation;
    }
    
    public static boolean isAutoTagCompletionEnabled(){
        lazyInit();
        return enableAutoTagCompletion;
    }
    
    private static void lazyInit() {
        if (INITED.compareAndSet(false, true)) {
            PREFERENCES = MimeLookup.getLookup(BladeLanguage.MIME_TYPE).lookup(Preferences.class);
            PREFERENCES.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, PREFERENCES));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
