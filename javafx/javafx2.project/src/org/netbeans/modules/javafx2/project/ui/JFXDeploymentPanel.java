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

/*
 * JFXDeploymentPanel.java
 *
 * Created on 1.8.2011, 15:51:50
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectProperties.BundlingType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Somol
 */
public class JFXDeploymentPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final String OTHER_RUNTIME_DIALOG_TITLE = NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_runtime_dialog_title"); //NOI18N
    private static final String DEFAULT_RT = NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_runtime_default"); //NOI18N
    private static final String PREDEFINED_RT = NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_runtime_predefined"); //NOI18N
    private static final String EMPTY_STRING = "$empty$"; //NOI18N
    private static final String ITEMS_DELIMITER = ";"; //NOI18N
    
    private File lastImageFolder = null;
    private JFXProjectProperties jfxProps;
    
    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    
    private volatile boolean comboBoxNativeBundlingActionRunning = false;
    
    /**
     * Creates new form JFXDeploymentPanel
     */
    public JFXDeploymentPanel(JFXProjectProperties props) {
        this.jfxProps = props;
        initComponents();
        if(JFXProjectProperties.isTrue(props.getEvaluator().getProperty(JFXProjectProperties.JAVAFX_SWING))) {
            // disable UI components irrelevant for FX-in-Swing project
            labelInitialRemark.setVisible(false);
            labelInitialRemark.setEnabled(false);
            labelInitialRemarkSwing.setVisible(true);
            labelInitialRemarkSwing.setEnabled(true);
            labelProperties.setVisible(false);
            labelProperties.setEnabled(false);
            labelPropertiesSwing.setVisible(true);
            labelPropertiesSwing.setEnabled(true);
            //checkBoxUpgradeBackground.setVisible(false);
            //checkBoxNoInternet.setVisible(false);
            checkBoxInstallPerm.setVisible(false);
            checkBoxDeskShortcut.setVisible(false);
            checkBoxMenuShortcut.setVisible(false);
            labelCustomJS.setVisible(false);
            labelCustomJSMessage.setVisible(false);
            buttonCustomJSMessage.setVisible(false);
            labelDownloadMode.setVisible(false);
            labelDownloadModeMessage.setVisible(false);
            buttonDownloadMode.setVisible(false);
            //checkBoxUpgradeBackground.setEnabled(false);
            //checkBoxNoInternet.setEnabled(false);
            checkBoxInstallPerm.setEnabled(false);
            checkBoxDeskShortcut.setEnabled(false);
            checkBoxMenuShortcut.setEnabled(false);
            labelCustomJS.setEnabled(false);
            labelCustomJSMessage.setEnabled(false);
            buttonCustomJSMessage.setEnabled(false);
            labelDownloadMode.setEnabled(false);
            labelDownloadModeMessage.setEnabled(false);
            buttonDownloadMode.setEnabled(false);
        } else {
            labelInitialRemark.setVisible(true);
            labelInitialRemark.setEnabled(true);
            labelInitialRemarkSwing.setVisible(false);
            labelInitialRemarkSwing.setEnabled(false);
            labelProperties.setVisible(true);
            labelProperties.setEnabled(true);
            labelPropertiesSwing.setVisible(false);
            labelPropertiesSwing.setEnabled(false);
            checkBoxInstallPerm.setModel(jfxProps.getInstallPermanentlyModel());
            checkBoxDeskShortcut.setModel(jfxProps.getAddDesktopShortcutModel());
            checkBoxMenuShortcut.setModel(jfxProps.getAddStartMenuShortcutModel());
            refreshCustomJSLabel();
            if(jfxProps.getRuntimeCP().isEmpty()) {
                buttonDownloadMode.setEnabled(false);
                labelDownloadMode.setEnabled(false);
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeNone")); // NOI18N
                labelDownloadModeMessage.setEnabled(false);
            } else {
                refreshDownloadModeControls();
            }
        }
        checkBoxUpgradeBackground.setModel(jfxProps.getBackgroundUpdateCheckModel());
        checkBoxNoInternet.setModel(jfxProps.getAllowOfflineModel());
        checkBoxDisableProxy.setModel(jfxProps.getDisableProxyModel());

        checkBoxUnrestrictedAcc.setSelected(jfxProps.getSigningEnabled());
        labelSigning.setEnabled(jfxProps.getSigningEnabled());
        labelSigningMessage.setEnabled(jfxProps.getSigningEnabled());
        checkBoxBLOB.setEnabled(jfxProps.getSigningEnabled());
        checkBoxBLOB.setSelected(jfxProps.getBLOBSigningEnabled());
        buttonSigning.setEnabled(jfxProps.getSigningEnabled());
        checkBoxBundle.setSelected(jfxProps.getNativeBundlingEnabled());
        refreshSigningLabel();
        refreshIconsLabel();
        initComboRT();
    }
    
    private void initComboRT() {
        comboBoxRT.getModel().setPredefined(DEFAULT_RT);
        comboBoxRT.getModel().addPredefined(tokenize(PREDEFINED_RT, ITEMS_DELIMITER).toArray());
        String rt = jfxProps.getRequestedRT();
        comboBoxRT.getModel().setSelectedItem(rt != null && !rt.isEmpty() ? rt : DEFAULT_RT);
        comboBoxRT.setEnabled(true);
        comboBoxRT.setGrowAction(new AbstractAction(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_runtime_other")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                askOtherRuntime();
            }
        });
        comboBoxRT.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    String cur = e.getItem().toString();
                    jfxProps.setRequestedRT(cur.equals(DEFAULT_RT) ? null : cur);
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelTopLabel = new javax.swing.JPanel();
        labelInitialRemark = new javax.swing.JLabel();
        labelInitialRemarkSwing = new javax.swing.JLabel();
        panelBottom = new javax.swing.JPanel();
        labelCommon = new javax.swing.JLabel();
        labelIcons = new javax.swing.JLabel();
        labelIconsMessage = new javax.swing.JLabel();
        buttonIcons = new javax.swing.JButton();
        checkBoxBundle = new javax.swing.JCheckBox();
        labelSigning = new javax.swing.JLabel();
        labelSigningMessage = new javax.swing.JLabel();
        warningSigning = new javax.swing.JLabel();
        buttonSigning = new javax.swing.JButton();
        checkBoxDisableProxy = new javax.swing.JCheckBox();
        labelProperties = new javax.swing.JLabel();
        labelPropertiesSwing = new javax.swing.JLabel();
        panelWS1 = new javax.swing.JPanel();
        checkBoxNoInternet = new javax.swing.JCheckBox();
        checkBoxUpgradeBackground = new javax.swing.JCheckBox();
        panelWS2 = new javax.swing.JPanel();
        checkBoxInstallPerm = new javax.swing.JCheckBox();
        checkBoxDeskShortcut = new javax.swing.JCheckBox();
        checkBoxMenuShortcut = new javax.swing.JCheckBox();
        labelCustomJS = new javax.swing.JLabel();
        labelCustomJSMessage = new javax.swing.JLabel();
        buttonCustomJSMessage = new javax.swing.JButton();
        labelDownloadMode = new javax.swing.JLabel();
        labelDownloadModeMessage = new javax.swing.JLabel();
        buttonDownloadMode = new javax.swing.JButton();
        labelRT = new javax.swing.JLabel();
        comboBoxRT = new org.netbeans.modules.javafx2.project.ui.RuntimeComboBox();
        panelSigning = new javax.swing.JPanel();
        checkBoxUnrestrictedAcc = new javax.swing.JCheckBox();
        checkBoxBLOB = new javax.swing.JCheckBox();
        keepInfoMessageHeight = new javax.swing.Box.Filler(new java.awt.Dimension(0, 32), new java.awt.Dimension(0, 32), new java.awt.Dimension(32767, 32));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setLayout(new java.awt.GridBagLayout());

        panelTopLabel.setLayout(new java.awt.GridBagLayout());

        labelInitialRemark.setText(org.openide.util.NbBundle.getBundle(JFXDeploymentPanel.class).getString("JFXDeploymentPanel.labelInitialRemark.text")); // NOI18N
        labelInitialRemark.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelInitialRemark.setPreferredSize(new java.awt.Dimension(1015, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        panelTopLabel.add(labelInitialRemark, gridBagConstraints);

        labelInitialRemarkSwing.setText(org.openide.util.NbBundle.getBundle(JFXDeploymentPanel.class).getString("JFXDeploymentPanel.labelInitialRemarkSwing.text")); // NOI18N
        labelInitialRemarkSwing.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelInitialRemarkSwing.setPreferredSize(new java.awt.Dimension(1047, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        panelTopLabel.add(labelInitialRemarkSwing, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(panelTopLabel, gridBagConstraints);

        panelBottom.setLayout(new java.awt.GridBagLayout());

        labelCommon.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelCommon.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 10, 0);
        panelBottom.add(labelCommon, gridBagConstraints);

        labelIcons.setLabelFor(labelIconsMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelIcons, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelIcons.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 19, 15, 10);
        panelBottom.add(labelIcons, gridBagConstraints);
        labelIcons.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelIcons.text")); // NOI18N
        labelIcons.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelIcons.text")); // NOI18N

        labelIconsMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelIconsMessage.text")); // NOI18N
        labelIconsMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        panelBottom.add(labelIconsMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonIcons, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.buttonIcons.text")); // NOI18N
        buttonIcons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonIconsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        panelBottom.add(buttonIcons, gridBagConstraints);
        buttonIcons.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonIcons.text")); // NOI18N
        buttonIcons.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonIcons.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxBundle, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxBundle.text")); // NOI18N
        checkBoxBundle.setToolTipText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxBundle.toolTipText")); // NOI18N
        checkBoxBundle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBundleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
        panelBottom.add(checkBoxBundle, gridBagConstraints);
        checkBoxBundle.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxBundle.text")); // NOI18N
        checkBoxBundle.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxBundle.text")); // NOI18N

        labelSigning.setLabelFor(labelSigningMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelSigning, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelSigning.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 37, 15, 10);
        panelBottom.add(labelSigning, gridBagConstraints);
        labelSigning.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelSigning.text")); // NOI18N
        labelSigning.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelSigning.text")); // NOI18N

        labelSigningMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelSigningMessage.text")); // NOI18N
        labelSigningMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        panelBottom.add(labelSigningMessage, gridBagConstraints);

        warningSigning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javafx2/project/ui/resources/info.png"))); // NOI18N
        warningSigning.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.warningSigning.text")); // NOI18N
        warningSigning.setPreferredSize(new java.awt.Dimension(526, 30));
        warningSigning.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 36, 9, 0);
        panelBottom.add(warningSigning, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonSigning, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.buttonSigning.text")); // NOI18N
        buttonSigning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSigningActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        panelBottom.add(buttonSigning, gridBagConstraints);
        buttonSigning.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonSigning.text")); // NOI18N
        buttonSigning.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonSigning.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxDisableProxy, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxDisableProxy.text")); // NOI18N
        checkBoxDisableProxy.setToolTipText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "TOOLTIP.JFXDeploymentPanel.checkBoxDisableProxy.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 15, 0);
        panelBottom.add(checkBoxDisableProxy, gridBagConstraints);
        checkBoxDisableProxy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxDisableProxy.text")); // NOI18N
        checkBoxDisableProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxDisableProxy.text")); // NOI18N

        labelProperties.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 10, 0);
        panelBottom.add(labelProperties, gridBagConstraints);

        labelPropertiesSwing.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelPropertiesSwing.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 10, 0);
        panelBottom.add(labelPropertiesSwing, gridBagConstraints);

        panelWS1.setLayout(new java.awt.GridBagLayout());

        checkBoxNoInternet.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNoInternet, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelWS1.add(checkBoxNoInternet, gridBagConstraints);
        checkBoxNoInternet.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N
        checkBoxNoInternet.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N

        checkBoxUpgradeBackground.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxUpgradeBackground, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelWS1.add(checkBoxUpgradeBackground, gridBagConstraints);
        checkBoxUpgradeBackground.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N
        checkBoxUpgradeBackground.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
        panelBottom.add(panelWS1, gridBagConstraints);

        panelWS2.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxInstallPerm, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelWS2.add(checkBoxInstallPerm, gridBagConstraints);
        checkBoxInstallPerm.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N
        checkBoxInstallPerm.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxDeskShortcut, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelWS2.add(checkBoxDeskShortcut, gridBagConstraints);
        checkBoxDeskShortcut.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N
        checkBoxDeskShortcut.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxMenuShortcut, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelWS2.add(checkBoxMenuShortcut, gridBagConstraints);
        checkBoxMenuShortcut.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N
        checkBoxMenuShortcut.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 15, 0);
        panelBottom.add(panelWS2, gridBagConstraints);

        labelCustomJS.setLabelFor(labelCustomJSMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelCustomJS, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelCustomJS.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 19, 15, 10);
        panelBottom.add(labelCustomJS, gridBagConstraints);
        labelCustomJS.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelCustomJS.text")); // NOI18N
        labelCustomJS.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelCustomJS.text")); // NOI18N

        labelCustomJSMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelCustomJSMessage.text")); // NOI18N
        labelCustomJSMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        panelBottom.add(labelCustomJSMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonCustomJSMessage, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N
        buttonCustomJSMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomJSMessageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        panelBottom.add(buttonCustomJSMessage, gridBagConstraints);
        buttonCustomJSMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N
        buttonCustomJSMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N

        labelDownloadMode.setLabelFor(labelDownloadModeMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelDownloadMode, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 19, 15, 10);
        panelBottom.add(labelDownloadMode, gridBagConstraints);
        labelDownloadMode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N
        labelDownloadMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N

        labelDownloadModeMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelDownloadModeMessage.text")); // NOI18N
        labelDownloadModeMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelBottom.add(labelDownloadModeMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonDownloadMode, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N
        buttonDownloadMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDownloadModeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelBottom.add(buttonDownloadMode, gridBagConstraints);
        buttonDownloadMode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N
        buttonDownloadMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N

        labelRT.setLabelFor(comboBoxRT);
        org.openide.awt.Mnemonics.setLocalizedText(labelRT, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelRT.text")); // NOI18N
        labelRT.setToolTipText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelRT.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(3, 19, 15, 10);
        panelBottom.add(labelRT, gridBagConstraints);
        labelRT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelRT.text")); // NOI18N
        labelRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelRT.toolTipText")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 0.1;
        panelBottom.add(comboBoxRT, gridBagConstraints);
        comboBoxRT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.comboBoxRT.text")); // NOI18N
        comboBoxRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.comboBoxRT.text")); // NOI18N

        panelSigning.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxUnrestrictedAcc, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N
        checkBoxUnrestrictedAcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUnrestrictedAccActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelSigning.add(checkBoxUnrestrictedAcc, gridBagConstraints);
        checkBoxUnrestrictedAcc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N
        checkBoxUnrestrictedAcc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxBLOB, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxBLOB.text")); // NOI18N
        checkBoxBLOB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBLOBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelSigning.add(checkBoxBLOB, gridBagConstraints);
        checkBoxBLOB.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxBLOB.text")); // NOI18N
        checkBoxBLOB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxBLOB.text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        panelBottom.add(panelSigning, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        panelBottom.add(keepInfoMessageHeight, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(panelBottom, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(filler2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshDownloadModeControls() {
        if(jfxProps.getRuntimeCP().size() > jfxProps.getLazyJars().size()) {
            if(jfxProps.getLazyJars().isEmpty()) {
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeEager")); // NOI18N
            } else {
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeMixed")); // NOI18N
            }
        } else {
            labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeLazy")); // NOI18N
        }
    }

    private void buttonCustomJSMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomJSMessageActionPerformed
        final JFXJavaScriptCallbacksPanel rc = new JFXJavaScriptCallbacksPanel(jfxProps);
        final DialogDescriptor dd = new DialogDescriptor(rc,
                NbBundle.getMessage(JFXDeploymentPanel.class, "TXT_JSCallbacks"), // NOI18N
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            jfxProps.setJSCallbacks(rc.getResources());
            jfxProps.setJSCallbacksChanged(true);
            refreshCustomJSLabel();
        }
    }//GEN-LAST:event_buttonCustomJSMessageActionPerformed

    private void buttonDownloadModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDownloadModeActionPerformed
        final JFXDownloadModePanel rc = new JFXDownloadModePanel(
                jfxProps.getRuntimeCP(),
                jfxProps.getLazyJars());
        final DialogDescriptor dd = new DialogDescriptor(rc,
                NbBundle.getMessage(JFXDeploymentPanel.class, "TXT_ManageResources"), // NOI18N
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            jfxProps.setLazyJars(rc.getResources());
            jfxProps.setLazyJarsChanged(true);
            refreshDownloadModeControls();
        }
    }//GEN-LAST:event_buttonDownloadModeActionPerformed

    private void checkBoxUnrestrictedAccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUnrestrictedAccActionPerformed
        boolean sel = checkBoxUnrestrictedAcc.isSelected();
        labelSigning.setEnabled(sel);
        labelSigningMessage.setEnabled(sel);
        checkBoxBLOB.setEnabled(sel);
        buttonSigning.setEnabled(sel);
        jfxProps.setSigningEnabled(sel);
        jfxProps.setPermissionsElevated(sel);
        if(jfxProps.getSigningEnabled() && jfxProps.getSigningType() == JFXProjectProperties.SigningType.NOSIGN) {
            jfxProps.setSigningType(JFXProjectProperties.SigningType.SELF);
        }
        refreshSigningLabel();
    }//GEN-LAST:event_checkBoxUnrestrictedAccActionPerformed

    private void buttonSigningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSigningActionPerformed
        JFXSigningPanel panel = new JFXSigningPanel(jfxProps);
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JFXSigningPanel.class, "TITLE_JFXSigningPanel"), true, null); // NOI18N
        panel.registerListeners();
        panel.setDialogDescriptor(dialogDesc);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
        if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.store();
            refreshSigningLabel();
        }
        panel.unregisterListeners();
        dialog.dispose();
    }//GEN-LAST:event_buttonSigningActionPerformed

    private void checkBoxBundleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBundleActionPerformed
        boolean sel = checkBoxBundle.isSelected();
        jfxProps.setNativeBundlingEnabled(sel);
    }//GEN-LAST:event_checkBoxBundleActionPerformed

    private void buttonIconsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonIconsActionPerformed
        JFXIconsPanel panel = new JFXIconsPanel(jfxProps, lastImageFolder);
        panel.registerDocumentListeners();
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JFXIconsPanel.class, "TITLE_JFXIconsPanel"), true, null); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
        if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.store();
            refreshIconsLabel();
        }
        panel.unregisterDocumentListeners();
    }//GEN-LAST:event_buttonIconsActionPerformed

    private void checkBoxBLOBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBLOBActionPerformed
        boolean sel = checkBoxBLOB.isSelected();
        jfxProps.setBLOBSigningEnabled(sel);
    }//GEN-LAST:event_checkBoxBLOBActionPerformed

    private void refreshCustomJSLabel() {
        int jsDefs = 0;
        for (Map.Entry<String,String> entry : jfxProps.getJSCallbacks().entrySet()) {
            if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                jsDefs++;
            }
        }
        if(jsDefs == 0) {
            labelCustomJSMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_CallbacksDefinedNone")); // NOI18N
        } else {
            labelCustomJSMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_CallbacksDefined", jsDefs)); // NOI18N
        }
    }

    private void refreshSigningLabel() {
        if(!jfxProps.getSigningEnabled() || jfxProps.getSigningType() == JFXProjectProperties.SigningType.NOSIGN) {
            labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningUnsigned")); // NOI18N
            //warningSigning.setVisible(true);
        } else {
            if(jfxProps.getSigningType() == JFXProjectProperties.SigningType.KEY) {
                labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningKey", jfxProps.getSigningKeyAlias())); // NOI18N
                //warningSigning.setVisible(false);
            } else {
                labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningGenerated")); // NOI18N
                //warningSigning.setVisible(true);
            }
        }
    }

    private void refreshIconsLabel() {
        String msg = ""; // NOI18N
        if(jfxProps.getWSIconPath() != null && !jfxProps.getWSIconPath().isEmpty()) {
            msg = NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsJNLPDefined"); // NOI18N
        }
        if(jfxProps.getSplashImagePath() != null && !jfxProps.getSplashImagePath().isEmpty()) {
            msg = msg.isEmpty() ? NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsSplashDefined") : //NOI18N
                    msg + ", " + NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsSplashDefined"); // NOI18N
        }
        if(jfxProps.getNativeIconPath() != null && !jfxProps.getNativeIconPath().isEmpty()) {
            msg = msg.isEmpty() ? NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsNativeDefined") : // NOI18N
                    msg + ", " + NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsNativeDefined"); // NOI18N
        }
        if(msg.isEmpty()) {
            msg = NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconsUndefined"); // NOI18N
        }
        labelIconsMessage.setText(msg);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCustomJSMessage;
    private javax.swing.JButton buttonDownloadMode;
    private javax.swing.JButton buttonIcons;
    private javax.swing.JButton buttonSigning;
    private javax.swing.JCheckBox checkBoxBLOB;
    private javax.swing.JCheckBox checkBoxBundle;
    private javax.swing.JCheckBox checkBoxDeskShortcut;
    private javax.swing.JCheckBox checkBoxDisableProxy;
    private javax.swing.JCheckBox checkBoxInstallPerm;
    private javax.swing.JCheckBox checkBoxMenuShortcut;
    private javax.swing.JCheckBox checkBoxNoInternet;
    private javax.swing.JCheckBox checkBoxUnrestrictedAcc;
    private javax.swing.JCheckBox checkBoxUpgradeBackground;
    private org.netbeans.modules.javafx2.project.ui.RuntimeComboBox comboBoxRT;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler keepInfoMessageHeight;
    private javax.swing.JLabel labelCommon;
    private javax.swing.JLabel labelCustomJS;
    private javax.swing.JLabel labelCustomJSMessage;
    private javax.swing.JLabel labelDownloadMode;
    private javax.swing.JLabel labelDownloadModeMessage;
    private javax.swing.JLabel labelIcons;
    private javax.swing.JLabel labelIconsMessage;
    private javax.swing.JLabel labelInitialRemark;
    private javax.swing.JLabel labelInitialRemarkSwing;
    private javax.swing.JLabel labelProperties;
    private javax.swing.JLabel labelPropertiesSwing;
    private javax.swing.JLabel labelRT;
    private javax.swing.JLabel labelSigning;
    private javax.swing.JLabel labelSigningMessage;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelSigning;
    private javax.swing.JPanel panelTopLabel;
    private javax.swing.JPanel panelWS1;
    private javax.swing.JPanel panelWS2;
    private javax.swing.JLabel warningSigning;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JFXDeploymentPanel.class.getName());
    }

    private void askOtherRuntime() {
        JFXRequestRuntimePanel panel = new JFXRequestRuntimePanel();
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, OTHER_RUNTIME_DIALOG_TITLE, true, null); //NOI18N
        panel.registerListener();
        panel.setDialogDescriptor(dialogDesc);
        panel.setInputText(null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
        if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
            String s = panel.getInputText().trim();
            comboBoxRT.getModel().setUserDefined(s);
            comboBoxRT.getModel().setSelectedItem(s);
            jfxProps.setRequestedRT(s);
        }
        panel.unregisterListener();
        dialog.dispose();      
    }
    
    private static List<String> tokenize(String sequence, String delimiter) {
        StringTokenizer st = new StringTokenizer(sequence, delimiter);
        List<String> r = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            r.add(next.equals(EMPTY_STRING) ? "" : next); // NOI18N
        }
        return r;
    }

}
