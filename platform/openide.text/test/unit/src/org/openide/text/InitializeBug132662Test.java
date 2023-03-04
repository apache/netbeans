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
import java.io.IOException;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

import org.openide.text.Line.Set;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class InitializeBug132662Test extends NbTestCase implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InitializeBug132662Test.class);
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
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    
    /** Creates new UndoRedoTest */
    public InitializeBug132662Test(String m) {
        super(m);
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        support = new CES(this, Lookup.EMPTY);
        MockServices.setServices(AnnoProv.class);
        AnnoProv.called = false;
    }
    
    

    public void testInitializeOnBackground() throws Exception {
        AnnoProv.c = support;
        support.open();
        
        AnnoProv.waitOpen();
        
        assertTrue("Called", AnnoProv.called);
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
    private final class CES extends CloneableEditorSupport implements Runnable {
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
            return new K();
        }

        public void run() {
        }
    } // end of CES
    private static final class K extends NbLikeEditorKit {
/* Uncomment this code to simulate the deadlock with mimelookup that uses two locks
        @Override
        public synchronized Document createDefaultDocument() {
            return super.createDefaultDocument();
        }

        @Override
        public synchronized Void call() throws Exception {
            synchronized (new JPanel().getTreeLock()) {
            }
            return super.call();
        }
 */
    }
    
    public static final class AnnoProv implements AnnotationProvider, Runnable {
        static boolean called;
        static CES c;
        static StyledDocument doc;
        static StyledDocument doc2;
        
        public void annotate(Set set, Lookup context) {
            try {
                c.notifyClosed();
                RequestProcessor.getDefault().post(this);
                Thread.sleep(300);
                doc = c.openDocument();

                synchronized (AnnoProv.class) {
                    called = true;
                    AnnoProv.class.notifyAll();
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        static synchronized void waitOpen() throws InterruptedException {
            while (!called) {
                AnnoProv.class.wait();
            }
        }

        public void run() {
            try {
                doc2 = c.openDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        
    }
}
