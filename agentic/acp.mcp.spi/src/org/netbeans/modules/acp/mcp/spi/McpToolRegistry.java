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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 * Registry of the MCP tools contributed by the {@link McpToolProvider}
 * implementations registered in the default lookup.
 *
 * <p>Providers are discovered dynamically, so any module registering an
 * {@code McpToolProvider} service contributes its tools — including modules
 * enabled after the IDE has started. The cached view of the tools is
 * invalidated whenever the set of providers changes, and listeners added
 * through {@link #addChangeListener} are notified so they can refresh.
 */
public final class McpToolRegistry {

    private static final Logger LOG = Logger.getLogger(McpToolRegistry.class.getName());
    private static final McpToolRegistry INSTANCE = new McpToolRegistry();

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final Lookup.Result<McpToolProvider> providers;
    /**
     * Kept in a field so the listener is not garbage collected: the registry
     * is a singleton living for the whole lifetime of the IDE.
     */
    private final LookupListener providerListener = evt -> providersChanged();

    private final Map<String, McpToolProvider> toolOwners = new LinkedHashMap<>();
    private List<McpToolProvider.McpToolDefinition> cachedTools;
    private List<McpToolProvider.McpToolDefinition> cachedAllTools;

    private McpToolRegistry() {
        providers = Lookup.getDefault().lookupResult(McpToolProvider.class);
        providers.addLookupListener(providerListener);
    }

    public static McpToolRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * The tools of the providers that are enabled by default.
     */
    public synchronized List<McpToolProvider.McpToolDefinition> getAllTools() {
        if (cachedTools == null) {
            List<McpToolProvider.McpToolDefinition> enabled = new ArrayList<>();
            for (McpToolProvider.McpToolDefinition tool : loadTools()) {
                McpToolProvider owner = toolOwners.get(tool.name());
                if (owner != null && owner.isEnabledByDefault()) {
                    enabled.add(tool);
                }
            }
            cachedTools = Collections.unmodifiableList(enabled);
        }
        return cachedTools;
    }

    /**
     * Every registered tool, whether or not its provider is enabled by default.
     */
    public synchronized List<McpToolProvider.McpToolDefinition> getAllAvailableTools() {
        return loadTools();
    }

    /**
     * Drops the cached tools so the next query re-queries the providers.
     * Called automatically when the set of providers changes.
     */
    public void refresh() {
        synchronized (this) {
            cachedTools = null;
            cachedAllTools = null;
            toolOwners.clear();
        }
        changeSupport.fireChange();
    }

    /**
     * Adds a listener notified whenever the set of available tools changes,
     * typically because a module contributing tools was enabled or disabled.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        McpToolProvider provider;
        synchronized (this) {
            loadTools();
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
            loadTools();
            provider = toolOwners.get(toolName);
        }
        return provider != null && provider.isEnabledByDefault();
    }

    /**
     * The tools grouped under a display label derived from each provider,
     * for presenting the available tools to the user.
     */
    public Map<String, List<McpToolProvider.McpToolDefinition>> getToolsByProvider() {
        Map<String, List<McpToolProvider.McpToolDefinition>> result = new LinkedHashMap<>();
        for (McpToolProvider provider : providers.allInstances()) {
            List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
            if (!tools.isEmpty()) {
                result.computeIfAbsent(providerLabel(provider), label -> new ArrayList<>()).addAll(tools);
            }
        }
        result.replaceAll((label, tools) -> Collections.unmodifiableList(tools));
        return result;
    }

    private void providersChanged() {
        LOG.log(Level.FINE, "MCP tool providers changed, invalidating the registry");
        refresh();
    }

    /**
     * Loads every tool of every registered provider. The caller must hold the
     * registry lock.
     */
    private List<McpToolProvider.McpToolDefinition> loadTools() {
        assert Thread.holdsLock(this);
        if (cachedAllTools == null) {
            toolOwners.clear();
            List<McpToolProvider.McpToolDefinition> all = new ArrayList<>();
            for (McpToolProvider provider : providers.allInstances()) {
                int added = 0;
                for (McpToolProvider.McpToolDefinition tool : provider.getTools()) {
                    McpToolProvider previous = toolOwners.putIfAbsent(tool.name(), provider);
                    if (previous != null) {
                        LOG.log(Level.WARNING, "Ignoring duplicate MCP tool {0} from {1}, already provided by {2}",
                                new Object[]{tool.name(), provider.getClass().getName(), previous.getClass().getName()});
                        continue;
                    }
                    all.add(tool);
                    added++;
                }
                LOG.log(Level.FINE, "Loaded {0} tools from {1}",
                        new Object[]{added, provider.getClass().getSimpleName()});
            }
            LOG.log(Level.INFO, "Loaded {0} MCP tools from {1} providers",
                    new Object[]{all.size(), providers.allInstances().size()});
            cachedAllTools = Collections.unmodifiableList(all);
        }
        return cachedAllTools;
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
}
