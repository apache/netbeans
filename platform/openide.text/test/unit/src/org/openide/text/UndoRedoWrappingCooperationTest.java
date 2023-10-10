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
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.junit.*;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;

/**
 * Testing CES's UndoGroupManager; BEGIN_COMMIT_GROUP, END_COMMIT_GROUP
 *
 * Also included are tests testSaveDocumentErrorCase and testRedoAfterSave.
 * They fail for some base CES functionality. They could be moved
 * to UndoRedoCooperationTest.
 *
 * @author  Ernie Rael
 */
public class UndoRedoWrappingCooperationTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CES support;
    // Env variables
    private String content = "Hello";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;
    
    /** Creates new TextTest */
    public UndoRedoWrappingCooperationTest (String s) {
        super(s);
    }
    
    protected javax.swing.text.EditorKit createEditorKit() {
        return new NbLikeEditorKit();
    }

    // could install a logger "Handler" and test the warning only when
    // expected. Maybe later.
    Level disableWarning()
    {
        Logger l = Logger.getLogger("org.openide.text.CloneableEditorSupport");
        Level level = l.getLevel();
        l.setLevel(Level.SEVERE);
        return level;
    }
    void enableWarning(Level level)
    {
        Logger l = Logger.getLogger("org.openide.text.CloneableEditorSupport");
        l.setLevel(level);
    }

    // Use these methods with the UndoRedoGroup patch
    CompoundEdit beginChunk(Document d) {
        sendUndoableEdit(d, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
        return null;
    }
    
    void endChunk(Document d) {
        endChunk(d, null);
    }

    void endChunk(Document d, CompoundEdit ce) {
        sendUndoableEdit(d, CloneableEditorSupport.END_COMMIT_GROUP);
    }

    void markChunk(Document d) {
        sendUndoableEdit(d, CloneableEditorSupport.MARK_COMMIT_GROUP);
    }

    void sendUndoableEdit(Document d, UndoableEdit ue) {
        if(d instanceof AbstractDocument) {
            UndoableEditListener[] uels = ((AbstractDocument)d).getUndoableEditListeners();
            UndoableEditEvent ev = new UndoableEditEvent(d, ue);
            for(UndoableEditListener uel : uels) {
                uel.undoableEditHappened(ev);
            }
        }
    }

    // Use these methods with compound edit implementation
    // CompoundEdit beginChunk(Document d) {
    //     CompoundEdit ce = new CompoundEdit();
    //     support.getUndoRedo().undoableEditHappened
    //             (new UndoableEditEvent(d, ce));
    //     return ce;
    // }

    // void endChunk(Document d, CompoundEdit ce) {
    //     ce.end();
    // }

    UndoRedo.Manager ur() {
        return support.getUndoRedo();
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }

    public void testTrivialChunk() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();

        // same operations as testSingleChunk,
        // but don't test modified/canUndo/canRedo state

        CompoundEdit ce = beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);
        endChunk(d, ce);

        assertEquals("data", "ab", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("after undo data", "", d.getText(0, d.getLength()));

        ur().redo();
        assertEquals("after redo data", "ab", d.getText(0, d.getLength()));
    }

    public void testSingleChunk() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        assertFalse("initially: not modified", support.isModified());
        assertFalse("initially: no undo", ur().canUndo());
        assertFalse("initially: no redo", ur().canRedo());

        CompoundEdit ce = beginChunk(d);
        assertFalse("start chunk: not modified", support.isModified());
        assertFalse("start chunk: no undo", ur().canUndo());
        assertFalse("start chunk: no redo", ur().canRedo());

        d.insertString(d.getLength(), "a", null);
        assertTrue("insert: modified", support.isModified());
        assertTrue("insert: can undo", ur().canUndo());
        assertFalse("insert: no redo", ur().canRedo());

        d.insertString(d.getLength(), "b", null);
        endChunk(d, ce);
        assertEquals("chunk: data", "ab", d.getText(0, d.getLength()));
        assertTrue("endChunk: modified", support.isModified());
        assertTrue("endChunk: can undo", ur().canUndo());
        assertFalse("endChunk: no redo", ur().canRedo());

        ur().undo();
        assertEquals("after undo: data", "", d.getText(0, d.getLength()));
        assertFalse("undo: not modified", support.isModified());
        assertFalse("undo: no undo", ur().canUndo());
        assertTrue("undo: can redo", ur().canRedo());

        ur().redo();
        assertEquals("after redo: data", "ab", d.getText(0, d.getLength()));
        assertTrue("redo: modified", support.isModified());
        assertTrue("redo: can undo", ur().canUndo());
        assertFalse("redo: no redo", ur().canRedo());
    }

    /** this also tests mixing regular and chunks */
    public void testExtraEndChunk() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();

        CompoundEdit ce = beginChunk(d);

        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        endChunk(d, ce);
        assertEquals("chunk: data", "ab", d.getText(0, d.getLength()));

        Level level = disableWarning();
        try {
            endChunk(d, ce);
            endChunk(d, ce);

            assertEquals("extraEnd: data", "ab", d.getText(0, d.getLength()));
            assertTrue("extraEnd: modified", support.isModified());
            assertTrue("extraEnd: can undo", ur().canUndo());
            assertFalse("extraEnd: no redo", ur().canRedo());

            d.insertString(d.getLength(), "c", null);
            d.insertString(d.getLength(), "d", null);
            endChunk(d, ce);
            assertEquals("extraEnd2: data", "abcd", d.getText(0, d.getLength()));
            ur().undo();
            endChunk(d, ce);
            if (!documentSupportsUndoMergingOfWords()) {
                assertEquals("undo1: data", "abc", d.getText(0, d.getLength()));
                ur().undo();
            }
            assertEquals("undo2: data", "ab", d.getText(0, d.getLength()));
            ur().undo();
            endChunk(d, ce);
            assertEquals("undo3: data", "", d.getText(0, d.getLength()));
            ur().redo();
            assertEquals("redo1: data", "ab", d.getText(0, d.getLength()));
            ur().redo();
            endChunk(d, ce);
            if (!documentSupportsUndoMergingOfWords()) {
                assertEquals("redo2: data", "abc", d.getText(0, d.getLength()));
                ur().redo();
            }
            assertEquals("redo3: data", "abcd", d.getText(0, d.getLength()));
        } finally {
            enableWarning(level);
        }
    }

    public void testUndoRedoWhileActiveChunk() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        CompoundEdit ce = beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        assertEquals("before undo: data", "ab", d.getText(0, d.getLength()));

        ur().undo();

        // These asserts assume that an undo in the middle of a chunk
        // is an undo on the whole chunk so far.

        assertEquals("after undo: data", "", d.getText(0, d.getLength()));
        assertFalse("after undo: not modified", support.isModified());
        assertFalse("after undo: no undo", ur().canUndo());
        assertTrue("after undo: can redo", ur().canRedo());

        // note still in the chunk.

        ur().redo();
        assertEquals("after redo: data", "ab", d.getText(0, d.getLength()));
        assertTrue("after redo: modified", support.isModified());
        assertTrue("after redo: can undo", ur().canUndo());
        assertFalse("after redo: no redo", ur().canRedo());

        ur().undo(); 
        assertEquals("after undo: data", "", d.getText(0, d.getLength()));

        // note still in the chunk.

        d.insertString(d.getLength(), "c", null);
        d.insertString(d.getLength(), "d", null);
        endChunk(d, ce);

        assertEquals("after endChunk: data", "cd", d.getText(0, d.getLength()));
        assertTrue("after endChunk: modified", support.isModified());
        assertTrue("after endChunk: can undo", ur().canUndo());
        assertFalse("after endChunk: no redo", ur().canRedo());

        
        ur().undo();
        assertEquals("undo after endChunk: data", "", d.getText(0, d.getLength()));
        assertFalse("undo after endChunk: not modified", support.isModified());
        assertFalse("undo after endChunk: no undo", ur().canUndo());
        assertTrue("undo after endChunk: can redo", ur().canRedo());
    }

    public void testSaveDocumentWhileActiveChunkCommon(boolean doFailCase) throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        CompoundEdit ce = beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        support.saveDocument (); // creates a separate undoable chunk
        assertFalse("save: not modified", support.isModified());
        assertTrue("save: can undo", ur().canUndo());
        assertFalse("save: no redo", ur().canRedo());

        d.insertString(d.getLength(), "c", null);
        d.insertString(d.getLength(), "d", null);
        endChunk(d, ce);

        assertEquals("insert, after save: data", "abcd", d.getText(0, d.getLength()));
        assertTrue("insert, after save: modified", support.isModified());
        assertTrue("insert, after save: can undo", ur().canUndo());
        assertFalse("insert, after save: no redo", ur().canRedo());

        ur().undo();
        assertEquals("undo, at save: data", "ab", d.getText(0, d.getLength()));
        assertFalse("undo, at save: not modified", support.isModified());
        assertTrue("undo, at save: can undo", ur().canUndo());
        assertTrue("undo, at save: can redo", ur().canRedo());

        ur().undo();
        assertEquals("undo, before save: data", "", d.getText(0, d.getLength()));

        if(doFailCase) {
            // ****************************************************************
            // CES BUG???
            assertTrue("undo, before save: modified", support.isModified());
            // ****************************************************************
        }

        assertFalse("undo, before save: can undo", ur().canUndo());
        assertTrue("undo, before save: can redo", ur().canRedo());

        ur().redo();
        assertEquals("redo, at save: data", "ab", d.getText(0, d.getLength()));
        assertFalse("redo, at save: not modified", support.isModified());
        assertTrue("redo, at save: can undo", ur().canUndo());
        assertTrue("redo, at save: can redo", ur().canRedo());
    }

    public void testSaveDocumentWhileActiveChunk() throws Exception {
        testSaveDocumentWhileActiveChunkCommon(false);
    }

    // This fails, below is "testSaveDocumentErrorCase" without chunking,
    // it also fails.
    // public void testSaveDocumentWhileActiveChunkErroCase() throws Exception {
    //     testSaveDocumentWhileActiveChunkCommon(true);
    // }

    public void testNestedChunks() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        CompoundEdit ce1 = beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        CompoundEdit ce2 = beginChunk(d); // creates a separate undoable chunk

        d.insertString(d.getLength(), "c", null);
        d.insertString(d.getLength(), "d", null);

        endChunk(d, ce1);

        d.insertString(d.getLength(), "e", null);
        d.insertString(d.getLength(), "f", null);

        endChunk(d, ce2);

        assertEquals("data", "abcdef", d.getText(0, d.getLength()));

        // following fails if nesting not supported
        ur().undo();
        assertEquals("undo1", "abcd", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo2", "ab", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo3", "", d.getText(0, d.getLength()));
    }

    public void testNestedEmpyChunks() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        // should have no effect
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "e", null);
        d.insertString(d.getLength(), "f", null);

        endChunk(d);

        assertEquals("data", "abef", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo3", "", d.getText(0, d.getLength()));
    }

    public void testNestedEmpyChunks2() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        // should have no effect
        beginChunk(d);
        beginChunk(d);
        endChunk(d);
        endChunk(d);
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "e", null);
        d.insertString(d.getLength(), "f", null);

        endChunk(d);

        assertEquals("data", "abef", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo3", "", d.getText(0, d.getLength()));
    }

    public void testNestedEmpyChunks3() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        beginChunk(d);
        d.insertString(d.getLength(), "c", null);

        // should have no effect
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "d", null);
        endChunk(d);

        // should have no effect
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "e", null);

        // should have no effect
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "f", null);

        // should have no effect
        beginChunk(d);
        endChunk(d);

        d.insertString(d.getLength(), "g", null);

        endChunk(d);

        assertEquals("data", "abcdefg", d.getText(0, d.getLength()));

        // following fails if nesting not supported
        ur().undo();
        assertEquals("undo1", "abcd", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo2", "ab", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo3", "", d.getText(0, d.getLength()));
    }

    public void testMarkCommitGroup() throws Exception {
        content = "";
        StyledDocument d = support.openDocument();
        beginChunk(d);
        d.insertString(d.getLength(), "a", null);
        d.insertString(d.getLength(), "b", null);

        markChunk(d); // creates a separate undoable chunk

        d.insertString(d.getLength(), "c", null);
        d.insertString(d.getLength(), "d", null);

        markChunk(d);

        d.insertString(d.getLength(), "e", null);
        d.insertString(d.getLength(), "f", null);

        endChunk(d);

        assertEquals("data", "abcdef", d.getText(0, d.getLength()));

        // following fails if nesting not supported
        ur().undo();
        assertEquals("undo1", "abcd", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo2", "ab", d.getText(0, d.getLength()));

        ur().undo();
        assertEquals("undo3", "", d.getText(0, d.getLength()));
    }
    
    protected boolean documentSupportsUndoMergingOfWords() {
        return false;
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            @Override
            public void close () throws java.io.IOException {
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

    public void markModified() throws java.io.IOException {
        if (cannotBeModified != null) {
            IOException e = new IOException ();
            Exceptions.attachLocalizedMessage(e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private final class CES extends CloneableEditorSupport {
        public boolean plain;
        
        
        public CES (Env env, org.openide.util.Lookup l) {
            super (env, l);
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

        protected javax.swing.text.EditorKit createEditorKit() {
            if (plain) {
                return super.createEditorKit ();
            } else {
                return UndoRedoWrappingCooperationTest.this.createEditorKit ();
            }
        }
    } // end of CES

}
