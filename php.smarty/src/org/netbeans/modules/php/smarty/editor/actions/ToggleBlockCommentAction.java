/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                    final TokenSequence<TplTopTokenId> ts = (TokenSequence<TplTopTokenId>) LexerUtils.getTplTopTokenSequence(doc, caretOffset);
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
