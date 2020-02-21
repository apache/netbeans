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
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;

import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;
import org.netbeans.modules.cnd.debugger.common2.utils.MRUComboBoxModel;
import org.netbeans.modules.cnd.debugger.common2.utils.ProcessListSupport;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.TreePathSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 */
public final class ProcessListPanel extends javax.swing.JPanel
        implements ExplorerManager.Provider, ChangeListener, Lookup.Provider {

    private static final boolean IS_TREE_VIEW_ENABLED = Boolean.valueOf(System.getProperty("cnd.debugger.common2.attach.treeview", "false"));//NOI18N
    private final static int FILTER_DELAY = 200;
    private final static int REFRESH_DELAY = 1000;
    private final ProviderLock providerLock = new ProviderLock();
    private final ExplorerManager manager = new ExplorerManager();
    private final Task filterTask;
    private final Task refreshTask;
    private final Lookup lookup;
    private ProcessView currentView = null;
    private ProcessListSupport.Provider listProvider = null;
    //private ProcessActionsSupport.Provider actionsProvider;
    private boolean autorefresh = false;
    private boolean showHierarchy = false;
    private ProcessesRootNode rootNode;
    private final InstanceContent content = new InstanceContent();
    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListenerImpl();
    private ProcessPanelCustomizer customizer = new DefaultCustomizer();
    private final ProcessFilter filter = new ProcessFilter();
    private static final RequestProcessor RP  = new RequestProcessor(ProcessListPanel.class.getName(), 1);
    private static final Preferences prefs =
            NbPreferences.forModule(ProcessListPanel.class);
    private final ChangeSupport changeSupport;
    
    private Lookup.Result<ProcessInfo> lookupResult = null;
    private MRUComboBoxModel filterModel;
    private static final Preferences filterPrefs =
            prefs.node("attach_filters");			//NOI18N  
        private static final Preferences lastFilterPrefs =
            prefs.node("attach_last_filer_value");			//NOI18N      
    
    /** Creates new form AttachToProcessPanel */
    public ProcessListPanel() {
        changeSupport = new ChangeSupport(this);
        lookup = new AbstractLookup(content);
        content.add(filter);

        initComponents();

        // Setting selected index in the model will force an update
        // (which fires the action event) and we're not ready for
        // that yet
       // filterReady = false;

//        // Fix up the combo box models
//        Vector<String> filters2 = restoreFilterPrefs();
//        filterModel = new MRUComboBoxModel(filters2);
//        filterCombo.setModel(filterModel);
//        
        filterLabel.setLabelFor(filterCombo);
        filterCombo.setEditable(true);
        filterCombo.setStorage("attach.panel", filterPrefs);//NOI18N
        filterCombo.read(lastFilterPrefs.get(LAST_FILTER_VALUE, ""));//NOI18N
        
        final JTextComponent cbEditor = (JTextComponent) filterCombo.getEditor().getEditorComponent();
        filter.set(cbEditor.getText());
        cbEditor.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }
        });
        filterTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                JTextComponent cbEditor = (JTextComponent) filterCombo.getEditor().getEditorComponent();
                filter.set(cbEditor.getText());
                updateChildren(getSelectedInfo());
            }
        });

//        filterFld.getDocument().addDocumentListener(new DocumentListener() {
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                filterTask.schedule(FILTER_DELAY);
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                filterTask.schedule(FILTER_DELAY);
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                filterTask.schedule(FILTER_DELAY);
//            }
//        });

        refreshTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                synchronized (providerLock) {
                    if (listProvider != null) {
                        listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
                    }
                }

                if (autorefresh) {
                    refreshTask.schedule(REFRESH_DELAY);
                }
            }
        });

        manager.addPropertyChangeListener(propertyChangeListener);        
    }
    private static final String LAST_FILTER_VALUE = "value";//NOI18N
    
//    //
//    // To support 6646693
//    //
//    private static final String PREF_COUNT = "count"; // NOI18N
//    private static final String PREF_ITEM = "item_"; // NOI18N
//
//    private void saveFilterPrefs() {
//        try {
//            filterPrefs.clear();
//
//            filterPrefs.putInt(PREF_COUNT, filterCombo.getItemCount());
//            for (int ix = 0; ix < filterCombo.getItemCount(); ix++) {
//                String item = (String) filterCombo.getItemAt(ix);
//                filterPrefs.put(PREF_ITEM + ix, item);
//            }
//
//            // Make changes appear on disc now
//            prefs.flush();
//
//        } catch (java.util.prefs.BackingStoreException x) {
//            return;
//        }
//    }
//    
//    
//    private Vector<String> restoreFilterPrefs() {
//        Vector<String> items = new Vector<String>();
//        int count = filterPrefs.getInt(PREF_COUNT, 0);
//
//        if (count == 0) {
//            // first time ever ... add the default match-all pattern
//            items.add("");
//            return items;
//        }
//
//        for (int ix = 0; ix < count; ix++) {
//            String item = filterPrefs.get(PREF_ITEM + ix, null);
//            if (item != null) {
//                items.add(item);
//            }
//        }
//        return items;
//    }    
//    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }    

    public void setCustomizer(ProcessPanelCustomizer customizer) {
        this.customizer = customizer;
    }

    class PropertyChangeListenerImpl implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selectedNodes = manager.getSelectedNodes();
                if (selectedNodes.length == 0) {
                    content.set(Collections.emptyList(), null);
                    changeSupport.fireChange();
                } else if (selectedNodes[0] instanceof ProcessNode) {
                    ProcessInfo info = ((ProcessNode) selectedNodes[0]).getInfo();
                    content.set(Arrays.asList(info), null);
                    changeSupport.fireChange();
                }
            }
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /*package*/ void setLoading() {
        if (rootNode == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setLoading();
                }
            });
            return;
        }
        this.rootNode.setLoading();
        currentView.expandNodes();
             
    }

    public void setListProvider(final ProcessListSupport.Provider newProvider) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setListProvider(newProvider);
                }
            });

            return;
        }

        synchronized (providerLock) {
            if (newProvider == listProvider) {
                return;
            }

            if (listProvider != null) {
                listProvider.removeChangeListener(this);
            }

            listProvider = newProvider;

            processListPanel.removeAll();

            if (newProvider != null) {
                newProvider.addChangeListener(this);
                currentView = new ProcessView(customizer.getHeaders(newProvider), customizer);
                processListPanel.add(currentView);
                refreshTask.schedule(0);
            } else {
                processListPanel.add(new JLabel("<No process list provider>")); // NOI18N
            }

            updateRootNode();

            revalidate();
            repaint();
        }
    }

//    public void setActionsProvider(ProcessActionsSupport.Provider newProvider) {
//        this.actionsProvider = newProvider;
//    }

    @Override
    public void addNotify() {
        super.addNotify();
        filterCombo.read(lastFilterPrefs.get(LAST_FILTER_VALUE, ""));//NOI18N
        if (listProvider != null) {
            listProvider.addChangeListener(this);
        }
    }

    @Override
    public void removeNotify() {

        if (refreshTask != null) {
            refreshTask.cancel();
        }

        synchronized (providerLock) {
            if (listProvider != null) {
                listProvider.removeChangeListener(this);
            }
        }
        lastFilterPrefs.put(LAST_FILTER_VALUE, filterCombo.getText());
        filterCombo.store();
        super.removeNotify();
    }

    private void updateRootNode() {
        assert SwingUtilities.isEventDispatchThread();

//        if (actionsProvider == null) {
//            actionsProvider = ProcessActionsSupport.getDefault();
//        }

        final ProcessInfo selectedInfo = getSelectedInfo();

        rootNode = new ProcessesRootNode(showHierarchy, customizer, filter);
        manager.setRootContext(rootNode);

        updateChildren(selectedInfo);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        filterCombo = new org.netbeans.modules.cnd.utils.ui.EditableComboBox();
        filterLabel = new javax.swing.JLabel();
        refreshBtn = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        processListPanel = new javax.swing.JPanel();
        processInfoPanel = new org.netbeans.modules.cnd.debugger.common2.ui.processlist.ProcessInfoPanel();
        treeTogleButton = new javax.swing.JToggleButton();
        listTogleButton = new javax.swing.JToggleButton();
        userProcessesOnlyCheckBox = new javax.swing.JCheckBox();

        filterLabel.setLabelFor(filterCombo);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.filterLabel.text")); // NOI18N

        refreshBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshBtn, org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.refreshBtn.text")); // NOI18N
        refreshBtn.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.refreshBtn.toolTipText")); // NOI18N
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.8);

        processListPanel.setMinimumSize(new java.awt.Dimension(200, 50));
        processListPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        processListPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setTopComponent(processListPanel);

        processInfoPanel.setMinimumSize(new java.awt.Dimension(100, 50));
        splitPane.setBottomComponent(processInfoPanel);

        buttonGroup1.add(treeTogleButton);
        treeTogleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/tree-toggle16.png"))); // NOI18N
        treeTogleButton.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.treeTogleButton.text")); // NOI18N
        treeTogleButton.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.toolTipText")); // NOI18N
        treeTogleButton.setName(""); // NOI18N
        treeTogleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeTogleButtonActionPerformed(evt);
            }
        });
        treeTogleButton.setVisible(IS_TREE_VIEW_ENABLED);

        buttonGroup1.add(listTogleButton);
        listTogleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/list-toggle16.png"))); // NOI18N
        listTogleButton.setSelected(true);
        listTogleButton.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.listTogleButton.text_1")); // NOI18N
        listTogleButton.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.listTogleButton.toolTipText")); // NOI18N
        listTogleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listTogleButtonActionPerformed(evt);
            }
        });
        listTogleButton.setVisible(IS_TREE_VIEW_ENABLED);

        userProcessesOnlyCheckBox.setSelected(prefs.getBoolean("showAllUserProcesses", false));//NOI18N
        userProcessesOnlyCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/ui/processlist/Bundle").getString("ProcessListPanel.userProcessesOnlyCheckBox.text.mn").charAt(0));
        userProcessesOnlyCheckBox.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.userProcessesOnlyCheckBox.text")); // NOI18N
        userProcessesOnlyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userProcessesOnlyCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(splitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(filterLabel)
                                .addGap(2, 2, 2)
                                .addComponent(filterCombo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(refreshBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(userProcessesOnlyCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(treeTogleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listTogleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filterLabel)
                            .addComponent(refreshBtn)
                            .addComponent(filterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(userProcessesOnlyCheckBox))
                    .addComponent(treeTogleButton)
                    .addComponent(listTogleButton))
                .addGap(5, 5, 5)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        if (listProvider != null) {
            setLoading();
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void treeTogleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeTogleButtonActionPerformed
        showHierarchy = true;
        updateRootNode();
        if (listProvider != null) {
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_treeTogleButtonActionPerformed

    private void listTogleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listTogleButtonActionPerformed
        showHierarchy = false;
        updateRootNode();
        if (listProvider != null) {
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_listTogleButtonActionPerformed

    private void userProcessesOnlyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userProcessesOnlyCheckBoxActionPerformed
        // TODO add your handling code here:
        if (listProvider != null) {
            setLoading();
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
        prefs.putBoolean("showAllUserProcesses", userProcessesOnlyCheckBox.isSelected());//NOI18N
    }//GEN-LAST:event_userProcessesOnlyCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private org.netbeans.modules.cnd.utils.ui.EditableComboBox filterCombo;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JToggleButton listTogleButton;
    private org.netbeans.modules.cnd.debugger.common2.ui.processlist.ProcessInfoPanel processInfoPanel;
    private javax.swing.JPanel processListPanel;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToggleButton treeTogleButton;
    private javax.swing.JCheckBox userProcessesOnlyCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src instanceof ProcessListSupport.Provider) {
            updateChildren(getSelectedInfo());
        }
    }

    public void setFilter(String filter) {
        this.filterCombo.getEditor().setItem(filter);
    }

    public String getFilter() {
        return this.filterCombo.getEditor().getItem() + "";
    }

    /**
     * Returns currently selected ProcessInfo. 
     * Should be called from AWT.
     * 
     * @return currently selected ProcessInfo
     */
    public ProcessInfo getSelectedInfo() {
        return currentView == null ? null : currentView.getSelectedInfo();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private void updateChildren(final ProcessInfo selectedInfo) {
        if (rootNode == null || listProvider == null) {
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateChildren(selectedInfo);
                }
            });
            return;
        }

        updateChildrenInAWT(selectedInfo);
    }

    private void updateChildrenInAWT(final ProcessInfo selectedInfo) {
        setLoading();
        RP.post(new Runnable() {
            @Override
            public void run() {
                final ProcessList processList = listProvider.getProcessList();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        rootNode.refresh(processList);
                        try {
                            currentView.expandNodes();
                            if (selectedInfo != null) {
                                Node node = rootNode.getNode(selectedInfo.getPID());
                                if (node != null) {
                                    manager.setSelectedNodes(new Node[]{node});
                                }
                            }
                        } catch (PropertyVetoException ex) {
                        }                       
                    }
                });
            }
        });
        

        
    }

    private static class ProcessView extends OutlineView {

        private final Set<Integer> collapsedPIDs = new HashSet<Integer>();
        private final TreeExpansionListener expansionListener;

        private ProcessView(final List<ProcessInfoDescriptor> descriptors, final ProcessPanelCustomizer customizer) {
            super(customizer.getOutlineHeaderName());

            setDragSource(false);
            setDropTarget(false);

            expansionListener = new TreeExpansionListener() {

                @Override
                public void treeExpanded(TreeExpansionEvent event) {
                    assert SwingUtilities.isEventDispatchThread();
                    Node node = Visualizer.findNode(event.getPath().getLastPathComponent());

                    if (node == null) {
                        return;
                    }

                    ProcessInfo info = node.getLookup().lookup(ProcessInfo.class);

                    if (info == null) {
                        return;
                    }

                    collapsedPIDs.remove(info.getPID());
                    // ??? 
                    // if collapse a child node and then collapse a parent node
                    // then do refresh and expand the parent, the child will be 
                    // also expanded! 
                    // so do own expansion that will collapse collapsed nodes
                    expandNodes();
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent event) {
                    assert SwingUtilities.isEventDispatchThread();
                    Node node = Visualizer.findNode(event.getPath().getLastPathComponent());

                    if (node == null) {
                        return;
                    }

                    ProcessInfo info = node.getLookup().lookup(ProcessInfo.class);

                    if (info == null) {
                        return;
                    }

                    collapsedPIDs.add(info.getPID());
                }
            };

            this.addTreeExpansionListener(expansionListener);
            setAllowedDropActions(DnDConstants.ACTION_NONE);

            Outline outline = getOutline();
            outline.setRootVisible(false);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            outline.setCellEditor(null);
            Property[] props = new Property[descriptors.size()];

            int idx = 0;

            for (ProcessInfoDescriptor d : descriptors) {
                props[idx++] = new PrototypeProperty(d.id, d.header, d.shortDescription);
            }

            setProperties(props);

            ETableColumnModel colModel = (ETableColumnModel) outline.getColumnModel();
            TableColumn firstColumn = colModel.getColumn(0);
            ETableColumn col = (ETableColumn) firstColumn;
            col.setNestedComparator(customizer);
        }

        private ProcessInfo getSelectedInfo() {
            ExplorerManager manager = ExplorerManager.find(ProcessView.this);

            if (manager == null) {
                return null;
            }

            Node[] selectedNodes = manager.getSelectedNodes();
            return selectedNodes.length == 0 ? null
                    : selectedNodes[0].getLookup().lookup(ProcessInfo.class);
        }

        private void expandNodes() {
            assert SwingUtilities.isEventDispatchThread();

            ExplorerManager manager = ExplorerManager.find(ProcessView.this);

            if (manager == null || !(manager.getRootContext() instanceof ProcessesRootNode)) {
                return;
            }

            removeTreeExpansionListener(expansionListener);

            try {
                ProcessesRootNode rootNode = (ProcessesRootNode) manager.getRootContext();

                expandAll(new TreePath(getOutline().getOutlineModel().getRoot()),
                        (TreeNode) getOutline().getOutlineModel().getRoot());

                for (Integer pid : collapsedPIDs) {
                    final Node node = rootNode.getNode(pid);
                    if (node != null) {
                        collapseNode(node);
                    }
                }
            } finally {
                addTreeExpansionListener(expansionListener);
            }
        }

        private void expandAll(TreePath path, TreeNode node) {
            if (node == null) {
                return;
            }

            TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
            tps.expandPath(path);

            Enumeration children = node.children();
            while (children.hasMoreElements()) {
                Object nextElement = children.nextElement();
                expandAll(path.pathByAddingChild(nextElement), (TreeNode) nextElement);
            }
        }
    }

    static final class PrototypeProperty extends PropertySupport.ReadOnly<Object> {

        PrototypeProperty(String name, String displayName, String description) {
            super(name, Object.class, displayName, description);
        }

        @Override
        public Object getValue() throws IllegalAccessException,
                InvocationTargetException {
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof Property
                    && getName().equals(((Property) o).getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }
    }

    private static class DefaultCustomizer implements ProcessPanelCustomizer {

        @Override
        public String getDisplayName(ProcessInfo info) {
            return Integer.toString(info.getPID());
        }

        @Override
        public List<ProcessInfoDescriptor> getValues(ProcessInfo info) {
            return info.getDescriptors();
        }

        @Override
        public List<ProcessInfoDescriptor> getHeaders(ProcessListSupport.Provider provider) {
            return provider.getDescriptors();
        }

        @Override
        public int compare(AbstractNode o1, AbstractNode o2) {
            if (o1 instanceof ProcessNode && o2 instanceof ProcessNode) {
                Integer pid1 = ((ProcessNode) o1).getInfo().getPID();
                Integer pid2 = ((ProcessNode) o2).getInfo().getPID();
                return pid1.compareTo(pid2);
            }

            return 1;
        }

        @Override
        public String getOutlineHeaderName() {
            return NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.outlineHeaderName"); // NOI18N
        }
    }

    private final static class ProviderLock {
    };
}
