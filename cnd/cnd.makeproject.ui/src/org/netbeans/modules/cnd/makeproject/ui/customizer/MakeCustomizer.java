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
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.MakeProjectCustomizerEx;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public final class MakeCustomizer extends javax.swing.JPanel implements HelpCtx.Provider {

    private Component currentCustomizer;
    private PropertyNode currentConfigurationNode = null;
    private final GridBagConstraints fillConstraints;
    private final Project project;
    private DialogDescriptor dialogDescriptor;
    private final ConfigurationDescriptor projectDescriptor;
    private final List<Item> items;
    private final List<Folder> folders;
    private final List<JComponent> controls;
    private CategoryView currentCategoryView;
    private String currentNodeName;
    private Configuration[] configurationItems;
    private Configuration[] selectedConfigurations;
    private int lastComboboxIndex = -1;
    private MakeContext lastContext;

    /** Creates new form MakeCustomizer */
    public MakeCustomizer(Project project, String preselectedNodeName, ConfigurationDescriptor projectDescriptor, List<Item> items, List<Folder> folders, Collection<JComponent> controls) {
        initComponents();
        this.projectDescriptor = projectDescriptor;
        this.controls = new ArrayList<>(controls);
        this.project = project;
        this.items = items;
        this.folders = folders;
        this.controls.add(configurationComboBox);
        this.controls.add(configurationsButton);

        configurationItems = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < configurationItems.length; i++) {
            configurationComboBox.addItem(configurationItems[i]);
        }
        if (configurationItems.length > 1) {
            configurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        }
        if (configurationItems.length > 2) {
            configurationComboBox.addItem(getString("MULTIPLE_CONFIGURATIONS"));
        }
        // Select default configuraton
        int selectedIndex = projectDescriptor.getConfs().getActiveAsIndex();
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        configurationComboBox.setSelectedIndex(selectedIndex);
        calculateSelectedConfs();

        HelpCtx.setHelpIDString(customizerPanel, "org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer"); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class, "AD_MakeCustomizer")); // NOI18N
        fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, items, folders), preselectedNodeName);
        currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class, "AN_BeanTreeViewCategories")); // NOI18N
        currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class, "AD_BeanTreeViewCategories")); // NOI18N
        categoryPanel.add(currentCategoryView, fillConstraints);

        // Accessibility
        configurationsButton.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_BUTTON_AD"));
        configurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATION_COMBOBOX_AD"));

        allConfigurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        allConfigurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_BUTTON_AD"));
        allConfigurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATION_COMBOBOX_AD"));
        allConfigurationComboBox.setToolTipText(getString("ALL_CONFIGURATIONS_TOOLTIP"));
    }
    private final Map<Item, SharedItemConfiguration> itemConfigurations = new HashMap<>();

    private SharedItemConfiguration getSharedItemConfiguration(Item item) {
        if (item == null) {
            return null;
        }
        SharedItemConfiguration res = itemConfigurations.get(item);
        if (res == null) {
            res = new SharedItemConfiguration(item);
            itemConfigurations.put(item, res);
        }
        return res;
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        categoryLabel = new javax.swing.JLabel();
        categoryPanel = new javax.swing.JPanel();
        propertyPanel = new javax.swing.JPanel();
        configurationPanel = new javax.swing.JPanel();
        configurationLabel = new javax.swing.JLabel();
        configurationComboBox = new javax.swing.JComboBox();
        allConfigurationComboBox = new javax.swing.JComboBox();
        configurationsButton = new javax.swing.JButton();
        customizerPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(900, 520));
        setLayout(new java.awt.GridBagLayout());

        categoryLabel.setLabelFor(categoryPanel);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, bundle.getString("CATEGORIES_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(categoryLabel, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setMinimumSize(new java.awt.Dimension(220, 4));
        categoryPanel.setPreferredSize(new java.awt.Dimension(220, 4));
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(categoryPanel, gridBagConstraints);
        categoryPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer_categoryPanel")); // NOI18N
        categoryPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer_categoryPanel")); // NOI18N

        propertyPanel.setLayout(new java.awt.GridBagLayout());

        configurationPanel.setLayout(new java.awt.GridBagLayout());

        configurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATION_COMBOBOX_MNE").charAt(0));
        configurationLabel.setLabelFor(configurationComboBox);
        configurationLabel.setText(bundle.getString("CONFIGURATION_COMBOBOX_LBL")); // NOI18N
        configurationPanel.add(configurationLabel, new java.awt.GridBagConstraints());

        configurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboBoxActionPerformed(evt);
            }
        });
        configurationPanel.add(configurationComboBox, new java.awt.GridBagConstraints());
        configurationPanel.add(allConfigurationComboBox, new java.awt.GridBagConstraints());

        org.openide.awt.Mnemonics.setLocalizedText(configurationsButton, bundle.getString("CONFIGURATIONS_BUTTON_LBL")); // NOI18N
        configurationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        configurationPanel.add(configurationsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        propertyPanel.add(configurationPanel, gridBagConstraints);

        customizerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        propertyPanel.add(customizerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(propertyPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configurationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationsButtonActionPerformed
        MyListEditorPanel configurationsEditor = new MyListEditorPanel(projectDescriptor);
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(configurationsEditor, gridBagConstraints);

        Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
        DialogDescriptor dd = new DialogDescriptor(outerPanel, getString("CONFIGURATIONS_EDITOR_TITLE"), true, options, NotifyDescriptor.OK_OPTION, 0, null, null);

        DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
        java.awt.Dialog dl = dialogDisplayer.createDialog(dd);
        //dl.setPreferredSize(new java.awt.Dimension(400, (int)dl.getPreferredSize().getHeight()));
        dl.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_EDITOR_AD"));
        dl.pack();
        dl.setSize(new java.awt.Dimension(400, (int) dl.getPreferredSize().getHeight()));
        try {
            dl.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CLOSED_OPTION);
        } finally {
            dl.dispose();
        }
        // Update data structure
        Configuration[] editedConfs = configurationsEditor.getListData().toArray(new Configuration[configurationsEditor.getListData().size()]);
        int active = -1;
        for (int i = 0; i < editedConfs.length; i++) {
            if (editedConfs[i].isDefault()) {
                active = i;
                break;
            }
        }
        projectDescriptor.getConfs().init(editedConfs, active);
        // Update gui with changes
        ActionListener[] actionListeners = configurationComboBox.getActionListeners();
        configurationComboBox.removeActionListener(actionListeners[0]); // assuming one and only one!
        configurationComboBox.removeAllItems();
        configurationComboBox.addActionListener(actionListeners[0]); // assuming one and only one!
        configurationItems = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < configurationItems.length; i++) {
            configurationComboBox.addItem(configurationItems[i]);
        }
        if (configurationItems.length > 1) {
            configurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        }
        if (configurationItems.length > 2) {
            configurationComboBox.addItem(getString("MULTIPLE_CONFIGURATIONS"));
        }
        configurationComboBox.setSelectedIndex(configurationsEditor.getSelectedIndex());
        calculateSelectedConfs();
    }//GEN-LAST:event_configurationsButtonActionPerformed

    private void configurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboBoxActionPerformed
        calculateSelectedConfs();
        refresh();
    }//GEN-LAST:event_configurationComboBoxActionPerformed

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public MakeContext getLastContext() {
        return lastContext;
    }

    public void refresh() {
        if (currentCategoryView != null) {
            String selectedNodeName = currentNodeName;
            categoryPanel.remove(currentCategoryView);
            currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, items, folders), null);
            currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class, "AN_BeanTreeViewCategories"));
            currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class, "AD_BeanTreeViewCategories"));
            categoryPanel.add(currentCategoryView, fillConstraints);
            if (selectedNodeName != null) {
                currentCategoryView.selectNode(selectedNodeName);
            }
        }
    }

    public void save() {
        if (lastContext != null) {
            lastContext.save();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox allConfigurationComboBox;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JComboBox configurationComboBox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JButton configurationsButton;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JPanel propertyPanel;
    // End of variables declaration//GEN-END:variables

    // HelpCtx.Provider implementation -----------------------------------------
    @Override
    public HelpCtx getHelpCtx() {
        if (currentConfigurationNode != null) {
            return HelpCtx.findHelp(currentConfigurationNode);
        } else {
            return null;
        }
    }

    // Private innerclasses ----------------------------------------------------
    private final class CategoryView extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager;
        private final BeanTreeView btv;
        private final String preselectedNodeName;

        CategoryView(Node rootNode, String preselectedNodeName) {
            this.preselectedNodeName = preselectedNodeName;
            // See #36315
            manager = new ExplorerManager();

            setLayout(new BorderLayout());

            Dimension size = new Dimension(220, 4);
            btv = new BeanTreeView();    // Add the BeanTreeView
            btv.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            btv.setPopupAllowed(false);
            btv.setRootVisible(true);
            btv.setDefaultActionAllowed(false);
            btv.setMinimumSize(size);
            btv.setPreferredSize(size);
            btv.setMaximumSize(size);
            btv.setDragSource(false);
            btv.setRootVisible(false);
            btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class, "AN_BeanTreeViewCategories"));
            btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class, "AD_BeanTreeViewCategories"));
            this.add(btv, BorderLayout.CENTER);
            manager.setRootContext(rootNode);
            ManagerChangeListener managerChangeListener = new ManagerChangeListener();
            manager.addPropertyChangeListener(managerChangeListener);
            selectNode(preselectedNodeName);
            //btv.expandAll();
            //expandCollapseTree(rootNode, btv);

            // Add been tree view to controls so it can be enabled/disabled correctly
            controls.add(btv);
        }

        private void expandCollapseTree(Node rootNode, BeanTreeView btv) {
            Children children = rootNode.getChildren();
            Node[] nodes1 = children.getNodes();
            for (int i = 0; i < nodes1.length; i++) {
                if (nodes1[i].getName().equals("Build") || nodes1[i].getName().equals("Debuggers")) // NOI18N
                {
                    btv.expandNode(nodes1[i]);
                } else {
                    btv.collapseNode(nodes1[i]);
                }
            }
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            //btv.expandAll();
            expandCollapseTree(manager.getRootContext(), btv);
            if (preselectedNodeName != null && preselectedNodeName.length() > 0) {
                selectNode(preselectedNodeName);
            }
        }

        private Node findNode(Node pnode, String name) {
            // First try all children of this node
            Node node = NodeOp.findChild(pnode, name);
            if (node != null) {
                return node;
            }
            // Then try it's children
            Children ch = pnode.getChildren();
            Node nodes[] = ch.getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node cnode = findNode(nodes[i], name);
                if (cnode != null) {
                    return cnode;
                }
            }

            return null;
        }

        private void selectNode(String name) {
            Node node = null;
            if (name != null) {
                node = findNode(manager.getRootContext(), name);
            }
            if (node == null) {
                node = (manager.getRootContext().getChildren().getNodes()[0]);
            }
            if (node != null) {
                try {
                    manager.setSelectedNodes(new Node[]{node});
                } catch (Exception e) {
                }
            }
        }

        /** Listens to selection change and shows the customizers as
         *  panels
         */
        private class ManagerChangeListener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() != manager) {
                    return;
                }

                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node nodes[] = manager.getSelectedNodes();
                    if (nodes == null || nodes.length <= 0) {
                        return;
                    }
                    Node node = nodes[0];
                    currentNodeName = node.getName();

                    if (currentCustomizer != null) {
                        customizerPanel.remove(currentCustomizer);
                    }
                    JComponent panel = new JPanel();
                    panel.setLayout(new java.awt.GridBagLayout());
                    currentConfigurationNode = (PropertyNode) node;
                    if (currentConfigurationNode.customizerStyle() == CustomizerNode.CustomizerStyle.PANEL) {
                        // IG investigate
                        panel.add(currentConfigurationNode.getPanel(null), fillConstraints);
                        configurationLabel.setEnabled(false);
                        configurationComboBox.setEnabled(false);
                        configurationsButton.setEnabled(true);
                        configurationComboBox.setVisible(false);
                        allConfigurationComboBox.setVisible(true);
                        allConfigurationComboBox.setEnabled(false);
                    } else if (currentConfigurationNode.customizerStyle() == CustomizerNode.CustomizerStyle.SHEET) {
                        panel.setBorder(new javax.swing.border.EtchedBorder());
                        PropertySheet propertySheet = new PropertySheet(); // See IZ 105525 for details.
                        List<DummyNode> dummyNodes = new ArrayList<>(selectedConfigurations.length * nodes.length);
                        for (Node selNode : nodes) {
                            if (selNode instanceof PropertyNode && ((PropertyNode) selNode).customizerStyle() == CustomizerNode.CustomizerStyle.SHEET) {
                                PropertyNode propNode = (PropertyNode) selNode;
                                for (int i = 0; i < selectedConfigurations.length; i++) {
                                    Sheet[] sheets = propNode.getSheets(selectedConfigurations[i]);
                                    if (sheets != null) {
                                        for (Sheet sheet : sheets) {
                                            if (((MakeConfigurationDescriptor) projectDescriptor).hasProjectCustomizer()) {
                                                MakeProjectCustomizerEx makeProjectCustomizer = (MakeProjectCustomizerEx) ((MakeConfigurationDescriptor) projectDescriptor).getProjectCustomizer();
                                                sheet = makeProjectCustomizer.getPropertySheet(sheet);
                                            }

                                            dummyNodes.add(new DummyNode(sheet, selectedConfigurations[i].getName()));
                                        }
                                    }
                                }
                            }
                        }
                        propertySheet.setNodes(dummyNodes.toArray(new DummyNode[dummyNodes.size()]));

                        panel.add(propertySheet, fillConstraints);
                        configurationLabel.setEnabled(true);
                        configurationComboBox.setEnabled(true);
                        configurationsButton.setEnabled(true);
                        configurationComboBox.setVisible(true);
                        allConfigurationComboBox.setVisible(false);
                    } else {
                        configurationLabel.setEnabled(false);
                        configurationComboBox.setEnabled(false);
                        configurationsButton.setEnabled(false);
                        configurationComboBox.setVisible(true);
                        allConfigurationComboBox.setVisible(false);
                    }
                    customizerPanel.add(panel, fillConstraints);
                    customizerPanel.validate();
                    customizerPanel.repaint();
                    currentCustomizer = panel;

                    CndUIUtilities.requestFocus(btv);

                    if (dialogDescriptor != null && currentConfigurationNode != null) {
                        dialogDescriptor.setHelpCtx(HelpCtx.findHelp(currentConfigurationNode));
                    }
                    return;
                }
            }
        }
    }

    private void calculateSelectedConfs() {
        if (configurationComboBox.getSelectedIndex() < configurationItems.length) {
            // One selected
            selectedConfigurations = new Configuration[]{(MakeConfiguration) configurationComboBox.getSelectedItem()};
            lastComboboxIndex = configurationComboBox.getSelectedIndex();
        } else if (configurationComboBox.getSelectedIndex() == configurationItems.length) {
            // All selected
            selectedConfigurations = configurationItems;
            lastComboboxIndex = configurationComboBox.getSelectedIndex();
        } else {
            // Some Selected
            while (true) {
                ConfSelectorPanel confSelectorPanel = new ConfSelectorPanel(getString("SELECTED_CONFIGURATIONS_LBL"), 'v', configurationItems, null);
                DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, getString("MULTIPLE_CONFIGURATIONS_TITLE"));
                DialogDisplayer.getDefault().notify(dd);
                if (dd.getValue() != DialogDescriptor.OK_OPTION) {
                    if (lastComboboxIndex <= configurationItems.length) {
                        configurationComboBox.setSelectedIndex(lastComboboxIndex);
                    }
                    break;
                }
                if (confSelectorPanel.getSelectedConfs().length > 1) {
                    selectedConfigurations = confSelectorPanel.getSelectedConfs();
                    lastComboboxIndex = configurationComboBox.getSelectedIndex();
                    break;
                } else {
                    String errormsg = getString("SELECT_MORE");
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }
    }

    // Private methods ---------------------------------------------------------
    private Node createRootNode(Project project, ConfigurationDescriptor projectDescriptor, List<Item> items, List<Folder> folders) {
        if (items != null) {
            List<SharedItemConfiguration> configs = new ArrayList<>();
            for (Item item : items) {
                if (item != null) {
                    configs.add(getSharedItemConfiguration(item));
                }
            }
            lastContext = new MakeContext(MakeContext.Kind.Item, project, getSelectedHost(), selectedConfigurations)
                    .setSharedItem(configs.toArray(new SharedItemConfiguration[configs.size()]))
                    .setConfigurationDescriptor(projectDescriptor);
            Lookup lookup = Lookups.fixed(lastContext);
            return ItemNodeFactory.createRootNodeItem(lookup);
        } else if (folders != null) {
            lastContext = new MakeContext(MakeContext.Kind.Folder, project, getSelectedHost(), selectedConfigurations)
                    .setFolders(folders.toArray(new Folder[folders.size()]))
                    .setConfigurationDescriptor(projectDescriptor);
            Folder[] array = folders.toArray(new Folder[folders.size()]);
            Lookup lookup = Lookups.fixed(lastContext, array);
            return FolderNodeFactory.createRootNodeFolder(lookup);
        } else {
            lastContext = new MakeContext(MakeContext.Kind.Project, project, getSelectedHost(), selectedConfigurations)
                    .setPanel(this)
                    .setConfigurationDescriptor(projectDescriptor);
            Lookup lookup = Lookups.fixed(lastContext, project); // FIXUP: MakeContext should be public so other projects could make use of the context. Adding 'project' so they can at least get that.
            return ProjectNodeFactory.createRootNodeProject(lookup);
        }
    }

    private ExecutionEnvironment getSelectedHost() {
        ExecutionEnvironment execEnv;
        if (configurationComboBox.getSelectedIndex() < configurationItems.length) {
            MakeConfiguration conf = (MakeConfiguration) configurationComboBox.getSelectedItem();
            execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        } else {
            // All or Multiple Configurations are selected.
            // Which host to use? let's calculate
            execEnv = ExecutionEnvironmentFactory.getLocal();
            if (selectedConfigurations != null && selectedConfigurations.length > 0) {
                for (int i = 0; i < selectedConfigurations.length; i++) {
                    MakeConfiguration conf = (MakeConfiguration) selectedConfigurations[i];
                    execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
                    if (execEnv.isLocal()) {
                        // found localhost => can break and does not check others
                        break;
                    }
                }
            }
        }
        return execEnv;
    }
    
    private static final class DummyNode extends AbstractNode {

        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null) {
                setSheet(sheet);
            }
            setName(name);
        }
    }
    
    private static final class MyListEditorPanel extends ListEditorPanel<Configuration> {
        final ConfigurationDescriptor projectDescriptor;
        public MyListEditorPanel(ConfigurationDescriptor descriptor) {
            super(descriptor.getConfs().getConfigurations());
            projectDescriptor = descriptor;
            setAllowedToRemoveAll(false);
        }

        private Folder getTestsRootFolder(MakeConfigurationDescriptor projectDescriptor) {
            Folder root = projectDescriptor.getLogicalFolders();
            for (Folder folder : root.getFolders()) {
                if (folder.isTestRootFolder()) {
                    return folder;
                }
            }
            return null;
        }

        // This initializations are required for test folders.
        // See org.netbeans.modules.cnd.simpleunit.editor.filecreation.TestSimpleIterator,
        // org.netbeans.modules.cnd.cncppunit.editor.filecreation.TestCppUnitIterator
        private void setCppUnitOptions(Configuration cfg, Folder rootFolder) {
            rootFolder.getAllTests().forEach((testFolder) -> {
                FolderConfiguration folderConfiguration = testFolder.getFolderConfiguration(cfg);
                LinkerConfiguration linkerConfiguration = folderConfiguration.getLinkerConfiguration();
                LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();
                librariesConfiguration.add(new LibraryItem.OptionItem("`cppunit-config --libs`")); // NOI18N
                linkerConfiguration.getOutput().setValue("${TESTDIR}/" + testFolder.getPath()); // NOI18N
                CCompilerConfiguration cCompilerConfiguration = folderConfiguration.getCCompilerConfiguration();
                CCCompilerConfiguration ccCompilerConfiguration = folderConfiguration.getCCCompilerConfiguration();
                cCompilerConfiguration.getCommandLineConfiguration().setValue("`cppunit-config --cflags`"); // NOI18N;
                ccCompilerConfiguration.getCommandLineConfiguration().setValue("`cppunit-config --cflags`"); // NOI18N;
                cCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
                ccCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
            });
        }

        @Override
        public Configuration addAction() {
            String newName = ConfigurationSupport.getUniqueNewName(getConfs());
            int type = MakeConfiguration.TYPE_MAKEFILE;
            String customizerId = null;
            String buildDir = null;
            if (getActive() != null) {
                type = ((MakeConfiguration) getActive()).getConfigurationType().getValue();
                customizerId = ((MakeConfiguration) getActive()).getCustomizerId();
                buildDir = ((MakeConfiguration) getActive()).getMakefileConfiguration().getBuildCommandWorkingDirValue();
            }
            Configuration newconf = projectDescriptor.defaultConf(newName, type, customizerId);
            if (buildDir != null) {
                ((MakeConfiguration) newconf).getMakefileConfiguration().getBuildCommandWorkingDir().setValue(buildDir);
            }
            ((MakeConfiguration) newconf).reCountLanguages((MakeConfigurationDescriptor) projectDescriptor);
            Configuration result = editActionImpl(newconf) ? newconf : null;
            Folder testFolder = getTestsRootFolder((MakeConfigurationDescriptor) projectDescriptor);
            if (testFolder != null) {
                setCppUnitOptions(result, testFolder);
            }
            return result;
        }

        @Override
        public Configuration copyAction(Configuration o) {
            Configuration c = o;
            Configuration copyConf = c.copy();
            copyConf.setDefault(false);
            copyConf.setName(ConfigurationSupport.getUniqueCopyName(getConfs(), c));
            copyConf.setCloneOf(null);
            return copyConf;
        }

        @Override
        public void removeAction(Configuration o, int i) {
            Configuration c = o;
            if (c.isDefault()) {
                if (getListData().get(0) == o) {
                    (getListData().get(1)).setDefault(true);
                } else {
                    (getListData().get(0)).setDefault(true);
                }
            }
            CompileOptionsProvider.getDefault().onRemove((MakeConfigurationDescriptor) projectDescriptor, (MakeConfiguration) c);
        }

        @Override
        public void defaultAction(Configuration o) {
            List<Configuration> confs = getListData();
            confs.forEach((c) -> {
                c.setDefault(false);
            });
            o.setDefault(true);
        }

        @Override
        public void editAction(Configuration o, int i) {
            editActionImpl(o);
        }

        /**
         * Edits name
         * @return true in the case user pressed OK, otherwise (if s/he pressed CANCEL) false
         */
        private boolean editActionImpl(Configuration o) {
            Configuration c = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("CONFIGURATION_RENAME_DIALOG_LABEL"), getString("CONFIGURATION_RENAME_DIALOG_TITLE")); // NOI18N
            notifyDescriptor.setInputText(c.getName());
            // Rename conf
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return false;
            }
            if (c.getName().equals(notifyDescriptor.getInputText())) {
                return true; // didn't change the name
            }
            String suggestedName = ConfigurationSupport.makeNameLegal(notifyDescriptor.getInputText());
            String name = ConfigurationSupport.getUniqueName(getConfs(), suggestedName);
            CompileOptionsProvider.getDefault().onRename((MakeConfigurationDescriptor) projectDescriptor, (MakeConfiguration) c, name);
            c.setName(name);
            return true;
        }

        @Override
        public String getListLabelText() {
            return getString("CONFIGURATIONS_LIST_NAME");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("CONFIGURATIONS_LIST_MNE").charAt(0);
        }

        public Configuration[] getConfs() {
            return getListData().toArray(new Configuration[getListData().size()]);
        }

        public Configuration getActive() {
            Configuration[] confs = getConfs();
            Configuration active = null;
            for (int i = 0; i < confs.length; i++) {
                if (confs[i].isDefault()) {
                    active = confs[i];
                    break;
                }
            }
            return active;
        }

        @Override
        protected void checkSelection() {
            super.checkSelection();
            int i = getSelectedIndex();
            if (i < 0) {
                return;
            }
            Configuration conf = getListData().get(i);
            getDefaultButton().setEnabled(!conf.isDefault());
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(MakeCustomizer.class).getString(s);
    }
}
