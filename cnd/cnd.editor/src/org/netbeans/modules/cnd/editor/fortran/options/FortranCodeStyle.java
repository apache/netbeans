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

package org.netbeans.modules.cnd.editor.fortran.options;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/** 
 * 
 */
public final class FortranCodeStyle {
    
    private final Preferences preferences;
    private final FortranFormat autoDetectedFormat;
    
    private FortranCodeStyle(Preferences preferences, FortranFormat autoDetectedFormat) {
        this.preferences = preferences;
        this.autoDetectedFormat = autoDetectedFormat;
    }

    public InputAttributes setupLexerAttributes(Document doc){
        InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (lexerAttrs == null) {
            lexerAttrs = new InputAttributes();
            doc.putProperty(InputAttributes.class, lexerAttrs);
        }
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, getRrightMargin(), true);
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, getFormatFortran(), true);
        return lexerAttrs;
    }

    // for options panel
    public static FortranCodeStyle get(Document doc, Preferences pref) {
        return new FortranCodeStyle(pref, CndLexerUtilities.detectFortranFormat(doc));
    }

    public static FortranCodeStyle get(Document doc) {
        return new FortranCodeStyle(CodeStylePreferences.get(doc).getPreferences(), CndLexerUtilities.detectFortranFormat(doc));
    }

    public boolean absoluteLabelIndent() {
        return true;
    }

    // General tabs and indents ------------------------------------------------
    
    public boolean expandTabToSpaces() {
        return preferences.getBoolean(FmtOptions.expandTabToSpaces, FmtOptions.getDefaultAsBoolean(FmtOptions.expandTabToSpaces));
    }

    public int getTabSize() {
        return preferences.getInt(FmtOptions.tabSize, FmtOptions.getDefaultAsInt(FmtOptions.tabSize));
    }

    public int getRrightMargin() {
        return preferences.getInt(FmtOptions.rightMargin, FmtOptions.getDefaultAsInt(FmtOptions.rightMargin));
    }

    public boolean indentCasesFromSwitch() {
        return true;
    }

    public CodeStyle.PreprocessorIndent indentPreprocessorDirectives() {
        return CodeStyle.PreprocessorIndent.START_LINE;
    }

    public int indentSize() {
        return preferences.getInt(FmtOptions.indentSize, FmtOptions.getDefaultAsInt(FmtOptions.indentSize));
    }

    public FortranFormat getFormatFortran() {
        if (isAutoFormatDetection()) {
            return autoDetectedFormat;
        }
        if (preferences.getBoolean(FmtOptions.freeFormat, FmtOptions.getDefaultAsBoolean(FmtOptions.freeFormat))) {
            return FortranFormat.FREE;
        } else {
            return FortranFormat.FIXED;
        }
    }

    /** For testing purposes only */
    public void setFreeFormatFortran(boolean freeFormat) {
        preferences.putBoolean(FmtOptions.freeFormat, freeFormat);
    }

    public boolean isAutoFormatDetection() {
        return preferences.getBoolean(FmtOptions.autoDetect, FmtOptions.getDefaultAsBoolean(FmtOptions.autoDetect));
    }

    /** For testing purposes only */
    public void setAutoFormatDetection(boolean autoFormatDetection) {
        preferences.putBoolean(FmtOptions.autoDetect, autoFormatDetection);
    }

    public boolean sharpAtStartLine() {
        return true;
    }

    public boolean spaceAfterComma() {
        return true;
    }

    public boolean spaceAroundAssignOps() {
        return true;
    }

    public boolean spaceAroundBinaryOps() {
        return true;
    }

    public boolean spaceAroundUnaryOps() {
        return false;
    }

    public boolean spaceBeforeComma() {
        return false;
    }

    public boolean spaceBeforeForParen() {
        return true;
    }

    public boolean spaceBeforeIfParen() {
        return true;
    }

    public boolean spaceBeforeKeywordParen() {
        return true;
    }

    public boolean spaceBeforeMethodCallParen() {
        return false;
    }

    public boolean spaceBeforeMethodDeclParen() {
        return false;
    }

    public boolean spaceBeforeSwitchParen() {
        return true;
    }

    public boolean spaceBeforeWhile() {
        return true;
    }

    public boolean spaceBeforeWhileParen() {
        return true;
    }

    public boolean spaceWithinForParens() {
        return false;
    }

    public boolean spaceWithinIfParens() {
        return false;
    }

    public boolean spaceWithinMethodCallParens() {
        return false;
    }

    public boolean spaceWithinMethodDeclParens() {
        return false;
    }

    public boolean spaceWithinParens() {
        return false;
    }

    public boolean spaceWithinSwitchParens() {
        return false;
    }

    public boolean spaceWithinWhileParens() {
        return false;
    }

}
