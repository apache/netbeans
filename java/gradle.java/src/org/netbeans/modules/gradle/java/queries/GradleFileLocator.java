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

package org.netbeans.modules.gradle.java.queries;

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
public class GradleFileLocator implements LineConvertors.FileLocator {

    private ClassPath classpath;
    private final Project project;
    private static final Object LOCK = new Object();
    private static final Logger LOG = Logger.getLogger(GradleFileLocator.class.getName());

    public GradleFileLocator(Project project) {
        this.project = project;
        NbGradleProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                 //explicitly listing both RESOURCE and PROJECT properties, it's unclear if both are required but since some other places call addWatchedPath but don't listen it's likely required
                if (NbGradleProject.PROP_RESOURCES.equals(evt.getPropertyName()) || NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    synchronized (LOCK) {
                        classpath = null;
                    }
                }
            }
        });
    }

    @Override
    public FileObject find(String filename) {
        if (filename == null) {
            return null;
        }
        ClassPath cp;
        synchronized (LOCK) {
            if (classpath == null) {
                classpath = getProjectClasspath(project);
            }
            cp = classpath;
        }
        FileObject toRet = cp.findResource(filename);
        if (toRet == null) {
            LOG.log(Level.FINE, "#221053: Cannot find FileObject for {0}", filename);
        }
        return toRet;
    }

    private ClassPath getProjectClasspath(Project p) {
        ClassPath result;
        ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
        Set<FileObject> roots = new HashSet<>();
        Sources sources = ProjectUtils.getSources(p);
        if (sources != null) {
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup group : groups) {
                roots.add(group.getRootFolder());
            }
        }

        Set<ClassPath> setCP = new HashSet<>();
        if (cpp != null) {
            for (FileObject file : roots) {
                ClassPath path = cpp.findClassPath(file, ClassPath.COMPILE);
                setCP.add(path);
            }
        }

        for (ClassPath cp : setCP) {
            FileObject[] rootsCP = cp.getRoots();
            for (FileObject fo : rootsCP) {
                FileObject[] aaa = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
                roots.addAll(Arrays.asList(aaa));
            }
        }
        //TODO: Use something more specific in the future Ticket #81
        JavaPlatform platform = JavaPlatform.getDefault();

        if (platform != null) {
            roots.addAll(Arrays.asList(platform.getSourceFolders().getRoots()));
        }
        result = ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
        return result;
    }
}
