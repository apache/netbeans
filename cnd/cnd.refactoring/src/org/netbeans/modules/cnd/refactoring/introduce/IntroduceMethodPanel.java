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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.api.model.support.CsmTypes.TypeDescriptor;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring.IntroduceMethodContext.FunctionKind;
import org.netbeans.modules.cnd.refactoring.ui.InsertPoint;
import org.netbeans.modules.cnd.refactoring.ui.UIUtilities.BooleanTableCellRenderer;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Panel contains components for signature change. There is table with
 * parameters, you can add parameters, reorder parameteres or remove not
 * used paramaters (not available yet). You can also change access modifier.
 *
 */
public class IntroduceMethodPanel extends JPanel implements CustomRefactoringPanel {

    private final ParamTableModel model;
    private final FileObject fileObject;
    private final ChangeListener parent;
    private final CsmObject selectedObj;
    private final CsmContext editorContext;
    private final IntroduceMethodRefactoring refactoring;
    private IntroduceMethodRefactoring.IntroduceMethodContext res;

    private static final String DEFAULT_VALUES_ONLY_IN_DECLARATION = "UseDefaultValueOnlyInFunctionDefinition"; // NOI18N
    private static Action editAction = null;
    private static final String[] columnNames = {
        getString("LBL_ChangeParsColByRef"), // NOI18N
        getString("LBL_ChangeParsColName"), // NOI18N
        getString("LBL_ChangeParsColType"), // NOI18N
    };

    private static final String ACTION_INLINE_EDITOR = "invokeInlineEditor";  //NOI18N

    public IntroduceMethodPanel(IntroduceMethodRefactoring refactoring, CsmObject selectedObj, CsmContext editorContext, ChangeListener parent) {
        this.refactoring = refactoring;
        this.parent = parent;
        this.selectedObj = selectedObj;
        this.editorContext = editorContext;
        this.fileObject = editorContext.getFileObject();
        model = new ParamTableModel(columnNames, 0);
        initComponents();
        paramTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    private boolean initialized = false;

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        res = refactoring.getIntroduceMethodContext();
        if (res == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                name.requestFocus();
                returnTypeTextField.setText("void"); //NOI18N
                name.setText("function"); //NOI18N
                name.selectAll();
                if (res.getFunctionKind() == FunctionKind.MethodDefinition) {
                    insertionPointLabel.setVisible(true);
                    insertPointCombo.setVisible(true);
                    insertPointCombo.setEnabled(true);
                    InsertPoint.initInsertPoints(insertPointCombo, res.getEnclosingClass());
                } else {
                    insertionPointLabel.setVisible(false);
                    insertPointCombo.setEnabled(false);
                    insertPointCombo.setVisible(false);
                }
                initTableData();
                DialogBinding.bindComponentToFile(fileObject, res.getSelectionFrom(), res.getSelectionTo() - res.getSelectionFrom(), previewEditorPane);
                previewEditorPane.setBackground(getBackground());
                updateSignaturePreview();
                DocumentListener documentListener = new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateSignaturePreview();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateSignaturePreview();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateSignaturePreview();
                    }
                };
                returnTypeTextField.getDocument().addDocumentListener(documentListener);
                name.getDocument().addDocumentListener(documentListener);
                initialized = true;
                //btnOk.setEnabled(((ErrorLabel)errorLabel).isInputTextValid());
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        name.requestFocusInWindow();
        return true;
    }

    @Override
    public void requestFocus() {
        name.requestFocus();
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    public DefaultTableModel getTableModel() {
        return model;
    }
    
    protected boolean isUseDefaultValueOnlyInFunctionDeclaration() {
        return NbPreferences.forModule(IntroduceMethodPanel.class).getBoolean(DEFAULT_VALUES_ONLY_IN_DECLARATION, false);
    }

    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            @Override
            public String validate(String text) {
                if( null == text || text.length() == 0 ) {
                    return "";
                }
                if (!CndLexerUtilities.isCppIdentifier(text)) {
                    return getDefaultErrorMessage( text );
                }
                return null;
            }
        };

        final ErrorLabel eLabel = new ErrorLabel( name.getDocument(), validator );
        eLabel.addPropertyChangeListener(  ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                //btnOk.setEnabled(eLabel.isInputTextValid());
            }
        });
        return eLabel;
    }

    String getDefaultErrorMessage( String inputText ) {
        return "'" + inputText +"' is not a valid identifier"; // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        returnTypeLabel = new javax.swing.JLabel();
        returnTypeTextField = new javax.swing.JTextField();
        modifiersPanel = new javax.swing.JPanel();
        insertionPointLabel = new javax.swing.JLabel();
        insertPointCombo = new javax.swing.JComboBox();
        eastPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        fillPanel = new javax.swing.JPanel();
        westPanel = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        paramTitle = new javax.swing.JLabel();
        previewChange = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewEditorPane = new javax.swing.JEditorPane();
        errorLabel = createErrorLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setAutoscrolls(true);
        setName(getString("LBL_TitleChangeParameters"));
        setLayout(new java.awt.GridBagLayout());

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceMethodPanel.class).getString("IntroduceMethodPanel.lblName.text")); // NOI18N

        name.setColumns(20);

        returnTypeLabel.setLabelFor(returnTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.returnTypeLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(returnTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .addComponent(returnTypeTextField)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(returnTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);

        modifiersPanel.setLayout(new java.awt.GridBagLayout());

        insertionPointLabel.setLabelFor(insertPointCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/refactoring/introduce/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(insertionPointLabel, bundle.getString("LBL_InsertPoint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        modifiersPanel.add(insertionPointLabel, gridBagConstraints);

        insertPointCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertPointComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        modifiersPanel.add(insertPointCombo, gridBagConstraints);
        insertPointCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_modifiersCombo")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(modifiersPanel, gridBagConstraints);

        eastPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 11, 1, 1));
        buttonsPanel.setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(westPanel, gridBagConstraints);

        paramTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        paramTitle.setLabelFor(paramTable);
        org.openide.awt.Mnemonics.setLocalizedText(paramTitle, bundle.getString("LBL_ChangeParsParameters")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(paramTitle, gridBagConstraints);

        previewChange.setLabelFor(previewEditorPane);
        org.openide.awt.Mnemonics.setLocalizedText(previewChange, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_ChangeParsPreview")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(previewChange, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(110, 70));

        previewEditorPane.setEditable(false);
        jScrollPane1.setViewportView(previewEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 0.3;
        add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(errorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void insertPointComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertPointComboActionPerformed
        updateSignaturePreview();
    }//GEN-LAST:event_insertPointComboActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        doMove(1);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        doMove(-1);
    }//GEN-LAST:event_moveUpButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel eastPanel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JComboBox insertPointCombo;
    private javax.swing.JLabel insertionPointLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblName;
    private javax.swing.JPanel modifiersPanel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JTextField name;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel paramTitle;
    private javax.swing.JLabel previewChange;
    private javax.swing.JEditorPane previewEditorPane;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
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

                } else {
                    moveDownButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
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
                if (selectedRows.length > 0) {
                    int min = selectedRows[0];
                    int max = selectedRows[selectedRows.length - 1];
                    setButtons(min, max);
                }

                // update preview
                updateSignaturePreview();
            }
        };
    }

    private void initTableData() {
        boolean isC = res.isC();
        CsmCacheManager.enter();
        try {
            for(IntroduceMethodRefactoring.VariableContext info : res.getImportantVariables()) {
                model.addRow(getParameterRow(info, isC));
            }
        } finally {
            CsmCacheManager.leave();
        }
    }

    private static Object[] getParameterRow(IntroduceMethodRefactoring.VariableContext info, boolean isC) {
        CsmVariable variable = info.getVariable();
        CsmType desc = variable.getType();
        if (isC) {
            CharSequence typeText = CsmInstantiationProvider.getDefault().getInstantiatedText(desc);
            if (isC) {
                CsmClassifier classifier = desc.getClassifier();
                if (classifier != null) {
                    if (classifier.getKind() == CsmDeclaration.Kind.STRUCT && !CharSequenceUtils.startsWith(typeText, "struct")) { //NOI18N
                        typeText = "struct " + typeText; //NOI18N
                    }
                }
            }
            String typeRepresentation = typeText.toString();
            return new Object[] { info.isWriteAccessInside(), variable.getName().toString(), typeRepresentation};
        } else {
            if (desc.isReference()) {
                TypeDescriptor typeDescriptor = new CsmTypes.TypeDescriptor(desc.isConst(), desc.isVolatile(), TypeDescriptor.NON_REFERENCE, desc.getPointerDepth(), desc.getArrayDepth());
                CsmType createType = CsmTypes.createType(desc, typeDescriptor);
                return new Object[] { true, variable.getName().toString(), createType.getCanonicalText().toString()};
            } else if (desc.getArrayDepth() > 0) {
                TypeDescriptor typeDescriptor = new CsmTypes.TypeDescriptor(false, desc.isVolatile(), TypeDescriptor.NON_REFERENCE, desc.getPointerDepth(), desc.getArrayDepth());
                CsmType createType = CsmTypes.createType(desc, typeDescriptor);
                return new Object[] { false, variable.getName().toString(), createType.getCanonicalText().toString()};
            } else {
                TypeDescriptor typeDescriptor = new CsmTypes.TypeDescriptor(desc.isConst(), desc.isVolatile(), TypeDescriptor.NON_REFERENCE, desc.getPointerDepth(), desc.getArrayDepth());
                CsmType createType = CsmTypes.createType(desc, typeDescriptor);
                if (desc.isBuiltInBased(true) && !info.isWriteAccessInside()) {
                    return new Object[] { false, variable.getName().toString(), createType.getCanonicalText().toString()};
                }
                if (desc.isPointer() && !info.isWriteAccessInside()) {
                    return new Object[] { false, variable.getName().toString(), createType.getCanonicalText().toString()};
                }
                return new Object[] { true, variable.getName().toString(), createType.getCanonicalText().toString()};
            }
        }
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
        moveUpButton.setEnabled(min > 0);
        moveDownButton.setEnabled(max < r);
    }
    
    private void initRenderer() {
        TableColumnModel tcm = paramTable.getColumnModel();
        Enumeration columns = paramTable.getColumnModel().getColumns();
        TableColumn tc;
        int i = 0;
        while (columns.hasMoreElements()) {
            tc = (TableColumn) columns.nextElement();
            switch (i) {
                case IntroduceMethodRefactoring.PARAM_BY_REF:
                    tc.setPreferredWidth(40);
                    tc.setMaxWidth(60);
                    break;
                case IntroduceMethodRefactoring.PARAM_NAME:
                    tc.setPreferredWidth(100);
                    break;
                case IntroduceMethodRefactoring.PARAM_TYPE:
                    tc.setPreferredWidth(200);
                    break;
                default:
            }
            i++;
        }
        paramTable.setDefaultRenderer(String.class, new ParamRenderer());
        paramTable.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
    }

    String getMethodDefinition() {
        try {
            String text = res.getDocument().getText(res.getSelectionFrom(), res.getSelectionTo() - res.getSelectionFrom());
            ArrayList<Diff> diffs = new ArrayList<>();
            if (res.isC()) {
                @SuppressWarnings("unchecked")
                Vector<Vector> data = model.getDataVector();
                List<?>[] parameters = data.toArray(new List[0]);
                for (int i = 0; i < parameters.length; i++) {
                    if ((Boolean)parameters[i].get(IntroduceMethodRefactoring.PARAM_BY_REF)) {
                        String name = (String) parameters[i].get(IntroduceMethodRefactoring.PARAM_NAME);
                        for(IntroduceMethodRefactoring.VariableContext varInfo : res.getImportantVariables()) {
                            if (name.equals(varInfo.getVariable().getName().toString())) {
                                for(CsmReference reference : varInfo.getReferences()) {
                                    if (res.getSelectionFrom() <= reference.getStartOffset() && reference.getEndOffset() <= res.getSelectionTo()) {
                                        diffs.add(new Diff(reference.getStartOffset()-res.getSelectionFrom(), name.length(), "(*"+name+")")); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (diffs.size() > 0) {
                Collections.sort(diffs);
                StringBuilder buf = new StringBuilder(text);
                for(Diff diff : diffs) {
                    buf.replace(diff.start, diff.start + diff.len, diff.to);
                }
                text = buf.toString();
            }
            return "\n"+genDeclarationString()+"{\n"+text+"\n}\n"; //NOI18N
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

    int getInsertPoint() {
        if (res.getFunctionKind() == FunctionKind.MethodDefinition){
            CsmFunction functionDeclaration = res.getFunctionDeclaration();
            InsertPoint insertPoint = (InsertPoint) insertPointCombo.getSelectedItem();
            if (insertPoint.getContainerClass() == null) {
                // default
                return functionDeclaration.getEndOffset();
            } else if (insertPoint.getIndex() == Integer.MIN_VALUE) {
                // at the beginning class
                return insertPoint.getContainerClass().getLeftBracketOffset()+1;
            } else if (insertPoint.getIndex() == Integer.MAX_VALUE) {
                // at the end class
                return insertPoint.getContainerClass().getEndOffset()-1;
            } else {
                return insertPoint.getElementDeclaration().getEndOffset();
            }
        } else {
            return 0;
        }
    }

    String getFunctionName() {
        return name.getText();
    }

    String getMethodCall() {
        StringBuilder buf = new StringBuilder();
        buf.append(name.getText());
        buf.append('('); // NOI18N
        // generate parameters to the preview string
        @SuppressWarnings("unchecked")
        Vector<Vector> data = model.getDataVector();
        List<?>[] parameters = data.toArray(new List[0]);
        for (int i = 0; i < parameters.length; i++) {
            if ((Boolean)parameters[i].get(IntroduceMethodRefactoring.PARAM_BY_REF)) {
                if (res.isC()) {
                    buf.append('&');
                }
            }
            buf.append(parameters[i].get(IntroduceMethodRefactoring.PARAM_NAME));
            if (i < parameters.length - 1) {
                buf.append(',').append(' '); // NOI18N
            }
        }
        buf.append(')'); //NOI18N
        buf.append(';'); //NOI18N
        
        return buf.toString();
    }

    private void updateSignaturePreview() {
        previewEditorPane.setText(genWrapDeclarationString());
        previewEditorPane.setCaretPosition(0);
    }

    private String genWrapDeclarationString() {
        String decl = genDeclarationString();
        StringBuilder buf = new StringBuilder();
        int col = 0;
        for(int i = 0; i < decl.length(); i++) {
            buf.append(decl.charAt(i));
            col++;
            if (col > 50) {
                if (decl.charAt(i) == ',') {
                    buf.append('\n');
                    col = 0;
                }
            }
        }
        return buf.toString();
    }

    private String genDeclarationString() {
        StringBuilder buf = new StringBuilder();

        // generate the return type for the method and name
        if (res.getFunctionKind() == FunctionKind.Function || res.getFunctionKind() == FunctionKind.MethodDeclarationDefinition) {
            buf.append(returnTypeTextField.getText());
            buf.append(' '); // NOI18N
        } else {
            createDefinitionQualifiedName(res.getFunctionDeclaration(), res.getEnclosingClass(), returnTypeTextField.getText(), res.getInsertScope(), buf);
        }
        buf.append(name.getText());
        buf.append('('); // NOI18N
        // generate parameters to the preview string
        @SuppressWarnings("unchecked")
        Vector<Vector> data = model.getDataVector();
        List<?>[] parameters = data.toArray(new List[0]);
        for (int i = 0; i < parameters.length; i++) {
            buf.append(parameters[i].get(IntroduceMethodRefactoring.PARAM_TYPE));
            if ((Boolean)parameters[i].get(IntroduceMethodRefactoring.PARAM_BY_REF)) {
                if (res.isC()) {
                    buf.append('*');
                } else {
                    buf.append('&');
                }
            }
            buf.append(' ');
            buf.append(parameters[i].get(IntroduceMethodRefactoring.PARAM_NAME));
            if (i < parameters.length - 1) {
                buf.append(',').append(' '); // NOI18N
            }
        }
        buf.append(')'); //NOI18N
        return buf.toString();
    }

    String getMethodDeclarationString() {
        StringBuilder buf = new StringBuilder("\n"); // NOI18N
        buf.append(returnTypeTextField.getText());
        buf.append(' '); // NOI18N
        buf.append(name.getText());
        buf.append('('); // NOI18N
        // generate parameters to the preview string
        @SuppressWarnings("unchecked")
        Vector<Vector> data = model.getDataVector();
        List<?>[] parameters = data.toArray(new List[0]);
        for (int i = 0; i < parameters.length; i++) {
            buf.append(parameters[i].get(IntroduceMethodRefactoring.PARAM_TYPE));
            if ((Boolean)parameters[i].get(IntroduceMethodRefactoring.PARAM_BY_REF)) {
                if (res.isC()) {
                    buf.append('*');
                } else {
                    buf.append('&');
                }
            }
            buf.append(' ');
            buf.append(parameters[i].get(IntroduceMethodRefactoring.PARAM_NAME));
            if (i < parameters.length - 1) {
                buf.append(',').append(' '); // NOI18N
            }
        }
        buf.append(");\n"); //NOI18N
        return buf.toString();
    }

    private static void createDefinitionQualifiedName(CsmFunction item, CsmClass parent, String returnType, CsmScope insertScope, StringBuilder buf) {
        addTemplate(item, parent, buf);
        buf.append(returnType);
        buf.append(' ');
        String scope = getQualifiedName(insertScope, parent);
        if (scope.isEmpty()) {
            buf.append(parent.getName());
        } else {
            buf.append(scope);
        }
        if (CsmKindUtilities.isTemplate(parent)) {
            final CsmTemplate template = (CsmTemplate)parent;
            List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
            if (templateParameters.size() > 0) {
                buf.append("<");//NOI18N
                boolean first = true;
                for(CsmTemplateParameter param : templateParameters) {
                    if (!first) {
                        buf.append(", "); //NOI18N
                    }
                    first = false;
                    buf.append(param.getName());
                }
                buf.append(">");//NOI18N
            }
        }
        buf.append("::"); //NOI18N
    }

    private static String getQualifiedName(CsmScope from, CsmScope to) {
        List<CsmScope> scopes = new ArrayList<>();
        while (!Objects.equals(from, to) && CsmKindUtilities.isScopeElement(to)) {
            scopes.add(0, to);
            to = ((CsmScopeElement) to).getScope();
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (CsmScope scope : scopes) {
            if (CsmKindUtilities.isNamedElement(scope)) {
                CsmNamedElement named = (CsmNamedElement) scope;
                if (!CharSequenceUtils.isNullOrEmpty(named.getName())) {
                    if (!first) {
                        sb.append("::"); // NOI18N
                    } else {
                        first = false;
                    }
                    // TODO: handle instantiations here
                    sb.append(named.getName());
                }
            }
        }
        return sb.toString();
    }

    private static void addTemplate(CsmFunction item, CsmClass parent, StringBuilder buf) {
        if (CsmKindUtilities.isTemplate(parent)) {
            final CsmTemplate template = (CsmTemplate)parent;
            List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
            if (templateParameters.size() > 0) {
                buf.append("template<");//NOI18N
                boolean first = true;
                for(CsmTemplateParameter param : templateParameters) {
                    if (!first) {
                        buf.append(", "); //NOI18N
                    }
                    first = false;
                    buf.append(param.getText());
                }
                buf.append(">\n");//NOI18N
            }
        }
        if (CsmKindUtilities.isTemplate(item)) {
            final CsmTemplate template = (CsmTemplate)item;
            List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
            if (templateParameters.size() > 0) {
                buf.append("template<");//NOI18N
                boolean first = true;
                for(CsmTemplateParameter param : templateParameters) {
                    if (!first) {
                        buf.append(", "); //NOI18N
                    }
                    first = false;
                    buf.append(param.getText());
                }
                buf.append(">\n");//NOI18N
            }
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(IntroduceMethodPanel.class, key);
    }

    private Action getEditAction() {
        if (editAction == null) {
            editAction = new EditAction();
        }
        return editAction;
    }

    private void autoEdit(JTable tab) {
        if (tab.editCellAt(tab.getSelectedRow(), tab.getSelectedColumn(), null) &&
                tab.getEditorComponent() != null) {
            if (tab.getEditorComponent() instanceof JTextField) {
                JTextField field = (JTextField) tab.getEditorComponent();
                field.setCaretPosition(field.getText().length());
                field.requestFocusInWindow();
                field.selectAll();
            } else if (tab.getEditorComponent() instanceof JCheckBox) {
                JCheckBox field = (JCheckBox) tab.getEditorComponent();
                field.setSelected(!field.isSelected());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    // this class is used for marking rows as read-only. If the user uses
    // standard DefaultTableModel, rows added through its methods is added
    // as a read-write. -- Use methods with Boolean paramater to add
    // rows marked as read-only.
    private static class ParamTableModel extends DefaultTableModel {
        
        public ParamTableModel(Object[] data, int rowCount) {
            super(data, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == IntroduceMethodRefactoring.PARAM_BY_REF) {
                return true;
            }
            if (column == IntroduceMethodRefactoring.PARAM_NAME) {
                return false;
            }
            if (column == IntroduceMethodRefactoring.PARAM_TYPE) {
                return true;
            }
            return false;
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (column == IntroduceMethodRefactoring.PARAM_NAME || column == IntroduceMethodRefactoring.PARAM_TYPE) {
                if (aValue instanceof String) {
                    aValue = ((String)aValue).trim();
                }
            }
            super.setValueAt(aValue, row, column);
        }
    } // end ParamTableModel

    private class EditAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent ae) {
            autoEdit((JTable) ae.getSource());
        }
    }
    
    private class ParamRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        private final Color origBackground;
        private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public ParamRenderer() {
            setOpaque(true);
            origBackground = getBackground();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table,  value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(origBackground);
            }
            setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            return this;
        }
    }

    private static class Diff implements Comparable<Diff>{
        private int start;
        private int len;
        private String to;

        public Diff(int start, int len, String to) {
            this.start = start;
            this.len = len;
            this.to = to;
        }

        @Override
        public int compareTo(Diff o) {
            return o.start - start;
        }
    }
}
