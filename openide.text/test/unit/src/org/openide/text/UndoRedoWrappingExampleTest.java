/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.openide.text;


import java.lang.reflect.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CompoundEdit;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.awt.UndoRedo;
import org.openide.util.RequestProcessor;


/** Example of an edit that encapsulates more edits into one.
 *
 * @author  Jaroslav Tulach
 */
public class UndoRedoWrappingExampleTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the method of manager that we are testing */
    private Method method;
    
    private CES support;
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    
    /** Creates new UndoRedoTest */
    public UndoRedoWrappingExampleTest(String s) {
        super(s);
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    public void testDeleteEachTenthCharFromDocument() throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                sb.append((char)('0' + j));
            }
        }
        
        content = sb.toString();
        
        StyledDocument doc = support.openDocument();
        assertEquals("200 chars there", 200, doc.getLength());
        
        CompoundEdit bigEdit = new CompoundEdit();
        support.getUndoRedo().undoableEditHappened(new UndoableEditEvent(doc, bigEdit));
        
        assertTrue("Big edit will consume other edits", bigEdit.isInProgress());
        
        for (int i = 199; i >= 0; i -= 10) {
            doc.remove(i, 1);
        }
        
        assertTrue("Big edit was still in consume mode", bigEdit.isInProgress());
        bigEdit.end();
        assertFalse("Big edit is over", bigEdit.isInProgress());
        assertTrue("Document is modified", modified);
        assertTrue("We can undo", support.getUndoRedo().canUndo());
        
        if (doc.getText(0, doc.getLength()).indexOf('9') != -1) {
            fail("There should be no 9 in the doc:\n" + doc.getText(0, doc.getLength()));
        }
        
        support.getUndoRedo().undo();
        
        assertEquals("Again 200", 200, doc.getLength());
        assertFalse("Not modified anymore", modified);

        
        assertTrue("We can redo", support.getUndoRedo().canRedo());
        support.getUndoRedo().redo();
        
        assertTrue("Document is modified", modified);
        assertTrue("We can undo", support.getUndoRedo().canUndo());
        
        if (doc.getText(0, doc.getLength()).indexOf('9') != -1) {
            fail("There should be no 9 in the doc:\n" + doc.getText(0, doc.getLength()));
        }
        
    }

    public void testDeleteEachTenthCharOnModifiedDocument() throws Exception {
        
        StyledDocument doc = support.openDocument();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                sb.append((char)('0' + j));
            }
        }
        assertEquals("empty", 0, doc.getLength());
        doc.insertString(0, sb.toString(), null);
        assertEquals("200 chars there", 200, doc.getLength());
        
        CompoundEdit bigEdit = new CompoundEdit();
        support.getUndoRedo().undoableEditHappened(new UndoableEditEvent(doc, bigEdit));
        
        assertTrue("Big edit will consume other edits", bigEdit.isInProgress());
        
        for (int i = 199; i >= 0; i -= 10) {
            doc.remove(i, 1);
        }
        
        assertTrue("Big edit was still in consume mode", bigEdit.isInProgress());
        bigEdit.end();
        assertFalse("Big edit is over", bigEdit.isInProgress());
        assertTrue("Document is modified", modified);
        assertTrue("We can undo", support.getUndoRedo().canUndo());
        
        if (doc.getText(0, doc.getLength()).indexOf('9') != -1) {
            fail("There should be no 9 in the doc:\n" + doc.getText(0, doc.getLength()));
        }
        
        support.getUndoRedo().undo();
        
        assertEquals("Again 200", 200, doc.getLength());
        assertTrue("Still modified", modified);

        
        assertTrue("We can redo", support.getUndoRedo().canRedo());
        support.getUndoRedo().redo();
        
        assertTrue("Document is modified", modified);
        assertTrue("We can undo", support.getUndoRedo().canUndo());
        
        if (doc.getText(0, doc.getLength()).indexOf('9') != -1) {
            fail("There should be no 9 in the doc:\n" + doc.getText(0, doc.getLength()));
        }
        
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
                return new NbLikeEditorKit ();
            }
        }
    } // end of CES

    private static final class FakeEdit implements javax.swing.undo.UndoableEdit {
        public boolean addEdit(javax.swing.undo.UndoableEdit anEdit) {
            return false;
        }

        public boolean canRedo() {
            return true;
        }

        public boolean canUndo() {
            return true;
        }

        public void die() {
        }

        public java.lang.String getPresentationName() {
            return "";
        }

        public java.lang.String getRedoPresentationName() {
            return "";
        }

        public java.lang.String getUndoPresentationName() {
            return "";
        }

        public boolean isSignificant() {
            return false;
        }

        public void redo() throws javax.swing.undo.CannotRedoException {
        }

        public boolean replaceEdit(javax.swing.undo.UndoableEdit anEdit) {
            return true;
        }

        public void undo() throws javax.swing.undo.CannotUndoException {
        }
        
    } // end of UndoableEdit
}
