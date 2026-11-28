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

import com.agentclientprotocol.sdk.error.AcpCapabilityException;
import com.agentclientprotocol.sdk.spec.AcpSchema.CreateTerminalRequest;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentManager;
import org.netbeans.modules.acp.api.AcpCommand;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpModeInfo;
import org.netbeans.modules.acp.api.AcpPlanEntry;
import org.netbeans.modules.acp.api.AcpSessionInfo;
import org.netbeans.modules.acp.api.AcpSessionListener;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.McpToolRegistry;
import org.netbeans.modules.acp.ui.AcpContextReceiver;
import org.netbeans.modules.acp.ui.AcpPermissionDialog;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider.ProjectContext;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider.SourceRoot;
import org.netbeans.modules.acp.ui.HtmlUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
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
@NbBundle.Messages({
    "LBL_Agent=Agent:",
    "BTN_Connect=Connect",
    "BTN_Disconnect=Disconnect",
    "BTN_NewSession=New Session",
    "BTN_Cancel=Cancel",
    "BTN_Send=Send",
    "BTN_Tools=Tools ({0}/{1})",
    "BTN_Commands=Commands ({0})",
    "LBL_TabTitle=ACP Agent Chat - {0}",
    "HINT_ConnectedAgent=Agent: {0}\nWorking directory: {1}",
    "HINT_SessionContext=Agent: {0}\nProject: {1}\nWorking directory: {2}\nSource roots: {3}",
    "STS_Disconnected=Disconnected",
    "STS_Connecting=Connecting...",
    "STS_Connected=Connected",
    "STS_CreatingSession=Creating session...",
    "TTL_SessionDirectory=Choose the Agent Session Directory",
    "STS_Ready=Ready",
    "STS_Thinking=Thinking...",
    "STS_Cancelled=Cancelled",
    "STS_Error=Error",
    "STS_ErrorDetail=Error: {0}",
    "STS_Tokens=Tokens: {0} / {1}",
    "MSG_ErrorCreatingSession=Error creating session: {0}",
    "MSG_PromptCancelled=Prompt cancelled.",
    "BTN_Sessions=Sessions",
    "LBL_NoSessions=No past sessions found.",
    "STS_ResumingSession=Resuming session...",
    "MSG_SessionResumed=Session resumed.",
    "MSG_SessionDeleted=Session deleted.",
    "MSG_NoSessionCapability=This agent does not support session management.",
    "LBL_AgentImage=[Image]",
    "BTN_Mode=Mode: {0}",
    "LBL_NoModes=No modes available.",
    "TTL_TerminalExecution=Allow Local Command?",
    "MSG_TerminalExecution=Agent {0} requests to run this command on your machine. Allow this command once?",
    "LBL_TerminalDirectory=Working directory: {0}",
    "LBL_TerminalEnvironment=Environment overrides: {0}"
})
public final class AcpChatTopComponent extends TopComponent implements AcpContextReceiver {

    private static final Logger LOG = Logger.getLogger(AcpChatTopComponent.class.getName());

    private final AcpChatSession session = new AcpChatSession();
    private final ChatRenderer chat = new ChatRenderer();
    private final JTextArea inputArea;
    private final JButton sendButton;
    private final JButton cancelButton;
    private final JButton toolsButton;
    private final JButton commandsButton;
    private final JButton connectButton;
    private final JButton newSessionButton;
    private final JButton sessionsButton;
    private final JComboBox<String> agentSelector;
    private final JLabel statusLabel;
    private final AcpFileMentionSupport mentionSupport;
    private List<AcpCommand> currentCommands = List.of();
    private String commandPrefix = "/"; // NOI18N
    private String currentModeId;
    private JButton modeButton;
    private Set<String> currentEnabledTools;
    private String selectedSessionDirectory;
    private long lastUsedTokens;
    private long lastTotalTokens;
    private JPopupMenu slashPopup;

    private AcpSessionListener createSessionListener(AcpConnection connection) {
        return new AcpSessionListener() {
            @Override
            public void onAgentMessage(String sessionId, String text) {
                session.dispatch(connection, sessionId, () -> chat.appendAgentChunk(text));
            }

            @Override
            public void onAgentThought(String sessionId, String text) {
                session.dispatch(connection, sessionId, () -> chat.appendThoughtChunk(text));
            }

            @Override
            public void onToolCall(String sessionId, AcpToolCall toolCall) {
                session.dispatch(connection, sessionId, () -> chat.appendToolCall(toolCall));
            }

            @Override
            public void onToolCallUpdate(String sessionId, AcpToolCall toolCall) {
                session.dispatch(connection, sessionId, () -> chat.appendToolCall(toolCall));
            }

            @Override
            public void onPlanUpdate(String sessionId, List<AcpPlanEntry> entries) {
                session.dispatch(connection, sessionId, () -> chat.appendPlan(entries));
            }

            @Override
            public void onPromptComplete(String sessionId, String stopReason) {
                session.dispatch(connection, sessionId, () -> {
                    chat.finishAgentMessage();
                    session.promptFinished();
                    updateInputState();
                    if ("cancelled".equals(stopReason)) { // NOI18N
                        chat.appendSystemMessage("<i>" + Bundle.MSG_PromptCancelled() + "</i>");
                        statusLabel.setText(Bundle.STS_Cancelled());
                    } else if (lastTotalTokens > 0) {
                        statusLabel.setText(Bundle.STS_Tokens(lastUsedTokens, lastTotalTokens));
                    } else {
                        statusLabel.setText(Bundle.STS_Ready());
                    }
                });
            }

            @Override
            public void onUsageUpdate(String sessionId, long usedTokens, long totalTokens) {
                session.dispatch(connection, sessionId, () -> {
                    lastUsedTokens = usedTokens;
                    lastTotalTokens = totalTokens;
                    statusLabel.setText(Bundle.STS_Tokens(usedTokens, totalTokens));
                });
            }

            @Override
            public void onAvailableCommands(String sessionId, List<AcpCommand> commands) {
                session.dispatch(connection, sessionId, () -> {
                    currentCommands = commands;
                    commandsButton.setText(Bundle.BTN_Commands(commands.size()));
                    commandsButton.setEnabled(!commands.isEmpty());
                });
            }

            @Override
            public void onAgentImage(String sessionId, String data, String mimeType) {
                ImageIcon image;
                try {
                    image = AcpChatImage.decode(data, mimeType);
                } catch (IOException | RuntimeException ex) {
                    LOG.log(Level.FINE, "Could not decode ACP image", ex); // NOI18N
                    return;
                }
                session.dispatch(connection, sessionId, () -> chat.appendImage(image));
            }

            @Override
            public void onModeUpdate(String sessionId, String newModeId) {
                session.dispatch(connection, sessionId, () -> {
                    currentModeId = newModeId;
                    if (modeButton != null) {
                        modeButton.setText(Bundle.BTN_Mode(getModeDisplayName(newModeId)));
                    }
                });
            }

            @Override
            public void onFileWritten(String sessionId, String path,
                    String oldContent, String newContent) {
                session.dispatch(connection, sessionId, () -> chat.appendFileChange(path, oldContent, newContent));
            }
        };
    }

    public AcpChatTopComponent() {
        setName(NbBundle.getMessage(AcpChatTopComponent.class, "CTL_AcpChatTopComponent"));
        setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_AcpChatTopComponent"));
        setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        agentSelector = new JComboBox<>();
        agentSelector.setMaximumSize(new Dimension(200, 28));
        toolbar.add(new JLabel(Bundle.LBL_Agent() + " "));
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
        connectButton = new JButton(Bundle.BTN_Connect());
        connectButton.addActionListener(e -> toggleConnection());
        toolbar.add(connectButton);
        newSessionButton = new JButton(Bundle.BTN_NewSession());
        newSessionButton.setEnabled(false);
        newSessionButton.addActionListener(e -> startNewSession());
        toolbar.add(newSessionButton);
        sessionsButton = new JButton(Bundle.BTN_Sessions());
        sessionsButton.setEnabled(false);
        sessionsButton.addActionListener(e -> showSessionBrowser());
        toolbar.add(sessionsButton);
        cancelButton = new JButton(Bundle.BTN_Cancel());
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelPrompt());
        toolbar.add(cancelButton);
        toolsButton = new JButton(Bundle.BTN_Tools(0, 0));
        toolsButton.setEnabled(false);
        toolsButton.addActionListener(e -> showToolsPopup());
        toolbar.add(toolsButton);
        commandsButton = new JButton(Bundle.BTN_Commands(0));
        commandsButton.setEnabled(false);
        commandsButton.addActionListener(e -> showCommandsPopup());
        toolbar.add(commandsButton);
        modeButton = new JButton(Bundle.BTN_Mode("default"));
        modeButton.setEnabled(false);
        modeButton.addActionListener(e -> showModePopup());
        toolbar.add(modeButton);
        toolbar.add(Box.createHorizontalGlue());
        statusLabel = new JLabel(Bundle.STS_Disconnected());
        toolbar.add(statusLabel);
        add(toolbar, BorderLayout.NORTH);

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

        sendButton = new JButton(Bundle.BTN_Send());
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setEnabled(false);

        JPanel inputPanel = new JPanel(new BorderLayout(4, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(sendButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chat, inputPanel);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(4);
        add(splitPane, BorderLayout.CENTER);

        mentionSupport = new AcpFileMentionSupport(inputArea);
        inputArea.setTransferHandler(new AcpFileDropHandler(inputArea, mentionSupport));
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkSlashCommand();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkSlashCommand();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        refreshAgentList();
    }

    @Override
    public void componentOpened() {
        refreshAgentList();
    }

    @Override
    public void componentClosed() {
        session.invalidateConnectionAttempt();
        disconnect();
    }

    @Override
    public void setContextForNextPrompt(String context) {
        if (context != null && !context.isEmpty()) {
            inputArea.insert(context + "\n", 0);
            inputArea.setCaretPosition(inputArea.getDocument().getLength());
        }
        inputArea.requestFocusInWindow();
    }

    private void showAddAgentDialog() {
        AcpAgentConfigPanel panel = new AcpAgentConfigPanel();
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AcpChatTopComponent.class, "TTL_AddAgent"));
        while (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            AcpAgentDescriptor descriptor = panel.buildDescriptor();
            if (descriptor != null) {
                AcpAgentManager.getDefault().addAgent(descriptor);
                refreshAgentList();
                agentSelector.setSelectedItem(descriptor.getName());
                break;
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
            if (session.invalidateConnectionAttempt(agentName)) {
                statusLabel.setText(Bundle.STS_Disconnected());
            }
            if (session.connection() != null && session.connection().getDescriptor().getName().equals(agentName)) {
                disconnect();
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

    private void disconnect() {
        AcpConnection connection = session.connection();
        if (connection == null) {
            return;
        }
        session.disconnect(() -> connectButton.setEnabled(true));
        chat.disconnect();
        currentEnabledTools = null;
        toolsButton.setText(Bundle.BTN_Tools(0, 0));
        toolsButton.setEnabled(false);
        selectedSessionDirectory = null;
        commandPrefix = "/"; // NOI18N
        resetSessionControls();
        setDisplayName(NbBundle.getMessage(AcpChatTopComponent.class, "CTL_AcpChatTopComponent"));
        setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_AcpChatTopComponent"));
        connectButton.setText(Bundle.BTN_Connect());
        connectButton.setEnabled(false);
        statusLabel.setText(Bundle.STS_Disconnected());
        updateInputState();
    }

    private void toggleConnection() {
        if (session.connection() != null) {
            disconnect();
            return;
        }
        String agentName = (String) agentSelector.getSelectedItem();
        if (agentName == null) {
            return;
        }
        AcpAgentDescriptor descriptor = AcpAgentManager.getDefault().getAgents().stream()
                .filter(agent -> agentName.equals(agent.getName()))
                .findFirst().orElse(null);
        if (descriptor == null) {
            return;
        }
        statusLabel.setText(Bundle.STS_Connecting());
        connectButton.setEnabled(false);
        String fallbackDir = getMainProjectDirectory();
        session.connect(descriptor, this::configureConnection, () -> {
            currentEnabledTools = descriptor.getEnabledTools();
            String workingDirectory = descriptor.getWorkingDirectory() != null
                    ? descriptor.getWorkingDirectory() : fallbackDir != null ? fallbackDir : "";
            commandPrefix = descriptor.getCommandPrefix();
            setDisplayName(Bundle.LBL_TabTitle(agentName));
            setToolTipText(Bundle.HINT_ConnectedAgent(agentName, workingDirectory));
            connectButton.setText(Bundle.BTN_Disconnect());
            statusLabel.setText(Bundle.STS_Connected());
            updateInputState();
        }, ex -> statusLabel.setText(Bundle.STS_ErrorDetail(AcpChatSession.errorMessage(ex))),
                () -> connectButton.setEnabled(true));
    }

    private AcpSessionListener configureConnection(AcpConnection connection) {
        connection.setPermissionHandler(request -> {
            AcpPermissionDialog.PermissionResult result = AcpPermissionDialog.showPermissionRequest(request);
            if (result.allowToolForSession() != null) {
                connection.allowToolForSession(request.sessionId(), result.allowToolForSession());
            }
            return result.response();
        });
        connection.setElicitationHandler(AcpElicitationDialog::show);
        connection.setTerminalAuthorizer(request -> confirmTerminal(connection, request));
        return createSessionListener(connection);
    }

    private boolean confirmTerminal(AcpConnection connection, CreateTerminalRequest request) {
        return Mutex.EVENT.readAccess(() -> {
            if (!session.isCurrent(connection, request.sessionId())) {
                return false;
            }
            StringBuilder details = new StringBuilder(String.valueOf(request.command()));
            if (request.args() != null) {
                request.args().forEach(arg -> details.append('\n').append(arg));
            }
            if (request.cwd() != null) {
                details.append("\n\n").append(Bundle.LBL_TerminalDirectory(request.cwd()));
            }
            if (request.env() != null && !request.env().isEmpty()) {
                details.append("\n").append(Bundle.LBL_TerminalEnvironment(
                        String.join(", ", request.env().stream().map(variable -> variable.name()).toList())));
            }
            JTextArea command = new JTextArea(details.toString(), 8, 50);
            command.setEditable(false);
            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.add(new JLabel(Bundle.MSG_TerminalExecution(
                    HtmlUtils.escapeHtml(connection.getDescriptor().getName()))), BorderLayout.NORTH);
            panel.add(new JScrollPane(command), BorderLayout.CENTER);
            NotifyDescriptor.Confirmation dialog = new NotifyDescriptor.Confirmation(panel,
                    Bundle.TTL_TerminalExecution(), NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE);
            dialog.setValue(NotifyDescriptor.CANCEL_OPTION);
            return DialogDisplayer.getDefault().notify(dialog) == NotifyDescriptor.OK_OPTION
                    && session.isCurrent(connection, request.sessionId());
        });
    }

    private void startNewSession() {
        AcpConnection connection = session.connection();
        if (!session.isIdle()) {
            return;
        }
        Project project = getContextProject();
        String mainDir = chooseSessionDirectory(connection, project);
        if (mainDir == null || connection != session.connection()) {
            return;
        }
        AcpChatSession.Transition transition = session.beginTransition(null);
        statusLabel.setText(Bundle.STS_CreatingSession());
        updateInputState();
        List<String> openDirs = getOpenProjectDirectories();
        session.createSession(transition, mainDir, openDirs, () -> collectProjectContext(project),
                opened -> finishSession(connection, transition, opened.id(), mainDir, opened.projectContext()),
                () -> {
                    statusLabel.setText(Bundle.STS_Connected());
                    updateInputState();
                }, ex -> {
                    LOG.log(Level.WARNING, "Failed to create new session", ex);
                    chat.appendSystemMessage(Bundle.MSG_ErrorCreatingSession(
                            HtmlUtils.escapeHtml(AcpChatSession.errorMessage(ex))));
                    statusLabel.setText(Bundle.STS_Error());
                    updateInputState();
                });
    }

    private static ProjectContext collectProjectContext(Project project) {
        try {
            return AcpProjectContextProvider.gatherProjectContext(project);
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to gather project context", ex);
            return null;
        }
    }

    private void finishSession(AcpConnection connection, AcpChatSession.Transition transition,
            String sessionId, String workingDirectory, ProjectContext projectContext) {
        String previousId = session.sessionId();
        String promptContext = projectContext == null || transition.isResume() ? null : projectContext.promptText();
        session.finishTransition(transition, sessionId, promptContext, () -> {
            if (previousId != null && !previousId.equals(sessionId)) {
                inputArea.setText("");
            }
            String agentName = connection.getDescriptor().getName();
            if (projectContext == null) {
                setToolTipText(Bundle.HINT_ConnectedAgent(agentName, workingDirectory));
            } else {
                List<String> roots = projectContext.sourceRoots().stream().map(SourceRoot::description).toList();
                setToolTipText(Bundle.HINT_SessionContext(agentName, projectContext.name(), workingDirectory,
                        roots.isEmpty() ? "" : "\n  " + String.join("\n  ", roots)));
            }
            chat.clear();
            resetSessionControls();
            updateToolsButton(countTools(currentEnabledTools));
            modeButton.setEnabled(!connection.getAvailableModes().isEmpty());
            currentModeId = connection.getCurrentModeId();
            if (currentModeId != null) {
                modeButton.setText(Bundle.BTN_Mode(getModeDisplayName(currentModeId)));
            }
            statusLabel.setText(Bundle.STS_Ready());
            if (transition.isResume()) {
                chat.appendSystemMessage(Bundle.MSG_SessionResumed());
            }
        });
        updateInputState();
    }

    private void resetSessionControls() {
        currentCommands = List.of();
        currentModeId = null;
        lastUsedTokens = 0;
        lastTotalTokens = 0;
        commandsButton.setText(Bundle.BTN_Commands(0));
        commandsButton.setEnabled(false);
        modeButton.setText(Bundle.BTN_Mode("—"));
        modeButton.setEnabled(false);
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || !session.canSend()) {
            return;
        }
        String attachments = mentionSupport.buildAttachmentContext(mentionSupport.consumeAttachedFiles());
        session.sendPrompt(text, attachments, () -> {
            chat.finishAgentMessage();
            chat.appendUserMessage(text);
            inputArea.setText("");
            updateInputState();
            statusLabel.setText(Bundle.STS_Thinking());
        }, ex -> {
            chat.finishAgentMessage();
            chat.appendSystemMessage(Bundle.STS_ErrorDetail(HtmlUtils.escapeHtml(AcpChatSession.errorMessage(ex))));
            updateInputState();
            statusLabel.setText(Bundle.STS_Error());
        });
    }

    private void cancelPrompt() {
        if (session.canCancel()) {
            session.cancelPrompt(this::updateInputState);
        }
    }

    private void updateInputState() {
        boolean idle = session.isIdle();
        sendButton.setEnabled(session.canSend());
        inputArea.setEnabled(session.canSend());
        cancelButton.setEnabled(session.canCancel());
        newSessionButton.setEnabled(idle && session.sessionId() == null);
        sessionsButton.setEnabled(idle);
    }

    private record ToolCounts(int enabled, int total) { }

    private static ToolCounts countTools(Set<String> enabledTools) {
        McpToolRegistry registry = McpToolRegistry.getInstance();
        List<McpToolProvider.McpToolDefinition> tools = registry.getAllAvailableTools();
        int enabled = (int) tools.stream().filter(tool -> enabledTools != null
                ? enabledTools.contains(tool.name()) : registry.isToolEnabledByDefault(tool.name())).count();
        return new ToolCounts(enabled, tools.size());
    }

    private void updateToolsButton(ToolCounts counts) {
        toolsButton.setText(Bundle.BTN_Tools(counts.enabled(), counts.total()));
        toolsButton.setEnabled(counts.total() > 0);
    }

    private void showToolsPopup() {
        McpToolRegistry registry = McpToolRegistry.getInstance();
        Map<String, List<McpToolProvider.McpToolDefinition>> toolsByProvider =
                registry.getToolsByProvider();
        if (toolsByProvider.isEmpty()) {
            return;
        }
        Set<String> enabledSet = currentEnabledTools;
        JPopupMenu popup = new JPopupMenu();
        boolean first = true;
        for (Map.Entry<String, List<McpToolProvider.McpToolDefinition>> group
                : toolsByProvider.entrySet()) {
            if (!first) {
                popup.addSeparator();
            }
            first = false;
            JMenuItem header = new JMenuItem("<html><b>" + group.getKey() + "</b></html>");
            header.setEnabled(false);
            popup.add(header);
            for (McpToolProvider.McpToolDefinition tool : group.getValue()) {
                boolean enabled;
                if (enabledSet != null) {
                    enabled = enabledSet.contains(tool.name());
                } else {
                    enabled = registry.isToolEnabledByDefault(tool.name());
                }
                String desc = tool.description();
                if (desc != null && desc.length() > 80) {
                    int dot = desc.indexOf(". ");
                    if (dot > 0 && dot <= 80) {
                        desc = desc.substring(0, dot + 1);
                    } else {
                        desc = desc.substring(0, 80) + "...";
                    }
                }
                String label;
                if (enabled) {
                    label = "<html>&nbsp;&nbsp;<b>" + tool.name() + "</b>"
                            + (desc != null ? " &ndash; " + desc : "")
                            + "</html>";
                } else {
                    label = "<html>&nbsp;&nbsp;<span style='color:gray'>" + tool.name()
                            + (desc != null ? " &ndash; " + desc : "")
                            + "</span></html>";
                }
                JMenuItem item = new JMenuItem(label);
                item.setEnabled(false);
                popup.add(item);
            }
        }
        popup.show(toolsButton, 0, toolsButton.getHeight());
    }

    private void showCommandsPopup() {
        if (currentCommands.isEmpty()) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        for (AcpCommand cmd : currentCommands) {
            String desc = cmd.description() != null
                    ? HtmlUtils.escapeHtml(cmd.description()) : "";
            String label = "<html><div style='width:280px'><b>"
                    + HtmlUtils.escapeHtml(cmd.name()) + "</b><br/>"
                    + "<span style='color:gray;font-size:smaller'>"
                    + desc + "</span></div></html>";
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(e -> {
                inputArea.setText(cmd.name() + " ");
                inputArea.requestFocusInWindow();
            });
            popup.add(item);
        }
        popup.show(commandsButton, 0, commandsButton.getHeight());
    }

    private String getModeDisplayName(String modeId) {
        if (modeId == null) {
            return "default";
        }
        if (session.connection() != null) {
            for (AcpModeInfo mode : session.connection().getAvailableModes()) {
                if (modeId.equals(mode.id()) && mode.name() != null) {
                    return mode.name();
                }
            }
        }
        int fragment = modeId.lastIndexOf('#');
        if (fragment >= 0 && fragment < modeId.length() - 1) {
            return modeId.substring(fragment + 1);
        }
        return modeId;
    }

    private void showModePopup() {
        AcpConnection connection = session.connection();
        String sessionId = session.sessionId();
        if (connection == null || !connection.isConnected() || !session.isCurrent(connection, sessionId)) {
            return;
        }
        List<AcpModeInfo> modes = connection.getAvailableModes();
        if (modes.isEmpty()) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        for (AcpModeInfo mode : modes) {
            String label = "<html><b>" + HtmlUtils.escapeHtml(mode.name()) + "</b>"
                    + (mode.id().equals(currentModeId) ? " &#x2713;" : "")
                    + (mode.description() != null
                            ? "<br/><span style='color:gray;font-size:smaller'>"
                            + HtmlUtils.escapeHtml(mode.description()) + "</span>"
                            : "")
                    + "</html>";
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(ev -> {
                if (session.isCurrent(connection, sessionId)) {
                    session.setMode(mode.id());
                }
            });
            popup.add(item);
        }
        popup.show(modeButton, 0, modeButton.getHeight());
    }

    private void checkSlashCommand() {
        SwingUtilities.invokeLater(() -> {
            dismissSlashPopup();
            if (currentCommands.isEmpty()) {
                return;
            }
            String text = inputArea.getText();
            if (text.isEmpty() || !text.startsWith(commandPrefix)
                    || text.contains(" ") || text.contains("\n")) {
                return;
            }
            String typed = text.substring(commandPrefix.length()).toLowerCase();
            List<AcpCommand> matches = new ArrayList<>();
            for (AcpCommand cmd : currentCommands) {
                if (cmd.name().toLowerCase().startsWith(typed)) {
                    matches.add(cmd);
                }
            }
            if (matches.isEmpty()) {
                return;
            }
            matches.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
            LOG.log(Level.FINE, "checkSlashCommand: {0} matches for ''{1}''",
                    new Object[]{matches.size(), typed});
            JPopupMenu popup = new JPopupMenu();
            popup.setFocusable(false);
            for (AcpCommand cmd : matches) {
                StringBuilder label = new StringBuilder("<html><b>")
                        .append(HtmlUtils.escapeHtml(cmd.name())).append("</b>");
                if (cmd.description() != null && !cmd.description().isEmpty()) {
                    String escaped = HtmlUtils.escapeHtml(cmd.description()).replace("\n", "<br>");
                    label.append("<br><span style='color:#666; font-size:small'>")
                            .append(escaped).append("</span>");
                }
                label.append("</html>");
                JMenuItem item = new JMenuItem(label.toString());
                item.addActionListener(ev -> {
                    inputArea.setText(commandPrefix + cmd.name() + " ");
                    inputArea.setCaretPosition(inputArea.getText().length());
                });
                popup.add(item);
            }
            slashPopup = popup;
            try {
                Rectangle2D caret = inputArea.modelToView2D(inputArea.getCaretPosition());
                if (caret != null) {
                    Rectangle bounds = caret.getBounds();
                    popup.show(inputArea, bounds.x, bounds.y + bounds.height);
                } else {
                    popup.show(inputArea, 0, inputArea.getHeight());
                }
            } catch (BadLocationException ex) {
                popup.show(inputArea, 0, inputArea.getHeight());
            }
        });
    }


    private void dismissSlashPopup() {
        if (slashPopup != null) {
            slashPopup.setVisible(false);
            slashPopup = null;
        }
    }

    private void showSessionBrowser() {
        AcpConnection connection = session.connection();
        if (!session.isIdle()) {
            return;
        }
        Project project = getContextProject();
        String mainDir = chooseSessionDirectory(connection, project);
        if (mainDir == null || connection != session.connection()) {
            return;
        }
        session.listSessions(mainDir, sessions -> {
            if (sessions.isEmpty()) {
                chat.appendSystemMessage(Bundle.LBL_NoSessions());
                return;
            }
            AcpSessionBrowser.show(sessions, connection,
                    info -> onResumeSession(info, connection, mainDir, project),
                    (info, deleted) -> onDeleteSession(info, connection, deleted));
        }, this::handleSessionListError);
    }

    private void handleSessionListError(IOException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof AcpCapabilityException) {
            chat.appendSystemMessage(Bundle.MSG_NoSessionCapability());
        } else {
            LOG.log(Level.WARNING, "Failed to list sessions", ex);
            chat.appendSystemMessage(Bundle.STS_ErrorDetail(HtmlUtils.escapeHtml(AcpChatSession.errorMessage(ex))));
        }
    }

    private void onResumeSession(AcpSessionInfo info, AcpConnection connection, String mainDir, Project project) {
        if (connection != session.connection() || !session.isIdle()) {
            return;
        }
        AcpChatSession.Transition transition = session.beginTransition(info.sessionId());
        statusLabel.setText(Bundle.STS_ResumingSession());
        updateInputState();
        List<String> openDirs = getOpenProjectDirectories();
        session.resumeSession(transition, mainDir, openDirs, () -> collectProjectContext(project),
                opened -> finishSession(connection, transition, opened.id(), mainDir, opened.projectContext()),
                ex -> {
                    LOG.log(Level.WARNING, "Failed to resume session", ex);
                    statusLabel.setText(Bundle.STS_Error());
                    updateInputState();
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(AcpChatSession.errorMessage(ex), NotifyDescriptor.ERROR_MESSAGE));
                });
    }

    private void onDeleteSession(AcpSessionInfo info, AcpConnection connection, Runnable deleted) {
        if (connection != session.connection() || !session.isIdle()) {
            return;
        }
        session.deleteSession(info.sessionId(), wasActive -> {
            deleted.run();
            if (wasActive) {
                inputArea.setText("");
                resetSessionControls();
                updateInputState();
            }
            chat.appendSystemMessage(Bundle.MSG_SessionDeleted());
        }, ex -> {
            LOG.log(Level.WARNING, "Failed to delete session", ex);
            statusLabel.setText(Bundle.STS_Error());
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(AcpChatSession.errorMessage(ex), NotifyDescriptor.ERROR_MESSAGE));
        });
    }

    private static String getMainProjectDirectory() {
        Project project = getContextProject();
        return project == null ? null : getLocalProjectDirectory(project);
    }

    private String chooseSessionDirectory(AcpConnection connection, Project project) {
        String projectPath = project == null ? null : getLocalProjectDirectory(project);
        if (projectPath != null) {
            return projectPath;
        }
        String configured = connection.getDescriptor().getWorkingDirectory();
        for (String candidate : new String[]{selectedSessionDirectory, configured}) {
            if (candidate != null) {
                File directory = new File(candidate);
                if (directory.isAbsolute() && directory.isDirectory()) {
                    return directory.getAbsolutePath();
                }
            }
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.TTL_SessionDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                && session.connection() == connection && chooser.getSelectedFile().isDirectory()) {
            selectedSessionDirectory = chooser.getSelectedFile().getAbsolutePath();
            return selectedSessionDirectory;
        }
        return null;
    }

    private static String getLocalProjectDirectory(Project project) {
        File directory = FileUtil.toFile(project.getProjectDirectory());
        return directory == null ? null : directory.getAbsolutePath();
    }

    private static Project getContextProject() {
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if (mainProject != null) {
            return mainProject;
        }
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        return openProjects.length > 0 ? openProjects[0] : null;
    }

    private static List<String> getOpenProjectDirectories() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        List<String> dirs = new ArrayList<>();
        for (Project p : openProjects) {
            String directory = getLocalProjectDirectory(p);
            if (directory != null) {
                dirs.add(directory);
            }
        }
        return dirs;
    }
}
