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

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.MavenEmbedder.ModelDescription;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class ReactorChecker implements PrerequisitesChecker {
    
    public @Override boolean checkRunConfig(RunConfig config) {
        RunConfig preExecution = config.getPreExecution();
        if (preExecution != null && !checkRunConfig(preExecution)) {
            return false;
        }
        if (preExecution == null && (config.getReactorStyle() == RunConfig.ReactorStyle.NONE)) {
            return true;
        }
        File dir = config.getExecutionDirectory();
        FileObject fo = FileUtil.toFileObject(dir);
        Project p = config.getProject();
        if (p == null || fo != p.getProjectDirectory()) {
            // Custom <basedir> perhaps? Skip.
            return true;
        }
        NbMavenProject mavenprj = p.getLookup().lookup(NbMavenProject.class);
        if (mavenprj == null) {
            // Unloadable?
            return true;
        }
        NbMavenProject reactor = findReactor(mavenprj);
        File reactorRoot = reactor.getMavenProject().getBasedir();
        
        if (config.getReactorStyle() == RunConfig.ReactorStyle.NONE) {
            if (preExecution != null && "build-with-dependencies".equals(preExecution.getActionName()) ) { // NOI18N
                if (reactor == mavenprj) {
                    config.setPreExecution(null);
                }
            }
            return true;
        }
        if (reactor != mavenprj) {
            try {
                M2Configuration cfg = ProjectManager.getDefault().findProject(FileUtil.toFileObject(reactorRoot)).getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration();
                if (cfg != null) {
                    List<String> reactorProfiles = cfg.getActivatedProfiles();
                    if (!reactorProfiles.isEmpty()) {
                        List<String> profiles = new ArrayList<String>(config.getActivatedProfiles());
                        profiles.addAll(reactorProfiles);
                        config.setActivatedProfiles(profiles);
                    }
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        config.setExecutionDirectory(reactorRoot);
        return true;
    }
    
    /**
     * Tries to find the reactor root starting from what may be just a submodule.
     * The intent is that running {@code mvn -f $reactor/pom.xml --projects $module} would work.
     * @param module a project to start the search at
     * @return its apparent reactor root; maybe just the same project
     */
    public static @NonNull NbMavenProject findReactor(@NonNull NbMavenProject module) { // #197232
        MavenProject prj = NbMavenProject.getPartialProject(module.getMavenProject());
        List<ModelDescription> models = MavenEmbedder.getModelDescriptors(prj);
        File moduleDir = prj.getBasedir();
        File current = moduleDir;
        if (current != null && models != null) { //models are null for totally broken projects..
            boolean first = true;
            for (ModelDescription model : models) {
                if (first) { //ignore the first value, it's the current project
                    first = false;
                    continue;
                }
                File loc = model.getLocation();
                if (loc == null || loc.getName().endsWith(".pom")) {
                    break;
                }
                File modelDir = loc.getParentFile();
                if (listsModule(modelDir, current, model.getModules())) {
                    current = modelDir;
                } else {
                    break;
                }
            }
            if (!moduleDir.equals(current)) {
                NbMavenProject loaded = load(current);
                if (loaded != null) {
                    return findReactor(loaded);
                }
            }
        }
        NbMavenProject p = load(prj.getBasedir().getParentFile());
        if (p != null) {
            MavenProject mp = NbMavenProject.getPartialProject(p.getMavenProject());
            if (listsModule(moduleDir.getParentFile(), moduleDir, mp.getModules())) {
                return findReactor(p);
            }
        }
        return module;
    }
    private static boolean listsModule(File parentDir, File moduleDir, List<String> modules) {
        for (String module : modules) {
            if (moduleDir.equals(FileUtilities.resolveFilePath(parentDir, module))) {
                return true;
            }
        }
        return false;
    }
    private static @CheckForNull NbMavenProject load(File parentDir) {
        if (parentDir == null) {
            return null;
        }
        FileObject d = FileUtil.toFileObject(parentDir);
        if (d != null) {
            try {
                Project p = ProjectManager.getDefault().findProject(d);
                if (p != null) {
                    return p.getLookup().lookup(NbMavenProject.class);
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

}
