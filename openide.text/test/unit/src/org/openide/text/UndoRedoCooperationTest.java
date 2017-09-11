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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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



import java.io.IOException;
import javax.swing.text.*;
import org.netbeans.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Emulating old UndoRedo manager deadlock.
 *
 * @author  Jaroslav Tulach
 */
public class UndoRedoCooperationTest extends NbTestCase implements CloneableEditorSupport.Env {
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
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    /** Creates new TextTest */
    public UndoRedoCooperationTest (String s) {
        super(s);
    }
    
    protected javax.swing.text.EditorKit createEditorKit() {
        return new NbLikeEditorKit();
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    public void testOneThreadDoingEditsOneThreadDoingReverts () throws Exception {
        final StyledDocument d = support.openDocument ();
        d.insertString (0, "Ahoj\n", null);
        assertTrue ("We can do undo now", support.getUndoRedo ().canUndo ());
        
        class Blocker implements javax.swing.event.DocumentListener {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                synchronized (UndoRedoCooperationTest.this) {
                    UndoRedoCooperationTest.this.notify ();
                    try {
                        UndoRedoCooperationTest.this.wait ();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        }
        d.addDocumentListener (new Blocker ());
        
        
        class Run implements Runnable {
            public void run () {
                try {
                    d.insertString (2, "Kuk", null);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        class Undo implements Runnable {
            public void run () {
                support.getUndoRedo ().undo ();                
                support.getUndoRedo ().undo ();                
                assertFalse (support.getUndoRedo ().canUndo ());
                assertTrue (support.getUndoRedo ().canRedo ());
            }
        }
        
        RequestProcessor.Task t1, t2;
        synchronized (this) {
            t1 = new RequestProcessor ("Inserting into document").post (new Run ());
            wait ();
            // now the inserting thread is blocked in EditThatCanBlockInAddEditMethod
            t2 = new RequestProcessor ("Doing undo").post (new Undo ());
            
            // wait a while till one of the undos is called
            Thread.sleep (100);
            // let the insert into document continue
            notify ();
        }
        
        // there should be a deadlock
        t1.waitFinished ();
        t2.waitFinished ();
    }
    
    public void testDeadlock8692 () throws Exception {
        doTest (0);
    }
    
    public void testUndoRedo () throws Exception {
        doTest (1000);
    }
    
    private void doTest (int sleep) throws Exception {
        final StyledDocument d = support.openDocument ();
        d.insertString (0, "Ahoj\n", null);
        support.saveDocument ();
        assertFalse ("Previous save make it non-modified", support.isModified ());
        
        cannotBeModified = "My reason";
        
        class R implements Runnable {
            private Exception ex;
            
            public void run () {
                try {
                    d.remove (0, 2);
                } catch (BadLocationException ex) {
                    this.ex = ex;
                }
            }
        }

        R r = new R ();
        NbDocument.runAtomic (d, r);

        if (sleep > 0) {
            Thread.sleep (sleep);
        }
        
        //
        // anyway we need to wait till all posted AWT tasks are finished
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { 
            public void run () { 
                // just wait
            }
        });
        
        assertEquals ("Text contains orignal version", "Aho", d.getText (0, 3));
    }
    
    public void testUndoMustBePossibleWithPlainDocument () throws Exception {
        support.plain = true;
        
        final StyledDocument d = support.openDocument ();

        assertTrue ("Document is not empty", d.getLength () > 0);
        
        d.remove (0, d.getLength ());
        String s = d.getText (0, d.getLength ());
        assertEquals ("The document is empty", "", s);
        
        assertTrue ("There is something to undo", support.getUndoRedo ().canUndo ());
        support.getUndoRedo ().undo ();

        s = d.getText (0, d.getLength ());
        assertEquals ("Contains the original content", content, s);
    }
    
    public void testClearTheMap() throws Exception {
        support.plain = false;
        
        final StyledDocument d = support.openDocument ();
        
        support.getUndoRedo().discardAllEdits();

        assertTrue ("Document is not empty", d.getLength () > 0);
        
        cannotBeModified = "Cannot";
        try {
            d.remove (0, d.getLength ());
        } catch (BadLocationException be) { /* expected */ }
        
        String s = d.getText (0, d.getLength ());
        assertEquals ("The document is the same", "Hello", s);
    }
    
    public void testEmptyRunAtomic() throws Exception {
        content = "";
        final StyledDocument d = support.openDocument ();
        d.insertString(0, "a", null);
        assertTrue(support.isModified());
        // Run empty runnable which should call notifyModify() followed by
        // notifyUnmodified()
        NbDocument.runAtomic(d, new Runnable() {
            public void run() {
                // Do nothing
            }
        });
        assertTrue("Empty runAtomic() must not reset the modified flag", support.isModified());
    }

    public void testCanUndoDoesNotMarkDocumentUnmodified() throws Exception {
        content = "";
        final StyledDocument d = support.openDocument ();
        d.insertString(0, "a", null);
        assertTrue(support.isModified());
        assertTrue(support.getUndoRedo().canUndo());
        assertTrue("canUndo() must not reset the modified flag", support.isModified());
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
                return UndoRedoCooperationTest.this.createEditorKit ();
            }
        }
    } // end of CES

}
