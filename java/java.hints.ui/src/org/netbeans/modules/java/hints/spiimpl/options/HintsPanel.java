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

package org.netbeans.modules.java.hints.spiimpl.options;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.java.hints.analysis.AnalyzerImpl;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Configuration;
import org.netbeans.modules.java.hints.spiimpl.refactoring.ConfigurationRenderer;
import org.netbeans.modules.java.hints.spiimpl.refactoring.ConfigurationsComboModel;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanelLogic.HintCategory;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.modules.options.editor.spi.OptionsFilter.Acceptor;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.options.OptionsPanelController.Keywords;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


@Keywords(location="Editor", tabTitle="#CTL_Hints_DisplayName2", keywords={"#CTL_DepScanning", "#CTL_Scope_Desc", "#CTL_Scope_Label"})
public final class HintsPanel extends javax.swing.JPanel   {
    
    private static final String DELETE = "delete";
    private static final String DECLARATIVE_HINT_TEMPLATE_LOCATION = "org-netbeans-modules-java-hints/templates/Inspection.hint";
            static final String[] EXTRA_NODE_KEYWORDS = new String[] {"CTL_DepScanning", "CTL_Scope_Desc", "CTL_Scope_Label"};

    private static final RequestProcessor WORKER = new RequestProcessor(HintsPanel.class.getName(), 1, false, false);

    private HintsPanelLogic logic;
    private DefaultTreeModel errorTreeModel;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ClassPathBasedHintWrapper cpBased;
    private final QueryStatus queryStatus;
    private final boolean showHeavyInspections;
    private final RequestProcessor.Task expandTask = WORKER.create(new Runnable() {
        @Override public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    JTree tree = HintsPanel.this.errorTree;
                    
                    for (int r = 0; r < tree.getRowCount(); r++) {
                        tree.expandRow(r);
                    }
                }
            });
            
        }
    });
    
    //AWT only:
    private HintMetadata toSelect = null;
    
    DefaultMutableTreeNode extraNode = new DefaultMutableTreeNode(NbBundle.getMessage(HintsPanel.class, "CTL_DepScanning")); //NOI18N
    private boolean hasNewHints;
    private boolean confirmed;
    
    @Messages("LBL_Loading=Loading...")
    HintsPanel(@NullAllowed final OptionsFilter filter, @NullAllowed final HintsSettings overlay) {
        this.cpBased = null;
        this.queryStatus = QueryStatus.SHOW_QUERIES;
        this.showHeavyInspections = false;
        WORKER.post(new Runnable() {

            @Override
            public void run() {
                RulesManager.getInstance();
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        HintsPanel.this.removeAll();
                        HintsPanel.this.init(filter, false, overlay != null, true, true, true, true, false);
                        if (overlay != null) {
                            HintsPanel.this.setOverlayPreferences(overlay, false);
                        }
                        buttonsPanel.setVisible(false);
                        searchPanel.setVisible(false);
                        configurationsPanel.setVisible(false);
                    }
                });
            }
        });

        setLayout(new GridBagLayout());
        add(new JLabel(Bundle.LBL_Loading()), new GridBagConstraints());
    }

    public HintsPanel(Configuration preselected, ClassPathBasedHintWrapper cpBased) {
        this.cpBased = cpBased;
        this.queryStatus = QueryStatus.ONLY_ENABLED;
        this.showHeavyInspections = true;
        init(null, true, false, false, true, true, true, false);
        configCombo.setSelectedItem(preselected);
    }
    public HintsPanel(HintMetadata preselected, @NullAllowed final CustomizerContext<?, ?> cc, ClassPathBasedHintWrapper cpBased) {
        this.cpBased = cpBased;
        this.queryStatus = cc == null ? QueryStatus.NEVER : QueryStatus.SHOW_QUERIES;
        this.showHeavyInspections = true;
        init(null, true, false, false, false, cc == null, false, cc != null);
        select(preselected);
        configurationsPanel.setVisible(false);
        
        if (cc != null) {
            errorTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override public void valueChanged(TreeSelectionEvent e) {
                    HintMetadata hm = getSelectedHint();

                    if (hm != null) {
                        cc.setSelectedId(AnalyzerImpl.ID_JAVA_HINTS_PREFIX + hm.id);
                    }
                }
            });
        }
    }

    public HintsPanel(Preferences configurations, ClassPathBasedHintWrapper cpBased, boolean direct) {
        this.cpBased = cpBased;
        this.queryStatus = QueryStatus.SHOW_QUERIES;
        this.showHeavyInspections = true;
        init(null, true, false, false, false, false, true, direct);
        setOverlayPreferences(HintsSettings.createPreferencesBasedHintsSettings(configurations, false, Severity.VERIFIER), direct);
        configurationsPanel.setVisible(false);
    }
    
    public void setOverlayPreferences(HintsSettings configurations, boolean direct) {
        if (logic != null)
            logic.setOverlayPreferences(configurations, direct);
    }

    public boolean hasNewHints() {
        return hasNewHints;
    }
    
    private OptionsFilter optionsFilter;
    
    private void setModel(DefaultTreeModel errorTreeModel) {
        if (optionsFilter!=null) {
            optionsFilter.installFilteringModel(errorTree, errorTreeModel, new AcceptorImpl());
        } else {
            errorTree.setModel(errorTreeModel);
        }
    }
    
    private void init(@NullAllowed OptionsFilter filter, boolean batchOnly, boolean filterSuggestions, boolean ignoreMissingFilter, boolean showSeverityCombo, boolean showOkCancel, boolean showCheckBoxes, boolean direct) {
        initComponents();
        // this init may be delayed, and the parent might already disable the entire panel, but the panel
        // is populated only now. Replicate the enabled state to the children.
        updateEnabledState();
        scriptScrollPane.setVisible(false);
        optionsFilter = null;
        if (!ignoreMissingFilter && filter==null) {
            optionsFilter = OptionsFilter.create(
                    searchTextField.getDocument(), new Runnable() {
                        @Override public void run() {}
                    });
        }
        configCombo.setModel(new ConfigurationsComboModel(true));
        configCombo.setRenderer(new ConfigurationRenderer());
//        if (useConfigCombo || inOptionsDialog) {
            configCombo.setSelectedItem(null);
//        }
        
        descriptionTextArea.setContentType("text/html"); // NOI18N
        descriptionTextArea.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
        
        scriptTextArea.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-javahints"));
        scriptTextArea.setEditable(true);
        

//        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
//            setOpaque( false );
        
        errorTree.setCellRenderer(showCheckBoxes? new CheckBoxRenderer() : new JLabelRenderer());
        errorTree.setRootVisible( false );
        errorTree.setShowsRootHandles( true );
        errorTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        
        errorTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleClick(e);
            }
            
            private void handleClick(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = e.getPoint();
                    TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                    if (path != null) {
                        DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (o.getUserObject() instanceof HintMetadata) {
                            HintMetadata hint = (HintMetadata) o.getUserObject();
                            if (hint.category.equals(Utilities.CUSTOM_CATEGORY)) {
                                JPopupMenu popup = new JPopupMenu();
                                popup.add(new JMenuItem(new RenameHint(o, hint, path)));
                                popup.add(new JMenuItem(new RemoveHint(o, hint)));
                                popup.show(errorTree, e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });
        
        errorTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE);
        errorTree.getActionMap().put(DELETE, new RemoveHint(null, null));

        toProblemCheckBox.setVisible(false);
        
        Collection<? extends HintMetadata> hints = !batchOnly?filterCustom(RulesManager.getInstance().readHints(null, null, null).keySet(), filterSuggestions):Utilities.getBatchSupportedHints(cpBased).keySet();

        errorTreeModel = constructTM(hints, !batchOnly && !filterSuggestions);

        if (filter != null) {
             filter.installFilteringModel(errorTree, errorTreeModel, new AcceptorImpl());
        } else {
            setModel(errorTreeModel);
        }

        initialized.set(true);
        update(direct);
        
        if (toSelect != null) {
            select(toSelect, true);
            
            toSelect = null;
        }
        
        boolean editEnabled = showOkCancel && FileUtil.getConfigFile(DECLARATIVE_HINT_TEMPLATE_LOCATION)!=null;
        newButton.setVisible(editEnabled);
        importButton.setVisible(false);
        exportButton.setVisible(false);
        editScriptButton.setVisible(editEnabled);
        editingButtons.setVisible(false);
        
        severityComboBox.setVisible(showSeverityCombo);
        severityLabel.setVisible(showSeverityCombo);
        okButton.setVisible(showOkCancel);
        cancelButton.setVisible(showOkCancel);
        validate();
    }
    
    private void updateEnabledState() {
        boolean enabled = isEnabled();
        enableDisableRecursively(this, enabled);
    }
    
    private void enableDisableRecursively(Component what, boolean enable) {
        what.setEnabled(enable);
        if (what instanceof Container) {
            for (Component c : ((Container) what).getComponents()) {
                enableDisableRecursively(c, enable);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorTree = new EditableJTree();
        detailsPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        severityLabel = new javax.swing.JLabel();
        severityComboBox = new javax.swing.JComboBox();
        toProblemCheckBox = new javax.swing.JCheckBox();
        customizerPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JEditorPane();
        descriptionLabel = new javax.swing.JLabel();
        editingButtons = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        cancelEdit = new javax.swing.JButton();
        openInEditor = new javax.swing.JButton();
        scriptScrollPane = new javax.swing.JScrollPane();
        scriptTextArea = new javax.swing.JEditorPane();
        buttonsPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        editScriptButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        configurationsPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        searchPanel = new javax.swing.JPanel();
        refactoringsLabel = new javax.swing.JLabel();
        searchLabel = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setDividerLocation(260);

        treePanel.setOpaque(false);
        treePanel.setLayout(new java.awt.BorderLayout());

        errorTree.setEditable(true);
        jScrollPane1.setViewportView(errorTree);
        errorTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleName")); // NOI18N
        errorTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleDescription")); // NOI18N

        treePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(treePanel);

        detailsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        detailsPanel.setOpaque(false);

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new java.awt.GridBagLayout());

        severityLabel.setLabelFor(severityComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        optionsPanel.add(severityLabel, gridBagConstraints);
        severityLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.severityLabel.AccessibleContext.accessibleDescription")); // NOI18N

        severityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        optionsPanel.add(severityComboBox, new java.awt.GridBagConstraints());
        severityComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AN_Show_As_Combo")); // NOI18N
        severityComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AD_Show_As_Combo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(toProblemCheckBox, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_InTasklist_CheckBox")); // NOI18N
        toProblemCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(toProblemCheckBox, gridBagConstraints);

        customizerPanel.setOpaque(false);
        customizerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(customizerPanel, gridBagConstraints);

        descriptionPanel.setOpaque(false);
        descriptionPanel.setLayout(new java.awt.GridBagLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setPreferredSize(new java.awt.Dimension(100, 50));
        descriptionScrollPane.setViewportView(descriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        descriptionPanel.add(descriptionScrollPane, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        descriptionPanel.add(descriptionLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelEdit, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.cancelEdit.text")); // NOI18N
        cancelEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelEditActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(openInEditor, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.openInEditor.text")); // NOI18N
        openInEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInEditorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editingButtonsLayout = new javax.swing.GroupLayout(editingButtons);
        editingButtons.setLayout(editingButtonsLayout);
        editingButtonsLayout.setHorizontalGroup(
            editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editingButtonsLayout.createSequentialGroup()
                .addComponent(openInEditor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 272, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelEdit))
        );
        editingButtonsLayout.setVerticalGroup(
            editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(openInEditor)
                .addComponent(cancelEdit)
                .addComponent(saveButton))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        descriptionPanel.add(editingButtons, gridBagConstraints);

        scriptTextArea.setEditable(false);
        scriptTextArea.setPreferredSize(new java.awt.Dimension(100, 50));
        scriptScrollPane.setViewportView(scriptTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        descriptionPanel.add(scriptScrollPane, gridBagConstraints);

        javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(descriptionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descriptionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(detailsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.newButton.text")); // NOI18N
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.importButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.exportButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editScriptButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.editScriptButton.text")); // NOI18N
        editScriptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editScriptButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addComponent(newButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 292, Short.MAX_VALUE)
                .addComponent(editScriptButton)
                .addGap(35, 35, 35)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton))
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newButton)
                    .addComponent(importButton)
                    .addComponent(exportButton)
                    .addComponent(editScriptButton)
                    .addComponent(cancelButton)
                    .addComponent(okButton)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(buttonsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.configLabel.text")); // NOI18N

        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout configurationsPanelLayout = new javax.swing.GroupLayout(configurationsPanel);
        configurationsPanel.setLayout(configurationsPanelLayout);
        configurationsPanelLayout.setHorizontalGroup(
            configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationsPanelLayout.createSequentialGroup()
                .addComponent(configLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configCombo, 0, 719, Short.MAX_VALUE))
        );
        configurationsPanelLayout.setVerticalGroup(
            configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(configLabel)
                .addComponent(configCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(configurationsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refactoringsLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.refactoringsLabel.text")); // NOI18N

        searchLabel.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.searchLabel.text")); // NOI18N

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addComponent(refactoringsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 576, Short.MAX_VALUE)
                .addComponent(searchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel)
                    .addComponent(refactoringsLabel)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(searchPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        applyChanges();
        confirmed = true;
        getRootPane().getParent().setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed
        
    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        if (configCombo.getSelectedItem() instanceof ActionListener) {
            ((ActionListener) configCombo.getSelectedItem()).actionPerformed(evt);
        }
        if (queryStatus == QueryStatus.ONLY_ENABLED) {
            errorTreeModel = constructTM(Utilities.getBatchSupportedHints(cpBased).keySet(), false);
            setModel(errorTreeModel);
            if (logic != null) {
                logic.errorTreeModel = errorTreeModel;
            }
        }
    }//GEN-LAST:event_configComboActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        try {
            FileObject tempFO = FileUtil.getConfigFile(DECLARATIVE_HINT_TEMPLATE_LOCATION); // NOI18N
            FileObject folderFO = FileUtil.getConfigFile("rules");
            if (folderFO == null) {
                folderFO = FileUtil.getConfigRoot().createFolder("rules");
            }
            DataFolder folder = (DataFolder) DataObject.find(folderFO);
            DataObject template = DataObject.find(tempFO);
            DataObject newIfcDO = template.createFromTemplate(folder, null);
            RulesManager.getInstance().reload();
            cpBased.reset();
            errorTreeModel = constructTM(Utilities.getBatchSupportedHints(cpBased).keySet(), false);
            setModel(errorTreeModel);
            logic.errorTreeModel = errorTreeModel;
            HintMetadata newHint = getHintByName(newIfcDO.getPrimaryFile().getNameExt());
            logic.writableSettings.setEnabled(newHint, true);
            select(newHint);
            hasNewHints = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
}//GEN-LAST:event_newButtonActionPerformed

    private String customHintCodeBeforeEditing;
    private boolean wasModified;
    
    private void editScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editScriptButtonActionPerformed
        descriptionScrollPane.setVisible(false);
        scriptScrollPane.setVisible(true);
        editScriptButton.setVisible(false);
        editingButtons.setVisible(true);
        optionsPanel.setVisible(false);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Script_Border"));        
        DataObject dob = getDataObject(getSelectedHint());
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        try {
            final StyledDocument doc = ec.openDocument();
            doc.render(new Runnable() {
                @Override public void run() {
                    try {
                        customHintCodeBeforeEditing = doc.getText(0, doc.getLength());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                        customHintCodeBeforeEditing = null;
                    }
                }
            });
            wasModified = DataObject.getRegistry().getModifiedSet().contains(dob);
            scriptTextArea.setDocument(doc);
            // Currently CloneableEditorSupport.getUndoRedo() UM is attached
            // but it would not be found by UndoAction (active TC's one would be used)
            // so assign a fresh new UM.
            UndoManager um = new UndoManager();
            doc.addUndoableEditListener(um); // Note: now two UMs listen on single doc
            doc.putProperty(UndoManager.class, um);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        newButton.setEnabled(false);
        searchTextField.setEnabled(false);
        configCombo.setEnabled(false);
        errorTree.setEnabled(false);
        okButton.setEnabled(false);
        validate();
}//GEN-LAST:event_editScriptButtonActionPerformed

    private void cancelEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelEditActionPerformed
        descriptionScrollPane.setVisible(true);
        scriptScrollPane.setVisible(false);

        optionsPanel.setVisible(true);
        editingButtons.setVisible(false);
        editScriptButton.setVisible(true);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border"));

        if (customHintCodeBeforeEditing != null) {
            DataObject dob = getDataObject(getSelectedHint());
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            try {
                final StyledDocument doc = ec.openDocument();
                NbDocument.runAtomic(doc, new Runnable() {
                    @Override public void run() {
                        try {
                            doc.remove(0, doc.getLength());
                            doc.insertString(0, customHintCodeBeforeEditing, null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                customHintCodeBeforeEditing = null;
                if (!wasModified) ec.saveDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        okButton.setEnabled(true);
        newButton.setEnabled(true);
        searchTextField.setEnabled(true);
        configCombo.setEnabled(true);
        errorTree.setEnabled(true);
        logic.valueChanged(null);
    }//GEN-LAST:event_cancelEditActionPerformed

    private void openInEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInEditorActionPerformed
        applyChanges();
        getRootPane().getParent().getParent().setVisible(false);
        DataObject dob = getDataObject(getSelectedHint());
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        ec.open();
    }//GEN-LAST:event_openInEditorActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final HintMetadata selectedHint = getSelectedHint();
        final String selectedHintId = selectedHint.id;
        DataObject dob = getDataObject(selectedHint);
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        try {
            ec.saveDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        RulesManager.getInstance().reload();
        cpBased.reset();
        errorTreeModel = constructTM(Utilities.getBatchSupportedHints(cpBased).keySet(), false);
        setModel(errorTreeModel);
        if (logic != null) {
            logic.errorTreeModel = errorTreeModel;
        }
        select(getHintByName(selectedHintId));
        customHintCodeBeforeEditing = null;
        cancelEditActionPerformed(evt);
        hasNewHints = true;
    }//GEN-LAST:event_saveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        getRootPane().getParent().setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private HintMetadata getHintByName(String name) {
        for (HintMetadata meta:Utilities.getBatchSupportedHints(cpBased).keySet()) {
            if (meta.id.startsWith(name)) {
                return meta;
            }
        }
        return null;
    }    
    synchronized void update(boolean direct) {
        if (!initialized.get()) return;
        HintsSettings overlay = null;
        if ( logic != null ) {
            logic.disconnect();
            overlay = logic.getOverlayPreferences();
        }
        logic = new HintsPanelLogic();
        logic.connect(errorTree, errorTreeModel, severityLabel, severityComboBox, toProblemCheckBox, customizerPanel, descriptionTextArea, configCombo, editScriptButton, overlay, direct);
    }
    
    synchronized void cancel() {
        if (logic == null || !initialized.get()) return;
        logic.disconnect();
        logic = null;
    }
    
    boolean isChanged() {
        return logic != null ? logic.isChanged() : false;
    }
    
    public void applyChanges() {
        if (!initialized.get()) return;
        logic.applyChanges();
        logic.disconnect();
        logic = null;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
    
    class JLabelRenderer implements TreeCellRenderer {
    
        private JLabel renderer = new JLabel();
        private DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            renderer.setBackground(selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor());
            renderer.setForeground(selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor());
            renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN));
            renderer.setOpaque(true);
            renderer.setEnabled(tree.isEnabled());

            Object data = ((DefaultMutableTreeNode) value).getUserObject();
            if (data instanceof HintCategory) {
                HintCategory cat = ((HintCategory) data);
                renderer.setText(cat.displayName);
            } else if (data instanceof HintMetadata) {
                HintMetadata treeRule = (HintMetadata) data;
                if (treeRule.options.contains(Options.QUERY)) {
                    renderer.setFont(renderer.getFont().deriveFont(Font.ITALIC));
                }
                renderer.setText(treeRule.displayName);

            } else {
                renderer.setText(value.toString());
            }

            return renderer;
        }
    }
    
    class CheckBoxRenderer implements TreeCellRenderer {
    
        private final TristateCheckBox renderer = new TristateCheckBox();
        private final DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            renderer.setBackground(selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor());
            renderer.setForeground(selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor());
            renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN));
            renderer.setEnabled(tree.isEnabled());

            Object data = ((DefaultMutableTreeNode) value).getUserObject();
            if (data instanceof HintCategory) {
                HintCategory cat = ((HintCategory) data);
                renderer.setText(cat.displayName);
                if (logic != null) {
                    renderer.setState(logic.isSelected((DefaultMutableTreeNode) value));
                }
            } else if (data instanceof HintMetadata) {
                HintMetadata treeRule = (HintMetadata) data;
                if (treeRule.options.contains(Options.QUERY)) {
                    renderer.setFont(renderer.getFont().deriveFont(Font.ITALIC));
                }
                renderer.setText(treeRule.displayName);

                if (logic != null) {
                    renderer.setSelected(logic.isEnabled(treeRule));
                }
            } else {
                renderer.setText(value.toString());
                if (value == extraNode && logic != null) {
                    renderer.setSelected(logic.getCurrentDependencyTracking() != DepScanningSettings.DependencyTracking.DISABLED);
                }
            }

            return renderer;
        }
    }

    enum State {
        SELECTED, NOT_SELECTED, OTHER;
    };

    private static class TristateCheckBox extends JCheckBox {

        private final TristateDecorator model;

        public TristateCheckBox() {
            super(null, null);
            model = new TristateDecorator(getModel());
            setModel(model);
            setState(State.OTHER);
        }

        /** No one may add mouse listeners, not even Swing! */
        @Override
        public void addMouseListener(MouseListener l) { }
        /**
         * Set the new state to either SELECTED, NOT_SELECTED or
         * OTHER.
         */
        public void setState(State state) { model.setState(state); }
        /** Return the current state, which is determined by the
         * selection status of the model. */
        public State getState() { return model.getState(); }
        @Override
        public void setSelected(boolean b) {
            if (b) {
                setState(State.SELECTED);
            } else {
                setState(State.NOT_SELECTED);
            }
        }
        /**
         * Exactly which Design Pattern is this?  Is it an Adapter,
         * a Proxy or a Decorator?  In this case, my vote lies with the
         * Decorator, because we are extending functionality and
         * "decorating" the original model with a more powerful model.
         */
        private class TristateDecorator implements ButtonModel {
            private final ButtonModel other;
            private TristateDecorator(ButtonModel other) {
                this.other = other;
            }
            private void setState(State state) {
                if (state == State.NOT_SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(false);
                } else if (state == State.SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(true);
                } else { // either "null" or OTHER
                    other.setArmed(true);
                    setPressed(true);
                    setSelected(true);
                }
            }
            /**
             * The current state is embedded in the selection / armed
             * state of the model.
             *
             * We return the SELECTED state when the checkbox is selected
             * but not armed, DONT_CARE state when the checkbox is
             * selected and armed (grey) and NOT_SELECTED when the
             * checkbox is deselected.
             */
            private State getState() {
                if (isSelected() && !isArmed()) {
                    // normal black tick
                    return State.SELECTED;
                } else if (isSelected() && isArmed()) {
                    // don't care grey tick
                    return State.OTHER;
                } else {
                    // normal deselected
                    return State.NOT_SELECTED;
                }
            }
            /** Filter: No one may change the armed status except us. */
            public void setArmed(boolean b) {
            }
            /** We disable focusing on the component when it is not
             * enabled. */
            public void setEnabled(boolean b) {
                setFocusable(b);
                other.setEnabled(b);
            }
            /** All these methods simply delegate to the "other" model
             * that is being decorated. */
            public boolean isArmed() { return other.isArmed(); }
            public boolean isSelected() { return other.isSelected(); }
            public boolean isEnabled() { return other.isEnabled(); }
            public boolean isPressed() { return other.isPressed(); }
            public boolean isRollover() { return other.isRollover(); }
            public void setSelected(boolean b) { other.setSelected(b); }
            public void setPressed(boolean b) { other.setPressed(b); }
            public void setRollover(boolean b) { other.setRollover(b); }
            public void setMnemonic(int key) { other.setMnemonic(key); }
            public int getMnemonic() { return other.getMnemonic(); }
            public void setActionCommand(String s) {
                other.setActionCommand(s);
            }
            public String getActionCommand() {
                return other.getActionCommand();
            }
            public void setGroup(ButtonGroup group) {
                other.setGroup(group);
            }
            public void addActionListener(ActionListener l) {
                other.addActionListener(l);
            }
            public void removeActionListener(ActionListener l) {
                other.removeActionListener(l);
            }
            public void addItemListener(ItemListener l) {
                other.addItemListener(l);
            }
            public void removeItemListener(ItemListener l) {
                other.removeItemListener(l);
            }
            public void addChangeListener(ChangeListener l) {
                other.addChangeListener(l);
            }
            public void removeChangeListener(ChangeListener l) {
                other.removeChangeListener(l);
            }
            public Object[] getSelectedObjects() {
                return other.getSelectedObjects();
            }
        }
    }

    // Variables declaration - do not modify                     
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton cancelEdit;
    private javax.swing.JComboBox configCombo;
    private javax.swing.JLabel configLabel;
    private javax.swing.JPanel configurationsPanel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JEditorPane descriptionTextArea;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JButton editScriptButton;
    private javax.swing.JPanel editingButtons;
    private javax.swing.JTree errorTree;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton newButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openInEditor;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel refactoringsLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JScrollPane scriptScrollPane;
    private javax.swing.JEditorPane scriptTextArea;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JComboBox severityComboBox;
    private javax.swing.JLabel severityLabel;
    private javax.swing.JCheckBox toProblemCheckBox;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private final Map<HintMetadata, TreePath> hint2Path =  new HashMap<HintMetadata, TreePath>();

    private DefaultTreeModel constructTM(Collection<? extends HintMetadata> metadata, boolean allHints) {
        HintCategory rootCategory = new HintCategory("");

        for (HintMetadata m : metadata) {
            if (m.options.contains(Options.NON_GUI)) continue;
            if (m.options.contains(Options.HEAVY)) {
                if (!showHeavyInspections) {
                    continue;
                }
            }
            if (   m.options.contains(Options.QUERY)
                && !Utilities.CUSTOM_CATEGORY.equals(m.category)) {
                if (queryStatus == QueryStatus.NEVER) {
                    continue;
                }
                if (queryStatus == QueryStatus.ONLY_ENABLED && logic != null && !logic.isEnabled(m)) {
                    continue;
                }
            } 

            HintCategory curr = rootCategory;
            int lastIndex = -1;
            boolean stop = false;
            OUTER: do {
                int newIndex = m.category.indexOf('/', lastIndex + 1);

                if (newIndex == (-1)) {
                    newIndex = m.category.length();
                    stop = true;
                }
                
                lastIndex = newIndex;
                
                String currentCategory = m.category.substring(0, newIndex);
                
                for (HintCategory hc : curr.subCategories) {
                    if (currentCategory.equals(hc.codeName)) {
                        curr = hc;
                        continue OUTER;
                    }
                }
                
                curr.subCategories.add(curr = new HintCategory(currentCategory));
            } while (!stop);
            
            curr.hints.add(m);
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<HintCategory, TreePath> category2Node = new IdentityHashMap<>();
        
        category2Node.put(rootCategory, new TreePath(root));
        
        List<HintCategory> hints = new LinkedList<>();
        
        hints.add(rootCategory);
        
        while (!hints.isEmpty()) {
            HintCategory cat = hints.remove(0);
            TreePath currentPath = category2Node.get(cat);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentPath.getLastPathComponent();
            
            cat.subCategories.sort(new Comparator<HintCategory>() {
                @Override public int compare(HintCategory o1, HintCategory o2) {
                    return HintsPanel.compare(o1.displayName, o2.displayName);
                }
            });
            
            for (HintCategory sub : cat.subCategories) {
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(sub);
                
                category2Node.put(sub, currentPath.pathByAddingChild(subNode));
                node.add(subNode);
            }
            
            hints.addAll(cat.subCategories);
            
            cat.hints.sort(new Comparator<HintMetadata>() {
                @Override public int compare(HintMetadata o1, HintMetadata o2) {
                    return o1.displayName.compareTo(o2.displayName);
                }
            });
            
            for (HintMetadata hm : cat.hints) {
                DefaultMutableTreeNode hintNode = new DefaultMutableTreeNode(hm);
                node.add(hintNode);
                hint2Path.put(hm, currentPath.pathByAddingChild(hintNode));
            }
        }

        if (allHints)
            root.add(extraNode);
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root) {

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (o.getUserObject() instanceof HintMetadata) {
                    try {
                        HintMetadata hint = (HintMetadata) o.getUserObject();
                        getDataObject(hint).rename((String) newValue);
                        RulesManager.getInstance().reload();
                        cpBased.reset();
                        errorTreeModel = constructTM(Utilities.getBatchSupportedHints(cpBased).keySet(), false);
                        setModel(errorTreeModel);
                        if (logic != null) {
                            logic.errorTreeModel = errorTreeModel;
                        }
                        select(getHintByName((String) newValue));
                        hasNewHints = true;
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(errorTree, NbBundle.getMessage(HintsPanel.class, "ERR_CannotRename", newValue));
                        errorTree.startEditingAtPath(path);
                    }
                }
            }
        };

        return defaultTreeModel;
    }

    public void select(HintMetadata hm) {
        select(hm, false);
    }
    
    public void select(HintMetadata hm, boolean setFocus) {
        if (errorTree == null) {
            //lazy init:
            toSelect = hm;
            return;
        }
        
	TreePath path = hint2Path.get(hm);

        if (path == null) return ;
	
        errorTree.setSelectionPath(path);
	errorTree.scrollPathToVisible(path);
        if (setFocus)
            errorTree.requestFocusInWindow();
    }

    private static int compare(String s1, String s2) {
        return clearNonAlpha(s1).compareToIgnoreCase(clearNonAlpha(s2));
    }

    private static String clearNonAlpha(String str) {
        StringBuilder sb = new StringBuilder(str.length());

        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private DataObject getDataObject(HintMetadata selectedHint) {
        String fileName = selectedHint.id.indexOf('-') != (-1) ? selectedHint.id.substring(0,selectedHint.id.lastIndexOf('-')) : selectedHint.id; //XXX
        FileObject fo = FileUtil.getConfigFile("rules/" + fileName);
        try {
            return fo == null ? null : DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private Collection<? extends HintMetadata> filterCustom(Set<HintMetadata> keySet, boolean filterSuggestions) {
        ArrayList<HintMetadata> list = new ArrayList<HintMetadata>();
        for (HintMetadata hint:keySet) {
            if (hint.kind == Kind.ACTION && filterSuggestions) continue;
            list.add(hint);
        }
        return list;
    }

    private final class AcceptorImpl implements Acceptor {

        public boolean accept(Object originalTreeNode, String filterText) {
            if (filterText.isEmpty()) return true;
            
            expandTask.schedule(100);
            
            if (originalTreeNode == extraNode) {
                for (String key : EXTRA_NODE_KEYWORDS) {
                    if (NbBundle.getMessage(HintsPanel.class, key).toLowerCase().contains(filterText)) {
                        return true;
                    }
                }
                
                return false;
            }
            
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) originalTreeNode;
            Object uo = n.getUserObject();
            
            if (!(uo instanceof HintMetadata)) return false;
            
            HintMetadata hm = (HintMetadata) uo;

            filterText = filterText.toLowerCase();

            if (hm.displayName.toLowerCase().contains(filterText)) {
                return true;
            }

            if (hm.description.toLowerCase().contains(filterText)) {
                return true;
            }

            for (String sw : hm.suppressWarnings) {
                if (sw.toLowerCase().contains(filterText)) {
                    return true;
                }
            }

            return false;
        }
    }

    public Configuration getSelectedConfiguration() {
        return (Configuration) configCombo.getSelectedItem();
    }
    
    public HintMetadata getSelectedHint() {
        TreePath selectionPath = errorTree.getSelectionModel().getSelectionPath();
        if (selectionPath==null) {
            return null;
}
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) (MutableTreeNode) (TreeNode) selectionPath.getLastPathComponent();
        if (lastPathComponent!= null && lastPathComponent.getUserObject() instanceof HintMetadata)
            return (HintMetadata) lastPathComponent.getUserObject();
        return null;
    }
    
    private class RemoveHint extends AbstractAction {

        HintMetadata hint;
        DefaultMutableTreeNode node;

        public RemoveHint(DefaultMutableTreeNode node, HintMetadata hint) {
            super(NbBundle.getMessage(RemoveHint.class, "CTL_Delete"));
            this.hint = hint;
            this.node = node;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            HintMetadata hint = this.hint;
            try {
                if (hint==null) {
                    hint = getSelectedHint();
                }
                if (hint == null) {
                    return;
                }
                DataObject d = getDataObject(hint);
                if (d == null) {
                    return;
                }
                if (JOptionPane.YES_OPTION == 
                        JOptionPane.showConfirmDialog(errorTree,
                        NbBundle.getMessage(HintsPanel.class, "MSG_DeleteConfirmMessage",  hint.displayName ),
                        NbBundle.getMessage(HintsPanel.class, "MSG_DeleteConfirmTitle"),
                        JOptionPane.YES_NO_OPTION)) {
                    d.delete();
                    RulesManager.getInstance().reload();
                    cpBased.reset();
                    //errorTreeModel.removeNodeFromParent(node);
                    errorTreeModel = constructTM(Utilities.getBatchSupportedHints(cpBased).keySet(), false);
                    setModel(errorTreeModel);
                    if (logic != null) {
                        logic.errorTreeModel = errorTreeModel;
                    }
                    hasNewHints = true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }
    
    private class RenameHint extends AbstractAction {

        HintMetadata hint;
        DefaultMutableTreeNode node;
        TreePath path;

        public RenameHint(DefaultMutableTreeNode node, HintMetadata hint, TreePath path) {
            super(NbBundle.getMessage(RemoveHint.class, "CTL_Rename"));
            this.hint = hint;
            this.node = node;
            this.path = path;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            errorTree.startEditingAtPath(path);
        }
    }
 
    private static class EditableJTree extends JTree {

        public EditableJTree() {
        }

        @Override
        public boolean isPathEditable(TreePath path) {

            DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (o.getUserObject() instanceof HintMetadata) {
                HintMetadata hint = (HintMetadata) o.getUserObject();
                if (hint.category.equals(Utilities.CUSTOM_CATEGORY)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private enum QueryStatus {
        SHOW_QUERIES,
        ONLY_ENABLED,
        NEVER;
    }
}
