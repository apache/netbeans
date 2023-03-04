/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.lang.model.SourceVersion;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CustomizerCompile extends JPanel implements HelpCtx.Provider {

    public CustomizerCompile( J2SEProjectProperties uiProperties ) {
        initComponents();

        uiProperties.COMPILE_ON_SAVE_MODEL.setMnemonic(compileOnSave.getMnemonic());
        compileOnSave.setModel(new UnselectedWhenDisabledButtonModel(uiProperties.COMPILE_ON_SAVE_MODEL,
                                                                     J2SEProjectUtil.isCompileOnSaveSupported(uiProperties.getProject())));
        
        uiProperties.JAVAC_DEPRECATION_MODEL.setMnemonic( deprecationCheckBox.getMnemonic() );
        deprecationCheckBox.setModel( uiProperties.JAVAC_DEPRECATION_MODEL );

        uiProperties.JAVAC_DEBUG_MODEL.setMnemonic( debugInfoCheckBox.getMnemonic() );
        debugInfoCheckBox.setModel( uiProperties.JAVAC_DEBUG_MODEL );

        uiProperties.DO_DEPEND_MODEL.setMnemonic(doDependCheckBox.getMnemonic());
        doDependCheckBox.setModel(uiProperties.DO_DEPEND_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL.setMnemonic(enableAPTCheckBox.getMnemonic());
        enableAPTCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_MODEL);

        uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL.setMnemonic(enableAPTEditorCheckBox.getMnemonic());
        enableAPTEditorCheckBox.setModel(uiProperties.ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL);

        annotationProcessorsList.setModel(uiProperties.ANNOTATION_PROCESSORS_MODEL);
        
        processorOptionsTable.setModel(uiProperties.PROCESSOR_OPTIONS_MODEL);

        fork.setModel(uiProperties.JAVAC_EXTERNAL_VM_MODEL);

        additionalJavacParamsField.setDocument( uiProperties.JAVAC_COMPILER_ARG_MODEL );

        uiProperties.PLATFORM_MODEL.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                enableJavacFork((ComboBoxModel<?>)e.getSource());
            }
        });

        fork.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                showDefaultPlatformWarning(!fork.isSelected());
            }
        });

        enableAPTCheckBoxActionPerformed(null);
        enableJavacFork(uiProperties.PLATFORM_MODEL);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerCompile.class );
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        compileOnSave = new javax.swing.JCheckBox();
        compileOnSaveDescription = new javax.swing.JLabel();
        debugInfoCheckBox = new javax.swing.JCheckBox();
        deprecationCheckBox = new javax.swing.JCheckBox();
        doDependCheckBox = new javax.swing.JCheckBox();
        enableAPTCheckBox = new javax.swing.JCheckBox();
        enableAPTEditorCheckBox = new javax.swing.JCheckBox();
        annotationProcessorsLabel = new javax.swing.JLabel();
        AnnotationProcessorsScrollPane = new javax.swing.JScrollPane();
        annotationProcessorsList = new javax.swing.JList();
        addProcessorButton = new javax.swing.JButton();
        removeProcessorButton = new javax.swing.JButton();
        processorOptionsLabel = new javax.swing.JLabel();
        processorOptionsScrollPane = new javax.swing.JScrollPane();
        processorOptionsTable = new javax.swing.JTable();
        addOptionButton = new javax.swing.JButton();
        removeOptionButton = new javax.swing.JButton();
        additionalJavacParamsLabel = new javax.swing.JLabel();
        additionalJavacParamsField = new javax.swing.JTextField();
        additionalJavacParamsExample = new javax.swing.JLabel();
        fork = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(compileOnSave, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("CustomizerCompile.CompileOnSave")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(compileOnSaveDescription, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("LBL_CompileOnSaveDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debugInfoCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Compiler_DebugInfo_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deprecationCheckBox, org.openide.util.NbBundle.getBundle(CustomizerCompile.class).getString("LBL_CustomizeCompile_Compiler_Deprecation_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doDependCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "CustomizerCompile.doDependCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAPTCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(enableAPTEditorCheckBox, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N

        annotationProcessorsLabel.setLabelFor(annotationProcessorsList);
        org.openide.awt.Mnemonics.setLocalizedText(annotationProcessorsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Annotation_Processors")); // NOI18N

        annotationProcessorsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        annotationProcessorsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                annotationProcessorsListValueChanged(evt);
            }
        });
        AnnotationProcessorsScrollPane.setViewportView(annotationProcessorsList);

        org.openide.awt.Mnemonics.setLocalizedText(addProcessorButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Add_Annotation_Processor")); // NOI18N
        addProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProcessorButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeProcessorButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Remove_Annotation_Processors")); // NOI18N
        removeProcessorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProcessorButtonActionPerformed(evt);
            }
        });

        processorOptionsLabel.setLabelFor(processorOptionsTable);
        org.openide.awt.Mnemonics.setLocalizedText(processorOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Processor_Options")); // NOI18N

        processorOptionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        processorOptionsTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        processorOptionsTable.getTableHeader().setReorderingAllowed(false);
        processorOptionsScrollPane.setViewportView(processorOptionsTable);
        processorOptionsTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                processorOptionsListSelectionChanged(evt);
            }
        });
        processorOptionsTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "TBL_ACSN_AnnotationProcesserOptions")); // NOI18N
        processorOptionsTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "TBL_ACSD_AnnotationProcesserOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addOptionButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Add_Annotation_ProcessorOption")); // NOI18N
        addOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOptionButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeOptionButton, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Remove_Processor_Option")); // NOI18N
        removeOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeOptionButtonActionPerformed(evt);
            }
        });

        additionalJavacParamsLabel.setLabelFor(additionalJavacParamsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsLabel, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_AdditionalCompilerOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(additionalJavacParamsExample, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_AdditionalCompilerOptionsExample")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fork, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "TXT_RunInSeparateVM")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(compileOnSave, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(debugInfoCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deprecationCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(doDependCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enableAPTCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(annotationProcessorsLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableAPTEditorCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(processorOptionsLabel)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(processorOptionsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                                        .addComponent(AnnotationProcessorsScrollPane))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(removeOptionButton)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(addOptionButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(addProcessorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(removeProcessorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))))
                .addGap(12, 12, 12))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(compileOnSaveDescription)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(fork)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(additionalJavacParamsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(additionalJavacParamsField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(additionalJavacParamsExample)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(compileOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compileOnSaveDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debugInfoCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deprecationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doDependCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableAPTCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableAPTEditorCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(annotationProcessorsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addProcessorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeProcessorButton))
                    .addComponent(AnnotationProcessorsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processorOptionsLabel)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processorOptionsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addOptionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeOptionButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fork)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(additionalJavacParamsLabel)
                    .addComponent(additionalJavacParamsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalJavacParamsExample))
        );

        compileOnSave.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizerCompile.CompileOnSave")); // NOI18N
        debugInfoCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustomizerCompile_jCheckBoxDebugInfo")); // NOI18N
        deprecationCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustomizerCompile_jCheckBoxDeprecation")); // NOI18N
        doDependCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACSD_doDependCheckBox")); // NOI18N
        enableAPTCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Enable_Annotation_Processing")); // NOI18N
        enableAPTEditorCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Enable_Editor_Annotation_Processing")); // NOI18N
        annotationProcessorsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "AD_CustomizeCompile_Annotation_Processors")); // NOI18N
        addProcessorButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSN_AddProcessor")); // NOI18N
        addProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSD_AddProcessor")); // NOI18N
        removeProcessorButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSN_RemoveProcessor")); // NOI18N
        removeProcessorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSD_RemoveProcessor")); // NOI18N
        addOptionButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSN_AddProcessorOption")); // NOI18N
        addOptionButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSD_AddProcessorOption")); // NOI18N
        removeOptionButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSD_RemoveProcessorOption")); // NOI18N
        removeOptionButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "BTN_ACSD_RemoveProcessorOption")); // NOI18N
        additionalJavacParamsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage (CustomizerCompile.class,"AD_AdditionalCompilerOptions"));
    }// </editor-fold>//GEN-END:initComponents

    private void addProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProcessorButtonActionPerformed
        final AddAnnotationProcessor panel = new AddAnnotationProcessor();
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage (CustomizerCompile.class, "LBL_AddAnnotationProcessor_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String fqn = panel.getProcessorFQN();
                desc.setValid(fqn.length() > 0);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible (true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultListModel)annotationProcessorsList.getModel()).addElement(panel.getProcessorFQN());
        }
        dlg.dispose();
    }//GEN-LAST:event_addProcessorButtonActionPerformed

    private void removeProcessorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProcessorButtonActionPerformed
        DefaultListModel model = (DefaultListModel) annotationProcessorsList.getModel();
        int[] indices = annotationProcessorsList.getSelectedIndices();
        for (int i = indices.length - 1 ; i >= 0 ; i--) {
            model.remove(indices[i]);
        }
        if (!model.isEmpty()) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1; 
            if (selectedIndex > model.size() - 1) {
                selectedIndex = model.size() - 1;
            }
            annotationProcessorsList.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event_removeProcessorButtonActionPerformed

    private void enableAPTCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAPTCheckBoxActionPerformed
        boolean b = enableAPTCheckBox.isSelected();
        enableAPTEditorCheckBox.setEnabled(b);
        annotationProcessorsLabel.setEnabled(b);
        annotationProcessorsList.setEnabled(b);
        addProcessorButton.setEnabled(b);
        int[] indices = annotationProcessorsList.getSelectedIndices();
        removeProcessorButton.setEnabled(b && indices != null && indices.length > 0);
        processorOptionsLabel.setEnabled(b);
        processorOptionsTable.setEnabled(b);
        addOptionButton.setEnabled(b);
        int[] rows = processorOptionsTable.getSelectedRows();
        removeOptionButton.setEnabled(b && rows != null && rows.length > 0);
    }//GEN-LAST:event_enableAPTCheckBoxActionPerformed

    private void enableJavacFork(@NonNull final ComboBoxModel<?> model) {
        final JavaPlatform platform = PlatformUiSupport.getPlatform(model.getSelectedItem());
        final boolean isDefaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform().equals(platform);
        fork.setVisible(isDefaultPlatform);
        showDefaultPlatformWarning(isDefaultPlatform && !fork.isSelected());
    }

    private void showDefaultPlatformWarning(final boolean visible) {
        final String warning = visible ?
            NbBundle.getMessage(CustomizerCompile.class, "TXT_DefaultPlatformWarning") :
            " "; //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            additionalJavacParamsExample,
            NbBundle.getMessage(CustomizerCompile.class, "LBL_AdditionalCompilerOptionsExample", warning));
    }

    private void annotationProcessorsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_annotationProcessorsListValueChanged
        int[] indices = annotationProcessorsList.getSelectedIndices();
        removeProcessorButton.setEnabled(enableAPTCheckBox.isSelected() && indices != null && indices.length > 0);
    }//GEN-LAST:event_annotationProcessorsListValueChanged

    private void addOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOptionButtonActionPerformed
        final AddProcessorOption panel = new AddProcessorOption();
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage (CustomizerCompile.class, "LBL_AddProcessorOption_Title")); //NOI18N
        desc.setValid(false);
        panel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String key = panel.getOptionKey();
                for(String s : key.split("\\.", -1)) { //NOI18N
                    if (!SourceVersion.isIdentifier(s)) {
                        desc.setValid(false);
                        return;
                    }
                }
                desc.setValid(true);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible (true);
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            ((DefaultTableModel)processorOptionsTable.getModel()).addRow(new String[] {panel.getOptionKey(), panel.getOptionValue()});
        }
        dlg.dispose();
    }//GEN-LAST:event_addOptionButtonActionPerformed

    private void removeOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeOptionButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) processorOptionsTable.getModel();
        int[] rows = processorOptionsTable.getSelectedRows();
        for(int i = rows.length - 1 ; i >= 0 ; i--) {
            model.removeRow(rows[i]);
        }
        if (model.getRowCount() > 0) {
            // Select reasonable row
            int selectedIndex = rows[rows.length - 1] - rows.length  + 1;
            if ( selectedIndex > model.getRowCount() - 1) {
                selectedIndex = model.getRowCount() - 1;
            }
            processorOptionsTable.setRowSelectionInterval(selectedIndex, selectedIndex);
        }
    }//GEN-LAST:event_removeOptionButtonActionPerformed

    private void processorOptionsListSelectionChanged(javax.swing.event.ListSelectionEvent evt) {
        int[] rows = processorOptionsTable.getSelectedRows();
        removeOptionButton.setEnabled(enableAPTCheckBox.isSelected() && rows != null && rows.length > 0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane AnnotationProcessorsScrollPane;
    private javax.swing.JButton addOptionButton;
    private javax.swing.JButton addProcessorButton;
    private javax.swing.JLabel additionalJavacParamsExample;
    private javax.swing.JTextField additionalJavacParamsField;
    private javax.swing.JLabel additionalJavacParamsLabel;
    private javax.swing.JLabel annotationProcessorsLabel;
    private javax.swing.JList annotationProcessorsList;
    private javax.swing.JCheckBox compileOnSave;
    private javax.swing.JLabel compileOnSaveDescription;
    private javax.swing.JCheckBox debugInfoCheckBox;
    private javax.swing.JCheckBox deprecationCheckBox;
    private javax.swing.JCheckBox doDependCheckBox;
    private javax.swing.JCheckBox enableAPTCheckBox;
    private javax.swing.JCheckBox enableAPTEditorCheckBox;
    private javax.swing.JCheckBox fork;
    private javax.swing.JLabel processorOptionsLabel;
    private javax.swing.JScrollPane processorOptionsScrollPane;
    private javax.swing.JTable processorOptionsTable;
    private javax.swing.JButton removeOptionButton;
    private javax.swing.JButton removeProcessorButton;
    // End of variables declaration//GEN-END:variables

    private static final class UnselectedWhenDisabledButtonModel extends JToggleButton.ToggleButtonModel {
        private final ButtonModel delegate;

        public UnselectedWhenDisabledButtonModel(ButtonModel delegate, boolean enabled) {
            this.delegate = delegate;
            setEnabled(enabled);
        }

        @Override
        public boolean isSelected() {
            return isEnabled() && delegate.isSelected();
        }

        @Override
        public void setSelected(boolean b) {
            delegate.setSelected(b);
        }

    }
}
