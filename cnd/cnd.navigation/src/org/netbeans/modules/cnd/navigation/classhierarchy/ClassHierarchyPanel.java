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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.navigation.services.HierarchyFactory;
import org.netbeans.modules.cnd.navigation.services.HierarchyModel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 */
public final class ClassHierarchyPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {
    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/classhierarchy/resources/subtypehierarchy.gif"; // NOI18N

    private final AbstractNode root;
    private volatile CsmUID<CsmClass> object;
    private boolean subDirection = true;
    private boolean recursive = true;
    private boolean plain = false;
    private ExplorerManager explorerManager = new ExplorerManager();
    private Action[] actions;
    private Action close;
    private final RequestProcessor.Task refreshTask = new RequestProcessor("ClassHierarchyPanelUpdater").create(new Updater());// NOI18N
    private AtomicBoolean menuAvaliable = new AtomicBoolean(false);

    /** Creates new form ClassHierarchyPanel */
    public ClassHierarchyPanel(boolean isView) {
        initComponents();
        if (!isView){
            // refresh
            toolBar.remove(0);
            // separator
            toolBar.remove(0);
            // a11n
            directOnlyButton.setFocusable(true);
            subtypeButton.setFocusable(true);
            supertypeButton.setFocusable(true);
            treeButton.setFocusable(true);
        }
        setName(NbBundle.getMessage(getClass(), "CTL_ClassHierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_ClassHierarchyTopComponent")); // NOI18N
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        getTreeView().setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        if (isView) {
            actions = new Action[]{new RefreshAction(),
                                   null, new SubTypeAction(), new SuperTypeAction(),
                                   null, new DirectOnlyAction(), new TreeAction()};
        } else {
            actions = new Action[]{null, new SubTypeAction(), new SuperTypeAction(),
                                   null, new DirectOnlyAction(), new TreeAction()};
        }
        root = new AbstractNode(children){
            @Override
            public Action[] getActions(boolean context) {
                return actions;
            }
        };
        getExplorerManager().setRootContext(root);
    }

    public void setClose() {
        close = new DialogClose();
        getTreeView().addCloseAction(close);
    }

    public void clearClose() {
        close = null;
        getTreeView().addCloseAction(close);
    }

    private Color getBorderColor(){
        return UIManager.getDefaults().getColor("SplitPane.shadow"); // NOI18N
    }

    private MyBeanTreeView getTreeView(){
        return (MyBeanTreeView)hierarchyPane;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        directionGroup = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        subtypeButton = new javax.swing.JToggleButton();
        supertypeButton = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        directOnlyButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        hierarchyPane = new MyBeanTreeView();

        setLayout(new java.awt.GridBagLayout());

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        toolBar.setMaximumSize(new java.awt.Dimension(74, 26));
        toolBar.setMinimumSize(new java.awt.Dimension(74, 26));
        toolBar.setOpaque(false);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/classhierarchy/resources/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setMaximumSize(new java.awt.Dimension(24, 24));
        refreshButton.setMinimumSize(new java.awt.Dimension(24, 24));
        refreshButton.setPreferredSize(new java.awt.Dimension(24, 24));
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        toolBar.add(refreshButton);
        toolBar.add(jSeparator1);

        directionGroup.add(subtypeButton);
        subtypeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/classhierarchy/resources/subtypehierarchy.gif"))); // NOI18N
        subtypeButton.setSelected(true);
        subtypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.subtypeButton.toolTipText")); // NOI18N
        subtypeButton.setFocusable(false);
        subtypeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        subtypeButton.setMaximumSize(new java.awt.Dimension(24, 24));
        subtypeButton.setMinimumSize(new java.awt.Dimension(24, 24));
        subtypeButton.setPreferredSize(new java.awt.Dimension(24, 24));
        subtypeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        subtypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subtypeButtonActionPerformed(evt);
            }
        });
        toolBar.add(subtypeButton);

        directionGroup.add(supertypeButton);
        supertypeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/classhierarchy/resources/supertypehierarchy.gif"))); // NOI18N
        supertypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.supertypeButton.toolTipText")); // NOI18N
        supertypeButton.setFocusable(false);
        supertypeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        supertypeButton.setMaximumSize(new java.awt.Dimension(24, 24));
        supertypeButton.setMinimumSize(new java.awt.Dimension(24, 24));
        supertypeButton.setPreferredSize(new java.awt.Dimension(24, 24));
        supertypeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        supertypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supertypeButtonActionPerformed(evt);
            }
        });
        toolBar.add(supertypeButton);
        toolBar.add(jSeparator2);

        directOnlyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/classhierarchy/resources/direct_only.png"))); // NOI18N
        directOnlyButton.setText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.directOnlyButton.text")); // NOI18N
        directOnlyButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.directOnlyButton.toolTipText")); // NOI18N
        directOnlyButton.setFocusable(false);
        directOnlyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directOnlyButton.setMaximumSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setMinimumSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setPreferredSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        directOnlyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directOnlyButtonActionPerformed(evt);
            }
        });
        toolBar.add(directOnlyButton);

        treeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/classhierarchy/resources/tree.png"))); // NOI18N
        treeButton.setText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.treeButton.text")); // NOI18N
        treeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.treeButton.toolTipText")); // NOI18N
        treeButton.setFocusable(false);
        treeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        treeButton.setMaximumSize(new java.awt.Dimension(24, 24));
        treeButton.setMinimumSize(new java.awt.Dimension(24, 24));
        treeButton.setPreferredSize(new java.awt.Dimension(24, 24));
        treeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        treeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeButtonActionPerformed(evt);
            }
        });
        toolBar.add(treeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        add(toolBar, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(getBorderColor()));
        jPanel2.setFocusable(false);
        jPanel2.setMinimumSize(new java.awt.Dimension(1, 1));
        jPanel2.setPreferredSize(new java.awt.Dimension(1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel2, gridBagConstraints);

        hierarchyPane.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(hierarchyPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        if (object != null) {
            CsmClass cls = object.getObject();
            if (cls != null){
                update(cls);
            }
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void subtypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtypeButtonActionPerformed
        if (subDirection == subtypeButton.isSelected()) {
            return;
        }
        setSubtypeHierarchy(true);
    }//GEN-LAST:event_subtypeButtonActionPerformed

    private void setSubtypeHierarchy(boolean isSub){
        if (object != null) {
            CsmClass cls = object.getObject();
            if (cls != null){
                subDirection = isSub;
                updateButtons();
                update(cls);
            }
        }
    }

    private void supertypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supertypeButtonActionPerformed
        if (subDirection != supertypeButton.isSelected()){
            return;
        }
        setSubtypeHierarchy(false);
    }//GEN-LAST:event_supertypeButtonActionPerformed

    private void directOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directOnlyButtonActionPerformed
        setRecursive(!directOnlyButton.isSelected());
}//GEN-LAST:event_directOnlyButtonActionPerformed

    private void treeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeButtonActionPerformed
        setPlain(!treeButton.isSelected());
    }//GEN-LAST:event_treeButtonActionPerformed

    private void setRecursive(boolean isRecursive){
        if (object != null) {
            CsmClass cls = object.getObject();
            if (cls != null){
                recursive = isRecursive;
                updateButtons();
                update(cls);
            }
        }
    }

    private void setPlain(boolean isPlain){
        if (object != null) {
            CsmClass cls = object.getObject();
            if (cls != null){
                plain = isPlain;
                updateButtons();
                update(cls);
            }
        }
    }

    public void setClass(CsmClass cls){
        if (cls != null) {
            object = UIDs.get(cls);
        }
        subDirection = true;
        updateButtons();
        update(cls);
    }

    private void updateButtons(){
        subtypeButton.setSelected(subDirection);
        supertypeButton.setSelected(!subDirection);
        directOnlyButton.setSelected(!recursive);
        treeButton.setSelected(!plain);

        refreshButton.setEnabled(menuAvaliable.get());
        subtypeButton.setEnabled(menuAvaliable.get());
        supertypeButton.setEnabled(menuAvaliable.get());
        directOnlyButton.setEnabled(menuAvaliable.get());
        treeButton.setEnabled(menuAvaliable.get());
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return hierarchyPane.requestFocusInWindow();
    }

    public void setWaiting() {
        menuAvaliable.set(false);
        updateButtons();
        final Children children = root.getChildren();
        if (!Children.MUTEX.isReadAccess()){
            Children.MUTEX.writeAccess(new Runnable(){
                @Override
                public void run() {
                    children.remove(children.getNodes());
                    children.add(new Node[] { ElementNode.getWaitNode(NbBundle.getMessage(ClassHierarchyPanel.class, "PleaseWait")) });
                }
            });
        }
    }

    public void setEmpty() {
        menuAvaliable.set(false);
        updateButtons();
        final Children children = root.getChildren();
        if (!Children.MUTEX.isReadAccess()){
            Children.MUTEX.writeAccess(new Runnable(){
                @Override
                public void run() {
                    children.remove(children.getNodes());
                    refreshTask.cancel();
                }
            });
        }
    }

    private synchronized void update(final CsmClass csmClass) {
        if (csmClass != null) {
            setWaiting();
            refreshTask.schedule(10);
        } else {
            setEmpty();
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton directOnlyButton;
    private javax.swing.ButtonGroup directionGroup;
    private javax.swing.JScrollPane hierarchyPane;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton subtypeButton;
    private javax.swing.JToggleButton supertypeButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToggleButton treeButton;
    // End of variables declaration//GEN-END:variables

    private class RefreshAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public RefreshAction() {
            putValue(Action.NAME, NbBundle.getMessage(ClassHierarchyPanel.class, "ClassHierarchyPanel.refreshButton.menuText")); //NOI18N
            putValue(Action.SMALL_ICON, refreshButton.getIcon());
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshButtonActionPerformed(e);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class SubTypeAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public SubTypeAction() {
            putValue(Action.NAME, getButtonTooltip(ClassHierarchyPanel.SUB_TYPES)); //NOI18N
            putValue(Action.SMALL_ICON, subtypeButton.getIcon());
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSubtypeHierarchy(true);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(subDirection);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class SuperTypeAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public SuperTypeAction() {
            putValue(Action.NAME, getButtonTooltip(ClassHierarchyPanel.SUPER_TYPES));
            putValue(Action.SMALL_ICON, supertypeButton.getIcon());
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSubtypeHierarchy(false);
        }


        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!subDirection);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class DirectOnlyAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public DirectOnlyAction() {
            putValue(Action.NAME, getButtonTooltip(ClassHierarchyPanel.DIRECT_ONLY));
            putValue(Action.SMALL_ICON, getButtonIcon(ClassHierarchyPanel.DIRECT_ONLY));
            menuItem = new JCheckBoxMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRecursive(!recursive);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!recursive);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class TreeAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public TreeAction() {
            putValue(Action.NAME, getButtonTooltip(ClassHierarchyPanel.TREE));
            putValue(Action.SMALL_ICON, getButtonIcon(ClassHierarchyPanel.TREE));
            menuItem = new JCheckBoxMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setPlain(!plain);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!plain);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class DialogClose extends AbstractAction {
        public DialogClose() {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Component p = ClassHierarchyPanel.this;
            while (p != null){
                if (p instanceof TopComponent) {
                    ((TopComponent)p).close();
                    return;
                } else if (p instanceof Window) {
                    ((Window)p).setVisible(false);
                    return;
                }
                p = p.getParent();
           }
        }
    }

    private static class MyBeanTreeView extends BeanTreeView {
        public MyBeanTreeView(){
        }
        public void addCloseAction(final Action action){
            tree.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                        if (action != null) {
                            action.actionPerformed(null);
                            e.consume();
                        }
                    }
                    super.keyReleased(e);
                }
            });
        }
    }

    private static final int SUB_TYPES = 1;
    private static final int SUPER_TYPES= 2;

    private static final int DIRECT_ONLY = 3;
    private static final int TREE = 4;

    private ImageIcon getButtonIcon(int kind){
        String path = null;
        switch (kind){
        case SUB_TYPES:
            path = "/org/netbeans/modules/cnd/navigation/classhierarchy/resources/subtypehierarchy.png"; // NOI18N
            break;
        case SUPER_TYPES:
            path = "/org/netbeans/modules/cnd/navigation/classhierarchy/resources/supertypehierarchy.png"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "/org/netbeans/modules/cnd/navigation/classhierarchy/resources/direct_only.png"; // NOI18N
            break;
        case TREE:
            path = "/org/netbeans/modules/cnd/navigation/classhierarchy/resources/tree.png"; // NOI18N
            break;
        }
        return new javax.swing.ImageIcon(getClass().getResource(path));
    }

    private String getButtonTooltip(int kind){
        String path = null;
        switch (kind){
        case SUB_TYPES:
            path = "ClassHierarchyPanel.subtypeButton.menuText"; // NOI18N
            break;
        case SUPER_TYPES:
            path = "ClassHierarchyPanel.supertypeButton.menuText"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "ClassHierarchyPanel.directOnlyButton.menuText"; // NOI18N
            break;
        case TREE:
            path = "ClassHierarchyPanel.treeButton.menuText"; // NOI18N
            break;
        }
        return org.openide.util.NbBundle.getMessage(getClass(), path);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("TypeView"); // NOI18N
    }

    private class Updater implements Runnable {
        HierarchyModel model;
        CsmClass csmClass;
        public Updater() {
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {

                final Children children = root.getChildren();
                if (!Children.MUTEX.isReadAccess()) {
                    Children.MUTEX.writeAccess(new Runnable() {

                        @Override
                        public void run() {
                            children.remove(children.getNodes());
                            HierarchyModel curModel = model;
                            if (curModel != null) {
                                final Node node = new HierarchyNode(new HierarchyModel.Node(csmClass, false), curModel, null);
                                children.add(new Node[]{node});
                                try {
                                    getExplorerManager().setSelectedNodes(new Node[]{node});
                                } catch (PropertyVetoException ex) {
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        menuAvaliable.set(true);
                                        updateButtons();
                                        ((BeanTreeView) hierarchyPane).expandNode(node);
                                    }
                                });
                            }
                        }
                    });
                }
            } else {
                CsmUID<CsmClass> uid = ClassHierarchyPanel.this.object;
                csmClass = uid == null ? null : uid.getObject();
                HierarchyModel newModel = null;
                if (csmClass != null) {
                    newModel = HierarchyFactory.getInstance().buildTypeHierarchyModel(csmClass, actions, subDirection, plain, recursive);
                    newModel.setCloseWindowAction(close);
                }
                model = newModel;
                SwingUtilities.invokeLater(this);
            }
        }
    }
}
