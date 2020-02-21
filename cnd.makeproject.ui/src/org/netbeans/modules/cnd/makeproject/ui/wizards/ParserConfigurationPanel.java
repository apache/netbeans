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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ParserConfigurationPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private final ParserConfigurationDescriptorPanel controller;
    private boolean first = true;

    /*package-local*/ ParserConfigurationPanel(ParserConfigurationDescriptorPanel sourceFoldersDescriptorPanel) {
        initComponents();
        this.controller = sourceFoldersDescriptorPanel;

        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("INCLUDE_LABEL_AD"));
        includeTextField.getAccessibleContext().setAccessibleDescription(getString("INCLUDE_LABEL_AD"));
        includeEditButton.getAccessibleContext().setAccessibleDescription(getString("INCLUDE_BROWSE_BUTTON_AD"));
        macroTextField.getAccessibleContext().setAccessibleDescription(getString("MACRO_LABEL_AD"));
        macroEditButton.getAccessibleContext().setAccessibleDescription(getString("MACRO_EDIT_BUTTON_AD"));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewMakeWizardP4"); // NOI18N
    }

    private void update(DocumentEvent e) {
        controller.stateChanged(null);
    }

    void read(WizardDescriptor settings) {
        manualButton.setEnabled(true);
        automaticButton.setEnabled(true);
        automaticButton.setSelected(true);
        togglePanel(false);
        if (first) {
            first = false;
            @SuppressWarnings("unchecked")
            List<? extends SourceFolderInfo> roots = WizardConstants.PROPERTY_SOURCE_FOLDERS_LIST.get(settings);
            if (roots != null) {
                StringBuilder buf = new StringBuilder();
                for(SourceFolderInfo folder : roots){
                    if (buf.length()>0) {
                        buf.append(';');
                    }
                    FileObject dir = folder.getFileObject();
                    if (dir != null) {
                        buf.append(RemoteFileUtil.getAbsolutePath(dir));
                        if (dir.isFolder()) {
                            final FileObject[] listFiles = dir.getChildren();
                            if (listFiles != null) {
                                for (FileObject sub : listFiles){
                                    if (sub.isFolder()) {
                                        if (sub.getNameExt().toLowerCase(Locale.getDefault()).endsWith("include")) { // NOI18N
                                            buf.append(';');
                                            buf.append(RemoteFileUtil.getAbsolutePath(sub));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                includeTextField.setText(buf.toString());
            }
        }
    }

    void store(WizardDescriptor wizardDescriptor) {
        if (manualButton.isSelected()) {
            WizardConstants.PROPERTY_INCLUDES.put(wizardDescriptor, includeTextField.getText());
            WizardConstants.PROPERTY_MACROS.put(wizardDescriptor, macroTextField.getText());
            WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE.put(wizardDescriptor, Boolean.TRUE);
            WizardConstants.PROPERTY_BUILD_LOG.put(wizardDescriptor, ""); // NOI18N
        } else {
            WizardConstants.PROPERTY_INCLUDES.put(wizardDescriptor, ""); // NOI18N
            WizardConstants.PROPERTY_MACROS.put(wizardDescriptor, ""); // NOI18N
            WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE.put(wizardDescriptor, Boolean.FALSE);
        }
    }

    boolean valid(WizardDescriptor settings) {
        return true;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        automaticButton = new javax.swing.JRadioButton();
        manualButton = new javax.swing.JRadioButton();
        codeModelPanel = new javax.swing.JPanel();
        codeModelLabel = new javax.swing.JLabel();
        includeLabel = new javax.swing.JLabel();
        includeTextField = new javax.swing.JTextField();
        includeEditButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        macroTextField = new javax.swing.JTextField();
        macroEditButton = new javax.swing.JButton();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setMinimumSize(new java.awt.Dimension(300, 158));
        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(automaticButton);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(automaticButton, bundle.getString("ParserAutomaticConfiguration")); // NOI18N
        automaticButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                automaticButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(automaticButton, gridBagConstraints);
        automaticButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "ParserAutomaticConfiguration_AD")); // NOI18N

        buttonGroup1.add(manualButton);
        org.openide.awt.Mnemonics.setLocalizedText(manualButton, bundle.getString("ParserManualConfiguration")); // NOI18N
        manualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(manualButton, gridBagConstraints);
        manualButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "ParserManualConfiguration_AD")); // NOI18N

        codeModelPanel.setLayout(new java.awt.GridBagLayout());

        codeModelLabel.setText(bundle.getString("CODEMODEL_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        codeModelPanel.add(codeModelLabel, gridBagConstraints);

        includeLabel.setLabelFor(includeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(includeLabel, bundle.getString("INCLUDE_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        codeModelPanel.add(includeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        codeModelPanel.add(includeTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(includeEditButton, bundle.getString("INCLUDE_BROWSE_BUTTON_TXT")); // NOI18N
        includeEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeEditButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        codeModelPanel.add(includeEditButton, gridBagConstraints);

        jLabel2.setLabelFor(macroTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("MACRO_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        codeModelPanel.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        codeModelPanel.add(macroTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(macroEditButton, bundle.getString("MACRO_EDIT_BUTTON_TXT")); // NOI18N
        macroEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                macroEditButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        codeModelPanel.add(macroEditButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(codeModelPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(bundle.getString("SourceFoldersInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "INFO_AREA_AN")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "INFO_AREA_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void automaticButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticButtonActionPerformed
        togglePanel(false);
        update((DocumentEvent) null);
    }//GEN-LAST:event_automaticButtonActionPerformed

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualButtonActionPerformed
        togglePanel(true);
        update((DocumentEvent) null);
    }//GEN-LAST:event_manualButtonActionPerformed

    private void togglePanel(boolean manual) {
        for (Component component : codeModelPanel.getComponents()) {
            component.setEnabled(manual);
        }
        if (manual) {
            instructionsTextArea.setText(getString("SourceFoldersInstructions")); // NOI18N
        } else {
            instructionsTextArea.setText(getString("DiscoveryInstructions")); // NOI18N
        }
    }

    private void macroEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_macroEditButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(macroTextField.getText(), "; "); // NOI18N
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken().trim());
        }
        MacrosListPanel panel = new MacrosListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addOuterPanel(panel), "Macro Definitions"); // NOI18N
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            List<String> newList = panel.getListData();
            StringBuilder macros = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    macros.append(";"); // NOI18N
                }
                macros.append(newList.get(i));
            }
            macroTextField.setText(macros.toString());
        }
    }//GEN-LAST:event_macroEditButtonActionPerformed

    private void includeEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeEditButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(includeTextField.getText(), ";"); // NOI18N
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        IncludesListPanel panel = new IncludesListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addOuterPanel(panel), getString("INCLUDE_DIRIRECTORIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            List<String> newList = panel.getListData();
            StringBuilder includes = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    includes.append(";"); // NOI18N
                }
                includes.append(newList.get(i));
            }
            includeTextField.setText(includes.toString());
        }
    }//GEN-LAST:event_includeEditButtonActionPerformed

    private JPanel addOuterPanel(JPanel innerPanel) {
        JPanel outerPanel = new JPanel();
        outerPanel.getAccessibleContext().setAccessibleDescription(getString("DIALOG_AD"));
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(innerPanel, gridBagConstraints);
        outerPanel.setPreferredSize(new Dimension(500, 250));
        return outerPanel;
    }

    private class IncludesListPanel extends ListEditorPanel<String> {

        public IncludesListPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            String seed = null;
            if (FileChooser.getCurrentChooserFile() != null) {
                seed = FileChooser.getCurrentChooserFile().getPath();
            }
            if (seed == null) {
                seed = System.getProperty("user.home"); // NOI18N
            }
            JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                    controller.getWizardDescriptor(),
                    getString("INCLUDE_DIR_DIALOG_TITLE_TXT"),
                    getString("INCLUDE_DIR_DIALOG_BUTTON_TXT"),
                    JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            itemPath = CndPathUtilities.normalizeSlashes(itemPath);
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("DIR_LIST_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("DIR_LIST_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o; // new String(o); ???
        }

        @Override
        public void editAction(String o, int i) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText().trim();
            replaceElement(o, newS, i);
        }
    }

    private static class MacrosListPanel extends ListEditorPanel<String> {

        public MacrosListPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("ADD_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            String newS = notifyDescriptor.getInputText().trim();
            return newS;
        }

        @Override
        public String getListLabelText() {
            return getString("MACROS_LIST_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("MACROS_LIST_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public void editAction(String o, int i) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText().trim();
            replaceElement(o, newS, i);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton automaticButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel codeModelLabel;
    private javax.swing.JPanel codeModelPanel;
    private javax.swing.JButton includeEditButton;
    private javax.swing.JLabel includeLabel;
    private javax.swing.JTextField includeTextField;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton macroEditButton;
    private javax.swing.JTextField macroTextField;
    private javax.swing.JRadioButton manualButton;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(PanelProjectLocationVisual.class, s);
    }
}
