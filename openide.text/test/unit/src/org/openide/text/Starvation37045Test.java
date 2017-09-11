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


import java.io.IOException;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;


/**
 * Exception during load of the document can cause starvation
 * in the thread that waits for that to happen.
 *
 * @author  Jaroslav Tulach
 */
public class Starvation37045Test extends NbTestCase
implements CloneableEditorSupport.Env {
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
    public Starvation37045Test (String s) {
        super(s);
    }
    
    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    @RandomlyFails // NB-Core-Build #8241
    public void testTheStarvation37045 () throws Exception {
        org.openide.util.Task task;
        
        synchronized (this) {
            org.openide.util.RequestProcessor.getDefault ().post (support);
            // wait for the support (another thread) to try to open and block
            wait ();

            // now post there another task
            task = org.openide.util.RequestProcessor.getDefault ().post (support);
            // wait for it to block, any amount of time is likely to do it
            Thread.sleep (500);
            
            // notify the first edit(), to continue (and throw exception)
            notify ();
        }

        // check for deadlock
        for (int i = 0; i < 5; i++) {
            if (task.isFinished ()) break;
            Thread.sleep (500);
        }
        
        // uncomment the next line if you want to see real starvation threaddump
        // task.waitFinished ();
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
        private boolean wait = true;
        
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

        protected javax.swing.text.EditorKit createEditorKit () {
            if (wait) {
                synchronized (Starvation37045Test.this) {
                    wait = false;
                    try {
                        Starvation37045Test.this.notifyAll ();
                        Starvation37045Test.this.wait ();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        fail (ex.getMessage ());
                    }
                }
                throw new IllegalStateException ("Let's pretend that I am broken!!!");
            } 
            return super.createEditorKit ();
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
