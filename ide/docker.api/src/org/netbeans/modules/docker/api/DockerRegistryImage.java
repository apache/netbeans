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

/**
 *
 * @author Petr Hejl
 */
public class DockerRegistryImage {

    private final String name;

    private final String description;

    private final long stars;

    private final boolean official;

    private final boolean automated;

    public DockerRegistryImage(String name, String description, long stars, boolean official, boolean automated) {
        this.name = name;
        this.description = description;
        this.stars = stars;
        this.official = official;
        this.automated = automated;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getStars() {
        return stars;
    }

    public boolean isOfficial() {
        return official;
    }

    public boolean isAutomated() {
        return automated;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
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
        final DockerRegistryImage other = (DockerRegistryImage) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HubImageInfo{" + "name=" + name + '}';
    }
}
