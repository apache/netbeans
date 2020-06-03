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
import org.clang.format.FormatGlobals;
import org.clang.format.FormatStyle;
import org.clank.java.std_errors;
import org.llvm.adt.StringRef;
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
    private FormatStyle delegate;
    private final boolean useOverrideOptions;

    private CodeStyle(Language language, String profileId, Preferences preferences, boolean useOverrideOptions) {
        this.language = language;
        this.profileId = profileId;
        this.preferences = preferences;
        this.useOverrideOptions = useOverrideOptions;
        delegate = null;
    }

    private CodeStyle(FormatStyle fs, boolean useOverrideOptions) {
        this.language = null;
        this.profileId = null;
        this.preferences = null;
        this.useOverrideOptions = useOverrideOptions;
        delegate = fs;
    }
    
    public synchronized static CodeStyle getDefault(Language language, Document doc) {
        String profileId = EditorOptions.getCurrentProfileId(language, doc);
        CodeStyle c;
        if (profileId.indexOf(':') > 0) {
            // it is a content of .clang-format
            c = CLANG_FORMAT.get(profileId);
            if (c == null) {
                FormatStyle fs = new FormatStyle();
                fs.Language = FormatStyle.LanguageKind.LK_Cpp;
                std_errors.error_code error = FormatGlobals.parseConfiguration(new StringRef(profileId), fs);
                if (error.$bool()) {
                    fs = FormatGlobals.getLLVMStyle();
                }
                c = new CodeStyle(fs, true);
                CLANG_FORMAT.put(profileId, c);
            }
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
            if (delegate != null) {
                return true;
            }
            return getOption(EditorOptions.overrideTabIndents,
                             EditorOptions.overrideTabIndentsDefault);
        }
        return true;
    }

    public FormatStyle getClangFormatStyle() {
        return delegate;
    }
    
    public int indentSize() {
        if (isOverideTabIndents()){
            if (delegate != null) {
              if (delegate.BreakBeforeBraces == FormatStyle.BraceBreakingStyle.BS_GNU) {
                return delegate.IndentWidth*2;
              } else {
                return delegate.IndentWidth;
              }
            }
            return getOption(EditorOptions.indentSize,
                             EditorOptions.indentSizeDefault);
        }
        return EditorOptions.getGlobalIndentSize();
    }

    public boolean expandTabToSpaces() {
        if (isOverideTabIndents()){
            if (delegate != null) {
                return delegate.UseTab == FormatStyle.UseTabStyle.UT_Never;
            }
            return getOption(EditorOptions.expandTabToSpaces,
                             EditorOptions.expandTabToSpacesDefault);
        }
        return EditorOptions.getGlobalExpandTabs();
    }

    public int getTabSize() {
        if (isOverideTabIndents()){
            if (delegate != null) {
                return delegate.TabWidth;
            }
            return getOption(EditorOptions.tabSize,
                             EditorOptions.tabSizeDefault);
        }
        return EditorOptions.getGlobalTabSize();
    }

    public int getFormatStatementContinuationIndent() {
        if (delegate != null) {
            return delegate.ContinuationIndentWidth;
        }
        return getOption(EditorOptions.statementContinuationIndent,
                         EditorOptions.statementContinuationIndentDefault);
    }

    public int getConstructorInitializerListContinuationIndent() {
        if (delegate != null) {
            return delegate.ConstructorInitializerIndentWidth;
        }
        return getOption(EditorOptions.constructorListContinuationIndent,
                         EditorOptions.constructorListContinuationIndentDefault);
    }

    public PreprocessorIndent indentPreprocessorDirectives(){
        if (delegate != null) {
            //Is there analog?
            return PreprocessorIndent.START_LINE;
        }
        return PreprocessorIndent.valueOf(getOption(EditorOptions.indentPreprocessorDirectives,
                                      EditorOptions.indentPreprocessorDirectivesDefault));
    }

    public VisibilityIndent indentVisibility(){
        if (delegate != null) {
            //Partly fit
            if (-delegate.AccessModifierOffset == delegate.IndentWidth) {
                return VisibilityIndent.NO_INDENT;
            } else {
                return VisibilityIndent.HALF_INDENT;
            }
        }
        return VisibilityIndent.valueOf(getOption(EditorOptions.indentVisibility,
                                      EditorOptions.indentVisibilityDefault));
    }
    
    public boolean indentNamespace() {
        if (delegate != null) {
            //Partly fit
            return delegate.NamespaceIndentation != FormatStyle.NamespaceIndentationKind.NI_None;
        }
        return getOption(EditorOptions.indentNamespace,
                         EditorOptions.indentNamespaceDefault);
    }

    public boolean indentCasesFromSwitch() {
        if (delegate != null) {
            return delegate.IndentCaseLabels;
        }
        return getOption(EditorOptions.indentCasesFromSwitch,
                         EditorOptions.indentCasesFromSwitchDefault);
    }

    public boolean absoluteLabelIndent() {
        if (delegate != null) {
            //Is there analog?
            return false;
        }
        return getOption(EditorOptions.absoluteLabelIndent,
                         EditorOptions.absoluteLabelIndentDefault);
    }

    public boolean sharpAtStartLine(){
        if (delegate != null) {
            //Is there analog?
            return true;
        }
        return getOption(EditorOptions.sharpAtStartLine,
                         EditorOptions.sharpAtStartLineDefault);
    }

    public boolean spaceKeepExtra(){
        if (delegate != null) {
            //Is there analog?
            return true;
        }
        return getOption(EditorOptions.spaceKeepExtra,
                         EditorOptions.spaceKeepExtraDefault);
    }
    // indents ------------------------------------------------
    public boolean spaceBeforeMethodDeclParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens == FormatStyle.SpaceBeforeParensOptions.SBPO_Always;
        }
        return getOption(EditorOptions.spaceBeforeMethodDeclParen,
                         EditorOptions.spaceBeforeMethodDeclParenDefault);
    }
    public boolean spaceBeforeMethodCallParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens == FormatStyle.SpaceBeforeParensOptions.SBPO_Always;
        }
        return getOption(EditorOptions.spaceBeforeMethodCallParen,
                         EditorOptions.spaceBeforeMethodCallParenDefault);
    }
    public boolean spaceBeforeIfParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens != FormatStyle.SpaceBeforeParensOptions.SBPO_Never;
        }
        return getOption(EditorOptions.spaceBeforeIfParen,
                         EditorOptions.spaceBeforeIfParenDefault);
    }
    public boolean spaceBeforeForParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens != FormatStyle.SpaceBeforeParensOptions.SBPO_Never;
        }
        return getOption(EditorOptions.spaceBeforeForParen,
                         EditorOptions.spaceBeforeForParenDefault);
    }
    public boolean spaceBeforeWhileParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens != FormatStyle.SpaceBeforeParensOptions.SBPO_Never;
        }
        return getOption(EditorOptions.spaceBeforeWhileParen,
                         EditorOptions.spaceBeforeWhileParenDefault);
    }
    public boolean spaceBeforeCatchParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens != FormatStyle.SpaceBeforeParensOptions.SBPO_Never;
        }
        return getOption(EditorOptions.spaceBeforeCatchParen,
                         EditorOptions.spaceBeforeCatchParenDefault);
    }
    public boolean spaceBeforeSwitchParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens != FormatStyle.SpaceBeforeParensOptions.SBPO_Never;
        }
        return getOption(EditorOptions.spaceBeforeSwitchParen,
                         EditorOptions.spaceBeforeSwitchParenDefault);
    }
    public boolean spaceBeforeKeywordParen() {
        if (delegate != null) {
            //Partly fit
            return delegate.SpaceBeforeParens == FormatStyle.SpaceBeforeParensOptions.SBPO_Always;
        }
        return getOption(EditorOptions.spaceBeforeKeywordParen,
                         EditorOptions.spaceBeforeKeywordParenDefault);
    }

    public BracePlacement getFormatNewlineBeforeBraceNamespace() {
        if (delegate != null) {
          switch(delegate.BreakBeforeBraces) {
            case BS_Attach:
              return BracePlacement.SAME_LINE;
            case BS_Linux:
              return BracePlacement.NEW_LINE;
            case BS_Mozilla:
              return BracePlacement.SAME_LINE;
            case BS_Stroustrup:
              return BracePlacement.SAME_LINE;
            case BS_Allman:
              return BracePlacement.NEW_LINE;
            case BS_GNU:
              return BracePlacement.NEW_LINE_HALF_INDENTED;
            case BS_WebKit:
              return BracePlacement.SAME_LINE;
            case BS_Custom:
            default:
              return BracePlacement.SAME_LINE;
          }
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceNamespace,
                                      EditorOptions.newLineBeforeBraceNamespaceDefault));
    }

    public BracePlacement getFormatNewlineBeforeBraceClass() {
        if (delegate != null) {
          switch(delegate.BreakBeforeBraces) {
            case BS_Attach:
              return BracePlacement.SAME_LINE;
            case BS_Linux:
              return BracePlacement.NEW_LINE;
            case BS_Mozilla:
              return BracePlacement.NEW_LINE;
            case BS_Stroustrup:
              return BracePlacement.SAME_LINE;
            case BS_Allman:
              return BracePlacement.NEW_LINE;
            case BS_GNU:
              return BracePlacement.NEW_LINE_HALF_INDENTED;
            case BS_WebKit:
              return BracePlacement.SAME_LINE;
            case BS_Custom:
            default:
              return BracePlacement.SAME_LINE;
          }
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceClass,
                                      EditorOptions.newLineBeforeBraceClassDefault));
    }

    public BracePlacement getFormatNewlineBeforeBraceDeclaration() {
        if (delegate != null) {
          switch(delegate.BreakBeforeBraces) {
            case BS_Attach:
              return BracePlacement.SAME_LINE;
            case BS_Linux:
              return BracePlacement.NEW_LINE;
            case BS_Mozilla:
              return BracePlacement.NEW_LINE;
            case BS_Stroustrup:
              return BracePlacement.NEW_LINE;
            case BS_Allman:
              return BracePlacement.NEW_LINE;
            case BS_GNU:
              return BracePlacement.NEW_LINE_HALF_INDENTED;
            case BS_WebKit:
              return BracePlacement.NEW_LINE;
            case BS_Custom:
            default:
              return BracePlacement.SAME_LINE;
          }
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceDeclaration,
                                      EditorOptions.newLineBeforeBraceDeclarationDefault));
    }
    public boolean ignoreEmptyFunctionBody(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.ignoreEmptyFunctionBodyDefault;
        }
        return getOption(EditorOptions.ignoreEmptyFunctionBody,
                         EditorOptions.ignoreEmptyFunctionBodyDefault);
    }
    public BracePlacement getFormatNewlineBeforeBraceLambda() {
        if (delegate != null) {
            //Is there analog?
            return getFormatNewlineBeforeBraceDeclaration();
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceLambda,
                                      EditorOptions.newLineBeforeBraceLambdaDefault));
    }

    public BracePlacement getFormatNewLineBeforeBraceSwitch() {
        if (delegate != null) {
          switch(delegate.BreakBeforeBraces) {
            case BS_Attach:
              return BracePlacement.SAME_LINE;
            case BS_Linux:
              return BracePlacement.SAME_LINE;
            case BS_Mozilla:
              return BracePlacement.SAME_LINE;
            case BS_Stroustrup:
              return BracePlacement.SAME_LINE;
            case BS_Allman:
              return BracePlacement.NEW_LINE;
            case BS_GNU:
              return BracePlacement.NEW_LINE_HALF_INDENTED;
            case BS_WebKit:
              return BracePlacement.SAME_LINE;
            case BS_Custom:
            default:
              return BracePlacement.SAME_LINE;
          }
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBraceSwitch,
                                      EditorOptions.newLineBeforeBraceSwitchDefault));
    }

    public BracePlacement getFormatNewlineBeforeBrace() {
        if (delegate != null) {
            return getFormatNewLineBeforeBraceSwitch();
        }
        return BracePlacement.valueOf(getOption(EditorOptions.newLineBeforeBrace,
                                      EditorOptions.newLineBeforeBraceDefault));
    }

    //NewLine
    public boolean newLineCatch(){
        if (delegate != null) {
          return delegate.BreakBeforeBraces == FormatStyle.BraceBreakingStyle.BS_Mozilla;
        }
        return getOption(EditorOptions.newLineCatch,
                         EditorOptions.newLineCatchDefault);
    }
    public boolean newLineElse(){
        if (delegate != null) {
          return delegate.BreakBeforeBraces == FormatStyle.BraceBreakingStyle.BS_Mozilla;
        }
        return getOption(EditorOptions.newLineElse,
                         EditorOptions.newLineElseDefault);
    }
    public boolean newLineWhile(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.newLineWhileDefault;
        }
        return getOption(EditorOptions.newLineWhile,
                         EditorOptions.newLineWhileDefault);
    }
    public boolean newLineFunctionDefinitionName(){
        if (delegate != null) {
          return delegate.AlwaysBreakAfterDefinitionReturnType != FormatStyle.DefinitionReturnTypeBreakingStyle.DRTBS_None;
        }
        return getOption(EditorOptions.newLineFunctionDefinitionName,
                         EditorOptions.newLineFunctionDefinitionNameDefault);
    }
    
    public boolean getFormatLeadingStarInComment() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.addLeadingStarInCommentDefault;
        }
        return getOption(EditorOptions.addLeadingStarInComment,
                         EditorOptions.addLeadingStarInCommentDefault);
    }

    public boolean getUseBlockComment() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.useBlockCommentDefault;
        }
        return getOption(EditorOptions.useBlockComment,
                EditorOptions.useBlockCommentDefault);
    }
    
    public boolean getUseInlineKeyword() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.useInlineKeywordDefault;
        }
        return getOption(EditorOptions.useInlineKeyword,
                EditorOptions.useInlineKeywordDefault);
    }
    
    //MultilineAlignment
    public boolean alignMultilineCallArgs() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineCallArgs,
                         EditorOptions.alignMultilineCallArgsDefault);
    }

    public boolean alignMultilineMethodParams() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineMethodParams,
                         EditorOptions.alignMultilineMethodParamsDefault);
    }

    public boolean alignMultilineFor() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineFor,
                         EditorOptions.alignMultilineForDefault);
    }
    public boolean alignMultilineIfCondition() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineIfCondition,
                         EditorOptions.alignMultilineIfConditionDefault);
    }
    public boolean alignMultilineWhileCondition() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineWhileCondition,
                         EditorOptions.alignMultilineWhileConditionDefault);
    }
    public boolean alignMultilineParen() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineParen,
                         EditorOptions.alignMultilineParenDefault);
    }
    public boolean alignMultilineArrayInit() {
        if (delegate != null) {
          return delegate.AlignOperands;
        }
        return getOption(EditorOptions.alignMultilineArrayInit,
                         EditorOptions.alignMultilineArrayInitDefault);
    }

    //SpacesAroundOperators
    public boolean spaceAroundUnaryOps() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAroundUnaryOpsDefault;
        }
        return getOption(EditorOptions.spaceAroundUnaryOps,
                         EditorOptions.spaceAroundUnaryOpsDefault);
    }
    public boolean spaceAroundBinaryOps() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAroundBinaryOpsDefault;
        }
        return getOption(EditorOptions.spaceAroundBinaryOps,
                         EditorOptions.spaceAroundBinaryOpsDefault);
    }
    public boolean spaceAroundAssignOps() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAroundAssignOpsDefault;
        }
        return getOption(EditorOptions.spaceAroundAssignOps,
                         EditorOptions.spaceAroundAssignOpsDefault);
    }
    public boolean spaceAroundTernaryOps() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAroundTernaryOpsDefault;
        }
        return getOption(EditorOptions.spaceAroundTernaryOps,
                         EditorOptions.spaceAroundTernaryOpsDefault);
    }
            
    public boolean spaceBeforeWhile() {
        if (delegate != null) {
          switch(delegate.SpaceBeforeParens){
            case SBPO_Never:
              return false;
            case SBPO_ControlStatements:
              return true;
            case SBPO_Always:
            default:
              return true;
          }
        }
        return getOption(EditorOptions.spaceBeforeWhile,
                         EditorOptions.spaceBeforeWhileDefault);
    }
    
    public boolean spaceBeforeElse() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeElseDefault;
        }
        return getOption(EditorOptions.spaceBeforeElse,
                         EditorOptions.spaceBeforeElseDefault);
    }

    public boolean spaceBeforeCatch() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeCatchDefault;
        }
        return getOption(EditorOptions.spaceBeforeCatch,
                         EditorOptions.spaceBeforeCatchDefault);
    }

    public boolean spaceBeforeComma() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeCommaDefault;
        }
        return getOption(EditorOptions.spaceBeforeComma,
                         EditorOptions.spaceBeforeCommaDefault);
    }

    public boolean spaceAfterComma() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAfterCommaDefault;
        }
        return getOption(EditorOptions.spaceAfterComma,
                         EditorOptions.spaceAfterCommaDefault);
    }
    
    public boolean spaceBeforeSemi() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeSemiDefault;
        }
        return getOption(EditorOptions.spaceBeforeSemi,
                         EditorOptions.spaceBeforeSemiDefault);
    }

    public boolean spaceAfterSemi() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAfterSemiDefault;
        }
        return getOption(EditorOptions.spaceAfterSemi,
                         EditorOptions.spaceAfterSemiDefault);
    }

    public boolean spaceBeforeColon() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeColonDefault;
        }
        return getOption(EditorOptions.spaceBeforeColon,
                         EditorOptions.spaceBeforeColonDefault);
    }

    public boolean spaceAfterColon() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAfterColonDefault;
        }
        return getOption(EditorOptions.spaceAfterColon,
                         EditorOptions.spaceAfterColonDefault);
    }
    
    public boolean spaceAfterTypeCast() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAfterTypeCastDefault;
        }
        return getOption(EditorOptions.spaceAfterTypeCast,
                         EditorOptions.spaceAfterTypeCastDefault);
    }
    
    public boolean spaceAfterOperatorKeyword() {
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceAfterOperatorKeywordfault;
        }
        return getOption(EditorOptions.spaceAfterOperatorKeyword,
                         EditorOptions.spaceAfterOperatorKeywordfault);
    }

    //SpacesBeforeLeftBraces
    public boolean spaceBeforeClassDeclLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeClassDeclLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeClassDeclLeftBrace,
                         EditorOptions.spaceBeforeClassDeclLeftBraceDefault);
    }
    public boolean spaceBeforeMethodDeclLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeMethodDeclLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeMethodDeclLeftBrace,
                         EditorOptions.spaceBeforeMethodDeclLeftBraceDefault);
    }
    public boolean spaceBeforeLambdaLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeLambdaLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeLambdaLeftBrace,
                         EditorOptions.spaceBeforeLambdaLeftBraceDefault);
    }
    public boolean spaceBeforeIfLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeIfLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeIfLeftBrace,
                         EditorOptions.spaceBeforeIfLeftBraceDefault);
    }
    public boolean spaceBeforeElseLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeElseLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeElseLeftBrace,
                         EditorOptions.spaceBeforeElseLeftBraceDefault);
    }
    public boolean spaceBeforeWhileLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeWhileLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeWhileLeftBrace,
                         EditorOptions.spaceBeforeWhileLeftBraceDefault);
    }
    public boolean spaceBeforeForLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeForLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeForLeftBrace,
                         EditorOptions.spaceBeforeForLeftBraceDefault);
    }
    public boolean spaceBeforeDoLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeDoLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeDoLeftBrace,
                         EditorOptions.spaceBeforeDoLeftBraceDefault);
    }
    public boolean spaceBeforeSwitchLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeSwitchLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeSwitchLeftBrace,
                         EditorOptions.spaceBeforeSwitchLeftBraceDefault);
    }
    public boolean spaceBeforeTryLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeTryLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeTryLeftBrace,
                         EditorOptions.spaceBeforeTryLeftBraceDefault);
    }
    public boolean spaceBeforeCatchLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeCatchLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeCatchLeftBrace,
                         EditorOptions.spaceBeforeCatchLeftBraceDefault);
    }
    public boolean spaceBeforeArrayInitLeftBrace(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceBeforeArrayInitLeftBraceDefault;
        }
        return getOption(EditorOptions.spaceBeforeArrayInitLeftBrace,
                         EditorOptions.spaceBeforeArrayInitLeftBraceDefault);
    }

    //SpacesWithinParentheses
    public boolean spaceWithinParens(){
        if (delegate != null) {
            //Is there analog?
            return delegate.SpacesInParentheses;
        }
        return getOption(EditorOptions.spaceWithinParens,
                         EditorOptions.spaceWithinParensDefault);
    }
    public boolean spaceWithinMethodDeclParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinMethodDeclParensDefault;
        }
        return getOption(EditorOptions.spaceWithinMethodDeclParens,
                         EditorOptions.spaceWithinMethodDeclParensDefault);
    }
    public boolean spaceWithinMethodCallParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinMethodCallParensDefault;
        }
        return getOption(EditorOptions.spaceWithinMethodCallParens,
                         EditorOptions.spaceWithinMethodCallParensDefault);
    }
    public boolean spaceWithinIfParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinIfParensDefault;
        }
        return getOption(EditorOptions.spaceWithinIfParens,
                         EditorOptions.spaceWithinIfParensDefault);
    }
    public boolean spaceWithinForParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinForParensDefault;
        }
        return getOption(EditorOptions.spaceWithinForParens,
                         EditorOptions.spaceWithinForParensDefault);
    }
    public boolean spaceWithinWhileParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinWhileParensDefault;
        }
        return getOption(EditorOptions.spaceWithinWhileParens,
                         EditorOptions.spaceWithinWhileParensDefault);
    }
    public boolean spaceWithinSwitchParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinSwitchParensDefault;
        }
        return getOption(EditorOptions.spaceWithinSwitchParens,
                         EditorOptions.spaceWithinSwitchParensDefault);
    }
    public boolean spaceWithinCatchParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinCatchParensDefault;
        }
        return getOption(EditorOptions.spaceWithinCatchParens,
                         EditorOptions.spaceWithinCatchParensDefault);
    }
    public boolean spaceWithinTypeCastParens(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinTypeCastParensDefault;
        }
        return getOption(EditorOptions.spaceWithinTypeCastParens,
                         EditorOptions.spaceWithinTypeCastParensDefault);
    }
    public boolean spaceWithinBraces(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.spaceWithinBracesDefault;
        }
        return getOption(EditorOptions.spaceWithinBraces,
                         EditorOptions.spaceWithinBracesDefault);
    }

    public int blankLinesBeforeClass(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.blankLinesBeforeClassDefault;
        }
        return getOption(EditorOptions.blankLinesBeforeClass,
                         EditorOptions.blankLinesBeforeClassDefault);
    }
//    public int blankLinesAfterClass(){
//        return getOption(EditorOptions.blankLinesAfterClass,
//                         EditorOptions.blankLinesAfterClassDefault);
//    }
    public int blankLinesAfterClassHeader(){
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.blankLinesAfterClassHeaderDefault;
        }
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
        if (delegate != null) {
            //Is there analog?
            return EditorOptions.blankLinesBeforeMethodsDefault;
        }
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
