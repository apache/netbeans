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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpConnectionManager;
import org.netbeans.modules.acp.api.AcpSessionInfo;
import org.netbeans.modules.acp.api.AcpSessionListener;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider.ProjectContext;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Owns the active connection and session. State changes run on the EDT. */
@NbBundle.Messages({
    "MSG_AuthenticationFailed=Authentication failed: {0}",
    "MSG_AgentOperationFailed=The agent operation failed."
})
final class AcpChatSession {

    private static final Logger LOG = Logger.getLogger(AcpChatSession.class.getName());

    private static final RequestProcessor REQUESTS = new RequestProcessor("ACP requests", 2);
    private static final RequestProcessor PROMPTS = new RequestProcessor("ACP prompts", 2);
    private static final RequestProcessor CONTROL = new RequestProcessor("ACP cancellation and close", 2);

    private final Executor worker;
    private final Executor promptWorker;
    private final Executor controlWorker;
    private AcpConnection connection;
    private AcpSessionListener listener;
    private ConnectionAttempt connectionAttempt;
    private String sessionId;
    private Transition transition;
    private String projectContext;
    private boolean prompting;
    private boolean cancelling;

    AcpChatSession() {
        this(REQUESTS, PROMPTS, CONTROL);
    }

    AcpChatSession(Executor worker, Executor promptWorker, Executor controlWorker) {
        this.worker = worker;
        this.promptWorker = promptWorker;
        this.controlWorker = controlWorker;
    }

    static String errorMessage(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            String message = cause.getLocalizedMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        return Bundle.MSG_AgentOperationFailed();
    }

    AcpConnection connection() {
        return connection;
    }

    String sessionId() {
        return sessionId;
    }

    boolean invalidateConnectionAttempt(String agentName) {
        if (connectionAttempt != null && agentName.equals(connectionAttempt.descriptor().getName())) {
            invalidateConnectionAttempt();
            return true;
        }
        return false;
    }

    void invalidateConnectionAttempt() {
        connectionAttempt = null;
    }

    void connect(AcpAgentDescriptor descriptor, Function<AcpConnection, AcpSessionListener> configure,
            Runnable connected, Consumer<Exception> failed, Runnable settled) {
        ConnectionAttempt attempt = new ConnectionAttempt(descriptor);
        connectionAttempt = attempt;
        worker.execute(() -> {
            try {
                AcpConnection opened = AcpConnectionManager.getDefault().connect(descriptor);
                SwingUtilities.invokeLater(() -> {
                    if (connectionAttempt != attempt) {
                        close(opened, settled);
                        return;
                    }
                    connectionAttempt = null;
                    connection = opened;
                    listener = configure.apply(opened);
                    opened.addSessionListener(listener);
                    connected.run();
                    settled.run();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    if (connectionAttempt == attempt) {
                        connectionAttempt = null;
                        failed.accept(ex);
                    }
                    settled.run();
                });
            }
        });
    }

    void disconnect(Runnable settled) {
        AcpConnection previous = connection;
        if (previous == null) {
            return;
        }
        previous.removeSessionListener(listener);
        previous.setPermissionHandler(null);
        previous.setElicitationHandler(null);
        previous.setTerminalAuthorizer(null);
        connection = null;
        listener = null;
        sessionId = null;
        transition = null;
        projectContext = null;
        prompting = false;
        cancelling = false;
        close(previous, settled);
    }

    private void close(AcpConnection previous, Runnable settled) {
        controlWorker.execute(() -> {
            try {
                previous.close();
            } finally {
                SwingUtilities.invokeLater(settled);
            }
        });
    }

    boolean isIdle() {
        return connection != null && connection.isConnected()
                && transition == null && !prompting && !cancelling;
    }

    boolean canSend() {
        return isIdle() && sessionId != null;
    }

    boolean canCancel() {
        return connection != null && connection.isConnected() && sessionId != null && prompting && !cancelling;
    }

    boolean isCurrent(AcpConnection owner, String id) {
        return owner == connection && id != null && id.equals(sessionId) && transition == null;
    }

    void dispatch(AcpConnection owner, String id, Runnable update) {
        SwingUtilities.invokeLater(() -> {
            if (connection != owner || id == null) {
                return;
            }
            if (transition != null) {
                transition.addUpdate(id, sessionId, update);
            } else if (id.equals(sessionId)) {
                update.run();
            }
        });
    }

    Transition beginTransition(String expectedId) {
        transition = new Transition(expectedId);
        return transition;
    }

    private boolean isCurrent(Transition expected) {
        return expected != null && transition == expected;
    }

    private boolean abandonTransition(Transition expected) {
        if (!isCurrent(expected)) {
            return false;
        }
        transition = null;
        return true;
    }

    void finishTransition(Transition expected, String id, String context, Runnable present) {
        if (abandonTransition(expected)) {
            sessionId = id;
            projectContext = context;
            present.run();
            expected.updates.getOrDefault(id, List.of()).forEach(Runnable::run);
        }
    }

    void createSession(Transition expected, String directory, List<String> additionalDirs,
            Supplier<ProjectContext> projectContext, Consumer<OpenedSession> ready,
            Runnable declined, Consumer<Exception> failed) {
        AcpConnection owner = connection;
        openSession(expected, () -> {
            ProjectContext context = projectContext.get();
            try {
                return new OpenedSession(owner.newSession(directory, additionalDirs), context);
            } catch (IOException ex) {
                if (!AcpAuthenticationDialog.isAuthenticationRequired(ex)) {
                    throw ex;
                }
            }
            String method = AcpAuthenticationDialog.chooseMethod(owner, () -> isCurrent(expected));
            if (method == null) {
                return null;
            }
            try {
                owner.authenticate(method);
            } catch (IOException ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                throw new IOException(Bundle.MSG_AuthenticationFailed(errorMessage(cause)), ex);
            }
            return new OpenedSession(owner.newSession(directory, additionalDirs), context);
        }, ready, declined, failed);
    }

    void resumeSession(Transition expected, String directory, List<String> additionalDirs,
            Supplier<ProjectContext> projectContext, Consumer<OpenedSession> ready, Consumer<Exception> failed) {
        AcpConnection owner = connection;
        openSession(expected, () -> {
            ProjectContext context = projectContext.get();
            return new OpenedSession(owner.resumeSession(expected.expectedId, directory, additionalDirs), context);
        }, ready, () -> {}, failed);
    }

    private void openSession(Transition expected, Callable<OpenedSession> open,
            Consumer<OpenedSession> ready, Runnable declined, Consumer<Exception> failed) {
        worker.execute(() -> {
            try {
                OpenedSession opened = open.call();
                SwingUtilities.invokeLater(() -> {
                    if (opened != null) {
                        if (isCurrent(expected)) {
                            ready.accept(opened);
                        }
                    } else if (abandonTransition(expected)) {
                        declined.run();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    if (abandonTransition(expected)) {
                        failed.accept(ex);
                    }
                });
            }
        });
    }

    void listSessions(String directory, Consumer<List<AcpSessionInfo>> listed, Consumer<IOException> failed) {
        AcpConnection owner = connection;
        worker.execute(() -> {
            try {
                List<AcpSessionInfo> sessions = owner.listSessions(directory);
                SwingUtilities.invokeLater(() -> {
                    if (connection == owner && isIdle()) {
                        listed.accept(sessions);
                    }
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    if (connection == owner) {
                        failed.accept(ex);
                    }
                });
            }
        });
    }

    void deleteSession(String id, Consumer<Boolean> deleted, Consumer<IOException> failed) {
        AcpConnection owner = connection;
        worker.execute(() -> {
            try {
                owner.deleteSession(id);
                SwingUtilities.invokeLater(() -> {
                    if (connection == owner) {
                        deleted.accept(removeSession(id));
                    }
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    if (connection == owner) {
                        failed.accept(ex);
                    }
                });
            }
        });
    }

    record OpenedSession(String id, ProjectContext projectContext) { }

    private boolean removeSession(String id) {
        if (!id.equals(sessionId)) {
            return false;
        }
        sessionId = null;
        projectContext = null;
        return true;
    }

    String beginPrompt() {
        prompting = true;
        String context = projectContext;
        projectContext = null;
        return context;
    }

    void promptFinished() {
        prompting = false;
    }

    private void promptFailed(String context) {
        projectContext = context;
        promptFinished();
    }

    void beginCancellation() {
        cancelling = true;
    }

    void finishCancellation() {
        cancelling = false;
    }

    void sendPrompt(String text, String attachments, Runnable submitted, Consumer<Exception> failed) {
        AcpConnection owner = connection;
        String id = sessionId;
        String context = beginPrompt();
        String prompt = (context == null ? "" : context + "\n\n") + attachments + text;
        submitted.run();
        promptWorker.execute(() -> {
            try {
                owner.sendPrompt(id, prompt);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    if (isCurrent(owner, id)) {
                        promptFailed(context);
                        failed.accept(ex);
                    }
                });
            }
        });
    }

    void cancelPrompt(Runnable stateChanged) {
        AcpConnection owner = connection;
        String id = sessionId;
        beginCancellation();
        stateChanged.run();
        controlWorker.execute(() -> {
            try {
                owner.cancel(id);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to cancel prompt", ex);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    if (isCurrent(owner, id)) {
                        finishCancellation();
                        stateChanged.run();
                    }
                });
            }
        });
    }

    void setMode(String mode) {
        AcpConnection owner = connection;
        String id = sessionId;
        worker.execute(() -> {
            try {
                owner.setSessionMode(id, mode);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to set mode", ex);
            }
        });
    }

    private record ConnectionAttempt(AcpAgentDescriptor descriptor) { }

    static final class Transition {

        private final String expectedId;
        private final Map<String, List<Runnable>> updates = new HashMap<>();

        private Transition(String expectedId) {
            this.expectedId = expectedId;
        }

        boolean isResume() {
            return expectedId != null;
        }

        private void addUpdate(String id, String activeId, Runnable update) {
            if (expectedId != null ? expectedId.equals(id) : !id.equals(activeId)) {
                updates.computeIfAbsent(id, key -> new ArrayList<>()).add(update);
            }
        }
    }
}
