/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class SyntaxHighlightingTest extends NbTestCase {
    
    public static void main(String... args) {
        TestRunner.run(SyntaxHighlightingTest.class);
    }
    
    /** Creates a new instance of SyntaxHighlightingTest */
    public SyntaxHighlightingTest(String name) {
        super(name);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimple");
//        includes.add("testNoPrologEpilogEmbedding");
//        includes.add("testEmbedded");
//        includes.add("testRanges");
//        filterTests(includes);
    }

    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
        setFilter(filter);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        
        suite.addTest(new SyntaxHighlightingTest("testSimple"));
        suite.addTest(new SyntaxHighlightingTest("testEmbedded"));
        suite.addTest(new SyntaxHighlightingTest("testComplex"));
        suite.addTest(new SyntaxHighlightingTest("testNoPrologEpilogEmbedding"));
        suite.addTest(new SyntaxHighlightingTest("testConcurrentModifications"));
        suite.addTest(new SyntaxHighlightingTest("testEvents"));
        suite.addTest(new SyntaxHighlightingTest("testRanges"));
        suite.addTest(new SyntaxHighlightingTest("testEmbeddedRanges"));
        
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        SyntaxHighlighting.TEST_FALLBACK_COLORING = new SimpleAttributeSet();
    }
    
    public void testSimple() {
        checkText("+ - / * public", TestTokenId.language());
    }

    public void testEmbedded() {
        checkText("/**//* this is a comment */", TestTokenId.language());
    }

    public void testComplex() {
        checkText(
            "public       /**/ +/-  private /** hello */ something /* this is a comment */ \"hi hi hi\" xyz    ",
            TestTokenId.language());
    }

    public void testNoPrologEpilogEmbedding() {
        checkText(
            "hello world 0-1-2-3-4-5-6-7-8-9-A-B-C-D-E-F      Ooops",
            TestPlainTokenId.language());
    }

    public void testConcurrentModifications() throws BadLocationException {
        Document doc = createDocument(TestTokenId.language(), "NetBeans NetBeans NetBeans");
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            assertFalse("There should be no highlights after co-modification", hs.moveNext());
        }
    }

    public void testEvents() throws BadLocationException {
        final String text = "Hello !";
        Document doc = createDocument(TestTokenId.language(), text);
        assertTrue("TokenHierarchy should be active", TokenHierarchy.get(doc).isActive());

        SyntaxHighlighting layer = new SyntaxHighlighting(doc);
        L listener = new L();
        layer.addHighlightsChangeListener(listener);

        assertHighlights(
            text,
            TokenHierarchy.create(text, TestTokenId.language()).tokenSequence(),
            layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE),
            ""
        );

        assertEquals("There should be no events", 0, listener.eventsCnt);

        final String addedText = "World";
        doc.insertString(6, addedText, SimpleAttributeSet.EMPTY);

        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertTrue("Wrong change start offset", 6 >= listener.lastStartOffset);
        assertTrue("Wrong change end offset", 6 + addedText.length() <= listener.lastEndOffset);
    }

    public void testRanges() {
        Document doc = createDocument(TestPlainTokenId.language(), "aaa   bbb   ccc");
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        HighlightsSequence hs = layer.getHighlights(1, 2);
        assertTrue("Highlight sequence should not be empty", hs.moveNext());
        assertEquals("Wrong start offset", 1, hs.getStartOffset());
        assertEquals("Wrong end offset", 2, hs.getEndOffset());
        assertFalse("There should only be one highlight", hs.moveNext());

        hs = layer.getHighlights(5, 11);
        checkHighlights("Wrong highlights", hs, new Integer [] { 5, 6, 6, 9, 9, 11 });
    }

    public void testEmbeddedRanges() {
        Document doc = createDocument(TestTokenId.language(), "public    /* word word word */ void");
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        HighlightsSequence hs = layer.getHighlights(19, 21);
        assertTrue("Highlight sequence should not be empty", hs.moveNext());
        assertEquals("Wrong start offset", 19, hs.getStartOffset());
        assertEquals("Wrong end offset", 21, hs.getEndOffset());
        assertFalse("There should only be one highlight", hs.moveNext());
    }

    private void checkHighlights(String message, HighlightsSequence seq, Integer... expected) {
        List<Integer> actual = new ArrayList<Integer>();
        while(seq.moveNext()) {
            actual.add(seq.getStartOffset());
            actual.add(seq.getEndOffset());
        }

        assertEquals(message, Arrays.asList(expected), actual);
    }

    private void checkText(String text, Language<?> lang) {
        System.out.println("Checking text: '" + text + "'\n");
        Document doc = createDocument(lang, text);
        text += '\n'; // Adding newline that gets added by AbstractDocument and gets lexed
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        TokenHierarchy<?> tokens = TokenHierarchy.create(text, lang);
        assertHighlights(text, tokens.tokenSequence(), hs, "");
        assertFalse("Unexpected highlights at the end of the sequence", hs.moveNext());
        System.out.println("------------------------\n");
    }
    
    private Document createDocument(Language lang, String text) {
        try {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.putProperty(Language.class, lang);
            doc.insertString(0, text, SimpleAttributeSet.EMPTY);
            return doc;
        } catch (BadLocationException e) {
            fail(e.getMessage());
            return null;
        }
    }
    
    private void assertHighlights(String text, TokenSequence<?> ts, HighlightsSequence hs, String indent) {
        int newlineOffset = Integer.MIN_VALUE;
        while (ts.moveNext()) {
            if (newlineOffset == Integer.MIN_VALUE) { // Init
                newlineOffset = Integer.MAX_VALUE;
                int tokenOffset = ts.offset();
                while (tokenOffset < text.length()) {
                    if (text.charAt(tokenOffset) == '\n') {
                        newlineOffset = tokenOffset;
                        break;
                    }
                    tokenOffset++;
                }
            }
            
            System.out.println(indent + "Token    : <" + 
                ts.offset() + ", " + 
                (ts.offset() + ts.token().length()) + ", '" + 
                ts.token().text() + "', " + 
                ts.token().id().name() + ">");
            
            TokenSequence<?> embeddedSeq = ts.embedded();
            int tokenOffset = ts.offset();
            int tokenEndOffset = tokenOffset + ts.token().length();
            int hiEOffset = tokenOffset;
            if (embeddedSeq == null) {
                int limitOffset = tokenEndOffset;
                do {
                    int hiSOffset = hiEOffset;
                    int nlState = 0; // No initial newlines
                    if (hiSOffset == newlineOffset) {
                        newlineOffset++;
                        hiSOffset = hiEOffset = newlineOffset;
                        nlState = 1; // Initial char is newline
                        while (nlState <= 2 && newlineOffset < text.length()) {
                            if (text.charAt(newlineOffset) == '\n') {
                                if (nlState == 1 && newlineOffset < limitOffset) {
                                    hiSOffset = hiEOffset = newlineOffset + 1;
                                } else {
                                    nlState = 3;
                                    break;
                                }
                            } else {
                                nlState = 2;
                            }
                            newlineOffset++;
                        }
                    }
                    if (nlState > 0) { // Found NL(s) at token begining
                        // hiEOffset already set
                    } else {
                        hiEOffset = Math.min(limitOffset, newlineOffset);
                    }
                    if (hiEOffset > hiSOffset) {
                        assertTrue("Cannot move highlight sequence to next highlight", hs.moveNext());
                        System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                        assertEquals("Wrong starting offset", hiSOffset, hs.getStartOffset());
                        assertEquals("Wrong ending offset", hiEOffset, hs.getEndOffset());
                    }
                } while (hiEOffset < limitOffset);
                // XXX: compare attributes as well
            } else {
                int prologueLength = embeddedPrologLength(ts, embeddedSeq);
                int epilogLength = embeddedEpilogLength(ts, embeddedSeq);
                
                if (prologueLength != -1 && epilogLength != -1) { // Tokens exist in embedded sequence
                    if (prologueLength > 0) {
                        int limitOffset = tokenOffset + prologueLength;
                        do {
                            int hiSOffset = hiEOffset;
                            int nlState = 0; // No initial newlines
                            if (hiSOffset == newlineOffset) {
                                newlineOffset++;
                                hiSOffset = hiEOffset = newlineOffset;
                                nlState = 1; // Initial char is newline
                                while (nlState <= 2 && newlineOffset < text.length()) {
                                    if (text.charAt(newlineOffset) == '\n') {
                                        if (nlState == 1 && newlineOffset < limitOffset) {
                                            hiSOffset = hiEOffset = newlineOffset + 1;
                                        } else {
                                            nlState = 3;
                                            break;
                                        }
                                    } else {
                                        nlState = 2;
                                    }
                                    newlineOffset++;
                                }
                            }
                            if (nlState > 0) { // Found NL(s) at token begining
                                // hiEOffset already set
                            } else {
                                hiEOffset = Math.min(limitOffset, newlineOffset);
                            }
                            if (hiEOffset > hiSOffset) {
                                assertTrue("Cannot move highlight sequence to next highlight", hs.moveNext());
                                System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                                assertEquals("Wrong starting offset", hiSOffset, hs.getStartOffset());
                                assertEquals("Wrong ending offset", hiEOffset, hs.getEndOffset());
                            }
                        } while (hiEOffset < limitOffset);
                        // XXX: compare attributes as well
                    }
                    
                    assertHighlights(text, ts.embedded(), hs, indent + "  ");
                    hiEOffset = tokenEndOffset - epilogLength;
                    if (epilogLength > 0) {
                        int limitOffset = tokenEndOffset;
                        do {
                            int hiSOffset = hiEOffset;
                            int nlState = 0; // No initial newlines
                            if (hiSOffset == newlineOffset) {
                                newlineOffset++;
                                hiSOffset = hiEOffset = newlineOffset;
                                nlState = 1; // Initial char is newline
                                while (nlState <= 2 && newlineOffset < text.length()) {
                                    if (text.charAt(newlineOffset) == '\n') {
                                        if (nlState == 1 && newlineOffset < limitOffset) {
                                            hiSOffset = hiEOffset = newlineOffset + 1;
                                        } else {
                                            nlState = 3;
                                            break;
                                        }
                                    } else {
                                        nlState = 2;
                                    }
                                    newlineOffset++;
                                }
                            }
                            if (nlState > 0) { // Found NL(s) at token begining
                                // hiEOffset already set
                            } else {
                                hiEOffset = Math.min(limitOffset, newlineOffset);
                            }
                            if (hiEOffset > hiSOffset) {
                                assertTrue("Cannot move highlight sequence to next highlight", hs.moveNext());
                                System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                                assertEquals("Wrong starting offset", hiSOffset, hs.getStartOffset());
                                assertEquals("Wrong ending offset", hiEOffset, hs.getEndOffset());
                            }
                        } while (hiEOffset < limitOffset);
                        // XXX: compare attributes as well
                    }
                } else { // No tokens in embeddedSeq
                    int limitOffset = tokenEndOffset;
                    do {
                        int hiSOffset = hiEOffset;
                        int nlState = 0; // No initial newlines
                        if (hiSOffset == newlineOffset) {
                            newlineOffset++;
                            hiSOffset = hiEOffset = newlineOffset;
                            nlState = 1; // Initial char is newline
                            while (nlState <= 2 && newlineOffset < text.length()) {
                                if (text.charAt(newlineOffset) == '\n') {
                                    if (nlState == 1 && newlineOffset < limitOffset) {
                                        hiSOffset = hiEOffset = newlineOffset + 1;
                                    } else {
                                        nlState = 3;
                                        break;
                                    }
                                } else {
                                    nlState = 2;
                                }
                                newlineOffset++;
                            }
                        }
                        if (nlState > 0) { // Found NL(s) at token begining
                            // hiEOffset already set
                        } else {
                            hiEOffset = Math.min(limitOffset, newlineOffset);
                        }
                        if (hiEOffset > hiSOffset) {
                            assertTrue("Cannot move highlight sequence to next highlight", hs.moveNext());
                            System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                            assertEquals("Wrong starting offset", hiSOffset, hs.getStartOffset());
                            assertEquals("Wrong ending offset", hiEOffset, hs.getEndOffset());
                        }
                    } while (hiEOffset < limitOffset);
                    // XXX: compare attributes as well
                }
            }
        }
    }
    
    private int embeddedPrologLength(
        TokenSequence<?> embeddingSeq, 
        TokenSequence<?> embeddedSeq) 
    {
        embeddedSeq.moveStart();
        if (embeddedSeq.moveNext()) {
            return embeddedSeq.offset() - embeddingSeq.offset();
        } else {
            return -1;
        }
    }
    
    private int embeddedEpilogLength(
        TokenSequence<?> embeddingSeq, 
        TokenSequence<?> embeddedSeq) 
    {
        embeddedSeq.moveEnd();
        if (embeddedSeq.movePrevious()) {
            return (embeddingSeq.offset() + embeddingSeq.token().length()) - (embeddedSeq.offset() + embeddedSeq.token().length());
        } else {
            return -1;
        }
    }

    private void dumpSequence(HighlightsSequence hs) {
        System.out.println("Dumping sequence: " + hs + " {");
        while(hs.moveNext()) {
            System.out.println("<" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
        }
        System.out.println("} End of sequence: " + hs + " dump ------------");
    }

    private static final class L implements HighlightsChangeListener {
        public int eventsCnt = 0;
        public int lastStartOffset;
        public int lastEndOffset;
        
        public void highlightChanged(HighlightsChangeEvent event) {
            eventsCnt++;
            lastStartOffset = event.getStartOffset();
            lastEndOffset = event.getEndOffset();
        }
    } // End of L class
}
