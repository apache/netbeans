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
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
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
public class EditToBeUndoneRedoneTest extends NbTestCase
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
    
    private static EditToBeUndoneRedoneTest RUNNING;
    
    public EditToBeUndoneRedoneTest(String s) {
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
    
    public void testUndoRedoEdits() throws Exception {
        Document doc = support.openDocument();
        doc.insertString(0, "a", null);
        UndoRedo.Manager ur = support.getUndoRedo();
        MyEdit myEdit = NbDocument.getEditToBeUndoneOfType(support, MyEdit.class);
        assertNotNull("Expected valid myEdit", myEdit);
        ur.undo();
        myEdit = NbDocument.getEditToBeRedoneOfType(support, MyEdit.class);
        assertNotNull("Expected valid myEdit", myEdit);
    }
    
    // testcase for bug: NETBEANS-691
    public void testUndoRedoUndoEdits() throws Exception {

        final StyledDocument d = support.openDocument();

        UndoRedo.Manager ur = support.getUndoRedo();
        UndoRedoManager urMgr = null;

        if (ur instanceof UndoRedoManager) {
            urMgr = (UndoRedoManager) ur;
        }

        d.insertString(d.getLength(), "a", null);
        final CompoundEdit bigEdit = new CompoundEdit();
        d.insertString(d.getLength(), "b", null);
        bigEdit.end();
        support.saveDocument();

        // setting the property to populate urMgr.onSaveTasksEdit field
        d.putProperty("beforeSaveRunnable", new Runnable() {

            public void run() {
                Runnable beforeSaveStart = (Runnable) d.getProperty("beforeSaveStart");
                if (beforeSaveStart != null) {
                    beforeSaveStart.run();
                    support.getUndoRedo().undoableEditHappened(new UndoableEditEvent(d, bigEdit));
                }
            }
        });

        urMgr.undo();
        support.saveDocument();
        d.putProperty("beforeSaveRunnable", null);
        assertEquals("after undo data", "a", d.getText(0, d.getLength()));

        urMgr.redo();
        support.saveDocument();
        assertEquals("after redo data", "ab", d.getText(0, d.getLength()));

        urMgr.undo();
        assertEquals("after redo data", "a", d.getText(0, d.getLength()));
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
    private static final class CES extends CloneableEditorSupport implements EditorCookie {
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

    private static final class MyEdit extends CompoundEdit { // Marker custom undo edit
        
    }
    
    private static final class MyKit extends NbLikeEditorKit {

        @Override
        public Document createDefaultDocument() {
            return new Doc() {

                @Override
                protected void fireUndoableEditUpdate(UndoableEditEvent e) {
                    UndoableEdit edit = e.getEdit();
                    MyEdit wrapEdit = new MyEdit();
                    wrapEdit.addEdit(edit);
                    wrapEdit.end();
                    e = new UndoableEditEvent(e.getSource(), wrapEdit);

                    super.fireUndoableEditUpdate(e);
                }

            };
        }
        
        
    }
}
