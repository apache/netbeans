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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=SharabilityQueryImplementation2.class, projectType="org-netbeans-modules-maven")
public class MavenSharabilityQueryImpl implements SharabilityQueryImplementation2 {
    
    private final Project project;

    public MavenSharabilityQueryImpl(Project proj) {
        project = proj;
    }
    
    public @Override SharabilityQuery.Sharability getSharability(URI uri) {
        //#119541 for the project's root, return MIXED right away.
        File file = FileUtil.normalizeFile(Utilities.toFile(uri));
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null && fo.equals(project.getProjectDirectory())) {
            return SharabilityQuery.Sharability.MIXED;
        }
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        // is this condition necessary?
        if (!file.getAbsolutePath().startsWith(basedir.getAbsolutePath())) {
            return SharabilityQuery.Sharability.UNKNOWN;
        }
        if (basedir.equals(file.getParentFile())) {
            // Interesting cases are of direct children.
            if (file.getName().equals("pom.xml")) { // NOI18N
                return SharabilityQuery.Sharability.SHARABLE;
            }
            if ("nbproject".equals(file.getName())) { //NOI18N
                // screw the netbeans profiler directory creation.
                // #98662
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
            if (file.getName().startsWith("nbactions")) { //NOI18N
                //non shared custom configurations shall not be added to version control.
                M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
                if (configs != null) {
                    Collection<M2Configuration> col = configs.getNonSharedConfigurations();
                    for (M2Configuration conf : col) {
                        if (file.getName().equals(M2Configuration.getFileNameExt(conf.getId()))) {
                            return SharabilityQuery.Sharability.NOT_SHARABLE;
                        }
                    }
                }
            }
            if (file.getName().equals("src")) { // NOI18N
                // hardcoding this name since Maven will only report particular subtrees
                return SharabilityQuery.Sharability.SHARABLE; // #174010
            }
        }

        //this part is slow if invoked on built project that is not opened (needs to load the embedder)
        //can it be replaced with code not touching the embedder?
        MavenProject proj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        Build build = proj.getBuild();
        if (build != null && build.getDirectory() != null) {
            File target = new File(build.getDirectory());
            if (target.equals(file) || file.getAbsolutePath().startsWith(target.getAbsolutePath())) {
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
        }

        // Some other subdir with potentially unknown contents.
        if (file.isDirectory()) {
            return SharabilityQuery.Sharability.MIXED;
        } else {
            return SharabilityQuery.Sharability.UNKNOWN;
        }
    }
    
}
