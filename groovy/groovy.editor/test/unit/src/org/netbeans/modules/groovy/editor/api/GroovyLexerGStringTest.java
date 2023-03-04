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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Test groovy lexer
 * 
 * @todo ${...} inside strings
 * 
 * @author Martin Adamek
 */
public class GroovyLexerGStringTest extends NbTestCase {

    public GroovyLexerGStringTest(String testName) {
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
    
    public void testGstringSimple(){
        
        TokenSequence<?> ts = seqForText("def s = \"hallo\"");
        
        next(ts, GroovyTokenId.LITERAL_def, "def");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "s");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        
    }
    
    public void testGstringSimple2(){
        
        TokenSequence<?> ts = seqForText("def s = \"hallo: $a\"");
        
        next(ts, GroovyTokenId.LITERAL_def, "def");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "s");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo: $");
        next(ts, GroovyTokenId.IDENTIFIER, "a");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"");
        
        dumpTokenStream(ts);  
        
    }
    
    public void testGstringSimple3(){
        
        TokenSequence<?> ts = seqForText("def s = \"hallo: ${a}\"");
        
        next(ts, GroovyTokenId.LITERAL_def, "def");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.IDENTIFIER, "s");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo: $");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.IDENTIFIER, "a");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"");
        
        dumpTokenStream(ts);  
        
    }
    
    public void testMinimalKiller(){
        TokenSequence<?> ts = seqForText("\"$a\"");
        dumpTokenStream(ts);  
    }
    
    public void testMinimalKiller2(){
        TokenSequence<?> ts = seqForText("\"$bb\"");
        dumpTokenStream(ts);  
    }
    
    public void testMinimalKiller3(){
        TokenSequence<?> ts = seqForText("\"c$\"");
        dumpTokenStream(ts);  
    }
 
    public void testMinimalKiller4(){
        TokenSequence<?> ts = seqForText("\"$a\"1");
        dumpTokenStream(ts);  
    }

    public void testMinimalKiller4b(){
        TokenSequence<?> ts = seqForText("\"$a\"co");
        dumpTokenStream(ts);  
    }    
    
    public void testMinimalKiller5(){
        TokenSequence<?> ts = seqForText("\"$a\"12");
        dumpTokenStream(ts);  
    }
    
    public void testMinimalKiller6(){
        TokenSequence<?> ts = seqForText("\"$\"");
        dumpTokenStream(ts);  
    }
    
    
    public void testDumpSimpleString(){
        
        TokenSequence<?> ts = seqForText("def s = \"this is a simple text to expose some errors\"");
        
        dumpTokenStream(ts);  
        
    }
    
    public void testDumpSimpleString2(){
        TokenSequence<?> ts = seqForText("$");
        dumpTokenStream(ts);  
    }

    public void testDumpSimpleString3(){
        TokenSequence<?> ts = seqForText("\"$\"");
        dumpTokenStream(ts);  
    }

    public void testDumpSimpleString4(){
        TokenSequence<?> ts = seqForText("test $");
        dumpTokenStream(ts);  
    }

    
        public void testFullSyntaxGstring(){
        String text = "def name = 'World'; println \"Hello, ${name}\"";
        
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
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "'World'", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "println", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"Hello, $", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LBRACE, "{", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.RBRACE, "}", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
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
    
    
    TokenSequence<?> seqForText(String text){
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        return hi.tokenSequence();
    }
    
    
    void next(TokenSequence<?> ts, GroovyTokenId id, String fixedText){
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,id, fixedText, -1);
    }
    
    
  
    
}
