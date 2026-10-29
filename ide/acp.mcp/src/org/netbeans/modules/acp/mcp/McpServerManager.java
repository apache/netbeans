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
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.acp.mcp.spi.McpToolRegistry;

public final class McpServerManager {

    private static final Logger LOG = Logger.getLogger(McpServerManager.class.getName());
    private static final McpServerManager INSTANCE = new McpServerManager();

    private final McpToolRegistry registry;
    private final McpHttpServer httpServer;
    private final ThreadLocal<Set<String>> pendingEnabledTools = new ThreadLocal<>();

    private McpServerManager() {
        this.registry = McpToolRegistry.getInstance();
        this.httpServer = new McpHttpServer(registry);
    }

    public static McpServerManager getInstance() {
        return INSTANCE;
    }

    public synchronized void ensureRunning() {
        if (!httpServer.isRunning()) {
            try {
                httpServer.start();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to start MCP server", ex);
            }
        }
    }

    public void stop() {
        httpServer.stop();
    }

    public void setEnabledTools(Set<String> enabledTools) {
        pendingEnabledTools.set(enabledTools);
        LOG.log(Level.INFO, "MCP tools staged for next session: {0}",
                enabledTools != null ? enabledTools : "all");
    }

    public AcpSchema.McpServer createSession() {
        ensureRunning();
        if (!httpServer.isRunning()) {
            return null;
        }
        Set<String> tools = pendingEnabledTools.get();
        pendingEnabledTools.remove();
        String token = httpServer.createSessionToken(tools);
        List<AcpSchema.HttpHeader> headers = List.of(
                new AcpSchema.HttpHeader("Authorization", "Bearer " + token));
        return new AcpSchema.McpServerHttp(
                "netbeans-ide",
                httpServer.getUrl(),
                headers
        );
    }

    public void removeSession(String bearerToken) {
        httpServer.removeSessionToken(bearerToken);
    }

    public String getUrl() {
        return httpServer.getUrl();
    }

    public boolean isRunning() {
        return httpServer.isRunning();
    }

    public McpToolRegistry getRegistry() {
        return registry;
    }
}
