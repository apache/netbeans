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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UndoRedoActionTest extends NbTestCase
implements UndoRedo.Provider {
    static {
        Locale.setDefault(Locale.ENGLISH);
    }
    private UndoRedo.Manager ur;
    private MyEdit me;

    public UndoRedoActionTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    private Action undoAction(Lookup lkp) {
        UndoAction u = UndoAction.get(UndoAction.class);
        assertTrue("instance: " + u, u instanceof ContextAwareAction);
        return ((ContextAwareAction) u).createContextAwareInstance(lkp);
    }

    private Action redoAction(Lookup lkp) {
        RedoAction r = RedoAction.get(RedoAction.class);
        assertTrue("instance: " + r, r instanceof ContextAwareAction);
        return ((ContextAwareAction) r).createContextAwareInstance(lkp);
    }

    public void testUndoDeliversChanges() {
        doUndoRedoTest(new UndoRedo.Manager(), true);
    }
    
    public void testUndoDeliversChangesWithTooManyEdits() {
        UndoRedo.Manager man = new UndoRedo.Manager() {
            @Override
            public boolean canUndo() {
                if (super.canUndo()) {
                    undoableEditHappened(new UndoableEditEvent(UndoRedoActionTest.this, new MyEdit(true)));
                }
                return super.canUndo();
            }
        };
        doUndoRedoTest(man, false);
    }


    private void doUndoRedoTest(UndoRedo.Manager man, boolean testCounts) {
        me = new MyEdit();
        man.undoableEditHappened(new UndoableEditEvent(this, me));
        assertTrue("Can undo", man.canUndo());
        this.ur = man;
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action u = undoAction(lkp);
        Action r = redoAction(lkp);

        assertFalse("Not enabled", u.isEnabled());
        assertFalse("Not enabledR", r.isEnabled());
        MyEdit lu = new MyEdit();
        MyEdit lr = new MyEdit();
        u.addPropertyChangeListener(lu);
        r.addPropertyChangeListener(lr);

        ic.add(this);

        assertTrue("Action is enabled", u.isEnabled());
        assertEquals("One change", 1, lu.cnt);
        assertEquals("No redo change", 0, lr.cnt);
        assertEquals("Undo presentation", "&Undo My Undo", u.getValue(Action.NAME));

        u.actionPerformed(new ActionEvent(this, 0, ""));
        if (testCounts) {
            assertEquals("my edit undone", 1, me.undo);

            assertFalse("No more undo", man.canUndo());
            assertTrue("But redo", man.canRedo());
            assertEquals("Another undo change", 2, lu.cnt);
            assertEquals("New redo change", 1, lr.cnt);
            assertTrue("Redo action enabled", r.isEnabled());
            assertEquals("Redo presentation correct", "&Redo My Redo", r.getValue(Action.NAME));
        }

        r.actionPerformed(new ActionEvent(this, 0, ""));
        assertFalse("Redo action no longer enabled", r.isEnabled());
    }

    @Override
    public UndoRedo getUndoRedo() {
        return ur;
    }

    private static final class MyEdit implements UndoableEdit, PropertyChangeListener {
        private int undo;
        private int redo;
        private int cnt;
        private boolean ignore;

        public MyEdit() {
            this(false);
        }

        public MyEdit(boolean ignore) {
            this.ignore = ignore;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) {
                cnt++;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
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
            return false;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            return true;
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
    }
}
