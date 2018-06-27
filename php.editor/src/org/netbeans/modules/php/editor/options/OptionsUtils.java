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

package org.netbeans.modules.php.editor.options;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.WeakListeners;

/**
 * @author Tomas Mysik
 */
public final class OptionsUtils {
    private static final AtomicBoolean INITED = new AtomicBoolean(false);

    private static final PreferenceChangeListener PREFERENCES_TRACKER = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_FULL.equals(settingName)) {
                autoCompletionFull = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_FULL,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_FULL_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_VARIABLES.equals(settingName)) {
                autoCompletionVariables = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_VARIABLES,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_VARIABLES_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_TYPES.equals(settingName)) {
                autoCompletionTypes = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_TYPES,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_TYPES_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_NAMESPACES.equals(settingName)) {
                autoCompletionNamespaces = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_NAMESPACES,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_NAMESPACES_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_SMART_QUOTES.equals(settingName)) {
                autoCompletionSmartQuotes = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_SMART_QUOTES,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_STRING_CONCATINATION.equals(settingName)) {
                autoStringConcatination = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_STRING_CONCATINATION,
                        CodeCompletionPanel.PHP_AUTO_STRING_CONCATINATION_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL.equals(settingName)) {
                autoCompletionUseLowercaseTrueFalseNull = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_COMPLETION_COMMENT_ASTERISK.equals(settingName)) {
                autoCompletionCommentAsterisk = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_COMMENT_ASTERISK,
                        CodeCompletionPanel.PHP_AUTO_COMPLETION_COMMENT_ASTERISK_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_STATIC_METHODS.equals(settingName)) {
                codeCompletionStaticMethods = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_STATIC_METHODS,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_STATIC_METHODS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_NON_STATIC_METHODS.equals(settingName)) {
                codeCompletionNonStaticMethods = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_NON_STATIC_METHODS,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_NON_STATIC_METHODS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING.equals(settingName)) {
                codeCompletionSmartParametersPreFilling = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_VARIABLES_SCOPE.equals(settingName)) {
                codeCompletionVariablesScope = CodeCompletionPanel.VariablesScope.resolve(preferences.get(CodeCompletionPanel.PHP_CODE_COMPLETION_VARIABLES_SCOPE, null));
            }

            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE.equals(settingName)) {
                codeCompletionType = CodeCompletionPanel.CodeCompletionType.resolve(preferences.get(CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE, null));
            }
        }
    };

    private static Preferences preferences;

    private static Boolean autoCompletionFull = null;
    private static Boolean autoCompletionVariables = null;
    private static Boolean autoCompletionTypes = null;
    private static Boolean autoCompletionNamespaces = null;
    private static Boolean autoCompletionSmartQuotes = null;
    private static Boolean autoStringConcatination = null;
    private static Boolean autoCompletionUseLowercaseTrueFalseNull = null;
    private static Boolean autoCompletionCommentAsterisk = null;

    private static Boolean codeCompletionStaticMethods = null;
    private static Boolean codeCompletionNonStaticMethods = null;
    private static Boolean codeCompletionSmartParametersPreFilling = null;

    private static CodeCompletionPanel.VariablesScope codeCompletionVariablesScope = null;

    private static CodeCompletionPanel.CodeCompletionType codeCompletionType = null;

    private OptionsUtils() {
    }

    /**
     * Code Completion after typing.
     */
    public static boolean autoCompletionFull() {
        lazyInit();
        assert autoCompletionFull != null;
        return autoCompletionFull;
    }

    /**
     * Variables after "$".
     */
    public static boolean autoCompletionVariables() {
        lazyInit();
        assert autoCompletionVariables != null;
        if (autoCompletionFull()) {
            return true;
        }
        return autoCompletionVariables;
    }

    /**
     * Classes including Members after "->", "::", "new", "extends", ...
     */
    public static boolean autoCompletionTypes() {
        lazyInit();
        assert autoCompletionTypes != null;
        if (autoCompletionFull()) {
            return true;
        }
        return autoCompletionTypes;
    }

    /**
     * Namespaces after "\\" (PHP 5.3 only).
     */
    public static boolean autoCompletionNamespaces() {
        lazyInit();
        assert autoCompletionNamespaces != null;
        if (autoCompletionFull()) {
            return true;
        }
        return autoCompletionNamespaces;
    }

    /**
     * Also Static Methods after "->".
     */
    public static boolean codeCompletionStaticMethods() {
        lazyInit();
        assert codeCompletionStaticMethods != null;
        return codeCompletionStaticMethods;
    }

    /**
     * Also Non-Static Methods after "::".
     */
    public static boolean codeCompletionNonStaticMethods() {
        lazyInit();
        assert codeCompletionNonStaticMethods != null;
        return codeCompletionNonStaticMethods;
    }

    /**
     * Parameters of methods/functions apre pre-filled by preceeding declared variables.
     */
    public static boolean codeCompletionSmartParametersPreFilling() {
        lazyInit();
        assert codeCompletionSmartParametersPreFilling != null;
        return codeCompletionSmartParametersPreFilling;
    }

    /**
     * Parameters of methods/functions apre pre-filled by preceeding declared variables.
     */
    public static boolean autoCompletionSmartQuotes() {
        lazyInit();
        assert autoCompletionSmartQuotes != null;
        return autoCompletionSmartQuotes;
    }

    public static boolean autoStringConcatination() {
        lazyInit();
        assert autoStringConcatination != null;
        return autoStringConcatination;
    }

    /**
     * TRUE -> true, FALSE -> false, NULL -> null.
     *
     * @return {@code true} if use lowercase, otherwise {@code false}
     */
    public static boolean autoCompletionUseLowercaseTrueFalseNull() {
        lazyInit();
        assert autoCompletionUseLowercaseTrueFalseNull != null;
        return autoCompletionUseLowercaseTrueFalseNull;
    }

    /**
     * Multi line comment only.
     *
     * @return {@code true} if " * " is inserted, otherwise {@code false}
     */
    public static boolean autoCompletionCommentAsterisk() {
        lazyInit();
        assert autoCompletionCommentAsterisk != null;
        return autoCompletionCommentAsterisk;
    }

    /**
     * All variables or only from current file.
     */
    public static CodeCompletionPanel.VariablesScope codeCompletionVariablesScope() {
        lazyInit();
        assert codeCompletionVariablesScope != null;
        return codeCompletionVariablesScope;
    }

    /**
     * Type of Code Completion (PHP 5.3 only).
     */
    public static CodeCompletionPanel.CodeCompletionType codeCompletionType() {
        lazyInit();
        assert codeCompletionType != null;
        return codeCompletionType;
    }

    private static void lazyInit() {
        if (INITED.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(FileUtils.PHP_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, preferences));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
