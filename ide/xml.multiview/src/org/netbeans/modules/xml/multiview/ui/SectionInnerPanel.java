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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.Refreshable;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.cookies.ErrorLocator;
import org.netbeans.modules.xml.multiview.cookies.LinkCookie;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class represents a panel that is contained in <code>SectionPanel</code>, 
 * i.e. an inner panel as the name implies. This class provides support for registering
 * UI components that modify the model by means of various <code>addModifier</code> and 
 * <code>addImmediateModifier</code> methods. See javadocs of those for 
 * more details. 
 *
 * @author mkuchtiak
 */
public abstract class SectionInnerPanel extends javax.swing.JPanel implements LinkCookie, ErrorLocator {
    private static final RequestProcessor RP = new RequestProcessor(SectionInnerPanel.class);

    private SectionView sectionView;
    private java.util.List refreshableList = new LinkedList();
    
    private boolean localFocusListenerInitialized = false;
    private FocusListener localFocusListener = new FocusListener() {
        public void focusGained(FocusEvent evt) {
            final FocusListener[] focusListeners = getFocusListeners();
            for (int i = 0; i < focusListeners.length; i++) {
                focusListeners[i].focusGained(evt);
            }
        }
        
        public void focusLost(FocusEvent evt) {
            processFocusEvent(evt);
        }
    };
    
    private RequestProcessor.Task refreshTask = RP.create(new Runnable() {
        public void run() {
            refreshView();
        }
    });
    
    private static final int REFRESH_DELAY = 50;
    private FlushFocusListener activeListener = null;
    private boolean closing = false;
    
    /** Constructor that takes the enclosing SectionView object as its argument
     * @param sectionView enclosing SectionView object
     */
    public SectionInnerPanel(SectionView sectionView) {
        this.sectionView = sectionView;
    }
    
    public synchronized void addFocusListener(FocusListener l) {
        super.addFocusListener(l);
        if (!localFocusListenerInitialized) {
            localFocusListenerInitialized = true;
            Container container = this;
            FocusListener focusListener = localFocusListener;
            addFocusListenerRecursively(container, focusListener);
        }
    }
    
    private void addFocusListenerRecursively(Container container, FocusListener focusListener) {
        final Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.isFocusable() && !(component instanceof JLabel)) {
                component.addFocusListener(focusListener);
            }
            if (component instanceof Container) {
                if (!(component instanceof SectionNodePanel)) {
                    addFocusListenerRecursively((Container) component, focusListener);
                }
            }
        }
    }
    
    /** Getter for section view
     * @return sectionView enclosing SectionView object
     */
    public SectionView getSectionView() {
        return sectionView;
    }
    
    /** 
     * Callback method called on focus lost event after the value was checked for correctness
     * (in case <code>source</code> was registered using one of the <code>addModifier</code> methods 
     * of this class, if <code>addImmediateModifier</code> was used instead, this method
     * gets called immediately when the state of <code>source</code> changes).
     * @param source the last focused JComponent or whose state was changed.
     * @param value the value that has been set (typed) in the component
     */
    public abstract void setValue(JComponent source, Object value);
    
    /** Callback method called on document change event. This is called for components that
     * require just-in-time validation.
     * @param source JTextComponent being actually edited
     * @param value the actual value of the component
     */
    public void documentChanged(javax.swing.text.JTextComponent source, String value) {
    }
    
    /** Callback method called on focus lost event after the value was checked for correctness.
     * and the result is that the value is wrong. The value should be rollbacked from the model.
     * @param source last focused JComponent
     */
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    /** Adds text component to the set of JTextComponents that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * @param tc JTextComponent whose content is related to data model
     */
    public final void addModifier(final JTextComponent tc) {
        tc.addFocusListener(new ModifyFocusListener(tc));
    }
    
    /** Adds text component to the set of JTextComponents that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     *
     * @param tc JTextComponent whose content is related to data model
     * @param test indicates whether the original value should be tested against the current value when focus is lost
     */
    public final void addModifier(final JTextComponent tc, boolean test) {
        tc.addFocusListener(new ModifyFocusListener(tc, test));
    }
    
    /**
     * Adds combo box component to the set of JComboBoxes that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * @param comboBox JComboBox whose content is related to data model
     */
    public final void addModifier(final JComboBox comboBox) {
        comboBox.addFocusListener(new ComboBoxModifyFocusListener(comboBox));
    }
    
    /**
     * Adds combo box component to the set of JComboBoxes that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     *
     * @param comboBox JComboBox whose content is related to data model
     * @param test indicates whether the original value should be tested against the current value when focus is lost
     */
    public final void addModifier(final JComboBox comboBox, boolean test) {
        comboBox.addFocusListener(new ComboBoxModifyFocusListener(comboBox, test));
    }
    
    
    /**
     * Adds radio button component to the set of JRadioButtons that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * @param radioButton JRadioButton whose content is related to data model
     */
    public final void addModifier(final JRadioButton radioButton){
        radioButton.addFocusListener(new RadioButtonModifyFocusListener(radioButton));
    }
    
    /**
     * Adds check box component to the set of JCheckBox that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * @param checkBox JCheckBox whose content is related to data model
     */
    public final void addModifier(final JCheckBox checkBox){
        checkBox.addFocusListener(new CheckBoxModifyFocusListener(checkBox));
    }
    
    /**
     * Adds check box component to the set of JCheckBoxes that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * Unlike <code>addModifier</code>, this method will cause <code>setValue()</code>
     * to be called on each action event related to the given <code>checkBox</code>. Note
     * that the same component should not be added both as a regular modifier and
     * as an immediate modifier.
     * @param checkBox JCheckBox whose content is related to data model
     */
    public final void addImmediateModifier(final JCheckBox checkBox){
        checkBox.addActionListener(new CheckBoxActionListener(checkBox));
    }
    
    /**
     * Adds the given <code>radioButton</code> to the set of JRadioButtons that modify the model.
     * After the value in this component is changed the setValue() method is called.
     * Unlike <code>addModifier</code>, this method will cause <code>setValue()</code>
     * to be called on each action event related to the given <code>checkBox</code>. Note
     * that the same component should not be added both as a regular modifier and
     * as an immediate modifier.
     * @param radioButton the radio button whose content is related to the data model
     */
    public final void addImmediateModifier(final JRadioButton radioButton){
        radioButton.addActionListener(new RadioButtonActionListener(radioButton));
    }

    /**
     * Adds combo box component to the set of JComboBoxes that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * Unlike <code>addModifier</code>, this method will cause <code>setValue()</code>
     * to be called on each action event related to the given <code>comboBox</code>.Note
     * that the same component should not be added both as a regular modifier and
     * as an immediate modifier.
     * @param comboBox JComboBox whose content is related to data model
     */
    public final void addImmediateModifier(final JComboBox comboBox) {
        comboBox.addActionListener(new ComboBoxActionListener(comboBox));
    }
    
    /** Adds text component to the set of JTextComponents that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * Unlike <code>addModifier</code>, this method will cause <code>setValue()</code>
     * to be called on every change related to the given <code>tc</code>.Note
     * that the same component should not be added both as a regular modifier and
     * as an immediate modifier.
     * @param tc JTextComponent whose content is related to data model
     */
    public final void addImmediateModifier(final JTextComponent tc) {
        tc.getDocument().addDocumentListener(new TextListener(tc, true));
    }
    
    /** Adds text component to the set of JTextComponents that should be validated for correctness.
     * After the value in this component is changed either setValue() method is called(value is correct)
     * or rollbackValue() method is called(value is incorrect). Also the documentChanged() method is called during editing.
     * @param tc JTextComponent whose content is related to data model and should be validated before saving to data model.
     */
    public final void addValidatee(final JTextComponent tc) {
        tc.getDocument().addDocumentListener(new TextListener(tc));
        tc.addFocusListener(new ValidateFocusListener(tc));
    }
    
    protected void scheduleRefreshView() {
        refreshTask.schedule(REFRESH_DELAY);
    }
    
    /**
     * Reloads data from data model
     */
    public void refreshView() {
        for (Iterator it = refreshableList.iterator(); it.hasNext();) {
            ((Refreshable) it.next()).refresh();
        }
    }
    
    protected void addRefreshable(Refreshable refreshable) {
        refreshableList.add(refreshable);
    }
    
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        scheduleRefreshView();
    }
    
    private class TextListener implements javax.swing.event.DocumentListener {
        
        private JTextComponent tc;
        private boolean setValue = false;
        
        TextListener(JTextComponent tc) {
            this(tc, false);
        }
        
        TextListener(JTextComponent tc, boolean setValue) {
            this.tc = tc;
            this.setValue = setValue;
        }
        
        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }
        
        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }
        
        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }
        
        private void update(javax.swing.event.DocumentEvent evt) {
            if (setValue){
                startUIChange();
                setValue(tc, tc.getText());
                signalUIChange();
                endUIChange();
            } else {
                documentChanged(tc, tc.getText());
            }
        }
    }
    
    
    private abstract class FlushFocusListener extends java.awt.event.FocusAdapter {
        public abstract boolean flushData();
    }
    
    private class ValidateFocusListener extends FlushFocusListener {
        private String orgValue;
        private boolean viewIsBuggy;
        private final JTextComponent tc;
        /**
         * Prevents 'fix now' dialog from popping up multiple times.
         */
        private boolean disable;
        
        public ValidateFocusListener(JTextComponent tc) {
            this.tc = tc;
        }
        
        public void focusGained(FocusEvent evt) {
            activeListener = this;
            orgValue = tc.getText();
            if (sectionView.getErrorPanel().getError() != null) {
                viewIsBuggy = true;
            } else {
                viewIsBuggy = false;
            }
        }
        
        public void focusLost(FocusEvent evt) {
            if (!closing) {
                if (!flushData()) {
                    Utils.runInAwtDispatchThread(new Runnable() {
                        public void run() {
                            //todo: make sure the panel is visible
                            tc.requestFocus();
                        }
                    });
                } else {
                    disable = false;
                    activeListener = null;
                }
            }
        }
        
        public boolean flushData() {
            Error error = sectionView.getErrorPanel().getError();
            if (error != null && error.isEditError() && tc == error.getFocusableComponent()) {
                if (Error.TYPE_WARNING == error.getSeverityLevel() && !disable) {
                    org.openide.DialogDescriptor desc = new RefreshSaveDialog(sectionView.getErrorPanel());
                    Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                    dialog.setVisible(true);
                    Integer opt = (Integer) desc.getValue();
                    if (opt.equals(RefreshSaveDialog.OPTION_FIX)) {
                        disable = true;
                        return false;
                    } else if (opt.equals(RefreshSaveDialog.OPTION_REFRESH)) {
                        rollbackValue(tc);
                        sectionView.checkValidity();
                    } else {
                        startUIChange();
                        setValue(tc, tc.getText());
                        signalUIChange();
                        endUIChange();
                        sectionView.checkValidity();
                    }
                } else if (!disable){
                    org.openide.DialogDescriptor desc = new RefreshDialog(sectionView.getErrorPanel());
                    Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                    dialog.setVisible(true);
                    Integer opt = (Integer) desc.getValue();
                    if (opt.equals(RefreshDialog.OPTION_FIX)) {
                        disable = true;
                        return false;
                    } else if (opt.equals(RefreshDialog.OPTION_REFRESH)) {
                        rollbackValue(tc);
                        sectionView.checkValidity();
                    }
                }
            } else {
                if (!tc.getText().equals(orgValue)) {
                    startUIChange();
                    setValue(tc, tc.getText());
                    signalUIChange();
                    endUIChange();
                    sectionView.checkValidity();
                } else {
                    if (viewIsBuggy) {
                        sectionView.checkValidity();
                    }
                }
            }
            disable = false;
            return true;
        }
    }
    
    private class ModifyFocusListener extends FlushFocusListener {
        private String orgValue;
        private final JTextComponent tc;
        // indicates whether the original value (orgValue) should be tested against the current value when focus is lost
        private boolean test;
        
        public ModifyFocusListener(JTextComponent tc) {
            this(tc, true);
        }
        
        public ModifyFocusListener(JTextComponent tc, boolean test) {
            this.tc = tc;
            this.test = test;
        }
        
        public void focusGained(FocusEvent evt) {
            orgValue = tc.getText();
            activeListener = this;
        }
        
        public void focusLost(FocusEvent evt) {
            if (!closing) {
                flushData();
                activeListener = null;
            }
        }
        
        public boolean flushData() {
            if (!test || !tc.getText().equals(orgValue)) {
                startUIChange();
                setValue(tc, tc.getText());
                signalUIChange();
                endUIChange();
            }
            return true;
        }
    }
    
    /**
     * Listener attached to combo boxes that modify the model.
     */
    private class ComboBoxModifyFocusListener extends FlushFocusListener {
        private Object orgValue;
        private final JComboBox comboBox;
        // indicates whether the original value (orgValue) should be tested against the current value when focus is lost
        private boolean test;
        
        public ComboBoxModifyFocusListener(JComboBox comboBox) {
            this(comboBox, true);
        }
        
        public ComboBoxModifyFocusListener(JComboBox comboBox, boolean test) {
            this.comboBox = comboBox;
            this.test = test;
        }
        
        public void focusGained(FocusEvent evt) {
            orgValue = comboBox.getSelectedItem();
            activeListener = this;
        }
        
        public void focusLost(FocusEvent evt) {
            if (!closing) {
                flushData();
                activeListener = null;
            }
        }
        
        public boolean flushData() {
            Object newValue = comboBox.getSelectedItem();
            boolean newEqualsOld = (newValue==null ? orgValue==null : newValue.equals(orgValue));
            if (!test || !newEqualsOld) {
                startUIChange();
                setValue(comboBox, comboBox.getSelectedItem());
                signalUIChange();
                endUIChange();
            }
            return true;
        }
    }
    
    /**
     * Listener attached to radio buttons that modify the model.
     */
    private class RadioButtonModifyFocusListener extends FlushFocusListener {
        private boolean orgValue;
        private final JRadioButton radioButton;
        
        public RadioButtonModifyFocusListener(JRadioButton radioButton) {
            this.radioButton = radioButton;
        }
        
        public void focusGained(FocusEvent evt) {
            orgValue = radioButton.isSelected();
            activeListener = this;
        }
        
        public void focusLost(FocusEvent evt) {
            if (!closing) {
                flushData();
                activeListener = null;
            }
        }
        
        public boolean flushData() {
            if (!(radioButton.isSelected() == orgValue)) {
                startUIChange();
                setValue(radioButton, Boolean.valueOf(radioButton.isSelected()));
                signalUIChange();
                endUIChange();
            }
            return true;
        }
    }
    
    public boolean canClose() {
        closing = true;
        try {
            if (activeListener != null) {
                return activeListener.flushData();
            }
            return true;
        } finally {
            closing = false;
        }
    }
    
    /**
     * Base class for action listeners for components that require setValue to be called
     * immediately on action event (instead of when focus is gained/lost).
     */
    private abstract class FlushActionListener implements ActionListener {
        
        public final void actionPerformed(ActionEvent e) {
            startUIChange();
            doSetValue(e);
            signalUIChange();
            endUIChange();
        }
        
        /**
         * Does the actual setting of the value to the model. Subclasses
         * should usually invoke <code>setValue</code> method here.
         */
        public abstract void doSetValue(ActionEvent e);
    }
    
    /**
     * Action listener for combo boxes that require setValue to be called
     * immediately on action event (instead of when focus is gained/lost).
     */
    private class ComboBoxActionListener extends FlushActionListener{
        
        private final JComboBox comboBox;
        
        public ComboBoxActionListener(JComboBox comboBox){
            this.comboBox = comboBox;
        }
        
        public void doSetValue(ActionEvent e) {
            setValue(comboBox, comboBox.getSelectedItem());
        }
    }
    
    /**
     * Action listener for checkboxes that require setValue to be called
     * immediately on action event (instead of when focus is gained/lost).
     */
    private class CheckBoxActionListener extends FlushActionListener{
        
        private final JCheckBox checkBox;
        
        public CheckBoxActionListener(JCheckBox checkBox){
            this.checkBox = checkBox;
        }
        
        public void doSetValue(ActionEvent e) {
            setValue(checkBox, Boolean.valueOf(checkBox.isSelected()));
        }
    }
    
    /**
     * Action listener for radio buttons that require setValue to be called
     * immediately on action event (instead of when focus is gained/lost).
     */
    private class RadioButtonActionListener extends FlushActionListener{
        
        private final JRadioButton radioButton;
        
        public RadioButtonActionListener(JRadioButton radioButton){
            this.radioButton = radioButton;
        }
        
        public void doSetValue(ActionEvent e) {
            setValue(radioButton, Boolean.valueOf(radioButton.isSelected()));
        }
    }
    
    /**
     * Listener attached to check boxes that modify the model.
     */
    private class CheckBoxModifyFocusListener extends FlushFocusListener {
        private boolean orgValue;
        private final JCheckBox checkBox;
        
        public CheckBoxModifyFocusListener(JCheckBox checkBox) {
            this.checkBox = checkBox;
        }
        
        public void focusGained(FocusEvent evt) {
            orgValue = checkBox.isSelected();
            activeListener = this;
        }
        
        public void focusLost(FocusEvent evt) {
            if (!closing) {
                flushData();
                activeListener = null;
            }
        }
        
        public boolean flushData() {
            if (!(checkBox.isSelected() == orgValue)) {
                startUIChange();
                setValue(checkBox, Boolean.valueOf(checkBox.isSelected()));
                signalUIChange();
                endUIChange();
            }
            return true;
        }
        
        public boolean canClose() {
            closing = true;
            try {
                if (activeListener != null) {
                    return activeListener.flushData();
                }
                return true;
            } finally {
                closing = false;
            }
        }
        
    }
    
    /** This will be called after model is changed from this panel
     * @deprecated use {@link SectionInnerPanel#endUIChange} instead
     */
    @Deprecated
    protected void signalUIChange() {
    }
    
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
    }
    
}
