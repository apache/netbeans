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

package org.netbeans.modules.xml.multiview;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import org.openide.text.NbDocument;
import org.openide.actions.SaveAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ProxyLookup;
import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.RequestProcessor;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public class XmlMultiViewElement extends AbstractMultiViewElement implements java.io.Serializable {
    static final long serialVersionUID = -326467724916080580L;

    private transient XmlMultiViewEditorSupport.XmlCloneableEditor xmlEditor;
    private transient javax.swing.JComponent toolbar;
    
    /**
     * Lazy initializer of a toolbar
     */
    private transient ToolbarInitializer initializer;
    
    private static final RequestProcessor INIT_RP = new RequestProcessor("XmlMultiViewElementToolbar initializer", 3); // NOI18N

    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement() {
    }

    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement(XmlMultiViewDataObject dObj) {
        super(dObj);
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public void componentHidden() {
    }
    
    @Override
    public void componentActivated() {
        getXmlEditor().componentActivated();
    }
    
    @Override
    public void componentShowing() {
        getXmlEditor().componentShowing();
    }
    
    @Override
    public void componentOpened() {
        getXmlEditor().componentOpened();
    }
    
    @Override
    public void componentClosed() {
        getXmlEditor().componentClosed();
    }
    
    @Override
    public org.openide.util.Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[] {
            dObj.getNodeDelegate().getLookup()
        });
    }

    @Override
    public javax.swing.JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            final JEditorPane editorPane = getXmlEditor().getEditorPane();
            synchronized (this) {
                if (toolbar == null) {
                    if (initializer == null) {
                        initializer = new ToolbarInitializer(editorPane);
                        INIT_RP.post(initializer);
                    }
                    JPanel fake = new JPanel();
                    fake.setLayout(new BorderLayout());
                    initializer.addToolbarPlaceholder(fake);
                    return fake;
                }
            }
        }
        return toolbar;
    }

    @Override
    public javax.swing.JComponent getVisualRepresentation() {
        return getXmlEditor();
    }

    private XmlMultiViewEditorSupport.XmlCloneableEditor getXmlEditor() {
        if (xmlEditor == null) {
            xmlEditor = (XmlMultiViewEditorSupport.XmlCloneableEditor) dObj.getEditorSupport().createCloneableEditor();
            final ActionMap map = xmlEditor.getActionMap();
            SaveAction act = SystemAction.get(SaveAction.class);
            KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            xmlEditor.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, "save"); //NOI18N
            map.put("save", act); //NOI18N
        }
        return xmlEditor;
    }
    
    /**
     * See defect #232413 - the document is loaded in a RP, then an AWT Runnable is scheduled, which 
     * sets the toolbar member variable for future getToolBarComponent invocations and also back-patches
     * the existing toolbar JPanel placeholders with the real contents.
     */
    private class ToolbarInitializer implements Runnable {
        private final List<JPanel>    toolbarPanels = new ArrayList<JPanel>(3);
        private final JEditorPane     editorPane;
        private Document doc;
        private JComponent      realToolBar;

        public ToolbarInitializer(JEditorPane editorPane) {
            this.editorPane = editorPane;
        }
        
        void addToolbarPlaceholder(JPanel p) {
            toolbarPanels.add(p);
        }
        
        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                runInAwt();
                return;
            }
            // prepare the document in RP:
            doc = editorPane.getDocument();
            SwingUtilities.invokeLater(this);
        }
        
        private void runInAwt() {
            if (doc instanceof NbDocument.CustomToolbar) {
                realToolBar = ((NbDocument.CustomToolbar) doc).createToolbar(editorPane);
            }
            synchronized (XmlMultiViewElement.this) {
                if (realToolBar == null) {
                    toolbar = new JPanel();
                } else {
                    toolbar = realToolBar;
                }
                initializer = null;
            }
            if (realToolBar == null) {
                return;
            }
            
            // patch existing toolbars
            for (JComponent p : toolbarPanels) {
                if (p.isValid()) {
                    p.add(realToolBar, BorderLayout.CENTER);
                }
            }
        }
        
    }

}
