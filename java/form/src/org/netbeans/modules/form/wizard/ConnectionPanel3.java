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

package org.netbeans.modules.form.wizard;

import javax.swing.event.*;
import java.lang.reflect.Method;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;
import org.openide.WizardDescriptor;

/**
 * The UI component of the ConnectionWizardPanel3.
 *
 * @author Tomas Pavek
 */

class ConnectionPanel3 extends javax.swing.JPanel {

    private ConnectionWizardPanel3 wizardPanel;

    private Class[] parameters;
    private ParametersPicker[] pickers;
    private boolean valid = false;

    private ChangeListener paramsChangeListener = null;

    /** Creates new form ConnectionPanel3 */
    public ConnectionPanel3(ConnectionWizardPanel3 wizardPanel) {
        this.wizardPanel = wizardPanel;

        initComponents ();

        java.util.ResourceBundle bundle = NbBundle.getBundle(ConnectionPanel3.class);

        setName(bundle.getString("CTL_CW_Step3_Title")); // NOI18N

        paramsChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                updatePreview();
            }
        };

        Mnemonics.setLocalizedText(paramLabel, bundle.getString("CTL_CW_ParamTabs")); // NOI18N
        Mnemonics.setLocalizedText(previewLabel, bundle.getString("CTL_CW_GeneratedPreview")); // NOI18N
        previewField.setText(bundle.getString("CTL_CW_Preview")); // NOI18N

        previewField.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_Preview")); // NOI18N
        parameterTabs.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ParamTabs")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ConnectionPanel3")); // NOI18N
        
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(2)); // NOI18N
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(450, 300);
    }

    void setMethod(Method m) {
        parameterTabs.removeChangeListener(paramsChangeListener);
        parameterTabs.removeAll();

        parameters = m.getParameterTypes();
        pickers = new ParametersPicker[parameters.length];
        for (int i=0; i < parameters.length; i++) {
            pickers[i] = new ParametersPicker(wizardPanel.getFormModel(),
                                              parameters[i]);
            pickers[i].addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent evt) {
                    updatePreview();
                }
            });
            pickers[i].setBorder(new javax.swing.border.EmptyBorder(6, 6, 5, 5));

            parameterTabs.addTab(
                org.openide.util.Utilities.getShortClassName(parameters[i]),
                null,
                pickers[i],
                parameters[i].getName());
        }

        valid = isValid();
        parameterTabs.addChangeListener(paramsChangeListener);
        updatePreview();
    }

    String getParametersText() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < pickers.length; i++) {
            buf.append(pickers[i].getText());
            if (i != pickers.length - 1)
                buf.append(", "); // NOI18N
        }
        return buf.toString();
    }

    Object[] getParameters() {
        try {
            Object values[] = new Object [pickers.length];
            for (int i = 0; i < pickers.length; i++)
                values[i] = pickers[i].getPropertyValue();

            return values;
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPreviewText() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < pickers.length; i++) {
            buf.append(pickers[i].getPreviewText());
            if (i != pickers.length - 1)
                buf.append(", "); // NOI18N
        }
        return buf.toString();
    }

    private void updatePreview() {
        previewField.setText(getPreviewText());

        boolean now = isFilled();
        if (now != valid) {
            valid = now;
            wizardPanel.fireStateChanged();
        }
    }

    boolean isFilled() {
        for (int i=0; i < pickers.length; i++)
            if (!pickers[i].isFilled())
                return false;
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        paramLabel = new javax.swing.JLabel();
        parameterTabs = new javax.swing.JTabbedPane();
        previewLabel = new javax.swing.JLabel();
        previewField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        paramLabel.setText("jLabel2");
        paramLabel.setLabelFor(parameterTabs);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(paramLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(parameterTabs, gridBagConstraints);

        previewLabel.setText("jLabel1");
        previewLabel.setLabelFor(previewField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(previewLabel, gridBagConstraints);

        previewField.setEditable(false);
        previewField.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(previewField, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel paramLabel;
    private javax.swing.JTextField previewField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JTabbedPane parameterTabs;
    // End of variables declaration//GEN-END:variables
}
