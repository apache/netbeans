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
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.openide.util.WeakListeners;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssPreferences {

    public static final String FIND_IN_UNRELATED_FILES = "cssFindInUnrelatedFiles"; //NOI18N
    public static final boolean FIND_IN_UNRELATED_FILES_DEFAULT = true;

    private static boolean findInUnrelatedFiles;

    private static String disabledErrorChecks;
    private static final String disabledErrorChecks_key = "disabledErrorChecks"; //NOI18N
    private static String disabledErrorChecks_default = ""; //NOI18N
    
    private static final char DELIMITER_CHAR = ';'; //NOI18N 
    private static final String DELIMITER = ""+DELIMITER_CHAR; //NOI18N 
    private static final String ENCODED_DELIMITER = "\\"+DELIMITER_CHAR; //NOI18N 
    
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(DELIMITER);
    private static final Pattern ENCODED_DELIMITER_PATTEN = Pattern.compile(ENCODED_DELIMITER);
    
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || FIND_IN_UNRELATED_FILES.equals(settingName)) {
                findInUnrelatedFiles = preferences.getBoolean(FIND_IN_UNRELATED_FILES, FIND_IN_UNRELATED_FILES_DEFAULT);
            }
            if (settingName == null || disabledErrorChecks_key.equals(settingName)) {
                disabledErrorChecks = preferences.get(disabledErrorChecks_key, disabledErrorChecks_default);
            }
        }
    };

    private static void lazyIntialize() {
        if (initialized.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(CssLanguage.CSS_MIME_TYPE).lookup(Preferences.class);
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

    public static Collection<String> getDisabledErrorChecks() {
        lazyIntialize();
        return getDisabledErrorChecksAsCollection();
    }

    public static boolean isErrorCheckingDisabledForCssErrorKey(String errorKey) {
        return getDisabledErrorChecks().contains(encodeKey(errorKey));
    }

    public static void setCssErrorChecking(String errorKey, boolean enabled) {
        lazyIntialize();
        errorKey = encodeKey(errorKey);
        
        Collection<String> mimescol = getDisabledErrorChecksAsCollection();

        if (mimescol.contains(errorKey)) {
            if (!enabled) {
                return; //already disabled
            } else {
                //already disabled, but should be enabled
                mimescol.remove(errorKey);
            }
        } else {
            if (!enabled) {
                mimescol.add(errorKey);
            } else {
                return; //already enabled
            }
        }

        preferences.put(disabledErrorChecks_key, encodeKeys(mimescol));

    }

    private static String encodeKeys(Collection<String> mimes) {
        StringBuilder b = new StringBuilder();
        for (String m : mimes) {
            b.append(m);
            b.append(DELIMITER_CHAR);
        }
        return b.toString();
    }

    /**
     * The keys are encoded!
     * @return 
     */
    private static Collection<String> getDisabledErrorChecksAsCollection() {
        //return modifiable collection!
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(disabledErrorChecks.split(DELIMITER)));
        return list;
    }

    private static String encodeKey(String key) {
        return DELIMITER_PATTERN.matcher(key).replaceAll(ENCODED_DELIMITER);
    }
    
    private static String decodeKey(String key) {
        return ENCODED_DELIMITER_PATTEN.matcher(key).replaceAll(DELIMITER);
    }

}
