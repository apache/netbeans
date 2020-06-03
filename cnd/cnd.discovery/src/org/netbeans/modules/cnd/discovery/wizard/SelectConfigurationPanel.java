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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.tree.FileConfigurationNode;
import org.netbeans.modules.cnd.discovery.wizard.tree.FolderConfigurationNode;
import org.netbeans.modules.cnd.discovery.wizard.tree.IncludesListModel;
import org.netbeans.modules.cnd.discovery.wizard.tree.MacrosListModel;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationNode;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class SelectConfigurationPanel extends JPanel {
    private static final RequestProcessor RP = new RequestProcessor(SelectConfigurationPanel.class.getName(), 1);
    private final SelectConfigurationWizard wizard;
    private boolean showResulting;
    private boolean wasTerminated = false;
    private boolean isStoped = false;

    /** Creates new form DiscoveryVisualPanel2 */
    public SelectConfigurationPanel(SelectConfigurationWizard wizard) {
        this.wizard = wizard;
        initComponents();
        configurationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addListeners();
        clearListModels();
    }

    private void addListeners(){
        configurationTree.addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateListModels();
            }
        });
        showInherited.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                showResulting = showInherited.isSelected();
                updateListModels();
            }
        });
    }

    private void updateListModels() {
        TreePath path = configurationTree.getSelectionPath();
        if (path != null) {
            Object selected = path.getLastPathComponent();
            if (selected instanceof ProjectConfigurationNode){
                ProjectConfigurationNode node = (ProjectConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getProject(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getProject(),showResulting));
                includeInherate.setSelected(false);
                macroInherate.setSelected(false);
            } else if (selected instanceof FolderConfigurationNode){
                FolderConfigurationNode node = (FolderConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getFolder(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getFolder(),showResulting));
                includeInherate.setSelected(node.isCheckedInclude());
                macroInherate.setSelected(node.isCheckedMacro());
            } else if (selected instanceof FileConfigurationNode){
                FileConfigurationNode node = (FileConfigurationNode)selected;
                includesList.setModel(new IncludesListModel(node.getFile(),showResulting));
                macrosList.setModel(new MacrosListModel(node.getFile(),showResulting));
                includeInherate.setSelected(node.isCheckedInclude());
                macroInherate.setSelected(node.isCheckedMacro());
            } else {
                clearListModels();
            }
        } else {
            clearListModels();
        }
    }

    private void clearListModels(){
        includesList.setModel(new EmptyListModel());
        macrosList.setModel(new EmptyListModel());
        includeInherate.setSelected(false);
        macroInherate.setSelected(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        configurationTree = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        includesList = new javax.swing.JList();
        includeInherate = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        macrosList = new javax.swing.JList();
        macroInherate = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        showInherited = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(180);
        jSplitPane1.setResizeWeight(0.5);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setLabelFor(configurationTree);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("TreeConfigurationTitle")); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
        jPanel1.add(jLabel1, java.awt.BorderLayout.NORTH);

        configurationTree.setRootVisible(false);
        configurationTree.setShowsRootHandles(true);
        jScrollPane1.setViewportView(configurationTree);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jLabel2.setLabelFor(includesList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("InludePathsListTitle")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
        jPanel3.add(jLabel2, java.awt.BorderLayout.NORTH);

        jScrollPane2.setViewportView(includesList);

        jPanel3.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(includeInherate, bundle.getString("InheriteParentIncludePathsText")); // NOI18N
        includeInherate.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0));
        includeInherate.setEnabled(false);
        includeInherate.setFocusable(false);
        includeInherate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(includeInherate, java.awt.BorderLayout.SOUTH);

        jSplitPane2.setTopComponent(jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel3.setLabelFor(macrosList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("UserMacrosListTitle")); // NOI18N
        jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0));
        jPanel4.add(jLabel3, java.awt.BorderLayout.NORTH);

        jScrollPane3.setViewportView(macrosList);

        jPanel4.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(macroInherate, bundle.getString("InheriteParentMacrosText")); // NOI18N
        macroInherate.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0));
        macroInherate.setEnabled(false);
        macroInherate.setFocusable(false);
        macroInherate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel4.add(macroInherate, java.awt.BorderLayout.SOUTH);

        jSplitPane2.setRightComponent(jPanel4);

        jPanel2.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(showInherited, bundle.getString("ShowInheritedConfigurationName")); // NOI18N
        showInherited.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        showInherited.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel5.add(showInherited, java.awt.BorderLayout.CENTER);

        add(jPanel5, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private static String getString(String key) {
        return NbBundle.getMessage(SelectConfigurationPanel.class, key);
    }

    private Icon getLoadingIcon() {
        String path = "org/netbeans/modules/cnd/discovery/wizard/resources/waitNode.gif"; // NOI18N
        Image image = ImageUtilities.loadImage(path);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }

    void read(final DiscoveryDescriptor wizardDescriptor) {
        if (wizardDescriptor.isInvokeProvider() || wasTerminated) {
            // clear model
            wizardDescriptor.setConfigurations(null);
            ConfigurationTreeModel model = new ConfigurationTreeModel();
            DefaultMutableTreeNode loading= new DefaultMutableTreeNode(getString("LoadingRootText")); // NOI18N
            ((DefaultMutableTreeNode)model.getRoot()).add(loading);
            DefaultTreeCellRenderer renderer= new DefaultTreeCellRenderer();
            configurationTree.setCellRenderer(renderer);
            renderer.setLeafIcon(getLoadingIcon());
            configurationTree.setModel(model);
            // count configurations in other thread.
            AnalyzingTask task = new AnalyzingTask(wizardDescriptor);
            RP.post(task);
            //task.start();
            isStoped = false;
            wasTerminated = true;
        }
        List<ProjectConfiguration> projectConfigurations = wizardDescriptor.getConfigurations();
        if (projectConfigurations != null) {
            for(ProjectConfiguration project : projectConfigurations){
                ConsolidationStrategy.consolidateModel(project);
            }
        }
        updateListModels();
    }

    private void creteTreeModel(DiscoveryDescriptor wizardDescriptor){
        ConfigurationTreeModel model = new ConfigurationTreeModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        List<ProjectConfiguration> projectConfigurations = wizardDescriptor.getConfigurations();
        if (projectConfigurations != null) {
            for(ProjectConfiguration project : projectConfigurations){
                root.add(new ProjectConfigurationNode((ProjectConfigurationImpl)project));
            }
        }
        configurationTree.setCellRenderer(new DefaultTreeCellRenderer());
        configurationTree.setModel(model);
    }

    void store(DiscoveryDescriptor wizardDescriptor) {
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        if (provider != null && wasTerminated){
            //System.out.println("Stop analyzing");
            isStoped = true;
            provider.cancel();
        }
    }

    boolean isValid(DiscoveryDescriptor settings) {
        List<ProjectConfiguration> projectConfigurations = settings.getConfigurations();
        if (projectConfigurations == null || projectConfigurations.isEmpty()) {
            return false;
        }
        return projectConfigurations.get(0).getFiles().size()>0;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree configurationTree;
    private javax.swing.JCheckBox includeInherate;
    private javax.swing.JList includesList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JCheckBox macroInherate;
    private javax.swing.JList macrosList;
    private javax.swing.JCheckBox showInherited;
    // End of variables declaration//GEN-END:variables


    private static class ConfigurationTreeModel extends DefaultTreeModel {
        public ConfigurationTreeModel() {
            super(new DefaultMutableTreeNode("Root")); // NOI18N
        }
    }

    public static class EmptyListModel extends AbstractListModel<String> {
        @Override
        public int getSize() {
            return 0;
        }
        @Override
        public String getElementAt(int i) {
            return null;
        }
    }

    private class AnalyzingTask extends Thread {
        private final DiscoveryDescriptor wizardDescriptor;
        public AnalyzingTask(DiscoveryDescriptor wizardDescriptor){
            this.wizardDescriptor = wizardDescriptor;
        }
        @Override
        public void run() {
            try {
                DiscoveryExtension.buildModel(wizardDescriptor, null);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        creteTreeModel(wizardDescriptor);
                        wizard.stateChanged(null);
                    }
                });
                //System.out.println("End analyzing");
                if (!isStoped){
                    wasTerminated = false;
                }
            } catch (Throwable ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    public static class MyProgress implements Progress {
        private ProgressHandle handle;
        private int done;
        private int length;
        private final String message;

        public MyProgress(String message) {
            this.message = message;
        }

        @Override
        public void start() {
            start(0);
        }

        @Override
        public void start(int length) {
            if (handle != null) {
                handle.finish();
            }
            handle = ProgressHandleFactory.createHandle(message);
            handle.start(length);
            done = 0;
            this.length = length;
        }

        @Override
        public void increment(String message) {
            if (handle != null) {
                done++;
                if (message == null) {
                    handle.progress(done);
                } else {
                    int i = message.lastIndexOf('\\');
                    if (i < 0) {
                        i = message.lastIndexOf('/');
                    }
                    if (i > 0) {
                        String msg = NbBundle.getMessage(SelectConfigurationPanel.class, "MSG_ParsingProgressFull", ""+done, ""+length, message.substring(i+1)); // NOI18N
                        handle.progress(msg, done);
                    } else {
                        handle.progress(done);
                    }
                }
            }
        }

        @Override
        public void done() {
            if (handle != null) {
                handle.finish();
                handle = null;
            }
        }

    }
}
