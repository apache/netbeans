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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentManager;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpConnectionManager;
import org.netbeans.modules.acp.api.AcpPlanEntry;
import org.netbeans.modules.acp.api.AcpSessionListener;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.netbeans.modules.acp.ui.AcpContextReceiver;
import org.netbeans.modules.acp.ui.AcpDiffViewer;
import org.netbeans.modules.acp.ui.AcpPermissionDialog;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "AcpChatTopComponent",
        iconBase = "org/netbeans/modules/acp/ui/resources/acp_chat.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 500)
@ActionID(category = "Window", id = "org.netbeans.modules.acp.ui.chat.AcpChatTopComponent")
@ActionReference(path = "Menu/Window", position = 7000)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_AcpChatAction",
        preferredID = "AcpChatTopComponent")
public final class AcpChatTopComponent extends TopComponent implements AcpContextReceiver {

    private static final Logger LOG = Logger.getLogger(AcpChatTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("ACP-Agent-RP", 2);
    private static final RequestProcessor CONTEXT_RP = new RequestProcessor("ACP-Context-RP", 1);
    private static final int DISPLAY_DELAY_MS = 50;
    private static final int RENDER_DELAY_MS = 200;

    private final JEditorPane chatPane;
    private final JTextArea inputArea;
    private final JButton sendButton;
    private final JButton cancelButton;
    private final JButton connectButton;
    private final JComboBox<String> agentSelector;
    private final JLabel statusLabel;
    private final StringBuilder chatHtml = new StringBuilder();
    private int renderedLength;
    private final Object bufferLock = new Object();
    private final StringBuilder currentMessageBuffer = new StringBuilder();
    private final StringBuilder currentThoughtBuffer = new StringBuilder();
    private final AtomicBoolean promptInProgress = new AtomicBoolean(false);
    private final Timer renderTimer;
    private final Timer displayTimer;

    private final Map<String, AcpToolCall.DiffContent> diffStore = new HashMap<>();
    private int diffCounter;
    private final AcpFileMentionSupport mentionSupport;
    private AcpConnection currentConnection;
    private String currentSessionId;

    private final AcpSessionListener sessionListener = new AcpSessionListener() {
        @Override
        public void onAgentMessage(String sessionId, String text) {
            synchronized (bufferLock) {
                currentMessageBuffer.append(text);
            }
            renderTimer.restart();
        }

        @Override
        public void onAgentThought(String sessionId, String text) {
            synchronized (bufferLock) {
                currentThoughtBuffer.append(text);
            }
            renderTimer.restart();
        }

        @Override
        public void onToolCall(String sessionId, AcpToolCall toolCall) {
            SwingUtilities.invokeLater(() -> appendToolCall(toolCall));
        }

        @Override
        public void onToolCallUpdate(String sessionId, AcpToolCall toolCall) {
            SwingUtilities.invokeLater(() -> appendToolCallUpdate(toolCall));
        }

        @Override
        public void onPlanUpdate(String sessionId, List<AcpPlanEntry> entries) {
            SwingUtilities.invokeLater(() -> appendPlan(entries));
        }

        @Override
        public void onPromptComplete(String sessionId, String stopReason) {
            SwingUtilities.invokeLater(() -> {
                flushMessageBuffers();
                promptInProgress.set(false);
                updateInputState();
                statusLabel.setText("Ready");
            });
        }

        @Override
        public void onUsageUpdate(String sessionId, long usedTokens, long totalTokens) {
            SwingUtilities.invokeLater(() ->
                statusLabel.setText(String.format("Tokens: %d / %d", usedTokens, totalTokens))
            );
        }

        @Override
        public void onFileWritten(String sessionId, String path,
                String oldContent, String newContent) {
            AcpToolCall.DiffContent diff = new AcpToolCall.DiffContent(
                    path,
                    oldContent != null ? oldContent : "",
                    newContent != null ? newContent : "");
            SwingUtilities.invokeLater(() -> {
                String diffId = "diff-" + (diffCounter++);
                diffStore.put(diffId, diff);
                String fileName = path;
                int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
                if (sep >= 0) {
                    fileName = path.substring(sep + 1);
                }
                String label = oldContent == null ? "Created" : "Modified";
                chatHtml.append("<div class='tool-call'>")
                        .append("&#x270F; <b>").append(label).append(": ")
                        .append(escapeHtml(fileName)).append("</b>")
                        .append(" <span class='tc-status-completed'>&#x2705; completed</span>")
                        .append("<br/><span class='tc-location'>")
                        .append(escapeHtml(path)).append("</span>");
                appendDiffPreview(diff);
                chatHtml.append("<br/><a class='diff-link' href='acp-diff:")
                        .append(diffId).append("'>View Full Diff</a>")
                        .append("</div>");
                updateChatDisplay();
            });
        }
    };

    public AcpChatTopComponent() {
        setName(NbBundle.getMessage(AcpChatTopComponent.class, "CTL_AcpChatTopComponent"));
        setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_AcpChatTopComponent"));
        setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        agentSelector = new JComboBox<>();
        agentSelector.setMaximumSize(new Dimension(200, 28));
        toolbar.add(new JLabel("Agent: "));
        toolbar.add(agentSelector);
        JButton addAgentButton = new JButton("+");
        addAgentButton.setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_AddAgent"));
        addAgentButton.addActionListener(e -> showAddAgentDialog());
        toolbar.add(addAgentButton);
        JButton editAgentButton = new JButton("...");
        editAgentButton.setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_EditAgent"));
        editAgentButton.addActionListener(e -> showEditAgentDialog());
        toolbar.add(editAgentButton);
        JButton removeAgentButton = new JButton("-");
        removeAgentButton.setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_RemoveAgent"));
        removeAgentButton.addActionListener(e -> removeSelectedAgent());
        toolbar.add(removeAgentButton);
        toolbar.addSeparator();
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> toggleConnection());
        toolbar.add(connectButton);
        JButton newSessionButton = new JButton("New Session");
        newSessionButton.addActionListener(e -> startNewSession());
        toolbar.add(newSessionButton);
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelPrompt());
        toolbar.add(cancelButton);
        toolbar.add(Box.createHorizontalGlue());
        statusLabel = new JLabel("Disconnected");
        toolbar.add(statusLabel);
        add(toolbar, BorderLayout.NORTH);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: sans-serif; font-size: 12px; margin: 8px; }");
        styleSheet.addRule(".user-msg { background-color: #BBDEFB; padding: 8px; margin: 6px 40px 6px 0; border-left: 4px solid #1976D2; }");
        styleSheet.addRule(".agent-msg { background-color: #E8E8E8; padding: 8px; margin: 6px 0 6px 40px; border-left: 4px solid #616161; }");
        styleSheet.addRule(".system-msg { background-color: #FFF9C4; padding: 6px; margin: 4px 0; border-left: 4px solid #F9A825; }");
        styleSheet.addRule(".thought { color: #666; font-style: italic; padding: 4px 8px; margin: 2px 0 2px 40px; background-color: #F5F5F5; }");
        styleSheet.addRule(".tool-call { background-color: #FFF3E0; padding: 6px; margin: 4px 0 4px 40px; border-left: 4px solid #FF9800; }");
        styleSheet.addRule(".plan { background-color: #E8F5E9; padding: 6px; margin: 4px 0 4px 40px; border-left: 4px solid #4CAF50; }");
        styleSheet.addRule(".user-label { color: #1565C0; font-weight: bold; }");
        styleSheet.addRule(".agent-label { color: #424242; font-weight: bold; }");
        styleSheet.addRule(".diff-add { color: #2E7D32; }");
        styleSheet.addRule(".diff-del { color: #C62828; }");
        styleSheet.addRule(".diff-link { color: #1565C0; }");
        styleSheet.addRule(".tc-kind { color: #E65100; font-weight: bold; }");
        styleSheet.addRule(".tc-status-completed { color: #2E7D32; }");
        styleSheet.addRule(".tc-status-failed { color: #C62828; }");
        styleSheet.addRule(".tc-status-progress { color: #F57F17; }");
        styleSheet.addRule(".tc-location { color: #5C6BC0; font-size: 11px; }");
        styleSheet.addRule("pre { background-color: #263238; color: #EEFFFF; padding: 8px; overflow-x: auto; }");
        styleSheet.addRule("code { font-family: monospace; }");

        chatPane = new JEditorPane();
        chatPane.setEditorKit(kit);
        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setText("<html><body></body></html>");
        chatPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getDescription();
                if (url != null && url.startsWith("acp-diff:")) {
                    String diffId = url.substring("acp-diff:".length());
                    AcpToolCall.DiffContent diff = diffStore.get(diffId);
                    if (diff != null) {
                        AcpDiffViewer.showDiff(diff);
                    }
                }
            }
        });
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "newline");
        inputArea.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputArea.getActionMap().put("newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputArea.insert("\n", inputArea.getCaretPosition());
            }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setEnabled(false);

        JPanel inputPanel = new JPanel(new BorderLayout(4, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(sendButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chatScroll, inputPanel);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(4);
        add(splitPane, BorderLayout.CENTER);

        renderTimer = new Timer(RENDER_DELAY_MS, e -> flushMessageBuffers());
        renderTimer.setRepeats(false);
        displayTimer = new Timer(DISPLAY_DELAY_MS, e -> renderChatDisplay());
        displayTimer.setRepeats(false);

        mentionSupport = new AcpFileMentionSupport(inputArea);
        inputArea.setTransferHandler(new AcpFileDropHandler(inputArea, mentionSupport));
        refreshAgentList();
    }

    @Override
    public void componentOpened() {
        refreshAgentList();
    }

    @Override
    public void componentClosed() {
        if (currentConnection != null && currentConnection.isConnected()) {
            RP.post(() -> {
                try {
                    currentConnection.close();
                } catch (Exception ex) {
                    // ignore on close
                }
            });
        }
    }

    @Override
    public void setContextForNextPrompt(String context) {
        if (context != null && !context.isEmpty()) {
            String existing = inputArea.getText().trim();
            if (existing.isEmpty()) {
                inputArea.setText(context + "\n");
            } else {
                inputArea.setText(context + "\n" + existing);
            }
            inputArea.setCaretPosition(inputArea.getDocument().getLength());
        }
        inputArea.requestFocusInWindow();
    }

    private void showAddAgentDialog() {
        AcpAgentConfigPanel panel = new AcpAgentConfigPanel();
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AcpChatTopComponent.class, "TTL_AddAgent"));
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            AcpAgentDescriptor descriptor = panel.buildDescriptor();
            if (descriptor != null) {
                AcpAgentManager.getDefault().addAgent(descriptor);
                refreshAgentList();
                agentSelector.setSelectedItem(descriptor.getName());
            }
        }
    }

    private void showEditAgentDialog() {
        String agentName = (String) agentSelector.getSelectedItem();
        if (agentName == null) {
            return;
        }
        AcpAgentDescriptor existing = AcpAgentManager.getDefault().getAgents().stream()
                .filter(a -> a.getName().equals(agentName))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            return;
        }
        AcpAgentConfigPanel panel = new AcpAgentConfigPanel(existing);
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AcpChatTopComponent.class, "TTL_EditAgent"));
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            AcpAgentDescriptor descriptor = panel.buildDescriptor();
            if (descriptor != null) {
                AcpAgentManager.getDefault().updateAgent(descriptor);
                refreshAgentList();
                agentSelector.setSelectedItem(descriptor.getName());
            }
        }
    }

    private void removeSelectedAgent() {
        String agentName = (String) agentSelector.getSelectedItem();
        if (agentName == null) {
            return;
        }
        NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(AcpChatTopComponent.class, "MSG_ConfirmRemoveAgent", agentName),
                NbBundle.getMessage(AcpChatTopComponent.class, "TTL_RemoveAgent"),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(confirm) == NotifyDescriptor.YES_OPTION) {
            if (currentConnection != null && currentConnection.isConnected()) {
                final AcpConnection connectionToClose = currentConnection;
                RP.post(() -> {
                    try {
                        connectionToClose.close();
                    } catch (Exception ex) {
                        // ignore
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (currentConnection == connectionToClose) {
                            currentConnection = null;
                            currentSessionId = null;
                            connectButton.setText("Connect");
                            statusLabel.setText("Disconnected");
                            updateInputState();
                        }
                    });
                });
            }
            AcpAgentManager.getDefault().removeAgent(agentName);
            refreshAgentList();
        }
    }

    private void refreshAgentList() {
        agentSelector.removeAllItems();
        List<AcpAgentDescriptor> agents = AcpAgentManager.getDefault().getAgents();
        for (AcpAgentDescriptor agent : agents) {
            agentSelector.addItem(agent.getName());
        }
    }

    private void toggleConnection() {
        if (currentConnection != null && currentConnection.isConnected()) {
            RP.post(() -> {
                try {
                    currentConnection.close();
                } catch (Exception ex) {
                    // ignore
                }
                SwingUtilities.invokeLater(() -> {
                    currentConnection = null;
                    currentSessionId = null;
                    connectButton.setText("Connect");
                    statusLabel.setText("Disconnected");
                    updateInputState();
                });
            });
        } else {
            String agentName = (String) agentSelector.getSelectedItem();
            if (agentName == null) {
                return;
            }
            AcpAgentDescriptor descriptor = AcpAgentManager.getDefault().getAgents().stream()
                    .filter(a -> a.getName().equals(agentName))
                    .findFirst()
                    .orElse(null);
            if (descriptor == null) {
                return;
            }
            statusLabel.setText("Connecting...");
            connectButton.setEnabled(false);
            RP.post(() -> {
                try {
                    AcpConnection connection = AcpConnectionManager.getDefault().connect(descriptor);
                    connection.addSessionListener(sessionListener);
                    connection.setPermissionHandler(req -> {
                        AcpPermissionDialog.PermissionResult pr =
                                AcpPermissionDialog.showPermissionRequest(req);
                        if (pr.allowToolForSession() != null) {
                            connection.allowToolForSession(req.sessionId(), pr.allowToolForSession());
                        }
                        return pr.response();
                    });
                    SwingUtilities.invokeLater(() -> {
                        currentConnection = connection;
                        connectButton.setText("Disconnect");
                        connectButton.setEnabled(true);
                        statusLabel.setText("Connected");
                        updateInputState();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: " + ex.getMessage());
                        connectButton.setEnabled(true);
                    });
                }
            });
        }
    }

    private void startNewSession() {
        if (currentConnection == null || !currentConnection.isConnected()) {
            return;
        }
        statusLabel.setText("Creating session...");
        RP.post(() -> {
            try {
                String sessionId = currentConnection.newSession(
                        getMainProjectDirectory(), getOpenProjectDirectories());
                SwingUtilities.invokeLater(() -> {
                    currentSessionId = sessionId;
                    chatHtml.setLength(0);
                    renderedLength = 0;
                    chatPane.setText("<html><body></body></html>");
                    statusLabel.setText("Ready");
                    updateInputState();
                });
                CONTEXT_RP.post(() -> sendProjectContext(sessionId));
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to create new session", ex);
                SwingUtilities.invokeLater(() -> {
                    appendSystemMessage("Error creating session: "
                            + escapeHtml(String.valueOf(ex.getMessage())));
                    statusLabel.setText("Error");
                });
            }
        });
    }

    private void sendProjectContext(String sessionId) {
        String projectContext = AcpProjectContextProvider.gatherProjectContext();
        if (projectContext != null && !projectContext.isEmpty() && currentConnection != null) {
            try {
                SwingUtilities.invokeLater(() -> appendSystemMessage(
                        "<i>Project context sent to agent.</i>"));
                currentConnection.sendPrompt(sessionId,
                        "[System context - do not respond to this, just acknowledge silently]\n\n"
                        + projectContext);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to send project context", ex);
            }
        }
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || currentSessionId == null || promptInProgress.get()) {
            return;
        }
        List<AcpFileMentionSupport.FileAttachment> files = mentionSupport.consumeAttachedFiles();
        String attachmentContext = mentionSupport.buildAttachmentContext(files);
        String fullPrompt = attachmentContext.isEmpty() ? text : attachmentContext + text;

        appendUserMessage(text);
        inputArea.setText("");
        promptInProgress.set(true);
        updateInputState();
        statusLabel.setText("Thinking...");

        RP.post(() -> {
            try {
                currentConnection.sendPrompt(currentSessionId, fullPrompt);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendSystemMessage("Error: " + escapeHtml(ex.getMessage()));
                    promptInProgress.set(false);
                    updateInputState();
                    statusLabel.setText("Error");
                });
            }
        });
    }

    private void cancelPrompt() {
        if (currentConnection != null && currentSessionId != null && promptInProgress.get()) {
            RP.post(() -> {
                try {
                    currentConnection.cancel(currentSessionId);
                    SwingUtilities.invokeLater(() -> {
                        appendSystemMessage("<i>Prompt cancelled.</i>");
                        promptInProgress.set(false);
                        updateInputState();
                        statusLabel.setText("Cancelled");
                    });
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Failed to cancel prompt", ex);
                }
            });
        }
    }

    private void updateInputState() {
        boolean canSend = currentConnection != null
                && currentConnection.isConnected()
                && currentSessionId != null
                && !promptInProgress.get();
        sendButton.setEnabled(canSend);
        inputArea.setEnabled(canSend);
        cancelButton.setEnabled(promptInProgress.get() && currentSessionId != null);
    }

    private void appendUserMessage(String text) {
        chatHtml.append("<div class='user-msg'><span class='user-label'>You:</span><br/>")
                .append(escapeHtml(text).replace("\n", "<br/>"))
                .append("</div>");
        updateChatDisplay();
    }

    private void appendSystemMessage(String html) {
        chatHtml.append("<div class='system-msg'>").append(html).append("</div>");
        updateChatDisplay();
    }

    private void flushMessageBuffers() {
        String thought;
        String message;
        synchronized (bufferLock) {
            thought = currentThoughtBuffer.toString();
            message = currentMessageBuffer.toString();
            currentThoughtBuffer.setLength(0);
            currentMessageBuffer.setLength(0);
        }
        boolean changed = false;
        if (!thought.isEmpty()) {
            chatHtml.append("<div class='thought'>")
                    .append(escapeHtml(thought))
                    .append("</div>");
            changed = true;
        }
        if (!message.isEmpty()) {
            chatHtml.append("<div class='agent-msg'><span class='agent-label'>Agent:</span><br/>")
                    .append(escapeHtml(message).replace("\n", "<br/>"))
                    .append("</div>");
            changed = true;
        }
        if (changed) {
            updateChatDisplay();
        }
    }

    private void appendToolCall(AcpToolCall toolCall) {
        renderToolCallCard(toolCall);
    }

    private void appendToolCallUpdate(AcpToolCall toolCall) {
        renderToolCallCard(toolCall);
    }

    private void renderToolCallCard(AcpToolCall toolCall) {
        String kindIcon = switch (toolCall.getKind()) {
            case "read" -> "&#x1F4C4;";
            case "edit", "write" -> "&#x270F;";
            case "execute" -> "&#x2699;";
            case "search" -> "&#x1F50D;";
            case "think" -> "&#x1F4AD;";
            case "delete" -> "&#x1F5D1;";
            case "fetch" -> "&#x1F310;";
            default -> "&#x1F527;";
        };
        String statusClass = switch (toolCall.getStatus()) {
            case AcpToolCall.STATUS_COMPLETED -> "tc-status-completed";
            case AcpToolCall.STATUS_FAILED -> "tc-status-failed";
            default -> "tc-status-progress";
        };
        String statusIcon = switch (toolCall.getStatus()) {
            case AcpToolCall.STATUS_COMPLETED -> "&#x2705;";
            case AcpToolCall.STATUS_FAILED -> "&#x274C;";
            case AcpToolCall.STATUS_IN_PROGRESS -> "&#x23F3;";
            default -> "&#x25CB;";
        };

        chatHtml.append("<div class='tool-call'>")
                .append(kindIcon).append(" <b>").append(escapeHtml(toolCall.getTitle())).append("</b>")
                .append(" <span class='tc-kind'>[").append(escapeHtml(toolCall.getKind())).append("]</span>")
                .append(" ").append(statusIcon)
                .append(" <span class='").append(statusClass).append("'>")
                .append(escapeHtml(toolCall.getStatus())).append("</span>");

        if (toolCall.getName() != null && !toolCall.getName().equals(toolCall.getTitle())) {
            chatHtml.append("<br/><i>").append(escapeHtml(toolCall.getName())).append("</i>");
        }

        if (!toolCall.getLocations().isEmpty()) {
            chatHtml.append("<br/>");
            for (AcpToolCall.Location loc : toolCall.getLocations()) {
                chatHtml.append("<span class='tc-location'>")
                        .append(escapeHtml(loc.path()));
                if (loc.line() != null) {
                    chatHtml.append(":").append(loc.line());
                }
                chatHtml.append("</span> ");
            }
        }

        if (!toolCall.getDiffs().isEmpty()) {
            for (AcpToolCall.DiffContent diff : toolCall.getDiffs()) {
                String diffId = "diff-" + (diffCounter++);
                diffStore.put(diffId, diff);

                chatHtml.append("<br/>");
                if (diff.path() != null) {
                    chatHtml.append("<b>").append(escapeHtml(diff.path())).append("</b><br/>");
                }
                appendDiffPreview(diff);
                chatHtml.append("<br/><a class='diff-link' href='acp-diff:")
                        .append(diffId).append("'>View Full Diff</a>");
            }
        }

        if (toolCall.getTextContent() != null && !toolCall.getTextContent().isEmpty()) {
            String preview = toolCall.getTextContent();
            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }
            chatHtml.append("<br/><code>").append(escapeHtml(preview)).append("</code>");
        }

        chatHtml.append("</div>");
        updateChatDisplay();
    }

    private void appendDiffPreview(AcpToolCall.DiffContent diff) {
        String[] oldLines = diff.oldText() != null ? diff.oldText().split("\n", -1) : new String[0];
        String[] newLines = diff.newText() != null ? diff.newText().split("\n", -1) : new String[0];

        int previewLines = 0;
        int maxPreview = 5;

        for (String line : oldLines) {
            if (previewLines >= maxPreview) {
                chatHtml.append("<span class='diff-del'>...</span><br/>");
                break;
            }
            chatHtml.append("<span class='diff-del'>- ")
                    .append(escapeHtml(line)).append("</span><br/>");
            previewLines++;
        }
        previewLines = 0;
        for (String line : newLines) {
            if (previewLines >= maxPreview) {
                chatHtml.append("<span class='diff-add'>...</span><br/>");
                break;
            }
            chatHtml.append("<span class='diff-add'>+ ")
                    .append(escapeHtml(line)).append("</span><br/>");
            previewLines++;
        }
    }

    private void appendPlan(List<AcpPlanEntry> entries) {
        chatHtml.append("<div class='plan'><b>Plan:</b><ul>");
        for (AcpPlanEntry entry : entries) {
            String icon = switch (entry.status()) {
                case "completed" -> "&#x2705;";
                case "in_progress" -> "&#x23F3;";
                default -> "&#x25CB;";
            };
            chatHtml.append("<li>").append(icon).append(" ")
                    .append(escapeHtml(entry.content()))
                    .append("</li>");
        }
        chatHtml.append("</ul></div>");
        updateChatDisplay();
    }

    // Coalesces bursts of updates into a single re-render.
    private void updateChatDisplay() {
        if (!displayTimer.isRunning()) {
            displayTimer.start();
        }
    }

    private void renderChatDisplay() {
        if (renderedLength > chatHtml.length()) {
            renderedLength = 0;
        }
        String fragment = chatHtml.substring(renderedLength);
        if (fragment.isEmpty()) {
            return;
        }
        // Only the newly appended HTML is parsed, instead of the whole document.
        boolean inserted = false;
        if (renderedLength > 0 && chatPane.getDocument() instanceof HTMLDocument) {
            HTMLDocument doc = (HTMLDocument) chatPane.getDocument();
            try {
                Element body = doc.getDefaultRootElement().getElement(1);
                doc.insertBeforeEnd(body, fragment);
                inserted = true;
            } catch (BadLocationException | IOException | RuntimeException ex) {
                inserted = false;
            }
        }
        if (!inserted) {
            chatPane.setText("<html><body>" + chatHtml + "</body></html>");
        }
        renderedLength = chatHtml.length();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) chatPane.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private static String getMainProjectDirectory() {
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if (mainProject != null) {
            return mainProject.getProjectDirectory().getPath();
        }
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        if (openProjects.length > 0) {
            return openProjects[0].getProjectDirectory().getPath();
        }
        return null;
    }

    private static List<String> getOpenProjectDirectories() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        List<String> dirs = new ArrayList<>();
        for (Project p : openProjects) {
            dirs.add(p.getProjectDirectory().getPath());
        }
        return dirs;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
