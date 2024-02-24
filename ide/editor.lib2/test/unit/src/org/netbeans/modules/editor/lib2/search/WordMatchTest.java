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
package org.netbeans.modules.editor.lib2.search;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;

/**
 *
 * @author Miloslav Metelka
 */
public class WordMatchTest extends NbTestCase {
    
    public WordMatchTest(String name) {
        super(name);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimple1");
//        includes.add("testSimpleUndoRedo");
//        includes.add("testCustomBounds");
//        includes.add("testEmptyCustomBounds");
//        includes.add("testRemoveNewline");
//        includes.add("testRandom");
//        includes.add("testLock");
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

    public void testOffset0Forward() throws Exception {
        Document doc = new PlainDocument();
        WordMatch wordMatch = WordMatch.get(doc);
        //                   012345678901234567890123456789
        doc.insertString(0, "abc abc dab ab a abab+c", null);
        int docLen = doc.getLength();
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "abca", docLen + 3);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "daba", docLen + 3);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "aba", docLen + 2);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "aa", docLen + 1);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "ababa", docLen + 4);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "ca", docLen + 1);
        wordMatch.matchWord(0, true);
        compareText(doc, 0, "ca", docLen + 1);

        wordMatch.matchWord(0, false);
        compareText(doc, 0, "ababa", docLen + 4);
    }
    
    public void testOffsetDocLenBackward() throws Exception {
        Document doc = new PlainDocument();
        WordMatch wordMatch = WordMatch.get(doc);
        //                   012345678901234567890123456789
        doc.insertString(0, "abc abc dab ab a", null);
        int docLen = doc.getLength();
        wordMatch.matchWord(docLen, false);
        compareText(doc, docLen - 1, "ab", docLen + 1);
        wordMatch.matchWord(docLen, false);
        compareText(doc, docLen - 1, "abc", docLen + 2);
        wordMatch.matchWord(docLen, false);
        compareText(doc, docLen - 1, "abc", docLen + 2);

        wordMatch.matchWord(docLen, true);
        compareText(doc, docLen - 1, "ab", docLen + 1);
        wordMatch.matchWord(docLen, true);
        compareText(doc, docLen - 1, "a", docLen);
    }

    public void testOffsetInsideBackward() throws Exception {
        Document doc = new PlainDocument();
        WordMatch wordMatch = WordMatch.get(doc);
        //                   012345678901234567890123456789
        doc.insertString(0, "abc abc dab ab a xyz ab abd", null);
        int docLen = doc.getLength();
        int offset = 15;
        wordMatch.matchWord(offset, false);
        compareText(doc, offset, "aba", docLen + 2);
        wordMatch.matchWord(docLen, true);
        compareText(doc, offset, "a ", docLen);
        wordMatch.matchWord(docLen, true);
        compareText(doc, offset, "aa ", docLen + 1);
        wordMatch.matchWord(docLen, true);
        compareText(doc, offset, "xyza", docLen + 3);
        wordMatch.matchWord(docLen, true);
        compareText(doc, offset, "abda", docLen + 3);
        wordMatch.matchWord(docLen, true);
        compareText(doc, offset, "abca", docLen + 3);
    }
    
    public void testMultiDocs() throws Exception {
        JEditorPane pane = new JEditorPane();
        pane.setText("abc ax ec ajo");
        JFrame frame = new JFrame();
        frame.getContentPane().add(pane);
        frame.pack(); // Allows to EditorRegistry.register() to make an item in the component list
        EditorApiPackageAccessor.get().setIgnoredAncestorClass(JEditorPane.class);
        EditorApiPackageAccessor.get().register(pane);
        
        Document doc = new PlainDocument();
        WordMatch wordMatch = WordMatch.get(doc);
        //                   012345678901234567890123456789
        doc.insertString(0, "abc a x ahoj", null);
        int docLen = doc.getLength();
        int offset = 5;
        wordMatch.matchWord(offset, false);
        compareText(doc, offset - 1, "abc ", docLen + 2);
        wordMatch.matchWord(offset, false);
        compareText(doc, offset - 1, "ahoj ", docLen + 3);
        wordMatch.matchWord(offset, false);
        compareText(doc, offset - 1, "ax ", docLen + 1);
        wordMatch.matchWord(offset, false);
        compareText(doc, offset - 1, "ajo ", docLen + 2);
        wordMatch.matchWord(offset, false);
        compareText(doc, offset - 1, "ajo ", docLen + 2);
        pane.setText(""); // Ensure this doc would affect WordMatch for other docs
    }

    private static void compareText(Document doc, int offset, String text, int docLen) throws Exception {
        String docDump = dumpDoc(doc);
        assertEquals(docDump, doc.getLength(), docLen);
        String textInDoc = doc.getText(offset, text.length());
        assertEquals(docDump + ", offset=" + offset, text, textInDoc);
    }

    private static String dumpDoc(Document doc) throws Exception {
        String text = doc.getText(0, doc.getLength());
        return "DocText=\"" + CharSequenceUtilities.debugText(text) + "\"";
    }
}
