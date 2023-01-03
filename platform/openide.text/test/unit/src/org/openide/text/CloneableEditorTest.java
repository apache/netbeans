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


import java.awt.GraphicsEnvironment;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.JEditorPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.io.NbMarshalledObject;
import org.openide.windows.*;

public class CloneableEditorTest extends NbTestCase
implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloneableEditorTest.class);
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
    
    private static CloneableEditorTest RUNNING;
    
    public CloneableEditorTest(String s) {
        super(s);
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
    
    private Object writeReplace () {
        return new Replace ();
    }
    
    public void testGetOpenedPanesWorksAfterDeserializationIssue39236 () throws Exception {
        support.open ();

        CloneableEditor ed = (CloneableEditor)support.getRef ().getAnyComponent ();
        
        JEditorPane[] panes = support.getOpenedPanes ();
        assertNotNull (panes);
        assertEquals ("One is there", 1, panes.length);
        
        NbMarshalledObject obj = new NbMarshalledObject (ed);
        ed.close ();
        
        panes = support.getOpenedPanes ();
        assertNull ("No panes anymore", panes);
        
        ed = (CloneableEditor)obj.get ();
        
        panes = support.getOpenedPanes ();
        assertNotNull ("One again", panes);
        assertEquals ("One is there again", 1, panes.length);
    }
    
    public void testInitializeBySupport() {
        CloneableEditor ce = new CloneableEditor(support);
        ce.initializeBySupport();
        assertEquals("Nodes changed", Node.EMPTY, ce.getActivatedNodes()[0]);
    }
    
    public void testEditorPaneActionMapIsParentOfCloneableEditorActionMap() {
        support.open();
        
        CloneableEditor ed = (CloneableEditor)support.getRef().getArbitraryComponent();
        
        assertSame(ed.getEditorPane().getActionMap(), ed.getActionMap().getParent());
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
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
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

        @Override
        protected void initializeCloneableEditor(CloneableEditor editor) {
            editor.setActivatedNodes(new Node[] { Node.EMPTY });
        }
        
        
    }

    private static final class Replace implements Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }
}
