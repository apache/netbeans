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

import java.util.Objects;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a tool call made by an ACP agent. Tool calls describe actions
 * the agent performs such as reading files, editing code, or executing commands.
 * <p>
 * Instances are immutable and created using the {@link Builder}.
 *
 * @since 1.0
 */
public final class AcpToolCall {

    /** Status value indicating the tool call has not yet started. */
    public static final String STATUS_PENDING = "pending"; // NOI18N

    /** Status value indicating the tool call is currently executing. */
    public static final String STATUS_IN_PROGRESS = "in_progress"; // NOI18N

    /** Status value indicating the tool call completed successfully. */
    public static final String STATUS_COMPLETED = "completed"; // NOI18N

    /** Status value indicating the tool call failed. */
    public static final String STATUS_FAILED = "failed"; // NOI18N

    private final String toolCallId;
    private final String title;
    private final String name;
    private final String kind;
    private final String status;

    private AcpToolCall(String toolCallId, String title, String name, String kind, String status) {
        this.toolCallId = toolCallId;
        this.title = title;
        this.name = name;
        this.kind = kind;
        this.status = status;
    }

    /**
     * Returns the unique identifier of this tool call.
     *
     * @return the tool call identifier, never {@code null}
     */
    public String getToolCallId() {
        return toolCallId;
    }

    /**
     * Returns the human-readable title of this tool call.
     *
     * @return the title, never {@code null}
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the programmatic name of the tool, if available.
     *
     * @return the tool name, or {@code null} if not specified
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the kind of operation this tool call represents.
     * Typical values include: read, edit, delete, execute, search, think, fetch, other.
     *
     * @return the kind, never {@code null}
     */
    public String getKind() {
        return kind;
    }

    /**
     * Returns the current execution status of this tool call.
     *
     * @return the status, never {@code null}
     * @see #STATUS_PENDING
     * @see #STATUS_IN_PROGRESS
     * @see #STATUS_COMPLETED
     * @see #STATUS_FAILED
     */
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AcpToolCall{" + "id=" + toolCallId + ", title=" + title + ", kind=" + kind + ", status=" + status + '}'; // NOI18N
    }

    /**
     * Builder for creating {@link AcpToolCall} instances.
     */
    public static final class Builder {

        private final String toolCallId;
        private String title = ""; // NOI18N
        private String name;
        private String kind = "other"; // NOI18N
        private String status = STATUS_PENDING;

        /**
         * Creates a new builder with the required tool call identifier.
         *
         * @param toolCallId the unique identifier of the tool call
         */
        public Builder(String toolCallId) {
            this.toolCallId = Objects.requireNonNull(toolCallId, "toolCallId must not be null");
        }

        /**
         * Sets the human-readable title.
         *
         * @param title the title
         * @return this builder
         */
        public Builder title(String title) {
            this.title = Objects.requireNonNull(title, "title must not be null");
            return this;
        }

        /**
         * Sets the programmatic tool name.
         *
         * @param name the tool name, may be {@code null}
         * @return this builder
         */
        public Builder name(@NullAllowed String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the kind of operation.
         *
         * @param kind the kind (e.g. read, edit, execute)
         * @return this builder
         */
        public Builder kind(String kind) {
            this.kind = Objects.requireNonNull(kind, "kind must not be null");
            return this;
        }

        /**
         * Sets the execution status.
         *
         * @param status the status
         * @return this builder
         */
        public Builder status(String status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
            return this;
        }

        /**
         * Builds an immutable {@link AcpToolCall} from the current state.
         *
         * @return the new tool call instance
         */
        public AcpToolCall build() {
            return new AcpToolCall(toolCallId, title, name, kind, status);
        }
    }
}
