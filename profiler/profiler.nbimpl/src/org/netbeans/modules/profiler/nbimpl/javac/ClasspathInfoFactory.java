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
package org.netbeans.modules.profiler.nbimpl.javac;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ClasspathInfoFactory {
    /**
     * Creates a {@linkplain ClasspathInfo} instance for the given project and all its subprojects, including sources and binaries
     * @param prj The project to create {@linkplain ClasspathInfo} instance for
     * @return Returns a {@linkplain ClasspathInfo} instance for the given project and all its subprojects, including sources and binaries
     */
    public static ClasspathInfo infoFor(Project prj) {
        return infoFor(prj, true);
    }
    
    /**
     * Creates a {@linkplain ClasspathInfo} instance for the given project, including sources and binaries
     * @param prj The project to create {@linkplain ClasspathInfo} instance for
     * @param includeSubprojects Should the subprojects be included as well?
     * @return Returns a {@linkplain ClasspathInfo} instance for the given project, including sources and binaries
     */
    public static ClasspathInfo infoFor(Project prj, final boolean includeSubprojects) {
        return infoFor(prj, includeSubprojects, true, true);
    }

    /**
     * Creates a {@linkplain ClasspathInfo} instance for the given project
     * @param prj The project to create {@linkplain ClasspathInfo} instance for
     * @param includeSubprojects Should the subprojects be included
     * @param includeSources Should the source be included
     * @param includeLibraries Should the binaries be included
     * @return Returns a {@linkplain ClasspathInfo} instance for the given project
     */
    public static ClasspathInfo infoFor(Project prj, final boolean includeSubprojects,
                                        final boolean includeSources, final boolean includeLibraries) {
        FileObject[] sourceRoots = ProjectUtilities.getSourceRoots(prj, includeSubprojects);

        if (((sourceRoots == null) || (sourceRoots.length == 0)) && !includeSubprojects) {
            sourceRoots = ProjectUtilities.getSourceRoots(prj, true);
        }

        final ClassPath cpEmpty = ClassPathSupport.createClassPath(new FileObject[0]);

        ClassPath cpSource = cpEmpty;
        if (includeSources) {
            if (sourceRoots.length == 0) {
                return null; // fail early
            }

            cpSource = ClassPathSupport.createClassPath(sourceRoots);
        }
        
        FileObject someFile = sourceRoots.length>0 ? sourceRoots[0] : prj.getProjectDirectory();
        ClassPath cpCompile = cpEmpty;
        if (includeLibraries) {
            java.util.List<URL> urlList = new ArrayList<URL>();
            cpCompile = ClassPath.getClassPath(someFile, ClassPath.COMPILE);
            cpCompile = cpCompile != null ? cpCompile : cpEmpty;
            
            // cleaning up compile classpatth; we need to get rid off all project's class file references in the classpath
            for (ClassPath.Entry entry : cpCompile.entries()) {
                SourceForBinaryQuery.Result rslt = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                FileObject[] roots = rslt.getRoots();

                if ((roots == null) || (roots.length == 0)) {
                    urlList.add(entry.getURL());
                }
                cpCompile = ClassPathSupport.createClassPath(urlList.toArray(new URL[0]));
            }
        }

        ClassPath cpBoot = includeLibraries ? ClassPath.getClassPath(someFile, ClassPath.BOOT) : cpEmpty;
        cpBoot = cpBoot != null ? cpBoot : cpEmpty;
        
        return ClasspathInfo.create(cpBoot, cpCompile, cpSource);
    }
}
