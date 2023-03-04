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
package org.netbeans.modules.php.latte.indent;

import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.latte.LatteSyntax;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.indent.api.support.IndentCommand;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteIndenter extends AbstractIndenter<LatteTopTokenId> {
    private static final Logger LOGGER = Logger.getLogger(LatteIndenter.class.getName());
    private Stack<LatteStackItem> stack = null;
    private int preservedLineIndentation = -1;

    public LatteIndenter(Context context) {
        super(LatteTopTokenId.language(), context);
    }

    @Override
    protected int getFormatStableStart(
            JoinedTokenSequence<LatteTopTokenId> ts,
            int startOffset,
            int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore) throws BadLocationException {
        return 0;
    }

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<LatteTopTokenId> context, List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        Stack<LatteStackItem> blockStack = getStack();
        List<IndentCommand> iis = new ArrayList<>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<LatteTopTokenId> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        boolean isBlockMacro = false;
        boolean isElseMacro = false;
        boolean afterDelimiter = false;
        boolean nonControlMacro = false;
        int embeddingLevel = 0;
        String lastMacro = "";
        // iterate over tokens on the line and push to stack any changes
        while (!context.isBlankLine() && ts.moveNext()
                 && ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset())
                || ts.offset() <= context.getLineEndOffset())) {
            Token<LatteTopTokenId> token = ts.token();
            if (token == null) {
                continue;
            } else if (ts.embedded() != null) {
                // indent for latte macro of the zero embedding level
                if (embeddingLevel == 1 && afterDelimiter) {
                    if (token.id() == LatteTopTokenId.T_LATTE && context.isIndentThisLine()) {
                        String markupToken = getMarkupTokenName(token);
                        isBlockMacro = LatteSyntax.isBlockMacro(markupToken) && !isShortedBlockMacro(token);
                        if (isBlockMacro) {
                            lastMacro = markupToken;
                            isElseMacro = LatteSyntax.isElseMacro(markupToken);
                        }
                    } else {
                        isBlockMacro = false;
                        isElseMacro = false;
                    }
                } else {
                    // non-latte token
                    // looks for entered block macro for the indentation - issue #226926
                    if (token.text().toString().indexOf("\n") == -1) { //NOI18N
                        nonControlMacro = true;
                    }
                }
                continue;
            }

            if (token.id() == LatteTopTokenId.T_LATTE_OPEN_DELIMITER) {
                afterDelimiter = true;
                embeddingLevel++;
                LatteStackItem state = new LatteStackItem(StackItemState.IN_RULE);
                blockStack.push(state);
            } else if (token.id() == LatteTopTokenId.T_LATTE_CLOSE_DELIMITER) {
                afterDelimiter = false;
                if (isInState(blockStack, StackItemState.IN_RULE)) {
                    // check that IN_RULE is the last state
                    LatteStackItem item = blockStack.pop();
                    embeddingLevel--;
                    if (embeddingLevel == 0) {
                        assert item.state == StackItemState.IN_RULE;
                        if (isBlockMacro) {
                            if (!blockStack.isEmpty()
                                    // issue #219375 - happens when the selection ends inside the Latte tag
                                    && blockStack.peek().getMacro() != null
                                    && LatteSyntax.isRelatedMacro(lastMacro, blockStack.peek().getMacro())) {
                                if (isElseMacro) {
                                    String macro = blockStack.pop().getMacro();
                                    blockStack.push(new LatteStackItem(StackItemState.IN_BODY, macro));
                                } else {
                                    blockStack.pop();
                                }
                                if (!nonControlMacro) {
                                    iis.add(new IndentCommand(IndentCommand.Type.RETURN, preservedLineIndentation));
                                }
                                nonControlMacro = false;
                            } else {
                                blockStack.push(new LatteStackItem(StackItemState.IN_BODY, lastMacro));
                            }
                        }
                    }
                }
            } else if (isCommentToken(token)) {
                int start = context.getLineStartOffset();
                if (start < ts.offset()) {
                    start = ts.offset();
                }
                int commentEndOffset = ts.offset() + ts.token().text().toString().trim().length() - 1;
                int end = context.getLineEndOffset();
                if (end > commentEndOffset) {
                    end = commentEndOffset;
                }
                if (start > end) {
                    // do nothing
                } else if (start == ts.offset()) {
                    if (end < commentEndOffset) {
                        // if comment ends on next line put formatter to IN_COMMENT state
                        int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
                        preservedLineIndentation = start - lineStart;
                    }
                } else if (end == commentEndOffset) {
                    String text = getDocument().getText(start, end - start + 1).trim();
                    if (!text.startsWith("*/")) {
                        // if line does not start with '*/' then treat it as unformattable
                        IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                        ic.setFixedIndentSize(preservedLineIndentation);
                        iis.add(ic);
                    }
                    preservedLineIndentation = -1;
                } else {
                    IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                    ic.setFixedIndentSize(preservedLineIndentation);
                    iis.add(ic);
                }
            }
        }
        if (context.isBlankLine() && iis.isEmpty()) {
            IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
            ic.setFixedIndentSize(preservedLineIndentation);
            iis.add(ic);
        }

        if (iis.isEmpty()) {
            iis.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getLineStartOffset()));
        }

        if (context.getNextLineStartOffset() != -1) {
            getIndentFromState(preliminaryNextLineIndent, false, context.getNextLineStartOffset());
            if (preliminaryNextLineIndent.isEmpty()) {
                preliminaryNextLineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getNextLineStartOffset()));
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            for (IndentCommand command : iis) {
                LOGGER.log(Level.FINE, command.toString());
            }
        }
        return iis;
    }

    private static boolean isShortedBlockMacro(Token<LatteTopTokenId> token) {
        return token.text().toString().endsWith("/"); //NOI18N
    }

    private String getMarkupTokenName(Token<LatteTopTokenId> token) {
        String result = "";
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(token.text(), LatteMarkupTokenId.language());
        TokenSequence<LatteMarkupTokenId> sequence = th.tokenSequence(LatteMarkupTokenId.language());
        while (sequence.moveNext()) {
            if (sequence.token().id() == LatteMarkupTokenId.T_WHITESPACE) {
                continue;
            } else {
                result = sequence.token().text().toString();
                break;
            }
        }
        return result;
    }

    private boolean isCommentToken(Token<LatteTopTokenId> token) {
        return token.id() == LatteTopTokenId.T_LATTE_COMMENT || token.id() == LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<LatteStackItem> blockStack = getStack();

        int lastUnprocessedItem = blockStack.size();
        for (int i = blockStack.size() - 1; i >= 0; i--) {
            if (!blockStack.get(i).processed) {
                lastUnprocessedItem = i;
            } else {
                break;
            }
        }
        for (int i = lastUnprocessedItem; i < blockStack.size(); i++) {
            LatteStackItem item = blockStack.get(i);
            assert !item.processed : item;
            if (item.state == StackItemState.IN_BODY) {
                IndentCommand ii = new IndentCommand(IndentCommand.Type.INDENT, lineStartOffset);
                if (item.indent != -1) {
                    ii.setFixedIndentSize(item.indent);
                }
                iis.add(ii);
                if (updateState) {
                    item.processed = Boolean.TRUE;
                }
            } else if (item.state == StackItemState.BODY_FINISHED) {
                IndentCommand ii = new IndentCommand(IndentCommand.Type.RETURN, lineStartOffset);
                iis.add(ii);
                if (updateState) {
                    item.processed = Boolean.TRUE;
                    blockStack.remove(i);
                    i--;
                }
            }
        }
    }

    private Stack<LatteStackItem> getStack() {
        return stack;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<LatteTopTokenId> token) {
        return false;
    }

    @Override
    protected void reset() {
        stack = new Stack<>();
        preservedLineIndentation = -1;
    }

    private boolean isInState(Stack<LatteStackItem> stack, StackItemState state) {
        for (LatteStackItem item : stack) {
            if (item.state == state) {
                return true;
            }
        }
        return false;
    }

    private static enum StackItemState {
        IN_BODY,
        IN_RULE,
        IN_VALUE,
        RULE_FINISHED,
        BODY_FINISHED;
    }

    private static final class LatteStackItem {
        private StackItemState state;
        private Boolean processed = false;
        private String macro;
        private int indent;

        private LatteStackItem(StackItemState state, String macro) {
            assert state != StackItemState.IN_BODY || (state == StackItemState.IN_BODY && !macro.isEmpty());
            this.macro = macro;
            this.state = state;
            this.indent = -1;
        }

        private LatteStackItem(StackItemState state) {
            this.state = state;
            this.indent = -1;
        }

        public String getMacro() {
            return macro;
        }

        @Override
        public String toString() {
            return "LatteStackItem[state=" + state + ",indent=" + indent + ",processed=" + processed + ",command=" + macro + "]";
        }

    }

}