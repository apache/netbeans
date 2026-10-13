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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Describes a configured ACP agent, including its display name, transport
 * mechanism, and connection parameters. Instances are immutable and created
 * using the {@link Builder}.
 *
 * @since 1.0
 */
public final class AcpAgentDescriptor {

    /**
     * Transport types supported by ACP agents.
     */
    public enum TransportType {
        /** Standard I/O transport: the agent runs as a child process. */
        STDIO,
        /** WebSocket transport: the agent is reached via a WebSocket URL. */
        WEBSOCKET
    }

    private final String name;
    private final TransportType transportType;
    private final List<String> command;
    private final String url;
    private final String workingDirectory;
    private final Map<String, String> environmentVariables;
    private final Set<String> enabledTools;

    private AcpAgentDescriptor(String name, TransportType transportType,
            List<String> command, String url, String workingDirectory,
            Map<String, String> environmentVariables, Set<String> enabledTools) {
        this.name = name;
        this.transportType = transportType;
        this.command = command;
        this.url = url;
        this.workingDirectory = workingDirectory;
        this.environmentVariables = environmentVariables;
        this.enabledTools = enabledTools;
    }

    /**
     * Returns the display name of this agent.
     *
     * @return the agent name, never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the transport type used to connect to this agent.
     *
     * @return the transport type, never {@code null}
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Returns the command and arguments used to launch the agent process.
     * Only meaningful for {@link TransportType#STDIO} transport.
     *
     * @return an unmodifiable list of command segments, never {@code null}
     */
    public List<String> getCommand() {
        return command;
    }

    /**
     * Returns the WebSocket URL of the agent.
     * Only meaningful for {@link TransportType#WEBSOCKET} transport.
     *
     * @return the URL, or {@code null} if not set
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the working directory for the agent process.
     * Only meaningful for {@link TransportType#STDIO} transport.
     *
     * @return the working directory path, or {@code null} if not set
     */
    @CheckForNull
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns the environment variables to set for the agent process.
     * Only meaningful for {@link TransportType#STDIO} transport.
     *
     * @return an unmodifiable map of environment variable names to values, never {@code null}
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Returns the set of MCP tool names enabled for this agent.
     * {@code null} means use the default tool set (tools where
     * the provider's {@code isEnabledByDefault()} returns {@code true}).
     *
     * @return an unmodifiable set of tool names, or {@code null} for defaults
     */
    @CheckForNull
    public Set<String> getEnabledTools() {
        return enabledTools;
    }

    @Override
    public String toString() {
        return "AcpAgentDescriptor{" + "name=" + name + ", transport=" + transportType + '}'; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AcpAgentDescriptor other)) {
            return false;
        }
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Builder for creating {@link AcpAgentDescriptor} instances.
     */
    public static final class Builder {

        private final String name;
        private final TransportType transportType;
        private List<String> command = List.of();
        private String url;
        private String workingDirectory;
        private Map<String, String> environmentVariables = Map.of();
        private Set<String> enabledTools;

        /**
         * Creates a new builder with the required name and transport type.
         *
         * @param name          the display name for the agent
         * @param transportType the transport type to use
         */
        public Builder(String name, TransportType transportType) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.transportType = Objects.requireNonNull(transportType, "transportType must not be null");
        }

        /**
         * Sets the command and arguments for STDIO transport.
         *
         * @param command the command segments
         * @return this builder
         */
        public Builder command(List<String> command) {
            this.command = List.copyOf(Objects.requireNonNull(command, "command must not be null"));
            return this;
        }

        /**
         * Sets the WebSocket URL for WEBSOCKET transport.
         *
         * @param url the WebSocket URL
         * @return this builder
         */
        public Builder url(@NullAllowed String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets the working directory for STDIO transport.
         *
         * @param workingDirectory the working directory path, may be {@code null}
         * @return this builder
         */
        public Builder workingDirectory(@NullAllowed String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        /**
         * Sets the environment variables for STDIO transport.
         *
         * @param environmentVariables the environment variable map
         * @return this builder
         */
        public Builder environmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = Collections.unmodifiableMap(
                    new LinkedHashMap<>(Objects.requireNonNull(environmentVariables, "environmentVariables must not be null")));
            return this;
        }

        /**
         * Sets the MCP tools enabled for this agent. Pass {@code null} to
         * use the default tool set.
         *
         * @param enabledTools the set of tool names, or {@code null}
         * @return this builder
         */
        public Builder enabledTools(@NullAllowed Set<String> enabledTools) {
            this.enabledTools = enabledTools == null ? null
                    : Collections.unmodifiableSet(new LinkedHashSet<>(enabledTools));
            return this;
        }

        /**
         * Builds an immutable {@link AcpAgentDescriptor} from the current state.
         *
         * @return the new descriptor instance
         * @throws IllegalStateException if required fields for the selected
         *         transport type are missing
         */
        public AcpAgentDescriptor build() {
            if (transportType == TransportType.STDIO && command.isEmpty()) {
                throw new IllegalStateException("STDIO transport requires a non-empty command");
            }
            if (transportType == TransportType.WEBSOCKET && (url == null || url.isBlank())) {
                throw new IllegalStateException("WEBSOCKET transport requires a URL");
            }
            return new AcpAgentDescriptor(name, transportType, command, url, workingDirectory, environmentVariables, enabledTools);
        }
    }
}
