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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.Properties;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.jpda.ui.WatchPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel for breakpoint conditions
 * 
 * @author  Martin Entlicher
 */
public class ConditionsPanel extends javax.swing.JPanel {

    private static final int MAX_SAVED_CONDITIONS = 10;
    
    /** Creates new form ConditionsPanel */
    public ConditionsPanel(String helpId) {
        initComponents();
        HelpCtx.setHelpIDString(tfCondition, helpId);
        tfConditionFieldForUI = new javax.swing.JTextField();
        tfConditionFieldForUI.setEnabled(false);
        tfConditionFieldForUI.setToolTipText(tfCondition.getToolTipText());

        // remove border from condition editor on some LAFs
        String lafID = UIManager.getLookAndFeel().getID();
        if (lafID.equals("Windows") || lafID.startsWith("FlatLaf")) { // NOI18N
            tfConditionFieldForUI.setBorder(BorderFactory.createEmptyBorder());
            spCondition.setBorder(BorderFactory.createEmptyBorder());
        }

        classFilterCheckBoxActionPerformed(null);
        conditionCheckBoxActionPerformed(null);
        cbWhenHitCountActionPerformed(null);
        int preferredHeight = tfConditionFieldForUI.getPreferredSize().height;
        Dimension spDim = spCondition.getPreferredSize();
        if (spDim.height > preferredHeight) {
            preferredHeight = spDim.height;
            tfConditionFieldForUI.setPreferredSize(new java.awt.Dimension(tfConditionFieldForUI.getPreferredSize().width, preferredHeight));
        }
        Border b = spCondition.getBorder();
        if (b instanceof WatchPanel.DelegatingBorder) {
            Insets insets = ((WatchPanel.DelegatingBorder) b).getInsets();
            insets.right = 1;
            ((WatchPanel.DelegatingBorder) b).setInsets(insets);
        }
        spCondition.setPreferredSize(spDim);
        //spCondition.setMinimumSize(spDim);
        tfHitCountFilter.setPreferredSize(
                new Dimension(8*tfHitCountFilter.getFontMetrics(tfHitCountFilter.getFont()).charWidth('8'),
                              tfHitCountFilter.getPreferredSize().height));
        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.equals"), // NOI18N
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.greater"), // NOI18N
            NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.multiple") // NOI18N
        }));
        conditionComboBox.setEditor(new ConditionComboBoxEditor());
        Object[] conditions = getSavedConditions();
        conditionComboBox.setModel(new DefaultComboBoxModel(conditions));
    }

    private static Object[] getSavedConditions() {
        return Properties.getDefault().getProperties("debugger.jpda").
                getArray("BPConditions", new Object[0]);
    }
    
    // Data Show:
    
    public void showCondition(boolean show) {
        conditionCheckBox.setVisible(show);
        if (show) {
            conditionCheckBoxActionPerformed(null);
        } else {
            conditionComboBox.setVisible(show);
        }
    }
    
    public void showClassFilter(boolean show) {
        classFilterCheckBox.setVisible(show);
        classIncludeFilterLabel.setVisible(show);
        classIncludeFilterTextField.setVisible(show);
        classExcludeFilterLabel.setVisible(show);
        classExcludeFilterTextField.setVisible(show);
        classExcludeFilterCheckBox.setVisible(false);
    }
    
    public void showExclusionClassFilter(boolean show) {
        showClassFilter(false);
        if (show) {
            classExcludeFilterCheckBox.setVisible(show);
            classExcludeFilterTextField.setVisible(show);
        }
        classExcludeFilterCheckBoxActionPerformed(null);
    }
    
    // Data Set:
    
    public void setClassMatchFilter(String[] filter) {
        String filterStr = getFilterStr(filter);
        classIncludeFilterTextField.setText(filterStr);
        classFilterCheckBox.setSelected(filterStr.length() > 0 || classExcludeFilterTextField.getText().length() > 0);
        classFilterCheckBoxActionPerformed(null);
    }
    
    public void setClassExcludeFilter(String[] filter) {
        String filterStr = getFilterStr(filter);
        classExcludeFilterTextField.setText(filterStr);
        if (classFilterCheckBox.isVisible()) {
            classFilterCheckBox.setSelected(filterStr.length() > 0 || classIncludeFilterTextField.getText().length() > 0);
            classFilterCheckBoxActionPerformed(null);
        }
        if (classExcludeFilterCheckBox.isVisible()) {
            classExcludeFilterCheckBox.setSelected(filterStr.length() > 0);
            classExcludeFilterCheckBoxActionPerformed(null);
        }
    }
    
    public void setCondition(String condition) {
        conditionCheckBox.setSelected(condition.length() > 0);
        conditionCheckBoxActionPerformed(null);
        tfCondition.setText(condition);
    }
    
    public void setHitCountFilteringStyle(HIT_COUNT_FILTERING_STYLE style) {
        cbHitStyle.setSelectedIndex((style != null) ? style.ordinal() : 0);
    }
    
    public void setHitCount(int hitCount) {
        if (hitCount != 0) {
            cbWhenHitCount.setSelected(true);
            tfHitCountFilter.setText(Integer.toString(hitCount));
        } else {
            cbWhenHitCount.setSelected(false);
            tfHitCountFilter.setText("");
        }
        cbWhenHitCountActionPerformed(null);
    }
    
    public void setupConditionPaneContext(String url, int line) {
        WatchPanel.setupContext(tfCondition, url, line, 0);
    }

    public void setupConditionPaneContext() {
        WatchPanel.setupContext(tfCondition, null);
    }

    private String getFilterStr(String[] filter) {
        if (filter == null || filter.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(filter[i]);
        }
        return sb.toString();
    }
    
    static String[] getFilter(String filterStr) {
        if (filterStr == null || filterStr.length() == 0) {
            return new String[] {};
        }
        StringTokenizer tokenizer = new StringTokenizer(filterStr, ", \t"); // NOI18N
        ArrayList<String> strsList = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            String str = tokenizer.nextToken();
            if (str.length() > 0) {
                strsList.add(str);
            }
        }
        String[] result = new String[strsList.size()];
        strsList.toArray(result);
        return result;
    }

    
    // Data Retrieval:
    
    public String[] getClassMatchFilter() {
        String filterStr;
        if (classFilterCheckBox.isSelected()) {
            filterStr = classIncludeFilterTextField.getText().trim();
        } else {
            filterStr = "";
        }
        return getFilter(filterStr);
    }
    
    public String[] getClassExcludeFilter() {
        String filterStr;
        if (classFilterCheckBox.isVisible() && classFilterCheckBox.isSelected() ||
            classExcludeFilterCheckBox.isVisible() && classExcludeFilterCheckBox.isSelected()) {
            filterStr = classExcludeFilterTextField.getText().trim();
        } else {
            filterStr = "";
        }
        return getFilter(filterStr);
    }
    
    public String getCondition() {
        if (conditionCheckBox.isSelected()) {
            String condition = tfCondition.getText().trim();
            if (condition.length() > 0) {
                condition = adjustCondition(condition);
                Object[] savedConditions = getSavedConditions();
                Object[] conditions = null;
                boolean containsCondition = false;
                for (int i = 0; i < savedConditions.length; i++) {
                    Object c = savedConditions[i];
                    if (condition.equals(c)) {
                        containsCondition = true;
                        conditions = savedConditions;
                        if (i > 0) {
                            System.arraycopy(conditions, 0, conditions, 1, i);
                            conditions[0] = condition;
                        }
                        break;
                    }
                }
                if (!containsCondition) {
                    if (savedConditions.length < MAX_SAVED_CONDITIONS) {
                        conditions = new Object[savedConditions.length + 1];
                        conditions[0] = condition;
                        System.arraycopy(savedConditions, 0, conditions, 1, savedConditions.length);
                    } else {
                        conditions = savedConditions;
                        System.arraycopy(conditions, 0, conditions, 1, conditions.length - 1);
                        conditions[0] = condition;
                    }
                }
                Properties.getDefault().getProperties("debugger.jpda").
                        setArray("BPConditions", conditions);
            }
            return condition;
        } else {
            return "";
        }
    }
    
    private static String adjustCondition(String condition) {
        while (condition.endsWith(";")) {
            condition = condition.substring(0, condition.length() - 1).trim();
        }
        return condition;
    }

    public HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
        if (!cbWhenHitCount.isSelected()) {
            return null;
        } else {
            return HIT_COUNT_FILTERING_STYLE.values()[cbHitStyle.getSelectedIndex()];
        }
    }
    
    public int getHitCount() {
        if (!cbWhenHitCount.isSelected()) {
            return 0;
        }
        String hcfStr = tfHitCountFilter.getText().trim();
        try {
            int hitCount = Integer.parseInt(hcfStr);
            return hitCount;
        } catch (NumberFormatException nfex) {
            return 0;
        }
    }
    
    public String valiadateMsg () {
        String hcfStr = tfHitCountFilter.getText().trim();
        if (cbWhenHitCount.isSelected()) {
            if (hcfStr.length() > 0) {
                int hitCountFilter;
                try {
                    hitCountFilter = Integer.parseInt(hcfStr);
                } catch (NumberFormatException e) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_Bad_Hit_Count_Filter_Spec", hcfStr);
                }
                if (hitCountFilter <= 0) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_NonPositive_Hit_Count_Filter_Spec");
                }
            } else {
                return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Hit_Count_Filter_Spec");
            }
        }
        if (conditionCheckBox.isSelected() && tfCondition.getText().trim().length() == 0) {
            return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Condition_Spec");
        }
        return null;
    }
    
    static String createClassPatternTip(String filterInfo) {
        StringBuffer buf = new StringBuffer();
        buf.append("<html>"); // NOI18N
        buf.append(filterInfo);
        buf.append("<br>"); // NOI18N
        buf.append(NbBundle.getMessage(ConditionsPanel.class, "TTT_Class_Pattern_Help_1"));
        buf.append("<br>"); // NOI18N
        buf.append(NbBundle.getMessage(ConditionsPanel.class, "TTT_Class_Pattern_Help_2"));
        buf.append("<br>"); // NOI18N
        buf.append(NbBundle.getMessage(ConditionsPanel.class, "TTT_Class_Pattern_Help_3"));
        return buf.toString();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        spCondition = new javax.swing.JScrollPane();
        tfCondition = new JEditorPane("text/x-java", "");
        classFilterCheckBox = new javax.swing.JCheckBox();
        classIncludeFilterLabel = new javax.swing.JLabel();
        classIncludeFilterTextField = new javax.swing.JTextField();
        classExcludeFilterLabel = new javax.swing.JLabel();
        classExcludeFilterCheckBox = new javax.swing.JCheckBox();
        classExcludeFilterTextField = new javax.swing.JTextField();
        conditionCheckBox = new javax.swing.JCheckBox();
        panelHitCountFilter = new javax.swing.JPanel();
        tfHitCountFilter = new javax.swing.JTextField();
        cbHitStyle = new javax.swing.JComboBox();
        cbWhenHitCount = new javax.swing.JCheckBox();
        conditionComboBox = new javax.swing.JComboBox();

        spCondition = createScrollableLineEditor();
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.spCondition.toolTipText")); // NOI18N

        tfCondition.setContentType("text/x-java");
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.tfCondition.toolTipText")); // NOI18N
        spCondition.setViewportView(tfCondition);
        tfCondition.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_ConditionTF")); // NOI18N

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(classFilterCheckBox, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classFilterCheckBox.text")); // NOI18N
        classFilterCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_CB_Classes_Filter_Throwing")); // NOI18N
        classFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classFilterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classFilterCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classFilterCheckBox, gridBagConstraints);

        classIncludeFilterLabel.setLabelFor(classIncludeFilterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classIncludeFilterLabel, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classIncludeFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(classIncludeFilterLabel, gridBagConstraints);
        classIncludeFilterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_IncludeClasses_LBL")); // NOI18N

        classIncludeFilterTextField.setToolTipText(createClassPatternTip(NbBundle.getMessage(ConditionsPanel.class, "TTT_CB_Classes_Matched")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classIncludeFilterTextField, gridBagConstraints);

        classExcludeFilterLabel.setLabelFor(classExcludeFilterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classExcludeFilterLabel, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classExcludeFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(classExcludeFilterLabel, gridBagConstraints);
        classExcludeFilterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_ExcludeClasses_LBL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(classExcludeFilterCheckBox, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.classExcludeFilterLabel.text")); // NOI18N
        classExcludeFilterCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_CB_Classes_Excluded")); // NOI18N
        classExcludeFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classExcludeFilterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classExcludeFilterCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classExcludeFilterCheckBox, gridBagConstraints);

        classExcludeFilterTextField.setToolTipText(createClassPatternTip(NbBundle.getMessage(ConditionsPanel.class, "TTT_CB_Classes_Excluded")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(classExcludeFilterTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(conditionCheckBox, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.conditionCheckBox.text")); // NOI18N
        conditionCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Line_Breakpoint_Condition")); // NOI18N
        conditionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        conditionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conditionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(conditionCheckBox, gridBagConstraints);

        panelHitCountFilter.setLayout(new java.awt.GridBagLayout());

        tfHitCountFilter.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Hit_Count")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(tfHitCountFilter, gridBagConstraints);
        tfHitCountFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_HitCountTF")); // NOI18N

        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "equals to", "is greater then", "is multiple of" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        panelHitCountFilter.add(cbHitStyle, gridBagConstraints);
        cbHitStyle.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_CB_HitCount")); // NOI18N
        cbHitStyle.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_CB_HitCount")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbWhenHitCount, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.text")); // NOI18N
        cbWhenHitCount.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Hit_Count")); // NOI18N
        cbWhenHitCount.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbWhenHitCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbWhenHitCountActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(cbWhenHitCount, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panelHitCountFilter, gridBagConstraints);

        conditionComboBox.setEditable(true);
        conditionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.spCondition.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 3, 3);
        add(conditionComboBox, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_Conditions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void classExcludeFilterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classExcludeFilterCheckBoxActionPerformed
    classExcludeFilterTextField.setEnabled(classExcludeFilterCheckBox.isSelected());
}//GEN-LAST:event_classExcludeFilterCheckBoxActionPerformed

private void cbWhenHitCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbWhenHitCountActionPerformed
    boolean isSelected = cbWhenHitCount.isSelected();
    cbHitStyle.setEnabled(isSelected);
    tfHitCountFilter.setEnabled(isSelected);
    if (isSelected) {
        cbHitStyle.requestFocusInWindow();
    }
}//GEN-LAST:event_cbWhenHitCountActionPerformed

private void conditionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionCheckBoxActionPerformed
    boolean isSelected = conditionCheckBox.isSelected();
    conditionComboBox.setEnabled(isSelected);
    conditionComboBox.setEditor(new ConditionComboBoxEditor());
    revalidate();
    repaint();
    if (isSelected) {
        tfCondition.requestFocusInWindow();
    }
}//GEN-LAST:event_conditionCheckBoxActionPerformed

private void classFilterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classFilterCheckBoxActionPerformed
    boolean classFilterEnabled = classFilterCheckBox.isSelected();
    classIncludeFilterTextField.setEnabled(classFilterEnabled);
    classExcludeFilterTextField.setEnabled(classFilterEnabled);
}//GEN-LAST:event_classFilterCheckBoxActionPerformed

    private JScrollPane createScrollableLineEditor() {
        JComponent [] editorComponents = Utilities.createSingleLineEditor("text/x-java");
        JScrollPane sp = (JScrollPane) editorComponents[0];
        tfCondition = (JEditorPane) editorComponents[1];
        return sp;
    }
    
    private final class ConditionComboBoxEditor implements ComboBoxEditor {

        @Override
        public Component getEditorComponent() {
            if (!conditionCheckBox.isSelected()) {
                return tfConditionFieldForUI;
            } else {
                return spCondition;
            }
        }

        @Override
        public void setItem(Object anObject) {
            if (anObject != null) {
                tfCondition.setText(anObject.toString());
            } else {
                tfCondition.setText("");
            }
        }

        @Override
        public Object getItem() {
            return tfCondition.getText();
        }

        @Override
        public void selectAll() {
            tfCondition.selectAll();
        }

        @Override
        public void addActionListener(ActionListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeActionListener(ActionListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private javax.swing.JTextField tfConditionFieldForUI;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHitStyle;
    private javax.swing.JCheckBox cbWhenHitCount;
    private javax.swing.JCheckBox classExcludeFilterCheckBox;
    private javax.swing.JLabel classExcludeFilterLabel;
    private javax.swing.JTextField classExcludeFilterTextField;
    private javax.swing.JCheckBox classFilterCheckBox;
    private javax.swing.JLabel classIncludeFilterLabel;
    private javax.swing.JTextField classIncludeFilterTextField;
    private javax.swing.JCheckBox conditionCheckBox;
    private javax.swing.JComboBox conditionComboBox;
    private javax.swing.JPanel panelHitCountFilter;
    private javax.swing.JScrollPane spCondition;
    private javax.swing.JEditorPane tfCondition;
    private javax.swing.JTextField tfHitCountFilter;
    // End of variables declaration//GEN-END:variables
    
}
