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
 * IR representation for an identifier.
 */
public final class IdentNode extends Expression implements PropertyKey, FunctionCall {
    private static final int PROPERTY_NAME     = 1 << 0;
    private static final int INITIALIZED_HERE  = 1 << 1;
    private static final int FUNCTION          = 1 << 2;
    private static final int FUTURESTRICT_NAME = 1 << 3;
    private static final int IS_DECLARED_HERE  = 1 << 4;
    private static final int IS_DEAD           = 1 << 5;
    private static final int DIRECT_SUPER      = 1 << 6;
    private static final int REST_PARAMETER    = 1 << 7;
    private static final int PROTO_PROPERTY    = 1 << 8;
    private static final int DEFAULT_PARAMETER = 1 << 9;
    private static final int DESTRUCTURED_PARAMETER = 1 << 10;

    /** Identifier. */
    private final String name;

    private final int flags;

    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish position
     * @param name    name of identifier
     */
    public IdentNode(final long token, final int finish, final String name) {
        super(token, finish);
        this.name = name;
        this.flags = 0;
    }

    private IdentNode(final IdentNode identNode, final String name, final int flags) {
        super(identNode);
        this.name = name;
        this.flags = flags;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterIdentNode(this)) {
            return visitor.leaveIdentNode(this);
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterIdentNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append(name);
    }

    /**
     * Get the name of the identifier
     * @return  IdentNode name
     */
    public String getName() {
        return name;
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    /**
     * Check if this IdentNode is a property name
     * @return true if this is a property name
     */
    public boolean isPropertyName() {
        return (flags & PROPERTY_NAME) == PROPERTY_NAME;
    }

    /**
     * Flag this IdentNode as a property name
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsPropertyName() {
        if (isPropertyName()) {
            return this;
        }
        return new IdentNode(this, name, flags | PROPERTY_NAME);
    }

    /**
     * Check if this IdentNode is a future strict name
     * @return true if this is a future strict name
     */
    public boolean isFutureStrictName() {
        return (flags & FUTURESTRICT_NAME) == FUTURESTRICT_NAME;
    }

    /**
     * Flag this IdentNode as a future strict name
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsFutureStrictName() {
        if (isFutureStrictName()) {
            return this;
        }
        return new IdentNode(this, name, flags | FUTURESTRICT_NAME);
    }

    /**
     * Helper function for local def analysis.
     * @return true if IdentNode is initialized on creation
     */
    public boolean isInitializedHere() {
        return (flags & INITIALIZED_HERE) == INITIALIZED_HERE;
    }

    /**
     * Flag IdentNode to be initialized on creation
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsInitializedHere() {
        if (isInitializedHere()) {
            return this;
        }
        return new IdentNode(this, name, flags | INITIALIZED_HERE);
    }

    /**
     * Is this a LET or CONST identifier used before its declaration?
     *
     * @return true if identifier is dead
     */
    public boolean isDead() {
        return (flags & IS_DEAD) != 0;
    }

    /**
     * Is this IdentNode declared here?
     *
     * @return true if identifier is declared here
     */
    public boolean isDeclaredHere() {
        return (flags & IS_DECLARED_HERE) != 0;
    }

    /**
     * Flag this IdentNode as being declared here.
     *
     * @return a new IdentNode equivalent to this but marked as declared here.
     */
    public IdentNode setIsDeclaredHere() {
        if (isDeclaredHere()) {
            return this;
        }
        return new IdentNode(this, name, flags | IS_DECLARED_HERE);
    }

    @Override
    public boolean isFunction() {
        return (flags & FUNCTION) == FUNCTION;
    }

    /**
     * Is this an internal symbol, i.e. one that starts with ':'. Those can
     * never be optimistic.
     * @return true if internal symbol
     */
    public boolean isInternal() {
        assert name != null;
        return name.charAt(0) == ':';
    }

    public boolean isDirectSuper() {
        return (flags & DIRECT_SUPER) != 0;
    }

    public IdentNode setIsDirectSuper() {
        return new IdentNode(this, name, flags | DIRECT_SUPER);
    }

    public boolean isRestParameter() {
        return (flags & REST_PARAMETER) != 0;
    }

    public IdentNode setIsRestParameter() {
        return new IdentNode(this, name, flags | REST_PARAMETER);
    }

    public boolean isProtoPropertyName() {
        return (flags & PROTO_PROPERTY) != 0;
    }

    public IdentNode setIsProtoPropertyName() {
        return new IdentNode(this, name, flags | PROTO_PROPERTY);
    }

    public boolean isDefaultParameter() {
        return (flags & DEFAULT_PARAMETER) != 0;
    }

    public IdentNode setIsDefaultParameter() {
        return new IdentNode(this, name, flags | DEFAULT_PARAMETER);
    }

    public boolean isDestructuredParameter() {
        return (flags & DESTRUCTURED_PARAMETER) != 0;
    }

    public IdentNode setIsDestructuredParameter() {
        return new IdentNode(this, name, flags | DESTRUCTURED_PARAMETER);
    }

    public boolean isPrivate() {
        return name != null && name.startsWith("#");
    }
}
