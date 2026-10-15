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

package org.netbeans.modules.maven.queries;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * JUnit tests queries.
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=MultipleRootsUnitTestForSourceQueryImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenTestForSourceImpl implements MultipleRootsUnitTestForSourceQueryImplementation {


    private final Project project;

    public MavenTestForSourceImpl(Project proj) {
        project = proj;
    }


    @Override
    public URL[] findUnitTests(FileObject fileObject) {
        try {
            List<URL> urls = new ArrayList<>();
            for (URI sourceRoot : project.getLookup().lookup(NbMavenProjectImpl.class).getSourceRoots(true)) {
                URL url = sourceRoot.toURL();
                FileObject fl = URLMapper.findFileObject(url);
                if (fileObject == fl) {
                    return null;
                }
                urls.add(url);
            }
            return urls.isEmpty() ? null : urls.toArray(new URL[0]);
        } catch (MalformedURLException exc) {
            ErrorManager.getDefault().notify(exc);
        }
        return null;
    }

    @Override
    public URL[] findSources(FileObject fileObject) {
        try {
            List<URL> urls = new ArrayList<>();
            for (URI sourceRoot : project.getLookup().lookup(NbMavenProjectImpl.class).getSourceRoots(false)) {
                URL url = sourceRoot.toURL();
                FileObject fl = URLMapper.findFileObject(url);
                if (fileObject == fl) {
                    return null;
                }
                urls.add(url);
            }
            return urls.isEmpty() ? null : urls.toArray(new URL[0]);
        } catch (MalformedURLException exc) {
            ErrorManager.getDefault().notify(exc);
        }
        return null;
    }

}
