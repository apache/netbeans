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

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * A wrapper for an expression that is in a position to be a join predecessor.
 */
public class JoinPredecessorExpression extends Expression {

    private final Expression expression;

    /**
     * A no-arg constructor does not wrap any expression on its own, but can be used as a place to contain a local
     * variable conversion in a place where an expression can otherwise stand.
     */
    public JoinPredecessorExpression() {
        this(null);
    }

    /**
     * A constructor for wrapping an expression and making it a join predecessor. Typically used on true and false
     * subexpressions of the ternary node as well as on the operands of short-circuiting logical expressions {@code &&}
     * and {@code ||}.
     * @param expression the expression to wrap
     */
    public JoinPredecessorExpression(final Expression expression) {
        super(expression == null ? 0L : expression.getToken(), expression == null ? 0 : expression.getStart(), expression == null ? 0 : expression.getFinish());
        this.expression = expression;
    }

    @Override
    public boolean isAlwaysFalse() {
        return expression != null && expression.isAlwaysFalse();
    }

    @Override
    public boolean isAlwaysTrue() {
        return expression != null && expression.isAlwaysTrue();
    }

    /**
     * Returns the underlying expression.
     * @return the underlying expression.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the underlying expression.
     * @param expression the new underlying expression
     * @return this or modified join predecessor expression object.
     */
    public JoinPredecessorExpression setExpression(final Expression expression) {
        if (expression == this.expression) {
            return this;
        }
        return new JoinPredecessorExpression(expression);
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterJoinPredecessorExpression(this)) {
            final Expression expr = getExpression();
            return visitor.leaveJoinPredecessorExpression(expr == null ? this : setExpression((Expression)expr.accept(visitor)));
        }
        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterJoinPredecessorExpression(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        if (expression != null) {
            expression.toString(sb, printType);
        }
    }

}
