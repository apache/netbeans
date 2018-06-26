/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

    private static final Map<Language<JsTokenId>, OptionsUtils> INSTANCES = new WeakHashMap<Language<JsTokenId>, OptionsUtils>();

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
