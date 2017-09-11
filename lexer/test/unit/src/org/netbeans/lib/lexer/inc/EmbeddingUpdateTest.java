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

package org.netbeans.lib.lexer.inc;

import javax.swing.text.AbstractDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.*;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class EmbeddingUpdateTest extends NbTestCase {
    
    public EmbeddingUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEmbeddingUpdate() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "a/*abc def*/", null);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*abc def*/", 1);
            TokenSequence<?> ets = ts.embedded();
            assertNotNull(ets);
            assertTrue(ts.moveNext());
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "abc", 3);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 6);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 7);
            assertFalse(ets.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        // Make "axbc" inside the comment
        doc.insertString(4, "x", null);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            assertEquals(1, tc.index());
            assertEquals(1, tc.offset());
            assertEquals(1, tc.addedTokenCount());
            assertEquals(1, tc.removedTokenCount());
            assertEquals(TestTokenId.language(), tc.language());
            assertEquals(1, tc.embeddedChangeCount());
            TokenChange<?> etc = tc.embeddedChange(0);
            assertEquals(0, etc.index());
            assertEquals(3, etc.offset());
            assertEquals(1, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
            assertEquals(1, etc.removedTokenCount());
            assertEquals(TestPlainTokenId.language(), etc.language());
            assertEquals(0, etc.embeddedChangeCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.remove(3, 8); // there will be empty /**/ so test empty embedded sequence
        doc.insertString(3, "x", null); // there will be empty /**/
    }        
        
    public void testEmbeddingActivityChange() throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "a/*abc def*/", null);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*abc def*/", 1);
            TokenSequence<?> ets = ts.embedded();
            assertNotNull(ets);
            assertTrue(ts.moveNext());
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "abc", 3);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 6);
            assertTrue(ets.moveNext());
            LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 7);
            assertFalse(ets.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
        final TokenHierarchyControl control = input.tokenHierarchyControl();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                control.setActive(false);
                control.setActive(true);
            }
        });

    }

}
