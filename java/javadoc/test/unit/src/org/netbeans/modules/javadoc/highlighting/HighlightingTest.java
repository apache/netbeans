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
package org.netbeans.modules.javadoc.highlighting;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import static junit.framework.TestCase.fail;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

public class HighlightingTest extends NbTestCase {

    public HighlightingTest(String name) {
        super(name);
    }

    public void testHighlights() throws Exception {
        String content
                = "/**the 1st line.\n"
                + " *\n"
                + " * the 3rd line\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 16});
    }

    public void testHighlightsWithMultiSentences_01() throws Exception {
        String content
                = "/**the 1st line. multiple sentences.\n"
                + " *\n"
                + " * the 3rd line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 16});
    }

    public void testHighlightsWithMultiSentences_02() throws Exception {
        String content
                = "/**the 1st line.multiple sentences.\n"
                + " *\n"
                + " * the 3rd line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 35});
    }

    public void testHighlightsOnMultiLines_01() throws Exception {
        String content
                = "/**\n"
                + " * the 2nd line.\n"
                + " * the 3rd line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 4, 7, 20});
    }

    public void testHighlightsOnMultiLines_02() throws Exception {
        String content
                = "/**the 1st line\n"
                + " * the 2nd line.\n"
                + " * the 3rd line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 16, 19, 32});
    }

    public void testHighlightsOnMultiLines_03() throws Exception {
        String content
                = "/**the first line\n"
                + " *\n"
                + " * the 3rd line.\n"
                + " * the 4th line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 18, 20, 21, 24, 37});
    }

    public void testHighlightsWithMultiPeriods() throws Exception {
        String content
                = "/**the 1st line...\n"
                + " *\n"
                + " * the 3rd line.\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 18});
    }

    // Japanese
    public void testHighlightsForJapanese() throws Exception {
        String content
                = "/**1行目。\n"
                + " *\n"
                + " * 3行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 7});
    }

    public void testHighlightsForJapaneseWithMultiSentences() throws Exception {
        String content
                = "/**1行目。複数の文。\n"
                + " *\n"
                + " * 3行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 7});
    }

    public void testHighlightsForJapaneseOnMultiLines_01() throws Exception {
        String content
                = "/**\n"
                + " * 2行目。\n"
                + " * 3行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 4, 7, 11});
    }

    public void testHighlightsForJapaneseOnMultiLines_02() throws Exception {
        String content
                = "/**1行目\n"
                + " * 2行目。\n"
                + " * 3行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 7, 10, 14});
    }

    public void testHighlightsForJapaneseOnMultiLines_03() throws Exception {
        String content
                = "/**1行目\n"
                + " *\n"
                + " * 3行目。\n"
                + " * 4行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 7, 9, 10, 13, 17});
    }

    public void testHighlightsForJapaneseWithMultiPeriods() throws Exception {
        String content
                = "/**1行目。。。\n"
                + " *\n"
                + " * 3行目。\n"
                + " * @author junichi11\n"
                + " */";
        checkHighlights(content, new int[]{3, 9});
    }

    public void testHighlightsOnMultiLinesMarkdown() throws Exception {
        String content
                = """
                  /// the 1st line
                  /// the 2nd line.
                  /// the 3rd line.
                  /// @author junichi11
                  ///""";
        checkHighlights(content, new int[]{4, 17, 21, 34});
    }

    /**
     * Check highlight ranges.
     *
     * @param content Javadoc
     * @param offsetRanges start and end offset ranges
     */
    private void checkHighlights(String content, int[] offsetRanges) {
        Document document = createDocument(content);
        ((AbstractDocument) document).readLock();
        try {
            Highlighting highlighting = new Highlighting(document);
            HighlightsSequence hs = highlighting.getHighlights(0, document.getLength());
            assertEquals(0, offsetRanges.length % 2);
            assertTrue(hs.moveNext());
            for (int i = 0; i < offsetRanges.length; i += 2) {
                assertEquals(offsetRanges[i], hs.getStartOffset());
                assertEquals(offsetRanges[i + 1], hs.getEndOffset());
                if (!hs.moveNext()) {
                    assertEquals(offsetRanges.length, i + 2);
                    break;
                }
            }
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    private Document createDocument(String text) {
        try {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.putProperty(Language.class, JavaTokenId.language());
            doc.insertString(0, text, SimpleAttributeSet.EMPTY);
            return doc;
        } catch (BadLocationException e) {
            fail(e.getMessage());
        }
        return null;
    }
}
