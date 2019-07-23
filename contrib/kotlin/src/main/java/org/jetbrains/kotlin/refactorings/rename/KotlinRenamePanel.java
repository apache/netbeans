/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRenamePanel extends JPanel implements CustomRefactoringPanel {

    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label;
    private javax.swing.JTextField nameField;
    private final transient String oldName;
    private final transient ChangeListener parent;
    
    public KotlinRenamePanel(String oldName, ChangeListener parent) {
        this.oldName = oldName;
        this.parent = parent;
        initComponents();
        nameField.setEnabled(true);
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                KotlinRenamePanel.this.parent.stateChanged(null);
            }
            @Override
            public void insertUpdate(DocumentEvent event) {
                KotlinRenamePanel.this.parent.stateChanged(null);
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                KotlinRenamePanel.this.parent.stateChanged(null);
            }
        });
    }
    
    public String getNameValue() {
        return nameField.getText();
    }
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setLabelFor(nameField);
        add(label, new java.awt.GridBagConstraints());

        nameField.setText(oldName);
        nameField.selectAll();
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(nameField, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }
    
    
    @Override
    public void initialize() {
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {                                    
        parent.stateChanged(null);
    } 
    
}