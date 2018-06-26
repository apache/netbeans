/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ToggleBlockCommentAction extends BaseAction {
    static final long serialVersionUID = -1L;
    private static final Logger LOGGER = Logger.getLogger(ToggleBlockCommentAction.class.getName());
    private static final String FORCE_COMMENT = "force-comment"; //NOI18N
    private static final String FORCE_UNCOMMENT = "force-uncomment"; //NOI18N
    private static final String COMMENT_DELIMITER_PART = "*"; //NOI18N
    private static final int COMMENT_DELIMITER_PART_LENGTH = COMMENT_DELIMITER_PART.length(); //NOI18N

    public ToggleBlockCommentAction() {
        super(ExtKit.toggleCommentAction);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled() || !(target.getDocument() instanceof BaseDocument)) {
                target.getToolkit().beep();
                return;
            }
            final int caretOffset = Utilities.isSelectionShowing(target) ? target.getSelectionStart() : target.getCaretPosition();
            final BaseDocument baseDocument = (BaseDocument) target.getDocument();
            final AtomicBoolean processedByLatte = new AtomicBoolean(false);
            baseDocument.runAtomic(new Runnable() {

                @Override
                public void run() {
                    performLatteAction(baseDocument, caretOffset, processedByLatte);
                }
            });
            if (!processedByLatte.get()) {
                performDefaultAction(evt, target);
            }
        }
    }

    private void performLatteAction(BaseDocument baseDocument, int caretOffset, AtomicBoolean processedByLatte) {
        TokenSequence<? extends LatteTopTokenId> topTs = LatteLexerUtils.getLatteTopTokenSequence(baseDocument, caretOffset);
        if (topTs != null) {
            topTs.move(caretOffset);
            if (topTs.moveNext() || topTs.movePrevious()) {
                Token<? extends LatteTopTokenId> token = topTs.token();
                if (token != null) {
                    LatteTopTokenId tokenId = token.id();
                    if (tokenId == LatteTopTokenId.T_LATTE_COMMENT || tokenId == LatteTopTokenId.T_LATTE_COMMENT_DELIMITER) {
                        uncomment(baseDocument, topTs);
                        processedByLatte.set(true);
                    } else if (tokenId == LatteTopTokenId.T_LATTE || tokenId == LatteTopTokenId.T_LATTE_OPEN_DELIMITER
                            || tokenId == LatteTopTokenId.T_LATTE_CLOSE_DELIMITER || tokenId == LatteTopTokenId.T_LATTE_ERROR) {
                        comment(baseDocument, topTs, processedByLatte);
                    }
                }
            }
        } else {
            processedByLatte.set(false);
        }
    }

    private void comment(BaseDocument baseDocument, TokenSequence<? extends LatteTopTokenId> topTs, AtomicBoolean processedByLatte) {
        if (moveToOpeningDelimiter(topTs)) {
            int start = topTs.offset() + topTs.token().length();
            if (moveToClosingDelimiter(topTs)) {
                int end = topTs.offset() + COMMENT_DELIMITER_PART_LENGTH;
                try {
                    baseDocument.insertString(start, COMMENT_DELIMITER_PART, null);
                    baseDocument.insertString(end, COMMENT_DELIMITER_PART, null);
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
                processedByLatte.set(true);
            }
        }
    }

    private static boolean moveToOpeningDelimiter(TokenSequence<? extends LatteTopTokenId> topTs) {
        boolean result = false;
        while (topTs.movePrevious()) {
            Token<? extends LatteTopTokenId> token = topTs.token();
            if (token == null || token.id() == LatteTopTokenId.T_HTML) {
                topTs.moveNext();
                token = topTs.token();
                if (token != null && token.id() == LatteTopTokenId.T_LATTE_OPEN_DELIMITER) {
                    result = true;
                } else {
                    result = false;
                }
                break;
            } else {
                continue;
            }
        }
        return result;
    }

    private static boolean moveToClosingDelimiter(TokenSequence<? extends LatteTopTokenId> topTs) {
        boolean result = false;
        while (topTs.moveNext()) {
            Token<? extends LatteTopTokenId> token = topTs.token();
            if (token == null || token.id() == LatteTopTokenId.T_HTML) {
                topTs.movePrevious();
                token = topTs.token();
                if (token != null && token.id() == LatteTopTokenId.T_LATTE_CLOSE_DELIMITER) {
                    result = true;
                } else {
                    result = false;
                }
                break;
            } else {
                continue;
            }
        }
        return result;
    }

    private void uncomment(BaseDocument baseDocument, TokenSequence<? extends LatteTopTokenId> topTs) {
        moveToOpeningCommentDelimiter(topTs);
        int start = topTs.offset() + topTs.token().length() - COMMENT_DELIMITER_PART_LENGTH;
        moveToClosingCommentDelimiter(topTs);
        int end = topTs.offset() - COMMENT_DELIMITER_PART_LENGTH;
        try {
            baseDocument.remove(start, COMMENT_DELIMITER_PART_LENGTH);
            baseDocument.remove(end, COMMENT_DELIMITER_PART_LENGTH);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private static void moveToOpeningCommentDelimiter(TokenSequence<? extends LatteTopTokenId> topTs) {
        while (topTs.movePrevious()) {
            Token<? extends LatteTopTokenId> token = topTs.token();
            if (token != null && (token.id() == LatteTopTokenId.T_LATTE_COMMENT || token.id() == LatteTopTokenId.T_LATTE_COMMENT_DELIMITER)) {
                continue;
            } else {
                topTs.moveNext();
                break;
            }
        }
    }

    private static void moveToClosingCommentDelimiter(TokenSequence<? extends LatteTopTokenId> topTs) {
        topTs.moveNext(); // T_LATTE_COMMENT - content
        topTs.moveNext(); // T_LATTE_COMMENT_DELIMITER - closing
    }

    private void performDefaultAction(ActionEvent evt, JTextComponent target) {
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
