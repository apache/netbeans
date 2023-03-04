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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class ProjectsRootNodeFindTest extends NbTestCase {
    
    public ProjectsRootNodeFindTest(String testName) {
        super(testName);
    }            

    public void testFindNode() throws Exception{
        
         //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");
        
        //prepearing project
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        CountDownLatch down = new CountDownLatch(1);
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject foMain = TestSupport.createTestProject(workDir, "prj_1");
        FileObject foChild = TestSupport.createTestProject(foMain, "prj_2");
        FileObject foData = foChild.createData("data", "txt");

        List<URL> list = new ArrayList<URL>();
        list.add(URLMapper.findURL(foMain, URLMapper.EXTERNAL));
        list.add(URLMapper.findURL(foChild, URLMapper.EXTERNAL));
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        icons.add(new ExtIcon());
        icons.add(new ExtIcon());
        List<String> names = new ArrayList<String>();
        names.add(list.get(0).toExternalForm());
        names.add(list.get(1).toExternalForm());
        TestSupport.TestProject prjMain = (TestSupport.TestProject) ProjectManager.getDefault().findProject(foMain);
        TestSupport.TestProject prjChild = (TestSupport.TestProject) ProjectManager.getDefault().findProject(foChild);
        prjMain.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        prjChild.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        
        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        assertEquals("2 children", 2, logicalView.getChildren().getNodesCount());

        // let project open code run
        down.countDown();
        TestProjectOpenedHookImpl.toOpen.await();
        
        OpenProjectList.waitProjectsFullyOpen();

        ProjectsRootNode phView = new ProjectsRootNode(ProjectsRootNode.PHYSICAL_VIEW);
        Node nodeMain = phView.findNode(foMain);
        assertNotNull(nodeMain);
        Node nodeChild = phView.findNode(foChild);
        assertNotNull(nodeChild);
        assertNotSame(nodeChild.getParentNode(), nodeMain);
        Node nodeData = phView.findNode(foData);
        assertNotNull(nodeData);
        assertEquals(nodeData.getParentNode(), nodeChild);
        assertNotSame(nodeData.getParentNode().getParentNode(), nodeMain);
    }

    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook
    implements Runnable {

        public static CountDownLatch toOpen = new CountDownLatch(2);
        public static int opened = 0;
        public static int closed = 0;

        private CountDownLatch toWaitOn;

        public TestProjectOpenedHookImpl(CountDownLatch toWaitOn) {
            this.toWaitOn = toWaitOn;
        }

        protected void projectClosed() {
            closed++;
        }

        Project[] arr;
        public void run() {
            try {
                arr = OpenProjects.getDefault().openProjects().get(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                fail("Wrong exception");
            } catch (ExecutionException ex) {
                fail("Wrong exception");
            } catch (TimeoutException ex) {
                // OK
            }
        }

        protected void projectOpened() {
            assertFalse("Running", OpenProjects.getDefault().openProjects().isDone());
            // now verify that other threads do not see results from the Future
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNull("TimeoutException thrown", arr);
            if (toWaitOn != null) {
                try {
                    toWaitOn.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            opened++;
            toOpen.countDown();
        }
    }
}
