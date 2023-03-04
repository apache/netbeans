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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test character preprocessing.
 *
 * @author mmetelka
 */
public class CharPreprocessingTest extends TestCase {

    public CharPreprocessingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testMaxFlySequenceLength() {
        String text = " \\u0020 public";
        TokenHierarchy hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " \\u0020 ", 0);
//        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", 8);
//        assertFalse(ts.moveNext());
    }    

}
