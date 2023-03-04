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

import java.util.EventListener;
import java.util.EventObject;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 *
 * @author Petr Hejl
 */
public final class StatusEvent extends EventObject {

    private final DockerInstance instance;

    private final String id;

    private final String message;

    private final String progress;

    private final Progress detail;

    private final boolean error;

    StatusEvent(DockerInstance instance, String id, String message,
            String progress, boolean error, Progress detail) {
        super(instance);
        this.instance = instance;
        this.id = id;
        this.message = message;
        this.progress = progress;
        this.error = error;
        this.detail = detail;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getProgress() {
        return progress;
    }

    @CheckForNull
    public Progress getDetail() {
        return detail;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public DockerInstance getSource() {
        return instance;
    }

    @Override
    public String toString() {
        return "StatusEvent{" + "id=" + id + ", message=" + message + ", progress=" + progress + ", detail=" + detail + ", error=" + error + '}';
    }

    public static class Progress {

        private final long current;

        private final long total;

        public Progress(long current, long total) {
            this.current = current;
            this.total = total;
        }

        public long getCurrent() {
            return current;
        }

        public long getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return "Progress{" + "current=" + current + ", total=" + total + '}';
        }
    }

    public static interface Listener extends EventListener {

        void onEvent(StatusEvent event);

    }
}
