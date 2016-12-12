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
package org.jetbrains.kotlin.projectsextensions.maven.classpath;

import com.google.common.collect.Lists;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenExtendedClassPath implements ClassPathExtender {

    private final Project project;
    private ClassPath boot = null;
    private ClassPath compile = null;
    private ClassPath execute = null;
    private ClassPath source = null;
    
    public MavenExtendedClassPath(Project project) {
        this.project = project;
        createClasspath();
    }
    
    private ClassPath getClasspath(List<String> paths) throws DependencyResolutionRequiredException, MalformedURLException {
        Set<URL> classpaths = new HashSet<>();
        if (paths == null) {
            return ClassPath.EMPTY;
        }
        Set<String> classpath = new HashSet<>();
        classpath.addAll(paths);
        
        for (String path : classpath) {
            if (path == null) continue;
            
            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                continue;
            }

            FileObject fileObject = FileUtil.toFileObject(file);
            if (FileUtil.isArchiveFile(fileObject)) {
                fileObject = FileUtil.getArchiveRoot(fileObject);
            }
            if (fileObject != null) {
                classpaths.add(fileObject.toURL());
            }
        }
        
        return ClassPathSupport.createClassPath(classpaths.toArray(new URL[classpaths.size()]));
    }
    
    private List<String> getCompileClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        
        List<String> compileClasspath = Lists.newArrayList();
        List<String> dependencyProjectsNames = Lists.newArrayList();
        for (Project depProj : MavenHelper.getDependencyProjects(proj)) {
            dependencyProjectsNames.add(depProj.getProjectDirectory().getName());
        }
        
        Set<Artifact> artifacts = mavenProj.getArtifacts();
        for (Artifact artifact : artifacts) {
            if (dependencyProjectsNames.contains(artifact.getArtifactId())) continue;
            
            File artifactFile = artifact.getFile();
            if (artifactFile == null) continue;
            
            compileClasspath.add(artifactFile.getAbsolutePath());
        }

        return compileClasspath;
    }
    
    private List<String> getRuntimeClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        List<String> runtimeClasspath = mavenProj.getRuntimeClasspathElements();
        if (runtimeClasspath == null || runtimeClasspath.isEmpty()) {
            KotlinLogger.INSTANCE.logInfo(proj.getProjectDirectory().getPath() + 
                    " runtime classpath is empty");
        }
        
        return runtimeClasspath;
    }
    
    private List<String> getCompileSourceRoots(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        List<String> compileClasspath = mavenProj.getCompileSourceRoots();
        if (compileClasspath == null || compileClasspath.isEmpty()) {
            KotlinLogger.INSTANCE.logInfo(proj.getProjectDirectory().getPath() + 
                    " compile source roots are empty");
        }
        
        return compileClasspath;
    }
    
    private List<String> getSystemClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        List<String> systemClasspath = mavenProj.getSystemClasspathElements();
        if (systemClasspath == null || systemClasspath.isEmpty()) {
            KotlinLogger.INSTANCE.logInfo(proj.getProjectDirectory().getPath() + 
                    " system classpath is empty");
        }
        
        return systemClasspath;
    }
    
    private JavaPlatform getJavaPlatform(Project project) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) return null;
        
        try {
            Method method = provider.getClass().getMethod("getJavaPlatform");
            return (JavaPlatform) method.invoke(provider);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private List<String> getTestClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        List<String> testClasspath = mavenProj.getTestClasspathElements();
        if (testClasspath == null || testClasspath.isEmpty()) {
            KotlinLogger.INSTANCE.logInfo(proj.getProjectDirectory().getPath() + 
                    " test classpath is empty");
        }
        
        return testClasspath;
    }
    
    private void createClasspath() {
        try {
            compile = getClasspath(getCompileClasspathElements(project));
            execute = getClasspath(getRuntimeClasspathElements(project));
            source = getClasspath(getCompileSourceRoots(project));
            
            JavaPlatform javaPlatform = getJavaPlatform(project);
            if (javaPlatform != null) {
                boot = javaPlatform.getBootstrapLibraries();
            } else {
                boot = ClassPath.EMPTY;
            }
            
            List<String> javaClasspathElements = new ArrayList<>();
            javaClasspathElements.addAll(getTestClasspathElements(project));
            compile = ClassPathSupport.createProxyClassPath(getClasspath(getCompileClasspathElements(project)), 
                    getClasspath(javaClasspathElements));
            
        } catch (DependencyResolutionRequiredException | MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {
        switch (type) {
            case ClassPath.COMPILE:
                return compile;
            case ClassPath.EXECUTE:
                return execute;
            case ClassPath.SOURCE:
                return source;
            case ClassPath.BOOT:
                return boot;
            default:
                return null;
        }
    }
    
}
