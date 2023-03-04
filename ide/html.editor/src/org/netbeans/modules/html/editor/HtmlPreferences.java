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

package org.netbeans.modules.html.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring.Mode;
import org.netbeans.modules.html.editor.refactoring.api.SelectorType;
import org.openide.util.WeakListeners;

/**
 *
 * @author marekfukala
 */
public class HtmlPreferences {

    private static boolean autocompleQuotesAfterEQS;
    private static boolean autocompleQuotes;
    public static boolean completionOffersEndTagAfterLt;
    private static boolean autoPopupCompletionWindow;
    private static boolean autoPopupEndTagAutoCompletion;
    private static boolean isPaletteCompletionEnabled;

    //extract inlined style panel preferences
    private static SelectorType selectorType;
    private static final String SELECTOR_TYPE_PROPERTY_NAME = "extractInlinedStylePanelSelectorType"; //NOI18N
    private static SelectorType SELECTOR_TYPE_DEFAULT = SelectorType.ID;

    private static Mode sectionMode;
    private static final String SECTION_MODE_PROPERTY_NAME = "extractInlinedStylePanelSectionMode"; //NOI18N
    private static Mode SECTION_MODE_DEFAULT = Mode.refactorToExistingEmbeddedSection;

    private static String mimetypesWithEnabledHtmlErrorChecking;
    private static final String mimetypesWithEnabledHtmlErrorChecking_key = "mimetypesWithEnabledHtmlErrorChecking"; //NOI18N
    private static String mimetypesWithEnabledHtmlErrorChecking_default = ""; //NOI18N
    private static String mimetypesDelimiter = ";"; //NOI18N
    
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS.equals(settingName)) {
                autocompleQuotesAfterEQS = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS, HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES.equals(settingName)) {
                autocompleQuotes = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES, HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT.equals(settingName)) {
                completionOffersEndTagAfterLt = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT, HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_COMPLETION_AUTOPOPUP_WINDOW.equals(settingName)) {
                autoPopupCompletionWindow = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_COMPLETION_AUTOPOPUP_WINDOW, HtmlCompletionOptionsPanel.HTML_COMPLETION_AUTOPOPUP_WINDOW_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_END_TAG_AUTOCOMPLETION_AUTOPOPUP.equals(settingName)) {
                autoPopupEndTagAutoCompletion = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_END_TAG_AUTOCOMPLETION_AUTOPOPUP, HtmlCompletionOptionsPanel.HTML_END_TAG_AUTOCOMPLETION_AUTOPOPUP_DEFAULT);
            }
            if (settingName == null || SELECTOR_TYPE_PROPERTY_NAME.equals(settingName)) {
                selectorType = SelectorType.valueOf(preferences.get(SELECTOR_TYPE_PROPERTY_NAME, SELECTOR_TYPE_DEFAULT.name()));
            }
            if (settingName == null || SECTION_MODE_PROPERTY_NAME.equals(settingName)) {
                sectionMode = Mode.valueOf(preferences.get(SECTION_MODE_PROPERTY_NAME, SECTION_MODE_DEFAULT.name()));
            }
            if (settingName == null || mimetypesWithEnabledHtmlErrorChecking_key.equals(settingName)) {
                mimetypesWithEnabledHtmlErrorChecking = preferences.get(mimetypesWithEnabledHtmlErrorChecking_key, mimetypesWithEnabledHtmlErrorChecking_default);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_ENABLE_PALETTE_COMPLETION.equals(settingName)) {
                isPaletteCompletionEnabled = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_ENABLE_PALETTE_COMPLETION, HtmlCompletionOptionsPanel.HTML_ENABLE_PALETTE_COMPLETION_DEFAULT);
            }
        }
    };

    private static void lazyIntialize() {
        if(initialized.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(HtmlKit.HTML_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    private HtmlPreferences() {
        //do not instantiate
    }

    /**
     * Autocomplete html quotations after equal sign typed in html tag?
     *
     * @return true if enabled
     */
    public static boolean autocompleteQuotesAfterEqualSign() {
        lazyIntialize();
        return autocompleQuotesAfterEQS;
    }

    /**
     * Autocomplete html quotations in tag attribute values
     *
     * @return true if enabled
     */
    public static boolean autocompleteQuotes() {
        lazyIntialize();
        return autocompleQuotes;
    }

    /**
     * Html code completion offers end tags after less than character
     *
     * @return true if enabled
     */
    public static boolean completionOffersEndTagAfterLt() {
        lazyIntialize();
        return completionOffersEndTagAfterLt;
    }

    public static boolean autoPopupCompletionWindow() {
        lazyIntialize();
        return autoPopupCompletionWindow;
    }

    public static boolean autoPopupEndTagAutoCompletion() {
        lazyIntialize();
        return autoPopupEndTagAutoCompletion;
    }

    public static SelectorType extractInlinedStylePanelSelectorType() {
        lazyIntialize();
        return selectorType;
    }

    public static void setExtractInlinedStylePanelSelectorType(SelectorType type) {
        lazyIntialize();
        preferences.put(SELECTOR_TYPE_PROPERTY_NAME, type.name());
    }

    public static ExtractInlinedStyleRefactoring.Mode extractInlinedStylePanelSectionMode() {
        lazyIntialize();
        return sectionMode;
    }

    public static void setExtractInlinedStylePanelSectionMode(Mode mode) {
        lazyIntialize();
        preferences.put(SECTION_MODE_PROPERTY_NAME, mode.name());
    }

    public static boolean isPaletteCompletionEnabled() {
        lazyIntialize();
        return isPaletteCompletionEnabled;
    }

    public static Collection<String> getMimetypesWithEnabledHtmlErrorChecking() {
        lazyIntialize();
        return getMimetypesWithEnabledHtmlErrorCheckingAsCollection();
    }

    public static boolean isHtmlErrorCheckingEnabledForMimetype(String mimetype) {
        boolean result = getMimetypesWithEnabledHtmlErrorChecking().contains(mimetype);
        return isHtmlMimeType(mimetype) ? !result : result; //see the setHtmlErrorChecking comment
    }

    public static void setHtmlErrorChecking(String mimetype, boolean enabled) {
        lazyIntialize();

        Collection<String> mimescol = getMimetypesWithEnabledHtmlErrorCheckingAsCollection();

        //those two mimetypes are enabled by default. If one wants them to be disabled
        //then they appear in the setting but the meaning is exactly opposite.
        boolean html = isHtmlMimeType(mimetype);
        if(html) {
            enabled = !enabled;
        }

        if(mimescol.contains(mimetype)) {
            if(enabled) {
                return ; //already enabled
            } else {
                //needs to be disabled
                mimescol.remove(mimetype);
            }
        } else {
            if(enabled) {
                mimescol.add(mimetype);
            } else {
                //already disabled
                return ;
            }
        }

        preferences.put(mimetypesWithEnabledHtmlErrorChecking_key, encodeMimetypes(mimescol));

    }

    private static boolean isHtmlMimeType(String mimetype) {
        return "text/html".equals(mimetype) || "text/xhtml".equals(mimetype); // NOI18N
    }

    private static String encodeMimetypes(Collection<String> mimes) {
        StringBuilder b = new StringBuilder();
        for(String m : mimes) {
            b.append(m);
            b.append(mimetypesDelimiter);
        }
        return b.toString();
    }

    private static Collection<String> getMimetypesWithEnabledHtmlErrorCheckingAsCollection() {
        lazyIntialize();
        //return modifiable collection!
        ArrayList<String> list = new ArrayList<>();
        if(mimetypesWithEnabledHtmlErrorChecking == null) {
            //somehow broken configuration, should normally be already loaded
            return Collections.emptyList();
        }
        list.addAll(Arrays.asList(mimetypesWithEnabledHtmlErrorChecking.split(mimetypesDelimiter)));
        return list;
    }

}
