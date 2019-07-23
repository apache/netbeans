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
package org.netbeans.modules.html.ojet.ui.wizard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class NewJetModuleWizardPanelUi extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(NewJetModuleWizardPanelUi.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile String name;
    private volatile String jsFolder;
    private volatile String htmlFolder;
    private volatile String createdJsFile;
    private volatile String createdHtmlFile;

    // @GuardedBy("EDT")
    @NullAllowed
    private Project project;


    @NbBundle.Messages("NewJetModuleWizardPanelUi.name=Name and Location")
    NewJetModuleWizardPanelUi() {
        assert EventQueue.isDispatchThread();
        initComponents();
        init();
    }

    private void init() {
        setName(Bundle.NewJetModuleWizardPanelUi_name());
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        nameTextField.getDocument().addDocumentListener(defaultDocumentListener);
        jsFolderTextField.getDocument().addDocumentListener(defaultDocumentListener);
        htmlFolderTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    String getFileName() {
        return name;
    }

    void setFileName(String fileName) {
        assert EventQueue.isDispatchThread();
        assert fileName != null;
        nameTextField.setText(fileName);
    }

    boolean hasProject() {
        assert EventQueue.isDispatchThread();
        return project != null;
    }

    void setProject(Project project) {
        assert EventQueue.isDispatchThread();
        this.project = project;
        setProjectName();
    }

    String getJsFolder() {
        return jsFolder;
    }

    void setJsFolder(String folder) {
        assert EventQueue.isDispatchThread();
        assert folder != null;
        jsFolderTextField.setText(folder);
    }

    String getHtmlFolder() {
        return htmlFolder;
    }

    void setHtmlFolder(String folder) {
        assert EventQueue.isDispatchThread();
        assert folder != null;
        htmlFolderTextField.setText(folder);
    }

    @CheckForNull
    public String getCreatedJsFile() {
        return createdJsFile;
    }

    @CheckForNull
    public String getCreatedHtmlFile() {
        return createdHtmlFile;
    }

    @NbBundle.Messages("NewJetModuleWizardPanelUi.project.none=<no project>")
    private void setProjectName() {
        assert EventQueue.isDispatchThread();
        String projectName;
        if (project != null) {
            projectName = ProjectUtils.getInformation(project).getDisplayName();
        } else {
            projectName = Bundle.NewJetModuleWizardPanelUi_project_none();
        }
        projectTextField.setText(projectName);
    }

    void updateCreatedFiles() {
        assert EventQueue.isDispatchThread();
        if (project == null
                || (name == null || name.trim().isEmpty())) {
            createdJsFile = null;
            createdHtmlFile = null;
            createdJsFileTextField.setText(""); // NOI18N
            createdHtmlFileTextField.setText(""); // NOI18N
            return;
        }
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        String jsPart = jsFolder + "/" + name + ".js"; // NOI18N
        String htmlPart = htmlFolder + "/" + name + ".html"; // NOI18N
        createdJsFile = new File(projectDir, jsPart.replace('/', File.separatorChar)).getAbsolutePath(); // NOI18N
        createdHtmlFile = new File(projectDir, htmlPart.replace('/', File.separatorChar)).getAbsolutePath(); // NOI18N
        createdJsFileTextField.setText(createdJsFile);
        createdHtmlFileTextField.setText(createdHtmlFile);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @NbBundle.Messages("NewJetModuleWizardPanelUi.chooser.folder.title=Select folder")
    private void selectFolder(JTextField component) {
        if (project == null) {
            LOGGER.log(Level.INFO, "Project is required");
            return;
        }
        FileObject projectDirectory = project.getProjectDirectory();
        File selectedDir = new FileChooserBuilder(NewJetModuleWizardPanelUi.class)
                .setTitle(Bundle.NewJetModuleWizardPanelUi_chooser_folder_title())
                .setDefaultWorkingDirectory(FileUtil.toFile(projectDirectory))
                .forceUseOfDefaultWorkingDirectory(true)
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .showOpenDialog();
        if (selectedDir == null) {
            return;
        }
        FileObject newDir = FileUtil.toFileObject(selectedDir);
        assert newDir != null : selectedDir;
        String relativePath = FileUtil.getRelativePath(projectDirectory, newDir);
        if (relativePath == null) {
            // should not happen normally
            component.setText(selectedDir.getAbsolutePath());
        } else {
            component.setText(relativePath);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new JLabel();
        nameTextField = new JTextField();
        projectLabel = new JLabel();
        projectTextField = new JTextField();
        jsFolderLabel = new JLabel();
        jsFolderTextField = new JTextField();
        jsFolderBrowseButton = new JButton();
        htmlFolderLabel = new JLabel();
        htmlFolderTextField = new JTextField();
        htmlFolderBrowseButton = new JButton();
        createdJsFileLabel = new JLabel();
        createdJsFileTextField = new JTextField();
        createdHtmlFileLabel = new JLabel();
        createdHtmlFileTextField = new JTextField();

        nameLabel.setLabelFor(nameTextField);
        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.nameLabel.text")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        Mnemonics.setLocalizedText(projectLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.projectLabel.text")); // NOI18N

        projectTextField.setEditable(false);

        jsFolderLabel.setLabelFor(jsFolderTextField);
        Mnemonics.setLocalizedText(jsFolderLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.jsFolderLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(jsFolderBrowseButton, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.jsFolderBrowseButton.text")); // NOI18N
        jsFolderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jsFolderBrowseButtonActionPerformed(evt);
            }
        });

        htmlFolderLabel.setLabelFor(htmlFolderTextField);
        Mnemonics.setLocalizedText(htmlFolderLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.htmlFolderLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(htmlFolderBrowseButton, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.htmlFolderBrowseButton.text")); // NOI18N
        htmlFolderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                htmlFolderBrowseButtonActionPerformed(evt);
            }
        });

        createdJsFileLabel.setLabelFor(createdJsFileTextField);
        Mnemonics.setLocalizedText(createdJsFileLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.createdJsFileLabel.text")); // NOI18N

        createdJsFileTextField.setEditable(false);

        createdHtmlFileLabel.setLabelFor(createdHtmlFileTextField);
        Mnemonics.setLocalizedText(createdHtmlFileLabel, NbBundle.getMessage(NewJetModuleWizardPanelUi.class, "NewJetModuleWizardPanelUi.createdHtmlFileLabel.text")); // NOI18N

        createdHtmlFileTextField.setEditable(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(htmlFolderLabel)
                    .addComponent(jsFolderLabel)
                    .addComponent(projectLabel)
                    .addComponent(nameLabel)
                    .addComponent(createdJsFileLabel)
                    .addComponent(createdHtmlFileLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(htmlFolderTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(htmlFolderBrowseButton))
                    .addComponent(createdHtmlFileTextField)
                    .addComponent(createdJsFileTextField)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(projectTextField)
                            .addComponent(jsFolderTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jsFolderBrowseButton))
                    .addComponent(nameTextField, GroupLayout.Alignment.TRAILING)))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jsFolderLabel)
                    .addComponent(jsFolderBrowseButton)
                    .addComponent(jsFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(htmlFolderLabel)
                    .addComponent(htmlFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(htmlFolderBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createdJsFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdJsFileLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createdHtmlFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdHtmlFileLabel)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jsFolderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jsFolderBrowseButtonActionPerformed
        selectFolder(jsFolderTextField);
    }//GEN-LAST:event_jsFolderBrowseButtonActionPerformed

    private void htmlFolderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_htmlFolderBrowseButtonActionPerformed
        selectFolder(htmlFolderTextField);
    }//GEN-LAST:event_htmlFolderBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel createdHtmlFileLabel;
    private JTextField createdHtmlFileTextField;
    private JLabel createdJsFileLabel;
    private JTextField createdJsFileTextField;
    private JButton htmlFolderBrowseButton;
    private JLabel htmlFolderLabel;
    private JTextField htmlFolderTextField;
    private JButton jsFolderBrowseButton;
    private JLabel jsFolderLabel;
    private JTextField jsFolderTextField;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel projectLabel;
    private JTextField projectTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

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
            name = nameTextField.getText();
            jsFolder = jsFolderTextField.getText();
            htmlFolder = htmlFolderTextField.getText();
            updateCreatedFiles();
            fireChange();
        }

    }

}
