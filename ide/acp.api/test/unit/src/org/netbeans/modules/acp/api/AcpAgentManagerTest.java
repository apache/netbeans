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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;

public class AcpAgentManagerTest extends NbTestCase {

    private AcpAgentManager manager;

    public AcpAgentManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        manager = new AcpAgentManager();
    }

    public void testInitiallyEmpty() {
        assertTrue(manager.getAgents().isEmpty());
    }

    public void testAddAgent() {
        AcpAgentDescriptor agent = stdioAgent("Agent1");
        manager.addAgent(agent);

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(1, agents.size());
        assertEquals("Agent1", agents.get(0).getName());
    }

    public void testAddDuplicateIsIgnored() {
        AcpAgentDescriptor agent1 = stdioAgent("Agent1");
        AcpAgentDescriptor agent1Copy = stdioAgent("Agent1");

        manager.addAgent(agent1);
        manager.addAgent(agent1Copy);

        assertEquals(1, manager.getAgents().size());
    }

    public void testAddMultipleAgents() {
        manager.addAgent(stdioAgent("Alpha"));
        manager.addAgent(stdioAgent("Beta"));
        manager.addAgent(stdioAgent("Gamma"));

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(3, agents.size());
        assertEquals("Alpha", agents.get(0).getName());
        assertEquals("Beta", agents.get(1).getName());
        assertEquals("Gamma", agents.get(2).getName());
    }

    public void testRemoveAgent() {
        manager.addAgent(stdioAgent("Agent1"));
        manager.addAgent(stdioAgent("Agent2"));

        manager.removeAgent("Agent1");

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(1, agents.size());
        assertEquals("Agent2", agents.get(0).getName());
    }

    public void testRemoveNonexistent() {
        manager.addAgent(stdioAgent("Agent1"));
        manager.removeAgent("NoSuchAgent");
        assertEquals(1, manager.getAgents().size());
    }

    public void testUpdateExistingAgent() {
        AcpAgentDescriptor original = new AcpAgentDescriptor.Builder(
                "Agent1", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("old-cmd"))
                .build();
        manager.addAgent(original);

        AcpAgentDescriptor updated = new AcpAgentDescriptor.Builder(
                "Agent1", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("new-cmd"))
                .workingDirectory("/new/path")
                .build();
        manager.updateAgent(updated);

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(1, agents.size());
        assertEquals(List.of("new-cmd"), agents.get(0).getCommand());
        assertEquals("/new/path", agents.get(0).getWorkingDirectory());
    }

    public void testUpdateAddsIfNotFound() {
        AcpAgentDescriptor agent = stdioAgent("NewAgent");
        manager.updateAgent(agent);

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(1, agents.size());
        assertEquals("NewAgent", agents.get(0).getName());
    }

    public void testAddNullThrows() {
        try {
            manager.addAgent(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testRemoveNullThrows() {
        try {
            manager.removeAgent(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testUpdateNullThrows() {
        try {
            manager.updateAgent(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testGetAgentsReturnsUnmodifiable() {
        manager.addAgent(stdioAgent("Agent1"));

        try {
            manager.getAgents().add(stdioAgent("Sneaky"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public void testPropertyChangeOnAdd() {
        TestPCL listener = new TestPCL();
        manager.addPropertyChangeListener(listener);

        manager.addAgent(stdioAgent("Agent1"));

        assertEquals(1, listener.events.size());
        assertEquals(AcpAgentManager.PROP_AGENTS, listener.events.get(0).getPropertyName());
    }

    public void testPropertyChangeOnRemove() {
        manager.addAgent(stdioAgent("Agent1"));

        TestPCL listener = new TestPCL();
        manager.addPropertyChangeListener(listener);
        manager.removeAgent("Agent1");

        assertEquals(1, listener.events.size());
        assertEquals(AcpAgentManager.PROP_AGENTS, listener.events.get(0).getPropertyName());
    }

    public void testNoPropertyChangeOnRemoveNonexistent() {
        TestPCL listener = new TestPCL();
        manager.addPropertyChangeListener(listener);
        manager.removeAgent("NoSuch");

        assertTrue(listener.events.isEmpty());
    }

    public void testPropertyChangeOnUpdateNewAgent() {
        manager.addAgent(stdioAgent("Agent1"));

        TestPCL listener = new TestPCL();
        manager.addPropertyChangeListener(listener);
        manager.updateAgent(stdioAgent("NewAgent"));

        assertEquals(1, listener.events.size());
        assertEquals(AcpAgentManager.PROP_AGENTS, listener.events.get(0).getPropertyName());
    }

    public void testUpdatePersistsEvenWhenListEquality() {
        manager.addAgent(stdioAgent("Agent1"));

        AcpAgentDescriptor updated = new AcpAgentDescriptor.Builder(
                "Agent1", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("new-cmd"))
                .build();
        manager.updateAgent(updated);

        AcpAgentManager freshManager = new AcpAgentManager();
        assertEquals(List.of("new-cmd"), freshManager.getAgents().get(0).getCommand());
    }

    public void testRemovePropertyChangeListener() {
        TestPCL listener = new TestPCL();
        manager.addPropertyChangeListener(listener);
        manager.removePropertyChangeListener(listener);

        manager.addAgent(stdioAgent("Agent1"));
        assertTrue(listener.events.isEmpty());
    }

    public void testPersistenceWithEnvironmentVariables() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "EnvAgent", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("agent", "--verbose"))
                .workingDirectory("/work")
                .environmentVariables(Map.of("KEY1", "val1", "KEY2", "val2"))
                .build();

        manager.addAgent(agent);

        AcpAgentManager freshManager = new AcpAgentManager();
        List<AcpAgentDescriptor> agents = freshManager.getAgents();
        assertEquals(1, agents.size());

        AcpAgentDescriptor loaded = agents.get(0);
        assertEquals("EnvAgent", loaded.getName());
        assertEquals(AcpAgentDescriptor.TransportType.STDIO, loaded.getTransportType());
        assertEquals(List.of("agent", "--verbose"), loaded.getCommand());
        assertEquals("/work", loaded.getWorkingDirectory());
        assertEquals(2, loaded.getEnvironmentVariables().size());
        assertEquals("val1", loaded.getEnvironmentVariables().get("KEY1"));
        assertEquals("val2", loaded.getEnvironmentVariables().get("KEY2"));
    }

    public void testPersistenceWebsocket() {
        AcpAgentDescriptor agent = new AcpAgentDescriptor.Builder(
                "WsAgent", AcpAgentDescriptor.TransportType.WEBSOCKET)
                .url("ws://example.com/agent")
                .build();

        manager.addAgent(agent);

        AcpAgentManager freshManager = new AcpAgentManager();
        List<AcpAgentDescriptor> agents = freshManager.getAgents();
        assertEquals(1, agents.size());
        assertEquals("WsAgent", agents.get(0).getName());
        assertEquals(AcpAgentDescriptor.TransportType.WEBSOCKET, agents.get(0).getTransportType());
        assertEquals("ws://example.com/agent", agents.get(0).getUrl());
    }

    public void testRemoveCleansUpPreferences() {
        manager.addAgent(stdioAgent("A"));
        manager.addAgent(stdioAgent("B"));
        manager.removeAgent("A");

        AcpAgentManager freshManager = new AcpAgentManager();
        List<AcpAgentDescriptor> agents = freshManager.getAgents();
        assertEquals(1, agents.size());
        assertEquals("B", agents.get(0).getName());
    }

    public void testUpdatePreservesOrder() {
        manager.addAgent(stdioAgent("A"));
        manager.addAgent(stdioAgent("B"));
        manager.addAgent(stdioAgent("C"));

        AcpAgentDescriptor updatedB = new AcpAgentDescriptor.Builder(
                "B", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("new-b"))
                .build();
        manager.updateAgent(updatedB);

        List<AcpAgentDescriptor> agents = manager.getAgents();
        assertEquals(3, agents.size());
        assertEquals("A", agents.get(0).getName());
        assertEquals("B", agents.get(1).getName());
        assertEquals("C", agents.get(2).getName());
        assertEquals(List.of("new-b"), agents.get(1).getCommand());
    }

    private static AcpAgentDescriptor stdioAgent(String name) {
        return new AcpAgentDescriptor.Builder(name, AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of("cmd"))
                .build();
    }

    private static final class TestPCL implements PropertyChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
    }
}
