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
            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_FIRST_CLASS_CALLABLE.equals(settingName)) {
                codeCompletionFirstClassCallable = preferences.getBoolean(
                        CodeCompletionPanel.PHP_CODE_COMPLETION_FIRST_CLASS_CALLABLE,
                        CodeCompletionPanel.PHP_CODE_COMPLETION_FIRST_CLASS_CALLABLE_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_VARIABLES_SCOPE.equals(settingName)) {
                codeCompletionVariablesScope = CodeCompletionPanel.VariablesScope.resolve(preferences.get(CodeCompletionPanel.PHP_CODE_COMPLETION_VARIABLES_SCOPE, null));
            }

            if (settingName == null || CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE.equals(settingName)) {
                codeCompletionType = CodeCompletionPanel.CodeCompletionType.resolve(preferences.get(CodeCompletionPanel.PHP_CODE_COMPLETION_TYPE, null));
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_TYPE.equals(settingName)) {
                globalNSImportType = CodeCompletionPanel.GlobalNamespaceAutoImportType.resolve(preferences.get(CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_TYPE, CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_TYPE_DEFAULT));
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_FUNCTION.equals(settingName)) {
                globalNSImportFunction = CodeCompletionPanel.GlobalNamespaceAutoImportType.resolve(preferences.get(CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_FUNCTION, CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_FUNCTION_DEFAULT));
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_CONST.equals(settingName)) {
                globalNSImportConst = CodeCompletionPanel.GlobalNamespaceAutoImportType.resolve(preferences.get(CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_CONST, CodeCompletionPanel.PHP_AUTO_IMPORT_GLOBAL_NS_IMPORT_CONST_DEFAULT));
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT.equals(settingName)) {
                autoImport = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_IMPORT,
                        CodeCompletionPanel.PHP_AUTO_IMPORT_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT_FILE_SCOPE.equals(settingName)) {
                autoImportFileScope = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_IMPORT_FILE_SCOPE,
                        CodeCompletionPanel.PHP_AUTO_IMPORT_FILE_SCOPE_DEFAULT);
            }

            if (settingName == null || CodeCompletionPanel.PHP_AUTO_IMPORT_NAMESPACE_SCOPE.equals(settingName)) {
                autoImportNamespaceScope = preferences.getBoolean(
                        CodeCompletionPanel.PHP_AUTO_IMPORT_NAMESPACE_SCOPE,
                        CodeCompletionPanel.PHP_AUTO_IMPORT_NAMESPACE_SCOPE_DEFAULT);
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
    private static Boolean codeCompletionFirstClassCallable = null;
    private static Boolean autoImport = null;
    private static Boolean autoImportFileScope = null;
    private static Boolean autoImportNamespaceScope = null;

    private static CodeCompletionPanel.VariablesScope codeCompletionVariablesScope = null;

    private static CodeCompletionPanel.CodeCompletionType codeCompletionType = null;
    private static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportType = null;
    private static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportFunction = null;
    private static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportConst = null;

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
     * Classes including Members after "->", "?->", "::", "new", "extends", ...
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
     * Also Static Methods after "->", "?->".
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
     * Code completion for first-class callable syntax. e.g. strlen(...)
     *
     * @return {@code true} if add (...) as a CC item, otherwise {@code false}
     */
    public static boolean codeCompletionFirstClassCallable() {
        lazyInit();
        assert codeCompletionFirstClassCallable != null;
        return codeCompletionFirstClassCallable;
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

    public static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportType() {
        lazyInit();
        assert globalNSImportType != null;
        return globalNSImportType;
    }

    public static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportFunction() {
        lazyInit();
        assert globalNSImportFunction != null;
        return globalNSImportFunction;
    }

    public static CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImportConst() {
        lazyInit();
        assert globalNSImportConst != null;
        return globalNSImportConst;
    }

    /**
     * Check whether auto import is enabled.
     *
     * @return {@code true} if auto import is enabled, {@code false} otherwise
     */
    public static boolean autoImport() {
        lazyInit();
        assert autoImport != null;
        return autoImport;
    }

    /**
     * Auto import for a file scope.
     *
     * @return {@code true} if import a use in a file scope, otherwise
     * {@code false}
     */
    public static boolean autoImportFileScope() {
        lazyInit();
        assert autoImportFileScope != null;
        return autoImportFileScope;
    }

    /**
     * Auto import for a namespace scope.
     *
     * @return {@code true} if import a use in a namespace scope, otherwise
     * {@code false}
     */
    public static boolean autoImportNamespaceScope() {
        lazyInit();
        assert autoImportNamespaceScope != null;
        return autoImportNamespaceScope;
    }

    private static void lazyInit() {
        if (INITED.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(FileUtils.PHP_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, PREFERENCES_TRACKER, preferences));
            PREFERENCES_TRACKER.preferenceChange(null);
        }
    }
}
