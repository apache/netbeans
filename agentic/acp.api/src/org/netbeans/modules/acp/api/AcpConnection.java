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

import org.netbeans.modules.acp.spi.AcpMcpServerProvider;

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
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.openide.filesystems.FileUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
    private static final String BEARER_PREFIX = "Bearer "; // NOI18N
    private static final String AUTHORIZATION_HEADER = "Authorization"; // NOI18N

    private final AcpAgentDescriptor descriptor;
    private final List<AcpSessionListener> listeners = new CopyOnWriteArrayList<>();

    private final Map<String, List<Path>> trustedDirectories = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionAllowedTools = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> fileSnapshots = new ConcurrentHashMap<>();
    private final Map<String, List<AcpSchema.ToolCallLocation>> storedLocations = new ConcurrentHashMap<>();
    private final AcpTerminalHandler terminalHandler = new AcpTerminalHandler();
    private final Object sessionLock = new Object();
    // A pending request owns its tokens until it fails or transfers them to its session ID.
    private final Map<Object, Set<String>> mcpTokensByOwner = new HashMap<>();

    private volatile AcpSyncClient client;
    private volatile boolean connected;
    private volatile List<AcpSchema.AuthMethod> authenticationMethods = List.of();
    private volatile Predicate<AcpSchema.CreateTerminalRequest> terminalAuthorizer;
    private volatile java.util.function.Function<AcpSchema.RequestPermissionRequest,
            AcpSchema.RequestPermissionResponse> permissionHandler;
    private volatile java.util.function.Function<AcpSchema.CreateElicitationRequest,
            AcpSchema.CreateElicitationResponse> elicitationHandler;
    private volatile List<AcpModeInfo> availableModes = List.of();
    private volatile String currentModeId;

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
                        validateEnvironment(envVars);
                        List<String> unexpanded = environmentReferences(envVars);
                        if (!unexpanded.isEmpty()) {
                            LOG.log(Level.WARNING, "Agent environment variables contain references that will be passed literally: {0}", // NOI18N
                                    String.join(", ", unexpanded));
                        }
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
            if (AcpWorkspaceFiles.isSupported()) {
                spec.readTextFileHandler(this::handleReadTextFile);
                spec.writeTextFileHandler(this::handleWriteTextFile);
            }
            spec.requestPermissionHandler(this::handleRequestPermission);
            spec.createTerminalHandler(this::createAuthorizedTerminal);
            spec.terminalOutputHandler(terminalHandler::handleTerminalOutput);
            spec.waitForTerminalExitHandler(terminalHandler::handleWaitForTerminalExit);
            spec.killTerminalHandler(terminalHandler::handleKillTerminal);
            spec.releaseTerminalHandler(terminalHandler::handleReleaseTerminal);
            spec.createElicitationHandler(this::handleElicitation);
            spec.clientInfo(new AcpSchema.Implementation("Apache NetBeans ACP Plugin", "1.0")); // NOI18N

            client = spec.build();
            AcpSchema.InitializeResponse initialization = client.initialize();
            authenticationMethods = initialization.authMethods() != null
                    ? List.copyOf(initialization.authMethods()) : List.of();
            connected = true;

            LOG.log(Level.INFO, "Connected to ACP agent: {0}", descriptor.getName()); // NOI18N
        } catch (Exception ex) {
            close();
            throw new IOException("Failed to connect to agent: " + descriptor.getName(), ex); // NOI18N
        }
    }

    /**
     * Validates environment overrides before starting an agent process.
     *
     * @param environment environment variable overrides
     * @throws IllegalArgumentException if an override cannot be used by the process
     */
    public static void validateEnvironment(Map<String, String> environment) {
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name == null || name.isEmpty() || name.indexOf('=') >= 0 || name.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("An environment variable has an invalid name"); // NOI18N
            }
            if (value == null || value.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Invalid value for environment variable " + name); // NOI18N
            }
        }
    }

    /**
     * Finds environment overrides containing shell-style variable references.
     * Values are passed directly to the process and are not expanded.
     *
     * @param environment environment variable overrides
     * @return names of overrides containing possible references
     */
    public static List<String> environmentReferences(Map<String, String> environment) {
        java.util.regex.Pattern reference = java.util.regex.Pattern.compile(
                "\\$[A-Za-z_][A-Za-z0-9_]*|\\$\\{[A-Za-z_][A-Za-z0-9_]*\\}|%[A-Za-z_][A-Za-z0-9_]*%");
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            if (entry.getValue() != null && reference.matcher(entry.getValue()).find()) {
                names.add(entry.getKey());
            }
        }
        return List.copyOf(names);
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
        AcpSyncClient sessionClient = requireClient();
        List<AcpSchema.McpServer> mcpServers = discoverMcpServers();
        if (!mcpServers.isEmpty()) {
            LOG.log(Level.INFO, "Discovered {0} MCP server(s) for new session", mcpServers.size());
        }
        return newSession(sessionClient, workingDirectory, additionalDirs, mcpServers);
    }

    private static AcpSchema.NewSessionRequest basicSessionRequest(String dir,
            List<AcpSchema.McpServer> mcpServers) {
        return mcpServers.isEmpty()
                ? new AcpSchema.NewSessionRequest(dir)
                : new AcpSchema.NewSessionRequest(dir, mcpServers);
    }

    private List<AcpSchema.McpServer> discoverMcpServers() {
        return discoverMcpServers(org.openide.util.Lookup.getDefault()
                .lookupAll(AcpMcpServerProvider.class), descriptor.getEnabledTools());
    }

    static List<AcpSchema.McpServer> discoverMcpServers(
            Iterable<? extends AcpMcpServerProvider> providers, Set<String> enabledTools) {
        Set<AcpSchema.McpServer> mcpServers = new LinkedHashSet<>();
        for (AcpMcpServerProvider provider : providers) {
            try {
                List<AcpSchema.McpServer> servers = provider.getMcpServers(enabledTools);
                if (servers != null) {
                    mcpServers.addAll(servers);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "MCP server discovery failed for provider " // NOI18N
                        + provider.getClass().getName(), ex);
            }
        }
        return new ArrayList<>(mcpServers);
    }

    private static Set<String> collectBearerTokens(List<AcpSchema.McpServer> servers) {
        Set<String> tokens = new LinkedHashSet<>();
        for (AcpSchema.McpServer server : servers) {
            List<AcpSchema.HttpHeader> headers;
            if (server instanceof AcpSchema.McpServerHttp http) {
                headers = http.headers();
            } else if (server instanceof AcpSchema.McpServerSse sse) {
                headers = sse.headers();
            } else {
                continue;
            }
            if (headers == null) {
                continue;
            }
            for (AcpSchema.HttpHeader header : headers) {
                if (AUTHORIZATION_HEADER.equalsIgnoreCase(header.name())
                        && header.value() != null
                        && header.value().startsWith(BEARER_PREFIX)) {
                    tokens.add(header.value().substring(BEARER_PREFIX.length()));
                }
            }
        }
        return tokens;
    }

    private List<AcpSchema.McpServer> registerMcpServers(AcpSyncClient sessionClient,
            Object request, List<AcpSchema.McpServer> supplied) {
        Set<String> suppliedTokens = collectBearerTokens(supplied);
        synchronized (sessionLock) {
            if (!connected || client != sessionClient) {
                revokeUnusedTokens(suppliedTokens);
                throw new IllegalStateException("ACP connection closed during session setup"); // NOI18N
            }
            mcpTokensByOwner.put(request, suppliedTokens);
            List<AcpSchema.McpServer> supported = supportedMcpServers(supplied,
                    sessionClient.getAgentCapabilities().supportsMcpHttp(),
                    sessionClient.getAgentCapabilities().supportsMcpSse());
            mcpTokensByOwner.put(request, collectBearerTokens(supported));
            revokeUnusedTokens(suppliedTokens);
            return supported;
        }
    }

    private void completeSession(AcpSyncClient sessionClient, Object request,
            String sessionId, List<Path> directories, AcpSchema.SessionModeState modes) {
        synchronized (sessionLock) {
            if (!connected || client != sessionClient) {
                throw new IllegalStateException("ACP connection closed during session setup"); // NOI18N
            }
            Objects.requireNonNull(sessionId, "Session ID is required"); // NOI18N
            captureModes(modes);
            trustedDirectories.put(sessionId, directories);
            Set<String> previous = mcpTokensByOwner.put(sessionId, mcpTokensByOwner.remove(request));
            revokeUnusedTokens(previous);
        }
    }

    private void releaseMcpTokens(Object owner) {
        synchronized (sessionLock) {
            revokeUnusedTokens(mcpTokensByOwner.remove(owner));
        }
    }

    // Called under sessionLock so that a token cannot gain a new owner while it is being revoked.
    private void revokeUnusedTokens(Set<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        for (String token : tokens) {
            if (mcpTokensByOwner.values().stream().anyMatch(owned -> owned.contains(token))) {
                continue;
            }
            for (AcpMcpServerProvider provider : org.openide.util.Lookup.getDefault()
                    .lookupAll(AcpMcpServerProvider.class)) {
                try {
                    provider.releaseBearerToken(token);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Failed to revoke bearer token", ex); // NOI18N
                }
            }
        }
    }

    static List<AcpSchema.McpServer> supportedMcpServers(List<AcpSchema.McpServer> servers,
            boolean supportsHttp, boolean supportsSse) {
        return servers.stream().filter(server -> {
            boolean supported = (!(server instanceof AcpSchema.McpServerHttp) || supportsHttp)
                    && (!(server instanceof AcpSchema.McpServerSse) || supportsSse);
            if (!supported) {
                LOG.log(Level.INFO, "Skipping MCP server with transport unsupported by agent: {0}", // NOI18N
                        server instanceof AcpSchema.McpServerHttp ? "http" : "sse"); // NOI18N
            }
            return supported;
        }).toList();
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
        return newSession(requireClient(), workingDirectory, additionalDirs,
                mcpServers != null ? mcpServers : List.of());
    }

    private String newSession(AcpSyncClient sessionClient, String workingDirectory,
            List<String> additionalDirs, List<AcpSchema.McpServer> mcpServers) throws IOException {
        Object request = new Object();
        try {
            List<AcpSchema.McpServer> effectiveMcpServers = registerMcpServers(sessionClient, request, mcpServers);
            String effectiveDir = sessionWorkingDirectory(workingDirectory);
            List<Path> primaryDirectory = trustedDirectory(effectiveDir);
            List<Path> directories = additionalDirs == null || additionalDirs.isEmpty()
                    ? primaryDirectory : trustedDirectories(primaryDirectory, additionalDirs);
            LOG.log(Level.INFO, "Agent {0} session creation: additional directories supported={1}, requested={2}", // NOI18N
                    new Object[]{descriptor.getName(), sessionClient.getAgentCapabilities().supportsAdditionalDirectories(),
                        additionalDirs != null ? additionalDirs.size() : 0});
            LOG.log(Level.FINE, "Session creation working directory={0}, additional directories={1}", // NOI18N
                    new Object[]{effectiveDir, additionalDirs});
            AcpSchema.NewSessionResponse newResp;
            if (additionalDirs != null && !additionalDirs.isEmpty()) {
                try {
                    newResp = sessionClient.newSession(new AcpSchema.NewSessionRequest(
                            effectiveDir, effectiveMcpServers, additionalDirs));
                    LOG.log(Level.INFO, "Agent {0} created session with {1} additional directories", // NOI18N
                            new Object[]{descriptor.getName(), additionalDirs.size()});
                } catch (AcpCapabilityException ex) {
                    if (!"sessionCapabilities.additionalDirectories".equals(ex.getCapability())) { // NOI18N
                        throw ex;
                    }
                    LOG.log(Level.INFO, "Agent does not support additionalDirectories, " // NOI18N
                            + "falling back without additionalDirectories"); // NOI18N
                    directories = primaryDirectory;
                    newResp = sessionClient.newSession(
                            basicSessionRequest(effectiveDir, effectiveMcpServers));
                }
            } else {
                newResp = sessionClient.newSession(
                        basicSessionRequest(effectiveDir, effectiveMcpServers));
            }
            String sessionId = newResp.sessionId();
            completeSession(sessionClient, request, sessionId,
                    directories, newResp.modes());
            return sessionId;
        } catch (RuntimeException ex) {
            throw new IOException("Failed to create new session", ex); // NOI18N
        } finally {
            releaseMcpTokens(request);
        }
    }

    /**
     * Lists past sessions known by the agent for the given working directory.
     *
     * @param workingDirectory the project directory, or {@code null}
     * @return a list of session summaries (never {@code null})
     * @throws IOException if the operation fails
     */
    public List<AcpSessionInfo> listSessions(String workingDirectory) throws IOException {
        ensureConnected();
        try {
            String cwd = workingDirectory;
            if (cwd == null || cwd.isBlank()) {
                cwd = descriptor.getWorkingDirectory();
            }
            AcpSchema.ListSessionsResponse response =
                    client.listSessions(new AcpSchema.ListSessionsRequest(cwd));
            List<AcpSessionInfo> result = new ArrayList<>();
            if (response.sessions() != null) {
                for (AcpSchema.SessionInfo si : response.sessions()) {
                    result.add(new AcpSessionInfo(si.sessionId(), si.title(), si.updatedAt()));
                }
            }
            return result;
        } catch (AcpCapabilityException ex) {
            throw new IOException("Agent does not support session listing", ex); // NOI18N
        } catch (RuntimeException ex) {
            throw new IOException("Failed to list sessions", ex); // NOI18N
        }
    }

    /**
     * Resumes an existing session. Sets up MCP servers, bearer tokens,
     * and trusted directories the same way as {@link #newSession}.
     *
     * @param sessionId        the session to resume
     * @param workingDirectory main project directory, or {@code null}
     * @return the session identifier (same as input)
     * @throws IOException if the resume fails
     */
    public String resumeSession(String sessionId, String workingDirectory) throws IOException {
        return resumeSession(sessionId, workingDirectory, null);
    }

    /**
     * Resumes an existing session with additional directories.
     *
     * @param sessionId        the session to resume
     * @param workingDirectory main project directory, or {@code null}
     * @param additionalDirs   additional project directories, or {@code null}
     * @return the session identifier (same as input)
     * @throws IOException if the resume fails
     */
    public String resumeSession(String sessionId, String workingDirectory,
            List<String> additionalDirs) throws IOException {
        AcpSyncClient sessionClient = requireClient();
        Object request = new Object();
        try {
            String effectiveDir = sessionWorkingDirectory(workingDirectory);
            List<Path> primaryDirectory = trustedDirectory(effectiveDir);
            List<Path> directories = additionalDirs == null || additionalDirs.isEmpty()
                    ? primaryDirectory : trustedDirectories(primaryDirectory, additionalDirs);

            LOG.log(Level.INFO, "Agent {0} session resume: additional directories supported={1}, requested={2}", // NOI18N
                    new Object[]{descriptor.getName(), sessionClient.getAgentCapabilities().supportsAdditionalDirectories(),
                        additionalDirs != null ? additionalDirs.size() : 0});
            LOG.log(Level.FINE, "Session resume working directory={0}, additional directories={1}", // NOI18N
                    new Object[]{effectiveDir, additionalDirs});

            List<AcpSchema.McpServer> mcpServers = registerMcpServers(sessionClient, request, discoverMcpServers());

            AcpSchema.ResumeSessionResponse resumeResp;
            if (additionalDirs != null && !additionalDirs.isEmpty()) {
                try {
                    resumeResp = sessionClient.resumeSession(new AcpSchema.ResumeSessionRequest(
                            sessionId, effectiveDir, mcpServers, additionalDirs));
                    LOG.log(Level.INFO, "Agent {0} resumed session with {1} additional directories", // NOI18N
                            new Object[]{descriptor.getName(), additionalDirs.size()});
                } catch (AcpCapabilityException ex) {
                    if (!"sessionCapabilities.additionalDirectories".equals(ex.getCapability())) { // NOI18N
                        throw ex;
                    }
                    LOG.log(Level.INFO, "Agent does not support additionalDirectories on resume, " // NOI18N
                            + "falling back without additionalDirectories"); // NOI18N
                    directories = primaryDirectory;
                    resumeResp = sessionClient.resumeSession(new AcpSchema.ResumeSessionRequest(
                            sessionId, effectiveDir, mcpServers));
                }
            } else {
                resumeResp = sessionClient.resumeSession(new AcpSchema.ResumeSessionRequest(
                        sessionId, effectiveDir, mcpServers));
            }
            completeSession(sessionClient, request, sessionId,
                    directories, resumeResp.modes());
            return sessionId;
        } catch (RuntimeException ex) {
            throw new IOException("Failed to resume session", ex); // NOI18N
        } finally {
            releaseMcpTokens(request);
        }
    }

    /**
     * Returns whether the agent supports resuming sessions.
     *
     * @return {@code true} if the agent advertises the resume capability
     */
    public boolean supportsResumeSession() {
        return client != null
                && client.getAgentCapabilities().supportsResumeSession();
    }

    /**
     * Returns whether the agent supports deleting sessions.
     *
     * @return {@code true} if the agent advertises the delete capability
     */
    public boolean supportsDeleteSession() {
        return client != null
                && client.getAgentCapabilities().supportsDeleteSession();
    }

    /**
     * Deletes a past session from the agent's history.
     *
     * @param sessionId the session to delete
     * @throws IOException if the deletion fails
     */
    public void deleteSession(String sessionId) throws IOException {
        AcpSyncClient sessionClient = requireClient();
        try {
            sessionClient.deleteSession(new AcpSchema.DeleteSessionRequest(sessionId));
            synchronized (sessionLock) {
                if (connected && client == sessionClient) {
                    trustedDirectories.remove(sessionId);
                    sessionAllowedTools.remove(sessionId);
                    terminalHandler.releaseSession(sessionId);
                    releaseMcpTokens(sessionId);
                }
            }
        } catch (RuntimeException ex) {
            throw new IOException("Failed to delete session", ex); // NOI18N
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
        AcpSyncClient sessionClient = requireClient();
        try {
            AcpSchema.PromptRequest request = AcpSchema.PromptRequest.text(sessionId, text);
            AcpSchema.PromptResponse response = sessionClient.prompt(request);
            String stopReason = response.stopReason() != null ? response.stopReason().value() : "end_turn"; // NOI18N
            LOG.log(Level.FINE, "PromptComplete: session={0}, stopReason={1}",
                    new Object[]{sessionId, stopReason});
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
        AcpSyncClient sessionClient = requireClient();
        try {
            sessionClient.cancel(new AcpSchema.CancelNotification(sessionId));
        } catch (Exception ex) {
            throw new IOException("Failed to cancel prompt", ex); // NOI18N
        }
    }

    /** Closes the connection and releases all resources. */
    public synchronized void close() {
        synchronized (sessionLock) {
            connected = false;
            authenticationMethods = List.of();
            sessionAllowedTools.clear();
            fileSnapshots.clear();
            storedLocations.clear();
            trustedDirectories.clear();
            terminalHandler.releaseAll();
            Set<String> tokens = new LinkedHashSet<>();
            mcpTokensByOwner.values().forEach(tokens::addAll);
            mcpTokensByOwner.clear();
            revokeUnusedTokens(tokens);
        }
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

    /**
     * Returns the authentication methods advertised during initialization.
     *
     * @return immutable list of available methods
     */
    public List<AcpSchema.AuthMethod> getAuthenticationMethods() {
        return authenticationMethods;
    }

    /**
     * Authenticates using an advertised agent-driven method. This operation may block.
     *
     * @param methodId the advertised method identifier
     * @throws IOException if authentication fails
     */
    public void authenticate(String methodId) throws IOException {
        AcpSyncClient sessionClient = requireClient();
        boolean supported = authenticationMethods.stream()
                .anyMatch(method -> method instanceof AcpSchema.AuthMethodAgent
                        && Objects.equals(method.id(), methodId));
        if (!supported) {
            throw new IOException("Unsupported authentication method: " + methodId); // NOI18N
        }
        try {
            sessionClient.authenticate(new AcpSchema.AuthenticateRequest(methodId));
        } catch (RuntimeException ex) {
            throw new IOException("Agent authentication failed", ex); // NOI18N
        }
    }

    private AcpSyncClient requireClient() {
        AcpSyncClient current = client;
        if (!connected || current == null) {
            throw new IllegalStateException("Not connected to agent"); // NOI18N
        }
        return current;
    }

    /**
     * Sets the callback that authorizes each request to execute a command on the IDE host.
     * The callback may block for user consent and runs on the ACP request thread.
     *
     * @param authorizer the authorization callback, or {@code null} to deny terminal creation
     */
    public void setTerminalAuthorizer(Predicate<AcpSchema.CreateTerminalRequest> authorizer) {
        synchronized (sessionLock) {
            terminalAuthorizer = authorizer;
        }
    }

    AcpSchema.CreateTerminalResponse createAuthorizedTerminal(AcpSchema.CreateTerminalRequest request) {
        String sessionId = request.sessionId();
        List<Path> directories = sessionId != null ? trustedDirectories.get(sessionId) : null;
        Predicate<AcpSchema.CreateTerminalRequest> authorizer = terminalAuthorizer;
        AcpSyncClient terminalClient = client;
        if (!connected || terminalClient == null || directories == null
                || authorizer == null || !authorizer.test(request)) {
            throw new AcpProtocolException(AcpErrorCodes.REQUEST_CANCELLED, "Local terminal execution was not authorized"); // NOI18N
        }
        synchronized (sessionLock) {
            if (!connected || client != terminalClient || trustedDirectories.get(sessionId) != directories
                    || terminalAuthorizer != authorizer) {
                throw new AcpProtocolException(AcpErrorCodes.REQUEST_CANCELLED, "Terminal authorization is no longer valid"); // NOI18N
            }
            return terminalHandler.handleCreateTerminal(request);
        }
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
            ContentBlock content = chunk.content();
            if (content instanceof AcpSchema.ImageContent img) {
                String data = img.data();
                String mimeType = img.mimeType();
                if (data != null && !data.isEmpty()) {
                    LOG.log(Level.FINE, "AgentImage: session={0}, mimeType={1}, dataLen={2}",
                            new Object[]{sessionId, mimeType, data.length()});
                    notifyListeners(l -> l.onAgentImage(sessionId, data,
                            mimeType != null ? mimeType : "image/png")); // NOI18N
                }
            } else {
                String text = extractText(content);
                LOG.log(Level.FINE, "AgentMessage: session={0}, text={1}",
                        new Object[]{sessionId, text});
                notifyListeners(l -> l.onAgentMessage(sessionId, text));
            }
        } else if (update instanceof AgentThoughtChunk chunk) {
            String text = extractText(chunk.content());
            LOG.log(Level.FINE, "AgentThought: session={0}, text={1}",
                    new Object[]{sessionId, text});
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
            snapshotFilesForToolCall(sessionId, tc.toolCallId(), tc.kind(), tc.locations());
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
                snapshotFilesForToolCall(sessionId, tcu.toolCallId(), tcu.kind(), tcu.locations());
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
        } else if (update instanceof AcpSchema.CurrentModeUpdate modeUpdate) {
            String modeId = modeUpdate.currentModeId();
            currentModeId = modeId;
            LOG.log(Level.INFO, "Session mode changed to: {0}", modeId); // NOI18N
            notifyListeners(l -> l.onModeUpdate(sessionId, modeId));
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

    private void snapshotFilesForToolCall(String sessionId, String toolCallId,
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
                    Path file = resolveWorkspaceFile(sessionId, loc.path(), false);
                    String content = AcpWorkspaceFiles.read(file, 1, null);
                    snapshots.put(loc.path(), content);
                } catch (IOException | InvalidPathException ex) {
                    LOG.log(Level.FINE, "Could not snapshot file: " + loc.path(), ex); // NOI18N
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
                Path file = resolveWorkspaceFile(sessionId, loc.path(), false);
                String newContent = AcpWorkspaceFiles.read(file, 1, null);
                if (!Objects.equals(oldContent, newContent)) {
                    String path = loc.path();
                    notifyListeners(l -> l.onFileWritten(sessionId, path, oldContent, newContent));
                }
            } catch (IOException | InvalidPathException ex) {
                LOG.log(Level.FINE, "Could not read file for diff: " + loc.path(), ex); // NOI18N
            }
        }
    }

    private String sessionWorkingDirectory(String requested) throws IOException {
        String directory = requested == null || requested.isBlank() ? descriptor.getWorkingDirectory() : requested;
        try {
            if (directory != null && !directory.isBlank() && Path.of(directory).isAbsolute()) {
                return directory;
            }
        } catch (InvalidPathException ex) {
            throw new IOException("Invalid session working directory", ex); // NOI18N
        }
        throw new IOException("An absolute session working directory is required"); // NOI18N
    }

    private static List<Path> trustedDirectories(List<Path> primaryDirectory, List<String> additionalDirs) {
        Set<Path> roots = new LinkedHashSet<>(primaryDirectory);
        for (String directory : additionalDirs) {
            roots.addAll(trustedDirectory(directory));
        }
        return List.copyOf(roots);
    }

    private static List<Path> trustedDirectory(String directory) {
        try {
            if (directory != null) {
                Path path = Path.of(directory);
                if (path.isAbsolute()) {
                    Path real = path.toRealPath();
                    if (Files.isDirectory(real)) {
                        return List.of(real);
                    }
                }
            }
        } catch (IOException | InvalidPathException ex) {
            LOG.log(Level.FINE, "Could not resolve session directory: " + directory, ex); // NOI18N
        }
        return List.of();
    }

    private Path resolveWorkspaceFile(String sessionId, String path, boolean allowNewFile) throws IOException {
        List<Path> roots = sessionId == null ? null : trustedDirectories.get(sessionId);
        if (roots == null || roots.isEmpty() || path == null || path.isBlank()) {
            throw new AccessDeniedException(String.valueOf(path));
        }
        return AcpWorkspaceFiles.resolve(roots, Path.of(path), allowNewFile);
    }

    private AcpSchema.ReadTextFileResponse handleReadTextFile(AcpSchema.ReadTextFileRequest request) {
        Integer line = request.line();
        Integer limit = request.limit();
        if ((line != null && line < 1) || (limit != null && limit < 1)) {
            throw new AcpProtocolException(AcpErrorCodes.INVALID_PARAMS, "Line and limit must be positive");
        }
        try {
            Path file = resolveWorkspaceFile(request.sessionId(), request.path(), false);
            return new AcpSchema.ReadTextFileResponse(AcpWorkspaceFiles.read(file, line == null ? 1 : line, limit));
        } catch (AccessDeniedException | NoSuchFileException | InvalidPathException ex) {
            throw new AcpProtocolException(AcpErrorCodes.INVALID_PARAMS,
                    "Read blocked: " + ex.getMessage()); // NOI18N
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to read file requested by agent: " + request.path(), ex); // NOI18N
            throw new AcpProtocolException(AcpErrorCodes.INTERNAL_ERROR,
                    "Failed to read file: " + request.path()); // NOI18N
        }
    }

    private AcpSchema.WriteTextFileResponse handleWriteTextFile(AcpSchema.WriteTextFileRequest request) {
        try {
            Path file = resolveWorkspaceFile(request.sessionId(), request.path(), true);
            String previous = AcpWorkspaceFiles.write(file, request.content());
            // Notify the NetBeans VFS so open editors pick up the change.
            org.openide.filesystems.FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file.toFile()));
            if (fo != null) {
                fo.refresh();
            }
            notifyListeners(l -> l.onFileWritten(request.sessionId(), request.path(), previous, request.content()));
            return new AcpSchema.WriteTextFileResponse();
        } catch (AccessDeniedException | NoSuchFileException | InvalidPathException ex) {
            throw new AcpProtocolException(AcpErrorCodes.INVALID_PARAMS,
                    "Write blocked: " + ex.getMessage()); // NOI18N
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to write file requested by agent: " + request.path(), ex); // NOI18N
            throw new AcpProtocolException(AcpErrorCodes.INTERNAL_ERROR,
                    "Failed to write file: " + request.path()); // NOI18N
        }
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
            try {
                resolveWorkspaceFile(sessionId, loc.path(), false);
            } catch (IOException | InvalidPathException ex) {
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

    /**
     * Sets a handler invoked when the agent requests structured input
     * via an elicitation form.
     *
     * @param handler the elicitation handler, or {@code null} to decline all
     */
    public void setElicitationHandler(
            java.util.function.Function<AcpSchema.CreateElicitationRequest,
                    AcpSchema.CreateElicitationResponse> handler) {
        this.elicitationHandler = handler;
    }

    /**
     * Switches the session to the specified mode.
     *
     * @param sessionId the session identifier
     * @param modeId    the mode to activate
     * @throws IOException if the mode switch fails
     */
    public void setSessionMode(String sessionId, String modeId) throws IOException {
        ensureConnected();
        try {
            client.setSessionMode(new AcpSchema.SetSessionModeRequest(sessionId, modeId));
        } catch (Exception ex) {
            throw new IOException("Failed to set session mode", ex); // NOI18N
        }
    }

    /** @return the available session modes, or an empty list */
    public List<AcpModeInfo> getAvailableModes() {
        return availableModes;
    }

    /** @return the current mode identifier, or {@code null} */
    public String getCurrentModeId() {
        return currentModeId;
    }

    private AcpSchema.CreateElicitationResponse handleElicitation(
            AcpSchema.CreateElicitationRequest request) {
        if (elicitationHandler != null) {
            return elicitationHandler.apply(request);
        }
        LOG.log(Level.WARNING, "No elicitation handler registered, declining"); // NOI18N
        return AcpSchema.CreateElicitationResponse.decline();
    }

    private void captureModes(AcpSchema.SessionModeState modeState) {
        if (modeState != null) {
            currentModeId = modeState.currentModeId();
            if (modeState.availableModes() != null) {
                List<AcpModeInfo> modes = new ArrayList<>();
                for (AcpSchema.SessionMode sm : modeState.availableModes()) {
                    modes.add(new AcpModeInfo(sm.id(), sm.name(), sm.description()));
                }
                availableModes = List.copyOf(modes);
            }
        }
    }
}
