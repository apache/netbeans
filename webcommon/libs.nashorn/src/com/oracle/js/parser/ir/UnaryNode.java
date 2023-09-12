/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser.ir;

import static com.oracle.js.parser.TokenType.BIT_NOT;
import static com.oracle.js.parser.TokenType.DECPOSTFIX;
import static com.oracle.js.parser.TokenType.INCPOSTFIX;

import com.oracle.js.parser.Token;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * UnaryNode nodes represent single operand operations.
 */
public final class UnaryNode extends Expression implements Assignment<Expression> {
    /** Right hand side argument. */
    private final Expression expression;

    /**
     * Constructor
     *
     * @param token  token
     * @param rhs    expression
     */
    public UnaryNode(final long token, final Expression rhs) {
        this(token, Math.min(rhs.getStart(), Token.descPosition(token)), Math.max(Token.descPosition(token) + Token.descLength(token), rhs.getFinish()), rhs);
    }

    /**
     * Constructor
     *
     * @param token      token
     * @param start      start
     * @param finish     finish
     * @param expression expression
     */
    public UnaryNode(final long token, final int start, final int finish, final Expression expression) {
        super(token, start, finish);
        this.expression   = expression;
    }


    private UnaryNode(final UnaryNode unaryNode, final Expression expression) {
        super(unaryNode);
        this.expression   = expression;
    }

    /**
     * Is this an assignment - i.e. that mutates something such as a++
     *
     * @return true if assignment
     */
    @Override
    public boolean isAssignment() {
        switch (tokenType()) {
        case DECPOSTFIX:
        case DECPREFIX:
        case INCPOSTFIX:
        case INCPREFIX:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean isSelfModifying() {
        return isAssignment();
    }

    @Override
    public Expression getAssignmentDest() {
        return isAssignment() ? getExpression() : null;
    }

    @Override
    public Expression getAssignmentSource() {
        return getAssignmentDest();
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterUnaryNode(this)) {
            return visitor.leaveUnaryNode(setExpression((Expression)expression.accept(visitor)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterUnaryNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        final TokenType tokenType = tokenType();
        final String    name      = tokenType.getName();
        final boolean   isPostfix = tokenType == DECPOSTFIX || tokenType == INCPOSTFIX;

        if (tokenType == TokenType.SPREAD_ARRAY || tokenType == TokenType.SPREAD_OBJECT) {
            sb.append("...");
            getExpression().toString(sb, printType);
            return;
        }

        if (tokenType == TokenType.AWAIT) {
            // await expression
            sb.append("await ");
            getExpression().toString(sb, printType);
            return;
        }

        boolean rhsParen = tokenType.needsParens(getExpression().tokenType(), false);

        if (!isPostfix) {
            if (name == null) {
                sb.append(tokenType.name());
                rhsParen = true;
            } else {
                sb.append(name);

                if (tokenType.ordinal() > BIT_NOT.ordinal()) {
                    sb.append(' ');
                }
            }
        }

        if (rhsParen) {
            sb.append('(');
        }

        getExpression().toString(sb, printType);

        if (rhsParen) {
            sb.append(')');
        }

        if (isPostfix) {
            sb.append(tokenType == DECPOSTFIX ? "--" : "++");
        }
    }

    /**
     * Get the right hand side of this if it is inherited by a binary expression,
     * or just the expression itself if still Unary
     *
     * @see BinaryNode
     *
     * @return right hand side or expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Reset the right hand side of this if it is inherited by a binary expression,
     * or just the expression itself if still Unary
     *
     * @see BinaryNode
     *
     * @param expression right hand side or expression node
     * @return a node equivalent to this one except for the requested change.
     */
    public UnaryNode setExpression(final Expression expression) {
        if (this.expression == expression) {
            return this;
        }
        return new UnaryNode(this, expression);
    }
}
