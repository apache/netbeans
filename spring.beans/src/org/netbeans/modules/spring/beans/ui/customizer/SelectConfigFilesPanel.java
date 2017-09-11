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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFilesUIs.ConfigFileSelectionTableModel;
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFilesUIs.FileDisplayName;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Andrei Badea
 */
public class SelectConfigFilesPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;

    private final RequestProcessor rp = new RequestProcessor("Spring config file detection thread", 1, true); // NOI18N
    private final Set<File> alreadySelectedFiles;
    private final Project project;

    private List<File> availableFiles;
    private DialogDescriptor descriptor;
    private Task detectTask;

    /**
     * Creates a new instance of the panel for a project and a set of already selected
     * files. The panel will run a background task to detect any config files in the given project.
     */
    public static SelectConfigFilesPanel create(Project project, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        return new SelectConfigFilesPanel(project, alreadySelectedFiles, fileDisplayName);
    }

    /**
     * Creates a new instance of the panel for a set of available and already selected
     * files. Since the available files are known, no config files detection
     * task will be run.
     */
    public static SelectConfigFilesPanel create(List<File> availableFiles, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        return new SelectConfigFilesPanel(availableFiles, alreadySelectedFiles, fileDisplayName);
    }

    private SelectConfigFilesPanel(List<File> availableFiles, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        this.alreadySelectedFiles = alreadySelectedFiles;
        this.availableFiles = availableFiles;
        this.project = null;
        initComponents(fileDisplayName);
    }

    private SelectConfigFilesPanel(Project project, Set<File> alreadySelectedFiles, FileDisplayName fileDisplayName) {
        this.project = project;
        this.alreadySelectedFiles = alreadySelectedFiles;
        initComponents(fileDisplayName);
    }

    private void initComponents(FileDisplayName fileDisplayName) {
        initComponents();
        ConfigFilesUIs.setupFilesSelectionTable(configFileTable, fileDisplayName);
        configFileTable.getParent().setBackground(configFileTable.getBackground());
        configFileTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public boolean open() {
        String title = NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_ConfigFilesTitle");
        descriptor = new DialogDescriptor(this, title, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDetection();
            }
        });
        if (availableFiles == null) {
            // No available files, will run the detection task.
            descriptor.setValid(false);
            configFileTable.setEnabled(true);
            progressBar.setIndeterminate(true);
            detectTask = rp.create(new FileDetector());
            detectTask.schedule(0);
        } else {
            updateAvailableFiles(availableFiles);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    public List<File> getAvailableFiles() {
        return availableFiles;
    }

    public List<File> getSelectedFiles() {
        return ConfigFilesUIs.getSelectedFiles(configFileTable);
    }

    public List<File> getSelectableFiles() {
        return ConfigFilesUIs.getSelectableFiles(configFileTable);
    }

    private void cancelDetection() {
        if (detectTask != null) {
            detectTask.cancel();
        }
    }
    private void updateSelectAllNonButtons() {
        final int maxSize = getSelectableFiles().size();
        final int size = getSelectedFiles().size();

        checkAllButton.setEnabled(maxSize > 0 && size < maxSize);
        uncheckAllButton.setEnabled(maxSize > 0 && size > 0);
    }

    private void updateAvailableFiles(List<File> availableFiles) {
        this.availableFiles = availableFiles;
        configFileTable.setEnabled(true);
        ConfigFilesUIs.connectFilesSelectionTable(availableFiles, alreadySelectedFiles, configFileTable);
        ConfigFilesUIs.setCheckBoxListener(configFileTable, new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSelectAllNonButtons();
            }
        });
        configFileTable.getColumnModel().getColumn(0).setMaxWidth(0);
        // In an attempt to hide the progress bar and label, but force
        // the occupy the same space.
        String message = (availableFiles.isEmpty()) ? NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_NoFilesFound") : " "; // NOI18N
        messageLabel.setText(message); // NOI18N
        progressBar.setIndeterminate(false);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(getBackground());
        descriptor.setValid(true);
        updateSelectAllNonButtons();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detectedFilesLabel = new javax.swing.JLabel();
        configFileScrollPane = new javax.swing.JScrollPane();
        configFileTable = new javax.swing.JTable();
        progressBar = new javax.swing.JProgressBar();
        messageLabel = new javax.swing.JLabel();
        checkAllButton = new javax.swing.JButton();
        uncheckAllButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(detectedFilesLabel, org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_ConfigFiles")); // NOI18N

        configFileTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        configFileTable.setShowHorizontalLines(false);
        configFileTable.setShowVerticalLines(false);
        configFileTable.setTableHeader(null);
        configFileScrollPane.setViewportView(configFileTable);

        progressBar.setString(" "); // NOI18N
        progressBar.setStringPainted(true);

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_PleaseWait")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkAllButton, org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnCheckAll.text")); // NOI18N
        checkAllButton.setActionCommand(org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnCheckAll.text")); // NOI18N
        checkAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(uncheckAllButton, org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnUncheckAll.text")); // NOI18N
        uncheckAllButton.setActionCommand(org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnUncheckAll.text")); // NOI18N
        uncheckAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uncheckAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configFileScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 704, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detectedFilesLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(uncheckAllButton))
                            .addComponent(messageLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(detectedFilesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configFileScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkAllButton)
                    .addComponent(uncheckAllButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        checkAllButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnCheckAll.accessibleText")); // NOI18N
        uncheckAllButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectConfigFilesPanel.class, "LBL_SelectConfigFilesPanel.btnUncheckAll.accessibleText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void uncheckAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uncheckAllButtonActionPerformed
        getTableModel().selectNone();
        configFileTable.repaint();
    }//GEN-LAST:event_uncheckAllButtonActionPerformed

    private void checkAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAllButtonActionPerformed
        getTableModel().selectAll();
        configFileTable.repaint();
    }//GEN-LAST:event_checkAllButtonActionPerformed

    private ConfigFileSelectionTableModel getTableModel() {
        return (ConfigFileSelectionTableModel) configFileTable.getModel();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkAllButton;
    private javax.swing.JScrollPane configFileScrollPane;
    private javax.swing.JTable configFileTable;
    private javax.swing.JLabel detectedFilesLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton uncheckAllButton;
    // End of variables declaration//GEN-END:variables

    private final class FileDetector implements Runnable {

        public void run() {
            final Set<File> result = new HashSet<File>();
            // Search in the source groups of the projects.
            for (SourceGroup group : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                for (FileObject fo : NbCollections.iterable(group.getRootFolder().getChildren(true))) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                        continue;
                    }
                    File file = FileUtil.toFile(fo);
                    if (file == null) {
                        continue;
                    }
                    result.add(file);
                }
            }
            // Search any providers of Spring config files registered in the project lookup.
            for (SpringConfigFileProvider provider : project.getLookup().lookupAll(SpringConfigFileProvider.class)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                result.addAll(provider.getConfigFiles());
            }
            final List<File> sorted = new ArrayList<File>(result.size());
            sorted.addAll(result);
            Collections.sort(sorted);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateAvailableFiles(sorted);
                }
            });
        }
    }
}
