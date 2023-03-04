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
package org.netbeans.modules.php.twig.editor.format;

import java.util.ArrayList;
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
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.twig.editor.TwigSyntax;
import org.netbeans.modules.php.twig.editor.lexer.TwigBlockTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigLexerUtils;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigVariableTokenId;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.indent.api.support.IndentCommand;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;

/**
 * Same indenter as is in Smarty and Latte.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigIndenter extends AbstractIndenter<TwigTopTokenId> {
    private static final Logger LOGGER = Logger.getLogger(TwigIndenter.class.getName());
    private Stack<TwigStackItem> stack = null;
    private int preservedLineIndentation = -1;
    private static final String SET_TAG = "set"; // NOI18N
    private static final String TRANS_TAG = "trans"; // NOI18N

    public TwigIndenter(Context context) {
        super(TwigTopTokenId.language(), context);
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<TwigTopTokenId> ts, int startOffset, int endOffset, OffsetRanges rangesToIgnore) throws BadLocationException {
        return 0;
    }

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<TwigTopTokenId> context, List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        Stack<TwigStackItem> blockStack = getStack();
        List<IndentCommand> iis = new ArrayList<>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<TwigTopTokenId> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        boolean isBlockMacro = false;
        boolean isElseMacro = false;
        boolean afterDelimiter = false;
        boolean nonControlMacro = false;
        int embeddingLevel = 0;
        String lastMacro = "";
        Token<TwigTopTokenId> lastToken = null; // #243184 for checking this case {% %}{% %}
        // iterate over tokens on the line and push to stack any changes
        while (!context.isBlankLine() && ts.moveNext()
                 && ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset())
                || ts.offset() <= context.getLineEndOffset())) {
            Token<TwigTopTokenId> token = ts.token();
            if (token == null) {
                lastToken = null;
                continue;
            } else if (ts.embedded() != null) {
                // indent for latte macro of the zero embedding level
                if (embeddingLevel == 1 && afterDelimiter) {
                    if (token.id() == TwigTopTokenId.T_TWIG_BLOCK && context.isIndentThisLine()) {
                        String markupToken = getMarkupTokenName(token);
                        isBlockMacro = TwigSyntax.isBlockMacro(markupToken) && !isShortedBlockMacro(token);
                        if (isBlockMacro) {
                            lastMacro = markupToken;
                            isElseMacro = TwigSyntax.isElseMacro(markupToken);
                        }
                    } else {
                        isBlockMacro = false;
                        isElseMacro = false;
                    }
                } else {
                    // non-twig token
                    // looks for entered block macro for the indentation - issue #226926
                    if (token.text().toString().indexOf("\n") == -1) { //NOI18N
                        nonControlMacro = true;
                    }
                }
                lastToken = token;
                continue;
            }

            if (token.id() == TwigTopTokenId.T_TWIG_BLOCK_START) {
                if (lastToken != null && lastToken.id() == TwigTopTokenId.T_TWIG_BLOCK_END) {
                     nonControlMacro = true;
                }
                afterDelimiter = true;
                embeddingLevel++;
                TwigStackItem state = new TwigStackItem(StackItemState.IN_RULE);
                blockStack.push(state);
            } else if (token.id() == TwigTopTokenId.T_TWIG_BLOCK_END) {
                afterDelimiter = false;
                if (isInState(blockStack, StackItemState.IN_RULE)) {
                    // check that IN_RULE is the last state
                    TwigStackItem item = blockStack.pop();
                    embeddingLevel--;
                    if (embeddingLevel == 0) {
                        assert item.state == StackItemState.IN_RULE;
                        if (isBlockMacro) {
                            if (!blockStack.isEmpty()
                                    // issue #219375 - happens when the selection ends inside the Latte tag
                                    && blockStack.peek().getMacro() != null
                                    && TwigSyntax.isRelatedMacro(lastMacro, blockStack.peek().getMacro())) {
                                if (isElseMacro) {
                                    String macro = blockStack.pop().getMacro();
                                    blockStack.push(new TwigStackItem(StackItemState.IN_BODY, macro));
                                } else {
                                    blockStack.pop();
                                }
                                if (!nonControlMacro) {
                                    iis.add(new IndentCommand(IndentCommand.Type.RETURN, preservedLineIndentation));
                                }
                                nonControlMacro = false;
                            } else {
                                blockStack.push(new TwigStackItem(StackItemState.IN_BODY, lastMacro));
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
                        int lineStart = LineDocumentUtils.getLineStart(getDocument(), ts.offset());
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
            lastToken = token;
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

    private static boolean isShortedBlockMacro(Token<TwigTopTokenId> token) {
        String tokenText = token.text().toString();
        return tokenText.endsWith("/") // NOI18N
                || isSetBlock(tokenText)
                || isShortedTransBlock(tokenText);
    }

    private static boolean isSetBlock(String tokenText){
        // e.g. {% set foo = 'foo' %}
        return tokenText.contains(SET_TAG) && tokenText.contains("="); // NOI18N
    }

    private static boolean isShortedTransBlock(String tokenText) {
        // e.g. {% trans "something" %}
        int indexOfTrans = tokenText.indexOf(TRANS_TAG);
        if (indexOfTrans != -1) {
            String prefix = tokenText.substring(0, indexOfTrans).trim();
            if (!prefix.isEmpty()) {
                return false;
            }
            String substring = tokenText.substring(indexOfTrans + TRANS_TAG.length()).trim();
            if (!substring.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String getMarkupTokenName(Token<TwigTopTokenId> token) {
        String result = "";
        TokenSequence<? extends TokenId> sequence = null;
        if (token.id() == TwigTopTokenId.T_TWIG_BLOCK) {
            TokenHierarchy<CharSequence> th = TokenHierarchy.create(token.text(), TwigBlockTokenId.language());
            sequence = th.tokenSequence(TwigBlockTokenId.language());
        } else if (token.id() == TwigTopTokenId.T_TWIG_VAR) {
            TokenHierarchy<CharSequence> th = TokenHierarchy.create(token.text(), TwigVariableTokenId.language());
            sequence = th.tokenSequence(TwigVariableTokenId.language());
        }
        if (sequence != null) {
            while (sequence.moveNext()) {
                if (sequence.token().id() != TwigBlockTokenId.T_TWIG_WHITESPACE
                        && sequence.token().id() != TwigVariableTokenId.T_TWIG_WHITESPACE
                        && !isWhitespaceControlModifier(sequence.token())) { // "-" is used for whitespace control
                    result = sequence.token().text().toString();
                    break;
                }
            }
        }
        return result;
    }

    private boolean isWhitespaceControlModifier(Token<? extends TokenId> token) {
        TokenId id = token.id();
        if (id == TwigBlockTokenId.T_TWIG_OPERATOR || id == TwigVariableTokenId.T_TWIG_OPERATOR) {
            return TwigLexerUtils.textEquals(token.text(), '-');
        }
        return false;
    }

    private boolean isCommentToken(Token<TwigTopTokenId> token) {
        return token.id() == TwigTopTokenId.T_TWIG_COMMENT;
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<TwigStackItem> blockStack = getStack();

        int lastUnprocessedItem = blockStack.size();
        for (int i = blockStack.size() - 1; i >= 0; i--) {
            if (!blockStack.get(i).processed) {
                lastUnprocessedItem = i;
            } else {
                break;
            }
        }
        for (int i = lastUnprocessedItem; i < blockStack.size(); i++) {
            TwigStackItem item = blockStack.get(i);
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

    private Stack<TwigStackItem> getStack() {
        return stack;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<TwigTopTokenId> token) {
        return false;
    }

    @Override
    protected void reset() {
        stack = new Stack<>();
        preservedLineIndentation = -1;
    }

    private boolean isInState(Stack<TwigStackItem> stack, StackItemState state) {
        for (TwigStackItem item : stack) {
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

    private static final class TwigStackItem {
        private StackItemState state;
        private Boolean processed = false;
        private String macro;
        private int indent;

        private TwigStackItem(StackItemState state, String macro) {
            assert state != StackItemState.IN_BODY || (state == StackItemState.IN_BODY && !macro.isEmpty());
            this.macro = macro;
            this.state = state;
            this.indent = -1;
        }

        private TwigStackItem(StackItemState state) {
            this.state = state;
            this.indent = -1;
        }

        public String getMacro() {
            return macro;
        }

        @Override
        public String toString() {
            return "TwigStackItem[state=" + state + ",indent=" + indent + ",processed=" + processed + ",command=" + macro + "]"; //NOI18N
        }

    }

}
