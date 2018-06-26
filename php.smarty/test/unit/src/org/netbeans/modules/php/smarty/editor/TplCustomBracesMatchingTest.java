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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Braces matching tests for tags with custom delimiters.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplCustomBracesMatchingTest extends TplTestBase {

    private static final TplBracesMatching MATCHER_FACTORY;

    static {
        MATCHER_FACTORY = new TplBracesMatching();
        TplBracesMatching.setTestMode(true);
    }
    private Document document;

    public TplCustomBracesMatchingTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupSmartyOptions("{[", "]}", SmartyFramework.Version.SMARTY3);
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
        setDocumentText("{[if]}  {[while]} nazdar {[/while]} {[/if]}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(7, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());

        matcher = createMatcher(20, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());
    }

    public void testForward() throws Exception {
        setDocumentText("{[if]}  {[while]} nazdar {[/while]} {[/if]}");
        //               01234567890123456789012345678901234567890123
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertOrigin(0, 6, matcher);
        assertMatch(36, 41, 41, 43, matcher);

        matcher = createMatcher(10, false, 1);
        assertOrigin(8, 17, matcher);
        assertMatch(25, 33, 33, 35, matcher);

    }

    public void testBackward() throws Exception {
        setDocumentText("{[if]}  {[while]} nazdar {[/while]} {[/if]}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(26, false, 1);
        assertOrigin(25, 35, matcher);
        assertMatch(8, 15, 15, 17, matcher);

        matcher = createMatcher(37, false, 1);
        assertOrigin(36, 43, matcher);
        assertMatch(0, 4, 4, 6, matcher);

    }

    public void testBoundaries() throws Exception {
        setDocumentText("{[if]}{[while]}{[/while]}{[/if]}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3

        //forward search
        BracesMatcher matcher = createMatcher(25, false, 1);
        assertOrigin(25, 32, matcher);
        assertMatch(0, 4, 4, 6, matcher);

        matcher = createMatcher(15, false, 1);
        assertOrigin(15, 25, matcher);
        assertMatch(6, 13, 13, 15, matcher);

        //backward search
        matcher = createMatcher(25, true, 1);
        assertOrigin(15, 25, matcher);
        assertMatch(6, 13, 13, 15, matcher);

        matcher = createMatcher(15, true, 1);
        assertOrigin(6, 15, matcher);
        assertMatch(15, 23, 23, 25, matcher);
    }

    public void testNoMatch() throws Exception {
        setDocumentText("{[if]}{[section]}{[/capture]}{[/if]}");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(19, false, 1);
        assertOrigin(17, 29, matcher);
        assertNull(matcher.findMatches()); // parser issues

        matcher = createMatcher(8, false, 1);
        assertOrigin(6, 17, matcher);
        assertNull(matcher.findMatches()); // parser issues
    }

    public void testMatchSingleTag() throws Exception {
        setDocumentText("{[eval]} {[assign var=var value=value]} {[include file=\"myfile.tpl\"]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(3, false, 1);
        assertOrigin(0, 8, matcher);
        assertMatch(3, 3, matcher);

        matcher = createMatcher(11, false, 1);
        assertOrigin(9, 39, matcher);
        assertMatch(11, 11, matcher);
    }

    public void testMatchSimpleTag() throws Exception {
        setDocumentText("{[$var]} {[$anotherVar]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(3, false, 1);
        assertOrigin(0, 8, matcher);
        assertMatch(3, 3, matcher);

        matcher = createMatcher(23, false, 1);
        assertOrigin(9, 24, matcher);
        assertMatch(23, 23, matcher);
    }

    public void testCommentTag() throws Exception {
        setDocumentText("{[* any comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(6, false, 1);
        assertOrigin(0, 19, matcher);
        assertMatch(6, 6, matcher);
    }

    public void testCommentTagEnd() throws Exception {
        setDocumentText("{[* any comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(19, false, 1);
        assertOrigin(0, 19, matcher);
        assertMatch(19, 19, matcher);
    }

    public void testCommentTagBegin1() throws Exception {
        setDocumentText("{[* comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertOrigin(0, 15, matcher);
        assertMatch(0, 0, matcher);
    }

    public void testCommentTagBegin2() throws Exception {
        setDocumentText("{[* comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(2, false, 1);
        assertOrigin(0, 15, matcher);
        assertMatch(2, 2, matcher);
    }

    public void testCommentTagBegin3() throws Exception {
        setDocumentText(" {[* comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertOrigin(1, 16, matcher);
        assertMatch(1, 1, matcher);
    }

    public void testCommentTagBegin4() throws Exception {
        setDocumentText("{[* comment *]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertOrigin(0, 15, matcher);
        assertMatch(1, 1, matcher);
    }

    public void testUnfinishedTag() throws Exception {
        setDocumentText("{[writing");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(8, false, 1);
        assertNull(matcher.findOrigin());
        assertMatch(8, 8, matcher);
    }

    public void testMoreMatchedTag() throws Exception {
        setDocumentText("{[if]}{[else]}{[/if]}");
        //               01234567890123456789012345678901234567890123456789012345678901234567890
        //               0         1         2         3         4         5         6         7
        BracesMatcher matcher = createMatcher(4, false, 1);
        assertOrigin(0, 6, matcher);
        assertMatch(new int[]{6, 12, 12, 14, 14, 19, 19, 21}, matcher);
    }

    //--------------------------------------------------------------------------
    private void assertOrigin(int expectedStart, int expectedEnd, BracesMatcher matcher) throws InterruptedException, BadLocationException {
        int[] origin = matcher.findOrigin();
        assertNotNull(origin);
        assertEquals("Incorrect origin block start:", expectedStart, origin[0]);
        assertEquals("Incorrect origin block end:", expectedEnd, origin[1]);
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
