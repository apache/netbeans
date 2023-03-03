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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Stola, Jan Jancura
 */
@OptionsPanelController.Keywords(keywords={"#KW_FormOptions"}, location="Java", tabTitle= "#Form_Editor")
public final class FormEditorCustomizer extends JPanel implements  ActionListener, ChangeListener {
    private JCheckBox cbFold = new JCheckBox ();
    private JCheckBox cbAssistant = new JCheckBox();
    private JCheckBox cbFQN = new JCheckBox();
    private JComboBox cbModifier = new JComboBox ();
    private JRadioButton rbGenerateLocals = new JRadioButton ();
    private JRadioButton rbGenerateFields = new JRadioButton ();
    private JComboBox cbLayoutStyle = new JComboBox ();
    private JComboBox cbComponentNames = new JComboBox ();
    private JComboBox cbListenerStyle = new JComboBox ();
    private JComboBox cbAutoI18n = new JComboBox();
    private JSpinner spGridSize = new JSpinner(new SpinnerNumberModel(10, 2, 100, 1));
    private PropertyPanel guideLineColEditor = new PropertyPanel();
    private PropertyPanel selectionBorderColEditor = new PropertyPanel();    
    private JCheckBox cbPaintLayout = new JCheckBox();

    private ColorSettingsProperty guideLineColorProperty;
    private ColorSettingsProperty selectionBorderColorProperty;

    private Set<Component> brandedInvisibleComponents;

    private boolean changed = false;
    private boolean listen = false;

    public FormEditorCustomizer () {
        
        ButtonGroup group = new ButtonGroup ();
        loc(cbFold, "Fold"); // NOI18N
        loc(cbAssistant, "Assistant"); // NOI18N
        loc(cbFQN, "Generate_FQN"); // NOI18N
        loc(rbGenerateLocals, "Generate_Locals"); // NOI18N
        group.add (rbGenerateLocals);
        loc(rbGenerateFields, "Generate_Fields"); // NOI18N
        group.add (rbGenerateFields);
        cbModifier.addItem(loc("Public_Modifier")); // NOI18N
        cbModifier.addItem(loc("Default_Modifier")); // NOI18N
        cbModifier.addItem(loc("Protected_Modifier")); // NOI18N
        cbModifier.addItem(loc("Private_Modifier")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_AUTO")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_JDK6")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_LIBRARY")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_DEFAULT")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_ON")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_OFF")); // NOI18N
        cbListenerStyle.addItem(loc("Anonymous")); // NOI18N
        cbListenerStyle.addItem(loc("InnerClass")); // NOI18N
        cbListenerStyle.addItem(loc("MainClass")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_DEFAULT")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_ON")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_OFF")); // NOI18N

        JLabel generateComponentsLabel = new JLabel(loc("Generate_Components")); // NOI18N
        JLabel variableModifierLabel = new JLabel();
        JLabel layoutStyleLabel = new JLabel();
        JLabel componentNamesLabel = new JLabel();
        JLabel selectionBorderColLabel = new JLabel();
        JLabel guideLineColLabel = new JLabel();
        JLabel listenerStyleLabel = new JLabel();
        JLabel autoI18nLabel = new JLabel();
        JLabel gridSizeLabel = new JLabel();
        JLabel codeGenSettingsHint = new JLabel();
        loc(variableModifierLabel, "Variable_Modifier"); // NOI18N
        loc(layoutStyleLabel, "Layout_Style"); // NOI18N
        loc(componentNamesLabel,"Component_Names"); // NOI18N
        loc(listenerStyleLabel, "Listener_Style"); // NOI18N
        loc(autoI18nLabel, "Auto_I18n"); // NOI18N
        loc(selectionBorderColLabel, "Selection_Border_Color"); // NOI18N
        loc(guideLineColLabel, "Guiding_Line_Color"); // NOI18N
        loc(gridSizeLabel, "Grid_Size"); // NOI18N
        loc(cbPaintLayout, "Paint_Layout"); // NOI18N
        loc(codeGenSettingsHint, "Code_Settings_Hint"); // NOI18N

        generateComponentsLabel.setToolTipText(loc("Generate_Components_Hint")); // NOI18N
        variableModifierLabel.setToolTipText(loc("HINT_VARIABLES_MODIFIER")); // NOI18N
        layoutStyleLabel.setToolTipText(loc("HINT_LAYOUT_CODE_TARGET")); // NOI18N
        componentNamesLabel.setToolTipText(loc("HINT_AUTO_SET_COMPONENT_NAME")); // NOI18N
        listenerStyleLabel.setToolTipText(loc("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        autoI18nLabel.setToolTipText(loc("HINT_AUTO_RESOURCE_GLOBAL")); // NOI18N
        guideLineColLabel.setToolTipText(loc("HINT_GUIDING_LINE_COLOR")); // NOI18N
        selectionBorderColLabel.setToolTipText(loc("HINT_SELECTION_BORDER_COLOR")); // NOI18N
        gridSizeLabel.setToolTipText(loc("HINT_GRID_SIZE")); // NOI18N
        cbFold.setToolTipText(loc("HINT_FOLD_GENERATED_CODE")); // NOI18N
        cbAssistant.setToolTipText(loc("HINT_ASSISTANT_SHOWN")); // NOI18N
        cbFQN.setToolTipText(loc("HINT_GENERATE_FQN")); // NOI18N
        cbPaintLayout.setToolTipText(loc("Paint_Layout_Hint")); // NOI18N
        rbGenerateLocals.getAccessibleContext().setAccessibleDescription(loc("Generate_Locals_ACSD")); // NOI18N
        rbGenerateFields.getAccessibleContext().setAccessibleDescription(loc("Generate_Fields_ACSD")); // NOI18N

        variableModifierLabel.setLabelFor(cbModifier);
        layoutStyleLabel.setLabelFor(cbLayoutStyle);
        componentNamesLabel.setLabelFor(cbComponentNames);        
        listenerStyleLabel.setLabelFor(cbListenerStyle);
        autoI18nLabel.setLabelFor(cbAutoI18n);
        guideLineColLabel.setLabelFor(guideLineColEditor);
        selectionBorderColLabel.setLabelFor(selectionBorderColEditor);
        gridSizeLabel.setLabelFor(spGridSize);

        brandVisibility("rbGenerateLocals", generateComponentsLabel, rbGenerateLocals, rbGenerateFields); // NOI18N
        brandVisibility("cbModifier", variableModifierLabel, cbModifier); // NOI18N
        brandVisibility("cbListenerStyle", listenerStyleLabel, cbListenerStyle); // NOI18N
        brandVisibility("cbAutoI18n", autoI18nLabel, cbAutoI18n); // NOI18N
        brandVisibility("cbLayoutStyle", layoutStyleLabel, cbLayoutStyle); // NOI18N
        brandVisibility("cbComponentNames", componentNamesLabel, cbComponentNames); // NOI18N
        brandVisibility("cbFold", cbFold); // NOI18N
        brandVisibility("cbAssistant", cbAssistant); // NOI18N
        brandVisibility("cbFQN", cbFQN); // NOI18N
        brandVisibility("guideLineColEditor", guideLineColLabel, guideLineColEditor); // NOI18NS
        brandVisibility("selectionBorderColEditor", selectionBorderColLabel, selectionBorderColEditor); // NOI18N
        brandVisibility("spGridSize", gridSizeLabel, spGridSize); // NOI18N
        brandVisibility("cbPaintLayout", cbPaintLayout); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        setLayout(layout);

        GroupLayout.ParallelGroup labelHorizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        addComponent(generateComponentsLabel, labelHorizontalGroup);
        addComponent(variableModifierLabel, labelHorizontalGroup);
        addComponent(componentNamesLabel, labelHorizontalGroup);
        addComponent(listenerStyleLabel, labelHorizontalGroup);
        addComponent(autoI18nLabel, labelHorizontalGroup);
        addComponent(guideLineColLabel, labelHorizontalGroup);
        addComponent(selectionBorderColLabel, labelHorizontalGroup);
        addComponent(gridSizeLabel, labelHorizontalGroup);
        addComponent(layoutStyleLabel, labelHorizontalGroup);

        GroupLayout.ParallelGroup componentHorizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
        addComponent(rbGenerateLocals, componentHorizontalGroup);
        addComponent(rbGenerateFields, componentHorizontalGroup);
        addComponent(cbFold, componentHorizontalGroup);
        addComponent(cbAssistant, componentHorizontalGroup);
        addComponent(cbFQN, componentHorizontalGroup);
        addComponent(cbModifier, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(cbComponentNames, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(cbListenerStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(cbAutoI18n, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(guideLineColEditor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(selectionBorderColEditor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(spGridSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 40, componentHorizontalGroup);
        addComponent(cbLayoutStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, componentHorizontalGroup);
        addComponent(cbPaintLayout, componentHorizontalGroup);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(labelHorizontalGroup)
                        .addGroup(componentHorizontalGroup)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeGenSettingsHint, 0, 0, Short.MAX_VALUE)
                        .addContainerGap()))
        );

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                generateComponentsLabel, rbGenerateLocals);
        addComponent(rbGenerateFields, verticalGroup);
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                variableModifierLabel, cbModifier);
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                listenerStyleLabel, cbListenerStyle);
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                autoI18nLabel, cbAutoI18n);
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                layoutStyleLabel, cbLayoutStyle);
        addLine(layout, verticalGroup, GroupLayout.Alignment.BASELINE,
                componentNamesLabel, cbComponentNames);
        addComponent(cbFQN, verticalGroup);
        addComponent(cbFold, verticalGroup);
        addComponent(cbAssistant, verticalGroup);
        addLine(layout, verticalGroup, GroupLayout.Alignment.CENTER,
                guideLineColLabel, guideLineColEditor);
        addLine(layout, verticalGroup, GroupLayout.Alignment.CENTER,
                selectionBorderColLabel, selectionBorderColEditor);
        addLine(layout, verticalGroup, GroupLayout.Alignment.CENTER,
                gridSizeLabel, spGridSize);
        addComponent(cbPaintLayout, verticalGroup);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        addComponent(codeGenSettingsHint, verticalGroup);
        verticalGroup.addContainerGap();

        layout.setVerticalGroup(verticalGroup);

        cbFold.addActionListener (this);
        cbAssistant.addActionListener(this);
        cbFQN.addActionListener(this);
        cbLayoutStyle.addActionListener (this);
        cbComponentNames.addActionListener (this);
        cbListenerStyle.addActionListener (this);
        cbModifier.addActionListener (this);
        rbGenerateFields.addActionListener (this);
        rbGenerateLocals.addActionListener (this);
        cbAutoI18n.addActionListener(this);
        spGridSize.addChangeListener(this);
        cbPaintLayout.addActionListener(this);
    }

    private void addComponent(Component comp, GroupLayout.Group targetGroup) {
        if (isVisible(comp)) {
            targetGroup.addComponent(comp);
        }
    }

    private void addComponent(Component comp, int min, int pref, int max, GroupLayout.Group targetGroup) {
        if (isVisible(comp)) {
            targetGroup.addComponent(comp, min, pref, max);
        }
    }

    private void addLine(GroupLayout layout, GroupLayout.Group targetGroup, GroupLayout.Alignment align,
                         Component... components) {
        if (!isVisible(components[0])) {
            return;
        }
        GroupLayout.Group group = layout.createParallelGroup(align);
        for (Component comp : components) {
            group.addComponent(comp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
        }
        targetGroup.addGroup(group);
    }

    private boolean isVisible(Component comp) {
        return brandedInvisibleComponents == null || !brandedInvisibleComponents.contains(comp);
    }

    private void brandVisibility(String key, Component... components) {
        if (FormUtils.getPresetValue("OPTIONS_"+key+"_HIDDEN", false)) { // NOI18N
            for (Component comp : components) {
                if (brandedInvisibleComponents == null) {
                    brandedInvisibleComponents = new HashSet<>();
                }
                brandedInvisibleComponents.add(comp);
            }
        }
    }

    private static String loc (String key) {
        return NbBundle.getMessage (FormEditorCustomizer.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton)c, loc(key));
        } else {
            Mnemonics.setLocalizedText((JLabel)c, loc(key));
        }
    }
    
    
    // other methods ...........................................................
    
    public void update() {
        listen = false;
        FormLoaderSettings options = FormLoaderSettings.getInstance();
        try {
            selectionBorderColorProperty = new ColorSettingsProperty("selectionBorderColor"); // NOI18N
            selectionBorderColEditor.setProperty(selectionBorderColorProperty);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            guideLineColorProperty = new ColorSettingsProperty("guidingLineColor"); // NOI18N
            guideLineColEditor.setProperty(guideLineColorProperty);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        cbFold.setSelected(options.getFoldGeneratedCode());
        cbAssistant.setSelected(options.getAssistantShown());
        cbFQN.setSelected(options.getGenerateFQN());
        rbGenerateLocals.setSelected(options.getVariablesLocal());
        rbGenerateFields.setSelected(!options.getVariablesLocal());
        if ((options.getVariablesModifier() & Modifier.PUBLIC) > 0) {
            cbModifier.setSelectedIndex(0);
        } else if ((options.getVariablesModifier() & Modifier.PROTECTED) > 0) {
            cbModifier.setSelectedIndex(2);
        } else if ((options.getVariablesModifier() & Modifier.PRIVATE) > 0) {
            cbModifier.setSelectedIndex(3);
        } else {
            cbModifier.setSelectedIndex(1);
        }
        if (!rbGenerateFields.isSelected()) {
            cbModifier.setEnabled(false);
        }
        cbListenerStyle.setSelectedIndex(options.getListenerGenerationStyle());
        cbLayoutStyle.setSelectedIndex(options.getLayoutCodeTarget());
        cbComponentNames.setSelectedIndex(options.getAutoSetComponentName());
        cbAutoI18n.setSelectedIndex(options.getI18nAutoMode());
        spGridSize.setValue(options.getGridX());
        cbPaintLayout.setSelected(options.getPaintAdvancedLayoutInfo() > 0);
        listen = true;
        changed = false;
    }

    public void applyChanges () {
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        options.setFoldGeneratedCode (cbFold.isSelected ());
        options.setAssistantShown(cbAssistant.isSelected());
        options.setGenerateFQN(cbFQN.isSelected());
        options.setListenerGenerationStyle (cbListenerStyle.getSelectedIndex ());
        options.setLayoutCodeTarget(cbLayoutStyle.getSelectedIndex ());
        options.setAutoSetComponentName(cbComponentNames.getSelectedIndex());
        options.setI18nAutoMode(cbAutoI18n.getSelectedIndex());
        options.setVariablesLocal (rbGenerateLocals.isSelected ());
        options.setGridX((Integer) spGridSize.getValue());
        options.setGridY((Integer) spGridSize.getValue());
        if (rbGenerateFields.isSelected()) {
            switch (cbModifier.getSelectedIndex ()) {
                case 0: options.setVariablesModifier (Modifier.PUBLIC);
                        break;
                case 1: options.setVariablesModifier (0);
                        break;
                case 2: options.setVariablesModifier (Modifier.PROTECTED);
                        break;
                case 3: options.setVariablesModifier (Modifier.PRIVATE);
                        break;
            }
        }
        guideLineColorProperty.apply();
        selectionBorderColorProperty.apply();
        options.setPaintAdvancedLayoutInfo(cbPaintLayout.isSelected() ? 3 : 0);
        changed = false;
    }

    void cancel () {
        changed = false;
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    private void fireChanged() {
        FormLoaderSettings options = FormLoaderSettings.getInstance();
        boolean isChanged = false;
        if (rbGenerateFields.isSelected()) {
            switch (cbModifier.getSelectedIndex()) {
                case 0:
                    isChanged = options.getVariablesModifier() != Modifier.PUBLIC;
                    break;
                case 1:
                    isChanged = options.getVariablesModifier() != 0;
                    break;
                case 2:
                    isChanged = options.getVariablesModifier() != Modifier.PROTECTED;
                    break;
                case 3:
                    isChanged = options.getVariablesModifier() != Modifier.PRIVATE;
                    break;
            }
        }
        try {
            changed = isChanged || options.getFoldGeneratedCode() != cbFold.isSelected()
                    || options.getAssistantShown() != cbAssistant.isSelected()
                    || options.getGenerateFQN() != cbFQN.isSelected()
                    || options.getListenerGenerationStyle() != cbListenerStyle.getSelectedIndex()
                    || options.getLayoutCodeTarget() != cbLayoutStyle.getSelectedIndex()
                    || options.getAutoSetComponentName() != cbComponentNames.getSelectedIndex()
                    || options.getI18nAutoMode() != cbAutoI18n.getSelectedIndex()
                    || options.getVariablesLocal() != rbGenerateLocals.isSelected()
                    || options.getGridX() != (Integer) spGridSize.getValue()
                    || options.getGridY() != (Integer) spGridSize.getValue()
                    || !options.getGuidingLineColor().equals(guideLineColorProperty.getValue())
                    || !options.getSelectionBorderColor().equals(selectionBorderColorProperty.getValue())
                    || options.getPaintAdvancedLayoutInfo() != (cbPaintLayout.isSelected() ? 3 : 0);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        if (listen) {
            fireChanged();
            if (rbGenerateLocals == e.getSource() && rbGenerateLocals.isSelected()) {
                cbModifier.setEnabled(false);
            } else if (rbGenerateFields == e.getSource() && rbGenerateFields.isSelected()) {
                cbModifier.setEnabled(true);
            }
        }
    }
    
    @Override
    public void stateChanged (ChangeEvent e) {
        if (listen) {
            fireChanged();
        }
    }

    private class ColorSettingsProperty extends PropertySupport.Reflection<Color> {
        private Color color;

        ColorSettingsProperty(String settingName) throws NoSuchMethodException {
            super(FormLoaderSettings.getInstance(), Color.class, settingName);
        }

        void apply() {
            if (color != null) {
                try {
                    super.setValue(color);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public Color getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (color != null) {
                return color;
            }
            return super.getValue();
        }
        
        @Override
        public void setValue(Color val) {
            color = val;
            fireChanged();
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return FormPropertyEditorManager.findBasicEditor(Color.class);
        }
    }
}
