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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.modules.project.ui.actions.TestSupport.TestProject;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.ImageDecorator;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.StatusDecorator;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

public class ProjectsRootNodeTest extends NbTestCase {
    
    public ProjectsRootNodeTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    /* XXX fails if either testBadging or testReplaceProjectSingleNonRootNode runs first; construction of BadgingNode triggers failure, for unknown reason
    public void testBehaviourOfProjectsLogicNode() throws Exception {
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        CountDownLatch down = new CountDownLatch(1);
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
            tmp.setLookup(Lookups.singleton(new TestProjectOpenedHookImpl(down)));
        }

        OpenProjectListSettings.getInstance().setOpenProjectsURLs(list);
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);

        Node logicalView = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        L listener = new L();
        logicalView.addNodeListener(listener);
        
        assertEquals("30 children", 30, logicalView.getChildren().getNodesCount());
        listener.assertEvents("None", 0);
        assertEquals("No project opened yet", 0, TestProjectOpenedHookImpl.opened);
        
        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNull("No project of this type, yet", p);
        }
        
        // let project open code run
        down.countDown();
        TestProjectOpenedHookImpl.toOpen.await();
        
        assertEquals("All projects opened", 30, TestProjectOpenedHookImpl.opened);
        
        OpenProjectList.waitProjectsFullyOpen();

        for (Node n : logicalView.getChildren().getNodes()) {
            TestSupport.TestProject p = n.getLookup().lookup(TestSupport.TestProject.class);
            assertNotNull("Nodes have correct project of this type", p);
        }
        
        listener.assertEvents("Goal is to receive no events at all", 0);
        assertTrue("Finished", OpenProjects.getDefault().openProjects().isDone());
        assertFalse("Not cancelled, Finished", OpenProjects.getDefault().openProjects().isCancelled());
        Project[] arr = OpenProjects.getDefault().openProjects().get();
        assertEquals("30", 30, arr.length);
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
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook 
    implements Runnable {
        
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
    */

    public void testBadging() throws Exception { // #135399
        class BadgedImage extends Image {
            final Image original;
            BadgedImage(Image original) {this.original = original;}
            public @Override int getWidth(ImageObserver observer) {return original.getWidth(observer);}
            public @Override int getHeight(ImageObserver observer) {return original.getHeight(observer);}
            public @Override ImageProducer getSource() {return original.getSource();}
            public @Override Graphics getGraphics() {return original.getGraphics();}
            public @Override Object getProperty(String name, ImageObserver observer) {return original.getProperty(name, observer);}
            public @Override void flush() {original.flush();}
        }
        class TestFS extends MultiFileSystem {
            String nameBadge = "";
            String htmlBadge = "";
            boolean badging;
            Set<? extends FileObject> badgedFiles;
            TestFS() throws Exception {
                super(FileUtil.createMemoryFileSystem());
            }
            
            class S implements StatusDecorator, ImageDecorator {
                    public String annotateName(String name, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return name + nameBadge;
                    }
                    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return name + htmlBadge;
                    }
                    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
                        badgedFiles = files;
                        return badging ? new BadgedImage(icon) : icon;
                    }
            }
            
            public @Override StatusDecorator getDecorator() {
                return new S();
            }
            void fireChange(boolean icon, boolean name, FileObject... files) {
                fireFileStatusChanged(new FileStatusEvent(this, new HashSet<FileObject>(Arrays.asList(files)), icon, name));
            }
        }
        TestFS fs = new TestFS();
        FileObject root = fs.getRoot();
        FileObject k1 = root.createData("hello");
        FileObject k2 = root.createFolder("there");
        MockLookup.setInstances(new ProjectFactory() {
            public boolean isProject(FileObject projectDirectory) {return projectDirectory.isRoot();}
            public Project loadProject(final FileObject projectDirectory, ProjectState state) throws IOException {
                if (projectDirectory.isRoot()) {
                    return new Project() {
                        public FileObject getProjectDirectory() {return projectDirectory;}
                        public Lookup getLookup() {return Lookup.EMPTY;}
                    };
                } else {
                    return null;
                }
            }
            public void saveProject(Project project) throws IOException, ClassCastException {}
        });
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        System.setProperty("test.nodelay", "true");
        ProjectsRootNode.BadgingNode node = new ProjectsRootNode.BadgingNode(null, new ProjectsRootNode.ProjectChildren.Pair(prj, ProjectsRootNode.LOGICAL_VIEW),
                new AbstractNode(Children.LEAF, Lookups.singleton(prj)) {
                    public @Override String getDisplayName() {return "Prj";}
                    public @Override String getHtmlDisplayName() {return "Prj";}
                }, true);
        //cache the isMain() method and skip one display name change fire
        node.getHtmlDisplayName();
        Thread.sleep(500);
        
        final AtomicInteger nameChanges = new AtomicInteger();
        final AtomicInteger iconChanges = new AtomicInteger();
        node.addNodeListener(new NodeAdapter() {
            public @Override void propertyChange(PropertyChangeEvent ev) {
                String p = ev.getPropertyName();
                if (p.equals(Node.PROP_DISPLAY_NAME)) {
                    nameChanges.incrementAndGet();
                } else if (p.equals(Node.PROP_ICON)) {
                    iconChanges.incrementAndGet();
                }
            }
        });
        assertEquals("Prj", node.getDisplayName());
        assertEquals("Prj", node.getHtmlDisplayName());
        assertFalse(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        fs.nameBadge = " *";
        fs.htmlBadge = " <mod>";
        fs.fireChange(false, true, k1);
        assertNotNull(node.task);
        node.task.waitFinished();
        assertEquals(1, nameChanges.intValue());
        assertEquals(0, iconChanges.intValue());
        assertEquals("Prj *", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertFalse(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2)), fs.badgedFiles);
        fs.badging = true;
        fs.fireChange(true, false, k2);
        node.task.waitFinished();
        assertEquals(1, nameChanges.intValue());
        assertEquals(1, iconChanges.intValue());
        assertEquals("Prj *", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertTrue(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2)), fs.badgedFiles);
        FileObject k3 = root.createData("again");
        fs.nameBadge = " +";
        fs.fireChange(false, true, k3);
        node.task.waitFinished();
        assertEquals(2, nameChanges.intValue());
        assertEquals(1, iconChanges.intValue());
        assertEquals("Prj +", node.getDisplayName());
        assertEquals("Prj <mod>", node.getHtmlDisplayName());
        assertTrue(node.getIcon(BeanInfo.ICON_COLOR_16x16) instanceof BadgedImage);
        assertEquals(new HashSet<FileObject>(Arrays.asList(k1, k2, k3)), fs.badgedFiles);
    }

    public void testIconAnnotated() throws IOException, Exception {
        final Image icon1 = ImageUtilities.loadImage("org/netbeans/modules/project/ui/resources/icon-1.png");
        final Image icon2 = ImageUtilities.loadImage("org/netbeans/modules/project/ui/resources/icon-2.png");
        final Image icon3 = ImageUtilities.loadImage("org/netbeans/modules/project/ui/resources/icon-3.png");
        class ProjectIconAnnotatorImpl implements ProjectIconAnnotator {
            private final ChangeSupport cs = new ChangeSupport(this);
            boolean enabled = true;
            public @Override Image annotateIcon(Project p, Image original, boolean openedNode) {
                if (!enabled) {
                    return icon1;
                } else if (openedNode) {
                    return icon2;
                } else {
                    return icon3;
                }
            }
            public @Override void addChangeListener(ChangeListener listener) {
                cs.addChangeListener(listener);
            }
            public @Override void removeChangeListener(ChangeListener listener) {
                cs.removeChangeListener(listener);
            }
            void disable() {
                enabled = false;
                cs.fireChange();
            }
        }
        Project prj = new TestProject(FileUtil.createMemoryFileSystem().getRoot(), null);
        ProjectIconAnnotatorImpl annotator = new ProjectIconAnnotatorImpl();
        MockLookup.setInstances(annotator);
        System.setProperty("test.nodelay", "true");
        ProjectsRootNode.BadgingNode node = new ProjectsRootNode.BadgingNode(null, new ProjectsRootNode.ProjectChildren.Pair(prj, ProjectsRootNode.LOGICAL_VIEW),
                new AbstractNode(Children.LEAF, Lookups.singleton(prj)), true);
        assertEquals(icon3, node.getIcon(BeanInfo.ICON_COLOR_16x16));
        assertEquals(icon2, node.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
        final MockPropertyChangeListener listener = new MockPropertyChangeListener();
        node.addNodeListener(new NodeAdapter() {
            public @Override void propertyChange(PropertyChangeEvent ev) {
                listener.propertyChange(ev);
            }
        });
        annotator.disable();
        listener.assertEvents(Node.PROP_ICON, Node.PROP_OPENED_ICON);
        assertEquals(icon1, node.getIcon(BeanInfo.ICON_COLOR_16x16));
        MockLookup.setInstances();
        listener.assertEvents(Node.PROP_ICON, Node.PROP_OPENED_ICON);
    }

    public void testReplaceProjectSingleNonRootNode() throws Exception { // #197864
        FileObject d = FileUtil.toFileObject(getWorkDir()).createFolder("p");
        final FileObject d2 = d.createFolder("testproject");
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        final TestProject p = (TestProject) ProjectManager.getDefault().findProject(d);
        p.setLookup(Lookups.singleton(new Sources() {
            public @Override SourceGroup[] getSourceGroups(String type) {
                if (type.equals(TYPE_GENERIC)) {
                    return new SourceGroup[] {GenericSources.group(p, d2, "testproject", "Testing", null, null)};
                } else {
                    return new SourceGroup[0];
                }
            }
            public @Override void addChangeListener(ChangeListener listener) {}
            public @Override void removeChangeListener(ChangeListener listener) {}
        }));
        final LazyProject lp = new LazyProject(d.toURL(), "p", new ExtIcon());
        Children ch = new ProjectsRootNode.ProjectChildren(ProjectsRootNode.PHYSICAL_VIEW) {
            public @Override void addNotify() {
                setKeys(Collections.singleton(new ProjectsRootNode.ProjectChildren.Pair(lp, ProjectsRootNode.PHYSICAL_VIEW)));
            }
        };
        ProjectsRootNode.checkNoLazyNode(ch);
        Node[] ns = ch.getNodes(true);
        assertEquals(1, ns.length);
        assertEquals("p - Testing", ns[0].getDisplayName());
    }

}
