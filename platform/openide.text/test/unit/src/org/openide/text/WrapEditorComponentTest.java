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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

/**
 * Tests that the CloneableEditorSupport.wrapEditorComponent() method
 * is called by CloneableEditor and its result value used.
 *
 * @author Andrei Badea
 */
public class WrapEditorComponentTest extends NbTestCase
implements CloneableEditorSupport.Env {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WrapEditorComponentTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    
    private WrapEditorComponentCES support;
    
    public WrapEditorComponentTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        support = new WrapEditorComponentCES(this, Lookup.EMPTY);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /**
     * Tests the wrapEditorComponent() method is called for a default editor.
     */
    public void testWrapEditorComponentInDefaultEditor() {
        searchForWrapperComponent();
    }
    
    /**
     * Tests the wrapEditorComponent() method is called for a custom editor 
     */
    public void testWrapEditorComponentInCustomEditor() {
        // first make the support return a document which has a custom editor
        support.setEditorKit(new NbLikeEditorKitWithCustomEditor());
        
        searchForWrapperComponent();
    }
    
    /**
     * Helper method which opens the support and searches for the wrapper 
     * component.
     */
    private void searchForWrapperComponent() {
        support.open();
        
        CloneableEditor ed = (CloneableEditor)support.getRef ().getAnyComponent();
        Component component = ed.getEditorPane();
        
        boolean found = false;
        while (component != ed) {
            if (WrapEditorComponentCES.WRAPPER_NAME.equals(component.getName())) {
                found = true;
                break;
            }
            component = component.getParent();
        }
        
        assertTrue("The panel containing the editor was not found in the TopComponent.", found);
        
        support.close();
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }    
    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }
    
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
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

    /**
     * Implementation of the CES which overrides the wrapEditorComponent()
     * method, wrapping the editor in a component named WRAPPER_NAME.
     */
    private static final class WrapEditorComponentCES extends CloneableEditorSupport {
        
        public static final String WRAPPER_NAME = "panelWrappingTheEditor";
        
        private EditorKit kit;
        
        public WrapEditorComponentCES(Env env, Lookup l) {
            super(env, l);
        }
        
        int cnt = 0;
        protected Component wrapEditorComponent(Component editorComponent) {
            if (cnt++ > 0) {
                fail("Two calls to wrap component");
            }
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setName(WRAPPER_NAME);
            panel.add(editorComponent, BorderLayout.CENTER);
            return panel;
        }
        
        protected EditorKit createEditorKit() {
            if (kit != null) {
                return kit;
            } else {
                return super.createEditorKit();
            }
        }
        
        public void setEditorKit(EditorKit kit) {
            this.kit = kit;
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
    }
    
    private static final class NbLikeEditorKitWithCustomEditor extends NbLikeEditorKit {
        
        public Document createDefaultDocument() {
            return new CustomDoc();
        }
        
        private final class CustomDoc extends Doc implements NbDocument.CustomEditor {
            
            public Component createEditor(JEditorPane j) {
                JScrollPane result = new JScrollPane();
                result.add(j);
                return result;
            }
        }
    }
}
