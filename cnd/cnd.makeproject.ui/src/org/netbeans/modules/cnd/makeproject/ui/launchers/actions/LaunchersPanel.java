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
package org.netbeans.modules.cnd.makeproject.ui.launchers.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.ui.launchers.actions.LaunchersConfig.LauncherConfig;
import org.netbeans.modules.cnd.makeproject.ui.runprofiles.ListTableModel;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class LaunchersPanel extends JPanel implements ExplorerManager.Provider, MakeContext.Savable {

    private final ExplorerManager manager = new ExplorerManager();
    private final SelectionChangeListener listener = new SelectionChangeListener();
    private final ArrayList<LauncherConfig> launchers = new ArrayList<>();
    private final LaunchersNodes nodes;
    private LauncherConfig selectedConfiguration;
    private final LaunchersConfig instance;
    private final ListTableModel envVarModel;
    private final JTable envVarTable;
    private final TreeView tree;
    private boolean modified = false;
    private volatile boolean resetFields = true;
    private final Preferences panelPreferences = NbPreferences.forModule(getClass()).node("launchers"); // NOI18N
    private static final String COMMPLETION_ACTION = "completion"; //NOI18N

    /**
     * Creates new form LaunchersPanel
     */
    public LaunchersPanel(Project project, final boolean standAloneDialog) {
        if (standAloneDialog) {
            setPreferredSize(new Dimension(panelPreferences.getInt("dialogSizeW", 640), // NOI18N
                                           panelPreferences.getInt("dialogSizeH", 450))); // NOI18N
        } else {
            setPreferredSize(new Dimension(640, 450));
        }
        setMinimumSize(new Dimension(400, 200));
        initComponents();
        addHierarchyListener((HierarchyEvent e) -> {
            if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
                if (!e.getChanged().isVisible()){
                    if (standAloneDialog) {
                        panelPreferences.putInt("dialogSizeW", getSize().width); // NOI18N
                        panelPreferences.putInt("dialogSizeH", getSize().height); // NOI18N
                    }
                    if (selectedConfiguration != null) {
                        int index = launchers.indexOf(selectedConfiguration);
                        panelPreferences.putInt("lastSelecion", index); // NOI18N
                    }
                }
            }
        });
        envVarModel = new ListTableModel(NbBundle.getMessage(LaunchersPanel.class, "EnvName"),
                                 NbBundle.getMessage(LaunchersPanel.class, "EnvValue"));
	envVarTable = new JTable(envVarModel);
	envVarModel.setTable(envVarTable);
	envVarScrollPane.setViewportView(envVarTable);
        envVarTable.getSelectionModel().addListSelectionListener(this::validateEnvButtons);

        manager.addPropertyChangeListener(listener);
        instance = new LaunchersConfig(project);
        instance.load();
        launchers.addAll(instance.getLaunchers());
        nodes = new LaunchersNodes(launchers);
        tree = new BeanTreeViewImpl();
        tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        LauncersListPanel.add(tree, BorderLayout.CENTER);
        update();
        final ActionListener actionListener = (ActionEvent e) -> {
            if (!resetFields) {
                updateListViewItem();
            }
        };
        publicCheckBox.addActionListener(actionListener);
        hideCheckBox.addActionListener(actionListener);
        final DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }
        };
        launcherNameTextField.getDocument().addDocumentListener(documentListener);
        runTextField.getDocument().addDocumentListener(documentListener);
        {
            InputMap im = runTextField.getInputMap();
            ActionMap am = runTextField.getActionMap();
            im.put(KeyStroke.getKeyStroke("ctrl SPACE"), COMMPLETION_ACTION); //NOI18N
            am.put(COMMPLETION_ACTION, new CompletionAction(runTextField));
        }
        {
            InputMap im = runDirTextField.getInputMap();
            ActionMap am = runDirTextField.getActionMap();
            im.put(KeyStroke.getKeyStroke("ctrl SPACE"), COMMPLETION_ACTION); //NOI18N
            am.put(COMMPLETION_ACTION, new CompletionAction(runDirTextField));
        }
        {
            InputMap im = buildTextField.getInputMap();
            ActionMap am = buildTextField.getActionMap();
            im.put(KeyStroke.getKeyStroke("ctrl SPACE"), COMMPLETION_ACTION); //NOI18N
            am.put(COMMPLETION_ACTION, new CompletionAction(buildTextField));
        }
        {
            InputMap im = symbolsTextField.getInputMap();
            ActionMap am = symbolsTextField.getActionMap();
            im.put(KeyStroke.getKeyStroke("ctrl SPACE"), COMMPLETION_ACTION); //NOI18N
            am.put(COMMPLETION_ACTION, new CompletionAction(symbolsTextField));
        }
    }

    @Override
    public void save() {
        updateSelectedConfiguration();
        if (modified) {
            instance.save(launchers);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    final void update() {
        LauncherConfig sc = selectedConfiguration;
        manager.setRootContext(new RootNode(nodes));
        modified = false;

        if (sc == null) {
            if (nodes.getNodesCount() > 0) {
                int index = panelPreferences.getInt("lastSelecion", -1); // NOI18N
                if (index < 0) {
                    index = 0;
                } else {
                    if (index >= nodes.getNodesCount()) {
                        index = 0;
                    }
                }
                try {
                    manager.setSelectedNodes(new Node[]{nodes.getNodeAt(index)});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Send this event to activate/deactivate buttons...
                listener.propertyChange(new PropertyChangeEvent(this, ExplorerManager.PROP_SELECTED_NODES, null, null));
            }
        } else {
            selectNode(sc);
        }
    }

    private void selectNode(final LauncherConfig cfg) {
        Children children = manager.getRootContext().getChildren();
        for (Node node : children.getNodes()) {
            if (node instanceof LauncherNode) {
                if (((LauncherNode) node).getConfiguration() == cfg) {
                    try {
                        manager.setSelectedNodes(new Node[]{node});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    break;
                }
            }
        }
    }

    private LauncherConfig getSelectedConfiguration() {
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length == 1 && selectedNodes[0] instanceof LauncherNode) {
            return ((LauncherNode) selectedNodes[0]).getConfiguration();
        } else {
            return null;
        }
    }

    private String getString(String s) {
        return s.trim().replace('\n', ' ').replace('\t', ' ');
    }

    private void updateSelectedConfiguration() {
        if (selectedConfiguration != null) {
            selectedConfiguration.setName(launcherNameTextField.getText().trim());
            selectedConfiguration.setCommand(getString(runTextField.getText()));
            selectedConfiguration.setBuildCommand(buildTextField.getText().trim());
            selectedConfiguration.setRunDir(runDirTextField.getText().trim());
            selectedConfiguration.setSymbolFiles(symbolsTextField.getText().trim());
            selectedConfiguration.setPublic(publicCheckBox.isSelected());
            selectedConfiguration.setHide(hideCheckBox.isSelected());
            selectedConfiguration.setrunInOwnTab(runInOwnTabCheckBox.isSelected());
            if (envVarTable.isEditing()) {
                TableCellEditor cellEditor = envVarTable.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }

            HashMap<String, String> newContent = new HashMap<>();
            for(int i = 0; i < envVarModel.getRowCount(); i++) {
                String key = (String) envVarModel.getValueAt(i, 0);
                String value = (String) envVarModel.getValueAt(i, 1);
                if (key == null || value == null) {
                    continue;
                }
                key = key.trim();
                if (!key.isEmpty()) {
                    newContent.put(key, value.trim());
                    
                }
            }
            if ( selectedConfiguration.getEnv().size() != newContent.size()) {
               modified = true;
            } else {
                modified |= !selectedConfiguration.getEnv().equals(newContent);
            }
            selectedConfiguration.getEnv().clear();
            selectedConfiguration.getEnv().putAll(newContent);
            modified |= selectedConfiguration.isModified();
            updateListViewItem();
        }
    }

    private void updateListViewItem() {
        if (selectedConfiguration != null) {
            Node[] selectedNodes = manager.getSelectedNodes();
            if (selectedNodes.length == 1 && selectedNodes[0] instanceof LauncherNode) {
                LauncherNode node = ((LauncherNode) selectedNodes[0]);
                if (selectedConfiguration == node.getConfiguration()) {
                    node.updateNode(launcherNameTextField.getText().trim(), getString(runTextField.getText()),
                            publicCheckBox.isSelected(), hideCheckBox.isSelected());
                }
            }
        }
    }
    private void enableControls() {
        boolean b = selectedConfiguration != null;
        boolean c = true;
        boolean top = true;
        boolean bottom = true;
        if (b) {
            c = selectedConfiguration.getID() >= 0;
            int index = launchers.indexOf(selectedConfiguration);
            if (index > 1) {
                bottom = index == launchers.size()-1;
                top = index == 2;
            }
        }
        upButton.setEnabled(b && !top);
        downButton.setEnabled(b && !bottom);
        removeButton.setEnabled(b && c);
        copyButton.setEnabled(b && c);
        launcherNameTextField.setEnabled(b && c);
        runTextField.setEnabled(b && c);
        buildTextField.setEnabled(b && c);
        publicCheckBox.setEnabled(b && c);
        hideCheckBox.setEnabled(b && c);
        runInOwnTabCheckBox.setEnabled(b && c);
        runDirTextField.setEnabled(b);
        symbolsTextField.setEnabled(b);
        addEnvButton.setEnabled(b);
        removeEnvButton.setEnabled(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        launchersListLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        LauncersListPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
        launcherNameLabel = new javax.swing.JLabel();
        launcherNameTextField = new javax.swing.JTextField();
        runLabel = new javax.swing.JLabel();
        buildLabel = new javax.swing.JLabel();
        buildTextField = new javax.swing.JTextField();
        runDirLabel = new javax.swing.JLabel();
        runDirTextField = new javax.swing.JTextField();
        symbolLabel = new javax.swing.JLabel();
        symbolsTextField = new javax.swing.JTextField();
        envLabel = new javax.swing.JLabel();
        envVarScrollPane = new javax.swing.JScrollPane();
        addEnvButton = new javax.swing.JButton();
        removeEnvButton = new javax.swing.JButton();
        publicCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        runTextField = new javax.swing.JTextArea();
        hideCheckBox = new javax.swing.JCheckBox();
        runInOwnTabCheckBox = new javax.swing.JCheckBox();

        launchersListLabel.setLabelFor(LauncersListPanel);
        org.openide.awt.Mnemonics.setLocalizedText(launchersListLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.launchersListLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        LauncersListPanel.setMaximumSize(new java.awt.Dimension(300, 2147483647));
        LauncersListPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(copyButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.copyButton.text")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addComponent(launchersListLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(leftPanelLayout.createSequentialGroup()
                                .addComponent(upButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downButton))
                            .addGroup(leftPanelLayout.createSequentialGroup()
                                .addComponent(addButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copyButton)
                        .addGap(0, 39, Short.MAX_VALUE))
                    .addComponent(LauncersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(launchersListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LauncersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(copyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upButton)
                    .addComponent(downButton)))
        );

        launcherNameLabel.setLabelFor(launcherNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(launcherNameLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.launcherNameLabel.text")); // NOI18N
        launcherNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LauncherDisplayNameToolTip")); // NOI18N

        launcherNameTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        runLabel.setLabelFor(runTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runLabel.text")); // NOI18N
        runLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "RunCommandToolTipText")); // NOI18N

        buildLabel.setLabelFor(buildTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buildLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.buildLabel.text")); // NOI18N
        buildLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "BuildCommandToolTip")); // NOI18N

        buildTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        runDirLabel.setLabelFor(runDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runDirLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runDirLabel.text")); // NOI18N
        runDirLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "RunDirectoryToolTip")); // NOI18N

        runDirTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        symbolLabel.setLabelFor(symbolsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(symbolLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.symbolLabel.text")); // NOI18N
        symbolLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "SymbolFilesToolTip")); // NOI18N

        symbolsTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        envLabel.setLabelFor(envVarScrollPane);
        org.openide.awt.Mnemonics.setLocalizedText(envLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.envLabel.text")); // NOI18N
        envLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "EnvToolTip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addEnvButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.addEnvButton.text")); // NOI18N
        addEnvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEnvButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeEnvButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.removeEnvButton.text")); // NOI18N
        removeEnvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEnvButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(publicCheckBox, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.publicCheckBox.text")); // NOI18N
        publicCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "PublicToolTip")); // NOI18N

        runTextField.setColumns(20);
        runTextField.setLineWrap(true);
        runTextField.setRows(5);
        runTextField.setWrapStyleWord(true);
        runTextField.setMinimumSize(new java.awt.Dimension(360, 17));
        jScrollPane1.setViewportView(runTextField);

        org.openide.awt.Mnemonics.setLocalizedText(hideCheckBox, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.hideCheckBox.text")); // NOI18N
        hideCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "HideTooltip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runInOwnTabCheckBox, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runInOwnTabCheckBox.text")); // NOI18N
        runInOwnTabCheckBox.setActionCommand(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runInOwnTabCheckBox.actionCommand")); // NOI18N

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .addComponent(envVarScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runLabel)
                            .addComponent(buildLabel)
                            .addComponent(runDirLabel)
                            .addComponent(symbolLabel)
                            .addComponent(launcherNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(launcherNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(symbolsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(runDirTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buildTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(envLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(runInOwnTabCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rightPanelLayout.createSequentialGroup()
                                .addComponent(publicCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(hideCheckBox)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addEnvButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeEnvButton)))
                .addContainerGap())
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(launcherNameLabel)
                    .addComponent(launcherNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runLabel)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildLabel)
                    .addComponent(buildTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runDirLabel)
                    .addComponent(runDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(symbolLabel)
                    .addComponent(symbolsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(envLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(envVarScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addEnvButton)
                    .addComponent(removeEnvButton)
                    .addComponent(publicCheckBox)
                    .addComponent(hideCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runInOwnTabCheckBox)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        updateSelectedConfiguration();
        int max = 1000;
        for(LauncherConfig cfg : launchers) {
            if (cfg.getID() >= max) {
                max = (cfg.getID() + 1000) / 1000;
                max = max *1000;
            }
        }
        LauncherConfig newConfiguration = new LauncherConfig(max, true);
        newConfiguration.setName("launcher" + max); //NOI18N
        newConfiguration.setCommand("\"${PROJECT_DIR}/${OUTPUT_PATH}\""); //NOI18N
        launchers.add(newConfiguration);
        nodes.restKeys();
        selectNode(newConfiguration);
        //cbScriptConfigurator.setSelectedIndex(0);
        modified = true;
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO: how to make it correctly??

        Node[] selectedNodes = manager.getSelectedNodes();
        Node nodeToSelect = null;
        int i = 0;

        if (selectedNodes.length > 0) {
            Node n = selectedNodes[0];
            Node[] nodes = manager.getRootContext().getChildren().getNodes();

            for (; i < nodes.length; i++) {
                if (nodes[i] == n) {
                    break;
                }
            }

            int idx = i + 1;

            if (idx >= nodes.length) {
                idx = i - 1;
            }

            nodeToSelect = idx < 0 ? null : nodes[idx];
        }

        launchers.remove(getSelectedConfiguration());
        nodes.restKeys();

        if (nodeToSelect != null) {
            try {
                manager.setSelectedNodes(new Node[]{nodeToSelect});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        modified = true;
    }//GEN-LAST:event_removeButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        updateSelectedConfiguration();
        LauncherConfig selectedConfiguration = getSelectedConfiguration();
        int index = launchers.indexOf(selectedConfiguration);
        LauncherConfig newConfiguration;
        if (index == launchers.size()-1) {
            int max = (selectedConfiguration.getID() + 1000) / 1000;
            max = max * 1000;
            newConfiguration = selectedConfiguration.copy(max);
            launchers.add(newConfiguration);
        } else {
            LauncherConfig next = launchers.get(index+1);
            int max = (selectedConfiguration.getID() + next.getID()) / 2;
            newConfiguration = selectedConfiguration.copy(max);
            launchers.add(index+1, newConfiguration);
        }
        nodes.restKeys();
        selectNode(newConfiguration);
        modified = true;
    }//GEN-LAST:event_copyButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length > 0) {
            updateSelectedConfiguration();
            final LauncherConfig current = getSelectedConfiguration();
            int curIndex = launchers.indexOf(current);
            LauncherConfig prev = launchers.get(curIndex-1);
            if (curIndex-2 > 0) {
                 LauncherConfig prevPrev = launchers.get(curIndex-2);
                 int prevIndex = prev.getID();
                 int prevPrevIndex = prevPrev.getID();
                 if (prevPrevIndex < prevIndex) {
                     int candidate = (prevPrevIndex + prevIndex) / 2;
                     if (prevPrevIndex < candidate && candidate < prevIndex) {
                         // set middle index to avoid full renumeration
                         current.setID(candidate);
                     }
                 }
            }
            launchers.set(curIndex, prev);
            launchers.set(curIndex-1, current);
            nodes.restKeys();
            modified = true;
            try {
                manager.setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(() -> {
                Node[] nodes1 = manager.getRootContext().getChildren().getNodes();
                int i = 0;
                Node nodeToSelect = null;
                for (; i < nodes1.length; i++) {
                    if (nodes1[i].getLookup().lookup(LauncherConfig.class) == current) {
                        nodeToSelect = nodes1[i];
                        break;
                    }
                }
                if (nodeToSelect != null){
                    try {
                        manager.setSelectedNodes(new Node[]{nodeToSelect});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length > 0) {
            updateSelectedConfiguration();
            final LauncherConfig current = getSelectedConfiguration();
            int curIndex = launchers.indexOf(current);
            LauncherConfig next = launchers.get(curIndex+1);
            if (curIndex+2 < launchers.size()) {
                LauncherConfig nextNext = launchers.get(curIndex+2);
                int nextIndex = next.getID();
                int nextNextIndex = nextNext.getID();
                if (nextIndex < nextNextIndex) {
                    int catndidate = (nextIndex + nextNextIndex) / 2;
                    if (nextIndex < catndidate && catndidate  < nextNextIndex) {
                        current.setID(catndidate);
                    }
                }
            } else {
                int candidate = (next.getID()+1000) / 1000;

                current.setID(candidate * 1000);
            }
            launchers.set(curIndex, next);
            launchers.set(curIndex+1, current);
            nodes.restKeys();
            modified = true;
            try {
                manager.setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(() -> {
                Node[] nodes1 = manager.getRootContext().getChildren().getNodes();
                int i = 0;
                Node nodeToSelect = null;
                for (; i < nodes1.length; i++) {
                    if (nodes1[i].getLookup().lookup(LauncherConfig.class) == current) {
                        nodeToSelect = nodes1[i];
                        break;
                    }
                }
                if (nodeToSelect != null){
                    try {
                        manager.setSelectedNodes(new Node[]{nodeToSelect});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void reorder(int[] perm) {
        if (perm[0] == 0 && perm[1] == 1) {
            updateSelectedConfiguration();
            Node[] selectedNodes = manager.getSelectedNodes();
            ArrayList<LaunchersConfig.LauncherConfig> copy = new ArrayList<>(launchers);
            for (int i = 0; i < perm.length; i++) {
                launchers.set(perm[i], copy.get(i));
            }
            for (int i = 2; i < launchers.size() - 1; i++) {
                LaunchersConfig.LauncherConfig prev = launchers.get(i - 1);
                LaunchersConfig.LauncherConfig cur = launchers.get(i);
                LaunchersConfig.LauncherConfig next = launchers.get(i + 1);
                if (prev.getID() < cur.getID() && cur.getID() < next.getID()) {
                    // It's OK
                } else if (prev.getID() < next.getID()) {
                    cur.setID((prev.getID() + next.getID()) / 2);

                }
            }
            modified = true;
            ((LaunchersNodes) nodes).restKeys();
            try {
                manager.setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void addEnvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEnvButtonActionPerformed
        envVarModel.addRow();
    }//GEN-LAST:event_addEnvButtonActionPerformed

    private void removeEnvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEnvButtonActionPerformed
        int selectedRow = envVarTable.getSelectedRow();
        if (selectedRow >= 0) {
            envVarModel.removeRows(new int[]{selectedRow});
        }
    }//GEN-LAST:event_removeEnvButtonActionPerformed

    private void validateEnvButtons(ListSelectionEvent e) {
	int[] selRows = envVarTable.getSelectedRows();
        removeButton.setEnabled(envVarModel.getRowCount() > 0 && selRows != null && selRows.length > 0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel LauncersListPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addEnvButton;
    private javax.swing.JLabel buildLabel;
    private javax.swing.JTextField buildTextField;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel envLabel;
    private javax.swing.JScrollPane envVarScrollPane;
    private javax.swing.JCheckBox hideCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel launcherNameLabel;
    private javax.swing.JTextField launcherNameTextField;
    private javax.swing.JLabel launchersListLabel;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JCheckBox publicCheckBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeEnvButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JLabel runDirLabel;
    private javax.swing.JTextField runDirTextField;
    private javax.swing.JCheckBox runInOwnTabCheckBox;
    private javax.swing.JLabel runLabel;
    private javax.swing.JTextArea runTextField;
    private javax.swing.JLabel symbolLabel;
    private javax.swing.JTextField symbolsTextField;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    private static class BeanTreeViewImpl extends BeanTreeView {

        public BeanTreeViewImpl() {
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            // Do nothing
        }

        @Override
        public void setRootVisible(boolean visible) {
            super.setRootVisible(visible);
            tree.setShowsRootHandles(visible);
        }
    }

    private final class SelectionChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                updateSelectedConfiguration();
                selectedConfiguration = getSelectedConfiguration();
                resetFields = true;
                setContent(selectedConfiguration);
                resetFields = false;
                enableControls();
            }
        }

        private void setContent(LauncherConfig cfg) {
            launcherNameTextField.setText(cfg == null ? null : cfg.getName());
            runTextField.setText(cfg == null ? null : cfg.getCommand());
            runTextField.setCaretPosition(0);
            runDirTextField.setText(cfg == null ? null : cfg.getRunDir());
            buildTextField.setText(cfg == null ? null : cfg.getBuildCommand());
            symbolsTextField.setText(cfg == null ? null : cfg.getSymbolFiles());
            publicCheckBox.setSelected(cfg == null ? false : cfg.getPublic());
            hideCheckBox.setSelected(cfg == null ? false : (cfg.isHide() || (cfg.getID() < 0)));
            runInOwnTabCheckBox.setSelected(cfg == null ? false : (cfg.runInOwnTab() || (cfg.getID() < 0)));
	    ArrayList<String> col0 = new ArrayList<>();
	    ArrayList<String> col1 = new ArrayList<>();
            int n;
            if (cfg != null) {
                for(Map.Entry<String,String> e : cfg.getEnv().entrySet()) {
                    col0.add(e.getKey());
                    col1.add(e.getValue());
                }
                n = cfg.getEnv().size();
            } else {
                n = 0;
            }
	    envVarModel.setData(n, col0, col1);
            envVarTable.tableChanged(null);
	}
    }
    
    private static final class LauncherNode extends AbstractNode {

        private Image icon;
        private static JTextField test = new JTextField();
        private String name;
        private String command;
        private boolean pub;
        private boolean hide;
        private int id;

        private LauncherNode(LaunchersConfig.LauncherConfig cfg) {
            super(Children.LEAF, Lookups.fixed(cfg));
            name = cfg.getDisplayedName();
            hide = cfg.isHide();
            command = cfg.getCommand();
            pub = cfg.getPublic();
            id = cfg.getID();
            updateIcon();
        }

        private void updateIcon() {
            final String resources = "org/netbeans/modules/cnd/makeproject/ui/launchers/actions/resources/"; // NOI18N
            String iconFile;
            if (id >= 0) {
                iconFile = pub ? "launcher_public.png" : "launcher_private.png"; // NOI18N
            } else {
                iconFile = pub ? "common_public.png" : "common_private.png"; // NOI18N
            }
            icon = ImageUtilities.icon2Image(ImageUtilities.loadImageIcon(resources + iconFile, false));
        }

        public LauncherConfig getConfiguration() {
            return getLookup().lookup(LauncherConfig.class);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return icon;
        }

        // TODO: How to make this correctly?
        public void updateNode(String name, String command, boolean pub, boolean hide) {
            this.name = name;
            this.command = command;
            this.pub = pub;
            this.hide = hide;
            fireDisplayNameChange(null, getDisplayName());
            updateIcon();
            fireIconChange();
        }

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public String getHtmlDisplayName() {
            if (hide || id < 0) {
                return "<font color='!textInactiveText'>" + getDisplayName() + "</font>"; // NOI18N
            }
            return super.getHtmlDisplayName();
        }

        @Override
        public String getDisplayName() {
            if (id < 0) {
                return NbBundle.getMessage(LaunchersPanel.class, "COMMON_PROPERTIES");
            } else {
                String res = name;
                if (res == null || res.isEmpty()) {
                    res = command;
                }
                return res;
            }
        }

        @Override
        public boolean canCut() {
            return id >= 0;
        }

        @Override
        public boolean canCopy() {
            return id >= 0;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }        

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    private static final class LaunchersNodes extends Children.Keys<LauncherConfig> {
        private final ArrayList<LauncherConfig> launcers;
        
        private LaunchersNodes(final ArrayList<LauncherConfig> launcers) {
            this.launcers = launcers;
            setKeys(launcers);
        }

        public void restKeys() {
            setKeys(launcers);
        }

        @Override
        protected Node[] createNodes(LauncherConfig key) {
            return new LauncherNode[]{new LauncherNode(key)};
        }
    }
    
    private final class RootNode extends AbstractNode implements Index {

        private RootNode(Children nodes) {
            super(nodes);
            getCookieSet().add(this);
        }

        @Override
        public int getNodesCount() {
            return getChildren().getNodesCount();
        }

        @Override
        public Node[] getNodes() {
            return getChildren().getNodes();
        }

        @Override
        public int indexOf(Node node) {
            int i = 0;
            for (Node n : getChildren().getNodes()) {
                if (n == node) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        @Override
        public void reorder() {
        }

        @Override
        public void reorder(int[] perm) {
            LaunchersPanel.this.reorder(perm);
        }

        @Override
        public void move(int x, int y) {
        }

        @Override
        public void exchange(int x, int y) {
        }

        @Override
        public void moveUp(int x) {
        }

        @Override
        public void moveDown(int x) {
        }

        @Override
        public void addChangeListener(ChangeListener chl) {
        }

        @Override
        public void removeChangeListener(ChangeListener chl) {
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }
    
    private class CompletionAction extends AbstractAction {
        private final JTextComponent textComponent;

        private CompletionAction(JTextComponent runTextField) {
            this.textComponent = runTextField;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            final int position = textComponent.getCaretPosition();
            Point location;
            try {
                location = textComponent.modelToView(position).getLocation();
            } catch (BadLocationException e2) {
                e2.printStackTrace(System.err);
                return;
            }
            String text = textComponent.getText();
            CompletionPanel suggestion = new CompletionPanel(textComponent, position, location);
        }
    }
    
    private static class CompletionPanel {
        private final JList list;
        private final JPopupMenu popupMenu;
        private final int insertionPosition;
        private final JTextComponent texComponent;

        public CompletionPanel(JTextComponent textarea, int position, final Point location) {
            this.insertionPosition = position;
            this.texComponent = textarea;
            list = createSuggestionList();
            popupMenu = new JPopupMenu() {
                @Override
                public void requestFocus() {
                    list.requestFocus();
                }
                
                @Override
                public boolean requestFocusInWindow() {
                    return list.requestFocusInWindow();
                }
                
            };
            popupMenu.removeAll();
            popupMenu.setOpaque(false);
            popupMenu.setBorder(null);
            popupMenu.add(list, BorderLayout.CENTER);
            int baseLine = texComponent.getBaseline(0, 0);
            if (baseLine < 0) {
                baseLine = texComponent.getHeight();
            }
            popupMenu.show(texComponent, location.x, baseLine + location.y);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    popupMenu.requestFocus();
                    popupMenu.requestFocusInWindow();
                }
            });
        }

        private JList createSuggestionList() {
            String text = NbBundle.getMessage(LaunchersPanel.class, "CompletionListText"); //NOI18N
            String[] data = text.split("\n"); //NOI18N
            JList list = new JList(data);
            list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        insertSelection();
                        e.consume();
                    }
                }
            });
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        insertSelection();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        hide();
                        e.consume();
                    }
                }
            });
            return list;
        }

        private void hide() {
            popupMenu.setVisible(false);
        }
        
        private void insertSelection() {
            hide();
            if (list.getSelectedValue() != null) {
                try {
                    String selection = (String) list.getSelectedValue();
                    final String selectedSuggestion = selection.substring(0, selection.indexOf(' '));
                    texComponent.getDocument().insertString(insertionPosition, selectedSuggestion, null);
                } catch (BadLocationException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        }
    }
        
}
