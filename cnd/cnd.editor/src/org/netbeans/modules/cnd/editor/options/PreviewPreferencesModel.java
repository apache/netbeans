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
package org.netbeans.modules.cnd.editor.options;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.editor.api.CodeStyle;

/**
 *
 */
public class PreviewPreferencesModel {

    public enum Filter {
        TabsAndIndents,
        Alignment,
        Braces,
        BlankLines,
        Spaces,
        All
    }

    private final Map<CodeStyle.Language, Map<String, PreviewPreferences>> preferences = new ConcurrentHashMap<CodeStyle.Language, Map<String, PreviewPreferences>>();
    private final Map<CodeStyle.Language, String> defaultStyles = new ConcurrentHashMap<CodeStyle.Language, String>();

    public PreviewPreferencesModel() {
    }

    public Map<String, PreviewPreferences> getPrefences(CodeStyle.Language language) {
        return preferences.get(language);
    }

    public synchronized void initLanguageMap(CodeStyle.Language language) {
        for (String style : EditorOptions.getAllStyles(language)) {
            initLanguageStylePreferences(language, style);
        }
        String def = defaultStyles.get(language);
        if (def == null) {
            def = EditorOptions.getCurrentProfileId(language, null);
            defaultStyles.put(language, def);
        }
    }

    public Map<String, PreviewPreferences> getLanguagePreferences(CodeStyle.Language language) {
        return preferences.get(language);
    }

    public String getLanguageDefaultStyle(CodeStyle.Language language) {
        return defaultStyles.get(language);
    }

    public void setLanguageDefaultStyle(CodeStyle.Language language, String def) {
        defaultStyles.put(language, def);
    }

    private void initLanguageStylePreferences(CodeStyle.Language language, String styleId) {
        Map<String, PreviewPreferences> prefs = preferences.get(language);
        if (prefs == null) {
            prefs = new HashMap<String, PreviewPreferences>();
            preferences.put(language, prefs);
        }
        PreviewPreferences clone = prefs.get(styleId);
        if (clone == null) {
            clone = new PreviewPreferences(EditorOptions.getPreferences(language, styleId), language, styleId);
            prefs.put(styleId, clone);
        }
    }

    public Map<String, PreviewPreferences> clonePreferences(CodeStyle.Language language) {
        Map<String, PreviewPreferences> newAllPreferences = new HashMap<String, PreviewPreferences>();
        for (Map.Entry<String, PreviewPreferences> entry2 : getLanguagePreferences(language).entrySet()) {
            PreviewPreferences pref = entry2.getValue();
            PreviewPreferences newPref = new PreviewPreferences(pref, language, entry2.getKey());
            newAllPreferences.put(entry2.getKey(), newPref);
        }
        return newAllPreferences;
    }

    public void resetPreferences(CodeStyle.Language language, Map<String, PreviewPreferences> newPreferences) {
        preferences.put(language, newPreferences);
    }

    public void clear(CodeStyle.Language language) {
        preferences.remove(language);
        defaultStyles.remove(language);
    }
}
