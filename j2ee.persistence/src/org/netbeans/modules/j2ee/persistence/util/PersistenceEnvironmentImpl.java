/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    private Logger logger = Logger.getLogger(PersistenceEnvironmentImpl.class.getName());
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
        List<URL> projectClassPathEntries = new ArrayList<URL>();
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
        List<URL> projectClassPathEntries = new ArrayList<URL>();
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
