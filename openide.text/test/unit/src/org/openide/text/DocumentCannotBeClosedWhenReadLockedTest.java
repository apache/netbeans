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



import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.text.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.util.Exceptions;

/**
 * Simulates issue 46981. Editor locks the document, but somebody else closes it
 * while it is working on it and a deadlock occurs.
 * @author  Petr Nejedly, Jaroslav Tulach
 */
public class DocumentCannotBeClosedWhenReadLockedTest extends NbTestCase implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DocumentCannotBeClosedWhenReadLockedTest.class);
    }

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
    
    CountDownLatch unmarkModifiedStarted = new CountDownLatch(1);
    
    CountDownLatch unmarkModifiedFinished = new CountDownLatch(1);

    /** Creates new TextTest */
    public DocumentCannotBeClosedWhenReadLockedTest(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DocumentCannotBeClosedWhenReadLockedTest.class));
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    public void testReadLockTheDocumentAndThenTryToCreateAPositionInItMeanWhileLetOtherThreadCloseAComponent () throws Exception {
        StyledDocument doc = support.openDocument ();
        final CloneableEditorSupport.Pane pane = support.openAt (support.createPositionRef (0, javax.swing.text.Position.Bias.Forward), 0);
        final CountDownLatch awtWorkStarted = new CountDownLatch(1);
        final CountDownLatch awtWorkFinished = new CountDownLatch(1);
        assertNotNull (pane);
        assertNotNull ("TopComponent is there", pane.getComponent ());
        
        // Perform a modification so that notifyModify() gets called as supposed by the test
        // Otherwise notifyUnmodified() would not be called (there's no reason to call it in such case).
        doc.insertString(0, "a", null);

        class DoWork implements Runnable {
            private boolean startedAWT;
            private boolean startedWork;
            private boolean finishedWork;
            
            public void run () {
                if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
                    doWorkInAWT ();
                } else {
                    doWork ();
                }
            }
             
            private void doWorkInAWT () {
                startedAWT = true;
                awtWorkStarted.countDown();
                
                try {
                    Thread.sleep (500);
                } catch (InterruptedException ex) {
                    fail (ex.getMessage ());
                }
                
                // this will call into notifyUnmodified.
                pane.getComponent ().close ();
                // Wait for close() to finish fully
                try {
                    unmarkModifiedFinished.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    fail();
                }

                assertFalse ("The document should be marked unmodified now", modified);
                awtWorkFinished.countDown();
            }
            
            private void doWork () {
                startedWork = true;
                try {
                    unmarkModifiedStarted.await();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }

                // now the document is blocked in after close, try to ask for a position
                support.createPositionRef (0, javax.swing.text.Position.Bias.Forward);
                finishedWork = true;
            }
            
        }
        DoWork doWork = new DoWork ();
        
        
        javax.swing.SwingUtilities.invokeLater (doWork);
        awtWorkStarted.await();
            
        
        doc.render (doWork);
        
        // wait for AWT work to finish
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {}});
        
        assertTrue ("AWT started", doWork.startedAWT);
        assertTrue ("Work started", doWork.startedWork);

        awtWorkFinished.await();
        assertTrue ("Work done", doWork.finishedWork);
    }

    public void testReadLockTheDocumentAndThenTryToCreateAPositionInItMeanWhileLetOtherThreadCloseIt () throws Exception {
        final CountDownLatch awtWorkStarted = new CountDownLatch(1);
        final CountDownLatch awtWorkFinished = new CountDownLatch(1);
        class DoWork implements Runnable {
            private boolean finishedWork;
            
            public void run () {
                if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
                    doWorkInAWT ();
                } else {
                    doWork ();
                }
            }
             
            private void doWorkInAWT () {
                awtWorkStarted.countDown();
                try {
                    Thread.sleep (500);
                } catch (InterruptedException ex) {
                    fail (ex.getMessage ());
                }
                
                // this will call into notifyUnmodified.
                support.close ();
                // Wait for close() to finish fully
                try {
                    unmarkModifiedFinished.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    fail();
                }

                assertFalse ("The document should be marked unmodified now", modified);
                awtWorkFinished.countDown();
            }
            
            private void doWork () {
                try {
                    unmarkModifiedStarted.await();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
                
                // now the document is blocked in after close, try to ask for a position
                support.createPositionRef (0, javax.swing.text.Position.Bias.Forward);
                finishedWork = true;
            }
            
        }
        DoWork doWork = new DoWork ();
        StyledDocument doc = support.openDocument ();
        
        // Perform a modification so that notifyModify() gets called as supposed by the test
        // Otherwise notifyUnmodified() would not be called (there's no reason to call it in such case).
        doc.insertString(0, "a", null);

        javax.swing.SwingUtilities.invokeLater (doWork);
        awtWorkStarted.await();
        
        doc.render (doWork);
        
        // maybe this needs to invokeAndWait something empty in AWT?
        awtWorkFinished.await();
        assertTrue ("Work done", doWork.finishedWork);
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
        unmarkModifiedStarted.countDown();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        modified = false;
        unmarkModifiedFinished.countDown();
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
        
        protected EditorKit createEditorKit () {
            return new NbLikeEditorKit ();
        }

        @Override
        protected boolean canClose() {
            return true; // Return true to allow closing of modified doc without asking and call unmarkModified()
        }

    } // end of CES
}
