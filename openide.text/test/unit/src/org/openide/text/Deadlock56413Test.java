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
import java.io.File;
import java.io.IOException;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.text.CloneableEditorSupport;
import org.openide.text.FilterDocument;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

/**
 * Try firing a PROP_TIME while the document is just being loaded.
 *
 * @author  Petr Nejedly
 */
public class Deadlock56413Test extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    boolean inCreateKit = false;
    boolean shouldWaitInCreate = true;
    Object kitLock = new Object();
    
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
    
    boolean loaded = false;
    
    
    public Deadlock56413Test(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(Deadlock56413Test.class));
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
        
    public void testDeadlock56413() throws Exception {
        // prime the event queue
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {public void run() {}});
        
        // start loading, will register a listener for PROP_TIME
	// and start a thread fetching the bits
	Thread loading = new Thread(new Runnable() { public void run() {
	    try {
	        StyledDocument docu = support.openDocument();
                loaded = true;
	    } catch (IOException ioe) {
		exception = ioe;
	    }
	}});
	loading.start();
	
	// as soon as it gets to createEditorKit, fire PROP_TIME
	synchronized(kitLock) {
	    while (!inCreateKit) kitLock.wait();
	    
	    // XXX: fire
	    setNewTime(new Date());
	    // let the reload thread lock the doc and block on COS$L
	    // no possible hook there
	    Thread.sleep(2000); 
	    
	    // allow kit to continue
	    shouldWaitInCreate = false;
	    kitLock.notifyAll();
	}
	
	loading.join(10000);
	assertNull("No exception thrown", exception);
	assertTrue("Loading finished", loaded);
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
    
    void setNewTime(Date d) {
	date = d;
        prop.firePropertyChange (PROP_TIME, null, null);
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
	
        protected StyledDocument createStyledDocument (EditorKit kit) {
            StyledDocument doc = super.createStyledDocument(kit);
            
            // have to store the field before unfusing the other thread
            // in normal conditions, the store would happen just on return.
            
            // CES.setDoc() no longer exists so it can't be called.
            // Test passes without the call so leaving it in current state for now.
//            try {
//                java.lang.reflect.Method f = CloneableEditorSupport.class.getDeclaredMethod("setDoc", StyledDocument.class, boolean.class);
//                f.setAccessible(true);
//                f.invoke(this, doc, true);
//            } catch (Exception e) {
//                exception = e;
//            }
            
	    synchronized(kitLock) {
		inCreateKit = true;
		kitLock.notifyAll();
	        try {
	            while (shouldWaitInCreate) kitLock.wait();
		} catch (InterruptedException e) {
		   exception = e;
		}
	    }

            return doc;
        }

    } // end of CES

}
