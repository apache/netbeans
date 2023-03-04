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

package org.netbeans.modules.gradle.actions;

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ReplaceTokenProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleBaseTokenProvider implements ReplaceTokenProvider {

    private static final Set<String> SUPPORTED = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "projectDir",
            "rootDir",
            "buildDir",
            "projectName",
            "projectPath",
            "group",
            "version",
            "status",
            "description",
            "selectedFile",
            "selectedFileName"
    )));
    
    final Project project;

    public GradleBaseTokenProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<String> getSupportedTokens() {
        return SUPPORTED;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        Map<String, String> ret = new HashMap<>();
        GradleBaseProject gbp = GradleBaseProject.get(project);
        ret.put("projectDir", gbp.getProjectDir().getAbsolutePath()); //NOI18N
        ret.put("rootDir", gbp.getRootDir().getAbsolutePath()); //NOI18N
        ret.put("buildDir", gbp.getBuildDir().getAbsolutePath()); //NOI18N
        ret.put("projectName", gbp.getName()); //NOI18N
        ret.put("projectPath", gbp.getPath()); //NOI18N
        ret.put("group", gbp.getGroup()); //NOI18N
        ret.put("version", gbp.getVersion()); //NOI18N
        ret.put("status", gbp.getStatus());  //NOI18N
        ret.put("description", gbp.getDescription()); //NOI18N

        FileObject fo = RunUtils.extractFileObjectfromLookup(context);
        if (fo != null) {
            ret.put("selectedFile", FileUtil.toFile(fo).getAbsolutePath());
            ret.put("selectedFileName", fo.getNameExt());
        }

        return ret;
    }

}
