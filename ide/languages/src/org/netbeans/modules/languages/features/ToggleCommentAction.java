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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
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
                            int startPos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                            int endPos = target.getSelectionEnd();
                            if (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) {
                                endPos--;
                            }
                            int lineCnt = LineDocumentUtils.getLineCount(doc, startPos, endPos);
                            List<String> mimeTypes = new ArrayList<>(lineCnt);
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
                            int pos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
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
                int end = LineDocumentUtils.getLineEndOffset(doc, offset);
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
                int lastNonWhitePos = LineDocumentUtils.getLineLastNonWhitespace(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - LineDocumentUtils.getLineStartOffset(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            doc.remove(lastNonWhitePos - commentLen + 1, commentLen);
                        }
                    }
                }
            }
            int firstNonWhitePos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (LineDocumentUtils.getLineEndOffset(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
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
                int lastNonWhitePos = LineDocumentUtils.getLineLastNonWhitespace(doc, offset);
                if (lastNonWhitePos != -1) {
                    int commentLen = suffix.length();
                    if (lastNonWhitePos - LineDocumentUtils.getLineStartOffset(doc, offset) >= commentLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, lastNonWhitePos - commentLen + 1, commentLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, suffix)) {
                            suffixCommentOk = true;
                        }
                    }
                }
            } else {
                suffixCommentOk = true;
            }
            int firstNonWhitePos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
            if (firstNonWhitePos != -1) {
                int commentLen = prefix.length();
                if (LineDocumentUtils.getLineEndOffset(doc, firstNonWhitePos) - firstNonWhitePos >= prefix.length()) {
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
