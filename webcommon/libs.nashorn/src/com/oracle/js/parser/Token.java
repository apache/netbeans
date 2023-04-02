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

package com.oracle.js.parser;

import static com.oracle.js.parser.TokenKind.LITERAL;

// @formatter:off
/**
 * A token is a 64 bit long value that represents a basic parse/lex unit.
 * This class provides static methods to manipulate lexer tokens.
 */
public final class Token {

    /**
     * We use 28 bits for the position and 28 bits for the length of the token.
     * This limits the maximal length of code we can handle to 2 ^ 28 - 1 bytes.
     */
    public static final int LENGTH_MASK = 0xfffffff;

    // The first 8 bits are used for the token type, followed by length and position
    private static final int LENGTH_SHIFT = 8;
    private static final int POSITION_SHIFT  = 36;

    private Token() {
    }

    /**
     * Create a compact form of token information.
     * @param type     Type of token.
     * @param position Start position of the token in the source.
     * @param length   Length of the token.
     * @return Token descriptor.
     */
    public static long toDesc(final TokenType type, final int position, final int length) {
        assert position <= LENGTH_MASK && length <= LENGTH_MASK;
        return (long)position << POSITION_SHIFT |
               (long)length   << LENGTH_SHIFT  |
               type.ordinal();
    }

    /**
     * Extract token position from a token descriptor.
     * @param token Token descriptor.
     * @return Start position of the token in the source.
     */
    public static int descPosition(final long token) {
        return (int)(token >>> POSITION_SHIFT);
    }

    /**
     * Normally returns the token itself, except in case of string tokens
     * which report their position past their opening delimiter and thus
     * need to have position and length adjusted.
     *
     * @param token Token descriptor.
     * @return same or adjusted token.
     */
    public static long withDelimiter(final long token) {
        final TokenType tokenType = Token.descType(token);
        switch(tokenType) {
            case STRING:
            case ESCSTRING:
            case EXECSTRING:
            case TEMPLATE:
            case TEMPLATE_TAIL: {
                final int start = Token.descPosition(token) - 1;
                final int len = Token.descLength(token) + 2;
                return toDesc(tokenType, start, len);
            }
            case TEMPLATE_HEAD:
            case TEMPLATE_MIDDLE: {
                final int start = Token.descPosition(token) - 1;
                final int len = Token.descLength(token) + 3;
                return toDesc(tokenType, start, len);
            }
            default: {
                return token;
            }
        }
    }

    /**
     * Extract token length from a token descriptor.
     * @param token Token descriptor.
     * @return Length of the token.
     */
    public static int descLength(final long token) {
        return (int)((token >>> LENGTH_SHIFT) & LENGTH_MASK);
    }

    /**
     * Extract token type from a token descriptor.
     * @param token Token descriptor.
     * @return Type of token.
     */
    public static TokenType descType(final long token) {
        return TokenType.getValues()[(int)token & 0xff];
    }

    /**
     * Change the token to use a new type.
     *
     * @param token   The original token.
     * @param newType The new token type.
     * @return The recast token.
     */
    public static long recast(final long token, final TokenType newType) {
        return token & ~0xFFL | newType.ordinal();
    }

    /**
     * Return a string representation of a token.
     * @param source  Token source.
     * @param token   Token descriptor.
     * @param verbose True to include details.
     * @return String representation.
     */
    public static String toString(final Source source, final long token, final boolean verbose) {
        final TokenType type = Token.descType(token);
        String result;

        if (source != null && type.getKind() == LITERAL) {
            result = source.getString(token);
        } else {
            result = type.getNameOrType();
        }

        if (verbose) {
            final int position = Token.descPosition(token);
            final int length = Token.descLength(token);
            result += " (" + position + ", " + length + ")";
        }

        return result;
    }

    /**
     * String conversion of token
     *
     * @param source the source
     * @param token  the token
     *
     * @return token as string
     */
    public static String toString(final Source source, final long token) {
        return Token.toString(source, token, false);
    }

    /**
     * String conversion of token - version without source given
     *
     * @param token  the token
     *
     * @return token as string
     */
    public static String toString(final long token) {
        return Token.toString(null, token, false);
    }
}
