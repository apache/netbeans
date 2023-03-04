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

package org.netbeans.modules.versioning.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Support for compound undo/redo in text components
 * @author ondra
 */
public class UndoRedoSupport {

    private static final Pattern DELIMITER_PATTERN = Pattern.compile("[ ,:;.!?\n\t]"); //NOI18N
    private final JTextComponent component;
    private CompoundEdit edit;
    private int lastOffset, lastLength;
    private final UndoManager um;
    private static final String ACTION_NAME_UNDO = "undo.action"; //NOI18N
    private static final String ACTION_NAME_REDO = "redo.action"; //NOI18N

    /**
     * Registers undo/redo manager on the given component. You should always call unregister once undo/redo is not needed.
     * @param component
     * @return
     */
    public static UndoRedoSupport register (JTextComponent component) {
        UndoRedoSupport cum = new UndoRedoSupport(component);
        cum.init();
        return cum;
    }

    /**
     * Unregisters undo/redo manager on the component, removes registered listeners, etc.
     */
    public void unregister () {
        um.discardAllEdits();
        component.getDocument().removeUndoableEditListener(um);
        component.getActionMap().remove(ACTION_NAME_UNDO);
        component.getActionMap().remove(ACTION_NAME_REDO);
    }

    private UndoRedoSupport (JTextComponent textComponent) {
        this.component = textComponent;
        um = new CompoundUndoManager();
    }

    private void init() {
        component.getDocument().addUndoableEditListener(um);
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_UNDO);
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), ACTION_NAME_UNDO);
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0), ACTION_NAME_UNDO);
        component.getActionMap().put(ACTION_NAME_UNDO, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (um.canUndo()) {
                    um.undo();
                }
            }
        });
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_REDO);
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), ACTION_NAME_REDO);
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0), ACTION_NAME_UNDO);
        component.getActionMap().put(ACTION_NAME_REDO, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (um.canRedo()) {
                    um.redo();
                }
            }
        });
    }

    private class CompoundUndoManager extends UndoManager {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            if (edit == null) {
                startNewEdit(e.getEdit());
                processDocumentChange();
                return;
            }
            //AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit();
            UndoableEdit event = e.getEdit();
            if (event instanceof DocumentEvent) {
                if (((DocumentEvent)event).getType().equals(DocumentEvent.EventType.CHANGE)) {
                    edit.addEdit(e.getEdit());
                    return;
                }
            }
            int offsetChange = component.getCaretPosition() - lastOffset;
            int lengthChange = component.getDocument().getLength() - lastLength;

            if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
                lastOffset = component.getCaretPosition();
                lastLength = component.getDocument().getLength();
                addEdit(e.getEdit());
                processDocumentChange();
            } else {
                // last change consists of multiple chars, start new compound edit
                startNewEdit(e.getEdit());
            }
        }

        private void startNewEdit (UndoableEdit atomicEdit) {
            if (edit != null) {
                // finish the last edit
                edit.end();
            }
            edit = new MyCompoundEdit();
            edit.addEdit(atomicEdit);
            addEdit(edit);
            lastOffset = component.getCaretPosition();
            lastLength = component.getDocument().getLength();
        }

        private void processDocumentChange() {
            boolean endEdit = lastOffset == 0;
            if (!endEdit) {
                try {
                    String lastChar = component.getDocument().getText(lastOffset - 1, 1);
                    endEdit = DELIMITER_PATTERN.matcher(lastChar).matches();
                } catch (BadLocationException ex) {
                }
            }
            if (endEdit) {
                // ending the current compound edit, next will be started
                edit.end();
                edit = null;
            }
        }
    }

    private class MyCompoundEdit extends CompoundEdit {

        @Override
        public boolean isInProgress() {
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            if (edit != null) {
                edit.end();
            }
            super.undo();
            edit = null;
        }
    }
}
