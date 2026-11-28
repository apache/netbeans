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
package org.netbeans.modules.acp.mcp.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;

public class GitToolProviderTest extends NbTestCase {

    private GitToolProvider provider;

    public GitToolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new GitToolProvider();
    }

    public void testIsDisabledByDefault() {
        assertFalse(provider.isEnabledByDefault());
    }

    public void testToolCount() {
        List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
        assertEquals(4, tools.size());
    }

    public void testToolNames() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names.contains("git_status"));
        assertTrue(names.contains("git_diff"));
        assertTrue(names.contains("git_log"));
        assertTrue(names.contains("file_history"));
    }

    public void testToolDefinitionsHaveDescriptions() {
        for (McpToolProvider.McpToolDefinition tool : provider.getTools()) {
            assertNotNull("Tool " + tool.name() + " should have a description", tool.description());
            assertFalse("Tool " + tool.name() + " description should not be empty",
                    tool.description().isEmpty());
        }
    }

    public void testToolDefinitionsHaveSchemas() {
        for (McpToolProvider.McpToolDefinition tool : provider.getTools()) {
            Map<String, Object> schema = tool.inputSchema();
            assertNotNull("Tool " + tool.name() + " should have a schema", schema);
            assertEquals("object", schema.get("type"));
            assertNotNull("Tool " + tool.name() + " should have properties", schema.get("properties"));
        }
    }

    public void testFileHistoryRequiresPath() {
        McpToolProvider.McpToolDefinition fileHistory = provider.getTools().stream()
                .filter(t -> "file_history".equals(t.name()))
                .findFirst()
                .orElseThrow();
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) fileHistory.inputSchema().get("required");
        assertNotNull(required);
        assertTrue(required.contains("path"));
    }

    public void testUnknownToolThrows() {
        try {
            provider.executeTool("nonexistent", Map.of());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("nonexistent"));
        } catch (Exception ex) {
            fail("Expected IllegalArgumentException, got " + ex.getClass().getSimpleName());
        }
    }

    public void testGitStatusNoRepo() throws Exception {
        Map<String, Object> result = provider.executeTool("git_status",
                Map.of("repoDir", "/nonexistent/repo/path"));
        assertEquals("error", result.get("status"));
    }

    public void testGitDiffInvalidRevision() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("repoDir", null);
        args.put("target", "HEAD; rm -rf /");
        Map<String, Object> result = provider.executeTool("git_diff", args);
        assertEquals("error", result.get("status"));
    }

    public void testGitDiffReportsInvalidRevisions() throws Exception {
        clearWorkDir();
        Path repo = getWorkDir().toPath();
        runGit(repo, "init");
        runGit(repo, "-c", "commit.gpgsign=false", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                "commit", "--allow-empty", "-m", "Initial commit");
        Project project = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return FileUtil.toFileObject(repo.toFile());
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        OpenProjects projects = OpenProjects.getDefault();
        projects.open(new Project[]{project}, false);
        try {
            for (String target : List.of("--ext-diff", "-Ofile")) {
                Map<String, Object> result = provider.executeTool("git_diff",
                        Map.of("repoDir", repo.toString(), "target", target));
                assertEquals("error", result.get("status"));
                assertEquals("Invalid revision: " + target, result.get("message"));
            }
            Files.writeString(repo.resolve("new-file.txt"), "test");
            Map<String, Object> valid = provider.executeTool("git_diff",
                    Map.of("repoDir", repo.toString(), "target", "HEAD"));
            assertEquals("ok", valid.get("status"));
            assertEquals(0, ((Number) valid.get("exitCode")).intValue());
            Map<String, Object> unknown = provider.executeTool("git_diff",
                    Map.of("repoDir", repo.toString(), "target", "missing-revision"));
            assertEquals("error", unknown.get("status"));
            assertTrue(((Number) unknown.get("exitCode")).intValue() != 0);
            assertFalse(((String) unknown.get("output")).isBlank());
        } finally {
            projects.close(new Project[]{project});
        }
    }

    private void runGit(Path directory, String... arguments) throws Exception {
        List<String> command = new java.util.ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(output, 0, process.waitFor());
    }

    public void testGitLogInvalidSince() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("repoDir", null);
        args.put("since", "$(evil)");
        Map<String, Object> result = provider.executeTool("git_log", args);
        assertEquals("error", result.get("status"));
    }

    public void testFileHistoryNullPath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("repoDir", null);
        args.put("path", null);
        Map<String, Object> result = provider.executeTool("file_history", args);
        assertEquals("error", result.get("status"));
    }

    public void testFileHistoryBlankPath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("repoDir", null);
        args.put("path", "  ");
        Map<String, Object> result = provider.executeTool("file_history", args);
        assertEquals("error", result.get("status"));
    }
}
