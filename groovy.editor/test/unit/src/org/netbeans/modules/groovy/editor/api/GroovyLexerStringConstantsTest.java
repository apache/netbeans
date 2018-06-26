/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
