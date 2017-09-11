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


import java.lang.reflect.*;
import javax.swing.text.StyledDocument;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.awt.UndoRedo;
import org.openide.util.RequestProcessor;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class UndoRedoTest extends NbTestCase implements CloneableEditorSupport.Env {
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
    public UndoRedoTest(Method m) {
        super(m.getName ());
        method = m;
    }
  
    public static junit.framework.TestSuite suite () throws Exception {
        Method[] arr = UndoRedo.Manager.class.getMethods ();
        
        NbTestSuite suite = new NbTestSuite ();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getDeclaringClass () == javax.swing.undo.UndoManager.class) {
                suite.addTest (new UndoRedoTest (arr[i]));
            }
        }
        return suite;
    }
    
    protected void runTest () throws Exception {
        support = new CES (this, org.openide.util.Lookup.EMPTY);

        assertNull ("The document is closed", support.getDocument ());
        
        UndoRedo ur = support.getUndoRedo ();
        
        Object[] args = new Object[0];
        
        if (method.getParameterTypes ().length == 1) {
            if (method.getParameterTypes ()[0] == javax.swing.undo.UndoableEdit.class) {
                args = new Object[] { new FakeEdit () };
            }
            if (method.getParameterTypes ()[0] == Integer.TYPE) {
                args = new Object[] { new Integer (30) };
            }
        }
        
        if (! "end".equals (method.getName ())) {
            try {
                // invoking of the method work
                method.invoke (ur, args);
            } catch (InvocationTargetException ex) {
                if (
                    ex.getTargetException () instanceof javax.swing.undo.CannotUndoException ||
                    ex.getTargetException () instanceof javax.swing.undo.CannotRedoException
                ) {
                    // ok
                } else {
                    throw ex;
                }
            }
        }
        
        
        final StyledDocument doc = support.openDocument ();
        doc.insertString (0, "First edit\n", null);
        doc.insertString (1, "Second", null);
        doc.remove (0, 5);

        // this might improve the situation for #47022
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { 
            public void run () { 
                // just wait
            }
        });

        assertTrue ("We did edits, we need to be able to do undo", ur.canUndo ());
        
        ur.undo ();
        
        assertTrue ("There is something to undo", ur.canUndo ());
        assertTrue ("There is something to redo", ur.canRedo ());

        
        class Blocker implements javax.swing.event.DocumentListener, Runnable {
            public long time;
            
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                synchronized (this) {
                    this.notify ();
                    try {
                        long time = System.currentTimeMillis ();
                        this.wait (100);
                        time = System.currentTimeMillis () - time;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
            
            public void run () {
                // the thread that modifies the document to block it
                try {
                    doc.insertString (0, "Kuk", null);
                } catch (javax.swing.text.BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Blocker blocker = new Blocker ();
        doc.addDocumentListener (blocker);
        synchronized (blocker) {
            RequestProcessor.getDefault ().post (blocker);
            blocker.wait ();
        }
    
        try {
            method.invoke (ur, args);
        } catch (InvocationTargetException ex) {
            if (
                ex.getTargetException () instanceof javax.swing.undo.CannotRedoException
            ) {
                // well, sometimes redo is not possible, so skip it
            } else {
                throw ex;
            }
        }
        
        synchronized (blocker) {
            blocker.notify ();
        }
        if (blocker.time > 50) {
            fail ("The method " + method + " should finish sooner than in " + blocker.time + " ms");
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
