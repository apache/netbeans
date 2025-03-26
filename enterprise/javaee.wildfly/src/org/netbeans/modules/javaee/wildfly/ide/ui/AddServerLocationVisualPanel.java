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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import static org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.getDefaultConfigurationFile;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class AddServerLocationVisualPanel extends javax.swing.JPanel {

    private final Set<ChangeListener> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Creates new form AddServerLocationVisualPanel
     */
    public AddServerLocationVisualPanel() {
        initComponents();
        setName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "TITLE_ServerLocation"));
        locationTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }
        });
        configurationTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }
        });
    }

    public String getInstallLocation() {
        return locationTextField.getText();
    }

    public String getConfigurationLocation() {
        return configurationTextField.getText();
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        new ArrayList<>(listeners).forEach(l -> l.stateChanged(ev));
    }

    private void locationChanged() {
        fireChangeEvent();
    }

    private String browseInstallLocation() {
        String insLocation = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(SwingUtilities.getWindowAncestor(this),
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }

    private String browseConfiguration() {
        String insLocation = null;
        JFileChooser chooser = getConfigJFileChooser();
        int returnValue = chooser.showDialog(SwingUtilities.getWindowAncestor(this),
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }

    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N

        chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N

        // set the current directory
        chooser.setSelectedFile(new File(locationTextField.getText().trim()));

        return chooser;
    }

    private JFileChooser getConfigJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigChooserName")); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().endsWith(".xml"));
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigChooserName")); //NOI18N

        chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigChooserName")); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigChooserName")); //NOI18N

        // set the current directory
        chooser.setSelectedFile(new File(configurationTextField.getText().trim()));

        return chooser;
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        configurationTextField = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(locationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridy = 1;
        add(jLabel1, gridBagConstraints);
        locationTextField.setColumns(15);
        locationTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));
        locationTextField.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(locationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton1.getAccessibleContext().setAccessibleDescription("ACSD_Browse_Button_InstallLoc");

        jLabel2.setLabelFor(configurationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigurationLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.gridy = 2;
        add(jLabel2, gridBagConstraints);
        configurationTextField.setColumns(15);
        configurationTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigurationLocation"));
        configurationTextField.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ConfigurationLocation"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(configurationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton2.getAccessibleContext().setAccessibleDescription("ACSD_Browse_Button_InstallLoc");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName("TITLE_AddServerLocationPanel");
        jPanel1.getAccessibleContext().setAccessibleDescription("AddServerLocationPanel_Desc");

        if (WildflyPluginProperties.getInstance().getInstallLocation() != null) {
            locationTextField.setText(WildflyPluginProperties.getInstance().getInstallLocation());
            if (WildflyPluginProperties.getInstance().getConfigLocation() != null) {
                configurationTextField.setText(WildflyPluginProperties.getInstance().getConfigLocation());
            } else {
                configurationTextField.setText(getDefaultConfigurationFile(WildflyPluginProperties.getInstance().getInstallLocation()));
            }
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String newLoc = browseInstallLocation();
        if (newLoc != null && !"".equals(newLoc)) {
            locationTextField.setText(newLoc);
            String configurationFilePath = newLoc + File.separatorChar
                        + "standalone" + File.separatorChar + "configuration"
                        + File.separatorChar + "standalone-full.xml";
            if (configurationTextField.getText() == null || configurationTextField.getText().isEmpty()) {
                if (new File(configurationFilePath).exists()) {
                    configurationTextField.setText(configurationFilePath);
                }
            } else if (!configurationTextField.getText().startsWith(newLoc)) {
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(AddServerLocationVisualPanel.class, "MSG_WARN_INSTALLATION_DIFFERS_CONFIGURATION", configurationFilePath),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(d);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                } else {
                    configurationTextField.setText(configurationFilePath);
                }
            }
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        String newLoc = browseConfiguration();
        if (newLoc != null && !"".equals(newLoc)) {
            configurationTextField.setText(newLoc);
        }
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JTextField configurationTextField;
    // End of variables declaration

}
