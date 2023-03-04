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
package org.netbeans.modules.tasklist.filter;

import java.awt.Component;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;

import org.openide.util.NbBundle;
import javax.swing.text.JTextComponent;

/**
 * GUI component for a single condition within the Filter Panel
 *
 * @author Tor Norbye
 * @author tl
 */
final class ConditionPanel extends JPanel implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1;

    private KeywordsFilter filter;
    private JComponent valueField;
   
    /** 
     * Creates new form ConditionPanel 
     *
     * @param filter filter to be used
     * @param cond condition to be shown or null
     */
    public ConditionPanel(KeywordsFilter filter, AppliedFilterCondition cond) {
        this.filter = filter;

        initComponents();
        initA11y();
        
        valueField = emptyPanel;
        
        // fill ComboBox with properties
        TaskProperty [] props = filter.getProperties();
	int selectedIndex = -1;

        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (int i = 0; i < props.length; i++) {
            AppliedFilterCondition[] c = filter.createConditions(props[i]);
            if (c.length != 0) {
                m.addElement(props[i]);
                if (cond == null)
                    cond = c[0];
            }
	    if (props[i].getName().equals(cond.getProperty().getName())) selectedIndex = i;
        }
        propertyCombo.setModel(m);
        propertyCombo.addActionListener(this);
        propertyCombo.setSelectedIndex(selectedIndex);
	propertyCombo.setRenderer(new ConditionPanel.PropertyCellRenderer());


	// construct the rest of items for the current condition 
        AppliedFilterCondition[] conditions = filter.createConditions(cond.getProperty());
        m = new DefaultComboBoxModel();
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i].sameType(cond)) {
                m.addElement(cond);
            } else {
                m.addElement(conditions[i]);
            }
        }
        relationCombo.setModel(m);
        relationCombo.addActionListener(this);
        relationCombo.setSelectedItem(cond);
        relationCombo.setRenderer(new ConditionPanel.ConditionCellRenderer());
        
        setValueComponent(cond.getCondition().createConstantComponent());
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == propertyCombo) {
            TaskProperty prop = (TaskProperty) propertyCombo.getSelectedItem();
            if( null == prop )
                return;
            AppliedFilterCondition[] c = filter.createConditions(prop);

            relationCombo.setModel(new DefaultComboBoxModel(c));
            if (c.length != 0) {
                relationCombo.setSelectedIndex(0);
                AppliedFilterCondition cond = (AppliedFilterCondition)relationCombo.getSelectedItem();
                setValueComponent(cond.getCondition().createConstantComponent());
            } else {
                setValueComponent(null);
            }
        } else if (evt.getSource() == relationCombo) {
            AppliedFilterCondition cond = 
                (AppliedFilterCondition) relationCombo.getSelectedItem();
            if( null != cond )
                setValueComponent(cond.getCondition().createConstantComponent());
        }
    }
    
    /**
     * Return a filter condition corresponding to what is in the GUI
     *
     * @return choosed filter condition
     */
    public AppliedFilterCondition getCondition() {
        AppliedFilterCondition cond = (AppliedFilterCondition)relationCombo.getSelectedItem();
        if (valueField != emptyPanel)
            cond.getCondition().getConstantFrom(valueField);
        return cond;
    }    

    private void initA11y() {
        propertyCombo.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ConditionPanel.class, 
                                    "ACSN_Property")); // NOI18N
        propertyCombo.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ConditionPanel.class, 
                                    "ACSD_Property")); // NOI18N
        relationCombo.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ConditionPanel.class, 
                                    "ACSN_Relation")); // NOI18N
        relationCombo.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ConditionPanel.class, 
                                    "ACSD_Relation")); // NOI18N
    }

    /**
     * Changes component with a constant.
     *
     * @param cmp new component or null if it should be removed
     */
    private void setValueComponent(JComponent cmp) {
        if (valueField == emptyPanel && cmp == null || valueField == cmp)
            return;
        
        remove(valueField);
        
        if (cmp == null)
            valueField = emptyPanel;
        else {
            if ((cmp instanceof JTextComponent) && (valueField instanceof JTextComponent) 
                && ((JTextComponent)cmp).getText().length()==0)             
                    ((JTextComponent)cmp).setText(((JTextComponent)valueField).getText());
            valueField = cmp;
	    
            // supply with default values if no specific one defined
            if (cmp.getAccessibleContext().getAccessibleName() == null) {
                cmp.getAccessibleContext().setAccessibleName(
                        NbBundle.getMessage(ConditionPanel.class,
                                            "ACSN_Value")); // NOI18N
            }

            if (cmp.getAccessibleContext().getAccessibleDescription() == null) {
                cmp.getAccessibleContext().setAccessibleDescription(
                        NbBundle.getMessage(ConditionPanel.class,
                                            "ACSD_Value")); // NOI18N
            }

            cmp.addPropertyChangeListener(FilterCondition.PROP_VALUE_VALID, this);
        }
        
        GridBagConstraints gridBagConstraints = 
            new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        add(valueField, gridBagConstraints); 
        revalidate();
        repaint();
    }
    
    public void focusPropertyCombo() {
        propertyCombo.requestFocusInWindow();
    }
    
    /**
     * Cell renderer for FilterCondition
     */
    private static class ConditionCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1;
        
        public ConditionCellRenderer () {}

        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            AppliedFilterCondition c = (AppliedFilterCondition) value;
            if (c != null) setText(c.getCondition().getDisplayName());
            return this;
        }        
    }


    /**
     * Cell renderer for Properties
     */
    private static class PropertyCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1;

        public PropertyCellRenderer () {}
        
        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            TaskProperty prop = (TaskProperty)value;
            if (prop != null) setText(prop.getName());
            return this;
        }        
    }


    // forward PROP_VALUE_VALID event
    public void propertyChange(PropertyChangeEvent evt) {
        putClientProperty(FilterCondition.PROP_VALUE_VALID, Boolean.valueOf(isValueValid()));
    }

    public boolean isValueValid() {
        AppliedFilterCondition cond = (AppliedFilterCondition) relationCombo.getSelectedItem();
        return valueField != emptyPanel && cond.getCondition().isValueValid(valueField);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(propertyCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(relationCombo, gridBagConstraints);

        emptyPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(emptyPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JPanel emptyPanel = new javax.swing.JPanel();
    final javax.swing.JComboBox propertyCombo = new javax.swing.JComboBox();
    final javax.swing.JComboBox relationCombo = new javax.swing.JComboBox();
    // End of variables declaration//GEN-END:variables
}
