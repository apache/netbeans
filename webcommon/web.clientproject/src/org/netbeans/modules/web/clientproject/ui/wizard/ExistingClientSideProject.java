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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.api.util.ValidationUtilities;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Project location for existing project.
 */
public class ExistingClientSideProject extends JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("EDT")
    boolean fireChanges = true;
    // @GuardedBy("EDT")
    String lastSiteRoot = ""; // NOI18N
    volatile String testDir = null;


    public ExistingClientSideProject() {
        initComponents();
        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        // site root
        siteRootTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectName();
                updateProjectDirectory();
                lastSiteRoot = getSiteRoot();
                detectClientSideProject(lastSiteRoot);
                fireChanges = true;
            }
        }));
        // sources
        sourcesTextField.getDocument().addDocumentListener(defaultDocumentListener);
        // project name
        projectNameTextField.getDocument().addDocumentListener(defaultDocumentListener);
        // project dir
        projectDirectoryTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public String getSiteRoot() {
        return siteRootTextField.getText().trim();
    }

    public String getSources() {
        return sourcesTextField.getText().trim();
    }

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getProjectDirectory() {
        return projectDirectoryTextField.getText().trim();
    }

    public String getTestDir() {
        return testDir;
    }

    public final void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public final void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @NbBundle.Messages({
        "ExistingClientSideProject.siteRoot.label=Site Root",
        "ExistingClientSideProject.sources.label=Source folder",
        "ExistingClientSideProject.error.noFolder=Site Root or/and Source folder must be selected.",
    })
    public String getErrorMessage() {
        String siteRoot = getSiteRoot();
        String sources = getSources();
        if (!StringUtilities.hasText(siteRoot)
                && !StringUtilities.hasText(sources)) {
            return Bundle.ExistingClientSideProject_error_noFolder();
        }
        String error = validateFolder(siteRoot, Bundle.ExistingClientSideProject_siteRoot_label());
        if (error != null) {
            return error;
        }
        error = validateFolder(sources, Bundle.ExistingClientSideProject_sources_label());
        if (error != null) {
            return error;
        }
        error = validateProjectName();
        if (error != null) {
            return error;
        }
        error = validateProjectDirectory();
        if (error != null) {
            return error;
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - folder name",
        "ExistingClientSideProject.error.folder.invalid={0} is not a valid path.",
        "# {0} - folder name",
        "ExistingClientSideProject.error.folder.nbproject={0} is already NetBeans project (maybe only in memory).",
    })
    private String validateFolder(String folder, String folderName) {
        if (!StringUtilities.hasText(folder)) {
            return null;
        }
        File folderDir = FileUtil.normalizeFile(new File(folder).getAbsoluteFile());
        if (!folderDir.isDirectory()) {
            return Bundle.ExistingClientSideProject_error_folder_invalid(folderName);
        } else if (ClientSideProjectUtilities.isProject(folderDir)) {
            return Bundle.ExistingClientSideProject_error_folder_nbproject(folderName);
        }
        return null;
    }

    @NbBundle.Messages("ExistingClientSideProject.error.name.empty=Project name must be provided.")
    private String validateProjectName() {
        String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_name_empty();
        }
        return null;
    }

    @NbBundle.Messages({
        "ExistingClientSideProject.error.projectDirectory.invalid=Project directory is not a valid path.",
        "ExistingClientSideProject.error.projectDirectory.empty=Project directory must be selected.",
        "ExistingClientSideProject.error.projectDirectory.alreadyProject=Project directory is already NetBeans project (maybe only in memory).",
        "ExistingClientSideProject.error.projectDirectory.notWritable=Project directory cannot be created."
    })
    private String validateProjectDirectory() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_projectDirectory_empty();
        }
        File projDir = FileUtil.normalizeFile(new File(projectDirectory).getAbsoluteFile());
        if (ClientSideProjectUtilities.isProject(projDir)) {
            return Bundle.ExistingClientSideProject_error_projectDirectory_alreadyProject();
        }
        if (!projDir.isDirectory()) {
            // not existing directory
            if (!ValidationUtilities.isValidFilename(projDir)) {
                return Bundle.ExistingClientSideProject_error_projectDirectory_invalid();
            }
            File existingParent = projDir;
            while (existingParent != null && !existingParent.exists()) {
                existingParent = existingParent.getParentFile();
            }
            if (existingParent == null || !existingParent.canWrite()) {
                return Bundle.ExistingClientSideProject_error_projectDirectory_notWritable();
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void updateProjectName() {
        projectNameTextField.setText(new File(getSiteRoot()).getName());
    }

    void updateProjectDirectory() {
        assert EventQueue.isDispatchThread();
        String projectDirectory = getProjectDirectory();
        if (!lastSiteRoot.isEmpty() && projectDirectory.equals(lastSiteRoot)) {
            // project directory is site root => do nothing
            return;
        }
        projectDirectoryTextField.setText(getSiteRoot());
    }

    void detectClientSideProject(String folder) {
        ClientSideProjectDetector detector = new ClientSideProjectDetector(new File(folder));
        if (detector.detected()) {
            projectNameTextField.setText(detector.getName());
            projectDirectoryTextField.setText(detector.getProjectDirPath());
            testDir = detector.getTestDirPath();
        } else {
            resetDetectedValues();
        }
    }

    void resetDetectedValues() {
        testDir = null;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        siteRootLabel = new JLabel();
        siteRootTextField = new JTextField();
        siteRootBrowseButton = new JButton();
        sourcesLabel = new JLabel();
        sourcesTextField = new JTextField();
        sourcesBrowseButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectDirectoryLabel = new JLabel();
        projectDirectoryTextField = new JTextField();
        projectDirectoryBrowseButton = new JButton();

        siteRootLabel.setLabelFor(siteRootTextField);
        Mnemonics.setLocalizedText(siteRootLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.siteRootLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(siteRootBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.siteRootBrowseButton.text")); // NOI18N
        siteRootBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                siteRootBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sourcesLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.sourcesLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(sourcesBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.sourcesBrowseButton.text")); // NOI18N
        sourcesBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sourcesBrowseButtonActionPerformed(evt);
            }
        });

        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectNameLabel.text")); // NOI18N

        projectDirectoryLabel.setLabelFor(projectDirectoryTextField);
        Mnemonics.setLocalizedText(projectDirectoryLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectDirectoryLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(projectDirectoryBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectDirectoryBrowseButton.text")); // NOI18N
        projectDirectoryBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectDirectoryBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(projectDirectoryLabel)
                    .addComponent(projectNameLabel)
                    .addComponent(siteRootLabel)
                    .addComponent(sourcesLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(projectNameTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(sourcesTextField)
                            .addComponent(siteRootTextField, Alignment.LEADING)
                            .addComponent(projectDirectoryTextField, Alignment.LEADING))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(siteRootBrowseButton, Alignment.TRAILING)
                            .addComponent(projectDirectoryBrowseButton, Alignment.TRAILING)
                            .addComponent(sourcesBrowseButton, Alignment.TRAILING)))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {projectDirectoryBrowseButton, siteRootBrowseButton, sourcesBrowseButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(siteRootTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(siteRootBrowseButton)
                    .addComponent(siteRootLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(sourcesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourcesLabel)
                    .addComponent(sourcesBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(projectNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectNameLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(projectDirectoryLabel)
                    .addComponent(projectDirectoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectDirectoryBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({
        "ExistingClientSideProject.siteRoot.dialog.title=Select Site Root",
    })
    private void siteRootBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_siteRootBrowseButtonActionPerformed
        File folder = browseFile(".siteRoot", Bundle.ExistingClientSideProject_siteRoot_dialog_title(), //NOI18N
                getSiteRoot());
        if (folder != null) {
            siteRootTextField.setText(FileUtil.normalizeFile(folder).getAbsolutePath());
        }
    }//GEN-LAST:event_siteRootBrowseButtonActionPerformed

    @NbBundle.Messages("ExistingClientSideProject.projectDirectory.dialog.title=Select Project Directory")
    private void projectDirectoryBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectDirectoryBrowseButtonActionPerformed
        File projectDirectory = browseFile(".projectDirectory", Bundle.ExistingClientSideProject_projectDirectory_dialog_title(), // NOI18N
                getProjectDirectory());
        if (projectDirectory != null) {
            projectDirectoryTextField.setText(FileUtil.normalizeFile(projectDirectory).getAbsolutePath());
        }
    }//GEN-LAST:event_projectDirectoryBrowseButtonActionPerformed

    @NbBundle.Messages({
        "ExistingClientSideProject.sources.dialog.title=Select Source Folder",
    })
    private void sourcesBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sourcesBrowseButtonActionPerformed
        File folder = browseFile(".sources", Bundle.ExistingClientSideProject_sources_dialog_title(), //NOI18N
                getSources());
        if (folder != null) {
            sourcesTextField.setText(FileUtil.normalizeFile(folder).getAbsolutePath());
        }
    }//GEN-LAST:event_sourcesBrowseButtonActionPerformed

    private File browseFile(String dirKey, String title, String currentDirectory) {
        File workDir = null;
        if (currentDirectory != null && !currentDirectory.isEmpty()) {
            File currDir = new File(currentDirectory);
            if (currDir.isDirectory()) {
                workDir = currDir;
            }
        }
        FileChooserBuilder builder = new FileChooserBuilder(ExistingClientSideProject.class.getName() + dirKey)
                .setTitle(title)
                .setDirectoriesOnly(true);
        if (workDir != null) {
            builder.setDefaultWorkingDirectory(workDir)
                    .forceUseOfDefaultWorkingDirectory(true);
        }
        return builder.showOpenDialog();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton projectDirectoryBrowseButton;
    private JLabel projectDirectoryLabel;
    private JTextField projectDirectoryTextField;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;
    private JButton siteRootBrowseButton;
    private JLabel siteRootLabel;
    private JTextField siteRootTextField;
    private JButton sourcesBrowseButton;
    private JLabel sourcesLabel;
    private JTextField sourcesTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        private final Runnable task;


        public DefaultDocumentListener() {
            this(null);
        }

        public DefaultDocumentListener(Runnable task) {
            this.task = task;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            assert EventQueue.isDispatchThread();
            if (task != null) {
                task.run();
            }
            if (fireChanges) {
                fireChange();
            }
        }

    }

}
