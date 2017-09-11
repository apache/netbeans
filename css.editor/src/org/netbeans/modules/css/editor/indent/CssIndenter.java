/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor.indent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.indent.api.support.IndentCommand;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;

/**
 * Logic of building indenter state is worth rewritting. It evolved over the time
 * and got into quite a messy state. It probably does all what's ever needed from
 * point of view of CSS language and there is enough unit tests to prove it but
 * it is just ugly and hard to read considering simplicity of CSS syntax.
 */
public class CssIndenter extends AbstractIndenter<CssTokenId> {

    private Stack<CssStackItem> stack = null;
    private int preservedLineIndentation = -1;
    /**
     * Model inComment state as a flag independent on stack of CssStackItem
     * to handle properly case like multiline comment within CSS value. In such
     * a case indenter should stay in CONTINUE state regardless of comments and
     * comments should in addition get PRESERVE_INDENTATION command.
     */
    private boolean inComment = false;
    // 218884:
    private boolean previousCommentLineStartsWithAsterix = false;

    private boolean inMedia = false;

    public CssIndenter(Context context) {
        super(CssTokenId.language(), context);
    }

    private Stack<CssStackItem> getStack() {
        return stack;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<CssTokenId> token) {
        return token.id() == CssTokenId.WS;
    }

    private boolean isCommentToken(Token<CssTokenId> token) {
        return token.id() == CssTokenId.COMMENT;
    }

    @Override
    protected void reset() {
        stack = new Stack<>();
        inComment = false;
        previousCommentLineStartsWithAsterix = false;
        inMedia = false;
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<CssTokenId> ts, int startOffset, int endOffset,
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
            Token<CssTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == CssTokenId.IDENT && ts.offset() < startOffset && balance == 0) {
                int[] index = ts.index();
                ts.moveNext();
                Token tk = LexUtilities.findNext(ts, Arrays.asList(CssTokenId.WS));
                ts.moveIndex(index);
                ts.moveNext();
                if (tk != null && tk.id() == CssTokenId.LBRACE) {
                    if (ts.movePrevious()) {
                        tk = LexUtilities.findPrevious(ts, Arrays.asList(CssTokenId.WS, 
                                CssTokenId.IDENT, CssTokenId.MEDIA_SYM, CssTokenId.COMMA,
                                CssTokenId.GREATER, CssTokenId.PLUS));
                        if (tk != null) {
                            ts.moveNext();
                            tk = LexUtilities.findNext(ts, Arrays.asList(CssTokenId.WS));
                        }
                    }
                    return ts.offset();
                }
            } else if (id == CssTokenId.RBRACE) {
                balance++;
            } else if (id == CssTokenId.LBRACE) {
                balance--;
            } else if (id == CssTokenId.MEDIA_SYM && ts.offset() < startOffset && balance == 0) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return LexUtilities.getTokenSequenceStartOffset(ts);
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<CssStackItem> blockStack = getStack();

        int lastUnprocessedItem = blockStack.size();
        for (int i = blockStack.size()-1; i>=0; i--) {
            if (!blockStack.get(i).processed) {
                lastUnprocessedItem = i;
            } else {
                break;
            }
        }
        for (int i=lastUnprocessedItem; i< blockStack.size(); i++) {
            CssStackItem item = blockStack.get(i);
            assert !item.processed : item;
            if (item.state == StackItemState.IN_MEDIA ||
                item.state == StackItemState.IN_RULE) {
                IndentCommand ii = new IndentCommand(IndentCommand.Type.INDENT, lineStartOffset);
                if (item.indent != -1) {
                    ii.setFixedIndentSize(item.indent);
                }
                iis.add(ii);
                if (updateState) {
                    item.processed = Boolean.TRUE;
                }
            } else if (item.state == StackItemState.IN_VALUE) {
                IndentCommand ii = new IndentCommand(IndentCommand.Type.CONTINUE, lineStartOffset);
                if (item.indent != -1) {
                    ii.setFixedIndentSize(item.indent);
                }
                iis.add(ii);
                // do not mark IN_VALUE as processed so that it is applied on all next lines:
            } else if (item.state == StackItemState.RULE_FINISHED ||
                    item.state == StackItemState.MEDIA_FINISHED) {
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
    protected List<IndentCommand> getLineIndent(IndenterContextData<CssTokenId> context,
            List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        Stack<CssStackItem> blockStack = getStack();
        List<IndentCommand> iis = new ArrayList<>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<CssTokenId> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        boolean ruleWasDefined = false;
        boolean mediaWasDefined = false;
        int lastLBrace = -1;
        // iterate over tokens on the line and push to stack any changes
        while (!context.isBlankLine() && ts.moveNext() &&
            ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset()) ||
                    ts.offset() <= context.getLineEndOffset()) ) {
            Token<CssTokenId> token = ts.token();
            if (token == null || ts.embedded() != null) {
                continue;
            }

            if (lastLBrace != -1 && token.id() != CssTokenId.WS) {
                CssStackItem state = blockStack.peek();
                assert (state.state == StackItemState.IN_RULE || state.state == StackItemState.IN_MEDIA);
                if (!isCommentToken(token)) {
                    state.indent = ts.offset() - context.getLineNonWhiteStartOffset();
                }
                lastLBrace = -1;
            }
            if (token.id() == CssTokenId.LBRACE) {
                if (inMedia) {
                    inMedia = false;
                    lastLBrace = ts.offset();
                    blockStack.push(new CssStackItem(StackItemState.IN_MEDIA));
                    mediaWasDefined = true;
                    //SASS&LESS - code blocks (e.g. rules) may be nested
//                } else if (!isInState(blockStack, StackItemState.IN_RULE)) {
                } else {
                    //SASS&LESS:
                    //
                    //first check if we are in IN_VALUE state and if so pop that
                    //state:
                    //
                    //div {
                    //   a:active {
                    //      color: red;
                    //   }
                    //}
                    //
                    if(isInState(blockStack, StackItemState.IN_VALUE)) {
                        blockStack.pop();
                    }
                    
                    CssStackItem state = new CssStackItem(StackItemState.IN_RULE);
                    lastLBrace = ts.offset();
                    blockStack.push(state);
                    ruleWasDefined = true;
                }
            } else if (token.id() == CssTokenId.COLON) {
                if (!isInState(blockStack, StackItemState.IN_VALUE) && isInState(blockStack, StackItemState.IN_RULE)) {
                    blockStack.push(new CssStackItem(StackItemState.IN_VALUE));
                }
            } else if (token.id() == CssTokenId.SEMI) {
                if (isInState(blockStack, StackItemState.IN_VALUE)) {
                    CssStackItem item = blockStack.pop();
                    assert item.state == StackItemState.IN_VALUE;
                }
            } else if (token.id() == CssTokenId.RBRACE) {
                if (isInState(blockStack, StackItemState.IN_RULE)) {
                    CssStackItem item = blockStack.pop();
                    if (item.state == StackItemState.IN_VALUE) {
                        // in cases like:
                        //
                        //  .rcol {
                        //    width:249px
                        //  }
                        //
                        item = blockStack.pop();
                    } else if (item.state == StackItemState.RULE_FINISHED) {
                        //SASS/LESS: nested rules: 
                        //div { .clz { ... } }
                        // if top state is RULE_FINISHED then remove the IN_RULE state
                        // underneath
                        // as unlimited number of rules might be nested, we need
                        // to find the closest IN_RULE item in the stack preceeded
                        // by RULE_FINISHED items
                        ListIterator<CssStackItem> listIterator = blockStack.listIterator();
                        while(listIterator.hasNext()) {
                            CssStackItem stackItem = listIterator.next();
                            if(stackItem.state == StackItemState.IN_RULE) {
                                item = stackItem;
                                listIterator.remove();
                                break;
                            }
                        }
                        
                        blockStack.push(new CssStackItem(StackItemState.RULE_FINISHED));
                    }
                    assert item.state == StackItemState.IN_RULE : item;
                    if (ts.offset() == context.getLineNonWhiteStartOffset()) {
                        // if "}" is first character on line then it changes line's indentation:
                        iis.add(new IndentCommand(IndentCommand.Type.RETURN, context.getLineStartOffset()));
                    } else {
                        // eg. "blue; }" - this does not have impact on indentation of current line but
                        // will need to be addressed on next line; pushing state RULE_FINISHED.
                        // but only if rule was not defined on line completely, eg. "a {b:c}" in which
                        // case nothing needs to be done.
                        if (!ruleWasDefined) {
                            blockStack.push(new CssStackItem(StackItemState.RULE_FINISHED));
                        }
                    }
                } else if (isInState(blockStack, StackItemState.IN_MEDIA)) {
                    CssStackItem item = blockStack.pop();
                    if (item.state == StackItemState.RULE_FINISHED) {
                        // if top state is RULE_FINISHED then remove state underneath
                        // which should be IN_MEDIA
                        item = blockStack.pop();
                        blockStack.push(new CssStackItem(StackItemState.RULE_FINISHED));
                    }
                    assert item.state == StackItemState.IN_MEDIA : item;
                    if (ts.offset() == context.getLineNonWhiteStartOffset()) {
                        // if "}" is first character on line then it changes line's indentation:
                        iis.add(new IndentCommand(IndentCommand.Type.RETURN, context.getLineStartOffset()));
                    } else {
                        // see desc of RULE_FINISHED above - the same logic applies here:
                        if (!mediaWasDefined) {
                            blockStack.push(new CssStackItem(StackItemState.MEDIA_FINISHED));
                        }
                    }
                }
            } else if (isCommentToken(token)) {
                    int start = context.getLineStartOffset();
                    if (start < ts.offset()) {
                        start = ts.offset();
                    }
                    int commentEndOffset = ts.offset()+ts.token().text().toString().trim().length()-1;
                    int end = context.getLineEndOffset();
                    if (end > commentEndOffset) {
                        end = commentEndOffset;
                    }
                    if (start > end) {
                        assert !inComment : "token="+token.text()+" start="+start+" end="+end;
                        // do nothing
                    } else if (start == ts.offset()) {
                        if (end < commentEndOffset) {
                            // if comment ends on next line put formatter to IN_COMMENT state
                            inComment = true;
                            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
                            preservedLineIndentation = start - lineStart;
                        }
                    } else if (end == commentEndOffset) {
                        String text = getDocument().getText(start, end-start+1).trim();
                        if (!text.startsWith("*/") || previousCommentLineStartsWithAsterix) {
                            // if line does not start with '*/' then treat it as unformattable
                            IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                            ic.setFixedIndentSize(preservedLineIndentation);
                            iis.add(ic);
                        }
                        assert inComment : "token="+token.text()+" start="+start+" end="+end;
                        inComment = false;
                        preservedLineIndentation = -1;
                    } else {
                        assert inComment : "token="+token.text()+" start="+start+" end="+end;
                        IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                        ic.setFixedIndentSize(preservedLineIndentation);
                        iis.add(ic);
                        previousCommentLineStartsWithAsterix = getDocument().getText(start, end-start+1).trim().startsWith("*");
                    }
            } else if (token.id() == CssTokenId.MEDIA_SYM) {
                // #164493:
                if (blockStack.size() == 0) {
                    inMedia = true;
                }
            }
        }
        if (context.isBlankLine() && inComment && iis.isEmpty()) {
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

        return iis;
    }

    private boolean isInState(Stack<CssStackItem> stack, StackItemState state) {
        for (CssStackItem item : stack) {
            if (item.state == state) {
                return true;
            }
        }
        return false;
    }

    private static enum StackItemState {
        IN_MEDIA,
        IN_RULE,
        IN_VALUE,
        RULE_FINISHED,
        MEDIA_FINISHED,
        ;
    }

    private static class CssStackItem  {
        private StackItemState state;
        private Boolean processed = false;
        private int indent;

        private CssStackItem(StackItemState state) {
            this.state = state;
            this.indent = -1;
        }

        @Override
        public String toString() {
            return "CssStackItem[state="+state+",indent="+indent+",processed="+processed+"]";
        }

    }

}
