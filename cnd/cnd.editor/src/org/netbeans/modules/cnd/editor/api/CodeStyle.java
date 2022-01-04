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

package org.netbeans.modules.cnd.editor.api;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.NbBundle;

/**
 *
 */
public final class CodeStyle {
    static {
        EditorOptions.codeStyleFactory = new FactoryImpl();
    }

    private static final Map<String, CodeStyle> INSTANCE_C = new HashMap<String, CodeStyle>();
    private static final Map<String, CodeStyle> INSTANCE_H = new HashMap<String, CodeStyle>();
    private static final Map<String, CodeStyle> INSTANCE_CPP = new HashMap<String, CodeStyle>();
    private static final Map<String, CodeStyle> CLANG_FORMAT = new HashMap<String, CodeStyle>();
    private final Language language;
    private final String profileId;
    private Preferences preferences;
    private final boolean useOverrideOptions;

    private CodeStyle(Language language, String profileId, Preferences preferences, boolean useOverrideOptions) {
        this.language = language;
        this.profileId = profileId;
        this.preferences = preferences;
        this.useOverrideOptions = useOverrideOptions;
    }

    public synchronized static CodeStyle getDefault(Language language, Document doc) {
        String profileId = EditorOptions.getCurrentProfileId(language, doc);
        CodeStyle c;
        if (profileId.indexOf(':') > 0) {
            // it is a content of .clang-format
            c = CLANG_FORMAT.get(profileId);
            return c;
        }
        switch(language) {
            case C:
                c = INSTANCE_C.get(profileId);
                if (c == null) {
                    c = create(language, profileId);
                    setSimplePreferences(language, c);
                    INSTANCE_C.put(profileId, c);
                }
                return c;
            case HEADER:
                c = INSTANCE_H.get(profileId);
                if (c == null) {
                    c = create(language, profileId);
                    setSimplePreferences(language, c);
                    INSTANCE_H.put(profileId, c);
                }
                return c;
            case CPP:
            default:
                c = INSTANCE_CPP.get(profileId);
                if (c == null) {
                    c = create(language, profileId);
                    setSimplePreferences(language, c);
                    INSTANCE_CPP.put(profileId, c);
                }
                return c;
        }
    }
    
    private static void setSimplePreferences(Language language, CodeStyle codeStyle){
        EditorOptions.updateSimplePreferences(language, codeStyle);
    }

    public synchronized static CodeStyle getDefault(Document doc) {
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (mimeType == null) {
            System.out.println("Undefined MIME type of document "+doc); // NOI18N
            //if (doc instanceof BaseDocument) {
            //    if (CKit.class.equals(((BaseDocument)doc).getKitClass())) {
            //        return getDefault(Language.C);
            //    }
            //}
        } else {
            if (mimeType.equals(MIMENames.C_MIME_TYPE)) {
                return getDefault(Language.C, doc);
            } else if (mimeType.equals(MIMENames.HEADER_MIME_TYPE)) {
                return getDefault(Language.HEADER, doc);
            }
        }
        return getDefault(Language.CPP, doc);
    }

    private static CodeStyle create(Language language, String profileId) {
        return new CodeStyle(language, profileId, EditorOptions.getPreferences(language, profileId), true);
    }

    // General indents ------------------------------------------------
    
    private boolean isOverideTabIndents(){
        if (useOverrideOptions) {
            return getOption(EditorOptions.overrideTabIndents,
                             EditorOptions.overrideTabIndentsDefault);
        }
        return true;
    }

    public int indentSize() {
        if (isOverideTabIndents()){
            return getOption(EditorOptions.indentSize,
                             EditorOptions.indentSizeDefault);
        }
        return EditorOptions.getGlobalIndentSize();
    }

    public boolean expandTabToSpaces() {
        if (isOverideTabIndents()){
            return getOption(EditorOptions.expandTabToSpaces,
                             EditorOptions.expandTabToSpacesDefault);
        }
        return EditorOptions.getGlobalExpandTabs();
    }

    public int getTabSize() {
        if (isOverideTabIndents()){
            return getOption(EditorOptions.tabSize,
                             EditorOptions.tabSizeDefault);
        }
        return EditorOptions.getGlobalTabSize();
    }

    public int getFormatStatementContinuationIndent() {
        return getOption(EditorOptions.statementContinuationIndent,
                         EditorOptions.statementContinuationIndentDefault);
    }

    public int getConstructorInitializerListContinuationIndent() {
        return getOption(EditorOptions.constructorListContinuationIndent,
                         EditorOptions.constructorListContinuationIndentDefault);
    }

    public PreprocessorIndent indentPreprocessorDirectives(){
        return PreprocessorIndent.valueOf(getOption(EditorOptions.indentPreprocessorDirectives,
                                      EditorOptions.indentPreprocessorDirectivesDefault));
    }

    public VisibilityIndent indentVisibility(){
        return VisibilityIndent.valueOf(getOption(EditorOptions.indentVisibility,
                                      EditorOptions.indentVisibilityDefault));
    }
    
    public boolean indentNamespace() {
        return getOption(EditorOptions.indentNamespace,
                         EditorOptions.indentNamespaceDefault);
    }

    public boolean indentCasesFromSwitch() {
        return getOption(EditorOptions.indentCasesFromSwitch,
                         EditorOptions.indentCasesFromSwitchDefault);
    }

    public boolean absoluteLabelIndent() {
        return getOption(EditorOptions.absoluteLabelIndent,
                         EditorOptions.absoluteLabelIndentDefault);
    }

    public boolean sharpAtStartLine(){
        return getOption(EditorOptions.sharpAtStartLine,
                         EditorOptions.sharpAtStartLineDefault);
    }

    public boolean spaceKeepExtra(){
        return getOption(EditorOptions.spaceKeepExtra,
                         EditorOptions.spaceKeepExtraDefault);
    }
    // indents ------------------------------------------------
    public boolean spaceBeforeMethodDeclParen() {
        return getOption(EditorOptions.spaceBeforeMethodDeclParen,
                         EditorOptions.spaceBeforeMethodDeclParenDefault);
    }
    public boolean spaceBeforeMethodCallParen() {
        return getOption(EditorOptions.spaceBeforeMethodCallParen,
                         EditorOptions.spaceBeforeMethodCallParenDefault);
    }
    public boolean spaceBeforeIfParen() {
        return getOption(EditorOptions.spaceBeforeIfParen,
                         EditorOptions.spaceBeforeIfParenDefault);
    }
    public boolean spaceBeforeForParen() {
        return getOption(EditorOptions.spaceBeforeForParen,
                         EditorOptions.spaceBeforeForParenDefault);
    }
    public boolean spaceBeforeWhileParen() {
        return getOption(EditorOptions.spaceBeforeWhileParen,
                         EditorOptions.spaceBeforeWhileParenDefault);
    }
    public boolean spaceBeforeCatchParen() {
        return getOption(EditorOptions.spaceBeforeCatchParen,
                         EditorOptions.spaceBeforeCatchParenDefault);
    }
    public boolean spaceBeforeSwitchParen() {
        return getOption(EditorOptions.spaceBeforeSwitchParen,
                         EditorOptions.spaceBeforeSwitchParenDefault);
    }
    public boolean spaceBeforeKeywordParen() {
        return getOption(EditorOptions.spaceBeforeKeywordParen,
                         EditorOptions.spaceBeforeKeywordParenDefault);
    }

    public BracePlacement getFormatNewlineBeforeBraceNamespace() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceNamespace,
                                      EditorOptions.newLineBeforeBraceNamespaceDefault));
    }

    public BracePlacement getFormatNewlineBeforeBraceClass() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceClass,
                                      EditorOptions.newLineBeforeBraceClassDefault));
    }

    public BracePlacement getFormatNewlineBeforeBraceDeclaration() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceDeclaration,
                                      EditorOptions.newLineBeforeBraceDeclarationDefault));
    }
    public boolean ignoreEmptyFunctionBody(){
        return getOption(EditorOptions.ignoreEmptyFunctionBody,
                         EditorOptions.ignoreEmptyFunctionBodyDefault);
    }
    public BracePlacement getFormatNewlineBeforeBraceLambda() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceLambda,
                                      EditorOptions.newLineBeforeBraceLambdaDefault));
    }

    public BracePlacement getFormatNewLineBeforeBraceSwitch() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceSwitch,
                                      EditorOptions.newLineBeforeBraceSwitchDefault));
    }

    public BracePlacement getFormatNewlineBeforeBrace() {
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBrace,
                                      EditorOptions.newLineBeforeBraceDefault));
    }

    //NewLine
    public boolean newLineCatch(){
        return getOption(EditorOptions.newLineCatch,
                         EditorOptions.newLineCatchDefault);
    }
    public boolean newLineElse(){
        return getOption(EditorOptions.newLineElse,
                         EditorOptions.newLineElseDefault);
    }
    public boolean newLineWhile(){
        return getOption(EditorOptions.newLineWhile,
                         EditorOptions.newLineWhileDefault);
    }
    public boolean newLineFunctionDefinitionName(){
        return getOption(EditorOptions.newLineFunctionDefinitionName,
                         EditorOptions.newLineFunctionDefinitionNameDefault);
    }
    
    public boolean getFormatLeadingStarInComment() {
        return getOption(EditorOptions.addLeadingStarInComment,
                         EditorOptions.addLeadingStarInCommentDefault);
    }

    public boolean getUseBlockComment() {
        return getOption(EditorOptions.useBlockComment,
                EditorOptions.useBlockCommentDefault);
    }
    
    public boolean getUseInlineKeyword() {
        return getOption(EditorOptions.useInlineKeyword,
                EditorOptions.useInlineKeywordDefault);
    }
    
    //MultilineAlignment
    public boolean alignMultilineCallArgs() {
        return getOption(EditorOptions.alignMultilineCallArgs,
                         EditorOptions.alignMultilineCallArgsDefault);
    }

    public boolean alignMultilineMethodParams() {
        return getOption(EditorOptions.alignMultilineMethodParams,
                         EditorOptions.alignMultilineMethodParamsDefault);
    }

    public boolean alignMultilineFor() {
        return getOption(EditorOptions.alignMultilineFor,
                         EditorOptions.alignMultilineForDefault);
    }
    public boolean alignMultilineIfCondition() {
        return getOption(EditorOptions.alignMultilineIfCondition,
                         EditorOptions.alignMultilineIfConditionDefault);
    }
    public boolean alignMultilineWhileCondition() {
        return getOption(EditorOptions.alignMultilineWhileCondition,
                         EditorOptions.alignMultilineWhileConditionDefault);
    }
    public boolean alignMultilineParen() {
        return getOption(EditorOptions.alignMultilineParen,
                         EditorOptions.alignMultilineParenDefault);
    }
    public boolean alignMultilineArrayInit() {
        return getOption(EditorOptions.alignMultilineArrayInit,
                         EditorOptions.alignMultilineArrayInitDefault);
    }

    //SpacesAroundOperators
    public boolean spaceAroundUnaryOps() {
        return getOption(EditorOptions.spaceAroundUnaryOps,
                         EditorOptions.spaceAroundUnaryOpsDefault);
    }
    public boolean spaceAroundBinaryOps() {
        return getOption(EditorOptions.spaceAroundBinaryOps,
                         EditorOptions.spaceAroundBinaryOpsDefault);
    }
    public boolean spaceAroundAssignOps() {
        return getOption(EditorOptions.spaceAroundAssignOps,
                         EditorOptions.spaceAroundAssignOpsDefault);
    }
    public boolean spaceAroundTernaryOps() {
        return getOption(EditorOptions.spaceAroundTernaryOps,
                         EditorOptions.spaceAroundTernaryOpsDefault);
    }
            
    public boolean spaceBeforeWhile() {
        return getOption(EditorOptions.spaceBeforeWhile,
                         EditorOptions.spaceBeforeWhileDefault);
    }
    
    public boolean spaceBeforeElse() {
        return getOption(EditorOptions.spaceBeforeElse,
                         EditorOptions.spaceBeforeElseDefault);
    }

    public boolean spaceBeforeCatch() {
        return getOption(EditorOptions.spaceBeforeCatch,
                         EditorOptions.spaceBeforeCatchDefault);
    }

    public boolean spaceBeforeComma() {
        return getOption(EditorOptions.spaceBeforeComma,
                         EditorOptions.spaceBeforeCommaDefault);
    }

    public boolean spaceAfterComma() {
        return getOption(EditorOptions.spaceAfterComma,
                         EditorOptions.spaceAfterCommaDefault);
    }
    
    public boolean spaceBeforeSemi() {
        return getOption(EditorOptions.spaceBeforeSemi,
                         EditorOptions.spaceBeforeSemiDefault);
    }

    public boolean spaceAfterSemi() {
        return getOption(EditorOptions.spaceAfterSemi,
                         EditorOptions.spaceAfterSemiDefault);
    }

    public boolean spaceBeforeColon() {
        return getOption(EditorOptions.spaceBeforeColon,
                         EditorOptions.spaceBeforeColonDefault);
    }

    public boolean spaceAfterColon() {
        return getOption(EditorOptions.spaceAfterColon,
                         EditorOptions.spaceAfterColonDefault);
    }
    
    public boolean spaceAfterTypeCast() {
        return getOption(EditorOptions.spaceAfterTypeCast,
                         EditorOptions.spaceAfterTypeCastDefault);
    }
    
    public boolean spaceAfterOperatorKeyword() {
        return getOption(EditorOptions.spaceAfterOperatorKeyword,
                         EditorOptions.spaceAfterOperatorKeywordfault);
    }

    //SpacesBeforeLeftBraces
    public boolean spaceBeforeClassDeclLeftBrace(){
        return getOption(EditorOptions.spaceBeforeClassDeclLeftBrace,
                         EditorOptions.spaceBeforeClassDeclLeftBraceDefault);
    }
    public boolean spaceBeforeMethodDeclLeftBrace(){
        return getOption(EditorOptions.spaceBeforeMethodDeclLeftBrace,
                         EditorOptions.spaceBeforeMethodDeclLeftBraceDefault);
    }
    public boolean spaceBeforeLambdaLeftBrace(){
        return getOption(EditorOptions.spaceBeforeLambdaLeftBrace,
                         EditorOptions.spaceBeforeLambdaLeftBraceDefault);
    }
    public boolean spaceBeforeIfLeftBrace(){
        return getOption(EditorOptions.spaceBeforeIfLeftBrace,
                         EditorOptions.spaceBeforeIfLeftBraceDefault);
    }
    public boolean spaceBeforeElseLeftBrace(){
        return getOption(EditorOptions.spaceBeforeElseLeftBrace,
                         EditorOptions.spaceBeforeElseLeftBraceDefault);
    }
    public boolean spaceBeforeWhileLeftBrace(){
        return getOption(EditorOptions.spaceBeforeWhileLeftBrace,
                         EditorOptions.spaceBeforeWhileLeftBraceDefault);
    }
    public boolean spaceBeforeForLeftBrace(){
        return getOption(EditorOptions.spaceBeforeForLeftBrace,
                         EditorOptions.spaceBeforeForLeftBraceDefault);
    }
    public boolean spaceBeforeDoLeftBrace(){
        return getOption(EditorOptions.spaceBeforeDoLeftBrace,
                         EditorOptions.spaceBeforeDoLeftBraceDefault);
    }
    public boolean spaceBeforeSwitchLeftBrace(){
        return getOption(EditorOptions.spaceBeforeSwitchLeftBrace,
                         EditorOptions.spaceBeforeSwitchLeftBraceDefault);
    }
    public boolean spaceBeforeTryLeftBrace(){
        return getOption(EditorOptions.spaceBeforeTryLeftBrace,
                         EditorOptions.spaceBeforeTryLeftBraceDefault);
    }
    public boolean spaceBeforeCatchLeftBrace(){
        return getOption(EditorOptions.spaceBeforeCatchLeftBrace,
                         EditorOptions.spaceBeforeCatchLeftBraceDefault);
    }
    public boolean spaceBeforeArrayInitLeftBrace(){
        return getOption(EditorOptions.spaceBeforeArrayInitLeftBrace,
                         EditorOptions.spaceBeforeArrayInitLeftBraceDefault);
    }

    //SpacesWithinParentheses
    public boolean spaceWithinParens(){
        return getOption(EditorOptions.spaceWithinParens,
                         EditorOptions.spaceWithinParensDefault);
    }
    public boolean spaceWithinMethodDeclParens(){
        return getOption(EditorOptions.spaceWithinMethodDeclParens,
                         EditorOptions.spaceWithinMethodDeclParensDefault);
    }
    public boolean spaceWithinMethodCallParens(){
        return getOption(EditorOptions.spaceWithinMethodCallParens,
                         EditorOptions.spaceWithinMethodCallParensDefault);
    }
    public boolean spaceWithinIfParens(){
        return getOption(EditorOptions.spaceWithinIfParens,
                         EditorOptions.spaceWithinIfParensDefault);
    }
    public boolean spaceWithinForParens(){
        return getOption(EditorOptions.spaceWithinForParens,
                         EditorOptions.spaceWithinForParensDefault);
    }
    public boolean spaceWithinWhileParens(){
        return getOption(EditorOptions.spaceWithinWhileParens,
                         EditorOptions.spaceWithinWhileParensDefault);
    }
    public boolean spaceWithinSwitchParens(){
        return getOption(EditorOptions.spaceWithinSwitchParens,
                         EditorOptions.spaceWithinSwitchParensDefault);
    }
    public boolean spaceWithinCatchParens(){
        return getOption(EditorOptions.spaceWithinCatchParens,
                         EditorOptions.spaceWithinCatchParensDefault);
    }
    public boolean spaceWithinTypeCastParens(){
        return getOption(EditorOptions.spaceWithinTypeCastParens,
                         EditorOptions.spaceWithinTypeCastParensDefault);
    }
    public boolean spaceWithinBraces(){
        return getOption(EditorOptions.spaceWithinBraces,
                         EditorOptions.spaceWithinBracesDefault);
    }

    public int blankLinesBeforeClass(){
        return getOption(EditorOptions.blankLinesBeforeClass,
                         EditorOptions.blankLinesBeforeClassDefault);
    }
//    public int blankLinesAfterClass(){
//        return getOption(EditorOptions.blankLinesAfterClass,
//                         EditorOptions.blankLinesAfterClassDefault);
//    }
    public int blankLinesAfterClassHeader(){
        return getOption(EditorOptions.blankLinesAfterClassHeader,
                         EditorOptions.blankLinesAfterClassHeaderDefault);
    }
//    public int blankLinesBeforeFields(){
//        return getOption(EditorOptions.blankLinesBeforeFields,
//                         EditorOptions.blankLinesBeforeFieldsDefault);
//    }
//    public int blankLinesAfterFields(){
//        return getOption(EditorOptions.blankLinesAfterFields,
//                         EditorOptions.blankLinesAfterFieldsDefault);
//    }
    public int blankLinesBeforeMethods(){
        return getOption(EditorOptions.blankLinesBeforeMethods,
                         EditorOptions.blankLinesBeforeMethodsDefault);
    }
//    public int blankLinesAfterMethods(){
//        return getOption(EditorOptions.blankLinesAfterMethods,
//                         EditorOptions.blankLinesAfterMethodsDefault);
//    }

    private boolean getOption(String key, boolean defaultValue) {
        defaultValue = (Boolean)EditorOptions.getDefault(language, profileId, key);
        return getPreferences().getBoolean(key, defaultValue);
    }

    private int getOption(String key, int defaultValue) {
        defaultValue = (Integer)EditorOptions.getDefault(language, profileId, key);
        return getPreferences().getInt(key, defaultValue);
    }

    private String getOption(String key, String defaultValue) {
        defaultValue = (String)EditorOptions.getDefault(language, profileId, key);
        return getPreferences().get(key, defaultValue);
    }

    private Preferences getPreferences(){
        return preferences;
    }

    private void setPreferences(Preferences preferences){
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "Code style for language "+language+". Preferences "+preferences; // NOI18N
    }

    // Nested classes ----------------------------------------------------------
    public enum Language {
        C(MIMENames.C_MIME_TYPE, "C_CodeStyles", "C_Style"), // NOI18N
        CPP(MIMENames.CPLUSPLUS_MIME_TYPE, "CPP_CodeStyles", "CPP_Style"), // NOI18N
        HEADER(MIMENames.HEADER_MIME_TYPE, "H_CodeStyles", "H_Style"); // NOI18N
        
        private final String mime;
        private final String node;
        private final String current;
        private Language(String mime, String node, String current) {
            this.mime = mime;
            this.node = node;
            this.current = current;
        }

        @Override
        public String toString() {
            return NbBundle.getMessage(CodeStyle.class, "LBL_Language_"+name()); // NOI18N
        }
        
        public String toMime() {
            return mime;
        }

        public String prefNodeName() {
            return node;
        }

        public String currentPropertyName() {
            return node;
        }
    }

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
        NEW_LINE_HALF_INDENTED,
        NEW_LINE_FULL_INDENTED;
        
        @Override
        public String toString() {
            return NbBundle.getMessage(CodeStyle.class, "LBL_bp_"+name()); // NOI18N
        }
    }

    public enum PreprocessorIndent {
        START_LINE,
        CODE_INDENT,
        PREPROCESSOR_INDENT;

        @Override
        public String toString() {
            return NbBundle.getMessage(CodeStyle.class, "LBL_pi_"+name()); // NOI18N
        }
    }

    public enum VisibilityIndent {
        NO_INDENT,
        HALF_INDENT;

        @Override
        public String toString() {
            return NbBundle.getMessage(CodeStyle.class, "LBL_vi_"+name()); // NOI18N
        }
    }

    // Communication with non public packages ----------------------------------
    private static class FactoryImpl implements EditorOptions.CodeStyleFactory {
        @Override
        public CodeStyle create(Language language, String profileID, Preferences preferences, boolean useOverrideOption) {
            return new CodeStyle(language, profileID, preferences, useOverrideOption);
        }
        @Override
        public Preferences getPreferences(CodeStyle codeStyle) {
            return codeStyle.getPreferences();
        }
        @Override
        public void setPreferences(CodeStyle codeStyle, Preferences preferences) {
            codeStyle.setPreferences(preferences);
        }
    } 
}
