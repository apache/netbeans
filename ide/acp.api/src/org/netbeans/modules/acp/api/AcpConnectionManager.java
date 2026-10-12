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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Singleton service managing active connections to ACP agents. Provides
 * methods to connect to and disconnect from agents, and to query the
 * set of currently active connections.
 * <p>
 * Obtain the singleton instance via {@link #getDefault()}.
 *
 * @since 1.0
 */
@ServiceProvider(service = AcpConnectionManager.class)
public final class AcpConnectionManager {

    private static final Logger LOG = Logger.getLogger(AcpConnectionManager.class.getName());

    private final Map<String, AcpConnection> connections = new ConcurrentHashMap<>();

    /**
     * Returns the default instance of the connection manager.
     *
     * @return the connection manager singleton
     */
    public static AcpConnectionManager getDefault() {
        AcpConnectionManager mgr = Lookup.getDefault().lookup(AcpConnectionManager.class);
        if (mgr == null) {
            mgr = new AcpConnectionManager();
        }
        return mgr;
    }

    /**
     * Creates a new connection to the specified agent and establishes it.
     * If a connection for the same agent name already exists, the existing
     * connection is returned.
     *
     * @param descriptor the agent descriptor
     * @return the established connection
     * @throws IOException if the connection cannot be established
     */
    public AcpConnection connect(AcpAgentDescriptor descriptor) throws IOException {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        // compute() is atomic per key, so concurrent calls cannot create duplicates
        try {
            AcpConnection connection = connections.compute(descriptor.getName(), (name, existing) -> {
                if (existing != null && existing.isConnected()) {
                    return existing;
                }
                if (existing != null) {
                    existing.close();
                }
                AcpConnection created = new AcpConnection(descriptor);
                try {
                    created.connect();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                LOG.log(Level.INFO, "Agent connected: {0}", name);
                return created;
            });
            return connection;
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Disconnects from the agent described by the given descriptor. If no
     * active connection exists for the agent, this method does nothing.
     *
     * @param descriptor the agent descriptor
     */
    public void disconnect(AcpAgentDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        AcpConnection connection = connections.remove(descriptor.getName());
        if (connection != null) {
            connection.close();
            LOG.log(Level.INFO, "Agent disconnected: {0}", descriptor.getName());
        }
    }

    /**
     * Returns an unmodifiable collection of all currently active connections.
     *
     * @return active connections, never {@code null}
     */
    public Collection<AcpConnection> getActiveConnections() {
        return Collections.unmodifiableCollection(connections.values());
    }
}
