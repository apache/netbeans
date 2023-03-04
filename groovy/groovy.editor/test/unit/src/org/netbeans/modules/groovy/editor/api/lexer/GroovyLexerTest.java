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

package org.netbeans.modules.groovy.editor.api.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 * Although this test class might not be necessary it is often not obvious what
 * is the meaning of some Groovy Lexer tokens. For such cases this should help
 * to get better idea.
 * 
 * @author Martin Janicek
 */
public class GroovyLexerTest extends GroovyTestBase {

    public GroovyLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testCharInDeclaration() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("a=\'x\'");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\'x\'");
    }
    
    public void testStringInDeclaration() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("a = \"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"string\"");
    }
    
    public void testMultilineString() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
            "def text = \"\"\"\\\n" +
            "hello there\n" +
            "how are you?\n" +
            "\"\"\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_def, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, 
            "\"\"\"\\\n" +
            "hello there\n" +
            "how are you?\n" +
            "\"\"\"");
    }
    
    public void testGString() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
            "def text = \"hello there ${name}, how are you?\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_def, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"hello there $");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LBRACE, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RBRACE, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, ", how are you?\"");
    }
    
    public void testLineComment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("// def =\"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LINE_COMMENT, "// def =\"string\"");
    }
    
    // Groovy supports special line comment started with #!
    public void testLineComment_SH_comment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("#! def =\"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LINE_COMMENT, "#! def =\"string\"");
    }
    
    public void testBlockComment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
                "/*"
                + "Testing block comment\n"
                + ".. is it work?\n"
                + "*/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.BLOCK_COMMENT,
                "/*"
                + "Testing block comment\n"
                + ".. is it work?\n"
                + "*/");
    }
    
    public void testDocumentation() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
                "/**"
                + "Some method description.\n"
                + "@param whatever\n"
                + "@return whaaat?"
                + "*/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.BLOCK_COMMENT,
                "/**"
                + "Some method description.\n"
                + "@param whatever\n"
                + "@return whaaat?"
                + "*/");
    }
    
    public void testForLoop() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("for (int i = 0; i < 10; i++) {");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_for, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_int, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SEMI, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "10");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SEMI, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.INC, "++");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LBRACE, "{");
    }
    
    private TokenSequence<GroovyTokenId> createTokenSequenceFor(String text) {
        return TokenHierarchy.create(text, GroovyTokenId.language()).tokenSequence(GroovyTokenId.language());
    }
}
