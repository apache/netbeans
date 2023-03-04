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

package org.netbeans.modules.web.core.syntax.formatting;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter.OffsetRanges;
import org.netbeans.modules.web.indent.api.support.IndentCommand;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;

public class ExpressionLanguageIndenter extends AbstractIndenter<ELTokenId> {

    private boolean inExression;
    private int indent = -1;
    
    public ExpressionLanguageIndenter(Context context) {
        super(ELTokenId.language(), context);
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<ELTokenId> ts, int startOffset, int endOffset, OffsetRanges rangesToIgnore) throws BadLocationException {
        // just jump to beginning:
        ts.move(startOffset, true);
        ts.moveNext();
        return LexUtilities.getTokenSequenceStartOffset(ts);
    }

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<ELTokenId> context, List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        List<IndentCommand> result = new ArrayList<IndentCommand>();
        IndentCommand ic;
        if (context.isLanguageBlockStart()) {
            inExression = false;
        }
        if (inExression) {
            ic = new IndentCommand(IndentCommand.Type.CONTINUE, context.getLineStartOffset(), getIndentationSize());
            if (indent != -1) {
                ic.setFixedIndentSize(indent);
            }
        } else {
            ic = new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getLineStartOffset(), getIndentationSize());
            inExression = true;
            context.getJoinedTokenSequences().move(context.getLineStartOffset());
            // try to find fixed indent:
            int lineRealFirstNonWhite = Utilities.getRowFirstNonWhite(getDocument(), context.getLineStartOffset());
            if (lineRealFirstNonWhite != -1 && context.getLineStartOffset() > lineRealFirstNonWhite) {
                int start = context.getLineStartOffset();
                context.getJoinedTokenSequences().move(context.getLineStartOffset());
                context.getJoinedTokenSequences().moveNext();
                Token<ELTokenId> tok = context.getJoinedTokenSequences().token();
                if (tok != null && tok.id() == ELTokenId.STRING_LITERAL && tok.text().toString().startsWith("\"")) {
                    start++;
                }
                indent = start - lineRealFirstNonWhite;
            } else {
                indent = -1;
            }
        }
        result.add(ic);
        if (context.getNextLineStartOffset() != -1) {
            if (context.isLanguageBlockEnd()) {
                ic = new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getNextLineStartOffset(), getIndentationSize());
            } else {
                ic = new IndentCommand(IndentCommand.Type.CONTINUE, context.getNextLineStartOffset(), getIndentationSize());
            }
            preliminaryNextLineIndent.add(ic);
        }
        return result;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<ELTokenId> token) {
        return token.id() == ELTokenId.WHITESPACE ||
                (token.id() == ELTokenId.STRING_LITERAL && token.text().toString().trim().length() == 0);
    }

    @Override
    protected void reset() {
        inExression = false;
        indent = -1;
    }

}
