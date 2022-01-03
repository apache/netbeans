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
package org.netbeans.modules.cnd.refactoring.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.Utilities;
import org.netbeans.modules.cnd.refactoring.plugins.EncapsulateFieldRefactoringPlugin;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.DeclarationGenerator;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.refactoring.support.MemberInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Panel used by Encapsulate Field refactoring. Contains components to
 * set parameters for the refactoring.
 *
 */
public final class EncapsulateFieldPanel extends javax.swing.JPanel implements CustomRefactoringPanel {
    private static final String ALWAYS_USE_ACCESSORS_PROPERTY = "always_use_accessors"; // NOI18N
    private static final String FIELD_ACCESS_INDEX_PROPERTY = "field_access_index"; // NOI18N
    private static final String METHOD_ACCESS_INDEX_PROPERTY = "method_access_index"; // NOI18N

    private final DefaultTableModel model;
    private final CsmObject selectedObject;
//    private final CsmContext editorContext;
    private CsmClass csmClassContainer;
    private final ChangeListener parent;
    private String classname;
    private boolean isUpperCase;
    private boolean hasOutOfClassMemberDefinitions = false;
    
    private static final String modifierNames[] = {
        "public", // NOI18N
        "protected", // NOI18N
        "<default>", // NOI18N
        "private" // NOI18N
    };
    
    private static final String[] columnNames = {
        getString("LBL_ColField"),  // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColGetter"), // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColSetter")  // NOI18N
    };
    
    // modifier items in combo - indexes
    private static final int MOD_PUBLIC_INDEX = 0;
    private static final int MOD_PROTECTED_INDEX = 1;
    private static final int MOD_DEFAULT_INDEX = 2;
    private static final int MOD_PRIVATE_INDEX = 3;

    private static final Class<?>[] columnTypes = new Class<?>[] {
        CsmField.class, java.lang.Boolean.class, AccessorInfo.class, java.lang.Boolean.class, AccessorInfo.class
    };
    
    /** 
     * Creates new form EncapsulateFieldPanel.
     *
     * @param selectedObjects  array of selected objects
     */
    public EncapsulateFieldPanel(CsmObject selectedObject, CsmContext editorContext, ChangeListener parent) {
        String title = getString("LBL_TitleEncapsulateFields");

        if (selectedObject == null) {
            this.selectedObject = Utilities.extractEnclosingClass(editorContext);
        } else {
            this.selectedObject = selectedObject;
        }
//        this.editorContext = editorContext;
        this.parent = parent;
        model = new TabM(columnNames, 0);
        initComponents();
        setName(title);
        jCheckAccess.setSelected(NbPreferences.forModule(DeclarationGenerator.class).getBoolean(EncapsulateFieldPanel.ALWAYS_USE_ACCESSORS_PROPERTY, false));
        jCheckAccess.setEnabled(false);
        jComboAccess.setSelectedIndex(NbPreferences.forModule(DeclarationGenerator.class).getInt(EncapsulateFieldPanel.METHOD_ACCESS_INDEX_PROPERTY, 0));
        jComboAccess.setEnabled(false);
        jComboField.setSelectedIndex(NbPreferences.forModule(DeclarationGenerator.class).getInt(EncapsulateFieldPanel.FIELD_ACCESS_INDEX_PROPERTY, 2));
        jComboField.setEnabled(false);
        // *** initialize table
        // set renderer for the column "Field" to display name of the feature (with icon)
        jTableFields.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        jTableFields.setDefaultRenderer(CsmField.class, new EncapsulateCsmFieldTableCellRenderer());
        jTableFields.setDefaultRenderer(AccessorInfo.class, new AccessorInfoRenderer());
        jTableFields.setDefaultEditor(AccessorInfo.class, new AccessorInfoTableEditor());
        // set background color of the scroll pane to be the same as the background
        // of the table
        jScrollField.setBackground(jTableFields.getBackground());
        jScrollField.getViewport().setBackground(jTableFields.getBackground());
        // set default row height
        jTableFields.setRowHeight(18);
        // set grid color to be consistent with other netbeans tables
        if (UIManager.getColor("control") != null) { // NOI18N
            jTableFields.setGridColor(UIManager.getColor("control")); // NOI18N
        }

        initEnumCombo(jComboSort, SortBy.DEFAULT);
        jComboSort.setEnabled(false);
        initEnumCombo(jComboJavadoc, Documentation.NONE);
        jComboJavadoc.setEnabled(false);
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private boolean initialized = false;
        
    @Override
    public final void initialize() {
        if (initialized) {
            return;
        }
        CsmObject selectedResolvedObject = CsmRefactoringUtils.getReferencedElement(selectedObject);
        int tableSelection = 0;
        for (CsmField field : initFields(selectedResolvedObject)) {
            boolean createGetter = field.equals(selectedResolvedObject);
            boolean createSetter = createGetter && !isConstant(field);
            String getName = GeneratorUtils.computeGetterName(field, isUpperCase);
            String setName = GeneratorUtils.computeSetterName(field, isUpperCase);
            model.addRow(new Object[] {
                MemberInfo.create(field),
                createGetter ? Boolean.TRUE : Boolean.FALSE,
                AccessorInfo.createGetter(field, getName),
                createSetter ? Boolean.TRUE : Boolean.FALSE,
                AccessorInfo.createSetter(field, setName),
            });
            if (createGetter) {
                tableSelection = model.getRowCount() - 1;
            }
        }

        packRows(jTableFields);

        setColumnWidth(1);
        setColumnWidth(3);

        jTableFields.changeSelection(tableSelection, 0, false, false);

        jTableFields.invalidate();
        jTableFields.repaint();
        model.addTableModelListener(new TableModelListener() {
            boolean isUpdating = false;
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdating) {
                    return;
                }
                int col = e.getColumn();
                int row = e.getFirstRow();
                if (col == 1 || col==3 ) {
                    Boolean value = (Boolean) model.getValueAt(row, col);
                    if (value) {
                        AccessorInfo ai = (AccessorInfo) model.getValueAt(row, col + 1);
                        if (ai != null) {
                            ai.reset();
                        }
                    }
                    try {
                        isUpdating = true;
                        model.fireTableCellUpdated(row, col + 1);
                    } finally {
                        isUpdating = false;
                    }
                } else {
                    AccessorInfo value = (AccessorInfo) model.getValueAt(row, col);
                    if (!isUpdating && (value == null || value.name == null || value.name.length() == 0)) {
                        try {
                            isUpdating = true;
                            model.setValueAt(Boolean.FALSE, row, col-1);
                        } finally {
                            isUpdating = false;
                        }
                    }
                }
                parent.stateChanged(null);
            }
        });

        initInsertPoints();
        
        initialized = true;
    }

    private void setColumnWidth(int a) {
        TableColumn col = jTableFields.getColumnModel().getColumn(a);
        JCheckBox box = new JCheckBox();
        int width = (int) box.getPreferredSize().getWidth();
        col.setPreferredWidth(width);
        col.setMinWidth(width);
        col.setMaxWidth(width);
        col.setResizable(false);        
    }
    
    private int getMinimumRowHeight(JTable table, int rowIndex) {
        int height = table.getRowHeight();
        
        for (int c=0; c<table.getColumnCount(); c++) {
            TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
            Component comp = table.prepareRenderer(renderer, rowIndex, c);
            int h = comp.getMinimumSize().height;
            height = Math.max(height, h);
        }
        return height;
    }
    
    private void packRows(JTable table) {
        int max = 0;
        int h;
        for (int r = 0; r < table.getRowCount(); r++) {
            h = getMinimumRowHeight(table, r);
            if (h > max) {
                max = h;
            }
        }
        table.setRowHeight(max);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, max));
    }
    
    /**
     * Returns table model with data provided by user.
     *
     * @return  data provided in table by user
     */
    protected DefaultTableModel getTableModel() {
        return model;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLblTitle = new javax.swing.JLabel();
        jButtonSelectAll = new javax.swing.JButton();
        jButtonSelectNone = new javax.swing.JButton();
        jButtonSelectGetters = new javax.swing.JButton();
        jButtonSelectSetters = new javax.swing.JButton();
        jLblInsertPoint = new javax.swing.JLabel();
        jComboInsertPoint = new javax.swing.JComboBox();
        jLblSort = new javax.swing.JLabel();
        jComboSort = new javax.swing.JComboBox();
        jLblJavadoc = new javax.swing.JLabel();
        jComboJavadoc = new javax.swing.JComboBox();
        jLblFieldVis = new javax.swing.JLabel();
        jComboField = new javax.swing.JComboBox();
        jLblAccessVis = new javax.swing.JLabel();
        jComboAccess = new javax.swing.JComboBox();
        jCheckAccess = new javax.swing.JCheckBox();
        jScrollField = new javax.swing.JScrollPane();
        jTableFields = new javax.swing.JTable();
        jInlineMethods = new javax.swing.JCheckBox();

        jLblTitle.setLabelFor(jTableFields);
        org.openide.awt.Mnemonics.setLocalizedText(jLblTitle, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_FieldList")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSelectAll, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectAll.text")); // NOI18N
        jButtonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSelectNone, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectNone.text")); // NOI18N
        jButtonSelectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectNoneActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSelectGetters, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectGetters.text")); // NOI18N
        jButtonSelectGetters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectGettersActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSelectSetters, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectSetters.text")); // NOI18N
        jButtonSelectSetters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectSettersActionPerformed(evt);
            }
        });

        jLblInsertPoint.setLabelFor(jComboInsertPoint);
        org.openide.awt.Mnemonics.setLocalizedText(jLblInsertPoint, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jLblInsertPoint.text")); // NOI18N

        jLblSort.setLabelFor(jComboSort);
        org.openide.awt.Mnemonics.setLocalizedText(jLblSort, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jLblSort.text")); // NOI18N

        jLblJavadoc.setLabelFor(jComboJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(jLblJavadoc, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jLblJavadoc.text")); // NOI18N

        jLblFieldVis.setLabelFor(jComboField);
        org.openide.awt.Mnemonics.setLocalizedText(jLblFieldVis, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_FieldVis")); // NOI18N

        jComboField.setModel(new javax.swing.DefaultComboBoxModel(modifierNames));

        jLblAccessVis.setLabelFor(jComboAccess);
        org.openide.awt.Mnemonics.setLocalizedText(jLblAccessVis, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_AccessVis")); // NOI18N

        jComboAccess.setModel(new javax.swing.DefaultComboBoxModel(modifierNames));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckAccess, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_AccessorsEven")); // NOI18N

        jTableFields.setModel(model);
        jTableFields.setCellSelectionEnabled(true);
        jTableFields.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableFields.getTableHeader().setReorderingAllowed(false);
        jScrollField.setViewportView(jTableFields);
        jTableFields.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_jTableFields")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jInlineMethods, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jInlineMethods.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckAccess)
                    .addComponent(jInlineMethods)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblAccessVis)
                            .addComponent(jLblFieldVis)
                            .addComponent(jLblInsertPoint)
                            .addComponent(jLblSort)
                            .addComponent(jLblJavadoc))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboInsertPoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboJavadoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboAccess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(125, 125, 125))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollField, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonSelectAll)
                            .addComponent(jButtonSelectNone)
                            .addComponent(jButtonSelectGetters)
                            .addComponent(jButtonSelectSetters))
                        .addGap(12, 12, 12))
                    .addComponent(jLblTitle))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonSelectAll, jButtonSelectGetters, jButtonSelectNone, jButtonSelectSetters});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jComboAccess, jComboField, jComboInsertPoint, jComboJavadoc, jComboSort});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectNone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectGetters)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectSetters))
                    .addComponent(jScrollField, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblInsertPoint)
                    .addComponent(jComboInsertPoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblSort)
                    .addComponent(jComboSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblJavadoc)
                    .addComponent(jComboJavadoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblFieldVis)
                    .addComponent(jComboField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblAccessVis)
                    .addComponent(jComboAccess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jInlineMethods)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckAccess)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jComboAccess, jComboField, jComboInsertPoint, jComboJavadoc, jComboSort});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonSelectAll, jButtonSelectGetters, jButtonSelectNone, jButtonSelectSetters});

        jButtonSelectAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectAll.acsd")); // NOI18N
        jButtonSelectNone.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectNone.acsd")); // NOI18N
        jButtonSelectGetters.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectGetters.acsd")); // NOI18N
        jButtonSelectSetters.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jButtonSelectSetters.acsd")); // NOI18N
        jComboInsertPoint.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jComboInsertPoint.acsd")); // NOI18N
        jComboSort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jComboSort.acsd")); // NOI18N
        jComboJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jComboJavadoc.acsd")); // NOI18N
        jComboField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_fieldModifiers")); // NOI18N
        jComboAccess.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_methodAcc")); // NOI18N
        jCheckAccess.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_useAccessors")); // NOI18N
        jInlineMethods.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_inlineMethods")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButtonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllActionPerformed
    makeSelection(true, 1, 3);
}//GEN-LAST:event_jButtonSelectAllActionPerformed

private void jButtonSelectNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectNoneActionPerformed
    makeSelection(false, 1, 3);
}//GEN-LAST:event_jButtonSelectNoneActionPerformed

private void jButtonSelectGettersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectGettersActionPerformed
    makeSelection(true, 1);
}//GEN-LAST:event_jButtonSelectGettersActionPerformed

private void jButtonSelectSettersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectSettersActionPerformed
    makeSelection(true, 3);
}//GEN-LAST:event_jButtonSelectSettersActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSelectAll;
    private javax.swing.JButton jButtonSelectGetters;
    private javax.swing.JButton jButtonSelectNone;
    private javax.swing.JButton jButtonSelectSetters;
    private javax.swing.JCheckBox jCheckAccess;
    private javax.swing.JComboBox jComboAccess;
    private javax.swing.JComboBox jComboField;
    private javax.swing.JComboBox jComboInsertPoint;
    private javax.swing.JComboBox jComboJavadoc;
    private javax.swing.JComboBox jComboSort;
    private javax.swing.JCheckBox jInlineMethods;
    private javax.swing.JLabel jLblAccessVis;
    private javax.swing.JLabel jLblFieldVis;
    private javax.swing.JLabel jLblInsertPoint;
    private javax.swing.JLabel jLblJavadoc;
    private javax.swing.JLabel jLblSort;
    private javax.swing.JLabel jLblTitle;
    private javax.swing.JScrollPane jScrollField;
    private javax.swing.JTable jTableFields;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(EncapsulateFieldPanel.class, key);
    }
    
    private static <E extends Enum<E> & Comparator<E>> void initEnumCombo(JComboBox combo, E defValue) {
        @SuppressWarnings("unchecked")
        List<E> enumList = new ArrayList<>(EnumSet.allOf(defValue.getClass()));
        Collections.sort(enumList, defValue);
        combo.setModel(new DefaultComboBoxModel(enumList.toArray()));
        combo.setSelectedItem(defValue);
    }
    
    private void makeSelection(boolean state, int... columns) {
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col : columns) {
                boolean value = (Boolean) model.getValueAt(row, col);
                if (state != value) {
                    model.setValueAt(state, row, col);
                }
            }
        }
    }

    /**
     * Returns the array of all fields from class which contains
     * selectedField provided as a parameter.
     *
     * @param   selectedField field, whose class is used for obtaining
     *                        array of fields.
     * @return  array of all fields in a class.
     */
    private List<CsmField> initFields(CsmObject selectedObject) {
        CsmObject selectedResolvedObject = CsmRefactoringUtils.getReferencedElement(selectedObject);
        assert selectedResolvedObject != null : "why unresolved element was used?";
        if (CsmKindUtilities.isClass(selectedResolvedObject)) {
            csmClassContainer = (CsmClass) selectedResolvedObject;
        } else {
            assert CsmKindUtilities.isField(selectedResolvedObject): "should be field";
            csmClassContainer = ((CsmField)selectedResolvedObject).getContainingClass();
        }
        List<CsmField> result = new ArrayList<>();
        Boolean anIsUpperCase = null;
        for (CsmMember member : csmClassContainer.getMembers()) {
            if (CsmKindUtilities.isField(member)) {
                result.add((CsmField) member);
            } else if (anIsUpperCase == null && CsmKindUtilities.isMethod(member)) {
                anIsUpperCase = GeneratorUtils.checkStartWithUpperCase((CsmMethod) member);
            }
        }
        this.isUpperCase = anIsUpperCase != null ? anIsUpperCase : true;
        this.classname = csmClassContainer.getQualifiedName().toString();
        final String title = " - " + classname; // NOI18N
        setName(getName() + title);

        return result;
    }
    
    private void initInsertPoints() {
        hasOutOfClassMemberDefinitions = InsertPoint.initInsertPoints(jComboInsertPoint, csmClassContainer);
        if (hasOutOfClassMemberDefinitions) {
            jInlineMethods.setSelected(NbPreferences.forModule(DeclarationGenerator.class).getBoolean(DeclarationGenerator.INLINE_PROPERTY, false));
            jInlineMethods.setEnabled(true);
        } else {
            jInlineMethods.setSelected(true);
            jInlineMethods.setEnabled(false);
        }
    }
    
    public final Collection<EncapsulateFieldInfo> getAllFields() {
        List<EncapsulateFieldInfo> result = new ArrayList<>();
        List<?> rows = model.getDataVector();
        for (Iterator<?> rowIt = rows.iterator(); rowIt.hasNext();) {
            List<?> row = (List<?>) rowIt.next();
            String getterName = (Boolean) row.get(1) ? ((AccessorInfo) row.get(2)).name : null;
            String setterName = (Boolean) row.get(3) ? ((AccessorInfo) row.get(4)).name : null;
            if (getterName != null || setterName != null) {
                // this item contains info about fields
                @SuppressWarnings("unchecked")
                MemberInfo<CsmField> mi = (MemberInfo<CsmField>) row.get(0);
                // getter
                CsmMethod defaultGetter = null;
                if (((AccessorInfo) row.get(2)).defaultAccessor != null) {
                    defaultGetter = (CsmMethod) ((AccessorInfo) row.get(2)).defaultAccessor.getElementHandle();
                }
                // setter
                CsmMethod defaultSetter = null;
                if (((AccessorInfo) row.get(4)).defaultAccessor != null) {
                    defaultSetter = (CsmMethod) ((AccessorInfo) row.get(4)).defaultAccessor.getElementHandle();
                }
                result.add(new EncapsulateFieldInfo(
                        mi.getElementHandle(),
                        "".equals(getterName)?null:getterName, // NOI18N
                        "".equals(setterName)?null:setterName,  // NOI18N
                        defaultGetter, defaultSetter));
            }
        }

        return result;
    }

    public boolean isMethodInline() {
        boolean inline = jInlineMethods.isSelected();
        if (hasOutOfClassMemberDefinitions) {
            NbPreferences.forModule(DeclarationGenerator.class).putBoolean(DeclarationGenerator.INLINE_PROPERTY, inline);
        }
        return inline;
    }

    public boolean isCheckAccess() {
        boolean res = jCheckAccess.isSelected();
        NbPreferences.forModule(DeclarationGenerator.class).putBoolean(EncapsulateFieldPanel.ALWAYS_USE_ACCESSORS_PROPERTY, res);
        return res;
    }
    
    public Set<CsmVisibility> getFieldModifiers() {
        int res = jComboField.getSelectedIndex();
        NbPreferences.forModule(DeclarationGenerator.class).putInt(EncapsulateFieldPanel.FIELD_ACCESS_INDEX_PROPERTY, res);
        CsmVisibility mod = getModifier(res);
        if (mod == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mod);
        }
    }
    
    public Set<CsmVisibility> getMethodModifiers() {
        int res = jComboAccess.getSelectedIndex();
        NbPreferences.forModule(DeclarationGenerator.class).putInt(EncapsulateFieldPanel.METHOD_ACCESS_INDEX_PROPERTY, res);
        CsmVisibility mod = getModifier(res);
        if (mod == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mod);
        }
    }

    private CsmVisibility getModifier(int index) {
        switch (index) {
            case MOD_PRIVATE_INDEX:
                return CsmVisibility.PRIVATE;
            case MOD_DEFAULT_INDEX:
                return CsmVisibility.NONE;
            case MOD_PROTECTED_INDEX:
                return CsmVisibility.PROTECTED;
            case MOD_PUBLIC_INDEX:
                return CsmVisibility.PUBLIC;
            default:
                throw new IllegalStateException("unexpected index:" + index); // NOI18N
        }
    }

    public InsertPoint getInsertPoint() {
        return (InsertPoint) jComboInsertPoint.getSelectedItem();
    }
    
    public SortBy getSortBy() {
        return (SortBy) jComboSort.getSelectedItem();
    }
    
    public Documentation getDocumentation() {
        return (Documentation) jComboJavadoc.getSelectedItem();
    }

    String getClassname() {
        return classname;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The class is used by EncapsulateFieldPanel - it represents table model
     * used inside in jTable. It denies to edit first column, returns the
     * column classes (Boolean, String, String, String) etc.
     */
    private static class TabM extends DefaultTableModel {
        
        public TabM(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }
        
        /**
         * Returns the appropriate class for column.
         *
         * @param  columnIndex  index of column for which we are looking for a class
         * @return  class which is used in the column
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        /**
         * We deny edit the field column (index 1), because field can't
         * be renamed when we encapsulate it.
         *
         * @param  row  doesn't matter
         * @param  column  for value 1, it returns false, otherwise true
         *
         * @return  true, if the cell is editable
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                return false;
            }
            if (column == 1 || column == 3) {
                return true;
            }
            return (Boolean) getValueAt(row, column - 1);
        }
    }
    
    private static final class AccessorInfo {
        String defaultName;
        MemberInfo<? extends CsmMember> defaultAccessor;
        String name;
        String accessorToolTip;
        String defaultAccessorToolTip;
        MemberInfo<? extends CsmMember> accessor;
        private CsmField fieldHandle;
        private boolean isGetter;

        public static AccessorInfo createGetter(CsmField field, String proposedName) {
            CsmMethod getter = EncapsulateFieldRefactoringPlugin.findMethod(field.getContainingClass(), proposedName, Collections.<CsmVariable>emptyList(), true);
            return create(field, getter, proposedName, true);
        }
        
        public static AccessorInfo createSetter(CsmField field, String proposedName) {
            CsmMethod setter = EncapsulateFieldRefactoringPlugin.findMethod(field.getContainingClass(), proposedName, Collections.singletonList(field), true);
            return create(field, setter, proposedName, false);
        }
        
        private static AccessorInfo create(CsmField field, CsmMethod method, String proposedName, boolean isGetter) {
            AccessorInfo ai = new AccessorInfo();
            ai.name = ai.defaultName = proposedName;
            ai.accessor = ai.defaultAccessor = (method != null) ? MemberInfo.create(method) : null;
            ai.accessorToolTip = ai.defaultAccessorToolTip = (method != null)
                    ? NbBundle.getMessage(
                            EncapsulateFieldPanel.class,
                            isGetter ? "MSG_EncapsulateFieldDeclaredGetter" : "MSG_EncapsulateFieldDeclaredSetter", // NOI18N
                            /*ElementHeaders.getHeader(method.getEnclosingElement(), javac, ElementHeaders.NAME))*/
                            method.getName().toString())
                    : null;
            ai.isGetter = isGetter;
            ai.fieldHandle = field;
            return ai;
        }
        
        public void reset() {
            name = defaultName;
            accessor = defaultAccessor;
            accessorToolTip = defaultAccessorToolTip;
        }
        
        public void setName(String s) {
            name = s;
            CsmField field = fieldHandle;
            CsmMethod method = null;
            method = isGetter
                    ? EncapsulateFieldRefactoringPlugin.findMethod(field.getContainingClass(), s, Collections.<CsmVariable>emptyList(), true)
                    : EncapsulateFieldRefactoringPlugin.findMethod(field.getContainingClass(), s, Collections.singletonList(field), true);
            accessor = method != null ? MemberInfo.create(method) : null;
            accessorToolTip = method != null
                    ? NbBundle.getMessage(
                            EncapsulateFieldPanel.class,
                            isGetter ? "MSG_EncapsulateFieldDeclaredGetter" : "MSG_EncapsulateFieldDeclaredSetter", // NOI18N
//                            ElementHeaders.getHeader(method.getContainingClass(), ElementHeaders.NAME))
                            method.getName().toString())
                    : null;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public enum SortBy implements Comparator<SortBy> {
        
        DEFAULT("EncapsulateFieldPanel.jComboSort.default"), // NOI18N
        PAIRS("EncapsulateFieldPanel.jComboSort.pairs"), // NOI18N
        ALPHABETICALLY("EncapsulateFieldPanel.jComboSort.alphabetically"), // NOI18N
        GETTERS_FIRST("EncapsulateFieldPanel.jComboSort.gettersFirst"); // NOI18N
        private final String displayName;

        private SortBy(String key) {
            this.displayName = getString(key);
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compare(SortBy o1, SortBy o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    
    public enum Documentation implements Comparator<Documentation> {
        
        DEFAULT("EncapsulateFieldPanel.jComboJavadoc.createDefault"), // NOI18N
        NONE("EncapsulateFieldPanel.jComboJavadoc.none"), // NOI18N
        COPY("EncapsulateFieldPanel.jComboJavadoc.copy"); // NOI18N
        
        private final String displayName;

        private Documentation(String key) {
            this.displayName = getString(key);
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compare(Documentation o1, Documentation o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    
    
    private static final class EncapsulateCsmFieldTableCellRenderer extends UIUtilities.CsmElementTableCellRenderer {

        @Override
        protected String extractText(Object value) {
            String s = super.extractText(value);
            if (s != null && isConstant(value)) {
                s = s + " : const"; // NOI18N
            }
            return s;
        }

    }

    private static boolean isConstant(Object value) {
        if (CsmKindUtilities.isCsmObject(value) && CsmKindUtilities.isVariable((CsmObject)value)) {
            return GeneratorUtils.isConstant((CsmVariable)value);
        } else {
            return false;
        }
    }
    
    private static final class AccessorInfoRenderer extends UIUtilities.CsmElementTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            AccessorInfo ai = (AccessorInfo) value;
            Object newValue = ai == null
                    ? null
                    : ai.accessor == null ? ai.name : ai.accessor;
            Component renderer = super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
            String toolTip = ai != null && table.isCellEditable(row, column)
                    ? ai.accessorToolTip
                    : null;
            boolean isEnabled = (Boolean) table.getModel().getValueAt(row, column - 1);
            setEnabled(isEnabled);
            if (isEnabled && ai != null && !ai.isGetter && isConstant(table.getValueAt(row, 0))) {
                Object o = UIManager.getDefaults().get("nb.errorForeground"); // NOI18N
                if (o instanceof Color) {
                    setBorder(BorderFactory.createLineBorder((Color) o));
                    String warning = NbBundle.getMessage(EncapsulateFieldPanel.class, "MSG_EncapsulateFieldFinalFieldWarning");
                    toolTip = toolTip == null ? warning : String.format("<html>%s<br>%s</html>", warning, toolTip); // NOI18N
                }
            }

            setToolTipText(toolTip);
            return renderer;
        }

    }
    
    private static final class AccessorInfoTableEditor extends DefaultCellEditor {

        private AccessorInfo ai;
        
        public AccessorInfoTableEditor() {
            super(new JTextField());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ai = (AccessorInfo) value;
            if (ai == null) {
                throw new IllegalStateException();
            }
            String cellEditorValue = ai.name;
            return super.getTableCellEditorComponent(table, cellEditorValue, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            String cellEditorValue = (String) super.getCellEditorValue();
            AccessorInfo retVal;
            if (cellEditorValue == null || cellEditorValue.length() == 0) {
                if (ai != null) {
                    ai.name = null;
                    ai.accessor = null;
                    ai.accessorToolTip = null;
                }
                retVal = ai;
            } else {
                if (!cellEditorValue.equals(ai.name)) {
                    computeNewValue();
                }
                retVal = ai;
            }
            return retVal;
        }
        
        private void computeNewValue() {
            AccessorInfo desc = ai;
            assert desc != null;
            desc.setName(((String) super.getCellEditorValue()).trim());
        }
        
    }
    // end INNER CLASSES
}
