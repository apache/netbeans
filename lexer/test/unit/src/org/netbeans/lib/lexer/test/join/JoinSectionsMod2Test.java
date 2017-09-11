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

import java.io.PrintStream;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinTextTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestJoinTopTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Additional tests of modification of join sections.
 *
 * @author Miloslav Metelka
 */
public class JoinSectionsMod2Test extends NbTestCase {
    
    public JoinSectionsMod2Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    public PrintStream getLog(String logName) {
        return System.out;
    }

    /**
     * Test will create custom embedding on "(x)" and a token sequence on it.
     * Then it will create custom embedding on "(y)" and preceding token sequence
     * should throw ConcurrentModificationException since its token will be changed
     * to a join token's part.
     */
    public void testTokenSequenceInvalidation() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a(x)b(y)c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

        final TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = hi.tokenSequence();
                assertTrue(ts.moveNext());
                LexerTestUtilities.assertTokenEquals(ts, TestJoinTopTokenId.TEXT, "a(x)b(y)c\n", -1);
                assertFalse(ts.moveNext());
                TokenSequence<?> ts1 = ts.embedded();
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1, TestJoinTextTokenId.TEXT, "a", -1);
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1, TestJoinTextTokenId.PARENS, "(x)", -1);
                ts1.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
                TokenSequence<?> ts2 = ts1.embedded();
                assertTrue(ts2.moveNext());
                LexerTestUtilities.assertTokenEquals(ts2, TestPlainTokenId.WORD, "x", -1);
                assertFalse(ts2.moveNext());
                assertEquals(ts2.token().partType(), PartType.COMPLETE);
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1, TestJoinTextTokenId.TEXT, "b", -1);
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1, TestJoinTextTokenId.PARENS, "(y)", -1);
                ts1.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
                TokenSequence<?> ts3 = ts1.embedded();
                assertTrue(ts3.moveNext());
                LexerTestUtilities.assertTokenEquals(ts3, TestPlainTokenId.WORD, "y", -1);
                assertFalse(ts3.moveNext());
                assertTrue(ts1.moveNext());
                LexerTestUtilities.assertTokenEquals(ts1, TestJoinTextTokenId.TEXT, "c\n", -1);
                assertTrue(ts1.movePrevious());

                // Operation on ts2 should not be possible
                try {
                    ts2.movePrevious();
                    fail("Operation on ts2 should throw ConcurrentModificationException");
                } catch (ConcurrentModificationException e) {
                    // Expected
                }
            }
        });
    }

    public void testEmbeddingDynamicCreation() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a%";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        LanguagePath embLP = LanguagePath.get(TestJoinTopTokenId.language()).
                embedded(TestJoinTextTokenId.inPercentsLanguage);
        List<TokenSequence<?>> tsList;
        ((AbstractDocument)doc).readLock();
        try {
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size()); // Contains single token for extra '\n' in the doc
            LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenHierarchyUpdate").setLevel(Level.FINEST); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListUpdater").setLevel(Level.FINE); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListListUpdate").setLevel(Level.FINE); // Extra logging
        
        doc.insertString(2, "%", null);
        ((AbstractDocument)doc).readLock();
        try {
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(2, 1);
        ((AbstractDocument)doc).readLock();
        try {
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size()); // Contains single token for extra '\n' in the doc
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testEmbeddingDynamicUpdate() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a%";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenHierarchyUpdate").setLevel(Level.FINEST); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListUpdater").setLevel(Level.FINE); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListListUpdate").setLevel(Level.FINE); // Extra logging
        
        doc.insertString(2, "%", null);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        LanguagePath embLP;
        List<TokenSequence<?>> tsList;
        ((AbstractDocument)doc).readLock();
        try {
            embLP = LanguagePath.get(TestJoinTopTokenId.language()).
                    embedded(TestJoinTextTokenId.inPercentsLanguage);
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(2, 1);
        ((AbstractDocument)doc).readLock();
        try {
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size()); // contains single token for extra '\n' at the end of doc
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        doc.insertString(2, "%", null); // BTW does not have to be '%'
        ((AbstractDocument)doc).readLock();
        try {
            tsList = hi.tokenSequenceList(embLP, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testNestedEmbeddingOffsetsRetaining() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a%";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenHierarchyUpdate").setLevel(Level.FINEST); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListUpdater").setLevel(Level.FINE); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListListUpdate").setLevel(Level.FINE); // Extra logging
        
        doc.insertString(2, "x", null);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        doc.insertString(3, "y", null);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        doc.remove(3, 1);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        doc.remove(2, 1);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        doc.insertString(2, "x", null);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        doc.insertString(3, "y", null);
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
    }

    public void testEmptyEmbedding() throws Exception {
        String text = "ab<[x]j>c<k[ y ]>d<[z]>";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTopTokenId.language());
        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created
        
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenHierarchyUpdate").setLevel(Level.FINEST); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListUpdater").setLevel(Level.FINE); // Extra logging
//        Logger.getLogger("org.netbeans.lib.lexer.inc.TokenListListUpdate").setLevel(Level.FINE); // Extra logging
        
        doc.remove(8, 10);
    }

    public void testJoinEmbeddingDynamicCreationAndRemoval() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a(x)b(y)c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinTextTokenId.language);
//        LexerTestUtilities.incCheck(doc, true); // Ensure the whole embedded hierarchy gets created

        final TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = hi.tokenSequence();
                assertTrue(ts.moveNext());
                assertTrue(ts.moveNext()); // on "(x)"
                // Create embedding that joins sections
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
                assertTrue(ts.moveNext()); // on "b"
                assertTrue(ts.moveNext()); // on "(y)"
                hi.tokenSequenceList(LanguagePath.get(TestJoinTextTokenId.language).
                        embedded(TestPlainTokenId.language()), 0, Integer.MAX_VALUE);
                // Create embedding that joins sections
                ts.createEmbedding(TestPlainTokenId.language(), 1, 1, true);
            }
        });

        doc.remove(0, doc.getLength());
    }

}
