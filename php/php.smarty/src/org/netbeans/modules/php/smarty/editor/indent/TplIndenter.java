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
package org.netbeans.modules.php.smarty.editor.indent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.smarty.editor.TplSyntax;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.indent.api.support.IndentCommand;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;

/**
 * @author Martin Fousek
 **/
public class TplIndenter extends AbstractIndenter<TplTopTokenId> {

    private static final Logger LOGGER = Logger.getLogger(TplIndenter.class.getName());

    private Stack<TplStackItem> stack = null;
    private int preservedLineIndentation = -1;

    public TplIndenter(Context context) {
        super(TplTopTokenId.language(), context);
    }

    private Stack<TplStackItem> getStack() {
        return stack;
    }

    private String getFunctionalTplTokenId(Token<TplTopTokenId> token) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(token.text(), TplTokenId.language());
        TokenSequence<TplTokenId> sequence = th.tokenSequence(TplTokenId.language());
        while (sequence.moveNext()) {
            if (sequence.token().id() == TplTokenId.WHITESPACE) {
                continue;
            } else {
                return CharSequenceUtilities.toString(sequence.token().text());
            }
        }
        return "";
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<TplTopTokenId> token) {
        return getFunctionalTplTokenId(token).equals("");
    }

    private boolean isCommentToken(Token<TplTopTokenId> token) {
        return token.id() == TplTopTokenId.T_COMMENT;
    }

    @Override
    protected void reset() {
        stack = new Stack<>();
        preservedLineIndentation = -1;
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<TplTopTokenId> ts, int startOffset, int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore) {
        // start from the end offset to properly calculate braces balance and
        // find correfct formatting start (consider case of a rule defined
        // within a media rule):
        ts.move(endOffset, false);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return LexUtilities.getTokenSequenceStartOffset(ts);
        }

        int balance = 0;
        // Look backwards to find a suitable context - beginning of a rule
        do {
            Token<TplTopTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == TplTopTokenId.T_SMARTY && ts.offset() < startOffset && balance == 0) {
                int[] index = ts.index();
                ts.moveNext();
                Token tk = LexUtilities.findNext(ts, Arrays.asList(TplTopTokenId.T_SMARTY));
                ts.moveIndex(index);
                ts.moveNext();
                if (tk != null && tk.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                    if (ts.movePrevious()) {
                        tk = LexUtilities.findPrevious(ts, Arrays.asList(TplTopTokenId.T_SMARTY));
                        if (tk != null) {
                            ts.moveNext();
                        }
                    }
                    return ts.offset();
                }
            } else if (id == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                balance++;
            } else if (id == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                balance--;
            } else if (id == TplTopTokenId.T_SMARTY && ts.offset() < startOffset && balance == 0) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return LexUtilities.getTokenSequenceStartOffset(ts);
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<TplStackItem> blockStack = getStack();

        int lastUnprocessedItem = blockStack.size();
        for (int i = blockStack.size() - 1; i >= 0; i--) {
            if (!blockStack.get(i).processed) {
                lastUnprocessedItem = i;
            } else {
                break;
            }
        }
        for (int i = lastUnprocessedItem; i < blockStack.size(); i++) {
            TplStackItem item = blockStack.get(i);
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

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<TplTopTokenId> context,
            List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        Stack<TplStackItem> blockStack = getStack();
        List<IndentCommand> iis = new ArrayList<>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<TplTopTokenId> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        boolean isSmartyBodyCommand = false;
        boolean isSmartyElseCommand = false;
        boolean afterDelimiter = false;
        boolean nonControlCommand = false;
        int embeddingLevel = 0;
        String lastTplCommand = "";
        // iterate over tokens on the line and push to stack any changes
        while (!context.isBlankLine() && ts.moveNext()
                && ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset())
                || ts.offset() <= context.getLineEndOffset())) {
            Token<TplTopTokenId> token = ts.token();
            if (token == null) {
                continue;
            } else if (ts.embedded() != null) {
                // indent for smarty command of the zero embedding level
                if (embeddingLevel == 1 && afterDelimiter) {
                    if (token.id() == TplTopTokenId.T_SMARTY && context.isIndentThisLine()) {
                        String tplToken = getFunctionalTplTokenId(token);
                        isSmartyBodyCommand = TplSyntax.isBlockCommand(tplToken);
                        if (isSmartyBodyCommand) {
                            lastTplCommand = tplToken;
                            isSmartyElseCommand = TplSyntax.isElseSmartyCommand(tplToken);
                        }
                    } else {
                        isSmartyBodyCommand = false;
                        isSmartyElseCommand = false;
                    }
                } else {
                    // non-smarty token
                    // looks for entered block command for the indentation - issue #226926
                    if (CharSequenceUtilities.indexOf(token.text(), "\n") == -1) { //NOI18N
                        nonControlCommand = true;
                    }
                }
                continue;
            }

            if (token.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                afterDelimiter = true;
                embeddingLevel++;
                TplStackItem state = new TplStackItem(StackItemState.IN_RULE);
                blockStack.push(state);
            } else if (token.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                afterDelimiter = false;
                if (isInState(blockStack, StackItemState.IN_RULE)) {
                    // check that IN_RULE is the last state
                    TplStackItem item = blockStack.pop();
                    embeddingLevel--;
                    if (embeddingLevel == 0) {
                        assert item.state == StackItemState.IN_RULE;
                        if (isSmartyBodyCommand) {
                            if (!blockStack.isEmpty()
                                    // issue #219375 - happens when the selection ends inside the Smarty tag
                                    && blockStack.peek().getCommand() != null
                                    && TplSyntax.isInRelatedCommand(lastTplCommand, blockStack.peek().getCommand())) {
                                if (isSmartyElseCommand) {
                                    String command = blockStack.pop().command;
                                    blockStack.push(new TplStackItem(StackItemState.IN_BODY, command));
                                } else {
                                    blockStack.pop();
                                }
                                if (!nonControlCommand) {
                                    iis.add(new IndentCommand(IndentCommand.Type.RETURN, context.getLineStartOffset()));
                                }
                                nonControlCommand = false;
                            } else {
                                blockStack.push(new TplStackItem(StackItemState.IN_BODY, lastTplCommand));
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
                        int lineStart = LineDocumentUtils.getLineStartOffset(getDocument(), ts.offset());
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

    private boolean isInState(Stack<TplStackItem> stack, StackItemState state) {
        for (TplStackItem item : stack) {
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

    private static class TplStackItem {

        private StackItemState state;
        private Boolean processed = false;
        private String command;
        private int indent;

        private TplStackItem(StackItemState state, String command) {
            assert state != StackItemState.IN_BODY || (state == StackItemState.IN_BODY && !command.isEmpty());
            this.command = command;
            this.state = state;
            this.indent = -1;
        }

        private TplStackItem(StackItemState state) {
            this.state = state;
            this.indent = -1;
        }

        public String getCommand() {
            return command;
        }

        @Override
        public String toString() {
            return "TplStackItem[state=" + state + ",indent=" + indent + ",processed=" + processed + ",command=" + command + "]";
        }
    }
}
