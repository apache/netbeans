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

package org.netbeans.lib.lexer.test.join;

import javax.swing.text.AbstractDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.lib.lexer.lang.TestJoinTopTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinTextTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test joining of embedded sections.
 *
 * @author mmetelka
 */
public class JoinSectionsPositioningTest extends NbTestCase {
    
    public JoinSectionsPositioningTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    private ModificationTextDocument initDocument() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a[b<cd>e]f<gh>i[][j<kl>m]n<o>p<q>r";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class,TestJoinTopTokenId.language());
        return doc;
    }
    
    public void testTokensAndEmbeddings() throws Exception { // TokenSequence.move() and moveIndex()
        ModificationTextDocument doc = initDocument();
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "a[b", 0);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<cd>", 3);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "e]f", 7);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<gh>", 10);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "i[][j", 14);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<kl>", 19);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "m]n", 23);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<o>", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "p", -1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TAG, "<q>", -1);
            assertTrue(ts.moveNext());
            // Contains extra '\n' at the end of document
            LexerTestUtilities.assertTokenEquals(ts,TestJoinTopTokenId.TEXT, "r\n", -1);
            assertFalse(ts.moveNext());
            assertEquals(11, ts.tokenCount());

            // Check regular TS.embedded()
            ts.moveStart();
            assertTrue(ts.moveNext()); // Over "a{b"
            TokenSequence<?> tse = ts.embedded();
            assertTrue(tse.moveNext());
            LexerTestUtilities.assertTokenEquals(tse,TestJoinTextTokenId.TEXT, "a", 0);
            assertTrue(tse.moveNext());
            LexerTestUtilities.assertTokenEquals(tse,TestJoinTextTokenId.BRACKETS, "[b", 1);
            assertEquals(tse.token().partType(), PartType.START);
            assertFalse(tse.moveNext());

            assertTrue(ts.moveNext()); // Over "<cd>"
            assertTrue(ts.moveNext()); // Over "e}f"
            TokenSequence<?> tse2 = ts.embedded();
            assertTrue(tse2.moveNext());
            LexerTestUtilities.assertTokenEquals(tse2,TestJoinTextTokenId.BRACKETS, "e]", 7);
            assertEquals(tse2.token().partType(), PartType.END);
            assertTrue(tse2.moveNext());
            LexerTestUtilities.assertTokenEquals(tse2,TestJoinTextTokenId.TEXT, "f", 9);
            assertEquals(tse2.token().partType(), PartType.START);
            assertFalse(tse2.moveNext());

            assertTrue(ts.moveNext()); // Over "<gh>"
            assertTrue(ts.moveNext()); // Over "i{}{j"
            TokenSequence<?> tse3 = ts.embedded();
            assertTrue(tse3.moveNext());
            LexerTestUtilities.assertTokenEquals(tse3,TestJoinTextTokenId.TEXT, "i", 14);
            assertEquals(tse3.token().partType(), PartType.END);
            assertTrue(tse3.moveNext());
            LexerTestUtilities.assertTokenEquals(tse3,TestJoinTextTokenId.BRACKETS, "[]", 15);
            assertEquals(tse3.token().partType(), PartType.COMPLETE);
            assertTrue(tse3.moveNext());
            LexerTestUtilities.assertTokenEquals(tse3,TestJoinTextTokenId.BRACKETS, "[j", 17);
            assertEquals(tse3.token().partType(), PartType.START);
            assertFalse(tse3.moveNext());


            // Check TS.embeddedJoin()
            TokenSequence<?> tsej = ts.embeddedJoined();
            assertEquals(2, tsej.index());
            assertTrue(tsej.moveNext());
            LexerTestUtilities.assertTokenEquals(tsej,TestJoinTextTokenId.TEXT, "fi", 9);
            assertEquals(9, tsej.token().offset(null)); // Assert also token.offset() besides TS.offset()
            assertEquals(tsej.token().partType(), PartType.COMPLETE);
            assertTrue(tsej.moveNext());
            LexerTestUtilities.assertTokenEquals(tsej,TestJoinTextTokenId.BRACKETS, "[]", 15);
            assertEquals(tsej.token().partType(), PartType.COMPLETE);
            assertTrue(tsej.moveNext());
            LexerTestUtilities.assertTokenEquals(tsej,TestJoinTextTokenId.BRACKETS, "[jm]", 17);
            assertEquals(tsej.token().partType(), PartType.COMPLETE);

            tsej.moveStart();
            assertTrue(tsej.moveNext());
            LexerTestUtilities.assertTokenEquals(tsej,TestJoinTextTokenId.TEXT, "a", 0);
            assertEquals(0, tsej.token().offset(null)); // Assert also token.offset() besides TS.offset()
            assertEquals(tsej.token().partType(), PartType.COMPLETE);
            assertTrue(tsej.moveNext());
            LexerTestUtilities.assertTokenEquals(tsej,TestJoinTextTokenId.BRACKETS, "[be]", 1);
            assertEquals(1, tsej.token().offset(null)); // Assert also token.offset() besides TS.offset()
            assertEquals(tsej.token().partType(), PartType.COMPLETE);
    //        assertFalse(tsej.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

    }

    public void testTSMove() throws Exception {
        ModificationTextDocument doc = initDocument();
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertEquals(1, ts.move(8));
            ts.moveStart();
            assertTrue(ts.moveNext());

            // Test TS.move() on embeddedJoin()
            TokenSequence<?> tsej = ts.embeddedJoined();
            assertEquals(-3, tsej.move(-3)); // Token starts at offset == 1
            assertEquals(0, tsej.index());

            assertEquals(7, tsej.move(8)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(6, tsej.move(7)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(5, tsej.move(6)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(2, tsej.move(3)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(1, tsej.move(2)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(1, tsej.move(2)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(1, tsej.move(2)); // Token starts at offset == 1
            assertEquals(1, tsej.index());

            assertEquals(1, tsej.move(16)); // Token starts at offset == 15
            assertEquals(3, tsej.index());

            assertEquals(0, tsej.move(15)); // Token starts at offset == 15
            assertEquals(3, tsej.index());

            assertEquals(0, tsej.move(17)); // Token starts at offset == 15
            assertEquals(4, tsej.index());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }

    public void testShortDoc() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<b>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            ts.embedded(); // Creates JTL
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

}
