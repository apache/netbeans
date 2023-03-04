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

package org.netbeans.modules.bugtracking.commons;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Support for compound undo/redo in text components
 * @author Ondrej Vrabec
 * @author Tomas Stupka
 * 
 */
public class UndoRedoSupport {

    private static final Pattern DELIMITER_PATTERN = Pattern.compile("[ ,:;.!?\n\t]"); //NOI18N
    
    private final DelegateManager delegateManager;
    private static final String ACTION_NAME_UNDO = "undo.action"; //NOI18N
    private static final String ACTION_NAME_REDO = "redo.action"; //NOI18N

    private final RequestProcessor rp = new RequestProcessor("Bugtracking undoredo", 50); // NOI18N
    
    public UndoRedoSupport () {
        delegateManager = new DelegateManager();
    }
    
    public synchronized UndoRedo getUndoRedo() {
        return delegateManager;
    }
    
    public void register (final TopComponent tc, boolean register) {
        if(register) {
            tc.removeContainerListener(undoRedoListener);
            tc.addContainerListener(undoRedoListener);        
            RequestProcessor.Task task = rp.create(new Runnable() {
                @Override
                public void run() {
                    undoRedoListener.register(tc, true);
                }
            });
            tc.putClientProperty(REGISTER_TASK, task);
            task.schedule(1000);
        } else {
            unregisterAll();
        }
    }
    
    private static final String REGISTER_TASK = "hyperlink.task";
    
    /**
     * Registers undo/redo manager on the given component. You should always call unregister once undo/redo is not needed.
     * @param issue
     * @param component
     * @return
     */
    private void register (JTextComponent component) {
        delegateManager.add(new CompoundUndoManager(component));
    }

    /**
     * Unregisters undo/redo manager on the component, removes registered listeners, etc.
     */
    private void unregisterAll () {
        delegateManager.removeAll();
    }
    
    /**
     * Unregisters undo/redo manager on the component, removes registered listeners, etc.
     */
    private void unregister (JTextComponent component) {
        delegateManager.remove(component);
    }
    
    private class CompoundUndoManager extends UndoRedo.Manager implements FocusListener {
        private final ChangeSupport support = new ChangeSupport(this);
        private CompoundEdit edit;
        private int lastOffset, lastLength;
        private final JTextComponent component;

        public CompoundUndoManager(JTextComponent component) {
            this.component = component;
        }
        
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            assert component != null;
            
            if (edit == null) {
                startNewEdit(component, e.getEdit());
                processDocumentChange(component);
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
                super.undoableEditHappened(e);
                processDocumentChange(component);
            } else {
                // last change consists of multiple chars, start new compound edit
                startNewEdit(component, e.getEdit());
            }
        }

        private void startNewEdit (JTextComponent component, UndoableEdit atomicEdit) {
            if (edit != null) {
                // finish the last edit
                edit.end();
            }
            edit = new MyCompoundEdit();
            edit.addEdit(atomicEdit);
            super.undoableEditHappened(new UndoableEditEvent(component, edit));
            lastOffset = component.getCaretPosition();
            lastLength = component.getDocument().getLength();
        }

        private void processDocumentChange(JTextComponent component) {
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

        @Override
        public void addChangeListener(ChangeListener l) {
            super.addChangeListener(l);
            support.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            super.removeChangeListener(l);
            support.removeChangeListener(l);
        }
        
        @Override
        public synchronized boolean canRedo() {
            boolean can = super.canRedo();
            if(!can) {
                return can;
            }
            return super.canRedo() && (component != null ? component.hasFocus() : false);
        }

        @Override
        public synchronized boolean canUndo() {
            return super.canUndo() && (component != null ? component.hasFocus() : false);
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            support.fireChange();
        }
        @Override
        public void focusLost(FocusEvent e) {
            support.fireChange();
        }        
        boolean hasFocus() {
            return component.hasFocus();
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

    
    private class DelegateManager implements UndoRedo {
        private final List<CompoundUndoManager> delegates = new LinkedList<CompoundUndoManager>();
        
        @Override
        public boolean canUndo() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.canUndo();
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canRedo() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.canRedo();
                    }
                }
            }
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        cm.undo();
                        return;
                    }
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        cm.redo();
                        return;
                    }
                }
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.addChangeListener(l);
                }
            }
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.removeChangeListener(l);
                }
            }
        }
        
        void discardAllEdits() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.discardAllEdits();
                }
            }
        }    
        
        @Override
        public String getUndoPresentationName() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.getUndoPresentationName();
                    }
                }
            }
            return "";            
        }

        @Override
        public String getRedoPresentationName() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.getRedoPresentationName();
                    }
                }
            }
            return "";
        }

        private void add(CompoundUndoManager cum) {
            cum.component.getDocument().addUndoableEditListener(cum);
            cum.component.addFocusListener(cum);            
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_UNDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), ACTION_NAME_UNDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0), ACTION_NAME_UNDO);
            cum.component.getActionMap().put(ACTION_NAME_UNDO, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (delegateManager.canUndo()) {
                        delegateManager.undo();
                    }
                }
            });
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_REDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), ACTION_NAME_REDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0), ACTION_NAME_UNDO);
            cum.component.getActionMap().put(ACTION_NAME_REDO, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (delegateManager.canRedo()) {
                        delegateManager.redo();
                    }
                }
            });
            
            synchronized(delegates) {
                delegates.add(cum);
            }
            
        }

        private void removeAll() {
            discardAllEdits();
            synchronized(delegates) {
                Iterator<CompoundUndoManager> it = delegates.iterator();
                while (it.hasNext()) {
                    CompoundUndoManager cum = it.next();
                    cum.component.getDocument().removeUndoableEditListener(cum);
                    cum.component.removeFocusListener(cum);
                    cum.component.getActionMap().remove(ACTION_NAME_UNDO);
                    cum.component.getActionMap().remove(ACTION_NAME_REDO);
                    it.remove();
                }
            }
        }

        private void remove(JTextComponent component) {
            synchronized(delegates) {
                Iterator<CompoundUndoManager> it = delegates.iterator();
                while (it.hasNext()) {
                    CompoundUndoManager cum = it.next();
                    if(component == cum.component) {
                        cum.component.getDocument().removeUndoableEditListener(cum);
                        cum.component.removeFocusListener(cum);
                        cum.component.getActionMap().remove(ACTION_NAME_UNDO);
                        cum.component.getActionMap().remove(ACTION_NAME_REDO);
                        it.remove();
                        break;
                    }
                }
            }
        }
    }

    private final UndoRedoListener undoRedoListener = new UndoRedoListener();
    private class UndoRedoListener implements ContainerListener {
        
        @Override
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            while(((c = c.getParent()) != null)) {
                if(c instanceof TopComponent) {
                    RequestProcessor.Task t = (RequestProcessor.Task) ((TopComponent)c).getClientProperty(REGISTER_TASK);
                    if(t != null) {
                        t.schedule(1000);
                    } 
                    break;
                }
            }
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            register((Container)e.getComponent(), false);
        }
        
        private void register(Component c, boolean register) {
            if(c instanceof JTextComponent) {
                JTextComponent tx = (JTextComponent) c;
                if(register) {
                    registerTask.add(tx);
                } else {
                    registerTask.remove(tx);
                }
            }
            if(c instanceof Container) {
                Container container = (Container) c;
                container.removeContainerListener(this);
                if(register) {
                    container.addContainerListener(this);
                }
                Component[] components = container.getComponents();
                for (Component cmp : components) {
                    register(cmp, register);
                }
            }
        }
    }    
    
    private final RegisterTask registerTask = new RegisterTask();
    private class RegisterTask implements Runnable {
        private final ConcurrentLinkedQueue<JTextComponent> toRegister = new ConcurrentLinkedQueue<JTextComponent>();
        private final ConcurrentLinkedQueue<JTextComponent> toUnregister = new ConcurrentLinkedQueue<JTextComponent>();
        private final RequestProcessor.Task task;

        public RegisterTask() {
            this.task = rp.create(this);
        }
        
        void add(JTextComponent tx) {
            toRegister.add(tx);
            task.schedule(500);
        }
        
        void remove(JTextComponent tx) {
            toUnregister.add(tx);
            task.schedule(500);
        }
        
        @Override
        public void run() {
            for (JTextComponent txt : toHandle(toRegister)) {
                register(txt);
            }            
            for (JTextComponent txt : toHandle(toUnregister)) {
                unregister(txt);
            }            
        }

        public List<JTextComponent> toHandle(ConcurrentLinkedQueue<JTextComponent> q) {
            List<JTextComponent> rs = new LinkedList<JTextComponent>();
            JTextComponent tx;
            while((tx = q.poll()) != null) {
                rs.add(tx);
            }
            return rs;            
        }
    }
}
