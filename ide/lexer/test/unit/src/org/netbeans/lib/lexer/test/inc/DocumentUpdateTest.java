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
package org.netbeans.lib.lexer.test.inc;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 *
 * @author Jan Lahoda
 */
public class DocumentUpdateTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public DocumentUpdateTest(String name) {
        super(name);
    }
    
    public void testUpdate1() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\t test\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            s.embedded();
        } finally {
            ((AbstractDocument)d).readUnlock();
        }

        d.insertString(5, "t", null);
    }
    
    public void testUpdate2() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\b\\t sfdsffffffffff\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            s.embedded();
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
        
        d.insertString(10, "t", null);
    }
    
    public void testUpdate3() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"t\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            assertNotNull(s.embedded());
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
        
        d.insertString(1, "\\", null);
        
        ((AbstractDocument)d).readLock();
        try {
        
            LexerTestUtilities.assertNextTokenEquals(h.tokenSequence(),TestTokenId.STRING_LITERAL, "\"\\t\"");

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            TokenSequence<?> e = s.embedded();

            assertNotNull(e);

            assertTrue(e.moveNext());

            assertEquals(e.token().id(),TestStringTokenId.TAB);
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
    }
    
}
