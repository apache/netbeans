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
package org.netbeans.modules.rust.options.cargo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.rust.options.impl.RustOptionsImpl;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

final class RustOptionsPanel extends javax.swing.JPanel {

    private static final String CARGO_COMMAND = "cargo";
    private static final String RUSTUP_COMMAND = "rustup";

    private final RustOptionsPanelController controller;
    private JFileChooser fileChooser;
    private SwingWorker<String, String> cargoVersionWorker;
    private SwingWorker<String, String> rustupVersionWorker;

    RustOptionsPanel(RustOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        txtCargoPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            private void documentChanged(DocumentEvent e) {
                controller.changed();
                File file = Paths.get(txtCargoPath.getText()).toFile();
                boolean executable = file.exists() && file.canExecute();
                cmdGetVersion.setEnabled(executable);
            }
        });
        txtRustupPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            private void documentChanged(DocumentEvent e) {
                controller.changed();
                File file = Paths.get(txtRustupPath.getText()).toFile();
                boolean executable = file.exists() && file.canExecute();
                cmdRustupGetVersion.setEnabled(executable);
            }
        });
    }

    private SwingWorker<String, String> newCargoVersionWorker() {
        return new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                String cargo = txtCargoPath.getText();
                return getVersionOf(cargo);
            }

            @Override
            protected void done() {
                cargoVersionWorker = null;
                String version;
                try {
                    version = get();
                    lblCargoVersion.setText(version);
                } catch (Exception ex) {
                    lblCargoVersion.setText("");
                }
            }
        };
    }

    private SwingWorker<String, String> newRustupVersionWorker() {
        return new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                String rustup = txtRustupPath.getText();
                return getVersionOf(rustup);
            }

            @Override
            protected void done() {
                rustupVersionWorker = null;
                String version;
                try {
                    version = get();
                    lblRustupVersion.setText(version);
                } catch (Exception ex) {
                    lblRustupVersion.setText("");
                }
            }
        };
    }

    private String getVersionOf(String path) throws Exception {
        File file = Paths.get(path).toFile();
        if (file.isFile() && file.canExecute()) {
            return getVersionOf(file);
        }
        return null;
    }

    private String getVersionOf(File executable) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{executable.getAbsolutePath(), "-V"});
        int ok = p.waitFor();
        if (ok == 0) {
            String version = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                do {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    version = line;
                } while (true);
            }
            p.destroy();
            return version;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblCargoLocation = new javax.swing.JLabel();
        txtCargoPath = new javax.swing.JTextField();
        cmdBrowse = new javax.swing.JButton();
        cmdGetVersion = new javax.swing.JButton();
        lblCargoVersion = new javax.swing.JLabel();
        lblRustupLocation = new javax.swing.JLabel();
        txtRustupPath = new javax.swing.JTextField();
        cmdRustupBrowse = new javax.swing.JButton();
        cmdRustupGetVersion = new javax.swing.JButton();
        lblRustupVersion = new javax.swing.JLabel();
        pnlSpacer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        lblCargoLocation.setLabelFor(txtCargoPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblCargoLocation, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.lblCargoLocation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblCargoLocation, gridBagConstraints);

        txtCargoPath.setText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.txtCargoPath.text")); // NOI18N
        txtCargoPath.setToolTipText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.txtCargoPath.toolTipText")); // NOI18N
        txtCargoPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCargoPathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(txtCargoPath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cmdBrowse, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdBrowse.text")); // NOI18N
        cmdBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdBrowse.toolTipText")); // NOI18N
        cmdBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(cmdBrowse, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cmdGetVersion, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdGetVersion.text")); // NOI18N
        cmdGetVersion.setEnabled(false);
        cmdGetVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGetVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        add(cmdGetVersion, gridBagConstraints);

        lblCargoVersion.setFont(lblCargoVersion.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(lblCargoVersion, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.lblCargoVersion.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblCargoVersion, gridBagConstraints);

        lblRustupLocation.setLabelFor(txtCargoPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblRustupLocation, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.lblRustupLocation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblRustupLocation, gridBagConstraints);

        txtRustupPath.setText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.txtRustupPath.text")); // NOI18N
        txtRustupPath.setToolTipText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.txtRustupPath.toolTipText")); // NOI18N
        txtRustupPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRustupPathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(txtRustupPath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cmdRustupBrowse, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdRustupBrowse.text")); // NOI18N
        cmdRustupBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdRustupBrowse.toolTipText")); // NOI18N
        cmdRustupBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRustupBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(cmdRustupBrowse, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cmdRustupGetVersion, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.cmdRustupGetVersion.text")); // NOI18N
        cmdRustupGetVersion.setEnabled(false);
        cmdRustupGetVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRustupGetVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        add(cmdRustupGetVersion, gridBagConstraints);

        lblRustupVersion.setFont(lblRustupVersion.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(lblRustupVersion, org.openide.util.NbBundle.getMessage(RustOptionsPanel.class, "RustOptionsPanel.lblRustupVersion.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblRustupVersion, gridBagConstraints);

        pnlSpacer.setPreferredSize(new java.awt.Dimension(8, 8));

        javax.swing.GroupLayout pnlSpacerLayout = new javax.swing.GroupLayout(pnlSpacer);
        pnlSpacer.setLayout(pnlSpacerLayout);
        pnlSpacerLayout.setHorizontalGroup(
            pnlSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 746, Short.MAX_VALUE)
        );
        pnlSpacerLayout.setVerticalGroup(
            pnlSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 717, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlSpacer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void txtCargoPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCargoPathActionPerformed
        controller.changed();
    }//GEN-LAST:event_txtCargoPathActionPerformed

    private void cmdBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBrowseActionPerformed

        showFileChooser(txtCargoPath, CARGO_COMMAND);

    }//GEN-LAST:event_cmdBrowseActionPerformed

    private void cmdGetVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGetVersionActionPerformed
        if (cargoVersionWorker == null) {
            cargoVersionWorker = newCargoVersionWorker();
            cargoVersionWorker.execute();
        }
    }//GEN-LAST:event_cmdGetVersionActionPerformed

    private void txtRustupPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRustupPathActionPerformed
        controller.changed();
    }//GEN-LAST:event_txtRustupPathActionPerformed

    private void cmdRustupBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRustupBrowseActionPerformed
        showFileChooser(txtRustupPath, RUSTUP_COMMAND);
    }//GEN-LAST:event_cmdRustupBrowseActionPerformed

    private void cmdRustupGetVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRustupGetVersionActionPerformed
        if (rustupVersionWorker == null) {
            rustupVersionWorker = newRustupVersionWorker();
            rustupVersionWorker.execute();
        }
    }//GEN-LAST:event_cmdRustupGetVersionActionPerformed

    @NbBundle.Messages({
        "TXT_EXECUTABLE=Executable files"
    })
    void showFileChooser(JTextField textField, String commandName) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser(System.getProperty("user.home")); // NOI18N
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileHidingEnabled(false);
        }
        String executableName = commandName + (Utilities.isWindows() ? ".exe" : ""); // NOI18N
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                boolean valid = f.isDirectory();
                valid = valid
                        || (f.isFile() && f.exists() && f.canExecute() && executableName.equals(f.getName()));
                return valid;
            }

            @Override
            public String getDescription() {
                return Bundle.TXT_EXECUTABLE();
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showDialog(this, null);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    void load() {
        Path cargoLocation = RustOptionsImpl.getCargoLocation(true);
        txtCargoPath.setText(cargoLocation == null ? "" : cargoLocation.toString());

        Path rustupLocation = RustOptionsImpl.getRustupLocation(true);
        txtRustupPath.setText(rustupLocation == null ? "" : rustupLocation.toString());
    }

    void store() {
        RustOptionsImpl.setCargoLocation(txtCargoPath.getText());
        Path cargoLocation = RustOptionsImpl.getCargoLocation(true);
        txtCargoPath.setText(cargoLocation == null ? "" : cargoLocation.toString());
        Path rustupLocation = RustOptionsImpl.getRustupLocation(true);
        txtRustupPath.setText(rustupLocation == null ? "" : rustupLocation.toString());
    }

    boolean valid() {
        File cargo = new File(txtCargoPath.getText());
        File rustup = new File(txtRustupPath.getText());
        return cargo.exists() && cargo.isFile() && cargo.canExecute() && rustup.exists() && rustup.isFile() && rustup.canExecute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdBrowse;
    private javax.swing.JButton cmdGetVersion;
    private javax.swing.JButton cmdRustupBrowse;
    private javax.swing.JButton cmdRustupGetVersion;
    private javax.swing.JLabel lblCargoLocation;
    private javax.swing.JLabel lblCargoVersion;
    private javax.swing.JLabel lblRustupLocation;
    private javax.swing.JLabel lblRustupVersion;
    private javax.swing.JPanel pnlSpacer;
    private javax.swing.JTextField txtCargoPath;
    private javax.swing.JTextField txtRustupPath;
    // End of variables declaration//GEN-END:variables

}
