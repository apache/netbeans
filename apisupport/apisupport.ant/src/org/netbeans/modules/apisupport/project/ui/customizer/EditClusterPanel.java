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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.awt.Dialog;
import java.io.File;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizerJavadoc;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizerSources;
import org.netbeans.modules.apisupport.project.universe.JavadocRootsSupport;
import org.netbeans.modules.apisupport.project.universe.SourceRootsSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Richard Michalsky
 */
public final class EditClusterPanel extends javax.swing.JPanel implements DocumentListener {

    private NbPlatformCustomizerSources sourcesPanel;
    private NbPlatformCustomizerJavadoc javadocPanel;
    private JButton okButton;

    /**
     * Displays Add Cluster dialog and lets user select path to external cluster.
     *
     * @param prj Project into which the path will be stored. Returned path is relative to the project dir if possible.
     * @return Info for newly added cluster or null if user cancelled the dialog.
     */
    static ClusterInfo showAddDialog(Project prj) {
        EditClusterPanel panel = new EditClusterPanel();
        panel.prjDir = FileUtil.toFile(prj.getProjectDirectory());
        panel.prj = prj;
        SourceRootsSupport srs = new SourceRootsSupport(new URL[0], null);
        panel.sourcesPanel.setSourceRootsProvider(srs);
        JavadocRootsSupport jrs = new JavadocRootsSupport(new URL[0], null);
        panel.javadocPanel.setJavadocRootsProvider(jrs);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(EditClusterPanel.class, "CTL_AddCluster_Title"), // NOI18N
                true,
                new Object[] { panel.okButton, NotifyDescriptor.CANCEL_OPTION },
                panel.okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.EditClusterPanel"),
                null);
        descriptor.setClosingOptions(null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        panel.updateDialog();
        dlg.setVisible(true);
        ClusterInfo retVal = null;
        if (descriptor.getValue() == panel.okButton) {
            retVal = ClusterInfo.createExternal(panel.getAbsoluteClusterPath(),
                    srs.getSourceRoots(), jrs.getJavadocRoots(), true);

        }
        dlg.dispose();
        return retVal;
    }

    /**
     * Shows Edit Cluster dialog for existing external cluster.
     * Browse button is disabled, user can only change src and javadoc.
     *
     * @param ci Original cluster info 
     * @return Updated cluster info or null if user cancelled the dialog
     */
    static ClusterInfo showEditDialog(ClusterInfo ci, Project prj) {
        EditClusterPanel panel = new EditClusterPanel();
        panel.prjDir = FileUtil.toFile(prj.getProjectDirectory());
        panel.prj = prj;
        SourceRootsSupport srs = new SourceRootsSupport(
                ci.getSourceRoots() == null ? new URL[0] : ci.getSourceRoots(), null);
        panel.sourcesPanel.setSourceRootsProvider(srs);
        JavadocRootsSupport jrs = new JavadocRootsSupport(
                ci.getJavadocRoots() == null ? new URL[0] : ci.getJavadocRoots(), null);
        panel.javadocPanel.setJavadocRootsProvider(jrs);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(EditClusterPanel.class, "CTL_EditCluster_Title"), // NOI18N
                true,
                new Object[] { panel.okButton, NotifyDescriptor.CANCEL_OPTION },
                panel.okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.EditClusterPanel"),
                null);
        descriptor.setClosingOptions(null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        panel.clusterDirText.setText(ci.getClusterDir().toString());
        panel.updateDialog();
        panel.browseButton.setEnabled(false);
        dlg.setVisible(true);
        ClusterInfo retVal = null;
        if (descriptor.getValue() == panel.okButton) {
            retVal = ClusterInfo.createExternal(panel.getAbsoluteClusterPath(), 
                    srs.getSourceRoots(), jrs.getJavadocRoots(), true);
        }
        dlg.dispose();
        return retVal;
    }
    private File prjDir;
    private Project prj;

    public EditClusterPanel() {
        initComponents();
        sourcesPanel = new NbPlatformCustomizerSources();
        sourcesPanelContainer.add(sourcesPanel);
        javadocPanel = new NbPlatformCustomizerJavadoc();
        javadocPanelContainer.add(javadocPanel);
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton,
            NbBundle.getMessage(EditClusterPanel.class, "CTL_OK"));
    }

    private File getAbsoluteClusterPath() {
        String maybeRelPath = clusterDirText.getText();
        return PropertyUtils.resolveFile(prjDir, maybeRelPath);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPanel = new javax.swing.JTabbedPane();
        mainPanel = new javax.swing.JPanel();
        browseButton = new javax.swing.JButton();
        clusterDir = new javax.swing.JLabel();
        clusterDirText = new javax.swing.JTextField();
        sourcesPanelContainer = new javax.swing.JPanel();
        javadocPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        clusterDir.setLabelFor(clusterDirText);
        org.openide.awt.Mnemonics.setLocalizedText(clusterDir, org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.clusterDir.text")); // NOI18N

        clusterDirText.setEditable(false);
        clusterDirText.setText(org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "MSG_BrowseForCluster")); // NOI18N
        clusterDirText.getDocument().addDocumentListener(this);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clusterDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clusterDirText, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clusterDir)
                    .addComponent(clusterDirText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap(470, Short.MAX_VALUE))
        );

        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        clusterDirText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.clusterDirText.AccessibleContext.accessibleDescription")); // NOI18N

        tabbedPanel.addTab(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.mainPanel.TabConstraints.tabTitle"), mainPanel); // NOI18N

        sourcesPanelContainer.setLayout(new java.awt.BorderLayout());
        tabbedPanel.addTab(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.sourcesPanelContainer.TabConstraints.tabTitle"), sourcesPanelContainer); // NOI18N

        javadocPanelContainer.setLayout(new java.awt.BorderLayout());
        tabbedPanel.addTab(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.javadocPanelContainer.TabConstraints.tabTitle"), javadocPanelContainer); // NOI18N

        add(tabbedPanel, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser(ModuleUISettings.getDefault().getLastUsedClusterLocation());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            AGAIN: for (;;) {
                if (! file.exists() || file.isFile() || ! ClusterUtils.isValidCluster(file)) {
                    if (Clusterize.clusterize(prj, file)) {
                        continue AGAIN;
                    }
                } else {
                    ModuleUISettings.getDefault().setLastUsedClusterLocation(file.getParentFile().getAbsolutePath());
                    String relPath = PropertyUtils.relativizeFile(prjDir, file);
                    clusterDirText.setText(relPath != null ? relPath : file.getAbsolutePath());
                }
                break;
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    public @Override void changedUpdate(DocumentEvent e) {
        updateDialog();
    }

    public @Override void insertUpdate(DocumentEvent e) {
        updateDialog();
    }

    public @Override void removeUpdate(DocumentEvent e) {
        updateDialog();
    }

    private void updateDialog() {
        okButton.setEnabled((getAbsoluteClusterPath()).exists());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel clusterDir;
    private javax.swing.JTextField clusterDirText;
    private javax.swing.JPanel javadocPanelContainer;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel sourcesPanelContainer;
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables


}
