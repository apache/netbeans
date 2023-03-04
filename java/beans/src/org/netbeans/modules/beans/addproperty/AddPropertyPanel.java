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
package org.netbeans.modules.beans.addproperty;

import java.awt.Rectangle;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A simple GUI for Add Property action.
 *
 * @author  Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddPropertyPanel extends javax.swing.JPanel {

    private boolean propNameModified = false;
    private boolean generatePCSModified = false;
    private boolean generateVCSModified = false;
    
    private DocumentListener propNameTextFieldDocumentListener;
    private FileObject file;
    private String className;
    private final CodeStyle cs;
    private List<String> existingFields;
    private String[] pcsName;
    private String[] vcsName;
    private JButton okButton;
    
    public AddPropertyPanel(FileObject file, String className, CodeStyle cs, List<String> existingFields, String[] pcsName, String[] vcsName, JButton okButton) {
        this.file = file;
        this.className = className;
        this.cs = cs;
        this.existingFields = existingFields;
        this.pcsName = pcsName;
        this.vcsName = vcsName;
        this.okButton = okButton;
        initComponents();
        previewScrollPane.putClientProperty(
                "HighlightsLayerExcludes", // NOI18N
                "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
                );

        DocumentListener documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                showPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                showPreview();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        nameTextField.getDocument().addDocumentListener(documentListener);
        ((JTextField) typeComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(documentListener);
        initializerTextField.getDocument().addDocumentListener(documentListener);

        propNameTextFieldDocumentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                propNameModified = true;
                showPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                propNameModified = true;
                showPreview();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        propNameTextField.getDocument().addDocumentListener(propNameTextFieldDocumentListener);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        previewEditorPane.setText("");
        generatePropertyChangeSupportCheckBox.setSelected(false);
        generateVetoablePropertyChangeSupportCheckBox.setSelected(false);
        showPreview();
    }

    private void showPreview() {
        if (!propNameModified) {
            propNameTextField.getDocument().removeDocumentListener(propNameTextFieldDocumentListener);
            propNameTextField.setText("PROP_" + nameTextField.getText().toUpperCase()); // NOI18N
            propNameTextField.getDocument().addDocumentListener(propNameTextFieldDocumentListener);
        }
        
        int i = staticCheckBox.isSelected() ? 1 : 0;
        
        generatePropertyChangeSupportCheckBox.setEnabled(pcsName[i] == null);
        generateVetoablePropertyChangeSupportCheckBox.setEnabled(vcsName[i] == null);
        
        if (staticCheckBox.isSelected()) {
            generatePropertyChangeSupportCheckBox.setSelected(false);
            generateVetoablePropertyChangeSupportCheckBox.setSelected(false);
            generatePropertyChangeSupportCheckBox.setEnabled(false);
            generateVetoablePropertyChangeSupportCheckBox.setEnabled(false);
        } else {
            if (!generatePCSModified && pcsName == null) {
                generatePropertyChangeSupportCheckBox.setEnabled(true);
                generatePropertyChangeSupportCheckBox.setSelected(boundCheckBox.isSelected());
            }

            if (!generateVCSModified && vcsName == null) {
                generateVetoablePropertyChangeSupportCheckBox.setEnabled(true);
                generateVetoablePropertyChangeSupportCheckBox.setSelected(vetoableCheckBox.isSelected());
            }
        }
        
        final String previewTemplate = new AddPropertyGenerator().generate(getAddPropertyConfig(), cs);
        previewEditorPane.setText(previewTemplate);
        
        String error = resolveError();
        
        if (error != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/error-glyph.gif"))); // NOI18N
            errorLabel.setText(error);
        }
        
        okButton.setEnabled(error == null);
        
        String warning = resolveWarning();
        
        if (warning != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/warning.gif"))); // NOI18N
            errorLabel.setText(warning);
        }
        
        errorLabel.setVisible(error != null || warning != null);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                previewEditorPane.setCaretPosition(0);
                previewEditorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });
    }
    
    private String resolveError() {
        if (nameTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldIsEmpty");
        }

        if (((JTextField) typeComboBox.getEditor().getEditorComponent()).getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_TypeIsEmpty");
        }
        
        if (boundCheckBox.isSelected() && propNameTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_BoundIsEmpty");
        }

        if (existingFields.contains(nameTextField.getText())) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(nameTextField.getText())});
        }
        
        if (boundCheckBox.isSelected() && existingFields.contains(propNameTextField.getText())) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(propNameTextField.getText())});
        }
        
        return null;
    }

    private String resolveWarning() {
        if (finalCheckBox.isSelected() && initializerTextField.getText().trim().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "WARN_FinalPropertyRequiresInitilizer");
        }
                
        if (staticCheckBox.isSelected() && boundCheckBox.isSelected() && pcsName[1] == null) {
            return NbBundle.getMessage(AddPropertyPanel.class, "WARN_CannotPCS");
        }
        
        if (staticCheckBox.isSelected() && boundCheckBox.isSelected() && vetoableCheckBox.isSelected() && vcsName[1] == null) {
            return NbBundle.getMessage(AddPropertyPanel.class, "WARN_CannotVCS");
        }
        
        return null;
    }
    
    public AddPropertyConfig getAddPropertyConfig() {
        final String type = typeComboBox.getSelectedItem().toString().trim();
        final String name = nameTextField.getText().trim();
        final String initializer = initializerTextField.getText().trim();
        AddPropertyConfig.ACCESS access = AddPropertyConfig.ACCESS.PACKAGE;
        if (privateRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PRIVATE;
        } else if (protectedRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PROTECTED;
        } else if (publicRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PUBLIC;
        }

        AddPropertyConfig.GENERATE generate = AddPropertyConfig.GENERATE.GETTER_AND_SETTER;
        if (generateGetterAndSetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.GETTER_AND_SETTER;
        } else if (generateGetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.GETTER;
        } else if (generateSetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.SETTER;
        }

        int i = staticCheckBox.isSelected() ? 1 : 0;
        AddPropertyConfig addPropertyConfig = new AddPropertyConfig(
                name, initializer, type, className, access, staticCheckBox.isSelected(), finalCheckBox.isSelected(), generate, generateJavadocCheckBox.isSelected(), boundCheckBox.isSelected(), propNameTextField.getText().trim(), vetoableCheckBox.isSelected(), indexedCheckBox.isSelected(), pcsName[i], vcsName[i], generatePropertyChangeSupportCheckBox.isSelected(), generateVetoablePropertyChangeSupportCheckBox.isSelected());
        return addPropertyConfig;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accessGroup = new javax.swing.ButtonGroup();
        getterSetterGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        equalsLabel = new javax.swing.JLabel();
        initializerTextField = new javax.swing.JTextField();
        semicolonLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        browseTypeButton = new javax.swing.JButton();
        privateRadioButton = new javax.swing.JRadioButton();
        packageRadioButton = new javax.swing.JRadioButton();
        protectedRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        staticCheckBox = new javax.swing.JCheckBox();
        finalCheckBox = new javax.swing.JCheckBox();
        generateGetterAndSetterRadioButton = new javax.swing.JRadioButton();
        generateGetterRadioButton = new javax.swing.JRadioButton();
        generateSetterRadioButton = new javax.swing.JRadioButton();
        generateJavadocCheckBox = new javax.swing.JCheckBox();
        boundCheckBox = new javax.swing.JCheckBox();
        propNameTextField = new javax.swing.JTextField();
        vetoableCheckBox = new javax.swing.JCheckBox();
        indexedCheckBox = new javax.swing.JCheckBox();
        generatePropertyChangeSupportCheckBox = new javax.swing.JCheckBox();
        generateVetoablePropertyChangeSupportCheckBox = new javax.swing.JCheckBox();
        previewLabel = new javax.swing.JLabel();
        previewScrollPane = new javax.swing.JScrollPane();
        previewEditorPane = new javax.swing.JEditorPane();
        errorLabel = new javax.swing.JLabel();

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameTextField.text")); // NOI18N

        equalsLabel.setLabelFor(initializerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(equalsLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.equalsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(semicolonLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.semicolonLabel.text")); // NOI18N

        typeLabel.setLabelFor(typeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.typeLabel.text")); // NOI18N

        typeComboBox.setEditable(true);
        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "String", "int", "boolean", "long", "double", "byte", "char", "short", "float" }));
        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseTypeButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.browseTypeButton.text")); // NOI18N
        browseTypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.browseTypeButton.toolTipText")); // NOI18N
        browseTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTypeButtonActionPerformed(evt);
            }
        });

        accessGroup.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(privateRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.privateRadioButton.text")); // NOI18N
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(packageRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(packageRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.packageRadioButton.text")); // NOI18N
        packageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(protectedRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(protectedRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.protectedRadioButton.text")); // NOI18N
        protectedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protectedRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(publicRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(publicRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.publicRadioButton.text")); // NOI18N
        publicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(staticCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.staticCheckBox.text")); // NOI18N
        staticCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(finalCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.finalCheckBox.text")); // NOI18N
        finalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalCheckBoxActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateGetterAndSetterRadioButton);
        generateGetterAndSetterRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateGetterAndSetterRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateGetterAndSetterRadioButton.text")); // NOI18N
        generateGetterAndSetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateGetterAndSetterRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateGetterRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(generateGetterRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateGetterRadioButton.text")); // NOI18N
        generateGetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateGetterRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateSetterRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(generateSetterRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateSetterRadioButton.text")); // NOI18N
        generateSetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSetterRadioButtonActionPerformed(evt);
            }
        });

        generateJavadocCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateJavadocCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateJavadocCheckBox.text")); // NOI18N
        generateJavadocCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateJavadocCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(boundCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.boundCheckBox.text")); // NOI18N
        boundCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundCheckBoxActionPerformed(evt);
            }
        });

        propNameTextField.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.propNameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vetoableCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.vetoableCheckBox.text")); // NOI18N
        vetoableCheckBox.setEnabled(false);
        vetoableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vetoableCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(indexedCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.indexedCheckBox.text")); // NOI18N
        indexedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexedCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(generatePropertyChangeSupportCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generatePropertyChangeSupportCheckBox.text")); // NOI18N
        generatePropertyChangeSupportCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatePropertyChangeSupportCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(generateVetoablePropertyChangeSupportCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateVetoablePropertyChangeSupportCheckBox.text")); // NOI18N
        generateVetoablePropertyChangeSupportCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateVetoablePropertyChangeSupportCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewLabel.text")); // NOI18N

        previewEditorPane.setContentType(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.contentType")); // NOI18N
        previewEditorPane.setEditable(false);
        previewEditorPane.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.text")); // NOI18N
        previewEditorPane.setToolTipText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.toolTipText")); // NOI18N
        previewScrollPane.setViewportView(previewEditorPane);

        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/error-glyph.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getBundle(AddPropertyPanel.class).getString("AddPropertyPanel.errorLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(typeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(staticCheckBox)
                                    .addComponent(privateRadioButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(finalCheckBox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(packageRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(protectedRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(publicRadioButton))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(boundCheckBox)
                                .addGap(35, 35, 35)
                                .addComponent(propNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                                .addGap(27, 27, 27))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(vetoableCheckBox))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(generateGetterAndSetterRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateGetterRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateSetterRadioButton))
                            .addComponent(generateJavadocCheckBox)
                            .addComponent(indexedCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(generatePropertyChangeSupportCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(generateVetoablePropertyChangeSupportCheckBox))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(typeComboBox, 0, 1, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(equalsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(initializerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(semicolonLabel))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(previewLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(errorLabel)
                            .addComponent(previewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {finalCheckBox, packageRadioButton, privateRadioButton, protectedRadioButton, publicRadioButton, staticCheckBox});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nameLabel, previewLabel, typeLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(semicolonLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(equalsLabel)
                    .addComponent(initializerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(browseTypeButton)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(packageRadioButton)
                            .addComponent(protectedRadioButton)
                            .addComponent(publicRadioButton)
                            .addComponent(privateRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(staticCheckBox))
                    .addComponent(finalCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateGetterAndSetterRadioButton)
                    .addComponent(generateSetterRadioButton)
                    .addComponent(generateGetterRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateJavadocCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(boundCheckBox)
                    .addComponent(propNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vetoableCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indexedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generatePropertyChangeSupportCheckBox)
                    .addComponent(generateVetoablePropertyChangeSupportCheckBox))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewLabel)
                    .addComponent(previewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(errorLabel)
                .addGap(17, 17, 17))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void finalCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalCheckBoxActionPerformed
        generateGetterAndSetterRadioButton.setEnabled(!finalCheckBox.isSelected());
        generateSetterRadioButton.setEnabled(!finalCheckBox.isSelected());
        if (finalCheckBox.isSelected()) {
            generateGetterRadioButton.setSelected(true);
        }
        showPreview();
    }//GEN-LAST:event_finalCheckBoxActionPerformed

    private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_privateRadioButtonActionPerformed

    private void packageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_packageRadioButtonActionPerformed

    private void protectedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protectedRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_protectedRadioButtonActionPerformed

    private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_publicRadioButtonActionPerformed

    private void staticCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_staticCheckBoxActionPerformed

    private void indexedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexedCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_indexedCheckBoxActionPerformed

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void generateJavadocCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateJavadocCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_generateJavadocCheckBoxActionPerformed

    private boolean generatePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
            
    private void boundCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundCheckBoxActionPerformed
        vetoableCheckBox.setEnabled(boundCheckBox.isSelected());
        if (generatePropertyChangeSupportCheckBox.isEnabled ()) {
            if (boundCheckBox.isSelected ()) {
                if (!generatePropertyChangeSupportCheckBox.isSelected ()) {
                    generatePropertyChangeSupportCheckBox.setSelected (true);
                    generatePropertyChangeSupportCheckBoxWasSelectedAutomatically = true;
                }
                if (!generateVetoablePropertyChangeSupportCheckBox.isSelected () &&
                    vetoableCheckBox.isSelected ()
                ) {
                    generateVetoablePropertyChangeSupportCheckBox.setSelected (true);
                    generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically = true;
                }
            } else {
                if (generatePropertyChangeSupportCheckBoxWasSelectedAutomatically) {
                    generatePropertyChangeSupportCheckBox.setSelected (false);
                    generatePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
                }
                if (generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically) {
                    generateVetoablePropertyChangeSupportCheckBox.setSelected (false);
                    generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
                }
            }
        }
        showPreview();
    }//GEN-LAST:event_boundCheckBoxActionPerformed

    private boolean generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
    
    private void vetoableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vetoableCheckBoxActionPerformed
        if (generateVetoablePropertyChangeSupportCheckBox.isEnabled()) {
            if (vetoableCheckBox.isSelected()) {
                generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically =
                        !generateVetoablePropertyChangeSupportCheckBox.isSelected();
                generateVetoablePropertyChangeSupportCheckBox.setSelected(true);
            } else if (generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically) {
                generateVetoablePropertyChangeSupportCheckBox.setSelected(false);
            }
        }
        showPreview();
    }//GEN-LAST:event_vetoableCheckBoxActionPerformed

    private void generateGetterAndSetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateGetterAndSetterRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_generateGetterAndSetterRadioButtonActionPerformed

    private void generateSetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSetterRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_generateSetterRadioButtonActionPerformed

    private void generateGetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateGetterRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_generateGetterRadioButtonActionPerformed

    private void generateVetoablePropertyChangeSupportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateVetoablePropertyChangeSupportCheckBoxActionPerformed
        generateVCSModified = true;
        generateVetoablePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
        showPreview();
    }//GEN-LAST:event_generateVetoablePropertyChangeSupportCheckBoxActionPerformed

    private void generatePropertyChangeSupportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatePropertyChangeSupportCheckBoxActionPerformed
        generatePCSModified = true;
        generatePropertyChangeSupportCheckBoxWasSelectedAutomatically = false;
        showPreview();
    }//GEN-LAST:event_generatePropertyChangeSupportCheckBoxActionPerformed

    private void browseTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTypeButtonActionPerformed
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(file), null);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();

            typeComboBox.setSelectedItem(fqn);
        }
    }//GEN-LAST:event_browseTypeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JCheckBox boundCheckBox;
    private javax.swing.JButton browseTypeButton;
    private javax.swing.JLabel equalsLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox finalCheckBox;
    private javax.swing.JRadioButton generateGetterAndSetterRadioButton;
    private javax.swing.JRadioButton generateGetterRadioButton;
    private javax.swing.JCheckBox generateJavadocCheckBox;
    private javax.swing.JCheckBox generatePropertyChangeSupportCheckBox;
    private javax.swing.JRadioButton generateSetterRadioButton;
    private javax.swing.JCheckBox generateVetoablePropertyChangeSupportCheckBox;
    private javax.swing.ButtonGroup getterSetterGroup;
    private javax.swing.JCheckBox indexedCheckBox;
    private javax.swing.JTextField initializerTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton packageRadioButton;
    private javax.swing.JEditorPane previewEditorPane;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JScrollPane previewScrollPane;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JTextField propNameTextField;
    private javax.swing.JRadioButton protectedRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JLabel semicolonLabel;
    private javax.swing.JCheckBox staticCheckBox;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JCheckBox vetoableCheckBox;
    // End of variables declaration//GEN-END:variables
}
