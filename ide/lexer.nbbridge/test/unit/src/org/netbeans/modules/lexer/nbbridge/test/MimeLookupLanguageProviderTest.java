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

package org.netbeans.modules.lexer.nbbridge.test;

import java.util.Collection;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.lexer.nbbridge.test.simple.SimplePlainTokenId;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class MimeLookupLanguageProviderTest extends NbTestCase{
    
    /** Creates a new instance of MimeLookupLanguageProviderTest */
    public MimeLookupLanguageProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        // Initialize the module system
        Collection<? extends ModuleInfo> infos = Lookup.getDefault().<ModuleInfo>lookupAll(ModuleInfo.class);
    }
    
    public void testFindLanguageForMT() {
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-simple-char");
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        assertNotNull("Can't find token hierarchy for a text/x-simple-char document", th);
        
        Language lang = th.tokenSequence().language();
        assertNotNull("Can't find language for text/x-simple-char", lang);
        assertEquals("Wrong language", "text/x-simple-char", lang.mimeType());
    }

    public void testLanguagesEmbeddingMapMT() throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-simple-plain");
        // All words have to be longer than 3 characters
        doc.insertString(0, "Hello 1234 0xFF00", SimpleAttributeSet.EMPTY);
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        assertNotNull("Can't find token hierarchy for a text/x-simple-plain document", th);
        
        TokenSequence seq = th.tokenSequence();
        Language lang = seq.language();
        assertNotNull("Can't find language for text/x-simple-plain", lang);
        assertEquals("Wrong language", "text/x-simple-plain", lang.mimeType());
        
        for(int i = 0; i < seq.tokenCount(); i++) {
            seq.moveIndex(i);
            assertTrue(seq.moveNext());
            Token token = seq.token();
            
            if (token.id() == SimplePlainTokenId.WORD) {
                TokenSequence embeddedSeq = seq.embedded();
                assertNotNull("Can't find embedded token sequence", embeddedSeq);

                Language embeddedLang = embeddedSeq.language();
                assertNotNull("Can't find language of the embedded sequence", embeddedLang);
                assertEquals("Wrong language of the embedded sequence", "text/x-simple-char", embeddedLang.mimeType());
                
                embeddedSeq.moveStart();
                assertTrue("Embedded sequence has no tokens (moveFirst)", embeddedSeq.moveNext());
                assertEquals("Wrong startSkipLength", 1, embeddedSeq.offset() - seq.offset());
                
                embeddedSeq.moveEnd();
                assertTrue("Embedded sequence has no tokens (moveLast)", embeddedSeq.movePrevious());
                assertEquals("Wrong endSkipLength", 2, 
                    (seq.offset() + seq.token().length()) - (embeddedSeq.offset() + embeddedSeq.token().length()));
            }
        }
    }
}
