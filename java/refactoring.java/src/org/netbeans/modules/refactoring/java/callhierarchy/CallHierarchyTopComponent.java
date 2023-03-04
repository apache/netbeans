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

package org.netbeans.modules.refactoring.java.callhierarchy;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.refactoring.java.callhierarchy.CallHierarchyModel.HierarchyType;
import org.netbeans.modules.refactoring.java.callhierarchy.CallHierarchyModel.Scope;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays call hierarchy.
 *
 * @author Jan Pokorsky
 */
final class CallHierarchyTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static CallHierarchyTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/refactoring/java/resources/callhierarchy.png"; // NOI18N

    private static final String PREFERRED_ID = "CallHierarchyTopComponent"; // NOI18N
    
    private final ExplorerManager manager;
    private final ExplorerManager managerCtx = new ExplorerManager();
    private CallHierarchyModel model;

    private CallHierarchyTopComponent() {
        manager = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(manager, getActionMap()));
        
        initComponents();

        jBtnRefresh.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/refresh.png", false));
        
        ContextPanel ctxpanel = new ContextPanel();
        ctxpanel.setLayout(new java.awt.BorderLayout());
        ctxpanel.add(listView, java.awt.BorderLayout.CENTER);
        jSplitPane1.setRightComponent(ctxpanel);
        
        Dimension dim = new Dimension(24, 24);
        jBtnRefresh.setMaximumSize(dim);
        jTogBtnCaller.setMaximumSize(dim);
        jTogBtnCallee.setMaximumSize(dim);
        jTogBtnScope.setMaximumSize(dim);
        jBtnCancel.setMaximumSize(dim);
        
        jBtnRefresh.setPreferredSize(dim);
        jTogBtnCaller.setPreferredSize(dim);
        jTogBtnCallee.setPreferredSize(dim);
        jTogBtnScope.setPreferredSize(dim);
        jBtnCancel.setPreferredSize(dim);
        
        setName(NbBundle.getMessage(CallHierarchyTopComponent.class, "CTL_CallHierarchyTopComponent"));
        setToolTipText(NbBundle.getMessage(CallHierarchyTopComponent.class, "HINT_CallHierarchyTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        
        jPopupMenuScope.addPopupMenuListener(new PopupMenuListener() {

            @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

            @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    jTogBtnScope.setSelected(false);
                }

            @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    jTogBtnScope.setSelected(false);
                }
            });
            
        manager.setRootContext(CallNode.createDefault());
        manager.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = manager.getSelectedNodes();
                if (selectedNodes.length == 1) {
                    managerCtx.setRootContext(new FilterNode(selectedNodes[0], new CallNode.CallChildren(true)));
                } else {
                    // no children
                    managerCtx.setRootContext(CallNode.createPleaseWait());
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuScope = new javax.swing.JPopupMenu();
        jMenuItemFilterAll = new javax.swing.JRadioButtonMenuItem();
        jMenuItemCurrProject = new javax.swing.JRadioButtonMenuItem();
        jMenuItemUnitTests = new javax.swing.JCheckBoxMenuItem();
        jMenuItemBaseClass = new javax.swing.JCheckBoxMenuItem();
        buttonGroupScope = new javax.swing.ButtonGroup();
        listView = new org.openide.explorer.view.ListView();
        jToolBar = new javax.swing.JToolBar();
        jBtnRefresh = new javax.swing.JButton();
        jBtnCancel = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jTogBtnCaller = new javax.swing.JToggleButton();
        jTogBtnCallee = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jTogBtnScope = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        beanTreeView = new org.openide.explorer.view.BeanTreeView();

        FormListener formListener = new FormListener();

        buttonGroupScope.add(jMenuItemFilterAll);
        jMenuItemFilterAll.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jMenuItemFilterAll, org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jMenuItemFilterAll.text")); // NOI18N
        jMenuItemFilterAll.addActionListener(formListener);
        jPopupMenuScope.add(jMenuItemFilterAll);

        buttonGroupScope.add(jMenuItemCurrProject);
        org.openide.awt.Mnemonics.setLocalizedText(jMenuItemCurrProject, org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jMenuItemCurrProject.text")); // NOI18N
        jMenuItemCurrProject.addActionListener(formListener);
        jPopupMenuScope.add(jMenuItemCurrProject);

        jMenuItemUnitTests.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jMenuItemUnitTests, org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jMenuItemUnitTests.text")); // NOI18N
        jMenuItemUnitTests.addActionListener(formListener);
        jPopupMenuScope.add(jMenuItemUnitTests);

        jMenuItemBaseClass.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jMenuItemBaseClass, org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jMenuItemBaseClass.text")); // NOI18N
        jMenuItemBaseClass.addActionListener(formListener);
        jPopupMenuScope.add(jMenuItemBaseClass);

        setLayout(new java.awt.BorderLayout());

        jToolBar.setFloatable(false);
        jToolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar.setRollover(true);

        jBtnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jBtnRefresh.toolTipText")); // NOI18N
        jBtnRefresh.setFocusable(false);
        jBtnRefresh.addActionListener(formListener);
        jToolBar.add(jBtnRefresh);

        jBtnCancel.setIcon(org.openide.util.ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/cancel.png", false));
        jBtnCancel.setToolTipText(org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jBtnCancel.toolTipText")); // NOI18N
        jBtnCancel.setEnabled(false);
        jBtnCancel.setFocusable(false);
        jBtnCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnCancel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnCancel.addActionListener(formListener);
        jToolBar.add(jBtnCancel);
        jToolBar.add(jSeparator1);

        jTogBtnCaller.setIcon(org.openide.util.ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/who_is_called.png", false));
        jTogBtnCaller.setSelected(true);
        jTogBtnCaller.setToolTipText(org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jTogBtnCaller.toolTipText")); // NOI18N
        jTogBtnCaller.setFocusable(false);
        jTogBtnCaller.addActionListener(formListener);
        jToolBar.add(jTogBtnCaller);

        jTogBtnCallee.setIcon(org.openide.util.ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/who_calls.png", false));
        jTogBtnCallee.setToolTipText(org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jTogBtnCallee.toolTipText")); // NOI18N
        jTogBtnCallee.setFocusable(false);
        jTogBtnCallee.addActionListener(formListener);
        jToolBar.add(jTogBtnCallee);
        jToolBar.add(jSeparator2);

        jTogBtnScope.setIcon(org.openide.util.ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/filter.png", false));
        jTogBtnScope.setToolTipText(org.openide.util.NbBundle.getMessage(CallHierarchyTopComponent.class, "CallHierarchyTopComponent.jTogBtnScope.toolTipText")); // NOI18N
        jTogBtnScope.setFocusable(false);
        jTogBtnScope.addItemListener(formListener);
        jToolBar.add(jTogBtnScope);

        add(jToolBar, java.awt.BorderLayout.WEST);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setLeftComponent(beanTreeView);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ItemListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jBtnRefresh) {
                CallHierarchyTopComponent.this.jBtnRefreshActionPerformed(evt);
            }
            else if (evt.getSource() == jBtnCancel) {
                CallHierarchyTopComponent.this.jBtnCancelActionPerformed(evt);
            }
            else if (evt.getSource() == jTogBtnCaller) {
                CallHierarchyTopComponent.this.jTogBtnCallerActionPerformed(evt);
            }
            else if (evt.getSource() == jTogBtnCallee) {
                CallHierarchyTopComponent.this.jTogBtnCalleeActionPerformed(evt);
            }
            else if (evt.getSource() == jMenuItemFilterAll) {
                CallHierarchyTopComponent.this.jMenuItemScopeActionPerformed(evt);
            }
            else if (evt.getSource() == jMenuItemCurrProject) {
                CallHierarchyTopComponent.this.jMenuItemScopeActionPerformed(evt);
            }
            else if (evt.getSource() == jMenuItemUnitTests) {
                CallHierarchyTopComponent.this.jMenuItemScopeActionPerformed(evt);
            }
            else if (evt.getSource() == jMenuItemBaseClass) {
                CallHierarchyTopComponent.this.jMenuItemBaseClassActionPerformed(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == jTogBtnScope) {
                CallHierarchyTopComponent.this.jTogBtnScopeItemStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

private void jBtnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRefreshActionPerformed
    if (model != null) {
        model.replaceRoot();
    }
}//GEN-LAST:event_jBtnRefreshActionPerformed

private void jTogBtnCallerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTogBtnCallerActionPerformed
    jTogBtnCallee.setSelected(false);
    if (model != null) {
        model.setType(HierarchyType.CALLER);
    }
}//GEN-LAST:event_jTogBtnCallerActionPerformed

private void jTogBtnCalleeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTogBtnCalleeActionPerformed
    jTogBtnCaller.setSelected(false);
    if (model != null) {
        model.setType(HierarchyType.CALLEE);
    }
}//GEN-LAST:event_jTogBtnCalleeActionPerformed

private void jTogBtnScopeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jTogBtnScopeItemStateChanged
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        jPopupMenuScope.show(jTogBtnScope, 0, jTogBtnScope.getHeight());
    }
}//GEN-LAST:event_jTogBtnScopeItemStateChanged

private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
    CallHierarchyTasks.stop();
}//GEN-LAST:event_jBtnCancelActionPerformed

private void jMenuItemScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScopeActionPerformed
    if (model != null) {
        model.setScopes(getScopes());
    }
}//GEN-LAST:event_jMenuItemScopeActionPerformed

    private void jMenuItemBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBaseClassActionPerformed
        if (model != null) {
            model.setScopes(getScopes());
            model.replaceRoot();
        }
    }//GEN-LAST:event_jMenuItemBaseClassActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView beanTreeView;
    private javax.swing.ButtonGroup buttonGroupScope;
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JButton jBtnRefresh;
    private javax.swing.JCheckBoxMenuItem jMenuItemBaseClass;
    private javax.swing.JRadioButtonMenuItem jMenuItemCurrProject;
    private javax.swing.JRadioButtonMenuItem jMenuItemFilterAll;
    private javax.swing.JCheckBoxMenuItem jMenuItemUnitTests;
    private javax.swing.JPopupMenu jPopupMenuScope;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton jTogBtnCallee;
    private javax.swing.JToggleButton jTogBtnCaller;
    private javax.swing.JToggleButton jTogBtnScope;
    private javax.swing.JToolBar jToolBar;
    private org.openide.explorer.view.ListView listView;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized CallHierarchyTopComponent getDefault() {
        if (instance == null) {
            instance = new CallHierarchyTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CallHierarchyTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CallHierarchyTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CallHierarchyTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof CallHierarchyTopComponent) {
            return (CallHierarchyTopComponent) win;
        }
        Logger.getLogger(CallHierarchyTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                jSplitPane1.setDividerLocation(0.8);
                jSplitPane1.setResizeWeight(1.0);
            }
        });
    }

    @Override
    public void componentClosed() {
        jBtnCancelActionPerformed(null);
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return beanTreeView.requestFocusInWindow();
    }
    
    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return CallHierarchyTopComponent.getDefault();
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private PropertyChangeListener modelListener = new PropertyChangeListener() {

        @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == CallHierarchyModel.PROP_ROOT) {
                    switchRootNode();
                }
            }
    };

    public void setModel(CallHierarchyModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(modelListener);
        }
        if (model != null) {
            model.addPropertyChangeListener(modelListener);
        }
        this.model = model;
        switchRootNode();
    }

    public void reset() {
        CallHierarchyTasks.stop();
        manager.setRootContext(CallNode.createPleaseWait());
    }
    
    public Node getRootNode() {
        return manager.getRootContext();
    }
    
    private void switchRootNode() {
        Node root = CallNode.createRoot(model);
        manager.setRootContext(root);
        try {
            manager.setSelectedNodes(new Node[]{root});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    HierarchyType getHierarchyType() {
        return this.jTogBtnCaller.isSelected() ? HierarchyType.CALLER : HierarchyType.CALLEE;
    }
    
    void setRunningState(boolean isRunning) {
        makeBusy(isRunning);
        jBtnCancel.setVisible(isRunning);
        jBtnCancel.setEnabled(isRunning);
        jBtnRefresh.setVisible(!isRunning);
    }

    Set<Scope> getScopes() {
        Set<Scope> scopes = new HashSet<Scope>();
        if (jMenuItemFilterAll.isSelected()) {
            scopes.add(Scope.ALL);
        } else if (jMenuItemCurrProject.isSelected()) {
            scopes.add(Scope.PROJECT);
        }
        if (jMenuItemUnitTests.isSelected()) {
            scopes.add(Scope.TESTS);
        }
        if(jMenuItemBaseClass.isSelected()) {
            scopes.add(Scope.BASE);
        }
        return scopes;
    }
    
    private final class ContextPanel extends JPanel implements ExplorerManager.Provider{

        @Override
        public ExplorerManager getExplorerManager() {
            return managerCtx;
        }
        
    }
}
