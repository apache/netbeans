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
import com.agentclientprotocol.sdk.error.AcpCapabilityException;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AcpConnection {

    private static final Logger LOG = Logger.getLogger(AcpConnection.class.getName());

    private final AcpAgentDescriptor descriptor;
    private final List<AcpSessionListener> listeners = new CopyOnWriteArrayList<>();

    private final Map<String, List<Path>> trustedDirectories = new ConcurrentHashMap<>();

    private volatile AcpSyncClient client;
    private volatile boolean connected;
    private volatile java.util.function.Function<AcpSchema.RequestPermissionRequest,
            AcpSchema.RequestPermissionResponse> permissionHandler;

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
            spec.writeTextFileHandler(this::handleWriteTextFile);
            spec.requestPermissionHandler(this::handleRequestPermission);
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
        return newSession(workingDirectory, null);
    }

    public String newSession(String workingDirectory, List<String> additionalDirs) throws IOException {
        ensureConnected();
        try {
            String effectiveDir = workingDirectory;
            if (effectiveDir == null || effectiveDir.isBlank()) {
                effectiveDir = descriptor.getWorkingDirectory();
            }
            String sessionId;
            if (additionalDirs != null && !additionalDirs.isEmpty()) {
                try {
                    AcpSchema.NewSessionRequest request =
                            new AcpSchema.NewSessionRequest(effectiveDir, List.of(), additionalDirs);
                    sessionId = client.newSession(request).sessionId();
                } catch (AcpCapabilityException ex) {
                    LOG.log(Level.INFO, "Agent does not support additionalDirectories, " // NOI18N
                            + "falling back to single working directory"); // NOI18N
                    AcpSchema.NewSessionRequest request =
                            new AcpSchema.NewSessionRequest(effectiveDir);
                    sessionId = client.newSession(request).sessionId();
                }
            } else {
                AcpSchema.NewSessionRequest request =
                        new AcpSchema.NewSessionRequest(effectiveDir);
                sessionId = client.newSession(request).sessionId();
            }

            List<Path> trusted = new ArrayList<>();
            if (effectiveDir != null) {
                trusted.add(Path.of(effectiveDir).toAbsolutePath().normalize());
            }
            String descriptorDir = descriptor.getWorkingDirectory();
            if (descriptorDir != null && !descriptorDir.isBlank()) {
                Path dp = Path.of(descriptorDir).toAbsolutePath().normalize();
                if (!trusted.contains(dp)) {
                    trusted.add(dp);
                }
            }
            if (additionalDirs != null) {
                for (String dir : additionalDirs) {
                    Path dp = Path.of(dir).toAbsolutePath().normalize();
                    if (!trusted.contains(dp)) {
                        trusted.add(dp);
                    }
                }
            }
            trustedDirectories.put(sessionId, List.copyOf(trusted));
            return sessionId;
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
            notifyListeners(l -> l.onPromptComplete(sessionId, stopReason));
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

    private void notifyListeners(Consumer<AcpSessionListener> action) {
        for (AcpSessionListener l : listeners) {
            try {
                action.accept(l);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error in session listener", ex); // NOI18N
            }
        }
    }

    private void handleSessionUpdate(SessionNotification notification) {
        String sessionId = notification.sessionId();
        SessionUpdate update = notification.update();

        if (update instanceof AgentMessageChunk chunk) {
            String text = extractText(chunk.content());
            notifyListeners(l -> l.onAgentMessage(sessionId, text));
        } else if (update instanceof AgentThoughtChunk chunk) {
            String text = extractText(chunk.content());
            notifyListeners(l -> l.onAgentThought(sessionId, text));
        } else if (update instanceof AcpSchema.ToolCall tc) {
            AcpToolCall toolCall = buildToolCall(tc.toolCallId(), tc.title(), tc.name(),
                    tc.kind(), tc.status(), tc.content(), tc.locations());
            notifyListeners(l -> l.onToolCall(sessionId, toolCall));
        } else if (update instanceof ToolCallUpdateNotification tcu) {
            AcpToolCall toolCall = buildToolCall(tcu.toolCallId(), tcu.title(), tcu.name(),
                    tcu.kind(), tcu.status(), tcu.content(), tcu.locations());
            notifyListeners(l -> l.onToolCallUpdate(sessionId, toolCall));
        } else if (update instanceof Plan plan) {
            List<AcpPlanEntry> entries = new ArrayList<>();
            for (AcpSchema.PlanEntry pe : plan.entries()) {
                String status = pe.status() != null ? pe.status().value() : AcpPlanEntry.STATUS_PENDING;
                entries.add(new AcpPlanEntry(pe.content(), status));
            }
            List<AcpPlanEntry> unmodifiable = List.copyOf(entries);
            notifyListeners(l -> l.onPlanUpdate(sessionId, unmodifiable));
        } else if (update instanceof UsageUpdate usage) {
            long used = usage.used() != null ? usage.used() : 0;
            long size = usage.size() != null ? usage.size() : 0;
            notifyListeners(l -> l.onUsageUpdate(sessionId, used, size));
        } else {
            LOG.log(Level.FINE, "Unhandled session update type: {0}", // NOI18N
                    update != null ? update.getClass().getName() : "null"); // NOI18N
        }
    }

    private static AcpToolCall buildToolCall(String toolCallId, String title, String name,
            AcpSchema.ToolKind kind, AcpSchema.ToolCallStatus status,
            java.util.List<AcpSchema.ToolCallContent> content,
            java.util.List<AcpSchema.ToolCallLocation> locations) {

        AcpToolCall.Builder builder = new AcpToolCall.Builder(toolCallId)
                .title(title != null ? title : "") // NOI18N
                .name(name)
                .kind(kind != null ? kind.value() : "other") // NOI18N
                .status(status != null ? status.value() : AcpToolCall.STATUS_PENDING);

        if (content != null) {
            List<AcpToolCall.DiffContent> diffs = new ArrayList<>();
            StringBuilder textBuf = new StringBuilder();
            for (AcpSchema.ToolCallContent tcc : content) {
                if (tcc instanceof AcpSchema.ToolCallDiff diff) {
                    diffs.add(new AcpToolCall.DiffContent(
                            diff.path(), diff.oldText(), diff.newText()));
                } else if (tcc instanceof AcpSchema.ToolCallContentBlock block) {
                    if (block.content() instanceof TextContent tc && tc.text() != null) {
                        textBuf.append(tc.text());
                    }
                }
            }
            if (!diffs.isEmpty()) {
                builder.diffs(diffs);
            }
            if (textBuf.length() > 0) {
                builder.textContent(textBuf.toString());
            }
        }

        if (locations != null && !locations.isEmpty()) {
            List<AcpToolCall.Location> locs = new ArrayList<>();
            for (AcpSchema.ToolCallLocation loc : locations) {
                locs.add(new AcpToolCall.Location(loc.path(), loc.line()));
            }
            builder.locations(locs);
        }

        return builder.build();
    }

    private static String extractText(ContentBlock content) {
        if (content instanceof TextContent tc) {
            return tc.text() != null ? tc.text() : ""; // NOI18N
        }
        return ""; // NOI18N
    }

    private boolean isPathTrusted(String sessionId, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        List<Path> trusted = trustedDirectories.get(sessionId);
        if (trusted == null || trusted.isEmpty()) {
            LOG.log(Level.FINE, "No trusted directories for session {0}, allowing access", sessionId); // NOI18N
            return true;
        }
        for (Path dir : trusted) {
            if (normalized.startsWith(dir)) {
                return true;
            }
        }
        LOG.log(Level.WARNING, "Path {0} is outside trusted directories: {1}", // NOI18N
                new Object[]{normalized, trusted});
        return false;
    }

    private AcpSchema.ReadTextFileResponse handleReadTextFile(AcpSchema.ReadTextFileRequest request) {
        Path filePath = Path.of(request.path());
        if (!isPathTrusted(request.sessionId(), filePath)) {
            LOG.log(Level.WARNING, "Agent read blocked outside trusted directory: {0}", request.path()); // NOI18N
            return new AcpSchema.ReadTextFileResponse(""); // NOI18N
        }
        try {
            String content = Files.readString(filePath);
            return new AcpSchema.ReadTextFileResponse(content);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to read file requested by agent: " + request.path(), ex); // NOI18N
            return new AcpSchema.ReadTextFileResponse(""); // NOI18N
        }
    }

    private AcpSchema.WriteTextFileResponse handleWriteTextFile(AcpSchema.WriteTextFileRequest request) {
        Path filePath = Path.of(request.path());
        if (!isPathTrusted(request.sessionId(), filePath)) {
            LOG.log(Level.WARNING, "Agent write blocked outside trusted directory: {0}", request.path()); // NOI18N
            return new AcpSchema.WriteTextFileResponse();
        }
        String oldContent = null;
        try {
            if (Files.exists(filePath)) {
                oldContent = Files.readString(filePath);
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Could not read existing content for: " + request.path(), ex); // NOI18N
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, request.content());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to write file requested by agent: " + request.path(), ex); // NOI18N
            return new AcpSchema.WriteTextFileResponse();
        }
        String sessionId = request.sessionId();
        String writtenPath = request.path();
        String prevContent = oldContent;
        String newContent = request.content();
        for (AcpSessionListener l : listeners) {
            try {
                l.onFileWritten(sessionId, writtenPath, prevContent, newContent);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Error in session listener onFileWritten", ex); // NOI18N
            }
        }
        return new AcpSchema.WriteTextFileResponse();
    }

    private AcpSchema.RequestPermissionResponse handleRequestPermission(
            AcpSchema.RequestPermissionRequest request) {
        if (permissionHandler != null) {
            return permissionHandler.apply(request);
        }
        if (request.options() != null && !request.options().isEmpty()) {
            return new AcpSchema.RequestPermissionResponse(
                    new AcpSchema.PermissionSelected(request.options().get(0).optionId()));
        }
        return new AcpSchema.RequestPermissionResponse(new AcpSchema.PermissionCancelled());
    }

    public void setPermissionHandler(
            java.util.function.Function<AcpSchema.RequestPermissionRequest,
                    AcpSchema.RequestPermissionResponse> handler) {
        this.permissionHandler = handler;
    }
}
