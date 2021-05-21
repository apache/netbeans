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
package org.netbeans.modules.docker;

import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerEntity;
import org.netbeans.modules.docker.api.DockerTag;

/**
 *
 * @author Petr Hejl
 */
public final class DockerUtils {

    public static final String DOCKER_FILE = "Dockerfile"; // NOI18N

    private DockerUtils() {
        super();
    }

    public static String getShortId(DockerEntity identifiable) {
        String id = identifiable.getId();
        int index = id.indexOf(':'); // NOI18N
        if (index >= 0) {
            id = id.substring(index + 1);
        }
        if (id.length() > 12) {
            return id.substring(0, 12);
        }
        return id;
    }

    public static String getImage(DockerTag tag) {
        String id = tag.getTag();
        if (id.equals("<none>:<none>")) { // NOI18N
            id = tag.getImage().getId();
        }
        return id;
    }

    public static String getTag(String repository, String tag) {
        if (repository == null) {
            return "<none>:<none>";
        }
        if (tag == null) {
            return repository + ":latest";
        }
        return repository + ":" + tag;
    }

    public static DockerContainer.Status getContainerStatus(String status) {
        if (status == null) {
            return DockerContainer.Status.STOPPED;
        }
        if (!status.startsWith("Up")) { // NOI18N
            return DockerContainer.Status.STOPPED;
        }
        if (!status.contains("Paused")) { // NOI18N
            return DockerContainer.Status.RUNNING;
        }
        return DockerContainer.Status.PAUSED;
    }
}
