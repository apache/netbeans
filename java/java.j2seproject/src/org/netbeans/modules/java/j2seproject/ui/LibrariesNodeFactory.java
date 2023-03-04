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

package org.netbeans.modules.java.j2seproject.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerLibraries;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-java-j2seproject", position=300)
public final class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        J2SEProject project = p.getLookup().lookup(J2SEProject.class);
        assert project != null;
        return new LibrariesNodeList(project);
    }

    private static class LibrariesNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String LIBRARIES = "Libs"; //NOI18N
        private static final String TEST_LIBRARIES = "TestLibs"; //NOI18N

        private final J2SEProject project;        
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private PropertyEvaluator evaluator;
        private UpdateHelper helper;
        private ReferenceHelper resolver;
        private final ClassPathSupport cs;
        
        LibrariesNodeList(@NonNull final J2SEProject proj) {
            Parameters.notNull("proj", proj);   //NOI18N
            project = proj;
            evaluator = project.evaluator();
            helper = project.getUpdateHelper();
            resolver = project.getReferenceHelper();
            cs = new ClassPathSupport(evaluator, resolver, helper.getAntProjectHelper(), helper, null);
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(LIBRARIES);
            URL[] testRoots = project.getTestSourceRoots().getRootURLs();
            boolean addTestSources = false;
            for (int i = 0; i < testRoots.length; i++) {
                File f = Utilities.toFile(URI.create(testRoots[i].toExternalForm()));
                if (f.exists()) {
                    addTestSources = true;
                    break;
                }
            }
            if (addTestSources) {
                result.add(TEST_LIBRARIES);
            }
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (key == LIBRARIES) {
                //Libraries Node
                return new LibrariesNode.Builder(project,evaluator, helper, resolver, cs).
                    setName(NbBundle.getMessage(LibrariesNodeFactory.class,"CTL_LibrariesNode")).
                    addClassPathProperties(ProjectProperties.RUN_CLASSPATH).
                    addClassPathIgnoreRefs(ProjectProperties.BUILD_CLASSES_DIR).
                    setBootPath(ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.BOOT)).
                    setPlatformProperty("platform.active").  //NOI18N
                    addLibrariesNodeActions(
                            LibrariesNode.createAddProjectAction(project, project.getSourceRoots()),
                            LibrariesNode.createAddLibraryAction(project.getReferenceHelper(), project.getSourceRoots(), null),
                            LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getSourceRoots()),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE)). // NOI18N
                    addModulePathProperties(ProjectProperties.RUN_MODULEPATH).
                    addModulePathIgnoreRefs(ProjectProperties.BUILD_CLASSES_DIR).
                    setModuleInfoBasedPath(project.getClassPathProvider().getProjectClassPaths(ClassPath.EXECUTE)[0]).
                    setSourcePath(project.getClassPathProvider().getProjectClassPaths(ClassPath.SOURCE)[0]).
                    build();
            } else if (key == TEST_LIBRARIES) {
                return new LibrariesNode.Builder(project,evaluator, helper, resolver, cs).
                    setName(NbBundle.getMessage(LibrariesNodeFactory.class,"CTL_TestLibrariesNode")).
                    addClassPathProperties(ProjectProperties.RUN_TEST_CLASSPATH).
                    addClassPathIgnoreRefs(
                            ProjectProperties.BUILD_TEST_CLASSES_DIR,
                            ProjectProperties.JAVAC_CLASSPATH,
                            ProjectProperties.BUILD_CLASSES_DIR).
                    addLibrariesNodeActions(
                            LibrariesNode.createAddProjectAction(project, project.getTestSourceRoots()),
                            LibrariesNode.createAddLibraryAction(project.getReferenceHelper(), project.getTestSourceRoots(), null),
                            LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getTestSourceRoots()),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE_TESTS)). // NOI18N
                    addModulePathProperties(ProjectProperties.RUN_TEST_MODULEPATH).
                    addModulePathIgnoreRefs(
                            ProjectProperties.BUILD_TEST_CLASSES_DIR,
                            ProjectProperties.JAVAC_MODULEPATH,
                            ProjectProperties.BUILD_CLASSES_DIR).
                    setModuleInfoBasedPath(project.getClassPathProvider().getProjectClassPaths(ClassPath.EXECUTE)[1]).
                    setSourcePath(project.getClassPathProvider().getProjectClassPaths(ClassPath.SOURCE)[1]).
                    build();
                    
            }
            assert false: "No node for key: " + key;
            return null;
            
        }

        public void addNotify() {
            project.getTestSourceRoots().addPropertyChangeListener(this);
        }

        public void removeNotify() {
            project.getTestSourceRoots().removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
    }
    
}
