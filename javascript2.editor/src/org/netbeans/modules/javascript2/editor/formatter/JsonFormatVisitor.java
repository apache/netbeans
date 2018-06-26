/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor;
import org.netbeans.modules.javascript2.json.parser.JsonParser;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

import static org.netbeans.modules.javascript2.editor.formatter.TokenUtils.*;

/**
 *
 * @author Dusan Balek
 */
public class JsonFormatVisitor extends JsonBaseVisitor<Void> {

    private final TokenUtils tokenUtils;

    public JsonFormatVisitor(FormatTokenStream tokenStream, TokenSequence<? extends JsTokenId> ts, int formatFinish) {
        this.tokenUtils = new TokenUtils(ts, tokenStream, formatFinish);
        for (FormatToken token : tokenStream) {
            if (token.getKind() == FormatToken.Kind.WHITESPACE) {
                LinkedList<FormatToken> newTokens = new LinkedList<FormatToken>() {
                    @Override
                    public boolean add(FormatToken token) {
                        FormatToken last = isEmpty() ? null : getLast();
                        if (last != null) {
                            last.setNext(token);
                            token.setPrevious(last);
                        }
                        return super.add(token);
                    }                    
                };
                int start = 0;
                for (int i = 0; i < token.getText().length(); i++) {
                    if (token.getText().charAt(i) == '\n') {
                        if (start > 0) {
                            newTokens.add(FormatToken.forAny(FormatToken.Kind.WHITESPACE, token.getOffset() + start, token.getText().subSequence(start, i), JsTokenId.WHITESPACE));
                        }
                        newTokens.add(FormatToken.forAny(FormatToken.Kind.EOL, token.getOffset() + i, token.getText().subSequence(start, i + 1), JsTokenId.EOL));
                        start = i + 1;
                    }
                }
                if (start > 0) {
                    newTokens.add(FormatToken.forAny(FormatToken.Kind.WHITESPACE, token.getOffset() + start, token.getText().subSequence(start, token.getText().length()), JsTokenId.WHITESPACE));
                }
                if (!newTokens.isEmpty()) {
                    FormatToken previous = token.previous();
                    if (previous != null) {
                        previous.setNext(newTokens.getFirst());
                        newTokens.getFirst().setPrevious(previous);
                    }
                    FormatToken next = token.next();
                    if (next != null) {
                        next.setPrevious(newTokens.getLast());
                        newTokens.getLast().setNext(next);
                    }
                }
            }
        }
    }

    @Override
    public Void visitObject(JsonParser.ObjectContext ctx) {
        // indentation mark
        FormatToken formatToken = tokenUtils.getPreviousToken(getStart(ctx), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_LEFT_BRACE));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_OBJECT_START));
            FormatToken previous = formatToken.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT));
            }
        }

        int objectFinish = getFinish(ctx);
        ctx.pair().stream().forEach((pair) -> {
            // mark property end
            FormatToken nextToken = tokenUtils.getNextToken(getFinish(pair), JsTokenId.OPERATOR_COMMA, objectFinish);
            if (nextToken != null) {
                appendTokenAfterLastVirtual(nextToken, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY), true);
            }
        });

        // put indentation mark after non white token preceeding curly bracket
        formatToken = tokenUtils.getPreviousNonWhiteToken(getFinish(ctx),
                getStart(ctx), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT_END));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_RIGHT_BRACE));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }
        return super.visitObject(ctx);
    }

    @Override
    public Void visitPair(JsonParser.PairContext ctx) {
        FormatToken colon = tokenUtils.getNextToken(getFinish(ctx.key()),
                JsTokenId.OPERATOR_COLON, getFinish(ctx));
        if (colon != null) {
            appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY_OPERATOR));
            FormatToken before = colon.previous();
            if (before != null) {
                appendTokenAfterLastVirtual(before, FormatToken.forFormat(FormatToken.Kind.BEFORE_PROPERTY_OPERATOR));
            }
        }
        return super.visitPair(ctx);
    }

    @Override
    public Void visitArray(JsonParser.ArrayContext ctx) {
        int start = getStart(ctx);
        int finish = getFinish(ctx);
        FormatToken leftBracket = tokenUtils.getNextToken(start, JsTokenId.BRACKET_LEFT_BRACKET, finish);            
        if (leftBracket != null) {
            if (leftBracket.previous() != null) {
                // mark beginning of the array (see issue #250150)
                appendToken(leftBracket.previous(), FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY));
            }
            appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_START));
            appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_BRACKET));
            appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            FormatToken rightBracket = tokenUtils.getPreviousToken(finish, JsTokenId.BRACKET_RIGHT_BRACKET, start + 1);
            if (rightBracket != null) {
                FormatToken previous = rightBracket.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_END));
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_BRACKET));
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }

        List<JsonParser.ValueContext> items = ctx.value();
        if (items != null && !items.isEmpty()) {
            int prevItemFinish = start;
            for (int i = 1; i < items.size(); i++) {
                JsonParser.ValueContext prevItem = items.get(i - 1);
                if (prevItem != null) {
                    prevItemFinish = getFinish(prevItem);
                }
                FormatToken comma = tokenUtils.getNextToken(prevItemFinish, JsTokenId.OPERATOR_COMMA, finish);
                if (comma != null) {
                    prevItemFinish = comma.getOffset();
                    appendTokenAfterLastVirtual(comma,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM));
                }
            }
        }
        return super.visitArray(ctx);
    }

    private int getStart(ParserRuleContext ctx) {
        return ctx.getStart().getStartIndex();
    }

    private int getFinish(ParserRuleContext ctx) {
        return ctx.getStop().getStopIndex();
    }
}
