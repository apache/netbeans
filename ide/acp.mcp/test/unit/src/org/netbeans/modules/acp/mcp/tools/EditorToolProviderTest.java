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

public class EditorToolProviderTest extends NbTestCase {

    private EditorToolProvider provider;

    public EditorToolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new EditorToolProvider();
    }

    public void testIsEnabledByDefault() {
        assertTrue(provider.isEnabledByDefault());
    }

    public void testToolCount() {
        List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
        assertEquals(3, tools.size());
    }

    public void testToolNames() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names.contains("get_current_file_context"));
        assertTrue(names.contains("get_tabs"));
        assertTrue(names.contains("open_pos"));
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
        }
    }

    public void testOpenPosRequiredParams() {
        McpToolProvider.McpToolDefinition openPos = provider.getTools().stream()
                .filter(t -> "open_pos".equals(t.name()))
                .findFirst()
                .orElseThrow();
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) openPos.inputSchema().get("required");
        assertNotNull(required);
        assertTrue(required.contains("filePath"));
        assertTrue(required.contains("line"));
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

    public void testOpenPosNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("line", 1);
        Map<String, Object> result = provider.executeTool("open_pos", args);
        assertEquals("error", result.get("status"));
    }

    public void testOpenPosBlankFilePath() throws Exception {
        Map<String, Object> result = provider.executeTool("open_pos",
                Map.of("filePath", "  ", "line", 1));
        assertEquals("error", result.get("status"));
    }

    public void testOpenPosNullLine() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/File.java");
        args.put("line", null);
        Map<String, Object> result = provider.executeTool("open_pos", args);
        assertEquals("error", result.get("status"));
    }
}
