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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.api.CodeStyle.PreprocessorIndent;
import org.netbeans.modules.cnd.spi.CndDocumentCodeStyleProvider;
import org.openide.util.NbBundle;

/**
 *
 */
public class EditorOptions {
    public static CodeStyleFactory codeStyleFactory;
    /*package*/static final String CODE_STYLE_NODE = "CodeStyle"; //NOI18N
    private static final String LIST_OF_STYLES_PROPERTY = "List_Of_Styles"; //NOI18N
    static {
        Class<?> c = CodeStyle.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private EditorOptions() {
    }

    private static final boolean TRACE = false;
    //indents
    /**
     * How many spaces should be added to the statement that continues
     * on the next line.
     */
    public static final String statementContinuationIndent = "statementContinuationIndent"; // NOI18N 
    public static final int statementContinuationIndentDefault = 8;

    public static final String constructorListContinuationIndent = "constructorListContinuationIndent"; // NOI18N 
    public static final int constructorListContinuationIndentDefault = 0;

    public static final String overrideTabIndents = "overrideTabIndents"; //NOI18N
    public static final boolean overrideTabIndentsDefault = true;

    public static final String indentSize = "indentSize"; // NOI18N
    //public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final int indentSizeDefault = 4;

    public static final String expandTabToSpaces = "expandTabToSpaces"; // NOI18N
    //public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final boolean expandTabToSpacesDefault = true;

    public static final String tabSize = "tabSize"; // NOI18N
    //public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final int tabSizeDefault = 8;

    /**
     * Whether to indent preprocessors positioned at start of line.
     * Those not starting at column 0 of the line will automatically be indented.
     * This setting is to prevent C/C++ code that is compiled with compilers that
     * require the processors to have '#' in column 0.
     * <B>Note:</B>This will not convert formatted preprocessors back to column 0.
     */
    public static final String indentPreprocessorDirectives = "indentPreprocessorDirectives"; //NOI18N
    public static final String indentPreprocessorDirectivesDefault = PreprocessorIndent.START_LINE.name();
    public static final String indentVisibility = "indentVisibility"; //NOI18N
    public static final String indentVisibilityDefault = CodeStyle.VisibilityIndent.NO_INDENT.name();
    public static final String sharpAtStartLine = "sharpAtStartLine"; //NOI18N
    public static final boolean sharpAtStartLineDefault = true;
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final boolean indentCasesFromSwitchDefault = true;
    public static final String absoluteLabelIndent = "absoluteLabelIndent"; //NOI18N
    public static final boolean absoluteLabelIndentDefault = true;

    public static final String indentNamespace = "indentNamespace"; //NOI18N
    public static final boolean indentNamespaceDefault = true;
    
    //BracesPlacement
    public static final String newLineBeforeBraceNamespace = "newLineBeforeBraceNamespace"; //NOI18N
    public static final String newLineBeforeBraceNamespaceDefault = BracePlacement.SAME_LINE.name();
    public static final String newLineBeforeBraceClass = "newLineBeforeBraceClass"; //NOI18N
    public static final String newLineBeforeBraceClassDefault = BracePlacement.SAME_LINE.name();
    /**
     * Whether insert extra new-line before the declaration or not.
     * Values: java.lang.Boolean instances
     * Effect: int foo() {
     *           function();
     *         }
     *           becomes (when set to true)
     *         int foo(test)
     *         {
     *           function();
     *         }
     */
    public static final String newLineBeforeBraceDeclaration = "newLineBeforeBraceDeclaration"; //NOI18N
    public static final String newLineBeforeBraceDeclarationDefault = BracePlacement.SAME_LINE.name();
    public static final String ignoreEmptyFunctionBody = "ignoreEmptyFunctionBody"; //NOI18N
    public static final boolean ignoreEmptyFunctionBodyDefault = false;
    public static final String newLineBeforeBraceLambda = "newLineBeforeBraceLambda"; //NOI18N
    public static final String newLineBeforeBraceLambdaDefault = BracePlacement.SAME_LINE.name();
    
    /**
     * Whether insert extra new-line before the compound bracket or not.
     * Values: java.lang.Boolean instances
     * Effect: if (test) {
     *           function();
     *         }
     *           becomes (when set to true)
     *         if (test)
     *         {
     *           function();
     *         }
     */
    public static final String newLineBeforeBrace = "newLineBeforeBrace"; //NOI18N
    public static final String newLineBeforeBraceDefault = BracePlacement.SAME_LINE.name();
    public static final String newLineBeforeBraceSwitch = "newLineBeforeBraceSwitch"; //NOI18N
    public static final String newLineBeforeBraceSwitchDefault = BracePlacement.SAME_LINE.name();

    //MultilineAlignment
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final boolean alignMultilineArrayInitDefault = false;
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final boolean alignMultilineCallArgsDefault = false;
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final boolean alignMultilineMethodParamsDefault = false;
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final boolean alignMultilineForDefault = false;
    public static final String alignMultilineIfCondition = "alignMultilineIfCondition"; //NOI18N
    public static final boolean alignMultilineIfConditionDefault = false;
    public static final String alignMultilineWhileCondition = "alignMultilineWhileCondition"; //NOI18N
    public static final boolean alignMultilineWhileConditionDefault = false;
    public static final String alignMultilineParen = "alignMultilineParen"; //NOI18N
    public static final boolean alignMultilineParenDefault = false;
    
    //NewLine
    public static final String newLineFunctionDefinitionName = "newLineFunctionDefinitionName"; //NOI18N
    public static final boolean newLineFunctionDefinitionNameDefault = false;
    public static final String newLineCatch = "newLineCatch"; //NOI18N
    public static final boolean newLineCatchDefault = false;
    public static final String newLineElse = "newLineElse"; //NOI18N
    public static final boolean newLineElseDefault = false;
    public static final String newLineWhile = "newLineWhile"; //NOI18N
    public static final boolean newLineWhileDefault = false;

    public static final String spaceKeepExtra = "spaceKeepExtra"; //NOI18N
    public static final boolean spaceKeepExtraDefault = false;

    //SpacesBeforeKeywords
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final boolean spaceBeforeWhileDefault = true;
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final boolean spaceBeforeElseDefault = true;
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final boolean spaceBeforeCatchDefault = true;

    //SpacesBeforeParentheses
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final boolean spaceBeforeMethodDeclParenDefault = false;
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final boolean spaceBeforeMethodCallParenDefault = false;
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final boolean spaceBeforeIfParenDefault = true;
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final boolean spaceBeforeForParenDefault = true;
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final boolean spaceBeforeWhileParenDefault = true;
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final boolean spaceBeforeCatchParenDefault = true;
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final boolean spaceBeforeSwitchParenDefault = true;
    
    //SpacesAroundOperators
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final boolean spaceAroundUnaryOpsDefault = false;
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final boolean spaceAroundBinaryOpsDefault = true;
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final boolean spaceAroundTernaryOpsDefault = true;
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final boolean spaceAroundAssignOpsDefault = true;
    
    //SpacesBeforeLeftBraces
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final boolean spaceBeforeClassDeclLeftBraceDefault = true;
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final boolean spaceBeforeMethodDeclLeftBraceDefault = true;
    public static final String spaceBeforeLambdaLeftBrace = "spaceBeforeLambdaLeftBrace"; //NOI18N
    public static final boolean spaceBeforeLambdaLeftBraceDefault = true;
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final boolean spaceBeforeIfLeftBraceDefault = true;
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final boolean spaceBeforeElseLeftBraceDefault = true;
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final boolean spaceBeforeWhileLeftBraceDefault = true;
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final boolean spaceBeforeForLeftBraceDefault = true;
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final boolean spaceBeforeDoLeftBraceDefault = true;
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final boolean spaceBeforeSwitchLeftBraceDefault = true;
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final boolean spaceBeforeTryLeftBraceDefault = true;
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final boolean spaceBeforeCatchLeftBraceDefault = true;
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final boolean spaceBeforeArrayInitLeftBraceDefault = false;
    
    //SpacesWithinParentheses
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final boolean spaceWithinParensDefault = false;
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final boolean spaceWithinMethodDeclParensDefault = false;
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final boolean spaceWithinMethodCallParensDefault = false;
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final boolean spaceWithinIfParensDefault = false;
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final boolean spaceWithinForParensDefault = false;
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final boolean spaceWithinWhileParensDefault = false;
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final boolean spaceWithinSwitchParensDefault = false;
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final boolean spaceWithinCatchParensDefault = false;
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final boolean spaceWithinTypeCastParensDefault = false;
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final boolean spaceWithinBracesDefault = false;
    public static final String spaceBeforeKeywordParen = "spaceBeforeKeywordParen"; //NOI18N
    public static final boolean spaceBeforeKeywordParenDefault = true;
    
    //SpacesOther
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final boolean spaceBeforeCommaDefault = false;
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final boolean spaceAfterCommaDefault = true;
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final boolean spaceBeforeSemiDefault = false;
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final boolean spaceAfterSemiDefault = true;
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final boolean spaceBeforeColonDefault = true;
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final boolean spaceAfterColonDefault = true;
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
    public static final boolean spaceAfterTypeCastDefault = true;
    public static final String spaceAfterOperatorKeyword = "spaceAfterOperatorKeyword"; //NOI18N
    public static final boolean spaceAfterOperatorKeywordfault = false;
    
    //BlankLines
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final int blankLinesBeforeClassDefault = 1;    
    //public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    //public static final int blankLinesAfterClassDefault = 0;    
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final int blankLinesAfterClassHeaderDefault = 0;    
    //public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    //public static final int blankLinesBeforeFieldsDefault = 0;    
    //public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    //public static final int blankLinesAfterFieldsDefault = 0;    
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final int blankLinesBeforeMethodsDefault = 1;    
    //public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    //public static final int blankLinesAfterMethodsDefault = 0;    

    //Other
    /** Whether the '*' should be added at the new line * in comment */
    public static final String addLeadingStarInComment = "addLeadingStarInComment"; // NOI18N
    public static final Boolean addLeadingStarInCommentDefault = true;
    /** Whether the block comment should be used by Comment action */
    public static final String useBlockComment = "useBlockComment"; // NOI18N
    public static final Boolean useBlockCommentDefault = false;
    /* Whether the 'inline' keyword should be used when generate getters/setters */
    public static final String useInlineKeyword = "useInlineKeyword"; // NOI18N
    public static final Boolean useInlineKeywordDefault = false;
    
    private static final String APACHE_PROFILE = "Apache"; // NOI18N
    private static final String DEFAULT_PROFILE = "Default"; // NOI18N
    private static final String GNU_PROFILE = "GNU"; // NOI18N
    private static final String LUNIX_PROFILE = "Linux"; // NOI18N
    public static final String ANSI_PROFILE = "ANSI"; // NOI18N
    private static final String OPEN_SOLARIS_PROFILE = "OpenSolaris"; // NOI18N
    private static final String K_AND_R_PROFILE = "KandR"; // NOI18N
    private static final String MYSQL_PROFILE = "MySQL"; // NOI18N
    private static final String WHITESMITHS_PROFILE = "Whitesmiths"; // NOI18N

    static final String[] PREDEFINED_STYLES = new String[] {
                                 DEFAULT_PROFILE, APACHE_PROFILE, GNU_PROFILE,
                                 LUNIX_PROFILE, ANSI_PROFILE, OPEN_SOLARIS_PROFILE,
                                 K_AND_R_PROFILE, MYSQL_PROFILE, WHITESMITHS_PROFILE
    };

    private static Map<String,Object> defaults;
    private static Map<String,Map<String,Object>> namedDefaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        defaults = new HashMap<String,Object>();
        // Indents
        defaults.put(overrideTabIndents, overrideTabIndentsDefault);
        defaults.put(indentSize, indentSizeDefault);
        defaults.put(expandTabToSpaces, expandTabToSpacesDefault);
        defaults.put(tabSize, tabSizeDefault);
        defaults.put(statementContinuationIndent,statementContinuationIndentDefault);
        defaults.put(constructorListContinuationIndent,constructorListContinuationIndentDefault);
        defaults.put(indentPreprocessorDirectives,indentPreprocessorDirectivesDefault);
        defaults.put(indentVisibility,indentVisibilityDefault);
        defaults.put(sharpAtStartLine, sharpAtStartLineDefault);
        defaults.put(indentNamespace, indentNamespaceDefault);
        defaults.put(indentCasesFromSwitch, indentCasesFromSwitchDefault);
        defaults.put(absoluteLabelIndent, absoluteLabelIndentDefault);
        defaults.put(spaceKeepExtra, spaceKeepExtraDefault);
        //BracesPlacement
        defaults.put(newLineBeforeBraceNamespace,newLineBeforeBraceNamespaceDefault);
        defaults.put(newLineBeforeBraceClass,newLineBeforeBraceClassDefault);
        defaults.put(newLineBeforeBraceDeclaration,newLineBeforeBraceDeclarationDefault);
        defaults.put(ignoreEmptyFunctionBody,ignoreEmptyFunctionBodyDefault);
        defaults.put(newLineBeforeBraceLambda,newLineBeforeBraceLambdaDefault);
        defaults.put(newLineBeforeBraceSwitch,newLineBeforeBraceSwitchDefault);
        defaults.put(newLineBeforeBrace,newLineBeforeBraceDefault);
        //MultilineAlignment
        defaults.put(alignMultilineArrayInit,alignMultilineArrayInitDefault);
        defaults.put(alignMultilineCallArgs,alignMultilineCallArgsDefault);
        defaults.put(alignMultilineMethodParams,alignMultilineMethodParamsDefault);
        defaults.put(alignMultilineFor,alignMultilineForDefault);
        defaults.put(alignMultilineIfCondition,alignMultilineIfConditionDefault);
        defaults.put(alignMultilineWhileCondition,alignMultilineWhileConditionDefault);
        defaults.put(alignMultilineParen,alignMultilineParenDefault);
        //NewLine
        defaults.put(newLineFunctionDefinitionName,newLineFunctionDefinitionNameDefault);
        defaults.put(newLineCatch,newLineCatchDefault);
        defaults.put(newLineElse,newLineElseDefault);
        defaults.put(newLineWhile,newLineWhileDefault);
        //SpacesBeforeKeywords
        defaults.put(spaceBeforeWhile,spaceBeforeWhileDefault);
        defaults.put(spaceBeforeElse,spaceBeforeElseDefault);
        defaults.put(spaceBeforeCatch,spaceBeforeCatchDefault);
        //SpacesBeforeParentheses
        defaults.put(spaceBeforeMethodDeclParen,spaceBeforeMethodDeclParenDefault);
        defaults.put(spaceBeforeMethodCallParen,spaceBeforeMethodCallParenDefault);
        defaults.put(spaceBeforeIfParen,spaceBeforeIfParenDefault);
        defaults.put(spaceBeforeForParen,spaceBeforeForParenDefault);
        defaults.put(spaceBeforeWhileParen,spaceBeforeWhileParenDefault);
        defaults.put(spaceBeforeCatchParen,spaceBeforeCatchParenDefault);
        defaults.put(spaceBeforeSwitchParen,spaceBeforeSwitchParenDefault);
        //SpacesAroundOperators
        defaults.put(spaceAroundUnaryOps,spaceAroundUnaryOpsDefault);
        defaults.put(spaceAroundBinaryOps,spaceAroundBinaryOpsDefault);
        defaults.put(spaceAroundTernaryOps,spaceAroundTernaryOpsDefault);
        defaults.put(spaceAroundAssignOps,spaceAroundAssignOpsDefault);
        //SpacesBeforeLeftBraces
        defaults.put(spaceBeforeClassDeclLeftBrace,spaceBeforeClassDeclLeftBraceDefault);
        defaults.put(spaceBeforeMethodDeclLeftBrace,spaceBeforeMethodDeclLeftBraceDefault);
        defaults.put(spaceBeforeLambdaLeftBrace,spaceBeforeLambdaLeftBraceDefault);
        defaults.put(spaceBeforeIfLeftBrace,spaceBeforeIfLeftBraceDefault);
        defaults.put(spaceBeforeElseLeftBrace,spaceBeforeElseLeftBraceDefault);
        defaults.put(spaceBeforeWhileLeftBrace,spaceBeforeWhileLeftBraceDefault);
        defaults.put(spaceBeforeForLeftBrace,spaceBeforeForLeftBraceDefault);
        defaults.put(spaceBeforeDoLeftBrace,spaceBeforeDoLeftBraceDefault);
        defaults.put(spaceBeforeSwitchLeftBrace,spaceBeforeSwitchLeftBraceDefault);
        defaults.put(spaceBeforeTryLeftBrace,spaceBeforeTryLeftBraceDefault);
        defaults.put(spaceBeforeCatchLeftBrace,spaceBeforeCatchLeftBraceDefault);
        defaults.put(spaceBeforeArrayInitLeftBrace,spaceBeforeArrayInitLeftBraceDefault);
        //SpacesWithinParentheses
        defaults.put(spaceWithinParens,spaceWithinParensDefault);
        defaults.put(spaceWithinMethodDeclParens,spaceWithinMethodDeclParensDefault);
        defaults.put(spaceWithinMethodCallParens,spaceWithinMethodCallParensDefault);
        defaults.put(spaceWithinIfParens,spaceWithinIfParensDefault);
        defaults.put(spaceWithinForParens,spaceWithinForParensDefault);
        defaults.put(spaceWithinWhileParens,spaceWithinWhileParensDefault);
        defaults.put(spaceWithinSwitchParens,spaceWithinSwitchParensDefault);
        defaults.put(spaceWithinCatchParens,spaceWithinCatchParensDefault);
        defaults.put(spaceWithinTypeCastParens,spaceWithinTypeCastParensDefault);
        defaults.put(spaceWithinBraces,spaceWithinBracesDefault);
        defaults.put(spaceBeforeKeywordParen,spaceBeforeKeywordParenDefault);
        //SpacesOther
        defaults.put(spaceBeforeComma,spaceBeforeCommaDefault);
        defaults.put(spaceAfterComma,spaceAfterCommaDefault);
        defaults.put(spaceBeforeSemi,spaceBeforeSemiDefault);
        defaults.put(spaceAfterSemi,spaceAfterSemiDefault);
        defaults.put(spaceBeforeColon,spaceBeforeColonDefault);
        defaults.put(spaceAfterColon,spaceAfterColonDefault);
        defaults.put(spaceAfterTypeCast,spaceAfterTypeCastDefault);
        defaults.put(spaceAfterOperatorKeyword,spaceAfterOperatorKeywordfault);
        //BlankLines
        defaults.put(blankLinesBeforeClass,blankLinesBeforeClassDefault);
        //defaults.put(blankLinesAfterClass,blankLinesAfterClassDefault);
        defaults.put(blankLinesAfterClassHeader,blankLinesAfterClassHeaderDefault);
        //defaults.put(blankLinesBeforeFields,blankLinesBeforeFieldsDefault);
        //defaults.put(blankLinesAfterFields,blankLinesAfterFieldsDefault);
        defaults.put(blankLinesBeforeMethods,blankLinesBeforeMethodsDefault);
        //defaults.put(blankLinesAfterMethods,blankLinesAfterMethodsDefault);      
        //Other
        defaults.put(addLeadingStarInComment,addLeadingStarInCommentDefault);
        defaults.put(useBlockComment,useBlockCommentDefault);
        defaults.put(useInlineKeyword,useInlineKeywordDefault);

        namedDefaults = new HashMap<String,Map<String,Object>>();

        Map<String,Object> apache = new HashMap<String,Object>();
        namedDefaults.put(APACHE_PROFILE, apache);
        apache.put(indentCasesFromSwitch, false);
        apache.put(alignMultilineCallArgs, true);
        apache.put(alignMultilineMethodParams, true);
        apache.put(alignMultilineIfCondition, true);
        apache.put(alignMultilineWhileCondition, true);
        apache.put(newLineCatch, true);
        apache.put(newLineElse, true);
        apache.put(newLineWhile, true);
        apache.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE.name());
        apache.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE.name());
        apache.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        apache.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());
// I see that GNU style differ from apache only in half indent
// Is it true?
        Map<String,Object> gnu = new HashMap<String,Object>();
        namedDefaults.put(GNU_PROFILE, gnu);
        gnu.put(indentCasesFromSwitch, false);
        gnu.put(alignMultilineCallArgs, true);
        gnu.put(alignMultilineMethodParams, true);
        gnu.put(alignMultilineIfCondition, true);
        gnu.put(alignMultilineWhileCondition, true);
        gnu.put(alignMultilineParen, true);
        gnu.put(spaceBeforeMethodCallParen, true);
        gnu.put(spaceBeforeMethodDeclParen, true);
        gnu.put(newLineFunctionDefinitionName, true);
        gnu.put(newLineCatch, true);
        gnu.put(newLineElse, true);
        gnu.put(newLineWhile, true);
        gnu.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(newLineBeforeBraceSwitch, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(newLineBeforeBrace, BracePlacement.NEW_LINE_HALF_INDENTED.name());
        gnu.put(ignoreEmptyFunctionBody,true);

        //LUNIX_PROFILE
        Map<String,Object> lunix = new HashMap<String,Object>();
        namedDefaults.put(LUNIX_PROFILE, lunix);
        lunix.put(indentCasesFromSwitch, false);
        lunix.put(indentSize, 8);
        lunix.put(expandTabToSpaces, false);
        lunix.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        lunix.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());
        lunix.put(spaceBeforeKeywordParen, false);
        
        //ANSI_PROFILE
        Map<String,Object> ansi = new HashMap<String,Object>();
        namedDefaults.put(ANSI_PROFILE, ansi);
        ansi.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE.name());
        ansi.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE.name());
        ansi.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        ansi.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());
        ansi.put(newLineBeforeBraceSwitch, BracePlacement.NEW_LINE.name());
        ansi.put(newLineBeforeBrace, BracePlacement.NEW_LINE.name());
        ansi.put(alignMultilineMethodParams, true);
        ansi.put(alignMultilineCallArgs, true);
        ansi.put(newLineCatch, true);
        ansi.put(newLineElse, true);
        ansi.put(newLineWhile, true);
        ansi.put(indentCasesFromSwitch, false);
        ansi.put(indentNamespace, false);
        
        //OPEN_SOLARIS_PROFILE
        Map<String,Object> solaris = new HashMap<String,Object>();
        namedDefaults.put(OPEN_SOLARIS_PROFILE, solaris);
        solaris.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE.name());
        solaris.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE.name());
        solaris.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        solaris.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());
        solaris.put(newLineFunctionDefinitionName, true);
        solaris.put(indentSize, 8);
        solaris.put(expandTabToSpaces, false);
        solaris.put(alignMultilineCallArgs, true);
        solaris.put(alignMultilineMethodParams, true);
        solaris.put(alignMultilineIfCondition, true);
        solaris.put(alignMultilineWhileCondition, true);
        solaris.put(alignMultilineFor, true);
        solaris.put(indentCasesFromSwitch, false);

        //K_AND_R_PROFILE
        Map<String,Object> KandR = new HashMap<String,Object>();
        namedDefaults.put(K_AND_R_PROFILE, KandR);
        KandR.put(absoluteLabelIndent, false);
        KandR.put(indentCasesFromSwitch, false);
        KandR.put(indentNamespace, false);
        KandR.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        KandR.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());

        //MYSQL_PROFILE
        Map<String,Object> mysql = new HashMap<String,Object>();
        namedDefaults.put(MYSQL_PROFILE, mysql);
        mysql.put(indentCasesFromSwitch, false);
        mysql.put(indentSize, 2);
        mysql.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE.name());
        mysql.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE.name());
        mysql.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE.name());
        mysql.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE.name());
        mysql.put(newLineBeforeBrace, BracePlacement.NEW_LINE.name());
        mysql.put(alignMultilineCallArgs, true);
        mysql.put(alignMultilineWhileCondition, true);
        mysql.put(alignMultilineFor, true);
        mysql.put(alignMultilineMethodParams, true);
        mysql.put(alignMultilineIfCondition, true);
        mysql.put(spaceAroundAssignOps, false);
        mysql.put(spaceKeepExtra, true);
        mysql.put(addLeadingStarInComment, false);
        
        //WHITESMITHS_PROFILE
        Map<String,Object> whitesmiths = new HashMap<String,Object>();
        namedDefaults.put(WHITESMITHS_PROFILE, whitesmiths);
        whitesmiths.put(alignMultilineArrayInit, true);
        whitesmiths.put(alignMultilineCallArgs, true);
        whitesmiths.put(alignMultilineCallArgs, true);
        whitesmiths.put(alignMultilineFor, true);
        whitesmiths.put(alignMultilineIfCondition, true);
        whitesmiths.put(alignMultilineMethodParams, true);
        whitesmiths.put(alignMultilineParen, true);
        whitesmiths.put(alignMultilineWhileCondition, true);
        whitesmiths.put(newLineBeforeBrace, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineBeforeBraceClass, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineBeforeBraceDeclaration, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineBeforeBraceLambda, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineBeforeBraceNamespace, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineBeforeBraceSwitch, BracePlacement.NEW_LINE_FULL_INDENTED.name());
        whitesmiths.put(newLineCatch, true);
        whitesmiths.put(newLineElse, true);
        whitesmiths.put(newLineWhile, true);
        
        //DEFAULT_PROFILE
        Map<String,Object> netbeans = new HashMap<String,Object>();
        namedDefaults.put(DEFAULT_PROFILE, netbeans);
        netbeans.put(overrideTabIndents, false);
    }

    public static Object getDefault(CodeStyle.Language language, String styleId, String id){
        Map<String,Object> map = namedDefaults.get(styleId);
        if (map != null){
            Object res = map.get(id);
            if (res != null){
                return res;
            }
        }
        if (styleId.indexOf('_') > 0) {
            styleId = styleId.substring(0, styleId.indexOf('_'));
            map = namedDefaults.get(styleId);
            if (map != null){
                Object res = map.get(id);
                if (res != null){
                    return res;
                }
            }
        }
        return defaults.get(id);
    }
    
    public static String getCurrentProfileId(CodeStyle.Language language, Document doc) {
        CndDocumentCodeStyleProvider csProvider = null;
        if (doc != null) {
            csProvider = (CndDocumentCodeStyleProvider) doc.getProperty(CndDocumentCodeStyleProvider.class);
        }
        if (csProvider != null) {
            String currentCodeStyle = csProvider.getCurrentCodeStyle(language.toMime(), doc);
            if (currentCodeStyle != null) {
                return currentCodeStyle;
            }
        }
        return CodeStylePreferencesProvider.INSTANCE.forDocument(doc, language.toMime()).node(CODE_STYLE_NODE).get(language.currentPropertyName(), DEFAULT_PROFILE); // NOI18N
    }

    public static void setCurrentProfileId(CodeStyle.Language language, String style) {
        CodeStylePreferencesProvider.INSTANCE.forDocument(null, language.toMime()).node(CODE_STYLE_NODE).put(language.currentPropertyName(), style); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EditorOptions.class, key);
    }

    public static String getStyleDisplayName(CodeStyle.Language language, String style) {
        for (String name : EditorOptions.PREDEFINED_STYLES) {
            if (style.equals(name)) {
                return getString(style + "_Name"); // NOI18N
            }
        }
        return CodeStylePreferencesProvider.INSTANCE.forDocument(null, language.toMime()).node(CODE_STYLE_NODE).get(style+"_Style_Name", style); // NOI18N
    }
    
    public static Preferences getPreferences(CodeStyle.Language language, String profileId) {
        return CodeStylePreferencesProvider.INSTANCE.forDocument(null, language.toMime()).node(language.prefNodeName()).node(profileId); // NOI18N
    }

    public static List<String> getAllStyles(CodeStyle.Language language) {
        StringBuilder def = new StringBuilder();
        for(String s: PREDEFINED_STYLES){
            if (def.length() > 0){
                def.append(',');
            }
            def.append(s);
        }
        String styles = CodeStylePreferencesProvider.INSTANCE.forDocument(null, language.toMime()).node(language.prefNodeName()).get(LIST_OF_STYLES_PROPERTY, def.toString()); // NOI18N
        List<String> res = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(styles,","); // NOI18N
        while(st.hasMoreTokens()) {
            res.add(st.nextToken());
        }
        return res;
    }

    public static void setAllStyles(CodeStyle.Language language, String list) {
        CodeStylePreferencesProvider.INSTANCE.forDocument(null, language.toMime()).node(language.prefNodeName()).put(LIST_OF_STYLES_PROPERTY, list); // NOI18N
    }

    public static CodeStyle createCodeStyle(CodeStyle.Language language, String profileID, Preferences p, boolean useOverrideOption) {
        return codeStyleFactory.create(language, profileID, p, useOverrideOption);
    }

    public static Preferences getPreferences(CodeStyle codeStyle){
        return codeStyleFactory.getPreferences(codeStyle);
    }

    public static void resetToDefault(CodeStyle codeStyle){
        Preferences preferences = getPreferences(codeStyle);
        for(Map.Entry<String,Object> entry : defaults.entrySet()){
            if (entry.getValue() instanceof Boolean){
                preferences.putBoolean(entry.getKey(), (Boolean)entry.getValue());
            } else if (entry.getValue() instanceof Integer){
                preferences.putInt(entry.getKey(), (Integer)entry.getValue());
            } else {
                preferences.put(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    public static void resetToDefault(CodeStyle codeStyle, String name){
        Preferences preferences = getPreferences(codeStyle);
        for(Map.Entry<String,Object> entry : namedDefaults.get(name).entrySet()){
            if (entry.getValue() instanceof Boolean){
                preferences.putBoolean(entry.getKey(), (Boolean)entry.getValue());
            } else if (entry.getValue() instanceof Integer){
                preferences.putInt(entry.getKey(), (Integer)entry.getValue());
            } else {
                preferences.put(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    public static Set<String> keys(){
        return defaults.keySet();
    }

    public static void setPreferences(CodeStyle codeStyle, Preferences preferences){
        codeStyleFactory.setPreferences(codeStyle, preferences);
    }

    public static void updateSimplePreferences(CodeStyle.Language language, CodeStyle codeStyle) {
        updateSimplePreferences(MimeLookup.getLookup(language.toMime()).lookup(Preferences.class), codeStyle);
    }

    private static final Set<String> set = new HashSet<String>();
    private static void updateSimplePreferences(Preferences p, CodeStyle codeStyle) {
        if (p != null) {
            if (TRACE) {
                if (!set.contains(p.absolutePath())) {
                    set.add(p.absolutePath());
                    p.addPreferenceChangeListener(new PreferenceChangeListener() {
                        @Override
                        public void preferenceChange(PreferenceChangeEvent evt) {
                            System.err.println("Changed "+evt.getKey()+"="+evt.getNewValue()+" in preferences "+evt.getNode().absolutePath());
                        }
                    });
                }
            }
            if (p.getBoolean(overrideTabIndents, overrideTabIndentsDefault)) {
                if (TRACE) {
                    System.err.println("Set language "+codeStyle+" preferences from CND storage");
                    System.err.println(SimpleValueNames.TAB_SIZE+"="+codeStyle.getTabSize());
                    System.err.println(SimpleValueNames.SPACES_PER_TAB+"="+codeStyle.indentSize());
                    System.err.println(SimpleValueNames.EXPAND_TABS+"="+codeStyle.expandTabToSpaces());
                    System.err.println(SimpleValueNames.INDENT_SHIFT_WIDTH+"="+codeStyle.indentSize());
                }
                p.putInt(SimpleValueNames.TAB_SIZE, codeStyle.getTabSize());
                p.putInt(SimpleValueNames.SPACES_PER_TAB, codeStyle.indentSize());
                p.putBoolean(SimpleValueNames.EXPAND_TABS, codeStyle.expandTabToSpaces());
                p.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, codeStyle.indentSize());
            } else {
                Preferences global = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
                if (global != null) {
                    if (TRACE) {
                        System.err.println("Set language "+codeStyle+" preferences from Global storage");
                        System.err.println(SimpleValueNames.TAB_SIZE+"="+global.getInt(SimpleValueNames.TAB_SIZE, tabSizeDefault));
                        System.err.println(SimpleValueNames.EXPAND_TABS+"="+global.getBoolean(SimpleValueNames.EXPAND_TABS, expandTabToSpacesDefault));
                        System.err.println(SimpleValueNames.SPACES_PER_TAB+"="+global.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, indentSizeDefault));
                        System.err.println(SimpleValueNames.INDENT_SHIFT_WIDTH+"="+global.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, indentSizeDefault));
                    }
                    p.remove(SimpleValueNames.TAB_SIZE);
                    p.remove(SimpleValueNames.EXPAND_TABS);
                    p.remove(SimpleValueNames.SPACES_PER_TAB);
                    p.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
                }
            }
        }
    }

    public static int getGlobalTabSize(){
        Preferences global = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        if (global != null) {
            return global.getInt(SimpleValueNames.TAB_SIZE, tabSizeDefault);
        }
        return tabSizeDefault;
    }

    public static boolean getGlobalExpandTabs(){
        Preferences global = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        if (global != null) {
            return global.getBoolean(SimpleValueNames.EXPAND_TABS, expandTabToSpacesDefault);
        }
        return expandTabToSpacesDefault;
    }

    public static int getGlobalIndentSize(){
        Preferences global = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        if (global != null) {
            return global.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, indentSizeDefault);
        }
        return indentSizeDefault;
    }

    public static interface CodeStyleFactory {
        CodeStyle create(CodeStyle.Language language, String profileID, Preferences preferences, boolean useOverrideOption);
        Preferences getPreferences(CodeStyle codeStyle);
        void setPreferences(CodeStyle codeStyle, Preferences preferences);
    }
}
