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
 * Summary of a past agent session, as returned by the agent's
 * {@code session/list} capability.
 *
 * @param sessionId the session identifier
 * @param title     human-readable session title (may be {@code null})
 * @param updatedAt ISO-8601 timestamp of the last update (may be {@code null})
 */
public record AcpSessionInfo(String sessionId, String title, String updatedAt) {
}
