/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
