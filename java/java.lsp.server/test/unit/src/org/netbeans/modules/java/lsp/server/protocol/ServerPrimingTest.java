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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class ServerPrimingTest extends NbTestCase {

    private static final String DISABLE_PRIMING = "disableProjectPriming";

    private String originalDisableSetting;

    public ServerPrimingTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        originalDisableSetting = System.getProperty(DISABLE_PRIMING);
    }

    @Override
    protected void tearDown() throws Exception {
        if (originalDisableSetting == null) {
            System.clearProperty(DISABLE_PRIMING);
        } else {
            System.setProperty(DISABLE_PRIMING, originalDisableSetting);
        }
        super.tearDown();
    }

    public void testPrimeProjectsSkipsPrimeActionWhenDisabled() throws Exception {
        System.setProperty(DISABLE_PRIMING, Boolean.TRUE.toString());

        Server.LanguageServerImpl server = new Server.LanguageServerImpl(null);
        RecordingActionProvider actionProvider = new RecordingActionProvider();
        Project project = createProject(actionProvider);
        Map<Project, CompletableFuture<Void>> local = new HashMap<>();

        CompletableFuture<Void>[] primingBuilds = server.primeProjects(Collections.singleton(project), 1, local);

        assertEquals("No priming builds should be scheduled.", 0, primingBuilds.length);
        assertEquals("Prime action must not be invoked when disabled.", 0, actionProvider.invocationCount);
        assertNotNull("Project should still be tracked as being opened.", local.get(project));
    }

    public void testPrimeProjectsReturnsPendingFutureForAlreadyTrackedProjectWhenDisabled() throws Exception {
        System.setProperty(DISABLE_PRIMING, Boolean.TRUE.toString());

        Server.LanguageServerImpl server = new Server.LanguageServerImpl(null);
        RecordingActionProvider actionProvider = new RecordingActionProvider();
        Project project = createProject(actionProvider);

        Map<Project, CompletableFuture<Void>> firstOpen = new HashMap<>();
        server.primeProjects(Collections.singleton(project), 1, firstOpen);
        CompletableFuture<Void> tracked = firstOpen.get(project);

        Map<Project, CompletableFuture<Void>> secondOpen = new HashMap<>();
        CompletableFuture<Void>[] primingBuilds = server.primeProjects(Collections.singleton(project), 2, secondOpen);

        assertNotNull("Project should be tracked after first open request.", tracked);
        assertEquals("Existing tracked future should be returned.", 1, primingBuilds.length);
        assertSame("Returned future should be the already tracked one.", tracked, primingBuilds[0]);
        assertTrue("No new tracking entry is expected for an already tracked project.", secondOpen.isEmpty());
        assertEquals("Prime action must not be invoked when disabled.", 0, actionProvider.invocationCount);
    }

    private Project createProject(ActionProvider actionProvider) throws IOException {
        File projectDir = new File(getWorkDir(), "p" + System.nanoTime());
        assertTrue(projectDir.mkdirs());
        FileObject projectDirectory = FileUtil.toFileObject(FileUtil.normalizeFile(projectDir));
        assertNotNull(projectDirectory);
        Lookup lookup = Lookups.fixed(actionProvider);
        return new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return projectDirectory;
            }

            @Override
            public Lookup getLookup() {
                return lookup;
            }
        };
    }

    private static final class RecordingActionProvider implements ActionProvider {
        int invocationCount;

        @Override
        public String[] getSupportedActions() {
            return new String[] { COMMAND_PRIME };
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            if (!COMMAND_PRIME.equals(command)) {
                throw new IllegalArgumentException(command);
            }
            invocationCount++;
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            return COMMAND_PRIME.equals(command);
        }
    }
}
