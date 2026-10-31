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

/**
 * Represents a single entry in an agent's plan. Each entry describes a step
 * the agent intends to take and its current execution status.
 *
 * @since 1.0
 */
public record AcpPlanEntry(String content, String status) {

    /**
     * Status value indicating the step has not yet started.
     */
    public static final String STATUS_PENDING = "pending"; // NOI18N

    /**
     * Status value indicating the step is currently being executed.
     */
    public static final String STATUS_IN_PROGRESS = "in_progress"; // NOI18N

    /**
     * Status value indicating the step has completed successfully.
     */
    public static final String STATUS_COMPLETED = "completed"; // NOI18N

    /**
     * Creates a new plan entry.
     *
     * @param content the description of the step, must not be {@code null}
     * @param status  the current status of the step, must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public AcpPlanEntry {
        java.util.Objects.requireNonNull(content, "content must not be null");
        java.util.Objects.requireNonNull(status, "status must not be null");
    }
}
