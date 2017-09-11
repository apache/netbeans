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
