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

package org.netbeans.lib.lexer.test.simple;

import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class CustomEmbeddingTest extends NbTestCase {
    
    public CustomEmbeddingTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() {
        String text = "abc/*def ghi */// line comment";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        THListener listener = new THListener();
        hi.addTokenHierarchyListener(listener);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*def ghi */", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.LINE_COMMENT, "// line comment", 15);
        assertTrue(ts.createEmbedding(TestTokenId.language(), 3, 0));
        
        // Check the fired event
        TokenHierarchyEvent evt = listener.fetchLastEvent();
        assertNotNull(evt);
        TokenChange<?> tc = evt.tokenChange();
        assertNotNull(tc);
        assertEquals(2, tc.index());
        assertEquals(15, tc.offset());
        assertEquals(0, tc.addedTokenCount());
        assertEquals(0, tc.removedTokenCount());
        assertEquals(TestTokenId.language(), tc.language());
        assertEquals(1, tc.embeddedChangeCount());
        TokenChange<?> etc = tc.embeddedChange(0);
        assertEquals(0, etc.index());
        assertEquals(18, etc.offset());
        assertEquals(0, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
        assertEquals(0, etc.removedTokenCount());
        assertEquals(TestTokenId.language(), etc.language());
        assertEquals(0, etc.embeddedChangeCount());
        
        // Test the contents of the embedded sequence
        TokenSequence<?> ets = ts.embedded(); // Over "// line comment"
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.IDENTIFIER, "line", 18);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.WHITESPACE, " ", 22);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.IDENTIFIER, "comment", 23);
        assertFalse(ets.moveNext());
        
        // Move main TS back and try extra embedding on comment
        assertTrue(ts.movePrevious());
        assertTrue(ts.createEmbedding(TestTokenId.language(), 2, 2));
        ets = ts.embedded(); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.IDENTIFIER, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.IDENTIFIER, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
        
        // Get the default embedding - should be available as well
        ets = ts.embedded(TestPlainTokenId.language()); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
        

        ets = ts.embedded(); // Get the custom embedded token sequence
        assertEquals(ets.language(), TestTokenId.language());
        assertTrue(ets.moveNext());
        assertTrue(ets.isValid());
        assertNotNull(ets);
        // Test removal of the embedding
        assertTrue(ts.removeEmbedding(TestTokenId.language()));
        // The embedded token sequence should no longer be valid
        assertFalse(ets.isValid());
        // Token sequence on which the removeEmbedding() was called should continue to be valid
        assertTrue(ts.isValid());
        // Repetitive removal should return false
        assertFalse(ts.removeEmbedding(TestTokenId.language()));

        
        // Check token sequence list
        // Create custom embedding again
        assertTrue(ts.createEmbedding(TestTokenId.language(), 2, 2));
        LanguagePath lpe = LanguagePath.get(TestTokenId.language()).embedded(TestTokenId.language());
        List<TokenSequence<?>> tsl = hi.tokenSequenceList(lpe, 0, Integer.MAX_VALUE);
        assertEquals(2, tsl.size());
        ts.removeEmbedding(TestTokenId.language());
        tsl = hi.tokenSequenceList(lpe, 0, Integer.MAX_VALUE);
        assertEquals(1, tsl.size());
    }
    
    public void testEmbeddingCaching() throws Exception {
        LanguageEmbedding<?> e = LanguageEmbedding.create(TestTokenId.language(), 2, 1);
        assertSame(TestTokenId.language(), e.language());
        assertSame(2, e.startSkipLength());
        assertSame(1, e.endSkipLength());
        LanguageEmbedding<?> e2 = LanguageEmbedding.create(TestTokenId.language(), 2, 1);
        assertSame(e, e2);
    }
    
    private static final class THListener implements TokenHierarchyListener {
        
        private TokenHierarchyEvent lastEvent;
    
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            this.lastEvent = evt;
        }
        
        public TokenHierarchyEvent fetchLastEvent() {
            TokenHierarchyEvent evt = lastEvent;
            lastEvent = null;
            return evt;
        }

    }

}
