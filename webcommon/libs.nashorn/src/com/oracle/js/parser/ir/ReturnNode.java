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

/**
 * IR representation for {@code return} statements.
 */
public class ReturnNode extends Statement {
    /** Optional expression. */
    private final Expression expression;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token token
     * @param finish finish
     * @param expression expression to return
     */
    public ReturnNode(final int lineNumber, final long token, final int finish, final Expression expression) {
        super(lineNumber, token, finish);
        this.expression = expression;
    }

    private ReturnNode(final ReturnNode returnNode, final Expression expression) {
        super(returnNode);
        this.expression = expression;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    /**
     * Check if this return node has an expression
     *
     * @return true if not a void return
     */
    public boolean hasExpression() {
        return expression != null;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterReturnNode(this)) {
            if (expression != null) {
                return visitor.leaveReturnNode(setExpression((Expression) expression.accept(visitor)));
            }
            return visitor.leaveReturnNode(this);
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterReturnNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append("return");
        if (expression != null) {
            sb.append(' ');
            expression.toString(sb, printType);
        }
    }

    /**
     * Get the expression this node returns
     *
     * @return return expression, or null if void return
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Reset the expression this node returns
     *
     * @param expression new expression, or null if void return
     * @return new or same return node
     */
    public ReturnNode setExpression(final Expression expression) {
        if (this.expression == expression) {
            return this;
        }
        return new ReturnNode(this, expression);
    }

}
