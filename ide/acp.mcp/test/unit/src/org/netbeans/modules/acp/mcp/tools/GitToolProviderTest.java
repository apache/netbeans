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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.acp.mcp.McpToolProvider;

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
