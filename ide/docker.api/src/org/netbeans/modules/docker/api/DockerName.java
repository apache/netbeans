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

import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public class DockerName {

    private final String registry;

    private final String namespace;

    private final String repository;

    private final String tag;

    private DockerName(String registry, String namespace, String repository, String tag) {
        this.registry = registry;
        this.namespace = namespace;
        this.repository = repository;
        this.tag = tag;
    }

    public static DockerName parse(String name) {
        Parameters.notNull("repository", name);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name can't be empty");
        }

        String[] parts = name.split("/", 3); // NOI18N

        String registry = null;
        String ns = null;
        String repo = null;
        String tag = null;
        if (parts.length == 1) {
            repo = parts[0];
        } else if (parts.length == 2) {
            String namespace = parts[0];
            // registry host
            if (namespace.contains(".") || namespace.contains(":") || "localhost".equals(namespace)) { // NOI18N
                registry = namespace;
            } else {
                ns = namespace;
            }
            repo = parts[1];
        } else if (parts.length == 3) {
            registry = parts[0];
            ns = parts[1];
            repo = parts[2];
        }

        int index = repo.indexOf(':');
        if (index > 0) {
            tag = repo.substring(index + 1);
            repo = repo.substring(0, index);
        }

        return new DockerName(registry, ns, repo, tag);
    }

    public String getRegistry() {
        return registry;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }
}
