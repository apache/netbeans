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

package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class ClassPathProviderImpl implements ClassPathProvider {

    public static final String SOURCE_CP = "classpath/html5"; //NOI18N

    private final ClientSideProject project;


    public ClassPathProviderImpl(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (SOURCE_CP.equals(type)) {
            if (ClientSideProjectUtilities.isParentOrItself(project.getSourcesFolder(), file)
                    || ClientSideProjectUtilities.isParentOrItself(project.getSiteRootFolder(), file)
                    || ClientSideProjectUtilities.isParentOrItself(project.getTestsFolder(false), file)
                    || ClientSideProjectUtilities.isParentOrItself(project.getTestsSeleniumFolder(false), file)) {
                return project.getSourceClassPath();
            }
        }
        return null;
    }

    public static ClassPath createProjectClasspath(PathResourceImplementation pathResourceImplementation) {
        assert pathResourceImplementation != null;
        return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>singletonList(pathResourceImplementation));
    }

    public static class PathImpl implements FilteringPathResourceImplementation {

        private final ClientSideProject project;
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);

        public PathImpl(ClientSideProject project) {
            this.project = project;
            this.project.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ClientSideProjectConstants.PROJECT_SOURCE_FOLDER.equals(evt.getPropertyName())
                            || ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER.equals(evt.getPropertyName())
                            || ClientSideProjectConstants.PROJECT_TEST_FOLDER.equals(evt.getPropertyName())
                            || ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER.equals(evt.getPropertyName())
                            || evt.getPropertyName().startsWith("file.reference.")) { // NOI18N
                        fireRootsChanged();
                    }
                }
            });
        }

        public void fireRootsChanged() {
            support.firePropertyChange(PROP_ROOTS, null, null);
        }

        @Override
        public boolean includes(URL root, String resource) {
            return !resource.startsWith("nbproject"); //NOI18N
        }

        @Override
        public URL[] getRoots() {
            List<URL> roots = new ArrayList<>();
            FileObject sourcesFolder = project.getSourcesFolder();
            FileObject siteRootFolder = project.getSiteRootFolder();
            boolean isSourcesParentOfSiteRoot = ClientSideProjectUtilities.isParentOrItself(sourcesFolder, siteRootFolder);
            boolean isSiteRootParentOfSources = ClientSideProjectUtilities.isParentOrItself(siteRootFolder, sourcesFolder);
            if (isSourcesParentOfSiteRoot
                    && isSiteRootParentOfSources) {
                // same folders
                assert sourcesFolder != null;
                assert sourcesFolder.equals(siteRootFolder) : sourcesFolder + " should equal to " + siteRootFolder;
                roots.add(sourcesFolder.toURL());
            } else if (isSourcesParentOfSiteRoot) {
                assert sourcesFolder != null;
                roots.add(sourcesFolder.toURL());
            } else if (isSiteRootParentOfSources) {
                assert siteRootFolder != null;
                roots.add(siteRootFolder.toURL());
            } else {
                if (sourcesFolder != null) {
                    roots.add(sourcesFolder.toURL());
                }
                if (siteRootFolder != null) {
                    roots.add(siteRootFolder.toURL());
                }
            }
            FileObject testsFolder = project.getTestsFolder(false);
            if (testsFolder != null
                    && !ClientSideProjectUtilities.isParentOrItself(sourcesFolder, testsFolder)
                    && !ClientSideProjectUtilities.isParentOrItself(siteRootFolder, testsFolder)) {
                roots.add(testsFolder.toURL());
            }
            FileObject testsSeleniumFolder = project.getTestsSeleniumFolder(false);
            if (testsSeleniumFolder != null
                    && !ClientSideProjectUtilities.isParentOrItself(sourcesFolder, testsSeleniumFolder)
                    && !ClientSideProjectUtilities.isParentOrItself(siteRootFolder, testsSeleniumFolder)) {
                roots.add(testsSeleniumFolder.toURL());
            }
            for (PlatformProvider provider : project.getPlatformProviders()) {
                roots.addAll(provider.getSourceRoots(project));
            }
            return roots.toArray(new URL[0]);
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

    }

}
