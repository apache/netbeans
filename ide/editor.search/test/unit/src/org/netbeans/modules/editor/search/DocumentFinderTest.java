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
package org.netbeans.modules.editor.search;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.editor.search.DocumentFinder.FindReplaceResult;

/**
 * @author Milutin Kristofic
 */
public class DocumentFinderTest {

    private static final int[] NOT_FOUND_RESULT = new int[]{-1, 0};

    public DocumentFinderTest() {
    }

    private static class SearchBuilder {

        final Map<String, Object> props = new HashMap<>();

        public SearchBuilder() {
            getDefaultValues();
        }

        private void getDefaultValues() {
            props.put(EditorFindSupport.FIND_WHAT, "test");
            props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
            props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
            props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
            props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
        }

        private SearchBuilder setFwdSearch(String findWhat) {
            getDefaultValues();
            props.put(EditorFindSupport.FIND_WHAT, findWhat);
            return this;
        }

        private SearchBuilder setBwdSearch(String findWhat) {
            getDefaultValues();
            props.put(EditorFindSupport.FIND_WHAT, findWhat);
            props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
            return this;
        }

        private SearchBuilder setWholeWordsBwdSearch(String findWhat) {
            getDefaultValues();
            props.put(EditorFindSupport.FIND_WHAT, findWhat);
            props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
            props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.TRUE);
            return this;
        }

        private SearchBuilder setWholeWordsFwdSearch(String findWhat) {
            getDefaultValues();
            props.put(EditorFindSupport.FIND_WHAT, findWhat);
            props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.TRUE);
            return this;
        }

        private SearchBuilder setMatchCase() {
            props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.TRUE);
            return this;
        }

        private SearchBuilder setPreserveCase() {
            props.put(EditorFindSupport.FIND_PRESERVE_CASE, Boolean.TRUE);
            return this;
        }

        public Map<String, Object> getProps() {
            return props;
        }
    }

    private int[] find(SearchBuilder searchBuilder, String docText, int startOffset, int endOffset) throws BadLocationException {
        return DocumentFinder.find(getDocument(docText), startOffset, endOffset, searchBuilder.getProps(), false);
    }
    
    private int[] find(SearchBuilder searchBuilder, String docText) throws BadLocationException {
        if (docText != null) {
            return DocumentFinder.find(getDocument(docText), 0, docText.length(), searchBuilder.getProps(), false);
        } else {
            return DocumentFinder.find(getDocument(docText), 0, 10, searchBuilder.getProps(), false);
        }
    }

    private DocumentFinder.FindReplaceResult replace(SearchBuilder searchBuilder, String docText, String replaceText) throws BadLocationException {
        if (docText != null) {
            return DocumentFinder.findReplaceResult(replaceText, getDocument(docText), 0, docText.length(), searchBuilder.getProps(), false);
        } else {
            return DocumentFinder.findReplaceResult(replaceText, getDocument(docText), 0, 10, searchBuilder.getProps(), false);
        }
    }

    private Document getDocument(String str) {
        JTextArea ta = new JTextArea(str);
        if (str != null && str.length() > 0) {
            ta.setCaretPosition(1);
        }
        Document doc = ta.getDocument();
        return doc;
    }

    @Test
    public void testSearchFalseFind() throws Exception {
        String findWhat = "test1";
        String docText = "";

        int[] finds = find(new SearchBuilder().setFwdSearch(findWhat), docText);
        final int[] expectedFinds = NOT_FOUND_RESULT;
        assertArrayEquals(expectedFinds, finds);

        docText = null;
        finds = find(new SearchBuilder().setFwdSearch(findWhat), docText);
        assertArrayEquals(expectedFinds, finds);
    }

    @Test
    public void testSearch() throws Exception {
        String findWhat = "test1";
        String docText = "...Text...aTest1 ...Text...";

        int[] finds = find(new SearchBuilder().setFwdSearch(findWhat), docText);
        final int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat), docText.toLowerCase().indexOf(findWhat) + findWhat.length()};
        assertArrayEquals(expectedFinds, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat), docText, docText.length(), docText.length());
        assertArrayEquals(expectedFinds, finds);
    }

    @Test
    public void testMatchCaseSearch() throws Exception {
        String findWhat = "test1";
        String docText = "...Text...test1...Text...";

        int[] finds = find(new SearchBuilder().setFwdSearch(findWhat).setMatchCase(), docText);
        final int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat), docText.toLowerCase().indexOf(findWhat) + findWhat.length()};
        assertArrayEquals(expectedFinds, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(expectedFinds, finds);

        docText = "...Text... Test1 ...Text...";
        finds = find(new SearchBuilder().setFwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        findWhat = "Test1";
        docText = "...Text...test1 ...Text...";
        finds = find(new SearchBuilder().setBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        findWhat = "Test1";
        docText = "...Text... test1 ...Text...";
        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);
    }

    @Test
    public void testWholeWordsSearch() throws Exception {
        String findWhat = "test1";
        String docText = "...Text... test1 ...Text...";

        int[] finds = find(new SearchBuilder().setFwdSearch(findWhat).setMatchCase(), docText);
        final int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat), docText.toLowerCase().indexOf(findWhat) + findWhat.length()};
        assertArrayEquals(expectedFinds, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(expectedFinds, finds);

        docText = "...Text...atest1...Text...";
        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);


        docText = "...Text... Test1 ...Text...";
        finds = find(new SearchBuilder().setFwdSearch(findWhat), docText);
        assertArrayEquals(expectedFinds, finds);

        finds = find(new SearchBuilder().setBwdSearch(findWhat), docText, docText.length(), docText.length());
        assertArrayEquals(expectedFinds, finds);

        findWhat = "Test1";
        docText = "...Text... test1 ...Text...";
        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);

        finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat).setMatchCase(), docText, docText.length(), docText.length());
        assertArrayEquals(NOT_FOUND_RESULT, finds);
    }

    /**
     * Test of backward whole words search
     * Bug #177126
     */
    @Test
    public void testBackWardSearchWholeWordsEndOfWords() throws Exception {
        String findWhat = "PAxx1";
        String docText = " PAxx11 ";

        int[] finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat), docText);

        final int[] expectedFinds = NOT_FOUND_RESULT;
        assertArrayEquals(expectedFinds, finds);
    }

    /**
     * Test of backward whole words search - a word is in the beginning of document
     * reopened Bug #177126
     */
    @Test
    public void testBackWardSearchWholeWordsBeginDocument() throws Exception {
        String findWhat = "Test1";
        String docText = "Test1 ";

        int[] finds = find(new SearchBuilder().setWholeWordsBwdSearch(findWhat), docText, docText.length(), docText.length());

        final int[] expectedFinds = {0, findWhat.length()};
        assertArrayEquals(expectedFinds, finds);
    }

    @Test
    public void testReplacePreserveCase() throws Exception {
        String findWhat = "Test1";
        String replaceText = "next1";
        
        String docText = "test1";
        String expectedReplace = "next1";
        
        FindReplaceResult replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);

        final int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
            
        docText = "Test1";        
        expectedReplace = "Next1";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "TEST1";
        expectedReplace = "NEXT1";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "tEST1";
        expectedReplace = "nEXT1";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
    }
    
    @Test
    public void testReplacePreserveCaseCamelCase() throws Exception {
        String findWhat = "FooBar";
        String replaceText = "SomethingElse";
        
        String docText = "fooBar";
        String expectedReplace = "somethingElse";
        
        FindReplaceResult replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);

        int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
            
        docText = "foobar";        
        expectedReplace = "somethingelse";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        findWhat = "JPanel";
        replaceText = "MBox";
        
        docText = "JPanel";
        expectedReplace = "MBox";
        expectedFinds = new int[] {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        docText = "jPanel";
        expectedReplace = "mBox";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "jpanel";
        expectedReplace = "mbox";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
     
        findWhat = "MyClass";
        replaceText = "DaRealJunk";
        
        docText = "MyClass";
        expectedFinds = new int[] {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        expectedReplace = "DaRealJunk";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        docText = "myClass";
        expectedReplace = "daRealJunk";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "myclass";
        expectedReplace = "darealjunk";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        findWhat = "DaRealJunk";
        replaceText = "MyClass";
        
        docText = "DaRealJunk";
        expectedFinds = new int[] {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        expectedReplace = "MyClass";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        docText = "daRealJunk";
        expectedReplace = "myClass";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "darealjunk";
        expectedReplace = "myclass";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        findWhat = "foo";
        replaceText = "fooBar";
        
        docText = "Foo";
        expectedFinds = new int[] {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        expectedReplace = "FooBar";
        replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
    }
    
    @Test
    public void testReplacePreserveCaseNotPreserveCase() throws Exception {
        String findWhat = "Test1";
        String replaceText = "next1";
        
        String docText = "test1";
        String expectedReplace = "next1";
        
        FindReplaceResult replaceResult = replace(new SearchBuilder().setPreserveCase().setFwdSearch(findWhat), docText, replaceText);

        final int[] expectedFinds = {docText.toLowerCase().indexOf(findWhat.toLowerCase()), docText.toLowerCase().indexOf(findWhat.toLowerCase()) + findWhat.length()};
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
            

        docText = "TEST1";
        expectedReplace = "next1";
        replaceResult = replace(new SearchBuilder().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());

        docText = "Test1";
        expectedReplace = "next1";
        replaceResult = replace(new SearchBuilder().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        docText = "Test1";
        replaceText = null;
        replaceResult = replace(new SearchBuilder().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertNull(replaceResult.getReplacedString());
        
        docText = "Test1";
        replaceText = "";
        expectedReplace = "";
        replaceResult = replace(new SearchBuilder().setFwdSearch(findWhat), docText, replaceText);
        assertArrayEquals(expectedFinds, replaceResult.getFoundPositions());
        assertEquals(expectedReplace, replaceResult.getReplacedString());
        
        
    }
}
