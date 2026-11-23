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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class RefactoringToolProviderTest extends NbTestCase {

    private RefactoringToolProvider provider;

    public RefactoringToolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new RefactoringToolProvider();
    }

    public void testIsDisabledByDefault() {
        assertFalse(provider.isEnabledByDefault());
    }

    public void testToolCount() {
        List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
        assertEquals(5, tools.size());
    }

    public void testToolNames() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names.contains("rename_file"));
        assertTrue(names.contains("move_refactor"));
        assertTrue(names.contains("safe_delete_file"));
        assertTrue(names.contains("find_file_usages"));
        assertTrue(names.contains("copy_class"));
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

    public void testRenameRequiredParams() {
        McpToolProvider.McpToolDefinition rename = findTool("rename_file");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) rename.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
        assertTrue(required.contains("newName"));
    }

    public void testMoveRefactorRequiredParams() {
        McpToolProvider.McpToolDefinition move = findTool("move_refactor");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) move.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
        assertTrue(required.contains("targetDirectory"));
    }

    public void testSafeDeleteRequiredParams() {
        McpToolProvider.McpToolDefinition safeDelete = findTool("safe_delete_file");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) safeDelete.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
    }

    public void testFindUsagesRequiredParams() {
        McpToolProvider.McpToolDefinition findUsages = findTool("find_file_usages");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) findUsages.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
    }

    public void testCopyClassRequiredParams() {
        McpToolProvider.McpToolDefinition copyClass = findTool("copy_class");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) copyClass.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
        assertTrue(required.contains("newName"));
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

    public void testRenameNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("newName", "NewName");
        Map<String, Object> result = provider.executeTool("rename_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testRenameBlankFilePath() throws Exception {
        Map<String, Object> result = provider.executeTool("rename_file",
                Map.of("filePath", "  ", "newName", "NewName"));
        assertEquals("error", result.get("status"));
    }

    public void testRenameNullNewName() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/File.java");
        args.put("newName", null);
        Map<String, Object> result = provider.executeTool("rename_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testRenameBlankNewName() throws Exception {
        Map<String, Object> result = provider.executeTool("rename_file",
                Map.of("filePath", "/some/File.java", "newName", ""));
        assertEquals("error", result.get("status"));
    }

    public void testMoveNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("targetDirectory", "/some/dir");
        Map<String, Object> result = provider.executeTool("move_refactor", args);
        assertEquals("error", result.get("status"));
    }

    public void testMoveNullTargetDir() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/File.java");
        args.put("targetDirectory", null);
        Map<String, Object> result = provider.executeTool("move_refactor", args);
        assertEquals("error", result.get("status"));
    }

    public void testSafeDeleteDoesNotCommitWhenPluginReportsReferences() throws Exception {
        clearWorkDir();
        Path folder = getWorkDir().toPath();
        Path target = Files.writeString(folder.resolve("Target.java"), "class Target {}");
        Files.writeString(folder.resolve("Caller.java"), "class Caller { Target target; }");
        AtomicBoolean committed = new AtomicBoolean();
        RefactoringPluginFactory pluginFactory = refactoring -> {
            if (!(refactoring instanceof SafeDeleteRefactoring)) {
                return null;
            }
            return new RefactoringPlugin() {
                @Override
                public Problem preCheck() {
                    return null;
                }

                @Override
                public Problem checkParameters() {
                    return null;
                }

                @Override
                public Problem fastCheckParameters() {
                    return null;
                }

                @Override
                public void cancelRequest() {
                }

                @Override
                public Problem prepare(RefactoringElementsBag elements) {
                    elements.registerTransaction(new Transaction() {
                        @Override
                        public void commit() {
                            committed.set(true);
                        }

                        @Override
                        public void rollback() {
                        }
                    });
                    return new Problem(false, "Caller.java references Target");
                }
            };
        };
        MockLookup.setInstances(pluginFactory);
        Project project = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return FileUtil.toFileObject(folder.toFile());
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        Project[] projects = {project};
        OpenProjects openProjects = OpenProjects.getDefault();
        try {
            openProjects.open(projects, false);
            var result = provider.executeTool("safe_delete_file", Map.of("filePath", target.toString()));
            assertEquals(Boolean.FALSE, result.get("deleted"));
            assertEquals(List.of("Caller.java references Target"), result.get("warnings"));
            assertFalse(committed.get());
            assertTrue(Files.exists(target));
        } finally {
            openProjects.close(projects);
            MockLookup.setInstances();
        }
    }

    public void testSafeDeleteNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        Map<String, Object> result = provider.executeTool("safe_delete_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testFindUsagesNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        Map<String, Object> result = provider.executeTool("find_file_usages", args);
        assertEquals("error", result.get("status"));
    }

    public void testCopyNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("newName", "Copy");
        Map<String, Object> result = provider.executeTool("copy_class", args);
        assertEquals("error", result.get("status"));
    }

    public void testCopyNullNewName() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/File.java");
        args.put("newName", null);
        Map<String, Object> result = provider.executeTool("copy_class", args);
        assertEquals("error", result.get("status"));
    }

    private McpToolProvider.McpToolDefinition findTool(String name) {
        return provider.getTools().stream()
                .filter(t -> name.equals(t.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tool not found: " + name));
    }
}
