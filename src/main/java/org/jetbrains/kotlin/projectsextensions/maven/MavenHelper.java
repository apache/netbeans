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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    
    public static NbMavenProjectImpl getMainParent(NbMavenProjectImpl proj) throws IOException {
        NbMavenProjectImpl parent = getMavenProject(getParentProjectDirectory(proj.getProjectDirectory()));
        return parent != null ? parent : proj;
    }
    
    public static List<? extends Project> getDependencyProjects(NbMavenProjectImpl project){
        List<NbMavenProjectImpl> dependencyProjects = new ArrayList<NbMavenProjectImpl>();
//        try {
//            NbMavenProjectImpl mainProject = getMainParent(project);
//            List modules = mainProject.getOriginalMavenProject().getModules();
//            
//            List compileDependencies = project.getOriginalMavenProject().getCompileDependencies();
//            
//            for (Object dependency : compileDependencies) {
//                if (modules.contains(((Dependency) dependency).getArtifactId())){
//                    NbMavenProjectImpl depProject = getMavenProject(
//                            mainProject.getProjectDirectory().getFileObject(((Dependency) dependency).getArtifactId()));
//                    dependencyProjects.add(depProject);
//                }
//            }
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
       
        return dependencyProjects;
    }
    
}
