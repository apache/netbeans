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
package org.netbeans.modules.acp.ui.chat;

import com.agentclientprotocol.sdk.agent.AcpAgent;
import com.agentclientprotocol.sdk.agent.transport.StdioAcpAgentTransport;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.awt.Dialog;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentManager;
import org.netbeans.modules.acp.api.AcpConnectionManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.test.MockLookup;

public class AcpPendingConnectionTest extends TestCase {

    public void testRemovingAgentDuringHandshakeClosesCompletedConnection() throws Exception {
        Path directory = Files.createTempDirectory("acp-pending-test");
        Path started = directory.resolve("started");
        Path proceed = directory.resolve("proceed");
        String name = "pending-" + UUID.randomUUID();
        AcpConnectionManager connections = new AcpConnectionManager();
        MockLookup.setInstances(connections, new DialogDisplayer() {
            @Override
            public Object notify(NotifyDescriptor descriptor) {
                return NotifyDescriptor.YES_OPTION;
            }

            @Override
            public Dialog createDialog(DialogDescriptor descriptor) {
                throw new UnsupportedOperationException();
            }
        });
        AcpAgentDescriptor descriptor = new AcpAgentDescriptor.Builder(name, AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-cp", System.getProperty("java.class.path"), DelayedAgent.class.getName(),
                        started.toString(), proceed.toString()))
                .build();
        AcpAgentManager manager = AcpAgentManager.getDefault();
        AcpChatTopComponent[] chat = new AcpChatTopComponent[1];
        try {
            manager.addAgent(descriptor);
            SwingUtilities.invokeAndWait(() -> {
                chat[0] = new AcpChatTopComponent();
                JComboBox<?> selector = field(chat[0], "agentSelector", JComboBox.class);
                selector.setSelectedItem(name);
                field(chat[0], "connectButton", JButton.class).doClick();
            });
            await(() -> Files.exists(started), "Agent did not start its handshake");
            SwingUtilities.invokeAndWait(() -> {
                try {
                    var remove = AcpChatTopComponent.class.getDeclaredMethod("removeSelectedAgent");
                    remove.setAccessible(true);
                    remove.invoke(chat[0]);
                } catch (ReflectiveOperationException ex) {
                    throw new AssertionError(ex);
                }
            });
            assertFalse(manager.getAgents().stream().anyMatch(agent -> name.equals(agent.getName())));
            Files.createFile(proceed);
            await(() -> {
                boolean[] ready = new boolean[1];
                try {
                    SwingUtilities.invokeAndWait(() -> ready[0] =
                            field(chat[0], "connectButton", JButton.class).isEnabled());
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
                return ready[0];
            }, "Connection attempt did not finish");
            SwingUtilities.invokeAndWait(() -> assertNull(field(chat[0], "currentConnection", Object.class)));
            assertTrue(connections.getActiveConnections().stream().noneMatch(connection -> connection.isConnected()));
        } finally {
            Files.writeString(proceed, "release");
            connections.disconnect(descriptor);
            manager.removeAgent(name);
            MockLookup.setInstances();
        }
    }

    private static void await(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(message, condition.getAsBoolean());
    }

    private static <T> T field(Object object, String name, Class<T> type) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(object));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    public static final class DelayedAgent {
        public static void main(String[] args) {
            try (var agent = AcpAgent.sync(new StdioAcpAgentTransport())
                    .initializeHandler(request -> {
                        try {
                            Files.createFile(Path.of(args[0]));
                            while (!Files.exists(Path.of(args[1]))) {
                                Thread.sleep(20);
                            }
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                        return AcpSchema.InitializeResponse.ok();
                    })
                    .newSessionHandler(request -> AcpSchema.NewSessionResponse.withGeneratedId())
                    .promptHandler((request, context) -> AcpSchema.PromptResponse.endTurn())
                    .build()) {
                agent.run();
            }
        }
    }
}
