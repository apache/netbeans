/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.dependency.reload;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.reload.MockProjectReloadImplementation.ProjectData;
import org.netbeans.modules.project.dependency.reload.ProjectReloadImplTest.Mock1;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal.StateParts;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class ProjectReloadImplTest extends NbTestCase {

    public ProjectReloadImplTest(String name) {
        super(name);
    }
    
    Collection<Logger> loggers = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        loggers.add(Logger.getLogger(ProjectReloadInternal.class.getName()));
        loggers.add(Logger.getLogger(Reloader.class.getName()));
        loggers.forEach(logger -> {
            logger.setLevel(Level.FINER);

            ConsoleHandler h = new ConsoleHandler();
            h.setLevel(Level.FINER);
            if (!Arrays.asList(logger.getHandlers()).stream().anyMatch(x -> x instanceof ConsoleHandler)) {
                logger.addHandler(h);
            }
        });
    }
    
    @Override
    public void tearDown() throws Exception {
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        ProjectManager.getDefault().clearNonProjectCache();
        TestProjectFactory.clear();

        ProjectManagerImplementation impl = Lookup.getDefault().lookup(ProjectManagerImplementation.class);
        Method m = impl.getClass().getDeclaredMethod("reset");
        m.setAccessible(true);
        m.invoke(impl);

        super.tearDown();
    }
    
    /**
     * If nothing was loaded using withProjectState(), the state must return NONE quality
     */
    public void testUnloadedMetaReturnsNone() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        assertNotNull(p);
        
        ProjectState ps = ProjectReload.getProjectState(p);
        assertNotNull(ps);
        assertEquals(ProjectReload.Quality.NONE, ps.getQuality());
        
        ProjectState ps2 = ProjectReload.getProjectState(p);
        assertSame("State should be cached", ps, ps2);
        
        assertEquals(0, ps2.getLoadedFiles().size());
        assertNull(ps2.getLookup().lookup(MockProjectReloadImplementation.ProjectData.class));
        assertEquals(-1, ps2.getTimestamp());
        
        assertTrue(ps.isValid());
        
        // allow the initial project load
        ProjectState ps3 = ProjectReload.getProjectState(p, true);
        assertNotSame(ps3, ps2);
        assertFalse(ps2.isValid());
        
        assertTrue(ps3.getQuality().isAtLeast(ProjectReload.Quality.BROKEN));
    }
    
    /**
     * ProjectStateRequest.load() will load to at least SIMPLE quality 
     */
    public void testLoadQuality() throws Exception  {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        assertNotNull(p);
        
        ProjectState ps = ProjectReload.getProjectState(p, false);
        assertNotNull(ps);
        assertEquals(ProjectReload.Quality.NONE, ps.getQuality());
        
        assertTrue(ps.isValid());
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertTrue(ps2.getQuality().isAtLeast(ProjectReload.Quality.SIMPLE));
        assertTrue(ps2.isValid());
        assertFalse(ps.isValid());
    }
    
    /**
     * Checks that file change makes project state inconsistent.
     */
    public void testFileChangeMakesInconsistent() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        assertNotNull(p);
        
        ProjectState ps1 = ProjectReload.getProjectState(p);
        assertTrue(ps1.isValid());
        
        AtomicBoolean changed = new AtomicBoolean();
        ps1.addChangeListener(e -> {
            changed.set(true);
        });

        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertTrue(ps2.isConsistent());
        
        CountDownLatch latch = new CountDownLatch(1);
        
        ps2.addChangeListener((e) -> {
            latch.countDown();
        });
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertSame(ps2, ps3);
        
        FileObject prjf = f.getFileObject("project.txt");
        Thread.sleep(2000);
        
        Files.setLastModifiedTime(FileUtil.toFile(prjf).toPath(), FileTime.from(Instant.now()));
        prjf.refresh();
        latch.await(10, TimeUnit.SECONDS);
        
        assertFalse(ps2.isConsistent());
        assertTrue(ps2.isValid());
        
        assertTrue(ps2.getChangedFiles().contains(prjf));
        
        ps3 = ProjectReload.withProjectState(p, StateRequest.refresh()).get();
        assertNotSame(ps2, ps3);
        
        assertFalse(ps2.isValid());

        assertTrue(ps3.isValid());
        assertTrue(ps3.isConsistent());
        
        assertTrue(changed.get());
    }
    
    /**
     * Checks that a quality lower or same as already loaded maintains the
     * current state instance.
<    */
    public void testLowerQualityProvidesOldState() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        assertNotNull(p);
        
        ProjectState ps1 = ProjectReload.getProjectState(p);
        assertTrue(ps1.isValid());
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertTrue(ps3.isValid());
        assertSame(ps3, ProjectReload.getProjectState(p));
        
        ProjectState ps4 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertSame(ps3, ps4);
        assertSame(ps3, ProjectReload.getProjectState(p));
        
        ProjectState ps5 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.BROKEN)).get();
        assertSame(ps3, ps5);
        assertTrue(ps3.isValid());
    }
    
    /**
     * Checks that if higher quality is requested, the project is loaded again.
     */
    public void testHigherQualityReloadsProject() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);

        ProjectState ps1 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertTrue(ps1.isValid());
        assertSame(ps1, ProjectReload.getProjectState(p));
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.reload().toQuality(ProjectReload.Quality.LOADED)).get();
        assertTrue(ps2.isValid());
        assertNotSame(ps1, ps2);
        assertFalse(ps1.isValid());
        assertSame(ps2, ProjectReload.getProjectState(p));
    }
    
    /**
     * Checks that the load future completes only after all the reload implementations did
     * load their parts.
     */
    public void testLoadFutureCompletesAtEnd() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        class M2 extends MockProjectReloadImplementation {
            CountDownLatch latch = new CountDownLatch(1);
            
            @Override
            protected ProjectStateBuilder createStateData(ProjectStateBuilder b, StateRequest r) {
                try {
                    assertTrue(latch.await(10, TimeUnit.SECONDS));
                } catch (InterruptedException ex) {
                    fail("Unexpected exception: " + ex);
                    ex.printStackTrace();
                }
                b.data("Hey !");
                return b;
            }
        }
        
        M2 mock2 = new M2();

        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        TestProjectFactory.addToProject(f, (p) -> { mock2.setProject(p); return mock2; });
        Project p = ProjectManager.getDefault().findProject(f);
        
        CompletableFuture<ProjectState> s = ProjectReload.withProjectState(p, StateRequest.load());
        Thread.sleep(1000);
        // evaluation should be still blocked on the latch
        assertFalse(s.isDone());
        
        mock2.latch.countDown();
        
        ProjectState ps1 = s.get(1000, TimeUnit.SECONDS);
        assertTrue(ps1.isValid());
        Collection<? extends MockProjectReloadImplementation.ProjectData> dd = ps1.getLookup().lookupAll(MockProjectReloadImplementation.ProjectData.class);
        assertEquals(1, dd.size());
        
        Collection<? extends String> dd2 = ps1.getLookup().lookupAll(String.class);
        assertEquals(1, dd2.size());
        
    }
    
    volatile CompletableFuture<ProjectStateData<ProjectData>> sync;
    
    class CancellableReload extends MockProjectReloadImplementation implements Runnable {
        Semaphore latch = new Semaphore(1);
        volatile Thread executing;
        volatile boolean interrupted;
        boolean cancelExec = true;

        @Override
        protected ProjectReloadImplementation.ProjectStateBuilder createStateData(
                ProjectReloadImplementation.ProjectStateBuilder b, StateRequest r) {
            b.data("Hey !");
            return b;
        }

        @Override
        public void run() {
        }

        public ProjectStateData<ProjectData> exec(ProjectStateData<ProjectData> s) {
            executing = Thread.currentThread();
            try {
                assertTrue(latch.tryAcquire(10 *1000, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                interrupted = true;
            }
            executing = null;
            return s;
        }

        @Override
        public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, ProjectReloadImplementation.LoadContext<ProjectData> context) {
            CompletableFuture<ProjectStateData<ProjectData>> f = super.reload(project, request, context); 
            if (!cancelExec) {
                return f;
            }
            CompletableFuture<ProjectStateData<ProjectData>> f2 = f.thenApplyAsync(this::exec, Executors.newCachedThreadPool());
            sync = f2;

            // make a copy. f3 will be cancelled and completes immediately but f2 will complete 
            // only after the worker is interrupt()ed.
            CompletableFuture<ProjectStateData<ProjectData>> f3 = f2.copy();
            context.setCancellable(() -> {
                if (executing != null) {
                    executing.interrupt();
                    return true;
                } else {
                    return false;
                }
            });
            return f3;
        }


    }

    /**
     * Checks that a load can be cancelled.
     */
    public void testProjectLoadCancel() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        CancellableReload mock2 = new CancellableReload();

        TestProjectFactory.addToProject(f, (p) -> { mock2.setProject(p); return mock2; });
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps1 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertEquals(ProjectReload.Quality.LOADED, ps1.getQuality());
        
        // now reload, but cancel the job:
        
        CompletableFuture<ProjectState> fut = ProjectReload.withProjectState(p, StateRequest.reload());
        Thread.sleep(300);
        fut.cancel(true);

        try {
            ProjectState ps2 = fut.get();
            fail("Should fail with exception");
        } catch (CancellationException ex) {
            // expected.
        }
        sync.get();
        // check the interruption reached into the executing thread.
         assertTrue(mock2.interrupted);
    }
    
    /**
     * If a ReloadImplementation issues an error and not a ProjectStateData, the data is synthesized as broken.
     * But the ReloadImplementation still fires events on an old ProjectStateData it may have cached. This test
     * checks, that the changes propagate to the synthesized "broken" instance and to client-observable ProjectState.
     * @throws Exception 
     */
    public void testBrokenStateDataFireEvents() throws Exception {
        
    }
    
    static final RequestProcessor ASYNC_RP = new RequestProcessor(ProjectReloadImplTest.class);
    
    class CancellableReload2 extends MockProjectReloadImplementation implements Runnable {
        Semaphore latch = new Semaphore(1);
        volatile Thread executing;
        volatile boolean interrupted;

        @Override
        protected ProjectReloadImplementation.ProjectStateBuilder createStateData(
                ProjectReloadImplementation.ProjectStateBuilder b, StateRequest r) {
            b.data("Hey !");
            executing = Thread.currentThread();
            try {
                latch.tryAcquire(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                interrupted = true;
            }
            return b;
        }

        @Override
        public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, LoadContext<ProjectData> context) {
            return CompletableFuture.completedFuture(null).thenComposeAsync(n -> super.reload(project, request, context), ASYNC_RP);
        }

        @Override
        public void run() {
        }
    }

    /**
     * If a project reload is cancelled in the middle, part of implementations may have produced their ProjectStateData
     * and forget the old ones. They are collected in Reloader, but since the process is cancelled, they will not form
     * ProjectState and replace it in the cache.
     * 
     * @throws Exception 
     */
    public void testLoadCancelFiresChangesOnState() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        MockProjectReloadImplementation mock1 = new MockProjectReloadImplementation();
        CancellableReload2 mock2 = new CancellableReload2();
        CancellableReload mock3 = new CancellableReload();
        
        mock3.latch.release(100);

        // this first one will complete
        TestProjectFactory.addToProject(f, (p) -> { mock1.setProject(p); return mock1; });
        // this second one will be cancelled AFTER it produces its data
        TestProjectFactory.addToProject(f, (p) -> { mock2.setProject(p); return mock2; });
        // and this one will be never contacted
        TestProjectFactory.addToProject(f, (p) -> { mock3.setProject(p); return mock3; });
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps1 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertEquals(ProjectReload.Quality.LOADED, ps1.getQuality());
        
        // now reload, but cancel the job:
        mock2.latch.drainPermits();
        
        CompletableFuture<ProjectState> fut = ProjectReload.withProjectState(p, StateRequest.reload());
        Thread.sleep(300);
        fut.cancel(true);
        Thread.sleep(1000);
        try {
            ProjectState ps2 = fut.get();
            fail("Should fail with exception");
        } catch (CancellationException ex) {
            // expected.
        }
        mock2.latch.release();
        
        // wait for the project load process to complete. There's no real event, so watch for
        // pending project operations:
        CountDownLatch ll = new CountDownLatch(1);
        ProjectReloadInternal.getInstance().runProjectAction(p, () -> {
            ll.countDown();
        });
        ll.await();
        // at this time, all events should be settled down.
        
        // now we have a cancelled reload, old state which is still valid
        assertTrue(ps1.isValid());
        
        ProjectStateData mock1New = mock1.lastData.get(null).get();
        StateParts validParts = ReloadApiAccessor.get().getParts(ps1);
        ProjectStateData mock1Valid = validParts.get(mock1);
        assertNotNull(mock1Valid);
        assertNotSame(mock1New, validParts.get(mock1));
        
        ProjectStateData mock2New = mock2.lastData.get(null).get();
        ProjectStateData mock2Valid = validParts.get(mock2);
        assertNotNull(mock2Valid);
        assertNotSame(mock2New, validParts.get(mock2));
        
        ProjectStateData mock3New = mock3.lastData.get(null).get();
        ProjectStateData mock3Valid = validParts.get(mock3);
        assertNotNull(mock3Valid);
        assertSame(mock3New, validParts.get(mock3));
        
        mock1New.fireChanged(false, true);
        assertFalse(ps1.isConsistent());
        assertTrue(ps1.isValid());
        
        mock2New.fireChanged(true, false);
        assertFalse(ps1.isValid());
    }
    
    class CHL implements ChangeListener {
        AtomicInteger changes = new AtomicInteger();
        
        @Override
        public void stateChanged(ChangeEvent e) {
            changes.incrementAndGet();
        }
        
    }

    /**
     * Checks that if a different ProjectState is produced,it invalidates
     * the previous state. The invalidated (but not GCed) states should still
     * fire changes to force its holders to refresh.
     */
    public void testDifferentStateInvalidatesPrevious() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        Semaphore cdl = new Semaphore(0);
        
        CHL l1 = new CHL();
        ProjectState ps1 = ProjectReload.getProjectState(p, false);
        ps1.addChangeListener(l1);
        assertTrue(ps1.isValid());
        assertEquals(ProjectReload.Quality.NONE, ps1.getQuality());
        
        ProjectReloadInternal.NOTIFIER.post(() -> {
            cdl.release();
        }, 300);
        assertTrue(cdl.tryAcquire(10, TimeUnit.SECONDS));

        // upgrade to 'loaded'
        CHL l2 = new CHL();
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        ps2.addChangeListener(l2);
        assertTrue(ps2.getQuality().isAtLeast(ProjectReload.Quality.SIMPLE));
        
        // must wait for all events to be dispatched: queue a task into event dispatch RP
        // to be the last, and wait for it.
        ProjectReloadInternal.NOTIFIER.post(() -> {
            cdl.release();
        }, 300);
        assertTrue(cdl.tryAcquire(10, TimeUnit.SECONDS));
        
        CHL l3 = new CHL();

        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.reload().toQuality(ProjectReload.Quality.LOADED)).get();
        ps3.addChangeListener(l3);
        assertTrue(ps3.getQuality().isAtLeast(ProjectReload.Quality.LOADED));
        
        ProjectReloadInternal.NOTIFIER.post(() -> {
            cdl.release();
        }, 300);
        assertTrue(cdl.tryAcquire(10, TimeUnit.SECONDS));

        CHL l4 = new CHL();
        ProjectState ps4 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        ps4.addChangeListener(l4);
        assertTrue(ps4.getQuality().isAtLeast(ProjectReload.Quality.RESOLVED));
        
        ProjectReloadInternal.NOTIFIER.post(() -> {
            cdl.release();
        }, 300);
        assertTrue(cdl.tryAcquire(10, TimeUnit.SECONDS));

        
        assertEquals(3, l1.changes.get());
        assertEquals(2, l2.changes.get());
        assertEquals(1, l3.changes.get());
        assertEquals(0, l4.changes.get());
    }
    
    /**
     * The changes are made rapidly, the invalidate events should coalesce to just 1.
     */
    public void testRapidEventsCoalesced() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        CHL l1 = new CHL();
        ProjectState ps1 = ProjectReload.getProjectState(p, false);
        ps1.addChangeListener(l1);
        assertTrue(ps1.isValid());
        assertEquals(ProjectReload.Quality.NONE, ps1.getQuality());
        
        CHL l2 = new CHL();
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        ps2.addChangeListener(l2);
        assertTrue(ps2.getQuality().isAtLeast(ProjectReload.Quality.SIMPLE));
        
        CHL l3 = new CHL();
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.reload().toQuality(ProjectReload.Quality.LOADED)).get();
        ps3.addChangeListener(l3);
        assertTrue(ps3.getQuality().isAtLeast(ProjectReload.Quality.LOADED));
        
        CHL l4 = new CHL();
        ProjectState ps4 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        ps4.addChangeListener(l4);
        assertTrue(ps4.getQuality().isAtLeast(ProjectReload.Quality.RESOLVED));
        
        Semaphore cdl = new Semaphore(0);
        ProjectReloadInternal.NOTIFIER.post(() -> {
            cdl.release();
        }, 300);
        assertTrue(cdl.tryAcquire(10, TimeUnit.SECONDS));

        // events coalesced
        assertTrue(l1.changes.get() < 3);
        // events coalesced
        assertTrue(l2.changes.get() < 2);
        // just 1 fired 
        assertEquals(1, l3.changes.get());
        // no events fired
        assertEquals(0, l4.changes.get());
    }
    
    /**
     * Simple load will do nothing if the state is loaded, but inconsistent
     */
    public void testLoadSkipsInconsistent() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        
        FileObject f2 = f.getFileObject("project.txt");
        Files.setLastModifiedTime(FileUtil.toFile(f2).toPath(),
                FileTime.from(Instant.now()));
        f2.refresh();
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertSame(ps2, ps3);
    }
    
    /**
     * Checks that ProjectStateLoad() ignores edited condition, if the project was
     * already loaded.
     */
    public void testLoadSkipsEdited() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        
        FileObject f2 = f.getFileObject("project.txt");
        
        EditorCookie ck = f2.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = ck.openDocument();
        doc.insertString(0, "aa", null);
        doc.remove(0, 2);

        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertSame(ps2, ps3);
    }
    
    /**
     * Checks that load will fail on edited files, if the project was never loaded.
     */
    public void testLoadUnloadedFailsOnEdited() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        FileObject f2 = f.getFileObject("project.txt");
        
        EditorCookie ck = f2.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = ck.openDocument();
        doc.insertString(0, "aa", null);
        doc.remove(0, 2);

        try {
            ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)e.getCause();
            assertEquals(ProjectOperationException.State.OUT_OF_SYNC, ex.getState());
        }
    }
    
    /**
     * Checks that refresh does nothing if files are consistent.
     */
    public void testRefreshRequestSkipsConsistent() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        
        FileObject f2 = f.getFileObject("project.txt");
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.refresh()).get();
        assertSame(ps2, ps3);
    }
    
    /**
     * Checks that if state is reported as inconsistent, refresh will load new state.
     */
    public void testRefreshRequestLoadsInconsistent() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        
        FileObject f2 = f.getFileObject("project.txt");
        Files.setLastModifiedTime(FileUtil.toFile(f2).toPath(),
                FileTime.from(Instant.now()));
        f2.refresh();
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.refresh()).get();
        assertNotSame(ps2, ps3);
        assertFalse(ps2.isValid());
    }
    
    /**
     * Checks that refresh fails on edited files if it decides to load project state
     */
    public void testRefreshFailsOnEdited() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        
        FileObject f2 = f.getFileObject("project.txt");
        
        EditorCookie ck = f2.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = ck.openDocument();
        doc.insertString(0, "aa", null);
        doc.remove(0, 2);

        try {
            ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.refresh()).get();
            fail("Should fail");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)e.getCause();
            assertEquals(ProjectOperationException.State.OUT_OF_SYNC, ex.getState());
        }
    }
    
    /**
     * Forced load should load the new state even though the old is consistent,
     */
    public void testForcedLoadsConsistent() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load().forceReload()).get();
        
        assertNotSame(ps2, ps3);
        assertTrue(ps3.isValid());
        assertFalse(ps2.isValid());
    }
    
    /**
     * Checks that a reload that happens in a custom context does not
     * overlap with the 'default' one.
     */
    public void testCustomContextNoOverlap() throws Exception {
        class Key {
            String k;

            public Key(String k) {
                this.k = k;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 29 * hash + Objects.hashCode(this.k);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Key other = (Key) obj;
                return Objects.equals(this.k, other.k);
            }
        }

        class ContextMockImpl extends MockProjectReloadImplementation {

            public ContextMockImpl(Project project) {
                super(project);
            }

            @Override
            public Object createVariant(Lookup context) {
                return context != null ? context.lookup(Key.class) : null;
            }

        }

        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new ContextMockImpl(p));
        Project p = ProjectManager.getDefault().findProject(f);

        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        assertEquals(ProjectReload.Quality.RESOLVED, ps2.getQuality());
        
        ProjectState ps3 = ProjectReload.withProjectState(p, StateRequest.load()).get();
        assertSame(ps2, ps3);
        
        Key k = new Key("a");
        ProjectState ps4 = ProjectReload.withProjectState(p, 
                StateRequest.load().
                        toQuality(ProjectReload.Quality.NONE).
                        context(Lookups.fixed(k))).get();
        assertNotSame(ps3, ps4);
    }
    
    /**
     * Checks that a ProjectState is GCed, when the client does not keep it.
     * @throws Exception 
     */
    public void testCollectedState() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
       
        TestProjectFactory.addToProject(f, (p) -> new MockProjectReloadImplementation(p));
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        assertNotNull(ps2);
        
        Reference refState = new WeakReference<>(ps2);
        // clear out the state:
        ps2 = null;
        assertGC("State should GC", refState);
    }
    
    public void testCollectedStateData() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        AtomicReference<MockProjectReloadImplementation> reload = new AtomicReference<>();
       
        TestProjectFactory.addToProject(f, (p) -> {
            MockProjectReloadImplementation x = new MockProjectReloadImplementation(p);
            reload.set(x);
            return x;
        });
        Project p = ProjectManager.getDefault().findProject(f);
        
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        assertNotNull(ps2);
        
        ProjectStateData d = ReloadApiAccessor.get().getParts(ps2).values().iterator().next();
        Reference rd = new WeakReference(d);
        d = null;
        ps2 = null;
        
        assertGC("State data should GC", rd);
        MockProjectReloadImplementation r = reload.get();
        assertTrue(r.releasedCalled);
        assertTrue(r.closeCalled);
    }
    
    class Mock1 extends MockProjectReloadImplementation {
        Collection<ProjectStateData> produced = new ArrayList<>();
        int counter;
        
        @Override
        protected ProjectStateBuilder<ProjectData> createStateData(ProjectStateBuilder b, StateRequest request) {
            super.createStateData(b, request);
            b.data(new ProjectData("load " + counter++));
            return b;
        }
        
        

        @Override
        protected ProjectStateData doCreateStateData(Project project, StateRequest request, LoadContext<ProjectData> context) {
            ProjectStateData d = super.doCreateStateData(project, request, context);
            produced.add(d);
            return d;
        }
    }
    
    /**
     * Will cause one repeat of the load.
     */
    class Mock2 extends MockProjectReloadImplementation {
        int counter = 0;
        int limit = 1;

        @Override
        protected ProjectStateBuilder<ProjectData> createStateData(ProjectStateBuilder b, StateRequest request) {
            super.createStateData(b, request);
            b.data("load " + counter);
            return b;
        }
        

        @Override
        protected ProjectStateData doCreateStateData(Project project, StateRequest request, LoadContext<ProjectData> context) {
            ProjectStateData d =  super.doCreateStateData(project, request, context);
            if (counter++ < limit) {
                context.retryReload();
            }
            return d;
        }
    }
    
    /**
     * Checks that all ProjectStateData, even those interim created during reload before retry,
     * are cleaned up with the implementation.
     * @throws Exception 
     */
    public void testRepeatedReload() throws Exception {
        Mock1 m1 = new Mock1();
        Mock2 m2 = new Mock2();
        
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        AtomicReference<MockProjectReloadImplementation> reload = new AtomicReference<>();
       
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m2.project = p;
            return m2;
        });

        Project p = ProjectManager.getDefault().findProject(f);
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        assertNotNull(ps2);
        
        Lookup l = ps2.getLookup();
        ProjectData pd = l.lookup(ProjectData.class);
        String s = l.lookup(String.class);
        assertNotNull(pd);
        assertNotNull(s);
        
        assertEquals("load 1", pd.contents);
        assertEquals("load 1", s);
    }
    
    /**
     * Checks that repeated reloads will fail the operation.
     * @throws Exception 
     */
    public void testInfiniteReload() throws Exception {
        Mock1 m1 = new Mock1();
        Mock2 m2 = new Mock2();
        
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
                
        m2.limit = 2;
       
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m2.project = p;
            return m2;
        });

        Project p = ProjectManager.getDefault().findProject(f);
        try {
            ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
            fail("Should fail with repeat-reload exception");
        } catch (ExecutionException t) {
            assertTrue(t.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)t.getCause();
            
            assertEquals(ProjectOperationException.State.ERROR, ex.getState());
        }
    }
    
    /**
     * During a load, a provider makes part of loaded data inconsistent. This should
     * result in a reload.
     */
    public void testProviderMakesLoadedInconsistent() throws Exception {
        class Mock3 extends MockProjectReloadImplementation {
            int counter = 0;
            int limit = 1;

            @Override
            protected ProjectReloadImplementation.ProjectStateBuilder<ProjectData> createStateData(ProjectReloadImplementation.ProjectStateBuilder b, StateRequest request) {
                super.createStateData(b, request);
                b.data(new String("load " + counter));
                return b;
            }


            @Override
            protected ProjectStateData doCreateStateData(Project project, StateRequest request, ProjectReloadImplementation.LoadContext<ProjectData> context) {
                ProjectStateData d =  super.doCreateStateData(project, request, context);
                if (counter++ < limit) {
                    context.markForReload(ProjectData.class);
                }
                return d;
            }
        }

        Mock1 m1 = new Mock1();
        Mock3 m3 = new Mock3();
        
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        AtomicReference<MockProjectReloadImplementation> reload = new AtomicReference<>();
       
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m3.project = p;
            return m3;
        });

        Project p = ProjectManager.getDefault().findProject(f);
        ProjectState ps2 = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED)).get();
        assertNotNull(ps2);
        
        Lookup l = ps2.getLookup();
        ProjectData pd = l.lookup(ProjectData.class);
        String s = l.lookup(String.class);
        assertNotNull(pd);
        assertNotNull(s);
        
        assertEquals("load 1", pd.contents);
        assertEquals("load 1", s);
    }
    
    static ScheduledExecutorService waitPool = Executors.newScheduledThreadPool(1);
    
    /**
     * Checks that events which are generated asynchronously by project
     * implementation will not be fired during project reload. Events
     * should be fired after project reload completes.
     * 
     * @throws Exception 
     */
    public void testProjectEventsPostponedAfterReload() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        WaitMock m1 = new WaitMock();
        
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        m1.latch.release();
        Project p = ProjectManager.getDefault().findProject(f);
        
        class CL implements ChangeListener {
            AtomicInteger events = new AtomicInteger(0);
            Semaphore sem = new Semaphore(0);
            @Override
            public void stateChanged(ChangeEvent e) {
                events.incrementAndGet();
                sem.release();
            }
            
        }
        ProjectState initial = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.LOADED)).get();        
        CL l = new CL();
        initial.addChangeListener(l);
        
        CompletableFuture<ProjectState> sf = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.RESOLVED));
        // the processing should now stop somewhere at the semaphore
        ProjectStateData ld = m1.lastData.get(null).get();
        assertNotNull(ld);
        
        ld.fireChanged(false, true);
        // hope this is sufficient to deliver events delayed by 300ms
        Thread.sleep(3000);
        
        assertEquals(0, l.events.get());
        m1.latch.release();
        // and another event, invalidating the previous state, should be delivered.
        
        Thread.sleep(3000);
        
        // these two events should be coalesced into one.
        assertEquals(1, l.events.get());
    }
    
    /**
     * Checks that a project without reload support will return NONE quality
     * @throws Exception 
     */
    public void testProjectWithoutReloadSupoort() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        Project p = ProjectManager.getDefault().findProject(f);
        ProjectState initial = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.NONE)).get();
        assertNotNull(initial);
        
        try {
            ProjectState failed = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.FALLBACK)).get();
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof ProjectOperationException);
            ProjectOperationException pox = (ProjectOperationException)ex.getCause();
            assertEquals(ProjectOperationException.State.UNSUPPORTED, pox.getState());
        }
        
    }
    
    public void testImplementationDoesNotParticipate() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        class M1 extends MockProjectReloadImplementation {
            boolean ignoreRequests;

            @Override
            public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, LoadContext<ProjectData> context) {
                if (ignoreRequests) {
                    return null;
                }
                return super.reload(project, request, context); 
            }
        }
        
        class M2 extends Mock1 {
            boolean ignoreRequests;

            @Override
            public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, ProjectReloadImplementation.LoadContext<ProjectData> context) {
                if (ignoreRequests) {
                    return null;
                }
                return super.reload(project, request, context); 
            }
            
        }

        M1 m1 = new M1();
        M2 m2 = new M2();
        
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m2.project = p;
            return m2;
        });

        Project p = ProjectManager.getDefault().findProject(f);
        
        m1.ignoreRequests = true;
        
        ProjectState initial = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.LOADED)).get();
        assertNotNull(initial);
        
        m1.ignoreRequests = false;
        m2.ignoreRequests = true;
        ProjectState second = ProjectReload.withProjectState(p, StateRequest.reload().toQuality(ProjectReload.Quality.LOADED)).get();
        assertNotNull(second);
    }
    
    /**
     * Checks that participants are able to see partial state generated by previous participants 
     * during the load.
     * @throws Exception 
     */
    public void testPartialStateDuringLoad() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        class M3 extends MockProjectReloadImplementation {

            @Override
            protected ProjectStateBuilder createStateData(ProjectStateBuilder b, StateRequest request) {
                super.createStateData(b, request);
                b.data(Long.valueOf(1234));
                return b;
            }

            @Override
            public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, LoadContext<ProjectData> context) {
                assertNotNull(context.getPartialState().getLookup().lookup(ProjectData.class));
                assertNotNull(context.getPartialState().getLookup().lookup(String.class));
                return super.reload(project, request, context);
            }
        }
        
        class M2 extends MockProjectReloadImplementation {
            int counter;
            
            @Override
            protected ProjectReloadImplementation.ProjectStateBuilder<ProjectData> createStateData(ProjectReloadImplementation.ProjectStateBuilder b, StateRequest request) {
                super.createStateData(b, request);
                b.data("load " + counter);
                return b;
            }
            @Override
            public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, ProjectReloadImplementation.LoadContext<ProjectData> context) {
                assertNotNull(context.getPartialState().getLookup().lookup(ProjectData.class));
                counter++;
                return super.reload(project, request, context); 
            }
        }
        
        MockProjectReloadImplementation m1 = new MockProjectReloadImplementation();
        M2 m2 = new M2();
        M3 m3 = new M3();

        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m2.project = p;
            return m2;
        });
        TestProjectFactory.addToProject(f, (p) -> {
            m3.project = p;
            return m3;
        });
        
        Project p = ProjectManager.getDefault().findProject(f);

        ProjectState initial = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.LOADED)).get();
        assertNotNull(initial);
        Collection allData = new ArrayList<>(initial.getLookup().lookupAll(Object.class));
        assertEquals(3, allData.size());
    }
    
    /**
     * Checks that multiple loads of a single project are serialized one after another.
     * @throws Exception 
     */
    public void testProjectMultipleLoadsSerialized() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        CountDownLatch[] reached = new CountDownLatch[2];
        reached[0] = new CountDownLatch(1);
        reached[1] = new CountDownLatch(1);
        
        WaitMock m1 = new WaitMock();
        m1.attemptReached = reached;
        
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        
        Project p = ProjectManager.getDefault().findProject(f);
        
        CompletableFuture<ProjectState> one = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.SIMPLE));
        CompletableFuture<ProjectState> two = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.LOADED));
        // two different reloads, since different qualities
        assertNotSame(one, two);
        
        // reload impl reached in #1
        reached[0].await();
        // #2 not invoked
        assertEquals(1, m1.called.get());
        
        // wait to reach reload #2
        assertFalse(reached[1].await(2, TimeUnit.SECONDS));
        // reload #2 still not invoked
        assertEquals(1, m1.called.get());
        
        // release #1
        m1.latch.release();
        
        one.thenAccept(s -> {
            try {
                // wait some more
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            // one is still valid, as #2 did not complete yet
            assertTrue(one.isDone());
            assertTrue(s.isValid());
            assertFalse(two.isDone());
        }).get();
        
        // wait for the 2nd
        assertTrue(reached[1].await(10, TimeUnit.SECONDS));
        assertTrue(one.get().isValid());
        assertFalse(two.isDone());
        
        m1.latch.release();
        ProjectState s2 = two.get();
        assertFalse(one.get().isValid());
        assertTrue(s2.isValid());
    }
    
    /**
     * Will delay execution by at most 10 seconds or until latch is released.
     */
    class WaitMock extends MockProjectReloadImplementation {
        ProjectStateData compare;
        
        Semaphore latch = new Semaphore(0);
        /**
         * Counts how many times reload was called.
         */
        AtomicInteger called = new AtomicInteger();
        
        /**
         * Released when reload is reached, for test synchronization
         */
        Semaphore reloadReached = new Semaphore(0);
        Semaphore afterReloadReached = new Semaphore(0);
        
        volatile StateRequest loadRequest;
        
        /**
         * Called will be used as a index to this
         */
        volatile CountDownLatch[] attemptReached;
        
        /**
         * Will be released when dataReleased callback is called
         */
        Semaphore dataReleaseLatch = new Semaphore(0);
        
        public WaitMock() {
        }

        @Override
        public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, ProjectReloadImplementation.LoadContext<ProjectData> context) {
            this.loadRequest = request;
            synchronized (this) {
                int n = called.getAndIncrement();
                if (attemptReached != null && attemptReached.length > n) {
                    attemptReached[n].countDown();
                }
            }
            reloadReached.release();
            CompletableFuture<ProjectStateData<ProjectData>> f = super.reload(project, request, context);
            afterReloadReached.release();
            return f.thenCombine(CompletableFuture.runAsync(() -> {
                try {
                    latch.acquire();
                } catch (InterruptedException ex) {
                    fail("Unexpected interrupt");
                }
            }).orTimeout(60, TimeUnit.SECONDS), (pair, v) -> pair);
        }

        @Override
        public void projectDataReleased(ProjectStateData<ProjectData> data) {
            super.projectDataReleased(data);
            if (data == compare) {
                dataReleaseLatch.release();
            }
        }
    }
    /**
     * Checks that loads of multiple projects may happen in parallel.
     * @throws Exception 
     */
    public void testMultipleProjectLoadsInParallel() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o1 = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f1 = FileUtil.copyFile(o1, wd, "Simple11._test");

        FileObject o2 = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f2 = FileUtil.copyFile(o2, wd, "Simple12._test");
        
        
        AtomicInteger called = new AtomicInteger();
        CountDownLatch[] reached = new CountDownLatch[2];
        reached[0] = new CountDownLatch(1);
        reached[1] = new CountDownLatch(1);
        
        WaitMock m1 = new WaitMock();
        WaitMock m2 = new WaitMock();
        
        TestProjectFactory.addToProject(f1, (p) -> {
            m1.project = p;
            return m1;
        });
        TestProjectFactory.addToProject(f2, (p) -> {
            m2.project = p;
            return m2;
        });
        
        Project p1 = ProjectManager.getDefault().findProject(f1);
        Project p2 = ProjectManager.getDefault().findProject(f2);
        
        CompletableFuture<ProjectState> one = ProjectReload.withProjectState(p1, StateRequest.load().toQuality(ProjectReload.Quality.SIMPLE));
        CompletableFuture<ProjectState> two = ProjectReload.withProjectState(p2, StateRequest.load().toQuality(ProjectReload.Quality.SIMPLE));
        
        // two different reloads, since different qualities
        assertNotSame(one, two);
        reached[0].await(1, TimeUnit.SECONDS);
        reached[1].await(1, TimeUnit.SECONDS);
        
        assertFalse(one.isDone());
        assertFalse(two.isDone());
        
        m1.latch.release();
        m2.latch.release();
        
        ProjectState s1 = one.get();
        ProjectState s2 = two.get();
        
        assertTrue(s1.isValid());
        assertTrue(s2.isValid());
    }

    /**
     * ProjectState may be GCed and its ProjectStateData will not be referenced, but
     * the Implementation should be informed only after project loading ends, not
     * during its reload() call.
     * 
     * Implementation details: during load X, the ProjectState from load X-1 is extracted
     * from cache and kept hard-referenced. Event coalescer will also hard-reference ProjectState
     * for ProjectReload.STATE_COALESCE_TIMEOUT_MS ms before it fires the events, then the 
     * entry for the ProjectState is cleared.
     * 
     * @throws Exception 
     */
    public void testDataReleasedAfterProjectLoadEnds() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());
        
        class WM extends WaitMock {
            ProjectStateData keep;
            
            public WM() {
            }
            
            @Override
            public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, StateRequest request, LoadContext<ProjectData> context) {
                CompletableFuture<ProjectStateData<ProjectData>> f = super.reload(project, request, context);
                return f.thenApply((d) -> {
                    keep = d;
                    return d;
                });
            }
        }
        
        WM m1 = new WM();
        
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        
        Project p = ProjectManager.getDefault().findProject(f);
        
        m1.latch.release(3);
        // create some ProjectStateData
        ProjectState one = ProjectReload.withProjectState(p, StateRequest.load().toQuality(ProjectReload.Quality.SIMPLE)).get();
        
        // let's play ugly and keep the state data hard-refed
        ProjectStateData keepData = m1.keep;
        m1.compare = keepData;
        
        assertTrue(one.isConsistent());
        m1.keep.fireChanged(false, true);
        assertFalse(one.isConsistent());
        
        m1.latch.release(3);

        class L implements ChangeListener {
            Semaphore ping = new Semaphore(0);
            volatile boolean pinged;

            @Override
            public void stateChanged(ChangeEvent e) {
                ProjectState s = (ProjectState)e.getSource();
                if (!s.isValid()) {
                    pinged = true;
                    ping.release();
                }
            }
        }
        L l = new L();
        one.addChangeListener(l);
        
        // make a new state. The previous state is often alive during reload, so we need to have 'one' to be cleared out from last-known caches
        ProjectState two = ProjectReload.withProjectState(p, StateRequest.refresh().toQuality(ProjectReload.Quality.SIMPLE)).get();
        assertFalse(m1.releasedCalled);

        // wait for the "one" event to get here, otherwise the state will be hard-referenced in ProjectReload.notifiers.
        ProjectReloadInternal.NOTIFIER.post(() -> {}, 300).waitFinished();
        l.ping.acquire();
        
        m1.latch.drainPermits();
        // start a new reload, and keep it stuck at semaphore
        CompletableFuture<ProjectState> three = ProjectReload.withProjectState(p, StateRequest.reload().toQuality(ProjectReload.Quality.SIMPLE));
        
        Thread.sleep(ProjectReloadInternal.STATE_TIMEOUT_MS + 1000);
        
        // release the project state
        Reference<ProjectState> ref = new WeakReference<>(one);
        one = null;
        two = null;
        // force GC in a hope the identity object will release.
        assertGC("Project State must be GCed", ref);
        
        // not released
        assertFalse(m1.releasedCalled);
        Thread.sleep(2000);
        // still not released
        assertFalse(m1.releasedCalled);
        
        // let the loading process to continue
        m1.latch.release(2);
        three.get();
        
        // wait a litte, as these postponed actions are executed after the reload Future completes
        assertTrue(m1.dataReleaseLatch.tryAcquire(2, TimeUnit.SECONDS));
    }
    

    /**
     * Checks that if a file is modified during reload, the API will not report
     * a failure, but attempts to retry the load.
     */
    public void testFileModifiedDuringLoad() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject o = FileUtil.toFileObject(getDataDir()).getFileObject("reload/Simple1._test");
        FileObject f = FileUtil.copyFile(o, wd, wd.getName());

        class WM extends WaitMock {
            public WM() {
            }

            @Override
            protected ProjectStateData doCreateStateData(Project project, StateRequest request, LoadContext<ProjectData> context) {
                context.ensureLoadContext(ProjectData.class, () -> new ProjectData("")).counter++;
                return super.doCreateStateData(project, request, context);
            }
            
            protected ProjectStateBuilder<ProjectData> createStateData(ProjectReloadImplementation.ProjectStateBuilder b, ProjectReload.StateRequest request) {
                ProjectStateBuilder<ProjectData> b2 = super.createStateData(b, request);
                return b2;
            }
        }

        WM m1 = new WM();
        
        TestProjectFactory.addToProject(f, (p) -> {
            m1.project = p;
            return m1;
        });
        
        Project p = ProjectManager.getDefault().findProject(f);
        
        CompletableFuture<ProjectState> future = ProjectReload.withProjectState(p, StateRequest.refresh());
        
        m1.afterReloadReached.acquire();
        
        FileObject pf = f.getFileObject("project.txt");
        Thread.sleep(2000);
        Files.setLastModifiedTime(FileUtil.toFile(pf).toPath(), FileTime.from(Instant.now()));
        // continue loading, this release will serve for the INITIAL non-NONE load state
        assertSame(Quality.NONE, m1.loadRequest.getMinQuality());
        assertEquals(1, m1.loadProjectData.counter);
        m1.latch.release();

        m1.afterReloadReached.acquire();
        Thread.sleep(2000);
        Files.setLastModifiedTime(FileUtil.toFile(pf).toPath(), FileTime.from(Instant.now()));
        assertEquals(1, m1.loadProjectData.counter);
        m1.latch.release();
        
        m1.afterReloadReached.acquire();
        assertEquals("The load goes through ReloadImplementation 2nd time", 2, m1.loadProjectData.counter);
        m1.latch.release();
        
        ProjectState ps = future.get();
        
        assertTrue("The result is the most recent", ps.isValid());
        assertTrue("Consistent after reload", ps.isConsistent());
    }
}
