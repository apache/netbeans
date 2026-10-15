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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.file.launcher.api.SourceLauncher;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.modules.java.lsp.server.protocol.Workspace;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

public abstract class SingleFileOptionsQueryImpl implements SingleFileOptionsQueryImplementation {

    private final Map<Workspace, WorkspaceSettings> workspace2Settings = new WeakHashMap<>();
    private final Map<Workspace, Map<FileObject, ResultImpl>> workspace2Folder2Options = new WeakHashMap<>();

    @Override
    public Result optionsFor(FileObject file) {
        if (isSingleSourceFile(file)) {
            Workspace workspace = Lookup.getDefault().lookup(Workspace.class);
            FileObject workspaceFolder = workspace != null ? findWorkspaceFolder(workspace, file) : null;

            if (workspaceFolder != null) {
                return getResult(workspace, workspaceFolder);
            } else {
                List<Workspace> workspaces;

                synchronized (this) {
                    workspaces = new ArrayList<>(workspace2Settings.keySet());
                }

                int count = 0;
                for (Workspace w : workspaces) {
                    if (w == null)
                        continue;   // Since a WeakHashMap is in use, it is possible to receive a null value.
                    FileObject folder = findWorkspaceFolder(w, file);
                    if (folder != null) {
                        return getResult(w, folder);
                    }
                    if (count++ == 0 && workspace == null)
                        workspace = w;
                }

                if (count == 1) {
                    // Since this is a single source file, associate it with the single open workspace,
                    // even when it is not a descendant of one of the root folders.
                    FileObject folder;
                    if (file.isFolder()) {
                        folder = file;
                    } else {
                        folder = file.getParent();
                        if (folder == null)
                            folder = file;
                    }
                    return getResult(workspace, folder);
                }
                return null;
            }
        }
        return null;
    }

    private synchronized Result getResult(Workspace workspace, FileObject workspaceFolder) {
        Map<FileObject, ResultImpl> folder2Result =
                workspace2Folder2Options.computeIfAbsent(workspace, w -> new HashMap<>());
        return folder2Result.computeIfAbsent(workspaceFolder, f -> new ResultImpl(folder2Result,
                workspaceFolder,
                getWorkspaceSettings(workspace)));
    }

    static FileObject findWorkspaceFolder(Workspace workspace, FileObject file) {
        for (FileObject workspaceFolder : workspace.getClientWorkspaceFolders()) {
            if (FileUtil.isParentOf(workspaceFolder, file) || workspaceFolder == file) {
                return workspaceFolder;
            }
        }

        //in case file is a source root, and the workspace folder is nested inside the root:
        for (FileObject workspaceFolder : workspace.getClientWorkspaceFolders()) {
            if (FileUtil.isParentOf(file, workspaceFolder)) {
                return workspaceFolder;
            }
        }

        return null;
    }

    private static final class ResultImpl extends FileChangeAdapter implements Result, ChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final Map<FileObject, ResultImpl> workspaceFolders2Results;
        private final FileObject workspaceFolder;
        private final WorkspaceSettings workspaceSettings;

        public ResultImpl(Map<FileObject, ResultImpl> workspaceFolders2Results,
                          FileObject workspaceFolder,
                          WorkspaceSettings workspaceSettings) {
            this.workspaceFolders2Results = workspaceFolders2Results;
            this.workspaceFolder = workspaceFolder;
            this.workspaceSettings = workspaceSettings;

            workspaceSettings.addChangeListener(this);
            workspaceFolder.addFileChangeListener(this);
        }

        @Override
        public String getOptions() {
            String options = workspaceSettings.getOptions();
            return options != null ? options : "";
        }

        @Override
        public URI getWorkDirectory() {
            String cwd = workspaceSettings.getWorkDirectory();
            FileObject workDir = cwd != null ? FileUtil.toFileObject(new File(cwd))
                                             : workspaceFolder;
            return workDir.toURI();
        }

        @Override
        public boolean registerRoot() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            cs.fireChange();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            workspaceFolders2Results.remove(workspaceFolder);
        }

    }

    private final class WorkspaceSettings {

        private final ChangeSupport cs = new ChangeSupport(this);

        private String options;
        private String workdirDirectory;

        public synchronized String getOptions() {
            return options;
        }

        public synchronized String getWorkDirectory() {
            return workdirDirectory;
        }

        public boolean setOptions(String options, String workingDirectory) {
            boolean modified = false;
            synchronized (this) {
                if (!Objects.equals(this.options, options)) {
                    this.options = options;
                    modified = true;
                }
                if (!Objects.equals(this.workdirDirectory, workingDirectory)) {
                    this.workdirDirectory = workingDirectory;
                    modified = true;
                }
            }
            if (modified) {
                cs.fireChange();
            }
            return modified;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }

    public boolean setConfiguration(Workspace workspace, String vmOptions, String workDirectory) {
        return getWorkspaceSettings(workspace).setOptions(vmOptions, workDirectory);
    }

    private synchronized WorkspaceSettings getWorkspaceSettings(Workspace workspace) {
        Parameters.notNull("workspace", workspace);
        return workspace2Settings.computeIfAbsent(workspace, w -> {
            return new WorkspaceSettings();
        });
    }

    //copied from SingleSourceFileUtil:
    static boolean isSingleSourceFile(FileObject fObj) {
        Project p = FileOwnerQuery.getOwner(fObj);
        if (p != null) {
            return false;
        }
        if (!fObj.isFolder() && !fObj.getExt().equalsIgnoreCase("java")) { //NOI18N
            return false;
        }
        return SourceLauncher.isSourceLauncherFile(fObj);
    }

}
