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

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Run configuration for SCRIPT.
 */
public final class RunConfigScript extends BaseRunConfig<RunConfigScript> {

    private boolean useDefaultInterpreter;
    private String interpreter;
    private String options;
    private String workDir;


    private RunConfigScript() {
    }

    public static PhpProjectProperties.RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.SCRIPT;
    }

    public static String getDisplayName() {
        return getRunAsType().getLabel();
    }

    //~ Factories

    public static RunConfigScript create() {
        return new RunConfigScript();
    }

    public static RunConfigScript forProject(final PhpProject project) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RunConfigScript>() {
            @Override
            public RunConfigScript run() {
                return new RunConfigScript()
                        .setUseDefaultInterpreter(false) // always false, interpreter itself is correctly set below
                        .setInterpreter(ProjectPropertiesSupport.getPhpInterpreter(project))
                        .setOptions(ProjectPropertiesSupport.getPhpArguments(project))
                        .setIndexParentDir(FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)))
                        .setIndexRelativePath(ProjectPropertiesSupport.getIndexFile(project))
                        .setArguments(ProjectPropertiesSupport.getArguments(project))
                        .setWorkDir(ProjectPropertiesSupport.getWorkDir(project));
            }
        });
    }

    //~ Methods

    @Override
    public File getIndexFile() {
        // #237370 - index file can start with "../"
        return FileUtil.normalizeFile(super.getIndexFile());
    }

    public String getHint() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(interpreter);
        if (StringUtils.hasText(options)) {
            sb.append(" "); // NOI18N
            sb.append(options);
        }
        if (StringUtils.hasText(indexRelativePath)) {
            sb.append(" "); // NOI18N
            sb.append(indexRelativePath);
        }
        if (StringUtils.hasText(arguments)) {
            sb.append(" "); // NOI18N
            sb.append(arguments);
        }
        return sb.toString();
    }

    //~ Getters & Setters

    public String getInterpreter() {
        return interpreter;
    }

    public RunConfigScript setInterpreter(String interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public String getOptions() {
        return options;
    }

    public RunConfigScript setOptions(String options) {
        this.options = options;
        return this;
    }

    public boolean getUseDefaultInterpreter() {
        return useDefaultInterpreter;
    }

    public RunConfigScript setUseDefaultInterpreter(boolean useDefaultInterpreter) {
        this.useDefaultInterpreter = useDefaultInterpreter;
        return this;
    }

    public String getWorkDir() {
        return workDir;
    }

    public RunConfigScript setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

}
