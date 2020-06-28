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
package org.netbeans.modules.docker.api;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainerDetail {

    private final String name;

    private final DockerContainer.Status status;

    private final boolean stdin;

    private final boolean tty;
    
    private final List<PortMapping> portMappings;

    public DockerContainerDetail(String name, DockerContainer.Status status, boolean stdin, boolean tty) {
        this.name = name;
        this.status = status;
        this.stdin = stdin;
        this.tty = tty;
        this.portMappings = Collections.emptyList();
    }
    
    public DockerContainerDetail(String name, DockerContainer.Status status, boolean stdin, boolean tty, List<PortMapping> portMappings) {
        this.name = name;
        this.status = status;
        this.stdin = stdin;
        this.tty = tty;
        this.portMappings = portMappings;
    }

    public String getName() {
        return name;
    }

    public DockerContainer.Status getStatus() {
        return status;
    }

    public boolean isStdin() {
        return stdin;
    }

    public boolean isTty() {
        return tty;
    }
    
    public List<PortMapping> portMappings() {
        return Collections.unmodifiableList(portMappings);
    }
    
    public boolean arePortExposed() {
        return portMappings.isEmpty();
    }
}
