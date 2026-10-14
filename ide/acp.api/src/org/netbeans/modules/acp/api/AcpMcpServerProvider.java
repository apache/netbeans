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
package org.netbeans.modules.acp.api;

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.util.List;

/**
 * SPI for providing MCP servers to ACP sessions. Implementations
 * registered in the default Lookup are automatically discovered
 * when creating new ACP sessions.
 */
public interface AcpMcpServerProvider {

    List<AcpSchema.McpServer> getMcpServers();

    /**
     * Revokes a bearer token that was previously issued by this provider.
     * Called when the ACP connection that used the token closes, so that the
     * token does not accumulate indefinitely in memory.
     * <p>
     * The default implementation is a no-op; providers that issue tokens (e.g.
     * the MCP HTTP server) must override this to perform the actual revocation.
     *
     * @param bearerToken the raw token value (without the {@code Bearer } prefix)
     */
    default void releaseBearerToken(String bearerToken) {
    }

    /**
     * Returns a human-readable summary of the tools provided by this
     * MCP server. Each entry is a pair of [name, description].
     * Used by the UI to display available tools after session creation.
     */
    default List<ToolInfo> getAvailableTools() {
        return List.of();
    }

    record ToolInfo(String name, String description) {
    }
}
