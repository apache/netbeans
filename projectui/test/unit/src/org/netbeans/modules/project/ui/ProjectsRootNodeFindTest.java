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
