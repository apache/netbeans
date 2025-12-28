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
package org.netbeans.modules.java.lsp.server.project;

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.TestCase.fail;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.problems.BrokenProjectNotifier;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
public class LspBrokenReferencesImplTest extends NbTestCase {

    public LspBrokenReferencesImplTest(String name) {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception {
        reporter = new TestProblemReporter();
        super.setUp();
        
        clearWorkDir();
        FileObject r = FileUtil.getConfigRoot();
        // Hack: create .instance registration in the config area. We do not have project lookup mocking service - yet.
        FileObject dir = FileUtil.createFolder(r, "Projects/org-netbeans-modules-maven/Lookup");
        dir.getFileSystem().runAtomicAction(() -> {
            FileObject regFile = dir.createData("test-problem-provider.instance");
            // works with the in-memory filesystem. Will not work if module system gets loaded.
            regFile.setAttribute("instanceCreate", forProjectServices());
            regFile.setAttribute("position", 10000);
        });
        BrokenProjectNotifier.getInstnace().start();

        MockLookup.setLayersAndInstances(displayer, tested);
        
        // causes project info to be loaded synchronously
        System.setProperty("test.load.sync", Boolean.TRUE.toString());
    }

    @Override
    protected void tearDown() throws Exception {
        BrokenProjectNotifier.getInstnace().stop();
        // Hack: create .instance registration in the config area. We do not have project lookup mocking service - yet.
        FileObject f = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven/Lookup/test-problem-provider.instance");
        if (f != null) {
            f.delete();
        }
        if (displayer != null) {
            for (Response r : displayer.expectedResponses) {
                if (r.responseLock != null) {
                    r.responseLock.countDown();
                }
            }
            Response r = displayer.currentResponse;
            if (r != null && r.responseLock != null) {
                r.responseLock.countDown();
            }
            while (displayer.currentResponse != null) {
                synchronized (displayer) {
                    displayer.notifyAll();
                }
            }
        }
        // reset custom thresholds
        ProjectAlertPresenter.MAX_PRESENTED_ERRORS = 10;
        ProjectAlertPresenter.ERRORS_WAKEUP_DELAY = 2 * 60 * 1000;
        ProjectAlertPresenter.QUESTION_WAKEUP_DELAY = 5 * 60 * 1000;
        super.tearDown();
    }
    
    private TestProblemReporter reporter = new TestProblemReporter();
    
    private FileObject createProject(FileObject dir, String name, String... lines) throws IOException {
        final FileObject[] ret = new FileObject[1];
        dir.getFileSystem().runAtomicAction(() -> {
            FileObject projDir = name == null ? dir : dir.createFolder(name);
            ret[0] = projDir;
            try (OutputStream os = projDir.createAndOpen("pom.xml");
                 OutputStreamWriter wr = new OutputStreamWriter(os);
                 PrintWriter pw = new PrintWriter(wr)) {
                for (String l : lines) {
                    pw.println(l);
                }
            }
        });
        return ret[0];
    }
    
    final DialogController displayer = new DialogController();
    final TestedImpl tested = new TestedImpl();
    
    final AtomicBoolean resolveCalled = new AtomicBoolean(false);
    final AtomicReference<ProjectProblemsProvider.ProjectProblem> ref = new AtomicReference<>();
    
    /**
     * Fills in 'ref'
     */
    private void createFixableError() {
        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "Test",
                "Fixable-error",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref.get());
                    resolveCalled.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref.set(pp);
        reporter.reportProblems.add(pp);
    }
    
    private FileObject createSimpleProject() throws IOException {
        
        File wdBase = getWorkDir();
        FileObject wdFile = FileUtil.toFileObject(wdBase);
        
        projectServices.put(wdBase.toPath(), Arrays.asList(reporter));
        FileObject pdir = createProject(wdFile, null, 
            "<project xmlns='http://maven.apache.org/POM/4.0.0'>",
            "   <modelVersion>4.0.0</modelVersion>",
            "   <artifactId>m</artifactId>" +
            "   <groupId>g</groupId>" +
            "   <version>1.0-SNAPSHOT</version>" +
            "   <name>Test Project</name>" +
            "</project>"
        );
        return pdir;
    }
        
    /**
     * Checks that a fixable error appears to the user, and can be resolved.
     */
    public void testFixableReport() throws Exception {
        FileObject pdir = createSimpleProject();
        
        Semaphore block = new Semaphore(0);
        
        displayer.expectedResponses.add(new Response("Test Project", "Fixable-error", NotifyDescriptor.OK_OPTION));
        displayer.responsePermits.drainPermits();
        
        createFixableError();
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        
        tested.presenterNotify.put(prj.getProjectDirectory(), block);
        
        assertNotNull(prj);
        ProjectProblemsProvider prov = prj.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(prov);
        assertFalse(prov.getProblems().isEmpty());
       
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(block.tryAcquire(10000, TimeUnit.SECONDS));
        ProjectAlertPresenter presenter = tested.getPresenter(prj);
        
        // The displayer execution is now blocked, so the presenter remains.
        assertNotNull(presenter);
        
        // release the displayer -> let the presenter to finish.
        displayer.responsePermits.release();
        
        assertTrue("Fixes accepted", presenter.getCompletion().get());
        assertNull("Presenter has finished", tested.getPresenter(prj));
        
        Collection<? extends ProjectProblemsProvider.ProjectProblem> probs = prov.getProblems();
        assertTrue(probs.isEmpty());
    }
    
    /**
     * Check that a project with 'hard' errors will issue the messages and then
     * allow the user to fix the fixable ones.
     * @throws Exception 
     */
    public void testHardErrorComesBeforeFixable() throws Exception {
        FileObject pdir = createSimpleProject();

        // deliberately in the reverse order
        createFixableError();

        ProjectProblemsProvider.ProjectProblem unfixable = ProjectProblemsProvider.ProjectProblem.createError(
                "Unfixable",
                "Bye-bye",
                null);
        reporter.reportProblems.add(unfixable);
        reporter.response.release(100);

        // response to the fixable error
        Response r = new Response("Test Project", "Fixable-error", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);
        
        // response to the unfixable error
        displayer.expectedResponses.add(new Response("Test Project", "Unfixable", NotifyDescriptor.OK_OPTION));

        // at the time the response is answered
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();
        
        assertTrue(r.responseSelected.tryAcquire(100, TimeUnit.SECONDS));
        assertSame(r, displayer.currentResponse);
       
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        
        // check that the notified list already contains the 'error' message
        assertTrue(displayer.notifyNow.stream().anyMatch(nd -> nd.getMessage().toString().contains("Bye-bye")));
        
        r.responseLock.countDown();
        
        assertTrue(p.getCompletion().get());
    }
    
    private List<Response> responses;
    
    private Semaphore responseCount;
    
    private FileObject pdir;
    
    /**
     * Fills in pdir, responseCount, responses.
     */
    private void setupProjectWithUnfixableErrors() throws Exception {
        pdir = createSimpleProject();

        List<ProjectProblemsProvider.ProjectProblem> unfixables = new ArrayList<>();
        responses = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            unfixables.add(ProjectProblemsProvider.ProjectProblem.createError(
                "Unfixable",
                "Unfixable" + i,
                null));
        }
        reporter.reportProblems.addAll(unfixables);
        reporter.response.release(100);

        responseCount = new Semaphore(0);
        for (int i = 0; i < 5; i++) {
            Response r = new Response("Test Project", "Unfixable" + i, NotifyDescriptor.OK_OPTION);
            r.responseSelected = responseCount;
            r.responseLock = new CountDownLatch(1);
            responses.add(r);
        }
        displayer.expectedResponses.addAll(responses);
        
    }
    
    /**
     * Checks that if there are too many errors (more than permitted threshold), they are still
     * displayed - after user confirms some of the earlier ones, the rest will appear.
     * @throws Exception 
     */
    public void testTooManyErrorsStillDisplayed() throws Exception {
        ProjectAlertPresenter.MAX_PRESENTED_ERRORS = 2;
        setupProjectWithUnfixableErrors();
        // at the time the response is answered
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();
        
        responses.get(4).responseLock.countDown();
        responses.get(3).responseLock.countDown();
        responses.get(2).responseLock.countDown();

        // wait till >= two responses are selected
        assertTrue(responseCount.tryAcquire(2, 10, TimeUnit.SECONDS));
        
        assertEquals("Two notify Descriptors received, others blocked by RP", 2, displayer.notifyNow.size());
        assertEquals("None response answered yet", 0, displayer.answeredResponses.size());
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse(p.getCompletion().isDone());

        // release three problems
        responses.get(0).responseLock.countDown();
        responses.get(1).responseLock.countDown();
        
        // the presenter should now be able to finish
        assertTrue("The presenter is able to finish", p.getCompletion().get());
    }
    
    /**
     * Checks that if there are errors displayed, they will eventually time out and
     * the process will terminate.
     * @throws Exception 
     */
    public void testIgnoredErrorsTimeout() throws Exception {
        // reduce the timeout to a manageable time:
        ProjectAlertPresenter.ERRORS_WAKEUP_DELAY = 1000;
        setupProjectWithUnfixableErrors();

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        // let the notifications to be fired.
        assertTrue(responseCount.tryAcquire(3, 10, TimeUnit.SECONDS));
        // wait some more
        Thread.sleep(250);
        
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse("Presenter is still running", p.getCompletion().isDone());
        
        assertTrue("The presenter finishes because of timeout", p.getCompletion().get());
    }
    
    /**
     * Checks that a fixable error *is* presented despite many hard errors, after a timeout.
     * @throws Exception 
     */
    public void testQuestionPresentedWithManyErrors() throws Exception {
        // reduce the timeout to a manageable time:
        ProjectAlertPresenter.ERRORS_WAKEUP_DELAY = 1000;
        setupProjectWithUnfixableErrors();
        createFixableError();

        Response r = new Response("Test Project", "Fixable-error", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(r);

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();
        
        assertTrue("Fixable error must be reported after timeout", r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
    }
    
    AtomicReference<ProjectProblemsProvider.ProjectProblem> ref2 = new AtomicReference<>();
    AtomicBoolean resolveCalled2 = new AtomicBoolean();
    
    private void setupTwoFixables() throws Exception {
        pdir = createSimpleProject();
        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "Test",
                "Fixable1",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref.get());
                    resolveCalled.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref.set(pp);
        reporter.reportProblems.add(pp);
        
        ProjectProblemsProvider.ProjectProblem pp2 = ProjectProblemsProvider.ProjectProblem.createError(
                "Test",
                "Fixable2",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref2.get());
                    resolveCalled2.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref2.set(pp);
        reporter.reportProblems.add(pp2);
    }
    
    public void testOneQuestionFollowsOther() throws Exception {
        setupTwoFixables();
        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);
        
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        // wait for the 1st response to be selected
        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        // the presenter is active, and the 2nd question is NOT yet even asked
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse(p.getCompletion().isDone());
        assertTrue(displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable1")));
        assertTrue(displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        // release the response
        r.responseLock.countDown();
        
        // 2nd response is presented - at this time 1st problem must be already resolved.
        assertTrue(r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        assertTrue(resolveCalled.get());
        
        r2.responseLock.countDown();
        assertTrue("The presenter is able to finish", p.getCompletion().get());        
        assertTrue(resolveCalled2.get());
    }
    
    public void testOneResolveOneNot() throws Exception {
        setupTwoFixables();
        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);
        
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        // wait for the 1st response to be selected
        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        // the presenter is active, and the 2nd question is NOT yet even asked
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse(p.getCompletion().isDone());
        assertTrue(displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable1")));
        assertTrue(displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        // release the response
        r.responseLock.countDown();
        
        // 2nd response is presented - at this time 1st problem must be already resolved.
        assertTrue(r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        assertTrue(resolveCalled.get());
        
        r2.responseLock.countDown();
        assertTrue("The presenter is able to finish", p.getCompletion().get());        
        assertTrue(resolveCalled2.get());
    }
    
    public void testRejectOneAcceptOther() throws Exception {
        setupTwoFixables();
        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.NO_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);
        
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        // wait for the 1st response to be selected
        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));

        // the presenter is active, and the 2nd question is NOT yet even asked
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse(p.getCompletion().isDone());
        assertTrue(displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable1")));
        /*
        if (!displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2"))) {
            System.err.println("");
        }
        */
        assertTrue(displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        // release the response
        r.responseLock.countDown();
        
        // 2nd response is presented - at this time 1st problem must be already resolved.
        assertTrue(r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        // the rejected fix did not happen
        assertFalse(resolveCalled.get());
        // the 2nd question presented
        assertTrue(displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        r2.responseLock.countDown();
        assertTrue("The presenter is able to finish", p.getCompletion().get());        
        assertTrue(resolveCalled2.get());
    }

    public void testRestResolvesWithoutAsking() throws Exception {
        setupTwoFixables();
        Response r = new Response("Test Project", "Fixable1", Bundle.ProjectProblems_RestOption());
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(r2);
        
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        // wait for the 1st response to be selected
        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));

        // the presenter is active, and the 2nd question is NOT yet even asked
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        assertFalse(p.getCompletion().isDone());
        assertTrue("1st dialog shown", displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable1")));
        assertTrue("2nd dialog not shown yet", displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        // release the response
        r.responseLock.countDown();
        assertTrue("The presenter is able to finish", p.getCompletion().get());
        
        assertTrue("2nd dialog never presented", displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
        
        // both fixes happened
        assertTrue("Fix 1 applied", resolveCalled.get());
        assertTrue("Fix 2 applied", resolveCalled2.get());
    }
    
    /**
     * After problem fix fails, a report with that error must be displayed.
     */
    public void testProblemFixFails() throws Exception {
        pdir = createSimpleProject();
        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable1",
                "Fixable1",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    resolveCalled.set(true);
                    return CompletableFuture.completedFuture(
                            ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, "NotResolved1")
                    );
                }
            }
        );
        ref.set(pp);
        reporter.reportProblems.add(pp);

        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response errResp = new Response("Test Project", "failed: NotResolved1", NotifyDescriptor.OK_OPTION);
        errResp.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(errResp);

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();

        // wait for the 1st response to be selected
        assertTrue("Fix response selected", errResp.responseSelected.tryAcquire(10, TimeUnit.SECONDS));

        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        
        Optional<NotifyDescriptor> nd = displayer.notifyNow.stream().filter(d -> d.getMessage().toString().contains("failed: NotResolved1")).findAny();
        assertTrue("Error dialog shown", nd.isPresent());
        assertEquals("Just OK is present.", 1, nd.get().getOptions().length);
    }
    
    private void setupFailAndOkFixables() throws Exception {
        pdir = createSimpleProject();
        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable1",
                "Fixable1",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    resolveCalled.set(true);
                    return CompletableFuture.completedFuture(
                            ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, "NotResolved1")
                    );
                }
            }
        );

        ProjectProblemsProvider.ProjectProblem pp2 = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable2",
                "Fixable2",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.add(ref2.get());
                    resolveCalled2.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref.set(pp);
        ref2.set(pp2);
        reporter.reportProblems.add(pp);
        reporter.reportProblems.add(pp2);

        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);

        Response errResp = new Response("Test Project", "failed: NotResolved1", NotifyDescriptor.OK_OPTION);
        errResp.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(errResp);

        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(r2);
    }
    
    
    /**
     * 1st question confirmed, resolution fails. Error message appears, user clicks details. Next question appears.
     * @throws Exception 
     */
    public void testFirstProblemFailsExecuteNext() throws Exception {
        setupFailAndOkFixables();
        Response r = displayer.expectedResponses.get(0);
        Response errResp = displayer.expectedResponses.get(1);
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();

        // wait for the 1st response to be selected
        assertTrue("Fix response selected", errResp.responseSelected.tryAcquire(10, TimeUnit.SECONDS));

        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        
        Optional<NotifyDescriptor> nd = displayer.notifyNow.stream().filter(d -> d.getMessage().toString().contains("failed: NotResolved1")).findAny();
        assertTrue("Error dialog shown", nd.isPresent());
        assertEquals("Fix all, Details, Cancel should be present.", 3, nd.get().getOptions().length);
        assertTrue(resolveCalled2.get());
        assertTrue("Second question displayed.", 
                displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable2")));
    }
    
    /**
     * After 1st fix fails, "Fix all" is pressed. The next dialog is not displayed, but the
     * fix is applied.
     * @throws Exception 
     */
    public void testFirstProblemFailsExecuteRest() throws Exception {
       setupFailAndOkFixables();
        Response r = displayer.expectedResponses.get(0);
        Response errResp = displayer.expectedResponses.get(1);
        // let the rest of fixes happen
        errResp.response = Bundle.ProjectProblems_RestOption();
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();

        // wait for the 1st response to be selected
        assertTrue("Fix response selected", errResp.responseSelected.tryAcquire(10, TimeUnit.SECONDS));

        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        
        Optional<NotifyDescriptor> nd = displayer.notifyNow.stream().filter(d -> d.getMessage().toString().contains("failed: NotResolved1")).findAny();
        assertTrue("Error dialog shown", nd.isPresent());
        assertEquals("Fix all, Details, Cancel should be present.", 3, nd.get().getOptions().length);
        assertTrue(resolveCalled2.get());
        assertTrue("Second question NOT displayed.", 
                displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("Fixable2")));
    }
    
    /**
     * The process is cancelled after 1st failure.
     * @throws Exception 
     */
    public void testFirstProblemFailsCancel() throws Exception {
        setupFailAndOkFixables();
        Response r = displayer.expectedResponses.get(0);
        Response errResp = displayer.expectedResponses.get(1);
        
        errResp.response = NotifyDescriptor.CANCEL_OPTION;
        
        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        ProjectAlertPresenter p = tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();

        // wait for the 1st response to be selected
        assertTrue("Fix response selected", errResp.responseSelected.tryAcquire(100, TimeUnit.SECONDS));

        assertFalse("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        
        Optional<NotifyDescriptor> nd = displayer.notifyNow.stream().filter(d -> d.getMessage().toString().contains("failed: NotResolved1")).findAny();
        assertTrue("Error dialog shown", nd.isPresent());
        assertEquals("Fix all, Details, Cancel should be present.", 3, nd.get().getOptions().length);
        assertFalse("Second fixable skipped.", resolveCalled2.get());
        assertTrue("Second question NOT displayed.", 
                displayer.notifyNow.stream().noneMatch(d -> d.getMessage().toString().contains("failed: NotResolved2")));
    }
    
    /**
     * Let the user ignore the 1st reported problem. The next problem should be displayed after timeout and
     * should proceed, of OKed.
     */
    public void testFirstProblemTimeoutNextOK() throws Exception {
        ProjectAlertPresenter.QUESTION_WAKEUP_DELAY = 500;
        setupTwoFixables();

        Response r = new Response("Test Project", "Fixable1", USER_IGNORED);
        r.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(r);
        
        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue(r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        TestedPresenter p = (TestedPresenter)tested.getPresenter(prj);
        assertNotNull(p);
        
        // wait for the timeout
        assertTrue(p.timeoutSem.tryAcquire(10, TimeUnit.SECONDS));

        
        // release the 2nd response
        r2.responseLock.countDown();
        assertTrue(r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled2.get());
    }
    
    /**
     * Simulate ignoring the question, then a next alert() comes for the project, so the next
     * question should be displayed.
     * @throws Exception 
     */
    public void testTimeoutNextDisplaysAfterAlert() throws Exception {
        ProjectAlertPresenter.QUESTION_WAKEUP_DELAY = 100000;
        setupTwoFixables();

        Response r = new Response("Test Project", "Fixable1", USER_IGNORED);
        r.responseSelected = new Semaphore(0);
        displayer.expectedResponses.add(r);
        
        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);


        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue("First question ignored", r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        TestedPresenter p = (TestedPresenter)tested.getPresenter(prj);
        assertNotNull(p);
        
        // wait some considerable time
        Thread.sleep(500);
        assertEquals("Second response not picked yet", 0, r2.responseSelected.availablePermits());
        assertEquals("Second dialog not displayed yet", 1, displayer.notifyNow.size());
        
        tested.showAlert(prj);

        assertTrue("Second question selected", r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals("Second dialog displayer", 2, displayer.notifyNow.size());        
        
        r2.responseLock.countDown();
        
        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled2.get());
        assertFalse(resolveCalled.get());
    }
    
    /**
     * If a next problem is discovered during a problem's fix, the additional problem will be displayed at the end,
     * but will not obscur existing conversation.
     */
    public void testDisplayAdditionalProblem() throws Exception {
        pdir = createSimpleProject();
        
        
        AtomicBoolean resolveCalled3 = new AtomicBoolean();
        AtomicReference<ProjectProblemsProvider.ProjectProblem> ref3 = new AtomicReference<>();
        ProjectProblemsProvider.ProjectProblem pp3 = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable3",
                "Fixable3",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref3.get());
                    resolveCalled3.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref3.set(pp3);

        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "Test",
                "Fixable1",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref.get());
                    resolveCalled.set(true);
                    reporter.reportProblems.add(pp3);
                    // report an error
                    reporter.fireProblems();
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );

        ProjectProblemsProvider.ProjectProblem pp2 = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable2",
                "Fixable2",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.add(ref2.get());
                    resolveCalled2.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref.set(pp);
        ref2.set(pp2);
        reporter.reportProblems.add(pp);
        reporter.reportProblems.add(pp2);

        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);
        
        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        r2.responseSelected = new Semaphore(0);
        r2.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r2);

        Response r3 = new Response("Test Project", "Fixable3", NotifyDescriptor.OK_OPTION);
        displayer.expectedResponses.add(r3);

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue("First question answered", r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        TestedPresenter p = (TestedPresenter)tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();
        
        assertTrue("2nd question answered", r2.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        assertEquals("First 2 problems presented", 2, displayer.notifyNow.size());
        
        r2.responseLock.countDown();
        
        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        assertTrue(resolveCalled2.get());
        assertTrue(resolveCalled3.get());
        
        assertTrue("Late problem was presented", displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Fixable3")));

    }
    
    /**
     * If an error is reported during problem fix, it is shown immediately
     */
    public void testDisplayAdditionalError() throws Exception {
        pdir = createSimpleProject();
        ProjectProblemsProvider.ProjectProblem pp = ProjectProblemsProvider.ProjectProblem.createError(
                "Test",
                "Fixable1",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.remove(ref.get());
                    resolveCalled.set(true);
                    reporter.reportProblems.add(ProjectProblemsProvider.ProjectProblem.createError("Test2", "Unfixable1", null));
                    // report an error
                    reporter.fireProblems();
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );

        ProjectProblemsProvider.ProjectProblem pp2 = ProjectProblemsProvider.ProjectProblem.createError(
                "TestFixable2",
                "Fixable2",
                new ProjectProblemResolver() {
                @Override
                public Future<ProjectProblemsProvider.Result> resolve() {
                    reporter.reportProblems.add(ref2.get());
                    resolveCalled2.set(true);
                    return CompletableFuture.completedFuture(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
        );
        ref.set(pp);
        ref2.set(pp2);
        reporter.reportProblems.add(pp);
        reporter.reportProblems.add(pp2);

        Response r = new Response("Test Project", "Fixable1", NotifyDescriptor.OK_OPTION);
        r.responseSelected = new Semaphore(0);
        r.responseLock = new CountDownLatch(1);
        displayer.expectedResponses.add(r);
        
        Response r2 = new Response("Test Project", "Fixable2", NotifyDescriptor.OK_OPTION);
        displayer.expectedResponses.add(r2);

        Response r3 = new Response("Test Project", "Unfixable1", NotifyDescriptor.OK_OPTION);
        displayer.expectedResponses.add(r3);

        Project prj = ProjectManager.getDefault().findProject(pdir);
        OpenProjects.getDefault().open(new Project[] { prj } , true);
        OpenProjects.getDefault().openProjects().get();

        assertTrue("First question answered", r.responseSelected.tryAcquire(10, TimeUnit.SECONDS));
        
        TestedPresenter p = (TestedPresenter)tested.getPresenter(prj);
        assertNotNull(p);
        
        r.responseLock.countDown();
        
        assertTrue("The presenter finished", p.getCompletion().get());
        assertTrue(resolveCalled.get());
        assertTrue(resolveCalled2.get());
        
        assertTrue("Late error was presented", displayer.notifyNow.stream().anyMatch(d -> d.getMessage().toString().contains("Unfixable1")));
    }
    
    static Map<Path, Collection> projectServices = Collections.synchronizedMap(new HashMap<>());
    
    static class TestedPresenter extends ProjectAlertPresenter {
        Semaphore timeoutSem = new Semaphore(0);
        
        public TestedPresenter(Project project, BrokenReferencesModel model, Env master) {
            super(project, model, master);
        }

        @Override
        void processOneRound(Ctx ctx) {
            super.processOneRound(ctx);
        }
        
        void resumeAfterTimeout(Ctx ctx) {
            super.resumeAfterTimeout(ctx);
            timeoutSem.release();
        }
    }
    
    static class TestedImpl extends BrokenReferencesImpl {
        Map<FileObject, Semaphore> presenterLock = Collections.synchronizedMap(new HashMap<>());
        Map<FileObject, Semaphore> presenterNotify = Collections.synchronizedMap(new HashMap<>());

        @Override
        ProjectAlertPresenter createPresenter(Project project, BrokenReferencesModel model) {
            return new TestedPresenter(project, model, this);
        }
        
        @Override
        public CompletableFuture<Void> showAlert(@NonNull Project project) {
            Semaphore s = presenterLock.get(project.getProjectDirectory());
            if (s != null) {
                try {
                    s.acquire();
                } catch (InterruptedException ex) {
                    fail();
                }
            }
            CompletableFuture<Void> r = super.showAlert(project);
            s = presenterNotify.get(project.getProjectDirectory());
            if (s != null) {
                s.release();
            }        
            return r;
        }
    }
    
    static class Response {
        String projectName;
        String description;
        String label;
        Object response;
        volatile Semaphore responseSelected;
        volatile CountDownLatch responseLock;

        public Response(String projectName, String label, Object response) {
            this.projectName = projectName;
            this.label = label;
            this.response = response;
        }

        public Response(String projectName, String label, String description, Object response) {
            this.projectName = projectName;
            this.description = description;
            this.label = label;
            this.response = response;
        }
        
        public boolean matches(NotifyDescriptor d) {
            return (d.getTitle().contains(projectName) && (label == null || d.getMessage().toString().contains(label)));
        }
        
        public Object respond(NotifyDescriptor d) {
            if (responseSelected != null) {
                responseSelected.release();
            }
            if (responseLock != null) {
                try {
                    responseLock.await();
                } catch (InterruptedException ex) {
                    fail();
                }
            }
            return response;
        }
    }
    
    /**
     * Controllable problem reporter.
     */
    static class TestProblemReporter implements ProjectProblemsProvider {
        List<ProjectProblem> reportProblems = Collections.synchronizedList(new ArrayList<>());
        Semaphore response = new Semaphore(50);
        Semaphore called = new Semaphore(0);
        PropertyChangeSupport supp = new PropertyChangeSupport(this);
        
        void clear() {
            reportProblems.clear();
            supp.firePropertyChange(PROP_PROBLEMS, null, null);
        }
        
        void fireProblems() {
            supp.firePropertyChange(PROP_PROBLEMS, null, null);
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            supp.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            supp.removePropertyChangeListener(listener);
        }

        @Override
        public Collection<? extends ProjectProblem> getProblems() {
            try {
                called.release();
                response.acquire();
                return reportProblems;
            } catch (InterruptedException ex) {
                fail();
                return null;
            }
        }
    }

    static final String USER_IGNORED = "user-ignore";
    
    /**
     * Mock displayer, so that user responses can be controlled.
     */
    static class DialogController extends DialogDisplayer {
        final List<NotifyDescriptor> notifyLater = Collections.synchronizedList(new ArrayList<>());
        final List<NotifyDescriptor> notifyNow = Collections.synchronizedList(new ArrayList<>());
        
        final Semaphore responsePermits = new Semaphore(100);
        final List<Response> expectedResponses =  Collections.synchronizedList(new ArrayList<>());
        final List<Response> answeredResponses =  Collections.synchronizedList(new ArrayList<>());
        volatile Response currentResponse =  null;
        final Semaphore responseAnswered = new Semaphore(0);

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            try {
                notifyNow.add(descriptor);
                responsePermits.acquire();
            } catch (InterruptedException ex) {
                fail("Interrupted");
            }
            
            List<Response> lst = new ArrayList<>(expectedResponses);
            for (Response r : lst) {
                if (r.matches(descriptor)) {
                    expectedResponses.remove(r);
                    currentResponse = r;
                    responseAnswered.release();
                    Object o = r.respond(descriptor);
                    answeredResponses.add(r);
                    if (o == USER_IGNORED) {
                        synchronized (this) {
                            try {
                                // block
                                wait();
                            } catch (InterruptedException ex) {
                                fail();
                            }
                        }
                        o = NotifyDescriptor.CLOSED_OPTION;
                    }
                    descriptor.setValue(o);
                    currentResponse = null;
                    return o;
                }
            }
            fail("Unexpected NotifyDescriptor: " + descriptor);
            return null;
        }

        @Override
        public void notifyLater(NotifyDescriptor descriptor) {
            notifyLater.add(descriptor);
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            fail();
            return null;
        }
        
        // ensure greater throughput for the otherwise simple default DialogDescriptor.notifyFuture
        RequestProcessor futureProcessor = new RequestProcessor("requests", 20);

        public <T extends NotifyDescriptor> CompletableFuture<T> notifyFuture(final T descriptor) {
            CompletableFuture<T> r = new CompletableFuture<>();
            // preserve potential context
            Lookup def = Lookup.getDefault();
            futureProcessor.post(new Runnable() {
                public void run() {
                    Lookups.executeWith(def, () -> {
                        try {
                            DialogController.this.notify(descriptor);
                            r.complete(descriptor);
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            r.completeExceptionally(t);
                        }
                    });
                }
            });
            return r;
        }
    }
    
    
    public static LookupProvider forProjectServices() {
        return new ProjectTestServices();
    }
    
    /**
     * This class is registered in the (maven) project Lookup, so that 
     * the test can mock / inject problem reporter service.
     */
    private static class ProjectTestServices implements LookupProvider {
        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            Project p = baseContext.lookup(Project.class);
            Collection services = projectServices.get(FileUtil.toFile(p.getProjectDirectory()).toPath());
            if (services == null) {
                return Lookup.EMPTY;
            } else {
                return Lookups.fixed(services.toArray(new Object[0]));
            }
        }
    }
    
}
