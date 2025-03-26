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
import java.lang.ref.WeakReference;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorSupportRedirectorTest extends NbTestCase
implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloneableEditorSupportRedirectorTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the content of lookup of support */
    private InstanceContent ic;
    Redirector red;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<PropertyChangeListener> propL = new java.util.ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;

    
    
    public CloneableEditorSupportRedirectorTest(String testName) {
        super(testName);
    }
    
    protected void setUp () {
        ic = new InstanceContent ();
        CES support = new CES (this, new AbstractLookup(ic));
        
        MockServices.setServices(Redirector.class);
        red = Lookup.getDefault().lookup(Redirector.class);
        assertNotNull(red);

        CloneableEditorSupportRedirectorTest t = new CloneableEditorSupportRedirectorTest("");
        red.master = support;
        InstanceContent slave = new InstanceContent();
        red.slave = new CES(t, new AbstractLookup (slave));
        slave.add(red.master);
    }
    
    public void testSameDocument() throws Exception {
        javax.swing.text.Document doc = red.slave.openDocument ();
        assertNotNull (doc);
        
        assertSame(doc, red.master.getDocument());
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Same text as in the stream", content, s);
        
        assertFalse ("No redo", red.slave.getUndoRedo ().canRedo ());
        assertFalse ("No undo", red.slave.getUndoRedo ().canUndo ());
    }
    
    public void testLineLookupIsPropagated () throws Exception {
        content = "Line1\nLine2\n";
        Integer template = new Integer (1);
        ic.add (template); // put anything into the lookup
        
        // in order to set.getLines() work correctly, the document has to be loaded
        red.master.openDocument();
        
        Line.Set set = red.master.getLineSet();
        assertSame("Same lines", set, red.slave.getLineSet());
        java.util.List list = set.getLines();
        assertEquals ("Three lines", 3, list.size ());
        
        Line l = (Line)list.get (0);
        Integer i = l.getLookup ().lookup (Integer.class);
        assertEquals ("The original integer", template, i);
        ic.remove (template);
        i = l.getLookup ().lookup (Integer.class);
        assertNull ("Lookup is dynamic, so now there is nothing", i);
    }
    
    
    public void testGetInputStream () throws Exception {
        content = "goes\nto\nInputStream";
        String added = "added before\n";
        javax.swing.text.Document doc = red.master.openDocument ();
        assertNotNull (doc);
        
        // modify the document
        doc.insertString(0, added, null);
        compareStreamWithString(red.master.getInputStream(), added + content);
        compareStreamWithString(red.slave.getInputStream(), added + content);
    }
    
    public void testGetInputStreamWhenClosed () throws Exception {
        content = "basic\ncontent";
        compareStreamWithString(red.master.getInputStream(), content);
        compareStreamWithString(red.slave.getInputStream(), content);
        // we should be doing this with the document still closed 
        assertNull("The document is supposed to be still closed", red.master.getDocument ());
    }
    
    public void testDocumentCannotBeModified () throws Exception {
        content = "Ahoj\nMyDoc";
        cannotBeModified = "No, you cannot modify this document in this test";
        
        javax.swing.text.Document doc = red.master.openDocument ();
        assertNotNull (doc);
        
        assertFalse ("Nothing to undo", red.master.getUndoRedo ().canUndo ());
        
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
        javax.swing.text.Document doc = red.master.openDocument ();
        assertNotNull (doc);
        
        assertTrue ("Document is loaded", red.master.isDocumentLoaded ());
        assertTrue ("Document is loaded", red.slave.isDocumentLoaded ());
        assertTrue ("Can be closed without problems", red.slave.close ());
        assertFalse ("Document is not loaded", red.master.isDocumentLoaded ());
        assertFalse ("Document is not loaded", red.slave.isDocumentLoaded ());
        
        WeakReference<?> ref = new WeakReference<Document>(doc);
        doc = null;
        
        assertGC ("Document can dissapear", ref);
    }

    /**
     * Tests that the wrapEditorComponent() method returns the passed
     * parameter (doesn't wrap the passed component in some additional UI).
     */
    public void testWrapEditorComponent() {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        assertSame(red.master.wrapEditorComponent(panel), panel);
        assertSame(red.slave.wrapEditorComponent(panel), panel);
    }

    public void testAfterOpenOfSlaveThereAreMasterPanes() throws Exception {
        red.slave.open();
        
        class Check implements Runnable {
            public void run() {
                assertTrue("Some panes are now open", red.master.getOpenedPanes() != null);
            }
        }
        Check check = new Check();
        
        SwingUtilities.invokeAndWait(check);
    }

    public void testGetEditorKit() {
        EditorKit kit = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("EditorKit should never be null", kit);
        // There shouldn't be any EK registered and we should get the default one
        assertEquals("Wrong default EditorKit", "org.openide.text.CloneableEditorSupport$PlainEditorKit", kit.getClass().getName());
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
    
    //
    // Implementation of the CloneableEditorred.master.Env
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
        return red.master;
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

    
    public static final class Redirector extends CloneableEditorSupportRedirector {
        CES master;
        CES slave;
    
        protected CloneableEditorSupport redirect(Lookup ces) {
            return ces.lookup(CloneableEditorSupport.class);
        }
}
}
