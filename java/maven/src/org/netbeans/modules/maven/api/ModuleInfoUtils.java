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
package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ModuleInfoSupport;

/**
 *
 * @author Tomas Stupka
 */
public final class ModuleInfoUtils {
    private static final String MODULE_INFO = "module-info.java"; // NOI18N
    
    /**
     * Adds the given artifacts as require entries into module-info.java.
     * The artifact scope determines the target module-info.java file - main or test.
     * If there is no module-info.java file then nothing happens.
     * 
     * @param project
     * @param artifacts 
     */
    public static void addRequires(NbMavenProject project, Collection<? extends Artifact> artifacts) {
        ModuleInfoSupport.addRequires(
            project.getMavenProject(), 
            artifacts.stream()
                .filter((a) -> Artifact.SCOPE_COMPILE.equals(a.getScope()) || Artifact.SCOPE_TEST.equals(a.getScope()))
                .collect(Collectors.toList()));
    }
    
    /**
     * Determines if the configured maven-compiler-plugin isn't too old in case 
     * there is a main module-info.java in the given project (has to be >= 3.6).
     * 
     * @param prj the project to be checked
     * @return <code>true</code> if there is no mofule-info file or if the m-c-p version is new enough (>= 3.6). Otherwise <code>false</code>
     */
    public static boolean checkModuleInfoAndCompilerFit(Project prj) {
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        if (nbprj == null) {
            return true;
        }
        
        if (!hasModuleInfo(nbprj)) {
            return true;
        }
        
        String version = PluginPropertyUtils.getPluginVersion(nbprj.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        if (version == null) {
            return true;
        }
        
        return new ComparableVersion(version).compareTo(new ComparableVersion(Constants.PLUGIN_COMPILER_VERSION_SUPPORTING_JDK9)) >= 0;
    }    
    
    private static boolean hasModuleInfo(NbMavenProject nbprj) {
        MavenProject mavenProject = nbprj.getMavenProject();        
        return hasModuleInfoInSourceRoot(mavenProject.getCompileSourceRoots()) || 
               hasModuleInfoInSourceRoot(mavenProject.getTestCompileSourceRoots());
    }

    private static boolean hasModuleInfoInSourceRoot(List<String> sourceRoots) {        
        for (String sourceRoot : sourceRoots) {
            if (new File(sourceRoot, MODULE_INFO).exists()) {
                return true;
            }
        }
        return false;
    }
}
