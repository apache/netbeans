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
package org.netbeans.modules.maven.refactoring.dependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class DocumentChangesConverterTest extends NbTestCase {

    public DocumentChangesConverterTest(String name) {
        super(name);
    }
    
    private Document document;
    private DocumentChangesConverter converter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        document = new DefaultStyledDocument();
        converter = new DocumentChangesConverter(document);
        document.addDocumentListener(converter);
    }
    
    public void testNoEdits() throws Exception {
        assertEquals(Arrays.asList(), converter.makeTextEdits());
    }
    
    private static final String CONTENT = 
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Integer tempor. Ut tempus purus at lorem. "
                + "Nulla pulvinar eleifend sem. Proin pede metus, vulputate nec, fermentum fringilla, vehicula vitae, "
                + "justo. Nullam faucibus mi quis velit. Nullam justo enim, consectetuer nec, ullamcorper ac, vestibulum "
                + "in, elit. Curabitur sagittis hendrerit ante. Sed vel lectus. Donec odio tempus molestie, porttitor "
                + "ut, iaculis quis, sem. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur "
                + "ridiculus mus. Aenean vel massa quis mauris vehicula lacinia. Morbi leo mi, nonummy eget tristique non, "
                + "rhoncus non leo. Duis bibendum, lectus ut viverra rhoncus, dolor nunc faucibus libero, eget facilisis "
                + "enim ipsum id lacus. Maecenas aliquet accumsan leo. Cum sociis natoque penatibus et magnis dis parturient "
                + "montes, nascetur ridiculus mus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per "
                + "inceptos hymenaeos.\n";
    
    void insertInitialText() throws BadLocationException {
        document.removeDocumentListener(converter);
        document.insertString(0, CONTENT, null);
        document.addDocumentListener(converter);
    }
    
    public void testSeparateEdits() throws Exception {
        insertInitialText();
        document.insertString(0, "112233", null);
        document.remove(41 + 6, 10);
        document.remove(58 + 6 - 10, 7);
        document.insertString(74 + 6 - 10 - 7, "Non ", null);
        assertSeparateEdits();
    }
    
    /**
     * The positions reported to the converter shift as the document is being edited.
     * So 2nd edit will not be observed on position 11, but position 11 + length of the previous insertion.
     * Check that the converter successfully adjusts the positions to point to the original document locations.
     */
    public void testOriginalDocPositions() throws Exception {
        insertInitialText();
        List<Integer> wordPositions = new ArrayList<>();
        List<Position> docPositions = new ArrayList<>();
        for (int p = CONTENT.indexOf(' '); p != -1; p = CONTENT.indexOf(' ', p + 1)) {
            wordPositions.add(p);
            docPositions.add(document.createPosition(p));
        }
        wordPositions.add(document.getLength());
        
        boolean add = true;
        int index = 0;
        for (Position p : docPositions) {
            if (add) {
                document.insertString(p.getOffset(), "0123", null);
            } else {
                document.remove(p.getOffset(), (wordPositions.get(index + 1) - wordPositions.get(index)));
            }
            index++;
        }
        
        List<TextEdit> edits = converter.makeTextEdits();
        
        for (int i = 0; i < edits.size(); i++) {
            assertEquals((int)wordPositions.get(i), edits.get(i).getStartOffset());
        }
    }
    
    /**
     * Checks that separate edits have their offsets adjusted accordingly
     */
    void assertSeparateEdits() throws Exception {
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(4, edits.size());
        
        TextEdit first = edits.get(0);
        TextEdit second = edits.get(1);
        
        assertEquals(0, first.getStartOffset());
        assertEquals(0, first.getEndOffset());
        assertEquals("112233", first.getNewText());
        
        assertEquals(41, second.getStartOffset());
        assertEquals(41 + 10, second.getEndOffset());
        assertNull(second.getNewText());
        
        TextEdit third = edits.get(2);
        TextEdit fourth = edits.get(3);
        
        assertEquals("Integer", CONTENT.substring(third.getStartOffset(), third.getEndOffset()));
        assertEquals(74, fourth.getStartOffset());
    }
    
    /**
     * Checks that random-ordered separate edits have their offsets adjusted accordingly and
     * come out ordered.
     */
    public void testUnorderedSeparateEdits() throws Exception {
        insertInitialText();
        document.remove(41, 10);
        document.insertString(74 - 10, "Non ", null);

        document.insertString(0, "112233", null);
        document.remove(58 + 6 - 10, 7);
        
        assertSeparateEdits();
    }
    
    public void testCoalesceFollowingInsert() throws Exception {
        insertInitialText();
        document.insertString(0, "112233", null);
        document.insertString(6, "445566", null);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        
        assertEquals("112233445566", edits.get(0).getNewText());
    }

    /**
     * 
     * Insert a text, and delete part of it
     */
    public void testInsertAndSmallDelete() throws Exception {
        document.insertString(0, "112233", null);
        document.remove(3, 2);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("1123", edits.get(0).getNewText());
        assertNoDelete(edits.get(0));
    }

    /**
     * 
     * Insert a text, and delete part of it
     */
    public void testInsertAndDeleteFromStart() throws Exception {
        document.insertString(0, "112233", null);
        document.remove(0, 3);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("233", edits.get(0).getNewText());
        assertNoDelete(edits.get(0));
    }

    /**
     * 
     * Insert a text, and delete part of it
     */
    public void testInsertAndDeleteToEnd() throws Exception {
        document.insertString(0, "112233", null);
        document.remove(3, 3);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("112", edits.get(0).getNewText());
        assertNoDelete(edits.get(0));
    }

    /**
     * 
     * Insert a text, and delete part of it
     */
    public void testInsertAndDeleteAfterEnd() throws Exception {
        insertInitialText();
        document.insertString(0, "112233", null);
        document.remove(3, 5);
        
        // the first edit will be trimmed. The second edit will contain rest of the delete.
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(2, edits.size());
        
        TextEdit first = edits.get(0);
        TextEdit second = edits.get(1);
        
        assertNull(second.getNewText());
        assertNoDelete(first);
        assertDeleteLength(second, 2);
        assertEquals("112", first.getNewText());
    }
    
    void assertNoDelete(TextEdit e) {
        assertEquals(0, e.getEndOffset() - e.getStartOffset());
    }
    
    void assertDeleteLength(TextEdit e, int l) {
        assertEquals(l, e.getEndOffset() - e.getStartOffset());
    }
    
    /**
     * 
     * Insert a text, and delete part of it
     */
    public void testInsertAndDeleteAll() throws Exception {
        insertInitialText();
        document.insertString(0, "112233", null);
        document.remove(0, 6);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(0, edits.size());
    }
    
    /**
     * Coalesces with an edit that continues right after, but was made in the past.
     * @throws Exception 
     */
    public void testCoalesceReverseEdits() throws Exception {
        insertInitialText();
        document.insertString(3, "112233", null);
        document.insertString(0, "000", null);

        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("000112233", edits.get(0).getNewText());
    }

    /**
     * Partially delete from a start of prior edit
     */
    public void testMergeToNextEdit() throws Exception {
        insertInitialText();
        document.insertString(3, "112233", null);
        document.remove(0, 5);
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("2233", edits.get(0).getNewText());
    }
    
    /**
     * Makes a replace at 6. Checks that a deletion that starts prior to 6
     * and deletes part of the replacing text will mold into single 
     * replace edit.
     * @throws Exception 
     */
    public void testDeleteFromNextReplace() throws Exception {
        insertInitialText();
        
        document.remove(6, 5);
        document.insertString(6, "0123456", null);
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        
        document.remove(3, 6);
        edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        assertEquals("3456", edits.get(0).getNewText());
    }

    /**
     * Makes a replace at 6. Check that replace that starts prior to 6
     * and deletes all replacing text will mold into a pure delete.
     * @throws Exception 
     */
    public void testMergeDeleteReplaceFully() throws Exception {
        insertInitialText();
        
        document.remove(6, 7);
        document.insertString(6, "0123456", null);
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        
        document.remove(3, 3 + 7);
        edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        
        TextEdit first = edits.get(0);
        assertNull(first.getNewText());
        assertEquals(3 + 7, first.getEndOffset() - first.getStartOffset());
    }
    
    /**
     * Make two replaces (one follows other). Delete all text from one replace plus
     * some of the text of the other
     * @throws Exception 
     */
    public void testDeleteOneAndHalfEdits() throws Exception {
        insertInitialText();
        
        document.remove(6, 7);
        document.insertString(6, "0123456", null);
        
        List<TextEdit> edits = converter.makeTextEdits();
        document.remove(6 + 7, 7);
        document.insertString(6 + 7, "0123456", null);
        
        edits = converter.makeTextEdits();
        assertEquals(2, edits.size());
        
        document.remove(3, 3 + 7 + 4);
        edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        
        TextEdit first = edits.get(0);
        assertEquals("456", first.getNewText());
        assertEquals(3, first.getStartOffset());
        // deleted: 3 prior to the first replace, then 7 chars of the 1st replace,
        // then additional 4 chars from the 2nd replace. 
        // Will absorb the 1st replace (7 chars deleted) and merge with the 2nd
        // (7 chars deleted)
        assertEquals(3 + (3 + 7 + 7), first.getEndOffset());
    }
    
    /**
     * Make a replace in the document, then insert that immediately precedes the replace.
     * These edits should merge into one.
     */
    public void testInsertBeforeReplace() throws Exception {
        insertInitialText();
        
        document.remove(6, 7);
        document.insertString(6, "0123456", null);

        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        document.insertString(6, "ABCD", null);
        
        edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        TextEdit first = edits.get(0);
        assertEquals("ABCD0123456", first.getNewText());
    }
    
    /**
     * Make a replace in the text. Then, make another replace inside the newly 
     * inserted text.
     * 
     * @throws Exception 
     */
    public void testReplaceFullyInsideReplace() throws Exception {
        insertInitialText();
        document.remove(6, 7);
        document.insertString(6, "abcdefghijkl", null);
        
        
        List<TextEdit> edits = converter.makeTextEdits();
        assertEquals(1, edits.size());
        document.remove(10, 3);
        document.insertString(10, "EFG--", null);
        edits = converter.makeTextEdits();
        
        assertEquals(1, edits.size());
        TextEdit first = edits.get(0);
        assertEquals("abcdEFG--hijkl", first.getNewText());
    }
}
