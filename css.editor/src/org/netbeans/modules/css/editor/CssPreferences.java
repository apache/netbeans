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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
