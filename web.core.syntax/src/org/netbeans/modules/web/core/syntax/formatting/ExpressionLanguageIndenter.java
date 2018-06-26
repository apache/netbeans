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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
