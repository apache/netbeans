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
package org.netbeans.modules.php.project.problems;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Future;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Problem resolver using directory chooser. The selected directory is saved
 * in project properties.
 */
public class DirectoryProblemResolver implements ProjectProblemResolver {

    private final PhpProject project;
    private final String propertyName;
    private final String dialogTitle;


    public DirectoryProblemResolver(PhpProject project, String propertyName, String dialogTitle) {
        this.project = project;
        this.propertyName = propertyName;
        this.dialogTitle = dialogTitle;
    }

    @NbBundle.Messages("DirectoryProblemResolver.dialog.choose=Choose")
    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        File selectedDir = new FileChooserBuilder(ProjectPropertiesProblemProvider.class)
                .setTitle(dialogTitle)
                .setDefaultWorkingDirectory(projectDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setApproveText(Bundle.DirectoryProblemResolver_dialog_choose())
                .showOpenDialog();
        if (selectedDir == null) {
            // no file selected
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }
        // save metadata
        String relPath = ProjectPropertiesSupport.relativizeFile(projectDir, selectedDir);
        PhpProjectProperties.save(project, Collections.singletonMap(propertyName, relPath), Collections.<String, String>emptyMap());
        // return unresolved state; it will change automatically once the metadata are really saved (property change will be fired)
        return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.project != null ? this.project.hashCode() : 0);
        hash = 19 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DirectoryProblemResolver other = (DirectoryProblemResolver) obj;
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        if ((this.propertyName == null) ? (other.propertyName != null) : !this.propertyName.equals(other.propertyName)) {
            return false;
        }
        return true;
    }

}
