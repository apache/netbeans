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

import java.util.Date;
import java.beans.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;

/**
 * Tries closing a document while holding a read lock and adding
 * a position reference from other thread.
 *
 * This test fails if switched to writeLock instead of readLock
 *
 * @author  Petr Nejedly
 */
public class Deadlock49178Test extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    boolean inWait = false;
    boolean shouldWait = true;
    Object waitLock = new Object();
    
    /** the support to work with */
    private CES support;

    // Env variables
    private String content = "Hello";
    private boolean valid = true;
    private boolean modified = false;
    private Date date = new Date ();
    private transient PropertyChangeSupport prop = new PropertyChangeSupport(this);
    private transient VetoableChangeSupport veto = new VetoableChangeSupport(this);
    private Exception exception;
    
    boolean processingDone = false;
    boolean closingDone = false;
    
    
    public Deadlock49178Test(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(Deadlock49178Test.class));
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
        
    public void testDeadlock49178() throws Exception {
	// open the document
        final StyledDocument docu = support.openDocument();

        // Perform a modification so that notifyModify() gets called as supposed by the test
        // Otherwise notifyUnmodified() would not be called (there's no reason to call it in such case).
        docu.insertString(0, "a", null);

	// start closing it
	Thread closing = new Thread(new Runnable() { public void run() {
            support.close(false); // will block in notifyUnmodified()
	    closingDone = true;
	}});
	closing.start();

        Thread processing = new Thread(new Runnable() {
            boolean second = false;
            
            public void run() {
                if (!second) {
                    second = true;
                    docu.render(this);
//                    NbDocument.runAtomic(docu, this);
                } else { // inside readLock
                    support.createPositionRef(0, Position.Bias.Forward);
                    processingDone = true;
                }
            }
        });

        
        synchronized(waitLock) {
	    while (!inWait) waitLock.wait();
        }

        processing.start();
        
        Thread.sleep(1000);
        synchronized(waitLock) {
            shouldWait = false;
            waitLock.notifyAll();
        }
	
	closing.join(10000);
        processing.join(10000);
	assertNull("No exception thrown", exception);
	assertTrue("Closing thread finished", closingDone);
	assertTrue("Processing thread finished", processingDone);
    }
    

    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        prop.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        prop.removePropertyChangeListener (l);
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
        veto.addVetoableChangeListener (l);
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {
        veto.removeVetoableChangeListener (l);
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
	return new ByteArrayInputStream(content.getBytes());
    }
    
    public java.io.OutputStream outputStream() throws java.io.IOException {
        return new ByteArrayOutputStream();
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

        protected void notifyUnmodified () {
            super.notifyUnmodified();
            
            synchronized(waitLock) {
		inWait = true;
		waitLock.notifyAll();
	        try {
	            while (shouldWait) waitLock.wait();
		} catch (InterruptedException e) {
		   exception = e;
		}
	    }
        }
        
        
        protected StyledDocument createStyledDocument (EditorKit kit) {
            return new Doc(); // Why the FilterDocument doesn't support WriteLockable?
        }

        class Doc extends DefaultStyledDocument implements NbDocument.WriteLockable {
            public void runAtomic (Runnable r) {
                writeLock();
                try {
                    r.run();
                } finally {
                    writeUnlock();
                }
            }
            
            public void runAtomicAsUser (Runnable r) throws BadLocationException {
                runAtomic(r);
            }    
        }

    } // end of CES

}
