/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
