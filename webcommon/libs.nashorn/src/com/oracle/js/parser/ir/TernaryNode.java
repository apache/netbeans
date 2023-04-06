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

import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * TernaryNode represent the ternary operator {@code ?:}. Note that for control-flow calculation reasons its branch
 * expressions (but not its test expression) are always wrapped in instances of {@link JoinPredecessorExpression}.
 */
public final class TernaryNode extends Expression {
    private final Expression test;
    private final JoinPredecessorExpression trueExpr;
    private final JoinPredecessorExpression falseExpr;

    /**
     * Constructor
     *
     * @param token     token
     * @param test      test expression
     * @param trueExpr  expression evaluated when test evaluates to true
     * @param falseExpr expression evaluated when test evaluates to true
     */
    public TernaryNode(final long token, final Expression test, final JoinPredecessorExpression trueExpr, final JoinPredecessorExpression falseExpr) {
        super(token, falseExpr.getFinish());
        this.test = test;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    private TernaryNode(final TernaryNode ternaryNode, final Expression test, final JoinPredecessorExpression trueExpr,
            final JoinPredecessorExpression falseExpr) {
        super(ternaryNode);
        this.test = test;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterTernaryNode(this)) {
            final Expression newTest = (Expression)getTest().accept(visitor);
            final JoinPredecessorExpression newTrueExpr = (JoinPredecessorExpression)trueExpr.accept(visitor);
            final JoinPredecessorExpression newFalseExpr = (JoinPredecessorExpression)falseExpr.accept(visitor);
            return visitor.leaveTernaryNode(setTest(newTest).setTrueExpression(newTrueExpr).setFalseExpression(newFalseExpr));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterTernaryNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        final TokenType tokenType  = tokenType();
        final boolean   testParen  = tokenType.needsParens(getTest().tokenType(), true);
        final boolean   trueParen  = tokenType.needsParens(getTrueExpression().tokenType(), false);
        final boolean   falseParen = tokenType.needsParens(getFalseExpression().tokenType(), false);

        if (testParen) {
            sb.append('(');
        }
        getTest().toString(sb, printType);
        if (testParen) {
            sb.append(')');
        }

        sb.append(" ? ");

        if (trueParen) {
            sb.append('(');
        }
        getTrueExpression().toString(sb, printType);
        if (trueParen) {
            sb.append(')');
        }

        sb.append(" : ");

        if (falseParen) {
            sb.append('(');
        }
        getFalseExpression().toString(sb, printType);
        if (falseParen) {
            sb.append(')');
        }
    }

    /**
     * Get the test expression for this ternary expression, i.e. "x" in x ? y : z
     * @return the test expression
     */
    public Expression getTest() {
        return test;
    }

    /**
     * Get the true expression for this ternary expression, i.e. "y" in x ? y : z
     * @return the true expression
     */
    public JoinPredecessorExpression getTrueExpression() {
        return trueExpr;
    }

    /**
     * Get the false expression for this ternary expression, i.e. "z" in x ? y : z
     * @return the false expression
     */
    public JoinPredecessorExpression getFalseExpression() {
        return falseExpr;
    }

    /**
     * Set the test expression for this node
     * @param test new test expression
     * @return a node equivalent to this one except for the requested change.
     */
    public TernaryNode setTest(final Expression test) {
        if (this.test == test) {
            return this;
        }
        return new TernaryNode(this, test, trueExpr, falseExpr);
    }

    /**
     * Set the true expression for this node
     * @param trueExpr new true expression
     * @return a node equivalent to this one except for the requested change.
     */
    public TernaryNode setTrueExpression(final JoinPredecessorExpression trueExpr) {
        if (this.trueExpr == trueExpr) {
            return this;
        }
        return new TernaryNode(this, test, trueExpr, falseExpr);
    }

    /**
     * Set the false expression for this node
     * @param falseExpr new false expression
     * @return a node equivalent to this one except for the requested change.
     */
    public TernaryNode setFalseExpression(final JoinPredecessorExpression falseExpr) {
        if (this.falseExpr == falseExpr) {
            return this;
        }
        return new TernaryNode(this, test, trueExpr, falseExpr);
    }
}
