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
import java.util.function.Function;

import com.oracle.js.parser.JSType;
import com.oracle.js.parser.Lexer.LexerToken;
import com.oracle.js.parser.Token;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

/**
 * Literal nodes represent JavaScript values.
 *
 * @param <T> the literal type
 */
public abstract class LiteralNode<T> extends Expression implements PropertyKey {
    /** Literal value */
    protected final T value;

    /**
     * Constructor
     *
     * @param token token
     * @param finish finish
     * @param value the value of the literal
     */
    protected LiteralNode(final long token, final int finish, final T value) {
        super(token, finish);
        this.value = value;
    }

    /**
     * Copy constructor
     *
     * @param literalNode source node
     */
    protected LiteralNode(final LiteralNode<T> literalNode) {
        this(literalNode, literalNode.value);
    }

    /**
     * A copy constructor with value change.
     *
     * @param literalNode the original literal node
     * @param newValue new value for this node
     */
    protected LiteralNode(final LiteralNode<T> literalNode, final T newValue) {
        super(literalNode);
        this.value = newValue;
    }

    /**
     * Initialization setter, if required for immutable state. This is used for things like
     * ArrayLiteralNodes that need to carry state for the splitter. Default implementation is just a
     * nop.
     *
     * @param lc lexical context
     * @return new literal node with initialized state, or same if nothing changed
     */
    public LiteralNode<?> initialize(final LexicalContext lc) {
        return this;
    }

    /**
     * Check if the literal value is null
     *
     * @return true if literal value is null
     */
    public boolean isNull() {
        return value == null;
    }

    @Override
    public String getPropertyName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Fetch boolean value of node.
     *
     * @return boolean value of node.
     */
    public boolean getBoolean() {
        return JSType.toBoolean(value);
    }

    /**
     * Fetch int32 value of node.
     *
     * @return Int32 value of node.
     */
    public int getInt32() {
        return JSType.toInt32(value);
    }

    /**
     * Fetch uint32 value of node.
     *
     * @return uint32 value of node.
     */
    public long getUint32() {
        return JSType.toUint32(value);
    }

    /**
     * Fetch long value of node
     *
     * @return long value of node
     */
    public long getLong() {
        return JSType.toLong(value);
    }

    /**
     * Fetch double value of node.
     *
     * @return double value of node.
     */
    public double getNumber() {
        return JSType.toNumber(value);
    }

    /**
     * Fetch String value of node.
     *
     * @return String value of node.
     */
    public String getString() {
        return String.valueOf(value);
    }

    /**
     * Fetch Object value of node.
     *
     * @return Object value of node.
     */
    public Object getObject() {
        return value;
    }

    /**
     * Test if the value is an array
     *
     * @return True if value is an array
     */
    public boolean isArray() {
        return false;
    }

    public List<Expression> getElementExpressions() {
        return null;
    }

    /**
     * Test if the value is a boolean.
     *
     * @return True if value is a boolean.
     */
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    /**
     * Test if the value is a string.
     *
     * @return True if value is a string.
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * Test if tha value is a number
     *
     * @return True if value is a number
     */
    public boolean isNumeric() {
        return value instanceof Number;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterLiteralNode(this)) {
            return visitor.leaveLiteralNode(this);
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterLiteralNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value.toString());
        }
    }

    /**
     * Get the literal node value
     *
     * @return the value
     */
    public final T getValue() {
        return value;
    }

    private static Expression[] valueToArray(final List<Expression> value) {
        return value.toArray(new Expression[0]);
    }

    /**
     * Create a new null literal
     *
     * @param token token
     * @param finish finish
     *
     * @return the new literal node
     */
    public static LiteralNode<Object> newInstance(final long token, final int finish) {
        return new NullLiteralNode(token, finish);
    }

    /**
     * Super class for primitive (side-effect free) literals.
     *
     * @param <T> the literal type
     */
    public static class PrimitiveLiteralNode<T> extends LiteralNode<T> {
        private PrimitiveLiteralNode(final long token, final int finish, final T value) {
            super(token, finish, value);
        }

        private PrimitiveLiteralNode(final PrimitiveLiteralNode<T> literalNode) {
            super(literalNode);
        }

        /**
         * Check if the literal value is boolean true
         *
         * @return true if literal value is boolean true
         */
        public boolean isTrue() {
            return JSType.toBoolean(value);
        }

        @Override
        public boolean isAlwaysFalse() {
            return !isTrue();
        }

        @Override
        public boolean isAlwaysTrue() {
            return isTrue();
        }

        @Override
        public String getPropertyName() {
            return String.valueOf(getObject());
        }
    }

    private static final class BooleanLiteralNode extends PrimitiveLiteralNode<Boolean> {

        private BooleanLiteralNode(final long token, final int finish, final boolean value) {
            super(Token.recast(token, value ? TokenType.TRUE : TokenType.FALSE), finish, value);
        }

        private BooleanLiteralNode(final BooleanLiteralNode literalNode) {
            super(literalNode);
        }

        @Override
        public boolean isTrue() {
            return value;
        }
    }

    /**
     * Create a new boolean literal
     *
     * @param token token
     * @param finish finish
     * @param value true or false
     *
     * @return the new literal node
     */
    public static LiteralNode<Boolean> newInstance(final long token, final int finish, final boolean value) {
        return new BooleanLiteralNode(token, finish, value);
    }

    private static final class NumberLiteralNode extends PrimitiveLiteralNode<Number> {
        private final Function<Number, String> toStringConverter;

        private NumberLiteralNode(final long token, final int finish, final Number value, final Function<Number, String> toStringConverter) {
            super(Token.recast(token, TokenType.DECIMAL), finish, value);
            this.toStringConverter = toStringConverter;
        }

        private NumberLiteralNode(final NumberLiteralNode literalNode) {
            super(literalNode);
            this.toStringConverter = literalNode.toStringConverter;
        }

        @Override
        public String getPropertyName() {
            return toStringConverter == null ? super.getPropertyName() : toStringConverter.apply(getValue());
        }
    }

    /**
     * Create a new number literal
     *
     * @param token token
     * @param finish finish
     * @param value literal value
     *
     * @return the new literal node
     */
    public static LiteralNode<Number> newInstance(final long token, final int finish, final Number value) {
        return new NumberLiteralNode(token, finish, value, null);
    }

    /**
     * Create a new number literal
     *
     * @param token token
     * @param finish finish
     * @param value literal value
     *
     * @return the new literal node
     */
    public static LiteralNode<Number> newInstance(final long token, final int finish, final Number value, final Function<Number, String> toStringConverter) {
        return new NumberLiteralNode(token, finish, value, toStringConverter);
    }

    private static final class StringLiteralNode extends PrimitiveLiteralNode<String> {
        private StringLiteralNode(final long token, final int finish, final String value) {
            super(Token.recast(token, TokenType.STRING), finish, value);
        }

        private StringLiteralNode(final StringLiteralNode literalNode) {
            super(literalNode);
        }

        @Override
        public void toString(final StringBuilder sb, final boolean printType) {
            sb.append('\"');
            sb.append(value);
            sb.append('\"');
        }
    }

    /**
     * Create a new string literal
     *
     * @param token token
     * @param finish finish
     * @param value string value
     *
     * @return the new literal node
     */
    public static LiteralNode<String> newInstance(final long token, final int finish, final String value) {
        return new StringLiteralNode(token, finish, value);
    }

    private static final class LexerTokenLiteralNode extends LiteralNode<LexerToken> {
        private LexerTokenLiteralNode(final long token, final int finish, final LexerToken value) {
            super(Token.recast(token, TokenType.STRING), finish, value);
            // TODO is string the correct token type here?
        }

        private LexerTokenLiteralNode(final LexerTokenLiteralNode literalNode) {
            super(literalNode);
        }

        @Override
        public void toString(final StringBuilder sb, final boolean printType) {
            sb.append(value.toString());
        }
    }

    /**
     * Create a new literal node for a lexer token
     *
     * @param token token
     * @param finish finish
     * @param value lexer token value
     *
     * @return the new literal node
     */
    public static LiteralNode<LexerToken> newInstance(final long token, final int finish, final LexerToken value) {
        return new LexerTokenLiteralNode(token, finish, value);
    }

    private static final class NullLiteralNode extends PrimitiveLiteralNode<Object> {

        private NullLiteralNode(final long token, final int finish) {
            super(Token.recast(token, TokenType.OBJECT), finish, null);
        }
    }

    /**
     * Array literal node class.
     */
    public static final class ArrayLiteralNode extends LiteralNode<Expression[]> implements LexicalContextNode {
        /**
         * Sub units with indexes ranges, in which to split up code generation, for large literals
         */
        private final List<ArrayUnit> units;

        private final boolean hasSpread;
        private final boolean hasTrailingComma;

        /**
         * An ArrayUnit is a range in an ArrayLiteral. ArrayLiterals can be split if they are too
         * large, for bytecode generation reasons
         */
        public static final class ArrayUnit {
            /** postsets range associated with the unit (hi not inclusive). */
            private final int lo;
            private final int hi;

            /**
             * Constructor
             *
             * @param lo lowest array index in unit
             * @param hi highest array index in unit + 1
             */
            public ArrayUnit(final int lo, final int hi) {
                this.lo = lo;
                this.hi = hi;
            }

            /**
             * Get the high index position of the ArrayUnit (non inclusive)
             *
             * @return high index position
             */
            public int getHi() {
                return hi;
            }

            /**
             * Get the low index position of the ArrayUnit (inclusive)
             *
             * @return low index position
             */
            public int getLo() {
                return lo;
            }
        }

        private static final class ArrayLiteralInitializer {

            static ArrayLiteralNode initialize(final ArrayLiteralNode node) {
                return new ArrayLiteralNode(node, node.value, node.units);
            }
        }

        /**
         * Constructor
         *
         * @param token token
         * @param finish finish
         * @param value array literal value, a Node array
         */
        protected ArrayLiteralNode(final long token, final int finish, final Expression[] value) {
            this(token, finish, value, false, false);
        }

        /**
         * Constructor
         *
         * @param token token
         * @param finish finish
         * @param value array literal value, a Node array
         * @param hasSpread true if the array has a spread element
         * @param hasTrailingComma true if the array literal has a comma after the last element
         */
        protected ArrayLiteralNode(final long token, final int finish, final Expression[] value, boolean hasSpread, boolean hasTrailingComma) {
            super(Token.recast(token, TokenType.ARRAY), finish, value);
            this.units = null;
            this.hasSpread = hasSpread;
            this.hasTrailingComma = hasTrailingComma;
        }

        /**
         * Copy constructor
         *
         * @param node source array literal node
         */
        private ArrayLiteralNode(final ArrayLiteralNode node, final Expression[] value, final List<ArrayUnit> units) {
            super(node, value);
            this.units = units;
            this.hasSpread = node.hasSpread;
            this.hasTrailingComma = node.hasTrailingComma;
        }

        @Override
        public boolean isArray() {
            return true;
        }

        public boolean hasSpread() {
            return hasSpread;
        }

        public boolean hasTrailingComma() {
            return hasTrailingComma;
        }

        /**
         * Returns a list of array element expressions. Note that empty array elements manifest
         * themselves as null.
         *
         * @return a list of array element expressions.
         */
        @Override
        public List<Expression> getElementExpressions() {
            return Collections.unmodifiableList(Arrays.asList(value));
        }

        /**
         * Setter that initializes all code generation meta data for an ArrayLiteralNode. This acts
         * a setter, so the return value may return a new node and must be handled
         *
         * @param lc lexical context
         * @return new array literal node with postsets, presets and element types initialized
         */
        @Override
        public ArrayLiteralNode initialize(final LexicalContext lc) {
            return Node.replaceInLexicalContext(lc, this, ArrayLiteralInitializer.initialize(this));
        }

        /**
         * Get the array units that make up this ArrayLiteral
         *
         * @see ArrayUnit
         * @return list of array units
         */
        public List<ArrayUnit> getUnits() {
            return units == null ? null : Collections.unmodifiableList(units);
        }

        @Override
        public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
            return Acceptor.accept(this, visitor);
        }

        @Override
        public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
            return Acceptor.accept(this, visitor);
        }

        @Override
        public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
            if (visitor.enterLiteralNode(this)) {
                final List<Expression> oldValue = Arrays.asList(value);
                final List<Expression> newValue = Node.accept(visitor, oldValue);
                return visitor.leaveLiteralNode(oldValue != newValue ? setValue(lc, newValue) : this);
            }
            return this;
        }

        @Override
        public <R> R accept(LexicalContext lc, TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
            return visitor.enterLiteralNode(this);
        }

        private ArrayLiteralNode setValue(final LexicalContext lc, final Expression[] value) {
            if (this.value == value) {
                return this;
            }
            return Node.replaceInLexicalContext(lc, this, new ArrayLiteralNode(this, value, units));
        }

        private ArrayLiteralNode setValue(final LexicalContext lc, final List<Expression> value) {
            return setValue(lc, value.toArray(new Expression[0]));
        }

        @Override
        public void toString(final StringBuilder sb, final boolean printType) {
            sb.append('[');
            boolean first = true;
            for (final Node node : value) {
                if (!first) {
                    sb.append(',');
                    sb.append(' ');
                }
                if (node == null) {
                    sb.append("undefined");
                } else {
                    node.toString(sb, printType);
                }
                first = false;
            }
            sb.append(']');
        }
    }

    /**
     * Create a new array literal of Nodes from a list of Node values
     *
     * @param token token
     * @param finish finish
     * @param value literal value list
     *
     * @return the new literal node
     */
    public static LiteralNode<Expression[]> newInstance(final long token, final int finish, final List<Expression> value) {
        return newInstance(token, finish, valueToArray(value));
    }

    /**
     * Create a new array literal of Nodes from a list of Node values
     *
     * @param token token
     * @param finish finish
     * @param value literal value list
     * @param hasSpread true if the array has a spread element
     * @param hasTrailingComma true if the array literal has a comma after the last element
     *
     * @return the new literal node
     */
    public static LiteralNode<Expression[]> newInstance(final long token, final int finish, final List<Expression> value, boolean hasSpread, boolean hasTrailingComma) {
        return new ArrayLiteralNode(token, finish, valueToArray(value), hasSpread, hasTrailingComma);
    }

    /**
     * Create a new array literal of Nodes
     *
     * @param token token
     * @param finish finish
     * @param value literal value array
     *
     * @return the new literal node
     */
    public static LiteralNode<Expression[]> newInstance(final long token, final int finish, final Expression[] value) {
        return new ArrayLiteralNode(token, finish, value);
    }
}
