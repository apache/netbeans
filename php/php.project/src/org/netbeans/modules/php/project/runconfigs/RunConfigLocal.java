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
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Run configuration for LOCAL WEB.
 */
public final class RunConfigLocal extends RunConfigWeb<RunConfigLocal> {

    private RunConfigLocal() {
    }

    public static PhpProjectProperties.RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.LOCAL;
    }

    public static String getDisplayName() {
        return getRunAsType().getLabel();
    }

    //~ Factories

    public static RunConfigLocal create() {
        return new RunConfigLocal();
    }

    public static RunConfigLocal forProject(final PhpProject project) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RunConfigLocal>() {
            @Override
            public RunConfigLocal run() {
                return new RunConfigLocal()
                        .setUrl(ProjectPropertiesSupport.getUrl(project))
                        .setIndexParentDir(FileUtil.toFile(ProjectPropertiesSupport.getWebRootDirectory(project)))
                        .setIndexRelativePath(ProjectPropertiesSupport.getIndexFile(project))
                        .setArguments(ProjectPropertiesSupport.getArguments(project));
            }
        });
    }

}
