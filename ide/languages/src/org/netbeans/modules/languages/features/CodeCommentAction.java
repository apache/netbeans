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
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;

/**
 *
 * @author Dan
 */
public class CodeCommentAction extends CommentAction {
    
    public static final String COMMENT_LINE = "COMMENT_LINE";

    public CodeCommentAction() {
        super(""); // NOI18N
    }

    @Override
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
            try {
                if (caret.isSelectionVisible()) {
                    final int startPos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                    final int endPos = target.getSelectionEnd();
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            try {
                                int end = (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) ?
                                    endPos-1 : endPos;
                                int lineCnt = LineDocumentUtils.getLineCount(doc, startPos, end);
                                List<String> mimeTypes = new ArrayList<>(lineCnt);
                                int pos = startPos;
                                for (int x = lineCnt ; x > 0; x--) {
                                    mimeTypes.add(getRealMimeType(ts, pos));
                                    pos = Utilities.getRowStart(doc, pos, 1);
                                }

                                pos = startPos;
                                for (Iterator iter = mimeTypes.iterator(); iter.hasNext(); ) {
                                    modifyLine(doc, (String)iter.next(), pos);
                                    pos = Utilities.getRowStart(doc, pos, 1);
                                }
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    });
                } else { // selection not visible
                    final int pos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                    final String mt = getRealMimeType(ts, pos);
                    doc.runAtomicAsUser (() -> {
                        try {
                            modifyLine(doc, mt, pos);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    });
                }
            } catch (BadLocationException e) {
                target.getToolkit().beep();
            }
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
    
    private void modifyLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeatureList ().getFeature (COMMENT_LINE);
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
    
}
