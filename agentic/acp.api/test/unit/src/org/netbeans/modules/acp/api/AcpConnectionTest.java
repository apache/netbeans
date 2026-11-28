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

import com.agentclientprotocol.sdk.agent.AcpAgent;
import com.agentclientprotocol.sdk.agent.transport.StdioAcpAgentTransport;
import com.agentclientprotocol.sdk.error.AcpErrorCodes;
import com.agentclientprotocol.sdk.error.AcpProtocolException;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.acp.spi.AcpMcpServerProvider;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.MockLookup;

public class AcpConnectionTest extends NbTestCase {

    public AcpConnectionTest(String name) {
        super(name);
    }

    private AcpAgentDescriptor createDescriptor() {
        return new AcpAgentDescriptor.Builder("test-agent",
                AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("echo", "hello"))
                .build();
    }

    public void testEnvironmentValidationAndReferences() {
        AcpConnection.validateEnvironment(Map.of("JAVA_HOME", "/opt/java", "EMPTY", ""));
        assertTrue(AcpConnection.environmentReferences(Map.of("TOKEN", "price$5")).isEmpty());
        assertEquals(List.of("PATH"), AcpConnection.environmentReferences(
                Map.of("PATH", "$JAVA_HOME/bin:$PATH")));
        assertEquals(List.of("CACHE"), AcpConnection.environmentReferences(
                Map.of("CACHE", "${HOME}/cache")));
        assertEquals(List.of("CACHE"), AcpConnection.environmentReferences(
                Map.of("CACHE", "%USERPROFILE%/cache")));
        try {
            AcpConnection.validateEnvironment(Map.of("BAD=NAME", "value"));
            fail("Invalid names must be rejected");
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().contains("value"));
        }
        try {
            AcpConnection.validateEnvironment(Map.of("TOKEN", "secret\0value"));
            fail("NUL values must be rejected");
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().contains("secret"));
        }
    }

    public void testConstructorRejectsNull() {
        try {
            new AcpConnection(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testGetDescriptor() {
        AcpAgentDescriptor desc = createDescriptor();
        AcpConnection conn = new AcpConnection(desc);
        assertSame(desc, conn.getDescriptor());
    }

    public void testInitiallyNotConnected() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        assertFalse(conn.isConnected());
    }

    public void testCloseOnDisconnectedIsHarmless() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.close();
        assertFalse(conn.isConnected());
    }

    public void testAddListenerRejectsNull() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.addSessionListener(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testRemoveListenerAcceptsUnknown() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        AcpSessionListener listener = new AcpSessionListener() {};
        conn.removeSessionListener(listener);
    }

    public void testSessionCredentialsAreRevokedWhenTheirLastOwnerGoesAway() throws Exception {
        List<String> revokedTokens = new ArrayList<>();
        AcpSchema.McpServer shared = new AcpSchema.McpServerHttp("test", "http://localhost",
                List.of(new AcpSchema.HttpHeader("Authorization", "Bearer shared")));
        AcpSchema.McpServer filtered = new AcpSchema.McpServerSse("test-sse", "http://localhost",
                List.of(new AcpSchema.HttpHeader("Authorization", "Bearer filtered")));
        AcpSchema.McpServer failed = new AcpSchema.McpServerHttp("failed", "http://localhost",
                List.of(new AcpSchema.HttpHeader("Authorization", "Bearer failed")));
        MockLookup.setInstances(new AcpMcpServerProvider() {
            @Override
            public List<AcpSchema.McpServer> getMcpServers() {
                return List.of(shared);
            }

            @Override
            public void releaseBearerToken(String token) {
                revokedTokens.add(token);
            }
        });
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        AcpAgentDescriptor descriptor = new AcpAgentDescriptor.Builder("test-agent",
                AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of(java, "-cp", System.getProperty("java.class.path"), TestAgent.class.getName()))
                .build();
        AcpConnection connection = new AcpConnection(descriptor);
        try {
            connection.connect();
            String cwd = getWorkDir().getAbsolutePath();
            String first = connection.newSession(cwd, List.of(), List.of(shared, filtered));
            String second = connection.newSession(cwd);
            assertEquals(List.of("filtered"), revokedTokens);
            assertTerminalDenied(connection, first);
            connection.setTerminalAuthorizer(request -> {
                fail("Unknown sessions must be rejected before asking for consent");
                return true;
            });
            assertTerminalDenied(connection, "unknown");
            connection.setTerminalAuthorizer(request -> false);
            assertTerminalDenied(connection, first);

            try {
                connection.newSession(Path.of(cwd, "fail").toString(), List.of(), List.of(failed, shared));
                fail("The test agent must reject this session");
            } catch (IOException expected) {
                assertEquals(List.of("filtered", "failed"), revokedTokens);
            }
            connection.deleteSession(first);
            assertEquals(List.of("filtered", "failed"), revokedTokens);
            connection.deleteSession(second);
            assertEquals(List.of("filtered", "failed", "shared"), revokedTokens);
        } finally {
            connection.close();
            MockLookup.setInstances();
        }
    }

    public void testAuthenticationBeforeRetryingSession() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        AcpAgentDescriptor descriptor = new AcpAgentDescriptor.Builder("auth-test",
                AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of(java, "-cp", System.getProperty("java.class.path"), TestAgent.class.getName()))
                .build();
        AcpConnection connection = new AcpConnection(descriptor);
        try {
            connection.connect();
            for (String invalid : Arrays.asList(null, "relative")) {
                try {
                    connection.newSession(invalid);
                    fail("Session creation requires an absolute working directory");
                } catch (IOException expected) {
                    assertEquals("An absolute session working directory is required", expected.getMessage());
                }
            }
            assertEquals("login", connection.getAuthenticationMethods().get(0).id());
            String cwd = Path.of(getWorkDir().getAbsolutePath(), "auth").toString();
            try {
                connection.newSession(cwd);
                fail("Authentication should be required");
            } catch (IOException expected) {
                assertTrue(expected.getCause() instanceof com.agentclientprotocol.sdk.spec.AcpError);
                assertEquals(AcpErrorCodes.AUTHENTICATION_REQUIRED,
                        ((com.agentclientprotocol.sdk.spec.AcpError) expected.getCause()).getCode());
            }
            connection.authenticate("login");
            assertNotNull(connection.newSession(cwd));
        } finally {
            connection.close();
        }
    }

    private static void assertTerminalDenied(AcpConnection connection, String sessionId) {
        try {
            connection.createAuthorizedTerminal(new AcpSchema.CreateTerminalRequest(
                    sessionId, "test-command-must-not-launch", List.of(), null, List.of(), null));
            fail("Terminal creation must require an authorized session");
        } catch (AcpProtocolException expected) {
            assertEquals(AcpErrorCodes.REQUEST_CANCELLED, expected.getCode());
        }
    }

    public static final class TestAgent {
        public static void main(String[] args) {
            var capabilities = AcpSchema.AgentCapabilities.builder().mcpHttp()
                    .sessionCapabilities(new AcpSchema.SessionCapabilities(null, null, null, Map.of(), null, null))
                    .build();
            java.util.concurrent.atomic.AtomicBoolean authenticated = new java.util.concurrent.atomic.AtomicBoolean();
            try (var agent = AcpAgent.sync(new StdioAcpAgentTransport())
                    .initializeHandler(request -> new AcpSchema.InitializeResponse(1, capabilities,
                            List.of(new AcpSchema.AuthMethodAgent("login", "Sign in", "Test authentication"))))
                    .authenticateHandler(request -> {
                        authenticated.set(true);
                        return new AcpSchema.AuthenticateResponse();
                    })
                    .newSessionHandler(request -> {
                        if ("auth".equals(Path.of(request.cwd()).getFileName().toString()) && !authenticated.get()) {
                            throw new AcpProtocolException(AcpErrorCodes.AUTHENTICATION_REQUIRED, "Authentication required");
                        }
                        if ("fail".equals(Path.of(request.cwd()).getFileName().toString())) {
                            throw new AcpProtocolException(AcpErrorCodes.INVALID_PARAMS, "Rejected test session");
                        }
                        return AcpSchema.NewSessionResponse.withGeneratedId();
                    })
                    .promptHandler((request, context) -> {
                        String prompt = request.text();
                        if (prompt != null && prompt.startsWith("roundtrip:")) {
                            String path = prompt.substring("roundtrip:".length());
                            context.writeFile(path, context.readFile(path) + " from the client");
                        }
                        return AcpSchema.PromptResponse.endTurn();
                    })
                    .deleteSessionHandler(request -> new AcpSchema.DeleteSessionResponse())
                    .build()) {
                agent.run();
            }
        }
    }

    public void testSendPromptWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.sendPrompt("session-1", "hello");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testCancelWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.cancel("session-1");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testNewSessionWithoutConnectionThrows() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.newSession("/tmp");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Not connected"));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public void testAllowToolForSession() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.allowToolForSession("s1", "read_file");
        conn.allowToolForSession("s1", "write_to_file");
    }

    public void testAllowToolRejectsNullSession() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        try {
            conn.allowToolForSession(null, "read_file");
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testSetPermissionHandler() {
        AcpConnection conn = new AcpConnection(createDescriptor());
        conn.setPermissionHandler(req -> null);
        conn.setPermissionHandler(null);
    }

    public void testMcpServersFilteredByAgentCapabilities() {
        AcpSchema.McpServer stdio = new AcpSchema.McpServerStdio("stdio", "server", List.of(), List.of());
        AcpSchema.McpServer http = new AcpSchema.McpServerHttp("http", "http://localhost", List.of());
        AcpSchema.McpServer sse = new AcpSchema.McpServerSse("sse", "http://localhost", List.of());
        List<AcpSchema.McpServer> servers = List.of(stdio, http, sse);

        assertEquals(List.of(stdio), AcpConnection.supportedMcpServers(servers, false, false));
        assertEquals(List.of(stdio, http), AcpConnection.supportedMcpServers(servers, true, false));
        assertEquals(List.of(stdio, sse), AcpConnection.supportedMcpServers(servers, false, true));
        assertEquals(servers, AcpConnection.supportedMcpServers(servers, true, true));
    }

    public void testMcpDiscoveryContinuesAfterProviderFailureAndRemovesDuplicates() {
        AcpSchema.McpServer first = new AcpSchema.McpServerHttp("first", "http://127.0.0.1/mcp", List.of());
        AcpSchema.McpServer second = new AcpSchema.McpServerStdio("second", "server", List.of(), List.of());
        AcpMcpServerProvider failing = () -> { throw new IllegalStateException("failure"); };
        AcpMcpServerProvider provider = () -> List.of(first, second);
        AcpMcpServerProvider duplicate = () -> List.of(first);
        assertEquals(List.of(first, second), AcpConnection.discoverMcpServers(
                List.of(failing, provider, duplicate), null));
    }

    public void testMcpDiscoveryPreservesDifferentCredentials() {
        AcpSchema.McpServer first = new AcpSchema.McpServerHttp("server", "http://127.0.0.1/mcp",
                List.of(new AcpSchema.HttpHeader("Authorization", "Bearer first")));
        AcpSchema.McpServer second = new AcpSchema.McpServerHttp("server", "http://127.0.0.1/mcp",
                List.of(new AcpSchema.HttpHeader("Authorization", "Bearer second")));
        assertEquals(List.of(first, second), AcpConnection.discoverMcpServers(
                List.of(() -> List.of(first), () -> List.of(second)), null));
    }

    public void testWorkspaceFileAccess() throws Exception {
        clearWorkDir();
        Path root = getWorkDir().toPath().toRealPath();
        Path workspace = Files.createDirectory(root.resolve("workspace"));
        Path lines = Files.writeString(workspace.resolve("lines.txt"), "one\r\ntwo\nthree");
        Path resolved = AcpWorkspaceFiles.resolve(List.of(workspace), lines, false);
        Path outside = Files.writeString(root.resolve("outside.txt"), "outside");
        try {
            AcpWorkspaceFiles.resolve(List.of(workspace), outside, false);
            fail("Only session directories are authorized");
        } catch (AccessDeniedException expected) {
            assertEquals(outside.toString(), expected.getFile());
        }
        try (var directory = Files.newDirectoryStream(root)) {
            if (!(directory instanceof SecureDirectoryStream<?>)) {
                try {
                    AcpWorkspaceFiles.read(resolved, 1, null);
                    fail("The provider cannot securely open the file");
                } catch (AccessDeniedException expected) {
                    assertTrue(expected.getReason().contains("secure directory access"));
                }
                return;
            }
        }
        assertEquals("two\n", AcpWorkspaceFiles.read(resolved, 2, 1));
        assertEquals("two\nthree", AcpWorkspaceFiles.read(resolved, 2, null));
        assertEquals("", AcpWorkspaceFiles.read(resolved, 5, 1));
        Path newFile = workspace.resolve("new.txt");
        Path destination = AcpWorkspaceFiles.resolve(List.of(workspace), newFile, true);
        assertNull(AcpWorkspaceFiles.write(destination, "first"));
        assertEquals("first", AcpWorkspaceFiles.write(destination, "second"));
        assertEquals("second", Files.readString(newFile));

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var descriptor = new AcpAgentDescriptor.Builder("workspace-test", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of(java, "-cp", System.getProperty("java.class.path"), TestAgent.class.getName()))
                .build();
        AcpConnection connection = new AcpConnection(descriptor);
        try {
            connection.connect();
            String session = connection.newSession(workspace.toString());
            connection.sendPrompt(session, "roundtrip:" + newFile);
            assertEquals("second from the client", Files.readString(newFile));
        } finally {
            connection.close();
        }
    }
}
