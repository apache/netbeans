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

package org.openide.text;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport.Env;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 * Deadlock of a thread simulating reloading of document and another thread trying to close the file.
 * 
 * @author Miloslav Metelka
 */
public class Deadlock207571Test extends NbTestCase
implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private transient CES support;

    // Env variables
    private transient String content = "";
    private transient boolean valid = true;
    private transient boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private transient String cannotBeModified;
    private transient Date date = new Date ();
    private final transient PropertyChangeSupport pcl;
    private transient VetoableChangeListener vetoL;
    
    private transient volatile boolean inReloadBeforeSupportLock;
    private transient volatile boolean closing;
    
    private static Deadlock207571Test RUNNING;
    
    public Deadlock207571Test(String s) {
        super(s);
        pcl = new PropertyChangeSupport(this);
    }
    
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);
        RUNNING = this;
    }
    
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    private Object writeReplace () {
        return new Replace ();
    }
    
    public void testCloseDocumentWhenCheckReload() throws Exception {
        final StyledDocument doc = support.openDocument();
        // Create position ref so that it gets processed by support.close()
        PositionRef posRef = support.createPositionRef(0, Position.Bias.Forward);
        // Reload first does runAtomic() and inside it it syncs on support.getLock()
        Runnable reloadSimulationRunnable = new Runnable() {
            private boolean inRunAtomic;

            @Override
            public void run() {
                if (!inRunAtomic) {
                    inRunAtomic = true;
                    NbDocument.runAtomic(doc, this);
                    return;
                }
                inReloadBeforeSupportLock = true;
                while (!closing) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                synchronized (support.getLock()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                inReloadBeforeSupportLock = true;
                try {
                    pcl.firePropertyChange(Env.PROP_TIME, null, null);
                } finally {
                    inReloadBeforeSupportLock = false;
                }
            }
        };
        Task reloadSimulationTask = RequestProcessor.getDefault().post(reloadSimulationRunnable);

        while (!inReloadBeforeSupportLock) {
            Thread.sleep(1);
        }
        closing = true;
        support.close();
        
        reloadSimulationTask.waitFinished(1000);        
    }
    
    public void testUndoThrowsException() throws Exception {
        Document doc = support.openDocument();
        doc.insertString(0, "a", null);
        UndoRedo.Manager ur = support.getUndoRedo();
        MyEdit edit = new MyEdit();
        ur.undoableEditHappened(new UndoableEditEvent(this, edit));
        ur.canUndo();
        assertFalse("Expecting not undone", edit.undone);
        ur.undo();
        assertTrue("Expecting undone", edit.undone);
        ur.redo();
        assertFalse("Expecting redone", edit.undone);

        edit.undoFail = true;
        assertEquals(0, edit.undoFailedCount);
        try {
            ur.undo();
            fail("Exception expected");
        } catch (CannotUndoException ex) {
            // Expected
        }
        assertEquals(1, edit.undoFailedCount);
        try {
            ur.undo();
            fail("Exception expected");
        } catch (CannotUndoException ex) {
            // Expected
        }
        assertEquals(2, edit.undoFailedCount);
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcl.addPropertyChangeListener(l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcl.removePropertyChangeListener(l);
    }
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return RUNNING.support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream (content.getBytes ());
    }
    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            @Override
            public void close () throws IOException {
                super.close ();
                content = new String (toByteArray ());
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws IOException {
        if (cannotBeModified != null) {
            final String notify = cannotBeModified;
            IOException e = new IOException () {
                @Override
                public String getLocalizedMessage () {
                    return notify;
                }
            };
            Exceptions.attachLocalizedMessage(e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }
    
    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        @Override
        protected EditorKit createEditorKit () {
            // Important to use NbLikeEditorKit since otherwise FilterDocument
            // would be created with improper runAtomic()
            return new MyKit ();
        }
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
        
    }

    private static final class Replace implements Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }

    private static final class MyEdit extends AbstractUndoableEdit {
        
        boolean undone;
        
        boolean undoFail;
        
        boolean redoFail;
        
        int undoFailedCount;

        @Override
        public void undo() throws CannotUndoException {
            assert (!undone) : "Already undone";
            if (undoFail) {
                undoFailedCount++;
                throw new CannotUndoException();
            }
            undone = true;
        }

        @Override
        public void redo() throws CannotRedoException {
            assert (undone) : "Already redone";
            if (redoFail) {
                throw new CannotRedoException();
            }
            undone = false;
        }

    } // end of UndoableEdit
    
    private static final class MyKit extends NbLikeEditorKit {

        @Override
        public Document createDefaultDocument() {
            return new Doc() {

                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    super.insertString(offs, str, a);
                }
                
            };
        }
        
        
    }
}
