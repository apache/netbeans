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
package org.netbeans.modules.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.TextDetail.DetailNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author jaras
 */
public class TextDetailTest extends NbTestCase {

    private static final String ELLIPSIS = "...";

    public TextDetailTest(String name) {
        super(name);
    }

    public void testMatchAtTheBeginingOfLine() {
        String match = "mockMatchTest";
        String line = createLongString('a', match, 'b', 0, 600);
        String htmlName = createHtmlDisplayName(line, match);
        int remaining = ((TextDetail.DetailNode.DETAIL_DISPLAY_LENGTH
                - match.length()) / 4) * 4; // follow rounding in the algorithm
        assertTrue(htmlName.startsWith("<b>" + match + "</b>"));
        assertEquals('b', htmlName.charAt(match.length() + "<b></b>".length()));
        assertEquals('b', htmlName.charAt(match.length() + "<b></b>".length() + remaining - 1));
        assertEquals('.', htmlName.charAt(match.length() + "<b></b>".length() + remaining));
        assertTrue(htmlName.endsWith(ELLIPSIS));
    }

    public void testMatchAtTheEndOfLine() {
        String match = "mockMatchTest";
        String line = createLongString('a', match, 'b', 600, 0);
        String htmlName = createHtmlDisplayName(line, match);
        int remaining = ((TextDetail.DetailNode.DETAIL_DISPLAY_LENGTH
                - match.length()) / 4) * 4; // follow rounding in the algorithm
        assertTrue(htmlName.endsWith("<b>" + match + "</b>"));
        assertEquals('a', htmlName.charAt(ELLIPSIS.length()));
        assertEquals('a', htmlName.charAt(ELLIPSIS.length() + remaining - 1));
        assertEquals('<', htmlName.charAt(ELLIPSIS.length() + remaining));
        assertTrue(htmlName.startsWith(ELLIPSIS));
    }

    public void testMatchInTheMiddleOfLongLine() {
        String match = "mockMatchTest";
        String line = createLongString('a', match, 'b', 300, 300);
        String htmlName = createHtmlDisplayName(line, match);
        int remaining = TextDetail.DetailNode.DETAIL_DISPLAY_LENGTH
                - match.length();
        int quarter = remaining / 4;
        assertTrue(htmlName.startsWith(ELLIPSIS));
        assertEquals(
                "<b>" + match + "</b>",
                htmlName.substring(ELLIPSIS.length() + quarter,
                ELLIPSIS.length() + quarter + match.length()
                + "<b></b>".length()));
        assertEquals('a', htmlName.charAt(3));
        assertEquals('a', htmlName.charAt(ELLIPSIS.length() + quarter - 1));
        assertEquals('b', htmlName.charAt(ELLIPSIS.length() + quarter
                + match.length() + "<b></b>".length()));
        assertEquals('b', htmlName.charAt(ELLIPSIS.length()
                + match.length() + "<b></b>".length() + 4 * quarter - 1));
        assertEquals('.', htmlName.charAt(ELLIPSIS.length()
                + match.length() + "<b></b>".length() + 4 * quarter));
        assertTrue(htmlName.endsWith(ELLIPSIS));
    }

    public void testMatchOfVeryLongString() {
        String match = createLongString('M', "", ' ', 600, 0);
        String line = createLongString('a', match, 'b', 100, 100);
        String htmlName = createHtmlDisplayName(line, match);
        assertTrue(htmlName.startsWith(ELLIPSIS + "<b>"));
        assertTrue(htmlName.endsWith("</b>" + ELLIPSIS));
        assertTrue(htmlName.length()
                <= TextDetail.DetailNode.DETAIL_DISPLAY_LENGTH
                + ELLIPSIS.length() * 3 + "<b></b>".length() * 2);
        assertTrue(htmlName.contains("</b>" + ELLIPSIS + "<b>"));
    }

    public void testMatchOfWholeVeryLongLine() {
        String line = createLongString('M', "inner", 'N', 300, 300);
        String htmlName = createHtmlDisplayName(line, line);
        assertTrue(htmlName.startsWith("<b>"));
        assertTrue(htmlName.endsWith("</b>"));
        assertTrue(htmlName.length()
                <= TextDetail.DetailNode.DETAIL_DISPLAY_LENGTH
                + ELLIPSIS.length() + "<b></b>".length() * 2);
        assertTrue(htmlName.contains("</b>" + ELLIPSIS + "<b>"));
    }

    public void testTrimLeadingWhitespace() {
        String htmlName = createHtmlDisplayName("   public", "public");
        assertEquals("<b>public</b>", htmlName);
    }

    public void testTrimLeadingWhitespace2() {
        String line = createLongString(' ', "inner", ' ', 500, 0);
        String htmlName = createHtmlDisplayName(line, "inner");
        assertEquals("<b>inner</b>", htmlName);
    }

    public void testTrimLeadingWhitespace3() {
        String line = "    " + createLongString('X', "inner", ' ', 500, 0);
        String htmlName = createHtmlDisplayName(line, "inner");
        assertTrue(htmlName.startsWith("...X"));
    }

    public void testMatchOfHtmlContent() {
        String htmlName = createHtmlDisplayName("a<b>BOLD</b>c", "<b>BOLD</b>");
        assertEquals("a<b>&lt;b>BOLD&lt;/b></b>c", htmlName);
    }

    public String createHtmlDisplayName(String line, String match) {
        TextDetail td = createMockTextDetail(line, match);
        DetailNode detailNode = new TextDetail.DetailNode(td, false, null);
        String htmlDisplayName = detailNode.getHtmlDisplayName();
        Pattern p = Pattern.compile("(<html><font .*>\\s*\\d+: </font>)(.*?)(\\s+<font .*>\\[.*\\]</font></html>)");
        Matcher m = p.matcher(htmlDisplayName);
        assertTrue(m.find());
        return m.group(2);
    }

    /**
     * Test for bug 243645 - OutOfMemoryError: Java heap space (OOME due do long
     * tooltips in Search Results window).
     */
    public void testLongDescriptionIsAcceptablyLong() {
        String longString = createLongString('a', "match", 'b', 1000,
                1000);
        TextDetail td = createMockTextDetail(longString, "match");
        td.addSurroundingLine(2, longString);
        td.addSurroundingLine(3, longString);
        td.addSurroundingLine(4, longString);
        td.addSurroundingLine(5, longString);
        DetailNode n = new DetailNode(td, false, null);
        assertTrue(n.getShortDescription().length()
                <= 5 * 240 + "<html></html>".length() + 5 * "<br/>".length()
                + "<b></b>".length() + 5 * "...".length());
    }

    private TextDetail createMockTextDetail(String line, String match) {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        try {
            FileObject fo = fs.getRoot().createData("mockMatch.txt");
            SearchPattern sp = SearchPattern.create(match, false, false, false);
            DataObject dob = DataObject.find(fo);
            TextDetail td = new TextDetail(dob, sp);
            td.setLine(1);
            int col0 = line.indexOf(match) + 1;
            td.setColumn(col0);
            td.setMatchedText(match);
            td.setMarkLength(match.length());
            td.setStartOffset(col0);
            td.setEndOffset(col0 + match.length());
            td.setLineText(line);
            return td;
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    /**
     * Create a mock line. The format of the line is: {prefix *
     * prefixLenght}{match}{suffix * suffixLenght}. Example:
     * aaaaaaThisIsMatchedTextbbbbbbbb.
     */
    private String createLongString(char prefix, String match, char suffix,
            int prefixLenght, int suffixLenght) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefixLenght; i++) {
            sb.append(prefix);
        }
        sb.append(match);
        for (int i = 0; i < suffixLenght; i++) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    public void testOptimizeLineText() {
        assertEquals(
                "Hel????ver?????",
                TextDetail.optimizeText("Hello everybody", 3, 7, 10).toString());
        assertEquals(
                "Hel????????????",
                TextDetail.optimizeText("Hello everybody", 3, 0, 2).toString());
        assertEquals(
                "Hello ?????????",
                TextDetail.optimizeText("Hello everybody", 3, 2, 6).toString());

        assertEquals(
                "ver",
                TextDetail.optimizeText("Hello everybody", 3, 7, 10)
                .subSequence(7, 10).toString());
        assertEquals(
                "ver",
                TextDetail.optimizeText("Hello everybody", 3, 7, 10)
                .subSequence(7, 10).toString());
        assertEquals(
                "Hel",
                TextDetail.optimizeText("Hello everybody", 3, 7, 10)
                .subSequence(0, 3).toString());
        assertEquals(
                "l????v",
                TextDetail.optimizeText("Hello everybody", 3, 7, 10)
                .subSequence(2, 8).toString());
    }
}
