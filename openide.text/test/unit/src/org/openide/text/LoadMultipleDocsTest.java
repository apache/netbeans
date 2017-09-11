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
    private final static class CES extends CloneableEditorSupport {
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

