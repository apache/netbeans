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

import java.beans.PropertyChangeSupport;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class NotifyModifiedTest extends NbTestCase
implements CloneableEditorSupport.Env {
    private static final Logger err = Logger.getLogger(NotifyModifiedTest.class.getName());
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.text.NotifyModifiedTest$Lkp");
    }

    /** the support to work with */
    protected CES support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private volatile boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private PropertyChangeSupport propL = new PropertyChangeSupport (this);
    private java.beans.VetoableChangeListener vetoL;
    private boolean shouldVetoNotifyModified;
    /** kit to create */
    private javax.swing.text.EditorKit editorKit;
    
    public NotifyModifiedTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
  
    @Override
    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }
    
    //
    // overwrite editor kit
    //
    
    protected javax.swing.text.EditorKit createEditorKit () {
        return null;
    }
    
    protected void checkThatDocumentLockIsNotHeld () {
    }

    protected void doesVetoedInsertFireBadLocationException (javax.swing.text.BadLocationException e) {
        // Since 6.43 (UndoRedoManager addition) the CES installs DocumentFilter (that prevents modification)
        // to any AbstractDocument-based document including javax.swing.text.PlainDocument so: if (e == null) => fail
        if (e == null) {
            fail("On non-nblike documents, vetoed insert does not generate BadLocationException");
        }
    }
    
    //
    // test methods
    //

    
    public void testJustOneCallToModified () throws Exception {
        content = "Line1\nLine2\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        doc.insertString (3, "Ahoj", null);
        assertEquals ("One modification", 1, support.notifyModified);

        doc.insertString (7, "Kuk", null);
        assertEquals ("Still one modification", 1, support.notifyModified);

        doc.remove (7, 3);
        assertEquals ("Still one modification2", 1, support.notifyModified);
        
        support.saveDocument (); Thread.sleep(300);
        assertEquals ("Marked unmodified", 1, support.notifyUnmodified);

        doc.remove (0, 1);
        assertEquals ("Modifies again", 2, support.notifyModified);
    }
    
    public void testTheDocumentReturnsBackIfModifyIsNotAllowed () throws Exception {
        content = "Nic\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        shouldVetoNotifyModified = true;
        
        // should be reverted in SwingUtilities.invokeLater
        try {
            doc.insertString (0, "Ahoj", null);
            // Previous insert should fail with exception
            doesVetoedInsertFireBadLocationException (null);
        } catch (javax.swing.text.BadLocationException e) {
            // Expecting the thrown exception
            doesVetoedInsertFireBadLocationException (e);
        }
        waitEQ ();
        
        assertEquals ("One modification called (but it was vetoed)", 1, support.notifyModified);
        assertEquals ("No unmodification called", 0, support.notifyUnmodified);

        String first = doc.getText (0, 1);
        assertEquals ("First letter is N", "N", first);
    }
    
    public void testTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes () throws Exception {
        class R implements Runnable {
            int[] i = { 0 };
            Document[] doc = { null };
            
            public void run () {
                try {
                    if (i[0] % 2 == 0) {
                        doc[0].insertString (0, "Ahoj", null);
                    } else {
                        doc[0].remove (0, 2);
                    }
                    // Previous insert should fail with exception
                    doesVetoedInsertFireBadLocationException (null);
                } catch (javax.swing.text.BadLocationException e) {
                    // Expecting the thrown exception
                    doesVetoedInsertFireBadLocationException (e);
                }
            }
        }
        
        R r = new R ();
        doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (r.i, r.doc, r);
    }

    public void testTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimesInAtomicSection () throws Exception {
        class R implements Runnable {
            int[] i = { 0 };
            javax.swing.text.StyledDocument[] doc = { null };
            
            private boolean inAtomic;
            
            public void run () {
                if (!inAtomic) {
                    inAtomic = true;
                    NbDocument.runAtomic(doc[0], this);
                    inAtomic = false;
                } else {
                    try {
                        doc[0].insertString (0, "Ahoj", null);
                    } catch (javax.swing.text.BadLocationException e) {
                        // Expected - Since 6.43 (UndoRedoManager addition) the CES installs DocumentFilter (that prevents modification)
                        // to any AbstractDocument-based document including javax.swing.text.PlainDocument
                        // so even individual modifications inside atomic runnable fail.
//                        fail ("Inside atomic no BadLocationException due to unmodifiable source");
                    }
                }
            }
        }
        
        R r = new R ();
        doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (r.i, r.doc, r);
    }
    
    /** Passing parameters by reference - e.g. arrays of size 1, so [0] can be filled and changed... */
    private void doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (int[] i, Document[] doc, Runnable op) throws Exception {
        content = "EmptyContentForTheDocument\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        doc[0] = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        shouldVetoNotifyModified = true;
     
        for (i[0] = 0; i[0] < 10; i[0]++) {
            // do operation that will be forbidden
            op.run ();
            waitEQ ();
            
            support.assertModified (false, "Is still unmodified");
        }
        
        String first = doc[0].getText (0, doc[0].getLength ());
        if (!first.equals (content)) {
            fail ("Expected: " + content + 
                  " but was: " + first);
        }
        
        assertEquals ("Vetoed modifications", 10, support.notifyModified);
        assertEquals ("No unmodification called", 0, support.notifyUnmodified);
    }
    
    
    public void testBadLocationException () throws Exception {
        content = "Nic\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        try {
            doc.insertString (10, "Ahoj", null);
            fail ("This should generate bad location exception");
        } catch (javax.swing.text.BadLocationException ex) {
            // ok
        }
        
        // Since JDK-11 the Swing Implementation checks boundary first before
        // calling into document filters -> we don't see an (attempted)
        // modification
        assertEquals (0 + " modification called (but it was vetoed)", 0, support.notifyModified);
        assertEquals (0 + " unmodification called", 0, support.notifyUnmodified);

        String first = doc.getText (0, 1);
        assertEquals ("First letter is N", "N", first);
    }
    
    public void testDoModificationsInAtomicBlock () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        
        class R implements Runnable {
            public void run () {
                try {
                    doc.insertString (0, "Ahoj", null);
                } catch (javax.swing.text.BadLocationException ex) {
                    AssertionFailedError e = new AssertionFailedError (ex.getMessage ());
                    e.initCause (ex);
                    throw e;
                }
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);
        
        assertEquals ("One modification", 1, support.notifyModified);
        assertEquals ("no unmod", 0, support.notifyUnmodified);
    }

    public void testDoModificationsInAtomicBlockAndRefuseThem () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        shouldVetoNotifyModified = true;
        
        class R implements Runnable {
            public boolean gotIntoRunnable;
            
            public void run () {
                gotIntoRunnable = true;
                
                try {
                    doc.insertString (0, "Ahoj", null);
                    fail("Unexpected to pass the insertString() due vetoed notifyModify()");
                    doc.remove (0, 1);
                    doc.remove (0, 1);
                } catch (javax.swing.text.BadLocationException ex) {
                    // Expected - since 6.43 (UndoRedoManager addition) the CES installs DocumentFilter (that prevents modification)
                    // so even individual modifications (inside runAtomic() will fail with BLE
//                    AssertionFailedError e = new AssertionFailedError (ex.getMessage ());
//                    e.initCause (ex);
//                    throw e;
                }
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);
        waitEQ ();
        
        // Since 6.43 (UndoRedoManager addition) if doc supports "supportsModificationListener" property
        // it is required to call notifyModify() before writeLock that spans atomic modification
        // (NbLikeEditorKit.Doc.insOrRemoveOrRunnable() was fixed to adhere.

        assertTrue ("Runable should be started", r.gotIntoRunnable);

        if (support.notifyModified == 0) {
            fail ("At least One notification expected");
        }
        assertEquals ("no unmod", 0, support.notifyUnmodified);
        
        support.assertModified (false, "Document is not modified");
        
        String text = doc.getText (0, doc.getLength ());
        assertEquals ("The text is the same as original content", content, text);
    }
    
    public void testRevertModificationAfterSave () throws Exception {
        content = "Ahoj";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();
        
        doc.insertString (4, " Jardo", null);
        doc.insertString (0, ":", null);

        String text = doc.getText (0, doc.getLength ());

        support.saveDocument (); Thread.sleep(300);
        support.assertModified (false, "Not modified");
        
        shouldVetoNotifyModified = true;
        try {
            doc.remove (0, 5);
            doesVetoedInsertFireBadLocationException (null);
        } catch (BadLocationException ex) {
            doesVetoedInsertFireBadLocationException (ex);
        }
        waitEQ ();
        
        support.assertModified (false, "Not modified");

        text = doc.getText (0, doc.getLength ());
        if (!":Ahoj Jardo".equals (text)) {
            fail ("The text as after save ':Ahoj Jardo' but was: " + text);
        }
    }
    
    public void testAtomicBlockWithoutModifications () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        class R implements Runnable {
            public void run () {
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);

        assertEquals ("The same number of modification and unmodifications", support.notifyModified, support.notifyUnmodified);
        assertEquals ("Actually it is zero", 0, support.notifyUnmodified);
    }
    
    
    
    public void testDoInsertAfterEmptyBlock () throws Exception {
        testAtomicBlockWithoutModifications (); // may produce notifyModify (see impl)
        
        support.getDocument ().insertString (0, "Ahoj", null);
        
        assertEquals ("One modification now", 1, support.notifyModified);
        assertEquals ("No unmodified", 0, support.notifyUnmodified);
        support.assertModified (true, "Is modified");
    }

    public void testDoRemoveAfterEmptyBlock () throws Exception {
        testAtomicBlockWithoutModifications (); // may produce notifyModify (see impl)
        
        support.getDocument ().remove (0, 4);
        
        assertEquals ("One modification now", 1, support.notifyModified);
        assertEquals ("No unmodified", 0, support.notifyUnmodified);
        support.assertModified (true, "Is modified");
    }
    
    public void testAtomicBlockWithoutModificationsAfterInsert () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (false, 1);
    }
    public void testAtomicBlockWithoutModificationsAfterInsertDouble () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (false, 2);
    }
    public void testAtomicUserBlockWithoutModificationsAfterInsert () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (true, 1);
    }
    public void testAtomicUserBlockWithoutModificationsAfterInsertDouble () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (true, 2);
    }
    
    private void doAtomicBlockWithoutModificationsAfterInsert (final boolean asUser, final int cnt) throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        doc.insertString(0, "Ahoj", null);
        
        class R implements Runnable {
            
            public int counter = cnt;
            
            public void run () {
                if (--counter > 0) {
                    if (asUser) {
                        try {
                            NbDocument.runAtomicAsUser(doc, this);
                        } catch (javax.swing.text.BadLocationException ex) {
                            throw (AssertionFailedError)new AssertionFailedError (ex.getMessage()).initCause(ex);
                        }
                    } else {
                        NbDocument.runAtomic(doc, this);
                    }
                }
            }
        }
        
        R r = new R ();
        
        support.assertModified (true, "Document must be modified");
        
        if (asUser) {
            NbDocument.runAtomicAsUser(doc, r);
        } else {
            NbDocument.runAtomic (doc, r);
        }

        support.assertModified (true, "Document must stay modified");
    }
    
    public void testUndoDoesMarkFileAsDirtyIssue56963 () throws Exception {
        content = "Somecontent";
        
        err.info("Going to open");
        final javax.swing.text.StyledDocument doc = support.openDocument();
        err.info("Opened: " + doc);

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        err.info("Going to remove " + len + " characters");
        doc.remove (0, len);
        err.info("Removed");Thread.sleep(300);
        
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        err.info("Going to save");
        support.saveDocument (); Thread.sleep(300);
        waitEQ ();
        err.info("Saved");

        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        
        err.info("Going to undo");
        support.getUndoRedo ().undo ();
        waitEQ ();
        err.info("Undoed");
        
        assertEquals ("Lengh it back", len, doc.getLength ());
        assertEquals ("Content is back", "Somecontent", doc.getText (0, len));
        
        err.info("Before assertModified");
        support.assertModified (true, "Document is Modified");

        err.info("Before redo");
        support.getUndoRedo ().redo ();
        waitEQ ();
        err.info("After redo");
        
        assertEquals ("Zero length", 0, doc.getLength ());
        
        support.assertModified (false, "Document is UnModified");
    }
    
    public void testReloadWithoutModifiedIssue57104 () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.saveDocument (); Thread.sleep(300);
        waitEQ ();
        
        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        
        
        content = "Newcontent";
        int newLen = content.length ();
        
        assertEquals ("Once modified", 1, support.notifyModified);
        assertEquals ("Once unmodified after save", 1, support.notifyUnmodified);
        
        propL.firePropertyChange (PROP_TIME, null, null);

        Thread.sleep(200); // Wait until reload EDT preparation task gets scheduled
        waitEQ ();

        Object newDoc = support.openDocument ();
        assertSame ("Reload does not change the document", newDoc, doc);
        
        assertEquals ("Length it new", newLen, doc.getLength ());
        assertEquals ("Content is new", "Newcontent", doc.getText (0, newLen));

        // getUndoRedo().discardAllEdits(); in CES around line 1848 uses doc.runAtomic() => may call notifyModify()
        int expectedCount = 1;
        assertEquals ("Modified", expectedCount, support.notifyModified);
        assertEquals ("Unmodified", expectedCount, support.notifyUnmodified);
    }

    public void testUndoMarksFileUnmodified () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        support.assertModified (true, "Document is modified");
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.getUndoRedo ().undo ();
        
        support.assertModified (false, "Document is unmodified");
    }    
    
    public void testReloadWhenModifiedIssue57104 () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));

        err.info("wait so first modification really happens later in time then the lastSaveTime is set to");
        Thread.sleep(300);
        
        doc.remove (0, len);
        
        err.info("After remove");
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        err.info("Before save");
        Thread.sleep(300); support.saveDocument (); Thread.sleep(300);
        waitEQ ();
        err.info("After save");
        
        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        assertEquals ("Once modified", 1, support.notifyModified);
        assertEquals ("Once unmodified after save", 1, support.notifyUnmodified);

        err.info("Before undo");
        support.getUndoRedo ().undo ();
        waitEQ ();
        err.info("After undo");
        
        assertEquals ("Lengh it back", len, doc.getLength ());
        assertEquals ("Content is back", "Somecontent", doc.getText (0, len));
        
        waitEQ ();
        support.assertModified (true, "Document is Modified");
        
        assertEquals ("One more modified", 2, support.notifyModified);
        assertEquals ("No unmodifications", 1, support.notifyUnmodified);

        
        content = "Newcontent";
        int newLen = content.length ();
        
        // does the reload
        propL.firePropertyChange (PROP_TIME, null, null);

        Thread.sleep(100); // Wait till reload will start processing
        waitEQ (); // Wait till EDT part of the reload is over

        Object newDoc = support.openDocument ();
        assertSame ("Reload does not change the document", newDoc, doc);
        
        assertEquals ("Length it new", newLen, doc.getLength ());
        assertEquals ("Content is new", "Newcontent", doc.getText (0, newLen));

        assertEquals ("No more modified", 2, support.notifyModified);
        assertEquals ("But one more unmodified", 2, support.notifyUnmodified);
    }
    
    private void waitEQ () throws Exception {
        // repeat five times to handle also runnables started from AWT
        for (int i = 0; i < 5; i++) {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () { } });
        }
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.addPropertyChangeListener (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.removePropertyChangeListener (l);
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
                date = new Date();
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
        //new Exception ("markModified: " + modified).printStackTrace(System.out);
        // The document is expected to hold write-lock when markModified() is called - this change
        // allows to get rid of extra unmarkModified() done when an atomic-lock section
        // does not perform any document modification.
//        checkThatDocumentLockIsNotHeld ();
    }
    
    public void unmarkModified() {
        modified = false;
        //new Exception ("unmarkModified: " + modified).printStackTrace(System.out);
//        checkThatDocumentLockIsNotHeld ();
    }
    
    /** Implementation of the CES */
    protected final class CES extends CloneableEditorSupport {
        public int notifyUnmodified;
        public int notifyModified;
        
        public CES (Env env, Lookup l) {
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
        
        @Override
        protected void notifyUnmodified () {
            notifyUnmodified++;
//            Exceptions.printStackTrace(new java.lang.Exception("notifyUnmodified: " + notifyUnmodified));
            
            super.notifyUnmodified();
        }

        @Override
        protected boolean notifyModified () {
            notifyModified++;
            
            if (shouldVetoNotifyModified) {
                return false;
            }
            
//            Exceptions.printStackTrace(new java.lang.Exception("notifyModified: " + notifyModified));
            
            boolean retValue;            
            retValue = super.notifyModified();
            return retValue;
        }
        
        @Override
        protected javax.swing.text.EditorKit createEditorKit() {
            javax.swing.text.EditorKit k = NotifyModifiedTest.this.createEditorKit ();
            if (k != null) {
                return k;
            } 
            return super.createEditorKit ();
        }

        public void assertModified (boolean modified, String msg) {
            assertEquals (msg, modified, isModified ());
        }
    }

}
