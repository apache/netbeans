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
package org.netbeans.modules.lsp.client.bindings.configuration;

import java.awt.event.ActionEvent;
import java.util.Map;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.NavigationHistory;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.ToggleCommentAction;
import static org.netbeans.editor.ext.ExtKit.toggleCommentAction;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.openide.util.NbBundle;

public class ToggleCommentActionImpl extends BaseAction {

    //copied from the ToggleCommentAction:
    static final long serialVersionUID = -1L;

    public ToggleCommentActionImpl() {
        super(toggleCommentAction);
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ToggleCommentAction.class, "ToggleCommentAction_shortDescription")); //NOI18N

        putValue(BaseAction.ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/comment.png"); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        Boolean forceComment = (Boolean) getValue("force-uncomment");
        String mimeType = NbEditorUtilities.getMimeType(target);
        LanguageConfiguration lc = MimeLookup.getLookup(mimeType).lookup(LanguageConfiguration.class);
        String lineCommentString = lc != null && lc.comments != null && lc.comments.lineComment != null ? lc.comments.lineComment.comment : null;
        if (lineCommentString != null) {
            commentUncomment(evt, target, forceComment, lineCommentString, lineCommentString.length(), lc != null && lc.comments != null && lc.comments.lineComment != null ? lc.comments.lineComment.noIdent : false);
        } else {
            target.getToolkit().beep();
        }
    }

    private void commentUncomment(ActionEvent evt, final JTextComponent target, final Boolean forceComment, String lineCommentString, int lineCommentStringLen, boolean noIndent) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            final Caret caret = target.getCaret();
            final BaseDocument doc = (BaseDocument)target.getDocument();
            doc.runAtomicAsUser (new Runnable () {
                public void run () {
                    if(caret instanceof EditorCaret) {
                        EditorCaret editorCaret = (EditorCaret) caret;
                        boolean beeped = false;
                        for (CaretInfo caretInfo : editorCaret.getSortedCarets()) {
                            try {
                                int startPos;
                                int endPos;

                                if (caretInfo.isSelectionShowing()) {
                                    int start = Math.min(caretInfo.getDot(), caretInfo.getMark());
                                    int end = Math.max(caretInfo.getDot(), caretInfo.getMark());
                                    startPos = LineDocumentUtils.getLineStartOffset(doc, start);
                                    endPos = end;
                                    if (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) {
                                        endPos--;
                                    }
                                    endPos = LineDocumentUtils.getLineEndOffset(doc, endPos);
                                } else { // selection not visible
                                    startPos = LineDocumentUtils.getLineStartOffset(doc, caretInfo.getDot());
                                    endPos = LineDocumentUtils.getLineEndOffset(doc, caretInfo.getDot());
                                }

                                int lineCount = LineDocumentUtils.getLineCount(doc, startPos, endPos);
                                boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount, lineCommentString, lineCommentStringLen);

                                if (comment) {
                                    comment(doc, startPos, lineCount, lineCommentString, noIndent);
                                } else {
                                    uncomment(doc, startPos, lineCount, lineCommentString, lineCommentStringLen);
                                }
                                // TODO:
//                                    NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                            } catch (BadLocationException e) {
                                if(!beeped) {
                                    target.getToolkit().beep();
                                    beeped = true;
                                }
                            }
                        }
                    } else {
                        try {
                            int startPos;
                            int endPos;

                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) {
                                    endPos--;
                                }
                                endPos = LineDocumentUtils.getLineEndOffset(doc, endPos);
                            } else { // selection not visible
                                startPos = LineDocumentUtils.getLineStartOffset(doc, caret.getDot());
                                endPos = LineDocumentUtils.getLineEndOffset(doc, caret.getDot());
                            }

                            int lineCount = LineDocumentUtils.getLineCount(doc, startPos, endPos);
                            boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount, lineCommentString, lineCommentStringLen);

                            if (comment) {
                                comment(doc, startPos, lineCount, lineCommentString, noIndent);
                            } else {
                                uncomment(doc, startPos, lineCount, lineCommentString, lineCommentStringLen);
                            }
                            NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                }
            });
        }
    }

    private boolean allComments(BaseDocument doc, int startOffset, int lineCount, String lineCommentString, int lineCommentStringLen) throws BadLocationException {
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            int firstNonWhitePos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
            if (firstNonWhitePos == -1) {
                return false;
            }

            if (LineDocumentUtils.getLineEndOffset(doc, firstNonWhitePos) - firstNonWhitePos < lineCommentStringLen) {
                return false;
            }

            CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
            if (!CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                return false;
            }

            offset = Utilities.getRowStart(doc, offset, +1);
        }
        return true;
    }

    private int commonIndent(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
        int indent = Integer.MAX_VALUE;
        for (int offset = startOffset; lineCount > 0 && indent > 0; lineCount--) {
            int firstNonWhiteSpace = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);

            if (firstNonWhiteSpace == (-1)) {
                indent = 0;
                break;
            }

            indent = Math.min(indent, firstNonWhiteSpace - offset);

            offset = Utilities.getRowStart(doc, offset, +1);
        }
        return indent;
    }

    private void comment(BaseDocument doc, int startOffset, int lineCount, String lineCommentString, boolean noIndent) throws BadLocationException {
        int indent = noIndent ? 0 : commonIndent(doc, startOffset, lineCount); //TODO: reflect noIndent
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            doc.insertString(offset + indent, lineCommentString + " ", null); // NOI18N
            offset = Utilities.getRowStart(doc, offset, +1);
        }
    }

    private void uncomment(BaseDocument doc, int startOffset, int lineCount, String lineCommentString, int lineCommentStringLen) throws BadLocationException {
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            // Get the first non-whitespace char on the current line
            int firstNonWhitePos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);

            // If there is any, check wheter it's the line-comment-chars and remove them
            if (firstNonWhitePos != -1) {
                int lineContentLen = LineDocumentUtils.getLineEndOffset(doc, firstNonWhitePos) - firstNonWhitePos;
                if (lineContentLen >= lineCommentStringLen) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen + (lineContentLen > lineCommentStringLen ? 1 : 0));
                    if (CharSequenceUtilities.textEquals(maybeLineComment.subSequence(0, lineCommentStringLen), lineCommentString)) {
                        int toRemove = lineCommentStringLen;
                        if (maybeLineComment.length() > lineCommentStringLen && maybeLineComment.charAt(maybeLineComment.length() - 1) == ' ') {
                            toRemove++;
                        }
                        doc.remove(firstNonWhitePos, toRemove);
                    }
                }
            }

            offset = Utilities.getRowStart(doc, offset, +1);
        }
    }
}
