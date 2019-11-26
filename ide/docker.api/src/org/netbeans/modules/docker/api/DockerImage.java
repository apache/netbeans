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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public final class DockerImage implements DockerInstanceEntity {

    private final DockerInstance instance;

    private final List<DockerTag> tags = new ArrayList<>();

    private final String id;

    private final long created;

    private final long size;

    private final long virtualSize;

    DockerImage(DockerInstance instance, List<String> tags,
            String id, long created, long size, long virtualSize) {
        this.instance = instance;
        this.id = id;
        this.created = created;
        this.size = size;
        this.virtualSize = virtualSize;
        // See #268513
        // I'm not quite sure HOW to reproduce this situation, but it happens
        // if we have image named as "something:<none>".
        if (tags != null) {
            for (String tag : tags) {
                this.tags.add(new DockerTag(this, tag));
            }
        } else {
             Logger.getLogger(DockerImage.class.getName()).log(Level.INFO, "Null tags for {0}", id);
        }
    }

    @Override
    public DockerInstance getInstance() {
        return instance;
    }

    public List<DockerTag> getTags() {
        return tags;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getShortId() {
        return org.netbeans.modules.docker.DockerUtils.getShortId(this);
    }

    public long getCreated() {
        return created;
    }

    public long getSize() {
        return size;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.instance);
        hash = 79 * hash + Objects.hashCode(this.id);
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
        final DockerImage other = (DockerImage) obj;
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
        return "DockerImage{" + "instance=" + instance + ", id=" + id + '}';
    }

    @Override
    public DockerEntityType getType() {
        return DockerEntityType.Container;
    }

}
