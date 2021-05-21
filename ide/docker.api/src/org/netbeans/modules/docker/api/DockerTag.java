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
public final class DockerTag implements DockerInstanceEntity {

    private final DockerImage image;

    private final String tag;
    
    DockerTag(DockerImage image, String tag) {
        this.image = image;
        this.tag = tag;
    }

    @Override
    public String getId() {
        return image.getId();
    }

    @Override
    public String getShortId() {
        return image.getShortId();
    }

    public DockerImage getImage() {
        return image;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.image);
        hash = 67 * hash + Objects.hashCode(this.tag);
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
        final DockerTag other = (DockerTag) obj;
        if (!Objects.equals(this.tag, other.tag)) {
            return false;
        }
        if (!Objects.equals(this.image, other.image)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DockerTag{" + "image=" + image + ", tag=" + tag + '}';
    }

    @Override
    public DockerInstance getInstance() {
       return image.getInstance();
    }

    @Override
    public DockerEntityType getType() {
        return DockerEntityType.Image;
    }
}
