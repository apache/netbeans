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

package org.netbeans.modules.maven.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.validation.adapters.DialogDescriptorAdapter;
import org.netbeans.api.validation.adapters.NotificationLineSupportAdapter;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import static org.netbeans.modules.maven.operations.Bundle.*;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.DialogDescriptor;
import org.openide.LifecycleManager;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class RenameProjectPanel extends javax.swing.JPanel {
    private final NbMavenProjectImpl project;
    private ValidationGroup vg;
    private NotificationLineSupport nls;

    @Messages({
        "NAME_Folder=Folder Name", 
        "NAME_Artifact=ArtifactId", 
        "# {0} - project display name",
        "RenameProjectPanel.lblRename.text2=Rename Project \"{0}\""})
    RenameProjectPanel(NbMavenProjectImpl prj) {
        initComponents();
        SwingValidationGroup.setComponentName(txtFolder, NAME_Folder());
        SwingValidationGroup.setComponentName(txtArtifactId, NAME_Artifact());
        this.project = prj;
        final String folder = project.getProjectDirectory().getNameExt();
        txtFolder.setText(folder);
        //load values..
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                MavenProject prj = project.getOriginalMavenProject();
                final String dn = prj.getName();
                final String artId = prj.getArtifactId();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        txtArtifactId.setText(artId);
                        txtDisplayName.setText(dn);
                        lblRename.setText(RenameProjectPanel_lblRename_text2(dn));
                    }
                });
            }
        });
        FileObject pomFO = project.getProjectDirectory().getFileObject("pom.xml");
        if(pomFO == null) {
            cbArtifactId.setEnabled(false);
            cbDisplayName.setEnabled(false);
            cbFolder.setEnabled(false);
            txtArtifactId.setEnabled(false);
            txtDisplayName.setEnabled(false);
            txtFolder.setEnabled(false);
        }
    }

    void createValidations(DialogDescriptor dd) {
        nls = dd.createNotificationLineSupport();
        vg = ValidationGroup.create(new NotificationLineSupportAdapter(nls), new DialogDescriptorAdapter(dd));
        vg.add(txtFolder,
                new OptionalValidator(cbFolder,
                    ValidatorUtils.merge(
                        StringValidators.REQUIRE_NON_EMPTY_STRING,
                        ValidatorUtils.merge(StringValidators.REQUIRE_VALID_FILENAME,
                        new FileNameExists(FileUtil.toFile(project.getProjectDirectory().getParent()))
                    )
                )));
        vg.add(txtArtifactId,
                new OptionalValidator(cbArtifactId,
                        MavenValidators.createArtifactIdValidators()
                ));
        checkEnablement();
    }


    private void checkEnablement() {
        txtArtifactId.setEnabled(cbArtifactId.isSelected());
        txtDisplayName.setEnabled(cbDisplayName.isSelected());
        txtFolder.setEnabled(cbFolder.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblRename = new javax.swing.JLabel();
        cbDisplayName = new javax.swing.JCheckBox();
        txtDisplayName = new javax.swing.JTextField();
        cbArtifactId = new javax.swing.JCheckBox();
        txtArtifactId = new javax.swing.JTextField();
        cbFolder = new javax.swing.JCheckBox();
        txtFolder = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(lblRename, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.lblRename.text")); // NOI18N

        cbDisplayName.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbDisplayName, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbDisplayName.text")); // NOI18N
        cbDisplayName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDisplayNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbArtifactId, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbArtifactId.text")); // NOI18N
        cbArtifactId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbArtifactIdActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbFolder, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbFolder.text")); // NOI18N
        cbFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblRename, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbArtifactId)
                            .addComponent(cbDisplayName)
                            .addComponent(cbFolder))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .addComponent(txtArtifactId, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .addComponent(txtFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRename)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbDisplayName)
                    .addComponent(txtDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbArtifactId)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbFolder)
                    .addComponent(txtFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbDisplayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDisplayNameActionPerformed
        vg.performValidation();
        checkEnablement();
    }//GEN-LAST:event_cbDisplayNameActionPerformed

    private void cbArtifactIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbArtifactIdActionPerformed
        vg.performValidation();
        checkEnablement();
    }//GEN-LAST:event_cbArtifactIdActionPerformed

    private void cbFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFolderActionPerformed
        vg.performValidation();
        checkEnablement();
    }//GEN-LAST:event_cbFolderActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbArtifactId;
    private javax.swing.JCheckBox cbDisplayName;
    private javax.swing.JCheckBox cbFolder;
    private javax.swing.JLabel lblRename;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JTextField txtFolder;
    // End of variables declaration//GEN-END:variables

    @Messages("RenameProject=Renaming Project")
    void renameProject() {
        final boolean artId = cbArtifactId.isSelected();
        final boolean dname = cbDisplayName.isSelected();
        final boolean folder = cbFolder.isSelected();
        final String newArtId = txtArtifactId.getText().trim();
        final String newDname = txtDisplayName.getText().trim();
        final String newFolder = txtFolder.getText().trim();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                List<ModelOperation<POMModel>> opers = new ArrayList<ModelOperation<POMModel>>();
                if (artId) {
                    opers.add(new ArtIdOperation(newArtId));
                }
                if (dname) {
                    opers.add(new DNameOperation(newDname));
                }
                FileObject pomFO = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                Utilities.performPOMModelOperations(pomFO, opers);
                if (folder) {
                    final ProgressHandle handle = ProgressHandle.createHandle(RenameProject());
                    //#76559
                    handle.start(MAX_WORK);
                    try {
                        checkParentProject(project, newFolder);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        doMoveProject(handle, project, newFolder, project.getProjectDirectory().getParent());
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        handle.finish();
                    }
                }
            }
        });
    }

    private static class ArtIdOperation implements ModelOperation<POMModel> {
        private final String artifactId;
        ArtIdOperation(String art) {
            artifactId = art;
        }
        @Override
        public void performOperation(POMModel model) {
            model.getProject().setArtifactId(artifactId);
        }
    }

    private static class DNameOperation implements ModelOperation<POMModel> {
        private final String name;
        DNameOperation(String nm) {
            name = nm;
        }
        @Override
        public void performOperation(POMModel model) {
            model.getProject().setName(name);
        }
    }

    private void checkParentProject(final NbMavenProjectImpl project, final String newName) throws IOException {
        Project[] prjs = OpenProjects.getDefault().getOpenProjects();
        for (Project prj : prjs) {
            if(prj.getProjectDirectory().equals(project.getProjectDirectory())) {
                continue;
            }
            final NbMavenProjectImpl parentProject = prj.getLookup().lookup(NbMavenProjectImpl.class);        
            if (parentProject != null) {
                List<String> modules = parentProject.getOriginalMavenProject().getModules();
                if(modules != null && !modules.isEmpty()) {
                    String oldName = project.getProjectDirectory().getNameExt();                
                    if(modules.contains(oldName)) {
                        rename(parentProject, oldName, newName);
                        return;
                    } 
                    File projectDir = project.getPOMFile().getParentFile();
                    File parentDir = parentProject.getPOMFile().getParentFile();
                    oldName = FileUtilities.relativizeFile(parentDir, projectDir);
                    if(modules.contains(oldName)) {
                        String relNewName = FileUtilities.relativizeFile(parentDir, new File(projectDir.getParent(), newName));
                        rename(parentProject, oldName, relNewName);                    
                    } 
                }
            }
        }                
    }
    
    private void rename(Project parentProject, final String oldName, final String newName) {
        FileObject pomFO = parentProject.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            @Override
            public void performOperation(POMModel model) {
                List<String> modules = model.getProject().getModules();
                if (modules != null && modules.contains(oldName)) {
                    //delete/add module from/to parent..
                    model.getProject().removeModule(oldName);
                    model.getProject().addModule(newName);
                }
            }
        };
        Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
    }

//--- copied from org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation

    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int    MAX_WORK = 100;

    // XXX copied from DefaultProjectOperationsImplementation
    private static void doMoveProject(ProgressHandle handle, Project project, String nueFolderName, FileObject newTarget) throws Exception {
        boolean originalOK = true;
        Project main    = OpenProjects.getDefault().getMainProject();
        boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
	FileObject target = null;

        try {

            int totalWork = MAX_WORK;
            double currentWorkDone = 0;

            handle.progress((int) currentWorkDone);

            close(project);

            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));

            FileObject projectDirectory = project.getProjectDirectory();
            NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
            File pomFile = impl.getPOMFile();

            double workPerFileAndOperation = totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK);

            FileLock lock = projectDirectory.lock();
            try {
                target = projectDirectory.move(lock, newTarget, nueFolderName, null);
            } finally {
                lock.releaseLock();
            }
            int lastWorkDone = (int) currentWorkDone;

            currentWorkDone += workPerFileAndOperation;

            if (lastWorkDone < (int) currentWorkDone) {
                handle.progress((int) currentWorkDone);
            }

            originalOK = false;

            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);
            //#243447 clear the cached model, however one still doesn't have any guarantee that the old project is not referenced and called from some place.
            //if it is, we will get unloadable project most likely and the #243447 scenario will occur again. Unfixable though with the current Project lifecycle.
            MavenProjectCache.clearMavenProject(pomFile);

            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));

            assert nue != null;
            assert nue != project : "got same Project for " + projectDirectory + " and " + target;

//            ProjectOperations.notifyMoved(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueProjectName);

            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));

            ProjectManager.getDefault().saveProject(nue);

            open(nue, wasMain);

            handle.progress(totalWork);
            handle.finish();
        } catch (Exception e) {
            if (originalOK) {
                open(project, wasMain);
            } else {
		assert target != null;

		//#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
		ProjectManager.getDefault().clearNonProjectCache();
		Project nue = ProjectManager.getDefault().findProject(target);
		if (nue != null) {
            open(nue, wasMain);
        }
            }
            // XXX: Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, errorKey, e.getLocalizedMessage()));
            throw e;
        }
    }

    private static void close(final Project prj) {
        LifecycleManager.getDefault().saveAll();
        OpenProjects.getDefault().close(new Project[] {prj});
    }

    private static void open(final Project prj, final boolean setAsMain) {
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        if (setAsMain) {
            OpenProjects.getDefault().setMainProject(prj);
        }
    }

    private static class OptionalValidator extends AbstractValidator<String> {
        private final JCheckBox checkbox;
        private final Validator<String> delegate;

        OptionalValidator(JCheckBox cb, Validator<String> validator) {
            super(String.class);
            checkbox = cb;
            delegate = validator;
        }
        
        @Override
        public void validate(Problems problems, String compName, String model) {
            if (checkbox.isSelected()) {
                delegate.validate(problems, compName, model);
            }
        }
    }

    private static class FileNameExists extends AbstractValidator<String> {
        private final File parent;

        FileNameExists(File parent) {
            super(String.class);
            assert parent.isDirectory() && parent.exists();
            this.parent = parent;
}

        @Override
        public void validate(Problems problems, String compName, String model) {
            File newDir = new File(parent, model);
            if (newDir.exists()) {
                problems.add("Folder with name '" + model + "' already exists.", Severity.FATAL);
            }
        }
    }
}
