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

package org.netbeans.modules.maven.j2ee.ui.wizard;

import java.awt.EventQueue;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import static org.netbeans.modules.maven.j2ee.ui.wizard.Bundle.*;
import org.netbeans.modules.maven.j2ee.ui.wizard.archetype.J2eeArchetypeFactory;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

public final class EAVisualPanel extends JPanel  {

    private EAWizardPanel panel;
    private final ValidationGroup vg;
    private final ServerSelectionHelper helper;


    @SuppressWarnings("unchecked")
    public EAVisualPanel(EAWizardPanel panel) {
        this.panel = panel;
        initComponents();

        helper = new ServerSelectionHelper(serverModel, j2eeVersion, J2eeModule.Type.EAR);
        vg = ValidationGroup.create();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                addArtifactIdValidatorFor(tfWeb);
                addArtifactIdValidatorFor(tfEar);
                addArtifactIdValidatorFor(tfEjb);
            }
        };
        if(EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }

        SwingValidationGroup.setComponentName(tfWeb, "Web ArtifactId");
        SwingValidationGroup.setComponentName(tfEar, "Ear ArtifactId");
        SwingValidationGroup.setComponentName(tfEjb, "Ejb ArtifactId");

        getAccessibleContext().setAccessibleDescription(LBL_EESettings());
    }

    private void addArtifactIdValidatorFor(JTextField textField) {
        vg.add(textField,
            ValidatorUtils.merge(MavenValidators.createArtifactIdValidators(),
            StringValidators.REQUIRE_VALID_FILENAME
        ));
    }

    @Messages("LBL_EESettings=Settings")
    @Override
    public String getName() {
        return LBL_EESettings();
    }

    void readSettings(WizardDescriptor wizardDescriptor) {
        fillTextFields(wizardDescriptor);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().addItem(vg, true);
            }
        });
    }

    void storeSettings(WizardDescriptor d) {
        File parent = (File) d.getProperty("projdir");
        Profile profile = helper.getSelectedProfile();

        if (profile == null) {
            profile = Profile.JAVA_EE_8_FULL;
        }

        helper.storeServerSettings(d);
        storeEarSettings(d, profile, parent);
        storeEjbSettings(d, profile, parent);
        storeWebSettings(d, profile, parent);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().remove(vg);
            }
        });
    }

    private void storeEarSettings(WizardDescriptor d, Profile profile, File parent) {
        String earText = tfEar.getText().trim();

        d.putProperty("ear_projdir", new File(parent, earText));
        d.putProperty("ear_versionInfo", createProjectInfo(d, earText));
        d.putProperty("ear_archetype", J2eeArchetypeFactory.getInstance().findArchetypeFor(J2eeModule.Type.EAR, profile));
    }

    private void storeEjbSettings(WizardDescriptor d, Profile profile, File parent) {
        if (chkEjb.isSelected()) {
            String ejbText = tfEjb.getText().trim();

            d.putProperty("ejb_projdir", new File(parent, ejbText));
            d.putProperty("ejb_versionInfo", createProjectInfo(d, ejbText));
            d.putProperty("ejb_archetype", J2eeArchetypeFactory.getInstance().findArchetypeFor(J2eeModule.Type.EJB, profile));
        } else {
            d.putProperty("ejb_projdir", null);
            d.putProperty("ejb_versionInfo", null);
            d.putProperty("ejb_archetype", null);
        }
    }

    private void storeWebSettings(WizardDescriptor d, Profile profile, File parent) {
        if (chkWeb.isSelected()) {
            String webText = tfWeb.getText().trim();

            d.putProperty("web_projdir", new File(parent, webText));
            d.putProperty("web_versionInfo", createProjectInfo(d, webText));
            d.putProperty("web_archetype", J2eeArchetypeFactory.getInstance().findArchetypeFor(J2eeModule.Type.WAR, profile));

        } else {
            d.putProperty("web_projdir", null);
            d.putProperty("web_versionInfo", null);
            d.putProperty("web_archetype", null);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().remove(vg);
            }
        });
    }

    private ProjectInfo createProjectInfo(WizardDescriptor d, String artifactId) {
        return new ProjectInfo((String) d.getProperty("groupId"), artifactId, (String) d.getProperty("version"), null);
    }

    private void fillTextFields(WizardDescriptor wiz) {
        String artifactId = (String) wiz.getProperty("artifactId");
        tfEar.setText(artifactId + "-ear");
        tfWeb.setText(artifactId + "-web");
        tfEjb.setText(artifactId + "-ejb");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        j2eeVersion = new javax.swing.JComboBox();
        lblEEVersion = new javax.swing.JLabel();
        chkEjb = new javax.swing.JCheckBox();
        chkWeb = new javax.swing.JCheckBox();
        lblEar = new javax.swing.JLabel();
        tfWeb = new javax.swing.JTextField();
        tfEjb = new javax.swing.JTextField();
        tfEar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        serverModel = new javax.swing.JComboBox();
        addButton = new javax.swing.JButton();

        lblEEVersion.setLabelFor(j2eeVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblEEVersion, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEEVersion.text")); // NOI18N

        chkEjb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkEjb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkEjb.text")); // NOI18N
        chkEjb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEjbActionPerformed(evt);
            }
        });

        chkWeb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkWeb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkWeb.text")); // NOI18N
        chkWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWebActionPerformed(evt);
            }
        });

        lblEar.setLabelFor(tfEar);
        org.openide.awt.Mnemonics.setLocalizedText(lblEar, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEar.text")); // NOI18N

        tfWeb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.text")); // NOI18N

        tfEjb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.text")); // NOI18N

        tfEar.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEar.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEEVersion)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(j2eeVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(serverModel, 0, 315, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkWeb)
                            .addComponent(chkEjb)
                            .addComponent(lblEar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfEar, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                            .addComponent(tfWeb, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                            .addComponent(tfEjb, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(serverModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEEVersion)
                    .addComponent(j2eeVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkEjb)
                    .addComponent(tfEjb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkWeb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfWeb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfEar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(145, Short.MAX_VALUE))
        );

        j2eeVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.cmbEEVersion.AccessibleContext.accessibleDescription")); // NOI18N
        chkEjb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkEjb.AccessibleContext.accessibleDescription")); // NOI18N
        chkWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkWeb.AccessibleContext.accessibleDescription")); // NOI18N
        tfWeb.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.AccessibleContext.accessibleName")); // NOI18N
        tfWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.AccessibleContext.accessibleDescription")); // NOI18N
        tfEjb.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.AccessibleContext.accessibleName")); // NOI18N
        tfEjb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.AccessibleContext.accessibleDescription")); // NOI18N
        tfEar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEar.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chkEjbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEjbActionPerformed
        // TODO add your handling code here:
        tfEjb.setEnabled(chkEjb.isSelected());
        vg.performValidation();
    }//GEN-LAST:event_chkEjbActionPerformed

    private void chkWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWebActionPerformed
        // TODO add your handling code here:
        tfWeb.setEnabled(chkWeb.isSelected());
        vg.performValidation();
    }//GEN-LAST:event_chkWebActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        helper.addServerButtonPressed();
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox chkEjb;
    private javax.swing.JCheckBox chkWeb;
    private javax.swing.JComboBox j2eeVersion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblEEVersion;
    private javax.swing.JLabel lblEar;
    private javax.swing.JComboBox serverModel;
    private javax.swing.JTextField tfEar;
    private javax.swing.JTextField tfEjb;
    private javax.swing.JTextField tfWeb;
    // End of variables declaration//GEN-END:variables

}

