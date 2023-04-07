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

import java.util.Collections;
import java.util.List;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * IR representation for a function call.
 */
public final class CallNode extends LexicalContextExpression {

    /** Function identifier or function body. */
    private final Expression function;

    /** Call arguments. */
    private final List<Expression> args;

    /** Is this a "new" operation */
    private static final int IS_NEW = 1 << 0;

    /** Can this be an eval? */
    private static final int IS_EVAL = 1 << 1;

    private final int flags;

    private final int lineNumber;

    private final boolean optional;

    /**
     * Constructors
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param function   the function to call
     * @param args       args to the call
     * @param isNew      true if this is a constructor call with the "new" keyword
     */
    public CallNode(final int lineNumber, final long token, final int finish, final Expression function, final List<Expression> args, final boolean isNew) {
        this(lineNumber, token, finish, function, args, isNew, false);
    }

    /**
     * Constructors
     *
     * @param lineNumber line number
     * @param token token
     * @param finish finish
     * @param function the function to call
     * @param args args to the call
     * @param isNew true if this is a constructor call with the "new" keyword
     * @param optional true if this call is optional (i.e. if the function
     * expression is {@code undefined} the result will be {@code undefined} and
     * not raise an error)
     */
    public CallNode(final int lineNumber, final long token, final int finish, final Expression function, final List<Expression> args, final boolean isNew, final boolean optional) {
        super(token, finish);

        this.function       = function;
        this.args           = args;
        this.flags          = isNew ? IS_NEW : 0;
        this.lineNumber     = lineNumber;
        this.optional       = optional;
    }

    private CallNode(final CallNode callNode, final Expression function, final List<Expression> args, final int flags, boolean optional) {
        super(callNode);
        this.lineNumber = callNode.lineNumber;
        this.function = function;
        this.args = args;
        this.flags = flags;
        this.optional = optional;
    }

    /**
     * Returns the line number.
     * @return the line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     *
     * @return node or replacement
     */
    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterCallNode(this)) {
            final CallNode newCallNode = (CallNode)visitor.leaveCallNode(
                    setFunction((Expression)function.accept(visitor)).
                    setArgs(Node.accept(visitor, args)));
            // Theoretically, we'd need to instead pass lc to every setter and do a replacement on each. In practice,
            // setType from TypeOverride can't accept a lc, and we don't necessarily want to go there now.
            if (this != newCallNode) {
                return Node.replaceInLexicalContext(lc, this, newCallNode);
            }
        }

        return this;
    }

    @Override
    public <R> R accept(LexicalContext lc, TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterCallNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        final StringBuilder fsb = new StringBuilder();
        function.toString(fsb, printType);
        sb.append(fsb);

        if (optional) {
            sb.append("?.");
        }

        sb.append('(');

        boolean first = true;

        for (final Node arg : args) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }

            arg.toString(sb, printType);
        }

        sb.append(')');
    }

    /**
     * Get the arguments for the call
     * @return a list of arguments
     */
    public List<Expression> getArgs() {
        return Collections.unmodifiableList(args);
    }

    /**
     * Reset the arguments for the call
     * @param args new arguments list
     * @return new callnode, or same if unchanged
     */
    public CallNode setArgs(final List<Expression> args) {
        if (this.args == args) {
            return this;
        }
        return new CallNode(this, function, args, flags, optional);
    }

    /**
     * Check if this call is a call to {@code eval}
     * @return true if this is a call to {@code eval}
     */
    public boolean isEval() {
        return (flags & IS_EVAL) != 0;
    }

    public CallNode setIsEval() {
        return setFlags(flags | IS_EVAL);
    }

    /**
     * Return the function expression that this call invokes
     * @return the function
     */
    public Expression getFunction() {
        return function;
    }

    /**
     * Reset the function expression that this call invokes
     * @param function the function
     * @return same node or new one on state change
     */
    public CallNode setFunction(final Expression function) {
        if (this.function == function) {
            return this;
        }
        return new CallNode(this, function, args, flags, optional);
    }

    /**
     * Check if this call is a new operation
     * @return true if this a new operation
     */
    public boolean isNew() {
        return (flags & IS_NEW) != 0;
    }

    private CallNode setFlags(final int flags) {
        if (this.flags == flags) {
            return this;
        }
        return new CallNode(this, function, args, flags, optional);
    }

    public boolean isOptional() {
        return optional;
    }

    public CallNode setOptional() {
        if (optional) {
            return this;
        }
        return new CallNode(this, function, args, flags, true);
    }
}
