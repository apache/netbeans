/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 * Test of NbDocument.findRecentEditorPane => CloneableEditorSupport.getRecentPane
 * When CloneableEditor is created its initialization is performed asynchronously.
 * Calling CloneableEditorSupport.getOpenedPanes in such case blocks AWT thread.
 * CloneableEditorSupport.getRecentPane returns immediately null when initialization
 * of CloneableEditor is not yet finished.
 *
 * @author Marek Slama
 */
public class CloneableEditorCreationFinishedTest extends NbTestCase
implements CloneableEditorSupport.Env, PropertyChangeListener {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloneableEditorCreationFinishedTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private transient CES support;

    // Env variables
    private transient String content = "";
    private transient boolean valid = true;
    private transient boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private transient String cannotBeModified;
    private transient Date date = new Date ();
    private transient List/*<PropertyChangeListener>*/ propL = new ArrayList ();
    private transient VetoableChangeListener vetoL;
    
    private static CloneableEditorCreationFinishedTest RUNNING;

    private boolean continueFlag;
    private int eventCounter;
    
    public CloneableEditorCreationFinishedTest(String s) {
        super(s);
    }
    
    // to be overriden in core.multiview
    protected CloneableEditorSupport.Pane createPane(CloneableEditorSupport sup) {
        return new CloneableEditor(sup);
    }
    
    @Override
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);
        RUNNING = this;
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    private Object writeReplace () {
        return new Replace ();
    }

    private boolean continueExecution () {
        return continueFlag;
    }

    /**
     * Test that CloneableEditorSupport.getRecentPane returns null when CloneableEditor
     * initialization is not yet finished. Also test that EditorCookie.Observable.PROP_OPENED_PANES
     * event is fired when CloneableEditor initialization finishes.
     *
     * @throws Exception
     */
    public void testEditorPaneFinished () throws Exception {
        FocusHandler h = new FocusHandler();
        
        support.addPropertyChangeListener(this);
        support.open ();
        JEditorPane pane = support.getRecentPane();
        assertNull("Must return null", pane);

        continueFlag = true;

        JEditorPane[] panes = support.getOpenedPanes();

        pane = support.getRecentPane();
        assertNotNull("Must return not null", pane);
        
        assertTrue(isUsedByCloneableEditor(pane));
        
        h.assertFocused("Our pane has been focused", pane);

        support.removePropertyChangeListener(this);
        support.close();

        assertFalse(isUsedByCloneableEditor(pane));
    }
    
    private boolean isUsedByCloneableEditor(JEditorPane pane) {
        return Boolean.TRUE.equals(pane.getClientProperty("usedByCloneableEditor"));
    }
    
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return RUNNING.support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream (content.getBytes ());
    }
    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            @Override
            public void close () throws IOException {
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
                @Override
                public String getLocalizedMessage () {
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
    private final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }

        @Override
        protected boolean asynchronousOpen() {
            return false;
        }
        
        @Override
        protected Pane createPane() {
            return CloneableEditorCreationFinishedTest.this.createPane(this);
        }
        
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
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
            return new NbLikeEditorKit () {
                public Void call() throws Exception {
                    while (true) {
                        Thread.sleep(100);
                        if (continueExecution()) {
                            break;
                        }
                    }
                    super.call();
                    return null;
                }

            };
        }
        
    }

    private static final class Replace implements Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_OPENED_PANES)) {
            final CloneableTopComponent cmp = support.getRef ().getArbitraryComponent();
            CloneableEditor ed = findEditor(cmp);
            assertNotNull("Editor is found", ed);
            final boolean paneReady = ed.isEditorPaneReady();
            if (eventCounter == 0) {
                assertFalse("First event. isEditorPaneReady must return false", paneReady);
                eventCounter++;
            } else if (eventCounter == 1) {
                assertTrue("Second event. isEditorPaneReady must return true", paneReady);
            }
        }
    }

    private CloneableEditor findEditor(Component cmp) {
        if (cmp instanceof CloneableEditor) {
            return (CloneableEditor)cmp;
        }
        if (cmp instanceof Container) {
            for (Component c : ((Container)cmp).getComponents()) {
                CloneableEditor ed = findEditor(c);
                if (ed != null) {
                    return ed;
                }
            }
        }
        return null;
    }

    private static final class FocusHandler extends Handler {
        private final Logger LOG;

        public FocusHandler() {
            LOG = Logger.getLogger("org.openide.text.CloneableEditor");
            LOG.setLevel(Level.FINE);
            LOG.addHandler(this);
            setLevel(Level.FINE);
        }
        List<JEditorPane> focused = new ArrayList<JEditorPane>();
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("requestFocusInWindow")) {
                focused.add((JEditorPane)record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public void assertFocused(String msg, JEditorPane pane) {
            assertEquals("One focused object. " + msg + ": " + focused, 1, focused.size());
            assertEquals(msg, pane, focused.get(0));
            focused.clear();
        }
    }
}
