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

package org.netbeans.modules.cnd.navigation.includeview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.navigation.hierarchy.LoadingNode;
import org.netbeans.modules.cnd.navigation.services.HierarchyFactory;
import org.netbeans.modules.cnd.navigation.services.IncludedModel;
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
public final class IncludeHierarchyPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider  {
    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("IncludeHierarchyWorker", 1); // NOI18N

    private final AbstractNode root;
    private transient ExplorerManager explorerManager = new ExplorerManager();
    private CsmUID<CsmFile> object;
    private boolean recursive = true;
    private boolean plain = true;
    private boolean whoIncludes = true;
    private Action[] actions;
    private Action close;
    private AtomicBoolean menuAvaliable = new AtomicBoolean(false);

    /** Creates new form IncludeHierarchyPanel */
    public IncludeHierarchyPanel(boolean isView) {
        initComponents();
        if (!isView){
            // refresh
            toolBar.remove(0);
            // separstor
            toolBar.remove(0);
            // a11n
            directOnlyButton.setFocusable(true);
            treeButton.setFocusable(true);
            whoIncludesButton.setFocusable(true);
            whoIsIncludedButton.setFocusable(true);
        }
        setName(NbBundle.getMessage(getClass(), "CTL_IncludeViewTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeViewTopComponent")); // NOI18N
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        getTreeView().setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        if (isView) {
            actions = new Action[]{new RefreshAction(),
                                   null, new WhoIncludesAction(), new WhoIsIncludedAction(),
                                   null, new DirectOnlyAction(), new TreeAction()};
        } else {
            actions = new Action[]{new WhoIncludesAction(), new WhoIsIncludedAction(),
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


    private MyBeanTreeView getTreeView(){
        return (MyBeanTreeView)hierarchyPane;
    }

    private Color getBorderColor(){
        return UIManager.getDefaults().getColor("SplitPane.shadow"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        whoIncludesButton = new javax.swing.JToggleButton();
        whoIsIncludedButton = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        directOnlyButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        hierarchyPane = new MyBeanTreeView();

        setLayout(new java.awt.GridBagLayout());

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setMaximumSize(new java.awt.Dimension(182, 26));
        toolBar.setMinimumSize(new java.awt.Dimension(182, 26));
        toolBar.setOpaque(false);
        toolBar.setPreferredSize(new java.awt.Dimension(182, 26));

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.refreshButton.toolTipText")); // NOI18N
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

        buttonGroup1.add(whoIncludesButton);
        whoIncludesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_includes.png"))); // NOI18N
        whoIncludesButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIncludesButton.text")); // NOI18N
        whoIncludesButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIncludesButton.toolTipText")); // NOI18N
        whoIncludesButton.setFocusable(false);
        whoIncludesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        whoIncludesButton.setMaximumSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setMinimumSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setPreferredSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        whoIncludesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whoIncludesButtonActionPerformed(evt);
            }
        });
        toolBar.add(whoIncludesButton);

        buttonGroup1.add(whoIsIncludedButton);
        whoIsIncludedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_is_included.png"))); // NOI18N
        whoIsIncludedButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIsIncludedButton.text")); // NOI18N
        whoIsIncludedButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIsIncludedButton.toolTipText")); // NOI18N
        whoIsIncludedButton.setFocusable(false);
        whoIsIncludedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        whoIsIncludedButton.setMaximumSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setMinimumSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setPreferredSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        whoIsIncludedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whoIsIncludedButtonActionPerformed(evt);
            }
        });
        toolBar.add(whoIsIncludedButton);
        toolBar.add(jSeparator2);

        directOnlyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/direct_only.png"))); // NOI18N
        directOnlyButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.directOnlyButton.text")); // NOI18N
        directOnlyButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.directOnlyButton.toolTipText")); // NOI18N
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

        treeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"))); // NOI18N
        treeButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.treeButton.text")); // NOI18N
        treeButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.treeButton.toolTipText")); // NOI18N
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
        gridBagConstraints.weightx = 1.0;
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
            CsmFile file = object.getObject();
            if (file != null){
                update(file);
            }
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void whoIncludesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIncludesButtonActionPerformed
        setWhoIncludes(true);
    }//GEN-LAST:event_whoIncludesButtonActionPerformed

    private void whoIsIncludedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIsIncludedButtonActionPerformed
        setWhoIncludes(false);
    }//GEN-LAST:event_whoIsIncludedButtonActionPerformed

    private void directOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directOnlyButtonActionPerformed
        setRecursive(!directOnlyButton.isSelected());
    }//GEN-LAST:event_directOnlyButtonActionPerformed

    private void treeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeButtonActionPerformed
        setPlain(!treeButton.isSelected());
    }//GEN-LAST:event_treeButtonActionPerformed

    private void setRecursive(boolean isRecursive){
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                recursive = isRecursive;
                updateButtons();
                update(file);
            }
        }
    }


    private void setPlain(boolean isPlain){
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                plain = isPlain;
                updateButtons();
                update(file);
            }
        }
    }

    private void setWhoIncludes(boolean isWhoIncludes) {
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                whoIncludes = isWhoIncludes;
                updateButtons();
                update(file);
            }
        }
    }

    public void setFile(CsmFile file) {
        if (file != null) {
            object = UIDs.get(file);
            if (file.isHeaderFile()) {
                recursive = false;
                plain = true;
                whoIncludes = true;
            } else {
                recursive = true;
                plain = false;
                whoIncludes = false;
            }
        }
        update(file);
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
                    children.add(new Node[]{new LoadingNode()});
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
                }
            });
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return hierarchyPane.requestFocusInWindow();
    }

    private void updateButtons(){
        whoIncludesButton.setSelected(whoIncludes);
        whoIsIncludedButton.setSelected(!whoIncludes);
        directOnlyButton.setSelected(!recursive);
        treeButton.setSelected(!plain);

        refreshButton.setEnabled(menuAvaliable.get());
        whoIncludesButton.setEnabled(menuAvaliable.get());
        whoIsIncludedButton.setEnabled(menuAvaliable.get());
        directOnlyButton.setEnabled(menuAvaliable.get());
        treeButton.setEnabled(menuAvaliable.get());
    }

    private synchronized void update(final CsmFile csmFile) {
        if (csmFile != null){
            Node[] oldSelection = getExplorerManager().getSelectedNodes();
            Children children = root.getChildren();
            setWaiting();
            Updater updater = new Updater(csmFile, oldSelection, children);
            RP.post(updater);
        } else {
            setEmpty();
        }
    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JToggleButton directOnlyButton;
    private javax.swing.JScrollPane hierarchyPane;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToggleButton treeButton;
    private javax.swing.JToggleButton whoIncludesButton;
    private javax.swing.JToggleButton whoIsIncludedButton;
    // End of variables declaration//GEN-END:variables

    private class RefreshAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public RefreshAction() {
            putValue(Action.NAME, NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.refreshButton.menuText")); //NOI18N
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

    private class WhoIncludesAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public WhoIncludesAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.WHO_INCLUDES));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.WHO_INCLUDES));
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setWhoIncludes(true);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(whoIncludes);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class WhoIsIncludedAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public WhoIsIncludedAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.WHO_IS_INCLUDED));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.WHO_IS_INCLUDED));
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setWhoIncludes(false);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!whoIncludes);
            menuItem.setEnabled(menuAvaliable.get());
            return menuItem;
        }
    }

    private class DirectOnlyAction extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public DirectOnlyAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.DIRECT_ONLY));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.DIRECT_ONLY));
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
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.TREE));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.TREE));
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

    private class DialogClose extends AbstractAction {
        public DialogClose() {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Component p = IncludeHierarchyPanel.this;
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

    private static final int WHO_INCLUDES = 1;
    private static final int WHO_IS_INCLUDED= 2;

    private static final int DIRECT_ONLY = 3;
    private static final int TREE = 4;


    private ImageIcon getButtonIcon(int kind){
        String path = null;
        switch (kind){
        case WHO_INCLUDES:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/who_includes.png"; // NOI18N
            break;
        case WHO_IS_INCLUDED:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/who_is_included.png"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/direct_only.png"; // NOI18N
            break;
        case TREE:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N
            break;
        }
        return new javax.swing.ImageIcon(getClass().getResource(path));
    }

    private String getButtonTooltip(int kind){
        String path = null;
        switch (kind){
        case WHO_INCLUDES:
            path = "IncludeHierarchyPanel.whoIncludesButton.menuText"; // NOI18N
            break;
        case WHO_IS_INCLUDED:
            path = "IncludeHierarchyPanel.whoIsIncludedButton.menuText"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "IncludeHierarchyPanel.directOnlyButton.menuText"; // NOI18N
            break;
        case TREE:
            path = "IncludeHierarchyPanel.treeButton.menuText"; // NOI18N
            break;
        }
        return org.openide.util.NbBundle.getMessage(getClass(), path);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("IncludeView"); // NOI18N
    }

    private class Updater implements Runnable {
        private final CsmFile csmFile;
        private final Node[] oldSelection;
        private final Children children;
        private Node node;
        private IncludedModel model;

        private Updater(CsmFile csmFile, Node[] oldSelection, Children children) {
            this.csmFile = csmFile;
            this.oldSelection = oldSelection;
            this.children = children;
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                if (!Children.MUTEX.isReadAccess()){
                    Children.MUTEX.writeAccess(new Runnable() {

                        @Override
                        public void run() {
                            children.remove(children.getNodes());
                            children.add(new Node[]{node});
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    menuAvaliable.set(true);
                                    updateButtons();
                                    ((BeanTreeView) hierarchyPane).expandNode(node);
                                    Node selected = findSelection();
                                    try {
                                        getExplorerManager().setSelectedNodes(new Node[]{selected});
                                    } catch (PropertyVetoException ex) {
                                    }
                                }
                            });
                        }
                    });
                }
            } else {
                model = HierarchyFactory.getInstance().buildIncludeHierarchyModel(csmFile, actions, whoIncludes, plain, recursive);
                model.setCloseWindowAction(close);
                node = new IncludeNode(csmFile, model, null);
                SwingUtilities.invokeLater(this);
            }
        }

        private Node findSelection() {
            if (oldSelection != null && oldSelection.length == 1
                    && (oldSelection[0] instanceof IncludeNode)) {
                CsmFile what = (CsmFile) ((IncludeNode) oldSelection[0]).getCsmObject();
                for (Node n : node.getChildren().getNodes()) {
                    if (n instanceof IncludeNode) {
                        CsmFile f2 = (CsmFile) ((IncludeNode) n).getCsmObject();
                        if (what != null && f2 != null) {
                            if (what.getAbsolutePath().equals(f2.getAbsolutePath())) {
                                return n;
                            }
                        }
                    }
                }
                return findInModel(what);
            }
            return node;
        }

        private Node findInModel(CsmFile what) {
            List<CsmFile> path = new ArrayList<CsmFile>();
            Set<CsmFile> antiLoop = new HashSet<CsmFile>();
            Node n = node;
            if (findInModel(csmFile, what, path, 25, antiLoop)) {
                for (int i = path.size() - 1; i >= 0; i--) {
                    CsmFile f1 = path.get(i);
                    ((BeanTreeView) hierarchyPane).expandNode(n);
                    for (Node c : n.getChildren().getNodes()) {
                        CsmFile f2 = (CsmFile) ((IncludeNode) c).getCsmObject();
                        if (f1 != null && f2 != null) {
                            if (f1.getAbsolutePath().equals(f2.getAbsolutePath())) {
                                n = c;
                                break;
                            }
                        }
                    }
                }
            }
            return n;
        }

        private boolean findInModel(CsmFile root, CsmFile what, List<CsmFile> path, int level, Set<CsmFile> antiLoop) {
            if (level < 0 || antiLoop.contains(root)) {
                return false;
            }
            antiLoop.add(root);
            Set<CsmFile> set = model.getModel().get(root);
            if (set != null) {
                for (CsmFile f : set) {
                    if (f.getAbsolutePath().equals(what.getAbsolutePath())) {
                        path.add(f);
                        return true;
                    }
                }
                for (CsmFile f : set) {
                    if (findInModel(f, what, path, level - 1, antiLoop)) {
                        path.add(f);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
