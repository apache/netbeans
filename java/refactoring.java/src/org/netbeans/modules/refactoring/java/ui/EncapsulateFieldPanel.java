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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel used by Encapsulate Field refactoring. Contains components to
 * set parameters for the refactoring.
 *
 * @author  Pavel Flaska
 * @author  Jan Pokorsky
 */
public final class EncapsulateFieldPanel extends javax.swing.JPanel implements CustomRefactoringPanel {
    private static final String ENCAPSULATE_FIELDS_USE_ACCESSORS_PREF = "ENCAPSULATE_FIELDS_USEACCESSORS"; // true
    private static final String ENCAPSULATE_FIELDS_FIELD_ACCESS_PREF = "ENCAPSULATE_FIELDS_ACCESS"; // 3
    private static final String ENCAPSULATE_FIELDS_METHOD_ACCESS_PREF = "ENCAPSULATE_FIELDS_METHOD_ACCESS"; // 0
    private static final String ENCAPSULATE_FIELDS_JAVADOC_PREF = "ENCAPSULATE_FIELDS_JAVADOC";
    private static final String ENCAPSULATE_FIELDS_SORT_PREF = "ENCAPSULATE_FIELDS_SORT";
    private static final String PROPERTY_SUPPORT_PREF = "PROPERTY_SUPPORT_PREF";
    private static final String VETOABLE_SUPPORT_PREF = "VETOABLE_SUPPORT_PREF";
    private static final int DEFAULT_ROW_HEIGHT = 18;
    
    private DefaultTableModel model;
    private TreePathHandle selectedObjects;
    private Collection<TreePathHandle> fields;
    private int offset;
    private ChangeListener parent;
    private String classname;
    private static final String[] COLUMN_NAMES = {
        getString("LBL_ColField"),  // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColGetter"), // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColSetter")  // NOI18N
    };
    private static final Class[] COLUMN_TYPES = new Class[] {
        MemberInfo.class, java.lang.Boolean.class, AccessorInfo.class, java.lang.Boolean.class, AccessorInfo.class
    };
    
    /** 
     * Creates new form EncapsulateFieldPanel.
     *
     * @param selectedObjects  array of selected objects
     */
    public EncapsulateFieldPanel(TreePathHandle selectedObject, Collection<TreePathHandle> fields, int offset, ChangeListener parent) {
        String title = getString("LBL_TitleEncapsulateFields");
        
        this.selectedObjects = selectedObject;
        this.parent = parent;
        this.fields = fields;
        this.offset = offset;
        model = new TabM(COLUMN_NAMES, 0);
        initComponents();
        setName(title);
        // *** initialize table
        // set renderer for the column "Field" to display name of the feature (with icon)
        jTableFields.setDefaultRenderer(MemberInfo.class, new EncapsulateMemberInfoTableCellRenderer());
        jTableFields.setDefaultRenderer(AccessorInfo.class, new AccessorInfoRenderer());
        JavaSource js = JavaSource.forFileObject(selectedObjects.getFileObject());
        jTableFields.setDefaultEditor(AccessorInfo.class, new AccessorInfoTableEditor(js));
        // set background color of the scroll pane to be the same as the background
        // of the table
        jScrollField.setBackground(jTableFields.getBackground());
        jScrollField.getViewport().setBackground(jTableFields.getBackground());
        jTableFields.setRowHeight(DEFAULT_ROW_HEIGHT);
        jTableFields.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        // set grid color to be consistent with other netbeans tables
        if (UIManager.getColor("control") != null) { // NOI18N
            jTableFields.setGridColor(UIManager.getColor("control")); // NOI18N
        }

        initEnumCombo(jComboAccess, ModifierName.values()[RefactoringModule.getOption(ENCAPSULATE_FIELDS_METHOD_ACCESS_PREF, ModifierName.PUBLIC.ordinal())]);
        initEnumCombo(jComboField, ModifierName.values()[RefactoringModule.getOption(ENCAPSULATE_FIELDS_FIELD_ACCESS_PREF, ModifierName.PRIVATE.ordinal())]);
        initEnumCombo(jComboSort, SortBy.values()[RefactoringModule.getOption(ENCAPSULATE_FIELDS_SORT_PREF, SortBy.PAIRS.ordinal())]);
        initEnumCombo(jComboJavadoc, Javadoc.values()[RefactoringModule.getOption(ENCAPSULATE_FIELDS_JAVADOC_PREF, Javadoc.DEFAULT.ordinal())]);
        
        jCheckAccess.setSelected(RefactoringModule.getOption(ENCAPSULATE_FIELDS_USE_ACCESSORS_PREF, true));
        boundCheckBox.setSelected(RefactoringModule.getOption(PROPERTY_SUPPORT_PREF, false));
        vetoableCheckBox.setSelected(RefactoringModule.getOption(VETOABLE_SUPPORT_PREF, false));
    }

    @Override
    public boolean requestFocusInWindow() {
        jTableFields.requestFocusInWindow();
        return true;
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private boolean initialized = false;
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        
        JavaSource js = JavaSource.forFileObject(selectedObjects.getFileObject());
        if (js==null) {
            throw new NullPointerException("Cannot get JavaSource for " + selectedObjects.getFileObject().getPath());
        }
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController javac) throws IOException {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    initialize(javac);
                }
            } , true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
        
    public void initialize(CompilationController javac) {
        TreePath selectedPath = selectedObjects.resolve(javac);
        Element selectedElm = javac.getTrees().getElement(selectedPath);
        
        int tableSelection = 0;
        for (VariableElement field : initFields(selectedPath, javac)) {
            TreePath fieldTPath = javac.getTrees().getPath(field);
            boolean createGetter = fields !=null? fields.contains(TreePathHandle.create(field, javac)) : selectedElm == field ;
            boolean createSetter = createGetter && !field.getModifiers().contains(Modifier.FINAL);
            CodeStyle cs = RefactoringUtils.getCodeStyle(javac);
            boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
            String getName = CodeStyleUtils.computeGetterName(field.getSimpleName(), field.asType().getKind() == TypeKind.BOOLEAN, staticMod, cs);
            String setName = CodeStyleUtils.computeSetterName(field.getSimpleName(), staticMod, cs);
            MemberInfo<TreePathHandle> mi = MemberInfo.create(fieldTPath, javac);
            if (mi == null) {
                continue;
            }
            model.addRow(new Object[] { 
                mi,
                createGetter ? Boolean.TRUE : Boolean.FALSE,                        
                AccessorInfo.createGetter(javac, field, getName),
                createSetter ? Boolean.TRUE : Boolean.FALSE,                        
                AccessorInfo.createSetter(javac, field, setName),
            });
            if (createGetter) {
                tableSelection = model.getRowCount() - 1;
            }
        }
        
        packRows(jTableFields);

        int checkboxColumnIndex = 1;
        setColumnWidth(checkboxColumnIndex);
        checkboxColumnIndex = 3;
        setColumnWidth(3);
        
        jTableFields.changeSelection(tableSelection, 0, false, false);

        jTableFields.invalidate();
        jTableFields.repaint();
        model.addTableModelListener(new TableModelListener() {
            private boolean isUpdating = false;
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdating) {
                    return;
                }
                int col = e.getColumn();
                int row = e.getFirstRow();
                if (col == 1 || col==3 ) {
                    Boolean value = (Boolean) model.getValueAt(row, col);
                    if (value.booleanValue()) {
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
        
        initInsertPoints(selectedPath, javac);
        
        initialized = true;
    }
    
    private void setColumnWidth(int columnIndex) {
        TableColumn col = jTableFields.getColumnModel().getColumn(columnIndex);
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
        for (int r=0; r<table.getRowCount(); r++) {
            h = getMinimumRowHeight(table, r);
            if (h>max) {
                max=h;
            }
        }
        table.setRowHeight(max);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, max));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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
        boundCheckBox = new javax.swing.JCheckBox();
        vetoableCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));

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

        jComboInsertPoint.setPreferredSize(new java.awt.Dimension(308, 31));

        jLblSort.setLabelFor(jComboSort);
        org.openide.awt.Mnemonics.setLocalizedText(jLblSort, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jLblSort.text")); // NOI18N

        jLblJavadoc.setLabelFor(jComboJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(jLblJavadoc, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.jLblJavadoc.text")); // NOI18N

        jLblFieldVis.setLabelFor(jComboField);
        org.openide.awt.Mnemonics.setLocalizedText(jLblFieldVis, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_FieldVis")); // NOI18N

        jLblAccessVis.setLabelFor(jComboAccess);
        org.openide.awt.Mnemonics.setLocalizedText(jLblAccessVis, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_AccessVis")); // NOI18N

        jCheckAccess.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckAccess, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "LBL_AccessorsEven")); // NOI18N

        jTableFields.setAutoCreateRowSorter(true);
        jTableFields.setModel(model);
        jTableFields.setCellSelectionEnabled(true);
        jTableFields.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableFields.getTableHeader().setReorderingAllowed(false);
        jScrollField.setViewportView(jTableFields);
        jTableFields.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "ACSD_jTableFields")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(boundCheckBox, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.boundCheckBox.text")); // NOI18N
        boundCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(vetoableCheckBox, org.openide.util.NbBundle.getMessage(EncapsulateFieldPanel.class, "EncapsulateFieldPanel.vetoableCheckBox.text")); // NOI18N
        vetoableCheckBox.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblTitle)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(29, 29, 29)
                                        .addComponent(vetoableCheckBox))
                                    .addComponent(boundCheckBox)
                                    .addComponent(jCheckAccess))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLblAccessVis)
                                    .addComponent(jLblFieldVis)
                                    .addComponent(jLblInsertPoint)
                                    .addComponent(jLblSort)
                                    .addComponent(jLblJavadoc))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jComboField, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboJavadoc, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboSort, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboAccess, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboInsertPoint, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jScrollField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonSelectSetters)
                            .addComponent(jButtonSelectNone)
                            .addComponent(jButtonSelectAll)
                            .addComponent(jButtonSelectGetters))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonSelectAll, jButtonSelectGetters, jButtonSelectNone, jButtonSelectSetters});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLblAccessVis, jLblFieldVis, jLblInsertPoint, jLblJavadoc, jLblSort});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
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
                    .addComponent(jScrollField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addComponent(jCheckAccess)
                .addGap(11, 11, 11)
                .addComponent(boundCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vetoableCheckBox)
                .addContainerGap())
        );

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

    private void boundCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundCheckBoxActionPerformed
        vetoableCheckBox.setEnabled(boundCheckBox.isSelected());
    }//GEN-LAST:event_boundCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boundCheckBox;
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
    private javax.swing.JLabel jLblAccessVis;
    private javax.swing.JLabel jLblFieldVis;
    private javax.swing.JLabel jLblInsertPoint;
    private javax.swing.JLabel jLblJavadoc;
    private javax.swing.JLabel jLblSort;
    private javax.swing.JLabel jLblTitle;
    private javax.swing.JScrollPane jScrollField;
    private javax.swing.JTable jTableFields;
    private javax.swing.JCheckBox vetoableCheckBox;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(EncapsulateFieldPanel.class, key);
    }
    
    private static <E extends Enum<E> & Comparator<E>> void initEnumCombo(JComboBox combo, E defValue) {
        Vector<E> enumList = new Vector<E>(EnumSet.allOf(defValue.getDeclaringClass()));
        enumList.sort(defValue);
        combo.setModel(new DefaultComboBoxModel(enumList));
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
    private List<VariableElement> initFields(TreePath selectedField, CompilationInfo javac) {
        if (selectedField == null) {
            return Collections.emptyList();
        }
        Element elm = javac.getTrees().getElement(selectedField);
        TypeElement encloser = null;
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            encloser = (TypeElement) elm.getEnclosingElement();
        } else {
            encloser = (TypeElement) elm;
        }
        if (encloser == null) {
            return Collections.emptyList();
        }
        List<VariableElement> result = new ArrayList<VariableElement>();
        for (Element member : encloser.getEnclosedElements()) {
            if (ElementKind.FIELD == member.getKind()) {
                result.add((VariableElement) member);
            }
        }
        
        this.classname = encloser.getQualifiedName().toString();
        final String title = " - " + classname; // NOI18N
        setName(getName() + title);
        
        return result;
    }
    
    private void initInsertPoints(TreePath selectedField, CompilationInfo javac) {
        Element elm = javac.getTrees().getElement(selectedField);
        TypeElement encloser = null;
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            encloser = (TypeElement) elm.getEnclosingElement();
        } else {
            encloser = (TypeElement) elm;
        }
        if (encloser == null) {
            InsertPoint.DEFAULT.index = offset;
            return;
        }
        List<InsertPoint> result = new ArrayList<InsertPoint>();
        int idx = 0;
        TreePath encloserPath = javac.getTrees().getPath(encloser);
        ClassTree encloserTree = (ClassTree) encloserPath.getLeaf();
        for (Tree memberTree : encloserTree.getMembers()) {
            if (memberTree.getKind() == Tree.Kind.METHOD) {
                Element member = javac.getTrees().getElement(new TreePath(encloserPath, memberTree));
                if (member != null && !javac.getElementUtilities().isSynthetic(member)) {
                    InsertPoint ip = new InsertPoint(idx + 1, NbBundle.getMessage(
                            EncapsulateFieldPanel.class,
                            "MSG_EncapsulateFieldInsertPointMethod",
                            MemberInfo.create(member, javac).getHtmlText()
                            ));
                    result.add(ip);
                }
            }
            ++idx;
        }
        InsertPoint.DEFAULT.index = offset;
        jComboInsertPoint.addItem(InsertPoint.DEFAULT);
        if (!result.isEmpty()) {
            jComboInsertPoint.addItem(new InsertPoint(result.get(0).index - 1,
                    getString("EncapsulateFieldPanel.jComboInsertPoint.first"))); // NOI18N
            jComboInsertPoint.addItem(new InsertPoint(result.get(result.size() - 1).index,
                    getString("EncapsulateFieldPanel.jComboInsertPoint.last"))); // NOI18N
            for (InsertPoint ip : result) {
                jComboInsertPoint.addItem(ip);
            }
        }
        jComboInsertPoint.setSelectedItem(InsertPoint.DEFAULT);
    }
    
    public Collection<EncapsulateFieldInfo> getAllFields() {
        List<EncapsulateFieldInfo> result = new ArrayList<EncapsulateFieldInfo>();
        List rows = model.getDataVector();
        for (Iterator rowIt = rows.iterator(); rowIt.hasNext();) {
            List row = (List) rowIt.next();
            String getterName = (Boolean) row.get(1) ? ((AccessorInfo) row.get(2)).name : null;
            String setterName = (Boolean) row.get(3) ? ((AccessorInfo) row.get(4)).name : null;
            if (getterName != null || setterName != null) {
                MemberInfo mi = (MemberInfo) row.get(0);
                result.add(new EncapsulateFieldInfo(
                        (TreePathHandle) mi.getElementHandle(),
                        "".equals(getterName)?null:getterName, // NOI18N
                        "".equals(setterName)?null:setterName)); // NOI18N
            }
        }

        return result;
    }
    
    public boolean isCheckAccess() {
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_USE_ACCESSORS_PREF, jCheckAccess.isSelected());
        return jCheckAccess.isSelected();
    }
    
    public boolean isBound() {
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_SORT_PREF, boundCheckBox.isSelected());
        return boundCheckBox.isSelected();
    }
    
    public boolean isVetoable() {
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_SORT_PREF, vetoableCheckBox.isSelected());
        return vetoableCheckBox.isSelected();
    }
    
    public Set<Modifier> getFieldModifiers() {
        ModifierName n = (ModifierName) jComboField.getSelectedItem();
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_FIELD_ACCESS_PREF, n.ordinal());
        Modifier mod = getModifier(n);
        if (mod == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mod);
        }
    }
    
    public Set<Modifier> getMethodModifiers() {
        ModifierName n = (ModifierName) jComboAccess.getSelectedItem();
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_METHOD_ACCESS_PREF, n.ordinal());
        Modifier mod = getModifier(n); 
        if (mod == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mod);
        }
    }

    private Modifier getModifier(ModifierName modName) {
        switch (modName) {
            case PRIVATE: 
                return Modifier.PRIVATE;
            case DEFAULT: 
                return null; /* no modifier */
            case PROTECTED: 
                return Modifier.PROTECTED;
            case PUBLIC:
                return Modifier.PUBLIC;
        }
        throw new IllegalStateException("unknown ModifierName: " + modName); // NOI18N
    }
    
    public Integer getInsertPoint() {
        return ((InsertPoint) jComboInsertPoint.getSelectedItem()).getIndex();
    }
    
    public SortBy getSortBy() {
        SortBy sortby = (SortBy) jComboSort.getSelectedItem();
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_SORT_PREF, sortby.ordinal());
        return sortby;
    }
    
    public Javadoc getJavadoc() {
        Javadoc javadoc = (Javadoc) jComboJavadoc.getSelectedItem();
        RefactoringModule.setOption(ENCAPSULATE_FIELDS_JAVADOC_PREF, javadoc.ordinal());
        return javadoc;
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
        public Class getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
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
            return ((Boolean) getValueAt(row, column-1)).booleanValue();
        }
    }
    
    private static final class AccessorInfo {
        private String defaultName;
        private MemberInfo<ElementHandle<ExecutableElement>> defaultAccessor;
        private String name;
        private String accessorToolTip;
        private String defaultAccessorToolTip;
        private MemberInfo<ElementHandle<ExecutableElement>> accessor;
        private ElementHandle<VariableElement> fieldHandle;
        private boolean isGetter;

        public static AccessorInfo createGetter(CompilationInfo javac, VariableElement field, String proposedName) {
            ExecutableElement getter = EncapsulateFieldRefactoringPlugin.findMethod(javac, (TypeElement) field.getEnclosingElement(), proposedName, Collections.<VariableElement>emptyList(), true);
            return create(javac, field, getter, proposedName, true);
        }
        
        public static AccessorInfo createSetter(CompilationInfo javac, VariableElement field, String proposedName) {
            ExecutableElement setter = EncapsulateFieldRefactoringPlugin.findMethod(javac, (TypeElement) field.getEnclosingElement(), proposedName, Collections.singletonList(field), true);
            return create(javac, field, setter, proposedName, false);
        }
        
        private static AccessorInfo create(CompilationInfo javac, VariableElement field, ExecutableElement method, String proposedName, boolean isGetter) {
            AccessorInfo ai = new AccessorInfo();
            ai.name = ai.defaultName = proposedName;
            ai.accessor = ai.defaultAccessor = method != null ? MemberInfo.create(method, javac) : null;
            ai.accessorToolTip = ai.defaultAccessorToolTip = method != null
                    ? NbBundle.getMessage(
                            EncapsulateFieldPanel.class,
                            isGetter ? "MSG_EncapsulateFieldDeclaredGetter" : "MSG_EncapsulateFieldDeclaredSetter", // NOI18N
                            ElementHeaders.getHeader(method.getEnclosingElement(), javac, ElementHeaders.NAME))
                    : null;
            ai.isGetter = isGetter;
            ai.fieldHandle = ElementHandle.create(field);
            return ai;
        }
        
        public void reset() {
            name = defaultName;
            accessor = defaultAccessor;
            accessorToolTip = defaultAccessorToolTip;
        }
        
        public void setName(CompilationInfo javac, String s) {
            name = s;
            VariableElement field = fieldHandle.resolve(javac);
            ExecutableElement method = isGetter
                    ? EncapsulateFieldRefactoringPlugin.findMethod(javac, (TypeElement) field.getEnclosingElement(), s, Collections.<VariableElement>emptyList(), true)
                    : EncapsulateFieldRefactoringPlugin.findMethod(javac, (TypeElement) field.getEnclosingElement(), s, Collections.singletonList(field), true);
            accessor = method != null ? MemberInfo.create(method, javac) : null;
            accessorToolTip = method != null
                    ? NbBundle.getMessage(
                            EncapsulateFieldPanel.class,
                            isGetter ? "MSG_EncapsulateFieldDeclaredGetter" : "MSG_EncapsulateFieldDeclaredSetter", // NOI18N
                            ElementHeaders.getHeader(method.getEnclosingElement(), javac, ElementHeaders.NAME))
                    : null;
        }
    }
    
    public enum SortBy implements Comparator<SortBy> {
        
//        DEFAULT("EncapsulateFieldPanel.jComboSort.default"), // NOI18N
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
    
        
    private enum ModifierName implements Comparator<ModifierName> {
        PUBLIC("public"), // NOI18N
        PROTECTED("protected"), // NOI18N
        DEFAULT("<default>"), // NOI18N
        PRIVATE("private"); // NOI18N
        
        private final String displayName;

        ModifierName(final String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compare(ModifierName o1, ModifierName o2) {
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
    
    public enum Javadoc implements Comparator<Javadoc> {
        
        DEFAULT("EncapsulateFieldPanel.jComboJavadoc.createDefault"), // NOI18N
        NONE("EncapsulateFieldPanel.jComboJavadoc.none"), // NOI18N
        COPY("EncapsulateFieldPanel.jComboJavadoc.copy"); // NOI18N
        
        private final String displayName;

        private Javadoc(String key) {
            this.displayName = getString(key);
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compare(Javadoc o1, Javadoc o2) {
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
    
    private static final class InsertPoint {
        
        public static final InsertPoint DEFAULT = new InsertPoint(Integer.MIN_VALUE,
                getString("EncapsulateFieldPanel.jComboInsertPoint.default")); // NOI18N
        private int index;
        private String description;

        private InsertPoint(int index, String description) {
            this.index = index;
            this.description = description;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return description;
        }
        
    }
    
    private static final class EncapsulateMemberInfoTableCellRenderer extends UIUtilities.JavaElementTableCellRenderer {

        @Override
        protected String extractText(Object value) {
            String s = super.extractText(value);
            s = s != null && value instanceof MemberInfo && ((MemberInfo) value).getModifiers().contains(Modifier.FINAL)
                    ? s + " : final" // NOI18N
                    : s;
            return s;
        }
        
    }
    
    private static final class AccessorInfoRenderer extends UIUtilities.JavaElementTableCellRenderer {

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
            boolean isEnabled = (Boolean) table.getModel().getValueAt(
                    table.convertRowIndexToModel(row),
                    table.convertColumnIndexToModel(column) - 1);
            setEnabled(isEnabled);
            if (isEnabled && ai != null && !ai.isGetter
                    && ((MemberInfo) table.getValueAt(row, 0)).getModifiers().contains(Modifier.FINAL)) {
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
    
    private static final class AccessorInfoTableEditor extends DefaultCellEditor implements Task<CompilationController> {

        private AccessorInfo ai;
        private JavaSource js;
        
        AccessorInfoTableEditor(JavaSource js) {
            super(new JTextField());
            this.js = js;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ai = (AccessorInfo) value;
            String cellEditorValue = ai == null ? null: ai.name;
            return super.getTableCellEditorComponent(table, cellEditorValue, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            String cellEditorValue = (String) super.getCellEditorValue();
            Object retVal;
            if (cellEditorValue == null || cellEditorValue.length() == 0) {
                if (ai != null && ai.name != null && ai.name.length() > 0) {
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
            try {
                js.runUserActionTask(this, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void run(CompilationController javac) throws Exception {
            AccessorInfo desc = ai;
            if (desc == null) {
                return;
            }
            desc.setName(javac, ((String) super.getCellEditorValue()).trim());
        }
        
    }
    // end INNER CLASSES
}
