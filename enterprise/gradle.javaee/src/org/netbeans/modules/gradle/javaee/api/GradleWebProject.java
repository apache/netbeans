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

package org.netbeans.modules.gradle.javaee.api;

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.io.File;
import java.util.Set;
import org.netbeans.api.project.Project;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleWebProject {
    
    File mainWar;
    File webAppDir;
    File webXml;
    File explodedWarDir;
    Set<File> classpath;

    public File getMainWar() {
        return mainWar;
    }

    public File getWebAppDir() {
        return webAppDir;
    }

    public File getWebXml() {
        return webXml;
    }

    public File getExplodedWarDir() {
        return explodedWarDir;
    }

    public Set<File> getClasspath() {
        return classpath;
    }
    
    public static GradleWebProject get(Project project) {
        return get(project.getLookup().lookup(NbGradleProject.class));
    }

    public static GradleWebProject get(NbGradleProject project) {
        return project != null ? project.projectLookup(GradleWebProject.class) : null;
    }
}
