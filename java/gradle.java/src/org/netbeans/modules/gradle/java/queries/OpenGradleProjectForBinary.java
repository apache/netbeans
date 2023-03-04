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
package org.netbeans.modules.gradle.java.queries;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author lkishalmi
 */
@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class, position = 105),
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class, position = 105)
})
public class OpenGradleProjectForBinary implements SourceForBinaryQueryImplementation2 {

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        if ("jar".equals(binaryRoot.getProtocol())) {
            try {
                URI uri = FileUtil.getArchiveFile(binaryRoot).toURI();
                if ("file".equals(uri.getScheme())) {
                    File jar = new File(uri);
                    Map<String, Project> projectArchives = projectArchives();
                    Project p = projectArchives.get(jar.getName());
                    if (p != null) {
                        GradleJavaProject gjp = GradleJavaProject.get(p);
                        File archive = gjp.getArchive(GradleJavaProject.CLASSIFIER_NONE);
                        URL root = FileUtil.urlForArchiveOrDir(archive);
                        SourceForBinaryQueryImplementation2 query = p.getLookup().lookup(SourceForBinaryQueryImplementation2.class);
                        return (query != null) ? query.findSourceRoots2(root) : null;
                    }
                }
            } catch (URISyntaxException ex) {
            }
        }
        return null;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    Map<String, Project> projectArchives() {
        Map<String, Project> ret = new HashMap<>();
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (Project project : projects) {
            GradleJavaProject gjp = GradleJavaProject.get(project);
            if (gjp != null) {
                File archive = gjp.getArchive(GradleJavaProject.CLASSIFIER_NONE);
                if (archive != null) {
                    ret.put(archive.getName(), project);
                }
            }
        }
        return ret;
    }
}
