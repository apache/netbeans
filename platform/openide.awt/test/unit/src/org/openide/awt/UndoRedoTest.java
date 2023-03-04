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

package org.openide.awt;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UndoRedoTest extends NbTestCase implements ChangeListener {
    private int cnt;

    public UndoRedoTest(String n) {
        super(n);
    }

    public void testUndoDeliversChanges() {
        UndoRedo.Manager ur = new UndoRedo.Manager();
        doUndoRedoTest(ur);
    }

    public void testUndoDeliversChangesWithTooManyEdits() {
        UndoRedo.Manager ur = new UndoRedo.Manager() {
            @Override
            public boolean canUndo() {
                if (super.canUndo()) {
                    undoableEditHappened(new UndoableEditEvent(this, new MyEdit(true)));
                }
                return super.canUndo();
            }
        };
        doUndoRedoTest(ur);
    }

    public void testUndoRedoStable() {
        class URManager extends UndoRedo.Manager {
            @Override
            public UndoableEdit editToBeUndone() {
                return super.editToBeUndone();
            }

            @Override
            public UndoableEdit lastEdit() {
                return super.lastEdit();
            }
        }

        URManager ur = new URManager();
        MyEdit myEdit = new MyEdit();
        ur.addEdit(myEdit);
        assertTrue("Expected undo is possible:", ur.canUndo());
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        ur.undo();
        assertTrue("Expected redo is possible:", ur.canRedo());
        ur.redo();
        
        // myEdit undo() will fail
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        myEdit.setUndoFails(true);
        try {
            ur.undo();
            fail("Expected CannotUndoException be thrown:");
        } catch (CannotUndoException ex) {
            // Expected
        }
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        
        // Add extra two non-significant edits and test undo
        MyEdit nse1 = new MyEdit();
        nse1.setSignificant(false);
        ur.addEdit(nse1);
        MyEdit nse2 = new MyEdit();
        nse2.setSignificant(false);
        ur.addEdit(nse2);
        assertSame("Expected nse2:", nse2, ur.lastEdit());
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        
        // two non-significant edits will be undone myEdit undo() will fail
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        myEdit.setUndoFails(true);
        try {
            ur.undo();
            fail("Expected CannotUndoException be thrown:");
        } catch (CannotUndoException ex) {
            // Expected
        }
        assertEquals("One undo expected in nse1", 1, nse1.undo);
        assertEquals("One undo expected in nse2", 1, nse2.undo);
        assertEquals("One redo expected in nse1", 1, nse1.redo);
        assertEquals("One redo expected in nse2", 1, nse2.redo);
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
        
        myEdit.setUndoFails(false);
        ur.undo();
        assertEquals("Two undos expected in nse1", 2, nse1.undo);
        assertEquals("Two undos expected in nse2", 2, nse2.undo);
        assertEquals("One redo expected in nse1", 1, nse1.redo);
        assertEquals("One redo expected in nse2", 1, nse2.redo);
        assertSame("Expected null:", null, ur.editToBeUndone());

        ur.redo();
        assertSame("Expected myEdit:", myEdit, ur.editToBeUndone());
    }

    private void doUndoRedoTest(UndoRedo.Manager ur) {
        assertFalse("Nothing to undo", ur.canUndo());
        ur.addChangeListener(this);
        MyEdit me = new MyEdit();
        ur.undoableEditHappened(new UndoableEditEvent(this, me));
        assertChange("One change");
        assertTrue("Can undo now", ur.canUndo());
        ur.undo();
        assertFalse("Cannot undo", ur.canUndo());
        assertChange("Snd change");

        assertTrue("But redo", ur.canRedo());
        ur.redo();
        assertChange("Third change");
        assertEquals("One undo", 1, me.undo);
        assertEquals("One redo", 1, me.redo);
    }
    
    private void assertChange(String msg) {
        if (cnt == 0) {
            fail(msg);
        }
        cnt = 0;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        cnt++;
    }
    private static final class MyEdit implements UndoableEdit, PropertyChangeListener {
        private int undo;
        private int redo;
        private int cnt;
        private boolean ignore;
        
        private boolean undoFails;
        private boolean significant;

        public MyEdit() {
            this(false);
        }

        public MyEdit(boolean ignore) {
            this.ignore = ignore;
            this.significant = true;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) {
                cnt++;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            if (undoFails) {
                throw new CannotUndoException();
            }
            undo++;
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void redo() throws CannotRedoException {
            redo++;
        }

        @Override
        public boolean canRedo() {
            return true;
        }

        @Override
        public void die() {
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof MyEdit && ((MyEdit)anEdit).ignore) {
                return true;
            }
            return false;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            return significant;
        }

        @Override
        public String getPresentationName() {
            return "My Edit";
        }

        @Override
        public String getUndoPresentationName() {
            return "My Undo";
        }

        @Override
        public String getRedoPresentationName() {
            return "My Redo";
        }
        
        void setUndoFails(boolean undoFails) {
            this.undoFails = undoFails;
        }

        void setSignificant(boolean significant) {
            this.significant = significant;
        }

    }

}