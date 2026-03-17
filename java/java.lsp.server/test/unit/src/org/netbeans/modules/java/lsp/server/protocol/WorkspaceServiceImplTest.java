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

import com.google.gson.JsonPrimitive;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.files.OpenedDocuments;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

public class WorkspaceServiceImplTest extends NbTestCase {
    private static final String MAVEN_PREFERENCES_NODE = "org/netbeans/modules/maven";
    private static final String MAVEN_USER_SETTINGS_XML = "userSettingsXml";
    private WorkspaceServiceImpl service;

    public WorkspaceServiceImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        NbPreferences.root().node(MAVEN_PREFERENCES_NODE).remove(MAVEN_USER_SETTINGS_XML);
        service = new WorkspaceServiceImpl(new TestLspServerState());
    }

    @Override
    protected void tearDown() throws Exception {
        NbPreferences.root().node(MAVEN_PREFERENCES_NODE).remove(MAVEN_USER_SETTINGS_XML);
        super.tearDown();
    }

    public void testUpdateMavenUserSettingsPreferencesStoresNormalizedPath() throws Exception {
        File customSettings = new File(getWorkDir(), "folder/../custom/settings.xml");

        service.updateMavenUserSettingsPreferences(new JsonPrimitive(customSettings.getAbsolutePath()));

        String stored = NbPreferences.root().node(MAVEN_PREFERENCES_NODE).get(MAVEN_USER_SETTINGS_XML, null);
        assertEquals(FileUtil.normalizeFile(customSettings).getAbsolutePath(), stored);
    }

    public void testUpdateMavenUserSettingsPreferencesClearsPreference() {
        NbPreferences.root().node(MAVEN_PREFERENCES_NODE).put(MAVEN_USER_SETTINGS_XML, "/tmp/custom-settings.xml");

        service.updateMavenUserSettingsPreferences(null);

        assertNull(NbPreferences.root().node(MAVEN_PREFERENCES_NODE).get(MAVEN_USER_SETTINGS_XML, null));
    }

    private static final class TestLspServerState implements LspServerState {
        @Override
        public CompletableFuture<Project[]> openedProjects() {
            return CompletableFuture.completedFuture(new Project[0]);
        }

        @Override
        public CompletableFuture<Project[]> asyncOpenSelectedProjects(List<FileObject> fileCandidates, boolean addWorkspace) {
            return CompletableFuture.completedFuture(new Project[0]);
        }

        @Override
        public CompletableFuture<Project> asyncOpenFileOwner(FileObject file) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return null;
        }

        @Override
        public OpenedDocuments getOpenedDocuments() {
            return null;
        }

        @Override
        public List<FileObject> getAcceptedWorkspaceFolders() {
            return Collections.emptyList();
        }

        @Override
        public List<FileObject> getClientWorkspaceFolders() {
            return Collections.emptyList();
        }
    }
}
