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

package org.netbeans.lib.lexer;

import java.lang.ref.WeakReference;
import javax.swing.text.AbstractDocument;
import javax.swing.text.PlainDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestCharTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleLanguageProvider;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;

/**
 *
 * @author vita
 */
public class LanguageManagerTest extends NbTestCase {
    
    public static final String MIME_TYPE_UNKNOWN = "text/x-unknown";
    public static final String MIME_TYPE_KNOWN = "text/x-known";
    
    
    /** Creates a new instance of LanguageManagerTest */
    public LanguageManagerTest(String name) {
        super(name);
    }

    public void testUnknownMimeType() {
        Language lang = LanguageManager.getInstance().findLanguage(MIME_TYPE_UNKNOWN);
        assertNull("There should be no language for " + MIME_TYPE_UNKNOWN, lang);
    }

    public void testKnownMimeType() {
        PlainDocument doc = new PlainDocument();
        doc.putProperty("mimeType", MIME_TYPE_KNOWN);
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            Language lang = th.tokenSequence().language();
            assertNotNull("There should be language for " + MIME_TYPE_KNOWN, lang);

            assertNotNull("Invalid mime type", lang.mimeType());
            assertEquals("Wrong language's mime type", MIME_TYPE_KNOWN, lang.mimeType());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }
    
    public void testCachingMT() {
        Language langA = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langA);
        
        Language langB = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langB);
        
        assertSame("The Language is not cached", langA, langB);
    }
    
    public void testGCedMT() {
        Language lang = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, lang);
        
        WeakReference<Language> ref = new WeakReference<Language>(lang);
        lang = null;
        
        assertGC("Language has not been GCed", ref);
    }

    public void testCacheRefreshMT() {
        Language langA = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langA);
        
        SimpleLanguageProvider.fireLanguageChange();
        
        Language langB = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langB);
        
        assertNotSame("The cache has not been refreshed", langA, langB);
    }

    /*
     * SimplePlainLanguage does not define any embedding. The SimpleLanguageProvider
     * however defines the SimpleCharLanguage as an embedded language for the TestPlainTokenId.WORD.
     * Therefore TestPlainTokenId.WHITESPACE should not have any embedded language and
     * TestPlainTokenId.WORD should have the SimpleCharLanguage.
     */
    public void testEmbedding() {
        TokenHierarchy th = TokenHierarchy.create("abc xyz 012 0xFF00 0-1-2-3-4-5-6-7-8-9", TestPlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        
        for( ; tokens.moveNext(); ) {
            TokenId id = tokens.token().id();
            TokenSequence embedded = tokens.embedded();
            
            if (id == TestPlainTokenId.WHITESPACE) {
                assertNull("Whitespace should not have any embedded language", embedded);
            } else if (id == TestPlainTokenId.WORD) {
                assertNotNull("Word should have an embedded token sequence", embedded);
                assertNotNull("Word should have an embedded language", embedded.language());
                assertEquals("Wrong embedded language", TestCharTokenId.MIME_TYPE, embedded.language().mimeType());
            }
        }
    }
    
    public void testCachingE() {
        TokenHierarchy th = TokenHierarchy.create("abc", TestPlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embeddedA = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedA);
        
        TokenSequence embeddedB = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedB);
        
        assertSame("The embedded language is not cached", embeddedA.language(), embeddedB.language());
    }

    public void testGCedE() {
        TokenHierarchy th = TokenHierarchy.create("abc", TestPlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embedded = tokens.embedded();
        assertNotNull("There should be an embedded language", embedded);
        
        WeakReference<Language> refLang = new WeakReference<Language>(embedded.language());
        embedded = null;

        WeakReference<Token> refToken = new WeakReference<Token>(tokens.token());
        tokens = null;
        th = null;
        
        // This no longer works after the language is statically held in the xxTokenId by the new convention
        //assertGC("The embedded language has not been GCed", refLang);
        assertGC("The token with embedded language has not been GCed", refToken);
    }
    
    public void testCacheRefreshedE() {
        TokenHierarchy th = TokenHierarchy.create("abc", TestPlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embeddedA = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedA);
        
        SimpleLanguageProvider.fireTokenLanguageChange();
        
        TokenSequence embeddedB = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedB);
        
        assertNotSame("The token language cache has not been refreshed", embeddedA, embeddedB);
    }
    
}
