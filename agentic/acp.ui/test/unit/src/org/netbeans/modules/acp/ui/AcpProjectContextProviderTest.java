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
package org.netbeans.modules.acp.ui;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider.ProjectContext;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class AcpProjectContextProviderTest extends TestCase {

    public void testRecognizesBuildAndListsOnlyPresentProjectFiles() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot().createFolder("project");
        root.createData("build.gradle.kts");
        root.createData("README.md");
        ProjectContext context = AcpProjectContextProvider.gatherProjectContext(project(root));
        assertEquals("project", context.name());
        assertEquals(root.getPath(), context.directory());
        assertEquals("Gradle", context.buildSystem());
        assertEquals(List.of("build.gradle.kts", "README.md"), context.keyFiles());
        String prompt = context.promptText();
        assertTrue(prompt, prompt.contains("**Build system**: Gradle"));
        assertTrue(prompt, prompt.contains("**Key files present**: `build.gradle.kts`, `README.md`"));
    }

    public void testOmitsUnknownBuildSystemAndHandlesNoProject() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot().createFolder("empty");
        ProjectContext context = AcpProjectContextProvider.gatherProjectContext(project(root));
        assertNull(context.buildSystem());
        assertTrue(context.keyFiles().isEmpty());
        String prompt = context.promptText();
        assertFalse(prompt, prompt.contains("**Build system**"));
        assertTrue(prompt, prompt.contains("**Key files present**: (none detected)"));
        assertNull(AcpProjectContextProvider.gatherProjectContext(null));
    }

    private static Project project(FileObject root) {
        return new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return root;
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
    }
}
