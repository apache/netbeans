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
package org.netbeans.modules.acp.mcp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.MockLookup;

public class McpToolRegistryTest extends NbTestCase {

    public McpToolRegistryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDisabledProviderIsSkipped() {
        MockLookup.setInstances(new DisabledProvider(), new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        List<McpToolProvider.McpToolDefinition> tools = McpToolRegistry.getInstance().getAllTools();

        Set<String> names = tools.stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue("Enabled tool should be present", names.contains("enabled_tool"));
        assertFalse("Disabled tool should not be present", names.contains("disabled_tool"));
    }

    public void testEnabledProviderIsIncluded() {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        List<McpToolProvider.McpToolDefinition> tools = McpToolRegistry.getInstance().getAllTools();

        Set<String> names = tools.stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue("enabled_tool should be present", names.contains("enabled_tool"));
    }

    public void testExecuteToolDelegatesToProvider() throws Exception {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        McpToolRegistry.getInstance().getAllTools();

        Map<String, Object> result = McpToolRegistry.getInstance().executeTool("enabled_tool", Map.of());
        assertEquals("ok", result.get("status"));
        assertEquals("executed", result.get("result"));
    }

    public void testExecuteUnknownToolThrows() {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        McpToolRegistry.getInstance().getAllTools();

        try {
            McpToolRegistry.getInstance().executeTool("no_such_tool", Map.of());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("no_such_tool"));
        } catch (Exception ex) {
            fail("Expected IllegalArgumentException, got " + ex.getClass().getSimpleName());
        }
    }

    public void testRefreshClearsCache() {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        List<McpToolProvider.McpToolDefinition> tools1 = McpToolRegistry.getInstance().getAllTools();
        Set<String> names1 = tools1.stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names1.contains("enabled_tool"));

        MockLookup.setInstances();
        McpToolRegistry.getInstance().refresh();
        List<McpToolProvider.McpToolDefinition> tools2 = McpToolRegistry.getInstance().getAllTools();
        Set<String> names2 = tools2.stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertFalse("enabled_tool should be gone after refresh", names2.contains("enabled_tool"));
    }

    public void testDisabledByDefaultFiltersGitAndFileSystem() {
        McpToolRegistry.getInstance().refresh();
        List<McpToolProvider.McpToolDefinition> tools = McpToolRegistry.getInstance().getAllTools();
        Set<String> names = tools.stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertFalse("git_status should not appear (disabled)", names.contains("git_status"));
        assertFalse("read_file should not appear (disabled)", names.contains("read_file"));
    }

    public static class EnabledProvider implements McpToolProvider {
        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("enabled_tool", "An enabled tool",
                    Map.of("type", "object", "properties", Map.of())));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> arguments) {
            return ToolResult.ok("result", "executed");
        }
    }

    public static class DisabledProvider implements McpToolProvider {
        @Override
        public boolean isEnabledByDefault() {
            return false;
        }

        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("disabled_tool", "A disabled tool",
                    Map.of("type", "object", "properties", Map.of())));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> arguments) {
            return ToolResult.ok("result", "should not run");
        }
    }
}
