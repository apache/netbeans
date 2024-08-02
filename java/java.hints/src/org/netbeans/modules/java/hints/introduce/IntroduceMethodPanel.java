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
package org.netbeans.modules.java.hints.introduce;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
@NbBundle.Messages({
    "ERR_MethodExistsOrConflict=A conflicting method already exists in the target class, or its supertypes",
    "INFO_MethodWillShadow=The method will shadow an existing one.",
    "WARN_OverridesRestrictedAccess=The method will override a method from supertype",
    "ERR_MethodNameEmpty=The method name is empty",
    "ERR_InvalidMethodName=The method name is not a Java identifier"
}) 
public class IntroduceMethodPanel extends CommonMembersPanel implements ChangeListener {
    
    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    
    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_DEFAULT = 3;
    private static final int ACCESS_PRIVATE = 4;
    
    private JButton btnOk;
    
    /**
     * True, if the target is an interface.
     */
    private boolean targetInterface;    
    private NameChangeSupport changeSupport;
    private NotificationLineSupport notifier;
    
    public IntroduceMethodPanel(String name, int duplicatesCount, Collection<TargetDescription> targets, boolean targetInterface) {
        super(targets);
        initComponents();
        
        this.targetInterface = targetInterface;

        this.changeSupport = new MethodNameSupport(this.name, true);
        this.changeSupport.setChangeListener(this);

        this.name.setText(name); // triggers validation task
        if ( name != null && name.trim().length() > 0 ) {
            this.name.setCaretPosition(name.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(name.length());
        }

        Preferences pref = getPreferences();
        
        if (!targetInterface) {
            int accessModifier = pref.getInt( "accessModifier", ACCESS_PRIVATE ); //NOI18N
            switch( accessModifier ) {
            case ACCESS_PUBLIC:
                accessPublic.setSelected( true );
                break;
            case ACCESS_PROTECTED:
                accessProtected.setSelected( true );
                break;
            case ACCESS_DEFAULT:
                accessDefault.setSelected( true );
                break;
            case ACCESS_PRIVATE:
                accessPrivate.setSelected( true );
                break;
            }
        } else {
            updateAccessVisible(false);
        }

        if (duplicatesCount == 0) {
            duplicates.setEnabled(false);
            duplicates.setSelected(false);
        } else {
            duplicates.setEnabled(true);
            duplicates.setSelected(true); //from pref
            duplicates.setText(duplicates.getText() + " (" + duplicatesCount + ")");
        }
        initialize(target, duplicates);
        updateTargetChange();
        target.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateTargetChange();
            }
        });
    }

    public void setNotifier(NotificationLineSupport notifier) {
        this.notifier = notifier;
    }

    private class MethodNameSupport extends NameChangeSupport {

        public MethodNameSupport(JTextField control, boolean initAsValid) {
            super(control, initAsValid);
        }

        @Override
        protected void notifyNameError(String msg) {
            notifier.setErrorMessage(msg);
        }

        @Override
        protected boolean updateUI(MemberSearchResult result) {
            if (result == null) {
                notifier.clearMessages();
                return true;
            }
            if (result.isConflicting()) {
                notifier.setErrorMessage(Bundle.ERR_MethodExistsOrConflict());
            } else if (result.getRequiredModifier() != null) {
                notifier.setWarningMessage(Bundle.WARN_OverridesRestrictedAccess());
            } else if (result.getShadowed() != null) {
                notifier.setInformationMessage(Bundle.INFO_MethodWillShadow());
            } else {
                notifier.clearMessages();
            }
            return !result.isConflicting();
        }
    }
    
    public void setValidator(MemberValidator validator) {
        changeSupport.setValidator(validator);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Modifier mustMod = changeSupport.getMinAccess();
        if (changeSupport.isValid()) {
            if (mustMod == null) {
                if (accessPublic.isVisible()) {
                    accessPublic.setEnabled(true);
                }
                if (accessProtected.isVisible()) {
                    accessProtected.setEnabled(true);
                }
                if (accessPrivate.isVisible()) {
                    accessPrivate.setEnabled(true);
                }
                if (accessDefault.isVisible()) {
                    accessDefault.setEnabled(true);
                }
                checkRefactorExisting.setEnabled(false);
                checkRefactorExisting.setSelected(false);
            } else {
                switch (mustMod) {
                    case PUBLIC:
                        accessProtected.setEnabled(false);
                        accessPrivate.setEnabled(false);
                        accessDefault.setEnabled(false);
                        break;
                    case DEFAULT:
                        accessProtected.setEnabled(false);
                        accessDefault.setEnabled(false);
                        accessPrivate.setEnabled(false);
                        break;

                    case PROTECTED:
                        accessDefault.setEnabled(false);
                        accessPrivate.setEnabled(false);
                        break;
                }
                checkRefactorExisting.setEnabled(true);
                checkRefactorExisting.setSelected(refactorExisting);
            }
            updateAccessSelection();
        }
        btnOk.setEnabled(changeSupport.isValid());
    }
    
    private void updateTargetChange() {
        int index = target.getSelectedIndex();
        if (index == -1) {
            updateAccessVisible(targetInterface);
            return;
        }
        TargetDescription desc = (TargetDescription)target.getModel().getSelectedItem();
        updateAccessVisible(!desc.iface);
        changeSupport.setTarget(desc.pathHandle);
    }
    
    private void updateAccessVisible(boolean v) {
        lblAccess.setVisible(v);
        accessPublic.setVisible(v);
        accessDefault.setVisible(v);
        accessPrivate.setVisible(v);
        accessProtected.setVisible(v);
        updateAccessSelection();
    }
    
    private boolean isAvailable(JComponent c) {
        return c.isVisible() && c.isEnabled();
    }
    
    private void updateAccessSelection() {
        boolean check = accessPrivate.isSelected();
        if (isAvailable(accessPrivate)) {
            return;
        }
        check |= accessProtected.isSelected();
        if (isAvailable(accessProtected)) {
            accessProtected.setSelected(check);
            return;
        }
        check |= accessDefault.isSelected();
        if (isAvailable(accessDefault)) {
            accessDefault.setSelected(check);
            return;
        }
        if (check) {
            accessPublic.setSelected(true);
        }
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( IntroduceFieldPanel.class ).node( "introduceField" ); //NOI18N
    }
    
    public void setOkButton( JButton btn ) {
        this.btnOk = btn;
        btnOk.setEnabled(changeSupport.isValid());
    }
    
    public boolean isRefactorExisting() {
        return checkRefactorExisting.isEnabled() && checkRefactorExisting.isVisible() && checkRefactorExisting.isSelected();
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
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessDefault = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        duplicates = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        target = new javax.swing.JComboBox();
        checkRefactorExisting = new javax.swing.JCheckBox();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceMethodPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        lblAccess.setLabelFor(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_public")); // NOI18N
        accessPublic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_protected")); // NOI18N
        accessProtected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessDefault);
        org.openide.awt.Mnemonics.setLocalizedText(accessDefault, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_Default")); // NOI18N
        accessDefault.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessDefault.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        accessPrivate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_private")); // NOI18N
        accessPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(duplicates, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.duplicates.text")); // NOI18N

        jLabel1.setLabelFor(target);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.jLabel1.text")); // NOI18N

        target.setModel(new DefaultComboBoxModel());
        target.setRenderer(new TargetsRendererImpl());

        checkRefactorExisting.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkRefactorExisting, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.checkRefactorExisting.text")); // NOI18N
        checkRefactorExisting.setEnabled(false);
        checkRefactorExisting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRefactorExistingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAccess)
                            .addComponent(lblName))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessPublic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessDefault)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessPrivate))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(target, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkRefactorExisting)
                            .addComponent(duplicates))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                    .addComponent(accessDefault)
                    .addComponent(accessPrivate))
                .addGap(18, 18, 18)
                .addComponent(duplicates)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(target, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkRefactorExisting)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        name.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AN_IntrMethod_Name")); // NOI18N
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Name")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Protected")); // NOI18N
        accessDefault.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Default")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void checkRefactorExistingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRefactorExistingActionPerformed
        if (checkRefactorExisting.isEnabled()) {
            refactorExisting = checkRefactorExisting.isSelected();
        }
    }//GEN-LAST:event_checkRefactorExistingActionPerformed
    
    private boolean refactorExisting = true; // PENDING: perhaps save default value in Preferences ?
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton accessDefault;
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox checkRefactorExisting;
    private javax.swing.JCheckBox duplicates;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JComboBox target;
    // End of variables declaration//GEN-END:variables
    
    public String getMethodName() {
        if (methodNameTest != null) return methodNameTest;
        return this.name.getText();
    }
    
    public Set<Modifier> getAccess() {
        if (accessTest != null) return accessTest;

        TargetDescription selTarget = (TargetDescription)target.getModel().getSelectedItem();
        Set<Modifier> set;
        int val;
        if (selTarget.iface) {
            val = -1;
            set = EnumSet.of(Modifier.DEFAULT);
        } else if( accessPublic.isSelected() ) {
            val = ACCESS_PUBLIC;
            set = EnumSet.of(Modifier.PUBLIC);
        } else if( accessProtected.isSelected() ) {
            val = ACCESS_PROTECTED;
            set = EnumSet.of(Modifier.PROTECTED);
        } else if( accessDefault.isSelected() ) {
            val = ACCESS_DEFAULT;
            set = Collections.emptySet();
        } else {
            val = ACCESS_PRIVATE;
            set = EnumSet.of(Modifier.PRIVATE);
        }
        if (val >= 0) {
            getPreferences().putInt( "accessModifier", val ); //NOI18N
        }
        return set;
    }

    public boolean getReplaceOther() {
        return replaceOtherTest != null ? replaceOtherTest : duplicates.isSelected();
    }

    //For tests:
    private String methodNameTest;
    private Set<Modifier> accessTest;
    private Boolean replaceOtherTest;
    
    void setAccess(Set<Modifier> access) {
        this.accessTest = access;
    }

    void setMethodName(String methodName) {
        this.methodNameTest = methodName;
    }

    void setReplaceOther(boolean v) {
        this.replaceOtherTest = v;
    }
}
