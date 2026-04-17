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

package org.netbeans.modules.form.editors;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Modifier;

import javax.swing.*;
import org.openide.awt.Mnemonics;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/** JPanel extension containing components which allows visual
 * editing Modifier object.
 * This class has two main properties: mask (possible values mask)
 * and modifier (current value).
 *
 * @author Petr Hamernik
 */
final class ModifierPanel {

    // ------------------------- Statics -------------------------------

    /** Name of 'mask' property */
    public static final String PROP_MASK = "mask"; // NOI18N

    /** Name of 'modifier' property (current value) */
    public static final String PROP_MODIFIER = "modifier"; // NOI18N
    
    private static final int CHECK_ABSTRACT         = 0;
    private static final int CHECK_FINAL            = 1;
    private static final int CHECK_STATIC           = 2;
    private static final int CHECK_SYNCHRONIZED     = 3;
    private static final int CHECK_TRANSIENT        = 4;
    private static final int CHECK_VOLATILE         = 5;
    private static final int CHECK_NATIVE           = 6;

    /** Names of modifiers */
    static final String MODIFIER_NAMES[] = {
        "abstract", "final", "static", "synchronized", "transient", "volatile", "native" // NOI18N
    };

    private static final String[] MODIFIER_LABEL_KEYS = {
        "LBL_ModifierPanel_Modifier_Abstract",         // NOI18N
        "LBL_ModifierPanel_Modifier_Final",            // NOI18N
        "LBL_ModifierPanel_Modifier_Static",           // NOI18N
        "LBL_ModifierPanel_Modifier_Synchronized",     // NOI18N
        "LBL_ModifierPanel_Modifier_Transient",        // NOI18N
        "LBL_ModifierPanel_Modifier_Volatile",         // NOI18N
        "LBL_ModifierPanel_Modifier_Native"            // NOI18N
    };
 
    private static final String[] MODIFIER_DESCRIPTION_KEYS = {
        "ACSD_ModifierPanel_Modifier_Abstract",         // NOI18N
        "ACSD_ModifierPanel_Modifier_Final",            // NOI18N
        "ACSD_ModifierPanel_Modifier_Static",           // NOI18N
        "ACSD_ModifierPanel_Modifier_Synchronized",     // NOI18N
        "ACSD_ModifierPanel_Modifier_Transient",        // NOI18N
        "ACSD_ModifierPanel_Modifier_Volatile",         // NOI18N
        "ACSD_ModifierPanel_Modifier_Native"            // NOI18N
    };

    /** Values of modifiers */
    static final int MODIFIER_VALUES[] = {
        Modifier.ABSTRACT, Modifier.FINAL, Modifier.STATIC, Modifier.SYNCHRONIZED,
        Modifier.TRANSIENT, Modifier.VOLATILE, Modifier.NATIVE
    };

    /** Count of the modifiers */
    static final int MODIFIER_COUNT = MODIFIER_VALUES.length;

    /** Names of accessibility */
    static final String ACCESS_NAMES[] = {
        NbBundle.getMessage(ModifierPanel.class, "LBL_ModifierPanel_Modifier_Default"), // NOI18N
        "private", "protected", "public" // NOI18N
    };

    /** Values of accessibility */
    static final int ACCESS_VALUES[] = {
        0, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC
    };

    /** Mask of access modifiers */
    static final int ACCESS_MASK = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC;
    
    /** Mask of non-access modifiers */
    private static final int OTHERS_MASK = Modifier.ABSTRACT |
            Modifier.FINAL | Modifier.STATIC | Modifier.SYNCHRONIZED |
            Modifier.TRANSIENT | Modifier.VOLATILE | Modifier.NATIVE | Modifier.STRICT;

    /** Mask of all possible modifiers. */
    static final int EDITABLE_MASK = ACCESS_MASK | OTHERS_MASK;

    // ------------------ Instance Fields --------------------------

    /** Reference back to the editor that created this panel. */
    private ModifierEditor myEditor;
    
    /** Current access values shown in the combo box */
    private int currentAccessValues[];

    /** Current access names shown in the combo box */
    private String currentAccessNames[];

    /** JCheckBox array */
    private JCheckBox[] checks;

    /** listener for visual changes */
    private ActionListener listener;

    /** Ignored flag - used during firing change events */
    private boolean ignored = false;

    /** listener for ModifierEditor changes */
    private PropertyChangeListener editorListener;

    /** Creates new form ModifiersPanel */
    public ModifierPanel(ModifierEditor ed) {
        myEditor = ed;
        currentAccessValues = ACCESS_VALUES;
        currentAccessNames = ACCESS_NAMES;
        
        editorListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PROP_MODIFIER.equals(evt.getPropertyName()) || 
                    PROP_MASK.equals(evt.getPropertyName())) {
                    updateAccess();
                    ignored = true;
                    updateComponents();
                    ignored = false;
                }
            }
        };
 
        myEditor.addPropertyChangeListener(WeakListeners.propertyChange(editorListener, myEditor));

        listener = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                // remove abstract, if private access is being selected.
                
                int selIndex = accessCombo.getSelectedIndex();
                // disallow abstract, if private access is selected.
                if (evt.getSource() == accessCombo && selIndex < 0) {
                    // revert the combo to the previous value.
                    return;
                }			   
                if (checks[CHECK_ABSTRACT].isSelected() &&
                        ((myEditor.getModifier() & MODIFIER_VALUES[CHECK_ABSTRACT]) == 0) &&
                        ((myEditor.getModifier() & Modifier.PRIVATE) > 0)) {
                    checks[CHECK_ABSTRACT].setSelected(false);
                }
                if (selIndex >= 0 &&
                        (currentAccessValues[accessCombo.getSelectedIndex()] & Modifier.PRIVATE) > 0 &&
                        (myEditor.getModifier() & Modifier.PRIVATE) == 0) { 
                    checks[CHECK_ABSTRACT].setSelected(false);
                }
			       
                excludeChecks(CHECK_ABSTRACT, CHECK_FINAL);
                excludeChecks(CHECK_ABSTRACT, CHECK_NATIVE);
                excludeChecks(CHECK_ABSTRACT, CHECK_STATIC);
                excludeChecks(CHECK_ABSTRACT, CHECK_SYNCHRONIZED);
                excludeChecks(CHECK_VOLATILE, CHECK_FINAL);
                
                if (!ignored)
                    updateValue();
                
            }
        };

        ignored = true;
        initBasicComponents();

        updateAccess();
        updateModifiers();
        updateComponents();
        ignored = false;
    }

    /** Makes sure that the specified two checkboxes are mutually exclusive by
     * unselecting one if the other one becomes selected.
     */
    private void excludeChecks(int check1, int check2) {
        if (checks[check1].isSelected() && ((myEditor.getModifier() & MODIFIER_VALUES[check1]) == 0))
            checks[check2].setSelected(false);
        else if (checks[check2].isSelected() && ((myEditor.getModifier() & MODIFIER_VALUES[check2]) == 0))
            checks[check1].setSelected(false);
    }

    private void initBasicComponents() {
        accessCombo = new JComboBox();
        accessCombo.addActionListener(listener);
        accessCombo.getAccessibleContext().setAccessibleName("ACS_AccessRights"); // NOI18N
        accessCombo.getAccessibleContext().setAccessibleDescription("ACSD_AccessRights"); // NOI18N
        
        modifPanel = new JPanel();
        modifPanel.setLayout(new java.awt.GridLayout(4, 2, 4, 4));
        modifPanel.getAccessibleContext().setAccessibleName("ACSN_OtherModifiers"); // NOI18N
        modifPanel.getAccessibleContext().setAccessibleDescription("ACSD_OtherModifiers"); // NOI18N

        checks = new JCheckBox[MODIFIER_COUNT];
        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i] = new JCheckBox();
            Mnemonics.setLocalizedText(checks[i], getString(MODIFIER_LABEL_KEYS[i]));
            checks[i].getAccessibleContext().setAccessibleDescription(getModifierDescription(i));
            modifPanel.add(checks[i]);
            checks[i].setEnabled((myEditor.getMask() & MODIFIER_VALUES[i]) != 0);
            checks[i].addActionListener(listener);
        }
    }
    
    private void initComponents() {
        jLabel1 = new JLabel();        
        jLabel1.setLabelFor(accessCombo);
        Mnemonics.setLocalizedText(jLabel1, getString("LAB_AccessRights")); // NOI18N

        jPanel2 = new JPanel();
        jPanel2.setLayout(new java.awt.BorderLayout(8, 8));
        jPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        jPanel2.add(jLabel1, java.awt.BorderLayout.WEST);
        jPanel2.add(accessCombo, java.awt.BorderLayout.CENTER);
        modifPanel.setBorder (new javax.swing.border.CompoundBorder(
                                  new javax.swing.border.TitledBorder(getString("LAB_Modifiers")), // NOI18N
                                  new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 3, 3, 3))
                              ));
        
        commpactP = new JPanel();
        commpactP.setLayout(new java.awt.BorderLayout());
        commpactP.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 7, 6, 7)));
        commpactP.add(modifPanel, java.awt.BorderLayout.CENTER);
        commpactP.add(jPanel2, java.awt.BorderLayout.NORTH);
        commpactP.getAccessibleContext().setAccessibleName("ACSN_ModifierPanel"); // NOI18N
        commpactP.getAccessibleContext().setAccessibleDescription(getString("ACSD_ModifierPanel")); // NOI18N
    }


    private JComboBox accessCombo;
    private JLabel jLabel1;
    private JPanel jPanel2;
    private JPanel modifPanel;
    private JPanel commpactP;
    private boolean isCompact = false;
    private boolean isPartial = false;
    
    public JComboBox getAccessComponent() {
        if (isCompact)
            throw new IllegalStateException("cannot use both getAccessComponent/getCompactComponent"); // NOI18N
        isPartial = true;
        return accessCombo;
    }
    
    public JComponent getModifiersComponent() {
        if (isCompact)
            throw new IllegalStateException("cannot use both getModifiersComponent/getCompactComponent"); // NOI18N
        isPartial = true;
        return modifPanel;
    }
    
    public JPanel getCompactComponent() {
        if (isPartial)
            throw new IllegalStateException(
                    "cannot use getAccessComponent/getModifiersComponent/getCompactComponent together"); // NOI18N
        isCompact = true;
        if (commpactP == null) {
            initComponents();
        }
        return commpactP;
    }

    /** Update access ComboBox values depending on new 'mask' property
     */
    private void updateAccess() {
        int selValue = myEditor.getModifier() & ACCESS_MASK;
        int selIndex = -1;

        int counter = 1;
        for (int i = 1; i < ACCESS_VALUES.length; i++) {
            if ((ACCESS_VALUES[i] & myEditor.getMask()) != 0)
                counter++;
        }
        currentAccessValues = new int[counter];
        currentAccessNames = new String[counter];

        currentAccessValues[0] = ACCESS_VALUES[0];
        currentAccessNames[0] = ACCESS_NAMES[0];
        counter = 1;

        for (int i = 1; i < ACCESS_VALUES.length; i++) {
            if ((ACCESS_VALUES[i] & myEditor.getMask()) != 0) {
                currentAccessValues[counter] = ACCESS_VALUES[i];
                currentAccessNames[counter] = ACCESS_NAMES[i];
                if (ACCESS_VALUES[i] == selValue) {
                    selIndex = counter;
                }
                counter++;
            }
        }
        if (selIndex == -1 && selValue == 0)
            selIndex = 0;

        ignored = true;
        accessCombo.setModel(new DefaultComboBoxModel(currentAccessNames));
        accessCombo.setSelectedIndex(selIndex);
        ignored = false;
    }

    /** Update enable status of all modifiers check boxes
     */
    private void updateModifiers() {
        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i].setEnabled((myEditor.getMask() & MODIFIER_VALUES[i]) != 0);
        }
    }

    /** Update the components inside the ModifierPanel depending on new value
     * of 'modifier' property.
     */
    private void updateComponents() {
	updateAccessCombo();
	updateModifiers();
        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i].setSelected((myEditor.getModifier() & MODIFIER_VALUES[i]) != 0);
        }
    }
    
    private void updateAccessCombo() {
        int selIndex = -1;
        if (myEditor.getModifier() == 0) {
            selIndex = 0;
        } else {
            for (int i = 1; i < currentAccessValues.length; i++) {
                if ((currentAccessValues[i] & myEditor.getModifier()) != 0) {
		            if (selIndex != -1) {
		                selIndex = -1;
		                break;
		            }
                    selIndex = i;
                }
            }
        }
	    if (accessCombo.getSelectedIndex() != selIndex) {
            accessCombo.setSelectedIndex(selIndex);
	    }
    }

    /** Updates the value depending on the status of the components. */
    private void updateValue() {
        int newValue = 0;
        int comboIndex = accessCombo.getSelectedIndex();
        Object type = myEditor.getType();
        int mask = 0;
        if (ModifierEditor.FULL_CUSTOM_EDITOR.equals(type) || ModifierEditor.ACCESS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
            mask |= ACCESS_MASK;
            if (comboIndex == -1) {
                newValue = myEditor.getModifier() & ACCESS_MASK;
            } else {
                newValue |= currentAccessValues[comboIndex];
            }
        }
        if (ModifierEditor.FULL_CUSTOM_EDITOR.equals(type) || ModifierEditor.OTHERS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
            mask |= OTHERS_MASK;
            for (int i = 0; i < MODIFIER_COUNT; i++) {
                if (checks[i].isSelected() && checks[i].isEnabled())
                    newValue |= MODIFIER_VALUES[i];
            }
        }
        
        int oldValue = myEditor.getModifier(); 
        if ((oldValue & mask) != newValue) {
            if (ModifierEditor.ACCESS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
                newValue |= (oldValue & ~ACCESS_MASK);
            } else if (ModifierEditor.OTHERS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
                newValue |= (oldValue & ~OTHERS_MASK);
            }
            myEditor.setModifier(newValue);
        }
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ModifierPanel.class, key);
    }
    
    static String getModifierDescription(int i) {
        return getString(MODIFIER_DESCRIPTION_KEYS[i]);
    }
    
}
