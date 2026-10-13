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

public class FileSystemToolProviderTest extends NbTestCase {

    private FileSystemToolProvider provider;

    public FileSystemToolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new FileSystemToolProvider();
    }

    public void testIsDisabledByDefault() {
        assertFalse(provider.isEnabledByDefault());
    }

    public void testToolCount() {
        List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
        assertEquals(7, tools.size());
    }

    public void testToolNames() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names.contains("read_file"));
        assertTrue(names.contains("write_to_file"));
        assertTrue(names.contains("replace_lines"));
        assertTrue(names.contains("insert_in_file"));
        assertTrue(names.contains("delete_lines"));
        assertTrue(names.contains("search_project"));
        assertTrue(names.contains("list_directory"));
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

    public void testReadFileRequiresFilePath() {
        McpToolProvider.McpToolDefinition readFile = findTool("read_file");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) readFile.inputSchema().get("required");
        assertTrue(required.contains("filePath"));
    }

    public void testSearchProjectRequiresQuery() {
        McpToolProvider.McpToolDefinition search = findTool("search_project");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) search.inputSchema().get("required");
        assertTrue(required.contains("query"));
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

    public void testReadFileNullPath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        Map<String, Object> result = provider.executeTool("read_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testReadFileBlankPath() throws Exception {
        Map<String, Object> result = provider.executeTool("read_file", Map.of("filePath", "  "));
        assertEquals("error", result.get("status"));
    }

    public void testWriteToFileNullPath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("content", "hello");
        Map<String, Object> result = provider.executeTool("write_to_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testWriteToFileNullContent() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/path");
        args.put("content", null);
        Map<String, Object> result = provider.executeTool("write_to_file", args);
        assertEquals("error", result.get("status"));
    }

    public void testSearchProjectNullQuery() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("query", null);
        Map<String, Object> result = provider.executeTool("search_project", args);
        assertEquals("error", result.get("status"));
    }

    public void testSearchProjectBlankQuery() throws Exception {
        Map<String, Object> result = provider.executeTool("search_project", Map.of("query", "  "));
        assertEquals("error", result.get("status"));
    }

    public void testSearchProjectInvalidRegex() throws Exception {
        Map<String, Object> result = provider.executeTool("search_project", Map.of("query", "[invalid"));
        assertEquals("error", result.get("status"));
        assertTrue(((String) result.get("message")).contains("Invalid regex"));
    }

    public void testListDirectoryNullPath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("dirPath", null);
        Map<String, Object> result = provider.executeTool("list_directory", args);
        assertEquals("error", result.get("status"));
    }

    public void testListDirectoryBlankPath() throws Exception {
        Map<String, Object> result = provider.executeTool("list_directory", Map.of("dirPath", ""));
        assertEquals("error", result.get("status"));
    }

    public void testNoRunCommandTool() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertFalse("run_command should not exist", names.contains("run_command"));
    }

    private McpToolProvider.McpToolDefinition findTool(String name) {
        return provider.getTools().stream()
                .filter(t -> name.equals(t.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Tool not found: " + name));
    }

    private static class AssertionError extends RuntimeException {
        AssertionError(String msg) {
            super(msg);
        }
    }
}
