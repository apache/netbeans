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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;

/**
 *
 * @author Daniel Prusa
 */
public class ToggleCommentAction extends ExtKit.ToggleCommentAction {

    public ToggleCommentAction() {
        super(""); // NOI18N
    }

    public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            final BaseDocument doc = (BaseDocument)target.getDocument();
            final Caret caret = target.getCaret();
            final TokenHierarchy th = TokenHierarchy.get (doc);
            if (th == null) {
                return;
            }
            final TokenSequence ts = th.tokenSequence();
            final boolean isCommented = true;
            doc.runAtomicAsUser (new Runnable () {
                public void run () {
                    try {
                        if (caret.isSelectionVisible()) {
                            int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                            int endPos = target.getSelectionEnd();
                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }
                            int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                            List mimeTypes = new ArrayList(lineCnt);
                            int pos = startPos;
                            boolean commented = isCommented;
                            for (int x = lineCnt ; x > 0; x--) {
                                String mimeType = getRealMimeType(ts, pos);
                                mimeTypes.add(mimeType);
                                commented = commented && isCommentedLine(doc, mimeType, pos);
                                pos = Utilities.getRowStart(doc, pos, 1);
                            }

                            pos = startPos;
                            for (Iterator iter = mimeTypes.iterator(); iter.hasNext(); ) {
                                if (commented) {
                                    uncommentLine(doc, (String)iter.next(), pos);
                                } else {
                                    commentLine(doc, (String)iter.next(), pos);
                                }
                                pos = Utilities.getRowStart(doc, pos, 1);
                            }
                        } else { // selection not visible
                            int pos = Utilities.getRowStart(doc, target.getSelectionStart());
                            String mt = getRealMimeType(ts, pos);
                            if (isCommentedLine(doc, mt, pos)) {
                                uncommentLine(doc, mt, pos);
                            } else {
                                commentLine(doc, mt, pos);
                            }
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                }
            });
        }
    }
    
    private String getRealMimeType(TokenSequence ts, int offset) {
        while (true) {
            ts.move(offset);
            if (!ts.moveNext())
                break;
            offset = ts.offset();
            TokenSequence ts2 = ts.embedded();
            if (ts2 == null) break;
            ts = ts2;
        }
        return ts.language().mimeType();
    }
    
    private void commentLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeatureList ().getFeature (CodeCommentAction.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int end = Utilities.getRowEnd(doc, offset);
                doc.insertString(end, suffix, null);
            }
            doc.insertString(offset, prefix, null);
        }
    }
    
    private void uncommentLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeatureList ().getFeature (CodeCommentAction.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int lastNonWhitePos = Utilities.getRowLastNonWhite(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - Utilities.getRowStart(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            doc.remove(lastNonWhitePos - commentLen + 1, commentLen);
                        }
                    }
                }
            }
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, commentLen);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, prefix)) {
                        doc.remove(firstNonWhitePos, commentLen);
                    }
                }
            }
        }
    }
    
    private boolean isCommentedLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        boolean suffixCommentOk = false;
        boolean prefixCommentOk = false;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeatureList ().getFeature (CodeCommentAction.COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return true;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int lastNonWhitePos = Utilities.getRowLastNonWhite(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - Utilities.getRowStart(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            suffixCommentOk = true;
                        }
                    }
                }
            } else {
                suffixCommentOk = true;
            }
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, commentLen);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, prefix)) {
                        prefixCommentOk = true;
                    }
                }
            }
            return prefixCommentOk && suffixCommentOk;
        } else {
            return true;
        }
    }
    
}
