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

import static com.oracle.js.parser.TokenKind.SPECIAL;
import static com.oracle.js.parser.TokenType.IDENT;

// @formatter:off
/**
 * Fast lookup of operators and keywords.
 *
 */
public final class TokenLookup {
    /**
     * Lookup table for tokens.
     */
    private static final TokenType[] table;

    /**
     * Table base character.
     */
    private static final int tableBase = ' ';

    /**
     * Table base character.
     */
    private static final int tableLimit = '~';

    /**
     * Table size.
     */
    private static final int tableLength = tableLimit - tableBase + 1;

    static {
        // Construct the table.
        table = new TokenType[tableLength];

        // For each token type.
        for (final TokenType tokenType : TokenType.getValues()) {
            // Get the name.
            final String name = tokenType.getName();

            // Filter tokens.
            if (name == null || tokenType.getKind() == TokenKind.JSX) {
                continue;
            }

            // Ignore null and special.
            if (tokenType.getKind() != SPECIAL) {
                // Get the first character of the name.
                final char first = name.charAt(0);
                // Translate that character into a table index.
                final int index = first - tableBase;
                assert index < tableLength : "Token name does not fit lookup table";

                // Get the length of the token so that the longest come first.
                final int length = tokenType.getLength();
                // Prepare for table insert.
                TokenType prev = null;
                TokenType next = table[index];

                // Find the right spot in the table.
                while (next != null && next.getLength() > length) {
                    prev = next;
                    next = next.getNext();
                }

                // Insert in table.
                tokenType.setNext(next);

                if (prev == null) {
                    table[index] = tokenType;
                } else {
                    prev.setNext(tokenType);
                }
            }
        }
    }

    private TokenLookup() {
    }

    /**
     * Lookup keyword.
     *
     * @param content parse content char array
     * @param position index of position to start looking
     * @param length   max length to scan
     *
     * @return token type for keyword
     */
    public static TokenType lookupKeyword(final String content, final int position, final int length) {
        // First character of keyword.
        final char first = content.charAt(position);

        // Must be lower case character.
        if ('a' <= first && first <= 'z') {
            // Convert to table index.
            final int index = first - tableBase;
            // Get first bucket entry.
            TokenType tokenType = table[index];

            // Search bucket list.
            while (tokenType != null) {
                final int tokenLength = tokenType.getLength();

                // if we have a length match maybe a keyword.
                if (tokenLength == length) {
                    // Do an exact compare of string.
                    final String name = tokenType.getName();
                    int i;
                    for (i = 0; i < length; i++) {
                        if (content.charAt(position + i) != name.charAt(i)) {
                            break;
                        }
                    }

                    if (i == length) {
                        // Found a match.
                        return tokenType;
                    }
                } else if (tokenLength < length) {
                    // Rest of tokens are shorter.
                    break;
                }

                // Try next token.
                tokenType = tokenType.getNext();
            }
        }

        // Not found.
        return IDENT;
    }


    /**
     * Lookup operator.
     *
     * @param ch0 0th char in stream
     * @param ch1 1st char in stream
     * @param ch2 2nd char in stream
     * @param ch3 3rd char in stream
     *
     * @return the token type for the operator
     */
    public static TokenType lookupOperator(final char ch0, final char ch1, final char ch2, final char ch3) {
        // Ignore keyword entries.
        if (tableBase < ch0 && ch0 <= tableLimit && !('a' <= ch0 && ch0 <= 'z')) {
            // Convert to index.
            final int index = ch0 - tableBase;
            // Get first bucket entry.
            TokenType tokenType = table[index];

            // Search bucket list.
            while (tokenType != null) {
                final String name = tokenType.getName();

                switch (name.length()) {
                case 1:
                    // One character entry.
                    return tokenType;
                case 2:
                    // Two character entry.
                    if (name.charAt(1) == ch1) {
                        return tokenType;
                    }
                    break;
                case 3:
                    // Three character entry.
                    if (name.charAt(1) == ch1 &&
                        name.charAt(2) == ch2) {
                        return tokenType;
                    }
                    break;
                case 4:
                    // Four character entry.
                    if (name.charAt(1) == ch1 &&
                        name.charAt(2) == ch2 &&
                        name.charAt(3) == ch3) {
                        return tokenType;
                    }
                    break;
                default:
                    break;
                }

                // Try next token.
                tokenType = tokenType.getNext();
            }
        }

        // Not found.
        return null;
    }
}
