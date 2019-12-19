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

package org.netbeans.modules.gradle.java;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ReplaceTokenProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class GradleJavaTokenProvider implements ReplaceTokenProvider {

    private static final Set<String> SUPPORTED = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "selectedClass",       //NOI18N
            "selectedMethod",      //NOI18N
            "affectedBuildTasks"   //NOI18N
    )));

    final Project project;

    public GradleJavaTokenProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<String> getSupportedTokens() {
        return SUPPORTED;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        Map<String, String> ret = new HashMap<>();
        processSelectedPackageAndClass(ret, context);
        processSelectedMethod(ret, context);
        processSourceSets(ret, context);
        return ret;
    }

    private void processSelectedPackageAndClass(final Map<String, String> map, Lookup context) {
        FileObject fo = RunUtils.extractFileObjectfromLookup(context);
        GradleJavaProject gjp = GradleJavaProject.get(project);
        String className = evaluateClassName(gjp, fo);
        if (className != null) {
            map.put("selectedClass", className);
        }
    }

    private void processSelectedMethod(final Map<String, String> map, Lookup context) {
        SingleMethod method = context.lookup(SingleMethod.class);
        FileObject fo = method != null ? method.getFile() : RunUtils.extractFileObjectfromLookup(context);
        if ((fo != null) && fo.isData()) {
            GradleJavaProject gjp = GradleJavaProject.get(project);
            String className = evaluateClassName(gjp, fo);
            String selectedMethod = method != null ? className + '.' + method.getMethodName() : className;
            map.put("selectedMethod", selectedMethod);
        }
    }

    private void processSourceSets(final Map<String, String> map, Lookup context) {
        FileObject[] fos = RunUtils.extractFileObjectsfromLookup(context);
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if ((fos.length > 0) && (gjp != null)) {
            Set<String> buildTasks = new HashSet<>();
            for (FileObject fo : fos) {
                File f = FileUtil.toFile(fo);
                GradleJavaSourceSet ss = gjp.containingSourceSet(f);
                if (ss != null) {
                    Set<GradleJavaSourceSet.SourceType> types = ss.getSourceTypes(f);
                    for (GradleJavaSourceSet.SourceType type : types) {
                        buildTasks.add(ss.getBuildTaskName(type));
                    }
                }
            }
            StringBuilder tasks = new StringBuilder();
            for (String task : buildTasks) {
                tasks.append(task).append(' ');
            }
            map.put("affectedBuildTasks", tasks.toString()); //NOI18N
        }
    }

    private String evaluateClassName(GradleJavaProject gjp, FileObject fo) {
        String ret = null;
        if ((gjp != null) && (fo != null)) {
            File f = FileUtil.toFile(fo);
            GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
            if (sourceSet != null) {
                String relPath = sourceSet.relativePath(f);
                ret = (relPath.lastIndexOf('.') > 0 ?
                        relPath.substring(0, relPath.lastIndexOf('.')) :
                        relPath).replace('/', '.');
                if (fo.isFolder()) {
                    ret = ret + '*';
                }
            }
        }
        return ret;
    }

}
