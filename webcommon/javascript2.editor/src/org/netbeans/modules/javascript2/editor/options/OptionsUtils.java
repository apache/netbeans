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

package org.netbeans.modules.javascript2.editor.options;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.openide.util.WeakListeners;

/**
 * @author Tomas Mysik
 * @author Petr Hejl
 * XXX copied from PHP and adapted to multiple languages
 */
public final class OptionsUtils {

    private static final Map<Language<JsTokenId>, OptionsUtils> INSTANCES = new WeakHashMap<>();

    public static final String AUTO_COMPLETION_TYPE_RESOLUTION = "codeCompletionTypeResolution"; //NOI18N
    public static final String AUTO_COMPLETION_SMART_QUOTES = "codeCompletionSmartQuotes"; //NOI18N
    public static final String AUTO_STRING_CONCATINATION = "codeCompletionStringAutoConcatination"; //NOI18N
    public static final String AUTO_COMPLETION_FULL = "autoCompletionFull"; // NOI18N
    public static final String AUTO_COMPLETION_AFTER_DOT = "autoCompletionAfterDot"; // NOI18N
    public static final String COMPETION_ITEM_SIGNATURE_WIDTH = "codeComletionItemDescriptionWith"; //NOI18N

    // default values
    public static final boolean AUTO_COMPLETION_TYPE_RESOLUTION_DEFAULT = false;
    public static final boolean AUTO_COMPLETION_SMART_QUOTES_DEFAULT = true;
    public static final boolean AUTO_STRING_CONCATINATION_DEFAULT = true;
    public static final boolean AUTO_COMPLETION_FULL_DEFAULT = false;
    public static final boolean AUTO_COMPLETION_AFTER_DOT_DEFAULT = true;
    public static final int COMPETION_ITEM_SIGNATURE_WIDTH_DEFAULT = 40;

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();

            if (settingName == null || AUTO_COMPLETION_TYPE_RESOLUTION.equals(settingName)) {
                autoCompletionTypeResolution = preferences.getBoolean(
                        AUTO_COMPLETION_TYPE_RESOLUTION,
                        AUTO_COMPLETION_TYPE_RESOLUTION_DEFAULT);
            }

            if (settingName == null || AUTO_COMPLETION_SMART_QUOTES.equals(settingName)) {
                autoCompletionSmartQuotes = preferences.getBoolean(
                        AUTO_COMPLETION_SMART_QUOTES,
                        AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
            }

            if (settingName == null || AUTO_STRING_CONCATINATION.equals(settingName)) {
                autoStringConcatination = preferences.getBoolean(
                        AUTO_STRING_CONCATINATION,
                        AUTO_STRING_CONCATINATION_DEFAULT);
            }

            if (settingName == null || AUTO_COMPLETION_FULL.equals(settingName)) {
                autoCompletionFull = preferences.getBoolean(
                        AUTO_COMPLETION_FULL,
                        AUTO_COMPLETION_FULL_DEFAULT);
            }
            if (settingName == null || AUTO_COMPLETION_AFTER_DOT.equals(settingName)) {
                autoCompletionAfterDot = preferences.getBoolean(
                        AUTO_COMPLETION_AFTER_DOT,
                        AUTO_COMPLETION_AFTER_DOT_DEFAULT);
            }
            if (settingName == null || COMPETION_ITEM_SIGNATURE_WIDTH.equals(settingName)) {
                codeCompletionItemSignatureWidth = preferences.getInt(
                        COMPETION_ITEM_SIGNATURE_WIDTH,
                        COMPETION_ITEM_SIGNATURE_WIDTH_DEFAULT);
            }
        }
    };

    private final String mimeType;

    private Preferences preferences;

    private Boolean autoCompletionTypeResolution = null;
    private Boolean autoCompletionSmartQuotes = null;
    private Boolean autoStringConcatination = null;
    private Boolean autoCompletionFull = null;
    private Boolean autoCompletionAfterDot = null;
    private Integer codeCompletionItemSignatureWidth = null;

    private OptionsUtils(Language<JsTokenId> language) {
        this.mimeType = language.mimeType();
    }

    public static synchronized OptionsUtils forLanguage(Language<JsTokenId> language) {
        OptionsUtils instance = INSTANCES.get(language);
        if (instance == null) {
            instance = new OptionsUtils(language);
            INSTANCES.put(language, instance);
        }
        return instance;
    }

    /**
     * Parameters of methods/functions apre pre-filled by preceeding declared variables.
     */
    public boolean autoCompletionTypeResolution() {
        lazyInit();
        assert autoCompletionTypeResolution != null;
        return autoCompletionTypeResolution;
    }

    public boolean autoCompletionSmartQuotes() {
        lazyInit();
        assert autoCompletionSmartQuotes != null;
        return autoCompletionSmartQuotes;
    }

    public boolean autoStringConcatination() {
        lazyInit();
        assert autoStringConcatination != null;
        return autoStringConcatination;
    }

    /**
     * Code Completion after typing.
     */
    public boolean autoCompletionFull() {
        lazyInit();
        assert autoCompletionFull != null;
        return autoCompletionFull;
    }

    /**
     * Code Completion items signature width.
     * @return number of chars
     */
    public int getCodeCompletionItemSignatureWidth() {
        lazyInit();
        assert codeCompletionItemSignatureWidth != null;
        return codeCompletionItemSignatureWidth;
    }

    /**
     * Variables after "$".
     */
    public boolean autoCompletionAfterDot() {
        lazyInit();
        assert autoCompletionAfterDot != null;
        if (autoCompletionFull()) {
            return true;
        }
        return autoCompletionAfterDot;
    }

    private void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(
                    PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    public void setTestTypeResolution(boolean value) {
        autoCompletionTypeResolution = value;
    }

    /**
     *
     * @param width number of chars of max width of signature
     */
    public void setCodeCompletionItemSignatureWidth(int width) {
        codeCompletionItemSignatureWidth = width;
    }
}
