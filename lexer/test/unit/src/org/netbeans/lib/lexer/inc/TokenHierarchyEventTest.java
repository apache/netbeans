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
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenHierarchyEventTest extends NbTestCase {
    
    public TokenHierarchyEventTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "abc def ghi";
        doc.insertString(0, text, null);
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", 4);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 7);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "ghi", 8);
            // Extra newline at doc's end is contained in the DocumentUtilities.getText(doc)
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", 11);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Do insert
        doc.insertString(5, "x", null);
        
        ((AbstractDocument)doc).readLock();
        try {
            // Check the fired event
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            assertNotNull(evt);
            TokenChange<?> tc = evt.tokenChange();
            assertNotNull(tc);
            assertEquals(2, tc.index());
            assertEquals(4, tc.offset());

            assertEquals(1, tc.removedTokenCount());
            TokenSequence<?> removedTS = tc.removedTokenSequence();
            assertTrue(removedTS.moveNext());
            LexerTestUtilities.assertTokenEquals(removedTS, TestTokenId.IDENTIFIER, 3, 4);

            assertEquals(1, tc.addedTokenCount());
            TokenSequence<?> currentTS = tc.currentTokenSequence();
            currentTS.moveIndex(tc.index());
            assertTrue(currentTS.moveNext());
            LexerTestUtilities.assertTokenEquals(currentTS, TestTokenId.IDENTIFIER, "dxef", 4);

            assertEquals(TestTokenId.language(), tc.language());
            assertEquals(0, tc.embeddedChangeCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }
    
}
