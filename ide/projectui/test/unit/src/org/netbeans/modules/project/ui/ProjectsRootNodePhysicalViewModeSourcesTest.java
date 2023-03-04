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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.MockServices;
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
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectsRootNodePhysicalViewModeSourcesTest extends NbTestCase {
    CountDownLatch down;
    
    public ProjectsRootNodePhysicalViewModeSourcesTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        
        down = new CountDownLatch(1);
        
        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject)ProjectManager.getDefault ().findProject (prj);
            assertNotNull("Project found", tmp);
            tmp.setLookup(Lookups.fixed(tmp, new TestProjectOpenedHookImpl(down), new TestSources(tmp)));
        }
        
        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);
        
         //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");
    }

    public void testBehaviourOfProjectsLogicNode() throws InterruptedException {
        Node view = new ProjectsRootNode(ProjectsRootNode.PHYSICAL_VIEW);
        L listener = new L();
        view.addNodeListener(listener);
        
        assertEquals("30 children", 30, view.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        
        for (Node n : view.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        
        // let project open code run
        down.countDown();
        TestProjectOpenedHookImpl.toOpen.await();
        
        assertEquals("All projects opened", 30, TestProjectOpenedHookImpl.opened);
        
        OpenProjectList.waitProjectsFullyOpen();

        Node[] all = view.getChildren().getNodes();
        assertEquals("3x30", 90, all.length);
        for (Node n : all) {
            LogicalView v = n.getLookup().lookup(LogicalView.class);
            assertEquals("View is not present in physical view", null, v);
        }
        
        // too bad, looks that we need to generate some events
        //listener.assertEvents("Goal is to receive no events at all", 0);
    }
    
    private static class L implements NodeListener {
        public List<EventObject> events = new ArrayList<EventObject>();
        
        public void childrenAdded(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenRemoved(NodeMemberEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void childrenReordered(NodeReorderEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void nodeDestroyed(NodeEvent ev) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(ev);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            assertFalse("No event in AWT thread", EventQueue.isDispatchThread());
            events.add(evt);
        }

        final void assertEvents(String string, int i) {
            assertEquals(string + events, i, events.size());
            events.clear();
        }
        
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook {
        
        public static CountDownLatch toOpen = new CountDownLatch(30);
        public static int opened = 0;
        public static int closed = 0;
        
        
        private CountDownLatch toWaitOn;
        
        public TestProjectOpenedHookImpl(CountDownLatch toWaitOn) {
            this.toWaitOn = toWaitOn;
        }
        
        protected void projectClosed() {
            closed++;
        }
        
        protected void projectOpened() {
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
    private static class LVP implements LogicalViewProvider {
        public Node createLogicalView() {
            return new LogicalView();
        }

        public Node findPath(Node root, Object target) {
            return null;
        }
    }
    
    private static class LogicalView extends AbstractNode {
        public LogicalView() {
            super(Children.LEAF);
        }
    }

    private static class TestSources implements Sources {
        private SourceGroup[] arr;
        public TestSources(Project p) throws IOException {
            FileObject internal = FileUtil.createFolder(p.getProjectDirectory(), "src");
            FileObject external = FileUtil.createFolder(p.getProjectDirectory().getParent(), "src" + p.getProjectDirectory().getNameExt());
            
            this.arr = new SourceGroup[] {
                new TestGroup(p.getProjectDirectory()),
                new TestGroup(internal),
                new TestGroup(external),
            };
        }
        

        public SourceGroup[] getSourceGroups(String type) {
            return arr;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
    private static class TestGroup implements SourceGroup {
        private FileObject root;
        
        public TestGroup(FileObject r) {
            this.root = r;
        }
        

        public FileObject getRootFolder() {
            return root;
        }

        public String getName() {
            return root.getNameExt();
        }

        public String getDisplayName() {
            return root.getNameExt();
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override public boolean contains(FileObject file) {
            return file.equals(root);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
