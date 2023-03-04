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

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.test.MockLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/** 
 * Testing usage of UserQuestionException in CES when CES.open loads
 * document asynchronously
 *
 * @author Marek Slama
 */
@RandomlyFails
public class CloneableEditorUserQuestionAsync2Test extends NbTestCase
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
    private Date date = new Date();
    private VetoableChangeListener vetoL;
    private IOException toThrow;

    public CloneableEditorUserQuestionAsync2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        MockLookup.setInstances(new DD());
        support = new CES(this, Lookup.EMPTY);
    }
        
    public void testExceptionThrownWhenDocumentIsBeingReadAWTYes () throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                MyEx my = new MyEx();

                toThrow = my;

                DD.options = null;
                DD.toReturn = NotifyDescriptor.YES_OPTION;
                
                support.open();
                JEditorPane [] panes = support.getOpenedPanes();
                assertNotNull(panes);
                assertEquals(panes.length, 1);
                assertNotNull(panes[0]);
            }
        });
    }

    public void testExceptionThrownWhenDocumentIsBeingReadAWTNo () throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                MyEx my = new MyEx();

                toThrow = my;

                DD.options = null;
                DD.toReturn = NotifyDescriptor.NO_OPTION;

                support.open();
                JEditorPane [] panes = support.getOpenedPanes();
                assertNull(panes);
            }
        });
    }
    
    public void testExceptionThrownWhenDocumentIsBeingReadYes () throws Exception {
        MyEx my = new MyEx();
        
        toThrow = my;
        
        DD.options = null;
        DD.toReturn = NotifyDescriptor.YES_OPTION;
        
        support.open ();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane[] panes = support.getOpenedPanes();
                assertNotNull(panes);
                assertEquals(panes.length, 1);
                assertNotNull(panes[0]);
                CloneableEditor ce = (CloneableEditor) support.getRef().getArbitraryComponent();
                assertNotNull(ce);
            }
        });
        
        CloneableEditor ce = (CloneableEditor) support.getRef().getArbitraryComponent();
        while (ce == null) {
            Thread.sleep(100);
            ce = (CloneableEditor) support.getRef().getArbitraryComponent();
        }
        assertNotNull(ce);
        while (!ce.isEditorPaneReadyTest()) {
            Thread.sleep(100);
        }
        assertTrue(ce.isEditorPaneReadyTest());
    }

    public void testExceptionThrownWhenDocumentIsBeingReadNo () throws Exception {
        MyEx my = new MyEx();

        toThrow = my;

        DD.options = null;
        DD.toReturn = NotifyDescriptor.NO_OPTION;
        
        support.open ();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane[] panes = support.getOpenedPanes();
                assertNull(panes);
            }
        });
    }
    
    private class MyEx extends UserQuestionException {
        private int confirmed;

        public @Override String getLocalizedMessage() {
            return "locmsg";
        }

        public @Override String getMessage() {
            return "msg";
        }

        public void confirmed () {
            confirmed++;
            toThrow = null;
        }
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removePropertyChangeListener(PropertyChangeListener l) {}
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        //Wait here so there is enough time for AWT to get into CE.getEditorPane
        //so we can test behavior when AWT is blocked in initVisual
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        if (toThrow != null) {
            throw toThrow;
        }
        return new ByteArrayInputStream(content.getBytes());
    }

    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            public @Override void close() throws IOException {
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

    public void markModified() throws IOException {
        if (cannotBeModified != null) {
            final String notify = cannotBeModified;
            IOException e = new IOException () {
                public @Override String getLocalizedMessage() {
                    return notify;
                }
            };
            Exceptions.attachLocalizedMessage(e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
        }

        @Override
        protected boolean asynchronousOpen() {
            return true;
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
        
    } // end of CES

    /** Our own dialog displayer.
     */
    private static final class DD extends DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull (options);
            assertNotNull (toReturn);
            options = descriptor.getOptions();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
    
}
