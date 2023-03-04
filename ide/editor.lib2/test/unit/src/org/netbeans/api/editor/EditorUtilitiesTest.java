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

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.document.CustomUndoDocument;

/**
 *
 * @author mmetelka
 */
public class EditorUtilitiesTest {

    public EditorUtilitiesTest() {
    }

    @Test
    public void testGetAction() throws Exception {
        EditorKit editorKit = new DefaultEditorKit();
        String actionName = DefaultEditorKit.backwardAction;
        Action result = EditorUtilities.getAction(editorKit, actionName);
        for (Action expected : editorKit.getActions()) {
            if (actionName.equals(expected.getValue(Action.NAME))) {
                assertEquals(expected, result);
                return;
            }
        }
        fail("Action " + actionName + " not found.");
    }

    @Test
    public void testAddCaretUndoableEdit() throws Exception {
        final JEditorPane pane = new JEditorPane("text/plain", "Haf");
        //final CompoundEdit compoundEdit = new CompoundEdit();
        final boolean[] editAdded = { false };
        final Document doc = pane.getDocument();
        doc.putProperty(CustomUndoDocument.class, new CustomUndoDocument() {
            @Override
            public void addUndoableEdit(UndoableEdit edit) {
                editAdded[0] = true;
            }
        });
        final EditorCaret editorCaret = new EditorCaret();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                pane.setCaret(editorCaret);
                EditorUtilities.addCaretUndoableEdit(doc, editorCaret);
            }
        });
        
        assertTrue(editAdded[0]);
    }

}
