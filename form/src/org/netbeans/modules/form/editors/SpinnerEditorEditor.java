/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.netbeans.modules.form.editors2.SpinnerModelEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Property editor for <code>editor</code> property of <code>JSpinner</code>.
 *
 * @author Jan Stola
 */
public class SpinnerEditorEditor extends PropertyEditorSupport
        implements XMLPropertyEditor, NamedPropertyEditor, FormAwareEditor {

    /** Determines whether the components of custom editor have been created. */
    private boolean initialized;
    /** Determines whether custom property editor should fire value changes. */
    private boolean fireChanges;
    /** Property being edited. */
    private FormProperty property;

    /**
     * Initializes components of custom editor. 
     */
    private void initGUI() {
        initialized = true;
        initComponents();
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        comboModel.addElement(typeToString(FormSpinnerEditor.TYPE_DEFAULT));
        comboModel.addElement(typeToString(FormSpinnerEditor.TYPE_DATE));
        comboModel.addElement(typeToString(FormSpinnerEditor.TYPE_LIST));
        comboModel.addElement(typeToString(FormSpinnerEditor.TYPE_NUMBER));
        typeCombo.setModel(comboModel);    
    }

    /**
     * Converts textual representation of spinner editor type to integer constant.
     * 
     * @param string textual representation of spinner editor type. 
     * @return integer constant that correspond to the textual representation
     * of the spinner editor type. Returns <code>-1</code> if the text cannot
     * be parsed.
     */
    private int stringToType(String string) {
        int type = -1;
        if (NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Default").equals(string)) { // NOI18N
            type = FormSpinnerEditor.TYPE_DEFAULT;
        } else if (NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Date").equals(string)) { // NOI18N
            type = FormSpinnerEditor.TYPE_DATE;
        } else if (NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_List").equals(string)) { // NOI18N
            type = FormSpinnerEditor.TYPE_LIST;
        } else if (NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Number").equals(string)) { // NOI18N
            type = FormSpinnerEditor.TYPE_NUMBER;
        }
        return type;
    }

    /**
     * Converts spinner editor type into human readable text. 
     * 
     * @param type spinner editor type.
     * @return human readable textual representation of spinner editor type.
     */
    private String typeToString(int type) {
        String string = null;
        switch (type) {
            case FormSpinnerEditor.TYPE_DEFAULT:
                string = NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Default"); // NOI18N
                break;
            case FormSpinnerEditor.TYPE_DATE:
                string = NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Date"); // NOI18N
                break;
            case FormSpinnerEditor.TYPE_LIST:
                string = NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_List"); // NOI18N
                break;
            case FormSpinnerEditor.TYPE_NUMBER:
                string = NbBundle.getMessage(getClass(), "LBL_SpinnerEditorEditor_Number"); // NOI18N
                break;
            default: assert false;
        }
        return string;
    }

    @Override
    public String getAsText() {
        String text = null;
        Object value = getValue();
        if (value instanceof FormSpinnerEditor) {
            FormSpinnerEditor editor = (FormSpinnerEditor)value;
            text = typeToString(editor.getType());
            String format = editor.getFormat();
            if (format != null) {
                text += " " + format; // NOI18N
            }
        } else {
            text = typeToString(FormSpinnerEditor.TYPE_DEFAULT);
        }
        return text;
    }

    @Override
    public void setAsText(String text) {
        if (text == null) throw new IllegalArgumentException();
        int index = text.indexOf(' ');
        String typeText;
        String format;
        if (index == -1) {
            typeText = text;
            format = null;
        } else {
            typeText = text.substring(0, index);
            format = text.substring(index+1);
        }
        int type = stringToType(typeText);
        if (type == -1) throw new IllegalArgumentException();
        if (type != FormSpinnerEditor.TYPE_DEFAULT) {
            FormSpinnerEditor editor = new FormSpinnerEditor(property, type, format);
            setValue(editor);
        }
    }

    /**
     * Determines whether this property editor supports custom editing. 
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns custom editor.
     * 
     * @return custom editor.
     */
    @Override
    public Component getCustomEditor() {
        fireChanges = false;
        if (!initialized) {
            initGUI();
        }
        Object value = getValue();
        if (value instanceof FormSpinnerEditor) {
            FormSpinnerEditor editor = (FormSpinnerEditor)value;
            typeCombo.setSelectedIndex(editor.getType());
            String format = editor.getFormat();
            if (format != null) {
                formatField.setText(format);
            }
        } else {
            // default value
            typeCombo.setSelectedIndex(0);
        }
        fireChanges = true;
        return customizerPanel;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customizerPanel = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        formatLabel = new javax.swing.JLabel();
        formatField = new javax.swing.JTextField();

        typeLabel.setText(org.openide.util.NbBundle.getMessage(SpinnerEditorEditor.class, "LBL_SpinnerEditorEditor_Type")); // NOI18N

        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });

        formatLabel.setText(org.openide.util.NbBundle.getMessage(SpinnerEditorEditor.class, "LBL_SpinnerEditorEditor_Format")); // NOI18N

        formatField.setColumns(15);
        formatField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formatFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout customizerPanelLayout = new javax.swing.GroupLayout(customizerPanel);
        customizerPanel.setLayout(customizerPanelLayout);
        customizerPanelLayout.setHorizontalGroup(
            customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeLabel)
                    .addComponent(formatLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeCombo, 0, 126, Short.MAX_VALUE)
                    .addComponent(formatField))
                .addContainerGap())
        );
        customizerPanelLayout.setVerticalGroup(
            customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatLabel)
                    .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formatFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formatFieldFocusLost
        updateValueFromUI();
    }//GEN-LAST:event_formatFieldFocusLost

    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        int index = typeCombo.getSelectedIndex();
        boolean editable = (index%2 == 1);
        if (editable != formatField.isEditable()) {
            formatField.setText(""); // NOI18N
            formatField.setEditable(editable);
        }
        updateValueFromUI();
    }//GEN-LAST:event_typeComboActionPerformed

    /**
     * Updates property editor according value represented by UI components
     * of the custom property editor.
     */
    private void updateValueFromUI() {
        if (!fireChanges) return;
        Object value = valueFromUI();
        if (value == null) {
            value = property.getDefaultValue();
        }
        updateModelProperty(value);
        setValue(value);
    }

    private void updateModelProperty(Object value) {
        if ((value instanceof FormSpinnerEditor) && (property instanceof RADProperty)) {
            RADProperty editorProperty = (RADProperty)property;
            RADProperty modelProperty = (RADProperty)editorProperty.getRADComponent().getPropertyByName("model"); // NOI18N
            if (modelProperty != null) {
                try {
                    Object spinnerModel = modelProperty.getRealValue();
                    FormSpinnerEditor editor = (FormSpinnerEditor)value;
                    int type = editor.getType();
                    if ((type == FormSpinnerEditor.TYPE_DATE) && !(spinnerModel instanceof SpinnerDateModel)) {
                        SpinnerDateModel newModel = new SpinnerDateModel();
                        modelProperty.setValue(new SpinnerModelEditor.FormSpinnerModel(newModel, newModel.getValue(), true, false, false));
                    } else if ((type == FormSpinnerEditor.TYPE_NUMBER) && !(spinnerModel instanceof SpinnerNumberModel)) {
                        SpinnerNumberModel newModel = new SpinnerNumberModel();
                        modelProperty.setValue(new SpinnerModelEditor.FormSpinnerModel(newModel, newModel.getValue()));
                    } else if ((type == FormSpinnerEditor.TYPE_LIST) && !(spinnerModel instanceof SpinnerListModel)) {
                        SpinnerListModel newModel = new SpinnerListModel();
                        modelProperty.setValue(new SpinnerModelEditor.FormSpinnerModel(newModel, newModel.getValue()));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Obtains value from the custom property editor.
     * 
     * @return value represented by UI components of the custom property editor.
     */
    private FormSpinnerEditor valueFromUI() {
        int index = typeCombo.getSelectedIndex();
        FormSpinnerEditor value;
        if (index == 0) {
            value = null;
        } else {
            String format = formatField.getText();
            if (index%2 == 0) {
                format = null;
            }
            value = new FormSpinnerEditor(property, index, format);
        }
        return value;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JTextField formatField;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables

    /** Name of the root tag of the spinner editor XML property editor. */
    private static final String XML_SPINNER_EDITOR = "SpinnerEditor"; // NOI18N
    /** Name of the type attribute. */
    private static final String ATTR_TYPE = "type"; // NOI18N
    /** Name of the format attribute. */
    private static final String ATTR_FORMAT = "format"; // NOI18N
    
    @Override
    public void readFromXML(Node element) throws IOException {
        NamedNodeMap attributes = element.getAttributes();
        String typeTxt = attributes.getNamedItem(ATTR_TYPE).getNodeValue();
        int type = Integer.parseInt(typeTxt);
        Node node = attributes.getNamedItem(ATTR_FORMAT);
        String format = null;
        if (node != null) {
            format = node.getNodeValue();
        }
        setValue(new FormSpinnerEditor(property, type, format));
    }

    @Override
    public Node storeToXML(Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_SPINNER_EDITOR);
        Object value = getValue();
        if (!(value instanceof FormSpinnerEditor)) {
            el.setAttribute(ATTR_TYPE, "" + FormSpinnerEditor.TYPE_DEFAULT); // NOI18N
        } else {
            FormSpinnerEditor editor = (FormSpinnerEditor)value;
            el.setAttribute(ATTR_TYPE, "" + editor.getType()); // NOI18N
            String format = editor.getFormat();
            if (format != null) {
                el.setAttribute(ATTR_FORMAT, editor.getFormat());
            }
        }
        return el;
    }

    /**
     * Returns display name of the editor. 
     * 
     * @return display name of the editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CTL_SpinnerEditorEditor_DisplayName"); // NOI18N
    }

    /**
     * Sets context of this property editor.
     * 
     * @param formModel form model.
     * @param property 
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = property;
    }

    /**
     * Raise form version to 6.0 - this editor is available since NB 6.0.
     */
    @Override
    public void updateFormVersionLevel() {
        property.getPropertyContext().getFormModel()
                .raiseVersionLevel(FormModel.FormVersion.NB60, FormModel.FormVersion.NB60);
    }

    /**
     * Returns initialization string for the value represented by this property editor.
     * 
     * @return initialization string. 
     */
    @Override
    public String getJavaInitializationString() {
        Object value = getValue();
        if (!(value instanceof FormSpinnerEditor)) {
            // should not happen
            return super.getJavaInitializationString();
        }
        FormSpinnerEditor editor = (FormSpinnerEditor)value;
        CodeVariable var = editor.getProperty().getRADComponent().getCodeExpression().getVariable();
        int type = editor.getType();
        String code = null;
        switch (type)  {
            case FormSpinnerEditor.TYPE_DATE:
                String format = editor.getFormat().replace("\"", "\\\""); // NOI18N
                code = "new javax.swing.JSpinner.DateEditor(" + var.getName() + ", \"" + format + "\")"; // NOI18N
                break;
            case FormSpinnerEditor.TYPE_LIST:
                code = "new javax.swing.JSpinner.ListEditor(" + var.getName() + ")"; // NOI18N
                break;
            case FormSpinnerEditor.TYPE_NUMBER:
                format = editor.getFormat().replace("\"", "\\\""); // NOI18N
                code = "new javax.swing.JSpinner.NumberEditor(" + var.getName() + ", \"" + format + "\")"; // NOI18N
                break;
            default: assert false;
        }
        return code;
    }

    /**
     * Wrapper for an editor of <code>JSpinner</code>.
     */
    static class FormSpinnerEditor extends FormDesignValueAdapter {
        /** Constant for the type of the spinner editor - default editor. */
        public static final int TYPE_DEFAULT = 0;
        /** Constant for the type of the spinner editor - date editor. */
        public static final int TYPE_DATE = 1;
        /** Constant for the type of the spinner editor - list editor. */
        public static final int TYPE_LIST = 2;
        /** Constant for the type of the spinner editor - number editor. */
        public static final int TYPE_NUMBER = 3;
        /** Type of the spinner editor. */
        private int type;
        /** Format of the spinner editor. */
        private String format;
        /** Property this editor belongs to. */
        private RADProperty property;

        /**
         * Creates new <code>FormSpinnerEditor</code>.
         * 
         * @param property property this editor belongs to.
         * @param type type of the spinner editor.
         * @param format format of the spinner editor.
         */
        FormSpinnerEditor(FormProperty property, int type, String format) {
            this.property = (RADProperty)property;
            this.type = type;
            this.format = format;
        }

        /**
         * Returns type of the spinner editor. 
         * 
         * @return type of the spinner editor.
         */
        public int getType() {
            return type;
        }

        /**
         * Returns format of the spinner editor. 
         * 
         * @return format of the spinner editor.
         */
        public String getFormat() {
            return format;
        }
        
        RADProperty getProperty() {
            return property;
        }

        /**
         * Returns design value.
         * 
         * @return design value.
         */
        @Override
        public Object getDesignValue() {
            Object value = null;
            switch (type) {
                case TYPE_DEFAULT:
                    value = property.getDefaultValue();
                    break;
                case TYPE_DATE:
                    value = new JSpinner.DateEditor((JSpinner)property.getRADComponent().getBeanInstance(), format);
                    break;
                case TYPE_LIST:
                    value = new JSpinner.ListEditor((JSpinner)property.getRADComponent().getBeanInstance());
                    break;
                case TYPE_NUMBER:
                    value = new JSpinner.NumberEditor((JSpinner)property.getRADComponent().getBeanInstance(), format);
                    break;
                default: assert false;
            }
            return value;
        }

        /**
         * Returns copy of the current value.
         * 
         * @param targetFormProperty property where this value should be copied to.
         * @return copy of the current value.
         */
        @Override
        public Object copy(FormProperty targetFormProperty) {
            return new FormSpinnerEditor(targetFormProperty, type, format);
        }
        
    }

}
