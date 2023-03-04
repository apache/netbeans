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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@RandomlyFails // e.g. if Thread.sleep calls commented out
public class ProjectsRootNodePreferredFromPopupTest extends NbTestCase {
    CountDownLatch first;
    CountDownLatch middle;
    CountDownLatch rest;
    
    public ProjectsRootNodePreferredFromPopupTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        
        first = new CountDownLatch(1);
        middle = new CountDownLatch(1);
        rest = new CountDownLatch(2);
        
        List<URL> list = new ArrayList<URL>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            FileObject prj = TestSupport.createTestProject(workDir, "prj" + i);
            URL url = URLMapper.findURL(prj, URLMapper.EXTERNAL);
            list.add(url);
            names.add(url.toExternalForm());
            icons.add(new ExtIcon());
            TestSupport.TestProject tmp = (TestSupport.TestProject)ProjectManager.getDefault ().findProject (prj);
            assertNotNull("Project found", tmp);
            CountDownLatch down = i == 0 ? first : (i == 5 ? middle : rest);
            tmp.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        }
        
        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);
        
         //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");
    }

    public void testPreferencesInOpenCanBeChanged() throws InterruptedException, IOException, Exception {
        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        L listener = new L();
        logicalView.addNodeListener(listener);
        
        assertEquals("10 children", 10, logicalView.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        
        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        
        Node midNode = logicalView.getChildren().getNodes()[5];
        {
            TestSupport.TestProject p = midNode.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        Project lazyP = midNode.getLookup().lookup(Project.class);
        assertNotNull("Some project is found", lazyP);
        assertEquals("It is lazy project", LazyProject.class, lazyP.getClass());
        
        middle.countDown();
        // not necessary, but to ensure middle really does not run
        Thread.sleep(300);
        assertEquals("Still no processing", 0, TestProjectOpenedHookImpl.opened);

        
        // make a file of some project selected, that 
        // shall trigger OpenProjectList.preferredProject(lazyP);
        Action[] arr = midNode.getActions(true);
        assertEquals("Two: " + Arrays.asList(arr), 2, arr.length);
        assertAction("Initializ", arr[0], false, midNode);
        assertAction("Close", arr[1], true, midNode);
        
        first.countDown();
        
        TestProjectOpenedHookImpl.toOpen.await();

        {
            TestSupport.TestProject p = null;
            for (int i = 0; i < 10; i++) {
                Node midNode2 = logicalView.getChildren().getNodes()[5];
                p = midNode.getLookup().lookup(TestSupport.TestProject.class);
                if (p != null) {
                    break;
                }
                Thread.sleep(100);
            }
            assertNotNull("The right project opened", p);
        }
        
        rest.countDown();
        rest.countDown();
        OpenProjectList.waitProjectsFullyOpen();
        
        assertEquals("All projects opened", 10, TestProjectOpenedHookImpl.opened);
        

        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Nodes have correct project of this type", p);
        }
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

    private void assertAction(String text, Action action, boolean b, Node n) throws Exception {
        final Action clone = action instanceof ContextAwareAction ? 
            ((ContextAwareAction)action).createContextAwareInstance(n.getLookup()) :
            action;
        
        assertTrue("Expecting " + text + " but was " + action, action.getClass().getName().contains(text));
        
        class Is implements Runnable {
            boolean is;
            public void run() {
                is = clone.isEnabled();
            }
        }
        Is enabled = new Is();
        SwingUtilities.invokeAndWait(enabled);
        
        assertEquals("Enabled? " + text + " and: " + b, b, enabled.is);
    }
    
}
