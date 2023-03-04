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

package org.netbeans.api.project;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.netbeans.modules.projectapi.nb.TimedWeakReference;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/* XXX tests needed:
 * - testModifiedProjectsNotGCd
 * ensure that modified projects cannot be collected
 * and that unmodified projects can (incl. after a save)
 * - testIsProject
 * - testCallFindProjectWithinLoadProjectProhibited
 * - testDeleteAndRecreateProject
 */

/**
 * Test ProjectManager find and save functionality.
 * @author Jesse Glick
 */
public class ProjectManagerTest extends NbTestCase {
    
    static {
        // For easier testing.
        TimedWeakReference.TIMEOUT = 1000;
    }
    
    public ProjectManagerTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject goodproject;
    private FileObject goodproject2;
    private FileObject badproject;
    private FileObject mysteryproject;
    private ProjectManager pm;

    protected @Override Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        goodproject = scratch.createFolder("good");
        goodproject.createFolder("testproject");
        goodproject2 = scratch.createFolder("good2");
        goodproject2.createFolder("testproject");
        badproject = scratch.createFolder("bad");
        badproject.createFolder("testproject").createData("broken");
        mysteryproject = scratch.createFolder("mystery");
        MockLookup.setInstances(TestUtil.testProjectFactory());
        pm = ProjectManager.getDefault();
        NbProjectManagerAccessor.reset();
    }
    
    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        goodproject = null;
        badproject = null;
        mysteryproject = null;
        pm = null;
        super.tearDown();
    }
    
    public void testFindProject() throws Exception {
        Project p = null;
        CharSequence log = Log.enable("TIMER", Level.FINE);
        try {
            p = pm.findProject(goodproject);
        } catch (IOException e) {
            fail("Should not fail to load goodproject: " + e);
        }
        if (log.toString().indexOf("Project") < 0) {
            fail("Shall log a message to timers/counters:\n" + log);
        }
        assertNotNull("Should have recognized goodproject", p);
        assertEquals("Correct project directory set", goodproject, p.getProjectDirectory());
        Project p2 = null;
        try {
            p2 = pm.findProject(badproject);
            fail("Should not have succeeded loading badproject");
        } catch (IOException e) {
            // OK
        }
        try {
            p2 = pm.findProject(mysteryproject);
        } catch (IOException e) {
            fail("Should not have failed loading mysteryproject: " + e);
        }
        assertNull("Should not have been able to load mysteryproject", p2);
        assertEquals("Repeated find calls should give same result", p, pm.findProject(goodproject));
        assertEquals("ProjectFactory was called only once on goodproject", 1, TestUtil.projectLoadCount(goodproject));
    }
    
    public void testFindProjectGC() throws Exception {
        Project p = null;
        try {
            p = pm.findProject(goodproject);
        } catch (IOException e) {
            fail("Should not fail to load goodproject: " + e);
        }
        assertNotNull("Should have recognized goodproject", p);
        assertEquals("ProjectFactory was called once so far on goodproject", 1, TestUtil.projectLoadCount(goodproject));
        Reference<?> pref = new WeakReference<Object>(p);
        p = null;
        Thread.sleep(TimedWeakReference.TIMEOUT); // make sure it is not being held strongly
        assertGC("Can collect an unused project with project directory still set", pref);
        p = pm.findProject(goodproject);
        assertNotNull("Can load goodproject again", p);
        assertEquals("Correct project directory set", goodproject, p.getProjectDirectory());
        assertEquals("ProjectFactory was called again on goodproject", 2, TestUtil.projectLoadCount(goodproject));
        pref = new WeakReference<Object>(p);
        p = null;
        Reference<?> dirref = new WeakReference<Object>(goodproject);
        goodproject = null;
        assertGC("Collected the project directory", dirref);
        assertGC("Can collect an unused project with project directory discarded", pref);
        goodproject = scratch.getFileObject("good");
        assertNotNull("goodproject dir still exists", goodproject);
        p = pm.findProject(goodproject);
        assertNotNull("Can load goodproject yet again", p);
        assertEquals("Correct project directory set again", goodproject, p.getProjectDirectory());
        assertEquals("ProjectFactory was called only once on new goodproject folder object", 1, TestUtil.projectLoadCount(goodproject));
    }
    
    public void testFindProjectDoesNotCacheLoadErrors() throws Exception {
        Project p = null;
        try {
            p = pm.findProject(badproject);
            fail("Should not have been able to load badproject");
        } catch (IOException e) {
            // Expected.
        }
        FileObject badprojectSubdir = badproject.getFileObject("testproject");
        assertNotNull("Has testproject", badprojectSubdir);
        FileObject brokenFile = badprojectSubdir.getFileObject("broken");
        assertNotNull("Has broken file", brokenFile);
        brokenFile.delete();
        try {
            p = pm.findProject(badproject);
        } catch (IOException e) {
            fail("badproject has been corrected, should not fail to load now: " + e);
        }
        assertNotNull("Loaded project", p);
        assertEquals("Right project dir", badproject, p.getProjectDirectory());
        badprojectSubdir.createData("broken");
        Project p2 = null;
        try {
            p2 = pm.findProject(badproject);
        } catch (IOException e) {
            fail("badproject is broken on disk but should still be in cache: " + e);
        }
        assertEquals("Cached badproject", p, p2);
        Reference<?> pref = new WeakReference<Object>(p);
        p = null;
        p2 = null;
        assertGC("Collected badproject cache", pref);
        try {
            p = pm.findProject(badproject);
            fail("Should not have been able to load badproject now that it is rebroken and not in cache");
        } catch (IOException e) {
            // Expected.
        }
    }
    
    public void testIsProject() throws Exception {
        assertTrue("Should have recognized goodproject", pm.isProject(goodproject));
        
        assertTrue("Should have recognized badproject", pm.isProject(badproject));
        
        assertFalse("Should not have been able to load mysteryproject", pm.isProject(mysteryproject));
    }
    
    public void testIsProject2() throws Exception {
        ProjectManager.Result r = pm.isProject2(goodproject);
        assertNotNull("Should have recognized goodproject", r);
        assertEquals(goodproject.getName(), r.getDisplayName());
        assertEquals(TestUtil.TEST_PROJECT_ICON, r.getIcon());
        assertEquals(TestUtil.TEST_PROJECT_TYPE, r.getProjectType());
        
        ProjectManager.Result r2 = pm.isProject2(badproject);
        assertNotNull("Should have recognized badproject", r2);
        assertNull("Should not have a project name for badproject", r2.getDisplayName());
        assertEquals(TestUtil.TEST_PROJECT_ICON, r2.getIcon());
        assertNull("Should not have a project type for badproject", r2.getProjectType());
        
        ProjectManager.Result r3 = pm.isProject2(mysteryproject);
        assertNull("Should not have been able to load mysteryproject", r3);
    }
    
    public void testModify() throws Exception {
        Project p1 = pm.findProject(goodproject);
        Project p2 = pm.findProject(goodproject2);
        Set<Project> p1p2 = new HashSet<Project>(Arrays.asList(p1, p2));
        assertEquals("start with no modified projects", Collections.emptySet(), pm.getModifiedProjects());
        assertTrue("p1 is not yet modified", !pm.isModified(p1));
        assertTrue("p2 is not yet modified", !pm.isModified(p2));
        TestUtil.modify(p1);
        assertEquals("just p1 has been modified", Collections.singleton(p1), pm.getModifiedProjects());
        assertTrue("p1 is modified", pm.isModified(p1));
        assertTrue("p2 is still not modified", !pm.isModified(p2));
        TestUtil.modify(p2);
        assertEquals("now both p1 and p2 have been modified", p1p2, pm.getModifiedProjects());
        assertTrue("p1 is modified", pm.isModified(p1));
        assertTrue("and p2 is modified too", pm.isModified(p2));
    }
    
    public void testSave() throws Exception {
        Project p1 = pm.findProject(goodproject);
        Project p2 = pm.findProject(goodproject2);
        Set<Project> p1p2 = new HashSet<Project>(Arrays.asList(p1, p2));
        assertEquals("start with no modified projects", Collections.emptySet(), pm.getModifiedProjects());
        assertEquals("p1 has never been saved", 0, TestUtil.projectSaveCount(p1));
        assertEquals("p2 has never been saved", 0, TestUtil.projectSaveCount(p2));
        TestUtil.modify(p1);
        assertEquals("just p1 was modified", Collections.singleton(p1), pm.getModifiedProjects());
        TestUtil.modify(p2);
        assertEquals("both p1 and p2 were modified now", p1p2, pm.getModifiedProjects());
        pm.saveProject(p1);
        assertEquals("p1 was saved so just p2 is modified", Collections.singleton(p2), pm.getModifiedProjects());
        assertEquals("p1 was saved once", 1, TestUtil.projectSaveCount(p1));
        assertEquals("p2 has not yet been saved", 0, TestUtil.projectSaveCount(p2));
        pm.saveProject(p2);
        assertEquals("now p1 and p2 are both saved", Collections.emptySet(), pm.getModifiedProjects());
        assertEquals("p1 was saved once", 1, TestUtil.projectSaveCount(p1));
        assertEquals("p2 has now been saved once", 1, TestUtil.projectSaveCount(p2));
        pm.saveProject(p2);
        assertEquals("saving p2 again has no effect", Collections.emptySet(), pm.getModifiedProjects());
        assertEquals("p1 still saved just once", 1, TestUtil.projectSaveCount(p1));
        assertEquals("redundant call to save did not really save again", 1, TestUtil.projectSaveCount(p2));
        TestUtil.modify(p1);
        TestUtil.modify(p2);
        assertEquals("both p1 and p2 modified again", p1p2, pm.getModifiedProjects());
        pm.saveAllProjects();
        assertEquals("saveAllProjects saved both p1 and p2", Collections.EMPTY_SET, pm.getModifiedProjects());
        assertEquals("p1 was saved again by saveAllProjects", 2, TestUtil.projectSaveCount(p1));
        assertEquals("p2 was saved again by saveAllProjects", 2, TestUtil.projectSaveCount(p2));
        pm.saveAllProjects();
        assertEquals("saveAllProjects twice has no effect", Collections.EMPTY_SET, pm.getModifiedProjects());
        assertEquals("p1 still only saved twice", 2, TestUtil.projectSaveCount(p1));
        assertEquals("p2 still only saved twice", 2, TestUtil.projectSaveCount(p2));
    }
    
    public void testSaveError() throws Exception {
        Project p1 = pm.findProject(goodproject);
        Project p2 = pm.findProject(goodproject2);
        TestUtil.modify(p1);
        TestUtil.modify(p2);
        Set<Project> p1p2 = new HashSet<Project>(Arrays.asList(p1, p2));
        assertEquals("both p1 and p2 are modified", p1p2, pm.getModifiedProjects());
        TestUtil.setProjectSaveWillFail(p1, new IOException("expected"));
        try {
            pm.saveProject(p1);
            fail("Saving p1 should have failed with an IOException");
        } catch (IOException e) {
            // Good.
        }
        assertTrue("p1 is still modified", pm.isModified(p1));
        assertEquals("both p1 and p2 are still modified", p1p2, pm.getModifiedProjects());
        pm.saveProject(p1);
        assertEquals("p1 was saved so just p2 is modified", Collections.singleton(p2), pm.getModifiedProjects());
        assertEquals("p1 was saved once", 1, TestUtil.projectSaveCount(p1));
        TestUtil.modify(p1);
        TestUtil.setProjectSaveWillFail(p1, new RuntimeException("expected"));
        try {
            pm.saveProject(p1);
            fail("Saving p1 should have failed with a RuntimeException");
        } catch (RuntimeException e) {
            // Good.
        }
        assertTrue("p1 is still modified", pm.isModified(p1));
        TestUtil.setProjectSaveWillFail(p1, new Error("expected"));
        try {
            pm.saveProject(p1);
            fail("Saving p1 should have failed with an Error");
        } catch (Error e) {
            // Good.
        }
        assertTrue("p1 is still modified", pm.isModified(p1));
        assertEquals("both p1 and p2 are still modified", p1p2, pm.getModifiedProjects());
        TestUtil.setProjectSaveWillFail(p1, new IOException("expected"));
        try {
            pm.saveAllProjects();
            fail("Saving p1 should have failed with an IOException");
        } catch (IOException e) {
            // Good.
        }
        assertTrue("p1 is still modified", pm.isModified(p1));
        assertTrue("p1 is still in the modified set", pm.getModifiedProjects().contains(p1));
        assertEquals("p1 was still only saved once", 1, TestUtil.projectSaveCount(p1));
        pm.saveAllProjects();
        assertEquals("both p1 and p2 are now saved", Collections.EMPTY_SET, pm.getModifiedProjects());
        assertEquals("p1 was now saved twice", 2, TestUtil.projectSaveCount(p1));
        assertEquals("p2 was saved exactly once (by one or the other saveAllProjects)", 1, TestUtil.projectSaveCount(p2));
    }
    
    public void testClearNonProjectCache() throws Exception {
        FileObject p1 = scratch.createFolder("p1");
        p1.createFolder("testproject");
        Project proj1 = pm.findProject(p1);
        assertNotNull("p1 immediately recognized as a project", proj1);
        FileObject p2 = scratch.createFolder("p2");
        assertNull("p2 not yet recognized as a project", pm.findProject(p2));
        FileObject p2a = scratch.createFolder("p2a");
        assertNull("p2a not yet recognized as a project", pm.findProject(p2a));
        FileObject p3 = scratch.createFolder("p3");
        FileObject p3broken = p3.createFolder("testproject").createData("broken");
        try {
            pm.findProject(p3);
            fail("p3 should throw an error");
        } catch (IOException e) {
            // Correct.
        }
        p2.createFolder("testproject");
        p2a.createFolder("testproject");
        p3broken.delete();
        pm.clearNonProjectCache();
        assertNotNull("now p2 is recognized as a project", pm.findProject(p2));
        assertNotNull("now p2a is recognized as a project", pm.findProject(p2a));
        assertNotNull("now p3 is recognized as a non-broken project", pm.findProject(p3));
        assertEquals("p1 still recognized as a project", proj1, pm.findProject(p1));
    }

    public void testNotifyDeleted() throws Exception {
        FileObject p1 = scratch.createFolder("p1");
        FileObject p1TestProject = p1.createFolder("testproject");
        
        Project project1 = pm.findProject(p1);
        
        assertNotNull("project1 is recognized", project1);
        p1TestProject.delete();
        TestUtil.notifyDeleted(project1);
        
        assertFalse("project1 is not valid", pm.isValid(project1));
        assertNull("project1 is deleted", pm.findProject(p1));

        FileObject p2 = scratch.createFolder("p2");
        FileObject p2TestProject = p2.createFolder("testproject");
        
        Project project2 = pm.findProject(p2);
        
        assertNotNull("project2 is recognized", project2);
        TestUtil.notifyDeleted(project2);
        
        assertFalse("project2 is not valid", pm.isValid(project2));
        
        Project project2b = pm.findProject(p2);
        
        assertTrue("project2 is newly recognized", project2b != project2);
        assertNotNull("project2 is newly recognized", project2b);

        FileObject p3 = scratch.createFolder("p3");
        FileObject p3TestProject = p3.createFolder("testproject");
        
        Project project3 = pm.findProject(p3);
        
        assertNotNull("project3 is recognized", project3);
        TestUtil.modify(project3);
        assertTrue("project3 is modified", pm.isModified(project3));
        TestUtil.notifyDeleted(project3);
        
        assertFalse("project3 is not valid", pm.isValid(project3));
        
        assertFalse(pm.isModified(project3)); // please do not throw an exception here
        
        FileObject p4 = scratch.createFolder("p4");
        FileObject p4TestProject = p4.createFolder("testproject");

        Project project4 = pm.findProject(p4);
        
        assertNotNull("project4 is recognized", project4);
        TestUtil.notifyDeleted(project4);
        
        assertFalse("project4 is not valid", pm.isValid(project3));
        
        TestUtil.notifyDeleted(project4); // please do not throw an exception here
    }

    public void testClearNonProjectCacheInWriteAccess() throws Exception {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                testClearNonProjectCache();
                return null;
            }
        });
    }

    public void testClearNonProjectCacheInReadAccess() throws Exception {
        ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                testClearNonProjectCache();
                return null;
            }
        });
    }

    public void testRenameProjectFolder() throws Exception { // #194046
        FileObject p1 = scratch.createFolder("p1");
        p1.createFolder("testproject");
        Project project1 = pm.findProject(p1);
        FileObject p2;
        FileLock lock = p1.lock();
        try {
            p2 = p1.move(lock, scratch, "p2", null);
        } finally {
            lock.releaseLock();
        }
        assertEquals(p1, p2);
        Project project2 = pm.findProject(p2);
        TestUtil.notifyDeleted(project1);
        assertEquals(project2, pm.findProject(p2));
    }
    
    /**
     * Helper method allowing to reset PM from other module's tests.
     * @param pm PM to reset
     */
    public static void resetProjectManager(ProjectManager pm) {
        NbProjectManagerAccessor.reset();
    }
}
