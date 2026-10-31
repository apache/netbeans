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
package org.netbeans.modules.acp.api;

import java.util.List;
import java.util.Objects;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a tool call made by an ACP agent.
 *
 * @since 1.0
 */
public final class AcpToolCall {

    public static final String STATUS_PENDING = "pending"; // NOI18N
    public static final String STATUS_IN_PROGRESS = "in_progress"; // NOI18N
    public static final String STATUS_COMPLETED = "completed"; // NOI18N
    public static final String STATUS_FAILED = "failed"; // NOI18N

    private final String toolCallId;
    private final String title;
    private final String name;
    private final String kind;
    private final String status;
    private final List<DiffContent> diffs;
    private final List<Location> locations;
    private final String textContent;

    private AcpToolCall(String toolCallId, String title, String name, String kind,
            String status, List<DiffContent> diffs, List<Location> locations,
            String textContent) {
        this.toolCallId = toolCallId;
        this.title = title;
        this.name = name;
        this.kind = kind;
        this.status = status;
        this.diffs = diffs;
        this.locations = locations;
        this.textContent = textContent;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getStatus() {
        return status;
    }

    public List<DiffContent> getDiffs() {
        return diffs;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public String getTextContent() {
        return textContent;
    }

    @Override
    public String toString() {
        return "AcpToolCall{" + "id=" + toolCallId + ", title=" + title // NOI18N
                + ", kind=" + kind + ", status=" + status + '}'; // NOI18N
    }

    /**
     * A diff produced by a tool call.
     */
    public record DiffContent(String path, String oldText, String newText) {
    }

    /**
     * A file location referenced by a tool call.
     */
    public record Location(String path, Integer line) {
    }

    public static final class Builder {

        private final String toolCallId;
        private String title = ""; // NOI18N
        private String name;
        private String kind = "other"; // NOI18N
        private String status = STATUS_PENDING;
        private List<DiffContent> diffs = List.of();
        private List<Location> locations = List.of();
        private String textContent;

        public Builder(String toolCallId) {
            this.toolCallId = Objects.requireNonNull(toolCallId, "toolCallId must not be null");
        }

        public Builder title(String title) {
            this.title = Objects.requireNonNull(title, "title must not be null");
            return this;
        }

        public Builder name(@NullAllowed String name) {
            this.name = name;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = Objects.requireNonNull(kind, "kind must not be null");
            return this;
        }

        public Builder status(String status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
            return this;
        }

        public Builder diffs(List<DiffContent> diffs) {
            this.diffs = List.copyOf(Objects.requireNonNull(diffs));
            return this;
        }

        public Builder locations(List<Location> locations) {
            this.locations = List.copyOf(Objects.requireNonNull(locations));
            return this;
        }

        public Builder textContent(@NullAllowed String textContent) {
            this.textContent = textContent;
            return this;
        }

        public AcpToolCall build() {
            return new AcpToolCall(toolCallId, title, name, kind, status,
                    diffs, locations, textContent);
        }
    }
}
