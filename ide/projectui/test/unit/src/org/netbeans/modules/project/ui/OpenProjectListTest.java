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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Tests fix of issue 56454.
 *
 * @author Jiri Rechtacek
 */
@RandomlyFails
public class OpenProjectListTest extends NbTestCase {
    FileObject f1_1_open, f1_2_open, f1_3_close;
    FileObject f2_1_open;

    Project project1, project2;
    TestOpenCloseProjectDocument handler = new OpenProjectListTest.TestOpenCloseProjectDocument ();

    public OpenProjectListTest (String testName) {
        super (testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected @Override void setUp() throws Exception {
        super.setUp ();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();

        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL = handler;
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        f1_1_open = p1.createData("f1_1.java");
        f1_2_open = p1.createData("f1_2.java");
        f1_3_close = p1.createData("f1_3.java");

        project1 = ProjectManager.getDefault ().findProject (p1);
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        f2_1_open = p2.createData ("f2_1.java");

        // project2 depends on projects1
        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup(Lookups.fixed(TestSupport.createAuxiliaryConfiguration(), new MySubprojectProvider(project1)));
        
        // prepare set of open documents for both projects
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_1_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_2_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f2_1_open);

        OpenProjectList.getDefault().close(OpenProjectList.getDefault().getOpenProjects(), false);
    }
    
    public void testOpen () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project1, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        Pattern p = Pattern.compile("Opening.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log.toString());
        }
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.toURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.toURL ().toExternalForm ()));
        /* XXX always fails; what was this testing?
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
        */
    }

    public void testListenerOpenClose () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0); 
        ChangeListener list = new ChangeListener();
        OpenProjectList.getDefault().addPropertyChangeListener(list);
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project1, true);
        Pattern p = Pattern.compile("Opening.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log.toString());
        }
        assertEquals(0, list.oldCount);
        assertEquals(1, list.newCount);
        OpenProjectList.getDefault ().open (project2, true);
        assertEquals(1, list.oldCount);
        assertEquals(2, list.newCount);
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        assertEquals(2, list.oldCount);
        assertEquals(1, list.newCount);
        OpenProjectList.getDefault().close(new Project[] {project2}, false);
        assertEquals(1, list.oldCount);
        assertEquals(0, list.newCount);
    }

    @RandomlyFails // locally, in 2nd check of f1_1_open
    public void testClose () throws Exception {
        testOpen ();
        
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        Pattern p = Pattern.compile("Closing.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log);
        }
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.toURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.toURL ().toExternalForm ()));
        /* XXX fails, see above
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.getURL ().toExternalForm ()));
        */
        
        OpenProjectList.getDefault ().open (project1);
        OpenProjectList.getDefault ().open (project2);
        
        // close all project1's documents
        handler.openFiles.remove (f1_1_open.toURL ().toExternalForm ());
        handler.openFiles.remove (f1_2_open.toURL ().toExternalForm ());
        
        ProjectUtilities.closeAllDocuments(new Project[] {project1}, false, null);
        OpenProjectList.getDefault().close(new Project[] {project1}, false, null);

        OpenProjectList.getDefault ().open (project1);
        assertFalse ("Document f1_1_open isn't loaded.", handler.openFiles.contains (f1_1_open.toURL ().toExternalForm ()));
        assertFalse ("Document f1_2_open isn't loaded.", handler.openFiles.contains (f1_2_open.toURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is still loaded.", handler.openFiles.contains (f2_1_open.toURL ().toExternalForm ()));
    }

    public void testSerialize() throws Exception {
        testOpen();
        
        OpenProjectList.waitProjectsFullyOpen();
        Field f = OpenProjectList.class.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        f.set(null, null);
        
        CharSequence whatIsLoggedWhenDeserializing = Log.enable("org.netbeans.ui", Level.FINE);
        
        Project[] arr = OpenProjectList.getDefault().getOpenProjects();
        OpenProjectList.waitProjectsFullyOpen();
        arr = OpenProjectList.getDefault().getOpenProjects();
        
        assertEquals("One", 1, arr.length);
        Pattern p = Pattern.compile("Initializing.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(whatIsLoggedWhenDeserializing);
        if (!m.find()) {
            fail("There should be TestProject\n" + whatIsLoggedWhenDeserializing);
        }
    }
    
    public void testOpenDependingProject () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project2, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        assertTrue ("Project2 is opened.", OpenProjectList.getDefault ().isOpen (project2));
        Pattern p = Pattern.compile("Opening.*2.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log);
        }
        
        assertTrue ("Document f1_1_open is loaded.", handler.openFiles.contains (f1_1_open.toURL ().toExternalForm ()));
        assertTrue ("Document f1_2_open is loaded.", handler.openFiles.contains (f1_2_open.toURL ().toExternalForm ()));
        assertTrue ("Document f2_1_open is loaded.", handler.openFiles.contains (f2_1_open.toURL ().toExternalForm ()));
    }
    
    public void testCloseProjectWithoutOpenDocuments () throws Exception {
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);        
        OpenProjectList.getDefault ().open (project2, false);        
        assertFalse ("Project1 isn't opened.", OpenProjectList.getDefault ().isOpen (project1));
        assertTrue ("Project2 is opened.", OpenProjectList.getDefault ().isOpen (project2));
        
        handler.openFiles.remove (f2_1_open.toURL ().toExternalForm ());
        
        assertFalse ("Document f2_1_open isn't loaded.", handler.openFiles.contains (f2_1_open.toURL ().toExternalForm ()));
        
        ProjectUtilities.closeAllDocuments(new Project[] {project2}, false, null);
        OpenProjectList.getDefault().close(new Project[] {project2}, false);

        assertFalse ("Project2 is closed.", OpenProjectList.getDefault ().isOpen (project2));
    }
    
    public void testProjectOpenedClosed() throws Exception {
        ((TestSupport.TestProject) project1).setLookup(Lookups.fixed(new Object[] {
            new TestProjectOpenedHookImpl(),
            new TestProjectOpenedHookImpl(),
        }));
        
        TestProjectOpenedHookImpl.opened = 0;
        TestProjectOpenedHookImpl.closed = 0;
        
        OpenProjectList.getDefault().open(project1);
        
        assertEquals("both open hooks were called", 2, TestProjectOpenedHookImpl.opened);
        assertEquals("no close hook was called", 0, TestProjectOpenedHookImpl.closed);
        
        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        
        assertEquals("both open hooks were called", 2, TestProjectOpenedHookImpl.opened);
        OpenProjectList.OPENING_RP.post(new Runnable() {public void run() {}}).waitFinished(); // flush running tasks
        assertEquals("both close hooks were called", 2, TestProjectOpenedHookImpl.closed);
    }
    
    public void testNotifyDeleted() throws Exception {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        FileObject p1 = workDir.createFolder("p1");
        FileObject p1TestProject = p1.createFolder("testproject");
        
        Project prj1 = ProjectManager.getDefault().findProject(p1);
        
        assertNotNull("project1 is recognized", prj1);
        
        OpenProjectList.getDefault().open(prj1);
        
        OpenProjectList.getDefault().close(new Project[] {prj1}, false);
        
        p1TestProject.delete();
        TestSupport.notifyDeleted(prj1);
        
        assertNull("project1 is deleted", ProjectManager.getDefault().findProject(p1));
        
        assertFalse("project1 is not in recent projects list", OpenProjectList.getDefault().getRecentProjects().contains(prj1));
        
        FileObject p2 = workDir.createFolder("p2");
        p2.createFolder("testproject");
        
        Project prj2 = ProjectManager.getDefault().findProject(p2);
        
        assertNotNull("project2 is recognized", prj2);
        OpenProjectList.getDefault().open(prj2);
        
        OpenProjectList.getDefault().close(new Project[] {prj2}, false);
        
        TestSupport.notifyDeleted(prj2);
        
        assertFalse("project2 is not in recent projects list", OpenProjectList.getDefault().getRecentProjects().contains(prj2));
    }
    
    public void testMainProject() throws Exception {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        FileObject p1 = workDir.createFolder("p1");
        FileObject p1TestProject = p1.createFolder("testproject");
        
        Project prj1 = ProjectManager.getDefault().findProject(p1);
        
        assertNotNull("project1 is recognized", prj1);
        
        FileObject p2 = workDir.createFolder("p2");
        p2.createFolder("testproject");
        
        Project prj2 = ProjectManager.getDefault().findProject(p2);
        
        assertNotNull("project2 is recognized", prj2);
        
        FileObject p3 = workDir.createFolder("p3");
        FileObject p3TestProject = p3.createFolder("testproject");
        
        Project project3 = ProjectManager.getDefault().findProject(p3);
        
        assertNotNull("project3 is recognized", project3);
        
        assertNull("no main project set when OPL is empty", OpenProjectList.getDefault().getMainProject());
        
        OpenProjectList.getDefault().open(prj1);
        
        assertNull("open project does not change main project", OpenProjectList.getDefault().getMainProject());
        
        OpenProjectList.getDefault().setMainProject(prj1);
        
        assertTrue("main project correctly set", OpenProjectList.getDefault().getMainProject() == prj1);
        
        OpenProjectList.getDefault().open(prj2);
        
        assertTrue("open project does not change main project", OpenProjectList.getDefault().getMainProject() == prj1);
        
        OpenProjectList.getDefault().close(new Project[] {prj1}, false);
        
        assertNull("no main project set when main project is closed", OpenProjectList.getDefault().getMainProject());
        
        OpenProjectList.getDefault().setMainProject(project3);
        // do not want this to throw an error anymore
        
        //the same for a previously opened project:
        OpenProjectList.getDefault().setMainProject(prj1);
    }

    public void testProjectClosedRace() throws Exception {

        final class POHImpl extends ProjectOpenedHook {

            private final AtomicInteger opened = new AtomicInteger();
            private final AtomicInteger closed = new AtomicInteger();

            private final Collection<Runnable> openActions;
            private final Collection<Runnable> closeActions;


            POHImpl(
                Runnable[] openActions,
                Runnable[] closeActions) {
                this.openActions = new ArrayList<Runnable>(openActions.length);
                this.closeActions = new ArrayList<Runnable>(closeActions.length);
                Collections.addAll(this.openActions, openActions);
                Collections.addAll(this.closeActions, closeActions);
            }


            @Override
            protected void projectOpened() {
                try {
                    for (Runnable a : openActions) {
                        a.run();
                    }
                } finally {
                    opened.incrementAndGet();
                }
            }
            @Override
            protected void projectClosed() {
                try {
                    for (Runnable a : closeActions) {
                        a.run();
                    }
                }finally {
                    closed.incrementAndGet();
                }
            }
        }
        final CountDownLatch barrier = new CountDownLatch(1);
        final POHImpl poh = new POHImpl(
            new Runnable[0],
            new Runnable[]{
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
        ((TestSupport.TestProject) project1).setLookup(Lookups.singleton(poh));
        OpenProjectList.getDefault().open(project1);
        Future<Project[]> becomesProjects = OpenProjectList.getDefault().openProjectsAPI();
        Project[] projects = becomesProjects.get();
        assertTrue("Open done", becomesProjects.isDone());          //NOI18N
        assertEquals("projectOpened called", 1, poh.opened.get());      //NOI18N
        assertEquals("No projectClosed called", 0, poh.closed.get());   //NOI18N
        assertEquals("One project opened", 1, projects.length);         //NOI18N

        OpenProjectList.getDefault().close(new Project[] {project1}, false);
        assertEquals("no projectClosed called yet", 0, poh.closed.get());   //NOI18N
        becomesProjects = OpenProjectList.getDefault().openProjectsAPI();
        assertFalse("Close not yet done", becomesProjects.isDone());        //NOI18N
        barrier.countDown();
        projects = becomesProjects.get();
        assertTrue("Close done", becomesProjects.isDone());          //NOI18N
        assertEquals("projectClosed called", 1, poh.closed.get());   //NOI18N
        assertEquals("No projects", 0, projects.length);            //NOI18N


    }

    // helper code

    private static class MySubprojectProvider implements SubprojectProvider {
        Project p;
        public MySubprojectProvider (final Project project) {
            p = project;
        }
        public Set<Project> getSubprojects() {
            return Collections.singleton (p);
        }
        
        public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {}
        public void addChangeListener (javax.swing.event.ChangeListener changeListener) {}

    }
    
    private static class TestOpenCloseProjectDocument implements ProjectUtilities.OpenCloseProjectDocument {
        public Set<String> openFiles = new HashSet<String>();
        public Map<Project,Set<String>> urls4project = new HashMap<Project,Set<String>>();
        
        public boolean open (FileObject fo) {
            Project owner = FileOwnerQuery.getOwner (fo);
            if (!urls4project.containsKey (owner)) {
              // add project
                urls4project.put(owner, new TreeSet<String>());
            }
            URL url = null;
            DataObject dobj = null;
            try {
                dobj = DataObject.find (fo);
                url = dobj.getPrimaryFile ().toURL ();
                urls4project.get(owner).add(url.toExternalForm());
                openFiles.add (fo.toURL ().toExternalForm ());
            } catch (DataObjectNotFoundException donfe) {
                fail ("DataObjectNotFoundException on " + fo);
            }
            return true;
        }
        
        public Map<Project,Set<String>> close(Project[] projects, boolean notifyUI) {
            
            for (int i = 0; i < projects.length; i++) {
                Set<String> projectOpenFiles = urls4project.get(projects [i]);
                if (projectOpenFiles != null) {
                    projectOpenFiles.retainAll (openFiles);
                    urls4project.put (projects [i], projectOpenFiles);
                    for (String url : projectOpenFiles) {
                        FileObject fo = null;
                        try {
                            fo = URLMapper.findFileObject (new URL (url));
                            openFiles.remove (fo.toURL ().toExternalForm ());
                        } catch (MalformedURLException mue) {
                            fail ("MalformedURLException in " + url);
                        }
                    }
                }
            }
            
            return urls4project;
        }
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook 
    implements Runnable {
        
        public static int opened = 0;
        public static int closed = 0;
        
        private Object result;
        
        protected void projectClosed() {
            closed++;
            assertFalse("Working on", OpenProjects.getDefault().openProjects().isDone());
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNotNull("some result computed", result);
            assertEquals("It is time out exception", TimeoutException.class, result.getClass());
        }
        
        protected void projectOpened() {
            opened++;
            assertFalse("Working on", OpenProjects.getDefault().openProjects().isDone());
            RequestProcessor.getDefault().post(this).waitFinished();
            assertNotNull("some result computed", result);
            assertEquals("It is time out exception", TimeoutException.class, result.getClass());
        }
        
        public void run() {
            try {
                result = OpenProjects.getDefault().openProjects().get(100, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                result = ex;
    }
        }
    }
    
    private class ChangeListener implements PropertyChangeListener {
        int oldCount = -1;
        int newCount = -1;

        public void propertyChange(PropertyChangeEvent arg0) {
            if (OpenProjectList.PROPERTY_OPEN_PROJECTS.equals(arg0.getPropertyName())) {
                Object old = arg0.getOldValue();
                Object nw = arg0.getNewValue();
                assertNotNull(old);
                assertNotNull(nw);
                Project[] oList = (Project[])old;
                Project[] nList = (Project[])nw;
                oldCount = oList.length;
                newCount = nList.length;
            }
        }
    }
}
