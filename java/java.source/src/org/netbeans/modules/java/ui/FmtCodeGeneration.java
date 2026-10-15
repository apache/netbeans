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

package org.netbeans.modules.java.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;

import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import static org.netbeans.modules.java.ui.CategorySupport.OPTION_ID;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.NbBundle;


/**
 *
 * @author Petr Hrebejk, Dusan Balek
 */
public class FmtCodeGeneration extends javax.swing.JPanel implements Runnable, ListSelectionListener {
    
    /** Creates new form FmtCodeGeneration */
    public FmtCodeGeneration() {
        initComponents();
        
        qualifyFieldAccessCheckBox.putClientProperty(OPTION_ID, qualifyFieldAccess);
        addOverrideAnnortationCheckBox.putClientProperty(OPTION_ID, addOverrideAnnotation);
        parametersFinalCheckBox.putClientProperty(OPTION_ID, makeParametersFinal);
        localVarsFinalCheckBox.putClientProperty(OPTION_ID, makeLocalVarsFinal);
        membersOrderList.putClientProperty(OPTION_ID, classMembersOrder);
        sortByVisibilityCheckBox.putClientProperty(OPTION_ID, sortMembersByVisibility);
        visibilityOrderList.putClientProperty(OPTION_ID, visibilityOrder);
        keepGASTogetherCheckBox.putClientProperty(OPTION_ID, keepGettersAndSettersTogether);
        sortMembersAlphaCheckBox.putClientProperty(OPTION_ID, sortMembersInGroups);
        sortUsesDependenciesCheckBox.putClientProperty(OPTION_ID, sortUsesDependencies);
        insertionPointComboBox.putClientProperty(OPTION_ID, classMemberInsertionPoint);
    }
    
    public static PreferencesCustomizer.Factory getController() {
        return new PreferencesCustomizer.Factory() {
            public PreferencesCustomizer create(Preferences preferences) {
                CodeGenCategorySupport support = new CodeGenCategorySupport(preferences, new FmtCodeGeneration());
                ((Runnable)support.panel).run();
                return support;
            }
        };
    }
    
    @Override
    public void run() {
        membersOrderList.setSelectedIndex(0);
        membersOrderList.addListSelectionListener(this);
        enableMembersOrderButtons();
        visibilityOrderList.setSelectedIndex(0);
        visibilityOrderList.addListSelectionListener(this);
        enableVisibilityOrder();
        enableInsertionPoint();
        enableDependencyOrder();
        otherLabel.setVisible(false);
        qualifyFieldAccessCheckBox.setVisible(false);
        addOverrideAnnortationCheckBox.setVisible(false);
        parametersFinalCheckBox.setVisible(false);
        localVarsFinalCheckBox.setVisible(false);
        jSeparator3.setVisible(false);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == membersOrderList)
            enableMembersOrderButtons();
        else
            enableVisibilityOrder();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        otherLabel = new javax.swing.JLabel();
        qualifyFieldAccessCheckBox = new javax.swing.JCheckBox();
        addOverrideAnnortationCheckBox = new javax.swing.JCheckBox();
        parametersFinalCheckBox = new javax.swing.JCheckBox();
        localVarsFinalCheckBox = new javax.swing.JCheckBox();
        memberOrderLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        membersOrderList = new javax.swing.JList();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        sortByVisibilityCheckBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        visibilityOrderList = new javax.swing.JList();
        visUpButton = new javax.swing.JButton();
        visDownButton = new javax.swing.JButton();
        insertionPointLabel = new javax.swing.JLabel();
        keepGASTogetherCheckBox = new javax.swing.JCheckBox();
        sortMembersAlphaCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        insertionPointComboBox = new javax.swing.JComboBox();
        sortUsesDependenciesCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_CodeGeneration")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(otherLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Other")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(qualifyFieldAccessCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_QualifyFieldAccess")); // NOI18N
        qualifyFieldAccessCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        qualifyFieldAccessCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        qualifyFieldAccessCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(addOverrideAnnortationCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_AddOverrideAnnotation")); // NOI18N
        addOverrideAnnortationCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addOverrideAnnortationCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addOverrideAnnortationCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(parametersFinalCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_ParametersFinal")); // NOI18N
        parametersFinalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        parametersFinalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        parametersFinalCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(localVarsFinalCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_LocalVariablesFinal")); // NOI18N
        localVarsFinalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localVarsFinalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        localVarsFinalCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(memberOrderLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOreder")); // NOI18N

        membersOrderList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        membersOrderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(membersOrderList);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederUp")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederDown")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sortByVisibilityCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_SortByVisibility")); // NOI18N
        sortByVisibilityCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByVisibilityCheckBoxActionPerformed(evt);
            }
        });

        visibilityOrderList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        visibilityOrderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(visibilityOrderList);

        org.openide.awt.Mnemonics.setLocalizedText(visUpButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederUp")); // NOI18N
        visUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visUpButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(visDownButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederDown")); // NOI18N
        visDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visDownButtonActionPerformed(evt);
            }
        });

        insertionPointLabel.setLabelFor(insertionPointComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(insertionPointLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_InsertionPoint")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepGASTogetherCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_KeepGASTogether")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sortMembersAlphaCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_SortMembersAlpha")); // NOI18N
        sortMembersAlphaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortMembersAlphaCheckBoxActionPerformed(evt);
            }
        });

        insertionPointComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        sortUsesDependenciesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(sortUsesDependenciesCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "FmtCodeGeneration.sortUsesDependenciesCheckBox.text")); // NOI18N
        sortUsesDependenciesCheckBox.setEnabled(false);
        sortUsesDependenciesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortUsesDependenciesCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(otherLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(qualifyFieldAccessCheckBox)
                            .addComponent(addOverrideAnnortationCheckBox)
                            .addComponent(parametersFinalCheckBox)
                            .addComponent(localVarsFinalCheckBox))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(insertionPointLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertionPointComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(memberOrderLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sortByVisibilityCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keepGASTogetherCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(visDownButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(visUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(sortMembersAlphaCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortUsesDependenciesCheckBox, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(otherLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qualifyFieldAccessCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addOverrideAnnortationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parametersFinalCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localVarsFinalCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(memberOrderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sortByVisibilityCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(visUpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(visDownButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepGASTogetherCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sortMembersAlphaCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sortUsesDependenciesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertionPointLabel)
                    .addComponent(insertionPointComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int idx = membersOrderList.getSelectedIndex();
        if (idx > 0) {
            Object val = membersOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).insertElementAt(val, idx - 1);
            membersOrderList.setSelectedIndex(idx - 1);
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int idx = membersOrderList.getSelectedIndex();
        if (idx >= 0 && idx < membersOrderList.getModel().getSize() - 1) {
            Object val = membersOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).insertElementAt(val, idx + 1);
            membersOrderList.setSelectedIndex(idx + 1);
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void visUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visUpButtonActionPerformed
        int idx = visibilityOrderList.getSelectedIndex();
        if (idx > 0) {
            Object val = visibilityOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).insertElementAt(val, idx - 1);
            visibilityOrderList.setSelectedIndex(idx - 1);
        }
    }//GEN-LAST:event_visUpButtonActionPerformed

    private void visDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visDownButtonActionPerformed
        int idx = visibilityOrderList.getSelectedIndex();
        if (idx >= 0 && idx < visibilityOrderList.getModel().getSize() - 1) {
            Object val = visibilityOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).insertElementAt(val, idx + 1);
            visibilityOrderList.setSelectedIndex(idx + 1);
        }
    }//GEN-LAST:event_visDownButtonActionPerformed

    private void sortByVisibilityCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByVisibilityCheckBoxActionPerformed
        enableVisibilityOrder();
        enableDependencyOrder();
    }//GEN-LAST:event_sortByVisibilityCheckBoxActionPerformed

    private void sortMembersAlphaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortMembersAlphaCheckBoxActionPerformed
        enableInsertionPoint();
        enableDependencyOrder();
    }//GEN-LAST:event_sortMembersAlphaCheckBoxActionPerformed

    private void sortUsesDependenciesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortUsesDependenciesCheckBoxActionPerformed
    }//GEN-LAST:event_sortUsesDependenciesCheckBoxActionPerformed
    
    private void enableDependencyOrder() {
        boolean b = sortByVisibilityCheckBox.isSelected()|| sortMembersAlphaCheckBox.isSelected();
        sortUsesDependenciesCheckBox.setEnabled(b);
    }
    
    private void enableMembersOrderButtons() {
        int idx = membersOrderList.getSelectedIndex();                
        upButton.setEnabled(idx > 0);
        downButton.setEnabled(idx >= 0 && idx < membersOrderList.getModel().getSize() - 1);
    }
    
    private void enableVisibilityOrder() {
        int idx = visibilityOrderList.getSelectedIndex();
        boolean b = sortByVisibilityCheckBox.isSelected();
        visibilityOrderList.setEnabled(b);
        visUpButton.setEnabled(b && idx > 0);
        visDownButton.setEnabled(b && idx >= 0 && idx < visibilityOrderList.getModel().getSize() - 1);
    }
    
    private void enableInsertionPoint() {
        if (ipModel == null) {
            ipModel = insertionPointComboBox.getModel();
        }
        Object[] values;
        Object toSelect = insertionPointComboBox.getSelectedItem();
        if (sortMembersAlphaCheckBox.isSelected()) {
            if (toSelect == ipModel.getElementAt(0) || toSelect == ipModel.getElementAt(1)) {
                toSelect = ipModel.getElementAt(2);
            }
            values = new Object[] {ipModel.getElementAt(2), ipModel.getElementAt(3)};
        } else {
            if (toSelect == ipModel.getElementAt(2)) {
                toSelect = ipModel.getElementAt(0);
            }
            values = new Object[] {ipModel.getElementAt(0), ipModel.getElementAt(1), ipModel.getElementAt(3)};
        }
        insertionPointComboBox.setModel(new DefaultComboBoxModel(values));
        insertionPointComboBox.setSelectedItem(toSelect);
    }
    
    private ComboBoxModel ipModel;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addOverrideAnnortationCheckBox;
    private javax.swing.JButton downButton;
    private javax.swing.JComboBox insertionPointComboBox;
    private javax.swing.JLabel insertionPointLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JCheckBox keepGASTogetherCheckBox;
    private javax.swing.JCheckBox localVarsFinalCheckBox;
    private javax.swing.JLabel memberOrderLabel;
    private javax.swing.JList membersOrderList;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JCheckBox parametersFinalCheckBox;
    private javax.swing.JCheckBox qualifyFieldAccessCheckBox;
    private javax.swing.JCheckBox sortByVisibilityCheckBox;
    private javax.swing.JCheckBox sortMembersAlphaCheckBox;
    private javax.swing.JCheckBox sortUsesDependenciesCheckBox;
    private javax.swing.JButton upButton;
    private javax.swing.JButton visDownButton;
    private javax.swing.JButton visUpButton;
    private javax.swing.JList visibilityOrderList;
    // End of variables declaration//GEN-END:variables

        private static final class CodeGenCategorySupport extends CategorySupport.DocumentCategorySupport {

        private CodeGenCategorySupport(Preferences preferences, JPanel panel) {
            super(preferences, "code-generation", panel, NbBundle.getMessage(FmtCodeGeneration.class, "SAMPLE_CodeGen"), //NOI18N
                    new String[] { FmtOptions.blankLinesBeforeFields, "1" }); //NOI18N
        }
    
        @Override
        protected void loadListData(JList list, String optionID, Preferences node) {
            DefaultListModel model = new DefaultListModel();
            String value = node.get(optionID, getDefaultAsString(optionID));
            for (String s : value.trim().split("\\s*[,;]\\s*")) { //NOI18N
                if (classMembersOrder.equals(optionID)) {
                    Element e = new Element();
                    if (s.startsWith("STATIC ")) { //NOI18N
                        e.isStatic = true;
                        s = s.substring(7);
                    }
                    e.kind = ElementKind.valueOf(s);
                    model.addElement(e);
                } else {
                    Visibility v = new Visibility();
                    v.kind = s;
                    model.addElement(v);
                }
            }
            list.setModel(model);
        }
        
        @Override
        protected void storeListData(final JList list, final String optionID, final Preferences node) {
            StringBuilder sb = null;
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(';');
                }
                if (classMembersOrder.equals(optionID)) {
                    Element e = (Element) list.getModel().getElementAt(i);
                    if (e.isStatic)
                        sb.append("STATIC "); //NOI18N
                    sb.append(e.kind.name());
                } else {
                    Visibility v = (Visibility) list.getModel().getElementAt(i);
                    sb.append(v.kind);
                }
            }
            String value = sb != null ? sb.toString() : ""; //NOI18N
            if (getDefaultAsString(optionID).equals(value))
                node.remove(optionID);
            else
                node.put(optionID, value);            
        }

        protected void doModification(ResultIterator resultIterator) throws Exception {
            WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
            copy.toPhase(Phase.RESOLVED);
            TreeMaker tm = copy.getTreeMaker();
            GeneratorUtilities gu = GeneratorUtilities.get(copy);
            CompilationUnitTree cut = copy.getCompilationUnit();
            ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
            VariableTree field = (VariableTree)ct.getMembers().get(1);
            List<Tree> members = new ArrayList<Tree>();
            AssignmentTree stat = tm.Assignment(tm.Identifier("name"), tm.Literal("Name")); //NOI18N
            BlockTree init = tm.Block(Collections.singletonList(tm.ExpressionStatement(stat)), false);
            members.add(init);
            members.add(gu.createConstructor(ct, Collections.<VariableTree>emptyList()));
            members.add(gu.createGetter(field));
            ModifiersTree mods = tm.Modifiers(EnumSet.of(Modifier.PRIVATE));
            ClassTree inner = tm.Class(mods, "Inner", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList()); //NOI18N
            members.add(inner);
            mods = tm.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));
            ClassTree nested = tm.Class(mods, "Nested", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList()); //NOI18N
            members.add(nested);
            IdentifierTree nestedId = tm.Identifier("Nested"); //NOI18N
            VariableTree staticField = tm.Variable(mods, "instance", nestedId, null); //NOI18N
            members.add(staticField);
            NewClassTree nct = tm.NewClass(null, Collections.<ExpressionTree>emptyList(), nestedId, Collections.<ExpressionTree>emptyList(), null);
            stat = tm.Assignment(tm.Identifier("instance"), nct); //NOI18N
            BlockTree staticInit = tm.Block(Collections.singletonList(tm.ExpressionStatement(stat)), true);
            members.add(staticInit);
            members.add(gu.createGetter(staticField));
            ClassTree newCT = gu.insertClassMembers(ct, members);
            copy.rewrite(ct, newCT);
        }
        
        private static class Element {
            
            private boolean isStatic;
            private ElementKind kind;

            @Override
            public String toString() {
                return (isStatic ? NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_STATIC") + " " : "") //NOI18N
                        + NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_" + kind.name()); //NOI18N
            }
        }

        private static class Visibility {
            
            private String kind;

            @Override
            public String toString() {
                return NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_" + kind); //NOI18N
            }
        }
    }
}
