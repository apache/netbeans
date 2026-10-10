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

import com.agentclientprotocol.sdk.client.AcpClient;
import com.agentclientprotocol.sdk.client.AcpSyncClient;
import com.agentclientprotocol.sdk.client.transport.AgentParameters;
import com.agentclientprotocol.sdk.client.transport.StdioAcpClientTransport;
import com.agentclientprotocol.sdk.client.transport.WebSocketAcpClientTransport;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.AgentMessageChunk;
import com.agentclientprotocol.sdk.spec.AcpSchema.AgentThoughtChunk;
import com.agentclientprotocol.sdk.spec.AcpSchema.ContentBlock;
import com.agentclientprotocol.sdk.spec.AcpSchema.Plan;
import com.agentclientprotocol.sdk.spec.AcpSchema.SessionNotification;
import com.agentclientprotocol.sdk.spec.AcpSchema.SessionUpdate;
import com.agentclientprotocol.sdk.spec.AcpSchema.TextContent;
import com.agentclientprotocol.sdk.spec.AcpSchema.ToolCallUpdateNotification;
import com.agentclientprotocol.sdk.spec.AcpSchema.UsageUpdate;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AcpConnection {

    private static final Logger LOG = Logger.getLogger(AcpConnection.class.getName());

    private final AcpAgentDescriptor descriptor;
    private final List<AcpSessionListener> listeners = new CopyOnWriteArrayList<>();

    private volatile AcpSyncClient client;
    private volatile boolean connected;

    public AcpConnection(AcpAgentDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null"); // NOI18N
    }

    public AcpAgentDescriptor getDescriptor() {
        return descriptor;
    }

    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Already connected to agent: " + descriptor.getName()); // NOI18N
        }

        try {
            AcpClient.SyncSpec spec;

            switch (descriptor.getTransportType()) {
                case STDIO -> {
                    List<String> cmd = descriptor.getCommand();
                    AgentParameters.Builder paramsBuilder = AgentParameters.builder(cmd.get(0));
                    if (cmd.size() > 1) {
                        paramsBuilder.args(cmd.subList(1, cmd.size()));
                    }
                    Map<String, String> envVars = descriptor.getEnvironmentVariables();
                    if (!envVars.isEmpty()) {
                        paramsBuilder.env(envVars);
                    }
                    if (descriptor.getWorkingDirectory() != null && !descriptor.getWorkingDirectory().isBlank()) {
                        paramsBuilder.workingDirectory(Path.of(descriptor.getWorkingDirectory()));
                    }
                    StdioAcpClientTransport transport = new StdioAcpClientTransport(paramsBuilder.build());
                    spec = AcpClient.sync(transport);
                }
                case WEBSOCKET -> {
                    WebSocketAcpClientTransport transport = new WebSocketAcpClientTransport(URI.create(descriptor.getUrl()));
                    spec = AcpClient.sync(transport);
                }
                default -> throw new IllegalStateException("Unknown transport type: " + descriptor.getTransportType()); // NOI18N
            }

            spec.sessionUpdateHandler(this::handleSessionUpdate);
            spec.readTextFileHandler(this::handleReadTextFile);
            spec.clientInfo(new AcpSchema.Implementation("Apache NetBeans ACP Plugin", "1.0")); // NOI18N

            client = spec.build();
            client.initialize();
            connected = true;

            LOG.log(Level.INFO, "Connected to ACP agent: {0}", descriptor.getName()); // NOI18N
        } catch (Exception ex) {
            close();
            throw new IOException("Failed to connect to agent: " + descriptor.getName(), ex); // NOI18N
        }
    }

    public String newSession(String workingDirectory) throws IOException {
        ensureConnected();
        try {
            String effectiveDir = workingDirectory;
            if (effectiveDir == null || effectiveDir.isBlank()) {
                effectiveDir = descriptor.getWorkingDirectory();
            }
            AcpSchema.NewSessionRequest request = new AcpSchema.NewSessionRequest(effectiveDir);
            return client.newSession(request).sessionId();
        } catch (Exception ex) {
            throw new IOException("Failed to create new session", ex); // NOI18N
        }
    }

    public void sendPrompt(String sessionId, String text) throws IOException {
        ensureConnected();
        try {
            AcpSchema.PromptRequest request = AcpSchema.PromptRequest.text(sessionId, text);
            AcpSchema.PromptResponse response = client.prompt(request);
            String stopReason = response.stopReason() != null ? response.stopReason().value() : "end_turn"; // NOI18N
            for (AcpSessionListener l : listeners) {
                try {
                    l.onPromptComplete(sessionId, stopReason);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onPromptComplete", ex); // NOI18N
                }
            }
        } catch (Exception ex) {
            throw new IOException("Failed to send prompt", ex); // NOI18N
        }
    }

    public void cancel(String sessionId) throws IOException {
        ensureConnected();
        try {
            client.cancel(new AcpSchema.CancelNotification(sessionId));
        } catch (Exception ex) {
            throw new IOException("Failed to cancel prompt", ex); // NOI18N
        }
    }

    public void close() {
        connected = false;
        if (client != null) {
            try {
                client.close();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error closing ACP client for: " + descriptor.getName(), ex); // NOI18N
            }
            client = null;
        }
        LOG.log(Level.INFO, "Disconnected from ACP agent: {0}", descriptor.getName()); // NOI18N
    }

    public boolean isConnected() {
        return connected;
    }

    public void addSessionListener(AcpSessionListener listener) {
        Objects.requireNonNull(listener, "listener must not be null"); // NOI18N
        listeners.add(listener);
    }

    public void removeSessionListener(AcpSessionListener listener) {
        listeners.remove(listener);
    }

    private void ensureConnected() {
        if (!connected) {
            throw new IllegalStateException("Not connected to agent: " + descriptor.getName()); // NOI18N
        }
    }

    private void handleSessionUpdate(SessionNotification notification) {
        String sessionId = notification.sessionId();
        SessionUpdate update = notification.update();

        if (update instanceof AgentMessageChunk chunk) {
            String text = extractText(chunk.content());
            for (AcpSessionListener l : listeners) {
                try {
                    l.onAgentMessage(sessionId, text);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onAgentMessage", ex); // NOI18N
                }
            }
        } else if (update instanceof AgentThoughtChunk chunk) {
            String text = extractText(chunk.content());
            for (AcpSessionListener l : listeners) {
                try {
                    l.onAgentThought(sessionId, text);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onAgentThought", ex); // NOI18N
                }
            }
        } else if (update instanceof AcpSchema.ToolCall tc) {
            AcpToolCall toolCall = new AcpToolCall.Builder(tc.toolCallId())
                    .title(tc.title() != null ? tc.title() : "") // NOI18N
                    .name(tc.name())
                    .kind(tc.kind() != null ? tc.kind().value() : "other") // NOI18N
                    .status(tc.status() != null ? tc.status().value() : AcpToolCall.STATUS_PENDING)
                    .build();
            for (AcpSessionListener l : listeners) {
                try {
                    l.onToolCall(sessionId, toolCall);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onToolCall", ex); // NOI18N
                }
            }
        } else if (update instanceof ToolCallUpdateNotification tcu) {
            AcpToolCall toolCall = new AcpToolCall.Builder(tcu.toolCallId())
                    .title(tcu.title() != null ? tcu.title() : "") // NOI18N
                    .name(tcu.name())
                    .kind(tcu.kind() != null ? tcu.kind().value() : "other") // NOI18N
                    .status(tcu.status() != null ? tcu.status().value() : AcpToolCall.STATUS_IN_PROGRESS)
                    .build();
            for (AcpSessionListener l : listeners) {
                try {
                    l.onToolCallUpdate(sessionId, toolCall);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onToolCallUpdate", ex); // NOI18N
                }
            }
        } else if (update instanceof Plan plan) {
            List<AcpPlanEntry> entries = new ArrayList<>();
            for (AcpSchema.PlanEntry pe : plan.entries()) {
                String status = pe.status() != null ? pe.status().value() : AcpPlanEntry.STATUS_PENDING;
                entries.add(new AcpPlanEntry(pe.content(), status));
            }
            List<AcpPlanEntry> unmodifiable = List.copyOf(entries);
            for (AcpSessionListener l : listeners) {
                try {
                    l.onPlanUpdate(sessionId, unmodifiable);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onPlanUpdate", ex); // NOI18N
                }
            }
        } else if (update instanceof UsageUpdate usage) {
            long used = usage.used() != null ? usage.used() : 0;
            long size = usage.size() != null ? usage.size() : 0;
            for (AcpSessionListener l : listeners) {
                try {
                    l.onUsageUpdate(sessionId, used, size);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error in session listener onUsageUpdate", ex); // NOI18N
                }
            }
        } else {
            LOG.log(Level.FINE, "Unhandled session update type: {0}", // NOI18N
                    update != null ? update.getClass().getName() : "null"); // NOI18N
        }
    }

    private static String extractText(ContentBlock content) {
        if (content instanceof TextContent tc) {
            return tc.text() != null ? tc.text() : ""; // NOI18N
        }
        return ""; // NOI18N
    }

    private AcpSchema.ReadTextFileResponse handleReadTextFile(AcpSchema.ReadTextFileRequest request) {
        try {
            String content = Files.readString(Path.of(request.path()));
            return new AcpSchema.ReadTextFileResponse(content);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to read file requested by agent: " + request.path(), ex); // NOI18N
            return new AcpSchema.ReadTextFileResponse(""); // NOI18N
        }
    }
}
