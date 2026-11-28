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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpConnectionManager;
import org.netbeans.modules.acp.api.AcpSessionListener;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

public class AcpPendingConnectionTest extends TestCase {

    public void testRemovingAgentDuringHandshakeClosesCompletedConnection() throws Exception {
        Path directory = Files.createTempDirectory("acp-pending-test");
        Path started = directory.resolve("started");
        Path proceed = directory.resolve("proceed");
        AcpAgentDescriptor descriptor = testAgent(started, proceed);
        AcpConnectionManager connections = new AcpConnectionManager();
        MockLookup.setInstances(connections);
        CountDownLatch settled = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AcpChatSession session = new AcpChatSession();
        try {
            SwingUtilities.invokeAndWait(() -> session.connect(descriptor, AcpPendingConnectionTest::listener,
                    () -> error.set(new AssertionError("Removed agent became active")), error::set, settled::countDown));
            await(() -> Files.exists(started), "Agent did not start its handshake");
            SwingUtilities.invokeAndWait(() -> assertTrue(session.invalidateConnectionAttempt(descriptor.getName())));
            Files.createFile(proceed);
            assertTrue("Connection attempt did not settle", settled.await(15, TimeUnit.SECONDS));
            assertNull(error.get());
            SwingUtilities.invokeAndWait(() -> assertNull(session.connection()));
            assertTrue(connections.getActiveConnections().stream().noneMatch(AcpConnection::isConnected));
        } finally {
            Files.writeString(proceed, "release");
            connections.disconnect(descriptor);
            MockLookup.setInstances();
            Files.deleteIfExists(started);
            Files.deleteIfExists(proceed);
            Files.deleteIfExists(directory);
        }
    }

    public void testSessionUpdatesReplayAfterPresentationAndOnlyForTheSelectedSession() throws Exception {
        Path directory = Files.createTempDirectory("acp-session-test");
        Path started = directory.resolve("started");
        Path proceed = Files.createFile(directory.resolve("proceed"));
        AcpAgentDescriptor descriptor = testAgent(started, proceed);
        AcpConnectionManager connections = new AcpConnectionManager();
        MockLookup.setInstances(connections);
        CountDownLatch settled = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AcpChatSession session = new AcpChatSession();
        try {
            SwingUtilities.invokeAndWait(() -> session.connect(descriptor, AcpPendingConnectionTest::listener,
                    () -> {}, error::set, settled::countDown));
            assertTrue(settled.await(15, TimeUnit.SECONDS));
            assertNull(error.get());
            List<String> events = new ArrayList<>();
            SwingUtilities.invokeAndWait(() -> {
                AcpChatSession.Transition transition = session.beginTransition("chosen");
                session.dispatch(session.connection(), "other", () -> events.add("wrong session"));
                session.dispatch(session.connection(), "chosen", () -> events.add("replayed"));
                SwingUtilities.invokeLater(() -> {
                    session.finishTransition(transition, "chosen", "project", () -> events.add("ready"));
                    session.dispatch(session.connection(), "chosen", () -> events.add("live"));
                });
            });
            SwingUtilities.invokeAndWait(() -> {});
            SwingUtilities.invokeAndWait(() -> {
                assertEquals(List.of("ready", "replayed", "live"), events);
                assertTrue(session.canSend());
                assertEquals("project", session.beginPrompt());
                assertTrue(session.canCancel());
                session.beginCancellation();
                assertFalse(session.canCancel());
                session.promptFinished();
                assertFalse(session.canSend());
                session.finishCancellation();
                assertTrue(session.canSend());
                session.disconnect(closed::countDown);
            });
            assertTrue(closed.await(15, TimeUnit.SECONDS));
        } finally {
            connections.disconnect(descriptor);
            MockLookup.setInstances();
            Files.deleteIfExists(started);
            Files.deleteIfExists(proceed);
            Files.deleteIfExists(directory);
        }
    }

    public void testBlockedPromptLeavesRequestAndControlWorkersAvailable() throws Exception {
        Path directory = Files.createTempDirectory("acp-blocked-prompt-test");
        Path started = directory.resolve("started");
        Path proceed = Files.createFile(directory.resolve("proceed"));
        Path promptStarted = directory.resolve("prompt-started");
        Path cancelled = directory.resolve("cancelled");
        Path release = directory.resolve("release");
        AcpAgentDescriptor descriptor = testAgent(started, proceed);
        AcpConnectionManager connections = new AcpConnectionManager();
        MockLookup.setInstances(connections);
        RequestProcessor requests = new RequestProcessor("Test ACP requests", 1);
        RequestProcessor prompts = new RequestProcessor("Test ACP prompts", 1);
        RequestProcessor control = new RequestProcessor("Test ACP control", 1);
        AcpChatSession session = new AcpChatSession(requests, prompts, control);
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        try {
            SwingUtilities.invokeAndWait(() -> session.connect(descriptor, AcpPendingConnectionTest::listener,
                    () -> {}, error::set, connected::countDown));
            assertTrue(connected.await(15, TimeUnit.SECONDS));
            assertNull(error.get());

            SwingUtilities.invokeAndWait(() -> {
                AcpChatSession.Transition transition = session.beginTransition(null);
                session.createSession(transition, directory.toAbsolutePath().toString(), List.of(), () -> null,
                        opened -> session.finishTransition(transition, opened.id(), null, ready::countDown),
                        ready::countDown, ex -> {
                            error.set(ex);
                            ready.countDown();
                        });
            });
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            assertNull(error.get());
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(session.canSend());
                session.sendPrompt("block", "", () -> {}, error::set);
            });
            await(() -> Files.exists(promptStarted), "Agent did not start the blocking prompt");

            // A connection or session request can still use the request worker.
            CountDownLatch requestRan = new CountDownLatch(1);
            CountDownLatch releaseWorker = new CountDownLatch(1);
            requests.execute(() -> {
                requestRan.countDown();
                try {
                    releaseWorker.await(15, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                assertTrue("The prompt blocked the request worker", requestRan.await(5, TimeUnit.SECONDS));
                SwingUtilities.invokeAndWait(() -> session.cancelPrompt(() -> {}));
                await(() -> Files.exists(cancelled), "Cancellation waited for a busy worker");
                SwingUtilities.invokeAndWait(() -> session.disconnect(closed::countDown));
                assertTrue("Close waited for a busy worker", closed.await(10, TimeUnit.SECONDS));
            } finally {
                releaseWorker.countDown();
            }
        } finally {
            Files.writeString(release, "release");
            connections.disconnect(descriptor);
            MockLookup.setInstances();
            requests.shutdownNow();
            prompts.shutdownNow();
            control.shutdownNow();
            Files.deleteIfExists(started);
            Files.deleteIfExists(proceed);
            Files.deleteIfExists(promptStarted);
            Files.deleteIfExists(cancelled);
            Files.deleteIfExists(release);
            Files.deleteIfExists(directory);
        }
    }

    public void testErrorMessageFallsBackToCauseAndLocalizedDefault() {
        assertEquals("Transport closed", AcpChatSession.errorMessage(
                new IOException(" ", new IOException("Transport closed"))));
        assertEquals(Bundle.MSG_AgentOperationFailed(), AcpChatSession.errorMessage(new IOException()));
    }

    private static AcpSessionListener listener(AcpConnection connection) {
        return new AcpSessionListener() { };
    }

    private static AcpAgentDescriptor testAgent(Path started, Path proceed) {
        return new AcpAgentDescriptor.Builder("test-agent", AcpAgentDescriptor.TransportType.STDIO)
                .command(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-cp", System.getProperty("java.class.path"), DelayedAgent.class.getName(),
                        started.toString(), proceed.toString()))
                .build();
    }

    private static void await(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(message, condition.getAsBoolean());
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
                    .promptHandler((request, context) -> {
                        if ("block".equals(request.text())) {
                            Path directory = Path.of(args[0]).getParent();
                            context.onCancel(() -> {
                                try {
                                    Files.createFile(directory.resolve("cancelled"));
                                } catch (IOException ex) {
                                    throw new IllegalStateException(ex);
                                }
                            });
                            try {
                                Files.createFile(directory.resolve("prompt-started"));
                                await(() -> Files.exists(directory.resolve("release")), "Test did not release the prompt");
                            } catch (Exception ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                        return AcpSchema.PromptResponse.endTurn();
                    })
                    .build()) {
                agent.run();
            }
        }
    }
}
