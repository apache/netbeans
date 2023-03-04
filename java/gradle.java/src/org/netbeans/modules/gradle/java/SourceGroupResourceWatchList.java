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

package org.netbeans.modules.gradle.java;

import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.spi.WatchedResourceProvider;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.classpath.ClassPathProviderImpl;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SourceGroupResourceWatchList implements WatchedResourceProvider {

    private final Project project;

    public SourceGroupResourceWatchList(Project project) {
        this.project = project;
    }

    @Override
    public Set<File> getWatchedResources() {
        Set<File> ret = Collections.<File>emptySet();
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null) {
            ret = new HashSet<>();
            for (GradleJavaSourceSet ss : gjp.getSourceSets().values()) {
                ret.addAll(ss.getAllDirs());
                for (File dir : ss.getJavaDirs()) {
                    ret.add(new File(dir, ClassPathProviderImpl.MODULE_INFO_JAVA));
                }
                for (File dir : ss.getGroovyDirs()) {
                    ret.add(new File(dir, ClassPathProviderImpl.MODULE_INFO_JAVA));
                }
            }
        }
        return ret;
    }

}
