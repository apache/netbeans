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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.projectsextensions.maven.buildextender.PomXmlModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenHelper {
    
    private static final Map<Project, List<? extends Project>> depProjects = new HashMap<>();
    
    private static final List<Project> askedToConfigure = new ArrayList<>();
    
    public static void configure(Project project) {
        if (askedToConfigure.contains(project)) {
            return;
        }
        
        MavenProject mavenProject = getOriginalMavenProject(project);
        if (mavenProject == null) return;
        if (!kotlinPluginConfigured(mavenProject)) {
            final PomXmlModifier pomModifier = new PomXmlModifier(project);
            ActionListener listener = new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    pomModifier.checkPom();
                }
            };
            NotificationDisplayer.getDefault().notify("Kotlin is not configured", 
                    new ImageIcon(ImageUtilities.loadImage("org/jetbrains/kotlin/kotlin.png")), 
                    "Configure Kotlin", listener);
        }
        
        askedToConfigure.add(project);
    }

    private static boolean kotlinPluginConfigured(MavenProject mavenProject) {
        Set<Artifact> pluginArtifacts = mavenProject.getPluginArtifacts();
        if (pluginArtifacts == null) return false;
        
        for (Artifact plugin : pluginArtifacts) {
            String groupId = plugin.getGroupId();
            String artifactId = plugin.getArtifactId();
            
            if (groupId.equals("org.jetbrains.kotlin") 
                    && artifactId.equals("kotlin-maven-plugin")) return true;
        }
        
        return false;
    }
    
    public static boolean hasParent(Project project) {
        return getParentProjectDirectory(project.getProjectDirectory()) != null;
    }
    
    public static MavenProject getOriginalMavenProject(Project proj) {
        Class clazz = proj.getClass();
        try {
            Method getOriginalProject = clazz.getMethod("getOriginalMavenProject");
            return (MavenProject) getOriginalProject.invoke(proj);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    public static NbMavenProject getProjectWatcher(Project proj) {
        Class clazz = proj.getClass();
        try {
            Method getProjectWatcher = clazz.getMethod("getProjectWatcher");
            return (NbMavenProject) getProjectWatcher.invoke(proj);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    @Nullable
    public static Project getMavenProject(FileObject dir) throws IOException {
        if (dir == null) {
            return null;
        }
        
        if (ProjectManager.getDefault().isProject(dir)){
            return ProjectManager.getDefault().findProject(dir);
        }
        
        return null;
    }
    
    public static boolean isModuled(Project project) {
        MavenProject originalProject = getOriginalMavenProject(project);
        if (originalProject == null) {
            return false;
        }
        return !originalProject.getModules().isEmpty();
    }
    
    public static boolean isMavenMainModuledProject(Project project) {
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
        if (ProjectManager.getDefault().isProject(proj.getParent())) {
            FileObject parent = getParentProjectDirectory(proj.getParent());
            
            if (parent != null) {
                return parent;
            }
            return proj.getParent();
        }
        
        return null;
    }
    
    private static FileObject getMainParentFolder(Project proj) {
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
    
    public static Project getMainParent(Project proj) throws IOException {
        Project parent = getMavenProject(getParentProjectDirectory(proj.getProjectDirectory()));
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
    
    public static List<? extends Project> getDependencyProjects(Project project){
        if (depProjects.get(project) == null) {
            depProjects.put(project, findDependencyProjects(project));
        }
        
        return depProjects.get(project);
    }
    
    private static List<? extends Project> findDependencyProjects(Project project){
        List<Project> dependencyProjects = new ArrayList<>();
        MavenProject originalProject = getOriginalMavenProject(project);
        if (originalProject == null) {
            return dependencyProjects;
        }
        List compileDependencies = originalProject.getCompileDependencies();
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
                Project dep = getMavenProject(module);
                if (dep != null) {
                    dependencyProjects.add(dep);
                }
            } catch (IOException ex) {
                KotlinLogger.INSTANCE.logException("Can't find module " + module.getName(), ex);
            }
        }
       
        return dependencyProjects;
    }
    
}
