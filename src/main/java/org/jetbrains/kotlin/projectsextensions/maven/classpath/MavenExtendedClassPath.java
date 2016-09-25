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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath.ClassPathProviderImpl;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
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
        Set<URL> classpaths = new HashSet<URL>();
        if (paths == null) {
            return ClassPathSupport.createClassPath(classpaths.toArray(new URL[0]));
        }
        Set<String> classpath = new HashSet<String>();
        classpath.addAll(paths);
        
        for (String path : classpath) {
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
        
        return mavenProj.getCompileClasspathElements();
    }
    
    private List<String> getRuntimeClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        
        return mavenProj.getRuntimeClasspathElements();
    }
    
    private List<String> getCompileSourceRoots(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        
        return mavenProj.getCompileSourceRoots();
    }
    
    private List<String> getSystemClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        
        return mavenProj.getSystemClasspathElements();
    }
    
    private List<String> getTestClasspathElements(Project proj) throws DependencyResolutionRequiredException {
        MavenProject mavenProj = MavenHelper.getOriginalMavenProject(proj);
        if (mavenProj == null) {
            return Collections.emptyList();
        }
        
        return mavenProj.getTestClasspathElements();
    }
    
    private void createClasspath() {
        try {
            compile = getClasspath(getCompileClasspathElements(project));
            execute = getClasspath(getRuntimeClasspathElements(project));
            source = getClasspath(getCompileSourceRoots(project));
            
            ClassPathProviderImpl impl = new ClassPathProviderImpl(project);
            ClassPath javaPlatform = impl.getJavaPlatform().getBootstrapLibraries();
            
            List<String> javaClasspathElements = new ArrayList<String>();
            javaClasspathElements.addAll(getSystemClasspathElements(project));
            javaClasspathElements.addAll(getTestClasspathElements(project));
            boot = ClassPathSupport.createProxyClassPath(getClasspath(javaClasspathElements), javaPlatform);
            
        } catch (DependencyResolutionRequiredException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {

        if (type.equals(ClassPath.COMPILE)) {
            return compile;
        } else if (type.equals(ClassPath.EXECUTE)) {
            return execute;
        } else if (type.equals(ClassPath.SOURCE)) {
            return source;
        } else if (type.equals(ClassPath.BOOT)) {
            return boot;
        }
        
        return null;
    }
    
}
