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

import java.util.EventListener;
import java.util.EventObject;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 *
 * @author Petr Hejl
 */
public final class BuildEvent extends EventObject {

    private final DockerInstance instance;

    private final String message;

    private final Error detail;

    private final boolean error;

    private final boolean upload;

    BuildEvent(DockerInstance instance, String message, boolean error, Error detail, boolean upload) {
        super(instance);
        this.instance = instance;
        this.message = message;
        this.detail = detail;
        this.error = error;
        this.upload = upload;
    }

    public String getMessage() {
        return message;
    }

    @CheckForNull
    public Error getDetail() {
        return detail;
    }

    public boolean isError() {
        return error;
    }

    public boolean isUpload() {
        return upload;
    }

    @Override
    public DockerInstance getSource() {
        return instance;
    }

    @Override
    public String toString() {
        return "BuildEvent{" + "instance=" + instance + ", message=" + message + ", detail=" + detail + ", error=" + error + ", upload=" + upload + '}';
    }

    public static class Error {

        private final long code;

        private final String message;

        public Error(long code, String message) {
            this.code = code;
            this.message = message;
        }

        public long getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Error{" + "code=" + code + ", message=" + message + '}';
        }
    }

    public static interface Listener extends EventListener {

        void onEvent(BuildEvent event);

    }
}
