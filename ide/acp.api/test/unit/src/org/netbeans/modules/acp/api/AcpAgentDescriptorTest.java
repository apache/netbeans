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
import java.util.Map;
import org.netbeans.junit.NbTestCase;

public class AcpAgentDescriptorTest extends NbTestCase {

    public AcpAgentDescriptorTest(String name) {
        super(name);
    }

    public void testStdioAgent() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "Claude", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("claude", "--chat"))
                .workingDirectory("/home/user/project")
                .environmentVariables(Map.of("LANG", "en_US.UTF-8"))
                .build();

        assertEquals("Claude", agent.getName());
        assertEquals(AcpAgentDescriptor.TransportType.STDIO, agent.getTransportType());
        assertEquals(List.of("claude", "--chat"), agent.getCommand());
        assertEquals("/home/user/project", agent.getWorkingDirectory());
        assertEquals(1, agent.getEnvironmentVariables().size());
        assertEquals("en_US.UTF-8", agent.getEnvironmentVariables().get("LANG"));
        assertNull(agent.getUrl());
    }

    public void testWebsocketAgent() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "Remote", AcpAgentDescriptor.TransportType.WEBSOCKET)
                .url("ws://localhost:8080")
                .build();

        assertEquals("Remote", agent.getName());
        assertEquals(AcpAgentDescriptor.TransportType.WEBSOCKET, agent.getTransportType());
        assertEquals("ws://localhost:8080", agent.getUrl());
        assertTrue(agent.getCommand().isEmpty());
    }

    public void testStdioRequiresCommand() {
        try {
            new AcpAgentDescriptor.Builder("Test", AcpAgentDescriptor.TransportType.STDIO)
                    .build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("command"));
        }
    }

    public void testWebsocketRequiresUrl() {
        try {
            new AcpAgentDescriptor.Builder("Test", AcpAgentDescriptor.TransportType.WEBSOCKET)
                    .build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("URL"));
        }
    }

    public void testWebsocketRejectsBlankUrl() {
        try {
            new AcpAgentDescriptor.Builder("Test", AcpAgentDescriptor.TransportType.WEBSOCKET)
                    .url("   ")
                    .build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("URL"));
        }
    }

    public void testNullNameThrows() {
        try {
            new AcpAgentDescriptor.Builder(null, AcpAgentDescriptor.TransportType.STDIO);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullTransportTypeThrows() {
        try {
            new AcpAgentDescriptor.Builder("Test", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullCommandThrows() {
        try {
            new AcpAgentDescriptor.Builder("Test", AcpAgentDescriptor.TransportType.STDIO)
                    .command(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullEnvVarsThrows() {
        try {
            new AcpAgentDescriptor.Builder("Test", AcpAgentDescriptor.TransportType.STDIO)
                    .environmentVariables(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testCommandIsImmutable() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "Test", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd"))
                .build();
        try {
            agent.getCommand().add("extra");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public void testEnvVarsAreImmutable() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "Test", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd"))
                .environmentVariables(Map.of("K", "V"))
                .build();
        try {
            agent.getEnvironmentVariables().put("K2", "V2");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public void testDefaultsForOptionalFields() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "Test", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd"))
                .build();

        assertNull(agent.getUrl());
        assertNull(agent.getWorkingDirectory());
        assertTrue(agent.getEnvironmentVariables().isEmpty());
    }

    public void testEqualityByName() {
        AcpAgentDescriptor a = new AcpAgentDescriptor.Builder(
                "Same", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd1"))
                .build();
        AcpAgentDescriptor b = new AcpAgentDescriptor.Builder(
                "Same", AcpAgentDescriptor.TransportType.WEBSOCKET)
                .url("ws://localhost")
                .build();
        AcpAgentDescriptor c = new AcpAgentDescriptor.Builder(
                "Different", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd2"))
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    public void testToString() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "MyAgent", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("agent"))
                .build();
        String str = agent.toString();
        assertTrue(str.contains("MyAgent"));
        assertTrue(str.contains("STDIO"));
    }

    public void testTransportTypeValues() {
        AcpAgentDescriptor.TransportType[] values = AcpAgentDescriptor.TransportType.values();
        assertEquals(2, values.length);
        assertEquals(AcpAgentDescriptor.TransportType.STDIO, AcpAgentDescriptor.TransportType.valueOf("STDIO"));
        assertEquals(AcpAgentDescriptor.TransportType.WEBSOCKET, AcpAgentDescriptor.TransportType.valueOf("WEBSOCKET"));
    }
}
