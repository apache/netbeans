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
 * based on org.netbeans.modules.java.hints.introduce.IntroduceFieldPanel
 */
public class IntroduceFieldPanel extends javax.swing.JPanel {

    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_PRIVATE = 4;
    private int[] allowInitMethods;
    private boolean allowFinalInCurrentMethod;
    private JButton btnOk;

    public IntroduceFieldPanel(String name, int[] allowInitMethods, int numOccurrences, boolean allowFinalInCurrentMethod, JButton btnOk) {
        this.btnOk = btnOk;

        initComponents();

        this.name.setText(name);
        if (name != null && name.trim().length() > 0) {
            this.name.setCaretPosition(name.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(name.length());
        }
        this.allowInitMethods = allowInitMethods;
        this.replaceAll.setEnabled(numOccurrences > 1);
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;

        Preferences pref = getPreferences();
        if (numOccurrences == 1) {
            replaceAll.setEnabled(false);
            replaceAll.setSelected(false);
        } else {
            replaceAll.setEnabled(true);
            replaceAll.setText(replaceAll.getText() + " (" + numOccurrences + ")"); //NOI18N
            replaceAll.setSelected(pref.getBoolean("replaceAll", true)); //NOI18N
        }

        declareFinal.setSelected(pref.getBoolean("declareFinal", true)); //NOI18N

        int accessModifier = pref.getInt("accessModifier", ACCESS_PRIVATE); //NOI18N
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

        int init = pref.getInt("initMethod", INIT_METHOD); //NOI18N
        switch (init) {
            case INIT_FIELD:
                initField.setSelected(true);
                break;
            case INIT_CONSTRUCTORS:
                initConstructors.setSelected(true);
                break;
            case INIT_METHOD:
                initMethod.setSelected(true);
                break;
        }

        adjustInitializeIn();
        adjustFinal();
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(IntroduceFieldPanel.class).node("introduceField"); //NOI18N
    }

    private void adjustInitializeIn() {
        int allowInitMethodsFlag = this.allowInitMethods[this.replaceAll.isSelected() ? 1 : 0];

        initMethod.setEnabled((allowInitMethodsFlag & INIT_METHOD) != 0);
        initField.setEnabled((allowInitMethodsFlag & INIT_FIELD) != 0);
        initConstructors.setEnabled((allowInitMethodsFlag & INIT_CONSTRUCTORS) != 0);

        if (!initMethod.isEnabled() && initMethod.isSelected()) {
            if (initField.isEnabled()) {
                initField.setSelected(true);
            } else {
                initConstructors.setSelected(true);
            }
        } else if (!initField.isEnabled() && initField.isSelected()) {
            if (initMethod.isEnabled()) {
                initMethod.setSelected(true);
            } else {
                initConstructors.setSelected(true);
            }
        } else if (!initConstructors.isEnabled() && initConstructors.isSelected()) {
            if (initMethod.isEnabled()) {
                initMethod.setSelected(true);
            } else {
                initField.setSelected(true);
            }
        }
    }

    private void adjustFinal() {
        declareFinal.setEnabled(!(initMethod.isSelected() && !allowFinalInCurrentMethod));
        if (initMethod.isSelected() && !allowFinalInCurrentMethod) {
            declareFinal.setSelected(false);
        }
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

        final ErrorLabel errorLabel = new ErrorLabel(name.getDocument(), validator);
        errorLabel.addPropertyChangeListener(ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(errorLabel.isInputTextValid());
            }
        });
        return errorLabel;
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

        initilizeIn = new javax.swing.ButtonGroup();
        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        replaceAll = new javax.swing.JCheckBox();
        declareFinal = new javax.swing.JCheckBox();
        lblInitializeIn = new javax.swing.JLabel();
        initMethod = new javax.swing.JRadioButton();
        initField = new javax.swing.JRadioButton();
        initConstructors = new javax.swing.JRadioButton();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        errLabel = createErrorLabel();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(replaceAll, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_ReplaceAll")); // NOI18N
        replaceAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        replaceAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        replaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(declareFinal, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_DeclareFinal")); // NOI18N
        declareFinal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        declareFinal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        declareFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declareFinalActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblInitializeIn, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "IntroduceFieldPanel.lblInitializeIn.text")); // NOI18N

        initilizeIn.add(initMethod);
        initMethod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(initMethod, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_CurrentMethod")); // NOI18N
        initMethod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initMethod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initMethodActionPerformed(evt);
            }
        });

        initilizeIn.add(initField);
        org.openide.awt.Mnemonics.setLocalizedText(initField, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Field")); // NOI18N
        initField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initFieldActionPerformed(evt);
            }
        });

        initilizeIn.add(initConstructors);
        org.openide.awt.Mnemonics.setLocalizedText(initConstructors, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Constructors")); // NOI18N
        initConstructors.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initConstructors.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initConstructors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initConstructorsActionPerformed(evt);
            }
        });

        lblAccess.setLabelFor(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_public")); // NOI18N
        accessPublic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_protected")); // NOI18N
        accessProtected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        accessPrivate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_private")); // NOI18N
        accessPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(errLabel, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "IntroduceFieldPanel.errLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAccess)
                            .addComponent(lblName))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessPublic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessPrivate))))
                    .addComponent(declareFinal)
                    .addComponent(replaceAll)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblInitializeIn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(initConstructors)
                            .addComponent(initField)
                            .addComponent(initMethod))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInitializeIn)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(initMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initField)
                        .addGap(7, 7, 7)
                        .addComponent(initConstructors)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(errLabel)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Name")); // NOI18N
        replaceAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_ReplaceAllOccurences")); // NOI18N
        declareFinal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_DeclareFinal")); // NOI18N
        initMethod.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_CurrentMethod")); // NOI18N
        initField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Field")); // NOI18N
        initConstructors.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Constructors")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Protected")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void declareFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declareFinalActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_declareFinalActionPerformed

private void initConstructorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initConstructorsActionPerformed
    adjustFinal();
}//GEN-LAST:event_initConstructorsActionPerformed

private void initFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initFieldActionPerformed
    adjustFinal();
}//GEN-LAST:event_initFieldActionPerformed

private void initMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initMethodActionPerformed
    adjustFinal();
}//GEN-LAST:event_initMethodActionPerformed

private void replaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllActionPerformed
    adjustInitializeIn();
}//GEN-LAST:event_replaceAllActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox declareFinal;
    private javax.swing.JLabel errLabel;
    private javax.swing.JRadioButton initConstructors;
    private javax.swing.JRadioButton initField;
    private javax.swing.JRadioButton initMethod;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblInitializeIn;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JCheckBox replaceAll;
    // End of variables declaration//GEN-END:variables

    public String getFieldName() {
        if (fieldNameTest != null) {
            return fieldNameTest;
        }
        return this.name.getText();
    }

    public int getInitializeIn() {
        if (initializeInTest != null) {
            return initializeInTest;
        }
        int ret;
        if (initMethod.isSelected()) {
            ret = INIT_METHOD;
        } else if (initField.isSelected()) {
            ret = INIT_FIELD;
        } else if (initConstructors.isSelected()) {
            ret = INIT_CONSTRUCTORS;
        } else {
            throw new IllegalStateException();
        }
        getPreferences().putInt("initMethod", ret); //NOI18N
        return ret;
    }

    public boolean isReplaceAll() {
        if (replaceAllTest != null) {
            return replaceAllTest;
        }
        boolean ret = replaceAll.isSelected();
        getPreferences().putBoolean("replaceAll", ret); //NOI18N
        return ret;
    }

    public CsmVisibility getAccess() {
        if (accessTest != null) {
            return accessTest;
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
        getPreferences().putInt("accessModifier", val); //NOI18N
        return set;
    }

    public boolean isDeclareFinal() {
        if (declareFinalTest != null) {
            return declareFinalTest;
        }
        boolean ret = declareFinal.isSelected();
        getPreferences().putBoolean("declareFinal", ret); //NOI18N
        return ret;
    }
    //For tests:
    private String fieldNameTest;
    private Integer initializeInTest;
    private Boolean replaceAllTest;
    private CsmVisibility accessTest;
    private Boolean declareFinalTest;

    void setAccess(CsmVisibility access) {
        this.accessTest = access;
    }

    void setDeclareFinal(Boolean declareFinal) {
        this.declareFinalTest = declareFinal;
    }

    void setFieldName(String fieldName) {
        this.fieldNameTest = fieldName;
    }

    void setInitializeIn(Integer initializeIn) {
        this.initializeInTest = initializeIn;
    }

    void setReplaceAll(Boolean replaceAll) {
        this.replaceAllTest = replaceAll;
    }
}
