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
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

import org.openide.text.Line.Set;
import org.openide.util.Lookup;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class LoadMultipleDocsTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(LoadMultipleDocsTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private MockEnv env1;
    private MockEnv env2;
    
    
    /** Creates new UndoRedoTest */
    public LoadMultipleDocsTest(String m) {
        super(m);
    }

    @Override
    protected void setUp() throws Exception {
        env1 = new MockEnv();
        env2 = new MockEnv();
        MockAnnoProvider.open = null;
        MockAnnoProvider.doc = null;
    }
    
    

    public void testInitializeAndWaitForAnotherDocument() throws Exception {
        MockServices.setServices(MockAnnoProvider.class);
        MockAnnoProvider.open = env2;
        
        env1.support.open();
        
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = env1.support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        assertNotNull(r.p);
        
        Document doc = MockAnnoProvider.getDoc();
        assertNotNull("Other document is also opened", doc);
    }

    public void testInitializeAndWaitForSameDocument() throws Exception {
        MockServices.setServices(MockAnnoProvider.class);
        MockAnnoProvider.open = env1;
        
        env1.support.open();
        
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = env1.support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        assertNotNull(r.p);
        
        Document doc = MockAnnoProvider.getDoc();
        assertNotNull("The same document is also opened in anntation processor", doc);
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    private static class MockEnv implements CloneableEditorSupport.Env {
        // Env variables
        private String content = "";
        private boolean valid = true;
        private boolean modified = false;
        private java.util.Date date = new java.util.Date();
        private java.util.List<java.beans.PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
        private java.beans.VetoableChangeListener vetoL;
        private CES support;
        
        public MockEnv() {
            support = new CES(this, Lookup.EMPTY);
        }

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
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
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
            return super.createEditorKit();
        }
    } // end of CES

    public static final class MockAnnoProvider implements AnnotationProvider {
        static MockEnv open;
        private static Object doc;
        
        static synchronized Document getDoc() throws IOException {
            if (!(doc instanceof Document)) {
                try {
                    MockAnnoProvider.class.wait(3000);
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
            if (doc instanceof Document) {
                return (Document)doc;
            } else if (doc == null) {
                throw new IOException("Time out " + doc);
            } else {
                throw (IOException)doc;
            }
        }
        
        public void annotate(Set set, Lookup context) {
            if (open != null) {
                CES ces = open.support;
                synchronized (MockAnnoProvider.class) {
                    try {
                        doc = ces.openDocument();
                    } catch (IOException ex) {
                        doc = ex;
                    } finally {
                        MockAnnoProvider.class.notifyAll();
                    }
                }
            }
        }
        
    }
}

