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
package org.netbeans.test.bookmarks;

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Element;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;

/**
 * Test of typing at begining/end and other typing tests.
 *
 * @author Miloslav Metelka
 */
public class BookmarksPersistenceTest extends EditorBookmarksTestCase {

    public BookmarksPersistenceTest(String testMethodName) {
        super(testMethodName);
    }

    public void testPersistence() {
        int[] bookmarkLines = new int[]{1, 7, 9};
        EditorOperator editorOper = null;

        openDefaultProject();

        Node node = new Node(new SourcePackagesNode(getDefaultProjectName()),
                "org.netbeans.test.bookmarks.BookmarksPersistenceTest|testPersistence.java"); // NOI18N
        new OpenAction().perform(node);

        try {

            editorOper = new EditorOperator("testPersistence.java");
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();

            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                new EventTool().waitNoEvent(1000);
            }

        } finally {
            editorOper.closeDiscardAll();
        }

        new OpenAction().perform(node);
        try {

            editorOper = new EditorOperator("testPersistence.java");
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, bookmarkLines);

        } finally {
            editorOper.closeDiscardAll();
        }
    }

    public void testBookmarkMove() {
        int bookmarkLine = 14;
        int lineToDelete = 12;

        openDefaultProject();
        Node node = new Node(new SourcePackagesNode(getDefaultProjectName()),
                "org.netbeans.test.bookmarks.BookmarksPersistenceTest|testBookmarkMove.java"); // NOI18N        
        new OpenAction().perform(node);
        EditorOperator editorOper = new EditorOperator("testBookmarkMove.java");

        try {            
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            editorOper.setCaretPosition(getLineOffset(doc, bookmarkLine));
            txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            editorOper.setCaretPosition(getLineOffset(doc, lineToDelete));
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK);
            doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, new int[]{bookmarkLine - 1});
        } finally {
            editorOper.closeDiscardAll();
        }
    }

//    public void testBookmarkMerge() {
//        int[] bookmarkLines = new int[]{9, 10, 11};
//
//        openDefaultProject();
//
//        openDefaultSampleFile();
//        try {
//            EditorOperator editorOper = getDefaultSampleEditorOperator();
//            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
//            Document doc = txtOper.getDocument();
//            for (int i = 0; i < bookmarkLines.length; i++) {
//                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
//                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            }
//            editorOper.setCaretPosition(bookmarkLines[0] + 1, 1);
//            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
//            BookmarkList bml = BookmarkList.get(doc);
//            checkBookmarksAtLines(bml, new int[]{9, 9, 9});
//        } finally {
//            closeFileWithDiscard();
//        }
//    }

    public void testNextBookmark() {
        int[] bookmarkLines = new int[]{9, 10, 11};
        int[] expectedLines = new int[]{9, 10, 11, 9};

        openDefaultProject();
        Node node = new Node(new SourcePackagesNode(getDefaultProjectName()),
                "org.netbeans.test.bookmarks.BookmarksPersistenceTest|testNextBookmark.java"); // NOI18N
        new OpenAction().perform(node);
        EditorOperator editorOper = new EditorOperator("testNextBookmark.java");

        try {
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc, 2));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_PERIOD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                int j = expectedLines[i];
                assertEquals("Caret is at bad location", j, actLine);
            }
        } finally {
           editorOper.closeDiscardAll();
        }
    }

    public void testPreviousBookmark() {
        int[] bookmarkLines = new int[]{9, 10, 11};
        int[] expectedLines = new int[]{11, 10, 9, 11};

        openDefaultProject();
        Node node = new Node(new SourcePackagesNode(getDefaultProjectName()),
                "org.netbeans.test.bookmarks.BookmarksPersistenceTest|testPreviousBookmark.java"); // NOI18N
        new OpenAction().perform(node);
        EditorOperator editorOper = new EditorOperator("testPreviousBookmark.java");
        try {
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc, 14));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int j = expectedLines[i];
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                assertEquals("Caret is at bad location", j, actLine);
            }
        } finally {
            editorOper.closeDiscardAll();
        }
    }

    private void checkBookmarksAtLines(BookmarkList bookmarkList, int[] expectedLineIndexes) {
        List<Bookmark> bookmarks = bookmarkList.getBookmarks ();
        assertEquals("Invalid bookmark count", expectedLineIndexes.length, bookmarks.size ());
        for (int i = 0; i < expectedLineIndexes.length; i++) {
            int expectedLineIndex = expectedLineIndexes[i];
            int lineIndex = bookmarks.get (i).getLineNumber();
            assertEquals("Bookmark line index " + lineIndex + " differs from expected " + expectedLineIndex+ "index "+i,
                    lineIndex,
                    expectedLineIndex);
        }
    }

    private int getLineOffset(Document doc, int lineIndex) {
        Element root = doc.getDefaultRootElement();
        return root.getElement(lineIndex).getStartOffset();
    }

    private int getLineIndex(Document doc, int offset) {
        Element root = doc.getDefaultRootElement();
        return root.getElementIndex(offset);
    }

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("#### " + getName() + " starts ####");
    }

    protected void tearDown() throws Exception {
        System.out.println("#### " + getName() + " ends ####");
        super.tearDown();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BookmarksPersistenceTest.class).enableModules(".*").clusters(".*")
    

);
    }
}
