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
package org.netbeans.modules.rust.options.rustanalyzer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.rust.options.impl.RustAnalyzerOptionsImpl;
import org.openide.LifecycleManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

final class RustAnalyzerPanel extends javax.swing.JPanel implements DocumentListener {

    private final RustAnalyzerOptionsPanelController controller;
    private final ComponentListener resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateInfoPaneWidth();
            }
        };
    private final HyperlinkListener hyperlinkListener = (HyperlinkEvent he) -> {
        try {
            if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                URI uri = he.getURL().toURI();
                Desktop.getDesktop().browse(uri);
            }
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    };
    private JFileChooser fileChooser;
    private SwingWorker<String, String> versionWorker;

    RustAnalyzerPanel(RustAnalyzerOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        txtRustAnalyzerPath.getDocument().addDocumentListener(this);
        infoPanel.setEditable(false);
        infoPanel.addHyperlinkListener(hyperlinkListener);
    }

    private void updateInfoPaneWidth() {
        int width = getParent().getWidth();
        setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        setPreferredSize(new Dimension(width, super.getMinimumSize().height));
        validate();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.getUnwrappedParent(this).addComponentListener(resizeListener);
        updateInfoPaneWidth();
    }

    @Override
    public void removeNotify() {
        SwingUtilities.getUnwrappedParent(this).removeComponentListener(resizeListener);
        super.removeNotify();
    }

    private SwingWorker<String, String> newWorker() {
        return new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                String rustAnalyser = txtRustAnalyzerPath.getText();
                return getVersionOf(rustAnalyser);
            }

            @Override
            protected void done() {
                versionWorker = null;
                String version;
                try {
                    version = get();
                    lblRustAnalyzerVersion.setText(version);
                } catch (Exception ex) {
                    lblRustAnalyzerVersion.setText("");
                }
            }
        };
    }

    private String getVersionOf(String aPossibleRustAnalyserPath) throws Exception {
        File rustAnalyser = Paths.get(aPossibleRustAnalyserPath).toFile();
        if (rustAnalyser.isFile() && rustAnalyser.canExecute()) {
            return getVersionOf(rustAnalyser);
        }
        return null;
    }

    private String getVersionOf(File rustAnalyser) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{rustAnalyser.getAbsolutePath(), "--version"});
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

        lblRustAnalyzerPath = new javax.swing.JLabel();
        txtRustAnalyzerPath = new javax.swing.JTextField();
        cmdBrowse = new javax.swing.JButton();
        cmdGetVersion = new javax.swing.JButton();
        lblRustAnalyzerVersion = new javax.swing.JLabel();
        infoPanel = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        lblRustAnalyzerPath.setLabelFor(txtRustAnalyzerPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblRustAnalyzerPath, org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.lblRustAnalyzerPath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblRustAnalyzerPath, gridBagConstraints);

        txtRustAnalyzerPath.setText(org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.txtRustAnalyzerPath.text")); // NOI18N
        txtRustAnalyzerPath.setToolTipText(org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.txtRustAnalyzerPath.toolTipText")); // NOI18N
        txtRustAnalyzerPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRustAnalyzerPathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(txtRustAnalyzerPath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cmdBrowse, org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.cmdBrowse.text")); // NOI18N
        cmdBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.cmdBrowse.toolTipText")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(cmdGetVersion, org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.cmdGetVersion.text")); // NOI18N
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

        lblRustAnalyzerVersion.setFont(lblRustAnalyzerVersion.getFont().deriveFont(lblRustAnalyzerVersion.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(lblRustAnalyzerVersion, org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.lblRustAnalyzerVersion.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(lblRustAnalyzerVersion, gridBagConstraints);

        infoPanel.setContentType("text/html"); // NOI18N
        infoPanel.setText(org.openide.util.NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.infoPanel.text")); // NOI18N
        infoPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(infoPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void txtRustAnalyzerPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRustAnalyzerPathActionPerformed
        controller.changed();
    }//GEN-LAST:event_txtRustAnalyzerPathActionPerformed

    private void cmdBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBrowseActionPerformed

        showFileChooser();

    }//GEN-LAST:event_cmdBrowseActionPerformed

    private void cmdGetVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGetVersionActionPerformed
        if (versionWorker == null) {
            versionWorker = newWorker();
            versionWorker.execute();
        }
    }//GEN-LAST:event_cmdGetVersionActionPerformed

    void showFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser(System.getProperty("user.home")); // NOI18N
            FileFilter rustAnalyserFilter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || (f.isFile() && f.exists() && f.canExecute() && f.getName().startsWith("rust-analyzer"));
                }

                @Override
                public String getDescription() {
                    return NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.filterRustAnalyser");
                }
            };
            fileChooser.addChoosableFileFilter(rustAnalyserFilter);
            fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
            fileChooser.setFileFilter(rustAnalyserFilter);
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileHidingEnabled(false);
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showDialog(this, null);
        if (result == JFileChooser.APPROVE_OPTION) {
            txtRustAnalyzerPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    void load() {
        Path rustAnalyzerLocation = RustAnalyzerOptionsImpl.getRustAnalyzerLocation(true, false);
        txtRustAnalyzerPath.setText(rustAnalyzerLocation == null ? "" : rustAnalyzerLocation.toString());
    }

    void store() {
        String path = txtRustAnalyzerPath.getText();
        Path origPath = RustAnalyzerOptionsImpl.getRustAnalyzerLocation(false, false);
        RustAnalyzerOptionsImpl.setRustAnalyzerLocation(path.trim().isEmpty() ? null : path);
        Path rustAnalyzerLocation = RustAnalyzerOptionsImpl.getRustAnalyzerLocation(true, false);
        txtRustAnalyzerPath.setText(rustAnalyzerLocation == null ? "" : rustAnalyzerLocation.toString());
        if(
                (nullOrBlank(path) && origPath != null)
                || (!nullOrBlank(path) && origPath == null)
        ) {
            askForRestart();
        }
    }

    private boolean nullOrBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    boolean valid() {
        String path = txtRustAnalyzerPath.getText();
        if(path.trim().isEmpty()) {
            return true;
        }
        File rustAnalyzer = new File(txtRustAnalyzerPath.getText());
        return rustAnalyzer.exists() && rustAnalyzer.isFile() && rustAnalyzer.canExecute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdBrowse;
    private javax.swing.JButton cmdGetVersion;
    private javax.swing.JTextPane infoPanel;
    private javax.swing.JLabel lblRustAnalyzerPath;
    private javax.swing.JLabel lblRustAnalyzerVersion;
    private javax.swing.JTextField txtRustAnalyzerPath;
    // End of variables declaration//GEN-END:variables

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
        File file = Paths.get(txtRustAnalyzerPath.getText()).toFile();
        boolean executable = file.exists() && file.canExecute();
        cmdGetVersion.setEnabled(executable);
    }

    private Notification restartNotification;

    private void askForRestart() {
        if(restartNotification != null) {
            restartNotification.clear();
        }
        restartNotification = NotificationDisplayer.getDefault().notify(
                NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.restartTitle"),
                ImageUtilities.loadImageIcon( "org/netbeans/core/windows/resources/restart.png", true ), //NOI18N
                NbBundle.getMessage(RustAnalyzerPanel.class, "RustAnalyzerPanel.restartDetails"),
                e -> {
                    if(restartNotification != null) {
                        restartNotification.clear();
                        restartNotification = null;
                    }
                    LifecycleManager.getDefault().markForRestart();
                    LifecycleManager.getDefault().exit();
                },
                NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.INFO);
    }
}
