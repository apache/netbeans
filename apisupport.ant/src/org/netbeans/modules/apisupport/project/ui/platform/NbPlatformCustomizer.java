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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.apisupport.project.api.BasicWizardPanel;
import static org.netbeans.modules.apisupport.project.ui.platform.Bundle.*;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 * Represents customizer for managing NetBeans platforms.
 *
 * @author Martin Krauskopf
 */
public final class NbPlatformCustomizer extends JPanel {
    
    @Messages("MSG_ChoosePlatform=Choose Platform Folder")
    static final String CHOOSER_STEP = MSG_ChoosePlatform();
    @Messages("MSG_PlatformName=Platform Name")
    static final String INFO_STEP = MSG_PlatformName();
    
    static final String PLAF_DIR_PROPERTY = "selectedPlafDir"; // NOI18N
    static final String PLAF_LABEL_PROPERTY = "selectedPlafLabel"; // NOI18N
    
    private NbPlatformCustomizerSources sourcesTab;
    private NbPlatformCustomizerModules modulesTab;
    private NbPlatformCustomizerJavadoc javadocTab;
    private NbPlatformCustomizerHarness harnessTab;

    @ActionID(category="Tools", id="org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizerAction")
    @ActionRegistration(displayName="#CTL_NbPlatformManager_Menu", iconInMenu=false)
    @ActionReference(path="Menu/Tools", position=400)
    @Messages("CTL_NbPlatformManager_Menu=&NetBeans Platforms")
    public static ActionListener showCustomizerAction() {
        return new ActionListener() {
            public @Override void actionPerformed(ActionEvent e) {
                NbPlatformCustomizer.showCustomizer();
            }
        };
    }
    
    @Messages({
        "PROGRESS_checking_for_upgrade=Checking for old harnesses to upgrade",
        "CTL_Close=&Close",
        "CTL_NbPlatformManager_Title=NetBeans Platform Manager"
    })
    public static Object showCustomizer() {
        final AtomicBoolean canceled = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() { // #207451
            @Override public void run() {
                HarnessUpgrader.checkForUpgrade();
            }
        }, PROGRESS_checking_for_upgrade(), canceled, false);
        NbPlatformCustomizer customizer = new NbPlatformCustomizer();
        JButton closeButton = new JButton();
        Mnemonics.setLocalizedText(closeButton, CTL_Close());
        DialogDescriptor descriptor = new DialogDescriptor(
                customizer,
                CTL_NbPlatformManager_Title(),
                true,
                new Object[] {closeButton},
                closeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer"),
                null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        dlg.dispose();
        return customizer.getSelectedNbPlatform();
    }
    
    private NbPlatformCustomizer() {
        initComponents();
        initTabs();
        platformsList.addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                refreshPlatform();
            }
        });
        refreshPlatform();
    }
    
    @Messages({
        "CTL_ModulesTab=Modules",
        "CTL_SourcesTab=Sources",
        "CTL_JavadocTab=Javadoc",
        "CTL_HarnessTab=Harness"
    })
    private void initTabs() {
        if (platformsList.getModel().getSize() > 0) {
            platformsList.setSelectedIndex(0);
            sourcesTab = new NbPlatformCustomizerSources();
            modulesTab = new NbPlatformCustomizerModules();
            javadocTab = new NbPlatformCustomizerJavadoc();
            harnessTab = new NbPlatformCustomizerHarness();
            detailPane.addTab(CTL_ModulesTab(), modulesTab);
            detailPane.addTab(CTL_SourcesTab(), sourcesTab);
            detailPane.addTab(CTL_JavadocTab(), javadocTab);
            detailPane.addTab(CTL_HarnessTab(), harnessTab);
            Container window = this.getTopLevelAncestor();
            if (window != null && window instanceof Window) {
                ((Window) window).pack();
            }
        }
    }
    
    private void refreshPlatform() {
        NbPlatform plaf = (NbPlatform) platformsList.getSelectedValue();
        if (plaf == null) {
            removeButton.setEnabled(false);
            return;
        }
        plfNameValue.setText(NbPlatform.getComputedLabel(plaf.getDestDir()));
        plfNameValue.setCaretPosition(0);
        plfFolderValue.setText(plaf.getDestDir().getAbsolutePath());
        plfFolderValue.setCaretPosition(plfFolderValue.getText().length());
        boolean isValid = plaf.isValid();
        if (isValid) {
            if (sourcesTab == null) {
                initTabs();
            }
            if (sourcesTab != null) {
                modulesTab.setPlatform(plaf);
                sourcesTab.setSourceRootsProvider(plaf);
                javadocTab.setJavadocRootsProvider(plaf);
                harnessTab.setPlatform(plaf);
            }
        } else {
            modulesTab.reset();
            detailPane.setSelectedIndex(0);
        }
        detailPane.setEnabledAt(0, isValid);
        detailPane.setEnabledAt(1, isValid);
        detailPane.setEnabledAt(2, isValid);
        removeButton.setEnabled(!plaf.isDefault());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformLbl = new javax.swing.JLabel();
        platformsListSP = new javax.swing.JScrollPane();
        platformsList = PlatformComponentFactory.getNbPlatformsList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        infoPane = new javax.swing.JPanel();
        plfName = new javax.swing.JLabel();
        pflFolder = new javax.swing.JLabel();
        plfNameValue = new javax.swing.JTextField();
        plfFolderValue = new javax.swing.JTextField();
        detailPane = new javax.swing.JTabbedPane();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.GridBagLayout());

        platformLbl.setLabelFor(platformsList);
        org.openide.awt.Mnemonics.setLocalizedText(platformLbl, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_Platforms")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(platformLbl, gridBagConstraints);

        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsListSP.setViewportView(platformsList);
        platformsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_CTL_platformsList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 12, 6);
        add(platformsListSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "CTL_AddPlatform")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlatform(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_CTL_addButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "CTL_RemovePlatfrom")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePlatform(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_CTL_removeButton")); // NOI18N

        infoPane.setLayout(new java.awt.GridBagLayout());

        plfName.setLabelFor(plfNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfName, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_PlatformName_N")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        infoPane.add(plfName, gridBagConstraints);

        pflFolder.setLabelFor(plfFolderValue);
        org.openide.awt.Mnemonics.setLocalizedText(pflFolder, org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "LBL_PlatformFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        infoPane.add(pflFolder, gridBagConstraints);

        plfNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        infoPane.add(plfNameValue, gridBagConstraints);
        plfNameValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_CTL_plfNameValue")); // NOI18N

        plfFolderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        infoPane.add(plfFolderValue, gridBagConstraints);
        plfFolderValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_CTL_plfFolderValue")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        infoPane.add(detailPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 12, 0);
        add(infoPane, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizer.class, "ACS_NbPlatformCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private PlatformComponentFactory.NbPlatformListModel getPlafListModel() {
        return (PlatformComponentFactory.NbPlatformListModel) platformsList.getModel();
    }
    
    private void removePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePlatform
        NbPlatform plaf = (NbPlatform) platformsList.getSelectedValue();
        if (plaf != null) {
            getPlafListModel().removePlatform(plaf);
            platformsList.setSelectedValue(NbPlatform.getDefaultPlatform(), true);
            refreshPlatform();
        }
    }//GEN-LAST:event_removePlatform
    
    @Messages("CTL_AddNetbeansPlatformTitle=Add NetBeans Platform")
    private void addPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlatform
        PlatformChooserWizardPanel chooser = new PlatformChooserWizardPanel(null);
        PlatformInfoWizardPanel info = new PlatformInfoWizardPanel(null);
        WizardDescriptor wd = new WizardDescriptor(new BasicWizardPanel[] {chooser, info});
        initPanel(chooser, wd, 0);
        initPanel(info, wd, 1);
        wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setTitle(CTL_AddNetbeansPlatformTitle());
        dialog.setVisible(true);
        dialog.toFront();
        if (wd.getValue() == WizardDescriptor.FINISH_OPTION) {
            String plafDir = (String) wd.getProperty(PLAF_DIR_PROPERTY);
            String plafLabel = (String) wd.getProperty(PLAF_LABEL_PROPERTY);
            String id = plafLabel.replace(' ', '_');
            NbPlatform plaf = getPlafListModel().addPlatform(id, plafDir, plafLabel);
            if (plaf != null) {
                platformsList.setSelectedValue(plaf, true);
                refreshPlatform();
            }
        }
    }//GEN-LAST:event_addPlatform
    
    private void initPanel(BasicWizardPanel panel, WizardDescriptor wd, int i) {
        panel.setSettings(wd);
        JComponent jc = (JComponent) panel.getComponent();
        jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
        jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] {
            CHOOSER_STEP, INFO_STEP
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTabbedPane detailPane;
    private javax.swing.JPanel infoPane;
    private javax.swing.JLabel pflFolder;
    private javax.swing.JLabel platformLbl;
    private javax.swing.JList platformsList;
    private javax.swing.JScrollPane platformsListSP;
    private javax.swing.JTextField plfFolderValue;
    private javax.swing.JLabel plfName;
    private javax.swing.JTextField plfNameValue;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    private Object getSelectedNbPlatform() {
        return platformsList.getSelectedValue();
    }
}
