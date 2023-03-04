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

/*
 * VariableFormatterEditPanel.java
 *
 * Created on Apr 3, 2009, 10:20:57 AM
 */

package org.netbeans.modules.debugger.jpda.ui.options;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public class VariableFormatterEditPanel extends javax.swing.JPanel {

    /** Creates new form VariableFormatterEditPanel */
    public VariableFormatterEditPanel() {
        initComponents();
        initChildrenVariablesTable();
    }

    public void load(VariablesFormatter f) {
        nameTextField.setText(f.getName());
        classTypesTextField.setText(f.getClassTypesCommaSeparated());
        subtypesCheckBox.setSelected(f.isIncludeSubTypes());
        valueFormatCheckBox.setSelected(f.getValueFormatCode().trim().length() > 0);
        valueEditorPane.setText(f.getValueFormatCode());
        childrenFormatCheckBox.setSelected(f.getChildrenFormatCode().trim().length() > 0 ||
                                           f.getChildrenVariables().size() > 0);
        childrenCodeEditorPane.setText(f.getChildrenFormatCode());
        Map<String, String> childrenVariables = f.getChildrenVariables();
        int n = childrenVariables.size();
        Iterator<Map.Entry<String, String>> childrenVariablesEntries = childrenVariables.entrySet().iterator();
        String[][] tableData = new String[n][2];
        for (int i = 0; i < n; i++) {
            Map.Entry<String, String> e = childrenVariablesEntries.next();
            tableData[i][0] = e.getKey();
            tableData[i][1] = e.getValue();
        }
        DefaultTableModel childrenVarsModel = new DefaultTableModel(tableData, tableColumnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        childrenVariablesTable.setModel(childrenVarsModel);
        DisablingCellRenderer.apply(childrenVariablesTable);
        childrenCodeRadioButton.setSelected(!f.isUseChildrenVariables());
        childrenVariablesRadioButton.setSelected(f.isUseChildrenVariables());
        testChildrenCheckBox.setSelected(f.getChildrenExpandTestCode().trim().length() > 0);
        testChildrenEditorPane.setText(f.getChildrenExpandTestCode());
        valueFormatCheckBoxActionPerformed(null);
        childrenFormatCheckBoxActionPerformed(null);
        nameTextField.requestFocusInWindow();
    }

    public void store(VariablesFormatter f) {
        f.setName(nameTextField.getText());
        f.setClassTypes(classTypesTextField.getText());
        f.setIncludeSubTypes(subtypesCheckBox.isSelected());
        f.setValueFormatCode(valueFormatCheckBox.isSelected() ? valueEditorPane.getText() : "");
        f.setChildrenFormatCode(childrenCodeEditorPane.getText());
        TableModel tableModel = childrenVariablesTable.getModel();
        f.getChildrenVariables().clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            f.addChildrenVariable((String) tableModel.getValueAt(i, 0), (String) tableModel.getValueAt(i, 1));
        }
        f.setUseChildrenVariables(childrenVariablesRadioButton.isSelected());
        f.setChildrenExpandTestCode(testChildrenCheckBox.isSelected() ? testChildrenEditorPane.getText() : "");
    }

    void setFormatterNames(Set<String> formatterNames) {
        this.formatterNames = formatterNames;
    }

    void setValidityObjects(DialogDescriptor validityDescriptor,
                            NotificationLineSupport validityNotificationSupport,
                            boolean continualValidityChecks) {
        this.validityDescriptor = validityDescriptor;
        this.validityNotificationSupport = validityNotificationSupport;
        this.continualValidityChecks = continualValidityChecks;
        attachValidityChecks();
    }

    private void attachValidityChecks() {
        DocumentListener validityDocumentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkValid();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValid();
            }
            public void changedUpdate(DocumentEvent e) {
                checkValid();
            }
        };
        nameTextField.getDocument().addDocumentListener(validityDocumentListener);
        classTypesTextField.getDocument().addDocumentListener(validityDocumentListener);
        checkValid();
    }

    private boolean checkValidName() {
        String name = nameTextField.getText().trim();
        if (name.length() == 0) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_EmptyFormatterName"));
            return false;
        } else if (formatterNames.contains(name)) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_ExistingFormatterName"));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkValidClasses() {
        String name = classTypesTextField.getText().trim();
        if (name.length() == 0) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_EmptyClassName"));
            return false;
        } else {
            int i = 0;
            char c = name.charAt(i);
            if (Character.isJavaIdentifierStart(c)) {
                boolean start = true;
                for (i++; i < name.length(); i++) {
                    c = name.charAt(i);
                    if (c == ',' || Character.isWhitespace(c)) {
                        start = true;
                        continue;
                    }
                    if (start && !Character.isJavaIdentifierStart(c) || !start && !Character.isJavaIdentifierPart(c) && c != '.') {
                        break;
                    }
                    start = false;
                }
            }
            if (i < name.length()) {
                validityDescriptor.setValid(false);
                validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_InvalidClassNameAtPos", (i+1)));
                return false;
            }
            return true;
        }
    }

    private void checkFormatterSelected() {
        boolean is = valueFormatCheckBox.isSelected() || childrenFormatCheckBox.isSelected();
        if (is) {
            validityDescriptor.setValid(true);
            validityNotificationSupport.clearMessages();
        } else {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_NoFormatSelected"));
        }
    }

    private void checkValid() {
        if (validityNotificationSupport == null || !continualValidityChecks) {
            return ;
        }
        if (checkValidName() && checkValidClasses()) {
            checkFormatterSelected();
        }
    }

    public boolean checkValidInput() {
        continualValidityChecks = true;
        checkValid();
        if (!validityDescriptor.isValid()) {
            if (!checkValidName()) {
                nameTextField.requestFocusInWindow();
            } else if (!checkValidClasses()) {
                classTypesTextField.requestFocusInWindow();
            } else {
                valueFormatCheckBox.requestFocusInWindow();
            }
            return false;
        } else {
            return true;
        }
    }

    static Color getDisabledFieldBackground() {
        JTextField disabledField = new JTextField();
        disabledField.setEditable(false);
        disabledField.setEnabled(false);
        return disabledField.getBackground();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        childrenButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        classTypesLabel = new javax.swing.JLabel();
        classTypesTextField = new javax.swing.JTextField();
        subtypesCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        valueFormatCheckBox = new javax.swing.JCheckBox();
        valueScrollPane = new javax.swing.JScrollPane();
        valueEditorPane = new javax.swing.JEditorPane();
        childrenFormatCheckBox = new javax.swing.JCheckBox();
        childrenCodeRadioButton = new javax.swing.JRadioButton();
        childrenCodeScrollPane = new javax.swing.JScrollPane();
        childrenCodeEditorPane = new javax.swing.JEditorPane();
        childrenVariablesRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        childrenVariablesTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        addVarButton = new javax.swing.JButton();
        removeVarButton = new javax.swing.JButton();
        moveUpVarButton = new javax.swing.JButton();
        moveDownVarButton = new javax.swing.JButton();
        testChildrenCheckBox = new javax.swing.JCheckBox();
        testChildrenScrollPane = new javax.swing.JScrollPane();
        testChildrenEditorPane = new javax.swing.JEditorPane();

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.nameTextField.text")); // NOI18N

        classTypesLabel.setLabelFor(classTypesTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classTypesLabel, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesLabel.text")); // NOI18N

        classTypesTextField.setText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesTextField.text")); // NOI18N
        classTypesTextField.setToolTipText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesLabel.tooltip")); // NOI18N

        subtypesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(subtypesCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.subtypesCheckBox.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(classTypesLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(classTypesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(subtypesCheckBox))
                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classTypesLabel)
                    .addComponent(classTypesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subtypesCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        classTypesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        subtypesCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.subtypesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(valueFormatCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.valueFormatCheckBox.text")); // NOI18N
        valueFormatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFormatCheckBoxActionPerformed(evt);
            }
        });

        valueScrollPane.setViewportView(valueEditorPane);

        org.openide.awt.Mnemonics.setLocalizedText(childrenFormatCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenFormatCheckBox.text")); // NOI18N
        childrenFormatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenFormatCheckBoxActionPerformed(evt);
            }
        });

        childrenButtonGroup.add(childrenCodeRadioButton);
        childrenCodeRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(childrenCodeRadioButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenCodeRadioButton.text")); // NOI18N
        childrenCodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenCodeRadioButtonActionPerformed(evt);
            }
        });

        childrenCodeScrollPane.setViewportView(childrenCodeEditorPane);

        childrenButtonGroup.add(childrenVariablesRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(childrenVariablesRadioButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesRadioButton.text")); // NOI18N
        childrenVariablesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenVariablesRadioButtonActionPerformed(evt);
            }
        });

        childrenVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(childrenVariablesTable);
        childrenVariablesTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesTable.a11y.name")); // NOI18N
        childrenVariablesTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesTable.a11y.description")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.addVarButton.text")); // NOI18N
        addVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.removeVarButton.text")); // NOI18N
        removeVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveUpVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveUpVarButton.text")); // NOI18N
        moveUpVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveDownVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveDownVarButton.text")); // NOI18N
        moveDownVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownVarButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addVarButton, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(removeVarButton)
            .addComponent(moveUpVarButton)
            .addComponent(moveDownVarButton)
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addVarButton, moveDownVarButton, moveUpVarButton, removeVarButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(addVarButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeVarButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(moveUpVarButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(moveDownVarButton))
        );

        addVarButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.addVarButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeVarButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.removeVarButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveUpVarButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveUpVarButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveDownVarButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveDownVarButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testChildrenCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.testChildrenCheckBox.text")); // NOI18N
        testChildrenCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.testChildrenCheckBox.tooltip")); // NOI18N
        testChildrenCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testChildrenCheckBoxActionPerformed(evt);
            }
        });

        testChildrenScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        testChildrenScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        testChildrenScrollPane.setViewportView(testChildrenEditorPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(valueFormatCheckBox)
                .addContainerGap(235, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(valueScrollPane))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(childrenFormatCheckBox)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(testChildrenScrollPane))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(testChildrenCheckBox)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(childrenVariablesRadioButton)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(childrenCodeScrollPane))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(childrenCodeRadioButton)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(valueFormatCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valueScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenFormatCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenCodeRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenCodeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenVariablesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testChildrenCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testChildrenScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        valueFormatCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.valueFormatCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        childrenFormatCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenFormatCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        childrenCodeRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenCodeRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        childrenVariablesRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVarButtonActionPerformed
        final DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        model.addRow(new Object[] { "", "" });
        final int index = model.getRowCount() - 1;
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index, index);
        childrenVariablesTable.editCellAt(index, 0);
        childrenVariablesTable.requestFocus();
         //DefaultCellEditor ed = (DefaultCellEditor)
        childrenVariablesTable.getCellEditor(index, 0).shouldSelectCell(
                new ListSelectionEvent(childrenVariablesTable,
                                       index, index, true));
        addVarButton.setEnabled(false);
        removeVarButton.setEnabled(false);
        childrenVariablesTable.getCellEditor(index, 0).addCellEditorListener(new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String value = (String) model.getValueAt(index, 0);
                        if (value.trim().length() == 0) {
                            model.removeRow(index);
                        }
                    }
                });
                childrenVariablesTable.getCellEditor(index, 0).removeCellEditorListener(this);
                addVarButton.setEnabled(true);
                removeVarButton.setEnabled(childrenVariablesTable.getSelectedRow() >= 0);
            }

            public void editingCanceled(ChangeEvent e) {
                model.removeRow(index);
                childrenVariablesTable.getCellEditor(index, 0).removeCellEditorListener(this);
                addVarButton.setEnabled(true);
                removeVarButton.setEnabled(childrenVariablesTable.getSelectedRow() >= 0);
            }
        });
    }//GEN-LAST:event_addVarButtonActionPerformed

    private void removeVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index < 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        model.removeRow(index);
        if (index < childrenVariablesTable.getRowCount() || --index >= 0) {
            childrenVariablesTable.setRowSelectionInterval(index, index);
        }
    }//GEN-LAST:event_removeVarButtonActionPerformed

    private void moveUpVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index <= 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        Object[] row = new Object[] { model.getValueAt(index, 0), model.getValueAt(index, 1) };
        model.removeRow(index);
        model.insertRow(index - 1, row);
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
    }//GEN-LAST:event_moveUpVarButtonActionPerformed

    private void moveDownVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index < 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        if (index >= (model.getRowCount() - 1)) return ;
        Object[] row = new Object[] { model.getValueAt(index, 0), model.getValueAt(index, 1) };
        model.removeRow(index);
        model.insertRow(index + 1, row);
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index + 1, index + 1);
    }//GEN-LAST:event_moveDownVarButtonActionPerformed

    private void valueFormatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFormatCheckBoxActionPerformed
        if (valueFormatCheckBox.isSelected()) {
            valueEditorPane.setEnabled(true);
            valueEditorPane.setBackground(nameTextField.getBackground());
            valueEditorPane.requestFocusInWindow();
        } else {
            valueEditorPane.setEnabled(false);
            valueEditorPane.setBackground(getDisabledFieldBackground());
        }
        checkValid();
    }//GEN-LAST:event_valueFormatCheckBoxActionPerformed

    private void childrenFormatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenFormatCheckBoxActionPerformed
        boolean selected = childrenFormatCheckBox.isSelected();
        childrenCodeRadioButton.setEnabled(selected);
        childrenVariablesRadioButton.setEnabled(selected);
        testChildrenCheckBox.setEnabled(selected);
        childrenCodeRadioButtonActionPerformed(null);
        childrenVariablesRadioButtonActionPerformed(null);
        testChildrenCheckBoxActionPerformed(null);
        checkValid();
    }//GEN-LAST:event_childrenFormatCheckBoxActionPerformed

    private void childrenCodeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenCodeRadioButtonActionPerformed
        if (childrenCodeRadioButton.isSelected() && childrenCodeRadioButton.isEnabled()) {
            childrenCodeEditorPane.setEnabled(true);
            childrenCodeEditorPane.setBackground(nameTextField.getBackground());
            childrenCodeEditorPane.requestFocusInWindow();
        }
        if (!childrenVariablesRadioButton.isSelected() || !childrenVariablesRadioButton.isEnabled()) {
            childrenVariablesTable.getSelectionModel().clearSelection();
            if (childrenVariablesTable.isEditing()) {
                childrenVariablesTable.getCellEditor().stopCellEditing();
            }
            childrenVariablesTable.setEnabled(false);
            addVarButton.setEnabled(false);
            removeVarButton.setEnabled(false);
            moveUpVarButton.setEnabled(false);
            moveDownVarButton.setEnabled(false);
        }
    }//GEN-LAST:event_childrenCodeRadioButtonActionPerformed

    private void childrenVariablesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenVariablesRadioButtonActionPerformed
        if (childrenVariablesRadioButton.isSelected() && childrenVariablesRadioButton.isEnabled()) {
            childrenVariablesTable.setEnabled(true);
            childrenVariablesTable.requestFocusInWindow();            
            addVarButton.setEnabled(true);
            int row = childrenVariablesTable.getSelectedRow();
            removeVarButton.setEnabled(row >= 0);
            moveUpVarButton.setEnabled(row > 0);
            moveDownVarButton.setEnabled(row >= 0 && row < childrenVariablesTable.getRowCount() - 1);
        }
        if (!childrenCodeRadioButton.isSelected() || !childrenCodeRadioButton.isEnabled()) {
            childrenCodeEditorPane.setEnabled(false);
            childrenCodeEditorPane.setBackground(getDisabledFieldBackground());
        }
    }//GEN-LAST:event_childrenVariablesRadioButtonActionPerformed

    private void testChildrenCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testChildrenCheckBoxActionPerformed
        if (testChildrenCheckBox.isSelected() && testChildrenCheckBox.isEnabled()) {
            testChildrenEditorPane.setEnabled(true);
            testChildrenEditorPane.setBackground(nameTextField.getBackground());
            testChildrenEditorPane.requestFocusInWindow();
        } else {
            testChildrenEditorPane.setEnabled(false);
            testChildrenEditorPane.setBackground(getDisabledFieldBackground());
        }
    }//GEN-LAST:event_testChildrenCheckBoxActionPerformed

    private void initChildrenVariablesTable() {
        removeVarButton.setEnabled(false);
        moveUpVarButton.setEnabled(false);
        moveDownVarButton.setEnabled(false);
        childrenVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = childrenVariablesTable.getSelectedRow();
                removeVarButton.setEnabled(row >= 0);
                moveUpVarButton.setEnabled(row > 0);
                moveDownVarButton.setEnabled(row >= 0 && row < childrenVariablesTable.getRowCount() - 1);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addVarButton;
    private javax.swing.ButtonGroup childrenButtonGroup;
    private javax.swing.JEditorPane childrenCodeEditorPane;
    private javax.swing.JRadioButton childrenCodeRadioButton;
    private javax.swing.JScrollPane childrenCodeScrollPane;
    private javax.swing.JCheckBox childrenFormatCheckBox;
    private javax.swing.JRadioButton childrenVariablesRadioButton;
    private javax.swing.JTable childrenVariablesTable;
    private javax.swing.JLabel classTypesLabel;
    private javax.swing.JTextField classTypesTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton moveDownVarButton;
    private javax.swing.JButton moveUpVarButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeVarButton;
    private javax.swing.JCheckBox subtypesCheckBox;
    private javax.swing.JCheckBox testChildrenCheckBox;
    private javax.swing.JEditorPane testChildrenEditorPane;
    private javax.swing.JScrollPane testChildrenScrollPane;
    private javax.swing.JEditorPane valueEditorPane;
    private javax.swing.JCheckBox valueFormatCheckBox;
    private javax.swing.JScrollPane valueScrollPane;
    // End of variables declaration//GEN-END:variables
    private final String[] tableColumnNames = new String[] {
        NbBundle.getMessage(CategoryPanelFormatters.class, "CategoryPanelFormatters.formatChildrenListTable.Name"),
        NbBundle.getMessage(CategoryPanelFormatters.class, "CategoryPanelFormatters.formatChildrenListTable.Value")
    };
    private Set<String> formatterNames;
    private DialogDescriptor validityDescriptor;
    private NotificationLineSupport validityNotificationSupport;
    private boolean continualValidityChecks = false;

}
