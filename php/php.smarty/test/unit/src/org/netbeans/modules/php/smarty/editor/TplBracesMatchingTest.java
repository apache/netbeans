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
package org.netbeans.modules.php.smarty.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Based on Marek's HtmlMatcherTest.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplBracesMatchingTest extends TplTestBase {

    private static final TplBracesMatching MATCHER_FACTORY;

    static {
        MATCHER_FACTORY = new TplBracesMatching();
        TplBracesMatching.setTestMode(true);
    }
    private Document document;

    public TplBracesMatchingTest(String name) {
        super(name);
    }

    public void testCreateMatcher() throws BadLocationException {
        setDocumentText(""); //init document
        createMatcher(0, false, 1);
        createMatcher(0, true, 1);
    }

    public void testMatchingOnEmptyFile() throws Exception {
        setDocumentText(" ");
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());
    }

    public void testNoOrigin() throws Exception {
        setDocumentText("{if}  {while} nazdar {/while} {/if}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(5, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());

        matcher = createMatcher(20, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());
    }

    public void testForward() throws Exception {
        setDocumentText("{if}  {while} nazdar {/while} {/if}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertOrigin(0, 4, matcher);
        assertMatch(30, 34, 34, 35, matcher);

        matcher = createMatcher(7, false, 1);
        assertOrigin(6, 13, matcher);
        assertMatch(21, 28, 28, 29, matcher);

    }

    public void testBackward() throws Exception {
        setDocumentText("{if}  {while} nazdar {/while} {/if}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(22, false, 1);
        assertOrigin(21, 29, matcher);
        assertMatch(6, 12, 12, 13, matcher);

        matcher = createMatcher(31, false, 1);
        assertOrigin(30, 35, matcher);
        assertMatch(0, 3, 3, 4, matcher);

    }

    public void testBoundaries() throws Exception {
        setDocumentText("{if}{while}{/while}{/if}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3

        //forward search
        BracesMatcher matcher = createMatcher(19, false, 1);
        assertOrigin(19, 24, matcher);
        assertMatch(0, 3, 3, 4, matcher);

        matcher = createMatcher(11, false, 1);
        assertOrigin(11, 19, matcher);
        assertMatch(4, 10, 10, 11, matcher);

        //backward search
        matcher = createMatcher(19, true, 1);
        assertOrigin(11, 19, matcher);
        assertMatch(4, 10, 10, 11, matcher);

        matcher = createMatcher(11, true, 1);
        assertOrigin(4, 11, matcher);
        assertMatch(11, 18, 18, 19, matcher);
    }

    public void testNoMatch() throws Exception {
        setDocumentText("{if}{section}{/capture}{/if}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(14, false, 1);
        assertOrigin(13, 23, matcher);
        assertNull(matcher.findMatches()); // parser issues

        matcher = createMatcher(5, false, 1);
        assertOrigin(4, 13, matcher);
        assertNull(matcher.findMatches()); // parser issues
    }

    public void testMatchSingleTag() throws Exception {
        setDocumentText("{eval} {assign var=var value=value} {include file=\"myfile.tpl\"}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(2, false, 1);
        assertOrigin(0, 6, matcher);
        assertMatch(2, 2, matcher);

        matcher = createMatcher(8, false, 1);
        assertOrigin(7, 35, matcher);
        assertMatch(8, 8, matcher);
    }

    public void testMatchSimpleTag() throws Exception {
        setDocumentText("{$var} {$anotherVar}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(2, false, 1);
        assertOrigin(0, 6, matcher);
        assertMatch(2, 2, matcher);

        matcher = createMatcher(19, false, 1);
        assertOrigin(7, 20, matcher);
        assertMatch(19, 19, matcher);
    }

    public void testCommentTag() throws Exception {
        setDocumentText("{* any comment *}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(5, false, 1);
        assertAllOrigins(new int[]{0, 17, 0, 2, 15, 17}, matcher);
        assertMatch(5, 5, matcher);
    }

    public void testCommentTagEnd() throws Exception {
        setDocumentText("{* any comment *}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(17, false, 1);
        assertAllOrigins(new int[]{0, 17, 0, 2, 15, 17}, matcher);
        assertMatch(17, 17, matcher);
    }

    public void testCommentTagBegin1() throws Exception {
        setDocumentText("{* comment *}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertAllOrigins(new int[]{0, 13, 0, 2, 11, 13}, matcher);
        assertMatch(0, 0, matcher);
    }

    public void testCommentTagBegin2() throws Exception {
        setDocumentText("{* comment *}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertAllOrigins(new int[]{0, 13, 0, 2, 11, 13}, matcher);
        assertMatch(1, 1, matcher);
    }

    public void testCommentTagBegin3() throws Exception {
        setDocumentText(" {* comment *}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertAllOrigins(new int[]{1, 14, 1, 3, 12, 14}, matcher);
        assertMatch(1, 1, matcher);
    }

    public void testUnfinishedTag() throws Exception {
        setDocumentText("{writing");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(7, false, 1);
        assertNull(matcher.findOrigin());
        assertMatch(7, 7, matcher);
    }

    public void testMoreMatchedTag() throws Exception {
        setDocumentText("{if}{else}{/if}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(3, false, 1);
        assertOrigin(0, 4, matcher);
        assertMatch(new int []{4, 9, 9, 10, 10, 14, 14, 15}, matcher);
    }

    //--------------------------------------------------------------------------
    private void assertOrigin(int expectedStart, int expectedEnd, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] origin = matcher.findOrigin();
        assertNotNull(origin);
        assertEquals("Incorrect origin block start:", expectedStart, origin[0]);
        assertEquals("Incorrect origin block end:", expectedEnd, origin[1]);
    }

    private void assertAllOrigins(int[] origins, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] foundOrigin = matcher.findOrigin();
        assertNotNull(foundOrigin);
        for (int i = 0; i < foundOrigin.length; i++) {
            assertEquals("Incorrect origin block:", origins[i], foundOrigin[i]);
        }
    }

    private void assertMatch(int expectedStart, int expectedEnd, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] match = matcher.findMatches();
        assertNotNull(match);
        assertEquals("Incorrect match block start:", expectedStart, match[0]);
        assertEquals("Incorrect match block end:", expectedEnd, match[1]);
    }

    private void assertMatch(int expectedStart1, int expectedEnd1, int expectedStart2, int expectedEnd2, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] match = matcher.findMatches();
        assertNotNull(match);
        assertEquals("Incorrect match block start:", expectedStart1, match[0]);
        assertEquals("Incorrect match block end:", expectedEnd1, match[1]);
        if (expectedStart2 != -1) {
            assertEquals("Incorrect match block start:", expectedStart2, match[2]);
        }
        if (expectedEnd2 != -1) {
            assertEquals("Incorrect match block end:", expectedEnd2, match[3]);
        }
    }

    private void assertMatch(int[] expected, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] match = matcher.findMatches();
        assertNotNull(match);
        for (int i = 0; i < match.length; i++) {
            assertEquals(expected[i], match[i]);
            
        }
    }

    private BracesMatcher createMatcher(int offset, boolean searchBackward, int lookahead) {
        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(document, offset, searchBackward, lookahead);
        BracesMatcher matcher = MATCHER_FACTORY.createMatcher(context);

        assertNotNull(matcher);

        return matcher;
    }

    private void setDocumentText(String text) throws BadLocationException {
        document = createDocument();
        document.remove(0, document.getLength());
        document.insertString(0, text, null);
    }

    private BaseDocument createDocument() {
        return getDocument("");
    }
}
