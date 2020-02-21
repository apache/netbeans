/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
