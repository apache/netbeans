/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.maven;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenHelper {
    
    private static final NbMavenProjectFactory PROJECT_FACTORY = 
            new NbMavenProjectFactory();
    private static final Map<Project, List<? extends Project>> depProjects = 
            new HashMap<Project, List<? extends Project>>();
    
    public static boolean hasParent(NbMavenProjectImpl project) {
        return getParentProjectDirectory(project.getProjectDirectory()) != null;
    }
    
    @Nullable
    public static NbMavenProjectImpl getMavenProject(FileObject dir) throws IOException {
        if (dir == null) {
            return null;
        }
        if (PROJECT_FACTORY.isProject(dir)){
            NbMavenProjectImpl project = (NbMavenProjectImpl) PROJECT_FACTORY.loadProject(
                    dir, 
                    new ProjectState(){
                        @Override
                        public void markModified() {}
                        @Override
                        public void notifyDeleted() throws IllegalStateException {}
                    });
            
            return project;
        }
        
        return null;
    }
    
    public static boolean isModuled(NbMavenProjectImpl project) {
        return !project.getOriginalMavenProject().getModules().isEmpty();
    }
    
    public static boolean isMavenMainModuledProject(NbMavenProjectImpl project) {
        if (isModuled(project)) {
            try {
                if (getMavenProject(project.getProjectDirectory().getParent()) == null) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    private static FileObject getParentProjectDirectory(FileObject proj) {
        if (PROJECT_FACTORY.isProject(proj.getParent())) {
            FileObject parent = getParentProjectDirectory(proj.getParent());
            
            if (parent != null) {
                return parent;
            }
            return proj.getParent();
        }
        
        return null;
    }
    
    private static FileObject getMainParentFolder(NbMavenProjectImpl proj) {
        FileObject projDir = proj.getProjectDirectory();
        FileObject parent = projDir;
        while (projDir != null) {
            projDir = getParentProjectDirectory(projDir);
            if (projDir != null) {
                parent = projDir;
            }
        }
        
        return parent;
    }
    
    public static NbMavenProjectImpl getMainParent(NbMavenProjectImpl proj) throws IOException {
        NbMavenProjectImpl parent = getMavenProject(getParentProjectDirectory(proj.getProjectDirectory()));
        return parent != null ? parent : proj;
    }
    
    private static Set<FileObject> allModules(FileObject parent) {
        Set<FileObject> modules = Sets.newHashSet();
        modules.add(parent);
        
        for (FileObject fo : parent.getChildren()) {
            if (fo.isFolder() && fo.getFileObject("pom.xml") != null) {
                modules.addAll(allModules(fo));
            }
        }
        
        return modules;
    }
    
    public static List<? extends Project> getDependencyProjects(NbMavenProjectImpl project){
        if (depProjects.get(project) == null) {
            depProjects.put(project, findDependencyProjects(project));
        }
        
        return depProjects.get(project);
    }
    
    private static List<? extends Project> findDependencyProjects(NbMavenProjectImpl project){
        List<NbMavenProjectImpl> dependencyProjects = new ArrayList<NbMavenProjectImpl>();
        List compileDependencies = project.getOriginalMavenProject().getCompileDependencies();
        List<String> dependencies = Lists.newArrayList();
        
        for (Object dependency : compileDependencies) {
            dependencies.add(((Dependency) dependency).getArtifactId());
        }
        
        FileObject mainParentFolder = getMainParentFolder(project);
        Set<FileObject> allModules = allModules(mainParentFolder);
        Set<FileObject> moduleDependencies = Sets.newHashSet();
        
        for (FileObject module : allModules) {
            if (dependencies.contains(module.getName())) {
                moduleDependencies.add(module);
            }
        }
        
        for (FileObject module : moduleDependencies) {
            try {
                NbMavenProjectImpl dep = getMavenProject(module);
                if (dep != null) {
                    dependencyProjects.add(dep);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
       
        return dependencyProjects;
    }
    
}
