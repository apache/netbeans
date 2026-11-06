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
package org.netbeans.modules.languages.env.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.languages.env.EnvFileResolver;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.modules.web.common.spi.ImportantFilesSupport;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

@ProjectServiceProvider(service = ImportantFilesImplementation.class, projectTypes = {
    @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject"),
    @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-php-project"),})
public class EnvFileImpl implements ImportantFilesImplementation {

    private final Project project;
    private final FileObject projectDir;
    private final FilesListener fileChangeListener = new FilesListener();
    @SuppressWarnings("this-escape")
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public EnvFileImpl(Project project) {
        assert project != null;
        this.project = project;
        this.projectDir = project.getProjectDirectory();
        projectDir.addFileChangeListener(WeakListeners.create(FileChangeListener.class, fileChangeListener, projectDir));
    }

    @Override
    public Collection<ImportantFilesImplementation.FileInfo> getFiles() {
        //custom env files
        List<FileInfo> envFiles = new ArrayList<>();
        for (FileObject file : project.getProjectDirectory().getChildren()) {
            if (file.isFolder()) {
                continue;
            }
            if (!file.getExt().equals(EnvFileResolver.ENV_EXT) && !file.getName().startsWith(EnvFileResolver.DOT_ENV)) {
                continue;
            }
            if (file.getMIMEType().equals(EnvFileResolver.MIME_TYPE)) {
                envFiles.add(new FileInfo(file));
            }
        }
        return envFiles;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private final class FilesListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            check(fe.getFile().getNameExt());
            check(fe.getName() + "." + fe.getExt()); // NOI18N
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        private void check(String filename) {
            if (filename.startsWith(EnvFileResolver.DOT_ENV) || filename.endsWith(EnvFileResolver.DOT_ENV)) {
                changeSupport.fireChange();
            }
        }
    }
}