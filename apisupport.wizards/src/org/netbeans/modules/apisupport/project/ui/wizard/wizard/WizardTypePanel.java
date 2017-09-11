/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import javax.swing.event.DocumentEvent;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The first panel in the <em>New Wizard Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class WizardTypePanel extends BasicWizardIterator.Panel {

    private DataModel data;
    private boolean lastStaticValue = true;
    
    public WizardTypePanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_WizardWizardTitle"));
        numberOfSteps.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
    }
    
    protected String getPanelName() {
        return getMessage("LBL_WizardType_Title");
    }
    
    protected void storeToDataModel() {
        data.setBranching(dynamic.isSelected());
        if (newFile.isSelected()) {
            data.setFileTemplateType(DataModel.TemplateType.FILE);
        } else if (newHTML4J.isSelected()) {
            data.setFileTemplateType(DataModel.TemplateType.HTML4J);
        } else if (newHTML.isSelected()) {
            data.setFileTemplateType(DataModel.TemplateType.HTML);
        } else {
            data.setFileTemplateType(DataModel.TemplateType.CUSTOM);
        }
        data.setNumberOfSteps(getNumberOfSteps());
    }
    
    private int getNumberOfSteps() {
        return Integer.parseInt(numberOfSteps.getText().trim());
    }

    protected void readFromDataModel() {
        checkValidity();
        switch (data.getFileTemplateType()) {
            case CUSTOM: this.custom.setSelected(true); break;
            case FILE: this.newFile.setSelected(true); break;
            case HTML4J: this.newHTML4J.setSelected(true); break;
            case HTML: this.newHTML.setSelected(true); break;
        }
        this.newHTML.setEnabled(data.isHTMLUIPossible());
        this.newHTML4J.setEnabled(data.isHTMLUIPossible());
        typeChanged(null);
    }
    
    private void checkValidity() {
        if (numberOfSteps.getText().trim().length() == 0) {
            setInfo(getMessage("MSG_EmptyNumberOfSteps"), false);
        } else if (!isNumberOfStepsValid()) {
            setError(getMessage("MSG_IncorrectNumberOfSteps"));
        } else {
            markValid();
        }
    }
    
    private boolean isNumberOfStepsValid() {
        try {
            return getNumberOfSteps() > 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(WizardTypePanel.class);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_WizardTypePanel"));
        custom.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Custom"));
        newFile.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_NewFile"));
        statik.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Static"));
        dynamic.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Dynamic"));
        numberOfStepsTxt.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_NumberOfSteps"));
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(WizardTypePanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        registrationType = new javax.swing.ButtonGroup();
        wizardSteps = new javax.swing.ButtonGroup();
        registrationTypeTxt = new javax.swing.JLabel();
        newHTML4J = new javax.swing.JRadioButton();
        newHTML = new javax.swing.JRadioButton();
        custom = new javax.swing.JRadioButton();
        newFile = new javax.swing.JRadioButton();
        wizardStepsTxt = new javax.swing.JLabel();
        statik = new javax.swing.JRadioButton();
        dynamic = new javax.swing.JRadioButton();
        numberOfStepsTxt = new javax.swing.JLabel();
        numberOfSteps = new javax.swing.JTextField();
        filler = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(registrationTypeTxt, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "LBL_RegistrationType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(registrationTypeTxt, gridBagConstraints);

        registrationType.add(newHTML4J);
        org.openide.awt.Mnemonics.setLocalizedText(newHTML4J, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_NewHTML4J")); // NOI18N
        newHTML4J.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newHTML4J.setMargin(new java.awt.Insets(0, 0, 0, 0));
        newHTML4J.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newHTML4JtypeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(newHTML4J, gridBagConstraints);

        registrationType.add(newHTML);
        org.openide.awt.Mnemonics.setLocalizedText(newHTML, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_NewHTML")); // NOI18N
        newHTML.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newHTML.setMargin(new java.awt.Insets(0, 0, 0, 0));
        newHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newHTMLtypeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(newHTML, gridBagConstraints);

        registrationType.add(custom);
        custom.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(custom, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_Custom")); // NOI18N
        custom.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        custom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        custom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(custom, gridBagConstraints);

        registrationType.add(newFile);
        org.openide.awt.Mnemonics.setLocalizedText(newFile, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_NewFile")); // NOI18N
        newFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newFile.setMargin(new java.awt.Insets(0, 0, 0, 0));
        newFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(newFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(wizardStepsTxt, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "LBL_WizardSteps")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(wizardStepsTxt, gridBagConstraints);

        wizardSteps.add(statik);
        statik.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(statik, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_Static")); // NOI18N
        statik.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        statik.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(statik, gridBagConstraints);

        wizardSteps.add(dynamic);
        org.openide.awt.Mnemonics.setLocalizedText(dynamic, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "CTL_Dynamic")); // NOI18N
        dynamic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dynamic.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 24, 0);
        add(dynamic, gridBagConstraints);

        numberOfStepsTxt.setLabelFor(numberOfSteps);
        org.openide.awt.Mnemonics.setLocalizedText(numberOfStepsTxt, org.openide.util.NbBundle.getMessage(WizardTypePanel.class, "LBL_NumberOfSteps")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(numberOfStepsTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 0);
        add(numberOfSteps, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
	private void typeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeChanged
        boolean isCustom = custom.isSelected();
        statik.setEnabled(isCustom);
        dynamic.setEnabled(isCustom);
        if (isCustom) {
            statik.setSelected(lastStaticValue);
        } else {
            lastStaticValue = statik.isSelected();
            dynamic.setSelected(true);
        }
        if (newHTML.isSelected() || newHTML4J.isSelected()) {
            numberOfSteps.setEnabled(false);
            numberOfSteps.setText("3");
        } else {
            numberOfSteps.setEnabled(true);
        }
	}//GEN-LAST:event_typeChanged

    private void newHTML4JtypeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newHTML4JtypeChanged
        typeChanged(evt);
    }//GEN-LAST:event_newHTML4JtypeChanged

    private void newHTMLtypeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newHTMLtypeChanged
        typeChanged(evt);
    }//GEN-LAST:event_newHTMLtypeChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton custom;
    private javax.swing.JRadioButton dynamic;
    private javax.swing.JLabel filler;
    private javax.swing.JRadioButton newFile;
    private javax.swing.JRadioButton newHTML;
    private javax.swing.JRadioButton newHTML4J;
    private javax.swing.JTextField numberOfSteps;
    private javax.swing.JLabel numberOfStepsTxt;
    private javax.swing.ButtonGroup registrationType;
    private javax.swing.JLabel registrationTypeTxt;
    private javax.swing.JRadioButton statik;
    private javax.swing.ButtonGroup wizardSteps;
    private javax.swing.JLabel wizardStepsTxt;
    // End of variables declaration//GEN-END:variables
    
}
