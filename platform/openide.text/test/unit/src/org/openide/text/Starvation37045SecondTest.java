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
import org.netbeans.junit.*;
import org.openide.util.Exceptions;


/**
 * Exception during load of the document can cause starvation
 * in the thread that waits for that to happen.
 *
 * @author  Jaroslav Tulach
 */
public class Starvation37045SecondTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CES support;
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    /** Creates new TextTest */
    public Starvation37045SecondTest (String s) {
        super(s);
    }
    
    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    @RandomlyFails // NB-Core-Build #8260
    public void testTheStarvation37045 () throws Exception {
        org.openide.util.Task task;
        
        synchronized (this) {
            support.prepareDocument ().waitFinished ();
            
            task = org.openide.util.RequestProcessor.getDefault ().post (support);
            // wait for the support (another thread) to try to open and block
//            wait ();
/*
            // now post there another task
            task = org.openide.util.RequestProcessor.getDefault ().post (support);
            // wait for it to block, any amount of time is likely to do it
            Thread.sleep (500);
*/            
            // notify the first edit(), to continue (and throw exception)
            notify ();
        }

        // check for deadlock
        for (int i = 0; i < 5; i++) {
            if (task.isFinished ()) break;
            Thread.sleep (500);
        }
        
        // uncomment the next line if you want to see real starvation threaddump
        task.waitFinished ();
        assertTrue ("Should be finished, but there is a starvation", task.isFinished ());
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
        throw new OutOfMemoryError("Ha ha ha");
        // return new java.io.ByteArrayInputStream (content.getBytes ());
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
    private final class CES extends CloneableEditorSupport 
    implements Runnable {
        private boolean wait = false;
        
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
        
        public void run () {
            boolean firstTime = wait;
            try {
                edit ();
                if (firstTime) {
                    fail ("It should throw an exception");
                }
            } catch (IllegalStateException ex) {
                if (!firstTime) throw ex;
                assertEquals ("Name of exception is correct", "Let's pretend that I am broken!!!", ex.getMessage ());
            }
        }
        
    } // end of CES
}
