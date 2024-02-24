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
package org.netbeans.modules.javafx2.editor.codegen;

import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * A simple GUI for Add FX Property action.
 *
 * @author  Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class AddPropertyPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(AddPropertyPanel.class.getName());
    private static ComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel();
    private static final String[] WRITABLE_PROPS = { "BooleanProperty", "DoubleProperty", "FloatProperty", "IntegerProperty", "ListProperty<?>", "LongProperty", "MapProperty<?,?>", "ObjectProperty<?>", "SetProperty<?>", "StringProperty" };
    private static final String[] READONLY_PROPS = { "ReadOnlyBooleanProperty", "ReadOnlyDoubleProperty", "ReadOnlyFloatProperty", "ReadOnlyIntegerProperty", "ReadOnlyListProperty<?>", "ReadOnlyLongProperty", "ReadOnlyMapProperty<?,?>", "ReadOnlyObjectProperty<?>", "ReadOnlySetProperty<?>", "ReadOnlyStringProperty" };
    
    private CompilationController javac;
    private final Scope scope;
    private List<String> existingFields;
    private JButton okButton;
    private final TypeElement readOnlyProperty;
    private final TypeElement property;
    private long time;
    private String oldText;
    
    
    public AddPropertyPanel(CompilationController javac, Scope scope, List<String> existingFields, JButton okButton) {
        this.javac = javac;
        this.scope = scope;
        this.existingFields = existingFields;
        this.okButton = okButton;
        readOnlyProperty = javac.getElements().getTypeElement("javafx.beans.property.ReadOnlyProperty"); //NOI18N
        property = javac.getElements().getTypeElement("javafx.beans.property.Property"); //NOI18N
        initComponents();
        typeComboBox.setSelectedIndex(WRITABLE_PROPS.length-1);

        class DL implements DocumentListener, Runnable {
            @Override
            public void run() {
                updateType();
            }

            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(this);
            }

            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(this);
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        
        DocumentListener documentListener = new DL();
        nameTextField.getDocument().addDocumentListener(documentListener);
        ((JTextField) typeComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(documentListener);
        initializerTextField.getDocument().addDocumentListener(documentListener);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateType();
    }

    private static final RequestProcessor RP = new RequestProcessor ("AddFxProperty-RequestProcessor",1);      //NOI18N
    private Worker running;
    private RequestProcessor.Task task;
    private void updateType() {
        time = System.currentTimeMillis();
        Object selectedItem = typeComboBox.getSelectedItem();
        String text = selectedItem == null ? "" : selectedItem.toString();
        if (oldText == null || !oldText.contentEquals(text)) {
            oldText = text;
            assert SwingUtilities.isEventDispatchThread();
            if (running != null) {
                running.cancel();
                task.cancel();
                running = null;
            }
            
            text = text.trim();

            if (text.isEmpty()) {
                implemenationCombobox.setModel(EMPTY_MODEL);
                return;
            }

            // Compute in other thread        
            running = new Worker(text);
            task = RP.post(running, 220);
        }
        
        int index = findMatchingComboItem();

        
        if(index == -1 && !text.isEmpty()) {
            int last = text.indexOf("<");
            // if the type does not contain package (= simple name), prepend javafx.beans automatically
            String qType = (last == -1 ? text :
                    text.substring(0, last)).indexOf('.') == -1 ?
                    "javafx.beans.property." + text : text;
            TypeMirror parseType = javac.getTreeUtilities().parseType(text, scope.getEnclosingClass());
            if (parseType == null || parseType.getKind() == TypeKind.ERROR || parseType.getKind() == TypeKind.OTHER) {
                // 2nd attempt, 
                parseType = javac.getTreeUtilities().parseType(qType, scope.getEnclosingClass());
            }
            if (parseType == null || parseType.getKind() == TypeKind.ERROR || parseType.getKind() == TypeKind.OTHER) {
                writableRadioButton.setEnabled(true);
                readonlyRadioButton.setEnabled(true);
            } else {
                TypeMirror erasure = javac.getTypes().erasure(parseType);
                TypeMirror propertyErasure = javac.getTypes().erasure(property.asType());
                if(javac.getTypes().isSubtype(erasure, propertyErasure)) {
                    writableRadioButton.setSelected(true);
                    writableRadioButton.setEnabled(false);
                    readonlyRadioButton.setEnabled(false);
                } else {
                    readonlyRadioButton.setSelected(true);
                    writableRadioButton.setEnabled(false);
                    readonlyRadioButton.setEnabled(false);
                }
            }
        } else {
            writableRadioButton.setEnabled(true);
            readonlyRadioButton.setEnabled(true);
        }
        
        String error = resolveError();
        
        if (error != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javafx2/editor/resources/error-glyph.gif"))); // NOI18N
            errorLabel.setText(error);
        }
        
        okButton.setEnabled(error == null);
        
        String warning = null;
        
        if (warning != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javafx2/editor/resources/warning.gif"))); // NOI18N
            errorLabel.setText(warning);
        }
        
        errorLabel.setVisible(error != null || warning != null);
    }
    
    private String resolveError() {
        if (nameTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldIsEmpty");
        }

        if (((JTextField) typeComboBox.getEditor().getEditorComponent()).getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_TypeIsEmpty");
        }
        
        if (existingFields.contains(nameTextField.getText())) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(nameTextField.getText())});
        }
        
        return null;
    }
    
    public AddFxPropertyConfig getAddPropertyConfig() {
        final String propertyType = typeComboBox.getSelectedItem().toString().trim();
        String implementationType = implemenationCombobox.getSelectedItem().toString().trim();
        final String name = nameTextField.getText().trim();
        final String initializer = initializerTextField.getText().trim();
        AddFxPropertyConfig.ACCESS access = AddFxPropertyConfig.ACCESS.PACKAGE;
        if (privateRadioButton.isSelected()) {
            access = AddFxPropertyConfig.ACCESS.PRIVATE;
        } else if (protectedRadioButton.isSelected()) {
            access = AddFxPropertyConfig.ACCESS.PROTECTED;
        } else if (publicRadioButton.isSelected()) {
            access = AddFxPropertyConfig.ACCESS.PUBLIC;
        }

        AddFxPropertyConfig.GENERATE generate = AddFxPropertyConfig.GENERATE.WRITABLE;
        if (!writableRadioButton.isSelected()) {
            generate = AddFxPropertyConfig.GENERATE.READ_ONLY;
        }
        
        if (implementationType.indexOf('<') != -1) { // NOI18N
            if (javac.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                // TODO: apply coding style ?
                implementationType = implementationType.substring(0, implementationType.indexOf('<')) + "<>"; // NOI18N
            } else {
                int propTemplate = propertyType.indexOf("<"); // NOI18N
                if (propTemplate != -1) {
                    implementationType = implementationType.substring(0, implementationType.indexOf('<')) + propertyType.substring(propTemplate);
                }
            }
        }

        AddFxPropertyConfig addPropertyConfig = new AddFxPropertyConfig(
                name, initializer, propertyType, implementationType, access, generate, generateJavadocCheckBox.isSelected());
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
        writableRadioButton = new javax.swing.JRadioButton();
        readonlyRadioButton = new javax.swing.JRadioButton();
        generateJavadocCheckBox = new javax.swing.JCheckBox();
        errorLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        implemenationCombobox = new javax.swing.JComboBox();

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameTextField.text")); // NOI18N

        equalsLabel.setLabelFor(initializerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(equalsLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.equalsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(semicolonLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.semicolonLabel.text")); // NOI18N

        typeLabel.setLabelFor(typeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.typeLabel.text")); // NOI18N

        typeComboBox.setEditable(true);
        typeComboBox.setModel(new DefaultComboBoxModel(WRITABLE_PROPS));
        typeComboBox.setRenderer(new ComboBoxRenderer());
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
        publicRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(publicRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.publicRadioButton.text")); // NOI18N
        publicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(writableRadioButton);
        writableRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(writableRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.writableRadioButton.text")); // NOI18N
        writableRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writableRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(readonlyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(readonlyRadioButton, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.readonlyRadioButton.text")); // NOI18N
        readonlyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readonlyRadioButtonActionPerformed(evt);
            }
        });

        generateJavadocCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateJavadocCheckBox, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateJavadocCheckBox.text")); // NOI18N
        generateJavadocCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateJavadocCheckBoxActionPerformed(evt);
            }
        });

        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javafx2/editor/resources/error-glyph.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getBundle(AddPropertyPanel.class).getString("AddPropertyPanel.errorLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.jLabel1.text")); // NOI18N

        implemenationCombobox.setEditable(true);
        implemenationCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SimpleStringProperty" }));
        implemenationCombobox.setRenderer(new ComboBoxRenderer());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(typeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(typeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(equalsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(implemenationCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initializerTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(semicolonLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(privateRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(packageRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(protectedRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(publicRadioButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(writableRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(readonlyRadioButton))
                            .addComponent(generateJavadocCheckBox)
                            .addComponent(errorLabel))
                        .addGap(0, 238, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {packageRadioButton, privateRadioButton, protectedRadioButton, publicRadioButton, readonlyRadioButton, writableRadioButton});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nameLabel, typeLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(semicolonLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(equalsLabel)
                    .addComponent(initializerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(implemenationCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(browseTypeButton)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageRadioButton)
                    .addComponent(protectedRadioButton)
                    .addComponent(publicRadioButton)
                    .addComponent(privateRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(writableRadioButton)
                    .addComponent(readonlyRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateJavadocCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
        updateType();
    }//GEN-LAST:event_privateRadioButtonActionPerformed

    private void packageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageRadioButtonActionPerformed
        updateType();
}//GEN-LAST:event_packageRadioButtonActionPerformed

    private void protectedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protectedRadioButtonActionPerformed
        updateType();
    }//GEN-LAST:event_protectedRadioButtonActionPerformed

    private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
        updateType();
}//GEN-LAST:event_publicRadioButtonActionPerformed

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        updateType();
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void generateJavadocCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateJavadocCheckBoxActionPerformed
        updateType();
    }//GEN-LAST:event_generateJavadocCheckBoxActionPerformed
   
    private void writableRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writableRadioButtonActionPerformed
        switchType(true);
    }//GEN-LAST:event_writableRadioButtonActionPerformed

    private void readonlyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readonlyRadioButtonActionPerformed
        switchType(false);
}//GEN-LAST:event_readonlyRadioButtonActionPerformed

    private void browseTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTypeButtonActionPerformed
        final Types types = javac.getTypes();
        ElementHandle<TypeElement> type = TypeElementFinder.find(javac.getClasspathInfo(), readOnlyProperty != null ? new TypeElementFinder.Customizer() {

            @Override
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            @Override
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                TypeElement resolved = typeHandle.resolve(javac);
                if(resolved != null) {
                    return types.isSubtype(types.erasure(resolved.asType()),
                            types.erasure(readOnlyProperty.asType()));
                }
                return false;
            }
        } : null);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();
            typeComboBox.setSelectedItem(fqn);
        }
    }//GEN-LAST:event_browseTypeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JButton browseTypeButton;
    private javax.swing.JLabel equalsLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox generateJavadocCheckBox;
    private javax.swing.ButtonGroup getterSetterGroup;
    private javax.swing.JComboBox implemenationCombobox;
    private javax.swing.JTextField initializerTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton packageRadioButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton protectedRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton readonlyRadioButton;
    private javax.swing.JLabel semicolonLabel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JRadioButton writableRadioButton;
    // End of variables declaration//GEN-END:variables

    private int findMatchingComboItem() {
        int index = typeComboBox.getSelectedIndex();
        if (index >= 0) {
            return index;
        }
        String orig = typeComboBox.getSelectedItem().toString();
        String s = orig;
        int last = s.indexOf("<");
        if (last > -1) {
            s = s.substring(0, last);
        }
        ComboBoxModel mdl = typeComboBox.getModel();
        for (int i = 0; i < mdl.getSize(); i++) {
            String t = mdl.getElementAt(i).toString();
            if (t.equals(s) ||
                (t.startsWith(s) && t.length() > last && t.charAt(last) == orig.charAt(last))) {
                return i;
            }
        }
        return -1;
    }
    
    private void switchType(boolean writable) {
        int index = findMatchingComboItem();
        if (index == -1) {
            return;
        }
        String s = typeComboBox.getSelectedItem().toString();
        int last = s.indexOf("<");
        String suffix = last == -1 ? "" : s.substring(last);
        if(writable) {
            typeComboBox.setModel(new DefaultComboBoxModel(WRITABLE_PROPS));
            typeComboBox.setSelectedIndex(index);
        } else {
            typeComboBox.setModel(new DefaultComboBoxModel(READONLY_PROPS));
            typeComboBox.setSelectedIndex(index);
        }
        if (last != -1) {
            String newType = typeComboBox.getSelectedItem().toString();
            int idx = newType.indexOf("<?>");
            if (idx != -1) {
                newType = newType.substring(0, idx) + suffix;
            }
            typeComboBox.setSelectedItem(newType);
        }
    }


    @SuppressWarnings("serial")
    private static class ComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ComboBoxRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (value != null) {
                String text = (String) value;
                text = text.substring(text.lastIndexOf('.') + 1);
                setText(text);
                setIcon(getClassIcon());
            } else {
                setText(null);
                setIcon(getEmptyIcon());
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    private static Icon CLASS_IMAGE_ICON;
    private static Icon getClassIcon() {
        if (CLASS_IMAGE_ICON == null) {
            CLASS_IMAGE_ICON = ElementIcons.getElementIcon(ElementKind.CLASS, EnumSet.noneOf(Modifier.class));
        }
        return CLASS_IMAGE_ICON;
    }
    
    private static Icon getEmptyIcon() {
        if (EMPTY_IMAGE_ICON == null) {
            EMPTY_IMAGE_ICON = new EmptyImageIcon();
        }
        return EMPTY_IMAGE_ICON;
    }
    
    private static EmptyImageIcon EMPTY_IMAGE_ICON;
    private static class EmptyImageIcon implements Icon {
        private static final int WIDTH = 16;
        private static final int HEIGHT = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Empty
        }

        @Override
        public int getIconWidth() {
            return WIDTH;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }
    }

    private class Worker implements Runnable {
        public static final String JAVAFXBEANSPROPERTY = "javafx.beans.property.";

        private volatile boolean canceled = false;
        private final String text;
        private final long createTime;

        public Worker(String text) {
            this.text = text;
            this.createTime = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Worker for {0} - created after {1} ms.", //NOI18N
                    new Object[]{
                        text,
                        System.currentTimeMillis() - time
                    });
        }

        @Override
        public void run() {
            for (;;) {
                final int[] retry = new int[1];

                LOGGER.log(Level.FINE, "Worker for {0} - started {1} ms.", //NOI18N
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                        });

                final List<? extends String> types = getTypeNames(text, retry);
                if (canceled) {
                    LOGGER.log(Level.FINE, "Worker for {0} exited after cancel {1} ms.", //NOI18N
                            new Object[]{
                                text,
                                System.currentTimeMillis() - createTime
                            });
                    return;
                }
                final ComboBoxModel fmodel = new DefaultComboBoxModel(types.toArray(new String[0]));
                if (canceled) {
                    LOGGER.log(Level.FINE, "Worker for {0} exited after cancel {1} ms.", //NOI18N
                            new Object[]{
                                text,
                                System.currentTimeMillis() - createTime
                            });
                    return;
                }

                if (!canceled && fmodel != null) {
                    LOGGER.log(Level.FINE, "Worker for text {0} finished after {1} ms.", //NOI18N
                            new Object[]{
                                text,
                                System.currentTimeMillis() - createTime
                            });
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            int prevIndex = implemenationCombobox.getSelectedIndex();
                            Object prevItem = implemenationCombobox.getSelectedItem();
                            implemenationCombobox.setModel(fmodel);
                            int typeIndex = findMatchingComboItem();
                                if(prevIndex == -1) {
                                implemenationCombobox.setSelectedItem(prevItem);
                            } else if(typeIndex >= 0) {
                                int index = -1;
                                if(writableRadioButton.isSelected()) {
                                    for (int i = 0; i < types.size(); i++) {
                                        String value = types.get(i);
                                        if(value.startsWith("Simple", value.lastIndexOf('.') +1)) {
                                            index = i;
                                            break;
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < types.size(); i++) {
                                        String value = types.get(i);
                                        if(value.endsWith("Wrapper")) {
                                            index = i;
                                            break;
                                        }
                                    }
                                }
                                if(index >= 0) {
                                    implemenationCombobox.setSelectedIndex(index);
                                }
                            }
//                            if (okButton != null) {
//                                okButton.setEnabled(true);
//                            }
                        }
                    });
                }

                if (retry[0] > 0) {
                    try {
                        Thread.sleep(retry[0]);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    return;
                }
            } // for
        }

        public void cancel() {
            if (time != -1) {
                LOGGER.log(Level.FINE, "Worker for text {0} canceled after {1} ms.", //NOI18N
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                        });
            }
            synchronized (this) {
                canceled = true;
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends String> getTypeNames(String text, int[] retry) {
            List<String> items = new ArrayList<String>();
            // Multiple providers: merge results
            String[] message = new String[1];
            assert RP.isRequestProcessorThread();

            DeclaredType typeElement = (DeclaredType) javac.getTreeUtilities().parseType(text, scope.getEnclosingClass());
            if (typeElement == null || typeElement.getKind() == TypeKind.ERROR) {
                typeElement = (DeclaredType) javac.getTreeUtilities().parseType(JAVAFXBEANSPROPERTY + text, scope.getEnclosingClass());
                if (typeElement == null || typeElement.getKind() == TypeKind.ERROR) {
                    return items;
                }
            }
            if (canceled) {return null;}

            ClassIndex classIndex = javac.getClasspathInfo().getClassIndex();
            Set<ElementHandle<TypeElement>> elements = getImplementorsAsHandles(classIndex, (TypeElement) typeElement.asElement());
            for (ElementHandle<TypeElement> elementHandle : elements) {
                StringBuilder qualifiedName = new StringBuilder(elementHandle.getQualifiedName());
                if((qualifiedName.indexOf("javafx.") != 0 || qualifiedName.indexOf("javafx.beans.property.") == 0) && qualifiedName.indexOf("javafx.beans.property.adapter.") != 0
                     && qualifiedName.indexOf("com.sun.") != 0) { // Remove implementation from scene builder
                    TypeElement resolved = elementHandle.resolve(javac);
                    if(resolved != null && !resolved.getModifiers().contains(Modifier.ABSTRACT) && javac.getTrees().isAccessible(scope, resolved)) {
                        if(qualifiedName.indexOf(JAVAFXBEANSPROPERTY) == 0) {
                            qualifiedName = qualifiedName.delete(0, JAVAFXBEANSPROPERTY.length());
                        }
                        if(!resolved.getTypeParameters().isEmpty()) {
                            qualifiedName.append("<");
                            boolean afterFirst = false;
                            for (int i = 0; i < resolved.getTypeParameters().size(); i++) {
                                if(afterFirst) {
                                    qualifiedName.append(",");
                                }
                                qualifiedName.append("?");
                                afterFirst = true;
                            }
                            qualifiedName.append(">");
                        }
                        items.add(qualifiedName.toString());
                    }
                }
            }
            
            if (canceled) {return null;}
            //                Collections.sort(items, new TypeComparator());
            return items;
        }
        
        private Set<ElementHandle<TypeElement>> getImplementorsAsHandles(ClassIndex idx, TypeElement el) {
            LinkedList<ElementHandle<TypeElement>> elements = new LinkedList<ElementHandle<TypeElement>>(
                    implementorsQuery(idx, ElementHandle.create(el)));
            Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
            while (!elements.isEmpty()) {
                if (canceled) {
                    return Collections.emptySet();
                }
                ElementHandle<TypeElement> next = elements.removeFirst();
                if (!result.add(next)) {
                    // it is a duplicate; do not query again
                    continue;
                }
                Set<ElementHandle<TypeElement>> foundElements = implementorsQuery(idx, next);
                elements.addAll(foundElements);
            }
            return result;
        }

        private Set<ElementHandle<TypeElement>> implementorsQuery(ClassIndex idx, ElementHandle<TypeElement> next) {
            return idx.getElements(next,
                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
        }
    }
}
