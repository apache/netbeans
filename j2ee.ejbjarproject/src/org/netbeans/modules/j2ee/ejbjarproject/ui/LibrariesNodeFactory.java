/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
