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

/**
 *
 * @author Petr Hejl
 */
public class PortMapping {

    private final ExposedPort.Type type;

    private final Integer port;

    private final Integer hostPort;

    private final String hostAddress;

    public PortMapping(ExposedPort.Type type, Integer port, Integer hostPort, String hostAddress) {
        this.type = type;
        this.port = port;
        this.hostPort = hostPort;
        this.hostAddress = hostAddress;
    }

    public ExposedPort.Type getType() {
        return type;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public String getHostAddress() {
        return hostAddress;
    }

}
