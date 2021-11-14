/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.java.lexer;

import java.util.EnumSet;
import java.util.function.Supplier;
import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavaMultiLineStringTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavaMultiLineStringLexerBatchTest extends TestCase {

    public JavaMultiLineStringLexerBatchTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testComments() {
        String text = "\"\"\"\n   foo\n   \n   bar\"\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.MULTILINE_STRING_LITERAL, "\"\"\"\n   foo\n   \n   bar\"\"\"");
        TokenSequence<?> tse = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.INDENT, "   ");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.TEXT, "foo");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.INDENT, "   ");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.INDENT, "   ");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.TEXT, "bar");
        assertFalse(tse.moveNext());
        assertFalse(ts.moveNext());
    }
    
    public void testShort1() {
        String text = "\"\"\"\n\"\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.MULTILINE_STRING_LITERAL, "\"\"\"\n\"\"\"");
        TokenSequence<?> tse = ts.embedded();
        assertFalse(tse.moveNext());
        assertFalse(ts.moveNext());
    }
    
    public void testShort2() {
        String text = "\"\"\"\n    \"\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.MULTILINE_STRING_LITERAL, "\"\"\"\n    \"\"\"");
        TokenSequence<?> tse = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.INDENT, "    ");
        assertFalse(tse.moveNext());
        assertFalse(ts.moveNext());
    }
    
    public void testIncomplete() {
        String text = "\"\"\"\n    \n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.MULTILINE_STRING_LITERAL, "\"\"\"\n    \n\n");
        TokenSequence<?> tse = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.TEXT, "    ");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.NEWLINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(tse, JavaMultiLineStringTokenId.NEWLINE, "\n");
        assertFalse(tse.moveNext());
        assertFalse(ts.moveNext());
    }
    
}
