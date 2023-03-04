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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Jan Lahoda
 */
public final class DefaultProjectOperationsImplementation {
    
    private static final Logger LOG = Logger.getLogger(DefaultProjectOperationsImplementation.class.getName());
    
    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int    MAX_WORK = 100;
    
    private DefaultProjectOperationsImplementation() {
    }
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
 
    //<editor-fold defaultstate="collapsed" desc="Delete Operation">
    /**
     * @return true if success
     */
    private static void performDelete(Project project, List<FileObject> toDelete, ProgressHandle handle) throws Exception {
        try {
            handle.start(toDelete.size() + 1 /*clean*/);
            
            int done = 0;
            
            handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Cleaning_Project"));
            
            ProjectOperations.notifyDeleting(project);
            
            handle.progress(++done);
            
            for (FileObject f : toDelete) {
                handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Deleting_File", FileUtil.getFileDisplayName(f)));
                
                if (f != null && f.isValid()) {
                    f.delete();
                }
                
                handle.progress(++done);
            }
            
            FileObject projectFolder = project.getProjectDirectory();
            projectFolder.refresh(); // #190983
            
            if (!projectFolder.isValid()) {
                LOG.log(Level.WARNING, "invalid project folder: {0}", projectFolder);
            } else if (projectFolder.getChildren().length == 0) {
                projectFolder.delete();
            } else {
                LOG.log(Level.WARNING, "project folder {0} was not empty: {1}", new Object[] {projectFolder, Arrays.asList(projectFolder.getChildren())});
            }
            
            handle.finish();
            
            ProjectOperations.notifyDeleted(project);
        } catch (Exception e) {
            String displayName = getDisplayName(project);
            String message     = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Project_cannot_be_deleted.", displayName);

            Exceptions.attachLocalizedMessage(e, message);
            throw e;
        }
    }
    
    public static void deleteProject(final Project project) {
        deleteProject(project, new GUIUserInputHandler());
    }
    
    static void deleteProject(final Project project, UserInputHandler handler) {
        String displayName = getDisplayName(project);
        FileObject projectFolder = project.getProjectDirectory();
        
        LOG.log(Level.FINE, "delete started: {0}", displayName);
        
        final List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(project);
        final List<FileObject> dataFiles = ProjectOperations.getDataFiles(project);
        final List<FileObject> allFiles = new ArrayList<FileObject>();
        
        allFiles.addAll(metadataFiles);
        allFiles.addAll(dataFiles);
        
        for (Iterator<FileObject> i = allFiles.iterator(); i.hasNext(); ) {
            FileObject f = i.next();
            if (!FileUtil.isParentOf(projectFolder, f)) {
                if (projectFolder.equals(f)) {
                    // sources == project directory
                    continue;
                }
                i.remove();
            }
        }
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
        final DefaultProjectDeletePanel deletePanel = new DefaultProjectDeletePanel(handle, displayName, FileUtil.getFileDisplayName(projectFolder), !dataFiles.isEmpty());
        
        String caption = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
        
        handler.showConfirmationDialog(deletePanel, project, caption, "Yes_Button", "No_Button", true, new Executor() { // NOI18N
            public @Override void execute() throws Exception {
                deletePanel.addProgressBar();
                close(project);
                
                if (deletePanel.isDeleteSources()) {
                    try {
                        performDelete(project, allFiles, handle);
                    } catch (IOException x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                } else {
                    try {
                        performDelete(project, metadataFiles, handle);
                    } catch (IOException x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                }
                deletePanel.removeProgressBar();
            }
        });
        
        LOG.log(Level.FINE, "delete done: {0}", displayName);
    }
    
    static interface UserInputHandler {
        void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor);
    }
    
    private static final class GUIUserInputHandler implements UserInputHandler {
        
        public @Override void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
            DefaultProjectOperationsImplementation.showConfirmationDialog(panel, project, caption, confirmButton, cancelButton, doSetMessageType, executor);
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Operation">
    public static void copyProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Handle"));
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, false);
        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Caption"), "Copy_Button", null, false, new Executor() { // NOI18N
            public @Override void execute() throws Exception {
                final String nueName = panel.getNewName();
                File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());
                
                FileObject newTargetFO = FileUtil.toFileObject(newTarget);
                if (newTargetFO == null) {
                    newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
                }
                final FileObject newTgtFO = newTargetFO;
                project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public @Override void run() throws IOException {
                        try {
                            doCopyProject(handle, project, nueName, newTgtFO);
                        } catch (IOException x) {
                            LOG.log(Level.WARNING, null, x);
                            NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                        } catch (Exception x) {
                            LOG.log(Level.WARNING, null, x);
                            NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(nd);
                        }
                    }
                });
            }
        });
    }
    
    /*package private for tests*/ static void doCopyProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) throws Exception {
        try {
            int totalWork = MAX_WORK;
            
            
            double currentWorkDone = 0;
            
            handle.progress((int) currentWorkDone);
            
            ProjectOperations.notifyCopying(project);
            
            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));
            
            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toCopyList = new ArrayList<FileObject>();
            for (FileObject child : projectDirectory.getChildren()) {
                if (child.isValid()) {
                    toCopyList.add(child);
                }
            }
            
            double workPerFileAndOperation = totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK) / toCopyList.size();

            for (FileObject toCopy : toCopyList) {
                doCopy(project, toCopy, target);
                
                int lastWorkDone = (int) currentWorkDone;
                
                currentWorkDone += workPerFileAndOperation;
                
                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }
            
            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);
            
            assert nue != null;
            
            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));
            
            ProjectOperations.notifyCopied(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueName);
            
            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));
            
            ProjectManager.getDefault().saveProject(nue);
            
            open(nue, false);
            
            handle.progress(totalWork);
            handle.finish();
        } catch (Exception e) {
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Cannot_Move", e.getLocalizedMessage()));
            throw e;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Move Operation">
    public static void moveProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Handle"));
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, true);
        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Caption"), "Move_Button", null, false, new Executor() { // NOI18N
            public @Override void execute() throws Exception {
                final String nueFolderName = panel.getProjectFolderName();
                final String nueProjectName = panel.getNewName();
                File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());
                
                FileObject newTargetFO = FileUtil.toFileObject(newTarget);
                if (newTargetFO == null) {
                    newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
                }
                final FileObject newTgtFO = newTargetFO;
                try {
                    doMoveProject(handle, project, nueFolderName, nueProjectName, newTgtFO, "ERR_Cannot_Move");
                } catch (IOException x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                } catch (Exception x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        });
    }
    
    public static void renameProject(Project project) {
        renameProject(project, null);
    }
    
    public static void renameProject(final Project project, final String nueName) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Handle"));
        final DefaultProjectRenamePanel panel = new DefaultProjectRenamePanel(handle, project, nueName);

        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Caption"), "Rename_Button", null, false, new Executor() { // NOI18N
            public @Override void execute() throws Exception {
                final String nueName = panel.getNewName();
                panel.addProgressBar();
                
                if (panel.getRenameProjectFolder()) {
                    try {
                        doMoveProject(handle, project, nueName, nueName, project.getProjectDirectory().getParent(), "ERR_Cannot_Rename");
                    } catch (IOException x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                } else {
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public @Override void run() throws IOException {
                            try {
                                doRenameProject(handle, project, nueName);
                            } catch (IOException x) {
                                LOG.log(Level.WARNING, null, x);
                                NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notifyLater(nd);
                            } catch (Exception x) {
                                LOG.log(Level.WARNING, null, x);
                                NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notifyLater(nd);
                            }
                        }
                    });
		}
                panel.removeProgressBar();
            }
        });
    }

    private static void doRenameProject(ProgressHandle handle, Project project, String nueName) throws Exception {
        Collection<? extends MoveOperationImplementation> operations = project.getLookup().lookupAll(MoveOperationImplementation.class);
        for (MoveOperationImplementation o : operations) {
            if (!(o instanceof MoveOrRenameOperationImplementation)) {
                Logger.getLogger(DefaultProjectOperationsImplementation.class.getName()).log(Level.WARNING,
                        "{0} should implement MoveOrRenameOperationImplementation", o.getClass().getName());
                doRenameProjectOld(handle, project, nueName, operations);
                return;
            }
        }
        // Better new style.
        try {
            handle.switchToDeterminate(4);
            int currentWorkDone = 0;
            handle.progress(++currentWorkDone);
            for (MoveOperationImplementation o : operations) {
                ((MoveOrRenameOperationImplementation) o).notifyRenaming();
            }
            handle.progress(++currentWorkDone);
            for (MoveOperationImplementation o : operations) {
                ((MoveOrRenameOperationImplementation) o).notifyRenamed(nueName);
            }
            handle.progress(++currentWorkDone);
            ProjectManager.getDefault().saveProject(project);
            handle.progress(++currentWorkDone);
            handle.finish();
        } catch (Exception e) {
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
            throw e;
        }
    }

    private static void doRenameProjectOld(ProgressHandle handle, Project project, String nueName,
            Collection<? extends MoveOperationImplementation> operations) throws Exception {
        boolean originalOK = true;
        Project main = OpenProjects.getDefault().getMainProject();
        boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
        Project nue = null;
        try {
            handle.switchToIndeterminate();
            handle.switchToDeterminate(5);
            int currentWorkDone = 0;
            FileObject projectDirectory = project.getProjectDirectory();
            File projectDirectoryFile = FileUtil.toFile(project.getProjectDirectory());
            close(project);
            handle.progress(++currentWorkDone);
            for (MoveOperationImplementation o : operations) {
                o.notifyMoving();
            }
            handle.progress(++currentWorkDone);
            for (MoveOperationImplementation o : operations) {
                o.notifyMoved(null, projectDirectoryFile, nueName);
            }
            handle.progress(++currentWorkDone);
            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            nue = ProjectManager.getDefault().findProject(projectDirectory);
            assert nue != null;
            originalOK = false;
            handle.progress(++currentWorkDone);
            operations = nue.getLookup().lookupAll(MoveOperationImplementation.class);
            for (MoveOperationImplementation o : operations) {
                o.notifyMoved(project, projectDirectoryFile, nueName);
            }
            ProjectManager.getDefault().saveProject(project);
            ProjectManager.getDefault().saveProject(nue);
            open(nue, wasMain);
            handle.progress(++currentWorkDone);
            handle.finish();
        } catch (Exception e) {
            if (originalOK) {
                open(project, wasMain);
            } else {
                assert nue != null;
                open(nue, wasMain);
            }
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
            throw e;
        }
    }

    /*package private for tests*/ static void doMoveProject(ProgressHandle handle, Project project, String nueFolderName, String nueProjectName, FileObject newTarget, String errorKey) throws Exception {
        boolean originalOK = true;
        Project main    = OpenProjects.getDefault().getMainProject();
        boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
	FileObject target = null;
        
        try {
            
            int totalWork = MAX_WORK;
            double currentWorkDone = 0;
            
            handle.progress((int) currentWorkDone);
            
            ProjectOperations.notifyMoving(project);
            
            close(project);
            
            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));
            
            FileObject projectDirectory = project.getProjectDirectory();
            LOG.log(Level.FINE, "doMoveProject 1/2: {0} @{1}", new Object[] {projectDirectory, project.hashCode()});
            if (LOG.isLoggable(Level.FINER)) {
                for (Project real : OpenProjects.getDefault().getOpenProjects()) {
                    LOG.log(Level.FINER, "  open project: {0} @{1}", new Object[] {real, real.hashCode()});
                }
            }
            
            double workPerFileAndOperation = totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK);

            FileLock lock = projectDirectory.lock();
            try {
                target = projectDirectory.move(lock, newTarget, nueFolderName, null);
            } finally {
                lock.releaseLock();
            }
            // TBD if #109580 matters here: do we need to delete nbproject/private? probably not
            int lastWorkDone = (int) currentWorkDone;

            currentWorkDone += workPerFileAndOperation;

            if (lastWorkDone < (int) currentWorkDone) {
                handle.progress((int) currentWorkDone);
            }
            
            originalOK = false;
            
            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);
            
            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));
            
            assert nue != null;
            assert nue != project : "got same Project for " + projectDirectory + " and " + target;
            LOG.log(Level.FINE, "doMoveProject 2/2: {0} @{1}", new Object[] {target, nue.hashCode()});

            ProjectOperations.notifyMoved(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueProjectName);
            
            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));
            
            ProjectManager.getDefault().saveProject(nue);
            
            open(nue, wasMain);
            if (LOG.isLoggable(Level.FINER)) {
                for (Project real : OpenProjects.getDefault().getOpenProjects()) {
                    LOG.log(Level.FINER, "  open project: {0} @{1}", new Object[] {real, real.hashCode()});
                }
            }
            
            handle.progress(totalWork);
            handle.finish();
        } catch (Exception e) {
            if (originalOK) {
                open(project, wasMain);
            } else {
		
		//#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
		ProjectManager.getDefault().clearNonProjectCache();
                if (target == null) {
                    //#227648 the original exception gets swallowed here because IAE is thrown down the road from findProject,
                    // to better understand what's going on, throw the original exception and don't attempt to open the target project
                    Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, errorKey, e.getLocalizedMessage()));
                    throw e;
                }
		Project nue = ProjectManager.getDefault().findProject(target);
		if (nue != null) {
            open(nue, wasMain);
        }
            }
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, errorKey, e.getLocalizedMessage()));
            throw e;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Move Utilities">
    private static void doCopy(Project original, FileObject from, FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return ;
        }
        
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(from).getProjectDirectory())) {
            return ;
        }
        
        //#109580
        if (SharabilityQuery.getSharability(from) == SharabilityQuery.Sharability.NOT_SHARABLE) {
            return;
        }
        
        if (from.isFolder()) {
            FileObject copy = toParent.createFolder(from.getNameExt());
            for (FileObject kid : from.getChildren()) {
                doCopy(original, kid, copy);
            }
        } else {
            assert from.isData();
            FileObject target = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
    }
    
    private static FileObject createFolder(File parent, String name) throws IOException {
        FileObject path = FileUtil.toFileObject(parent);
        if (path != null) {
            return path.createFolder(name);
        } else {
            return createFolder(parent.getParentFile(), parent.getName()).createFolder(name);
        }
    }
    
    private static JComponent wrapPanel(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        return component;
    }
    
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
        final JButton confirm = new JButton();
        Mnemonics.setLocalizedText(confirm, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_" + confirmButton));
        final JButton cancel  = new JButton(cancelButton == null ?
              NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Cancel_Button")
            : NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_" + cancelButton));
        
        confirm.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_" + confirmButton));
        cancel.getAccessibleContext().setAccessibleDescription(cancelButton == null ?
              NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_Cancel_Button")
            : NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_" + cancelButton));
        
        assert panel instanceof InvalidablePanel;
        
        ((InvalidablePanel) panel).addChangeListener(new ChangeListener() {
            public @Override void stateChanged(ChangeEvent e) {
                confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
            }
        });
        
        confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
        
        final Dialog[] dialog = new Dialog[1];

        DialogDescriptor dd = new DialogDescriptor(doSetMessageType ? panel : wrapPanel(panel), caption, true, new Object[] {confirm, cancel}, cancelButton != null ? cancel : confirm, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            private boolean operationRunning;
            public @Override void actionPerformed(ActionEvent e) {
                //#65634: making sure that the user cannot close the dialog before the operation is finished:
                if (operationRunning) {
                    return ;
                }
                
                if (dialog[0] instanceof JDialog) {
                    ((JDialog) dialog[0]).getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                    ((JDialog) dialog[0]).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                }
                
                operationRunning = true;
		
                if (e.getSource() == confirm) {
                    confirm.setEnabled(false);
                    cancel.setEnabled(false);
                    ((InvalidablePanel) panel).showProgress();
                    
                    Component findParent = panel;
                    
                    while (findParent != null && !(findParent instanceof Window)) {
                        findParent = findParent.getParent();
                    }
                    
                    if (findParent != null) {
                        ((Window) findParent).pack();
                    }
                    
                    RequestProcessor.getDefault().post(new Runnable() {
                        public @Override void run() {
                            final AtomicReference<Throwable> e = new AtomicReference<Throwable>();
                            try {
                                executor.execute();
                            } catch (Throwable ex) {
                                e.set(ex);
                                if (ex instanceof ThreadDeath) {
                                    throw (ThreadDeath)ex;
                                }
                            } finally {                            
                                SwingUtilities.invokeLater(new Runnable() {
                                    public @Override void run() {
                                        if (dialog[0] != null) {
                                            dialog[0].setVisible(false);
                                        }
                                        if (e.get() != null) {
                                            LOG.log(Level.WARNING, null, e.get());
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    dialog[0].setVisible(false);
                }
            }
        });
        
        if (doSetMessageType) {
            dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        }
        
        dd.setClosingOptions(new Object[0]);
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        
        dialog[0].setVisible(true);
        
        dialog[0].dispose();
        dialog[0] = null;
    }
    
    static @CheckForNull String computeError(@NullAllowed File location, String projectNameText, boolean pureRename) {
        return computeError(location, projectNameText, null, pureRename);
    }
    
    static @CheckForNull String computeError(@NullAllowed File location, String projectNameText, String projectFolderText, boolean pureRename) {
        if (projectNameText.length() == 0) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Name_Must_Entered");
        }
        if (projectNameText.indexOf('/') != -1 || projectNameText.indexOf('\\') != -1) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Not_Valid_Filename", projectNameText);
        }
        if (hasTrailingWhiteSpace(projectNameText)) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Trailing_Whitespace", projectNameText); //NOI18N
        }

        if (location == null) {
            return null; // #199241: skip other checks for remote projects
        }

        File parent = location;
        String checkFileRes = null; // result of check of last path segment
        if (!location.exists()) {
            //if some dirs in teh chain are not created, consider it ok.
            checkFileRes = checkFileOrFolderName(parent);
            parent = location.getParentFile();
            while (parent != null && !parent.exists() && checkFileRes == null) {
                checkFileRes = checkFileOrFolderName(parent);
                parent = parent.getParentFile();
            }
            if (parent == null) {
                return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Does_Not_Exist");
            }
        }
        if (checkFileRes != null) {
            return checkFileRes;
        }

        if (!parent.canWrite()) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Read_Only");
        }
        
        File projectFolderFile = null;
        if (projectFolderText == null) {
            projectFolderFile = new File(location, projectNameText);
        } else {
            projectFolderFile = new File(projectFolderText);
        }
        
        if (projectFolderFile.exists() && !pureRename) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Folder_Exists");
        }
        
        return null;
    }

    private static boolean hasTrailingWhiteSpace(String s) {
        return s.matches("(^\\s.*)|(.*\\s$)");                          //NOI18N
    }

    private static String checkFileOrFolderName(File f) {
        String n = f.getName();
        if (hasTrailingWhiteSpace(n)) {
            return NbBundle.getMessage(
                    DefaultProjectOperationsImplementation.class,
                    "ERR_Trailing_Whitespace", n);                      //NOI18N
        } else if (n.contains("/") || n.contains("\\")) {               //NOI18N
            return NbBundle.getMessage(
                    DefaultProjectOperationsImplementation.class,
                    "ERR_Not_Valid_Foldername", n);                     //NOI18N
        } else {
            return null;
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
    
    static interface Executor {
        public void execute() throws Exception;
    }
    
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
    //</editor-fold>
    
}
