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
package org.netbeans.modules.maven.problems;

import org.netbeans.modules.maven.execute.MockMavenExec;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.InternalActionDelegate;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;
import org.openide.windows.InputOutput;

/**
 *
 * @author sdedic
 */
public class PrimingActionTest extends NbTestCase {

    public PrimingActionTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        System.getProperties().remove("test.reload.sync");
        super.tearDown(); 
    }

    private void setupBrokenProject() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), 
             "<project xmlns='http://maven.apache.org/POM/4.0.0'>"
            + "  <modelVersion>4.0.0</modelVersion>" 
            + "  <parent>"
            + "    <groupId>g</groupId>"
            + "    <artifactId>par</artifactId>"
            + "    <version>0</version>"
            + "  </parent>" 
            + "  <artifactId>m</artifactId>" 
            + "  <groupId>g</groupId>"
            + "</project>");
        
    }
    
    private void setupOKProject() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), 
             "<project xmlns='http://maven.apache.org/POM/4.0.0'>"
            + "  <modelVersion>4.0.0</modelVersion>" 
            + "  <artifactId>m</artifactId>" 
            + "  <groupId>g</groupId>"
            + "    <version>0</version>"
            + "</project>");
        
    }
    
    public void testActionPresent() throws Exception {
        setupBrokenProject();
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME));
    }
    
    private Collection<? extends ProjectProblemsProvider.ProjectProblem> collectProblems(Project p) {
        Collection<? extends ProjectProblemsProvider.ProjectProblem> res = p.getLookup().lookupAll(ProjectProblemsProvider.class).stream().
                map(ProjectProblemsProvider::getProblems).
                flatMap(Collection::stream).
                collect(Collectors.toList());
        return res;
    }
    
    public void testLookupNotBroken() throws Exception {
        setupBrokenProject();
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        Collection<? extends ActionProvider> provs = p.getLookup().lookupAll(ActionProvider.class);
        assertEquals(1, provs.size());
        
        p.getLookup().lookupAll(ProjectProblemsProvider.class).toArray();
        InternalActionDelegate[] deles = p.getLookup().lookupAll(InternalActionDelegate.class).toArray(new InternalActionDelegate[0]);
        assertTrue(deles.length >= 1);
        // still single
        Collection<? extends ActionProvider> newProvs = p.getLookup().lookupAll(ActionProvider.class);
        assertEquals(1, newProvs.size());
        
        assertSame(provs.iterator().next(), newProvs.iterator().next());
    }
    
    /**
     * Checks that the action is enabled even though problems are not evaluated yet.
     */
    public void testActionEnabledConservatively() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        MavenModelProblemsProvider.RP.submit(() -> {
            try {
                cdl.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        setupBrokenProject();
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        // no problems yet, and the resolution is blocked.
        assertTrue(collectProblems(p).isEmpty());
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        boolean enabled = ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY);
        cdl.countDown();
        assertTrue(enabled);
    }
    
    /**
     * Checks that action is still enabled if the problems contains problems solvable by
     * priming.
     */
    public void testActionEnabledWithBrokenProject() throws Exception {
        setupBrokenProject();
        System.setProperty("test.reload.sync", "true");
        
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        // we've identified problems
        assertFalse(collectProblems(p).isEmpty());
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        boolean enabled = ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY);
        assertTrue(enabled);
    }

    /**
     * Checks that the action is not enabled unnecessarily after the problems are
     * collected.
     */
    public void testActionNotEnabledOnOKProject() throws Exception {
        setupOKProject();
        System.setProperty("test.reload.sync", "true");
        
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        // no problems at all
        assertTrue(collectProblems(p).isEmpty());
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        boolean enabled = ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY);
        assertFalse(enabled);
    }
    
    /**
     * Checks that the priming build does not actually run on OK project, although
     * the action may be temporarily enabled.
     */
    public void testPrimingBuildNotRunOnOK() throws Exception {
        MockMavenExec mme = new MockMavenExec();
        MockMavenExec.Reporter r = new MockMavenExec.Reporter();
        MockLookup.setLayersAndInstances(mme);
        CountDownLatch cdl = new CountDownLatch(1);
        MavenModelProblemsProvider.RP.submit(() -> {
            try {
                cdl.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        setupOKProject();
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        // no problems yet, and the resolution is blocked.
        Collection<? extends ProjectProblemsProvider.ProjectProblem> probs = collectProblems(p);
        assertEquals(0, probs.size());
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        boolean enabled = ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookups.fixed(r));
        
        // the actual detection is still broken.
        assertTrue(enabled);
        
        // release the problem analysis
        cdl.countDown();
        
        class Prog extends ActionProgress {
            CountDownLatch finish = new CountDownLatch(1);
            volatile boolean started;
            volatile boolean finished;
            
            @Override
            protected void started() {
                started = true;
            }

            @Override
            public void finished(boolean success) {
                finished = true;
                finish.countDown();
            }
        }
        
        Prog progress = new Prog();

        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress));
        progress.finish.await();
        
        assertTrue(progress.started);
        assertTrue(progress.finished);
        
        // but the execution was NOT really done
        assertFalse(r.executed);
        
        // check that the action is now disabled.
        assertFalse(ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY));
    }
    
    /**
     * Checks that broken build will really execute maven, but block the execution,
     * using mock service in Lookup.
     */
    public void testPrimingBuildExecutes() throws Exception {
        MockMavenExec mme = new MockMavenExec();
        MockLookup.setLayersAndInstances(mme);

        System.setProperty("test.reload.sync", "true");

        MockMavenExec.Reporter r = new MockMavenExec.Reporter();
        setupBrokenProject();
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        Collection<? extends ProjectProblemsProvider.ProjectProblem> probs = collectProblems(p);
        // #1 - parent POM is missing, #2 - dependencies are missing.
        assertEquals(2, probs.size());
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        boolean enabled = ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookups.fixed(r));
        assertTrue(enabled);
        class Prog extends ActionProgress {
            CountDownLatch finish = new CountDownLatch(1);
            volatile boolean started;
            volatile boolean finished;
            
            @Override
            protected void started() {
                started = true;
            }

            @Override
            public void finished(boolean success) {
                finished = true;
                finish.countDown();
            }
        }
        
        Prog progress = new Prog();

        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress, r));
        progress.finish.await();

        // but the execution was NOT really done
        assertTrue(r.executed);
    }
}
