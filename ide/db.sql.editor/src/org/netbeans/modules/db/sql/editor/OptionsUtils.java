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

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.db.sql.lexer.SQLLanguageConfig;
import org.openide.util.WeakListeners;

/**
 * @author Tomas Mysik
 */
public final class OptionsUtils {
    // package visible to allow access from unit tests
    public static final String PAIR_CHARACTERS_COMPLETION = "pair-characters-completion"; //NOI18N
    public static final String SQL_AUTO_COMPLETION_SUBWORDS = "sql-completion-subwords"; //NOI18N
    public static final boolean SQL_AUTO_COMPLETION_SUBWORDS_DEFAULT = false;

    private static final PreferenceChangeListener PREFERENCES_TRACKER = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();

            if (settingName == null || PAIR_CHARACTERS_COMPLETION.equals(settingName)) {
                pairCharactersCompletion = preferences.getBoolean(PAIR_CHARACTERS_COMPLETION, true);
            }

            if (settingName == null || SQL_AUTO_COMPLETION_SUBWORDS.equals(settingName)) {
                sqlCompletionSubwords = preferences.getBoolean(SQL_AUTO_COMPLETION_SUBWORDS, SQL_AUTO_COMPLETION_SUBWORDS_DEFAULT);
            }
        }
    };

    private static Preferences preferences;

    private static volatile boolean pairCharactersCompletion = true;
    private static volatile boolean sqlCompletionSubwords = SQL_AUTO_COMPLETION_SUBWORDS_DEFAULT;

    private OptionsUtils() {
    }

    /**
     * Option: "Insert Closing Brackets Automatically"
     * 
     * @return true  if clsing brackets should be  inserted 
     */
    public static boolean isPairCharactersCompletion() {
        lazyInit();
        return pairCharactersCompletion;
    }

    /**
     * Option: "Subword Completion"
     *
     * @return true if code completion should be subword based
     */
    public static boolean isSqlCompletionSubwords() {
        lazyInit();
        return sqlCompletionSubwords;
    }

    private synchronized static void lazyInit() {
        if (preferences == null) {
            preferences = MimeLookup.getLookup(SQLLanguageConfig.mimeType).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, preferences));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
