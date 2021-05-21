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
package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.editors2.JTableHeaderEditor;
import org.netbeans.modules.form.editors2.TableColumnModelEditor;
import org.netbeans.modules.form.editors2.TableModelEditor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Customizer for JTable.
 *
 * @author Jan Stola
 */
public class TableCustomizer extends JPanel implements Customizer, FormAwareEditor {
    private JTable table;

    private static final int ROW_MAX = 100;
    private static final int COLUMN_MAX = 100;
    
    /** Property editor for model from component section. */
    private RADConnectionPropertyEditor modelFromComponentEd;
    /** Property editor for custom code model section. */
    private RADConnectionPropertyEditor modelCustomEd;

    /** Customizer for model from component. */
    private Component modelFromComponentCustomizer;
    /** Customizer for custom code model. */
    private Component modelCustomCustomizer;
    /** Customizer for bound model. */
    private BindingCustomizer modelBoundCustomizer;

    /** Elements binding property. */
    private BindingProperty bindingProperty;   
    /** Model property. */
    private FormProperty modelProperty;
    /** TableHeader property. */
    private FormProperty headerProperty;
    /** ColumnModel property. */
    private RADProperty columnModelProperty;
    /** ColumnSelectionAllowed property. */
    private FormProperty columnSelectionAllowedProperty;

    /** Information about columns. */
    private List<ColumnInfo> columns;
    /** Table model for table with column information. */
    private CustomizerTableModel columnTableModel;
    /** Table model for table with row information. */
    private TableModelEditor.NbTableModel rowTableModel;

    /** Binding support for the corresponding form model. */
    private BindingDesignSupport bindingSupport;

    /**
     * Creates new <code>TableCustomizer</code>.
     */
    public TableCustomizer() {
        init();
    }

    private void init() {
        initComponents();
        initColumnTypeCombo();
        initSelectionTypeCombo();
        initWidthCombos();
        modelFromComponentEd = new RADConnectionPropertyEditor(TableModel.class, RADConnectionPropertyEditor.Type.FormConnection);
        modelCustomEd = new RADConnectionPropertyEditor(TableModel.class, RADConnectionPropertyEditor.Type.CustomCode);
        columnTableModel = new CustomizerTableModel();
        columnsTable.setModel(columnTableModel);
        columnsTable.getSelectionModel().addListSelectionListener(new ColumnSelectionListener());
        rowsTable.getSelectionModel().addListSelectionListener(new RowSelectionListener());
        titleListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (lastSelectedColumn != -1) {
                    columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn); 
                }
            }
        };
    }

    /**
     * Initializes column type combo box. 
     */
    private void initColumnTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Object")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_String")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Boolean")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Integer")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Byte")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Short")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Long")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Float")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_Type_Double")); // NOI18N
        columnTypeCombo.setModel(model);
        DefaultComboBoxModel editorModel = new DefaultComboBoxModel();
        for (int i=0; i<model.getSize(); i++) {
            editorModel.addElement(model.getElementAt(i));
        }
        typeCellEditor.setModel(editorModel);
    }

    /**
     * Initializes column selection type combo box. 
     */
    private void initSelectionTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_None")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Single")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Contiguous")); // NOI18N
        model.addElement(bundle.getString("LBL_TableCustomizer_SelectionType_Discontiguous")); // NOI18N
        selectionModelCombo.setModel(model);
    }

    /**
     * Initializes width combo boxes.
     */
    private void initWidthCombos() {
        String defaultWidth = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Width_Default"); // NOI18N
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthMinCombo.setModel(model);
        model =  new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthPrefCombo.setModel(model);
        model =  new DefaultComboBoxModel();
        model.addElement(defaultWidth);
        widthMaxCombo.setModel(model);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modelButtonGroup = new javax.swing.ButtonGroup();
        columnExpressionLabel = new javax.swing.JLabel();
        typeCellEditor = new javax.swing.JComboBox();
        tabbedPane = new javax.swing.JTabbedPane();
        modelTab = new javax.swing.JPanel();
        modelHardcodedChoice = new javax.swing.JRadioButton();
        modelBoundChoice = new javax.swing.JRadioButton();
        modelBoundPanel = new javax.swing.JPanel();
        modelFromComponentChoice = new javax.swing.JRadioButton();
        modelFromComponentPanel = new javax.swing.JPanel();
        modelCustomChoice = new javax.swing.JRadioButton();
        modelCustomPanel = new javax.swing.JPanel();
        columnsTab = new javax.swing.JPanel();
        columnsScrollPane = new javax.swing.JScrollPane();
        columnsTable = new javax.swing.JTable();
        columnCountLabel = new javax.swing.JLabel();
        insertColumnButton = new javax.swing.JButton();
        columnCountSpinner = new javax.swing.JSpinner();
        deleteColumnButton = new javax.swing.JButton();
        moveUpColumnButton = new javax.swing.JButton();
        moveDownColumnButton = new javax.swing.JButton();
        columnTitleLabel = new javax.swing.JLabel();
        columnTypeLabel = new javax.swing.JLabel();
        columnEditorLabel = new javax.swing.JLabel();
        columnRendererLabel = new javax.swing.JLabel();
        columnTitlePanel = new org.openide.explorer.propertysheet.PropertyPanel();
        columnTypeCombo = new javax.swing.JComboBox();
        columnEditorPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        columnRendererPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        separator = new javax.swing.JSeparator();
        selectionModelLabel = new javax.swing.JLabel();
        selectionModelCombo = new javax.swing.JComboBox();
        reorderingAllowedChoice = new javax.swing.JCheckBox();
        resizableColumnChoice = new javax.swing.JCheckBox();
        editableColumnChoice = new javax.swing.JCheckBox();
        widthPrefLabel = new javax.swing.JLabel();
        widthMinLabel = new javax.swing.JLabel();
        widthMaxLabel = new javax.swing.JLabel();
        widthPrefCombo = new javax.swing.JComboBox();
        widthMinCombo = new javax.swing.JComboBox();
        widthMaxCombo = new javax.swing.JComboBox();
        placeHolder1 = new javax.swing.JLabel();
        placeHolder1.setVisible(false);
        placeHolder2 = new javax.swing.JLabel();
        placeHolder2.setVisible(false);
        columnErrorLabel = new javax.swing.JLabel();
        rowsTab = new javax.swing.JPanel();
        rowsScrollPane = new javax.swing.JScrollPane();
        rowsTable = new javax.swing.JTable();
        rowCountLabel = new javax.swing.JLabel();
        rowCountSpinner = new javax.swing.JSpinner();
        insertRowButton = new javax.swing.JButton();
        deleteRowButton = new javax.swing.JButton();
        moveUpRowButton = new javax.swing.JButton();
        moveDownRowButton = new javax.swing.JButton();
        rowErrorLabel = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(columnExpressionLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Expression")); // NOI18N

        tabbedPane.addChangeListener(formListener);

        modelButtonGroup.add(modelHardcodedChoice);
        modelHardcodedChoice.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(modelHardcodedChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Hardcoded")); // NOI18N
        modelHardcodedChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelHardcodedChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelHardcodedChoice.addActionListener(formListener);

        modelButtonGroup.add(modelBoundChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelBoundChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Bound")); // NOI18N
        modelBoundChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelBoundChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelBoundChoice.addActionListener(formListener);

        javax.swing.GroupLayout modelBoundPanelLayout = new javax.swing.GroupLayout(modelBoundPanel);
        modelBoundPanel.setLayout(modelBoundPanelLayout);
        modelBoundPanelLayout.setHorizontalGroup(
            modelBoundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
        );
        modelBoundPanelLayout.setVerticalGroup(
            modelBoundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        modelButtonGroup.add(modelFromComponentChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelFromComponentChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_FromComponent")); // NOI18N
        modelFromComponentChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelFromComponentChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelFromComponentChoice.addActionListener(formListener);

        javax.swing.GroupLayout modelFromComponentPanelLayout = new javax.swing.GroupLayout(modelFromComponentPanel);
        modelFromComponentPanel.setLayout(modelFromComponentPanelLayout);
        modelFromComponentPanelLayout.setHorizontalGroup(
            modelFromComponentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
        );
        modelFromComponentPanelLayout.setVerticalGroup(
            modelFromComponentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        modelButtonGroup.add(modelCustomChoice);
        org.openide.awt.Mnemonics.setLocalizedText(modelCustomChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Custom")); // NOI18N
        modelCustomChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modelCustomChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modelCustomChoice.addActionListener(formListener);

        javax.swing.GroupLayout modelCustomPanelLayout = new javax.swing.GroupLayout(modelCustomPanel);
        modelCustomPanel.setLayout(modelCustomPanelLayout);
        modelCustomPanelLayout.setHorizontalGroup(
            modelCustomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
        );
        modelCustomPanelLayout.setVerticalGroup(
            modelCustomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout modelTabLayout = new javax.swing.GroupLayout(modelTab);
        modelTab.setLayout(modelTabLayout);
        modelTabLayout.setHorizontalGroup(
            modelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modelBoundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modelHardcodedChoice)
                    .addComponent(modelBoundChoice)
                    .addComponent(modelFromComponentChoice)
                    .addComponent(modelCustomChoice)
                    .addComponent(modelFromComponentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modelCustomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        modelTabLayout.setVerticalGroup(
            modelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modelHardcodedChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelBoundChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelBoundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelFromComponentChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelFromComponentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelCustomChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelCustomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        modelHardcodedChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Hardcoded_ACSD")); // NOI18N
        modelBoundChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Bound_ACSD")); // NOI18N
        modelFromComponentChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_FromComponent_ACSD")); // NOI18N
        modelCustomChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Model_Custom_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ModelTab"), modelTab); // NOI18N

        columnsScrollPane.setViewportView(columnsTable);
        columnsTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Table")); // NOI18N
        columnsTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Table_ACSD")); // NOI18N

        columnCountLabel.setLabelFor(columnCountSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(columnCountLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Count")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(insertColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Insert")); // NOI18N
        insertColumnButton.addActionListener(formListener);

        columnCountSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        columnCountSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(deleteColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Delete")); // NOI18N
        deleteColumnButton.setEnabled(false);
        deleteColumnButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveUp")); // NOI18N
        moveUpColumnButton.setEnabled(false);
        moveUpColumnButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveDownColumnButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveDown")); // NOI18N
        moveDownColumnButton.setEnabled(false);
        moveDownColumnButton.addActionListener(formListener);

        columnTitleLabel.setLabelFor(columnTitlePanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnTitleLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Title")); // NOI18N

        columnTypeLabel.setLabelFor(columnTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(columnTypeLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Type")); // NOI18N

        columnEditorLabel.setLabelFor(columnEditorPanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnEditorLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Editor")); // NOI18N

        columnRendererLabel.setLabelFor(columnRendererPanel);
        org.openide.awt.Mnemonics.setLocalizedText(columnRendererLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Renderer")); // NOI18N

        columnTitlePanel.setEnabled(false);

        javax.swing.GroupLayout columnTitlePanelLayout = new javax.swing.GroupLayout(columnTitlePanel);
        columnTitlePanel.setLayout(columnTitlePanelLayout);
        columnTitlePanelLayout.setHorizontalGroup(
            columnTitlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        columnTitlePanelLayout.setVerticalGroup(
            columnTitlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        columnTypeCombo.setEnabled(false);
        columnTypeCombo.addActionListener(formListener);

        columnEditorPanel.setEnabled(false);

        javax.swing.GroupLayout columnEditorPanelLayout = new javax.swing.GroupLayout(columnEditorPanel);
        columnEditorPanel.setLayout(columnEditorPanelLayout);
        columnEditorPanelLayout.setHorizontalGroup(
            columnEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        columnEditorPanelLayout.setVerticalGroup(
            columnEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        columnRendererPanel.setEnabled(false);

        javax.swing.GroupLayout columnRendererPanelLayout = new javax.swing.GroupLayout(columnRendererPanel);
        columnRendererPanel.setLayout(columnRendererPanelLayout);
        columnRendererPanelLayout.setHorizontalGroup(
            columnRendererPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        columnRendererPanelLayout.setVerticalGroup(
            columnRendererPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        selectionModelLabel.setLabelFor(selectionModelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(selectionModelLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_SelectionModel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reorderingAllowedChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ReorderingAllowed")); // NOI18N
        reorderingAllowedChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reorderingAllowedChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(resizableColumnChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Resizable")); // NOI18N
        resizableColumnChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        resizableColumnChoice.setEnabled(false);
        resizableColumnChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resizableColumnChoice.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(editableColumnChoice, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Editable")); // NOI18N
        editableColumnChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editableColumnChoice.setEnabled(false);
        editableColumnChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        editableColumnChoice.addActionListener(formListener);

        widthPrefLabel.setLabelFor(widthPrefCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthPrefLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Pref")); // NOI18N

        widthMinLabel.setLabelFor(widthMinCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthMinLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Min")); // NOI18N

        widthMaxLabel.setLabelFor(widthMaxCombo);
        org.openide.awt.Mnemonics.setLocalizedText(widthMaxLabel, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Max")); // NOI18N

        widthPrefCombo.setEditable(true);
        widthPrefCombo.setEnabled(false);

        widthMinCombo.setEditable(true);
        widthMinCombo.setEnabled(false);

        widthMaxCombo.setEditable(true);
        widthMaxCombo.setEnabled(false);

        columnErrorLabel.setForeground(new java.awt.Color(204, 0, 0));

        javax.swing.GroupLayout columnsTabLayout = new javax.swing.GroupLayout(columnsTab);
        columnsTab.setLayout(columnsTabLayout);
        columnsTabLayout.setHorizontalGroup(
            columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(columnsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(columnErrorLabel)
                    .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, columnsTabLayout.createSequentialGroup()
                        .addComponent(columnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(insertColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, columnsTabLayout.createSequentialGroup()
                                .addComponent(columnCountLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(columnCountSpinner))
                            .addComponent(deleteColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(moveUpColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(moveDownColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(columnsTabLayout.createSequentialGroup()
                        .addComponent(selectionModelLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectionModelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(reorderingAllowedChoice)
                    .addGroup(columnsTabLayout.createSequentialGroup()
                        .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(columnTypeLabel)
                            .addComponent(columnEditorLabel)
                            .addComponent(columnRendererLabel)
                            .addComponent(columnTitleLabel)
                            .addComponent(placeHolder1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(placeHolder2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(columnTitlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(columnRendererPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(columnEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(columnTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(columnsTabLayout.createSequentialGroup()
                                .addComponent(resizableColumnChoice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editableColumnChoice))
                            .addGroup(columnsTabLayout.createSequentialGroup()
                                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(widthPrefLabel)
                                    .addComponent(widthMinLabel)
                                    .addComponent(widthMaxLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(widthPrefCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(widthMinCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(widthMaxCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(45, 45, 45)))
                .addContainerGap())
        );
        columnsTabLayout.setVerticalGroup(
            columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(columnsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(columnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addGroup(columnsTabLayout.createSequentialGroup()
                        .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(columnCountLabel)
                            .addComponent(columnCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertColumnButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteColumnButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveUpColumnButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownColumnButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(columnTitlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(resizableColumnChoice)
                        .addComponent(editableColumnChoice)
                        .addComponent(columnTitleLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(columnTypeLabel)
                    .addComponent(widthPrefLabel)
                    .addComponent(widthPrefCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(columnTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(columnEditorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(widthMinLabel)
                        .addComponent(columnEditorLabel)
                        .addComponent(widthMinCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(columnRendererPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(widthMaxLabel)
                        .addComponent(columnRendererLabel)
                        .addComponent(widthMaxCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(placeHolder1)
                    .addComponent(placeHolder2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(columnsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectionModelLabel)
                    .addComponent(selectionModelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reorderingAllowedChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(columnErrorLabel)
                .addContainerGap())
        );

        columnCountLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Count_ACSD")); // NOI18N
        insertColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Insert_ACSD")); // NOI18N
        columnCountSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Count_ACSD")); // NOI18N
        deleteColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_Delete_ACSD")); // NOI18N
        moveUpColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveUp_ACSD")); // NOI18N
        moveDownColumnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Columns_MoveDown_ACSD")); // NOI18N
        columnTitleLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Title_ACSD")); // NOI18N
        columnTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Type_ACSD")); // NOI18N
        columnEditorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Editor_ACSD")); // NOI18N
        columnRendererLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Renderer_ACSD")); // NOI18N
        columnTitlePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Title_ACSD")); // NOI18N
        columnTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Type_ACSD")); // NOI18N
        columnEditorPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Editor_ACSD")); // NOI18N
        columnRendererPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Renderer_ACSD")); // NOI18N
        selectionModelLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_SelectionModel_ACSD")); // NOI18N
        selectionModelCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_SelectionModel_ACSD")); // NOI18N
        reorderingAllowedChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ReorderingAllowed_ACSD")); // NOI18N
        resizableColumnChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Resizable_ACSD")); // NOI18N
        editableColumnChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Editable_ACSD")); // NOI18N
        widthPrefLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Pref_ACSD")); // NOI18N
        widthMinLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Min_ACSD")); // NOI18N
        widthMaxLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Max_ACSD")); // NOI18N
        widthPrefCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Min_ACSD")); // NOI18N
        widthMinCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Pref_ACSD")); // NOI18N
        widthMaxCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Width_Max_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ColumnsTab"), columnsTab); // NOI18N

        rowsScrollPane.setViewportView(rowsTable);
        rowsTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Table")); // NOI18N
        rowsTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Table_ACSD")); // NOI18N

        rowCountLabel.setLabelFor(rowCountSpinner);
        rowCountLabel.setText(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Count")); // NOI18N

        rowCountSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rowCountSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(insertRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Insert")); // NOI18N
        insertRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(deleteRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Delete")); // NOI18N
        deleteRowButton.setEnabled(false);
        deleteRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveUp")); // NOI18N
        moveUpRowButton.setEnabled(false);
        moveUpRowButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(moveDownRowButton, org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveDown")); // NOI18N
        moveDownRowButton.setEnabled(false);
        moveDownRowButton.addActionListener(formListener);

        rowErrorLabel.setForeground(new java.awt.Color(204, 0, 0));

        javax.swing.GroupLayout rowsTabLayout = new javax.swing.GroupLayout(rowsTab);
        rowsTab.setLayout(rowsTabLayout);
        rowsTabLayout.setHorizontalGroup(
            rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rowsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rowErrorLabel)
                    .addGroup(rowsTabLayout.createSequentialGroup()
                        .addComponent(rowsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(rowsTabLayout.createSequentialGroup()
                                .addComponent(rowCountLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rowCountSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                            .addComponent(insertRowButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(deleteRowButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(moveUpRowButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(moveDownRowButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        rowsTabLayout.setVerticalGroup(
            rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rowsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rowsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addGroup(rowsTabLayout.createSequentialGroup()
                        .addGroup(rowsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rowCountLabel)
                            .addComponent(rowCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertRowButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteRowButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveUpRowButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownRowButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rowErrorLabel)
                .addContainerGap())
        );

        rowCountLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Count_ACSD")); // NOI18N
        rowCountSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Count_ACSD")); // NOI18N
        insertRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Insert_ACSD")); // NOI18N
        deleteRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_Delete_ACSD")); // NOI18N
        moveUpRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveUp_ACSD")); // NOI18N
        moveDownRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Rows_MoveDown_ACSD")); // NOI18N

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_RowsTab"), rowsTab); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
        );

        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ACSN")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_ACSD")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, javax.swing.event.ChangeListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == modelHardcodedChoice) {
                TableCustomizer.this.modelHardcodedChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelBoundChoice) {
                TableCustomizer.this.modelBoundChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelFromComponentChoice) {
                TableCustomizer.this.modelFromComponentChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == modelCustomChoice) {
                TableCustomizer.this.modelCustomChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == insertColumnButton) {
                TableCustomizer.this.insertColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == deleteColumnButton) {
                TableCustomizer.this.deleteColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveUpColumnButton) {
                TableCustomizer.this.moveUpColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveDownColumnButton) {
                TableCustomizer.this.moveDownColumnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == columnTypeCombo) {
                TableCustomizer.this.columnTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == resizableColumnChoice) {
                TableCustomizer.this.resizableColumnChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == editableColumnChoice) {
                TableCustomizer.this.editableColumnChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == insertRowButton) {
                TableCustomizer.this.insertRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == deleteRowButton) {
                TableCustomizer.this.deleteRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveUpRowButton) {
                TableCustomizer.this.moveUpRowButtonActionPerformed(evt);
            }
            else if (evt.getSource() == moveDownRowButton) {
                TableCustomizer.this.moveDownRowButtonActionPerformed(evt);
            }
        }

        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            if (evt.getSource() == tabbedPane) {
                TableCustomizer.this.tabbedPaneStateChanged(evt);
            }
            else if (evt.getSource() == columnCountSpinner) {
                TableCustomizer.this.columnCountSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == rowCountSpinner) {
                TableCustomizer.this.rowCountSpinnerStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void columnTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnTypeComboActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.setType(columnTypeCombo.getSelectedIndex());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
    }//GEN-LAST:event_columnTypeComboActionPerformed

    private void editableColumnChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editableColumnChoiceActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.setEditable(editableColumnChoice.isSelected());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
    }//GEN-LAST:event_editableColumnChoiceActionPerformed

    private void resizableColumnChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizableColumnChoiceActionPerformed
        ColumnInfo info = columns.get(lastSelectedColumn);
        info.getColumn().setResizable(resizableColumnChoice.isSelected());
        columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
        updateWidthCombos();
    }//GEN-LAST:event_resizableColumnChoiceActionPerformed

    private void moveDownRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownRowButtonActionPerformed
        stopCellEditing(rowsTable);
        int index = rowsTable.getSelectedRow();
        rowTableModel.moveRow(index, index+1);
        rowsTable.getSelectionModel().setSelectionInterval(index+1, index+1);
        ensureRowVisible(rowsTable, index+1);
    }//GEN-LAST:event_moveDownRowButtonActionPerformed

    private void moveUpRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpRowButtonActionPerformed
        stopCellEditing(rowsTable);
        int index = rowsTable.getSelectedRow();
        rowTableModel.moveRow(index, index-1);
        rowsTable.getSelectionModel().setSelectionInterval(index-1, index-1);
        ensureRowVisible(rowsTable, index-1);
    }//GEN-LAST:event_moveUpRowButtonActionPerformed

    private void deleteRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteRowButtonActionPerformed
        stopCellEditing(rowsTable);
        if (checkRowCount(rowTableModel.getRowCount()-rowsTable.getSelectedRowCount())) {
            int[] index = rowsTable.getSelectedRows();
            for (int i=index.length-1; i>=0; i--) {
                rowTableModel.removeRow(index[i]);
            }
            rowCountSpinner.setValue(rowTableModel.getRowCount());
        }
    }//GEN-LAST:event_deleteRowButtonActionPerformed

    private void insertRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertRowButtonActionPerformed
        stopCellEditing(rowsTable);
        if (checkRowCount(rowTableModel.getRowCount()+1)) {
            rowTableModel.addRow(rowTableModel.getRowCount());
            rowCountSpinner.setValue(rowTableModel.getRowCount());
        }
    }//GEN-LAST:event_insertRowButtonActionPerformed

    private void rowCountSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rowCountSpinnerStateChanged
        stopCellEditing(rowsTable);
        int rowNo = ((Integer)rowCountSpinner.getValue()).intValue();
        ensureRowCount(rowNo);
    }//GEN-LAST:event_rowCountSpinnerStateChanged

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        if (tabbedPane.getSelectedIndex() == 2) {
            updateColumnSection();
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void columnCountSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_columnCountSpinnerStateChanged
        stopCellEditing(columnsTable);
        int columnNo = ((Integer)columnCountSpinner.getValue()).intValue();
        ensureColumnCount(columnNo);
    }//GEN-LAST:event_columnCountSpinnerStateChanged

    private void moveDownColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownColumnButtonActionPerformed
        stopCellEditing(columnsTable);
        int index = columnsTable.getSelectedRow();
        columnsTable.clearSelection();
        ColumnInfo column = columns.remove(index);
        columns.add(index+1, column);
        if (modelHardcodedChoice.isSelected()) {
            rowTableModel.moveColumn(index, index+1);
        }
        columnsTable.getSelectionModel().setSelectionInterval(index+1, index+1);
        ensureRowVisible(columnsTable, index+1);
    }//GEN-LAST:event_moveDownColumnButtonActionPerformed

    private void moveUpColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpColumnButtonActionPerformed
        stopCellEditing(columnsTable);
        int index = columnsTable.getSelectedRow();
        columnsTable.clearSelection();
        ColumnInfo column = columns.remove(index);
        columns.add(index-1, column);
        if (modelHardcodedChoice.isSelected()) {
            rowTableModel.moveColumn(index, index-1);
        }
        columnsTable.getSelectionModel().setSelectionInterval(index-1, index-1);
        ensureRowVisible(columnsTable, index-1);
    }//GEN-LAST:event_moveUpColumnButtonActionPerformed

    private void deleteColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteColumnButtonActionPerformed
        stopCellEditing(columnsTable);
        if (checkColumnCount(columns.size()-columnsTable.getSelectedRowCount())) {
            boolean hardcoded = modelHardcodedChoice.isSelected();
            int[] index = columnsTable.getSelectedRows();
            for (int i=index.length-1; i>=0; i--) {
                lastSelectedColumn = -1;
                columns.remove(index[i]);
                if (hardcoded) {
                    rowTableModel.removeColumn(index[i]);
                }
                columnTableModel.fireTableRowsDeleted(index[i], index[i]);
            }
            columnCountSpinner.setValue(columns.size());
        }
    }//GEN-LAST:event_deleteColumnButtonActionPerformed

    private void insertColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertColumnButtonActionPerformed
        stopCellEditing(columnsTable);
        if (checkColumnCount(columns.size()+1)) {
            columns.add(new ColumnInfo(columnModelProperty, getFreeColumnIndex()));
            if (modelHardcodedChoice.isSelected()) {
                rowTableModel.addColumn(rowTableModel.getColumnCount());
            }
            int size = columns.size();
            columnTableModel.fireTableRowsInserted(size-1, size-1);
            columnCountSpinner.setValue(columns.size());
        }
    }//GEN-LAST:event_insertColumnButtonActionPerformed

    private void modelCustomChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelCustomChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelCustomChoiceActionPerformed

    private void modelFromComponentChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelFromComponentChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelFromComponentChoiceActionPerformed

    private void modelBoundChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelBoundChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelBoundChoiceActionPerformed

    private void modelHardcodedChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelHardcodedChoiceActionPerformed
        updateModelCustomizers();
    }//GEN-LAST:event_modelHardcodedChoiceActionPerformed

    private void ensureRowVisible(JTable table, int row) {
        Rectangle visRect = table.getVisibleRect();
        Rectangle cellRect = table.getCellRect(row, 0, false);
        table.scrollRectToVisible(new Rectangle(visRect.x, cellRect.y, visRect.width, cellRect.height));
    }

    private static void stopCellEditing(JTable table) {
        TableCellEditor editor = table.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
    }

    private int getFreeColumnIndex() {
        Set<Integer> set = new HashSet<Integer>();
        for (ColumnInfo info : columns) {
            set.add(info.getColumn().getIndex());
        }
        int index = 0;
        while (set.contains(index)) index++;
        return index;
    }

    private int lastSelectedCustomizer = -1;
    private void updateModelCustomizers() {
        boolean userCode = modelCustomChoice.isSelected();
        boolean fromComponent = modelFromComponentChoice.isSelected();
        boolean hardcoded = modelHardcodedChoice.isSelected();
        boolean bound = modelBoundChoice.isSelected();
        if (modelBoundCustomizer != null) {
            modelBoundCustomizer.getBindingPanel().setVisible(bound);
        }
        modelFromComponentCustomizer.setVisible(fromComponent);
        modelCustomCustomizer.setVisible(userCode);
        tabbedPane.setEnabledAt(2, hardcoded);
        if (bound) {
            checkBindingType();
        } else {
            tabbedPane.setEnabledAt(1, true);
        }
        if (fromComponent) {
            checkModelFromComponent();
            if (columns.isEmpty()) userCode = true; // Hide content of columns section
        }
        columnsScrollPane.setVisible(!userCode);
        columnCountLabel.setVisible(!userCode && !fromComponent);
        columnCountSpinner.setVisible(!userCode && !fromComponent);
        insertColumnButton.setVisible(!userCode && !fromComponent);
        deleteColumnButton.setVisible(!userCode  && !fromComponent);
        moveUpColumnButton.setVisible(!userCode && !fromComponent);
        moveDownColumnButton.setVisible(!userCode && !fromComponent);
        columnTitleLabel.setVisible(!userCode);
        columnTypeLabel.setVisible(hardcoded || bound);
        columnEditorLabel.setVisible(!userCode);
        columnRendererLabel.setVisible(!userCode);
        columnTitlePanel.setVisible(!userCode);
        columnEditorPanel.setVisible(!userCode);
        columnRendererPanel.setVisible(!userCode);
        columnTypeCombo.setVisible(hardcoded || bound);
        columnTypeCombo.setEditable(bound);
        resizableColumnChoice.setVisible(!userCode);
        editableColumnChoice.setVisible(hardcoded || bound);
        separator.setVisible(!userCode);
        widthMinLabel.setVisible(!userCode);
        widthMinCombo.setVisible(!userCode);
        widthPrefLabel.setVisible(!userCode);
        widthPrefCombo.setVisible(!userCode);
        widthMaxLabel.setVisible(!userCode);
        widthMaxCombo.setVisible(!userCode);
        if (expressionCombo != null) {
            expressionCombo.setVisible(bound);
        }
        columnExpressionLabel.setVisible(bound);
        boolean switch1 = bound != (columnExpressionLabel.getParent() != null);
        boolean switch2 = fromComponent != (dummyLabel1.getParent() != null);
        if (switch1) {
            if (switch2) {
                if (bound) {
                    switchHelper2();
                    switchHelper1();
                } else {
                    switchHelper1();
                    switchHelper2();
                }
            } else {
                switchHelper1();
            }
        } else if (switch2) {
            switchHelper2();
        }
        if ((lastSelectedCustomizer != -1)
            && (((lastSelectedCustomizer != 0) && hardcoded)
                || ((lastSelectedCustomizer != 1) && bound))) {
            ensureColumnCount(0);
            ensureRowCount(0);
            rowCountSpinner.setValue(0);
            // Clear rowTableModel
            for (int i=rowTableModel.getColumnCount()-1; i>=0; i--) {
                rowTableModel.removeColumn(i);
            }
        }
        columnCountSpinner.setValue(columns.size());
        lastSelectedCustomizer = (hardcoded ? 0 : (bound ? 1 : (fromComponent ? 2 : 3)));
        columnTableModel.setModelType(lastSelectedCustomizer);
    }

    private void switchHelper1() {
        GroupLayout layout = (GroupLayout)columnsTab.getLayout();
        if (modelBoundChoice.isSelected()) {
            layout.replace(columnTypeLabel, columnExpressionLabel);
            layout.replace(columnTypeCombo, expressionCombo);
            layout.replace(placeHolder1, columnTypeLabel);
            layout.replace(placeHolder2, columnTypeCombo);
        } else {
            layout.replace(columnTypeCombo, placeHolder2);
            layout.replace(columnTypeLabel, placeHolder1);
            layout.replace(columnExpressionLabel, columnTypeLabel);
            layout.replace(expressionCombo, columnTypeCombo);                
        }        
    }

    private void switchHelper2() {
        GroupLayout layout = (GroupLayout)columnsTab.getLayout();
        if (modelFromComponentChoice.isSelected()) {
            layout.replace(columnRendererLabel, dummyLabel1);
            layout.replace(columnRendererPanel, dummyLabel2);
            layout.replace(columnEditorLabel, columnRendererLabel);
            layout.replace(columnEditorPanel, columnRendererPanel);
            layout.replace(columnTypeLabel, columnEditorLabel);
            layout.replace(columnTypeCombo, columnEditorPanel);
        } else {
            layout.replace(columnEditorPanel, columnTypeCombo);
            layout.replace(columnEditorLabel, columnTypeLabel);
            layout.replace(columnRendererPanel, columnEditorPanel);
            layout.replace(columnRendererLabel, columnEditorLabel);
            layout.replace(dummyLabel2, columnRendererPanel);
            layout.replace(dummyLabel1, columnRendererLabel);
        }
    }

    private void updateModel(PropertyEditor propEd) {
        try {
            Object value = propEd.getValue();
            if (value == null) {
                modelProperty.restoreDefaultValue();
            } else {
                modelProperty.setValue(new FormProperty.ValueWithEditor(value, propEd));
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private ComboBoxWithTree expressionCombo;
    private JLabel dummyLabel1 = new JLabel();
    private JLabel dummyLabel2 = new JLabel();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnCountLabel;
    private javax.swing.JSpinner columnCountSpinner;
    private javax.swing.JLabel columnEditorLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnEditorPanel;
    private javax.swing.JLabel columnErrorLabel;
    private javax.swing.JLabel columnExpressionLabel;
    private javax.swing.JLabel columnRendererLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnRendererPanel;
    private javax.swing.JLabel columnTitleLabel;
    private org.openide.explorer.propertysheet.PropertyPanel columnTitlePanel;
    private javax.swing.JComboBox columnTypeCombo;
    private javax.swing.JLabel columnTypeLabel;
    private javax.swing.JScrollPane columnsScrollPane;
    private javax.swing.JPanel columnsTab;
    private javax.swing.JTable columnsTable;
    private javax.swing.JButton deleteColumnButton;
    private javax.swing.JButton deleteRowButton;
    private javax.swing.JCheckBox editableColumnChoice;
    private javax.swing.JButton insertColumnButton;
    private javax.swing.JButton insertRowButton;
    private javax.swing.JRadioButton modelBoundChoice;
    private javax.swing.JPanel modelBoundPanel;
    private javax.swing.ButtonGroup modelButtonGroup;
    private javax.swing.JRadioButton modelCustomChoice;
    private javax.swing.JPanel modelCustomPanel;
    private javax.swing.JRadioButton modelFromComponentChoice;
    private javax.swing.JPanel modelFromComponentPanel;
    private javax.swing.JRadioButton modelHardcodedChoice;
    private javax.swing.JPanel modelTab;
    private javax.swing.JButton moveDownColumnButton;
    private javax.swing.JButton moveDownRowButton;
    private javax.swing.JButton moveUpColumnButton;
    private javax.swing.JButton moveUpRowButton;
    private javax.swing.JLabel placeHolder1;
    private javax.swing.JLabel placeHolder2;
    private javax.swing.JCheckBox reorderingAllowedChoice;
    private javax.swing.JCheckBox resizableColumnChoice;
    private javax.swing.JLabel rowCountLabel;
    private javax.swing.JSpinner rowCountSpinner;
    private javax.swing.JLabel rowErrorLabel;
    private javax.swing.JScrollPane rowsScrollPane;
    private javax.swing.JPanel rowsTab;
    private javax.swing.JTable rowsTable;
    private javax.swing.JComboBox selectionModelCombo;
    private javax.swing.JLabel selectionModelLabel;
    private javax.swing.JSeparator separator;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox typeCellEditor;
    private javax.swing.JComboBox widthMaxCombo;
    private javax.swing.JLabel widthMaxLabel;
    private javax.swing.JComboBox widthMinCombo;
    private javax.swing.JLabel widthMinLabel;
    private javax.swing.JComboBox widthPrefCombo;
    private javax.swing.JLabel widthPrefLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    updateFromUI();
                }
            });
        }
    }

    private void updateFromUI() {
        if (columnsTable.isEditing()) {
            columnsTable.getCellEditor().stopCellEditing();
        }
        updateColumnSection();
        if (!modelBoundChoice.isSelected() && (bindingProperty != null) && !bindingProperty.isDefaultValue()) {
            bindingProperty.restoreDefaultValue();
        }
        if (modelFromComponentChoice.isSelected()) {
            updateModel(modelFromComponentEd);
        } else if (modelCustomChoice.isSelected()) {
            updateModel(modelCustomEd);
        } else if (modelBoundChoice.isSelected()) {
            if (modelProperty.isChanged()) {
                try {
                    modelProperty.restoreDefaultValue();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
            modelBoundCustomizer.getBindingFromUI();
            MetaBinding binding = modelBoundCustomizer.getBinding();
            if (binding != null) {
                if (binding.hasSubBindings()) {
                    binding.clearSubBindings();
                }
                if (tabbedPane.isEnabledAt(1)) {
                    for (ColumnInfo info : columns) {
                        String expression = info.getExpression();
                        MetaBinding subBinding = binding.addSubBinding(expression, null);
                        FormProperty titleProp = info.getColumn().getTitle();
                        Object value = null;
                        try {
                            value = titleProp.getValue();
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                        }
                        if (value instanceof String) {
                            try {
                                subBinding.setParameter(MetaBinding.NAME_PARAMETER, (String)value);
                                titleProp.restoreDefaultValue();
                            } catch (Exception ex) {
                                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                            }
                        } else {
                            subBinding.setParameter(MetaBinding.NAME_PARAMETER, null);
                        }
                        String clazz = info.getClazz();
                        if ((clazz != null) && (!clazz.equals("Object"))) { // NOI18N
                            subBinding.setParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER, clazz + ".class"); // NOI18N
                        }
                        if (!info.isEditable()) {
                            subBinding.setParameter(MetaBinding.EDITABLE_PARAMETER, "false"); // NOI18N
                        }
                    }
                }
            }
            bindingProperty.setValue(binding);
        } else if (modelHardcodedChoice.isSelected()) {
            int count = columns.size();
            for (int i=0; i<count; i++) {
                ColumnInfo info = columns.get(i);
                FormProperty prop = info.getColumn().getTitle();
                Object value = null;
                try {
                    value = prop.getValue();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
                if (value instanceof String) {
                    try {
                        prop.restoreDefaultValue();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            }
            try {
                modelProperty.setValue(new FormProperty.ValueWithEditor(rowTableModel, new TableModelEditor()));
            } catch (Exception ex) {
                String message = NbBundle.getMessage(getClass(), "MSG_TableCustomizer_ModelPropertyError"); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }

        // columnModel
        TableColumnModelEditor.FormTableColumnModel model = new TableColumnModelEditor.FormTableColumnModel(columnModelProperty);
        if (!modelCustomChoice.isSelected()) {
            for (ColumnInfo column : columns) {
                model.getColumns().add(column.getColumn());
            }
        }
        int selectionModel = selectionModelCombo.getSelectedIndex();
        model.setSelectionModel(selectionModel);
        try {
            columnModelProperty.setValue(new FormProperty.ValueWithEditor(model, new TableColumnModelEditor()));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        try {
            columnSelectionAllowedProperty.setValue(selectionModel != 0);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }

        // tableHeader
        Object value = null;
        try {
            value = headerProperty.getValue();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        boolean resizingAllowed = true;
        if (value instanceof JTableHeaderEditor.FormTableHeader) {
            JTableHeaderEditor.FormTableHeader header = (JTableHeaderEditor.FormTableHeader)value;
            resizingAllowed = header.isResizingAllowed();
        }
        try {
            value = new JTableHeaderEditor.FormTableHeader(headerProperty, resizingAllowed, reorderingAllowedChoice.isSelected());
            headerProperty.setValue(new FormProperty.ValueWithEditor(value, new JTableHeaderEditor()));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }
    
    @Override
    public void setObject(Object table) {
        assert (table instanceof JTable);
        this.table = (JTable)table;
    }

    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        assert (property instanceof RADProperty);
        
        // Obtain relevant properties
        RADProperty prop = (RADProperty)property;
        RADComponent comp = prop.getRADComponent();
        modelProperty = (FormProperty)comp.getPropertyByName("model"); // NOI18N        
        modelFromComponentEd.setContext(formModel, modelProperty);
        modelCustomEd.setContext(formModel, modelProperty);
        headerProperty = (FormProperty)comp.getPropertyByName("tableHeader"); // NOI18N
        columnModelProperty = (RADProperty)comp.getPropertyByName("columnModel"); // NOI18N
        columnSelectionAllowedProperty = (FormProperty)comp.getPropertyByName("columnSelectionAllowed"); // NOI18N

        // Obtain binding support
        bindingSupport = FormEditor.getBindingSupport(formModel);
        modelBoundChoice.setVisible(bindingSupport != null);
        modelBoundPanel.setVisible(bindingSupport != null);

        // Determine type of model
        try {
            Object value = modelProperty.getValue();
            PropertyEditor propEd = modelProperty.getCurrentEditor();
            if (propEd instanceof RADConnectionPropertyEditor) {
                RADConnectionPropertyEditor.Type type = ((RADConnectionPropertyEditor)propEd).getEditorType();
                if (type == RADConnectionPropertyEditor.Type.CustomCode) {
                    modelCustomEd.setValue(value);
                    modelCustomChoice.setSelected(true);
                } else {
                    modelFromComponentEd.setValue(value);
                    modelFromComponentChoice.setSelected(true);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }

        if (bindingSupport != null) {
            bindingProperty = comp.getBindingProperty("elements"); // NOI18N
            MetaBinding binding = bindingProperty.getValue();
            modelBoundCustomizer = new BindingCustomizer(bindingProperty);
            modelBoundCustomizer.setBinding(binding);
            if (binding != null) {
                modelBoundChoice.setSelected(true);
            }
            expressionCombo = modelBoundCustomizer.getSubExpressionCombo();
            expressionCombo.setEnabled(false);
            expressionCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ColumnInfo info = columns.get(lastSelectedColumn);
                    Object expression = expressionCombo.getSelectedItem();
                    info.setExpression((expression == null) ? null : expression.toString());
                    String clazz = "Object"; // NOI18N
                    TreePath treePath = expressionCombo.getSelectedTreePath();
                    if (treePath != null) {
                        Object pComp = treePath.getLastPathComponent();
                        if (pComp instanceof BindingCustomizer.ExpressionNode) {
                            clazz = ((BindingCustomizer.ExpressionNode)pComp).getTypeName();
                            clazz = FormUtils.autobox(clazz);
                            if (clazz.startsWith("java.lang.")) { // NOI18N
                                clazz = clazz.substring(10);
                            }
                        }
                    }
                    columnTypeCombo.setSelectedItem(clazz);
                    columnTableModel.fireTableRowsUpdated(lastSelectedColumn, lastSelectedColumn);
                }
            });
            modelBoundCustomizer.addTypeChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (modelBoundChoice.isSelected()) {
                        checkBindingType();
                    }
                }            
            });
        }

        // Replace dummy panels by customizers
        GroupLayout layout = (GroupLayout)modelTab.getLayout();
        modelFromComponentCustomizer = modelFromComponentEd.getCustomEditor();
        layout.replace(modelFromComponentPanel, modelFromComponentCustomizer);
        modelCustomCustomizer = modelCustomEd.getCustomEditor();
        layout.replace(modelCustomPanel, modelCustomCustomizer);
        if (modelBoundCustomizer != null) {
            layout.replace(modelBoundPanel, modelBoundCustomizer.getBindingPanel());
        }
        
        modelFromComponentEd.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateModelCustomizers();
            }
        });

        columns = new LinkedList<ColumnInfo>();
        Object value = null;
        try {
            value = columnModelProperty.getValue();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        if (value instanceof TableColumnModelEditor.FormTableColumnModel) {
            TableColumnModelEditor.FormTableColumnModel columnModel = (TableColumnModelEditor.FormTableColumnModel)value;
            for (TableColumnModelEditor.FormTableColumn column : columnModel.getColumns()) {
                columns.add(new ColumnInfo(column));
            }
            selectionModelCombo.setSelectedIndex(columnModel.getSelectionModel());
        } else {
            value = false;
            try {
                value = columnSelectionAllowedProperty.getRealValue();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            if (Boolean.TRUE.equals(value)) {
                selectionModelCombo.setSelectedIndex(2); // multiple interval selection
            } else {
                selectionModelCombo.setSelectedIndex(0); // not allowed
            }
        }
        
        try {
            value = headerProperty.getValue();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        boolean reorderingAllowed = true;
        if (value instanceof JTableHeaderEditor.FormTableHeader) {
            reorderingAllowed = ((JTableHeaderEditor.FormTableHeader)value).isReorderingAllowed();            
        }
        reorderingAllowedChoice.setSelected(reorderingAllowed);
        setColumnsToUI();
        updateModelCustomizers();
    }

    // FormAwareEditor
    @Override
    public void updateFormVersionLevel() {
    }

    /**
     * Called when bound model is selected to enable/disable
     * the columns tab. 
     */
    private void checkBindingType() {
        FormUtils.TypeHelper type = modelBoundCustomizer.getSelectedType();
        Class<?> clazz = (type == null) ? null : FormUtils.typeToClass(type);
        boolean collection = (clazz != null) && (Collection.class.isAssignableFrom(clazz) || Object.class.equals(clazz));
        tabbedPane.setEnabledAt(1, collection);
        expressionCombo.setEnabled(false);
    }

    /**
     * Called when model from existing component is selected
     * to update columns tab. 
     */
    private void checkModelFromComponent() {
        try {
            Object value = modelFromComponentEd.getValue();
            if (value instanceof FormDesignValue) {
                value = ((FormDesignValue)value).getDesignValue();
            }
            if (value instanceof TableModel) {
                TableModel model = (TableModel)value;
                ensureColumnCount(model.getColumnCount());
                for (int i=0; i<model.getColumnCount(); i++) {
                    FormProperty title = columns.get(i).getColumn().getTitle();
                    if (!title.isChanged()) {
                        title.setValue(model.getColumnName(i));
                    }
                }
            } else {
                ensureColumnCount(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private void setColumnsToUI() {
        rowTableModel = new TableModelEditor.NbTableModel(new String[0], new Class[0], new boolean[0], 0);
        if (modelHardcodedChoice.isSelected()) {
            try {
                Object value = modelProperty.getValue();
                if (value instanceof TableModelEditor.NbTableModel) {
                    TableModelEditor.NbTableModel model = (TableModelEditor.NbTableModel)value;
                    int rowCount = model.getRowCount();
                    ensureRowCount(rowCount);
                    int columnCount = model.getColumnCount();
                    ensureColumnCount(columnCount);
                    String[] titles = new String[columnCount];
                    Class[] types = new Class[columnCount];
                    boolean[] editable = new boolean[columnCount];
                    for (int i=0; i<columnCount; i++) {
                        ColumnInfo info = columns.get(i);
                        info.setEditable(model.isColumnEditable(i));
                        FormProperty title = info.getColumn().getTitle();
                        if (!title.isChanged()) {
                            title.setValue(model.getColumnName(i));
                        }
                        info.setType(typeToIndex(model.getColumnClass(i).getName()));
                        titles[i] = model.getColumnName(i);
                        types[i] = model.getColumnClass(i);
                        editable[i] = model.isColumnEditable(i);
                    }
                    rowTableModel = new TableModelEditor.NbTableModel(titles, types, editable, rowCount);
                    for (int i=0; i<columnCount; i++) {                    
                        for (int j=0; j<rowCount; j++) {
                            rowTableModel.setValueAt(model.getValueAt(j,i),j,i);
                        }
                    }
                }
                // Issue 140846
                int columnCount = rowTableModel.getColumnCount();
                for (int i=columns.size()-1; i>=columnCount; i--) {
                    columns.remove(i);
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        } else if (modelBoundChoice.isSelected()) {
            MetaBinding binding = bindingProperty.getValue();
            if (binding != null) {
                TableModel model = table.getModel();
                ensureColumnCount(binding.hasSubBindings() ? binding.getSubBindings().size() : 0);
                int index = 0;
                if (binding.hasSubBindings()) {
                    for (MetaBinding subBinding : binding.getSubBindings()) {
                        ColumnInfo info = columns.get(index);
                        String columnClass = subBinding.getParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER);
                        if ((columnClass != null) && columnClass.trim().endsWith(".class")) { // NOI18N
                            columnClass = columnClass.trim();
                            columnClass = columnClass.substring(0, columnClass.length()-6);
                        }
                        info.setClazz(columnClass);
                        info.setExpression(subBinding.getSourcePath());
                        FormProperty title = info.getColumn().getTitle();
                        if (!title.isChanged()) {
                            String columnName = subBinding.getParameter(MetaBinding.NAME_PARAMETER);
                            if (columnName == null) {
                                if ((model != null) && (model.getColumnCount() > index)) {
                                    columnName = model.getColumnName(index);
                                } else {
                                    String name = subBinding.getSourcePath();
                                    if (bindingSupport.isSimpleExpression(name)) {
                                        columnName = bindingSupport.unwrapSimpleExpression(name);
                                        columnName = bindingSupport.capitalize(columnName);
                                    }
                                }
                            }
                            if (columnName != null) {
                                try {
                                    title.setValue(columnName);
                                } catch (Exception ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                                }
                            }
                        }
                        String editableColumn = subBinding.getParameter(MetaBinding.EDITABLE_PARAMETER);
                        if (editableColumn != null) {
                            info.setEditable(!"false".equals(editableColumn)); // NOI18N
                        }
                        index++;
                    }
                }
            }
        }
        rowsTable.setModel(rowTableModel);
        rowCountSpinner.setValue(rowTableModel.getRowCount());
    }

    /**
     * Converts column type to index of column type combo. 
     * 
     * @param type column type.
     * @return index of the given column type in column type combo.
     */
    private static int typeToIndex(String type) {
        if (type.indexOf('.') == -1) {
            type = "java.lang." + type; // NOI18N
        }
        int index = 0;
        if (Object.class.getName().equals(type)) {
            index = 0;
        } else if (String.class.getName().equals(type)) {
            index = 1;
        } else if (Boolean.class.getName().equals(type)) {
            index = 2;
        } else if (Integer.class.getName().equals(type)) {
            index = 3;
        } else if (Byte.class.getName().equals(type)) {
            index = 4;
        } else if (Short.class.getName().equals(type)) {
            index = 5;
        } else if (Long.class.getName().equals(type)) {
            index = 6;
        } else if (Float.class.getName().equals(type)) {
            index = 7;
        } else if (Double.class.getName().equals(type)) {
            index = 8;
        }
        return index;
    }

    /**
     * Returns column type that corresponds to the given index in column type combo.
     * 
     * @param index index of column type in column type combo.
     * @return column type that corresponds to the given index in column type combo.
     */
    private static Class indexToType(int index) {
        Class type;
        switch (index) {
            case 1: type = String.class; break;
            case 2: type = Boolean.class; break;
            case 3: type = Integer.class; break;
            case 4: type = Byte.class; break;
            case 5: type = Short.class; break;
            case 6: type = Long.class; break;
            case 7: type = Float.class; break;
            case 8: type = Double.class; break;
            default: type = Object.class; break;
        }
        return type;
    }

    private boolean reverting;

    /**
     * Ensures that there is correct number of rows in column info table 
     * and correct number of columns in row info table.
     * 
     * @param columnCount number of columns.
     */
    private void ensureColumnCount(int columnCount) {
        if (checkColumnCount(columnCount)) {
            boolean hardcoded = modelHardcodedChoice.isSelected();
            for (int i=columns.size(); i<columnCount; i++) {
                columns.add(new ColumnInfo(columnModelProperty, getFreeColumnIndex()));
                if (hardcoded) {
                    rowTableModel.addColumn(i);
                }
            }
            for (int i=columns.size()-1; i>=columnCount; i--) {
                if (lastSelectedColumn == i) {
                    lastSelectedColumn = -1;
                }
                columns.remove(i);
                if (hardcoded) {
                    rowTableModel.removeColumn(i);
                }
            }
            columnTableModel.fireTableDataChanged();
        } else {
            reverting = true;
            columnCountSpinner.setValue(columns.size());
            reverting = false;
        }
    }

    private boolean checkColumnCount(int columnCount) {
        boolean ok = columnCount<=COLUMN_MAX;
        if (ok && !reverting) {
            columnErrorLabel.setText(""); // NOI18N
        } else {
            columnErrorLabel.setText(NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Column_Warning_MaxExceeded")); // NOI18N
        }
        return ok;
    }

    /**
     * Ensures that there is correct number of rows in row info table.
     */
    private void ensureRowCount(int rowCount) {
        if (checkRowCount(rowCount)) {
            for (int i=rowTableModel.getRowCount(); i<rowCount; i++) {
                rowTableModel.addRow(i);
            }
            for (int i=rowTableModel.getRowCount()-1; i>=rowCount; i--) {
                rowTableModel.removeRow(i);
            }
        } else {
            reverting = true;
            rowCountSpinner.setValue(rowTableModel.getRowCount());
            reverting = false;
        }
    }

    private boolean checkRowCount(int rowCount) {
        boolean ok = rowCount<=ROW_MAX;
        if (ok && !reverting) {
            rowErrorLabel.setText(""); // NOI18N
        } else {
            rowErrorLabel.setText(NbBundle.getMessage(TableCustomizer.class, "LBL_TableCustomizer_Row_Warning_MaxExceeded")); // NOI18N
        }
        return ok;
    }

    /**
     * Returns title of the specified column.
     * 
     * @param columnNo column index. 
     * @return title of the specified column.
     */
    private String getTitle(int columnNo) {
        String title = "null"; // NOI18N
        ColumnInfo info = columns.get(columnNo);
        FormProperty titleProp = info.getColumn().getTitle();
        if (titleProp.isChanged()) {
            try {
                Object value = titleProp.getRealValue();
                if (value != null) {
                    title = value instanceof String ? (String)value : "";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
        return title;
    }

    private void updateWidthCombos() {
        boolean resizable = resizableColumnChoice.isSelected();
        widthMinCombo.setEnabled(resizable);
        widthMaxCombo.setEnabled(resizable);
        if (!resizable) {
            widthMinCombo.setSelectedIndex(0);
            widthMaxCombo.setSelectedIndex(0);
        }
    }

    private int lastSelectedColumn = -1;
    private void updateColumnSection() {
        if (lastSelectedColumn != -1) {
            ColumnInfo info = columns.get(lastSelectedColumn);
            info.setEditable(editableColumnChoice.isSelected());
            info.setType(columnTypeCombo.getSelectedIndex());
            if (columnTypeCombo.getEditor().getEditorComponent().hasFocus()) {
                info.setClazz(columnTypeCombo.getEditor().getItem().toString());
            } else {
                info.setClazz(columnTypeCombo.getSelectedItem().toString());
            }
            if (expressionCombo != null) {
                Object expression = expressionCombo.getSelectedItem();
                info.setExpression((expression == null) ? "null" : expression.toString()); // NOI18N
            }
            if (modelHardcodedChoice.isSelected()) {
                Class oldClass = rowTableModel.getColumnClass(lastSelectedColumn);
                Class newClass = indexToType(info.getType());
                if (newClass != oldClass) {
                    rowTableModel.setColumnClass(lastSelectedColumn, newClass);
                    for (int i=0; i<rowTableModel.getRowCount(); i++) {
                        rowTableModel.setValueAt(null, i, lastSelectedColumn);
                    }
                }
                rowTableModel.setColumnName(lastSelectedColumn, getTitle(lastSelectedColumn));
                rowTableModel.setColumnEditable(lastSelectedColumn, info.isEditable());
                rowTableModel.fireTableStructureChanged();
            }
            TableColumnModelEditor.FormTableColumn column = info.getColumn();
            column.setResizable(resizableColumnChoice.isSelected());
            int width = -1;
            try {
                width = Integer.parseInt(widthMinCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setMinWidth(width);
            width = -1;
            try {
                width = Integer.parseInt(widthPrefCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setPrefWidth(width);
            width = -1;
            try {
                width = Integer.parseInt(widthMaxCombo.getSelectedItem().toString());
            } catch (NumberFormatException nfex) {}
            column.setMaxWidth(width);
            info.getColumn().getTitle().removePropertyChangeListener(titleListener);
        }
        int[] index = columnsTable.getSelectedRows();
        boolean single = (index.length == 1);
        columnTitlePanel.setEnabled(single);
        columnEditorPanel.setEnabled(single);
        columnRendererPanel.setEnabled(single);
        columnTypeCombo.setEnabled(single);
        resizableColumnChoice.setEnabled(single);
        editableColumnChoice.setEnabled(single);
        widthMinCombo.setEnabled(single);
        widthPrefCombo.setEnabled(single);
        widthMaxCombo.setEnabled(single);
        if (expressionCombo != null) {
            expressionCombo.setEnabled(single);
        }
        if (single) {
            lastSelectedColumn = index[0];
            ColumnInfo info = columns.get(index[0]);
            editableColumnChoice.setSelected(info.isEditable());
            if (modelBoundChoice.isSelected()) {
                expressionCombo.setSelectedItem(info.getExpression());
                columnTypeCombo.setSelectedItem(info.getClazz());
                if (columnTypeCombo.getEditor().getEditorComponent().hasFocus()) {
                    columnTypeCombo.getEditor().setItem(info.getClazz());
                }
            } else {
                columnTypeCombo.setSelectedIndex(info.getType());
            }
            TableColumnModelEditor.FormTableColumn selectedColumn = info.getColumn();
            columnTitlePanel.setProperty(selectedColumn.getTitle());
            columnEditorPanel.setProperty(selectedColumn.getEditor());
            columnRendererPanel.setProperty(selectedColumn.getRenderer());
            resizableColumnChoice.setSelected(selectedColumn.isResizable());
            if (selectedColumn.getMinWidth() == -1) {
                widthMinCombo.setSelectedIndex(0);
            } else {
                widthMinCombo.setSelectedItem(selectedColumn.getMinWidth());
            }
            if (selectedColumn.getPrefWidth() == -1) {
                widthPrefCombo.setSelectedIndex(0);
            } else {
                widthPrefCombo.setSelectedItem(selectedColumn.getPrefWidth());
            }
            if (selectedColumn.getMaxWidth() == -1) {
                widthMaxCombo.setSelectedIndex(0);
            } else {
                widthMaxCombo.setSelectedItem(selectedColumn.getMaxWidth());
            }
            info.getColumn().getTitle().addPropertyChangeListener(titleListener);
        } else {
            lastSelectedColumn = -1;
        }
        updateWidthCombos();
    }
    private PropertyChangeListener titleListener;

    static class ColumnInfo {
        private TableColumnModelEditor.FormTableColumn column;
        private int type = 0;
        private boolean editable = true;
        private String expression = "null"; // NOI18N
        private String clazz;

        ColumnInfo(TableColumnModelEditor.FormTableColumn column) {
            this.column = column;
        }

        ColumnInfo(RADProperty columnModelProperty, int index) {
            this(new TableColumnModelEditor.FormTableColumn(columnModelProperty, index));
        }

        public TableColumnModelEditor.FormTableColumn getColumn() {
            return column;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getClazz() {
            return (clazz == null) ? NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Type_Object") : clazz; // NOI18N
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }
    }

    /**
     * Selection listener for the columnsTable. 
     */
    class ColumnSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int[] index = columnsTable.getSelectedRows();
            boolean empty = (index.length == 0);
            boolean multi = (index.length > 1);
            deleteColumnButton.setEnabled(!empty);
            moveUpColumnButton.setEnabled(!empty && !multi && (index[0] > 0));
            moveDownColumnButton.setEnabled(!empty && !multi && (index[0] < columns.size()-1));
            updateColumnSection();
        }
        
    }

    /**
     * Selection listener for the rowsTable. 
     */
    class RowSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int[] index = rowsTable.getSelectedRows();
            boolean empty = (index.length == 0);
            boolean multi = (index.length > 1);
            deleteRowButton.setEnabled(!empty);
            moveUpRowButton.setEnabled(!empty && !multi && (index[0] > 0));
            moveDownRowButton.setEnabled(!empty && !multi && (index[0] < rowTableModel.getRowCount()-1));
        }

    }

    /**
     * Table model for the columnsTable. 
     */
    class CustomizerTableModel extends AbstractTableModel {
        private int modelType;

        public void setModelType(int modelType) {
            this.modelType = modelType;
            fireTableStructureChanged();
            if (modelType == 0) {
                columnsTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCellEditor));
            }
        }

        @Override
        public int getRowCount() {
            return (columns == null) ? 0 : columns.size();
        }

        @Override
        public int getColumnCount() {
            int columnCount;
            switch (modelType) {
                case 0: columnCount = 4; break;
                case 1: columnCount = 4; break;
                case 2: columnCount = 2; break;
                default: columnCount = 0;
            }
            return columnCount;
        }

        @Override
        public String getColumnName(int column) {
            String name = null;
            switch (column) {
                case 0: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Title"); break; // NOI18N
                case 1: 
                    switch (modelType) {
                        case 0: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Type"); break; // NOI18N
                        case 1: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_Expression"); break; // NOI18N
                        case 2: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_ResizableH"); break; // NOI18N
                    }
                    break;
                case 2: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_ResizableH"); break; // NOI18N
                case 3: name = NbBundle.getMessage(getClass(), "LBL_TableCustomizer_EditableH"); break; // NOI18N
            }
            return name;
        }

        @Override
        public Class getColumnClass(int column) {
            if ((column > 1) || ((column == 1) && (modelType == 2))) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;
            ColumnInfo info = columns.get(rowIndex);
            switch (columnIndex) {
                case 0: value = getTitle(rowIndex); break;
                case 1:
                    switch (modelType) {
                        case 0: value = indexToType(info.getType()).getName().substring(10); break;
                        case 1: value = info.getExpression(); break;
                        case 2: value = info.getColumn().isResizable();
                    }
                    break;
                case 2: value = info.getColumn().isResizable(); break;
                case 3: value = info.isEditable(); break;
            }
            return value;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            ColumnInfo info = columns.get(rowIndex);
            switch (columnIndex) {
                case 0: 
                    try {
                        info.getColumn().getTitle().setValue(value);
                        columnTitlePanel.repaint();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                    break;
                case 1:
                    switch (modelType) {
                        case 0:
                            info.setType(typeToIndex((String)value));
                            columnTypeCombo.setSelectedIndex(info.getType());
                            break;
                        case 1:
                            info.setExpression((String)value);
                            expressionCombo.setSelectedItem(value);
                            break;
                        case 2:
                            info.getColumn().setResizable((Boolean)value);
                            resizableColumnChoice.setSelected((Boolean)value);
                            break;
                    }
                    break;
                case 2:
                    info.getColumn().setResizable((Boolean)value);
                    resizableColumnChoice.setSelected((Boolean)value);
                    updateWidthCombos();
                    break;
                case 3:
                    info.setEditable((Boolean)value);
                    editableColumnChoice.setSelected((Boolean)value);
                    break;
            }
        }

    }

}
