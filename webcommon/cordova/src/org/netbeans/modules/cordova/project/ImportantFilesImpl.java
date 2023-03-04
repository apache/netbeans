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
package org.netbeans.modules.cordova.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.modules.web.common.spi.ImportantFilesSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Roman Svitanic
 */
@NbBundle.Messages("LBL_CordovaPlugins=Cordova Plugins")
@ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
public class ImportantFilesImpl implements ImportantFilesImplementation {

    private final ImportantFilesSupport support;
    private final ImportantFilesSupport support2;
    
    private final ImportantFilesSupport.FileInfoCreator fileInfoCreator = new ImportantFilesSupport.FileInfoCreator() {
        @Override
        public FileInfo create(FileObject fileObject) {
            return new FileInfo(
                    fileObject,
                    fileObject.getName().equals("plugins") ? Bundle.LBL_CordovaPlugins(): fileObject.getName(), //NOI18N
                    null);
        }
    };

    public ImportantFilesImpl(Project project) {
        assert project != null;
        support = ImportantFilesSupport.create(project.getProjectDirectory().getFileObject("nbproject"), "plugins.properties"); // NOI18N
        support2 = ImportantFilesSupport.create(project.getProjectDirectory(), "config.xml"); // NOI18N
    }

    @Override
    public Collection<FileInfo> getFiles() {
        List<FileInfo> ret = new ArrayList<>(support.getFiles(fileInfoCreator));
        ret.addAll(support2.getFiles(fileInfoCreator));
        return ret;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
        support2.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
        support2.removeChangeListener(listener);
    }
}
