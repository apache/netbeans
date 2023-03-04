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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class InitializeInAWTTest extends NbTestCase implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InitializeInAWTTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the method of manager that we are testing */
    
    private CES support;
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<java.beans.PropertyChangeListener> propL = new java.util.ArrayList<java.beans.PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;
    private FindActionCheck find;
    
    public InitializeInAWTTest(String m) {
        super(m);
    }

    @Override
    protected boolean runInEQ() {
        return getName().contains("AWT");
    }

    @Override
    protected void setUp() throws Exception {
        support = new CES(this, Lookup.EMPTY);
        find = new FindActionCheck();
    }

    @Override
    protected void tearDown() throws Exception {
        find.assertAction();
    }

    protected @Override int timeOut() {
        return 10000;
    }

    public void testInitializeOnBackground() throws Exception {
        assertFalse("Running out of AWT", SwingUtilities.isEventDispatchThread());
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        assertNotNull(r.p);

        assertKit(r.p.getEditorKit());
    }
    
    private static void assertKit(EditorKit kit) {
        if (kit instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit) kit;
            assertNotNull("the kit's call mehtod expected to be called", nb.callThread);
        } else {
            fail("Should use NbLikeEditorKit: " + kit);
        }
    }
    
    public void testInitializeOnBackgroundInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        r.run();
        assertNotNull(r.p);

        assertKit(r.p.getEditorKit());
    }

    public void testInitializeAndBlockInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    run();
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        assertKit(r.p.getEditorKit());
    }
    
    
    public void testQueryDocumentInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            Document doc;
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    final TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
                    if (tc instanceof CloneableEditor) {
                        CloneableEditor ed = (CloneableEditor)tc;
                        p = ed.getEditorPane();
                        doc = ed.getEditorPane().getDocument();
                    }
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        assertKit(r.p.getEditorKit());
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
    
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream (content.getBytes ());
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

        @Override
        protected javax.swing.text.EditorKit createEditorKit() {
            return new K();
        }

        @Override
        protected CloneableEditor createCloneableEditor() {
            CloneableEditor ed = super.createCloneableEditor();
            // "oldInitialize" no longer used
//            ed.putClientProperty("oldInitialize", Boolean.TRUE);
            return ed;
        }
        
        
    } // end of CES

    private static final class MyAction extends CallbackSystemAction {
        @Override
        public String getName() {
            return "MyAction";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }

    static final class K extends NbLikeEditorKit  {
        @Override
        public javax.swing.text.Document createDefaultDocument() {
            return new EdDoc();
        }

        @Override
        public Action[] getActions() {
            List<Action> arr = new ArrayList<Action>(Arrays.asList(super.getActions()));
            Action a = MyAction.get(MyAction.class);
            arr.add(a);
            return arr.toArray(new Action[0]);
        }

        private final class EdDoc extends Doc implements NbDocument.CustomEditor {
            public Component createEditor(JEditorPane j) {
                j.getActionMap().put("MyAction", MyAction.get(MyAction.class));
                return j;
            }

        }

/* Uncomment this code to simulate the deadlock with mimelookup that uses two locks
        @Override
        public synchronized Document createDefaultDocument() {
            return super.createDefaultDocument();
        }

        @Override
        public synchronized Void call() throws Exception {
            synchronized (new JPanel().getTreeLock()) {
            }
            return super.call();
        }
 */

    }

    static final class FindActionCheck implements LookupListener, Mutex.Action<Void> {
        private Result<ActionMap> res;

        private Action last;

        FindActionCheck() {
            res = Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
            resultChanged(null);
            res.addLookupListener(this);
            
            Mutex.EVENT.readAccess(this);

            assertEquals("No action provided now", null, last);
        }

        public void resultChanged(LookupEvent ev) {
            if (!res.allItems().isEmpty()) {
                ActionMap m = res.allInstances().iterator().next();
                last = m.get("MyAction");
            } else {
                last = null;
            }
        }

        public void assertAction() {
            res.removeLookupListener(this);
            assertNotNull("Result found", last);
        }

        @Override
        public Void run() {
            assertTrue("Is EDT", EventQueue.isDispatchThread());
            TopComponent tc =new TopComponent();
            tc.open();
            tc.requestActive();
            return null;
        }
    }
}
