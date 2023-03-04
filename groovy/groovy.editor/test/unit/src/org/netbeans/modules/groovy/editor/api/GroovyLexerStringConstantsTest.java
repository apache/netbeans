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
public class GroovyLexerStringConstantsTest extends NbTestCase {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.groovy.editor.GroovyLexerStringConstants");

    public GroovyLexerStringConstantsTest(String testName) {
        super(testName);
        LOG.setLevel(Level.OFF);
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
 
   public void testStringConstants1(){
        TokenSequence<?> ts = seqForText("\"\'E\'T\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"\'E\'T\"");
        
        dumpTokenStream(ts);  
    }  
    
   public void testStringConstants2(){
        TokenSequence<?> ts = seqForText("\"\'E$a\'T\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"\'E$");
        next(ts, GroovyTokenId.IDENTIFIER, "a");
        next(ts, GroovyTokenId.STRING_LITERAL, "\'T\"");
        
        dumpTokenStream(ts);  
    }

   public void testStringConstants3(){
        TokenSequence<?> ts = seqForText("\"\'E${id}\'T\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"\'E$");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.IDENTIFIER, "id");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.STRING_LITERAL, "\'T\"");
        
        dumpTokenStream(ts);  
    }

    void dumpTokenStream (TokenSequence ts){
        // System.out.println("#############################################");
        
        while (ts.moveNext()) {
              Token t = ts.token();
//              System.out.println("-------------------------------");  
//              System.out.println("Token-ID   :" + t.id());
//              if(t.toString() != null)
//                System.out.println("Token-Name   :" + t.toString());  
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
