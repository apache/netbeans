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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Misc utility functions used when creating new project
 */
public class NewProjectWizardUtils {

    public static boolean isFullRemote(WizardDescriptor wizardDescriptor) {
        return WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor) != null;
    }

    public static FileSystem getFileSystem(WizardDescriptor wizardDescriptor) {
        if (isFullRemote(wizardDescriptor)) {
            String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizardDescriptor);
            CndUtils.assertNotNull(hostUID, "Null host UID"); //NOI18N
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
            return FileSystemProvider.getFileSystem(env);
        } else {
            return FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        }
    }

    public static FileObject getFileObject(String path, WizardDescriptor wizardDescriptor) {
        if (isFullRemote(wizardDescriptor)) {
            String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizardDescriptor);
            CndUtils.assertNotNull(hostUID, "Null host UID"); //NOI18N
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
            return RemoteFileUtil.getFileObject(path, env);
        } else {
            return CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(path));
        }
    }

    public static ExecutionEnvironment getExecutionEnvironment(WizardDescriptor wizardDescriptor) {
        String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizardDescriptor);
        return (hostUID == null) ?
            ServerList.getDefaultRecord().getExecutionEnvironment() :
            ExecutionEnvironmentFactory.fromUniqueID(hostUID);
    }

    public static boolean fileExists(String absolutePath, WizardDescriptor wizardDescriptor) {
        if (isFullRemote(wizardDescriptor)) {
            return RemoteFileUtil.fileExists(absolutePath, getExecutionEnvironment(wizardDescriptor));
        } else {
            return new File(absolutePath).exists();
        }
    }

    public static boolean isDirectory(String absolutePath, WizardDescriptor wizardDescriptor) {
        if (isFullRemote(wizardDescriptor)) {
            return RemoteFileUtil.isDirectory(absolutePath, getExecutionEnvironment(wizardDescriptor));
        } else {
            return new File(absolutePath).isDirectory();
        }
    }

    public static JFileChooser createFileChooser(Project project,
            String titleText, String buttonText,
            int mode, FileFilter[] filters,
            String initialPath, boolean useParent) {
        
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                execEnv = remoteProject.getSourceFileSystemHost();
            }
        }
        return RemoteFileChooserUtil.createFileChooser(execEnv, titleText, buttonText, mode, filters, initialPath, useParent);
    }
    
    public static ExecutionEnvironment getDefaultSourceEnvironment() {
        String externalForm = System.getProperty("cnd.default.project.source.env"); //NOI18N
        if (externalForm != null) {
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(externalForm);
            if (env != null) {
                return env;
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static JFileChooser createFileChooser(WizardDescriptor wd, String titleText,
            String buttonText, int mode, FileFilter[] filters,
            String initialPath, boolean useParent) {

        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        if (isFullRemote(wd)) {
            String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wd);
            if (hostUID != null) {
                execEnv = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
            }
        }
        return RemoteFileChooserUtil.createFileChooser(execEnv, titleText, buttonText, mode, filters, initialPath, useParent);
    }


}
