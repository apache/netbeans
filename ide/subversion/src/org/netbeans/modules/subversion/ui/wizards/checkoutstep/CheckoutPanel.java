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

package org.netbeans.modules.subversion.ui.wizards.checkoutstep;

import javax.swing.GroupLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import org.netbeans.modules.subversion.client.SvnClientFactory;

/**
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public class CheckoutPanel extends JPanel {

    /**
     * Creates new form CheckoutPanel
     */
    public CheckoutPanel() {
        initComponents();
        if (SvnClientFactory.isSvnKit()) {
            workingCopyFormat.setVisible(false);
        } else {
            boolean newFormat = SvnClientFactory.isJavaHl() || SvnClientFactory.isCLI() && !SvnClientFactory.isCLIOldFormat();
            workingCopyFormat.setText(getString(newFormat ? "MSG_WorkingCopyFormat17" : "MSG_WorkingCopyFormat16")); //NOI18N
            preferOldFormatCheckBox.setSelected(false);
            preferOldFormatCheckBox.setVisible(false);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="UI Definition Code">
    private void initComponents() {
        lblSpecifyRepoFolders = new JLabel();
        lblRepoFolders = new JLabel();
        lblWorkingCopy = new JLabel();
        lblRepoRevision = new JLabel();
        lblLocalFolder = new JLabel();
        lblSpecifyLocalFolders = new JLabel();

        setName(getString("BK2024")); // NOI18N

        lblRepoFolders.setLabelFor(repositoryPathTextField);
        lblRepoRevision.setLabelFor(revisionTextField);

        repositoryPathTextField.setColumns(30);
        revisionTextField.setColumns(7);

        Mnemonics.setLocalizedText(lblSpecifyRepoFolders, getString("CTL_Checkout_RepositoryHint")); // NOI18N
        Mnemonics.setLocalizedText(lblRepoFolders, getString("CTL_Checkout_RepositoryFolder")); // NOI18N
        Mnemonics.setLocalizedText(browseRepositoryButton, getString("CTL_Checkout_Browse1")); // NOI18N
        Mnemonics.setLocalizedText(lblRepoRevision, getString("CTL_Checkout_Revision")); // NOI18N
        Mnemonics.setLocalizedText(searchRevisionButton, getString("CTL_Checkout_Search")); // NOI18N
        Mnemonics.setLocalizedText(browseRevisionButton, getString("CTL_Checkout_Browse")); // NOI18N
        revisionTextField.setToolTipText(getString("CTL_Checkout_EmptyHint")); //NOI18N

        atWorkingDirLevelCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        atWorkingDirLevelCheckBox.setEnabled(false);

        Mnemonics.setLocalizedText(atWorkingDirLevelCheckBox, getString("CTL_Checkout_CheckoutContentEmpty")); // NOI18N
        Mnemonics.setLocalizedText(exportCheckBox, getString("CTL_Checkout_Export")); // NOI18N

        lblLocalFolder.setLabelFor(workdirTextField);

        workdirTextField.setColumns(30);

        Mnemonics.setLocalizedText(lblSpecifyLocalFolders, getString("CTL_Checkout_LocalFolderHint")); // NOI18N
        Mnemonics.setLocalizedText(lblLocalFolder, getString("CTL_Checkout_LocalFolder")); // NOI18N
        Mnemonics.setLocalizedText(browseWorkdirButton, getString("CTL_Checkout_Browse2")); // NOI18N
        Mnemonics.setLocalizedText(lblWorkingCopy, getString("CTL_Checkout_WorkingCopy")); // NOI18N
        Mnemonics.setLocalizedText(preferOldFormatCheckBox, getString("CTL_PreferOldFormat.text")); // NOI18N

        scanForProjectsCheckBox.setSelected(true);
        scanForProjectsCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        Mnemonics.setLocalizedText(scanForProjectsCheckBox, getString("CTL_Scan_After_Checkout")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .addComponent(lblSpecifyRepoFolders)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(LEADING)
                                        .addComponent(lblRepoFolders)
                                        .addComponent(lblRepoRevision))
                                .addPreferredGap(RELATED)
                                .addGroup(layout.createParallelGroup(LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(repositoryPathTextField)
                                                .addPreferredGap(RELATED)
                                                .addComponent(browseRepositoryButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(revisionTextField, DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                                .addPreferredGap(RELATED)
                                                .addComponent(searchRevisionButton)
                                                .addComponent(browseRevisionButton))))
                        .addComponent(atWorkingDirLevelCheckBox)
                        .addComponent(exportCheckBox)
                        .addComponent(lblSpecifyLocalFolders)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(LEADING)
                                        .addComponent(lblLocalFolder)
                                        .addComponent(lblWorkingCopy))
                                .addPreferredGap(RELATED)
                                .addGroup(layout.createParallelGroup(LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(workdirTextField)
                                                .addPreferredGap(RELATED)
                                                .addComponent(browseWorkdirButton))
                                        .addComponent(workingCopy, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(workingCopyFormat, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(preferOldFormatCheckBox)))
                        .addComponent(scanForProjectsCheckBox)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(lblSpecifyRepoFolders)
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblRepoFolders)
                                .addComponent(repositoryPathTextField)
                                .addComponent(browseRepositoryButton))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblRepoRevision)
                                .addComponent(revisionTextField)
                                .addComponent(searchRevisionButton)
                                .addComponent(browseRevisionButton))
                        .addPreferredGap(UNRELATED)
                        .addComponent(atWorkingDirLevelCheckBox)
                        .addPreferredGap(RELATED)
                        .addComponent(exportCheckBox)
                        .addGap(28)
                        .addComponent(lblSpecifyLocalFolders)
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblLocalFolder)
                                .addComponent(workdirTextField)
                                .addComponent(browseWorkdirButton))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblWorkingCopy)
                                .addComponent(workingCopy))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workingCopyFormat)        
                        .addComponent(preferOldFormatCheckBox)        
                        .addGap(18)
                        .addComponent(scanForProjectsCheckBox)
        );
        layout.linkSize(new Component[] {lblRepoFolders, lblRepoRevision, lblLocalFolder, lblWorkingCopy});

        browseRepositoryButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Repository_Foldres")); //NOI18N
        scanForProjectsCheckBox.getAccessibleContext().setAccessibleDescription(getString("ACSD_Scan_After_Checkout")); //NOI18N
        lblRepoFolders.getAccessibleContext().setAccessibleDescription(getString("ASCD_Repository_Folders_to_Checkout"));    //NOI18N
        lblRepoRevision.getAccessibleContext().setAccessibleDescription(getString("ASCD_Repository_Revision"));  //NOI18N
        browseWorkdirButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Local_Directory"));   //NOI18N
        searchRevisionButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Search_Revision_Number"));  //NOI18N
        browseRevisionButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Revision_Number"));  //NOI18N
        atWorkingDirLevelCheckBox.getAccessibleContext().setAccessibleDescription(getString("ASCD_Checkout_only_folder_contents"));  //NOI18N
        exportCheckBox.getAccessibleContext().setAccessibleDescription(getString("ASCD_Checkout_Export"));  //NOI18N
        preferOldFormatCheckBox.getAccessibleContext().setAccessibleDescription(getString("ASCD_PreferOldFomat")); //NOI18N
    }// </editor-fold>

    private static String getString(String msgKey) {
        return NbBundle.getMessage(CheckoutPanel.class, msgKey);
    }

    final JCheckBox atWorkingDirLevelCheckBox = new JCheckBox();
    final JCheckBox exportCheckBox = new JCheckBox();
    final JCheckBox preferOldFormatCheckBox = new JCheckBox();
    final JButton browseRepositoryButton = new JButton();
    final JButton browseWorkdirButton = new JButton();
    private JLabel lblLocalFolder;
    private JLabel lblRepoFolders;
    private JLabel lblRepoRevision;
    private JLabel lblSpecifyLocalFolders;
    private JLabel lblSpecifyRepoFolders;
    private JLabel lblWorkingCopy;
    final JTextField repositoryPathTextField = new JTextField();
    final JTextField revisionTextField = new JTextField();
    final JCheckBox scanForProjectsCheckBox = new JCheckBox();
    final JButton searchRevisionButton = new JButton();
    final JButton browseRevisionButton = new JButton();
    final JTextField workdirTextField = new JTextField();
    final JLabel workingCopy = new JLabel();
    final JLabel workingCopyFormat = new JLabel();
    
}
