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
package org.netbeans.modules.javascript.nodejs.ui.customizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.ui.NodeJsPathPanel;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

final class NodeJsCustomizerPanel extends JPanel implements HelpCtx.Provider {

    private final ProjectCustomizer.Category category;
    private final NodeJsPreferences preferences;
    final NodeJsPathPanel nodeJsPathPanel;
    final List<String> debuggerProtocols = new ArrayList<>();

    volatile boolean enabled;
    volatile boolean defaultNode;
    volatile String node;
    volatile boolean syncChanges;


    public NodeJsCustomizerPanel(ProjectCustomizer.Category category, Project project) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert project != null;

        this.category = category;
        preferences = NodeJsSupport.forProject(project).getPreferences();
        nodeJsPathPanel = new NodeJsPathPanel();

        initComponents();
        init();
    }

    private void init() {
        nodePathPanel.add(nodeJsPathPanel, BorderLayout.CENTER);
        // init
        enabled = preferences.isEnabled();
        enabledCheckBox.setSelected(enabled);
        node = preferences.getNode();
        nodeJsPathPanel.setNode(node);
        nodeJsPathPanel.setNodeSources(preferences.getNodeSources());
        nodeJsPathPanel.setDebugProtocol(preferences.getDebugProtocol());
        defaultNode = preferences.isDefaultNode();
        if (defaultNode) {
            defaultNodeRadioButton.setSelected(true);
        } else {
            customNodeRadioButton.setSelected(true);
        }
        syncChanges = preferences.isSyncEnabled();
        syncCheckBox.setSelected(syncChanges);
        // ui
        enableAllFields();
        validateData();
        // listeners
        ItemListener defaultItemListener = new DefaultItemListener();
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        enabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enabled = e.getStateChange() == ItemEvent.SELECTED;
                validateData();
                enableAllFields();
            }
        });
        defaultNodeRadioButton.addItemListener(defaultItemListener);
        customNodeRadioButton.addItemListener(defaultItemListener);
        nodeJsPathPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                node = nodeJsPathPanel.getNode();
                validateData();
            }
        });
        syncCheckBox.addItemListener(defaultItemListener);
    }

    void enableAllFields() {
        // default
        defaultNodeRadioButton.setEnabled(enabled);
        configureNodeButton.setEnabled(enabled && defaultNode);
        // custom
        customNodeRadioButton.setEnabled(enabled);
        nodeJsPathPanel.enablePanel(enabled && !defaultNode);
        // sync
        syncCheckBox.setEnabled(enabled);
    }

    void validateData() {
        ValidationResult result = new NodeJsPreferencesValidator()
                .validateCustomizer(enabled, defaultNode, node, nodeJsPathPanel.getNodeSources())
                .getResult();
        if (result.hasErrors()) {
            category.setErrorMessage(result.getFirstErrorMessage());
            category.setValid(false);
            return;
        }
        if (result.hasWarnings()) {
            category.setErrorMessage(result.getFirstWarningMessage());
            category.setValid(true);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    void saveData() {
        preferences.setEnabled(enabled);
        preferences.setNode(node);
        preferences.setNodeSources(nodeJsPathPanel.getNodeSources());
        preferences.setDefaultNode(defaultNode);
        preferences.setSyncEnabled(syncChanges);
        preferences.setDebugProtocol(nodeJsPathPanel.getDebugProtocol());
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerPanel"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodeBbuttonGroup = new ButtonGroup();
        enabledCheckBox = new JCheckBox();
        configureNodeButton = new JButton();
        defaultNodeRadioButton = new JRadioButton();
        customNodeRadioButton = new JRadioButton();
        nodePathPanel = new JPanel();
        syncCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.enabledCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(configureNodeButton, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.configureNodeButton.text")); // NOI18N
        configureNodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureNodeButtonActionPerformed(evt);
            }
        });

        nodeBbuttonGroup.add(defaultNodeRadioButton);
        Mnemonics.setLocalizedText(defaultNodeRadioButton, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.defaultNodeRadioButton.text")); // NOI18N

        nodeBbuttonGroup.add(customNodeRadioButton);
        Mnemonics.setLocalizedText(customNodeRadioButton, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.customNodeRadioButton.text")); // NOI18N

        nodePathPanel.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(syncCheckBox, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.syncCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(nodePathPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(defaultNodeRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(configureNodeButton))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(enabledCheckBox)
                    .addComponent(customNodeRadioButton)
                    .addComponent(syncCheckBox))
                .addContainerGap(141, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultNodeRadioButton)
                    .addComponent(configureNodeButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customNodeRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodePathPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(syncCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureNodeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configureNodeButtonActionPerformed
        assert EventQueue.isDispatchThread();
        OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
    }//GEN-LAST:event_configureNodeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configureNodeButton;
    private JRadioButton customNodeRadioButton;
    private JRadioButton defaultNodeRadioButton;
    private JCheckBox enabledCheckBox;
    private ButtonGroup nodeBbuttonGroup;
    private JPanel nodePathPanel;
    private JCheckBox syncCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            defaultNode = defaultNodeRadioButton.isSelected();
            syncChanges = syncCheckBox.isSelected();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                enableAllFields();
                validateData();
            }
        }

    }

}
