/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestTokenId;
import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class FlyTokensTest extends TestCase {

    public FlyTokensTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testMaxFlySequenceLength() {
        // Both "public" and " " are flyweight
        String text = "public public public public public public public ";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        int firstNonFlyIndex = -1;
        int secondNonFlyIndex = -1;
        int tokenIndex = 0;
        for (int i = 0; i < 7; i++) {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;
        }
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH, firstNonFlyIndex);
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH * 2 + 1, secondNonFlyIndex);
    }    
}
