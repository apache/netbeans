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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.DefaultProjectOperationsImplementationExecutor;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.support.ProjectOperations;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class DefaultProjectOperationsImplementationExecutorImpl implements DefaultProjectOperationsImplementationExecutor {
    private static final Logger LOG = Logger.getLogger(DefaultProjectOperationsImplementationExecutorImpl.class.getName());
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int MAX_WORK = 100;
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public void doDeleteProject(Project project, boolean onlyMetadata, ProgressHandle handle) throws Exception {
        FileObject projectFolder = project.getProjectDirectory();
        
        LOG.log(Level.FINE, "delete started: {0}", getDisplayName(project));
        
        final List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(project);
        final List<FileObject> dataFiles = ProjectOperations.getDataFiles(project);
        final List<FileObject> allFiles = new ArrayList<>();
        
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
        List<FileObject> toDelete;
        if (onlyMetadata) {
            toDelete = metadataFiles;
        } else {
            toDelete = allFiles;
        }
        try {
            handle.start(toDelete.size() + 1 /*clean*/);
            
            int done = 0;
            
            handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "LBL_Progress_Cleaning_Project"));
            
            ProjectOperations.notifyDeleting(project);
            
            handle.progress(++done);
            
            for (FileObject f : toDelete) {
                handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "LBL_Progress_Deleting_File", FileUtil.getFileDisplayName(f)));
                
                if (f != null && f.isValid()) {
                    f.delete();
                }
                
                handle.progress(++done);
            }
            
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
            String message     = NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "LBL_Project_cannot_be_deleted.", displayName);

            Exceptions.attachLocalizedMessage(e, message);
            throw e;
        }
    }

    @Override
    public Project doCopyProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) throws Exception {
        try {
            int totalWork = MAX_WORK;
            
            
            double currentWorkDone = 0;
            
            handle.progress((int) currentWorkDone);
            
            ProjectOperations.notifyCopying(project);
            
            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));
            
            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toCopyList = new ArrayList<>();
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
            
            handle.progress(totalWork);
            handle.finish();
            return nue;
        } catch (Exception e) {
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Copy", e.getLocalizedMessage()));
            throw e;
        }
    }

    @Override
    public Project doMoveProject(ProgressHandle handle, Project project, String nueFolderName, String nueProjectName, FileObject newTarget, boolean isMove) throws Exception {
        boolean originalOK = true;
        FileObject target = null;

        try {

            int totalWork = MAX_WORK;
            double currentWorkDone = 0;

            handle.progress((int) currentWorkDone);

            ProjectOperations.notifyMoving(project);

            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));

            FileObject projectDirectory = project.getProjectDirectory();
            LOG.log(Level.FINE, "doMoveProject 1/2: {0} @{1}", new Object[]{projectDirectory, project.hashCode()});
            if (LOG.isLoggable(Level.FINER)) {
                for (Project real : OpenProjects.getDefault().getOpenProjects()) {
                    LOG.log(Level.FINER, "  open project: {0} @{1}", new Object[]{real, real.hashCode()});
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
            LOG.log(Level.FINE, "doMoveProject 2/2: {0} @{1}", new Object[]{target, nue.hashCode()});

            ProjectOperations.notifyMoved(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueProjectName);

            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));

            ProjectManager.getDefault().saveProject(nue);

            if (LOG.isLoggable(Level.FINER)) {
                for (Project real : OpenProjects.getDefault().getOpenProjects()) {
                    LOG.log(Level.FINER, "  open project: {0} @{1}", new Object[]{real, real.hashCode()});
                }
            }

            handle.progress(totalWork);
            handle.finish();
            return nue;
        } catch (Exception e) {
            if (originalOK) {
                return project;
            } else {

                //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
                ProjectManager.getDefault().clearNonProjectCache();
                if (target == null) {
                    //#227648 the original exception gets swallowed here because IAE is thrown down the road from findProject,
                    // to better understand what's going on, throw the original exception and don't attempt to open the target project
                    if (isMove) {
                        Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Move", e.getLocalizedMessage()));
                    } else {
                        Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
                    }
                    throw e;
                }
                Project nue = ProjectManager.getDefault().findProject(target);
                if (nue != null) {
                    return nue;
                }
            }
            if (isMove) {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Move", e.getLocalizedMessage()));
            } else {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
            }
            throw e;
        }
    }

    @Override
    public boolean isOldStyleRename(ProgressHandle handle, Project project, String nueName) throws Exception {
        Collection<? extends MoveOperationImplementation> operations = project.getLookup().lookupAll(MoveOperationImplementation.class);
        for (MoveOperationImplementation o : operations) {
            if (!(o instanceof MoveOrRenameOperationImplementation)) {
                LOG.log(Level.WARNING, "{0} should implement MoveOrRenameOperationImplementation", o.getClass().getName()); // NOI18N
                return true;
            }
        }
        return false;
    }

    @Override
    public Project doRenameProject(ProgressHandle handle, Project project, String nueName) throws Exception {
        Collection<? extends MoveOperationImplementation> operations = project.getLookup().lookupAll(MoveOperationImplementation.class);
        for (MoveOperationImplementation o : operations) {
            if (!(o instanceof MoveOrRenameOperationImplementation)) {
                LOG.log(Level.WARNING, "{0} should implement MoveOrRenameOperationImplementation", o.getClass().getName()); // NOI18N
                return doRenameProjectOld(handle, project, nueName, operations);
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
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(DefaultProjectOperationsImplementationExecutorImpl.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
            throw e;
        }
        return null;
    }
    
    private void doCopy(Project original, FileObject from, FileObject toParent) throws IOException {
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
            /*FileObject target =*/ FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
    }
    
    private Project doRenameProjectOld(ProgressHandle handle, Project project, String nueName,
            Collection<? extends MoveOperationImplementation> operations) throws Exception {
        boolean originalOK = true;
        Project nue = null;
        try {
            handle.switchToIndeterminate();
            handle.switchToDeterminate(5);
            int currentWorkDone = 0;
            FileObject projectDirectory = project.getProjectDirectory();
            File projectDirectoryFile = FileUtil.toFile(project.getProjectDirectory());
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
            handle.progress(++currentWorkDone);
            handle.finish();
            return nue;
        } catch (Exception e) {
            if (originalOK) {
                return project;
            } else {
                assert nue != null;
                return nue;
            }
        }
    }
}
