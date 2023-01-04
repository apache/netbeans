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

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorSupportTest extends NbTestCase
implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloneableEditorSupportTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
        System.setProperty("assertgc.paths", "0");
    }
    /** the support to work with */
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;
    /** Delay in miliseconds to simulate delay between closing stream and file modification time. */
    private long delay = 0L;

    public void testSaveAfterLoadOfFileThatWasModifiedInFuture() throws Exception {
        content = "Ahoj\nMyDoc";
        // simulate the the just opened file has modification time in future
        // intentionally extremly big to make it work even in debugger
        date = new Date(System.currentTimeMillis() + 86400000);
        
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);

        doc.remove(0, doc.getLength());

        support.saveDocument();
        
        doc.getProperty(Object.class);

        assertEquals("Saved ok and empty", "", content);
    }

    public void testDocCanBeGCdWhenNotModifiedButOpened() throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        WeakReference<Object> ref = new WeakReference<Object>(doc);
        doc = null;
        
        assertGC ("Document can dissapear", ref, Collections.singleton(support));

        assertTrue ("Can be closed without problems", support.close ());
        assertFalse ("Document is not loaded", support.isDocumentLoaded ());
    }
    
    public void testDocumentIsNotGCedIfModified () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        doc.insertString (0, "Zmena", null);

        assertTrue ("Is modified", support.isModified ());

        WeakReference<Object> ref = new WeakReference<Object>(doc);
        doc = null;

        boolean ok;
        try {
            assertGC ("Should fail", ref);
            ok = false;
        } catch (AssertionFailedError expected) {
            ok = true;
        }
        if (!ok) {
            fail ("Document should not disappear, as it is modified");
        }
        
        assertTrue ("Document remains loaded", support.isDocumentLoaded ());

    }
    
    @RandomlyFails // http://deadlock.netbeans.org/hudson/job/NB-Core-Build/9880/testReport/
    public void testDocumentIsNotGCedIfOpenedInEditor () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        support.open();
        class R implements Runnable {
            JEditorPane[] arr;
            public void run() {
                arr = support.getOpenedPanes();
            }
            
            public JEditorPane[] getArr() throws Exception {
                SwingUtilities.invokeAndWait(this);
                return arr;
            }
        }
        R panes = new R();
        assertNotNull("There is one pane", panes.getArr());
        
        assertFalse("Not modified", support.isModified ());
        
        WeakReference<Object> ref = new WeakReference<Object>(doc);
        doc = null;

        boolean ok;
        try {
            assertGC ("Should fail", ref);
            ok = false;
        } catch (AssertionFailedError expected) {
            ok = true;
        }
        if (!ok) {
            fail ("Document should not disappear, as it is modified");
        }
        
        assertTrue ("Document remains loaded", support.isDocumentLoaded ());
        
        support.close();
        
        assertNull("There is no pane", panes.getArr());
        assertGC ("Should succeed with GC now", ref);
    }
    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<java.beans.PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;

    
    public CloneableEditorSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }
    
    public void testDocumentCanBeRead () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Same text as in the stream", content, s);
        
        assertFalse ("No redo", support.getUndoRedo ().canRedo ());
        assertFalse ("No undo", support.getUndoRedo ().canUndo ());
    }
    
    public void testLineLookupIsPropagated () throws Exception {
        content = "Line1\nLine2\n";
        Integer template = new Integer (1);
        ic.add (template); // put anything into the lookup
        
        // in order to set.getLines() work correctly, the document has to be loaded
        support.openDocument();
        
        Line.Set set = support.getLineSet();
        java.util.List list = set.getLines();
        assertEquals ("Three lines", 3, list.size ());
        
        Line l = (Line)list.get (0);
        Integer i = l.getLookup().lookup(Integer.class);
        assertEquals ("The original integer", template, i);
        ic.remove (template);
        i = l.getLookup().lookup(Integer.class);
        assertNull ("Lookup is dynamic, so now there is nothing", i);
    }

    public void testGetInputStream () throws Exception {
        content = "goes\nto\nInputStream";
        String added = "added before\n";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        // modify the document
        doc.insertString(0, added, null);
        compareStreamWithString(support.getInputStream(), added + content);
    }
    
    public void testGetInputStreamWhenClosed () throws Exception {
        content = "basic\ncontent";
        compareStreamWithString(support.getInputStream(), content);
        // we should be doing this with the document still closed 
        assertNull("The document is supposed to be still closed", support.getDocument ());
    }
    
    public void testDocumentCannotBeModified () throws Exception {
        content = "Ahoj\nMyDoc";
        cannotBeModified = "No, you cannot modify this document in this test";
        
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        assertFalse ("Nothing to undo", support.getUndoRedo ().canUndo ());
        
        // this should not be allowed
        try {
            doc.insertString (0, "Kuk", null);
            fail("Modification should not proceed");
        } catch (BadLocationException ex) {
            // Expected
        }
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("The document is now the same as at the begining", content, s);
        
        assertEquals ("Message has been shown to user in status bar", cannotBeModified, org.openide.awt.StatusDisplayer.getDefault ().getStatusText ());
    }
    
    public void testDocumentCanBeGarbageCollectedWhenClosed () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        assertTrue ("Document is loaded", support.isDocumentLoaded ());
        assertTrue ("Can be closed without problems", support.close ());
        assertFalse ("Document is not loaded", support.isDocumentLoaded ());
        
        WeakReference<Object> ref = new WeakReference<Object>(doc);
        doc = null;
        
        assertGC ("Document can dissapear", ref);
    }

    /**
     * Tests that the wrapEditorComponent() method returns the passed
     * parameter (doesn't wrap the passed component in some additional UI).
     */
    public void testWrapEditorComponent() {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        assertSame(support.wrapEditorComponent(panel), panel);
    }

    public void testSaveWhenNoDocumentOpen() throws IOException {
        modified = true;
        support.saveDocument();
    }

    public void testGetEditorKit() {
        EditorKit kit = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("EditorKit should never be null", kit);
        // There shouldn't be any EK registered and we should get the default one
        assertEquals("Wrong default EditorKit", "org.openide.text.CloneableEditorSupport$PlainEditorKit", kit.getClass().getName());
    }

    /** Tests that UserQuestionException is thrown when saving externally modified document. */
    public void testSaveExternallyModified() throws Exception {
        content = "Ahoj\nMyDoc";
        support.openDocument();
        modified = true;
        // simulate external modification
        date = new Date(System.currentTimeMillis() + 100);
        try {
            support.saveDocument();
            fail("UserQuestionException should be thrown because of external modification.");
        } catch (UserQuestionException e) {
            // OK, exception expected.
        }
    }

    /** Tests that UserQuestionException is NOT thrown when document is not externally
     * modified but there is some delay between closing stream and setting file modification time.
     * See issue 149069.
     */
    public void testSaveNotExternallyModified() throws Exception {
        content = "Ahoj\nMyDoc";
        support.openDocument();
        modified = true;
        // intentionally extremly big to make it work even in debugger
        delay = 86400000;
        support.saveDocument();
        modified = true;
        try {
            support.saveDocument();
        } catch (UserQuestionException e) {
            fail("UserQuestionException should NOT be thrown (see #149069).");
        }
    }

    private void compareStreamWithString(InputStream is, String s) throws Exception{
        int i;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        byte b1[] = baos.toByteArray();
        byte b2[] = s.getBytes();
        assertTrue("Same bytes as would result from the string: " + s, Arrays.equals(b1, b2));
    }
    
    public void testDocumentBeforeSaveRunnableProcessed() throws Exception {
        content = "Ahoj\nMyDoc";
        final javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        final boolean[] processed = { false };
        doc.putProperty("beforeSaveRunnable", new Runnable() {
            public void run() {
                // See CloneableEditorSupport for property explanation
                Runnable beforeSaveStart = (Runnable) doc.getProperty("beforeSaveStart");
                if (beforeSaveStart != null) {
                    beforeSaveStart.run();
                }

                processed[0] = true;

                // See CloneableEditorSupport for property explanation
                Runnable beforeSaveEnd = (Runnable) doc.getProperty("beforeSaveEnd");
                if (beforeSaveEnd != null) {
                    beforeSaveEnd.run();
                }
            }
        });
        doc.insertString(0, "Nazdar", null); // Modify doc to allow save
        support.saveDocument();
        assertTrue("CES.saveDocument() did not execute a runnable in \"beforeSaveRunnable\" document property",
                processed[0]);
    }
    
    public void testPrepareDocument() throws Exception {
        content = "Ahoj\nMyDoc";
        Task task = support.prepareDocument();
        task.waitFinished();

        try {
            Object o = new Object();
            assertGC("", new WeakReference<Object>(o));
        } catch (AssertionFailedError e) {
            // ignore, intentional
        }

        Document doc = support.getDoc();
        assertNotNull("Document should not be GCed while its loading task exists", doc);

        Task task2 = support.prepareDocument();
        assertTrue("Loading task should be finished", task2.isFinished());
        assertNotSame("Expecting different task instance", task, task2);
        task2 = null;
        
        Reference<Task> taskRef = new WeakReference<Task>(task);
        Reference<Document> docRef = new WeakReference<Document>(doc);
        task = null;
        doc = null;
        assertGC("Can't GC document loading task", taskRef);
        assertGC("Can't GC document", docRef);
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
            public void close() throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
                date = new Date(System.currentTimeMillis() + delay);
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
}
