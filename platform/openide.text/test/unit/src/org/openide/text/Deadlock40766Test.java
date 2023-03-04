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



import java.io.IOException;
import javax.swing.text.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Exception during load of the document can cause starvation
 * in the thread that waits for that to happen.
 *
 * @author  Petr Nejedly, Jaroslav Tulach
 */
public class Deadlock40766Test extends NbTestCase implements CloneableEditorSupport.Env {
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
    public Deadlock40766Test(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(Deadlock40766Test.class));
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    RequestProcessor my = new RequestProcessor("my");
    
    public void testDeadlock40766() throws Exception {
        org.openide.util.Task task;
        
        synchronized (support.helperLock) {
            my.post (support);
            // wait for the support (another thread) to try to open and block
            support.helperLock.wait ();
        }
        
        // now the RP if after the doc test but have not locked
        // let's change the state now.
        
        StyledDocument doc = support.openDocument();
        
        synchronized (support.helperLock) {
            // wait till it gets into support lock
            support.helperLock.notifyAll();
            support.helperLock.wait (1000);
        }

        // now the RP holds lock but doesn't have doc readAccess
        
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                synchronized (support.helperLock) {
                    support.helperLock.notifyAll();
                }
                
                try {
                    support.openDocument();
                } catch (IOException ioe) {
                    fail(ioe.getMessage());
                }
                
            }
        });
        
    }
    
    /* #38013 was a deadlock where:
     * 1) one thread started adding PositionRef before the document was loaded,
     *    slept for a while on a synchronized and awakened with newly load
     *    documet that it needed to readlock.
     * 2) second thread loaded the document and wanted to convert positions
     *    from inside its writelock.
     *
     * Reproduction:
     * 1. Start thread A, let it try to add PositionRef without a doc.
     *    As soon as it acquires PR$M lock, switch to B
     * 2. Start thread B, wait for A thread's rendezvous, start loading
     *    document (which spawns thread C)
     *    after 1000ms unblock thread A (C should already have locked document)
     */
    public void testDeadlock38013() throws Exception {
        org.openide.util.Task task;

        // this is thread B
        
        synchronized (support.helperLock) {
            my.post (support); // thread A
            support.helperLock.wait ();
            // we've got the beforeLock notification, we need the "after" one
            // so let's respin the locks
            support.helperLock.notifyAll();
            support.helperLock.wait ();
        }
        //now, B have the RP.M's lock and we have 1000 ms to lock
        // the document and try to get PR.M's lock from C
                
        StyledDocument doc = support.openDocument();
        
    }

    public void testCreatePositionCanBeCalledFromWriteLockOnDocument () throws Exception {
        final StyledDocument doc = support.openDocument ();
        
        class R implements Runnable {
            boolean inAtomic;
            PositionRef ref;
            
            public void run () {
                if (!inAtomic) {
                    inAtomic = true;
                    NbDocument.runAtomic (doc, this);
                    return;
                }
                
                synchronized (this) {
                    notifyAll ();
                    try {
                        wait (1000);
                    } catch (InterruptedException ex) {
                        fail (ex.getMessage ());
                    }
                }
                ref = support.createPositionRef (0, Position.Bias.Backward);
            }
        }
        
        RequestProcessor.Task task;
        R r = new R ();
        synchronized (r) {
            task = RequestProcessor.getDefault ().post (r);
            r.wait ();
        }
        
        // now R holds write lock on the document, and will wake up soon
        // grab the lock from oposite site
        PositionRef ref = support.createPositionRef (1, Position.Bias.Backward);
        
        assertNotNull ("Ref created", ref);
        task.waitFinished ();
        assertNotNull ("Ref1 crated", r.ref);
        
    } // end of testCreatePositionCanBeCalledFromWriteLockOnDocument
    
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
    private final class CES extends CloneableEditorSupport  implements Runnable {
        
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
        
        
        protected StyledDocument createStyledDocument (EditorKit kit) {
            class Doc extends DefaultStyledDocument implements NbDocument.WriteLockable {
                public void runAtomic (Runnable r) {
                    writeLock();
                    try {
                        r.run();
                    } finally {
                        writeUnlock();
                    }
                }
                public void runAtomicAsUser (Runnable r) {
                    runAtomic(r);
                }
            }
            StyledDocument sd = new Doc();
            return sd;
        }

        Object helperLock = new Object();

        void howToReproduceDeadlock40766(boolean beforeLock) {
            if (my.isRequestProcessorThread()) {
                synchronized(helperLock) {
                    try {
                        helperLock.notifyAll();
                        helperLock.wait(1000);
                    } catch (InterruptedException ie) {
                        fail (ie.getMessage ());
                    }
                }
            }
        }
        
        public void run () {
            createPositionRef(0, Position.Bias.Forward);
        }

    } // end of CES
}
