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
package org.netbeans.modules.j2ee.persistence.util;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sp153251
 */
@ProjectServiceProvider(service=PersistenceEnvironment.class, projectType={
    "org-netbeans-modules-maven",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject"
})
public class PersistenceEnvironmentImpl implements PersistenceEnvironment{
        /** Handle to the current project to which this HibernateEnvironment is bound*/
    private Project project;
    private WeakReference<CustomClassLoader> loaderRef;

    /**
     * Creates a new hibernate environment for this NetBeans project.
     *
     * @param project NB project.
     */
    public PersistenceEnvironmentImpl(Project project) {
        this.project = project;
    }
    /**
     * Prepares and returns a custom classloader for this project.
     * The classloader is capable of loading project classes and resources.
     * 
     * @param classpaths, custom classpaths that are registered along with project based classpath.
     * @return classloader which is a URLClassLoader instance.
     */
    @Override
    public ClassLoader getProjectClassLoader(URL[] classpaths) {
        CustomClassLoader customClassLoader = new CustomClassLoader(classpaths, getClass().getClassLoader());
        if(loaderRef !=null){
            CustomClassLoader cL = loaderRef.get();
            if(cL != null){
                URL[] oldUrls = cL.getURLs();
                classpaths = customClassLoader.getURLs();//reassign just in case if CustomClassLoader may modify assigned urls to have proper comparison below.
                if(Arrays.equals(oldUrls, classpaths)) {
                    return cL;
                }
            }
        }
        loaderRef = new WeakReference<CustomClassLoader>((CustomClassLoader) customClassLoader);
        return customClassLoader;
    }
    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @param projectFile may not be used in method realization
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    @Override
    public List<URL> getProjectClassPath(FileObject projectFile) {
        List<URL> projectClassPathEntries = new ArrayList<>();
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return projectClassPathEntries;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPathProvider cpProv = project.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpProv.findClassPath(sourceRoot, ClassPath.EXECUTE);
        if(cp == null){
            cp = cpProv.findClassPath(sourceRoot, ClassPath.COMPILE);
        }
        for (ClassPath.Entry cpEntry : cp.entries()) {
            if(cpEntry.isValid()){
                //if project isn't build, there may be number of invalid entries and may be in some other cases
                projectClassPathEntries.add(cpEntry.getURL());
            }
        }

        return projectClassPathEntries;
    }

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    @Override
    public List<URL> getProjectClassPath() {
        List<URL> projectClassPathEntries = new ArrayList<>();
        for (SourceGroup sourceGroup : getSourceGroups(project)) {
            if (sourceGroup == null) {
                continue;
            }
            ClassPath cp = ClassPath.getClassPath(sourceGroup.getRootFolder(), ClassPath.COMPILE);

            for (ClassPath.Entry cpEntry : cp.entries()) {
                projectClassPathEntries.add(cpEntry.getURL());
            }
        }

        return projectClassPathEntries;
    }

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public FileObject getLocation() {
        return PersistenceLocation.getLocation(project);
    }
    
    
    
    private static SourceGroup[] getSourceGroups(Project project) {
        Sources projectSources = ProjectUtils.getSources(project);
        // first, try to get resources
        SourceGroup[] resources = projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (resources.length > 0) {
            return resources;
        }
        // try to create it
        SourceGroup resourcesSourceGroup = SourceGroupModifier.createSourceGroup(
            project, JavaProjectConstants.SOURCES_TYPE_RESOURCES, JavaProjectConstants.SOURCES_HINT_MAIN);
        if (resourcesSourceGroup != null) {
            return new SourceGroup[] {resourcesSourceGroup};
        }
        // fallback to java sources
        return projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
}
