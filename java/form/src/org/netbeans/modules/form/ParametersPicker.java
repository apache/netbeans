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


package org.netbeans.modules.form;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;

/** The ParametersPicker is a panel which allows to enter a method parameter data.
 *
 * @author  Ian Formanek
 */
public class ParametersPicker extends javax.swing.JPanel {

    static final long serialVersionUID =1116033799965380000L;
    
    private boolean previousValue = false;
        
    /**
     * Initializes the Form.
     * 
     * @param formModel form model.
     * @param requiredType type of the parameter.
     */
    public ParametersPicker(FormModel formModel, Class requiredType) {
        this.formModel = formModel;
        this.requiredType = requiredType;
        initComponents();        

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(valueButton);
        bg.add(beanButton);
        bg.add(propertyButton);
        bg.add(methodButton);
        bg.add(codeButton);

        if ((!requiredType.isPrimitive()) &&
            (!requiredType.equals(String.class))) {
            valueButton.setEnabled(false);
            propertyButton.setSelected(true);
        }

        // localize components
        paramLabel.setLabelFor(this);
        propertyLabel.setText(FormUtils.getBundleString("CTL_CW_NoProperty")); // NOI18N
        methodLabel.setText(FormUtils.getBundleString("CTL_CW_NoMethod")); // NOI18N

        Mnemonics.setLocalizedText(valueButton, FormUtils.getBundleString("CTL_CW_Value")); // NOI18N
        Mnemonics.setLocalizedText(beanButton, FormUtils.getBundleString("CTL_CW_Bean")); // NOI18N
        Mnemonics.setLocalizedText(propertyButton, FormUtils.getBundleString("CTL_CW_Property")); // NOI18N
        Mnemonics.setLocalizedText(methodButton, FormUtils.getBundleString("CTL_CW_Method")); // NOI18N
        Mnemonics.setLocalizedText(codeButton, FormUtils.getBundleString("CTL_CW_UserCode")); // NOI18N

        beansList = new ArrayList<RADComponent>();
        for (RADComponent radComp : formModel.getAllComponents()) {
            if (this.requiredType.isAssignableFrom(radComp.getBeanClass()))
                beansList.add(radComp);
        }
        if (beansList.size() > 0) {
            beansList.sort(new ComponentComparator());

            beanCombo.addItem(FormUtils.getBundleString("CTL_CW_SelectBean")); // NOI18N
            for (Iterator it = beansList.iterator(); it.hasNext(); ) {
                RADComponent radComp = (RADComponent) it.next();
                if (radComp == formModel.getTopRADComponent())
                    beanCombo.addItem(
                        FormUtils.getBundleString("CTL_FormTopContainerName")); // NOI18N
                else
                    beanCombo.addItem(radComp.getName());
            }

            beanCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent evt) {
                    int index = beanCombo.getSelectedIndex();
                    if (index == 0) {
                        selectedComponent = null;
                    } else {
                        selectedComponent = beansList.get(index - 1);
                    }
                    fireStateChange();
                }
            }
                                      );
        }
        else beanButton.setEnabled(false);    // no beans on the form are of the required type

        updateParameterTypes();
            
        HelpCtx.setHelpIDString(this, "gui.source.modifying.property"); // NOI18N
        
        valueButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Value")); // NOI18N
        beanButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Bean")); // NOI18N
        propertyButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Property")); // NOI18N
        methodButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Method")); // NOI18N
        codeButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_UserCode")); // NOI18N

        valueField.getAccessibleContext().setAccessibleName(valueButton.getText());
        valueField.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_ValueField")); // NOI18N
        beanCombo.getAccessibleContext().setAccessibleName(beanButton.getText());
        beanCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_BeanCombo")); // NOI18N
        propertyLabel.getAccessibleContext().setAccessibleName(propertyButton.getText());
        propertyLabel.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_PropertyLabel")); // NOI18N
        methodLabel.getAccessibleContext().setAccessibleName(methodButton.getText());
        methodLabel.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_MethodLabel")); // NOI18N
        codeArea.getAccessibleContext().setAccessibleName(codeButton.getText());
        codeArea.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_UserCodeArea")); // NOI18N

        propertyDetailsButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_PropertyButton")); // NOI18N
        methodDetailsButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_MethodButton")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_ParametersPicker")); // NOI18N
    }
    
    public void setPropertyValue(RADConnectionPropertyEditor.RADConnectionDesignValue value, Object realValue) {        
        if (value == null) {
            previousValue = realValue != null;
            return; // can happen if starting without previously set value
        } else {
            previousValue = true;               
        }
        
        switch (value.type) {
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_VALUE:
                valueButton.setSelected(true);
                valueField.setText(value.value);
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN:
                beanButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                int index = beansList.indexOf(selectedComponent);
                if (index > -1)
                    beanCombo.setSelectedIndex(index+1);
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY:
                propertyButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                selectedProperty = value.getProperty();
                if (selectedComponent.getCodeExpression() == null) {
                    propertyLabel.setText(
                        FormUtils.getBundleString("CTL_CONNECTION_INVALID")); // NOI18N
                }
                else if (selectedComponent == formModel.getTopRADComponent()) {
                    propertyLabel.setText(selectedProperty.getName());
                }
                else {
                    propertyLabel.setText(selectedComponent.getName() + "." + selectedProperty.getName()); // NOI18N
                }
                propertyLabel.selectAll();
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD:
                methodButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                selectedMethod = value.getMethod();
                if (selectedComponent.getCodeExpression() == null) {
                    methodLabel.setText(
                        FormUtils.getBundleString("CTL_CONNECTION_INVALID")); // NOI18N
                }
                else if (selectedComponent == formModel.getTopRADComponent()) {
                    methodLabel.setText(selectedMethod.getName());
                }
                else {
                    methodLabel.setText(selectedComponent.getName() + "." + selectedMethod.getName()); // NOI18N
                }
                methodLabel.selectAll();
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE:
            default:
                codeButton.setSelected(true);
                codeArea.setText(value.userCode);
                break;
        }

        // update enabled state
        updateParameterTypes();
    }

    /** Get the customized property value.
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a valid property value
     *(and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        if (valueButton.isSelected() && !"".equals(valueField.getText())) {            
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(requiredType, valueField.getText());
        } else if (beanButton.isSelected() && selectedComponent != null) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent);
        } else if (codeButton.isSelected() && !"".equals(codeArea.getText())) {            
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(codeArea.getText());
        } else if (propertyButton.isSelected() && selectedProperty != null) {            
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent, selectedProperty);
        } else if (methodButton.isSelected() && selectedMethod != null) {            
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent, selectedMethod);
        } else if(!previousValue) {            
            return BeanSupport.NO_VALUE;               
        }
        
        IllegalStateException exc = new IllegalStateException();
        ErrorManager.getDefault().annotate(
            exc, ErrorManager.USER, null, 
            FormUtils.getBundleString("ERR_NothingEntered"), // NOI18N
            null, null);
        throw exc;
        
    }

    public String getPreviewText() {
        if (!isFilled())
            return FormUtils.getBundleString("CTL_CW_NotSet"); // NOI18N
        if (codeButton.isSelected()) {
            return FormUtils.getBundleString("CTL_CW_Code"); // NOI18N
        }
        return getText();
    }

    public String getText() {
        if (!isFilled())
            return FormUtils.getBundleString("CTL_CW_NotSet"); // NOI18N
        if (valueButton.isSelected()) {
            if (requiredType.equals(String.class)) {
                String s = valueField.getText();
                s = s.replace("\\", "\\\\"); // fixes bug 835 // NOI18N
                s = s.replace("\"", "\\\""); // NOI18N
                return "\""+s+"\""; // NOI18N
            }
            else
                return(valueField.getText() != null) ? valueField.getText() : ""; // NOI18N
        } else if (codeButton.isSelected()) {
            return codeArea.getText();
        } else if (beanButton.isSelected()) {
            if (selectedComponent == formModel.getTopRADComponent()) {
                return "this"; // NOI18N
            } else {
                return(selectedComponent.getName());
            }
        } else if (propertyButton.isSelected()) {
            StringBuilder sb = new StringBuilder();
            if (selectedComponent != formModel.getTopRADComponent()) {
                sb.append(selectedComponent.getName());
                sb.append("."); // NOI18N
            }
            if (selectedProperty != null) {
                sb.append(selectedProperty.getReadMethod().getName());
                sb.append("()"); // NOI18N
            } else {
                sb.append("???"); // NOI18N
            }
            return  sb.toString();
        } else if (methodButton.isSelected()) {
            StringBuilder sb = new StringBuilder();
            if (selectedComponent != formModel.getTopRADComponent()) {
                sb.append(selectedComponent.getName());
                sb.append("."); // NOI18N
            }
            sb.append(selectedMethod.getName()); // [FUTURE: - method parameters]
            sb.append("()"); // NOI18N
            return  sb.toString();
        } else return ""; // NOI18N
    }

    public boolean isFilled() {
        if (codeButton.isSelected()) {
            if (requiredType.equals(String.class)) return true;
            else return !"".equals(codeArea.getText()); // NOI18N
        } else if (beanButton.isSelected()) {
            return(selectedComponent != null);
        } else if (propertyButton.isSelected()) {
            return(selectedProperty != null);
        } else if (valueButton.isSelected()) {
            if (requiredType.equals(String.class)) return true;
            else return !"".equals(valueField.getText()); // NOI18N
        } else if (methodButton.isSelected()) {
            return(selectedMethod != null);
        } else return false;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        if (listeners == null)
            listeners = new ArrayList<ChangeListener>();
        listeners.add(l);
    }

    public synchronized void removeListener(ChangeListener l) {
        if (listeners == null)
            return;
        listeners.remove(l);
    }

    private synchronized void fireStateChange() {
        if (listeners == null)
            return;
        List<ChangeListener> list = new ArrayList<ChangeListener>(listeners);
        ChangeEvent evt = new ChangeEvent(this);
        for (Iterator<ChangeListener> it = list.iterator(); it.hasNext();)
            it.next().stateChanged(evt);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        valueButton = new javax.swing.JRadioButton();
        valueField = new javax.swing.JTextField();
        beanButton = new javax.swing.JRadioButton();
        beanCombo = new javax.swing.JComboBox();
        propertyButton = new javax.swing.JRadioButton();
        propertyLabel = new javax.swing.JTextField();
        propertyDetailsButton = new javax.swing.JButton();
        methodButton = new javax.swing.JRadioButton();
        methodLabel = new javax.swing.JTextField();
        methodDetailsButton = new javax.swing.JButton();
        codeButton = new javax.swing.JRadioButton();
        codeScrollPane = new javax.swing.JScrollPane();
        codeArea = FormEditor.createCodeEditorPane(formModel);
        paramLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        valueButton.setText("Value:");
        valueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(valueButton, gridBagConstraints);

        valueField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                updateState(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(valueField, gridBagConstraints);

        beanButton.setText("Bean:");
        beanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(beanButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(beanCombo, gridBagConstraints);

        propertyButton.setText("Property:");
        propertyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(propertyButton, gridBagConstraints);

        propertyLabel.setEditable(false);
        propertyLabel.setText(FormUtils.getBundleString("CTL_CW_NoProperty"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(propertyLabel, gridBagConstraints);

        propertyDetailsButton.setText("...");
        propertyDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertyDetailsButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(propertyDetailsButton, gridBagConstraints);

        methodButton.setText("Method Call:");
        methodButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(methodButton, gridBagConstraints);

        methodLabel.setEditable(false);
        methodLabel.setText(FormUtils.getBundleString("CTL_CW_NoMethod"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(methodLabel, gridBagConstraints);

        methodDetailsButton.setText("...");
        methodDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodDetailsButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(methodDetailsButton, gridBagConstraints);

        codeButton.setText("User Code:");
        codeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(codeButton, gridBagConstraints);

        codeArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                codeAreaMouseClicked(evt);
            }
        });
        codeArea.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                updateState(evt);
            }
        });

        codeScrollPane.setViewportView(codeArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(codeScrollPane, gridBagConstraints);

        paramLabel.setText(FormUtils.getBundleString("CTL_CW_GetParametersFrom"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(paramLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void codeAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_codeAreaMouseClicked
        if (!codeButton.isSelected())
            codeButton.doClick();
    }//GEN-LAST:event_codeAreaMouseClicked

    private void methodDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodDetailsButtonActionPerformed
        MethodPicker picker = new MethodPicker(formModel, null, requiredType);
        picker.setSelectedComponent(selectedComponent);
        picker.setSelectedMethod(selectedMethod);

        String title = FormUtils.getFormattedBundleString(
            "CTL_FMT_CW_SelectMethod", // NOI18N
            new Object[] { Utilities.getShortClassName(requiredType) });

        final DialogDescriptor dd = new DialogDescriptor(picker, title);
        dd.setValid(picker.isPickerValid());
        picker.addPropertyChangeListener("pickerValid", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt2) {
                dd.setValid(((Boolean)evt2.getNewValue()).booleanValue());
            }
        });
        java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            selectedComponent = picker.getSelectedComponent();
	    
	    MethodPicker.MethodPickerItem selectedItem = picker.getSelectedMethod();	    
	    selectedMethod = selectedItem.getMethodDescriptor();
	    if(selectedMethod==null) {
		switchToCodeArea(selectedItem.getMethodName());				
		return;		
	    }
	                
            methodLabel.setEnabled(true);
            if (selectedComponent == formModel.getTopRADComponent()) {
                methodLabel.setText(selectedMethod.getName());
            } else {
                methodLabel.setText(selectedComponent.getName() + "." + selectedMethod.getName()); // NOI18N
            }
            methodLabel.repaint();
            fireStateChange();
        }
    }//GEN-LAST:event_methodDetailsButtonActionPerformed

    private void updateState(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_updateState
        fireStateChange();
        codeArea.getCaret().setVisible(codeButton.isSelected() && codeArea.hasFocus());
    }//GEN-LAST:event_updateState

    private void propertyDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyDetailsButtonActionPerformed
        if (propertyPicker == null) {
            propertyPicker = new PropertyPicker(formModel, null, requiredType);
        }
        propertyPicker.setSelectedComponent(selectedComponent);
        propertyPicker.setSelectedProperty(selectedProperty);
        
        String title = FormUtils.getFormattedBundleString(
            "CTL_FMT_CW_SelectProperty", // NOI18N
            new Object[] { Utilities.getShortClassName(requiredType) });
        
        final DialogDescriptor dd = new DialogDescriptor(propertyPicker, title);
        dd.setValid(propertyPicker.isPickerValid());
        propertyPicker.addPropertyChangeListener("pickerValid", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt2) {
                dd.setValid(((Boolean)evt2.getNewValue()).booleanValue());
            }
        });
        java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            selectedComponent = propertyPicker.getSelectedComponent();
	    
	    PropertyPicker.PropertyPickerItem selectedItem = propertyPicker.getSelectedProperty();
	    selectedProperty = selectedItem.getPropertyDescriptor();	    
	    if(selectedProperty == null) {				
		switchToCodeArea(selectedItem.getReadMethodName());		
		return;
	    }
	    
            propertyLabel.setEnabled(true);
            if (selectedComponent == formModel.getTopRADComponent()) {
                propertyLabel.setText(selectedProperty.getName());
            } else {
                propertyLabel.setText(selectedComponent.getName() + "." + selectedProperty.getName()); // NOI18N
            }
            propertyLabel.repaint();
            fireStateChange();
        }
    }//GEN-LAST:event_propertyDetailsButtonActionPerformed

    private void switchToCodeArea(String text) {	
	codeArea.setText(text);
	codeButton.setSelected(true);	
	updateParameterTypes();
    }
    
    private void typeButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeButtonPressed
        updateParameterTypes();
        if (beanButton.isSelected()) {
            beanCombo.requestFocus();
        } else if (codeButton.isSelected()) {
            codeArea.requestFocus();
        } else if (propertyButton.isSelected()) {
            propertyDetailsButton.requestFocus();
        } else if (methodButton.isSelected()) {
            methodDetailsButton.requestFocus();
        } else if (valueButton.isSelected()) {
            valueField.requestFocus();
        }
    }//GEN-LAST:event_typeButtonPressed

    private void updateParameterTypes() {
        valueField.setEnabled(valueButton.isSelected());
        beanCombo.setEnabled(beanButton.isSelected());
        if (!propertyButton.isSelected())
            propertyLabel.setText(FormUtils.getBundleString("CTL_CW_NoProperty")); // NOI18N
        propertyLabel.setEnabled(propertyButton.isSelected());
        propertyLabel.repaint();
        propertyDetailsButton.setEnabled(propertyButton.isSelected());

        if (!methodButton.isSelected())
            methodLabel.setText(FormUtils.getBundleString("CTL_CW_NoMethod")); // NOI18N
        methodLabel.setEnabled(methodButton.isSelected());
        methodLabel.repaint();
        methodDetailsButton.setEnabled(methodButton.isSelected());
        codeArea.setEnabled(codeButton.isSelected());
        //codeArea.setEditable(codeButton.isSelected());
        codeArea.getCaret().setVisible(codeButton.isSelected() && codeArea.hasFocus());                
        fireStateChange();
    }   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton beanButton;
    private javax.swing.JComboBox beanCombo;
    private javax.swing.JEditorPane codeArea;
    private javax.swing.JRadioButton codeButton;
    private javax.swing.JScrollPane codeScrollPane;
    private javax.swing.JRadioButton methodButton;
    private javax.swing.JButton methodDetailsButton;
    private javax.swing.JTextField methodLabel;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JRadioButton propertyButton;
    private javax.swing.JButton propertyDetailsButton;
    private javax.swing.JTextField propertyLabel;
    private javax.swing.JRadioButton valueButton;
    private javax.swing.JTextField valueField;
    // End of variables declaration//GEN-END:variables

    private FormModel formModel;
    private Class<?> requiredType;

    private PropertyPicker propertyPicker;

    private List<ChangeListener> listeners;
    private RADComponent selectedComponent;
    private PropertyDescriptor selectedProperty;
    private MethodDescriptor selectedMethod;

    private java.util.List<RADComponent> beansList;

    // -------

    static class ComponentComparator implements Comparator<RADComponent> {
        @Override
        public int compare(RADComponent comp1, RADComponent comp2) {
            if (comp1 == comp2)
                return 0;

            RADComponent topComp = comp1.getFormModel().getTopRADComponent();
            if (comp1 == topComp)
                return -1;
            if (comp2 == topComp)
                return 1;

            return comp1.getName().compareTo(comp2.getName());
        }
    }
}
