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

package org.netbeans.core.ui.options.general;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.beaninfo.editors.HtmlBrowser;
import org.netbeans.core.ProxySettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.Keywords(keywords={"#KW_General"}, location=OptionsDisplayer.GENERAL)
public class GeneralOptionsPanel extends JPanel implements ActionListener {
    
    private GeneralOptionsModel     model;
    private HtmlBrowser.FactoryEditor editor;
    private AdvancedProxyPanel advancedPanel;
//    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private boolean valid = true;

    private final Icon PROXY_TEST_OK = ImageUtilities.loadImageIcon("org/netbeans/core/ui/options/general/ok_16.png", false);
    private final Icon PROXY_TEST_ERROR = ImageUtilities.loadImageIcon("org/netbeans/core/ui/options/general/error_16.png", false);
    
    /** 
     * Creates new form GeneralOptionsPanel. 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GeneralOptionsPanel () {
        initComponents ();

        Color nbErrorForeground = UIManager.getColor("nb.errorForeground");
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0);
        }
        errorLabel.setForeground(nbErrorForeground);
        Image img = ImageUtilities.loadImage("org/netbeans/core/ui/resources/error.gif"); //NOI18N
        errorLabel.setIcon(new ImageIcon(img));
        errorLabel.setVisible(false);
        
        loc (lWebBrowser, "Web_Browser");
        loc (lWebProxy, "Web_Proxy");
        loc (lProxyHost, "Proxy_Host");
        loc (lProxyPort, "Proxy_Port");
            
            
        cbWebBrowser.getAccessibleContext ().setAccessibleName (loc ("AN_Web_Browser"));
        cbWebBrowser.getAccessibleContext ().setAccessibleDescription (loc ("AD_Web_Browser"));
        tfProxyHost.getAccessibleContext ().setAccessibleName (loc ("AN_Host"));
        tfProxyHost.getAccessibleContext ().setAccessibleDescription (loc ("AD_Host"));
        tfProxyPort.getAccessibleContext ().setAccessibleName (loc ("AN_Port"));
        tfProxyPort.getAccessibleContext ().setAccessibleDescription (loc ("AD_Port"));
        rbNoProxy.addActionListener (this);
        rbUseSystemProxy.addActionListener (this);
        rbHTTPProxy.addActionListener (this);
        cbWebBrowser.addActionListener (this);
        tfProxyHost.addActionListener (this);
        tfProxyPort.addActionListener (this);
        tfProxyPort.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePortValue();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePortValue();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePortValue();
            }
        });
        
        ButtonGroup bgProxy = new ButtonGroup ();
        bgProxy.add (rbNoProxy);
        bgProxy.add (rbUseSystemProxy);
        bgProxy.add (rbHTTPProxy);
        loc (rbNoProxy, "No_Proxy");
        loc (rbUseSystemProxy, "Use_System_Proxy_Settings");
        loc (rbHTTPProxy, "Use_HTTP_Proxy");
        
        loc (lUsage, "Usage_Statistics");
        lUsage.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Statistics"));
        lUsage.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Statistics"));

        loc (jUsageCheck, "Usage_Check");
        jUsageCheck.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Check"));
        jUsageCheck.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Check"));

        lblUsageInfo.setText(loc("CTL_Usage_Info"));
        lblUsageInfo.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Info"));
        lblUsageInfo.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Info"));
        
        lblLearnMore.setText(loc("CTL_Learn_More"));
        lblLearnMore.getAccessibleContext ().setAccessibleDescription (loc ("AD_Learn_More"));
        lblLearnMore.getAccessibleContext ().setAccessibleName (loc ("AN_Learn_More"));
        
        pbProxyWaiting.setVisible(false);
        
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());

        //#144853: Show statistics ui only in IDE not in Platform.
        if (System.getProperty("nb.show.statistics.ui") == null) {
            jSeparator3.setVisible(false);
            lUsage.setVisible(false);
            jUsageCheck.setVisible(false);
            lblUsageInfo.setVisible(false);
            lblLearnMore.setVisible(false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lWebBrowser = new javax.swing.JLabel();
        cbWebBrowser = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        lWebProxy = new javax.swing.JLabel();
        rbNoProxy = new javax.swing.JRadioButton();
        rbUseSystemProxy = new javax.swing.JRadioButton();
        rbHTTPProxy = new javax.swing.JRadioButton();
        lProxyHost = new javax.swing.JLabel();
        tfProxyHost = new javax.swing.JTextField();
        lProxyPort = new javax.swing.JLabel();
        tfProxyPort = new javax.swing.JTextField();
        bMoreProxy = new javax.swing.JButton();
        editBrowserButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        errorLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblLearnMore = new javax.swing.JLabel();
        lblUsageInfo = new javax.swing.JLabel();
        jUsageCheck = new javax.swing.JCheckBox();
        lUsage = new javax.swing.JLabel();
        bReloadProxy = new javax.swing.JButton();
        bTestConnection = new javax.swing.JButton();
        lblTestResult = new javax.swing.JLabel();
        pbProxyWaiting = new javax.swing.JProgressBar();

        lWebBrowser.setLabelFor(cbWebBrowser);
        org.openide.awt.Mnemonics.setLocalizedText(lWebBrowser, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.lWebBrowser.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lWebProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_lWebProxy")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rbNoProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbNoProxy.text")); // NOI18N
        rbNoProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbUseSystemProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbUseSystemProxy.text")); // NOI18N
        rbUseSystemProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbHTTPProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Use_HTTP_Proxy", new Object[] {})); // NOI18N
        rbHTTPProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        lProxyHost.setLabelFor(tfProxyHost);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyHost, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Host", new Object[] {})); // NOI18N

        tfProxyHost.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusLost(evt);
            }
        });

        lProxyPort.setLabelFor(tfProxyPort);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyPort, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Port", new Object[] {})); // NOI18N

        tfProxyPort.setColumns(4);
        tfProxyPort.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bMoreProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy")); // NOI18N
        bMoreProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMoreProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editBrowserButton, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.text")); // NOI18N
        editBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBrowserButtonActionPerformed(evt);
            }
        });

        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblLearnMore, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Learn_More")); // NOI18N
        lblLearnMore.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblLearnMoreMouseEntered(evt);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblLearnMoreMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        jPanel1.add(lblLearnMore, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblUsageInfo, "<html>The usage statistics help us better understand user\nrequirements and prioritize improvements in future releases. We will never\nreverse-engineer the collected data to find specific details about your projects.</html>"); // NOI18N
        lblUsageInfo.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        jPanel1.add(lblUsageInfo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jUsageCheck, "Help us improve the NetBeans IDE by providing anonymous usage data"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        jPanel1.add(jUsageCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lUsage, "Usage Statistics:"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        jPanel1.add(lUsage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bReloadProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.bReloadProxy.text")); // NOI18N
        bReloadProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bReloadProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bTestConnection, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.bTestConnection.text")); // NOI18N
        bTestConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTestConnectionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblTestResult, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.lblTestResult.text")); // NOI18N

        pbProxyWaiting.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lWebBrowser)
                        .addGap(18, 18, 18)
                        .addComponent(cbWebBrowser, 0, 1317, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editBrowserButton))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 1495, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lWebProxy)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(90, 90, 90)
                                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 1313, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(rbHTTPProxy)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lProxyHost)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tfProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 1055, Short.MAX_VALUE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(rbNoProxy)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(rbUseSystemProxy)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(bReloadProxy)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lProxyPort)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bMoreProxy))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(bTestConnection)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblTestResult)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pbProxyWaiting, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 1495, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lWebBrowser)
                    .addComponent(cbWebBrowser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editBrowserButton))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbNoProxy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbUseSystemProxy)
                            .addComponent(bReloadProxy))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbHTTPProxy)
                            .addComponent(lProxyHost)
                            .addComponent(tfProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lProxyPort)
                            .addComponent(tfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bMoreProxy)))
                    .addComponent(lWebProxy))
                .addGap(30, 30, 30)
                .addComponent(errorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblTestResult)
                    .addComponent(bTestConnection)
                    .addComponent(pbProxyWaiting, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bMoreProxy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy.AN")); // NOI18N
        bMoreProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy.AD")); // NOI18N
        editBrowserButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.AN")); // NOI18N
        editBrowserButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void editBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBrowserButtonActionPerformed
    final WebBrowsersOptionsModel wbModel = new WebBrowsersOptionsModel();
    WebBrowsersOptionsPanel wbPanel = new WebBrowsersOptionsPanel(wbModel, cbWebBrowser.getSelectedItem().toString());
    DialogDescriptor dialogDesc = new DialogDescriptor (wbPanel, loc("LBL_WebBrowsersPanel_Title"), true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (DialogDescriptor.OK_OPTION.equals(e.getSource())) {
                    wbModel.applyChanges();
                } else {
                    wbModel.discardChanges();
                }
            }
        });
    dialogDesc.setHelpCtx( new HelpCtx("WebBrowsersManager") ); //NOI18N
    DialogDisplayer.getDefault().createDialog(dialogDesc).setVisible(true);
    if (dialogDesc.getValue().equals(DialogDescriptor.OK_OPTION)) {
        updateWebBrowsers();
        for (int i = 0, items = cbWebBrowser.getItemCount(); i < items; i++) {
            Object item = cbWebBrowser.getItemAt(i);
            if (item.equals(wbModel.getSelectedValue())) {
                cbWebBrowser.setSelectedItem(item);
                break;
            }
        }
    }
}//GEN-LAST:event_editBrowserButtonActionPerformed

private void bMoreProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoreProxyActionPerformed
    assert model != null : "Model found when AdvancedProxyPanel is created";
    if (advancedPanel == null) {
        advancedPanel = new AdvancedProxyPanel (model);
    }
    DialogDescriptor dd = new DialogDescriptor (advancedPanel, loc ("LBL_AdvancedProxyPanel_Title"));
    advancedPanel.setDialogDescriptor(dd);
    dd.createNotificationLineSupport();
    advancedPanel.update (tfProxyHost.getText (), tfProxyPort.getText ());
    DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
    if (DialogDescriptor.OK_OPTION.equals (dd.getValue ())) {
        advancedPanel.applyChanges ();
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());
        isChanged ();
    }    
}//GEN-LAST:event_bMoreProxyActionPerformed

    private void tfProxyPortFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusLost
        tfProxyPort.select (0, 0);
    }//GEN-LAST:event_tfProxyPortFocusLost

    private void tfProxyHostFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusLost
        tfProxyHost.select (0, 0);
    }//GEN-LAST:event_tfProxyHostFocusLost

    private void tfProxyPortFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusGained
        tfProxyPort.setCaretPosition (0);
        tfProxyPort.selectAll ();        
    }//GEN-LAST:event_tfProxyPortFocusGained

    private void tfProxyHostFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusGained
        tfProxyHost.setCaretPosition (0);
        tfProxyHost.selectAll ();
    }//GEN-LAST:event_tfProxyHostFocusGained

    private void lblLearnMoreMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLearnMoreMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_lblLearnMoreMouseEntered

    private void lblLearnMoreMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLearnMoreMousePressed
        URL u = null;
        try {
            u = new URL(loc("METRICS_INFO_URL"));
        } catch (MalformedURLException exc) {
        }
        if (u != null) {
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(u);
        }

    }//GEN-LAST:event_lblLearnMoreMousePressed

    private void bReloadProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bReloadProxyActionPerformed
        ProxySettings.reload();
        rbUseSystemProxy.setToolTipText(getUseSystemProxyToolTip());
    }//GEN-LAST:event_bReloadProxyActionPerformed

    private void bTestConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTestConnectionActionPerformed
        int type;
        String host, port, nonProxyHosts;
        
        if (rbNoProxy.isSelected()) {
            type = ProxySettings.DIRECT_CONNECTION;
            host = null;
            port = null;
            nonProxyHosts = null;
        } else if (rbUseSystemProxy.isSelected()) {
            type = ProxySettings.AUTO_DETECT_PROXY;
            host = null;
            port = null;
            nonProxyHosts = null;
        } else {
            type = ProxySettings.MANUAL_SET_PROXY;
            host = tfProxyHost.getText();
            port = tfProxyPort.getText();
            nonProxyHosts = advancedPanel == null ? null : advancedPanel.getNonProxyHosts();
        }
        
        GeneralOptionsModel.testConnection(this, type, host, port, nonProxyHosts);     
    }//GEN-LAST:event_bTestConnectionActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bMoreProxy;
    private javax.swing.JButton bReloadProxy;
    private javax.swing.JButton bTestConnection;
    private javax.swing.JComboBox cbWebBrowser;
    private javax.swing.JButton editBrowserButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JCheckBox jUsageCheck;
    private javax.swing.JLabel lProxyHost;
    private javax.swing.JLabel lProxyPort;
    private javax.swing.JLabel lUsage;
    private javax.swing.JLabel lWebBrowser;
    private javax.swing.JLabel lWebProxy;
    private javax.swing.JLabel lblLearnMore;
    private javax.swing.JLabel lblTestResult;
    private javax.swing.JLabel lblUsageInfo;
    private javax.swing.JProgressBar pbProxyWaiting;
    private javax.swing.JRadioButton rbHTTPProxy;
    private javax.swing.JRadioButton rbNoProxy;
    private javax.swing.JRadioButton rbUseSystemProxy;
    private javax.swing.JTextField tfProxyHost;
    private javax.swing.JTextField tfProxyPort;
    // End of variables declaration//GEN-END:variables
    
    private void validatePortValue() {
        clearError();

        boolean oldValid = valid;
        valid = isPortValid();
        if (!valid) {
            showError(loc("LBL_GeneralOptionsPanel_PortError")); // NOI18N
        }

        if (oldValid != valid) {
            firePropertyChange(OptionsPanelController.PROP_VALID, oldValid, valid);
        }
    }

    private boolean isPortValid() {
        String port = tfProxyPort.getText();
        boolean portStatus = true;
        if (port != null && port.length() > 0) {
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException nfex) {
                portStatus = false;
            }
        }

        return portStatus;
    }

    private void showError(String message) {        
        errorLabel.setVisible(true);
        errorLabel.setText(message);        
        bTestConnection.setEnabled(false);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        bTestConnection.setEnabled(true);
    }

    private static String loc (String key, String... params) {
        return NbBundle.getMessage (GeneralOptionsPanel.class, key, params);
    }
    
    private String getUseSystemProxyToolTip() {
        if (rbUseSystemProxy.isSelected()) {
            String toolTip;
            String sHost = getProxyPreferences().get(ProxySettings.SYSTEM_PROXY_HTTP_HOST, ""); // NOI18N            
            if (GeneralOptionsModel.usePAC()) {
                toolTip = getPacFile();
            } else if (sHost == null || sHost.trim().length() == 0) {
                toolTip = loc("GeneralOptionsPanel_rbUseSystemProxy_Direct"); // NOI18N
            } else {
                String sPort = getProxyPreferences().get(ProxySettings.SYSTEM_PROXY_HTTP_PORT, ""); // NOI18N
                toolTip = loc("GeneralOptionsPanel_rbUseSystemProxy_Format", sHost, sPort);
            }
            return toolTip;
        } else {
            return null;
        }
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    void update () {
        model = new GeneralOptionsModel ();
        
        // proxy settings
        switch (model.getProxyType ()) {
            case ProxySettings.DIRECT_CONNECTION:
                rbNoProxy.setSelected (true);
                bReloadProxy.setEnabled(false);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                lProxyHost.setEnabled(false);
                lProxyPort.setEnabled(false);
                bMoreProxy.setEnabled (false);
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
                rbUseSystemProxy.setSelected (true);
                bReloadProxy.setEnabled(true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                lProxyHost.setEnabled(false);
                lProxyPort.setEnabled(false);
                bMoreProxy.setEnabled (false);
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                rbHTTPProxy.setSelected (true);
                bReloadProxy.setEnabled(false);
                tfProxyHost.setEnabled (true);
                tfProxyPort.setEnabled (true);
                lProxyHost.setEnabled(true);
                lProxyPort.setEnabled(true);
                bMoreProxy.setEnabled (true);
                break;
            case ProxySettings.AUTO_DETECT_PAC:
                rbUseSystemProxy.setSelected (true);
                bReloadProxy.setEnabled(true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                lProxyHost.setEnabled(false);
                lProxyPort.setEnabled(false);
                bMoreProxy.setEnabled (false);
                break;
        }
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());

        jUsageCheck.setSelected(model.getUsageStatistics());
        
        updateWebBrowsers();
    }
    
    private void updateWebBrowsers() {
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        cbWebBrowser.removeAllItems ();
        // 188767: editor.getTags() can take long time => fill the combo
        // with the selected item only for now and load the rest of its content
        // outside event-dispatch thread
        cbWebBrowser.addItem(editor.getAsText());
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final String[] tags = editor.getTags ();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        cbWebBrowser.removeAllItems ();
                        if (tags.length > 0) {
                            for (String tag : tags) {
                                cbWebBrowser.addItem(tag);
                            }
                            cbWebBrowser.setSelectedItem(editor.getAsText());
                            lWebBrowser.setVisible(true);
                            cbWebBrowser.setVisible(true);
                            editBrowserButton.setVisible(true);
                            jSeparator2.setVisible(true);
                        } else {
                            // #153747 hide web browser settings for platform
                            lWebBrowser.setVisible(false);
                            cbWebBrowser.setVisible(false);
                            editBrowserButton.setVisible(false);
                            jSeparator2.setVisible(false);
                        }
                    }
                });
            }
        });
    }
    
    void applyChanges () {
        // listening on JTextFields dont work!
        // if (!changed) return; 
        
        if (model == null) {
            return;
        }
        
        // proxy settings
        if (rbNoProxy.isSelected ()) {
            model.setProxyType (0);
        } else
        if (rbUseSystemProxy.isSelected ()) {
            model.setProxyType (1);
        } else {
            model.setProxyType (2);
        }
        
        model.setHttpProxyHost (tfProxyHost.getText ());
        model.setHttpProxyPort (tfProxyPort.getText ());
        if (model.useProxyAllProtocols ()) {
            model.setHttpsProxyHost (tfProxyHost.getText ());
            model.setHttpsProxyPort (tfProxyPort.getText ());
            model.setSocksHost (tfProxyHost.getText ());
            model.setSocksPort (tfProxyPort.getText ());
        }

        // web browser settings
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        editor.setAsText ((String) cbWebBrowser.getSelectedItem ());

        model.setUsageStatistics(jUsageCheck.isSelected());
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return isPortValid();
    }
    
    boolean isChanged () {
        if (model == null) {
            return false;
        }
        // web browser settings
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        String browser = editor.getAsText();
        // web browser settings are hidden in platform
        if (cbWebBrowser.isVisible() && browser != null && !browser.equals((String) cbWebBrowser.getSelectedItem())) {
            return true;
        }
        // proxy settings
        int proxyType = model.getProxyType();
        if (rbNoProxy.isSelected() && proxyType != 0) {
            return true;
        } else if (rbUseSystemProxy.isSelected() && proxyType != 1) {
            return true;
        } else if (rbHTTPProxy.isSelected() && proxyType != 2) {
            return true;
        }
        if (!tfProxyHost.getText().equals(model.getHttpProxyHost())) {
            return true;
        }
        if (!tfProxyPort.getText().equals(model.getHttpProxyPort())) {
            return true;
        }
        // usage statistics settings
        if (jUsageCheck.isSelected() != model.getUsageStatistics()) {
            return true;
        }
        return false;
    }
    
    void updateTestConnectionStatus(final GeneralOptionsModel.TestingStatus status, final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                switch (status) {
                    case NOT_TESTED:
                        lblTestResult.setText(" "); // NOI18N
                        lblTestResult.setIcon(null);
                        lblTestResult.setToolTipText("");
                        pbProxyWaiting.setVisible(false);
                        break;
                    case WAITING:
                        lblTestResult.setText(" "); // NOI18N
                        lblTestResult.setIcon(null);
                        lblTestResult.setToolTipText("");
                        pbProxyWaiting.setVisible(true);
                        break;
                    case OK:
                        lblTestResult.setText(" "); // NOI18N
                        lblTestResult.setIcon(PROXY_TEST_OK);
                        lblTestResult.setToolTipText(loc("GeneralOptionsPanel.proxy.result.ok")); // NOI18N
                        pbProxyWaiting.setVisible(false);
                        break;
                    case FAILED:
                        lblTestResult.setText(message);
                        lblTestResult.setIcon(PROXY_TEST_ERROR);
                        lblTestResult.setToolTipText(loc("GeneralOptionsPanel.proxy.result.failed", message)); // NOI18N
                        pbProxyWaiting.setVisible(false);
                        break;
                }
            }
        });
    }


    @Override
    public void actionPerformed (ActionEvent e) {
        bReloadProxy.setEnabled(rbUseSystemProxy.isSelected());
        tfProxyHost.setEnabled (rbHTTPProxy.isSelected ());
        tfProxyPort.setEnabled (rbHTTPProxy.isSelected ());
        lProxyHost.setEnabled(rbHTTPProxy.isSelected ());
        lProxyPort.setEnabled(rbHTTPProxy.isSelected ());
        bMoreProxy.setEnabled (rbHTTPProxy.isSelected ());
        if (rbHTTPProxy.isSelected()){
            //focus textfield when manual proxy is selected
            tfProxyHost.requestFocusInWindow();
        }
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());
    }

    private static String getPacFile() {
        return getProxyPreferences().get(ProxySettings.SYSTEM_PAC, "");
    }
    
    private static Preferences getProxyPreferences() {
        return NbPreferences.forModule(ProxySettings.class);
    }
}
