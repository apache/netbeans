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
            return roots.toArray(new URL[roots.size()]);
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
