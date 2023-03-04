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

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.modules.project.ui.actions.TestSupport.TestProject;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

public class ProjectsRootNodeNotRecognizedTest extends NbTestCase {
    private static CountDownLatch down;
    
    public ProjectsRootNodeNotRecognizedTest(String testName) {
        super(testName);
    }            

    @RandomlyFails // NB-Core-Build #4346: child at 0
    public void testBadgingNodeIsOKIfProjectIsNoLongerRecognized() throws Exception{
        
        //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");        
        //prepearing project
        MockLookup.setInstances(new TestFactory());
        down = new CountDownLatch(1);
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject foMain = TestSupport.createTestProject(workDir, "prj_1");
        FileObject foAnother = TestSupport.createTestProject(workDir, "prj_2");
        FileObject foData = foAnother.createData("data", "txt");

        List<URL> list = new ArrayList<URL>();
        list.add(URLMapper.findURL(foMain, URLMapper.EXTERNAL));
        list.add(URLMapper.findURL(foAnother, URLMapper.EXTERNAL));
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        icons.add(new ExtIcon());
        icons.add(new ExtIcon());
        List<String> names = new ArrayList<String>();
        names.add(list.get(0).toExternalForm());
        names.add(list.get(1).toExternalForm());
        
        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        Node logicalView = new ProjectsRootNode(ProjectsRootNode.PHYSICAL_VIEW);
        assertEquals("2 children", 2, logicalView.getChildren().getNodesCount());
        assertNotNull("Still lazy project", logicalView.getChildren().getNodeAt(0).getLookup().lookup(LazyProject.class));

        // let project open code run
        down.countDown();
        OpenProjectList.waitProjectsFullyOpen();

        // now verify that both dirs has been refused
        assertTrue("Contains main: " + TestFactory.refused, TestFactory.refused.contains(foMain));
        assertTrue("Contains another: " + TestFactory.refused, TestFactory.refused.contains(foAnother));
        Node child = logicalView.getChildren().getNodeAt(0);
        assertNotNull("child at 0", child);
        assertNull("No lazy project", child.getLookup().lookup(LazyProject.class));
    }


    private static final class TestFactory implements ProjectFactory {
        static Set<FileObject> refused = new HashSet<FileObject>();

        public boolean isProject(FileObject projectDirectory) {
            return new TestSupport.TestProjectFactory().isProject(projectDirectory);
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            try {
                down.await();
                if (refused.add(projectDirectory)) {
                    TestProject p = new TestSupport.TestProject(projectDirectory, state);
                    p.setLookup(new NullLVPLookup(p, projectDirectory, state));
                    return p;
                }
                return null;
            } catch (InterruptedException ex) {
                throw new IOException();
            }
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }

    }

    private static final class NullLVPLookup extends AbstractLookup {
        private FileObject projectDirectory;
        private ProjectState state;
        public NullLVPLookup(Project p, FileObject projectDirectory, ProjectState state) {
            this(new InstanceContent(), p, projectDirectory, state);
        }

        private NullLVPLookup(InstanceContent ic, Project p, FileObject projectDirectory, ProjectState state) {
            super(ic);
            ic.add(p);
            this.projectDirectory = projectDirectory;
            this.state = state;

        }

        @Override
        protected void beforeLookup(Template<?> template) {
            if (template.getType() == Sources.class) {
                try {
                    //state.notifyDeleted();
                    projectDirectory.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

    }
}
