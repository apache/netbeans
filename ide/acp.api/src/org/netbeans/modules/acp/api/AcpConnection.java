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
import com.agentclientprotocol.sdk.error.AcpErrorCodes;
import com.agentclientprotocol.sdk.error.AcpProtocolException;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.AgentMessageChunk;
import com.agentclientprotocol.sdk.spec.AcpSchema.AgentThoughtChunk;
import com.agentclientprotocol.sdk.spec.AcpSchema.ContentBlock;
import com.agentclientprotocol.sdk.spec.AcpSchema.Plan;
import com.agentclientprotocol.sdk.spec.AcpSchema.SessionNotification;
import com.agentclientprotocol.sdk.spec.AcpSchema.SessionUpdate;
import com.agentclientprotocol.sdk.spec.AcpSchema.TextContent;
import com.agentclientprotocol.sdk.spec.AcpSchema.ToolCallUpdateNotification;
import com.agentclientprotocol.sdk.spec.AcpSchema.AvailableCommandsUpdate;
import com.agentclientprotocol.sdk.spec.AcpSchema.SessionInfoUpdate;
import com.agentclientprotocol.sdk.spec.AcpSchema.UsageUpdate;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A connection to an ACP agent, over stdio or WebSocket. Handles the
 * handshake, session creation, prompting and cancellation, and notifies
 * registered {@link AcpSessionListener}s of agent updates.
 * <p>
 * Methods that talk to the agent ({@link #connect()}, {@link #newSession},
 * {@link #sendPrompt}, {@link #cancel}) block and must not be called from the
 * event dispatch thread.
 */
public final class AcpConnection {

    private static final Logger LOG = Logger.getLogger(AcpConnection.class.getName());

    private final AcpAgentDescriptor descriptor;
    private final List<AcpSessionListener> listeners = new CopyOnWriteArrayList<>();

    private final Map<String, List<Path>> trustedDirectories = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionAllowedTools = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> fileSnapshots = new ConcurrentHashMap<>();
    private final Map<String, List<AcpSchema.ToolCallLocation>> storedLocations = new ConcurrentHashMap<>();

    private volatile AcpSyncClient client;
    private volatile boolean connected;
    private volatile java.util.function.Function<AcpSchema.RequestPermissionRequest,
            AcpSchema.RequestPermissionResponse> permissionHandler;

    /**
     * @param descriptor the agent configuration, must not be {@code null}
     */
    public AcpConnection(AcpAgentDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null"); // NOI18N
    }

    /**
     * @return the agent descriptor used to create this connection
     */
    public AcpAgentDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Establishes the transport (stdio or WebSocket) and performs the
     * ACP handshake. Must not be called from the EDT.
     *
     * @throws IOException if the connection cannot be established
     * @throws IllegalStateException if already connected
     */
    public synchronized void connect() throws IOException {
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

    /**
     * Creates a new agent session with auto-discovered MCP servers.
     *
     * @param workingDirectory main project directory, or {@code null}
     * @return the session identifier
     * @throws IOException if session creation fails
     */
    public String newSession(String workingDirectory) throws IOException {
        return newSession(workingDirectory, (List<String>) null);
    }

    /**
     * Creates a new agent session with auto-discovered MCP servers.
     *
     * @param workingDirectory main project directory, or {@code null}
     * @param additionalDirs   additional project directories, or {@code null}
     * @return the session identifier
     * @throws IOException if session creation fails
     */
    public String newSession(String workingDirectory, List<String> additionalDirs) throws IOException {
        List<AcpSchema.McpServer> mcpServers = new ArrayList<>();
        try {
            for (AcpMcpServerProvider provider : org.openide.util.Lookup.getDefault()
                    .lookupAll(AcpMcpServerProvider.class)) {
                List<AcpSchema.McpServer> servers = provider.getMcpServers();
                if (servers != null) {
                    mcpServers.addAll(servers);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "MCP server discovery failed (modules may not be initialized yet)", ex);
        }
        if (!mcpServers.isEmpty()) {
            LOG.log(Level.INFO, "Discovered {0} MCP server(s) for new session", mcpServers.size());
        }
        return newSession(workingDirectory, additionalDirs, mcpServers);
    }

    private static AcpSchema.NewSessionRequest basicSessionRequest(String dir,
            List<AcpSchema.McpServer> mcpServers) {
        return mcpServers.isEmpty()
                ? new AcpSchema.NewSessionRequest(dir)
                : new AcpSchema.NewSessionRequest(dir, mcpServers);
    }

    /**
     * Creates a new agent session with explicit MCP servers.
     *
     * @param workingDirectory main project directory, or {@code null}
     * @param additionalDirs   additional project directories, or {@code null}
     * @param mcpServers       MCP servers to expose to the agent
     * @return the session identifier
     * @throws IOException if session creation fails
     */
    public String newSession(String workingDirectory, List<String> additionalDirs,
            List<AcpSchema.McpServer> mcpServers) throws IOException {
        ensureConnected();
        try {
            String effectiveDir = workingDirectory;
            if (effectiveDir == null || effectiveDir.isBlank()) {
                effectiveDir = descriptor.getWorkingDirectory();
            }
            List<AcpSchema.McpServer> effectiveMcpServers =
                    mcpServers != null ? mcpServers : List.of();

            String sessionId;
            if (additionalDirs != null && !additionalDirs.isEmpty()) {
                try {
                    sessionId = client.newSession(new AcpSchema.NewSessionRequest(
                            effectiveDir, effectiveMcpServers, additionalDirs)).sessionId();
                } catch (AcpCapabilityException ex) {
                    LOG.log(Level.INFO, "Agent does not support additionalDirectories, " // NOI18N
                            + "falling back without additionalDirectories"); // NOI18N
                    sessionId = client.newSession(
                            basicSessionRequest(effectiveDir, effectiveMcpServers)).sessionId();
                }
            } else {
                sessionId = client.newSession(
                        basicSessionRequest(effectiveDir, effectiveMcpServers)).sessionId();
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
            List<Path> resolved = new ArrayList<>();
            for (Path t : trusted) {
                Path r = resolveReal(t);
                if (!resolved.contains(r)) {
                    resolved.add(r);
                }
            }
            trustedDirectories.put(sessionId, List.copyOf(resolved));
            return sessionId;
        } catch (Exception ex) {
            throw new IOException("Failed to create new session", ex); // NOI18N
        }
    }

    /**
     * Sends a text prompt to the agent in the given session. Blocks until
     * the agent completes the turn.
     *
     * @param sessionId the session identifier
     * @param text      the prompt text
     * @throws IOException if the prompt cannot be sent
     */
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

    /**
     * Requests cancellation of the current prompt in the given session.
     *
     * @param sessionId the session identifier
     * @throws IOException if the cancellation fails
     */
    public void cancel(String sessionId) throws IOException {
        ensureConnected();
        try {
            client.cancel(new AcpSchema.CancelNotification(sessionId));
        } catch (Exception ex) {
            throw new IOException("Failed to cancel prompt", ex); // NOI18N
        }
    }

    /** Closes the connection and releases all resources. */
    public synchronized void close() {
        connected = false;
        sessionAllowedTools.clear();
        fileSnapshots.clear();
        storedLocations.clear();
        trustedDirectories.clear();
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

    /** @return {@code true} if the connection is established */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param listener the listener to add, must not be {@code null}
     */
    public void addSessionListener(AcpSessionListener listener) {
        Objects.requireNonNull(listener, "listener must not be null"); // NOI18N
        listeners.add(listener);
    }

    /**
     * @param listener the listener to remove
     */
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
            LOG.log(Level.FINE, "ToolCall: id={0}, name={1}, title={2}, kind={3}, status={4}, "
                    + "contentCount={5}, locationCount={6}",
                    new Object[]{tc.toolCallId(), tc.name(), tc.title(),
                        tc.kind(), tc.status(),
                        tc.content() != null ? tc.content().size() : 0,
                        tc.locations() != null ? tc.locations().size() : 0});
            if (tc.content() != null) {
                for (AcpSchema.ToolCallContent tcc : tc.content()) {
                    LOG.log(Level.FINE, "  ToolCall content type: {0}",
                            tcc.getClass().getSimpleName());
                }
            }
            if (tc.locations() != null) {
                for (AcpSchema.ToolCallLocation loc : tc.locations()) {
                    LOG.log(Level.FINE, "  ToolCall location: path={0}, line={1}",
                            new Object[]{loc.path(), loc.line()});
                }
            }
            AcpToolCall toolCall = buildToolCall(tc.toolCallId(), tc.title(), tc.name(),
                    tc.kind(), tc.status(), tc.content(), tc.locations());
            snapshotFilesForToolCall(tc.toolCallId(), tc.kind(), tc.locations());
            notifyListeners(l -> l.onToolCall(sessionId, toolCall));
        } else if (update instanceof ToolCallUpdateNotification tcu) {
            LOG.log(Level.FINE, "ToolCallUpdate: id={0}, name={1}, title={2}, kind={3}, status={4}, "
                    + "contentCount={5}, locationCount={6}",
                    new Object[]{tcu.toolCallId(), tcu.name(), tcu.title(),
                        tcu.kind(), tcu.status(),
                        tcu.content() != null ? tcu.content().size() : 0,
                        tcu.locations() != null ? tcu.locations().size() : 0});
            if (tcu.content() != null) {
                for (AcpSchema.ToolCallContent tcc : tcu.content()) {
                    LOG.log(Level.FINE, "  ToolCallUpdate content type: {0}",
                            tcc.getClass().getSimpleName());
                }
            }
            if (tcu.locations() != null) {
                for (AcpSchema.ToolCallLocation loc : tcu.locations()) {
                    LOG.log(Level.FINE, "  ToolCallUpdate location: path={0}, line={1}",
                            new Object[]{loc.path(), loc.line()});
                }
            }
            if (!fileSnapshots.containsKey(tcu.toolCallId())) {
                LOG.log(Level.FINE, "  No snapshot yet for {0}, attempting late snapshot", tcu.toolCallId());
                snapshotFilesForToolCall(tcu.toolCallId(), tcu.kind(), tcu.locations());
            }
            AcpToolCall toolCall = buildToolCall(tcu.toolCallId(), tcu.title(), tcu.name(),
                    tcu.kind(), tcu.status(), tcu.content(), tcu.locations());
            LOG.log(Level.FINE, "  Built AcpToolCall: diffs={0}, hasTextContent={1}",
                    new Object[]{toolCall.getDiffs().size(), toolCall.getTextContent() != null});
            if (toolCall.getDiffs().isEmpty()) {
                LOG.log(Level.FINE, "  No SDK diffs, trying snapshot-based diff emission");
                emitDiffsFromSnapshots(sessionId, tcu.toolCallId(), tcu.status(), tcu.locations());
            }
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
        } else if (update instanceof AvailableCommandsUpdate acu) {
            List<AcpCommand> commands = new ArrayList<>();
            if (acu.availableCommands() != null) {
                for (AcpSchema.AvailableCommand ac : acu.availableCommands()) {
                    commands.add(new AcpCommand(ac.name(), ac.description()));
                }
            }
            LOG.log(Level.INFO, "Agent available commands: {0}",
                    commands.stream().map(AcpCommand::name)
                            .collect(java.util.stream.Collectors.joining(", ")));
            List<AcpCommand> unmodifiable = List.copyOf(commands);
            notifyListeners(l -> l.onAvailableCommands(sessionId, unmodifiable));
        } else if (update instanceof SessionInfoUpdate siu) {
            LOG.log(Level.INFO, "Session info: title={0}, updatedAt={1}",
                    new Object[]{siu.title(), siu.updatedAt()});
            String title = siu.title();
            String updatedAt = siu.updatedAt();
            notifyListeners(l -> l.onSessionInfo(sessionId, title, updatedAt));
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

    private static boolean isWriteKind(AcpSchema.ToolKind kind) {
        // Agents classify file-modifying tools inconsistently — Claude Code
        // sends EDIT, OTHER, or even EXECUTE for writes.  Snapshot any kind
        // that could write to disk; the diff is only emitted when the file
        // actually changed, so false positives are harmless.
        return kind == AcpSchema.ToolKind.EDIT
                || kind == AcpSchema.ToolKind.OTHER
                || kind == AcpSchema.ToolKind.EXECUTE
                || kind == AcpSchema.ToolKind.DELETE
                || kind == AcpSchema.ToolKind.MOVE
                || kind == null;
    }

    private void snapshotFilesForToolCall(String toolCallId,
            AcpSchema.ToolKind kind,
            List<AcpSchema.ToolCallLocation> locations) {
        if (!isWriteKind(kind)) {
            LOG.log(Level.FINE, "  snapshot skip: kind={0} is not a write kind", kind);
            return;
        }
        if (locations == null || locations.isEmpty()) {
            LOG.log(Level.FINE, "  snapshot skip: no locations for toolCallId={0}", toolCallId);
            return;
        }
        Map<String, String> snapshots = new ConcurrentHashMap<>();
        for (AcpSchema.ToolCallLocation loc : locations) {
            if (loc.path() != null) {
                try {
                    Path p = Path.of(loc.path());
                    if (Files.exists(p) && Files.isRegularFile(p)) {
                        String content = Files.readString(p);
                        snapshots.put(loc.path(), content);
                        LOG.log(Level.FINE, "  snapshot captured: {0} ({1} chars)",
                                new Object[]{loc.path(), content.length()});
                    } else {
                        LOG.log(Level.FINE, "  snapshot: file not found or not regular: {0}", loc.path());
                    }
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Could not snapshot file: " + loc.path(), ex); // NOI18N
                }
            }
        }
        if (!snapshots.isEmpty()) {
            fileSnapshots.put(toolCallId, snapshots);
            storedLocations.put(toolCallId, List.copyOf(locations));
            LOG.log(Level.FINE, "  snapshots stored for {0}: {1} file(s)",
                    new Object[]{toolCallId, snapshots.size()});
        } else {
            LOG.log(Level.FINE, "  no snapshots captured for {0}", toolCallId);
        }
    }

    private void emitDiffsFromSnapshots(String sessionId, String toolCallId,
            AcpSchema.ToolCallStatus status,
            List<AcpSchema.ToolCallLocation> locations) {
        if (status == null || !AcpToolCall.STATUS_COMPLETED.equals(status.value())) {
            LOG.log(Level.FINE, "  emitDiffs skip: status={0} (need completed)",
                    status != null ? status.value() : "null");
            return;
        }
        Map<String, String> snapshots = fileSnapshots.remove(toolCallId);
        List<AcpSchema.ToolCallLocation> effectiveLocations = locations;
        if ((effectiveLocations == null || effectiveLocations.isEmpty())) {
            effectiveLocations = storedLocations.get(toolCallId);
            LOG.log(Level.FINE, "  emitDiffs: update had no locations, using stored={0}",
                    effectiveLocations != null ? effectiveLocations.size() : 0);
        }
        storedLocations.remove(toolCallId);
        LOG.log(Level.FINE, "  emitDiffs: hasSnapshots={0}, locationCount={1}",
                new Object[]{snapshots != null, effectiveLocations != null ? effectiveLocations.size() : 0});
        if (effectiveLocations == null || effectiveLocations.isEmpty()) {
            LOG.log(Level.FINE, "  emitDiffs skip: no locations");
            return;
        }
        for (AcpSchema.ToolCallLocation loc : effectiveLocations) {
            if (loc.path() == null) {
                continue;
            }
            String oldContent = snapshots != null ? snapshots.get(loc.path()) : null;
            try {
                Path p = Path.of(loc.path());
                if (Files.exists(p) && Files.isRegularFile(p)) {
                    String newContent = Files.readString(p);
                    boolean changed = !Objects.equals(oldContent, newContent);
                    LOG.log(Level.FINE, "  emitDiffs: path={0}, hasOld={1}, newLen={2}, changed={3}",
                            new Object[]{loc.path(), oldContent != null, newContent.length(), changed});
                    if (changed) {
                        String path = loc.path();
                        notifyListeners(l -> l.onFileWritten(sessionId, path, oldContent, newContent));
                        LOG.log(Level.FINE, "  emitDiffs: fired onFileWritten for {0}", path);
                    }
                } else {
                    LOG.log(Level.FINE, "  emitDiffs: file not found or not regular: {0}", loc.path());
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not read file for diff: " + loc.path(), ex); // NOI18N
            }
        }
    }

    private boolean isPathTrusted(String sessionId, Path path) {
        Path normalized = resolveReal(path);
        List<Path> trusted = trustedDirectories.get(sessionId);
        if (trusted == null || trusted.isEmpty()) {
            LOG.log(Level.WARNING, "No trusted directories for session {0}, denying access", sessionId); // NOI18N
            return false;
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

    /** Resolves symlinks of the deepest existing ancestor, then re-appends the non-existing remainder. */
    private static Path resolveReal(Path path) {
        Path abs = path.toAbsolutePath().normalize();
        Path existing = abs;
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing == null) {
            return abs;
        }
        try {
            Path real = existing.toRealPath();
            return real.resolve(existing.relativize(abs)).normalize();
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Could not resolve real path of: " + abs, ex); // NOI18N
            return abs;
        }
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
        LOG.log(Level.FINE, "writeTextFileHandler called: path={0}", request.path());
        Path filePath = Path.of(request.path());
        String sessionId = request.sessionId();
        String writtenPath = request.path();
        boolean trusted = isPathTrusted(sessionId, filePath);
        String oldContent = null;
        try {
            if (Files.exists(filePath)) {
                oldContent = Files.readString(filePath);
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Could not read existing content for: " + request.path(), ex); // NOI18N
        }
        String prevContent = oldContent;
        String newContent = request.content();
        if (trusted) {
            try {
                Path parent = filePath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.writeString(filePath, request.content());
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Failed to write file requested by agent: " + request.path(), ex); // NOI18N
                throw new AcpProtocolException(AcpErrorCodes.INTERNAL_ERROR,
                        "Failed to write file: " + request.path()); // NOI18N
            }
        } else {
            LOG.log(Level.WARNING, "Agent write blocked outside trusted directory: {0}", request.path()); // NOI18N
            throw new AcpProtocolException(AcpErrorCodes.INVALID_PARAMS,
                    "Write blocked: path is outside the trusted directories: " + request.path()); // NOI18N
        }
        notifyListeners(l -> l.onFileWritten(sessionId, writtenPath, prevContent, newContent));
        return new AcpSchema.WriteTextFileResponse();
    }

    private AcpSchema.RequestPermissionResponse handleRequestPermission(
            AcpSchema.RequestPermissionRequest request) {
        List<AcpSchema.PermissionOption> options = request.options();
        if (options != null && !options.isEmpty() && isToolCallTrusted(request)) {
            AcpSchema.PermissionOption allow = findAllowOption(options);
            if (allow != null) {
                return new AcpSchema.RequestPermissionResponse(
                        new AcpSchema.PermissionSelected(allow.optionId()));
            }
        }
        if (options != null && !options.isEmpty() && isToolSessionAllowed(request)) {
            AcpSchema.PermissionOption allow = findAllowOption(options);
            if (allow != null) {
                return new AcpSchema.RequestPermissionResponse(
                        new AcpSchema.PermissionSelected(allow.optionId()));
            }
        }
        if (permissionHandler != null) {
            return permissionHandler.apply(request);
        }
        LOG.log(Level.WARNING, "No permission handler registered, cancelling permission request"); // NOI18N
        return new AcpSchema.RequestPermissionResponse(new AcpSchema.PermissionCancelled());
    }

    private static AcpSchema.PermissionOption findAllowOption(List<AcpSchema.PermissionOption> options) {
        for (AcpSchema.PermissionOption opt : options) {
            AcpSchema.PermissionOptionKind kind = opt.kind();
            if (AcpSchema.PermissionOptionKind.ALLOW_ONCE.equals(kind)
                    || AcpSchema.PermissionOptionKind.ALLOW_ALWAYS.equals(kind)) {
                return opt;
            }
        }
        return null;
    }

    private boolean isToolSessionAllowed(AcpSchema.RequestPermissionRequest request) {
        AcpSchema.ToolCallUpdate toolCall = request.toolCall();
        if (toolCall == null) {
            return false;
        }
        String toolName = toolCall.name();
        if (toolName == null) {
            return false;
        }
        Set<String> allowed = sessionAllowedTools.get(request.sessionId());
        return allowed != null && allowed.contains(toolName);
    }

    /**
     * Auto-approves a tool for the remainder of the session.
     *
     * @param sessionId the session identifier
     * @param toolName  the tool to auto-approve
     */
    public void allowToolForSession(String sessionId, String toolName) {
        Objects.requireNonNull(sessionId);
        sessionAllowedTools.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(Objects.requireNonNull(toolName));
        LOG.log(Level.INFO, "Tool auto-allowed for session: {0}", toolName); // NOI18N
    }

    private boolean isToolCallTrusted(AcpSchema.RequestPermissionRequest request) {
        AcpSchema.ToolCallUpdate toolCall = request.toolCall();
        if (toolCall == null) {
            return false;
        }
        AcpSchema.ToolKind kind = toolCall.kind();
        // Only auto-approve READ operations in trusted directories.
        // All other kinds (EDIT, EXECUTE, DELETE, MOVE, OTHER, null)
        // must go through the permission UI.
        if (kind != AcpSchema.ToolKind.READ) {
            return false;
        }
        List<AcpSchema.ToolCallLocation> locations = toolCall.locations();
        if (locations == null || locations.isEmpty()) {
            return false;
        }
        String sessionId = request.sessionId();
        for (AcpSchema.ToolCallLocation loc : locations) {
            if (loc.path() == null || !isPathTrusted(sessionId, Path.of(loc.path()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets a handler invoked when the agent requests permission for a
     * tool call that is not auto-approved.
     *
     * @param handler the permission handler, or {@code null} to clear
     */
    public void setPermissionHandler(
            java.util.function.Function<AcpSchema.RequestPermissionRequest,
                    AcpSchema.RequestPermissionResponse> handler) {
        this.permissionHandler = handler;
    }
}
