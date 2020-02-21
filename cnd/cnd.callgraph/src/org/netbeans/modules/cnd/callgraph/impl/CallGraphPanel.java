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
package org.netbeans.modules.cnd.callgraph.impl;

import org.netbeans.modules.cnd.callgraph.support.ExportAction;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphUI;
import org.netbeans.modules.cnd.callgraph.api.ui.Catalog;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphActionEDTRunnable;
import org.netbeans.modules.cnd.callgraph.impl.CallGraphScene.LayoutKind;
import org.netbeans.modules.cnd.callgraph.support.ExportXmlAction;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 */
public class CallGraphPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider  {

    private ExplorerManager explorerManager = new ExplorerManager();
    private final AbstractNode root;
    private List<Action> actions;
    private CallModel model;
    private boolean showGraph;
    private boolean isCalls;
    private boolean isShowOverriding;
    private boolean isShowParameters;
    public static final String IS_CALLS = "CallGraphIsCalls"; // NOI18N
    public static final String IS_SHOW_OVERRIDING = "CallGraphIsShowOverriding"; // NOI18N
    public static final String IS_SHOW_PARAMETERS = "CallGraphIsShowParameters"; // NOI18N
    public static final String INITIAL_LAYOUT = "CallGraphLayout"; // NOI18N

    private CallGraphScene scene;
    private final transient FocusTraversalPolicy newPolicy;
    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); // NOI18N
    private static final int MAX_EXPANDED_TREE_NODES = 1000;
    private AtomicBoolean isSetDividerLocation = new AtomicBoolean(false);
    private static final RequestProcessor RP = new RequestProcessor("CallGraphPanel", 2);//NOI18N
    private final CallGraphUI graphUI;
    private final Catalog messagesCatalog;    
    final ShowOverridingAction showOverridingAction;
    private final Icon overridingIcon = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/show_overriding.gif"));

    /** Creates new form CallGraphPanel */
    public CallGraphPanel(CallGraphUI graphUI) {
        this.graphUI = graphUI;
        messagesCatalog = graphUI == null || graphUI.getCatalog() == null ? new DefaultCatalog() : graphUI.getCatalog();
        showOverridingAction = new ShowOverridingAction();        
        initComponents();
        isCalls = NbPreferences.forModule(CallGraphPanel.class).getBoolean(IS_CALLS, true);
        isShowOverriding = NbPreferences.forModule(CallGraphPanel.class).getBoolean(IS_SHOW_OVERRIDING, false);
        isShowParameters = NbPreferences.forModule(CallGraphPanel.class).getBoolean(IS_SHOW_PARAMETERS, false);
        getTreeView().setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        this.showGraph = graphUI == null ? false : graphUI.showGraph();
        actions = new ArrayList<Action>();
        actions.add(new RefreshAction());
        actions.add(new FocusOnAction());        
        actions.add(null);
        actions.add(new WhoIsCalledAction());
        actions.add(new WhoCallsAction());
        actions.add((showOverridingAction));
        actions.add(null);
        actions.add(new ShowFunctionParameters());        
        if (showGraph) {
            scene = new CallGraphScene();
            ExportAction exportAction = new ExportAction(scene, this);
            ExportXmlAction exportXmlAction = new ExportXmlAction(scene, this);
            actions.add(exportAction);
            actions.add(exportXmlAction);
            scene.setExportAction(exportAction);
            scene.setExportXmlAction(exportXmlAction);
        }
        actions.add(null);
        actions.add(new ExpandAction());
        actions.add(new ExpandAllAction());        
        //add all actions from the provider
        if (graphUI != null) {
            actions.addAll(graphUI.getActions(new CallGraphActionEDTRunnable() {

                @Override
                public void run() {
                    updateButtons();
                    update();
                }
            }));
        }
        root = new AbstractNode(children){
            @Override
            public Action[] getActions(boolean context) {
                return actions.toArray(new Action[actions.size()]);
            }
        };
        getExplorerManager().setRootContext(root);
        if (showGraph) {
            initGraph();
        } else {
            Component left = jSplitPane1.getLeftComponent();
            remove(jSplitPane1);
            jSplitPane1.remove(left);
            add(left, java.awt.BorderLayout.CENTER);
        }
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = getExplorerManager().getSelectedNodes();
                if (selectedNodes.length == 1) {
                    Node selected = selectedNodes[0];
                    if (selected instanceof CallNode) {
                        getContextPanel().setRootContent((CallNode) selected);
                    } else {
                        getContextPanel().setRootContent(null);
                    }
                }
            }
        });
        ArrayList<Component> order = new ArrayList<Component>();
        order.add(treeView);
        order.add(contextPanel);
        order.add(refresh);
        order.add(focusOn);
        order.add(calls);
        order.add(callers);
        newPolicy = new MyOwnFocusTraversalPolicy(this, order);
        setFocusTraversalPolicy(newPolicy);
        if( isMacLaf ) {
            jToolBar1.setBackground(macBackground);
        }
    }

    private void initGraph() {
        JComponent view = scene.createView();
        graphView.setViewportView(view);
        view.setFocusable(isCalls);
        int aInt = NbPreferences.forModule(CallGraphPanel.class).getInt(INITIAL_LAYOUT, 0);
        switch (aInt) {
            case 0:
                scene.setLayout(LayoutKind.grid);
                break;
            case 1:
                scene.setLayout(LayoutKind.hierarchical);
                break;
            case 2:
                scene.setLayout(LayoutKind.hierarchical_inverted);
                break;
            case 3:
                scene.setLayout(LayoutKind.horizontal);
                break;
        }
        graphView.setFocusable(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        refresh = new javax.swing.JButton();
        focusOn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        calls = new javax.swing.JToggleButton();
        callers = new javax.swing.JToggleButton();
        overriding = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        graphView = new JScrollPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        treeView = new BeanTreeView();
        contextPanel = new ContextPanel(graphUI);

        setFocusCycleRoot(true);
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);
        jToolBar1.setFocusable(false);
        jToolBar1.setName("jToolBar1"); // NOI18N

        refresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/refresh.png"))); // NOI18N
        refresh.setToolTipText(getMessage("RefreshActionTooltip"));
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setName("refresh"); // NOI18N
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        jToolBar1.add(refresh);

        focusOn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/focus_on.png"))); // NOI18N
        focusOn.setToolTipText(getMessage("FocusOnActionTooltip"));
        focusOn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        focusOn.setName("focusOn"); // NOI18N
        focusOn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        focusOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusOnActionPerformed(evt);
            }
        });
        jToolBar1.add(focusOn);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator1);

        calls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/who_is_called.png"))); // NOI18N
        calls.setToolTipText(getMessage("CallsActionTooltip"));
        calls.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calls.setMaximumSize(new java.awt.Dimension(28, 28));
        calls.setMinimumSize(new java.awt.Dimension(28, 28));
        calls.setName("calls"); // NOI18N
        calls.setPreferredSize(new java.awt.Dimension(28, 28));
        calls.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callsActionPerformed(evt);
            }
        });
        jToolBar1.add(calls);

        callers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/who_calls.png"))); // NOI18N
        callers.setToolTipText(getMessage("CallersActionTooltip"));
        callers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        callers.setMaximumSize(new java.awt.Dimension(28, 28));
        callers.setMinimumSize(new java.awt.Dimension(28, 28));
        callers.setName("callers"); // NOI18N
        callers.setPreferredSize(new java.awt.Dimension(28, 28));
        callers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        callers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callersActionPerformed(evt);
            }
        });
        jToolBar1.add(callers);

        overriding.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/show_overriding.gif"))); // NOI18N
        overriding.setToolTipText(getMessage("ShowOverridingTooltip"));
        overriding.setFocusable(false);
        overriding.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        overriding.setMaximumSize(new java.awt.Dimension(28, 28));
        overriding.setMinimumSize(new java.awt.Dimension(28, 28));
        overriding.setName("overriding"); // NOI18N
        overriding.setPreferredSize(new java.awt.Dimension(28, 28));
        overriding.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        overriding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overridingActionPerformed(evt);
            }
        });
        jToolBar1.add(overriding);

        add(jToolBar1, java.awt.BorderLayout.LINE_START);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setFocusable(false);
        jSplitPane1.setName(""); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        graphView.setToolTipText("");
        graphView.setFocusable(false);
        graphView.setName(""); // NOI18N
        jSplitPane1.setRightComponent(graphView);
        graphView.getAccessibleContext().setAccessibleName("getMessage(\"pictorial.part\")");
        graphView.getAccessibleContext().setAccessibleDescription("getMessage(\"pictorial.part\")");

        jSplitPane2.setDividerLocation(-10);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setFocusable(false);
        jSplitPane2.setName(""); // NOI18N

        treeView.setName("treeView"); // NOI18N
        jSplitPane2.setLeftComponent(treeView);
        treeView.getAccessibleContext().setAccessibleName(getMessage("CGP_TreeView_AN"));
        treeView.getAccessibleContext().setAccessibleDescription(getMessage("CGP_TreeView_AD"));

        contextPanel.setMinimumSize(new java.awt.Dimension(10, 35));
        contextPanel.setName("contextPanel"); // NOI18N
        contextPanel.setPreferredSize(new java.awt.Dimension(10, 35));
        jSplitPane2.setRightComponent(contextPanel);
        contextPanel.getAccessibleContext().setAccessibleName(getMessage("CGP_ListView_AM"));
        contextPanel.getAccessibleContext().setAccessibleDescription(getMessage("CGP_ListView_AD"));

        jSplitPane1.setLeftComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        update();
    }//GEN-LAST:event_refreshActionPerformed

    private void callsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callsActionPerformed
        if (isCalls == calls.isSelected()) {
            return;
        }
        setDirection(true);
    }//GEN-LAST:event_callsActionPerformed

    private void callersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callersActionPerformed
        if (isCalls != callers.isSelected()) {
            return;
        }
        setDirection(false);
    }//GEN-LAST:event_callersActionPerformed

private void focusOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_focusOnActionPerformed
    Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes == null || nodes.length != 1){
        return;
    }
    Node node = nodes[0];
        if (node instanceof FunctionRootNode){
        update();
        } else if (node instanceof CallNode){
            Call call = ((CallNode)node).getCall();
        if (isCalls) {
            model.setRoot(call.getCallee());
        } else {
            model.setRoot(call.getCaller());
        }
        setName(model.getName());
            setToolTipText(getName()+" - "+getMessage("CTL_CallGraphTopComponent")); // NOI18N
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JTabbedPane) {
                int i = ((JTabbedPane) parent).getSelectedIndex();
                    if (i >=0) {
                    ((JTabbedPane) parent).setTitleAt(i, getName() + "  "); // NOI18N
                }
                break;
            } else if (parent instanceof TopComponent) {
                ((TopComponent) parent).setName(getToolTipText()); // NOI18N
                break;
            }
        }
        update();
    }
}//GEN-LAST:event_focusOnActionPerformed

private void overridingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overridingActionPerformed
    showOverridingAction.actionPerformed(evt);
    
}//GEN-LAST:event_overridingActionPerformed

    private void setShowOverriding(boolean showOverriding){
        isShowOverriding = showOverriding;
        NbPreferences.forModule(CallGraphPanel.class).putBoolean(IS_SHOW_OVERRIDING, isShowOverriding);        
    }

    private void setShowParameters(boolean showParameters){
        isShowParameters = showParameters;
        NbPreferences.forModule(CallGraphPanel.class).putBoolean(IS_SHOW_PARAMETERS, isShowParameters);
        updateButtons();
        update();
    }

   private void setDirection(boolean direction){
        isCalls = direction;
        NbPreferences.forModule(CallGraphPanel.class).putBoolean(IS_CALLS, isCalls);
        updateButtons();
        update();
    }

    private void updateButtons(){
        calls.setSelected(isCalls);
        callers.setSelected(!isCalls);
        overriding.setSelected(isShowOverriding);
    }

    private String getMessage(String key, Object ... parameters) {
        return messagesCatalog.getMessage(key, parameters);
    }

    public void setModel(CallModel model) {
        this.model = model;
        Function function = model.getRoot();
        if (function != null && !function.kind().equals(Function.Kind.FUNCTION)) {
            isCalls = false;
        }
        updateButtons();
        update();
    }
    
    public CallModel getModel() {
        return model;
    }
           
    private synchronized void update() {
        final CallGraphState state = new CallGraphState(model, scene, actions);
        if (showGraph) {
            scene.setModel(state);
        }
        if (showGraph) {
            scene.clean();
        }
        //model.getRoot() can be too expensive to invoke it in UI thread
        RP.post(new Runnable() {

            @Override
            public void run() {
                final Function function = model.getRoot();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (function != null) {
                            final Children children = root.getChildren();
                            if (!Children.MUTEX.isReadAccess()) {
                                Children.MUTEX.writeAccess(new Runnable() {
                                    @Override
                                    public void run() {
                                        children.remove(children.getNodes());
                                        Node selectedNode = null;
                                        //if root of the model is invisible
                                        if (!model.isRootVisible()) {
                                            List<Call> childrenList = isCalls ? model.getCallees(function) : model.getCallers(function);
                                            Node[] functions = new Node[childrenList.size()];
                                            for (int i = 0; i < childrenList.size(); i++) {
                                                Call call = childrenList.get(i);
                                                Function f = isCalls ? call.getCallee() : call.getCaller();
                                                functions[i] = new FunctionRootNode(f, state, isCalls);
                                            }
                                            if (functions.length > 0) {
                                                selectedNode = functions[0];
                                            }
                                            children.add(functions);
                                        } else {
                                            selectedNode = new FunctionRootNode(function, state, isCalls);
                                            children.add(new Node[]{selectedNode});
                                        }
                                        final Node node = selectedNode;
                                        try {
                                            getExplorerManager().setSelectedNodes(new Node[]{node});
                                        } catch (PropertyVetoException ex) {
                                        }
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                getTreeView().expandNode(node);
                                            }
                                        });
                                    }
                                });
                            }
                        } else {
                            final Children children = root.getChildren();
                            if (!Children.MUTEX.isReadAccess()) {
                                Children.MUTEX.writeAccess(new Runnable() {
                                    @Override
                                    public void run() {
                                        children.remove(children.getNodes());
                                    }
                                });
                            }
                        }
                    }
                });
            }            
        });
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        treeView.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return treeView.requestFocusInWindow();
    }

    public final BeanTreeView getTreeView(){
        return (BeanTreeView)treeView;
    }

    private ContextPanel getContextPanel(){
        return (ContextPanel)contextPanel;
    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("CallGraphView"); // NOI18N
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton callers;
    private javax.swing.JToggleButton calls;
    private javax.swing.JPanel contextPanel;
    private javax.swing.JButton focusOn;
    private javax.swing.JScrollPane graphView;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton overriding;
    private javax.swing.JButton refresh;
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables

    private final class RefreshAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public RefreshAction() {
            putValue(Action.NAME, getMessage("RefreshAction"));  // NOI18N
            putValue(Action.SMALL_ICON, refresh.getIcon());
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshActionPerformed(e);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private final class WhoCallsAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public WhoCallsAction() {
            putValue(Action.NAME, getMessage("CallersAction"));  // NOI18N
            putValue(Action.SMALL_ICON, callers.getIcon());
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setDirection(false);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!isCalls);
            return menuItem;
        }
    }

    private final class WhoIsCalledAction extends AbstractAction implements Presenter.Popup {
        private final JRadioButtonMenuItem menuItem;
        public WhoIsCalledAction() {
            putValue(Action.NAME, getMessage( "CallsAction"));  // NOI18N
            putValue(Action.SMALL_ICON, calls.getIcon());
            menuItem = new JRadioButtonMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setDirection(true);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(isCalls);
            return menuItem;
        }
    }

    private final class ShowOverridingAction extends org.netbeans.modules.cnd.callgraph.api.ui.CallGraphAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowOverridingAction() {
            super(new CallGraphActionEDTRunnable() {

                @Override
                public void run() {                    
                    updateButtons();
                    update();
                }
            });
            putValue(Action.NAME, getMessage( "ShowOverridingAction"));  // NOI18N
            putValue(Action.SMALL_ICON, overridingIcon);
            menuItem = new JCheckBoxMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void doNonEDTAction() {
            setShowOverriding(!isShowOverriding);
            model.update();
        }

        @Override
        public void doEDTAction() {
        
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(isShowOverriding);
            return menuItem;
        }
    }

    private final class ShowFunctionParameters extends AbstractAction implements Presenter.Popup {
        private final JCheckBoxMenuItem menuItem;
        public ShowFunctionParameters() {
            putValue(Action.NAME, getMessage("ShowFunctionSignature"));  // NOI18N
            menuItem = new JCheckBoxMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setShowParameters(!isShowParameters);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(isShowParameters);
            return menuItem;
        }
    }

    private final class FocusOnAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public FocusOnAction() {
            putValue(Action.NAME, getMessage("FocusOnAction"));  // NOI18N
            putValue(Action.SMALL_ICON, focusOn.getIcon());
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            focusOnActionPerformed(e);
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }
    
    private void expandAllImpl(final Node n) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                expandAllWorker(n);
            }

        });
    }
    
    private void expandAllWorker(Node n) {
        final AtomicBoolean canceled = new AtomicBoolean(false);
        ProgressHandle progress = ProgressHandleFactory.createHandle(getMessage("ExpandAll"), new Cancellable() {  // NOI18N
            
            @Override
            public boolean cancel() {
                canceled.set(true);
                return true;
            }
        });
        progress.start();
        try {
            List<List<Node>> list = new ArrayList<List<Node>>();
            expandAllImpl(progress, canceled, n, list);
            int count = 0;
            for(int i = 0; i < list.size(); i++) {
                for(Node node : list.get(i)) {
                    getTreeView().expandNode(node);
                    count++;
                    if (count >= MAX_EXPANDED_TREE_NODES) {
                        int allCount = 0;
                        for(int j = 0; j < list.size(); j++) {
                            allCount += list.get(j).size();
                        }
                        StatusDisplayer.getDefault().setStatusText(getMessage("ExpandedLimitWarning", count, allCount, i, list.size(), allCount));
                        return;
                    }
                }
            }
        } finally {
            progress.finish();
        }
    }
    
    private void expandAllImpl(ProgressHandle progress, AtomicBoolean canceled, Node node, List<List<Node>> list) {
        int level = 0;
        if (list.size() < level+1) {
            list.add(new ArrayList<Node>());
        }
        list.get(level).add(node);
        while(true) {
            level++;
            if (list.size() < level+1) {
                list.add(new ArrayList<Node>());
            }
            List<Node> prev = list.get(level-1);
            if (prev.isEmpty()) {
                return;
            }
            for(Node n : prev) {
                updateProgress(progress, list);
                Children children = n.getChildren();
                if (canceled.get()) {
                    return;
                }
                if (children != null) {
                    if (children instanceof CallChildren) {
                        ((CallChildren) children).init();
                    }
                    if (canceled.get()) {
                        return;
                    }
                    for (Node subNode : children.getNodes()) {
                        list.get(level).add(subNode);
                        updateProgress(progress, list);
                        if (canceled.get()) {
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private void updateProgress(ProgressHandle progress, List<List<Node>> list) {
        int count = 0;
        for(int i = 0; i < list.size(); i++) {
            count += list.get(i).size();
        }
        if (count%100 == 0) {
            progress.progress(getMessage("ExpandedProgress", count, list.size()));  // NOI18N
        }
    }   
    
    private final class ExpandAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public ExpandAction() {
            putValue(Action.NAME, getMessage("Expand"));  // NOI18N
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }     

        @Override
        public void actionPerformed(ActionEvent e) {
            Node[] nodes = getExplorerManager().getSelectedNodes();
            if (nodes == null || nodes.length == 0){
                getTreeView().expandAll();                
            } else {
                getTreeView().expandNode(nodes[0]);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }    

    private final class ExpandAllAction extends AbstractAction implements Presenter.Popup {
        private final JMenuItem menuItem;
        public ExpandAllAction() {
            putValue(Action.NAME, getMessage("ExpandAll"));  // NOI18N
            menuItem = new JMenuItem(this);
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }     

        @Override
        public void actionPerformed(ActionEvent e) {
            Node[] nodes = getExplorerManager().getSelectedNodes();
            if (nodes == null || nodes.length == 0){
                expandAllImpl(root);
            } else {
                expandAllImpl(nodes[0]);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private static final class ContextPanel extends JPanel implements ExplorerManager.Provider {
        private final ExplorerManager managerCtx = new ExplorerManager();
        private final ListView listView = new ListView();
        private final CallGraphUI graphUI;

        private ContextPanel(CallGraphUI graphUI){
            this.graphUI = graphUI;
            initDefaultView();
        }

        private void initDefaultView() {
            removeAll();
            listView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AM")); // NOI18N
            listView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AD")); // NOI18N
            setLayout(new java.awt.BorderLayout());
            add(listView, java.awt.BorderLayout.CENTER);
            listView.setFocusable(false);
            listView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return managerCtx;
        }

        @Override
        public boolean requestFocusInWindow() {
            super.requestFocusInWindow();
            return listView.requestFocusInWindow();
        }

        private void setRootContent(final CallNode node){
            //allow implementator to implement call as a node
            initDefaultView();
            Collection<Node> list = new ArrayList<Node>(1);
            list.add(new CallContext(new LoadingOccurencesNode()));
            Node root  = new CallContextRoot(new ContextList(list));
            getExplorerManager().setRootContext(root);
            revalidate();
            RP.post(new Runnable() {

                @Override
                public void run() {
                    Collection<Node> list;
                    Node root;
                    if (node == null) {
                        list = Collections.<Node>emptyList();
                        root  = new CallContextRoot(new ContextList(list));
                    } else {
                        Call call = node.getCall();
                        list = new ArrayList<Node>(call.getOccurrences().size());
                        final JPanel contextPanel = graphUI == null ? null : graphUI.getContextPanel(call);
                        if (contextPanel != null) {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    removeAll();
                                    setLayout(new java.awt.BorderLayout());
                                    add(contextPanel, java.awt.BorderLayout.CENTER);
                                    revalidate();

                                }
                            });
                            return;
                        } else {
                            if (call instanceof Node) {
                                root = (Node)call;

                            } else {
                                for (Call.Occurrence occurrence : call.getOccurrences()) {
                                    list.add(new CallContext(occurrence));
                                }
                                root  = new CallContextRoot(new ContextList(list));
                            }
                        }
                    }
                    final Node rootNode = root;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            initDefaultView();
                            getExplorerManager().setRootContext(rootNode);
                            revalidate();
                        }
                    });

                }
            });

        }
        private static final class ContextList extends Children.Array {
            private ContextList(Collection<Node> nodes){
                super(nodes);
            }
        }

        private static final class CallContextRoot extends AbstractNode {
            public CallContextRoot(Children.Array array) {
                super(array);
            }
        }

        public static class CallContext extends AbstractNode {
            private final Call.Occurrence occurrence;
            public CallContext(Call.Occurrence element) {
                super( Children.LEAF);
                occurrence = element;
            }

            @Override
            public String getHtmlDisplayName() {
                if (occurrence != null) {
                    return occurrence.getHtmlDisplayName();
                }
                return super.getHtmlDisplayName();
            }
            @Override
            public Action getPreferredAction() {
                return new GoToOccurrenceAction(occurrence);
            }

            @Override
            public String getShortDescription() {
                String ret = occurrence.getDescription();
                if (ret != null){
                    return ret;
                }
                return super.getShortDescription();
            }

            @Override
            public Action[] getActions(boolean context) {
                return new Action[0];
            }

            @Override
            public Image getIcon(int type) {
                if (occurrence instanceof Node) {
                    return ((Node)occurrence).getIcon(type);
                }
                return super.getIcon(type);
            }
        }
    }

    public static class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy {
        private final ArrayList<Component> order;
        private final Container panel;
        public MyOwnFocusTraversalPolicy(Container panel, List<Component> order) {
            this.order = new ArrayList<Component>(order.size());
            this.order.addAll(order);
            this.panel = panel;
        }
        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            if (focusCycleRoot == panel) {
                int idx = getIndex(aComponent);
                idx = (idx + 1) % order.size();
                return getComponentAtIndex(idx);
            }
            return null;
        }

        private int getIndex(Component aComponent) {
            int idx = order.indexOf(aComponent);
            while (idx == -1) {
                aComponent = aComponent.getParent();
                if (aComponent == null) {
                    return -1;
                }
                idx = order.indexOf(aComponent);
            }
            return idx;
        }

        private Component getComponentAtIndex(int idx) {
            if (idx == 0) {
                return ((BeanTreeView)order.get(idx)).getViewport().getView();
            } else if (idx == 1) {
                    return ((ContextPanel)order.get(idx)).listView.getViewport().getView();
            }
            return order.get(idx);
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            if (focusCycleRoot == panel) {
                int idx = getIndex(aComponent) - 1;
                if (idx < 0) {
                    idx = order.size() - 1;
                }
                return getComponentAtIndex(idx);
            }
            return null;
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            if (focusCycleRoot == panel) {
                return getComponentAtIndex(0);
            }
            return null;
        }

        @Override
        public Component getLastComponent(Container focusCycleRoot) {
            if (focusCycleRoot == panel) {
                return getComponentAtIndex(order.size()-1);
            }
            return null;
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            if (focusCycleRoot == panel) {
                return getComponentAtIndex(0);
            }
            return null;
        }
    }
    
    private static class LoadingOccurencesNode extends AbstractNode implements Call.Occurrence {
        public LoadingOccurencesNode() {
            super(Children.LEAF);
            setName("dummy"); // NOI18N
            setDisplayName(NbBundle.getMessage(getClass(), "Loading")); // NOI18N
        }

        @Override
        public Image getIcon(int param) {
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/callgraph/resources/waitNode.gif"); // NOI18N
        }

        @Override
        public void open() {
        }
        
        @Override
        public String getHtmlDisplayName() {
            return getDescription();
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(getClass(), "Loading");
        }

        @Override
        public String getDescription() {
            return  getDisplayName();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LoadingNode) {
                return this == obj;
            }
            return false;
        }
    }
}
