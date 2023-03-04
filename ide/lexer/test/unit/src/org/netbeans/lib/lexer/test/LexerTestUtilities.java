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

package org.netbeans.lib.lexer.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.test.dump.TokenDumpCheck;


/**
 * Various utilities related to lexer's and token testing.
 *
 * @author mmetelka
 */
public final class LexerTestUtilities {
    
    private static final String LAST_TOKEN_HIERARCHY = "last-token-hierarchy";

    private static Field tokenListField;
    
    private LexerTestUtilities() {
        // no instances
    }
    
    public static void assertConsistency(TokenHierarchy<?> hi) {
        String error = LexerApiPackageAccessor.get().tokenHierarchyOperation(hi).checkConsistency();
        if (error != null) {
            TestCase.fail("\n\n!!!!!->->->->->-> CONSISTENCY ERROR in Token Hierarchy <-<-<-<-<-<-!!!!!!!\n" +
                    error + "\n\nINCONSISTENT TOKEN HIERARCHY:\n" + hi);
        }
    }
    
    /**
     * @see #assertTokenEquals(String, TokenSequence, TokenId, String, int)
     */
    public static void assertTokenEquals(TokenSequence<?> ts, TokenId id, String text, int offset) {
        assertTokenEquals(null, ts, id, text, offset);
    }

    /**
     * @see #assertTokenEquals(String, TokenSequence, TokenId, int, int)
     */
    public static void assertTokenEquals(TokenSequence<?> ts, TokenId id, int length, int offset) {
        assertTokenEquals(null, ts, id, length, offset);
    }

    /**
     * Compare <code>TokenSequence.token()</code> to the given
     * token id, text and offset.
     *
     * @param offset expected offset. It may be -1 to prevent offset testing.
     */
    public static void assertTokenEquals(String message, TokenSequence<?> ts, TokenId id, String text, int offset) {
        assertTokenEquals(message, ts, id, text, offset, (text != null) ? text.length() : -1);
    }

    /**
     * Compare <code>TokenSequence.token()</code> to the given
     * token id, length and offset.
     *
     * @param length length of the token (text might be null for removed token so it could not be compared).
     * @param offset expected offset. It may be -1 to prevent offset testing.
     */
    public static void assertTokenEquals(String message, TokenSequence<?> ts, TokenId id, int length, int offset) {
        assertTokenEquals(message, ts, id, null, offset, length);
    }

    private static void assertTokenEquals(String message, TokenSequence<?> ts, TokenId id, String text, int offset, int length) {
        message = messagePrefix(message);
        Token<?> t = ts.token();
        TestCase.assertNotNull("Token is null", t);
        TokenId tId = t.id();
        TestCase.assertEquals(message + "Invalid token.id() for text=\"" + debugTextOrNull(t.text()) + '"', id, tId);
        if (length != -1) {
            TestCase.assertEquals("Invalid token length", length, t.length());
        }
        if (text != null) {
            CharSequence tText = t.text();
            assertTextEquals(message + "Invalid token.text() for id=" + LexerUtilsConstants.idToString(id), text, tText);
            TestCase.assertEquals(message + "Invalid token.length()", text.length(), t.length());
        }

        if (offset != -1) {
            int tsOffset = ts.offset();
            TestCase.assertEquals(message + "Invalid tokenSequence.offset()", offset, tsOffset);

            // It should also be true that if the token is non-flyweight then
            // ts.offset() == t.offset()
            // and if it's flyweight then t.offset() == -1
            int tOffset = t.offset(null);
            assertTokenOffsetMinusOneForFlyweight(t.isFlyweight(), tOffset);
            if (!t.isFlyweight()) {
                assertTokenOffsetsEqual(message, tOffset, offset);
            }
        }
    }
    
    public static void assertTokenEquals(TokenSequence<?> ts, TokenId id, String text, int offset,
    int lookahead, Object state) {
        assertTokenEquals(null, ts, id, text, offset, lookahead, state);
    }

    public static void assertTokenEquals(String message, TokenSequence<?> ts, TokenId id, String text, int offset,
    int lookahead, Object state) {
        assertTokenEquals(message, ts, id, text, offset);

        Token t = ts.token();
        message = messagePrefix(message);
        TestCase.assertEquals(message + "Invalid token.lookahead()", lookahead, lookahead(ts));
        TestCase.assertEquals(message + "Invalid token.state()", state, state(ts));
    }
    
    public static void assertTokenOffsetsEqual(String message, int offset1, int offset2) {
        if (offset1 != -1 && offset2 != -1) { // both non-flyweight
            TestCase.assertEquals(messagePrefix(message)
                    + "Offsets equal", offset1, offset2);
        }
    }
    
    public static void assertTokenFlyweight(Token token) {
        TestCase.assertEquals("Token flyweight", true, token.isFlyweight());
    }
    
    public static void assertTokenNotFlyweight(Token token) {
        TestCase.assertEquals("Token not flyweight", true, !token.isFlyweight());
    }
    
    private static void assertTokenOffsetMinusOneForFlyweight(boolean tokenFlyweight, int offset) {
        if (tokenFlyweight) {
            TestCase.assertEquals("Flyweight token => token.offset()=-1", -1, offset);
        } else { // non-flyweight
            TestCase.assertTrue("Non-flyweight token => token.offset()!=-1 but " + offset, (offset != -1));
        }
    }

    /**
     * Assert that the next token in the token sequence
     */
    public static void assertNextTokenEquals(TokenSequence<?> ts, TokenId id, String text) {
        assertNextTokenEquals(null, ts, id, text);
    }

    public static void assertNextTokenEquals(String message, TokenSequence<?> ts, TokenId id, String text) {
        String messagePrefix = messagePrefix(message);
        TestCase.assertTrue(messagePrefix + "No next token available", ts.moveNext());
        assertTokenEquals(message, ts, id, text, -1);
    }
    
    /**
     * Compare contents of the given token sequences by moving through all their
     * tokens.
     * <br/>
     * Token hierarchies are given to check implementations
     * of the Token.offset(TokenHierarchy) - useful for checking of token snapshots.
     *
     * @param message message to display (may be null).
     * @param expected non-null token sequence to be compared to the other token sequence.
     * @param expectedHi token hierarchy to which expected relates.
     * @param actual non-null token sequence to be compared to the other token sequence.
     * @param actualHi token hierarchy to which actual relates.
     * @param testLookaheadAndState whether lookahead and states should be checked
     *  or not. Generally it should be true but for snapshots checking it must
     *  be false because snapshots do not hold lookaheads and states.
     */
    public static void assertTokenSequencesEqual(String message,
    TokenSequence<?> expected, TokenHierarchy<?> expectedHi,
    TokenSequence<?> actual, TokenHierarchy<?> actualHi,
    boolean testLookaheadAndState, boolean dumpWholeHi) {
        String prefix = messagePrefix(message);
        TestCase.assertEquals(prefix + "Move previous: ", expected.movePrevious(), actual.movePrevious());
        int i = 0;
        while (expected.moveNext()) {
            String prefixI = prefix + "->[" + i + "]";
            TestCase.assertTrue(prefixI + ": Cannot moveNext() in test token sequence", actual.moveNext());
            assertTokensEqual(prefixI, expected, expectedHi, actual, actualHi, testLookaheadAndState);
            i++;
        }
        TestCase.assertFalse(prefix + "moveNext() possible at end of test token sequence", actual.moveNext());
    }

    private static void assertTokensEqual(String message,
    TokenSequence<?> ts, TokenHierarchy tokenHierarchy,
    TokenSequence<?> ts2, TokenHierarchy tokenHierarchy2, boolean testLookaheadAndState) {
        Token<?> t = ts.token();
        Token<?> t2 = ts2.token();

        message = messagePrefix(message);
        TestCase.assertEquals(message + "Invalid token id", t.id(), t2.id());
        assertTextEquals(message + "Invalid token text", t.text(), t2.text());
        
        assertTokenOffsetsEqual(message, t.offset(tokenHierarchy), t2.offset(tokenHierarchy2));
        TestCase.assertEquals(message + "Invalid tokenSequence offset", ts.offset(), ts2.offset());

        // Checking LOOKAHEAD and STATE matching in case they are filled in (during tests)
        if (TokenList.LOG.isLoggable(Level.FINE) && testLookaheadAndState) {
            TestCase.assertEquals(message + "Invalid token.lookahead()", lookahead(ts), lookahead(ts2));
            TestCase.assertEquals(message + "Invalid token.state()", state(ts), state(ts2));
        }
        TestCase.assertEquals(message + "Invalid token length", t.length(), t2.length());
        TestCase.assertEquals(message + "Invalid token part", t.partType(), t2.partType());
    }
    
    /**
     * Compute number of flyweight tokens in the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return number of flyweight tokens in the token sequence.
     */
    public static int flyweightTokenCount(TokenSequence<?> ts) {
        int flyTokenCount = 0;
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                flyTokenCount++;
            }
        }
        return flyTokenCount;
    }
    
    /**
     * Compute total number of characters represented by flyweight tokens
     * in the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return number of characters contained in the flyweight tokens
     *  in the token sequence.
     */
    public static int flyweightTextLength(TokenSequence<?> ts) {
        int flyTokenTextLength = 0;
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                flyTokenTextLength += ts.token().text().length();
            }
        }
        return flyTokenTextLength;
    }
    
    /**
     * Compute distribution of flyweight token lengths accross the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return non-null list containing number of the flyweight tokens that have the length
     *  equal to the index in the list.
     */
    public static List<Integer> flyweightDistribution(TokenSequence<?> ts) {
        List<Integer> distribution = new ArrayList<Integer>();
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                int len = ts.token().text().length();
                while (distribution.size() <= len) {
                    distribution.add(0);
                }
                distribution.set(len, distribution.get(len) + 1);
            }
        }
        return distribution;
    }
    
    public static boolean collectionsEqual(Collection<?> c1, Collection<?> c2) {
        return c1.containsAll(c2) && c2.containsAll(c1);
    }
    
    public static void assertCollectionsEqual(Collection expected, Collection actual) {
        assertCollectionsEqual(null, expected, actual);
    }

    public static void assertCollectionsEqual(String message, Collection expected, Collection actual) {
        if (!collectionsEqual(expected, actual)) {
            message = messagePrefix(message);
            for (Iterator it = expected.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!actual.contains(o)) {
                    System.err.println(actual.toString());
                    TestCase.fail(message + " Object " + o + " not contained in tested collection");
                }
            }
            for (Iterator it = actual.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!expected.contains(o)) {
                    System.err.println(actual.toString());
                    TestCase.fail(message + " Extra object " + o + " contained in tested collection");
                }
            }
            TestCase.fail("Collections not equal for unknown reason!");
        }
    }
    
    public static void incCheck(Document doc, boolean nested) {
        TokenHierarchy<?> incHi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            assertConsistency(incHi);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        Language<?> language = (Language<?>)
                doc.getProperty(Language.class);
        String docText = null;
        try {
            // Get the text including the extra newline since DocumentUtilities.getText(doc)
            // returns it so it gets lexed
            docText = doc.getText(0, doc.getLength() + 1);
        } catch (BadLocationException e) {
            e.printStackTrace();
            TestCase.fail("BadLocationException occurred");
        }
        
        TokenHierarchy<?> batchHi;
        ((AbstractDocument)doc).readLock();
        try {
            batchHi = TokenHierarchy.create(docText, language);
            TokenSequence<?> batchTS = batchHi.tokenSequence();
            TokenSequence<?> incTS = incHi.tokenSequence();
            try {
                // Compare lookaheads and states as well
                assertTokenSequencesEqual("TOP", batchTS, batchHi, incTS, incHi, true, false);
            } catch (Throwable t) {
                // Go forward two tokens to have an extra tokens context
                batchTS.moveNext();
                batchTS.moveNext();
                StringBuilder sb = new StringBuilder(512);
                sb.append("BATCH token sequence dump:\n").append(batchTS);
                sb.append("\n\nTEST token sequence dump:\n").append(incTS);
                TokenHierarchy<?> lastHi = (TokenHierarchy<?>)doc.getProperty(LAST_TOKEN_HIERARCHY);
                if (lastHi != null) {
    //                    System.err.println("PREVIOUS batch token sequence dump:\n" + lastHi.tokenSequence());
                }
                throw new IllegalStateException(sb.toString(), t);
            }

            if (nested) {
                batchTS.moveStart();
                incTS.moveStart();
                try {
                    incCheckNested("TOP", doc, batchTS, batchHi, incTS, incHi);
                } catch (Throwable t) { // Re-throw with hierarchy info
                    StringBuilder sb = new StringBuilder(512);
                    sb.append("\n\n\nERROR in HIERARCHY!!!!!!!!\n");
                    sb.append(t.toString());
                    sb.append("\n\nBATCH token hierarchy:\n").append(batchHi);
                    sb.append("\n\n\n\nTEST token hierarchy:\n").append(incHi);
                    t.printStackTrace();
                    throw new IllegalStateException(sb.toString(), t);
                }
            }

            // Check the change since last modification
            TokenHierarchy<?> lastHi = (TokenHierarchy<?>)doc.getProperty(LAST_TOKEN_HIERARCHY);
            if (lastHi != null) {
                // TODO comparison
            }
            doc.putProperty(LAST_TOKEN_HIERARCHY, batchHi); // new last batch token hierarchy

            // Do another check since some TLLs may be created during embedded TS checking
            assertConsistency(incHi);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public static void incCheckNested(String message, Document doc,
            TokenSequence<?> batch, TokenHierarchy<?> batchTH,
            TokenSequence<?> inc, TokenHierarchy<?> incTH
    ) {
        int i = 0;
        while (inc.moveNext()) {
            TestCase.assertTrue("No more tokens in batch token sequence", batch.moveNext());
            TokenSequence<?> batchE = batch.embedded();
            TokenSequence<?> incE = inc.embedded();
            String messageE = message + "->[" + i + "]";
            if (incE != null) {
                TestCase.assertNotNull("Batch embedded sequence is null", batchE);
                assertTokenSequencesEqual(messageE, batchE, batchTH, incE, incTH, true, true);
                incE.moveStart();
                batchE.moveStart();

                incCheckNested(messageE, doc, batchE, batchTH, incE, incTH);
            } else { // Inc embedded is null
                TestCase.assertNull("Batch embedded sequence non-null", batchE);
            }
            i++;
        }
    }

    /**
     * Get lookahead for the token to which the token sequence is positioned.
     * <br/>
     * The method uses reflection to get reference to tokenList field in token sequence.
     */
    public static int lookahead(TokenSequence<?> ts) {
        return tokenList(ts).lookahead(ts.index());
    }

        /**
     * Get state for the token to which the token sequence is positioned.
     * <br/>
     * The method uses reflection to get reference to tokenList field in token sequence.
     */
    public static Object state(TokenSequence<?> ts) {
        return tokenList(ts).state(ts.index());
    }

    /**
     * Compare whether the two character sequences represent the same text.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        return TokenUtilities.equals(text1, text2);
    }
    
    public static void assertTextEquals(CharSequence expected, CharSequence actual) {
        assertTextEquals(null, expected, actual);
    }
    
    public static void assertTextEquals(String message, CharSequence expected, CharSequence actual) {
        if (!textEquals(expected, actual)) {
            TestCase.fail(messagePrefix(message) +
                " expected:\"" + expected + "\" but was:\"" + actual + "\"");
        }
    }
    
    /**
     * Return the given text as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text.
     */
    public static String debugText(CharSequence text) {
        return TokenUtilities.debugText(text);
    }
    
    /**
     * Return the given text as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text or "<null>".
     */
    public static String debugTextOrNull(CharSequence text) {
        return (text != null) ? debugText(text) : "<null>";
    }

    public static void initLastDocumentEventListening(Document doc) {
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            public void removeUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            public void changedUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            private void storeEvent(DocumentEvent evt) {
                evt.getDocument().putProperty(DocumentEvent.class, evt);
            }
        });
    }
    
    public static DocumentEvent getLastDocumentEvent(Document doc) {
        return (DocumentEvent)doc.getProperty(DocumentEvent.class);
    }
    
    public static void initLastTokenHierarchyEventListening(Document doc) {
        TokenHierarchy hi = TokenHierarchy.get(doc);
        hi.addTokenHierarchyListener(TestTokenChangeListener.INSTANCE);
    }
    
    public static TokenHierarchyEvent getLastTokenHierarchyEvent(Document doc) {
        return (TokenHierarchyEvent)doc.getProperty(TokenHierarchyEvent.class);
    }
    
    /**
     * Get token list from the given token sequence for testing purposes.
     */
    public static <T extends TokenId> TokenList<T> tokenList(TokenSequence<T> ts) {
        try {
            if (tokenListField == null) {
                tokenListField = ts.getClass().getDeclaredField("tokenList");
                tokenListField.setAccessible(true);
            }
            @SuppressWarnings("unchecked")
            TokenList<T> tl = (TokenList<T>)tokenListField.get(ts);
            return tl;
        } catch (Exception e) {
            TestCase.fail(e.getMessage());
            return null; // never reached
        }
    }
    
    private static String messagePrefix(String message) {
        if (message != null) {
            message = message + ": ";
        } else {
            message = "";
        }
        return message;
    }

    /**
     * Set whether the lexer should run in testing mode where there are some
     * additional correctness checks performed.
     */
    public static void setTesting(boolean testing) {
        TokenList.LOG.setLevel(testing ? Level.FINE : Level.INFO);
    }
    
    /**
     * Check whether token descriptions dump file (a file with added suffix ".tokens.txt")
     * for the given input file exists and whether it has the same content
     * like the one obtained by lexing the input file.
     * <br/>
     * It allows to test whether the tested lexer still produces the same tokens.
     * <br/>
     * The method will only pass successfully if both the input file and token descriptions
     * files exist and the token descriptions file contains the same information
     * as the generated files.
     * <br/>
     * If the token descriptions file does not exist the method will create it.
     * <br/>
     * As the lexer's behavior at the EOF is important and should be well tested
     * there is a support for splitting input file virtually into multiple inputs
     * by virtual EOF - see <code>TokenDumpTokenId</code> for details.
     * <br/>
     * Also there is possibility to specify special chars
     * - see <code>TokenDumpTokenId</code> for details.
     *
     * @param test non-null test (used for calling test.getDataDir()).
     * @param relFilePath non-null file path relative to datadir of the test.
     *  <br/>
     *  For example if "testfiles/testinput.mylang.txt" gets passed the test method will
     *  search for <code>new File(test.getDataDir() + "testfiles/testinput.mylang.txt")</code>,
     *  read its content, lex it and create token descriptions. Then it will search for 
     *  <code>new File(test.getDataDir() + "testfiles/testinput.mylang.txt.tokens.txt")</code>
     *  and it will compare the file content with the generated descriptions.
     *  
     */
    public static void checkTokenDump(NbTestCase test, String relFilePath,
    Language<?> language) throws Exception {
        TokenDumpCheck.checkTokenDump(test, relFilePath, language);
    }
    
    private static final class TestTokenChangeListener implements TokenHierarchyListener {
        
        static TestTokenChangeListener INSTANCE = new TestTokenChangeListener();
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            TokenHierarchy hi = evt.tokenHierarchy();
            Document d = (Document)hi.inputSource();
            d.putProperty(TokenHierarchyEvent.class, evt);
        }
        
    }
}
