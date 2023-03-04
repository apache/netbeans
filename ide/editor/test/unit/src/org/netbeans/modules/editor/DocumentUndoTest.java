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
package org.netbeans.modules.editor;

import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;

/**
 * Test the annotations attached to the editor.
 *
 * @author Miloslav Metelka
 */
public class DocumentUndoTest extends BaseDocumentUnitTestCase {
    
    private UndoManager undoManager;
    
    public DocumentUndoTest(String testMethodName) {
        super(testMethodName);
        
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        undoManager = new UndoManager();
        getDocument().addUndoableEditListener(undoManager);
    }

    public void testUndoWordAtOnce() throws Exception {
        insertByAtomicChars(0, "abc");
        undoManager.undo();
        assertDocumentText("Expected empty document", "");
    }
    
    public void testUndoSecondWordFromTwo() throws Exception {
        insertByAtomicChars(0, "abc def");
        undoManager.undo();
        assertDocumentText("Expected second word undone", "abc ");
    }
    
    public void testUndoAtomicThenNonAtomic() throws Exception {
        insertByAtomicChars(0, "a b");
        getDocument().insertString(3, "c", null);
        undoManager.undo();
        assertDocumentText("Expected second word undone", "a ");
    }
    
    public void testUndoNonAtomicThenAtomic() throws Exception {
        getDocument().insertString(0, "a", null);
        insertByAtomicChars(1, "bc");
        undoManager.undo();
        assertDocumentText("Expected empty document", "");
    }
    
    public void testUndoTwoNonAtomic() throws Exception {
        getDocument().insertString(0, "a", null);
        getDocument().insertString(1, "b", null);
        undoManager.undo();
        assertDocumentText("Expected empty document", "");
    }
    
    private void insertByAtomicChars(int offset, String text) throws Exception {
        BaseDocument doc = getDocument();
        for (int i = 0; i < text.length(); i++) {
            doc.atomicLock();
            try {
                doc.insertString(offset + i, text.substring(i, i + 1), null);
            } finally {
                doc.atomicUnlock();
            }
        }
    }

}
