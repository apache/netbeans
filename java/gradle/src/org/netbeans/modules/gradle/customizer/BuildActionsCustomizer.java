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

package org.netbeans.modules.gradle.customizer;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.ProjectActionMappingProvider;
import org.netbeans.modules.gradle.execute.GradleCliEditorKit;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
@Messages("TXT_CUSTOM=Custom...")
public class BuildActionsCustomizer extends javax.swing.JPanel {

    private static final String NB_ACTIONS = "nb-actions.xml"; //NOI18N

    private final static String CUSTOM_ACTION = Bundle.TXT_CUSTOM();
    private static final String CARD_NOSELECT = "empty"; //NOI18N
    private static final String CARD_DETAILS = "details"; //NOI18N

    final Project project;

    final Set<String> customNames = new HashSet<>();
    final DefaultListModel<CustomActionMapping> customActionsModel = new DefaultListModel<>();
    final DefaultComboBoxModel<String> availableActionsModel = new DefaultComboBoxModel<>();
    final ActionListener saveListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            save();
        }
    };

    final DocumentListener applyListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            apply();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            apply();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            apply();
        }
    };

    private volatile boolean autoApply;
    private boolean comboReady;

    /**
     * Creates new form BuildActionsCustomizer
     */
    public BuildActionsCustomizer(Project project) {
        this.project = project;
        initComponents();
        lsActions.setCellRenderer(new MyListCellRenderer());
        tfLabel.getDocument().addDocumentListener(applyListener);
        EditorKit kit = CloneableEditorSupport.getEditorKit(GradleCliEditorKit.MIME_TYPE);
        taArgs.setEditorKit(kit);
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            taArgs.getDocument().putProperty(Document.StreamDescriptionProperty, gbp);
        }
        taArgs.getDocument().addDocumentListener(applyListener);
        initDefaultModels();
        comboReady = true;
    }

    private void initDefaultModels() {
        ProjectActionMappingProvider mappingProvider = project.getLookup().lookup(ProjectActionMappingProvider.class);
        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);

        Set<String> allAvailableActions = new TreeSet<>(Arrays.asList(actionProvider.getSupportedActions()));

        Set<String> customizedActions = mappingProvider.customizedActions();
        for (String action : mappingProvider.customizedActions()) {
            CustomActionMapping mapping = new CustomActionMapping(mappingProvider.findMapping(action));
            customActionsModel.addElement(mapping);
            if (action.startsWith(ActionMapping.CUSTOM_PREFIX)) {
                customNames.add(action);
            }
        }
        availableActionsModel.addElement(CUSTOM_ACTION);

        // Add those actions to the combo box which were not customized yet.
        for (String action : allAvailableActions) {
            if (!customizedActions.contains(action)) {
                availableActionsModel.addElement(action);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbActions = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lsActions = new javax.swing.JList<>();
        pnDetailsPanel = new javax.swing.JPanel();
        lbNoAction = new javax.swing.JLabel();
        pnDetails = new javax.swing.JPanel();
        lbName = new javax.swing.JLabel();
        lbLabel = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        tfLabel = new javax.swing.JTextField();
        lbArgs = new javax.swing.JLabel();
        btRemove = new javax.swing.JButton();
        lbReloadRule = new javax.swing.JLabel();
        cbReloadRule = new javax.swing.JComboBox<>();
        cbRepeatable = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        taArgs = new javax.swing.JEditorPane();
        cbAdd = new javax.swing.JComboBox<>();
        lbTitle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lbActions, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbActions.text")); // NOI18N

        lsActions.setModel(customActionsModel);
        lsActions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lsActions.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lsActionsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lsActions);

        pnDetailsPanel.setLayout(new java.awt.CardLayout());

        lbNoAction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lbNoAction, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbNoAction.text")); // NOI18N
        pnDetailsPanel.add(lbNoAction, "empty");

        pnDetails.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(lbName, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbLabel, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbLabel.text")); // NOI18N

        tfName.setEditable(false);
        tfName.setEnabled(false);

        tfLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lbArgs, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbArgs.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btRemove, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.btRemove.text")); // NOI18N
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lbReloadRule, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbReloadRule.text")); // NOI18N

        cbReloadRule.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEVER", "DEFAULT", "ALWAYS", "ALWAYS_ONLINE" }));
        cbReloadRule.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbReloadRuleItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRepeatable, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.cbRepeatable.text")); // NOI18N
        cbRepeatable.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbRepeatableStateChanged(evt);
            }
        });

        taArgs.setContentType("text/x-gradle-cli"); // NOI18N
        jScrollPane3.setViewportView(taArgs);

        javax.swing.GroupLayout pnDetailsLayout = new javax.swing.GroupLayout(pnDetails);
        pnDetails.setLayout(pnDetailsLayout);
        pnDetailsLayout.setHorizontalGroup(
            pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnDetailsLayout.createSequentialGroup()
                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnDetailsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btRemove))
                    .addGroup(pnDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnDetailsLayout.createSequentialGroup()
                                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbName)
                                    .addComponent(lbLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfLabel)
                                    .addComponent(tfName)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnDetailsLayout.createSequentialGroup()
                                .addComponent(lbArgs)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnDetailsLayout.createSequentialGroup()
                                .addComponent(lbReloadRule)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbReloadRule, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbRepeatable, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                            .addGroup(pnDetailsLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jScrollPane3)))))
                .addContainerGap())
        );
        pnDetailsLayout.setVerticalGroup(
            pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbName)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLabel)
                    .addComponent(tfLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbArgs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbReloadRule)
                    .addComponent(cbReloadRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbRepeatable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                .addComponent(btRemove)
                .addContainerGap())
        );

        pnDetailsPanel.add(pnDetails, "details");

        cbAdd.setModel(availableActionsModel);
        cbAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lbTitle, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.lbTitle.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BuildActionsCustomizer.class, "BuildActionsCustomizer.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbActions)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pnDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cbAdd, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbTitle)
                .addGap(10, 10, 10)
                .addComponent(lbActions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lsActionsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lsActionsValueChanged
        CardLayout cardLayout = (CardLayout) pnDetailsPanel.getLayout();
        ActionMapping mapping = getSelectedMapping();
        autoApply = false;
        if (mapping != null) {
            cardLayout.show(pnDetailsPanel, CARD_DETAILS);

            tfName.setText(mapping.getName());
            tfLabel.setText(mapping.getDisplayName());
            tfLabel.setEnabled(mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX));
            taArgs.setText(mapping.getArgs());
            cbReloadRule.setSelectedItem(mapping.getReloadRule().name());
            cbRepeatable.setSelected(mapping.isRepeatable());
            autoApply = true;
        } else {
            cardLayout.show(pnDetailsPanel, CARD_NOSELECT);
        }
    }//GEN-LAST:event_lsActionsValueChanged

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        CustomActionMapping mapping = getSelectedMapping();
        customActionsModel.removeElement(mapping);
        String action = mapping.getName();
        if (!action.startsWith(ActionMapping.CUSTOM_PREFIX)) {
            availableActionsModel.addElement(action);
        } else {
            customNames.remove(action);
        }

    }//GEN-LAST:event_btRemoveActionPerformed

    private void apply() {
        if (autoApply) {
            CustomActionMapping mapping = getSelectedMapping();
            if (mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX)) {
                mapping.setDisplayName(tfLabel.getText());
            }
            mapping.setArgs(taArgs.getText());
            mapping.setReloadRule(ActionMapping.ReloadRule.valueOf(cbReloadRule.getSelectedItem().toString()));
            mapping.setRepeatable(cbRepeatable.isSelected());
            lsActions.repaint();
        }
    }

    private void cbRepeatableStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbRepeatableStateChanged
        apply();
    }//GEN-LAST:event_cbRepeatableStateChanged

    private void cbReloadRuleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbReloadRuleItemStateChanged
        apply();
    }//GEN-LAST:event_cbReloadRuleItemStateChanged

    private void cbAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAddActionPerformed
        if (!comboReady) return;
        String action = availableActionsModel.getElementAt(cbAdd.getSelectedIndex());
        if (action != CUSTOM_ACTION) {
            availableActionsModel.removeElement(action);
        } else {
            action = findNewCustonActionId();
        }
        ProjectActionMappingProvider mappingProvider = project.getLookup().lookup(ProjectActionMappingProvider.class);
        ActionMapping defaultMapping = mappingProvider.findMapping(action);
        CustomActionMapping mapping = defaultMapping != null ? new CustomActionMapping(defaultMapping) : new CustomActionMapping(action);
        customActionsModel.addElement(mapping);
        lsActions.setSelectedIndex(customActionsModel.indexOf(mapping));
        cbAdd.setSelectedIndex(0);
    }//GEN-LAST:event_cbAddActionPerformed

    public ActionListener getSaveListener() {
        return saveListener;
    }

    private CustomActionMapping getSelectedMapping() {
        int index = lsActions.getSelectedIndex();
        return index >= 0 ? customActionsModel.elementAt(index) : null;
    }

    private String findNewCustonActionId() {
        int i = 1;
        String ret;
        do {
            ret = ActionMapping.CUSTOM_PREFIX + i++;
        } while (customNames.contains(ret));
        customNames.add(ret);
        return ret;
    }

    private void save() {
        Set<CustomActionMapping> mappings = new TreeSet<>();
        Enumeration<CustomActionMapping> elements = customActionsModel.elements();
        while (elements.hasMoreElements()) {
            mappings.add(elements.nextElement());
        }
        try {
            FileObject fo = project.getProjectDirectory().getFileObject(NB_ACTIONS);
            fo = fo != null ? fo : project.getProjectDirectory().createData(NB_ACTIONS);
            try (PrintWriter out = new PrintWriter(fo.getOutputStream(), true)) {
                out.println("<?xml version=\"1.0\"?>");
                out.println("<!DOCTYPE actions SYSTEM \"action-mapping.dtd\">");
                out.println("<actions>");
                for (CustomActionMapping mapping : mappings) {
                    out.print("    <action name=\"" + mapping.getName() + "\"");
                    if (mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX)) {
                        out.print(" displayName=\"" + mapping.getDisplayName() + "\"");
                    }
                    if (!mapping.isRepeatable()) {
                        out.print("repeatable=\"false\"");
                    }
                    out.println(">");

                    out.println("        <args>" + mapping.getArgs() + "</args>");
                    if (mapping.getReloadRule() != ActionMapping.ReloadRule.DEFAULT) {
                        out.println("        <reload rule=\"" + mapping.getReloadRule().name() + "\"/>");
                    }
                    out.println("    </action>");
                }
                out.println("</actions>");
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (IOException ex) {

        }
    }

    static class MyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CustomActionMapping) {
                CustomActionMapping mapping = (CustomActionMapping) value;
                String displayName = mapping.getDisplayName();
                if (mapping.getArgs().contains("${input:")) {
                    displayName = displayName + "...";
                }
                label.setText(mapping.isChanged() ? "<html><b>" + displayName + "</b>" : displayName);
            }
            return label;
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btRemove;
    private javax.swing.JComboBox<String> cbAdd;
    private javax.swing.JComboBox<String> cbReloadRule;
    private javax.swing.JCheckBox cbRepeatable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lbActions;
    private javax.swing.JLabel lbArgs;
    private javax.swing.JLabel lbLabel;
    private javax.swing.JLabel lbName;
    private javax.swing.JLabel lbNoAction;
    private javax.swing.JLabel lbReloadRule;
    private javax.swing.JLabel lbTitle;
    private javax.swing.JList<CustomActionMapping> lsActions;
    private javax.swing.JPanel pnDetails;
    private javax.swing.JPanel pnDetailsPanel;
    private javax.swing.JEditorPane taArgs;
    private javax.swing.JTextField tfLabel;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
