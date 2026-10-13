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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

public final class McpToolRegistry {

    private static final Logger LOG = Logger.getLogger(McpToolRegistry.class.getName());
    private static final McpToolRegistry INSTANCE = new McpToolRegistry();

    private final Map<String, McpToolProvider> toolOwners = new LinkedHashMap<>();
    private List<McpToolProvider.McpToolDefinition> cachedTools;
    private List<McpToolProvider.McpToolDefinition> cachedAllTools;

    private McpToolRegistry() {
    }

    public static McpToolRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized List<McpToolProvider.McpToolDefinition> getAllTools() {
        if (cachedTools == null) {
            cachedTools = loadTools();
        }
        return cachedTools;
    }

    public synchronized List<McpToolProvider.McpToolDefinition> getAllAvailableTools() {
        if (cachedAllTools == null) {
            cachedAllTools = loadAllTools();
        }
        return cachedAllTools;
    }

    public synchronized void refresh() {
        cachedTools = null;
        cachedAllTools = null;
        toolOwners.clear();
    }

    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        McpToolProvider provider;
        synchronized (this) {
            if (cachedTools == null) {
                cachedTools = loadTools();
            }
            provider = toolOwners.get(name);
        }
        if (provider == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        LOG.log(Level.INFO, "Executing MCP tool: {0}", name);
        return provider.executeTool(name, arguments);
    }

    public boolean isToolEnabledByDefault(String toolName) {
        McpToolProvider provider;
        synchronized (this) {
            if (cachedAllTools == null) {
                cachedAllTools = loadAllTools();
            }
            provider = toolOwners.get(toolName);
        }
        return provider != null && provider.isEnabledByDefault();
    }

    private List<McpToolProvider.McpToolDefinition> loadTools() {
        Collection<? extends McpToolProvider> providers = Lookup.getDefault().lookupAll(McpToolProvider.class);
        List<McpToolProvider.McpToolDefinition> allTools = new ArrayList<>();
        for (McpToolProvider provider : providers) {
            if (!provider.isEnabledByDefault()) {
                LOG.log(Level.FINE, "Skipping disabled provider: {0}", provider.getClass().getSimpleName());
                continue;
            }
            List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
            for (McpToolProvider.McpToolDefinition tool : tools) {
                toolOwners.put(tool.name(), provider);
                allTools.add(tool);
            }
            LOG.log(Level.INFO, "Loaded {0} tools from {1}", new Object[]{tools.size(), provider.getClass().getSimpleName()});
        }
        return Collections.unmodifiableList(allTools);
    }

    public Map<String, List<McpToolProvider.McpToolDefinition>> getToolsByProvider() {
        Collection<? extends McpToolProvider> providers = Lookup.getDefault().lookupAll(McpToolProvider.class);
        Map<String, List<McpToolProvider.McpToolDefinition>> result = new LinkedHashMap<>();
        for (McpToolProvider provider : providers) {
            List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
            if (!tools.isEmpty()) {
                String label = providerLabel(provider);
                synchronized (this) {
                    for (McpToolProvider.McpToolDefinition tool : tools) {
                        toolOwners.put(tool.name(), provider);
                    }
                }
                result.put(label, Collections.unmodifiableList(tools));
            }
        }
        return result;
    }

    private static String providerLabel(McpToolProvider provider) {
        String name = provider.getClass().getSimpleName();
        if (name.endsWith("ToolProvider")) {
            name = name.substring(0, name.length() - "ToolProvider".length());
        } else if (name.endsWith("Provider")) {
            name = name.substring(0, name.length() - "Provider".length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private List<McpToolProvider.McpToolDefinition> loadAllTools() {
        Collection<? extends McpToolProvider> providers = Lookup.getDefault().lookupAll(McpToolProvider.class);
        List<McpToolProvider.McpToolDefinition> allTools = new ArrayList<>();
        for (McpToolProvider provider : providers) {
            List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
            for (McpToolProvider.McpToolDefinition tool : tools) {
                toolOwners.put(tool.name(), provider);
                allTools.add(tool);
            }
        }
        return Collections.unmodifiableList(allTools);
    }
}
