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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private static final RequestProcessor RP = new RequestProcessor("ACP-Agent-RP", 2);
    private static final int RENDER_DELAY_MS = 200;

    private final JEditorPane chatPane;
    private final JTextArea inputArea;
    private final JButton sendButton;
    private final JButton connectButton;
    private final JComboBox<String> agentSelector;
    private final JLabel statusLabel;
    private final StringBuilder chatHtml = new StringBuilder();
    private final StringBuilder currentMessageBuffer = new StringBuilder();
    private final StringBuilder currentThoughtBuffer = new StringBuilder();
    private final AtomicBoolean promptInProgress = new AtomicBoolean(false);
    private final Timer renderTimer;

    private AcpConnection currentConnection;
    private String currentSessionId;

    private final AcpSessionListener sessionListener = new AcpSessionListener() {
        @Override
        public void onAgentMessage(String sessionId, String text) {
            currentMessageBuffer.append(text);
            renderTimer.restart();
        }

        @Override
        public void onAgentThought(String sessionId, String text) {
            currentThoughtBuffer.append(text);
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
        toolbar.add(Box.createHorizontalGlue());
        statusLabel = new JLabel("Disconnected");
        toolbar.add(statusLabel);
        add(toolbar, BorderLayout.NORTH);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: sans-serif; font-size: 12px; margin: 8px; }");
        styleSheet.addRule(".user-msg { background-color: #E3F2FD; padding: 8px; margin: 4px 0; border-radius: 4px; }");
        styleSheet.addRule(".agent-msg { background-color: #F5F5F5; padding: 8px; margin: 4px 0; border-radius: 4px; }");
        styleSheet.addRule(".thought { color: #888; font-style: italic; padding: 4px 8px; margin: 2px 0; }");
        styleSheet.addRule(".tool-call { background-color: #FFF3E0; padding: 6px; margin: 4px 0; border-left: 3px solid #FF9800; }");
        styleSheet.addRule(".plan { background-color: #E8F5E9; padding: 6px; margin: 4px 0; }");
        styleSheet.addRule("pre { background-color: #263238; color: #EEFFFF; padding: 8px; overflow-x: auto; }");
        styleSheet.addRule("code { font-family: monospace; }");

        chatPane = new JEditorPane();
        chatPane.setEditorKit(kit);
        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setText("<html><body></body></html>");
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
                RP.post(() -> {
                    try {
                        currentConnection.close();
                    } catch (Exception ex) {
                        // ignore
                    }
                });
                currentConnection = null;
                currentSessionId = null;
                connectButton.setText("Connect");
                statusLabel.setText("Disconnected");
                updateInputState();
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
                String sessionId = currentConnection.newSession(getMainProjectDirectory());
                SwingUtilities.invokeLater(() -> {
                    currentSessionId = sessionId;
                    chatHtml.setLength(0);
                    chatPane.setText("<html><body></body></html>");
                    statusLabel.setText("Ready");
                    updateInputState();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Error: " + ex.getMessage())
                );
            }
        });
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || currentSessionId == null || promptInProgress.get()) {
            return;
        }
        appendUserMessage(text);
        inputArea.setText("");
        promptInProgress.set(true);
        updateInputState();
        statusLabel.setText("Thinking...");

        RP.post(() -> {
            try {
                currentConnection.sendPrompt(currentSessionId, text);
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

    private void updateInputState() {
        boolean canSend = currentConnection != null
                && currentConnection.isConnected()
                && currentSessionId != null
                && !promptInProgress.get();
        sendButton.setEnabled(canSend);
        inputArea.setEnabled(canSend);
    }

    private void appendUserMessage(String text) {
        chatHtml.append("<div class='user-msg'><b>You:</b><br/>")
                .append(escapeHtml(text))
                .append("</div>");
        updateChatDisplay();
    }

    private void appendSystemMessage(String html) {
        chatHtml.append("<div class='agent-msg'>").append(html).append("</div>");
        updateChatDisplay();
    }

    private void flushMessageBuffers() {
        SwingUtilities.invokeLater(() -> {
            boolean changed = false;
            if (currentThoughtBuffer.length() > 0) {
                chatHtml.append("<div class='thought'>")
                        .append(escapeHtml(currentThoughtBuffer.toString()))
                        .append("</div>");
                currentThoughtBuffer.setLength(0);
                changed = true;
            }
            if (currentMessageBuffer.length() > 0) {
                chatHtml.append("<div class='agent-msg'><b>Agent:</b><br/>")
                        .append(escapeHtml(currentMessageBuffer.toString()).replace("\n", "<br/>"))
                        .append("</div>");
                currentMessageBuffer.setLength(0);
                changed = true;
            }
            if (changed) {
                updateChatDisplay();
            }
        });
    }

    private void appendToolCall(AcpToolCall toolCall) {
        chatHtml.append("<div class='tool-call'>")
                .append("<b>Tool: ").append(escapeHtml(toolCall.getTitle())).append("</b>")
                .append(" <i>[").append(escapeHtml(toolCall.getKind())).append("]</i>")
                .append(" — ").append(escapeHtml(toolCall.getStatus()))
                .append("</div>");
        updateChatDisplay();
    }

    private void appendToolCallUpdate(AcpToolCall toolCall) {
        chatHtml.append("<div class='tool-call'>")
                .append("Tool ").append(escapeHtml(toolCall.getTitle()))
                .append(": ").append(escapeHtml(toolCall.getStatus()))
                .append("</div>");
        updateChatDisplay();
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

    private void updateChatDisplay() {
        chatPane.setText("<html><body>" + chatHtml + "</body></html>");
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

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
