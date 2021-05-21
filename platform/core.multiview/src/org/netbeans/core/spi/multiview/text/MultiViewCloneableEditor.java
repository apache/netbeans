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

package org.netbeans.core.spi.multiview.text;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.Document;
import org.netbeans.api.actions.Savable;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.NbDocument.CustomToolbar;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * @author  mkleint
 */
class MultiViewCloneableEditor extends CloneableEditor  implements MultiViewElement {
    private static final long serialVersionUID =-3126744316644172415L;

    private transient MultiViewElementCallback multiViewObserver;
    private transient JPanel bar;
    
    public MultiViewCloneableEditor() {
        super();
    }
    
    public MultiViewCloneableEditor(CloneableEditorSupport support) {
        super(support, true);
        initializeBySupport();
    }
    
    @Override
    public JComponent getToolbarRepresentation() {
        if (bar == null) {
            bar = new JPanel();
            bar.setLayout(new BorderLayout());
            fillInBar();
        }
        return bar;
    }
    
    @Override
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    @Override
    public final void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    protected final MultiViewElementCallback getElementObserver() {
        return multiViewObserver;
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentShowing() {
        updateDisplayText();
        super.componentShowing();
    }
    
    @Override
    public javax.swing.Action[] getActions() {
        return super.getActions();
    }
    
    @Override
    public org.openide.util.Lookup getLookup() {
        if (multiViewObserver == null) {
            return getLookupSuper();
        }
        return multiViewObserver.getTopComponent().getLookup();
    }
    
    @Override
    public String preferredID() {
        return super.preferredID();
    }
    
    
    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    
    @Override
    public void updateName() {
        super.updateName();
        updateDisplayText();
    }

    private void updateDisplayText() {
        if (multiViewObserver != null) {
            TopComponent tc = multiViewObserver.getTopComponent();
            tc.setHtmlDisplayName(getHtmlDisplayName());
            tc.setDisplayName(getDisplayName());
            tc.setName(getName());
            tc.setToolTipText(getToolTipText());
            tc.setIcon(getIcon());
        }
    }
    
    @Override
    public void open() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.open();
        }
        
    }

    /** Suppress the de-initialization of editor infrastructure, as this
     * is not real TopComponent, rather just an element embedded in real
     * components. Those components are responsible for cleaning up.
     * @return 
     */
    @Override
    protected boolean closeLast() {
        return true;
    }
    
    @Messages({
        "# {0} - file name", "MSG_SaveModified=File {0} is modified. Save?",
        "MSG_SaveModified_no_name=File is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        final CloneableEditorSupport sup = getLookup().lookup(CloneableEditorSupport.class);
        Enumeration<CloneableTopComponent> en = getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                // at least two is OK
                return CloseOperationState.STATE_OK;
            }
        }
        
        Savable sav = getLookup().lookup(Savable.class);
        if (sav != null) {
            AbstractAction save = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        sup.saveDocument();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            try {
                if (sav.getClass().getMethod("toString").getDeclaringClass() != Object.class) {
                    save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(sav));
                } else {
                    Logger.getLogger(MultiViewCloneableEditor.class.getName()).log(Level.WARNING, 
                            "Need to override toString() to contain the file name in o.n.api.action.Savable {0} with lookup {1}", 
                            new Object[] {sav.getClass(), getLookup().lookupAll(Object.class)});
                    Node n = getLookup().lookup(Node.class);
                    if (n != null) {
                        // #201696: compatibility fallback.
                        save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(n.getDisplayName()));
                    } else {
                        save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified_no_name());
                    }
                }
            } catch (NoSuchMethodException x) {
                assert false : x;
            }
            return MultiViewFactory.createUnsafeCloseState("editor", save, null);
        } 
        return CloseOperationState.STATE_OK;
    }

    Lookup getLookupSuper() {
        return super.getLookup();
    }

    @Override
    public void revalidate() {
        super.revalidate();
        // we reuse the fact that revalidate is called by CloneableEditor
        // after the pane is initialized
        fillInBar();
    }

    private void fillInBar() {
        if (bar != null && bar.getComponentCount() == 0 && pane != null) {
            Document doc = pane.getDocument();
            if (doc instanceof NbDocument.CustomToolbar) {
                CustomToolbar custom = (NbDocument.CustomToolbar)doc;
                final JToolBar content = custom.createToolbar(pane);
                if (content != null) {
                    bar.add(content, BorderLayout.CENTER);
                }
            }
        }
    }
}
