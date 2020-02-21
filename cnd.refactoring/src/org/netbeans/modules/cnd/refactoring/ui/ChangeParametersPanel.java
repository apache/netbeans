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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Panel contains components for signature change. There is table with
 * parameters, you can add parameters, reorder parameteres or remove not
 * used paramaters (not available yet). You can also change access modifier.
 *
 */
public class ChangeParametersPanel extends JPanel implements CustomRefactoringPanel {

    private final CsmObject selectedElement;
    private CsmFunction functionObj;
    private final ParamTableModel model;
    private final ChangeListener parent;

    private static final String DEFAULT_VALUES_ONLY_IN_DECLARATION = "UseDefaultValueOnlyInFunctionDefinition"; // NOI18N

    private static Action editAction = null;
    private String returnType;
    
    private static final String[] modifierNames = {
        "public", // NOI18N
        "protected", // NOI18N
        "private", // NOI18N
        "<default>", // NOI18N
    };
    
    @Override
    public Component getComponent() {
        return this;
    }
    
    /*package*/final static int PARAM_NAME = 0;
    /*package*/final static int PARAM_TYPE = 1;
    /*package*/final static int PARAM_VALUE = 2;
    /*package*/final static int PARAM_ORIG_INDEX = 3;
    /*package*/final static int PARAM_USED = 4;

    private static final String[] columnNames = {
        getString("LBL_ChangeParsColName"), // NOI18N
        getString("LBL_ChangeParsColType"), // NOI18N
        getString("LBL_ChangeParsColDefVal"), // NOI18N
        getString("LBL_ChangeParsColOrigIdx"), // NOI18N
        getString("LBL_ChangeParsParUsed") // NOI18N
    };

    // modifier items in combo - indexes
    private static final int MOD_PUBLIC_INDEX = 0;
    private static final int MOD_PROTECTED_INDEX = 1;
    private static final int MOD_PRIVATE_INDEX = 2;
    private static final int MOD_DEFAULT_INDEX = 3;

    private static final String ACTION_INLINE_EDITOR = "invokeInlineEditor";  //NOI18N

    /** Creates new form ChangeMethodSignature */
    public ChangeParametersPanel(CsmObject selectedObj, CsmContext editorContext, ChangeListener parent) {
        if (selectedObj == null) {
            this.selectedElement = editorContext.getEnclosingFunction();
        } else {
            this.selectedElement = selectedObj;
        }
        this.parent = parent;
        model = new ParamTableModel(columnNames, 0);
        initComponents();
        defaultsOnlyInFunDeclaration.setSelected(isUseDefaultValueOnlyInFunctionDeclaration());
    }
    
    private boolean initialized = false;
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        CsmFunction fun = ((CsmFunction) CsmRefactoringUtils.getReferencedElement(selectedElement)).getDeclaration();
        functionObj = fun;
        returnType = functionObj.getReturnType().getCanonicalText().toString();
        if (CsmKindUtilities.isMethod(functionObj)) {
            CsmMethod method = (CsmMethod)CsmBaseUtilities.getFunctionDeclaration(functionObj);
            setModifier(method.getVisibility());
        } else {
            setModifier(CsmVisibility.NONE);
        }
        modifiersCombo.setEnabled(false);
        initTableData();
        previewChange.setText(genDeclarationString());
        initialized = true;
    }
    
    protected DefaultTableModel getTableModel() {
        return model;
    }
    
    protected CsmVisibility getModifier() {
        int index = modifiersCombo.getSelectedIndex();
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
                assert false: "unexpected index:" + index;
        }
        return CsmVisibility.NONE;
    }

    protected boolean isUseDefaultValueOnlyInFunctionDeclaration() {
        return NbPreferences.forModule(ChangeParametersPanel.class).getBoolean(DEFAULT_VALUES_ONLY_IN_DECLARATION, false);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        modifiersPanel = new javax.swing.JPanel();
        modifiersLabel = new javax.swing.JLabel();
        modifiersCombo = new javax.swing.JComboBox();
        defaultsOnlyInFunDeclaration = new javax.swing.JCheckBox();
        eastPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        fillPanel = new javax.swing.JPanel();
        westPanel = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        paramTitle = new javax.swing.JLabel();
        previewChange = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setAutoscrolls(true);
        setName(getString("LBL_TitleChangeParameters"));
        setLayout(new java.awt.GridBagLayout());

        modifiersPanel.setLayout(new java.awt.GridBagLayout());

        modifiersLabel.setLabelFor(modifiersCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/refactoring/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(modifiersLabel, bundle.getString("LBL_ChangeParsMods")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        modifiersPanel.add(modifiersLabel, gridBagConstraints);

        modifiersCombo.setModel(new DefaultComboBoxModel(modifierNames));
        modifiersCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiersComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        modifiersPanel.add(modifiersCombo, gridBagConstraints);
        modifiersCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_modifiersCombo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(defaultsOnlyInFunDeclaration, org.openide.util.NbBundle.getMessage(ChangeParametersPanel.class, "ChangeParametersPanel.defaultsOnlyInFunDeclaration.text")); // NOI18N
        defaultsOnlyInFunDeclaration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsOnlyInFunDeclarationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        modifiersPanel.add(defaultsOnlyInFunDeclaration, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(modifiersPanel, gridBagConstraints);

        eastPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 11, 1, 1));
        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("LBL_ChangeParsAdd")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsAdd")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("LBL_ChangeParsRemove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, bundle.getString("LBL_ChangeParsMoveUp")); // NOI18N
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, bundle.getString("LBL_ChangeParsMoveDown")); // NOI18N
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        buttonsPanel.add(moveDownButton, gridBagConstraints);
        moveDownButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsMoveDown")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        eastPanel.add(buttonsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        eastPanel.add(fillPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(eastPanel, gridBagConstraints);

        westPanel.setPreferredSize(new java.awt.Dimension(453, 100));

        paramTable.setModel(model);
        initRenderer();
        paramTable.getSelectionModel().addListSelectionListener(getListener1());
        paramTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        model.addTableModelListener(getListener2());
        paramTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), ACTION_INLINE_EDITOR); //NOI18N
        paramTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_INLINE_EDITOR); //NOI18N
        paramTable.getActionMap().put(ACTION_INLINE_EDITOR, getEditAction()); //NOI18N
        paramTable.setSurrendersFocusOnKeystroke(true);
        paramTable.setCellSelectionEnabled(false);
        paramTable.setRowSelectionAllowed(true);
        paramTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE); //NOI18N
        paramTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //NOI18N
        westPanel.setViewportView(paramTable);
        paramTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_paramTable")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(westPanel, gridBagConstraints);

        paramTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        paramTitle.setLabelFor(paramTable);
        org.openide.awt.Mnemonics.setLocalizedText(paramTitle, bundle.getString("LBL_ChangeParsParameters")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(paramTitle, gridBagConstraints);

        previewChange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getBundle(ChangeParametersPanel.class).getString("LBL_ChangeParsPreview"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(previewChange, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void modifiersComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiersComboActionPerformed
        previewChange.setText(genDeclarationString());
    }//GEN-LAST:event_modifiersComboActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        acceptEditedValue(); 
        int[] selectedRows = paramTable.getSelectedRows();
        ListSelectionModel selectionModel = paramTable.getSelectionModel();
        for (int i = 0; i < selectedRows.length; ++i) {
            boolean b = ((Boolean) ((Vector) model.getDataVector().get(selectedRows[i] - i)).get(PARAM_USED)).booleanValue();
            if (!b) {
                String title = getString("LBL_ChangeParsCannotDeleteTitle");
                String mes = MessageFormat.format(getString("LBL_ChangeParsCannotDelete"),((Vector) model.getDataVector().get(selectedRows[i] - i)).get(PARAM_NAME));
                int a = JOptionPane.showConfirmDialog(this, mes, title, JOptionPane.YES_NO_OPTION);
                if (a==JOptionPane.YES_OPTION) {
                    model.removeRow(selectedRows[i] - i);
                    selectionModel.clearSelection();
                }
            } else {
                model.removeRow(selectedRows[i] - i);
                selectionModel.clearSelection();
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        doMove(1);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        doMove(-1);
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        acceptEditedValue(); 
        int rowCount = model.getRowCount();
        model.addRow(new Object[] { "par" + rowCount, "int", "0", Integer.valueOf(-1), Boolean.TRUE }); // NOI18N
    }//GEN-LAST:event_addButtonActionPerformed

    private void defaultsOnlyInFunDeclarationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsOnlyInFunDeclarationActionPerformed
        NbPreferences.forModule(ChangeParametersPanel.class).putBoolean(DEFAULT_VALUES_ONLY_IN_DECLARATION, defaultsOnlyInFunDeclaration.isSelected());
        previewChange.setText(genDeclarationString());
    }//GEN-LAST:event_defaultsOnlyInFunDeclarationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JCheckBox defaultsOnlyInFunDeclaration;
    private javax.swing.JPanel eastPanel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JComboBox modifiersCombo;
    private javax.swing.JLabel modifiersLabel;
    private javax.swing.JPanel modifiersPanel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel paramTitle;
    private javax.swing.JLabel previewChange;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane westPanel;
    // End of variables declaration//GEN-END:variables

    private ListSelectionListener getListener1() {
        return new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (!lsm.isSelectionEmpty()) {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    setButtons(minIndex, maxIndex);

                    boolean enableRemoveBtn = true;
                    for (int i = minIndex; i <= maxIndex; i++) {
                        enableRemoveBtn = model.isRemovable(i);
                        if (!enableRemoveBtn) {
                            break;
                        }
                    }
                    removeButton.setEnabled(enableRemoveBtn);
                } else {
                    moveDownButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
                    removeButton.setEnabled(false);
                }
            }
        };
    }
    
    private TableModelListener getListener2() {
        return new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // update buttons availability
                int[] selectedRows = paramTable.getSelectedRows();
                if (selectedRows.length == 0) {
                    removeButton.setEnabled(false);
                } else {
                    boolean enableRemoveBtn = true;
                    for (int i = 0; i < selectedRows.length; i++) {
                        if (selectedRows[i] < model.getRowCount()) {
                            enableRemoveBtn = model.isCellEditable(selectedRows[i], 0);
                            if (!enableRemoveBtn) {
                                break;
                            }
                        }
                    }
                    removeButton.setEnabled(enableRemoveBtn);
                    int min = selectedRows[0];
                    int max = selectedRows[selectedRows.length - 1];
                    setButtons(min, max);
                }

                // update preview
                previewChange.setText(genDeclarationString());

                parent.stateChanged(null);
            }
        };
    }

    private void initTableData() {
        Collection<CsmParameter> pars = functionObj.getParameters();

        List<CsmType> typeList = new ArrayList<>();
        for (CsmParameter par: pars) {
            typeList.add(par.getType());
        }

        Collection<CsmFunction> allMethods = new LinkedHashSet<>();
        allMethods.add(functionObj);
        if (CsmKindUtilities.isMethod(functionObj)) {
            CsmMethod method = (CsmMethod)functionObj;
            if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                allMethods.addAll(CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, true));
                assert !allMethods.isEmpty() : "must be at least start object " + method;
            }
        }

        Collection<CsmFunction> allFunctions = new LinkedHashSet<>();
        for (CsmFunction csmFunction : allMethods) {
            CsmFunctionDefinition definition = csmFunction.getDefinition();
            if (definition != null) {
                allFunctions.add(definition);
            } else {
                allFunctions.add(csmFunction);
            }
        }
        for (CsmFunction currentMethod: allFunctions) {
            int originalIndex = 0;
            CsmFile containingFile = currentMethod.getContainingFile();
            for (CsmParameter par: currentMethod.getParameters()) {
                CsmType desc = par.getType();
                String typeRepresentation;
                if (par.isVarArgs() && originalIndex == pars.size()-1) {
                    typeRepresentation = "..."; // NOI18N
                } else {
                    typeRepresentation = getTypeStringRepresentation(desc);
                }
                Collection<CsmReference> references = Collections.emptySet();
                if (CsmKindUtilities.isFunctionDefinition(currentMethod)) {
                    references = CsmReferenceRepository.getDefault().getReferences(par, containingFile, CsmReferenceKind.ALL, Interrupter.DUMMY);
                }
                Boolean removable = references.size() <= 1;
                if (model.getRowCount()<=originalIndex) {
                    Object[] parRep = new Object[] { par.getName().toString(), typeRepresentation, "", Integer.valueOf(originalIndex), removable };
                    model.addRow(parRep);
                } else {
                    removable = Boolean.valueOf(model.isRemovable(originalIndex) && removable.booleanValue());
                    // vector of objects
                    @SuppressWarnings("unchecked")
                    Vector<Object> data = (Vector<Object>) model.getDataVector().get(originalIndex);
                    data.set(PARAM_USED, removable);
                }
                originalIndex++;
            }
        }
    }
    
    private static String getTypeStringRepresentation(CsmType desc) {
        return desc.getCanonicalText().toString();
    }

    private boolean acceptEditedValue() {
        TableCellEditor tce = paramTable.getCellEditor();
        if (tce != null) {
            return paramTable.getCellEditor().stopCellEditing();
        }
        return false;
    }
    
    private void doMove(int step) {
        acceptEditedValue(); 
        
        ListSelectionModel selectionModel = paramTable.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        
        selectionModel.clearSelection();
        model.moveRow(min, max, min + step);
        selectionModel.addSelectionInterval(min + step, max + step);
    }
    
    private void setButtons(int min, int max) {
        int r = model.getRowCount() - 1;
        moveUpButton.setEnabled(min > 0 ? true : false);
        moveDownButton.setEnabled(max < r ? true : false);
    }
    
    private void initRenderer() {
        TableColumnModel tcm = paramTable.getColumnModel();
        paramTable.removeColumn(tcm.getColumn(3));
        paramTable.removeColumn(tcm.getColumn(3));
        Enumeration columns = paramTable.getColumnModel().getColumns();
        TableColumn tc = null;
        while (columns.hasMoreElements()) {
            tc = (TableColumn) columns.nextElement();
            tc.setCellRenderer(new ParamRenderer());
        }
    }

    private CsmVisibility modifier = CsmVisibility.NONE;

    private void setModifier(CsmVisibility visibility) {
        this.modifier = visibility;
        switch (visibility) {
            case NONE:
                modifiersCombo.setSelectedIndex(MOD_DEFAULT_INDEX);
                break;
            case PRIVATE:
                modifiersCombo.setSelectedIndex(MOD_PRIVATE_INDEX);
                break;
            case PROTECTED:
                modifiersCombo.setSelectedIndex(MOD_PROTECTED_INDEX);
                break;
            case PUBLIC:
                modifiersCombo.setSelectedIndex(MOD_PUBLIC_INDEX);
                break;
        }
    }

    public String genDeclarationString() {
        // generate preview for modifiers
        // access modifiers
        String mod = modifiersCombo.getSelectedIndex() != MOD_DEFAULT_INDEX /*default modifier?*/ ?
            (String) modifiersCombo.getSelectedItem() + ":" : ""; // NOI18N
        
        StringBuilder buf = new StringBuilder(mod);
        boolean defValueInDecl = defaultsOnlyInFunDeclaration.isSelected();
        // other than access modifiers - using data provided by the element
        // first of all, reset access modifier, because it is generated from combo value
//        String otherMod = CsmVisibility.toString(((CallableFeature) refactoredObj).getModifiers() & 0xFFFFFFF8);
//        if (otherMod.length() != 0) {
//            buf.append(otherMod);
//            buf.append(' ');
//        }
        // generate the return type for the method and name
        // for the both - method and constructor
        String name;
        if (CsmKindUtilities.isConstructor(functionObj)) {
            buf.append(' '); // NOI18N
            name = CsmRefactoringUtils.getSimpleText(functionObj);
        } else {
            buf.append(returnType);
            buf.append(' '); // NOI18N
            name = CsmRefactoringUtils.getSimpleText(functionObj);
        }
        buf.append(name);
        buf.append('('); // NOI18N
        // generate parameters to the preview string
        @SuppressWarnings("unchecked")
        Vector<Vector> data = model.getDataVector();
        List<?>[] parameters = data.toArray(new List[0]);
        for (int i = 0; i < parameters.length; i++) {
            buf.append(parameters[i].get(PARAM_TYPE));
            buf.append(' ');
            buf.append(parameters[i].get(PARAM_NAME));
            if (defValueInDecl) {
                String defParam = (String) parameters[i].get(PARAM_VALUE);
                defParam = defParam.trim();
                if (defParam != null && defParam.length() > 0) {
                    buf.append(" = ").append(defParam); // NOI18N
                }
            }
            if (i < parameters.length - 1) {
                buf.append(',').append(' '); // NOI18N
            }
        }
        buf.append(')'); //NOI18N
        
        return buf.toString();
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPanel.class, key);
    }

    private static Action getEditAction() {
        if (editAction == null) {
            editAction = new EditAction();
        }
        return editAction;
    }

    private static void autoEdit(JTable tab) {
        if (tab.editCellAt(tab.getSelectedRow(), tab.getSelectedColumn(), null) &&
                tab.getEditorComponent() != null) {
            JTextField field = (JTextField) tab.getEditorComponent();
            field.setCaretPosition(field.getText().length());
            field.requestFocusInWindow();
            field.selectAll();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    // this class is used for marking rows as read-only. If the user uses
    // standard DefaultTableModel, rows added through its methods is added
    // as a read-write. -- Use methods with Boolean paramater to add
    // rows marked as read-only.
    static class ParamTableModel extends DefaultTableModel {
        
        public ParamTableModel(Object[] data, int rowCount) {
            super(data, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column > 2) {
                // check box indicating usage of parameter is not editable
                return false;
            }
            // otherwise, check that user can change only the values provided
            // for the new parameter. (name change of old parameters aren't
            // allowed.
            Integer origIdx = (Integer) ((Vector) getDataVector().get(row)).get(PARAM_ORIG_INDEX);
            return origIdx.intValue() == -1 ? true : false;
        }
        
        public boolean isRemovable(int row) {
            return ((Boolean) ((Vector) getDataVector().get(row)).get(PARAM_USED)).booleanValue();
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (column == PARAM_NAME || column == PARAM_TYPE || column == PARAM_VALUE) {
                if (aValue instanceof String) {
                    aValue = ((String)aValue).toString().trim();
                }
            }
            super.setValueAt(aValue, row, column);
        }
    } // end ParamTableModel

    private static class EditAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent ae) {
            autoEdit((JTable) ae.getSource());
        }
    }
    
    class ParamRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        Color origBackground;
        
        public ParamRenderer() {
            setOpaque(true);
            origBackground = getBackground();
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column)
        {
            super.getTableCellRendererComponent(table,  value, isSelected, hasFocus, row, column);
            boolean isRemovable = model.isRemovable(row);
            if (!isSelected) {
                if (!isRemovable) {
                    setBackground(UIManager.getColor("Panel.background")); // NOI18N
                } else {
                    setBackground(origBackground);
                }
            }
            return this;
        }
        
    }
    
    // end INNERCLASSES
}
