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

package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.modules.cnd.makeproject.api.DefaultProjectOperationsImplementationExecutor;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.uiapi.DefaultProjectOperationsImplementationUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Copy-pasted from org.netbeans.modules.project.uiapi
 * Intention is to contribute it back as soon as it is adapted to remote environment.
 * Since we are planning to contribute it back,
 * NEVER use any remote or cnd stuff directly, but only via a well defined SPI
 *
 */
@org.openide.util.lookup.ServiceProvider(service=DefaultProjectOperationsImplementationUI.class)
public final class DefaultProjectOperationsImplementation extends DefaultProjectOperationsImplementationUI {
    
    private static final Logger LOG = Logger.getLogger(DefaultProjectOperationsImplementation.class.getName());
    
    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int    MAX_WORK = 100;
    
    public DefaultProjectOperationsImplementation() {
    }
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
 
    @Override
    public void deleteProject(final MakeProject project, final DefaultProjectOperationsImplementationExecutor executor) {
        UserInputHandler handler = new GUIUserInputHandler();
        String displayName = getDisplayName(project);
        FileObject projectFolder = project.getProjectDirectory();
        final List<FileObject> dataFiles = ProjectOperations.getDataFiles(project);
        
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
        final DefaultProjectDeletePanel deletePanel = new DefaultProjectDeletePanel(handle, displayName, FileUtil.getFileDisplayName(projectFolder), !dataFiles.isEmpty());
        
        String caption = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
        
        handler.showConfirmationDialog(deletePanel, project, caption, "Yes_Button", "No_Button", true, () -> { // NOI18N
            deletePanel.addProgressBar();
            close(project);
            
            if (deletePanel.isDeleteSources()) {
                try {
                    executor.doDeleteProject(project, false, handle);
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
                    executor.doDeleteProject(project, true, handle);
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
    
    @Override
    public void copyProject(final MakeProject project, final DefaultProjectOperationsImplementationExecutor executor) {
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Handle"));
        final DefaultProjectCopyPanel panel = new DefaultProjectCopyPanel(handle, project, false);
        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Caption"), "Copy_Button", null, false, () -> {// NOI18N
            final String nueName = panel.getNewName();
            File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());
            FileObject newTargetFO = FileUtil.toFileObject(newTarget);
            if (newTargetFO == null) {
                newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
            }
            final FileObject newTgtFO = newTargetFO;
            project.getProjectDirectory().getFileSystem().runAtomicAction(() -> {
                try {
                    Project copy = executor.doCopyProject(handle, project, nueName, newTgtFO);
                    open(copy, false);
                } catch (IOException x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                } catch (Exception x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            });
        });
    }
    
    @Override
    public void moveProject(final MakeProject project, final DefaultProjectOperationsImplementationExecutor executor) {
        final ProgressHandle handle =ProgressHandle.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Handle"));
        final DefaultProjectCopyPanel panel = new DefaultProjectCopyPanel(handle, project, true);
        //#76559
        handle.start(MAX_WORK);
        Project main = OpenProjects.getDefault().getMainProject();
        final boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());

        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Caption"), "Move_Button", null, false, () -> { // NOI18N
            close(project);
            
            final String nueFolderName = panel.getProjectFolderName();
            final String nueProjectName = panel.getNewName();
            File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());
            
            FileObject newTargetFO = FileUtil.toFileObject(newTarget);
            if (newTargetFO == null) {
                newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
            }
            final FileObject newTgtFO = newTargetFO;
            try {
                Project move = executor.doMoveProject(handle, project, nueFolderName, nueProjectName, newTgtFO, true); // NOI18N
                open(move, wasMain);
            } catch (IOException x) {
                LOG.log(Level.WARNING, null, x);
                NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            } catch (Exception x) {
                LOG.log(Level.WARNING, null, x);
                NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        });
    }
    
    @Override
    public void renameProject(final MakeProject project, final DefaultProjectOperationsImplementationExecutor executor) {
        final String nueName = null;
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Handle"));
        final DefaultProjectRenamePanel panel = new DefaultProjectRenamePanel(handle, project, nueName);

        //#76559
        handle.start(MAX_WORK);
        Project main = OpenProjects.getDefault().getMainProject();
        final boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Caption"), "Rename_Button", null, false, () -> { // NOI18N
            final String nueName1 = panel.getNewName();
            panel.addProgressBar();
            if (panel.getRenameProjectFolder()) {
                try {
                    Project move = executor.doMoveProject(handle, project, nueName1, nueName1, project.getProjectDirectory().getParent(), false); // NOI18N
                    close(project);
                    open(move, wasMain);
                }catch (IOException x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }catch (Exception x) {
                    LOG.log(Level.WARNING, null, x);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            } else {
                project.getProjectDirectory().getFileSystem().runAtomicAction(() -> {
                    try {
                        if (executor.isOldStyleRename(handle, project, nueName1)) {
                            close(project);
                        }
                        Project rename = executor.doRenameProject(handle, project, nueName1);
                        if (rename != null) {
                            open(rename, wasMain);
                        }
                    }catch (IOException x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }catch (Exception x) {
                        LOG.log(Level.WARNING, null, x);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(x.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                });
            }
            panel.removeProgressBar();
        });
    }
    
    private FileObject createFolder(File parent, String name) throws IOException {
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
        
        ((InvalidablePanel) panel).addChangeListener((ChangeEvent e) -> {
            confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
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
                    
                    RequestProcessor.getDefault().post(() -> {
                        final AtomicReference<Throwable> ref = new AtomicReference<>();
                        try {
                            executor.execute();
                        } catch (Throwable ex) {
                            ref.set(ex);
                            if (ex instanceof ThreadDeath) {
                                throw (ThreadDeath)ex;
                            }
                        } finally {
                            SwingUtilities.invokeLater(() -> {
                                dialog[0].setVisible(false);
                                if (ref.get() != null) {
                                    LOG.log(Level.WARNING, null, ref.get());
                                }
                            });
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

    static @CheckForNull String computeError(@NullAllowed FileProxy location, String projectNameText, boolean pureRename) {
        return computeError(location, projectNameText, null, pureRename);
    }
    
    static @CheckForNull String computeError(@NullAllowed FileProxy location, String projectNameText, String projectFolderText, boolean pureRename) {
        if (projectNameText.length() == 0) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Name_Must_Entered");
        }
        if (projectNameText.indexOf('/') != -1 || projectNameText.indexOf('\\') != -1) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Not_Valid_Filename", projectNameText);
        }

        if (location == null) {
            return null; // #199241: skip other checks for remote projects
        }

        FileProxy parent = location;
        if (!location.exists()) {
            //if some dirs in teh chain are not created, consider it ok.
            parent = location.getParentFile();
            while (parent != null && !parent.exists()) {
                parent = parent.getParentFile();
            }
            if (parent == null) {
                return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Does_Not_Exist");
            }
        }

        if (!parent.canWrite()) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Read_Only");
        }

        FileProxy projectFolderFile = null;
        if (projectFolderText == null) {
            projectFolderFile = location.getChild(projectNameText);
        } else {
            projectFolderFile = FileProxy.createAbsolute(location, projectFolderText);
        }

        // It's ok to check exists just in EDT in the case of rename:
        // parent directory content already in cache.
        // Other usages can cause UI slowness here
        if (projectFolderFile.exists() && !pureRename) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Folder_Exists");
        }

        return null;
    }
    
    private void close(final Project prj) {
        LifecycleManager.getDefault().saveAll();
        OpenProjects.getDefault().close(new Project[] {prj});
    }
    
    private void open(final Project prj, final boolean setAsMain) {
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        if (setAsMain) {
            OpenProjects.getDefault().setMainProject(prj);
        }
    }

    private static interface Executor {
        public void execute() throws Exception;
    }

    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
}
