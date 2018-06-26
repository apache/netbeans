/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.wizard;

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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.api.util.ValidationUtilities;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Project location for existing project.
 */
public class ExistingProjectVisual extends JPanel {

    private static final String TEST_DIR_NAME = "test"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("EDT")
    boolean fireChanges = true;
    // @GuardedBy("EDT")
    String lastSources = ""; // NOI18N
    // @GuardedBy("EDT")
    String lastProjectName = ""; // NOI18N


    public ExistingProjectVisual() {
        initComponents();
        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        // source root
        sourcesTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectName();
                updateProjectDirectory();
                lastSources = getSources();
                fireChanges = true;
            }
        }));
        // site root
        siteRootTextField.getDocument().addDocumentListener(defaultDocumentListener);
        // project name
        projectNameTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectDirectoryName();
                lastProjectName = getProjectName();
                fireChanges = true;
            }
        }));
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

    @CheckForNull
    public String getTestDir() {
        String projectDirectory = getProjectDirectory();
        if (!StringUtilities.hasText(projectDirectory)) {
            return null;
        }
        if (new File(projectDirectory, TEST_DIR_NAME).isDirectory()) {
            return TEST_DIR_NAME;
        }
        return null;
    }

    public final void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public final void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @NbBundle.Messages({
        "ExistingProjectVisual.siteRoot.label=Site Root",
        "ExistingProjectVisual.sources.label=Source folder",
        "ExistingProjectVisual.error.noSources=Source folder must be selected.",
    })
    public String getErrorMessage() {
        String sources = getSources();
        if (!StringUtilities.hasText(sources)) {
            return Bundle.ExistingProjectVisual_error_noSources();
        }
        String siteRoot = getSiteRoot();
        String error = validateFolder(siteRoot, Bundle.ExistingProjectVisual_siteRoot_label());
        if (error != null) {
            return error;
        }
        error = validateFolder(sources, Bundle.ExistingProjectVisual_sources_label());
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
        "ExistingProjectVisual.error.folder.invalid={0} is not a valid path.",
        "# {0} - folder name",
        "ExistingProjectVisual.error.folder.nbproject={0} is already NetBeans project (maybe only in memory).",
    })
    private String validateFolder(String folder, String folderName) {
        if (!StringUtilities.hasText(folder)) {
            return null;
        }
        File folderDir = FileUtil.normalizeFile(new File(folder).getAbsoluteFile());
        if (!folderDir.isDirectory()) {
            return Bundle.ExistingProjectVisual_error_folder_invalid(folderName);
        } else if (NodeJsUtils.isProject(folderDir)) {
            return Bundle.ExistingProjectVisual_error_folder_nbproject(folderName);
        }
        return null;
    }

    @NbBundle.Messages("ExistingProjectVisual.error.name.empty=Project name must be provided.")
    private String validateProjectName() {
        String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return Bundle.ExistingProjectVisual_error_name_empty();
        }
        return null;
    }

    @NbBundle.Messages({
        "ExistingProjectVisual.error.projectDirectory.invalid=Project directory is not a valid path.",
        "ExistingProjectVisual.error.projectDirectory.empty=Project directory must be selected.",
        "ExistingProjectVisual.error.projectDirectory.alreadyProject=Project directory is already NetBeans project (maybe only in memory).",
        "ExistingProjectVisual.error.projectDirectory.notWritable=Project directory cannot be created."
    })
    private String validateProjectDirectory() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.isEmpty()) {
            return Bundle.ExistingProjectVisual_error_projectDirectory_empty();
        }
        File projDir = FileUtil.normalizeFile(new File(projectDirectory).getAbsoluteFile());
        if (NodeJsUtils.isProject(projDir)) {
            return Bundle.ExistingProjectVisual_error_projectDirectory_alreadyProject();
        }
        if (!projDir.isDirectory()) {
            // not existing directory
            if (!ValidationUtilities.isValidFilename(projDir)) {
                return Bundle.ExistingProjectVisual_error_projectDirectory_invalid();
            }
            File existingParent = projDir;
            while (existingParent != null && !existingParent.exists()) {
                existingParent = existingParent.getParentFile();
            }
            if (existingParent == null || !existingParent.canWrite()) {
                return Bundle.ExistingProjectVisual_error_projectDirectory_notWritable();
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void updateProjectName() {
        projectNameTextField.setText(new File(getSources()).getName());
    }

    void updateProjectDirectory() {
        assert EventQueue.isDispatchThread();
        String projectDirectory = getProjectDirectory();
        if (!lastSources.isEmpty() && projectDirectory.equals(lastSources)) {
            // project directory is source root => do nothing
            return;
        }
        projectDirectoryTextField.setText(getSources());
    }

    void updateProjectDirectoryName() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.equals(getSources())) {
            // project directory is source root => do nothing
            return;
        }
        if (!lastProjectName.isEmpty()
                && !projectDirectory.equals(lastProjectName)
                && projectDirectory.endsWith(lastProjectName)) {
            // yes, project directory follows project name
            String newProjDir = projectDirectory.substring(0, projectDirectory.length() - lastProjectName.length()) + getProjectName();
            projectDirectoryTextField.setText(newProjDir);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourcesLabel = new JLabel();
        sourcesTextField = new JTextField();
        sourcesBrowseButton = new JButton();
        siteRootLabel = new JLabel();
        siteRootTextField = new JTextField();
        siteRootBrowseButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectDirectoryLabel = new JLabel();
        projectDirectoryTextField = new JTextField();
        projectDirectoryBrowseButton = new JButton();

        Mnemonics.setLocalizedText(sourcesLabel, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.sourcesLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(sourcesBrowseButton, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.sourcesBrowseButton.text")); // NOI18N
        sourcesBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sourcesBrowseButtonActionPerformed(evt);
            }
        });

        siteRootLabel.setLabelFor(siteRootTextField);
        Mnemonics.setLocalizedText(siteRootLabel, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.siteRootLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(siteRootBrowseButton, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.siteRootBrowseButton.text")); // NOI18N
        siteRootBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                siteRootBrowseButtonActionPerformed(evt);
            }
        });

        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.projectNameLabel.text")); // NOI18N

        projectDirectoryLabel.setLabelFor(projectDirectoryTextField);
        Mnemonics.setLocalizedText(projectDirectoryLabel, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.projectDirectoryLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(projectDirectoryBrowseButton, NbBundle.getMessage(ExistingProjectVisual.class, "ExistingProjectVisual.projectDirectoryBrowseButton.text")); // NOI18N
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
                    .addComponent(projectNameLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(projectNameTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(projectDirectoryTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(projectDirectoryBrowseButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(siteRootLabel)
                    .addComponent(sourcesLabel))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(sourcesTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(sourcesBrowseButton))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(siteRootTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(siteRootBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {projectDirectoryBrowseButton, siteRootBrowseButton, sourcesBrowseButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(sourcesBrowseButton)
                    .addComponent(sourcesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourcesLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(siteRootBrowseButton)
                    .addComponent(siteRootTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(siteRootLabel))
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
        "ExistingProjectVisual.siteRoot.dialog.title=Select Site Root",
    })
    private void siteRootBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_siteRootBrowseButtonActionPerformed
        File folder = browseFile(".siteRoot", Bundle.ExistingProjectVisual_siteRoot_dialog_title(), //NOI18N
                getSiteRoot());
        if (folder != null) {
            siteRootTextField.setText(FileUtil.normalizeFile(folder).getAbsolutePath());
        }
    }//GEN-LAST:event_siteRootBrowseButtonActionPerformed

    @NbBundle.Messages("ExistingProjectVisual.projectDirectory.dialog.title=Select Project Directory")
    private void projectDirectoryBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectDirectoryBrowseButtonActionPerformed
        File projectDirectory = browseFile(".projectDirectory", Bundle.ExistingProjectVisual_projectDirectory_dialog_title(), // NOI18N
                getProjectDirectory());
        if (projectDirectory != null) {
            projectDirectoryTextField.setText(FileUtil.normalizeFile(projectDirectory).getAbsolutePath());
        }
    }//GEN-LAST:event_projectDirectoryBrowseButtonActionPerformed

    @NbBundle.Messages({
        "ExistingProjectVisual.sources.dialog.title=Select Source Folder",
    })
    private void sourcesBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sourcesBrowseButtonActionPerformed
        File folder = browseFile(".sources", Bundle.ExistingProjectVisual_sources_dialog_title(), //NOI18N
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
        FileChooserBuilder builder = new FileChooserBuilder(ExistingProjectVisual.class.getName() + dirKey)
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
