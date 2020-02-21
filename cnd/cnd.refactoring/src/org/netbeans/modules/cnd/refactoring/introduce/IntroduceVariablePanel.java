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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.openide.util.NbPreferences;

/**
 * based on org.netbeans.modules.java.hints.introduce.IntroduceVariablePanel
 */
public class IntroduceVariablePanel extends javax.swing.JPanel {

    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_PRIVATE = 4;
    private boolean introduceConstant;
    private JButton btnOk;

    public IntroduceVariablePanel(int numDuplicates, String type, String defaultName, boolean introduceConstant, JButton btnOk) {
        this.btnOk = btnOk;

        initComponents();

        this.introduceConstant = introduceConstant;

        lblAccess.setVisible(introduceConstant);
        accessPublic.setVisible(introduceConstant);
        accessProtected.setVisible(introduceConstant);
        accessPrivate.setVisible(introduceConstant);

        Preferences pref = getPreferences(introduceConstant);
        if (numDuplicates == 0) {
            replaceAll.setEnabled(false);
            replaceAll.setSelected(false);
        } else {
            replaceAll.setEnabled(true);
            replaceAll.setText(replaceAll.getText() + " (" + (numDuplicates + 1) + ")"); // NOI18N
            replaceAll.setSelected(pref.getBoolean("replaceAll", true)); //NOI18N
        }

        // Do not support const check box. Instead edit type
        declareFinal.setVisible(false);
        //declareFinal.setEnabled(!introduceConstant);
        //declareFinal.setSelected(introduceConstant ? true : pref.getBoolean("declareFinal", true)); //NOI18N

        if (!introduceConstant) {
            int accessModifier = pref.getInt("accessModifier", ACCESS_PUBLIC); //NOI18N
            switch (accessModifier) {
                case ACCESS_PUBLIC:
                    accessPublic.setSelected(true);
                    break;
                case ACCESS_PROTECTED:
                    accessProtected.setSelected(true);
                    break;
                case ACCESS_PRIVATE:
                    accessPrivate.setSelected(true);
                    break;
            }
        }
        typeField.setText(type);
        if (introduceConstant) {
            name.setText(defaultName.toUpperCase());
        } else {
            name.setText(defaultName);
        }
        if (name != null && defaultName.trim().length() > 0) {
            this.name.setCaretPosition(defaultName.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(defaultName.length());
        }
    }

    private Preferences getPreferences(boolean introduceConstant) {
        return NbPreferences.forModule(IntroduceVariablePanel.class).node(introduceConstant ? "introduceConstant" : "introduceVariable"); //NOI18N
    }

    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            @Override
            public String validate(String text) {
                if (null == text || text.length() == 0) {
                    return ""; // NOI18N
                }
                if (!CndLexerUtilities.isCppIdentifier(text)) {
                    return getDefaultErrorMessage(text);
                }
                return null;
            }
        };

        final ErrorLabel label = new ErrorLabel(name.getDocument(), validator);
        label.addPropertyChangeListener(ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(label.isInputTextValid());
            }
        });
        return label;
    }

    String getDefaultErrorMessage(String inputText) {
        return "'" + inputText + "' is not a valid identifier"; // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        replaceAll = new javax.swing.JCheckBox();
        declareFinal = new javax.swing.JCheckBox();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        errorLabel = createErrorLabel();
        typeLabel = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(replaceAll, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_ReplaceAll")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(declareFinal, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_DeclareFinal")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        accessPublic.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_public")); // NOI18N

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_protected")); // NOI18N

        accessGroup.add(accessPrivate);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_private")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "IntroduceVariablePanel.errorLabel.text")); // NOI18N

        typeLabel.setLabelFor(typeField);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "IntroduceVariablePanel.typeLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblAccess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessPublic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessPrivate))
                            .addComponent(typeField, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(replaceAll)
                            .addComponent(declareFinal))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccess)
                    .addComponent(accessPublic)
                    .addComponent(accessProtected)
                    .addComponent(accessPrivate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(declareFinal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Name")); // NOI18N
        replaceAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_ReplaceAllOccurences")); // NOI18N
        declareFinal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_DeclareFinal")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Protected")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox declareFinal;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JCheckBox replaceAll;
    private javax.swing.JTextField typeField;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
    private CsmVisibility testAccess;

    public String getVariableName() {
        return name.getText();
    }

    public String getType() {
        return typeField.getText();
    }

    public boolean isReplaceAll() {
        boolean ret = replaceAll.isSelected();
        getPreferences(introduceConstant).putBoolean("replaceAll", ret); //NOI18N
        return ret;
    }

    //public boolean isDeclareFinal() {
    //    boolean ret = declareFinal.isSelected();
    //    getPreferences(introduceConstant).putBoolean("declareFinal", ret); //NOI18N
    //    return ret;
    //}

    public CsmVisibility getAccess() {
        if (testAccess != null) {
            return testAccess;
        }

        CsmVisibility set;
        int val;
        if (accessPublic.isSelected()) {
            val = ACCESS_PUBLIC;
            set = CsmVisibility.PUBLIC;
        } else if (accessProtected.isSelected()) {
            val = ACCESS_PROTECTED;
            set = CsmVisibility.PROTECTED;
        } else {
            val = ACCESS_PRIVATE;
            set = CsmVisibility.PRIVATE;
        }
        getPreferences(introduceConstant).putInt("accessModifier", val); //NOI18N
        return set;
    }

    //for tests only:
    void setVariableName(String name) {
        this.name.setText(name);
    }

    //void setDeclareFinal(boolean declareFinal) {
    //    this.declareFinal.setSelected(declareFinal);
    //}

    void setReplaceAll(boolean replaceAll) {
        this.replaceAll.setSelected(replaceAll);
    }

    void setAccess(CsmVisibility access) {
        testAccess = access;
    }
}
