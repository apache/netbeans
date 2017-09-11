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
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.*;
import javax.swing.text.StyledDocument;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;

/**
 * Simulates issue 46981. Editor locks the document, but somebody else closes it
 * while it is working on it and a deadlock occurs.
 * @author  Petr Nejedly, Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #8023: Waiting 10s for AWT and nothing! Exiting to prevent deadlock [2x]
public class DocumentCannotBeClosedWhenAWTBlockedTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CES support;
    // Env variables
    private String content = "Hello";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;

    static {
        Logger l = Logger.getLogger("");
        Handler[] arr = l.getHandlers();
        for (int i = 0; i < arr.length; i++) {
            l.removeHandler(arr[i]);
        }
        l.addHandler(new ErrManager());
    }
    
    
    /** lock to use for communication between AWT & main thread */
    private Object LOCK = new Object ();
    
    /** Creates new TextTest */
    public DocumentCannotBeClosedWhenAWTBlockedTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
        System.setProperty("org.openide.util.Lookup", DocumentCannotBeClosedWhenAWTBlockedTest.class.getName() + "$Lkp");
        
        super.setUp();
        
        Lookup l = Lookup.getDefault();
        if (!(l instanceof Lkp)) {
            fail("Wrong lookup: " + l);
        }
        
        clearWorkDir();
        
        support = new CES(this, org.openide.util.Lookup.EMPTY);
        
        ErrManager.messages.setLength(0);
    }
    
    public void testModifyAndBlockAWTAndTryToClose () throws Exception {
        StyledDocument doc = support.openDocument();
        doc.insertString(0, "Ble", null);
        
        assertTrue("Modified", support.isModified());
        
        class Block implements Runnable {
            public synchronized void run() {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        Block b = new Block();
        javax.swing.SwingUtilities.invokeLater(b);
        
        boolean success = support.canClose();
        
        synchronized (b) {
            b.notifyAll();
        }
        
        assertFalse("Support cannot close as we cannot ask the user", success);
        
        if (ErrManager.messages.indexOf("InterruptedException") == -1) {
            fail("InterruptedException exception should be reported: " + ErrManager.messages);
        }
    }

	
    public void testBlockingAWTForFiveSecIsOk() throws Exception {
        StyledDocument doc = support.openDocument();
        doc.insertString(0, "Ble", null);
        
        assertTrue("Modified", support.isModified());
        
        class Block implements Runnable {
            public synchronized void run() {
                try {
                    wait(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        Block b = new Block();
        javax.swing.SwingUtilities.invokeLater(b);
        
        boolean success = support.canClose();
        
        synchronized (b) {
            b.notifyAll();
        }
        
        assertTrue("Ok, we managed to ask the question", success);
        
        if (ErrManager.messages.length() > 0) {
            fail("No messages should be reported: " + ErrManager.messages);
        }
    }

    public void testCallingFromAWTIsOk() throws Exception {
        StyledDocument doc = support.openDocument();
        doc.insertString(0, "Ble", null);
        
        assertTrue("Modified", support.isModified());
        
        class AWT implements Runnable {
            boolean success;
            
            public synchronized void run() {
                success = support.canClose();
            }
        }
        
        AWT b = new AWT();
        javax.swing.SwingUtilities.invokeAndWait(b);
        
        assertTrue("Ok, we managed to ask the question", b.success);
        
        if (ErrManager.messages.length() > 0) {
            fail("No messages should be reported: " + ErrManager.messages);
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
        
        protected EditorKit createEditorKit () {
            return new NbLikeEditorKit ();
        }
    } // end of CES
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
			ic.add(new DD());
			ic.add(new ErrManager());
        }
    }
    
    /** Our own dialog displayer.
     */
    private static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        public static boolean disableTest;
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
			return descriptor.getOptions()[0];
        }
        
    } // end of DD
    private static final class ErrManager extends Handler {
        static final StringBuffer messages = new StringBuffer();
        static int nOfMessages;
        static final String DELIMITER = ": ";

        /** setup in setUp */
        static java.io.PrintStream log = System.err;
        
        private String prefix;
        
        public ErrManager () {
            prefix = "";
        }
        
        private ErrManager (String pr) {
            this.prefix = pr;
        }
        
        static void resetMessages() {
            messages.delete(0, ErrManager.messages.length());
            nOfMessages = 0;
        }
        
        private void logImpl(String s) {
            synchronized (ErrManager.messages) {
                nOfMessages++;
                messages.append('['); log.print ('[');
                messages.append(prefix); log.print (prefix);
                messages.append("] - "); log.print ("] - ");
                messages.append(s); log.println (s);
                messages.append('\n'); 
            }
        }
        
        public void publish(LogRecord record) {
            logImpl(record.getMessage());
            if (record.getThrown() != null) {
                StringWriter w = new StringWriter ();
                record.getThrown().printStackTrace (new java.io.PrintWriter (w));
                logImpl (w.toString ());
            }
        }

        public void flush() {
        }

        public void close() {
        }
    } // end of ErrManager
}
