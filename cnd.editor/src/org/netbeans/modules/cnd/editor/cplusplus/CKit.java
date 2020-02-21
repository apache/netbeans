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
/** C++ editor kit with appropriate document */
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;

public class CKit extends CCKit {

    public CKit() {
        // default constructor needed to be created from services
    }
    
    @Override
    public String getContentType() {
        return MIMENames.C_MIME_TYPE;
    }

    @Override
    protected Language<CppTokenId> getLanguage() {
        return CppTokenId.languageC();
    }

    @Override
    protected Action getCommentAction() {
        return new CCommentAction();
    }

    @Override
    protected Action getUncommentAction() {
        return new CUncommentAction();
    }

    @Override
    protected Action getToggleCommentAction() {
        return new CToggleCommentAction();
    }
    private static final String START_BLOCK_COMMENT = "/*"; // NOI18N
    private static final String END_BLOCK_COMMENT = "*/"; // NOI18N
    private static final String insertStartCommentString = START_BLOCK_COMMENT + "\n"; // NOI18N
    private static final String insertEndCommentString = END_BLOCK_COMMENT + "\n"; // NOI18N

    private static final class CCommentAction extends CommentAction {

        private CCommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            doCStyleComment(target);
        }

        private static void doCStyleComment(final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                doc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        Caret caret = target.getCaret();
                        try {
                            int startPos;
                            int endPos;
                            String endString = insertEndCommentString;
                            //if (caret.isSelectionVisible()) {
                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                    endPos--;
                                }

                                int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                                endPos = Utilities.getRowStart(doc, startPos, +lineCnt);
                            } else {
                                // selection not visible, surround only one line
                                startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                endPos = Utilities.getRowStart(doc, startPos, +1);
                                if (endPos == -1) {
                                    endPos = doc.getLength();
                                    endString = "\n" + insertEndCommentString; // NOI18N
                                }
                            }
                            // insert end line
                            doc.insertString(endPos, endString, null);
                            // then start line
                            doc.insertString(startPos, insertStartCommentString, null);
//                            NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }
    }

    private static final class CUncommentAction extends UncommentAction {

        private CUncommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            doCStyleUncomment(target);
        }

        private static void doCStyleUncomment(final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                doc.runAtomicAsUser(new Runnable() {

                    @Override
                    public void run() {
                        Caret caret = target.getCaret();
                        try {
                            int startPos;
                            int endPos;
                            if (Utilities.isSelectionShowing(caret)) {
                                startPos = target.getSelectionStart();
                                endPos = target.getSelectionEnd();
                                if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                    endPos--;
                                }
                            } else {
                                // selection not visible
                                endPos = startPos = target.getSelectionStart();
                            }
                            // get token inside selection
                            TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, startPos, true, false);
                            if (ts == null) {
                                return;
                            }
                            Token<TokenId> tok = ts.token();
                            int offset = ts.offset();
                            while (offset < endPos && tok.id() == CppTokenId.WHITESPACE && ts.moveNext()) {
                                tok = ts.token();
                                offset = ts.offset();
                            }
                            if (tok.id() == CppTokenId.BLOCK_COMMENT) {
                                int commentBlockStartOffset = offset;
                                int commentBlockEndOffset = commentBlockStartOffset + tok.length();
                                int startLineStartPos = Utilities.getRowStart(doc, commentBlockStartOffset);
                                int startLineEndPos = Utilities.getRowEnd(doc, startLineStartPos);
                                String startLineContent = doc.getText(startLineStartPos, startLineEndPos - startLineStartPos);
                                if (!START_BLOCK_COMMENT.equals(startLineContent.trim())) {
                                    // not only "\*" on the line => remove only "\*" itself
                                    startLineStartPos = commentBlockStartOffset;
                                    startLineEndPos = startLineStartPos + START_BLOCK_COMMENT.length();
                                } else {
                                    // remove full line with eol
                                    startLineEndPos = startLineEndPos < doc.getLength() - 1 ? startLineEndPos + 1 : doc.getLength();
                                }
                                int endLineStartPos = Utilities.getRowStart(doc, commentBlockEndOffset);
                                int endLineEndPos = Utilities.getRowEnd(doc, endLineStartPos);
                                String endLineContent = doc.getText(endLineStartPos, endLineEndPos - endLineStartPos);
                                if (!END_BLOCK_COMMENT.equals(endLineContent.trim())) {
                                    // not only "*/" on the line => remove only "*/" itself
                                    endLineEndPos = commentBlockEndOffset;
                                    endLineStartPos = endLineEndPos - END_BLOCK_COMMENT.length();
                                } else {
                                    // remove full line with eol
                                    endLineEndPos = endLineEndPos < doc.getLength() - 1 ? endLineEndPos + 1 : doc.getLength();
                                }
                                // remove end line
                                doc.remove(endLineStartPos, endLineEndPos - endLineStartPos);
                                // remove start line
                                doc.remove(startLineStartPos, startLineEndPos - startLineStartPos);
                            }
//                            NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }
    }

    private static final class CToggleCommentAction extends ToggleCommentAction {

        private CToggleCommentAction() {
            // fake string 
            super("//"); // NOI18N 
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                CodeStyle style = CodeStyle.getDefault(doc);
                if (style.getUseBlockComment()) {
                    if (allComments(target)) {
                        CUncommentAction.doCStyleUncomment(target);
                    } else {
                        CCommentAction.doCStyleComment(target);
                    }
                } else {
                    super.actionPerformed(evt, target);
                }
            }
        }
        
        private boolean allComments(final JTextComponent target) {
            final BaseDocument doc = (BaseDocument) target.getDocument();
            final AtomicBoolean res = new AtomicBoolean(false);
            doc.render(new Runnable() {

                @Override
                public void run() {
                    Caret caret = target.getCaret();
                    Token<TokenId> tok = null;
                    try {
                        int startPos;
                        int endPos;
                        //if (caret.isSelectionVisible()) {
                        if (Utilities.isSelectionShowing(caret)) {
                            startPos = target.getSelectionStart();
                            endPos = target.getSelectionEnd();
                            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                endPos--;
                            }
                        } else {
                            // selection not visible
                            endPos = startPos = target.getSelectionStart();
                        }
                        // get token inside selection
                        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, startPos, true, false);
                        if (ts == null) {
                            return;
                        }
                        tok = ts.token();
                        while (ts.offset() < endPos && tok.id() == CppTokenId.WHITESPACE && ts.moveNext()) {
                            // all in comment means only whitespaces or block commens
                            tok = ts.token();
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                    res.set((tok != null) && (tok.id() == CppTokenId.BLOCK_COMMENT));
                }
            });
            return res.get();
        }
    }
}
