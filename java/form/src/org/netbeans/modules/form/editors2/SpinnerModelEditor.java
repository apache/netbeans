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

package org.netbeans.modules.form.editors2;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import org.netbeans.modules.form.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Property editor for <code>SpinnerModel</code>.
 *
 * @author Jan Stola
 */
public class SpinnerModelEditor extends PropertyEditorSupport
        implements XMLPropertyEditor, NamedPropertyEditor, FormAwareEditor {

    /** Determines whether the components of custom editor have been created. */
    private boolean initialized;
    /** Determines whether custom property editor should fire value changes. */
    private boolean fireChanges;
    /** Property being edited. */
    private FormProperty property;
    
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
        if (value instanceof FormSpinnerModel) {
            updateUI((FormSpinnerModel)value);
        } else {
            // default
            modelTypeCombo.setSelectedIndex(0);
        }
        fireChanges = true;
        return customizerPanel;
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        String msg = NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_SpinnerModel"); // NOI18N
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, rectangle.x, rectangle.y + (rectangle.height - fm.getHeight())/2 + fm.getAscent());
    }

    /**
     * Initializes components of custom editor. 
     */
    private void initGUI() {
        fireChanges = false;
        initComponents();
        initModelTypeCombo();
        initCalendarFieldCombo();
        initNumberTypeCombo();
        initListItems();
        initialDateSpinner.setModel(new SpinnerDateModel());
        initialDateSpinner.setValue(new Date());
        minimumDateSpinner.setModel(new SpinnerDateModel());
        maximumDateSpinner.setModel(new SpinnerDateModel());
        stepSizeSpinner.setValue(Integer.valueOf(1));
        initialListLabel.setVisible(false);
        initialListCombo.setVisible(false);
        fireChanges = true;
        initialized = true;
    }

    /**
     * Initializes list items text area. 
     */
    private void initListItems() {
        String format = NbBundle.getMessage(getClass(), "SpinnerModelEditor_Item"); // NOI18N
        String[] items = new String[4];
        for (int i=0; i<items.length; i++) {
            items[i] = MessageFormat.format(format, i);
        }
        StringBuilder sb = new StringBuilder();
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        for (Object item : items) {
            sb.append(item).append('\n');
            comboModel.addElement(item);
        }
        listItemsArea.setText(sb.toString());
        initialListCombo.setModel(comboModel);
        initialListCombo.setSelectedIndex(0);
    }

    /**
     * Initializes model type combo box. 
     */
    private void initModelTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("SpinnerModelEditor_Default")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Date")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_List")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Number")); // NOI18N
        modelTypeCombo.setModel(model);
    }

    /**
     * Initializes calendar field combo box. 
     */
    private void initCalendarFieldCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("SpinnerModelEditor_Era")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Year")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Month")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_WeekOfYear")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_WeekOfMonth")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_DayOfMonth")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_DayOfYear")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_DayOfWeek")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_DayOfWeekInMonth")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_AMPM")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Hour")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_HourOfDay")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Minute")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Second")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Millisecond")); // NOI18N
        calendarFieldCombo.setModel(model);
        calendarFieldCombo.setSelectedIndex(5); // DAY_OF_MONTH
    }

    /**
     * Initializes number type combo box. 
     */
    private void initNumberTypeCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        model.addElement(bundle.getString("SpinnerModelEditor_Byte")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Double")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Float")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Integer")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Long")); // NOI18N
        model.addElement(bundle.getString("SpinnerModelEditor_Short")); // NOI18N
        numberTypeCombo.setModel(model);
        numberTypeCombo.setSelectedIndex(3); // Integer
    }

    /**
     * Updates UI of custom editor according to given <code>model</code>.
     * 
     * @param model form wrapper of spinner model.
     */
    private void updateUI(FormSpinnerModel model) {
        SpinnerModel spinnerModel = model.getModel();
        if (spinnerModel instanceof SpinnerDateModel) {
            modelTypeCombo.setSelectedIndex(1);
            updateDateUI(model);
        } else if (spinnerModel instanceof SpinnerListModel) {
            modelTypeCombo.setSelectedIndex(2);
            updateListUI(model);
        } else if (spinnerModel instanceof SpinnerNumberModel) {
            modelTypeCombo.setSelectedIndex(3);
            updateNumberUI(model);
        } else {
            assert false;
        }
        updateVisibilityOfModelProperties();
    }

    /**
     * Updates date section of UI of custom editor according to given <code>model</code>.
     * 
     * @param model form wrapper of spinner model.
     */
    private void updateDateUI(FormSpinnerModel model) {
        Date now = new Date();
        SpinnerDateModel dateModel = (SpinnerDateModel)model.getModel();
        initialNowCheckBox.setSelected(model.isInitialNow());
        initialDateSpinner.setValue(model.isInitialNow() ? now : model.getInitialValue());
        initialDateSpinner.setEnabled(!model.isInitialNow());
        // minimum
        Comparable minimum = dateModel.getStart();
        minimumDateCheckBox.setSelected(minimum != null);
        minimumNowCheckBox.setEnabled(minimum != null);
        minimumNowCheckBox.setSelected(model.isMinimumNow());
        minimumDateSpinner.setEnabled(minimum != null && !model.isMinimumNow());
        minimum = model.isMinimumNow() ? now : minimum;
        minimumDateSpinner.setValue(minimum == null ? now : minimum);
        // maximum
        Comparable maximum = dateModel.getEnd();
        maximumDateCheckBox.setSelected(maximum != null);
        maximumNowCheckBox.setEnabled(maximum != null);
        maximumNowCheckBox.setSelected(model.isMaximumNow());
        maximumDateSpinner.setEnabled(maximum != null && !model.isMaximumNow());
        maximum = model.isMaximumNow() ? now : maximum;
        maximumDateSpinner.setValue(maximum == null ? now : maximum);
        // calendar field
        int index = 0;
        switch (dateModel.getCalendarField()) {
            case Calendar.ERA: index = 0; break;
            case Calendar.YEAR: index = 1; break;
            case Calendar.MONTH: index = 2; break;
            case Calendar.WEEK_OF_YEAR: index = 3; break;
            case Calendar.WEEK_OF_MONTH: index = 4; break;
            case Calendar.DAY_OF_MONTH: index = 5; break;
            case Calendar.DAY_OF_YEAR: index = 6; break;
            case Calendar.DAY_OF_WEEK: index = 7; break;
            case Calendar.DAY_OF_WEEK_IN_MONTH: index = 8; break;
            case Calendar.AM_PM: index = 9; break;
            case Calendar.HOUR: index = 10; break;
            case Calendar.HOUR_OF_DAY: index = 11; break;
            case Calendar.MINUTE: index = 12; break;
            case Calendar.SECOND: index = 13; break;
            case Calendar.MILLISECOND: index = 14; break;
        }
        calendarFieldCombo.setSelectedIndex(index);
    }

    /**
     * Updates list section of UI of custom editor according to given <code>model</code>.
     * 
     * @param model form wrapper of spinner model.
     */
    private void updateListUI(FormSpinnerModel model) {
        SpinnerListModel listModel = (SpinnerListModel)model.getModel();
        List items = listModel.getList();
        StringBuilder sb = new StringBuilder();
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        for (Object item : items) {
            sb.append(item).append('\n');
            comboModel.addElement(item);
        }
        listItemsArea.setText(sb.toString());
        initialListCombo.setModel(comboModel);
        initialListCombo.setSelectedItem(model.getInitialValue());
    }

    /**
     * Updates number section of UI of custom editor according to given <code>model</code>.
     * 
     * @param model form wrapper of spinner model.
     */
    private void updateNumberUI(FormSpinnerModel model) {
        SpinnerNumberModel numberModel = (SpinnerNumberModel)model.getModel();
        Class<?> clazz = model.getInitialValue().getClass();
        int typeIndex = 0;
        if (clazz == Byte.class) {
            typeIndex = 0;
        } else if (clazz == Double.class) {
            typeIndex = 1;
        } else if (clazz == Float.class) {
            typeIndex = 2;
        } else if (clazz == Integer.class) {
            typeIndex = 3;
        } else if (clazz == Long.class) {
            typeIndex = 4;
        } else if (clazz == Short.class) {
            typeIndex = 5;
        } else {
            assert false;
        }
        numberTypeCombo.setSelectedIndex(typeIndex);
        minimumNumberSpinner.setValue(Integer.valueOf(0));
        maximumNumberSpinner.setValue(Integer.valueOf(0));
        updateNumberEditors();
        initialNumberSpinner.setValue(model.getInitialValue());
        Comparable minimum = numberModel.getMinimum();
        minimumNumberCheckBox.setSelected(minimum != null);
        minimumNumberSpinner.setEnabled(minimum != null);
        if (minimum != null) {
            minimumNumberSpinner.setValue(minimum);
        }
        Comparable maximum = numberModel.getMaximum();
        maximumNumberCheckBox.setSelected(maximum != null);
        maximumNumberSpinner.setEnabled(maximum != null);
        if (maximum != null) {
            maximumNumberSpinner.setValue(maximum);
        }
        stepSizeSpinner.setValue(numberModel.getStepSize());
    }

    /**
     * Updates value of the property editor according to UI.
     */
    private void updateFromUI() {
        if (!fireChanges) return;
        Object value = valueFromUI();
        if (value == null) {
            value = property.getDefaultValue();
        }
        setValue(value);
    }

    /**
     * Returns spinner model represented by custom editor. 
     * 
     * @return spinner model represented by custom editor.
     */
    private FormSpinnerModel valueFromUI() {
        FormSpinnerModel value = null;
        int modelType = modelTypeCombo.getSelectedIndex();
        if (modelType == 0) {
            // default
            value = null;
        } else if (modelType == 1) {
            // date
            value = dateFromUI();
        } else if (modelType == 2) {
            // list
            value = listFromUI();
        } else if (modelType == 3) {
            // number
            value = numberFromUI();
        } else {
            assert false;
        }
        return value;
    }

    /**
     * Returns date spinner model represented by custom editor.
     * 
     * @return date spinner model represented by custom editor.
     */
    private FormSpinnerModel dateFromUI() {
        Date now = new Date();
        boolean minimumNow = false;
        Date minimum = null;
        if (minimumDateCheckBox.isSelected()) {
            if (minimumNowCheckBox.isSelected()) {
                minimumNow = true;
                minimum = now;
            } else {
                minimum = (Date)minimumDateSpinner.getValue();
            }
        }
        boolean initialNow = false;
        Date initial;
        if (initialNowCheckBox.isSelected()) {
            initialNow = true;
            initial = now;
        } else {
            initial = (Date)initialDateSpinner.getValue();
            if (minimumNow && (initial.getTime()-minimum.getTime() < 0)) {
                initial = minimum;
            }
        }
        boolean maximumNow = false;
        Date maximum = null;
        if (maximumDateCheckBox.isSelected()) {
            if (maximumNowCheckBox.isSelected()) {
                maximumNow = true;
                maximum = now;
            } else {
                maximum = (Date)maximumDateSpinner.getValue();
                if (initialNow && (maximum.getTime()-initial.getTime() < 0)) {
                    maximum = initial;
                }
                if (minimumNow && (maximum.getTime()-minimum.getTime() < 0)) {
                    maximum = minimum;
                }
            }
        }
        int field = 0;
        switch (calendarFieldCombo.getSelectedIndex()) {
            case 0: field = Calendar.ERA; break;
            case 1: field = Calendar.YEAR; break;
            case 2: field = Calendar.MONTH; break;
            case 3: field = Calendar.WEEK_OF_YEAR; break;
            case 4: field = Calendar.WEEK_OF_MONTH; break;
            case 5: field = Calendar.DAY_OF_MONTH; break;
            case 6: field = Calendar.DAY_OF_YEAR; break;
            case 7: field = Calendar.DAY_OF_WEEK; break;
            case 8: field = Calendar.DAY_OF_WEEK_IN_MONTH; break;
            case 9: field = Calendar.AM_PM; break;
            case 10: field = Calendar.HOUR; break;
            case 11: field = Calendar.HOUR_OF_DAY; break;
            case 12: field = Calendar.MINUTE; break;
            case 13: field = Calendar.SECOND; break;
            case 14: field = Calendar.MILLISECOND; break;
            default: assert false;
        }
        SpinnerDateModel model = new SpinnerDateModel(initial, minimum, maximum, field);
        return new FormSpinnerModel(model, initial, initialNow, minimumNow, maximumNow);
    }

    /**
     * Returns list spinner model represented by custom editor.
     * 
     * @return list spinner model represented by custom editor.
     */
    private FormSpinnerModel listFromUI() {
        String initial = (String)initialListCombo.getSelectedItem();
        List<String> listItems = listItemsFromUI();
        SpinnerListModel model = new SpinnerListModel(listItems);
        return new FormSpinnerModel(model, initial);
    }

    /**
     * Returns spinner list model items represented by custom editor.
     * 
     * @return spinner list model items represented by custom editor.
     */
    private List<String> listItemsFromUI() {
        String itemsText = listItemsArea.getText();
        StringTokenizer st = new StringTokenizer(itemsText, "\n"); // NOI18N
        List<String> items = new LinkedList<String>();
        while (st.hasMoreTokens()) {
            items.add(st.nextToken());
        }
        if (items.isEmpty()) {
            items.add(""); // NOI18N
        }
        return items;
    }

    /**
     * Returns selected number type. 
     * 
     * @return selected number type.
     */
    private Class selectedNumberType() {
        Class<?> clazz = null;
        switch (numberTypeCombo.getSelectedIndex()) {
            case 0: clazz = Byte.class; break;
            case 1: clazz = Double.class; break;
            case 2: clazz = Float.class; break;
            case 3: clazz = Integer.class; break;
            case 4: clazz = Long.class; break;
            case 5: clazz = Short.class; break;
            default: assert false;
        }
        return clazz;
    }

    /**
     * Returns number spinner model represented by custom editor.
     * 
     * @return number spinner model represented by custom editor.
     */
    private FormSpinnerModel numberFromUI() {
        Number initial = (Number)initialNumberSpinner.getValue();
        Number minimum = null;
        if (minimumNumberCheckBox.isSelected()) {
            minimum = (Number)minimumNumberSpinner.getValue();
        }
        Number maximum = null;
        if (maximumNumberCheckBox.isSelected()) {
            maximum = (Number)maximumNumberSpinner.getValue();
        }
        Number stepSize = (Number)stepSizeSpinner.getValue();
        Number[] n = cast(selectedNumberType(), initial, minimum, maximum, stepSize);
        SpinnerNumberModel model = new SpinnerNumberModel(n[0], (Comparable)n[1], (Comparable)n[2], n[3]);
        return new FormSpinnerModel(model, n[0]);
    }

    /**
     * Casts given numbers to the specified type.
     * 
     * @param clazz class to cast to.
     * @param number numbers to cast.
     * @return numbers casted to the specified type.
     */
    private static Number[] cast(Class clazz, Number... number) {
        Number[] result = new Number[number.length];
        for (int i=0; i<number.length; i++) {
            Number n = number[i];
            if (n == null) {
                result[i] = null; 
            } else {
                if (Integer.class == clazz) {
                    result[i] = Integer.valueOf(n.intValue());
                } else if (Long.class == clazz) {
                    result[i] = Long.valueOf(n.longValue());
                } else if (Float.class == clazz) {
                    result[i] = Float.valueOf(n.floatValue());
                } else if (Double.class == clazz) {
                    result[i] = Double.valueOf(n.doubleValue());
                } else if (Byte.class == clazz) {
                    result[i] = Byte.valueOf(n.byteValue());
                } else if (Short.class == clazz) {
                    result[i] = Short.valueOf(n.shortValue());
                } else {
                    assert false;
                }
            }
        }
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customizerPanel = new javax.swing.JPanel();
        modelTypeLabel = new javax.swing.JLabel();
        modelTypeCombo = new javax.swing.JComboBox();
        modelPropertiesLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        modelTypePanel = new javax.swing.JPanel();
        datePanel = new javax.swing.JPanel();
        initialDateLabel = new javax.swing.JLabel();
        initialDateSpinner = new javax.swing.JSpinner();
        initialNowCheckBox = new javax.swing.JCheckBox();
        minimumDateCheckBox = new javax.swing.JCheckBox();
        minimumDateSpinner = new javax.swing.JSpinner();
        minimumNowCheckBox = new javax.swing.JCheckBox();
        maximumDateCheckBox = new javax.swing.JCheckBox();
        maximumDateSpinner = new javax.swing.JSpinner();
        maximumNowCheckBox = new javax.swing.JCheckBox();
        calendarFieldLabel = new javax.swing.JLabel();
        calendarFieldCombo = new javax.swing.JComboBox();
        listPanel = new javax.swing.JPanel();
        listItemsLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        listItemsArea = new javax.swing.JTextArea();
        initialListLabel = new javax.swing.JLabel();
        initialListCombo = new javax.swing.JComboBox();
        numberPanel = new javax.swing.JPanel();
        numberTypeLabel = new javax.swing.JLabel();
        numberTypeCombo = new javax.swing.JComboBox();
        initialNumberLabel = new javax.swing.JLabel();
        initialNumberSpinner = new javax.swing.JSpinner();
        minimumNumberCheckBox = new javax.swing.JCheckBox();
        minimumNumberSpinner = new javax.swing.JSpinner();
        maximumNumberCheckBox = new javax.swing.JCheckBox();
        maximumNumberSpinner = new javax.swing.JSpinner();
        stepSizeLabel = new javax.swing.JLabel();
        stepSizeSpinner = new javax.swing.JSpinner();
        defaultPanel = new javax.swing.JPanel();

        FormListener formListener = new FormListener();

        modelTypeLabel.setLabelFor(modelTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(modelTypeLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_ModelType")); // NOI18N

        modelTypeCombo.addActionListener(formListener);

        modelPropertiesLabel.setText(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_ModelProperties")); // NOI18N

        modelTypePanel.setLayout(new java.awt.CardLayout());

        initialDateLabel.setLabelFor(initialDateSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(initialDateLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialDate")); // NOI18N

        initialDateSpinner.setEnabled(false);
        initialDateSpinner.addChangeListener(formListener);

        initialNowCheckBox.setSelected(true);
        initialNowCheckBox.setText(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialNow")); // NOI18N
        initialNowCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initialNowCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initialNowCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(minimumDateCheckBox, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumDate")); // NOI18N
        minimumDateCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        minimumDateCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        minimumDateCheckBox.addActionListener(formListener);

        minimumDateSpinner.setEnabled(false);
        minimumDateSpinner.addChangeListener(formListener);

        minimumNowCheckBox.setText(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNow")); // NOI18N
        minimumNowCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        minimumNowCheckBox.setEnabled(false);
        minimumNowCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        minimumNowCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(maximumDateCheckBox, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumDate")); // NOI18N
        maximumDateCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        maximumDateCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        maximumDateCheckBox.addActionListener(formListener);

        maximumDateSpinner.setEnabled(false);
        maximumDateSpinner.addChangeListener(formListener);

        maximumNowCheckBox.setText(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNow")); // NOI18N
        maximumNowCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        maximumNowCheckBox.setEnabled(false);
        maximumNowCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        maximumNowCheckBox.addActionListener(formListener);

        calendarFieldLabel.setLabelFor(calendarFieldCombo);
        org.openide.awt.Mnemonics.setLocalizedText(calendarFieldLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_CalendarField")); // NOI18N

        calendarFieldCombo.addActionListener(formListener);

        javax.swing.GroupLayout datePanelLayout = new javax.swing.GroupLayout(datePanel);
        datePanel.setLayout(datePanelLayout);
        datePanelLayout.setHorizontalGroup(
            datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(initialDateLabel)
                    .addComponent(minimumDateCheckBox)
                    .addComponent(maximumDateCheckBox)
                    .addComponent(calendarFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(initialDateSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(minimumDateSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(maximumDateSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(calendarFieldCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(datePanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(maximumNowCheckBox))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, datePanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(minimumNowCheckBox)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, datePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initialNowCheckBox)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        datePanelLayout.setVerticalGroup(
            datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialDateLabel)
                    .addComponent(initialNowCheckBox)
                    .addComponent(initialDateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minimumDateCheckBox)
                    .addComponent(minimumNowCheckBox)
                    .addComponent(minimumDateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maximumDateCheckBox)
                    .addComponent(maximumNowCheckBox)
                    .addComponent(maximumDateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(calendarFieldLabel)
                    .addComponent(calendarFieldCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        initialDateSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialDate_ACSD")); // NOI18N
        initialNowCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialNow_ACSD")); // NOI18N
        minimumDateCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumDate_ACSD")); // NOI18N
        minimumDateSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumDate_ACSN")); // NOI18N
        minimumDateSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumDate_ACSD")); // NOI18N
        minimumNowCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNow_ACSD")); // NOI18N
        maximumDateCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumDate_ACSD")); // NOI18N
        maximumDateSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumDate_ACSN")); // NOI18N
        maximumDateSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumDate_ACSD")); // NOI18N
        maximumNowCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNow_ACSD")); // NOI18N
        calendarFieldCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_StepSize_ACSD")); // NOI18N

        modelTypePanel.add(datePanel, "date");

        listItemsLabel.setLabelFor(listItemsArea);
        org.openide.awt.Mnemonics.setLocalizedText(listItemsLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_ListItems")); // NOI18N

        listItemsArea.setRows(4);
        listItemsArea.addFocusListener(formListener);
        scrollPane.setViewportView(listItemsArea);
        listItemsArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_ListItems")); // NOI18N

        initialListLabel.setLabelFor(initialListCombo);
        org.openide.awt.Mnemonics.setLocalizedText(initialListLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialList")); // NOI18N

        initialListCombo.addActionListener(formListener);

        javax.swing.GroupLayout listPanelLayout = new javax.swing.GroupLayout(listPanel);
        listPanel.setLayout(listPanelLayout);
        listPanelLayout.setHorizontalGroup(
            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addComponent(listItemsLabel)
                    .addGroup(listPanelLayout.createSequentialGroup()
                        .addComponent(initialListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initialListCombo, 0, 84, Short.MAX_VALUE)))
                .addContainerGap())
        );
        listPanelLayout.setVerticalGroup(
            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listItemsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialListLabel)
                    .addComponent(initialListCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        initialListCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialList_ACSD")); // NOI18N

        modelTypePanel.add(listPanel, "list");

        numberTypeLabel.setLabelFor(numberTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(numberTypeLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_NumberType")); // NOI18N

        numberTypeCombo.addActionListener(formListener);

        initialNumberLabel.setLabelFor(initialNumberSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(initialNumberLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialNumber")); // NOI18N

        initialNumberSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(minimumNumberCheckBox, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNumber")); // NOI18N
        minimumNumberCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        minimumNumberCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        minimumNumberCheckBox.addActionListener(formListener);

        minimumNumberSpinner.setEnabled(false);
        minimumNumberSpinner.addChangeListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(maximumNumberCheckBox, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNumber")); // NOI18N
        maximumNumberCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        maximumNumberCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        maximumNumberCheckBox.addActionListener(formListener);

        maximumNumberSpinner.setEnabled(false);
        maximumNumberSpinner.addChangeListener(formListener);

        stepSizeLabel.setLabelFor(stepSizeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(stepSizeLabel, org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_StepSize")); // NOI18N

        stepSizeSpinner.addChangeListener(formListener);

        javax.swing.GroupLayout numberPanelLayout = new javax.swing.GroupLayout(numberPanel);
        numberPanel.setLayout(numberPanelLayout);
        numberPanelLayout.setHorizontalGroup(
            numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(numberPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(numberTypeLabel)
                    .addComponent(initialNumberLabel)
                    .addComponent(minimumNumberCheckBox)
                    .addComponent(maximumNumberCheckBox)
                    .addComponent(stepSizeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(numberTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(initialNumberSpinner)
                    .addComponent(minimumNumberSpinner)
                    .addComponent(maximumNumberSpinner)
                    .addComponent(stepSizeSpinner))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        numberPanelLayout.setVerticalGroup(
            numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(numberPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberTypeLabel)
                    .addComponent(numberTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialNumberLabel)
                    .addComponent(initialNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minimumNumberCheckBox)
                    .addComponent(minimumNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maximumNumberCheckBox)
                    .addComponent(maximumNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stepSizeLabel)
                    .addComponent(stepSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        numberTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_NumberType_ACSD")); // NOI18N
        initialNumberSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_InitialNumber_ACSD")); // NOI18N
        minimumNumberCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNumber_ACSD")); // NOI18N
        minimumNumberSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNumber_ACSN")); // NOI18N
        minimumNumberSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MinimumNumber_ACSD")); // NOI18N
        maximumNumberCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNumber_ACSD")); // NOI18N
        maximumNumberSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNumber_ACSN")); // NOI18N
        maximumNumberSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_MaximumNumber_ACSD")); // NOI18N
        stepSizeSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_StepSize_ACSD")); // NOI18N

        modelTypePanel.add(numberPanel, "number");

        javax.swing.GroupLayout defaultPanelLayout = new javax.swing.GroupLayout(defaultPanel);
        defaultPanel.setLayout(defaultPanelLayout);
        defaultPanelLayout.setHorizontalGroup(
            defaultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 167, Short.MAX_VALUE)
        );
        defaultPanelLayout.setVerticalGroup(
            defaultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        modelTypePanel.add(defaultPanel, "default");

        javax.swing.GroupLayout customizerPanelLayout = new javax.swing.GroupLayout(customizerPanel);
        customizerPanel.setLayout(customizerPanelLayout);
        customizerPanelLayout.setHorizontalGroup(
            customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(modelTypePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, customizerPanelLayout.createSequentialGroup()
                        .addComponent(modelTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, customizerPanelLayout.createSequentialGroup()
                        .addComponent(modelPropertiesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)))
                .addContainerGap())
        );
        customizerPanelLayout.setVerticalGroup(
            customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelTypeLabel)
                    .addComponent(modelTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(customizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(modelPropertiesLabel)
                    .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        modelTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpinnerModelEditor.class, "SpinnerModelEditor_ModelType_ACSD")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener, javax.swing.event.ChangeListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == modelTypeCombo) {
                SpinnerModelEditor.this.modelTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == initialNowCheckBox) {
                SpinnerModelEditor.this.initialNowCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == minimumDateCheckBox) {
                SpinnerModelEditor.this.minimumDateCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == minimumNowCheckBox) {
                SpinnerModelEditor.this.minimumNowCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == maximumDateCheckBox) {
                SpinnerModelEditor.this.maximumDateCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == maximumNowCheckBox) {
                SpinnerModelEditor.this.maximumNowCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == calendarFieldCombo) {
                SpinnerModelEditor.this.calendarFieldComboActionPerformed(evt);
            }
            else if (evt.getSource() == initialListCombo) {
                SpinnerModelEditor.this.initialListComboActionPerformed(evt);
            }
            else if (evt.getSource() == numberTypeCombo) {
                SpinnerModelEditor.this.numberTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == minimumNumberCheckBox) {
                SpinnerModelEditor.this.minimumNumberCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == maximumNumberCheckBox) {
                SpinnerModelEditor.this.maximumNumberCheckBoxActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == listItemsArea) {
                SpinnerModelEditor.this.listItemsAreaFocusLost(evt);
            }
        }

        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            if (evt.getSource() == initialDateSpinner) {
                SpinnerModelEditor.this.initialDateSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == minimumDateSpinner) {
                SpinnerModelEditor.this.minimumDateSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == maximumDateSpinner) {
                SpinnerModelEditor.this.maximumDateSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == initialNumberSpinner) {
                SpinnerModelEditor.this.initialNumberSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == minimumNumberSpinner) {
                SpinnerModelEditor.this.minimumNumberSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == maximumNumberSpinner) {
                SpinnerModelEditor.this.maximumNumberSpinnerStateChanged(evt);
            }
            else if (evt.getSource() == stepSizeSpinner) {
                SpinnerModelEditor.this.stepSizeSpinnerStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void listItemsAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_listItemsAreaFocusLost
        if (!fireChanges) return;
        fireChanges = false;
        int selectedIndex = initialListCombo.getSelectedIndex();
        Object selected = initialListCombo.getSelectedItem();
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();        
        for (String item : listItemsFromUI()) {
            comboModel.addElement(item);
        }
        initialListCombo.setModel(comboModel);
        // Try to keep the selection
        if (selected != null) {
            initialListCombo.setSelectedItem(selected);
            if (!selected.equals(initialListCombo.getSelectedItem())) {
                if (initialListCombo.getModel().getSize() > selectedIndex) {
                    initialListCombo.setSelectedIndex(selectedIndex);
                }
            }
        }
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_listItemsAreaFocusLost

    private void initialListComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialListComboActionPerformed
        updateFromUI();
    }//GEN-LAST:event_initialListComboActionPerformed

    private void maximumNumberCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maximumNumberCheckBoxActionPerformed
        if (!fireChanges) return;
        maximumNumberSpinner.setEnabled(maximumNumberCheckBox.isSelected());
        if (maximumNumberCheckBox.isSelected()) {
            Comparable maximum = (Comparable)maximumNumberSpinner.getValue();
            Comparable initial = (Comparable)initialNumberSpinner.getValue();
            if (maximum.compareTo(initial) < 0) {
                fireChanges = false;
                maximumNumberSpinner.setValue(initial);
                fireChanges = true;
            }
        }
        updateFromUI();
    }//GEN-LAST:event_maximumNumberCheckBoxActionPerformed

    private void minimumNumberCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimumNumberCheckBoxActionPerformed
        if (!fireChanges) return;
        minimumNumberSpinner.setEnabled(minimumNumberCheckBox.isSelected());
        if (minimumNumberCheckBox.isSelected()) {
            Comparable minimum = (Comparable)minimumNumberSpinner.getValue();
            Comparable initial = (Comparable)initialNumberSpinner.getValue();
            if (minimum.compareTo(initial) > 0) {
                fireChanges = false;
                minimumNumberSpinner.setValue(initial);
                fireChanges = true;
            }
        }
        updateFromUI();
    }//GEN-LAST:event_minimumNumberCheckBoxActionPerformed

    private void maximumNumberSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maximumNumberSpinnerStateChanged
        maximumNumberUpdated();
        updateFromUI();
    }//GEN-LAST:event_maximumNumberSpinnerStateChanged

    private void minimumNumberSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minimumNumberSpinnerStateChanged
        minimumNumberUpdated();
        updateFromUI();
    }//GEN-LAST:event_minimumNumberSpinnerStateChanged

    private void initialNumberSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_initialNumberSpinnerStateChanged
        if (!fireChanges) return;
        fireChanges = false;
        Comparable initial = (Comparable)initialNumberSpinner.getValue();
        if (minimumNumberCheckBox.isSelected()) {
            Comparable minimum = (Comparable)minimumNumberSpinner.getValue();
            if (initial.compareTo(minimum) < 0) {
                minimumNumberSpinner.setValue(initial);
            }
        }
        if (maximumNumberCheckBox.isSelected()) {
            Comparable maximum = (Comparable)maximumNumberSpinner.getValue();
            if (initial.compareTo(maximum) > 0) {
                maximumNumberSpinner.setValue(initial);
            }
        }
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_initialNumberSpinnerStateChanged

    private void stepSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_stepSizeSpinnerStateChanged
        updateFromUI();
    }//GEN-LAST:event_stepSizeSpinnerStateChanged

    private void numberTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberTypeComboActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        updateNumberEditors();
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_numberTypeComboActionPerformed

    /**
     * Updates spinners in the number section of the custom editor to reflect
     * selected number type. 
     */
    private void updateNumberEditors() {
        Number[] n = cast(selectedNumberType(),
            (Number)initialNumberSpinner.getValue(),
            (Number)minimumNumberSpinner.getValue(),
            (Number)maximumNumberSpinner.getValue(),
            (Number)stepSizeSpinner.getValue());
        initialNumberSpinner.setValue(n[0]);
        initialNumberSpinner.setEditor(new JSpinner.NumberEditor(initialNumberSpinner));
        minimumNumberSpinner.setValue(n[1]);
        minimumNumberSpinner.setEditor(new JSpinner.NumberEditor(minimumNumberSpinner));
        maximumNumberSpinner.setValue(n[2]);
        maximumNumberSpinner.setEditor(new JSpinner.NumberEditor(maximumNumberSpinner));
        stepSizeSpinner.setValue(n[3]);
        stepSizeSpinner.setEditor(new JSpinner.NumberEditor(stepSizeSpinner));
    }

    private void calendarFieldComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarFieldComboActionPerformed
        updateFromUI();
    }//GEN-LAST:event_calendarFieldComboActionPerformed

    private void maximumDateSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maximumDateSpinnerStateChanged
        if (!fireChanges) return;
        maximumDateUpdated();
        updateFromUI();
    }//GEN-LAST:event_maximumDateSpinnerStateChanged

    private void minimumDateSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minimumDateSpinnerStateChanged
        if (!fireChanges) return;
        minimumDateUpdated();
        updateFromUI();
    }//GEN-LAST:event_minimumDateSpinnerStateChanged

    private void initialDateSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_initialDateSpinnerStateChanged
        if (!fireChanges) return;
        initialDateUpdated();
        updateFromUI();
    }//GEN-LAST:event_initialDateSpinnerStateChanged

    private void maximumDateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maximumDateCheckBoxActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        boolean enabled = maximumDateCheckBox.isSelected();
        maximumNowCheckBox.setEnabled(enabled);
        if (enabled) {
            Date maximum = (Date)maximumDateSpinner.getValue();
            Date initial = (Date)initialDateSpinner.getValue();
            if (maximum.getTime() - initial.getTime() < 0) {
                if (maximumNowCheckBox.isSelected()) {
                    if (!initialNowCheckBox.isSelected()) {
                        maximumNowCheckBox.setSelected(false);
                        maximumDateSpinner.setValue(initial);
                    }
                } else {
                    maximumDateSpinner.setValue(initial);
                }
            }
        }
        maximumDateSpinner.setEnabled(enabled && !maximumNowCheckBox.isSelected());
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_maximumDateCheckBoxActionPerformed

    private void minimumDateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimumDateCheckBoxActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        boolean enabled = minimumDateCheckBox.isSelected();
        minimumNowCheckBox.setEnabled(enabled);
        if (enabled) {
            Date minimum = (Date)minimumDateSpinner.getValue();
            Date initial = (Date)initialDateSpinner.getValue();
            if (initial.getTime() - minimum.getTime() < 0) {
                if (minimumNowCheckBox.isSelected()) {
                    if (!initialNowCheckBox.isSelected()) {
                        minimumNowCheckBox.setSelected(false);
                        minimumDateSpinner.setValue(initial);
                    }
                } else {
                    minimumDateSpinner.setValue(initial);
                }
            }
        }
        minimumDateSpinner.setEnabled(enabled && !minimumNowCheckBox.isSelected());
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_minimumDateCheckBoxActionPerformed

    private void maximumNowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maximumNowCheckBoxActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        boolean enabled = !maximumNowCheckBox.isSelected();
        maximumDateSpinner.setEnabled(enabled);
        if (!enabled) {
            maximumDateSpinner.setValue(new Date());
        }
        maximumDateUpdated();
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_maximumNowCheckBoxActionPerformed

    private void minimumNowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimumNowCheckBoxActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        boolean enabled = !minimumNowCheckBox.isSelected();
        minimumDateSpinner.setEnabled(enabled);
        if (!enabled) {
            minimumDateSpinner.setValue(new Date());
        }
        minimumDateUpdated();
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_minimumNowCheckBoxActionPerformed

    private void initialNowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialNowCheckBoxActionPerformed
        if (!fireChanges) return;
        fireChanges = false;
        boolean enabled = !initialNowCheckBox.isSelected();
        initialDateSpinner.setEnabled(enabled);
        if (!enabled) {
            initialDateSpinner.setValue(new Date());
        }
        initialDateUpdated();
        fireChanges = true;
        updateFromUI();
    }//GEN-LAST:event_initialNowCheckBoxActionPerformed

    private void modelTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelTypeComboActionPerformed
        CardLayout layout = (CardLayout)modelTypePanel.getLayout();
        String name = null;
        switch (modelTypeCombo.getSelectedIndex()) {
            case 0: name = "default"; break; // NOI18N
            case 1: name = "date"; break; // NOI18N
            case 2: name = "list"; break; // NOI18N
            case 3: name = "number"; break; // NOI18N
            default: assert false;
        }
        updateVisibilityOfModelProperties();        
        layout.show(modelTypePanel, name);
        updateFromUI();
    }//GEN-LAST:event_modelTypeComboActionPerformed

    /**
     * Hides or shows "Model Properties" label and separator.
     */
    private void updateVisibilityOfModelProperties() {
        boolean visible = (modelTypeCombo.getSelectedIndex() != 0);
        modelPropertiesLabel.setVisible(visible);
        separator.setVisible(visible);
    }

    /**
     * Makes the model consistent after change of the minimum number.
     */
    private void minimumNumberUpdated() {
        if (!fireChanges) return;
        fireChanges = false;
        if (minimumNumberCheckBox.isSelected()) {
            Comparable minimum = (Comparable)minimumNumberSpinner.getValue();
            Comparable initial = (Comparable)initialNumberSpinner.getValue();
            if (initial.compareTo(minimum) < 0) {
                initialNumberSpinner.setValue(minimum);
            }
            if (maximumNumberCheckBox.isSelected()) {
                Comparable maximum = (Comparable)maximumNumberSpinner.getValue();
                if (maximum.compareTo(minimum) < 0) {
                    maximumNumberSpinner.setValue(minimum);
                }
            }
        }
        fireChanges = true;
    }

    /**
     * Makes the model consistent after change of the maximum number.
     */
    private void maximumNumberUpdated() {
        if (!fireChanges) return;
        fireChanges = false;
        if (maximumNumberCheckBox.isSelected()) {
            Comparable maximum = (Comparable)maximumNumberSpinner.getValue();
            Comparable initial = (Comparable)initialNumberSpinner.getValue();
            if (initial.compareTo(maximum) > 0) {
                initialNumberSpinner.setValue(maximum);
            }
            if (minimumNumberCheckBox.isSelected()) {
                Comparable minimum = (Comparable)minimumNumberSpinner.getValue();
                if (maximum.compareTo(minimum) < 0) {
                    minimumNumberSpinner.setValue(maximum);
                }
            }
        }
        fireChanges = true;
    }

    /**
     * Makes the model consistent after change of the initial date.
     */
    private void initialDateUpdated() {
        fireChanges = false;
        Date initial = (Date)initialDateSpinner.getValue();
        if (minimumDateCheckBox.isSelected()) {
            // make sure minimum <= initial
            Date minimum = (Date)minimumDateSpinner.getValue();
            if (initial.getTime()-minimum.getTime() < 0) {
                if (minimumNowCheckBox.isSelected()) {
                    if (!initialNowCheckBox.isSelected()) {
                        minimumNowCheckBox.setSelected(false);
                        minimumDateSpinner.setEnabled(true);
                        minimumDateSpinner.setValue(initial);
                    }
                } else {
                    minimumDateSpinner.setValue(initial);
                }
            }
        }
        if (maximumDateCheckBox.isSelected()) {
            // make sure initial <= maximum
            Date maximum = (Date)maximumDateSpinner.getValue();
            if (maximum.getTime()-initial.getTime() < 0) {
                if (maximumNowCheckBox.isSelected()) {
                    if (!initialNowCheckBox.isSelected()) {
                        maximumNowCheckBox.setSelected(false);
                        maximumDateSpinner.setEnabled(true);
                        maximumDateSpinner.setValue(initial);
                    }
                } else {
                    maximumDateSpinner.setValue(initial);
                }
            }
        }
        fireChanges = true;
    }

    /**
     * Makes the model consistent after change of the minimum date.
     */
    private void minimumDateUpdated() {
        fireChanges = false;
        if (!minimumDateCheckBox.isSelected()) return;
        Date minimum = (Date)minimumDateSpinner.getValue();
        // make sure minimum <= initial
        Date initial = (Date)initialDateSpinner.getValue();
        if (initial.getTime()-minimum.getTime() < 0) {
            if (initialNowCheckBox.isSelected()) {
                if (!minimumNowCheckBox.isSelected()) {
                    initialNowCheckBox.setSelected(false);
                    initialDateSpinner.setEnabled(true);
                    initialDateSpinner.setValue(minimum);
                }
            } else {
                initialDateSpinner.setValue(minimum);
            }
        }
        if (maximumDateCheckBox.isSelected()) {
            // make sure minimum <= maximum
            Date maximum = (Date)maximumDateSpinner.getValue();
            if (maximum.getTime()-minimum.getTime() < 0) {
                if (maximumNowCheckBox.isSelected()) {
                    if (!minimumNowCheckBox.isSelected()) {
                        maximumNowCheckBox.setSelected(false);
                        maximumDateSpinner.setEnabled(true);
                        maximumDateSpinner.setValue(minimum);
                    }
                } else {
                    maximumDateSpinner.setValue(minimum);
                }
            }
        }
        fireChanges = true;
    }

    /**
     * Makes the model consistent after change of the maximum date.
     */
    private void maximumDateUpdated() {
        if (!maximumDateCheckBox.isSelected()) return;
        fireChanges = false;
        Date maximum = (Date)maximumDateSpinner.getValue();
        // make sure initial <= maximum
        Date initial = (Date)initialDateSpinner.getValue();
        if (maximum.getTime()-initial.getTime() < 0) {
            if (initialNowCheckBox.isSelected()) {
                if (!maximumNowCheckBox.isSelected()) {
                    initialNowCheckBox.setSelected(false);
                    initialDateSpinner.setEnabled(true);
                    initialDateSpinner.setValue(maximum);
                }
            } else {
                initialDateSpinner.setValue(maximum);
            }
        }
        if (minimumDateCheckBox.isSelected()) {
            // make sure minimum <= maximum
            Date minimum = (Date)minimumDateSpinner.getValue();
            if (maximum.getTime()-minimum.getTime() < 0) {
                if (minimumNowCheckBox.isSelected()) {
                    if (!maximumNowCheckBox.isSelected()) {
                        minimumNowCheckBox.setSelected(false);
                        minimumDateSpinner.setEnabled(true);
                        minimumDateSpinner.setValue(maximum);
                    }
                } else {
                    minimumDateSpinner.setValue(maximum);
                }
            }
        }
        fireChanges = true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox calendarFieldCombo;
    private javax.swing.JLabel calendarFieldLabel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JPanel datePanel;
    private javax.swing.JPanel defaultPanel;
    private javax.swing.JLabel initialDateLabel;
    private javax.swing.JSpinner initialDateSpinner;
    private javax.swing.JComboBox initialListCombo;
    private javax.swing.JLabel initialListLabel;
    private javax.swing.JCheckBox initialNowCheckBox;
    private javax.swing.JLabel initialNumberLabel;
    private javax.swing.JSpinner initialNumberSpinner;
    private javax.swing.JTextArea listItemsArea;
    private javax.swing.JLabel listItemsLabel;
    private javax.swing.JPanel listPanel;
    private javax.swing.JCheckBox maximumDateCheckBox;
    private javax.swing.JSpinner maximumDateSpinner;
    private javax.swing.JCheckBox maximumNowCheckBox;
    private javax.swing.JCheckBox maximumNumberCheckBox;
    private javax.swing.JSpinner maximumNumberSpinner;
    private javax.swing.JCheckBox minimumDateCheckBox;
    private javax.swing.JSpinner minimumDateSpinner;
    private javax.swing.JCheckBox minimumNowCheckBox;
    private javax.swing.JCheckBox minimumNumberCheckBox;
    private javax.swing.JSpinner minimumNumberSpinner;
    private javax.swing.JLabel modelPropertiesLabel;
    private javax.swing.JComboBox modelTypeCombo;
    private javax.swing.JLabel modelTypeLabel;
    private javax.swing.JPanel modelTypePanel;
    private javax.swing.JPanel numberPanel;
    private javax.swing.JComboBox numberTypeCombo;
    private javax.swing.JLabel numberTypeLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel stepSizeLabel;
    private javax.swing.JSpinner stepSizeSpinner;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns initialization string for the value represented by this property editor.
     * 
     * @return initialization string. 
     */
    @Override
    public String getJavaInitializationString() {
        Object value = getValue();
        if (!(value instanceof FormSpinnerModel)) {
            // should not happen
            return super.getJavaInitializationString();
        }
        FormSpinnerModel model = (FormSpinnerModel)value;
        SpinnerModel spinnerModel = model.getModel();
        String code = null;
        if (spinnerModel instanceof SpinnerDateModel) {
            code = dateInitializationString(model);
        } else if (spinnerModel instanceof SpinnerNumberModel) {
            code = numberInitializationString(model);
        } else if (spinnerModel instanceof SpinnerListModel) {
            code = listInitializationString(model);
        } else {
            assert false;
        }
        return code;
    }

    /**
     * Returns initialization string for list spinner model.
     * 
     * @return initializtaion string for list spinner model.
     */
    private static String listInitializationString(FormSpinnerModel model) {
        StringBuilder code = new StringBuilder("new javax.swing.SpinnerListModel(new String[] {"); // NOI18N
        SpinnerListModel listModel = (SpinnerListModel)model.getModel();
        List items = listModel.getList();
        for (Object item: items) {
            String text = item.toString();
            text = text.replace("\"", "\\\""); // NOI18N
            code.append('"').append(text).append("\", "); // NOI18N
        }
        code.delete(code.length()-2, code.length());
        code.append("})"); // NOI18N
        return code.toString();
    }

    /**
     * Returns initialization string for number spinner model.
     * 
     * @return initialization string for number spinner model.
     */
    private String numberInitializationString(FormSpinnerModel model) {
        StringBuilder code = new StringBuilder();
        SpinnerNumberModel numberModel = (SpinnerNumberModel)model.getModel();
        code.append("new javax.swing.SpinnerNumberModel("); // NOI18N
        Object initial = model.getInitialValue();
        Object minimum = numberModel.getMinimum();
        Object maximum = numberModel.getMaximum();
        Object stepSize = numberModel.getStepSize();
        Class<?> clazz = initial.getClass();
        if (clazz == Integer.class
                && Integer.valueOf(0).equals(initial)
                && (minimum == null) && (maximum == null)
                && (Integer.valueOf(1).equals(stepSize))) {
            // default constructor
            code.append(')');
        } else {
            // general constructor
            String prefix = ""; // NOI18N
            String suffix = ""; // NOI18N
            if (clazz == Long.class) {
                suffix = "L"; // NOI18N
                if (minimum != null && maximum != null) {
                    prefix = "Long.valueOf(";
                    suffix += ")";
                }
            } else if (clazz == Float.class) {
                suffix = "f"; // NOI18N
                if (minimum != null && maximum != null) {
                    prefix = "Float.valueOf(";
                    suffix += ")";
                }
            } else if (clazz == Double.class) {
                suffix = "d"; // NOI18N
            } else if (clazz == Byte.class) {
                prefix = "(byte)"; // NOI18N
                if (minimum != null && maximum != null) {
                    prefix = "Byte.valueOf(" + prefix;
                    suffix += ")";
                }
            } else if (clazz == Short.class) {
                prefix = "(short)"; // NOI18N
                if (minimum != null && maximum != null) {
                    prefix = "Short.valueOf(" + prefix;
                    suffix += ")";
                }
            }
            code.append(prefix).append(initial).append(suffix).append(", "); // NOI18N
            if (minimum == null) {
                code.append("null, "); // NOI18N
            } else{
                code.append(prefix).append(minimum).append(suffix).append(", "); // NOI18N
            }
            if (maximum == null) {
                code.append("null, "); // NOI18N
            } else{
                code.append(prefix).append(maximum).append(suffix).append(", "); // NOI18N
            }
            code.append(prefix).append(stepSize).append(suffix).append(")"); // NOI18N
        }
        return code.toString();
    }

    /**
     * Returns initialization string for date spinner model.
     * 
     * @return initializtaion string for date spinner model.
     */
    private String dateInitializationString(FormSpinnerModel model) {
        StringBuilder code = new StringBuilder();
        SpinnerDateModel dateModel = (SpinnerDateModel)model.getModel();
        code.append("new javax.swing.SpinnerDateModel("); // NOI18N
        if (model.isInitialNow() && (dateModel.getCalendarField() == Calendar.DAY_OF_MONTH)
                && (dateModel.getStart() == null) && (dateModel.getEnd() == null)) {
            // default constructor
            code.append(')');
        } else {
            // initial
            code.append("new java.util.Date("); // NOI18N
            if (!model.isInitialNow()) {
                code.append(((Date)model.getInitialValue()).getTime());
                code.append('L');
            }
            code.append("), "); // NOI18N
            // minimum
            Date minimum = (Date)dateModel.getStart();
            if (minimum == null) {
                code.append("null"); // NOI18N
            } else {
                code.append("new java.util.Date("); // NOI18N
                if (!model.isMinimumNow()) {
                    code.append(minimum.getTime());
                    code.append('L');
                }
                code.append(')');
            }
            code.append(", "); // NOI18N
            // maximum
            Date maximum = (Date)dateModel.getEnd();
            if (maximum == null) {
                code.append("null"); // NOI18N
            } else {
                code.append("new java.util.Date("); // NOI18N
                if (!model.isMaximumNow()) {
                    code.append(maximum.getTime());
                    code.append('L');
                }
                code.append(')');
            }
            code.append(", "); // NOI18N
            // calendar field
            int field = dateModel.getCalendarField();
            String fieldText = null;
            switch (field) {
                case Calendar.ERA: fieldText = "ERA"; break; // NOI18N
                case Calendar.YEAR: fieldText = "YEAR"; break; // NOI18N
                case Calendar.MONTH: fieldText = "MONTH"; break; // NOI18N
                case Calendar.WEEK_OF_YEAR: fieldText = "WEEK_OF_YEAR"; break; // NOI18N
                case Calendar.WEEK_OF_MONTH: fieldText = "WEEK_OF_MONTH"; break; // NOI18N
                case Calendar.DAY_OF_MONTH: fieldText = "DAY_OF_MONTH"; break; // NOI18N
                case Calendar.DAY_OF_YEAR: fieldText = "DAY_OF_YEAR"; break; // NOI18N
                case Calendar.DAY_OF_WEEK: fieldText = "DAY_OF_WEEK"; break; // NOI18N
                case Calendar.DAY_OF_WEEK_IN_MONTH: fieldText = "DAY_OF_WEEK_IN_MONTH"; break; // NOI18N
                case Calendar.AM_PM: fieldText = "AM_PM"; break; // NOI18N
                case Calendar.HOUR: fieldText = "HOUR"; break; // NOI18N
                case Calendar.HOUR_OF_DAY: fieldText = "HOUR_OF_DAY"; break; // NOI18N
                case Calendar.MINUTE: fieldText = "MINUTE"; break; // NOI18N
                case Calendar.SECOND: fieldText = "SECOND"; break; // NOI18N
                case Calendar.MILLISECOND: fieldText = "MILLISECOND"; break; // NOI18N
                default: assert false;
            }
            code.append("java.util.Calendar.").append(fieldText).append(')'); // NOI18N
        }
        return code.toString();
    }

    /** Name of the root tag of the spinner model XML property editor. */
    private static final String XML_SPINNER_MODEL = "SpinnerModel"; // NOI18N
    /** Name of the tag where items of spinner list model are stored. */
    private static final String XML_LIST_ITEM = "ListItem"; // NOI18N
    /** Name of the initial attribute. */
    private static final String ATTR_INITIAL = "initial"; // NOI18N
    /** Name of the minimum attribute. */
    private static final String ATTR_MINIMUM = "minimum"; // NOI18N
    /** Name of the maximum attribute. */
    private static final String ATTR_MAXIMUM = "maximum"; // NOI18N
    /** Name of the step size attribute. */
    private static final String ATTR_STEP_SIZE = "stepSize"; // NOI18N
    /** Name of the type attribute. */
    private static final String ATTR_TYPE = "type"; // NOI18N
    /** Name of the number type attribute. */
    private static final String ATTR_NUMBER_TYPE = "numberType"; // NOI18N
    /** Name of the value attribute (on list item tag). */
    private static final String ATTR_VALUE = "value"; // NOI18N
    /** Value denoting default type. */
    private static final String VALUE_TYPE_DEFAULT = "default"; // NOI18N
    /** Value denoting number type. */
    private static final String VALUE_TYPE_NUMBER = "number"; // NOI18N
    /** Value denoting date type. */
    private static final String VALUE_TYPE_DATE = "date"; // NOI18N
    /** Value denoting list type. */
    private static final String VALUE_TYPE_LIST = "list"; // NOI18N
    /** Value denoting now (date value). */
    private static final String VALUE_NOW = "now"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        String type = attributes.getNamedItem(ATTR_TYPE).getNodeValue();
        Object value = null;
        if (VALUE_TYPE_DEFAULT.equals(type)) {
            value = property.getDefaultValue();
        } else if (VALUE_TYPE_DATE.equals(type)) {
            value = readDateFromXML(element);
        } else if (VALUE_TYPE_NUMBER.equals(type)) {
            value = readNumberFromXML(element);
        } else if (VALUE_TYPE_LIST.equals(type)) {
            value = readListFromXML(element);
        }
        setValue(value);
    }

    @Override
    public Node storeToXML(Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_SPINNER_MODEL);
        Object value = getValue();
        if (!(value instanceof FormSpinnerModel)) {
            el.setAttribute(ATTR_TYPE, VALUE_TYPE_DEFAULT);
        } else {
            FormSpinnerModel model = (FormSpinnerModel)value;
            SpinnerModel spinnerModel = model.getModel();
            if (spinnerModel instanceof SpinnerNumberModel) {
                storeNumberToXML(el, model);
            } else if (spinnerModel instanceof SpinnerDateModel) {
                storeDateToXML(el, model);
            } else if (spinnerModel instanceof SpinnerListModel) {
                storeListToXML(doc, el, model);
            }
        }
        return el;
    }

    private static void storeNumberToXML(org.w3c.dom.Element el, FormSpinnerModel model) {
        el.setAttribute(ATTR_TYPE, VALUE_TYPE_NUMBER);
        SpinnerNumberModel numberModel = (SpinnerNumberModel)model.getModel();
        el.setAttribute(ATTR_NUMBER_TYPE, model.getInitialValue().getClass().getName());
        el.setAttribute(ATTR_INITIAL, model.getInitialValue().toString());
        Object minimum = numberModel.getMinimum();
        if (minimum != null) {
            el.setAttribute(ATTR_MINIMUM, minimum.toString());
        }
        Object maximum = numberModel.getMaximum();
        if (maximum != null) {
            el.setAttribute(ATTR_MAXIMUM, maximum.toString());
        }
        el.setAttribute(ATTR_STEP_SIZE, numberModel.getStepSize().toString());
    }

    private static Object readNumberFromXML(org.w3c.dom.Node el) {
        org.w3c.dom.NamedNodeMap attributes = el.getAttributes();
        String numberType = attributes.getNamedItem(ATTR_NUMBER_TYPE).getNodeValue();
        String initialTxt = attributes.getNamedItem(ATTR_INITIAL).getNodeValue();
        org.w3c.dom.Node node = attributes.getNamedItem(ATTR_MINIMUM);
        String minimumTxt = null;
        if (node != null) {
            minimumTxt = node.getNodeValue();
        }
        node = attributes.getNamedItem(ATTR_MAXIMUM);
        String maximumTxt = null;
        if (node != null) {
            maximumTxt = node.getNodeValue();
        }
        String stepSizeTxt = attributes.getNamedItem(ATTR_STEP_SIZE).getNodeValue();
        Number stepSize = null;
        Comparable minimum = null;
        Comparable maximum = null;
        Number initial = null;
        if (numberType.equals("java.lang.Integer")) { // NOI18N
            initial = Integer.parseInt(initialTxt);
            minimum = (minimumTxt == null) ? null : Integer.parseInt(minimumTxt);
            maximum = (maximumTxt == null) ? null : Integer.parseInt(maximumTxt);
            stepSize = Integer.parseInt(stepSizeTxt);
        } else if (numberType.equals("java.lang.Long")) { // NOI18N
            initial = Long.parseLong(initialTxt);
            minimum = (minimumTxt == null) ? null : Long.parseLong(minimumTxt);
            maximum = (maximumTxt == null) ? null : Long.parseLong(maximumTxt);
            stepSize = Long.parseLong(stepSizeTxt);
        } else if (numberType.equals("java.lang.Float")) { // NOI18N
            initial = Float.parseFloat(initialTxt);
            minimum = (minimumTxt == null) ? null : Float.parseFloat(minimumTxt);
            maximum = (maximumTxt == null) ? null : Float.parseFloat(maximumTxt);
            stepSize = Float.parseFloat(stepSizeTxt);
        } else if (numberType.equals("java.lang.Double")) { // NOI18N
            initial = Double.parseDouble(initialTxt);
            minimum = (minimumTxt == null) ? null : Double.parseDouble(minimumTxt);
            maximum = (maximumTxt == null) ? null : Double.parseDouble(maximumTxt);
            stepSize = Double.parseDouble(stepSizeTxt);
        } else if (numberType.equals("java.lang.Short")) { // NOI18N
            initial = Short.parseShort(initialTxt);
            minimum = (minimumTxt == null) ? null : Short.parseShort(minimumTxt);
            maximum = (maximumTxt == null) ? null : Short.parseShort(maximumTxt);
            stepSize = Short.parseShort(stepSizeTxt);
        } else if (numberType.equals("java.lang.Byte")) { // NOI18N
            initial = Byte.parseByte(initialTxt);
            minimum = (minimumTxt == null) ? null : Byte.parseByte(minimumTxt);
            maximum = (maximumTxt == null) ? null : Byte.parseByte(maximumTxt);
            stepSize = Byte.parseByte(stepSizeTxt);
        }
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(initial, minimum, maximum, stepSize);
        return new FormSpinnerModel(spinnerModel, initial);
    }

    private static void storeDateToXML(org.w3c.dom.Element el, FormSpinnerModel model) {
        el.setAttribute(ATTR_TYPE, VALUE_TYPE_DATE);
        SpinnerDateModel dateModel = (SpinnerDateModel)model.getModel();
        String initialText;
        if (model.isInitialNow()) {
            initialText = VALUE_NOW;
        } else {
            Date initial = (Date)model.getInitialValue();
            initialText = "" + initial.getTime(); // NOI18N
        }
        el.setAttribute(ATTR_INITIAL, initialText);
        Date minimum = (Date)dateModel.getStart();
        if (minimum != null) {
            String minimumText;
            if (model.isMinimumNow()) {
                minimumText = VALUE_NOW;
            } else {
                minimumText = "" + minimum.getTime(); // NOI18N
            }
            el.setAttribute(ATTR_MINIMUM, minimumText);
        }
        Date maximum = (Date)dateModel.getEnd();
        if (maximum != null) {
            String maximumText;
            if (model.isMaximumNow()) {
                maximumText = VALUE_NOW;
            } else{
                maximumText = "" + maximum.getTime(); // NOI18N
            }
            el.setAttribute(ATTR_MAXIMUM, maximumText);
        }
        el.setAttribute(ATTR_STEP_SIZE, "" + dateModel.getCalendarField()); // NOI18N
    }

    private static Object readDateFromXML(org.w3c.dom.Node el) {
        org.w3c.dom.NamedNodeMap attributes = el.getAttributes();
        String initialTxt = attributes.getNamedItem(ATTR_INITIAL).getNodeValue();
        Date now = new Date();
        boolean initialNow = false;
        Date initial;
        if (VALUE_NOW.equals(initialTxt)) {
            initialNow = true;
            initial = now;
        } else {
            initial = new Date(Long.parseLong(initialTxt));
        }
        Node node = attributes.getNamedItem(ATTR_MINIMUM);
        String minimumTxt = (node == null) ? null : node.getNodeValue();
        boolean minimumNow = false;
        Date minimum = null;
        if (minimumTxt != null) {
            if (VALUE_NOW.equals(minimumTxt)) {
                minimumNow = true;
                minimum = now;
            } else {
                minimum = new Date(Long.parseLong(minimumTxt));
            }
        }
        node = attributes.getNamedItem(ATTR_MAXIMUM);
        String maximumTxt = (node == null) ? null : node.getNodeValue();
        boolean maximumNow = false;
        Date maximum = null;
        if (maximumTxt != null) {
            if (VALUE_NOW.equals(maximumTxt)) {
                maximumNow = true;
                maximum = now;
            } else {
                maximum = new Date(Long.parseLong(maximumTxt));
            }
        }
        String stepSizeTxt = attributes.getNamedItem(ATTR_STEP_SIZE).getNodeValue();
        int calendarField = Integer.parseInt(stepSizeTxt);
        SpinnerDateModel spinnerModel = new SpinnerDateModel(initial, minimum, maximum, calendarField);
        return new FormSpinnerModel(spinnerModel, initial, initialNow, minimumNow, maximumNow);
    }

    private static void storeListToXML(Document doc, org.w3c.dom.Element el, FormSpinnerModel model) {
        el.setAttribute(ATTR_TYPE, VALUE_TYPE_LIST);
        SpinnerListModel listModel = (SpinnerListModel)model.getModel();
        List items = listModel.getList();
        for (Object item : items) {
            org.w3c.dom.Element elItem = doc.createElement(XML_LIST_ITEM);
            elItem.setAttribute(ATTR_VALUE, item.toString());
            el.appendChild(elItem);
        }
    }

    private static Object readListFromXML(org.w3c.dom.Node el) {
        org.w3c.dom.NodeList nodes = el.getChildNodes();
        List<String> list = new LinkedList<String>();
        for (int i=0; i<nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (XML_LIST_ITEM.equals(node.getNodeName())) {
                list.add(node.getAttributes().getNamedItem(ATTR_VALUE).getNodeValue());
            }
        }
        SpinnerListModel spinnerModel = new SpinnerListModel(list);
        return new FormSpinnerModel(spinnerModel, list.get(0));
    }

    /**
     * Retruns display name of this property editor. 
     * 
     * @return diaplay name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "SpinnerModelEditor"); // NOI18N
    }

    /**
     * Sets context of the property editor. 
     * 
     * @param formModel form model.
     * @param property property being edited.
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
     * Form wrapper for <code>SpinnerModel</code>. It allows us to keep
     * some meta-information about the <code>SpinnerModel</code>.
     */
    public static class FormSpinnerModel extends FormDesignValueAdapter {
        /** Spinner model. */
        private SpinnerModel model;
        /** Initial value of the spinner model. */
        private Object initialValue;
        /** Determines whether the initial value represents "now". */
        private boolean initialNow;
        /** Determines whether the minimum value represents "now". */
        private boolean minimumNow;
        /** Determines whether the maximum value represents "now". */
        private boolean maximumNow;

        /**
         * Creates new <code>FormSpinnerModel</code>.
         * 
         * @param model spinner list model.
         * @param initialValue initial value of the model.
         */
        public FormSpinnerModel(SpinnerListModel model, Object initialValue) {
            this.model = model;
            this.initialValue = initialValue;
        }

        /**
         * Creates new <code>FormSpinnerModel</code>.
         * 
         * @param model spinner number model.
         * @param initialValue initial value of the model.
         */
        public FormSpinnerModel(SpinnerNumberModel model, Object initialValue) {
            this.model = model;
            this.initialValue = initialValue;
        }

        /**
         * Creates new <code>FormSpinnerModel</code>.
         * 
         * @param model spinner date model.
         * @param initialValue initial value of the model.
         * @param initialNow determines whether the initial value represents "now". 
         * @param minimumNow determines whether the minimum value represents "now". 
         * @param maximumNow determines whether the maximum value represents "now". 
         */
        public FormSpinnerModel(SpinnerDateModel model, Object initialValue,
                boolean initialNow, boolean minimumNow, boolean maximumNow) {
            this.model = model;
            this.initialValue = initialValue;
            this.initialNow = initialNow;
            this.minimumNow = minimumNow;
            this.maximumNow = maximumNow;
        }

        /**
         * Creates new <code>FormSpinnerModel</code> as a copy
         * of some existing model.
         * 
         * @param original model to copy.
         */
        private FormSpinnerModel(FormSpinnerModel original) {
            this.model = original.model;
            this.initialValue = original.initialValue;
            this.initialNow = original.initialNow;
            this.minimumNow = original.minimumNow;
            this.maximumNow = original.maximumNow;
        }

        /**
         * Returns the wrapped spinner model.
         * 
         * @return the wrapped spinner model.
         */
        public SpinnerModel getModel() {
            return model;
        }

        /**
         * Returns initial value of the model. 
         * 
         * @return initial value of the model.
         */
        public Object getInitialValue() {
            return initialValue;
        }

        /**
         * Determines whether the initial value represents "now".
         * 
         * @return <code>true</code> if the initial value represents "now",
         * returns <code>false</code> otherwise.
         */
        public boolean isInitialNow() {
            return initialNow;
        }

        /**
         * Determines whether the minimum value represents "now".
         * 
         * @return <code>true</code> if the minimum value represents "now",
         * returns <code>false</code> otherwise.
         */
        public boolean isMinimumNow() {
            return minimumNow;
        }

        /**
         * Determines whether the maximum value represents "now".
         * 
         * @return <code>true</code> if the maximum value represents "now",
         * returns <code>false</code> otherwise.
         */
        public boolean isMaximumNow() {
            return maximumNow;
        }

        /**
         * Returns design value.
         * 
         * @return design value.
         */
        @Override
        public SpinnerModel getDesignValue() {
            return model;
        }

        @Override
        public Object copy(FormProperty targetFormProperty) {
            return new FormSpinnerModel(this);
        }

    }
    
}
