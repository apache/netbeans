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
package org.netbeans.modules.php.smarty.editor.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.editor.TplMetaData;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.modules.php.smarty.editor.utlis.TplUtils;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ToggleBlockCommentAction extends BaseAction {

    static final long serialVersionUID = -1L;
    static final private String FORCE_COMMENT = "force-comment"; //NOI18N
    static final private String FORCE_UNCOMMENT = "force-uncomment"; //NOI18N

    public ToggleBlockCommentAction() {
        super(ExtKit.toggleCommentAction);
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled() || !(target.getDocument() instanceof BaseDocument)) {
                target.getToolkit().beep();
                return;
            }

            final int caretOffset = Utilities.isSelectionShowing(target) ? target.getSelectionStart() : target.getCaretPosition();
            final BaseDocument doc = (BaseDocument) target.getDocument();
            doc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    final AtomicBoolean commentIt = new AtomicBoolean(true);
                    final TokenSequence<TplTopTokenId> ts = LexerUtils.getTplTopTokenSequence(doc, caretOffset);
                    final TplMetaData tplMetaData = TplUtils.getProjectPropertiesForFileObject(Source.create(doc).getFileObject());

                    // XXX - clean up needed
                    if (ts != null && SmartyOptions.getInstance().getToggleCommentOption() == SmartyFramework.ToggleCommentOption.SMARTY) {
                        ts.move(caretOffset);
                        ts.moveNext();

                        if (ts.token().id() == TplTopTokenId.T_COMMENT) {
                            commentIt.set(false);
                        } else {
                            try {
                                ts.move(Utilities.getRowStart(doc, caretOffset) + tplMetaData.getCloseDelimiter().length() + 1);
                                ts.moveNext();
                                if (ts.token() != null && ts.token().id() == TplTopTokenId.T_COMMENT
                                        && Utilities.getRowEnd(doc, caretOffset) == caretOffset) {
                                    commentIt.set(false);
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        try {
                            // get insert, remove positions
                            int[] positions = getPositions(ts, commentIt.get(), caretOffset, target, tplMetaData);
                            String startComment = tplMetaData.getOpenDelimiter() + "*"; //NOI18N

                            if (commentIt.get()) {
                                doc.insertString(positions[0], startComment, null);
                                doc.insertString(positions[1] + startComment.length(), "*" + tplMetaData.getCloseDelimiter(), null); //NOI18N
                            } else {
                                doc.remove(positions[0], startComment.length());
                                doc.remove(positions[1], tplMetaData.getCloseDelimiter().length() + 1);
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        boolean applied = false;
                        if (ts != null) {
                            // comment just the actual TPL tag
                            ts.move(caretOffset);
                            ts.moveNext();
                            commentIt.set(!uncommentIt(ts));

                            // reset the tokenSequence state
                            ts.move(caretOffset);
                            ts.moveNext();
                            if (ts.token().id() == TplTopTokenId.T_COMMENT || ts.token().id() == TplTopTokenId.T_SMARTY
                                    || ts.token().id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                                applied = true;
                                try {
                                    // get insert, remove positions
                                    int[] positions = getDelimiterPositions(ts, tplMetaData);

                                    if (commentIt.get()) {
                                        doc.insertString(positions[0], "*", null); //NOI18N
                                        doc.insertString(positions[1] + tplMetaData.getCloseDelimiter().length() + 2, "*", null); //NOI18N
                                    } else {
                                        doc.remove(positions[0], 1);
                                        doc.remove(positions[1], 1);
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                        if (!applied) {
                            // another languages processing
                            BaseAction action = (BaseAction) CslActions.createToggleBlockCommentAction();
                            if (getValue(FORCE_COMMENT) != null) {
                                action.putValue(FORCE_COMMENT, getValue(FORCE_COMMENT));
                            }
                            if (getValue(FORCE_UNCOMMENT) != null) {
                                action.putValue(FORCE_UNCOMMENT, getValue(FORCE_UNCOMMENT));
                            }
                            action.actionPerformed(evt, target);
                        }
                    }
                }
            });
        }
    }

    // XXX - refactor into one method
    private static int[] getPositions(TokenSequence<TplTopTokenId> ts, boolean commentIt, int caretOffset,
            JTextComponent target, TplMetaData tplMetaData) throws BadLocationException {
        int[] positions = new int[2];
        if (commentIt) {
            if (Utilities.isSelectionShowing(target)) {
                positions[0] = caretOffset;
                positions[1] = target.getSelectionEnd();
            } else {
                positions[0] = Utilities.getRowStart((BaseDocument) target.getDocument(), caretOffset);
                positions[1] = Utilities.getRowEnd((BaseDocument) target.getDocument(), caretOffset);
            }
        } else {
            while (ts.movePrevious() && ts.token().id() != TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                //NOOP - just find start
            }
            positions[0] = ts.offset();
            while (ts.moveNext() && ts.token().id() != TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                //NOOP - just find end
            }
            positions[1] = ts.offset() - tplMetaData.getCloseDelimiter().length() - 2;
        }
        return positions;
    }

    private static int[] getDelimiterPositions(TokenSequence<TplTopTokenId> ts, TplMetaData tplMetaData) throws BadLocationException {
        int[] positions = new int[2];
        while (ts.movePrevious() && ts.token().id() != TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
            //NOOP - just find start
        }
        positions[0] = ts.offset() + 1;
        while (ts.moveNext() && ts.token().id() != TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
            //NOOP - just find end
        }
        positions[1] = ts.offset() - tplMetaData.getCloseDelimiter().length() - 1;
        return positions;
    }

    private static boolean uncommentIt(TokenSequence<TplTopTokenId> ts) {
        if (ts.token().id() == TplTopTokenId.T_COMMENT) {
            return true;
        } else if (ts.token().id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
            return ts.moveNext() && ts.token().id() == TplTopTokenId.T_COMMENT;
        } else if (ts.token().id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
            return ts.movePrevious() && ts.token().id() == TplTopTokenId.T_COMMENT;
        }
        return false;
    }
}
