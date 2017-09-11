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

package org.netbeans.api.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.lang.TestLineTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test methods of token sequence.
 *
 * @author mmetelka
 */
public class TokenHierarchyTest extends NbTestCase {
    
    public TokenHierarchyTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws java.lang.Exception {
    }

    protected @Override void tearDown() throws java.lang.Exception {
    }
    
    public void testLanguagePaths() {
        String text = "abc\ndef";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestLineTokenId.language());
        Set<LanguagePath> lps = hi.languagePaths();
        assertNotNull(lps);
        assertEquals(1, lps.size());
        assertTrue(lps.contains(LanguagePath.get(TestLineTokenId.language())));
        
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestLineTokenId.LINE, "abc\n", 0);
        ts.createEmbedding(TestPlainTokenId.language(), 0, 0);
        lps = hi.languagePaths();
        assertEquals(2, lps.size());
    }
    
    public void testDocLanguagePaths() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            Set<LanguagePath> lps = hi.languagePaths();
            assertEquals(0, lps.size());

            // Now put a valid language into document
            doc.putProperty(Language.class,TestLineTokenId.language());

            // Re-check language paths again
            lps = hi.languagePaths();
            assertNotNull(lps);
            assertEquals(1, lps.size());
            assertTrue(lps.contains(LanguagePath.get(TestLineTokenId.language())));
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }
    
    public void testSameEmbeddedToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            TokenSequence<?> ets = ts.embedded();
            assertTrue(ets.moveNext());
            Token<?> et = ets.token();
            assertNotNull(et);

            TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
            TokenSequence<?> ts2 = hi2.tokenSequence();
            assertTrue(ts2.moveNext());
            TokenSequence<?> ets2 = ts2.embedded();
            assertTrue(ets2.moveNext());
            Token<?> et2 = ets2.token();
            assertNotNull(et2);

            assertSame(et, et2);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testEmbeddingOnSubSequence() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);

        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            TokenSequence<?> ets = ts.embedded();
            assertTrue(ets.moveNext());
            Token<?> et = ets.token();
            assertNotNull(et);

            TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
            TokenSequence<?> ts2 = hi2.tokenSequence();
            // Use subsequence
            ts2 = ts2.subSequence(0);
            assertTrue(ts2.moveNext());
            TokenSequence<?> ets2 = ts2.embedded();
            assertTrue(ets2.moveNext());
            Token<?> et2 = ets2.token();
            assertNotNull(et2);

            assertSame("Same tokens expected", et, et2);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testEmbeddingOnSubSequenceSimple() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            // Use subsequence
            ts = ts.subSequence(0);
            assertTrue(ts.moveNext());
            TokenSequence<?> ets = ts.embedded();
            assertTrue(ets.moveNext());
            Token<?> et = ets.token();
            assertNotNull(et);

            // Ask the original top-level token sequence again
            TokenSequence<?> ets2 = ts.embedded();
            assertTrue(ets2.moveNext());
            Token<?> et2 = ets2.token();
            assertNotNull(et2);

            assertSame(et, et2);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void test120906() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        doc.putProperty(Language.class,TestTokenId.language());
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            List<TokenSequence<?>> ets1 = hi.embeddedTokenSequences(4, false);
            assertEquals("Wrong number of embedded TokenSequences", 2, ets1.size());
            assertEquals("Wrong offset from the most embedded TokenSequence", 3, ets1.get(1).offset());

            List<TokenSequence<?>> ets2 = hi.embeddedTokenSequences(6, false);
            assertEquals("Wrong number of embedded TokenSequences", 1, ets2.size());
            assertEquals("Wrong offset from the most embedded TokenSequence", 0, ets2.get(0).offset());

            List<TokenSequence<?>> ets3 = hi.embeddedTokenSequences(3, true);
            assertEquals("Wrong number of embedded TokenSequences", 1, ets3.size());
            assertEquals("Wrong offset from the most embedded TokenSequence", 0, ets3.get(0).offset());

            List<TokenSequence<?>> ets4 = hi.embeddedTokenSequences(0, true);
            assertEquals("Wrong number of embedded TokenSequences", 0, ets4.size());

            // Lexer works over char sequence DocumentUtilities.getText(doc) which is doc.getLength()+1 long
            List<TokenSequence<?>> ets5 = hi.embeddedTokenSequences(doc.getLength()+1, false);
            assertEquals("Wrong number of embedded TokenSequences", 0, ets5.size());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testMultiThreadTokenSequenceCreation() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "a b";
        doc.insertString(0, text, null);
        doc.putProperty(Language.class,TestTokenId.language());
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenHierarchyOperation<?,?> hiOp = LexerApiPackageAccessor.get().tokenHierarchyOperation(hi);
        assertFalse(hiOp.isActiveNoInit());
        TSAccessor.hi = hi;
        int threadCount = 3;
        List<TSAccessor> threads = new ArrayList<TSAccessor>(threadCount);
        while (threads.size() < threadCount) {
            TSAccessor a = new TSAccessor(doc);
            threads.add(a);
            a.start();
        }
        for (TSAccessor a : threads) {
            // repeat to be sure that the notifyAll will find all the threads waiting
            while (a.isAlive()) {
                synchronized (hi) {
                    hi.notifyAll(); // Should wake up all waiting threads
                }
                a.join(10);
            }
        }
        Token<?> token1 = threads.get(0).token1;
        assertNotNull(token1);
        for (TSAccessor a : threads) {
            assertNotNull(a.token1);
            assertSame(token1, a.token1);
        }
    }

    private static final class TSAccessor extends Thread {
        
        static TokenHierarchy<?> hi;
        
        TokenSequence<?> ts;
        Token<?> token1;
        
        private Document doc;
        
        private boolean readLocked;
        
        TSAccessor(Document doc) {
            this.doc = doc;
        }
        
        public void run() {
            if (readLocked) {
                synchronized (hi) {
                    try {
                        hi.wait();
                    } catch (InterruptedException e) {
                    }
                }
                ts = hi.tokenSequence();
                assertTrue(ts.moveNext());
                token1 = ts.token();
                
            } else {
                readLocked = true;
                doc.render(this);
            }
        }

    }

}
