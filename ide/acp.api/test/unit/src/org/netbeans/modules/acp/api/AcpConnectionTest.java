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

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.util.List;
import org.netbeans.junit.NbTestCase;

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
}
