/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * CssRuleCreateActionDialog.java
 *
 * Created on February 3, 2005, 9:16 AM
 */

package org.netbeans.modules.css.editor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.editor.HtmlTags;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Dialog for creating the Style Rule
 * @author  Winston Prakash
 * @version 1.0
 */
public class CssRuleCreateActionDialog extends javax.swing.JPanel {

    private JDialog dialog;
    private DialogDescriptor dlg = null;

    private static final String ELEMENT_TYPE = "elelment"; //NOI18N
    private static final String CLASS_TYPE = "class"; //NOI18N
    private static final String ELEMENT_ID_TYPE = "element_id"; //NOI18N

    private static final String NONE = "<None>";  //NOI18N

    DefaultListModel selectedRules = new DefaultListModel();

    private String styleRuleName = "";

    /** Creates new form CssRuleCreateActionDialog */
    public CssRuleCreateActionDialog() {
        initComponents();
        String[] htmlTags = HtmlTags.getTags();

        // Optional prefix
        DefaultComboBoxModel htmlTagsModel1 = new DefaultComboBoxModel();
        htmlTagsModel1.addElement(NONE);
        htmlTagsModel1.addElement("a:link");
        htmlTagsModel1.addElement("a:visited");
        htmlTagsModel1.addElement("a:hover");
        htmlTagsModel1.addElement("a:active");
        for( int i=0; i< htmlTags.length; i++){
            htmlTagsModel1.addElement(htmlTags[i]);
        }

        DefaultComboBoxModel htmlTagsModel = new DefaultComboBoxModel();
        //htmlTagsModel.addElement(NONE);
        for( int i=0; i< htmlTags.length; i++){
            htmlTagsModel.addElement(htmlTags[i]);
        }
        selectElementComboBox.setModel(htmlTagsModel);
        classPrefixComboBox.setModel(htmlTagsModel1);
        ruleHierarchyList.setModel(selectedRules);
        removeRuleButton.setEnabled(false);
    }

    public void showDialog(){
        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                    styleRuleName = previewTextField.getText().trim();
                    // As Jeff pointed out even if user has not added
                    // any value to the right hand side the values selected
                    // in the left hand side should be used
                    if(styleRuleName.equals("")){
                        String selectionType = selectRuleButtonGroup.getSelection().getActionCommand();
                        styleRuleName = getRule(selectionType);
                    }
                    dialog.setVisible(false);
                }
            }
        };
        dlg = new DialogDescriptor(this, NbBundle.getMessage(CssRuleCreateActionDialog.class, "STYLE_RULE_EDITOR_TITLE"), true, listener);
        dlg.setHelpCtx(new HelpCtx("projrave_ui_elements_css_create_style_rule")); // NOI18N

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    public String getStyleRuleName(){
        return styleRuleName;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectRuleButtonGroup = new javax.swing.ButtonGroup();
        previewPanel = new javax.swing.JPanel();
        previewLable = new javax.swing.JLabel();
        previewTextField = new javax.swing.JTextField();
        rulePanel = new javax.swing.JPanel();
        addRemoveRulePanel = new javax.swing.JPanel();
        addRuleButton = new javax.swing.JButton();
        removeRuleButton = new javax.swing.JButton();
        selectRulePanel = new javax.swing.JPanel();
        selectElementRadioButton = new javax.swing.JRadioButton();
        selectElementComboBox = new javax.swing.JComboBox();
        selectClassRadioButton = new javax.swing.JRadioButton();
        selectClassTextField = new javax.swing.JTextField();
        selectElelmentIdRadioButton = new javax.swing.JRadioButton();
        selectElementIdTextField = new javax.swing.JTextField();
        classPrefixComboBox = new javax.swing.JComboBox();
        classPrefixSeparator = new javax.swing.JLabel();
        ruleHierarchyPanel = new javax.swing.JPanel();
        moveRulePanel = new javax.swing.JPanel();
        moveRuleUpButton = new javax.swing.JButton();
        moveRuleDownButton = new javax.swing.JButton();
        hierarchyContainer = new javax.swing.JPanel();
        ruleHierarchyScroll = new javax.swing.JScrollPane();
        ruleHierarchyList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        previewPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8));
        previewPanel.setLayout(new java.awt.BorderLayout(5, 5));

        previewLable.setLabelFor(previewTextField);
        org.openide.awt.Mnemonics.setLocalizedText(previewLable, org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "RULE_PREVIEW_LABEL")); // NOI18N
        previewPanel.add(previewLable, java.awt.BorderLayout.WEST);

        previewTextField.setEditable(false);
        previewPanel.add(previewTextField, java.awt.BorderLayout.CENTER);
        previewTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "PREVIEW_LABEL_ACCESSIBLE_DESC")); // NOI18N

        add(previewPanel, java.awt.BorderLayout.SOUTH);

        rulePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rulePanel.setLayout(new java.awt.BorderLayout(3, 3));

        addRemoveRulePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        addRemoveRulePanel.setLayout(new java.awt.GridBagLayout());

        addRuleButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ADD_RULE_BUTTON_MNEMONIC").charAt(0));
        addRuleButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ADD_RULE_LBL")); // NOI18N
        addRuleButton.setToolTipText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ADD_RULE_TOOL_TIP")); // NOI18N
        addRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRuleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        addRemoveRulePanel.add(addRuleButton, gridBagConstraints);

        removeRuleButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "REMOVE_RULE_BUTTON_MNEMONIC").charAt(0));
        removeRuleButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "REMOVE_RULE_LBL")); // NOI18N
        removeRuleButton.setToolTipText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "REMOVE_RULE_TOOL_TIP")); // NOI18N
        removeRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRuleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        addRemoveRulePanel.add(removeRuleButton, gridBagConstraints);

        rulePanel.add(addRemoveRulePanel, java.awt.BorderLayout.CENTER);

        selectRulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "RULE_TYPE_PANEL_TITLE"))); // NOI18N
        selectRulePanel.setLayout(new java.awt.GridBagLayout());

        selectRuleButtonGroup.add(selectElementRadioButton);
        selectElementRadioButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_RULE_TYPE_MNEMONIC").charAt(0));
        selectElementRadioButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "HTML_ELELEMT")); // NOI18N
        selectElementRadioButton.setActionCommand(ELEMENT_TYPE);
        selectElementRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRuleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        selectRulePanel.add(selectElementRadioButton, gridBagConstraints);
        selectElementRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "HTML_ELEMENT_RULE_TYPE_ACCESSIBLE_DESCRIPTION")); // NOI18N

        selectElementComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "a", "abbr" }));
        selectElementComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 5);
        selectRulePanel.add(selectElementComboBox, gridBagConstraints);
        selectElementComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "HTML_ELEMENT_ACCESSIBLE_NAME")); // NOI18N
        selectElementComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "HTML_ELEMENT_ACCESSIBLE_DESC")); // NOI18N

        selectRuleButtonGroup.add(selectClassRadioButton);
        selectClassRadioButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "CLASS_RULE_TYPE_MNEMONIC").charAt(0));
        selectClassRadioButton.setSelected(true);
        selectClassRadioButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "CLASS_NAME_LBL")); // NOI18N
        selectClassRadioButton.setActionCommand(CLASS_TYPE);
        selectClassRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRuleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        selectRulePanel.add(selectClassRadioButton, gridBagConstraints);
        selectClassRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "CLASS_RULE_TYPE_ACCESSIBLE_DESCRIPTION")); // NOI18N

        selectClassTextField.setColumns(15);
        selectClassTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                selectClassTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 5, 5);
        selectRulePanel.add(selectClassTextField, gridBagConstraints);
        selectClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "CLASS_TEXT_FIELD_ACCESSIBLE_NAME")); // NOI18N
        selectClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "CLASS_TEXT_FIELD_ACCESSIBLE_DESC")); // NOI18N

        selectRuleButtonGroup.add(selectElelmentIdRadioButton);
        selectElelmentIdRadioButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_ID_RULE_TYPE_MNEMONIC").charAt(0));
        selectElelmentIdRadioButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_ID_LBL")); // NOI18N
        selectElelmentIdRadioButton.setActionCommand(ELEMENT_ID_TYPE);
        selectElelmentIdRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRuleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        selectRulePanel.add(selectElelmentIdRadioButton, gridBagConstraints);
        selectElelmentIdRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_ID_RULE_TYPE_ACCESSIBLE_DESCRIPTION")); // NOI18N

        selectElementIdTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 5);
        selectRulePanel.add(selectElementIdTextField, gridBagConstraints);
        selectElementIdTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_ID_TEXTFIELD_ACCESSIBLE_NAME")); // NOI18N
        selectElementIdTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "ELEMENT_ID_TEXTFIELD_ACCESSIBLE_DESC")); // NOI18N

        classPrefixComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "abbr" }));
        classPrefixComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "OPTIONAL_ELEMENT_TOOLTIP")); // NOI18N
        classPrefixComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 2);
        selectRulePanel.add(classPrefixComboBox, gridBagConstraints);
        classPrefixComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "OPTIONAL_ELEMENT_ACCESSIBLE_DESC")); // NOI18N

        classPrefixSeparator.setFont(new java.awt.Font("Dialog", 1, 18));
        classPrefixSeparator.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        selectRulePanel.add(classPrefixSeparator, gridBagConstraints);

        rulePanel.add(selectRulePanel, java.awt.BorderLayout.WEST);

        ruleHierarchyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "RULE_HIERARCHY_TITLE"))); // NOI18N
        ruleHierarchyPanel.setLayout(new java.awt.BorderLayout(2, 2));

        moveRulePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 5));
        moveRulePanel.setLayout(new java.awt.GridBagLayout());

        moveRuleUpButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "UP_RULE_BUTTON_MNEMONIC").charAt(0));
        moveRuleUpButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "MOVE_RULE_UP_LBL")); // NOI18N
        moveRuleUpButton.setToolTipText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "UP_RULE_BUTTON_TOOLTIP")); // NOI18N
        moveRuleUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRuleUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        moveRulePanel.add(moveRuleUpButton, gridBagConstraints);

        moveRuleDownButton.setMnemonic(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "DOWN_RULE_BUTTON_MNEMONIC").charAt(0));
        moveRuleDownButton.setText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "MOVE_RULE_DOWN_LBL")); // NOI18N
        moveRuleDownButton.setToolTipText(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "DOWN_RULE_BUTTON_TOOLTIP")); // NOI18N
        moveRuleDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRuleDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        moveRulePanel.add(moveRuleDownButton, gridBagConstraints);

        ruleHierarchyPanel.add(moveRulePanel, java.awt.BorderLayout.EAST);

        hierarchyContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        hierarchyContainer.setLayout(new java.awt.BorderLayout());

        ruleHierarchyScroll.setPreferredSize(new java.awt.Dimension(150, 200));
        ruleHierarchyScroll.setViewportView(ruleHierarchyList);
        ruleHierarchyList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "STYLE_RULE_LIST_ACCESSIBLE_NAME")); // NOI18N
        ruleHierarchyList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CssRuleCreateActionDialog.class, "STYLE_RULE_LIST_ACCESSIBLE_DESC")); // NOI18N

        hierarchyContainer.add(ruleHierarchyScroll, java.awt.BorderLayout.CENTER);

        ruleHierarchyPanel.add(hierarchyContainer, java.awt.BorderLayout.CENTER);

        rulePanel.add(ruleHierarchyPanel, java.awt.BorderLayout.EAST);

        add(rulePanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void selectClassTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectClassTextFieldKeyTyped
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                if (!selectClassTextField.getText().equals("")){
                    classPrefixComboBox.setEnabled(true);
                }else{
                    classPrefixComboBox.setEnabled(false);
                }
            }
        });
    }//GEN-LAST:event_selectClassTextFieldKeyTyped
    
    private void moveRuleDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveRuleDownActionPerformed
        int index = ruleHierarchyList.getSelectedIndex();
        if(index >=0 && index < selectedRules.getSize()){
            Object currentObject = selectedRules.get(index);
            int nextIndex = index+1;
            if(nextIndex < selectedRules.getSize()) {
                Object prevObject = selectedRules.get(nextIndex);
                selectedRules.setElementAt(currentObject, index+1);
                selectedRules.setElementAt(prevObject, index);
                ruleHierarchyList.setSelectedIndex(index+1);
                resetRuleHierarchy();
            }
        }
    }//GEN-LAST:event_moveRuleDownActionPerformed
    
    private void moveRuleUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveRuleUpActionPerformed
        int index = ruleHierarchyList.getSelectedIndex();
        if(index > 0){
            Object currentObject = selectedRules.get(index);
            Object prevObject = selectedRules.get(index-1);
            selectedRules.setElementAt(currentObject, index-1);
            selectedRules.setElementAt(prevObject, index);
            ruleHierarchyList.setSelectedIndex(index-1);
            resetRuleHierarchy();
        }
    }//GEN-LAST:event_moveRuleUpActionPerformed
    
    private void removeRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRuleButtonActionPerformed
        Object[] selections = ruleHierarchyList.getSelectedValues();
        for(int i=0; i< selections.length ;i++){
            if (selectedRules.contains(selections[i])){
                selectedRules.removeElement(selections[i]);
            }
        }
        if(!selectedRules.isEmpty()) {
            ruleHierarchyList.setSelectedIndex(0);
        }else{
            removeRuleButton.setEnabled(false);
        }
        resetRuleHierarchy();
    }//GEN-LAST:event_removeRuleButtonActionPerformed
    
    private void selectRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRuleActionPerformed
        String ruleType = evt.getActionCommand();
        switch (ruleType) {
            case ELEMENT_TYPE:
                selectElementComboBox.setEnabled(true);
                classPrefixComboBox.setEnabled(false);
                selectClassTextField.setEnabled(false);
                selectElementIdTextField.setEnabled(false);
                break;
            case CLASS_TYPE:
                selectElementComboBox.setEnabled(false);
                classPrefixComboBox.setEnabled(true);
                selectClassTextField.setEnabled(true);
                selectElementIdTextField.setEnabled(false);
                break;
            case ELEMENT_ID_TYPE:
                selectElementComboBox.setEnabled(false);
                classPrefixComboBox.setEnabled(false);
                selectClassTextField.setEnabled(false);
                selectElementIdTextField.setEnabled(true);
                break;
        }
        resetRuleHierarchy();
    }//GEN-LAST:event_selectRuleActionPerformed
    
    private void addRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRuleButtonActionPerformed
        String ruleType = selectRuleButtonGroup.getSelection().getActionCommand();
        String rule = null;
        switch (ruleType) {
            case ELEMENT_TYPE:
                rule = (String) selectElementComboBox.getSelectedItem();
                if(rule.equals(NONE)) rule = null;
                break;
            case CLASS_TYPE:
                if(!selectClassTextField.getText().trim().equals("")){
                    String rulePrefix = (String) classPrefixComboBox.getSelectedItem();
                    rule = "." + selectClassTextField.getText().trim();
                    if(!rulePrefix.equals(NONE)){
                        rule = rulePrefix  + rule;
                    }
                }   break;
            case ELEMENT_ID_TYPE:
                if(!selectElementIdTextField.getText().trim().equals("")){
                    rule = "#" + selectElementIdTextField.getText().trim();;
                }   break;
        }
        if((rule != null) && (!selectedRules.contains(rule))){
            selectedRules.addElement(rule);
            ruleHierarchyList.setSelectedValue(rule,true);
            removeRuleButton.setEnabled(true);
        }
        resetRuleHierarchy();
    }//GEN-LAST:event_addRuleButtonActionPerformed
    
    private String getRule(String ruleType){
        String rule = null;
        switch (ruleType) {
            case ELEMENT_TYPE:
                rule = (String) selectElementComboBox.getSelectedItem();
                if(rule.equals(NONE)) rule = null;
                break;
            case CLASS_TYPE:
                if(!selectClassTextField.getText().trim().equals("")){
                    String rulePrefix = (String) classPrefixComboBox.getSelectedItem();
                    rule = "." + selectClassTextField.getText().trim();
                    if(!rulePrefix.equals(NONE)){
                        rule = rulePrefix  + rule;
                    }
                }   break;
            case ELEMENT_ID_TYPE:
                if(!selectElementIdTextField.getText().trim().equals("")){
                    rule = "#" + selectElementIdTextField.getText().trim();;
                }   break;
        }
        return rule;
    }
    
    private void resetRuleHierarchy(){
        StringBuffer ruleSetBuf = new StringBuffer();
        for(int i = 0; i < selectedRules.size(); i++){
            String ruleName = ((String) selectedRules.get(i)).trim();
            ruleSetBuf.append(ruleName);
            if(i < selectedRules.size()-1 )ruleSetBuf.append(" ");
        }
        previewTextField.setText(ruleSetBuf.toString());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addRemoveRulePanel;
    private javax.swing.JButton addRuleButton;
    private javax.swing.JComboBox classPrefixComboBox;
    private javax.swing.JLabel classPrefixSeparator;
    private javax.swing.JPanel hierarchyContainer;
    private javax.swing.JButton moveRuleDownButton;
    private javax.swing.JPanel moveRulePanel;
    private javax.swing.JButton moveRuleUpButton;
    private javax.swing.JLabel previewLable;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JTextField previewTextField;
    private javax.swing.JButton removeRuleButton;
    private javax.swing.JList ruleHierarchyList;
    private javax.swing.JPanel ruleHierarchyPanel;
    private javax.swing.JScrollPane ruleHierarchyScroll;
    private javax.swing.JPanel rulePanel;
    private javax.swing.JRadioButton selectClassRadioButton;
    private javax.swing.JTextField selectClassTextField;
    private javax.swing.JRadioButton selectElelmentIdRadioButton;
    private javax.swing.JComboBox selectElementComboBox;
    private javax.swing.JTextField selectElementIdTextField;
    private javax.swing.JRadioButton selectElementRadioButton;
    private javax.swing.ButtonGroup selectRuleButtonGroup;
    private javax.swing.JPanel selectRulePanel;
    // End of variables declaration//GEN-END:variables
}
