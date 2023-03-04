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
package org.netbeans.modules.html.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;

import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Marek Fukala
 */
public class HtmlMatcherTest extends TestBase {

    private static final HtmlBracesMatching MATCHER_FACTORY;
    static {
        MATCHER_FACTORY = new HtmlBracesMatching();
        HtmlBracesMatching.testMode = true;
    }
    
    private Document document;
    
    public HtmlMatcherTest(String name) {
        super(name);
    }


    @Override
    protected void setUp() throws Exception {
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
        super.setUp();
    }

//    @Override
//    public void setUp() {
//        try {
//            super.setUp();
//
//            FileSystem memFS = FileUtil.createMemoryFileSystem();
//            FileObject fo = memFS.getRoot().createData("test", "html");
//            assertNotNull(fo);
//
//            DataObject dobj = DataObject.find(fo);
//            assertNotNull(dobj);
//
//            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
//            assertNotNull(cookie);
//
//            document = cookie.openDocument();
//            assertEquals(0, document.getLength());
//
//            LanguageRegistry registry = LanguageRegistry.getInstance();
//            Language l = registry.getLanguageByMimeType("text/html");
//            assertNotNull(l);
//        } catch (Exception ex) {
//            throw new IllegalStateException("Error setting up tests", ex);
//        }
//
//    }

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
        setDocumentText("<html>  <body> nazdar </body> </html>");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(7, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());
        
        matcher = createMatcher(17, false, 1);
        assertNull(matcher.findOrigin());
        assertNull(matcher.findMatches());
    }
    
    public void testForward() throws Exception {
        setDocumentText("<p><a> nazdar </a> </p>");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(0, false, 1);
        assertOrigin(0, 3, matcher);
        assertMatch(19, 23, matcher);
        
        matcher = createMatcher(4, false, 1);
        assertOrigin(3, 6, matcher);
        assertMatch(14, 18, matcher);
        
    }
    
    public void testBackward() throws Exception {
        setDocumentText("<p><a> nazdar </a> </p>");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(15, false, 1);
        assertOrigin(14, 18, matcher);
        assertMatch(3, 5, 5, 6, matcher);
        
        matcher = createMatcher(20, false, 1);
        assertOrigin(19, 23, matcher);
        assertMatch(0, 2, 2, 3, matcher);
        
    }

    public void testBoundaries() throws Exception {
        setDocumentText("<p><a></a></p>");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3

        //backward search
        BracesMatcher matcher = createMatcher(10, false, 1);
        assertOrigin(10, 14, matcher);
        assertMatch(0, 2, 2, 3, matcher);
        
        matcher = createMatcher(6, false, 1);
        assertOrigin(6, 10, matcher);
        assertMatch(3, 5, 5, 6, matcher);

        //forward search
        matcher = createMatcher(10, true, 1);
        assertOrigin(6, 10, matcher);
        assertMatch(3, 5, 5, 6, matcher);

        matcher = createMatcher(6, true, 1);
        assertOrigin(3, 6, matcher);
        assertMatch(6, 10, -1, -1, matcher);
        
    }
    
    public void testNoMatch() throws Exception {
        setDocumentText("<html><div></body></html>");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(13, false, 1);
        assertOrigin(11, 18, matcher);
        assertMatch(13, 13, matcher); //body has optional end tag so returning the searched position range (hack)
        
        matcher = createMatcher(8, false, 1);
        assertOrigin(6, 11, matcher);
        assertNull(matcher.findMatches());
        
    }
    
      
    public void testBlockComment() throws Exception {
        setDocumentText("<!-- comment -->");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertOrigin(0, 4, matcher);
        assertMatch(13, 16, matcher);
        
        matcher = createMatcher(14, false, 1);
        assertOrigin(13, 16, matcher);
        assertMatch(0, 4, matcher);
        
    }
    
    public void testMultilineBlockComment() throws Exception {
        setDocumentText("<!-- comment\ncomment line2 -->");
        //               0123456789012 345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(1, false, 1);
        assertOrigin(0, 4, matcher);
        assertMatch(27, 30, matcher);
        
        matcher = createMatcher(28, false, 1);
        assertOrigin(27, 30, matcher);
        assertMatch(0, 4, matcher);
        
    }
    
    public void testBlockCommentWithPrefix() throws Exception {
        setDocumentText(" <!-- comment -->");
        //               0123456789012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(1, true, 1);
        assertNull(matcher.findOrigin());
        
        matcher = createMatcher(1, false, 1);
        assertOrigin(1, 5, matcher);
        assertMatch(14, 17, matcher);
    }
    
     public void testBlockCommentBoundary() throws Exception {
        setDocumentText(" <!-- com\nment -->");
        //               0123456789 012345678901234567890123456789
        //               0         1         2         3
        BracesMatcher matcher = createMatcher(18, true, 2);
        assertOrigin(15, 18, matcher);
        assertMatch(1, 5, matcher);
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
        if(expectedStart2 != -1) {
            assertEquals("Incorrect match block start:", expectedStart2, match[2]);
        }
        if(expectedEnd2 != -1) {
            assertEquals("Incorrect match block end:", expectedEnd2, match[3]);
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
    
}
