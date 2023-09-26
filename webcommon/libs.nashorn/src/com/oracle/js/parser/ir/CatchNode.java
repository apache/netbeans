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
 * IR representation of a catch clause.
 */
public final class CatchNode extends Statement {
    /** Exception identifier. */
    private final IdentNode exception;

    /** Exception condition. */
    private final Expression exceptionCondition;

    /** Catch body. */
    private final Block body;

    private final boolean isSyntheticRethrow;

    /**
     * Constructors
     *
     * @param lineNumber         lineNumber
     * @param token              token
     * @param finish             finish
     * @param exception          variable name of exception
     * @param exceptionCondition exception condition
     * @param body               catch body
     * @param isSyntheticRethrow true if this node is a synthetically generated rethrow node.
     */
    public CatchNode(final int lineNumber, final long token, final int finish, final IdentNode exception,
            final Expression exceptionCondition, final Block body, final boolean isSyntheticRethrow) {
        super(lineNumber, token, finish);
        this.exception          = exception == null ? null : exception.setIsInitializedHere();
        this.exceptionCondition = exceptionCondition;
        this.body               = body;
        this.isSyntheticRethrow = isSyntheticRethrow;
    }

    private CatchNode(final CatchNode catchNode, final IdentNode exception, final Expression exceptionCondition,
            final Block body, final boolean isSyntheticRethrow) {
        super(catchNode);
        this.exception          = exception;
        this.exceptionCondition = exceptionCondition;
        this.body               = body;
        this.isSyntheticRethrow = isSyntheticRethrow;
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterCatchNode(this)) {
            return visitor.leaveCatchNode(
                setException(exception == null ? null : (IdentNode)exception.accept(visitor)).
                setExceptionCondition(exceptionCondition == null ? null : (Expression)exceptionCondition.accept(visitor)).
                setBody((Block)body.accept(visitor)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterCatchNode(this);
    }

    @Override
    public boolean isTerminal() {
        return body.isTerminal();
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printTypes) {
        sb.append(" catch (");
        exception.toString(sb, printTypes);

        if (exceptionCondition != null) {
            sb.append(" if ");
            exceptionCondition.toString(sb, printTypes);
        }
        sb.append(')');
    }

    /**
     * Get the identifier representing the exception thrown
     * @return the exception identifier
     */
    public IdentNode getException() {
        return exception;
    }

    /**
     * Get the exception condition for this catch block
     * @return the exception condition
     */
    public Expression getExceptionCondition() {
        return exceptionCondition;
    }

    /**
     * Reset the exception condition for this catch block
     * @param exceptionCondition the new exception condition
     * @return new or same CatchNode
     */
    public CatchNode setExceptionCondition(final Expression exceptionCondition) {
        if (this.exceptionCondition == exceptionCondition) {
            return this;
        }
        return new CatchNode(this, exception, exceptionCondition, body, isSyntheticRethrow);
    }

    /**
     * Get the body for this catch block
     * @return the catch block body
     */
    public Block getBody() {
        return body;
    }

    /**
     * Resets the exception of a catch block
     * @param exception new exception
     * @return new catch node if changed, same otherwise
     */
    public CatchNode setException(final IdentNode exception) {
        if (this.exception == exception) {
            return this;
        }
        return new CatchNode(this, exception, exceptionCondition, body, isSyntheticRethrow);
    }

    private CatchNode setBody(final Block body) {
        if (this.body == body) {
            return this;
        }
        return new CatchNode(this, exception, exceptionCondition, body, isSyntheticRethrow);
    }

    /**
     * Is this catch block a non-JavaScript constructor, for example created as
     * part of the rethrow mechanism of a finally block in Lower? Then we just
     * pass the exception on and need not unwrap whatever is in the ECMAException
     * object catch symbol
     * @return true if a finally synthetic rethrow
     */
    public boolean isSyntheticRethrow() {
        return isSyntheticRethrow;
    }
}
