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

package org.netbeans.modules.j2ee.ejbjarproject.ui;

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
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.ExtraLibrariesNode;
import org.netbeans.modules.javaee.project.api.ant.ui.logicalview.ExtraLibrariesTestNode;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.CustomizerLibraries;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * NodeFactory to create source and test library nodes.
 * 
 * @author gpatil
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject", position=100)
public final class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project p) {
        EjbJarProject project = p.getLookup().lookup(EjbJarProject.class);
        assert project != null;
        return new LibrariesNodeList(project);
    }

    private static class LibrariesNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String LIBRARIES = "Libs"; //NOI18N
        private static final String TEST_LIBRARIES = "TestLibs"; //NOI18N

        private final SourceRoots testSources;
        private final EjbJarProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private final PropertyEvaluator evaluator;
        private final UpdateHelper updateHelper;
        private final ReferenceHelper refHelper;
        private final ClassPathSupport cs;
        
        LibrariesNodeList(EjbJarProject proj) {
            project = proj;
            testSources = project.getTestSourceRoots();
            evaluator = project.evaluator();
            updateHelper = project.getUpdateHelper();
            refHelper = project.getReferenceHelper();
            cs = new ClassPathSupport(evaluator, refHelper, updateHelper.getAntProjectHelper(), updateHelper, 
                    new ClassPathSupportCallbackImpl(updateHelper.getAntProjectHelper()));
        }
        
        @Override
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(LIBRARIES);
            URL[] testRoots = testSources.getRootURLs();
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

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(String key) {
            if (LIBRARIES.equals(key)) {
                //Libraries Node
                return  new LibrariesNode(
                    NbBundle.getMessage(LibrariesNodeFactory.class,"CTL_LibrariesNode"), // NOI18N
                    project,
                    evaluator,
                    updateHelper,
                    refHelper,
                    ProjectProperties.JAVAC_CLASSPATH,
                    new String[] { ProjectProperties.BUILD_CLASSES_DIR },
                    "platform.active", //NOI18N
                    new Action[] {
                            LibrariesNode.createAddProjectAction(project, project.getSourceRoots()),
                            LibrariesNode.createAddLibraryAction(refHelper, project.getSourceRoots(), null),
                            LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getSourceRoots()),
                        null,
                        ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE), //NOI18N
                    },
                    ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES,
                    cs,
                    new ExtraLibrariesNode(project, evaluator, EjbJarProjectProperties.J2EE_SERVER_INSTANCE, cs)
                );
            } else if (TEST_LIBRARIES.equals(key)) {
                return  new LibrariesNode(
                    NbBundle.getMessage(LibrariesNodeFactory.class,"CTL_TestLibrariesNode"), // NOI18N
                    project,
                    evaluator,
                    updateHelper,
                    refHelper,
                    ProjectProperties.JAVAC_TEST_CLASSPATH,
                    new String[] {
                        ProjectProperties.BUILD_TEST_CLASSES_DIR,
                        ProjectProperties.JAVAC_CLASSPATH,
                        ProjectProperties.BUILD_CLASSES_DIR,
                    },
                    null,
                    new Action[] {
                        LibrariesNode.createAddProjectAction(project, project.getTestSourceRoots()),
                        LibrariesNode.createAddLibraryAction(refHelper, project.getTestSourceRoots(), null),
                        LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getTestSourceRoots()),
                        null,
                        ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE_TESTS), //NOI18N
                    },
                    null,
                    cs,
                    new ExtraLibrariesTestNode(project, evaluator, EjbJarProjectProperties.J2EE_SERVER_INSTANCE, cs)
                    );
            }
            assert false: "No node for key: " + key; // NOI18N
            return null;
            
        }

        @Override
        public void addNotify() {
            testSources.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            testSources.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
    }
    
}
