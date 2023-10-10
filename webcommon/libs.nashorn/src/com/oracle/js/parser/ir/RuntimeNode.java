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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

/**
 * IR representation for a runtime call.
 */
public class RuntimeNode extends Expression {

    /**
     * Request enum used for meta-information about the runtime request
     */
    public enum Request {
        /** ReferenceError type */
        REFERENCE_ERROR,
        /** ToString conversion */
        TO_STRING(TokenType.VOID, Object.class, 1),
        /** Get template object from raw and cooked string arrays. */
        GET_TEMPLATE_OBJECT(TokenType.TEMPLATE, Object.class, 2);

        /** token type */
        private final TokenType tokenType;

        /** return type for request */
        private final Class<?> returnType;

        /** arity of request */
        private final int arity;

        Request() {
            this(TokenType.VOID, Object.class, 0);
        }

        Request(final TokenType tokenType, final Class<?> returnType, final int arity) {
            this.tokenType = tokenType;
            this.returnType = returnType;
            this.arity = arity;
        }

        /**
         * Get arity
         *
         * @return the arity of the request
         */
        public int getArity() {
            return arity;
        }

        /**
         * Get the return type
         *
         * @return return type for request
         */
        public Class<?> getReturnType() {
            return returnType;
        }

        /**
         * Get token type
         *
         * @return token type for request
         */
        public TokenType getTokenType() {
            return tokenType;
        }
    }

    /** Runtime request. */
    private final Request request;

    /** Call arguments. */
    private final List<Expression> args;

    /**
     * Constructor
     *
     * @param token token
     * @param finish finish
     * @param request the request
     * @param args arguments to request
     */
    public RuntimeNode(final long token, final int finish, final Request request, final List<Expression> args) {
        super(token, finish);

        this.request = request;
        this.args = args;
    }

    private RuntimeNode(final RuntimeNode runtimeNode, final Request request, final List<Expression> args) {
        super(runtimeNode);

        this.request = request;
        this.args = args;
    }

    /**
     * Constructor
     *
     * @param token token
     * @param finish finish
     * @param request the request
     * @param args arguments to request
     */
    public RuntimeNode(final long token, final int finish, final Request request, final Expression... args) {
        this(token, finish, request, Arrays.asList(args));
    }

    /**
     * Constructor
     *
     * @param parent parent node from which to inherit source, token, finish
     * @param request the request
     * @param args arguments to request
     */
    public RuntimeNode(final Expression parent, final Request request, final List<Expression> args) {
        super(parent);

        this.request = request;
        this.args = args;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterRuntimeNode(this)) {
            return visitor.leaveRuntimeNode(setArgs(Node.accept(visitor, args)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterRuntimeNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append("Runtime.");
        sb.append(request);
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
     * Get the arguments for this runtime node
     *
     * @return argument list
     */
    public List<Expression> getArgs() {
        return Collections.unmodifiableList(args);
    }

    /**
     * Set the arguments of this runtime node
     *
     * @param args new arguments
     * @return new runtime node, or identical if no change
     */
    public RuntimeNode setArgs(final List<Expression> args) {
        if (this.args == args) {
            return this;
        }
        return new RuntimeNode(this, request, args);
    }

    /**
     * Get the request that this runtime node implements
     *
     * @return the request
     */
    public Request getRequest() {
        return request;
    }
}
