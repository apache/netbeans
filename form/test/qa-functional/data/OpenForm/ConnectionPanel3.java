/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.form.wizard;

import javax.swing.event.*;
import java.lang.reflect.Method;

import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;

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
            public void stateChanged(ChangeEvent evt) {
                updatePreview();
            }
        };

        paramLabel.setText(bundle.getString("CTL_CW_ParamTabs")); // NOI18N
        paramLabel.setDisplayedMnemonic(
            bundle.getString("CTL_CW_ParamTabs_Mnemonic").charAt(0)); // NOI18N
        previewLabel.setText(
            bundle.getString("CTL_CW_GeneratedPreview")); // NOI18N
        previewLabel.setDisplayedMnemonic(
            bundle.getString("CTL_CW_GeneratedPreview_Mnemonic").charAt(0)); // NOI18N
        previewField.setText(bundle.getString("CTL_CW_Preview")); // NOI18N

        previewField.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_Preview")); // NOI18N
        parameterTabs.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ParamTabs")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ConnectionPanel3")); // NOI18N
        
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(2)); // NOI18N
    }

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
        StringBuffer buf = new StringBuffer();
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
        StringBuffer buf = new StringBuffer();
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
