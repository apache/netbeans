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

package org.netbeans.api.editor;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of curiosities of Swing's PlainDocument implementation.
 *
 * @author Miloslav Metelka
 */
public class PlainDocumentTest extends NbTestCase {
    
    public PlainDocumentTest(String name) {
        super(name);
    }
    
    public void testBehaviour() throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, "test hello world", null);
        UndoManager undo = new UndoManager();
        doc.addUndoableEditListener(undo);
        Position pos = doc.createPosition(2);
        doc.remove(0, 3);
        assert (pos.getOffset() == 0);
        undo.undo();
        assert (pos.getOffset() == 2);
        
        Position pos2 = doc.createPosition(5);
        doc.remove(4, 2);
        Position pos3 = doc.createPosition(4);
        assertSame(pos2, pos3);
        undo.undo();
        assert (pos3.getOffset() == 5);
    }

    public void testCuriosities() throws Exception {
        // Test position at offset 0 does not move after insert
        Document doc = new PlainDocument();
        doc.insertString(0, "test", null);
        Position pos = doc.createPosition(0);
        assertEquals(0, pos.getOffset());
        doc.insertString(0, "a", null);
        assertEquals(0, pos.getOffset());
        
        // Test there is an extra newline above doc.getLength()
        assertEquals("\n", doc.getText(doc.getLength(), 1));
        assertEquals("atest\n", doc.getText(0, doc.getLength() + 1));
        
        // Test the last line element contains the extra newline
        Element lineElem = doc.getDefaultRootElement().getElement(0);
        assertEquals(0, lineElem.getStartOffset());
        assertEquals(doc.getLength() + 1, lineElem.getEndOffset());

        // Test that once position gets to zero it won't go anywhere else (unless undo performed)
        pos = doc.createPosition(1);
        doc.remove(0, 1);
        assertEquals(0, pos.getOffset());
        doc.insertString(0, "b", null);
        assertEquals(0, pos.getOffset());
    }
    
}
