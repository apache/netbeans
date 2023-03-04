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

package org.netbeans.modules.csl.api;

import org.netbeans.modules.csl.api.EditHistory;
import java.util.Random;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import junit.framework.TestCase;

/**
 *
 * @author Tor Norbye
 */
public class EditHistoryTest extends TestCase {
    public EditHistoryTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private Document getDocument(String s) throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, s, null);
        return doc;
    }

    private void validateHistory(String original, String modified, EditHistory history) throws Exception {
        // Check position mapping
// This won't work in the presence of removes! There will be positions lost that maps back to the
// boundaries of the deleted block rather than the interior        
//        for (int i = 0; i < original.length(); i++) {
//            int newPos = history.convertOldToNew(i);
//            int oldPos = history.convertNewToOld(newPos);
//            assertEquals("Incorrect position mapping for " + modified.charAt(i), i, oldPos);
//        }

        // Ensure that the head and tail of the document is identical to the beginning
        String head = original.substring(0, history.getStart());
        assertEquals("Wrong head; ", head, modified.substring(0, history.getStart()));
        String tail = original.substring(history.getStart()+history.getOriginalSize());
        assertEquals("Wrong tail; ", tail, modified.substring(history.getStart()+history.getEditedSize()));
    }

    private void insert(Document document, EditHistory history, int offset, String string) throws Exception {
        try {
            document.addDocumentListener(history);
            document.insertString(offset, string, null);
        } finally {
            document.removeDocumentListener(history);
        }
    }

    private void remove(Document document, EditHistory history, int offset, int length) throws Exception {
        try {
            document.addDocumentListener(history);
            document.remove(offset, length);
        } finally {
            document.removeDocumentListener(history);
        }
    }

    public void testInserts1() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   He__lloWorld
        insert(doc, history, 5, "__");
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__lloWorld", modified);
        validateHistory(original, modified, history);
        assertEquals(5, history.getStart());
        assertEquals(0, history.getOriginalSize());
        assertEquals(2, history.getEditedSize());
    }

    public void testRemoves1() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HeoWorld
        remove(doc, history, 5, 2);
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   HeoWorld", modified);
        validateHistory(original, modified, history);
        assertEquals(5, history.getStart());
        assertEquals(2, history.getOriginalSize());
        assertEquals(0, history.getEditedSize());
    }

    public void testMultipleRemoves1() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HeoWorld
        remove(doc, history, 5, 2);
        //012345678901234567890
        //   HelloWorld
        //   HeoWld
        remove(doc, history, 7, 2);
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   HeoWld", modified);
        assertEquals(5, history.getStart());
        assertEquals(11, history.getOriginalEnd());
        assertEquals(7, history.getEditedEnd());
        assertEquals(6, history.getOriginalSize());
        assertEquals(2, history.getEditedSize());
        assertEquals(-4, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMultipleRemoves2() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HelloWod
        remove(doc, history, 10, 2);
        //012345678901234567890
        //   HelloWod
        //   HeoWod
        remove(doc, history, 5, 2);
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   HeoWod", modified);
        assertEquals(5, history.getStart());
        assertEquals(12, history.getOriginalEnd());
        assertEquals(8, history.getEditedEnd());
        assertEquals(7, history.getOriginalSize());
        assertEquals(3, history.getEditedSize());
        assertEquals(-4, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMultipleRemoves3() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HelloWod
        remove(doc, history, 10, 2);
        //012345678901234567890
        //   HelloWod
        //   HeoWod
        remove(doc, history, 5, 2);
        //012345678901234567890
        //   HeoWod
        //   Heood
        remove(doc, history, 6, 1);
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   Heood", modified);
        assertEquals(5, history.getStart());
        assertEquals(12, history.getOriginalEnd());
        assertEquals(7, history.getEditedEnd());
        assertEquals(7, history.getOriginalSize());
        assertEquals(2, history.getEditedSize());
        assertEquals(-5, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMultipleInserts1() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   He__lloWorld
        insert(doc, history, 5, "__");
        //012345678901234567890
        //   He__lloWorld
        //   He__llo__World
        insert(doc, history, 10, "__");

        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__llo__World", modified);
        assertEquals(5, history.getStart());
        assertEquals(8, history.getOriginalEnd());
        assertEquals(12, history.getEditedEnd());
        assertEquals(3, history.getOriginalSize());
        assertEquals(7, history.getEditedSize());
        assertEquals(4, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMultipleInserts2() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HelloWo__rld
        insert(doc, history, 10, "__");
        //012345678901234567890
        //   HelloWo__rld
        //   He__lloWo__rld
        insert(doc, history, 5, "__");

        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__lloWo__rld", modified);
        assertEquals(5, history.getStart());
        assertEquals(10, history.getOriginalEnd());
        assertEquals(14, history.getEditedEnd());
        assertEquals(5, history.getOriginalSize());
        assertEquals(9, history.getEditedSize());
        assertEquals(4, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMultipleInserts3() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   HelloWo__rld
        insert(doc, history, 10, "__");
        //012345678901234567890
        //   HelloWo__rld
        //   He__lloWo__rld
        insert(doc, history, 5, "__");
        //012345678901234567890
        //   He__lloWo__rld
        //   He__ll__oWo__rld
        insert(doc, history, 9, "__");

        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__ll__oWo__rld", modified);
        assertEquals(5, history.getStart());
        assertEquals(10, history.getOriginalEnd());
        assertEquals(16, history.getEditedEnd());
        assertEquals(5, history.getOriginalSize());
        assertEquals(11, history.getEditedSize());
        assertEquals(6, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMixed2() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   He__lloWorld
        insert(doc, history, 5, "__");
        //012345678901234567890
        //   He__lloWorld
        //   He__llorld
        remove(doc, history, 10, 2);

        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__llorld", modified);
        assertEquals(5, history.getStart());
        assertEquals(10, history.getOriginalEnd());
        assertEquals(10, history.getEditedEnd());
        assertEquals(5, history.getOriginalSize());
        assertEquals(5, history.getEditedSize());
        assertEquals(0, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testMixed3() throws Exception {
        EditHistory history = new EditHistory();
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   He__lloWorld
        insert(doc, history, 5, "__");
        //012345678901234567890
        //   He__lloWorld
        //   He__llo__World
        insert(doc, history, 10, "__");
        //012345678901234567890
        //   He__llo__World
        //   He__l__World
        remove(doc, history, 8, 2);

        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__l__World", modified);
        assertEquals(5, history.getStart());
        assertEquals(8, history.getOriginalEnd());
        assertEquals(10, history.getEditedEnd());
        assertEquals(3, history.getOriginalSize());
        assertEquals(5, history.getEditedSize());
        assertEquals(2, history.getSizeDelta());
        validateHistory(original, modified, history);
    }

    public void testRandom() throws Exception {
        // Try lots of edits and make sure the edit history at the end is valid
        String PREFIX = "DONTTOUCHSTART";
        String SUFFIX = "DONTTOUCHEND";
        String original = PREFIX +
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
            SUFFIX;

        Document doc = getDocument(original);
        EditHistory history = new EditHistory();
        Random random = new Random(500);
        for (int i = 0; i < 100; i++) {
            boolean insert = random.nextBoolean();
            int docLength = doc.getLength()-PREFIX.length()-SUFFIX.length()-1;

            int offset = (int)(random.nextDouble()*docLength)+PREFIX.length();
            if (insert) {
                insert(doc, history, offset, "_");
            } else {
                remove(doc, history, offset, 1);
            }
        }
        String modified = doc.getText(0, doc.getLength());
        validateHistory(original, modified, history);
    }

    public void testCombined1() throws Exception {
        EditHistory firstHistory = new EditHistory();
        EditHistory history = new EditHistory();
        firstHistory.add(history);
        String original = "   HelloWorld";
        Document doc = getDocument(original);
        //012345678901234567890
        //   HelloWorld
        //   He__lloWorld
        insert(doc, history, 5, "__");
        String modified = doc.getText(0, doc.getLength());
        assertEquals("   He__lloWorld", modified);
        validateHistory(original, modified, history);
        assertEquals(5, history.getStart());
        assertEquals(0, history.getOriginalSize());
        assertEquals(2, history.getEditedSize());

        // Add some more history
        original = modified;
        EditHistory oldHistory = history;
        history = new EditHistory();
        oldHistory.add(history);
        insert(doc, history, 10, "__");
        modified = doc.getText(0, doc.getLength());
        assertEquals("   He__llo__World", modified);
        validateHistory(original, modified, history);
        assertEquals(10, history.getStart());
        assertEquals(0, history.getOriginalSize());
        assertEquals(2, history.getEditedSize());

        // Now test combined
        // Just most recent:
        assertEquals(1, history.getVersion());
        EditHistory h;

        h = EditHistory.getCombinedEdits(oldHistory.getVersion(), history);
        assertNotNull(h);
        assertEquals(10, h.getStart());
        assertEquals(0, h.getOriginalSize());
        assertEquals(2, h.getEditedSize());

        h = EditHistory.getCombinedEdits(-1, history);
        assertNotNull(h);
        assertEquals(5, h.getStart());
        assertEquals(3, h.getOriginalSize());
        assertEquals(7, h.getEditedSize());

        // From the beginning:
        assertEquals(0, oldHistory.getVersion());
        h = EditHistory.getCombinedEdits(history.getVersion(), history);
        assertNull(h);
    }

    public void testChoppedHistory() throws Exception {
        EditHistory old = new EditHistory();
        for (int i = 0; i < 50; i++) {
            EditHistory history = new EditHistory();
            old.add(history);
            old = history;
        }

        assertNotNull(EditHistory.getCombinedEdits(48, old));
        assertNotNull(EditHistory.getCombinedEdits(47, old));
        assertNotNull(EditHistory.getCombinedEdits(46, old));

        EditHistory curr = old;
        for (int i = 0; i < 5; i++) {
            assertTrue(curr.previous != null);
            assertTrue(curr.previous != curr);
            curr = curr.previous;
        }

        int i = 0;
        for (; i < 49; i++) {
            assertTrue(curr.previous != curr);
            curr = curr.previous;
            if (curr == null) {
                break;
            }
        }
        // Make sure we reached the end of the previous pointers well before the 50
        assertTrue(i < 40);
    }

}
