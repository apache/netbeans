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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.navigation.classhierarchy.ClassHierarchyPanel;
import org.netbeans.modules.cnd.navigation.includeview.IncludeHierarchyPanel;
import org.netbeans.modules.cnd.navigation.includeview.IncludeNode;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays c/c++ hierarchy.
 */
final class HierarchyTopComponent extends TopComponent implements CsmModelListener {

    private static HierarchyTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/classhierarchy/resources/subtypehierarchy.gif"; // NOI18N
    private static final String PREFERRED_ID = "HierarchyTopComponent"; // NOI18N
    private JComponent last = null;
    private static final RequestProcessor RP = new RequestProcessor("HierarchyWorker", 1); // NOI18N

    private HierarchyTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_HierarchyTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }

    
    void setClass(final TypeContextFinder context, final boolean setClose) {
        setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_TypeHierarchyTopComponent")); // NOI18N
        ClassHierarchyPanel panel;
        if (last instanceof ClassHierarchyPanel) {
            panel = (ClassHierarchyPanel) last;
        } else {
            removeAll();
            panel = new ClassHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        panel.setWaiting();
        Runnable worker = new Runnable() {
            private CsmClass cls;

            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    if (cls == null) {
                        String msg = NbBundle.getMessage(getClass(), "MESSAGE_NoContextClass"); // NOI18N
                        StatusDisplayer.getDefault().setStatusText(msg);
                    }
                    setClass(cls, setClose);
                } else {
                    cls = context.getCsmClass();
                    SwingUtilities.invokeLater(this);
                }
            }
        };
        RP.post(worker);
    }
    
    void setClass(CsmClass decl, boolean setClose) {
        if (decl == null) {
            setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        } else {
            setName(decl.getName()+" - "+NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        }
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_TypeHierarchyTopComponent")); // NOI18N
        ClassHierarchyPanel panel;
        if (last instanceof ClassHierarchyPanel) {
            panel = (ClassHierarchyPanel) last;
        } else {
            removeAll();
            panel = new ClassHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        if (setClose) {
            panel.setClose();
        } else {
            panel.clearClose();
        }
        panel.setClass(decl);
        last.requestFocusInWindow();
    }

    void setFile(final InclideContextFinder context, final boolean setClose) {
        setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeHierarchyTopComponent")); // NOI18N
        IncludeHierarchyPanel panel;
        if (last instanceof IncludeHierarchyPanel) {
            panel = (IncludeHierarchyPanel) last;
        } else {
            removeAll();
            panel = new IncludeHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        panel.setWaiting();
        Runnable worker = new Runnable() {
            private CsmFile file;

            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    setFile(file, setClose);
                } else {
                    file = context.getFile();
                    SwingUtilities.invokeLater(this);
                }
            }
        };
        RP.post(worker);
    }

    @Override
    public Lookup getLookup() {
        if (last instanceof IncludeHierarchyPanel) {
            IncludeHierarchyPanel p = (IncludeHierarchyPanel) last;
            ExplorerManager explorerManager = p.getExplorerManager();
            Node[] selectedNodes = explorerManager.getSelectedNodes();
            if (selectedNodes.length == 1) {
                if (selectedNodes[0] instanceof IncludeNode) {
                    IncludeNode node = (IncludeNode) selectedNodes[0];
                    return node.getNodeLookup();
                }
            }
        }
        return super.getLookup(); //To change body of generated methods, choose Tools | Templates.
    }
    
    void setFile(CsmFile file, boolean setClose) {
        if (file != null) {
            setName(file.getName()+" - "+NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        } else {
            setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        }
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeHierarchyTopComponent")); // NOI18N
        if (!(last instanceof IncludeHierarchyPanel)) {
            removeAll();
            IncludeHierarchyPanel panel = new IncludeHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        if (setClose) {
            ((IncludeHierarchyPanel)last).setClose();
        } else {
            ((IncludeHierarchyPanel)last).clearClose();
        }
        ((IncludeHierarchyPanel)last).setFile(file);
        last.requestFocusInWindow();
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (last != null) {
            last.requestFocusInWindow();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jButton1.setBackground(new JTextArea().getBackground());
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(HierarchyTopComponent.class, "NoViewAvailable")); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setEnabled(false);
        add(jButton1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HierarchyTopComponent getDefault() {
        if (instance == null) {
            instance = new HierarchyTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HierarchyTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof HierarchyTopComponent) {
            return (HierarchyTopComponent)win;
        }
        Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    public @Override int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public @Override void componentOpened() {
        CsmListeners.getDefault().addModelListener(this);
    }

    public @Override void componentClosed() {
        removeAll();
        initComponents();
        last = null;
        CsmListeners.getDefault().removeModelListener(this);
    }

    /** replaces this in object stream */
    public @Override Object writeReplace() {
        return new ResolvableHelper();
    }

    protected @Override String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return HierarchyTopComponent.getDefault();
        }
    }

    @Override
    public void projectOpened(CsmProject project) {
    }

    @Override
    public void projectClosed(CsmProject project) {
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    HierarchyTopComponent tc = HierarchyTopComponent.findInstance();
                    if (tc.isOpened()) {
                        tc.close();
                    }
                }
            });
        }
    }

    @Override
    public void modelChanged(CsmChangeEvent e) {
    }
    
    final static class InclideContextFinder {
        private final Node[] activatedNodes;
        InclideContextFinder(Node[] activatedNodes) {
            this.activatedNodes = activatedNodes;
        }
        
        private CsmFile getFile() {
            return ContextUtils.findFile(activatedNodes);
        }
    }

    final static class TypeContextFinder {
        private final Node[] activatedNodes;
        TypeContextFinder(Node[] activatedNodes) {
            this.activatedNodes = activatedNodes;
        }
        
        private CsmClass getCsmClass() {
            return ContextUtils.getContextClass(activatedNodes);
        }
    }
}
