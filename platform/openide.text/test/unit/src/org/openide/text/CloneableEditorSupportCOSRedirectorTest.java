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

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableOpenSupportRedirector;

/**
 * based on functionality from CloneableEditorSupportRedirectorTest
 * @author Vladimir Voskresensky
 */
public class CloneableEditorSupportCOSRedirectorTest extends NbTestCase
implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloneableEditorSupportCOSRedirectorTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the content of lookup of support */
    private InstanceContent ic;
    private Redirector red;
    private DialogDisplayer dd;

    
    // Env variables
    private final StringBuilder content;
    private boolean modified = false;
    private final java.util.Date date = new java.util.Date ();
    private java.util.List<PropertyChangeListener> propL = new java.util.ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;

    
    
    public CloneableEditorSupportCOSRedirectorTest(String testName) {
        super(testName);
        this.content = new StringBuilder("");
    }

    public CloneableEditorSupportCOSRedirectorTest(StringBuilder content) {
        super("");
        this.content = content;
    }
    
    protected void setUp () {
        ic = new InstanceContent ();
        CES support = new CES (this, new AbstractLookup(ic));
        
        MockServices.setServices(Redirector.class, DD.class);
        red = Lookup.getDefault().lookup(Redirector.class);
        assertNotNull(red);
        dd = Lookup.getDefault().lookup(DD.class);
        assertNotNull(dd);

        CloneableEditorSupportCOSRedirectorTest t = new CloneableEditorSupportCOSRedirectorTest(this.content);
        red.master = support;
        InstanceContent slave = new InstanceContent();
        red.slave = new CES(t, new AbstractLookup (slave));
        slave.add(red.master);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
        red.master.close();
        red.slave.close();     
        content.delete(0, content.length());
    }
        
    public void testSameDocument() throws Exception {    
        red.master.open();
        red.slave.open();
        javax.swing.text.Document doc = red.slave.openDocument ();
        assertNotNull (doc);
        
        assertSame(doc, red.master.getDocument());
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Same text as in the stream", content.toString(), s);
        
        assertFalse ("No redo", red.slave.getUndoRedo ().canRedo ());
        assertFalse ("No undo", red.slave.getUndoRedo ().canUndo ());
    }
    
    public void testLineLookupIsPropagated () throws Exception {
        content.append("Line1\nLine2\n");
        red.master.open();
        red.slave.open();
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
        content.append("goes\nto\nInputStream");
        red.master.open();
        red.slave.open();
        String added = "added before\n";
        javax.swing.text.Document doc = red.master.openDocument ();
        assertNotNull (doc);
        
        // modify the document
        doc.insertString(0, added, null);
        compareStreamWithString(red.master.getInputStream(), added + content);
        compareStreamWithString(red.slave.getInputStream(), added + content);
    }
    
    public void testDocumentCanBeGarbageCollectedWhenClosed () throws Exception {
        content.append("Ahoj\nMyDoc");
        red.master.open();
        red.slave.open();
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

    public void testAfterOpenOfSlaveThereArePanesAndEvent() throws Exception {
        red.master.open();
        final AtomicBoolean wasNonEmtpyOpenedPanesEvent = new AtomicBoolean(false);
        final AtomicBoolean emtpyPanes = new AtomicBoolean(false);
        final AtomicBoolean nonEmtpyPanes = new AtomicBoolean(false);
        
        PropertyChangeListener l = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                    wasNonEmtpyOpenedPanesEvent.set(red.slave.getOpenedPanes() != null);
                }
            }

        };
        red.slave.addPropertyChangeListener(l);
        
        class Check implements Runnable {
            @Override
            public void run() {
                // editor not yet opened, attach listener and open from there
                emtpyPanes.set(red.slave.getOpenedPanes() == null);
                red.slave.open();
                nonEmtpyPanes.set(red.slave.getOpenedPanes() != null);
            }
        }
        Check check = new Check();
        
        SwingUtilities.invokeAndWait(check);
        red.slave.removePropertyChangeListener(l);
        assertTrue("No panes are open before red.slave.open", emtpyPanes.get());
        assertTrue("Some panes are now open after red.slave.open", nonEmtpyPanes.get());
        assertTrue("PROP_OPENED_PANES event was not fired", wasNonEmtpyOpenedPanesEvent.get());
    }

    public void testGetEditorKit() {
        EditorKit kit = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("EditorKit should never be null", kit);
        // There shouldn't be any EK registered and we should get the default one
        assertEquals("Wrong default EditorKit", "org.openide.text.CloneableEditorSupport$PlainEditorKit", kit.getClass().getName());
    }
    
    private void compareStreamWithString(InputStream is, CharSequence s) throws Exception{
        int i;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        byte b1[] = baos.toByteArray();
        byte b2[] = s.toString().getBytes();
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
        return new java.io.ByteArrayInputStream (content.toString().getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            @Override
            public void close () throws java.io.IOException {
                super.close ();
                content.append(new String (toByteArray ()));
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return true;
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
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }

        @Override
        protected boolean asynchronousOpen() {
            return true;
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

    
    public static final class Redirector extends CloneableOpenSupportRedirector {
        CES master;
        CES slave;

        @Override
        protected CloneableOpenSupport redirect(CloneableOpenSupport.Env env) {
            if (env == slave.cesEnv()) {
                return master;
            }
            return null;
        }

        @Override
        protected void opened(CloneableOpenSupport.Env env) {
        }

        @Override
        protected void closed(CloneableOpenSupport.Env env) {
        }
    }
    /**
     * Our own dialog displayer when modified CES is closed we agree to close it.
     */
    public static final class DD extends DialogDisplayer {

        public static Object[] options;
        public static Object toReturn;
        public static boolean disableTest;

        public java.awt.Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException("Not implemented");
        }

        public Object notify(NotifyDescriptor descriptor) {
            return NotifyDescriptor.CLOSED_OPTION;
        }
    } // end of DD    
}
