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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Permits selection of build harness associated with a NetBeans platform.
 * @author Jesse Glick
 * @see "#71628"
 */
public class NbPlatformCustomizerHarness extends JPanel {
    
    private NbPlatform plaf;
    
    public NbPlatformCustomizerHarness() {
        initComponents();
    }
    
    void setPlatform(NbPlatform plaf) {
        this.plaf = plaf;
        if (plaf.isDefault()) {
            ideButton.setSelected(true);
        } else {
            File harnessLocation = plaf.getHarnessLocation();
            NbPlatform dflt = NbPlatform.getDefaultPlatform();
            if (dflt != null && harnessLocation.equals(dflt.getHarnessLocation())) {
                ideButton.setSelected(true);
            } else if (harnessLocation.equals(plaf.getBundledHarnessLocation())) {
                platformButton.setSelected(true);
            } else {
                otherButton.setSelected(true);
            }
        }
        update();
        ApisupportAntUIUtils.setText(otherText, plaf.getHarnessLocation().getAbsolutePath());
    }
    
    private void update() {
        versionText.setText(plaf.getHarnessVersion().getDisplayName());
        if (plaf.isDefault()) {
            platformButton.setEnabled(false);
            otherButton.setEnabled(false);
        } else {
            platformButton.setEnabled(true);
            otherButton.setEnabled(true);
        }
        browseButton.setEnabled(otherButton.isSelected());
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        ideButton = new javax.swing.JRadioButton();
        platformButton = new javax.swing.JRadioButton();
        otherButton = new javax.swing.JRadioButton();
        otherText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        versionLabel = new javax.swing.JLabel();
        versionText = new javax.swing.JTextField();

        buttonGroup.add(ideButton);
        ideButton.setMnemonic('I');
        org.openide.awt.Mnemonics.setLocalizedText(ideButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.ideButton")); // NOI18N
        ideButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ideButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ideButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ideButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(platformButton);
        platformButton.setMnemonic('P');
        org.openide.awt.Mnemonics.setLocalizedText(platformButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.platformButton")); // NOI18N
        platformButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        platformButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        platformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(otherButton);
        otherButton.setMnemonic('O');
        org.openide.awt.Mnemonics.setLocalizedText(otherButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.otherButton")); // NOI18N
        otherButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        otherButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        otherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherButtonActionPerformed(evt);
            }
        });

        otherText.setEditable(false);

        browseButton.setMnemonic('B');
        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.browseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        versionLabel.setDisplayedMnemonic('V');
        versionLabel.setLabelFor(versionText);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.versionLabel")); // NOI18N

        versionText.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ideButton)
                            .addComponent(platformButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(otherButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(otherText, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(versionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(versionText, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ideButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(platformButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(otherButton)
                    .addComponent(browseButton)
                    .addComponent(otherText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(196, Short.MAX_VALUE))
        );

        ideButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessSuppliedIDE")); // NOI18N
        platformButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessSuppliedPlatform")); // NOI18N
        otherButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessOther")); // NOI18N
        otherText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.otherText.AccessibleContext.accessibleName")); // NOI18N
        otherText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.otherText.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        versionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessVersion")); // NOI18N
        versionText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.versionText.AccessibleContext.accessibleName")); // NOI18N
        versionText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.versionText.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPlatformCustomizerHarness.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser jfc = new JFileChooser() {
            // Trick stolen from ProjectChooserAccessory.ProjectFileChooser:
            public void approveSelection() {
                File dir = FileUtil.normalizeFile(getSelectedFile());
                if (NbPlatform.isHarness(dir)) {
                    super.approveSelection();
                } else {
                    setCurrentDirectory(dir);
                }
            }
        };
        FileUtil.preventFileChooserSymlinkTraversal(jfc, null);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setSelectedFile(plaf.getHarnessLocation());
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                plaf.setHarnessLocation(FileUtil.normalizeFile(jfc.getSelectedFile()));
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        update();
        ApisupportAntUIUtils.setText(otherText, plaf.getHarnessLocation().getAbsolutePath());
    }//GEN-LAST:event_browseButtonActionPerformed

    private void otherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherButtonActionPerformed
        try {
            plaf.setHarnessLocation(FileUtil.normalizeFile(new File(otherText.getText())));
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_otherButtonActionPerformed

    private void platformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformButtonActionPerformed
        try {
            plaf.setHarnessLocation(plaf.getBundledHarnessLocation());
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_platformButtonActionPerformed

    private void ideButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ideButtonActionPerformed
        try {
            assert plaf != null;
            NbPlatform dflt = NbPlatform.getDefaultPlatform();
            if (dflt != null) {
                plaf.setHarnessLocation(dflt.getHarnessLocation());
            } else {
                Logger.getLogger(NbPlatformCustomizerHarness.class.getName()).warning("No default platform found"); // #113233
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_ideButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton ideButton;
    private javax.swing.JRadioButton otherButton;
    private javax.swing.JTextField otherText;
    private javax.swing.JRadioButton platformButton;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JTextField versionText;
    // End of variables declaration//GEN-END:variables
    
}
