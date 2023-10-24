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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import org.netbeans.modules.gradle.java.execute.JavaRunUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public class Utils {

    public static ClassPath getJdkSources(Project project) {
        JavaPlatform jdk = JavaRunUtils.getActivePlatform(project).second();
        if (jdk != null) {
            return jdk.getSourceFolders();
        }
        return null;
    }

    public static ClassPath getSources(Project project) {
        ProjectSourcesClassPathProvider pgcpp = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        List<SourceForBinaryQueryImplementation2> sourceQueryImpls = new ArrayList<>(2);
        sourceQueryImpls.addAll(project.getLookup().lookupAll(SourceForBinaryQueryImplementation2.class));
        sourceQueryImpls.addAll(Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation2.class));

        Set<FileObject> srcs = new LinkedHashSet<>();
        for (ClassPath projectSourcePath : pgcpp.getProjectClassPath(ClassPath.SOURCE)) {
            srcs.addAll(Arrays.asList(projectSourcePath.getRoots()));
        }

        for (ClassPath cp : pgcpp.getProjectClassPath(ClassPath.EXECUTE)) {
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = entry.getURL();
                SourceForBinaryQueryImplementation2.Result ret;
                for (SourceForBinaryQueryImplementation2 sourceQuery : sourceQueryImpls) {
                    ret = sourceQuery.findSourceRoots2(url);
                    if (ret != null) {
                        List<FileObject> roots = Arrays.asList(ret.getRoots());
                        if (!roots.isEmpty()) {
                            srcs.addAll(roots);
                            break;
                        }
                    }
                }
            }
        }
        FileObject[] roots = srcs.toArray(new FileObject[srcs.size()]);
        return ClassPathSupport.createClassPath(roots);
    }

}
