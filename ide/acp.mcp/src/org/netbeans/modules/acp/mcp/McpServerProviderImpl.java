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

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.acp.api.AcpMcpServerProvider;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.McpToolRegistry;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AcpMcpServerProvider.class)
public final class McpServerProviderImpl implements AcpMcpServerProvider {

    private static final Logger LOG = Logger.getLogger(McpServerProviderImpl.class.getName());

    @Override
    public List<AcpSchema.McpServer> getMcpServers() {
        McpServerManager manager = McpServerManager.getInstance();
        AcpSchema.McpServer server = manager.createSession();
        if (server == null) {
            LOG.log(Level.WARNING, "MCP server could not be started");
            return List.of();
        }
        LOG.log(Level.INFO, "Providing MCP server at {0}", manager.getUrl());
        return List.of(server);
    }

    @Override
    public void releaseBearerToken(String bearerToken) {
        McpServerManager.getInstance().removeSession(bearerToken);
    }

    @Override
    public List<ToolInfo> getAvailableTools() {
        LOG.log(Level.INFO, "getAvailableTools called");
        McpToolRegistry registry = McpToolRegistry.getInstance();
        List<McpToolProvider.McpToolDefinition> tools = registry.getAllTools();
        LOG.log(Level.INFO, "getAvailableTools: registry returned {0} tools", tools.size());
        List<ToolInfo> result = new ArrayList<>(tools.size());
        for (McpToolProvider.McpToolDefinition tool : tools) {
            result.add(new ToolInfo(tool.name(), tool.description()));
        }
        return result;
    }
}
