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

/**
 * Listener for session update events from an ACP agent. Implementations
 * receive streaming notifications as the agent processes a prompt, including
 * message chunks, tool calls, plan updates, and usage information.
 * <p>
 * All methods have default empty implementations so that listeners can
 * override only the events they are interested in.
 *
 * @since 1.0
 */
public interface AcpSessionListener {

    /**
     * Called when the agent sends a message text chunk. Multiple chunks may
     * arrive for a single message; concatenate them to form the complete text.
     *
     * @param sessionId the session identifier
     * @param text      the message text chunk
     */
    default void onAgentMessage(String sessionId, String text) {
    }

    /**
     * Called when the agent sends a thought text chunk. Thoughts represent
     * the agent's internal reasoning.
     *
     * @param sessionId the session identifier
     * @param text      the thought text chunk
     */
    default void onAgentThought(String sessionId, String text) {
    }

    /**
     * Called when the agent initiates a new tool call.
     *
     * @param sessionId the session identifier
     * @param toolCall  the tool call details
     */
    default void onToolCall(String sessionId, AcpToolCall toolCall) {
    }

    /**
     * Called when a previously reported tool call changes status (e.g. from
     * pending to completed or failed).
     *
     * @param sessionId the session identifier
     * @param toolCall  the updated tool call details
     */
    default void onToolCallUpdate(String sessionId, AcpToolCall toolCall) {
    }

    /**
     * Called when the agent updates its execution plan. The list contains
     * all plan entries in order, replacing any previously reported plan.
     *
     * @param sessionId the session identifier
     * @param entries   the current plan entries
     */
    default void onPlanUpdate(String sessionId, List<AcpPlanEntry> entries) {
    }

    /**
     * Called when the prompt turn has finished. The stop reason indicates
     * why the agent stopped (e.g. "end_turn", "max_tokens").
     *
     * @param sessionId  the session identifier
     * @param stopReason the reason the prompt turn finished
     */
    default void onPromptComplete(String sessionId, String stopReason) {
    }

    /**
     * Called when the agent reports token usage information.
     *
     * @param sessionId   the session identifier
     * @param usedTokens  the number of tokens consumed so far
     * @param totalTokens the total token budget available
     */
    default void onUsageUpdate(String sessionId, long usedTokens, long totalTokens) {
    }

    /**
     * Called when the agent writes a text file through the ACP file handler.
     * The old and new content are provided so that listeners can display
     * or review the changes.
     *
     * @param sessionId  the session identifier
     * @param path       the file path that was written
     * @param oldContent the previous file content, or {@code null} if the file
     *                   did not exist
     * @param newContent the new file content written by the agent
     */
    default void onFileWritten(String sessionId, String path,
            String oldContent, String newContent) {
    }
}
