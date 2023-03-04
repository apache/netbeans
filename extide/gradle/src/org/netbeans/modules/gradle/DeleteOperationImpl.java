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
package org.netbeans.modules.gradle;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.actions.ActionToTaskUtils;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunConfig.ExecFlag;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ProjectState;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.modules.gradle.api.execute.RunConfig.ExecFlag.REPEATABLE;
/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service=DeleteOperationImplementation.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class DeleteOperationImpl implements DeleteOperationImplementation {
    final Project project;

    public DeleteOperationImpl(Project project) {
        this.project = project;
    }

    @Override
    @Messages({
        "# {0} - The project name",
        "LBL_Clean4Delete=Clean Before Delete ({0})"
    })
    public void notifyDeleting() throws IOException {
        ActionMapping mapping = ActionToTaskUtils.getActiveMapping(COMMAND_DELETE, project, Lookup.EMPTY);
        RunConfig config = RunUtils.createRunConfig(project, COMMAND_DELETE,
                ActionProviderImpl.taskName(project, COMMAND_DELETE, Lookup.EMPTY),
                mapping.isRepeatable() ? EnumSet.of(REPEATABLE) : EnumSet.noneOf(ExecFlag.class),
                Utilities.parseParameters(mapping.getArgs()));
        ExecutorTask task = RunUtils.executeGradle(config, "");
        task.result();
        task.getInputOutput().getOut().close();
        task.getInputOutput().getErr().close();
    }

    @Override
    public void notifyDeleted() throws IOException {
        project.getLookup().lookup(ProjectState.class).notifyDeleted();
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        List<FileObject> ret = new LinkedList<>();
        if (project instanceof NbGradleProjectImpl) {
            NbGradleProjectImpl prj = (NbGradleProjectImpl) project;
            GradleFiles gfs = prj.getGradleFiles();
            List<File> meta = new LinkedList<>();
            if (gfs.isRootProject()) {
                meta.addAll(gfs.getProjectFiles());
            } else {
                if (!gfs.isScriptlessSubProject()) {
                    meta.add(gfs.getBuildScript());
                }
            }
            for (File file : meta) {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    ret.add(fo);
                }
            }
        }
        return ret;
    }

    @Override
    public List<FileObject> getDataFiles() {
        return Collections.singletonList(project.getProjectDirectory());
    }


}
