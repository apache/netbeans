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
 * IR representation of an indexed access (brackets operator.)
 */
public final class IndexNode extends BaseNode {
    /** Property index. */
    private final Expression index;
    private final boolean optional;

    /**
     * Constructors
     *
     * @param token token
     * @param finish finish
     * @param base base node for access
     * @param index index for access
     */
    public IndexNode(final long token, final int finish, final Expression base, final Expression index) {
        this(token, finish, base, index, false);
    }

    /**
     * Constructors
     *
     * @param token   token
     * @param finish  finish
     * @param base    base node for access
     * @param index   index for access
     * @param optional true if this index operation is optional (i.e. if the
     * base expression is {@code undefined} the result will be
     * {@code undefined} and not raise an error)
     */
    public IndexNode(final long token, final int finish, final Expression base, final Expression index, final boolean optional) {
        super(token, finish, base, false, false);
        this.index = index;
        this.optional = optional;
    }

    private IndexNode(final IndexNode indexNode, final Expression base, final Expression index, final boolean isFunction, final boolean isSuper, final boolean optional) {
        super(indexNode, base, isFunction, isSuper);
        this.index = index;
        this.optional = optional;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterIndexNode(this)) {
            return visitor.leaveIndexNode(
                setBase((Expression)base.accept(visitor)).
                setIndex((Expression)index.accept(visitor)));
        }
        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterIndexNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        final boolean needsParen = tokenType().needsParens(base.tokenType(), true);

        if (needsParen) {
            sb.append('(');
        }

        base.toString(sb, printType);

        if (needsParen) {
            sb.append(')');
        }

        if (optional) {
            sb.append("?.");
        }

        sb.append('[');
        index.toString(sb, printType);
        sb.append(']');
    }

    /**
     * Get the index expression for this IndexNode
     * @return the index
     */
    public Expression getIndex() {
        return index;
    }

    private IndexNode setBase(final Expression base) {
        if (this.base == base) {
            return this;
        }
        return new IndexNode(this, base, index, isFunction(), isSuper(), optional);
    }

    /**
     * Set the index expression for this node
     * @param index new index expression
     * @return a node equivalent to this one except for the requested change.
     */
    public IndexNode setIndex(final Expression index) {
        if (this.index == index) {
            return this;
        }
        return new IndexNode(this, base, index, isFunction(), isSuper(), optional);
    }

    @Override
    public IndexNode setIsFunction() {
        if (isFunction()) {
            return this;
        }
        return new IndexNode(this, base, index, true, isSuper(), optional);
    }

    @Override
    public IndexNode setIsSuper() {
        if (isSuper()) {
            return this;
        }
        return new IndexNode(this, base, index, isFunction(), true, optional);
    }

    public boolean isOptional() {
        return optional;
    }

    public IndexNode setOptional() {
        if (optional) {
            return this;
        }
        return new IndexNode(this, base, index, isFunction(), isSuper(), true);
    }
}
