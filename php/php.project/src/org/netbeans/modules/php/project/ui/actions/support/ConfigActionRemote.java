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

package org.netbeans.modules.php.project.ui.actions.support;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigRemoteValidator;
import org.netbeans.modules.php.project.ui.actions.UploadCommand;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Action implementation for REMOTE configuration.
 * It means uploading, running and debugging web pages on a remote web server.
 * @author Tomas Mysik
 */
class ConfigActionRemote extends ConfigActionLocal {

    protected ConfigActionRemote(PhpProject project) {
        super(project);
    }

    @Override
    public boolean isProjectValid() {
        return isValid(RunConfigRemoteValidator.validateConfigAction(RunConfigRemote.forProject(project), true) == null);
    }

    @Override
    public boolean isFileValid() {
        return isValid(RunConfigRemoteValidator.validateConfigAction(RunConfigRemote.forProject(project), false) == null);
    }

    @Override
    public void runProject() {
        eventuallyUploadFiles();
        super.runProject();
    }

    @Override
    public void debugProject() {
        eventuallyUploadFiles();
        super.debugProject();
    }

    @Override
    protected void preShowUrl(Lookup context) {
        eventuallyUploadFiles(CommandUtils.filesForContextOrSelectedNodes(context));
    }

    private void eventuallyUploadFiles() {
        eventuallyUploadFiles((FileObject[]) null);
    }

    private void eventuallyUploadFiles(FileObject... preselectedFiles) {
        UploadCommand uploadCommand = (UploadCommand) CommandUtils.getCommand(project, UploadCommand.ID);
        if (!uploadCommand.isActionEnabled(null)) {
            return;
        }

        PhpProjectProperties.UploadFiles uploadFiles = RunConfigRemote.forProject(project).getUploadFilesType();
        assert uploadFiles != null;

        if (PhpProjectProperties.UploadFiles.ON_RUN.equals(uploadFiles)) {
            uploadCommand.uploadFiles(new FileObject[] {ProjectPropertiesSupport.getSourcesDirectory(project)}, preselectedFiles);
        }
    }

}
