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

package org.netbeans.modules.groovy.editor.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Test groovy lexer
 * 
 * @author Martin Adamek
 */
public class GroovyLexerBatchTest extends NbTestCase {

    public GroovyLexerBatchTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        Logger.getLogger("org.netbeans.modules.groovy.editor.lexer.GroovyLexer").setLevel(Level.FINEST);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    // uncomment this to have logging from GroovyLexer
//    protected Level logLevel() {
//        // enabling logging
//        return Level.INFO;
//        // we are only interested in a single logger, so we set its level in setUp(),
//        // as returning Level.FINEST here would log from all loggers
//    }

    public void testDiv() {
        String text = "def s = 4 / 2\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NUM_INT, "4", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.DIV, "/", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NUM_INT, "2", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
 
    public void testImportStatements(){
        
        String input = 
                "import java.util.HashSet;\n" +
                "import java.util.List;\n" +
                "import java.util.Set;\n" +
                "import java.util.Iterator;\n" +
                "\n" +
                "println \"hallo\"\n";
        
        TokenSequence<?> ts = seqForText(input);
        dumpTokenStream(ts);
    }    
    
    
    
    public void testGstringLexing1(){
        
        TokenSequence<?> ts = seqForText("{{}}");
        
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.RBRACE, "}");
    }
    
    public void testGstringLexing2(){
        
        TokenSequence<?> ts = seqForText("x=\"z\";assert\"h{$x}\".size()==4");
        
        next(ts, GroovyTokenId.IDENTIFIER, "x");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"z\"");
        next(ts, GroovyTokenId.SEMI, ";");
        next(ts, GroovyTokenId.LITERAL_assert, "assert");
    }
    
    public void testGstringLexing3(){
        
        TokenSequence<?> ts = seqForText("println \"hallo\".size()");
        
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.DOT, ".");
        next(ts, GroovyTokenId.IDENTIFIER, "size");
        next(ts, GroovyTokenId.LPAREN, "(");
        next(ts, GroovyTokenId.RPAREN, ")");
    }
    
    public void testGstringLexing4(){
        
        TokenSequence<?> ts = seqForText("\"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
    }

    
    public void testGstringLexing5(){
        
        TokenSequence<?> ts = seqForText("println \"Hello $name here!\"\r\n");
        
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $");
        next(ts, GroovyTokenId.IDENTIFIER, "name");
        next(ts, GroovyTokenId.STRING_LITERAL, " here!\"");
        next(ts, GroovyTokenId.NLS, "\r\n");
    }
    
    public void testGstringLexing6(){
        
        TokenSequence<?> ts = seqForText("println \"Hello $name here!\"; println 'aaa'\n");
        
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $");
        next(ts, GroovyTokenId.IDENTIFIER, "name");
        next(ts, GroovyTokenId.STRING_LITERAL, " here!\"");
        next(ts, GroovyTokenId.SEMI, ";");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "'aaa'");
        next(ts, GroovyTokenId.NLS, "\n");
    }
    
    public void testGstringLexing7(){
        
        TokenSequence<?> ts = seqForText("def mappings = { \"/$controller/$action?/$id?\" { constraints { } } }\n");
        
        next(ts, GroovyTokenId.LITERAL_def, "def");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "mappings");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"/$");
        next(ts, GroovyTokenId.IDENTIFIER, "controller");
        next(ts, GroovyTokenId.STRING_LITERAL, "/$");
        next(ts, GroovyTokenId.IDENTIFIER, "action");
        next(ts, GroovyTokenId.STRING_LITERAL, "?/$");
        next(ts, GroovyTokenId.IDENTIFIER, "id");
        next(ts, GroovyTokenId.STRING_LITERAL, "?\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "constraints");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.NLS, "\n");
    }
    
    public void testMultipleStringConstants(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
    }
    
    public void testMultipleStringConstantsClosed(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        assertFalse(ts.moveNext());
    }
    
    public void testMultipleStringConstantsDosTerminated(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"\r\n");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.NLS, "\r\n");
        assertFalse(ts.moveNext());
    } 
    
    public void testMultipleStringConstantsUnixTerminated(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"\n");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.NLS, "\n");
        assertFalse(ts.moveNext());
    }     
    
    public void testLengthTest1(){
        String text = "true";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertTrue(ts.moveNext());
        Token t = ts.token();
        assertTrue(t.length() == 4);
        
    }
    
    public void testLengthTest2(){
        String text = "\"hallo\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertTrue(ts.moveNext());
        Token t = ts.token();
        assertTrue(t.length() == 7);
        
    }
    
    public void testLengthTest3(){
        String text = "assert true";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 6);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 4);
    }
    
    public void testLengthTest4(){
        String text = "\"hallo\".size()";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 4);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
    }
    
    public void testLengthTest5(){
        String text = "println \"hallo\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
    }    
    
     public void testLengthTest6(){
        String text = "println \"hallo\"\n";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
    }
     

    public void testGstringsWithoutNewLine() {
        String text = "def name = 'foo'; def s = \"H $name\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "'foo'", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $", -1); // this should be same in both tests
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
    }
    
    public void testGstringsBeforeNewLine(){
        String text = "def name = 'foo'; def s = \"H $name\"\n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "'foo'", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $", -1); // this should be same in both tests
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
    
    public void testQuotes() {
        String text = "def s = \"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"\"", -1);
    }
    
    public void test1() {
        String text = 
                "class Foo {\n" +
                "\n" +
                "private def aaa;\n" +
                "}";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_class, "class", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "Foo", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LBRACE, "{", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_private, "private", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "aaa", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
   
    public void testCRLF() {
        String text = 
                "class LineTermination {\n" +
                "\n" +
                "\r\n" +
                "\r" +
                "}";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_class, "class", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "LineTermination", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LBRACE, "{", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\r\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\r", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.RBRACE, "}", -1);
    }
    public void test2() {
        String commentText = "/* test comment  */";
        String text = "abc+ " + commentText + "def public publica publi static x";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", 4);
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        int commentIndex = ts.index();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", offset);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_public, "public", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "publica", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "publi", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_static, "static", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "x", -1);
        assertFalse(ts.moveNext());

        // Go back to block comment
        assertEquals(0, ts.moveIndex(commentIndex));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.BLOCK_COMMENT, commentText, commentTextStartOffset);

    }
    
    public void testPerf() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7000; i++) {
            sb.append("public static x + y /* test comment */ abc * def\n");
        }
        String text = sb.toString();

        long tm;
        Language<GroovyTokenId> language = GroovyTokenId.language();
        tm = System.currentTimeMillis();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language);
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        TokenSequence<?> ts = hi.tokenSequence();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        // Fetch 2 initial tokens - should be lexed lazily
        tm = System.currentTimeMillis();
        ts.moveNext();
        ts.token();
        ts.moveNext();
        ts.token();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        ts.moveIndex(0);
        int cntr = 1; // On the first token
        while (ts.moveNext()) {
            Token t = ts.token();
            cntr++;
        }
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 6000); // Should be fast
    }
    
    TokenSequence<?> seqForText(String text){
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        return hi.tokenSequence();
    }
    
    
    void next(TokenSequence<?> ts, GroovyTokenId id, String fixedText){
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,id, fixedText, -1);
    }
    
    void dumpTokenStream (TokenSequence ts){
//        System.out.println("#############################################");
        
        while (ts.moveNext()) {
              Token t = ts.token();
//              System.out.println("-------------------------------");  
//              System.out.println("Token-Text :" + t.toString());  
//              System.out.println("Token-ID   :" + t.id());
            }
    }
    
}
