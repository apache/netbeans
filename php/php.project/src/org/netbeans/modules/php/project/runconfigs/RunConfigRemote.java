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
package org.netbeans.modules.php.project.runconfigs;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsRemoteWeb;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Run configuration for REMOTE WEB.
 */
public final class RunConfigRemote extends RunConfigWeb<RunConfigRemote> {

    public static final String NO_CONFIG_NAME = "no-config"; // NOI18N
    public static final String MISSING_CONFIG_NAME = "missing-config"; // NOI18N
    public static final RemoteConfiguration NO_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty(NO_CONFIG_NAME, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_NoRemoteConfiguration")); // NOI18N
    public static final RemoteConfiguration MISSING_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty(MISSING_CONFIG_NAME, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_MissingRemoteConfiguration")); // NOI18N


    private RemoteConfiguration remoteConfiguration;
    private String uploadDirectory;
    private PhpProjectProperties.UploadFiles uploadFilesType;
    private boolean permissionsPreserved;
    private boolean uploadDirectly;


    private RunConfigRemote() {
    }

    public static PhpProjectProperties.RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.REMOTE;
    }

    public static String getDisplayName() {
        return getRunAsType().getLabel();
    }

    //~ Factories

    public static RunConfigRemote create() {
        return new RunConfigRemote();
    }

    public static RunConfigRemote forProject(final PhpProject project) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RunConfigRemote>() {
            @Override
            public RunConfigRemote run() {
                return new RunConfigRemote()
                        .setUrl(ProjectPropertiesSupport.getUrl(project))
                        .setIndexParentDir(FileUtil.toFile(ProjectPropertiesSupport.getWebRootDirectory(project)))
                        .setIndexRelativePath(ProjectPropertiesSupport.getIndexFile(project))
                        .setArguments(ProjectPropertiesSupport.getArguments(project))
                        .setRemoteConfiguration(RemoteConnections.get().remoteConfigurationForName(ProjectPropertiesSupport.getRemoteConnection(project)))
                        .setUploadDirectory(ProjectPropertiesSupport.getRemoteDirectory(project))
                        .setUploadFilesType(ProjectPropertiesSupport.getRemoteUpload(project))
                        .setPermissionsPreserved(ProjectPropertiesSupport.areRemotePermissionsPreserved(project))
                        .setUploadDirectly(ProjectPropertiesSupport.isRemoteUploadDirectly(project));
            }
        });
    }

    //~ Methods

    public String getRemoteConnectionHint() {
        if (remoteConfiguration == null
                || remoteConfiguration == RunConfigRemote.NO_REMOTE_CONFIGURATION
                || remoteConfiguration == RunConfigRemote.MISSING_REMOTE_CONFIGURATION) {
            return null;
        }
        return remoteConfiguration.getUrl(RemoteUtils.sanitizeUploadDirectory(uploadDirectory, true));
    }

    public String getSanitizedUploadDirectory() {
        return RemoteUtils.sanitizeUploadDirectory(uploadDirectory, true);
    }

    //~ Getters & setters

    public UploadFiles getUploadFilesType() {
        return uploadFilesType;
    }

    public RunConfigRemote setUploadFilesType(UploadFiles uploadFilesType) {
        this.uploadFilesType = uploadFilesType;
        return this;
    }

    public RemoteConfiguration getRemoteConfiguration() {
        return remoteConfiguration;
    }

    public RunConfigRemote setRemoteConfiguration(RemoteConfiguration remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
        return this;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public RunConfigRemote setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        return this;
    }

    public boolean arePermissionsPreserved() {
        return permissionsPreserved;
    }

    public RunConfigRemote setPermissionsPreserved(boolean permissionsPreserved) {
        this.permissionsPreserved = permissionsPreserved;
        return this;
    }

    public boolean getUploadDirectly() {
        return uploadDirectly;
    }

    public RunConfigRemote setUploadDirectly(boolean uploadDirectly) {
        this.uploadDirectly = uploadDirectly;
        return this;
    }

}
