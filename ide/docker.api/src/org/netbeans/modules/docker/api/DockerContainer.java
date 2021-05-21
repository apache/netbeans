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

import java.util.Objects;

/**
 *
 * @author Petr Hejl
 */
public final class DockerContainer implements DockerInstanceEntity {

    @Override
    public DockerEntityType getType() {
        return DockerEntityType.Container;
    }

    public enum Status {
        RUNNING,
        PAUSED,
        STOPPED
    }

    private final DockerInstance instance;

    private final String id;

    private final String image;

    private final String name;

    private Status status;

    DockerContainer(DockerInstance instance, String id, String image, String name, Status status) {
        this.instance = instance;
        this.id = id;
        this.image = image;
        this.name = name;
        this.status = status;
    }

    @Override
    public DockerInstance getInstance() {
        return instance;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getShortId() {
        return org.netbeans.modules.docker.DockerUtils.getShortId(this);
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.instance);
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final DockerContainer other = (DockerContainer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DockerContainer{" + "instance=" + instance + ", id=" + id + ", image=" + image + '}';
    }

}
