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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;

import org.netbeans.junit.RandomlyFails;
import org.openide.text.InitializeInAWTTest.FindActionCheck;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
@RandomlyFails // NB-Core-Build #9876, #9889, #9924, #9968, #10010
public class InitializeOnBackgroundTest extends NbTestCase implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InitializeOnBackgroundTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the method of manager that we are testing */
    
    private CES support;
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<java.beans.PropertyChangeListener> propL = new java.util.ArrayList<java.beans.PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;
    private FindActionCheck find;
    
    
    /** Creates new UndoRedoTest */
    public InitializeOnBackgroundTest(String m) {
        super(m);
    }

    @Override
    protected boolean runInEQ() {
        return getName().contains("AWT");
    }

    @Override
    protected void setUp() throws Exception {
        support = new CES(this, Lookup.EMPTY);
        find = new FindActionCheck();
    }

    @Override
    protected void tearDown() throws Exception {
        find.assertAction();
    }
    
    
    @RandomlyFails // NB-Core-Build #1981, #1984
    public void testInitializeOnBackground() throws Exception {
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        assertNotNull(r.p);
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }
    
    public void testInitializeOnBackgroundInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        r.run();
        assertNotNull(r.p);
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }

    public void testInitializeAndBlockInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    run();
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }
    
    
    public void testQueryDocumentInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            Document doc;
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    final TopComponent atc = WindowManager.getDefault().getRegistry().getActivated();
                    if (atc instanceof CloneableEditor) {
                        CloneableEditor ed = (CloneableEditor)atc;
                        p = ed.getEditorPane();
                        doc = ed.getEditorPane().getDocument();
                    }
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
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
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private final class CES extends CloneableEditorSupport {
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

        @Override
        protected javax.swing.text.EditorKit createEditorKit() {
            return new InitializeInAWTTest.K();
        }
    } // end of CES
}
