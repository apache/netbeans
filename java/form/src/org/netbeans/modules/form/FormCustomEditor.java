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

package org.netbeans.modules.form;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.*;

/**
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditor extends JPanel implements PropertyChangeListener {

    private static final int DEFAULT_WIDTH  = 350;
    private static final int DEFAULT_HEIGHT = 350;

    // -----------------------------------------------------------------------------
    // Private variables

    private FormPropertyEditor editor;
    private PropertyEditor[] allEditors;
    private Component[] allCustomEditors;
    private int originalEditorIndex;

    private javax.swing.JPanel cardPanel;
    private javax.swing.JComboBox editorsCombo;

    /** Creates new form FormCustomEditor
     * 
     * @param editor form property editor
     * @param currentCustomEditor current custom editor
     */
    public FormCustomEditor(FormPropertyEditor editor,
                            Component currentCustomEditor)
    {
        JLabel modeLabel = new JLabel();
        editorsCombo = new JComboBox();
        editorsCombo.setRenderer(new EditorComboRenderer());
        JPanel borderPanel = new JPanel(); // panel with a border containing the panel with editors (cardPanel)
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(1, 1, 2, 2)));
        borderPanel.setLayout(new BorderLayout());
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());
        borderPanel.add(cardPanel, BorderLayout.CENTER);

        FormProperty property = editor.getProperty();
        String selectModeText;
        if (property instanceof RADProperty) {
            selectModeText = FormUtils.getFormattedBundleString("FMT_EditingModeLabel1", // NOI18N
                new Object[] { ((RADProperty)property).getRADComponent().getName(),
                                property.getName() });
        } else {
            selectModeText = FormUtils.getFormattedBundleString("FMT_EditingModeLabel2", // NOI18N
                new Object[] { property.getName() });
        }
        Mnemonics.setLocalizedText(modeLabel, selectModeText);
        editorsCombo.setToolTipText(FormUtils.getBundleString("EditingMode_Hint")); // NOI18N
        modeLabel.setLabelFor(editorsCombo);

        this.editor = editor;
        allEditors = editor.getAllEditors();

        PropertyEditor currentEditor = editor.getCurrentEditor();

        allCustomEditors = new Component[allEditors.length];

        PropertyEnv env = editor.getPropertyEnv();
        Object currentValue = editor.getValue();

        // go through all available property editors, set their values and
        // setup their custom editors
        for (int i=0; i < allEditors.length; i++) {
            PropertyEditor prEd = allEditors[i];
            boolean current = currentEditor == prEd;
            boolean valueSet = false;
            Component custEd = null;

            if (current) {
                valueSet = true;
                custEd = currentCustomEditor;
            }
            else {
                editor.getPropertyContext().initPropertyEditor(prEd, property);
                if (env != null && prEd instanceof ExPropertyEditor)
                    ((ExPropertyEditor)prEd).attachEnv(env);

                if (currentValue != null) {
                    try {
                        boolean isFormDesignValue = (currentValue instanceof FormDesignValue);
                        if (editor.getPropertyType().isAssignableFrom(currentValue.getClass())
                                && !isFormDesignValue)
                        {   // currentValue is a real property value corresponding
                            // to property editor value type
                            prEd.setValue(currentValue);
                            valueSet = true;
                        }
                        else if (isFormDesignValue) {
                            Object realValue = // get real value of the design value
                                ((FormDesignValue)currentValue).getDesignValue();
                            if (realValue != FormDesignValue.IGNORED_VALUE) {
                                // there is a known real value
                                prEd.setValue(realValue); 
                                valueSet = true;
                            }
                        }
                    }
                    catch (IllegalArgumentException ex) {} // ignore
                }
                // [null value should not be set?]

                if (!valueSet) {
                    // no reasonable value for this property editor, try to
                    // set the default value
                    Object defaultValue = property.getDefaultValue();
                    if (defaultValue != BeanSupport.NO_VALUE) {
                        prEd.setValue(defaultValue);
                    }
                }

                if (prEd.supportsCustomEditor())
                    custEd = prEd.getCustomEditor();
            }

            String editorName;
            if (prEd instanceof NamedPropertyEditor) {
                editorName = ((NamedPropertyEditor)prEd).getDisplayName();
            } else {
                editorName = i == 0 ?
                    FormUtils.getBundleString("CTL_DefaultEditor_DisplayName") // NOI18N
                    : Utilities.getShortClassName(prEd.getClass());
            }

            if (custEd == null || custEd instanceof Window) {
                JPanel p = new JPanel(new GridBagLayout());
                JLabel label = new JLabel(
                    FormUtils.getBundleString("CTL_PropertyEditorDoesNot")); // NOI18N
                p.add(label);
                p.getAccessibleContext().setAccessibleDescription(label.getText());
                custEd = p;
            }

            allCustomEditors[i] = custEd;
            cardPanel.add(editorName, custEd);
            editorsCombo.addItem(editorName);
            if (current) {
                originalEditorIndex = i;
                editorsCombo.setSelectedIndex(i);
                updateAccessibleDescription(custEd);
            }
        }

        if (env != null) {
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addPropertyChangeListener(this);
        }

        // build layout when the combo box is filled
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(borderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(modeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(editorsCombo, GroupLayout.PREFERRED_SIZE, editorsCombo.getPreferredSize().width*5/4, GroupLayout.PREFERRED_SIZE)))
            .addContainerGap()
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(modeLabel)
                .addComponent(editorsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(borderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, (String) editorsCombo.getSelectedItem());

        editorsCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CardLayout cl2 = (CardLayout) cardPanel.getLayout();
                cl2.show(cardPanel, (String) editorsCombo.getSelectedItem());

                updateHelpAndAccessibleDescription();
            }
        });

        editorsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_EditingMode")); // NOI18N

        updateHelpAndAccessibleDescription();
    }

    private void updateHelpAndAccessibleDescription() {
        HelpCtx.setHelpIDString(this, null);
        int i = editorsCombo.getSelectedIndex();
        HelpCtx helpCtx = i < 0 ? null : HelpCtx.findHelp(cardPanel.getComponent(i));
        String helpID = helpCtx != null && helpCtx != HelpCtx.DEFAULT_HELP ? helpCtx.getHelpID() : "f1_mat_prop_html"; // NOI18N
        HelpCtx.setHelpIDString(this, helpID);

        updateAccessibleDescription(i < 0 ? null : cardPanel.getComponent(i));
    }
    
    private void updateAccessibleDescription(Component comp) {
        if (comp instanceof javax.accessibility.Accessible
            && comp.getAccessibleContext().getAccessibleDescription() != null) {

            getAccessibleContext().setAccessibleDescription(
                FormUtils.getFormattedBundleString(
                    "ACSD_FormCustomEditor", // NOI18N
                    new Object[] {
                        comp.getAccessibleContext().getAccessibleDescription()
                    }
                )
            );
        } else {
            getAccessibleContext().setAccessibleDescription(null);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension inh = super.getPreferredSize();
        return new Dimension(Math.max(inh.width, DEFAULT_WIDTH), Math.max(inh.height, DEFAULT_HEIGHT));
    }

    private class EditorComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == originalEditorIndex && editorsCombo.isPopupVisible()) {
                setFont(list.getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
    }

    /**
     * Called by property sheet when OK button is pressed and the value is valid.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            Object value = commitChanges0();
            editor.setEditedValue(value); // set the value to the main editor
        }
    }

    /**
     * Used by PropertyAction to mimic property sheet behavior - trying to invoke
     * PropertyEnv listener of the current property editor (we can't create our
     * own PropertyEnv instance).
     * 
     * @return current value
     * @throws java.beans.PropertyVetoException if someone vetoes this change.
     */
    public Object commitChanges() throws PropertyVetoException {
        int currentIndex = editorsCombo.getSelectedIndex();
        PropertyEditor currentEditor = currentIndex > -1 ? allEditors[currentIndex] : null;
        if (currentEditor instanceof ExPropertyEditor) {
            // we can only guess - according to the typical pattern the propetry
            // editor itself or the custom editor usually implement the listener
            // registered in PropertyEnv
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PropertyEnv.PROP_STATE, null, PropertyEnv.STATE_VALID);
            if (currentEditor instanceof VetoableChangeListener) {
                ((VetoableChangeListener)currentEditor).vetoableChange(evt);
            }
            Component currentCustEd = currentIndex > -1 ? allCustomEditors[currentIndex] : null;
            if (currentCustEd instanceof VetoableChangeListener) {
                ((VetoableChangeListener)currentCustEd).vetoableChange(evt);
            }
            if (currentEditor instanceof PropertyChangeListener) {
                ((PropertyChangeListener)currentEditor).propertyChange(evt);
            }
            if (currentCustEd instanceof PropertyChangeListener) {
                ((PropertyChangeListener)currentCustEd).propertyChange(evt);
            }
        }
        return commitChanges0();
    }

    private Object commitChanges0() {
        int currentIndex = editorsCombo.getSelectedIndex();
        PropertyEditor currentEditor = currentIndex > -1 ? allEditors[currentIndex] : null;
        if (currentEditor != null) {
            // assuming the editor already has the new value set through PropertyEnv listener
            Object value = currentEditor.getValue();
            if (editor.getProperty().canWrite()) { // issue 83770
                // create a special "value with editor" to switch the current
                // editor in FormProperty
                if (editor.getPropertyEnv() == null) {
                    value = new FormProperty.ValueWithEditor(value, currentEditor, true);
                } else {
                    Object[] nodes = editor.getPropertyEnv().getBeans();
                    if (nodes == null || nodes.length <= 1) {
                        value = new FormProperty.ValueWithEditor(value, currentEditor, true);
                    }
                    else { // there are more nodes selected
                        value = new FormProperty.ValueWithEditor(value, currentIndex, true);
                    }
                }
            }
            return value;
        }
        return BeanSupport.NO_VALUE;
    }
}
