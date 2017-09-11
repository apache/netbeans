/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
