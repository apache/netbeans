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
package org.netbeans.modules.javaee.wildfly.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.SocketBinding;

/**
 * A container for {@link SocketBindings}.
 */
public class SocketContainer {

    private final String name;
    private final String defaultInterface;
    private final int portOffset;
    private final Set<SocketBinding> socketBindings = new HashSet<>();

    /**
     * Creates a new container from the given configuration values.
     *
     * @param name The container name.
     * @param defaultInterface The default interface.
     * @param portOffset The port offset.
     * @param socketBindings The {@link SocketBinding}s.
     */
    public SocketContainer(String name, String defaultInterface, int portOffset, Set<SocketBinding> socketBindings) {
        this.name = name;
        this.defaultInterface = defaultInterface;
        this.portOffset = portOffset;
        this.socketBindings.addAll(socketBindings);
    }

    public String getName() {
        return name;
    }

    public String getDefaultInterface() {
        return defaultInterface;
    }

    public int getPortOffset() {
        return portOffset;
    }

    /**
     * Retrieve all available {@link SocketBinding}s.
     *
     * @return The set of socket bindings.
     */
    public Set<SocketBinding> getSocketBindings() {
        return Collections.unmodifiableSet(socketBindings);
    }

    /**
     * Retrieve an {@link Optional} holding the {@link SocketBinding} with the given name.
     *
     * @param name The name of the socket binding.
     * @return The optional.
     */
    public Optional<SocketBinding> getSocketByName(String name) {
        return socketBindings.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst();

    }
}
