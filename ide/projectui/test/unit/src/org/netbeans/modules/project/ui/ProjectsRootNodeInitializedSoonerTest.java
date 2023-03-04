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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class ProjectsRootNodeInitializedSoonerTest extends NbTestCase {
    
    public ProjectsRootNodeInitializedSoonerTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }

    public void testWrongOrderOfInitialization() throws Exception {
        
        //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");
        
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        for (int i = 0; i < 30; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj);
            assertNotNull("Project found", tmp);
            final TestProjectOpenedHookImpl hook = new TestProjectOpenedHookImpl();
            tmp.setLookup(Lookups.fixed(tmp, hook));
            hook.lkp = tmp.getLookup();
        }

        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        

        class H extends Handler {
            boolean ok;

            @Override
            public void publish(LogRecord record) {
                if (ok) {
                    return;
                }

                if (record.getLevel().intValue() < getLevel().intValue()) {
                    return;
                }
                if (record.getMessage().contains("BadgingNode init")) {
                    ok = true;
                    // now simulate that the projects are open before
                    // the BadgingNode is really constructed and can
                    // attach its listener to OpenProjectList
                    try {
                        TestProjectOpenedHookImpl.toOpen.await();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H h = new H();
        h.setLevel(Level.ALL);
        OpenProjectList.LOGGER.addHandler(h);
        OpenProjectList.LOGGER.setUseParentHandlers(false);
        OpenProjectList.LOGGER.setLevel(Level.ALL);

        assertEquals("30 children", 30, logicalView.getChildren().getNodesCount());

        OpenProjectList.waitProjectsFullyOpen();
        assertTrue("Handler was called", h.ok);
        assertEquals("All projects opened", 30, TestProjectOpenedHookImpl.opened);

        int i = 0;
        for (Node n : logicalView.getChildren().getNodes()) {
            i++;
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Project type is correct " + i, p);
        }
        
        

        List<Node> arrNodes = logicalView.getChildren().snapshot();
        assertEquals("30 nodes:\n" + arrNodes, 30, arrNodes.size());

        assertTrue("Finished", OpenProjects.getDefault().openProjects().isDone());
        assertFalse("Not cancelled, Finished", OpenProjects.getDefault().openProjects().isCancelled());
        Project[] arr = OpenProjects.getDefault().openProjects().get();
        assertEquals("30", 30, arr.length);
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook 
    implements Runnable, LogicalViewProvider {
        
        public static CountDownLatch toOpen = new CountDownLatch(30);
        public static int opened = 0;
        public static int closed = 0;
        private Lookup lkp;
        
        
        public TestProjectOpenedHookImpl() {
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
            opened++;
            toOpen.countDown();
        }

        public Node createLogicalView() {
            return new AbstractNode(Children.LEAF, lkp);
        }

        public Node findPath(Node root, Object target) {
            return null;
        }
        
    }
}
