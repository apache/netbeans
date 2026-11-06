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
package org.netbeans.modules.acp.mcp.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
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

    public void testProviderRegisteredLaterIsDiscovered() {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        assertFalse("other_tool should not be there yet", toolNames().contains("other_tool"));

        MockLookup.setInstances(new EnabledProvider(), new OtherProvider());
        assertTrue("a provider registered later should be picked up without an explicit refresh",
                toolNames().contains("other_tool"));
    }

    public void testChangeListenerNotifiedWhenProvidersChange() {
        MockLookup.setInstances(new EnabledProvider());
        McpToolRegistry.getInstance().refresh();
        McpToolRegistry.getInstance().getAllTools();

        int[] changes = new int[1];
        ChangeListener listener = evt -> changes[0]++;
        McpToolRegistry.getInstance().addChangeListener(listener);
        try {
            MockLookup.setInstances(new EnabledProvider(), new OtherProvider());
            assertTrue("listener should have been notified", changes[0] > 0);
        } finally {
            McpToolRegistry.getInstance().removeChangeListener(listener);
        }
    }

    public void testDuplicateToolNameKeepsTheFirstProvider() throws Exception {
        MockLookup.setInstances(new EnabledProvider(), new DuplicateProvider());
        McpToolRegistry.getInstance().refresh();

        List<McpToolProvider.McpToolDefinition> tools = McpToolRegistry.getInstance().getAllAvailableTools();
        long occurrences = tools.stream()
                .filter(tool -> "enabled_tool".equals(tool.name()))
                .count();
        assertEquals("a tool name must be listed only once", 1, occurrences);
        assertEquals("executed", McpToolRegistry.getInstance()
                .executeTool("enabled_tool", Map.of()).get("result"));
    }

    private static Set<String> toolNames() {
        return McpToolRegistry.getInstance().getAllTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
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

    public static class OtherProvider implements McpToolProvider {
        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("other_tool", "Another tool",
                    Map.of("type", "object", "properties", Map.of())));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> arguments) {
            return ToolResult.ok("result", "other");
        }
    }

    /** Claims a tool name that {@link EnabledProvider} already provides. */
    public static class DuplicateProvider implements McpToolProvider {
        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("enabled_tool", "A clashing tool",
                    Map.of("type", "object", "properties", Map.of())));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> arguments) {
            return ToolResult.ok("result", "duplicate");
        }
    }
}
