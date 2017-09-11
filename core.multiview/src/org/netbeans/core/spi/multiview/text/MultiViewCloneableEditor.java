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
        Enumeration en = getReference().getComponents();
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
