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
package org.netbeans.modules.selenium2.server;

import java.awt.Dialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class Selenium2Customizer extends javax.swing.JPanel {

    private DialogDescriptor descriptor;

    /**
     * Creates new form Selenium2Customizer
     */
    @NbBundle.Messages({"Tooltip_SeleniumServerJar=The selenium standalone server jar file location",
        "Tooltip_FirefoxProfileDir=Normally, a fresh empty Firefox profile\n"
        + "is generated every time server is launched.\n"
        + "You can specify a directory to make the server\n"
        + "copy your profile directory instead.",
        "Tooltip_UserExtensionFile=Indicates a JavaScript file that\n"
        + "will be loaded into selenium",
        "Tooltip_Port=The port number the selenium server should use\n(default 4444)",
        "Tooltip_SingleWindow=Puts you into a mode where the test web\n"
        + "site executes in a frame. This mode should\n"
        + "only be selected if the application under\n"
        + "test does not use frames."})
    public Selenium2Customizer() {
        initComponents();
        assignTooltips();
        assignPersistedValues();
    }
    
    private void assignTooltips() {
        tfSeleniumServerJar.setToolTipText("<html><pre>"+Bundle.Tooltip_SeleniumServerJar()+"</pre></html>");
        bSeleniumServerJar.setToolTipText("<html><pre>"+Bundle.Tooltip_SeleniumServerJar()+"</pre></html>");
        
        tfFirefoxProfileDir.setToolTipText("<html><pre>"+Bundle.Tooltip_FirefoxProfileDir()+"</pre></html>");
        bFirefoxProfileDir.setToolTipText("<html><pre>"+Bundle.Tooltip_FirefoxProfileDir()+"</pre></html>");
        
        tfUserExtensionFile.setToolTipText("<html><pre>"+Bundle.Tooltip_UserExtensionFile()+"</pre></html>");
        bUserExtensionFile.setToolTipText("<html><pre>"+Bundle.Tooltip_UserExtensionFile()+"</pre></html>");
        
        spinnerPort.setToolTipText("<html><pre>"+Bundle.Tooltip_Port()+"</pre></html>");
        cbSingleWindow.setToolTipText("<html><pre>"+Bundle.Tooltip_SingleWindow()+"</pre></html>");
    }
    
    private void assignPersistedValues() {
        String l = getSeleniumServerJarLocation();
        tfSeleniumServerJar.setText(l == null ? "" : l);
        tfFirefoxProfileDir.setText(Selenium2ServerSupport.getPrefs().get(Selenium2ServerSupport.FIREFOX_PROFILE_TEMPLATE_DIR, ""));
        tfUserExtensionFile.setText(Selenium2ServerSupport.getPrefs().get(Selenium2ServerSupport.USER_EXTENSION_FILE, ""));
        spinnerPort.setValue(Selenium2ServerSupport.getPrefs().getInt(Selenium2ServerSupport.PORT, Selenium2ServerSupport.PORT_DEFAULT));
        cbSingleWindow.setSelected(Selenium2ServerSupport.getPrefs().getBoolean(Selenium2ServerSupport.SINGLE_WINDOW, Selenium2ServerSupport.SINGLE_WINDOW_DEFAULT));
    }
    
    @NbBundle.Messages("MSG_CONFIGURE=Configure Selenium Server")
    public static boolean showCustomizer() {
        Selenium2Customizer panel = new Selenium2Customizer();
        DialogDescriptor descriptor = new DialogDescriptor(panel, Bundle.MSG_CONFIGURE());
        panel.setDescriptor(descriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Selenium2ServerSupport.getPrefs().put(Selenium2ServerSupport.SELENIUM_SERVER_JAR, panel.tfSeleniumServerJar.getText());
            Selenium2ServerSupport.getPrefs().put(Selenium2ServerSupport.FIREFOX_PROFILE_TEMPLATE_DIR, panel.tfFirefoxProfileDir.getText());
            Selenium2ServerSupport.getPrefs().put(Selenium2ServerSupport.USER_EXTENSION_FILE, panel.tfUserExtensionFile.getText());
            Selenium2ServerSupport.getPrefs().putInt(Selenium2ServerSupport.PORT, Integer.parseInt(panel.spinnerPort.getValue().toString()));
            Selenium2ServerSupport.getPrefs().putBoolean(Selenium2ServerSupport.SINGLE_WINDOW, panel.cbSingleWindow.isSelected());
            return true;
        } else {
            return false;
        }
    }
    
    private static String getSeleniumServerJarLocation() {
        return Selenium2ServerSupport.getPrefs().get(Selenium2ServerSupport.SELENIUM_SERVER_JAR, null);
    }

    private void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        updateValidity();
    }
    
    private void updateValidity() {
        descriptor.setValid(isValidJSTestDriverJar(tfSeleniumServerJar.getText()));
    }

    private static boolean isValidJSTestDriverJar(String s) {
        if (s == null) {
            return false;
        }
        File f = new File(s);
        return (f.exists() && isValidFileName(f));
    }
    
    private static boolean isValidFileName(File f) {
        return (f.getName().toLowerCase().startsWith("selenium-server-standalone") && //NOI18N
                f.getName().toLowerCase().endsWith(".jar")); //NOI18N
    }
    
    public static boolean isConfiguredProperly() {
        return isValidJSTestDriverJar(getSeleniumServerJarLocation());
    }
    
    public static String getJSTestDriverJar() {
        return getSeleniumServerJarLocation();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelSeleniumServerJar = new javax.swing.JLabel();
        tfSeleniumServerJar = new javax.swing.JTextField();
        bSeleniumServerJar = new javax.swing.JButton();
        labelFirefoxProfileDir = new javax.swing.JLabel();
        tfFirefoxProfileDir = new javax.swing.JTextField();
        bFirefoxProfileDir = new javax.swing.JButton();
        labelUserExtensionFile = new javax.swing.JLabel();
        tfUserExtensionFile = new javax.swing.JTextField();
        bUserExtensionFile = new javax.swing.JButton();
        labelPort = new javax.swing.JLabel();
        cbSingleWindow = new javax.swing.JCheckBox();
        spinnerPort = new javax.swing.JSpinner();

        labelSeleniumServerJar.setLabelFor(tfSeleniumServerJar);
        org.openide.awt.Mnemonics.setLocalizedText(labelSeleniumServerJar, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.labelSeleniumServerJar.text")); // NOI18N

        tfSeleniumServerJar.setEditable(false);
        tfSeleniumServerJar.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(bSeleniumServerJar, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.bSeleniumServerJar.text")); // NOI18N
        bSeleniumServerJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSeleniumServerJarActionPerformed(evt);
            }
        });

        labelFirefoxProfileDir.setLabelFor(tfFirefoxProfileDir);
        org.openide.awt.Mnemonics.setLocalizedText(labelFirefoxProfileDir, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.labelFirefoxProfileDir.text")); // NOI18N

        tfFirefoxProfileDir.setEditable(false);
        tfFirefoxProfileDir.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(bFirefoxProfileDir, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.bFirefoxProfileDir.text")); // NOI18N
        bFirefoxProfileDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bFirefoxProfileDirActionPerformed(evt);
            }
        });

        labelUserExtensionFile.setLabelFor(tfUserExtensionFile);
        org.openide.awt.Mnemonics.setLocalizedText(labelUserExtensionFile, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.labelUserExtensionFile.text")); // NOI18N

        tfUserExtensionFile.setEditable(false);
        tfUserExtensionFile.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(bUserExtensionFile, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.bUserExtensionFile.text")); // NOI18N
        bUserExtensionFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUserExtensionFileActionPerformed(evt);
            }
        });

        labelPort.setLabelFor(spinnerPort);
        org.openide.awt.Mnemonics.setLocalizedText(labelPort, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.labelPort.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSingleWindow, org.openide.util.NbBundle.getMessage(Selenium2Customizer.class, "Selenium2Customizer.cbSingleWindow.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSeleniumServerJar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfSeleniumServerJar, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bSeleniumServerJar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelFirefoxProfileDir)
                            .addComponent(labelUserExtensionFile)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelPort)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbSingleWindow)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfUserExtensionFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bUserExtensionFile))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfFirefoxProfileDir)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bFirefoxProfileDir)))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {labelFirefoxProfileDir, labelSeleniumServerJar, labelUserExtensionFile});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSeleniumServerJar)
                    .addComponent(bSeleniumServerJar)
                    .addComponent(tfSeleniumServerJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFirefoxProfileDir)
                    .addComponent(bFirefoxProfileDir)
                    .addComponent(tfFirefoxProfileDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUserExtensionFile)
                    .addComponent(bUserExtensionFile)
                    .addComponent(tfUserExtensionFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPort)
                    .addComponent(spinnerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbSingleWindow))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {bFirefoxProfileDir, bSeleniumServerJar, bUserExtensionFile, labelFirefoxProfileDir, labelSeleniumServerJar, labelUserExtensionFile, tfFirefoxProfileDir, tfSeleniumServerJar, tfUserExtensionFile});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cbSingleWindow, labelPort, spinnerPort});

    }// </editor-fold>//GEN-END:initComponents

    private void bSeleniumServerJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSeleniumServerJarActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Jar File", "jar"));
        chooser.setSelectedFile(new File(tfSeleniumServerJar.getText().trim()));
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            tfSeleniumServerJar.setText(chooser.getSelectedFile().getAbsolutePath());
            updateValidity();
        }
    }//GEN-LAST:event_bSeleniumServerJarActionPerformed

    private void bFirefoxProfileDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bFirefoxProfileDirActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(tfFirefoxProfileDir.getText().trim()));
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            tfFirefoxProfileDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_bFirefoxProfileDirActionPerformed

    private void bUserExtensionFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUserExtensionFileActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Javascript File", "js"));
        chooser.setSelectedFile(new File(tfUserExtensionFile.getText().trim()));
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            tfUserExtensionFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_bUserExtensionFileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bFirefoxProfileDir;
    private javax.swing.JButton bSeleniumServerJar;
    private javax.swing.JButton bUserExtensionFile;
    private javax.swing.JCheckBox cbSingleWindow;
    private javax.swing.JLabel labelFirefoxProfileDir;
    private javax.swing.JLabel labelPort;
    private javax.swing.JLabel labelSeleniumServerJar;
    private javax.swing.JLabel labelUserExtensionFile;
    private javax.swing.JSpinner spinnerPort;
    private javax.swing.JTextField tfFirefoxProfileDir;
    private javax.swing.JTextField tfSeleniumServerJar;
    private javax.swing.JTextField tfUserExtensionFile;
    // End of variables declaration//GEN-END:variables

}
