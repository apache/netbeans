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
