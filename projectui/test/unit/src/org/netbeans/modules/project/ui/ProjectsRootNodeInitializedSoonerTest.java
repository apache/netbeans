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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
