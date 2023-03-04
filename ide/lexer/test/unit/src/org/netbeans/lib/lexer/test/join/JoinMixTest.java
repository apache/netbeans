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

package org.netbeans.lib.lexer.test.join;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinMixTagTokenId;
import org.netbeans.lib.lexer.lang.TestJoinMixTextTokenId;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 *
 * @author Miloslav Metelka
 */
public class JoinMixTest extends NbTestCase {
    
    public JoinMixTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    @Override
    public PrintStream getLog() {
        return System.out;
//        return super.getLog();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();
    }

    public void testMixedSectionsForward() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined

            TestJoinMixTagTokenId.setJoinSections(true);
            ts.moveIndex(3);
            ts.moveNext();
            embedded = ts.embedded();
            assertTrue(embedded.moveNext());
            assertEquals(1, embedded.token().text().length());
            assertEquals("y", embedded.token().text().toString());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
        doc.remove(1, 1);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            assertEquals(5, embedded.offset());
            assertEquals("y", embedded.token().text().toString());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testMixedSectionsBackward() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(3);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined

            TestJoinMixTagTokenId.setJoinSections(true);
            ts.moveIndex(3);
            ts.moveNext();
            embedded = ts.embedded();
            assertTrue(embedded.moveNext());
            assertEquals(1, embedded.token().text().length());
            assertEquals("y", embedded.token().text().toString());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.remove(1, 1);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            assertEquals(5, embedded.offset());
            assertEquals("y", embedded.token().text().toString());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testLastSectionBackward() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(3);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined

            TestJoinMixTagTokenId.setJoinSections(true);
            // Do not physically fetch the token with embedding
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.remove(1, 1);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            assertEquals(5, embedded.offset());
            assertEquals("y", embedded.token().text().toString());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testInsert() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(3); // Over tag token
            ts.moveNext();
//            TokenSequence<?> embedded = ts.embedded();
//            assert (embedded != null); // Non-joined

            TestJoinMixTagTokenId.setJoinSections(true);
            // Do not physically fetch the token with embedding
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.insertString(0, "u<v", null);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testTokenListListInsert() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(3); // Over tag token
            ts.moveNext();
            LanguagePath elp = LanguagePath.get(
                    TestJoinMixTagTokenId.language()).embedded(
                    TestJoinMixTextTokenId.language);
            List<TokenSequence<?>> tsList = hi.tokenSequenceList(elp, 0, Integer.MAX_VALUE);
            assertEquals(2, tsList.size());
//            TokenSequence<?> embedded = ts.embedded();
//            assert (embedded != null); // Non-joined

            TestJoinMixTagTokenId.setJoinSections(true);
            // Do not physically fetch the token with embedding
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.insertString(0, "u<v", null);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testAddedEmbeddingBecomesJoined() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<>b>c"; // "<>" is text
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {
            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setAllowEmbedding(true);
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveEnd(); // Over tag token
            ts.movePrevious();

//            LanguagePath elp = LanguagePath.get(
//                    TestJoinMixTagTokenId.language()).embedded(
//                            TestJoinMixTextTokenId.language);
//            List<TokenSequence<?>> tsList = hi.tokenSequenceList(elp, 0, Integer.MAX_VALUE);
//            assertEquals(1, tsList.size());

            
            TestJoinMixTagTokenId.setAllowEmbedding(true);
            TestJoinMixTagTokenId.setJoinSections(true);

        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.remove(2, 1);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

    public void testAllowAndDisallowEmbedding() throws Exception {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a<x>b<y z>c";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, TestJoinMixTagTokenId.language());
        ((AbstractDocument) doc).readLock();
        try {
            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            TestJoinMixTagTokenId.setAllowEmbedding(true);
            TestJoinMixTagTokenId.setJoinSections(false);
            ts.moveIndex(3); // Over tag token
            ts.moveNext();
            assertNotNull(ts.embedded()); // Embedding should be created

            TestJoinMixTagTokenId.setAllowEmbedding(false); // Prevent embedding creation for "<y z>"
            LanguagePath elp = LanguagePath.get(
                    TestJoinMixTagTokenId.language()).embedded(
                    TestJoinMixTextTokenId.language);
            // Explicitly ask for TestJoinMixTextTokenId.language and mark that the embedding
            // was not created for the requested language (by using appropriate LanguageIds in a WrapTokenId)
            List<TokenSequence<?>> tsList = hi.tokenSequenceList(elp, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size());

            TestJoinMixTagTokenId.setAllowEmbedding(true);
            TestJoinMixTagTokenId.setJoinSections(true);

            ts.moveIndex(1); // Over tag token
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            // Should be null since the impl remembers that request for embedding did not produce
            // any embedding. When impl would change this may be changed too.
            assertNull(embedded);
            tsList = hi.tokenSequenceList(elp, 0, Integer.MAX_VALUE);
            assertEquals(1, tsList.size());

            embedded = ts.embedded(TestJoinMixTextTokenId.language);
            // Should be null since the impl remembers that request for embedding did not produce
            // any embedding. When impl would change this may be changed too.
            assertNull(embedded);
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }

        doc.insertString(0, "u<v", null);

        ((AbstractDocument) doc).readLock();
        try {

            TokenHierarchy<?> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveIndex(1);
            ts.moveNext();
            TokenSequence<?> embedded = ts.embedded();
            assert (embedded != null); // Non-joined
            assertTrue(embedded.moveNext());
            
        } finally {
            ((AbstractDocument) doc).readUnlock();
        }
        
    }

}
