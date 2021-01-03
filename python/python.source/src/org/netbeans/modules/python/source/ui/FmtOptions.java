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
package org.netbeans.modules.python.source.ui;

import org.netbeans.modules.python.source.CodeStyle;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import static org.netbeans.modules.python.source.CodeStyle.*;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.PythonFormatter;
import org.netbeans.modules.python.source.PythonParserResult;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class FmtOptions {
    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String labelIndent = "labelIndent"; //NOI18N
    public static final String absoluteLabelIndent = "absoluteLabelIndent"; //NOI18N
    public static final String indentTopLevelClassMembers = "indentTopLevelClassMembers"; //NOI18N
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH;

    /*
    public static final String addLeadingStarInComment = "addLeadingStarInComment"; //NOI18N

    public static final String preferLongerNames = "preferLongerNames"; //NOI18N
    public static final String fieldNamePrefix = "fieldNamePrefix"; //NOI18N
    public static final String fieldNameSuffix = "fieldNameSuffix"; //NOI18N
    public static final String staticFieldNamePrefix = "staticFieldNamePrefix"; //NOI18N
    public static final String staticFieldNameSuffix = "staticFieldNameSuffix"; //NOI18N
    public static final String parameterNamePrefix = "parameterNamePrefix"; //NOI18N
    public static final String parameterNameSuffix = "parameterNameSuffix"; //NOI18N
    public static final String localVarNamePrefix = "localVarNamePrefix"; //NOI18N
    public static final String localVarNameSuffix = "localVarNameSuffix"; //NOI18N
    public static final String qualifyFieldAccess = "qualifyFieldAccess"; //NOI18N
    public static final String useIsForBooleanGetters = "useIsForBooleanGetters"; //NOI18N
    public static final String addOverrideAnnotation = "addOverrideAnnotation"; //NOI18N
    public static final String makeLocalVarsFinal = "makeLocalVarsFinal"; //NOI18N
    public static final String makeParametersFinal = "makeParametersFinal"; //NOI18N
    public static final String classMembersOrder = "classMembersOrder"; //NOI18N
    
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final String alignMultilineAnnotationArgs = "alignMultilineAnnotationArgs"; //NOI18N
    public static final String alignMultilineImplements = "alignMultilineImplements"; //NOI18N
    public static final String alignMultilineThrows = "alignMultilineThrows"; //NOI18N
    public static final String alignMultilineParenthesized = "alignMultilineParenthesized"; //NOI18N
    public static final String alignMultilineBinaryOp = "alignMultilineBinaryOp"; //NOI18N
    public static final String alignMultilineTernaryOp = "alignMultilineTernaryOp"; //NOI18N
    public static final String alignMultilineAssignment = "alignMultilineAssignment"; //NOI18N
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final String placeElseOnNewLine = "placeElseOnNewLine"; //NOI18N
    public static final String placeWhileOnNewLine = "placeWhileOnNewLine"; //NOI18N
    public static final String placeCatchOnNewLine = "placeCatchOnNewLine"; //NOI18N
    public static final String placeFinallyOnNewLine = "placeFinallyOnNewLine"; //NOI18N
    public static final String placeNewLineAfterModifiers = "placeNewLineAfterModifiers"; //NOI18N
    
    public static final String wrapExtendsImplementsKeyword = "wrapExtendsImplementsKeyword"; //NOI18N
    public static final String wrapExtendsImplementsList = "wrapExtendsImplementsList"; //NOI18N
    public static final String wrapMethodParams = "wrapMethodParams"; //NOI18N
    public static final String wrapThrowsKeyword = "wrapThrowsKeyword"; //NOI18N
    public static final String wrapThrowsList = "wrapThrowsList"; //NOI18N
    public static final String wrapMethodCallArgs = "wrapMethodCallArgs"; //NOI18N
    public static final String wrapAnnotationArgs = "wrapAnnotationArgs"; //NOI18N
    public static final String wrapChainedMethodCalls = "wrapChainedMethodCalls"; //NOI18N
    public static final String wrapArrayInit = "wrapArrayInit"; //NOI18N
    public static final String wrapFor = "wrapFor"; //NOI18N
    public static final String wrapForStatement = "wrapForStatement"; //NOI18N
    public static final String wrapIfStatement = "wrapIfStatement"; //NOI18N
    public static final String wrapWhileStatement = "wrapWhileStatement"; //NOI18N
    public static final String wrapDoWhileStatement = "wrapDoWhileStatement"; //NOI18N
    public static final String wrapAssert = "wrapAssert"; //NOI18N
    public static final String wrapEnumConstants = "wrapEnumConstants"; //NOI18N
    public static final String wrapAnnotations = "wrapAnnotations"; //NOI18N
    public static final String wrapBinaryOps = "wrapBinaryOps"; //NOI18N
    public static final String wrapTernaryOps = "wrapTernaryOps"; //NOI18N
    public static final String wrapAssignOps = "wrapAssignOps"; //NOI18N
    
    public static final String blankLinesBeforePackage = "blankLinesBeforePackage"; //NOI18N
    public static final String blankLinesAfterPackage = "blankLinesAfterPackage"; //NOI18N
    public static final String blankLinesBeforeImports = "blankLinesBeforeImports"; //NOI18N
    public static final String blankLinesAfterImports = "blankLinesAfterImports"; //NOI18N
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final String spaceBeforeFinally = "spaceBeforeFinally"; //NOI18N
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final String spaceBeforeSynchronizedParen = "spaceBeforeSynchronizedParen"; //NOI18N
    public static final String spaceBeforeAnnotationParen = "spaceBeforeAnnotationParen"; //NOI18N    
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final String spaceBeforeFinallyLeftBrace = "spaceBeforeFinallyLeftBrace"; //NOI18N
    public static final String spaceBeforeSynchronizedLeftBrace = "spaceBeforeSynchronizedLeftBrace"; //NOI18N
    public static final String spaceBeforeStaticInitLeftBrace = "spaceBeforeStaticInitLeftBrace"; //NOI18N
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final String spaceWithinSynchronizedParens = "spaceWithinSynchronizedParens"; //NOI18N
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final String spaceWithinAnnotationParens = "spaceWithinAnnotationParens"; //NOI18N
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final String spaceWithinArrayInitBrackets = "spaceWithinArrayInitBrackets"; //NOI18N
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
     */

    // Spaces
    public static final String addSpaceAroundOperators = "spaceAroundOperators"; //NOI18N
    public static final String removeSpaceInParens = "spaceInsideParens"; //NOI18N
    public static final String addSpaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String removeSpaceBeforeSep = "spaceBeforeSeparator"; //NOI18N
    public static final String removeSpaceInParamAssign = "spaceInKeywordAssign"; //NOI18N
    public static final String collapseSpaces = "collapseSpaces"; //NOI18N
    // Imports
    public static final String formatImports = "formatImports"; //NOI18N
    public static final String oneImportPerLine = "oneImportPerLine"; //NOI18N
    public static final String removeDuplicates = "removeDuplicates"; //NOI18N
    public static final String systemLibsFirst = "systemLibsFirst"; //NOI18N
    public static final String cleanupUnusedImports = "cleanupUnusedImports"; //NOI18N
    public static final String preferSymbolImports = "preferSymbolImports"; //NOI18N
    public static final String sortImports = "sortImports"; //NOI18N
    public static final String separateFromImps = "separateFromImps"; //NOI18N
    public static CodeStyleProducer codeStyleProducer;
    static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    static final String DEFAULT_PROFILE = "default"; // NOI18N
    static final String PROJECT_PROFILE = "project"; // NOI18N
    static final String usedProfile = "usedProfile"; // NOI18N

    private FmtOptions() {
    }

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }

    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }

    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }

    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    // Private section ---------------------------------------------------------
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    private static final String WRAP_ALWAYS = WrapStyle.WRAP_ALWAYS.name();
    //private static final String WRAP_IF_LONG  = WrapStyle.WRAP_IF_LONG.name();
    private static final String WRAP_NEVER = WrapStyle.WRAP_NEVER.name();

    //private static final String CLEANUP_COMMENT  = ImportCleanupStyle.COMMENT_OUT.name();
    private static final String IMP_LEAVE_ALONE = ImportCleanupStyle.LEAVE_ALONE.name();
    private static Map<String, String> defaults;


    static {
        createDefaults();
    }

    private static void createDefaults() {
        String defaultValues[][] = {
            {expandTabToSpaces, TRUE}, //NOI18N
            {tabSize, "4"}, //NOI18N
            {spacesPerTab, "4"}, //NOI18N
            {indentSize, "4"}, //NOI18N
            {continuationIndentSize, "8"}, //NOI18N
            {labelIndent, "0"}, //NOI18N
            {absoluteLabelIndent, FALSE}, //NOI18N
            {indentTopLevelClassMembers, TRUE}, //NOI18N
            {indentCasesFromSwitch, TRUE}, //NOI18N
            {rightMargin, "80"}, //NOI18N

            /*
            { addLeadingStarInComment, TRUE}, //NOI18N

            { preferLongerNames, TRUE}, //NOI18N
            { fieldNamePrefix, ""}, //NOI18N // XXX null
            { fieldNameSuffix, ""}, //NOI18N // XXX null
            { staticFieldNamePrefix, ""}, //NOI18N // XXX null
            { staticFieldNameSuffix, ""}, //NOI18N // XXX null
            { parameterNamePrefix, ""}, //NOI18N // XXX null
            { parameterNameSuffix, ""}, //NOI18N // XXX null
            { localVarNamePrefix, ""}, //NOI18N // XXX null
            { localVarNameSuffix, ""}, //NOI18N // XXX null
            { qualifyFieldAccess, FALSE}, //NOI18N // XXX
            { useIsForBooleanGetters, TRUE}, //NOI18N
            { addOverrideAnnotation, TRUE}, //NOI18N
            { makeLocalVarsFinal, FALSE}, //NOI18N
            { makeParametersFinal, FALSE}, //NOI18N
            { classMembersOrder, ""}, //NOI18N // XXX

            { alignMultilineMethodParams, FALSE}, //NOI18N
            { alignMultilineCallArgs, FALSE}, //NOI18N
            { alignMultilineAnnotationArgs, FALSE}, //NOI18N
            { alignMultilineImplements, FALSE}, //NOI18N
            { alignMultilineThrows, FALSE}, //NOI18N
            { alignMultilineParenthesized, FALSE}, //NOI18N
            { alignMultilineBinaryOp, FALSE}, //NOI18N
            { alignMultilineTernaryOp, FALSE}, //NOI18N
            { alignMultilineAssignment, FALSE}, //NOI18N
            { alignMultilineFor, FALSE}, //NOI18N
            { alignMultilineArrayInit, FALSE}, //NOI18N
            { placeElseOnNewLine, FALSE}, //NOI18N 
            { placeWhileOnNewLine, FALSE}, //NOI18N
            { placeCatchOnNewLine, FALSE}, //NOI18N 
            { placeFinallyOnNewLine, FALSE}, //NOI18N 
            { placeNewLineAfterModifiers, FALSE}, //NOI18N

            { wrapExtendsImplementsKeyword, WRAP_NEVER}, //NOI18N
            { wrapExtendsImplementsList, WRAP_NEVER}, //NOI18N
            { wrapMethodParams, WRAP_NEVER}, //NOI18N
            { wrapThrowsKeyword, WRAP_NEVER}, //NOI18N
            { wrapThrowsList, WRAP_NEVER}, //NOI18N
            { wrapMethodCallArgs, WRAP_NEVER}, //NOI18N
            { wrapAnnotationArgs, WRAP_NEVER}, //NOI18N
            { wrapChainedMethodCalls, WRAP_NEVER}, //NOI18N
            { wrapArrayInit, WRAP_NEVER}, //NOI18N
            { wrapFor, WRAP_NEVER}, //NOI18N
            { wrapForStatement, WRAP_ALWAYS}, //NOI18N
            { wrapIfStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapDoWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapAssert, WRAP_NEVER}, //NOI18N
            { wrapEnumConstants, WRAP_NEVER}, //NOI18N
            { wrapAnnotations, WRAP_ALWAYS}, //NOI18N
            { wrapBinaryOps, WRAP_NEVER}, //NOI18N
            { wrapTernaryOps, WRAP_NEVER}, //NOI18N
            { wrapAssignOps, WRAP_NEVER}, //NOI18N

            { blankLinesBeforePackage, "0"}, //NOI18N
            { blankLinesAfterPackage, "1"}, //NOI18N
            { blankLinesBeforeImports, "1"}, //NOI18N 
            { blankLinesAfterImports, "1"}, //NOI18N
            { blankLinesBeforeClass, "1"}, //NOI18N 
            { blankLinesAfterClass, "0"}, //NOI18N
            { blankLinesAfterClassHeader, "1"}, //NOI18N 
            { blankLinesBeforeFields, "0"}, //NOI18N 
            { blankLinesAfterFields, "0"}, //NOI18N
            { blankLinesBeforeMethods, "1"}, //NOI18N
            { blankLinesAfterMethods, "0"}, //NOI18N

            { spaceBeforeWhile, TRUE}, //NOI18N // XXX
            { spaceBeforeElse, TRUE}, //NOI18N // XXX
            { spaceBeforeCatch, TRUE}, //NOI18N // XXX
            { spaceBeforeFinally, TRUE}, //NOI18N // XXX
            { spaceBeforeMethodDeclParen, FALSE}, //NOI18N
            { spaceBeforeMethodCallParen, FALSE}, //NOI18N
            { spaceBeforeIfParen, TRUE}, //NOI18N
            { spaceBeforeForParen, TRUE}, //NOI18N
            { spaceBeforeWhileParen, TRUE}, //NOI18N
            { spaceBeforeCatchParen, TRUE}, //NOI18N
            { spaceBeforeSwitchParen, TRUE}, //NOI18N
            { spaceBeforeSynchronizedParen, TRUE}, //NOI18N
            { spaceBeforeAnnotationParen, FALSE}, //NOI18N    
            { spaceAroundUnaryOps, FALSE}, //NOI18N
            { spaceAroundBinaryOps, TRUE}, //NOI18N
            { spaceAroundTernaryOps, TRUE}, //NOI18N
            { spaceAroundAssignOps, TRUE}, //NOI18N
            { spaceBeforeClassDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeMethodDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeIfLeftBrace, TRUE}, //NOI18N
            { spaceBeforeElseLeftBrace, TRUE}, //NOI18N
            { spaceBeforeWhileLeftBrace, TRUE}, //NOI18N
            { spaceBeforeForLeftBrace, TRUE}, //NOI18N
            { spaceBeforeDoLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSwitchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeTryLeftBrace, TRUE}, //NOI18N
            { spaceBeforeCatchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeFinallyLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSynchronizedLeftBrace, TRUE}, //NOI18N
            { spaceBeforeStaticInitLeftBrace, TRUE}, //NOI18N
            { spaceBeforeArrayInitLeftBrace, FALSE}, //NOI18N
            { spaceWithinParens, FALSE}, //NOI18N
            { spaceWithinMethodDeclParens, FALSE}, //NOI18N
            { spaceWithinMethodCallParens, FALSE}, //NOI18N
            { spaceWithinIfParens, FALSE}, //NOI18N
            { spaceWithinForParens, FALSE}, //NOI18N
            { spaceWithinWhileParens, FALSE}, //NOI18N
            { spaceWithinSwitchParens, FALSE}, //NOI18N
            { spaceWithinCatchParens, FALSE}, //NOI18N
            { spaceWithinSynchronizedParens, FALSE}, //NOI18N
            { spaceWithinTypeCastParens, FALSE}, //NOI18N
            { spaceWithinAnnotationParens, FALSE}, //NOI18N
            { spaceWithinBraces, FALSE}, //NOI18N
            { spaceWithinArrayInitBrackets, FALSE}, //NOI18N
            { spaceBeforeComma, FALSE}, //NOI18N
            { spaceAfterComma, TRUE}, //NOI18N
            { spaceBeforeSemi, FALSE}, //NOI18N
            { spaceAfterSemi, TRUE}, //NOI18N
            { spaceBeforeColon, TRUE}, //NOI18N
            { spaceAfterColon, TRUE}, //NOI18N
            { spaceAfterTypeCast, TRUE}, //NOI18N
             */
            // Spaces
            {addSpaceAroundOperators, TRUE},
            {removeSpaceInParens, TRUE},
            {addSpaceAfterComma, TRUE},
            {removeSpaceBeforeSep, TRUE},
            {removeSpaceInParamAssign, TRUE},
            {collapseSpaces, TRUE},
            // Imports
            {formatImports, TRUE},
            {oneImportPerLine, TRUE},
            {removeDuplicates, TRUE},
            {systemLibsFirst, TRUE},
            {preferSymbolImports, TRUE},
            {sortImports, TRUE},
            {cleanupUnusedImports, IMP_LEAVE_ALONE},
            {separateFromImps, FALSE},};

        defaults = new HashMap<>();

        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }

    // Support section ---------------------------------------------------------
    public static class CategorySupport implements ActionListener, DocumentListener, PreviewProvider, PreferencesCustomizer {
        public static final String OPTION_ID = "org.netbeans.modules.python.editor.options.FormatingOptions.ID";
        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;
        private static final ComboItem wrap[] = new ComboItem[]{
            new ComboItem(WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS"), // NOI18N
            new ComboItem(WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG"), // NOI18N
            new ComboItem(WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER") // NOI18N
        };
        private static final ComboItem cleanupImports[] = new ComboItem[]{
            new ComboItem(ImportCleanupStyle.LEAVE_ALONE.name(), "LBL_imp_LEAVE_ALONE"), // NOI18N
            new ComboItem(ImportCleanupStyle.COMMENT_OUT.name(), "LBL_imp_COMMENT_OUT"), // NOI18N
            new ComboItem(ImportCleanupStyle.DELETE.name(), "LBL_imp_DELETE") // NOI18N
        };
        private final String previewText;
        private final String id;
        protected final JPanel panel;
        private final List<JComponent> components = new LinkedList<>();
        private JEditorPane previewPane;
        private final Preferences preferences;
        private final Preferences previewPrefs;

        protected CategorySupport(Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
            this.preferences = preferences;
            this.id = id;
            this.panel = panel;
            this.previewText = previewText != null ? previewText : NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default"); //NOI18N

            // Scan the panel for its components
            scan(panel, components);

            // Initialize the preview preferences
            Preferences forcedPrefs = new PreviewPreferences();
            for (String[] option : forcedOptions) {
                forcedPrefs.put(option[0], option[1]);
            }
            this.previewPrefs = new ProxyPreferences(preferences, forcedPrefs);

            // Load and hook up all the components
            loadFrom(preferences);
            addListeners();
        }

        protected void addListeners() {
            scan(ADD_LISTENERS, null);
        }

        protected void loadFrom(Preferences preferences) {
//            loaded = true;
            scan(LOAD, preferences);
//            loaded = false;
        }
//
//        public void applyChanges() {
//            storeTo(preferences);
//        }
//

        protected void storeTo(Preferences p) {
            scan(STORE, p);
        }

        protected void notifyChanged() {
//            if (loaded)
//                return;
            storeTo(preferences);
            refreshPreview();
        }

        // ActionListener implementation ---------------------------------------
        @Override
        public void actionPerformed(ActionEvent e) {
            notifyChanged();
        }

        // DocumentListener implementation -------------------------------------
        @Override
        public void insertUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            notifyChanged();
        }

        // PreviewProvider methods -----------------------------------------------------
        @Override
        public JComponent getPreviewComponent() {
            if (previewPane == null) {
                previewPane = new JEditorPane();
                previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtOptions.class, "AN_Preview")); //NOI18N
                previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtOptions.class, "AD_Preview")); //NOI18N
                previewPane.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
                previewPane.setEditorKit(CloneableEditorSupport.getEditorKit(PythonMIMEResolver.PYTHON_MIME_TYPE));
                previewPane.setEditable(false);
            }
            return previewPane;
        }

        @Override
        public void refreshPreview() {
            JEditorPane jep = (JEditorPane)getPreviewComponent();
            try {
                int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
                jep.putClientProperty("TextLimitLine", rm); //NOI18N
            } catch (NumberFormatException e) {
                // Ignore it
            }
            try {
                Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }

            CodeStyle codeStyle = codeStyleProducer.create(previewPrefs);
            jep.setIgnoreRepaint(true);

//            if (jep.getDocument() instanceof BaseDocument) {
//                BaseDocument document = (BaseDocument) jep.getDocument();
//                final org.netbeans.editor.Formatter f = document.getFormatter();
//                try {
//                    f.reformatLock();
//                    try {
//                        int reformattedLen = f.reformat(document, 0, document.getLength());
//                    } catch (BadLocationException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                } finally {
//                    f.reformatUnlock();
//                }
//            }

            // Hacky code to do preview: We want to preview blank text without
            // a data object... this doesn't work very well so requires some hacks
            // to create a temp file, format it, then save it and delete it
            // (to avoid save confirmation dialogs on the modified file etc)
            PythonFormatter formatter = new PythonFormatter(codeStyle);
            PythonParserResult info = null;
            File tmp = null;
            FileObject tmpFo = null;
            if (formatter.needsParserResult()) {
                try {
                    tmp = File.createTempFile("preview", ".py"); // NOI18N
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
                    writer.write(previewText);
                    writer.close();
                    final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(tmp));
                    tmpFo = fo;
                    // TODO - I need to get the classpath involved here such that it can
                    // find used/unused libraries
//                    if (!SourceUtils.isScanInProgress()) {
//                        // I'm using custom GSF code here because I want to set up an explicit
//                        // source path for the fake file object which includes the Python
//                        // libraries (since we need them for the isSystemModule lookup
//                        //SourceModel model = SourceModelFactory.getInstance().getModel(fo);
//                        //if (model != null && !model.isScanInProgress()) {
//                        List<FileObject> roots = new ArrayList<FileObject>(new PythonLanguage().getCoreLibraries());
//
//                        final PythonPlatformManager manager = PythonPlatformManager.getInstance();
//                        final String platformName = manager.getDefaultPlatform();
//                        PythonPlatform activePlatform = manager.getPlatform(platformName);
//                        if (activePlatform != null) {
//                            roots.addAll(activePlatform.getUniqueLibraryRoots());
//                            ClassPath boot = ClassPathSupport.createClassPath(roots.toArray(new FileObject[roots.size()]));
//                            ClassPath source = ClassPathSupport.createClassPath(new FileObject[]{fo.getParent()});
//                            ClassPath compile = source;
//
//                            ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, source);
//                            Source model = Source.create(cpInfo, fo);
//                            if (model != null) {
//                                final CompilationInfo[] infoHolder = new CompilationInfo[1];
//                                //model.runUserActionTask(new CancellableTask<CompilationInfo>() {
//                                model.runUserActionTask(new CancellableTask<CompilationController>() {
//                                    public void cancel() {
//                                    }
//
//                                    //public void run(CompilationInfo info) throws Exception {
//                                    public void run(CompilationController info) throws Exception {
//                                        info.toPhase(Phase.RESOLVED);
//                                        infoHolder[0] = info;
//                                        // Force open so info.getFileObject will succeed
//                                        GsfUtilities.getDocument(fo, true);
//                                    }
//                                }, false);
//                                info = infoHolder[0];
//                            }
//                        }
//                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            try {
                if (info != null && info.getSnapshot().getSource().getDocument(false) != null) {
                    Document doc = info.getSnapshot().getSource().getDocument(false);
                    formatter.reformat(null, doc, 0, doc.getLength(), info);
                    jep.setText(doc.getText(0, doc.getLength()));
                    // Save file to avoid warning on exit
                    DataObject dobj = DataObject.find(info.getSnapshot().getSource().getFileObject());
                    SaveCookie cookie = dobj.getCookie(SaveCookie.class);
                    if (cookie != null) {
                        cookie.save();
                    }
                } else {
                    Document doc = jep.getDocument();
                    if (doc.getLength() > 0) {
                        doc.remove(0, doc.getLength());
                    }
                    doc.insertString(0, previewText, null);
                    formatter.reformat(null, doc, 0, doc.getLength(), null);
                    jep.setText(doc.getText(0, doc.getLength()));
                }
            } catch (DataObjectNotFoundException dof) {
                Exceptions.printStackTrace(dof);
            } catch (IOException | BadLocationException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            if (tmpFo != null) {
                try {
                    tmpFo.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (tmp != null) {
                tmp.delete();
            }


            jep.setIgnoreRepaint(false);
            jep.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
            jep.repaint(100);
        }

        // PreferencesCustomizer implementation --------------------------------
        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public String getDisplayName() {
            return panel.getName();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        // PreferencesCustomizer.Factory implementation ------------------------
        public static final class Factory implements PreferencesCustomizer.Factory {
            private final String id;
            private final Class<? extends JPanel> panelClass;
            private final String previewText;
            private final String[][] forcedOptions;

            public Factory(String id, Class<? extends JPanel> panelClass, String previewText, String[]... forcedOptions) {
                this.id = id;
                this.panelClass = panelClass;
                this.previewText = previewText;
                this.forcedOptions = forcedOptions;
            }

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                try {
                    return new CategorySupport(preferences, id, panelClass.newInstance(), previewText, forcedOptions);
                } catch (IllegalAccessException | InstantiationException e) {
                    return null;
                }
            }
        } // End of CategorySupport.Factory class

        // Private methods -----------------------------------------------------
        private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
            switch (operation) {
            case LOAD:
                loadData(jc, optionID, p);
                break;
            case STORE:
                storeData(jc, optionID, p);
                break;
            case ADD_LISTENERS:
                addListener(jc);
                break;
            }
        }

        private void scan(int what, Preferences p) {
            for (JComponent jc : components) {
                Object o = jc.getClientProperty(OPTION_ID);
                if (o instanceof String) {
                    performOperation(what, jc, (String)o, p);
                } else if (o instanceof String[]) {
                    for (String oid : (String[])o) {
                        performOperation(what, jc, oid, p);
                    }
                }
            }
        }

        private void scan(Container container, List<JComponent> components) {
            for (Component c : container.getComponents()) {
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if (o instanceof String || o instanceof String[]) {
                        components.add(jc);
                    }
                }
                if (c instanceof Container) {
                    scan((Container)c, components);
                }
            }
        }

        /** Very smart method which tries to set the values in the components correctly
         */
        private void loadData(JComponent jc, String optionID, Preferences node) {

            if (jc instanceof JTextField) {
                JTextField field = (JTextField)jc;
                field.setText(node.get(optionID, getDefaultAsString(optionID)));
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox)jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected(node.getBoolean(optionID, df));
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox)jc;
                String value = node.get(optionID, getDefaultAsString(optionID));
                ComboBoxModel model = createModel(value);
                cb.setModel(model);
                ComboItem item = whichItem(value, model);
                cb.setSelectedItem(item);
            }

        }

        private void storeData(JComponent jc, String optionID, Preferences node) {

            if (jc instanceof JTextField) {
                JTextField field = (JTextField)jc;

                String text = field.getText();

                // XXX test for numbers
                if (isInteger(optionID)) {
                    try {
                        int i = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }

                // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
                // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
                // which is why we must not remove them. Proper solution would be to
                // store formatting preferences to MimeLookup and not use NbPreferences.
                // The problem currently is that MimeLookup based Preferences do not support subnodes.
                if (!optionID.equals(tabSize) &&
                        !optionID.equals(spacesPerTab) && !optionID.equals(indentSize) &&
                        getDefaultAsString(optionID).equals(text)) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, text);
                }
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox)jc;
                if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == checkBox.isSelected()) {
                    node.remove(optionID);
                } else {
                    node.putBoolean(optionID, checkBox.isSelected());
                }
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox)jc;
                // Logger.global.info( cb.getSelectedItem() + " " + optionID);
                String value = ((ComboItem)cb.getSelectedItem()).value;
                if (getDefaultAsString(optionID).equals(value)) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, value);
                }
            }
        }

        private void addListener(JComponent jc) {
            if (jc instanceof JTextField) {
                JTextField field = (JTextField)jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox)jc;
                checkBox.addActionListener(this);
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox)jc;
                cb.addActionListener(this);
            }
        }

        private ComboBoxModel createModel(String value) {

            // is it imports?
            for (ComboItem comboItem : cleanupImports) {
                if (value.equals(comboItem.value)) {
                    return new DefaultComboBoxModel(cleanupImports);
                }
            }

            // is it wrap
            for (ComboItem comboItem : wrap) {
                if (value.equals(comboItem.value)) {
                    return new DefaultComboBoxModel(wrap);
                }
            }

            return null;
        }

        private static ComboItem whichItem(String value, ComboBoxModel model) {

            for (int i = 0; i < model.getSize(); i++) {
                ComboItem item = (ComboItem)model.getElementAt(i);
                if (value.equals(item.value)) {
                    return item;
                }
            }
            return null;
        }

        private static class ComboItem {
            String value;
            String displayName;

            public ComboItem(String value, String key) {
                this.value = value;
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }
        }
    }

    public static class PreviewPreferences extends AbstractPreferences {
        private Map<String, Object> map = new HashMap<>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }

        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return (String)map.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray(array);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // read-only, no subnodes
    public static final class ProxyPreferences extends AbstractPreferences {
        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }

        @Override
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String getSpi(String key) {
            for (Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        @Override
        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<>();
            for (Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[keys.size()]);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    } // End of ProxyPreferences class

    public static interface CodeStyleProducer {
        public CodeStyle create(Preferences preferences);
    }
}
