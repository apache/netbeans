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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Color;
import java.io.File;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.modelutil.spi.CsmDisplayUtilitiesProvider;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.netbeans.swing.plaf.LFCustoms;

/**
 *
 */
public class CsmDisplayUtilities {

    private CsmDisplayUtilities() {
    }

    public static String getContextLineHtml(CsmFile csmFile, final int stToken, final int endToken, boolean tokenInBold) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        StyledDocument stDoc = CsmUtilities.openDocument(ces);
        String displayText = null;
        if (stDoc instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) stDoc;
            try {
                int stOffset = stToken;
                int endOffset = endToken;
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stOffset);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endOffset) + 1;
                if (!tokenInBold) {
                    stOffset = -1;
                    endOffset = -1;
                }
                displayText = getLineHtml(startLine, endLine, stOffset, endOffset, doc);
            } catch (BadLocationException ex) {
                // skip
            }
        }
        return displayText;
    }

    public static String getContextLine(CsmFile csmFile, final int stToken, final int endToken) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        StyledDocument stDoc = CsmUtilities.openDocument(ces);
        String displayText = null;
        if (stDoc instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) stDoc;
            try {
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stToken);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endToken) + 1;
                displayText = doc.getText(startLine, endLine - startLine);
            } catch (BadLocationException ex) {
                // skip
            }
        }
        return displayText;
    }

    public static String getLineHtml(int startLine, int endLine, final int stToken, final int endToken, BaseDocument doc) throws BadLocationException {
        int startBold = stToken - startLine;
        int endBold = endToken - startLine;
        String content = doc.getText(startLine, endLine - startLine);
        String mime = DocumentUtilities.getMimeType(doc);
        if (startBold >= 0 && endBold >= 0 && startBold <= content.length() && endBold <= content.length()  && startBold < endBold) {
            StringBuilder buf = new StringBuilder();
            buf.append(getHtml(mime, trimStart(content.substring(0, startBold))));
            buf.append("<b>"); //NOI18N
            buf.append(getHtml(mime, content.substring(startBold, endBold)));
            buf.append("</b>");//NOI18N
            buf.append(getHtml(mime, trimEnd(content.substring(endBold))));
            return buf.toString();
        } else {
            return getHtml(mime, content);
        }
    }

    public static String getHtml(String mime, String content) {
        final StringBuilder buf = new StringBuilder();
        Language<CppTokenId> lang = CndLexerUtilities.getLanguage(mime);
        if (lang == null) {
            return content;
        }
        TokenHierarchy<?> tokenH = TokenHierarchy.create(content, lang);
        TokenSequence<?> tok = tokenH.tokenSequence();
        appendHtml(buf, tok);
        return buf.toString();
    }

    public static CharSequence getTooltipText(CsmObject item) {
        return CsmDisplayUtilitiesProvider.getDefault().getTooltipText(item);
    }

    public static CharSequence getTypeText(CsmType type, boolean expandInstantiations, boolean evaluateExpressions) {
        return CsmDisplayUtilitiesProvider.getDefault().getTypeText(type, expandInstantiations, evaluateExpressions);
    }

    private final static boolean SKIP_COLORING = Boolean.getBoolean("cnd.test.skip.coloring");// NOI18N

    private static void appendHtml(StringBuilder buf, TokenSequence<?> ts) {
        FontColorSettings settings = null;
        LanguagePath languagePath = ts.languagePath();
        while (!SKIP_COLORING && languagePath != null && settings == null) {
            String mime = languagePath.mimePath();
            Lookup lookup = MimeLookup.getLookup(mime);
            settings = lookup.lookup(FontColorSettings.class);
        }
        while (ts.moveNext()) {
            Token<?> token = ts.token();
            TokenSequence<?> es = ts.embedded();
            if (es != null && es.language() == CppTokenId.languagePreproc()) {
                appendHtml(buf, es);
            } else {
                String category = token.id().primaryCategory();
                if (category == null) {
                    category = CppTokenId.WHITESPACE_CATEGORY; //NOI18N
                }
                String text;
                if (CppTokenId.WHITESPACE_CATEGORY.equals(category)) {
                    // whitespace
                    text = " "; // NOI18N
                } else {
                    text = token.text().toString();
                }
                if (settings != null) {
                    AttributeSet set = settings.getTokenFontColors(category);
                    buf.append(addHTMLColor(htmlize(text), set));
                } else {
                    buf.append(htmlize(text));
                }
            }
        }
    }

    public static String htmlize(CharSequence input) {
        if (input == null) {
            System.err.println("null string");// NOI18N
            return "";// NOI18N
        }
        String temp = input.toString().replace("&", "&amp;");// NOI18N
        temp = temp.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        if (temp.indexOf('\n') > 0) {
            return "<pre>"+temp+"</pre>"; // NOI18N
        }
        return temp;
    }

    public static String addHTMLColor(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuilder buf = new StringBuilder(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(LFCustoms.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    public static String addHTMLColor(String string, Color color, boolean bold) {
        if (color == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuilder buf = new StringBuilder(string);
        if (bold) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(color) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    public static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    private static String trimStart(String s) {
        for (int x = 0; x < s.length(); x++) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(x, s.length());
            }
        }
        return "";
    }

    private static String trimEnd(String s) {
        for (int x = s.length() - 1; x >= 0; x--) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }

    public static String shrinkPath(CharSequence path, int maxDisplayedDirLen, int nrDisplayedFrontDirs, int nrDisplayedTrailingDirs) {
        return shrinkPath(path, true, File.separator, maxDisplayedDirLen, nrDisplayedFrontDirs, nrDisplayedTrailingDirs);
    }

    public static String shrinkPath(CharSequence path, boolean shrink, String separator, int maxDisplayedDirLen, int nrDisplayedFrontDirs, int nrDisplayedTrailingDirs) {
        final String SLASH = "/"; //NOI18N
        StringBuilder builder = new StringBuilder(path);
        String toReplace = null;
        if (SLASH.equals(separator)) {
            if (builder.indexOf("\\") >= 0) { // NOI18N
                toReplace = "\\"; // NOI18N
            }
        } else {
            if (builder.indexOf(SLASH) >= 0) {
                toReplace = SLASH;
            }
        }
        if (toReplace != null) {
            // replace all "/" or "\" to system separator
            builder = new StringBuilder(builder.toString().replace(toReplace, separator));
        }
        int len = builder.length();
        if (shrink && len > maxDisplayedDirLen) {

            StringBuilder reverse = new StringBuilder(builder).reverse();
            int st = builder.indexOf(separator);
            if (st < 0) {
                st = 0;
            } else {
                st++;
            }
            int end = 0;
            while (reverse.charAt(end) == separator.charAt(0)) {
                end++;
            }
            int firstSlash = nrDisplayedFrontDirs > 0 ? Integer.MAX_VALUE : -1;
            for (int i = nrDisplayedFrontDirs; i > 0 && firstSlash > 0; i--) {
                firstSlash = builder.indexOf(separator, st);
                st = firstSlash + 1;
            }
            int lastSlash = nrDisplayedTrailingDirs > 0 ? Integer.MAX_VALUE : -1;
            for (int i = nrDisplayedTrailingDirs; i > 0 && lastSlash > 0; i--) {
                lastSlash = reverse.indexOf(separator, end);
                end = lastSlash + 1;
            }
            if (lastSlash > 0 && firstSlash > 0) {
                lastSlash = len - lastSlash;
                if (firstSlash < lastSlash - 1) {
                    builder.replace(firstSlash, lastSlash - 1, "..."); // NOI18N
                }
            }
        }
        return builder.toString(); // NOI18N
    }
}
