/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.docker.api;

import java.util.Objects;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public class ExposedPort {

    public enum Type {
        TCP,
        UDP
    }

    private final int port;

    private final Type type;

    public ExposedPort(int port, Type type) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port number must be between 1 and 65535");
        }
        Parameters.notNull("type", type);

        this.port = port;
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.port;
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExposedPort other = (ExposedPort) obj;
        if (this.port != other.port) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
