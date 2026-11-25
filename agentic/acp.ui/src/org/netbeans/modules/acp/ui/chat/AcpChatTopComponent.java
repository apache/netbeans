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
import com.agentclientprotocol.sdk.error.AcpErrorCodes;
import com.agentclientprotocol.sdk.spec.AcpError;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.CreateTerminalRequest;
import org.netbeans.modules.acp.ui.HtmlUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.api.AcpCommand;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentManager;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpConnectionManager;
import org.netbeans.modules.acp.api.AcpModeInfo;
import org.netbeans.modules.acp.api.AcpPlanEntry;
import org.netbeans.modules.acp.api.AcpSessionInfo;
import org.netbeans.modules.acp.api.AcpSessionListener;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.McpToolRegistry;
import org.netbeans.modules.acp.ui.AcpContextReceiver;
import org.netbeans.modules.acp.ui.AcpDiffViewer;
import org.netbeans.modules.acp.ui.AcpLocationNavigator;
import org.netbeans.modules.acp.ui.AcpMarkdownRenderer;
import org.netbeans.modules.acp.ui.AcpPermissionDialog;
import org.netbeans.modules.acp.ui.AcpProjectContextProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Mutex;
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
    "STS_Ready=Ready",
    "STS_Thinking=Thinking...",
    "STS_Cancelled=Cancelled",
    "STS_Error=Error",
    "TTL_Authentication=Agent Authentication",
    "MSG_Authentication=Select an authentication method for {0}.",
    "MSG_AuthenticationFailed=Authentication failed: {0}",
    "MSG_AuthenticationUnavailable=The agent requires authentication but did not offer a supported sign-in method. Sign in using the agent's own tools or check its configured credentials, then try again.",
    "STS_ErrorDetail=Error: {0}",
    "STS_Tokens=Tokens: {0} / {1}",
    "MSG_ErrorCreatingSession=Error creating session: {0}",
    "MSG_PromptCancelled=Prompt cancelled.",
    "LBL_FileCreated=Created",
    "LBL_FileModified=Modified",
    "LBL_Completed=completed",
    "LBL_ViewFullDiff=View Full Diff",
    "LBL_You=You:",
    "LBL_AgentSays=Agent:",
    "LBL_Plan=Plan:",
    "BTN_Sessions=Sessions",
    "TTL_SessionBrowser=Past Sessions",
    "LBL_SessionEntry={0} — {1}",
    "LBL_NoSessions=No past sessions found.",
    "BTN_ResumeSession=Resume",
    "BTN_DeleteSession=Delete",
    "STS_ResumingSession=Resuming session...",
    "MSG_SessionResumed=Session resumed.",
    "MSG_SessionDeleted=Session deleted.",
    "MSG_NoSessionCapability=This agent does not support session management.",
    "LBL_AgentImage=[Image]",
    "BTN_Mode=Mode: {0}",
    "LBL_NoModes=No modes available.",
    "TTL_Elicitation=Agent Input Request",
    "TTL_TerminalExecution=Allow Local Command?",
    "MSG_TerminalExecution=Agent {0} requests to run this command on your machine. Allow this command once?",
    "LBL_TerminalDirectory=Working directory: {0}",
    "LBL_TerminalEnvironment=Environment overrides: {0}",
    "BTN_Accept=Accept",
    "BTN_Decline=Decline"
})
public final class AcpChatTopComponent extends TopComponent implements AcpContextReceiver {

    private static final Logger LOG = Logger.getLogger(AcpChatTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("ACP-Agent-RP", 2);
    private static final int DISPLAY_DELAY_MS = 50;
    private static final int RENDER_DELAY_MS = 200;
    private static final int SESSION_BROWSER_WIDTH = 450;
    private static final int SESSION_BROWSER_HEIGHT = 250;

    private final JEditorPane chatPane;
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
    private final StringBuilder chatHtml = new StringBuilder();
    private int renderedLength;
    private final StringBuilder currentMessageBuffer = new StringBuilder();
    private final StringBuilder currentThoughtBuffer = new StringBuilder();
    private boolean promptInProgress;
    private boolean cancellationRequested;
    private boolean inAgentMessage;
    private int agentMsgDivEndPos = -1;
    private final Timer renderTimer;
    private final Timer displayTimer;

    private final Map<String, AcpToolCall.DiffContent> diffStore = new HashMap<>();
    private int diffCounter;
    private final AcpFileMentionSupport mentionSupport;
    private List<AcpCommand> currentCommands = List.of();
    private String commandPrefix = "/"; // NOI18N
    private String currentModeId;
    private JButton modeButton;
    private Set<String> currentEnabledTools;
    private String currentWorkingDir;
    private AcpConnection currentConnection;
    private AcpSessionListener currentListener;
    private long connectionGeneration;
    private String pendingAgentName;
    private String currentSessionId;
    private SessionTransition sessionTransition;
    private String pendingProjectContext;
    private long lastUsedTokens;
    private long lastTotalTokens;
    private JPopupMenu slashPopup;

    private AcpSessionListener createSessionListener(AcpConnection connection) {
        return new AcpSessionListener() {
            @Override
            public void onAgentMessage(String sessionId, String text) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    currentMessageBuffer.append(text);
                    renderTimer.restart();
                });
            }

            @Override
            public void onAgentThought(String sessionId, String text) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    currentThoughtBuffer.append(text);
                    renderTimer.restart();
                });
            }

            @Override
            public void onToolCall(String sessionId, AcpToolCall toolCall) {
                dispatchSessionEvent(connection, sessionId, () -> appendToolCall(toolCall));
            }

            @Override
            public void onToolCallUpdate(String sessionId, AcpToolCall toolCall) {
                dispatchSessionEvent(connection, sessionId, () -> appendToolCallUpdate(toolCall));
            }

            @Override
            public void onPlanUpdate(String sessionId, List<AcpPlanEntry> entries) {
                dispatchSessionEvent(connection, sessionId, () -> appendPlan(entries));
            }

            @Override
            public void onPromptComplete(String sessionId, String stopReason) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    flushMessageBuffers();
                    inAgentMessage = false;
                    agentMsgDivEndPos = -1;
                    promptInProgress = false;
                    updateInputState();
                    if ("cancelled".equals(stopReason)) { // NOI18N
                        appendSystemMessage("<i>" + Bundle.MSG_PromptCancelled() + "</i>");
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
                dispatchSessionEvent(connection, sessionId, () -> {
                    lastUsedTokens = usedTokens;
                    lastTotalTokens = totalTokens;
                    statusLabel.setText(Bundle.STS_Tokens(usedTokens, totalTokens));
                });
            }

            @Override
            public void onAvailableCommands(String sessionId, List<AcpCommand> commands) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    currentCommands = commands;
                    commandsButton.setText(Bundle.BTN_Commands(commands.size()));
                    commandsButton.setEnabled(!commands.isEmpty());
                });
            }

            @Override
            public void onSessionInfo(String sessionId, String title, String updatedAt) {
                if (title == null || title.isBlank()) {
                    return;
                }
                dispatchSessionEvent(connection, sessionId, () -> {
                    String projectName = HtmlUtils.extractProjectName(title);
                    if (projectName != null && !projectName.isEmpty()) {
                        String projectDir = HtmlUtils.extractProjectDirectory(title);
                        List<String> sourceRoots = HtmlUtils.extractSourceRoots(title);
                        String workDir = projectDir != null ? projectDir
                                : currentWorkingDir != null ? currentWorkingDir : "";
                        String roots = sourceRoots.isEmpty() ? "" : "\n  " + String.join("\n  ", sourceRoots);
                        setToolTipText(Bundle.HINT_SessionContext(
                                connection.getDescriptor().getName(), projectName, workDir, roots));
                    }
                });
            }

            @Override
            public void onAgentImage(String sessionId, String data, String mimeType) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    chatHtml.append("<div class='agent-msg'>")
                            .append("<img src='data:").append(mimeType != null ? mimeType : "image/png")
                            .append(";base64,").append(data)
                            .append("' style='max-width:400px; max-height:300px;' />")
                            .append("</div>");
                    inAgentMessage = false;
                    agentMsgDivEndPos = -1;
                    updateChatDisplay();
                });
            }

            @Override
            public void onModeUpdate(String sessionId, String newModeId) {
                dispatchSessionEvent(connection, sessionId, () -> {
                    currentModeId = newModeId;
                    if (modeButton != null) {
                        modeButton.setText(Bundle.BTN_Mode(getModeDisplayName(newModeId)));
                    }
                });
            }

            @Override
            public void onFileWritten(String sessionId, String path,
                    String oldContent, String newContent) {
                AcpToolCall.DiffContent diff = new AcpToolCall.DiffContent(
                        path,
                        oldContent != null ? oldContent : "",
                        newContent != null ? newContent : "");
                dispatchSessionEvent(connection, sessionId, () -> {
                    String diffId = "diff-" + (diffCounter++);
                    diffStore.put(diffId, diff);
                    String fileName = path;
                    int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
                    if (sep >= 0) {
                        fileName = path.substring(sep + 1);
                    }
                    String label = oldContent == null ? Bundle.LBL_FileCreated() : Bundle.LBL_FileModified();
                    chatHtml.append("<div class='tool-call'>")
                            .append("&#x270F; <b>").append(label).append(": ")
                            .append(HtmlUtils.escapeHtml(fileName)).append("</b>")
                            .append(" <span class='tc-status-completed'>&#x2705; ")
                            .append(Bundle.LBL_Completed()).append("</span>")
                            .append("<br/><a class='tc-location' href='acp-nav:")
                            .append(path).append("'>")
                            .append(HtmlUtils.escapeHtml(path)).append("</a>");
                    appendDiffPreview(diff);
                    chatHtml.append("<br/><a class='diff-link' href='acp-diff:")
                            .append(diffId).append("'>").append(Bundle.LBL_ViewFullDiff()).append("</a>")
                            .append("</div>");
                    updateChatDisplay();
                });
            }
        };
    }

    private void dispatchSessionEvent(AcpConnection connection, String sessionId, Runnable event) {
        SwingUtilities.invokeLater(() -> {
            if (connection != currentConnection || sessionId == null) {
                return;
            }
            if (sessionTransition != null) {
                sessionTransition.addUpdate(sessionId, currentSessionId, event);
            } else if (sessionId.equals(currentSessionId)) {
                event.run();
            }
        });
    }

    private static final class SessionTransition {
        final String expectedId;
        final Map<String, List<Runnable>> updates = new HashMap<>();

        SessionTransition(String expectedId) {
            this.expectedId = expectedId;
        }

        void addUpdate(String sessionId, String activeId, Runnable event) {
            if (expectedId != null ? expectedId.equals(sessionId) : !sessionId.equals(activeId)) {
                updates.computeIfAbsent(sessionId, id -> new ArrayList<>()).add(event);
            }
        }
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

        HTMLEditorKit kit = new HTMLEditorKit() {
            @Override
            public void install(JEditorPane c) {
                super.install(c);
                for (java.awt.event.MouseMotionListener mml : c.getMouseMotionListeners()) {
                    if (mml instanceof LinkController) {
                        final java.awt.event.MouseMotionListener delegate = mml;
                        c.removeMouseMotionListener(mml);
                        c.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                            @Override
                            public void mouseMoved(java.awt.event.MouseEvent e) {
                                try { delegate.mouseMoved(e); } catch (ArrayIndexOutOfBoundsException ignored) {}
                            }
                            @Override
                            public void mouseDragged(java.awt.event.MouseEvent e) {
                                try { delegate.mouseDragged(e); } catch (ArrayIndexOutOfBoundsException ignored) {}
                            }
                        });
                        break;
                    }
                }
            }
        };
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
        styleSheet.addRule("h1 { font-size: 16px; margin: 8px 0 4px; }");
        styleSheet.addRule("h2 { font-size: 14px; margin: 6px 0 4px; }");
        styleSheet.addRule("h3 { font-size: 13px; margin: 4px 0 2px; }");
        styleSheet.addRule("table { border-collapse: collapse; margin: 4px 0; }");
        styleSheet.addRule("th, td { border: 1px solid #ccc; padding: 4px 8px; }");
        styleSheet.addRule("th { background-color: #f0f0f0; font-weight: bold; }");
        styleSheet.addRule("blockquote { border-left: 3px solid #ccc; padding-left: 8px; color: #666; margin: 4px 0; }");
        styleSheet.addRule("ul, ol { margin: 4px 0 4px 20px; }");

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
                } else if (url != null && url.startsWith("acp-nav:")) {
                    AcpLocationNavigator.handleNavUrl(url);
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
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                checkSlashCommand();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                checkSlashCommand();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
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
        ++connectionGeneration;
        pendingAgentName = null;
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
            if (agentName.equals(pendingAgentName)) {
                ++connectionGeneration;
                pendingAgentName = null;
                statusLabel.setText(Bundle.STS_Disconnected());
            }
            if (currentConnection != null && currentConnection.getDescriptor().getName().equals(agentName)) {
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
        AcpConnection connection = currentConnection;
        if (connection == null) {
            return;
        }
        connection.removeSessionListener(currentListener);
        connection.setPermissionHandler(null);
        connection.setElicitationHandler(null);
        connection.setTerminalAuthorizer(null);
        currentConnection = null;
        currentListener = null;
        sessionTransition = null;
        currentSessionId = null;
        pendingProjectContext = null;
        promptInProgress = false;
        cancellationRequested = false;
        renderTimer.stop();
        displayTimer.stop();
        currentMessageBuffer.setLength(0);
        currentThoughtBuffer.setLength(0);
        currentEnabledTools = null;
        toolsButton.setText(Bundle.BTN_Tools(0, 0));
        toolsButton.setEnabled(false);
        currentWorkingDir = null;
        commandPrefix = "/"; // NOI18N
        resetSessionControls();
        setDisplayName(NbBundle.getMessage(AcpChatTopComponent.class, "CTL_AcpChatTopComponent"));
        setToolTipText(NbBundle.getMessage(AcpChatTopComponent.class, "HINT_AcpChatTopComponent"));
        connectButton.setText(Bundle.BTN_Connect());
        connectButton.setEnabled(false);
        statusLabel.setText(Bundle.STS_Disconnected());
        updateInputState();
        RP.post(() -> {
            try {
                connection.close();
            } finally {
                SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
            }
        });
    }

    private void toggleConnection() {
        if (currentConnection != null) {
            disconnect();
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
            statusLabel.setText(Bundle.STS_Connecting());
            connectButton.setEnabled(false);
            String fallbackDir = getMainProjectDirectory();
            long generation = ++connectionGeneration;
            pendingAgentName = agentName;
            RP.post(() -> {
                try {
                    AcpConnection connection = AcpConnectionManager.getDefault().connect(descriptor);
                    AcpSessionListener listener = createSessionListener(connection);
                    Set<String> enabledSet = descriptor.getEnabledTools();
                    String workDir = descriptor.getWorkingDirectory() != null
                            ? descriptor.getWorkingDirectory()
                            : fallbackDir != null ? fallbackDir : "";
                    String agentPrefix = descriptor.getCommandPrefix();
                    SwingUtilities.invokeLater(() -> {
                        if (generation != connectionGeneration) {
                            RP.post(() -> {
                                try {
                                    connection.close();
                                } finally {
                                    SwingUtilities.invokeLater(() -> connectButton.setEnabled(true));
                                }
                            });
                            return;
                        }
                        pendingAgentName = null;
                        connection.addSessionListener(listener);
                        connection.setPermissionHandler(req -> {
                            AcpPermissionDialog.PermissionResult pr =
                                    AcpPermissionDialog.showPermissionRequest(req);
                            if (pr.allowToolForSession() != null) {
                                connection.allowToolForSession(req.sessionId(), pr.allowToolForSession());
                            }
                            return pr.response();
                        });
                        connection.setElicitationHandler(this::handleElicitation);
                        connection.setTerminalAuthorizer(request -> confirmTerminal(connection, request));
                        currentListener = listener;
                        currentConnection = connection;
                        currentEnabledTools = enabledSet;
                        currentWorkingDir = workDir;
                        commandPrefix = agentPrefix;
                        setDisplayName(Bundle.LBL_TabTitle(agentName));
                        setToolTipText(Bundle.HINT_ConnectedAgent(agentName, workDir));
                        connectButton.setText(Bundle.BTN_Disconnect());
                        connectButton.setEnabled(true);
                        statusLabel.setText(Bundle.STS_Connected());
                        updateInputState();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        if (generation == connectionGeneration) {
                            pendingAgentName = null;
                        }
                        statusLabel.setText(generation == connectionGeneration
                                ? Bundle.STS_ErrorDetail(ex.getMessage()) : Bundle.STS_Disconnected());
                        connectButton.setEnabled(true);
                    });
                }
            });
        }
    }

    private boolean confirmTerminal(AcpConnection connection, CreateTerminalRequest request) {
        return Mutex.EVENT.readAccess(() -> {
            if (!isCurrentSession(connection, request.sessionId())) {
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
                    && isCurrentSession(connection, request.sessionId());
        });
    }

    private boolean isCurrentSession(AcpConnection connection, String sessionId) {
        return connection == currentConnection && sessionId != null
                && sessionId.equals(currentSessionId) && sessionTransition == null;
    }

    private void startNewSession() {
        AcpConnection connection = currentConnection;
        if (connection == null || !connection.isConnected() || sessionTransition != null || promptInProgress || cancellationRequested) {
            return;
        }
        SessionTransition transition = new SessionTransition(null);
        sessionTransition = transition;
        statusLabel.setText(Bundle.STS_CreatingSession());
        updateInputState();
        Project project = getContextProject();
        String mainDir = project != null ? project.getProjectDirectory().getPath() : null;
        List<String> openDirs = getOpenProjectDirectories();
        RP.post(() -> {
            try {
                String projectContext = collectProjectContext(project);
                String sessionId;
                try {
                    sessionId = connection.newSession(mainDir, openDirs);
                } catch (IOException ex) {
                    if (!isAuthenticationRequired(ex)) {
                        throw ex;
                    }
                    String methodId = chooseAuthenticationMethod(connection, transition);
                    if (methodId == null) {
                        SwingUtilities.invokeLater(() -> {
                            if (connection == currentConnection && transition == sessionTransition) {
                                sessionTransition = null;
                                statusLabel.setText(Bundle.STS_Connected());
                                updateInputState();
                            }
                        });
                        return;
                    }
                    try {
                        connection.authenticate(methodId);
                    } catch (IOException authFailure) {
                        Throwable cause = authFailure.getCause() != null ? authFailure.getCause() : authFailure;
                        throw new IOException(Bundle.MSG_AuthenticationFailed(cause.getMessage()), authFailure);
                    }
                    sessionId = connection.newSession(mainDir, openDirs);
                }
                String createdSession = sessionId;
                SwingUtilities.invokeLater(() -> finishSession(connection, transition, createdSession, projectContext));
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to create new session", ex);
                SwingUtilities.invokeLater(() -> {
                    if (connection == currentConnection && transition == sessionTransition) {
                        sessionTransition = null;
                        appendSystemMessage(Bundle.MSG_ErrorCreatingSession(
                                HtmlUtils.escapeHtml(String.valueOf(ex.getMessage()))));
                        statusLabel.setText(Bundle.STS_Error());
                        updateInputState();
                    }
                });
            }
        });
    }

    private static boolean isAuthenticationRequired(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof AcpError error && error.getCode() == AcpErrorCodes.AUTHENTICATION_REQUIRED) {
                return true;
            }
        }
        return false;
    }

    private String chooseAuthenticationMethod(AcpConnection connection, SessionTransition transition) {
        List<AcpSchema.AuthMethod> methods = connection.getAuthenticationMethods().stream()
                .filter(method -> method instanceof AcpSchema.AuthMethodAgent)
                .toList();
        return Mutex.EVENT.readAccess(() -> {
            if (connection != currentConnection || transition != sessionTransition) {
                return null;
            }
            if (methods.isEmpty()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        Bundle.MSG_AuthenticationUnavailable(), NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            JComboBox<String> choices = new JComboBox<>(methods.stream()
                    .map(method -> method.name() != null ? method.name() : method.id())
                    .toArray(String[]::new));
            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.add(new JLabel(Bundle.MSG_Authentication(connection.getDescriptor().getName())), BorderLayout.NORTH);
            panel.add(choices, BorderLayout.CENTER);
            JTextArea description = new JTextArea(3, 45);
            description.setEditable(false);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setOpaque(false);
            Runnable updateDescription = () -> {
                String text = methods.get(choices.getSelectedIndex()).description();
                description.setText(text != null ? text : "");
            };
            choices.addActionListener(event -> updateDescription.run());
            updateDescription.run();
            panel.add(description, BorderLayout.SOUTH);
            NotifyDescriptor.Confirmation dialog = new NotifyDescriptor.Confirmation(panel,
                    Bundle.TTL_Authentication(), NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(dialog) != NotifyDescriptor.OK_OPTION
                    || connection != currentConnection || transition != sessionTransition) {
                return null;
            }
            return methods.get(choices.getSelectedIndex()).id();
        });
    }

    private static String collectProjectContext(Project project) {
        try {
            return AcpProjectContextProvider.gatherProjectContext(project);
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to gather project context", ex);
            return null;
        }
    }

    private void finishSession(AcpConnection connection, SessionTransition transition,
            String sessionId, String projectContext) {
        if (connection != currentConnection || transition != sessionTransition) {
            return;
        }
        sessionTransition = null;
        if (currentSessionId != null && !currentSessionId.equals(sessionId)) {
            inputArea.setText("");
        }
        currentSessionId = sessionId;
        pendingProjectContext = projectContext;
        renderTimer.stop();
        displayTimer.stop();
        currentMessageBuffer.setLength(0);
        currentThoughtBuffer.setLength(0);
        inAgentMessage = false;
        agentMsgDivEndPos = -1;
        diffStore.clear();
        chatHtml.setLength(0);
        renderedLength = 0;
        chatPane.setText("<html><body></body></html>");
        resetSessionControls();
        updateToolsButton(countTools(currentEnabledTools));
        List<AcpModeInfo> modes = connection.getAvailableModes();
        modeButton.setEnabled(!modes.isEmpty());
        currentModeId = connection.getCurrentModeId();
        if (currentModeId != null) {
            modeButton.setText(Bundle.BTN_Mode(getModeDisplayName(currentModeId)));
        }
        statusLabel.setText(Bundle.STS_Ready());
        if (transition.expectedId != null) {
            appendSystemMessage(Bundle.MSG_SessionResumed());
        }
        transition.updates.getOrDefault(sessionId, List.of()).forEach(Runnable::run);
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
        AcpConnection connection = currentConnection;
        String sessionId = currentSessionId;
        if (text.isEmpty() || connection == null || sessionId == null
                || sessionTransition != null || promptInProgress || cancellationRequested) {
            return;
        }
        List<AcpFileMentionSupport.FileAttachment> files = mentionSupport.consumeAttachedFiles();
        String context = pendingProjectContext;
        String projectContext = context != null ? context + "\n\n" : "";
        String fullPrompt = projectContext + mentionSupport.buildAttachmentContext(files) + text;
        pendingProjectContext = null;
        appendUserMessage(text);
        inputArea.setText("");
        promptInProgress = true;
        updateInputState();
        statusLabel.setText(Bundle.STS_Thinking());

        RP.post(() -> {
            try {
                connection.sendPrompt(sessionId, fullPrompt);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    if (isCurrentSession(connection, sessionId)) {
                        appendSystemMessage(Bundle.STS_ErrorDetail(HtmlUtils.escapeHtml(ex.getMessage())));
                        pendingProjectContext = context;
                        promptInProgress = false;
                        updateInputState();
                        statusLabel.setText(Bundle.STS_Error());
                    }
                });
            }
        });
    }

    private void cancelPrompt() {
        AcpConnection connection = currentConnection;
        String sessionId = currentSessionId;
        if (connection == null || sessionId == null || !promptInProgress || cancellationRequested) {
            return;
        }
        cancellationRequested = true;
        updateInputState();
        RP.post(() -> {
            try {
                connection.cancel(sessionId);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to cancel prompt", ex);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    if (isCurrentSession(connection, sessionId)) {
                        cancellationRequested = false;
                        updateInputState();
                    }
                });
            }
        });
    }

    private void updateInputState() {
        boolean connected = currentConnection != null && currentConnection.isConnected();
        boolean idle = connected && sessionTransition == null && !promptInProgress && !cancellationRequested;
        boolean hasSession = connected && currentSessionId != null;
        sendButton.setEnabled(idle && hasSession);
        inputArea.setEnabled(idle && hasSession);
        cancelButton.setEnabled(promptInProgress && hasSession && !cancellationRequested);
        newSessionButton.setEnabled(idle && !hasSession);
        sessionsButton.setEnabled(idle);
    }

    private void appendUserMessage(String text) {
        chatHtml.append("<div class='user-msg'><span class='user-label'>").append(Bundle.LBL_You()).append("</span><br/>")
                .append(HtmlUtils.escapeHtml(text).replace("\n", "<br/>"))
                .append("</div>");
        updateChatDisplay();
    }

    private void appendSystemMessage(String html) {
        chatHtml.append("<div class='system-msg'>").append(html).append("</div>");
        updateChatDisplay();
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
        Map<String, java.util.List<McpToolProvider.McpToolDefinition>> toolsByProvider =
                registry.getToolsByProvider();
        if (toolsByProvider.isEmpty()) {
            return;
        }
        Set<String> enabledSet = currentEnabledTools;
        JPopupMenu popup = new JPopupMenu();
        boolean first = true;
        for (Map.Entry<String, java.util.List<McpToolProvider.McpToolDefinition>> group
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
        if (currentConnection != null) {
            for (AcpModeInfo mode : currentConnection.getAvailableModes()) {
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
        AcpConnection connection = currentConnection;
        String sessionId = currentSessionId;
        if (connection == null || !connection.isConnected() || sessionId == null || sessionTransition != null) {
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
                if (isCurrentSession(connection, sessionId)) {
                    RP.post(() -> {
                        try {
                            connection.setSessionMode(sessionId, mode.id());
                        } catch (Exception ex) {
                            LOG.log(Level.WARNING, "Failed to set mode", ex);
                        }
                    });
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
                java.awt.Rectangle caret = inputArea.modelToView(
                        inputArea.getCaretPosition());
                if (caret != null) {
                    popup.show(inputArea, caret.x, caret.y + caret.height);
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
        AcpConnection connection = currentConnection;
        if (connection == null || !connection.isConnected() || promptInProgress || cancellationRequested || sessionTransition != null) {
            return;
        }
        String mainDir = getMainProjectDirectory();
        RP.post(() -> {
            List<AcpSessionInfo> sessions;
            try {
                sessions = connection.listSessions(mainDir);
            } catch (IOException ex) {
                handleSessionListError(ex);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                if (connection != currentConnection || promptInProgress || cancellationRequested || sessionTransition != null) {
                    return;
                }
                if (sessions.isEmpty()) {
                    appendSystemMessage(Bundle.LBL_NoSessions());
                    return;
                }
                showSessionBrowserDialog(sessions, connection);
            });
        });
    }

    private void handleSessionListError(IOException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof AcpCapabilityException) {
            SwingUtilities.invokeLater(() ->
                    appendSystemMessage(Bundle.MSG_NoSessionCapability()));
        } else {
            LOG.log(Level.WARNING, "Failed to list sessions", ex);
            SwingUtilities.invokeLater(() ->
                    appendSystemMessage(Bundle.STS_ErrorDetail(
                            HtmlUtils.escapeHtml(String.valueOf(ex.getMessage())))));
        }
    }

    private void showSessionBrowserDialog(List<AcpSessionInfo> sessions, AcpConnection connection) {
        DefaultListModel<AcpSessionInfo> listModel = new DefaultListModel<>();
        JList<AcpSessionInfo> sessionList = buildSessionList(sessions, listModel,
                connection.getDescriptor().getName());

        JPanel browserPanel = new JPanel(new BorderLayout(4, 4));
        browserPanel.setPreferredSize(new Dimension(SESSION_BROWSER_WIDTH, SESSION_BROWSER_HEIGHT));
        browserPanel.add(new JScrollPane(sessionList), BorderLayout.CENTER);

        JButton resumeBtn = new JButton(Bundle.BTN_ResumeSession());
        resumeBtn.setEnabled(connection.supportsResumeSession());
        JButton deleteBtn = new JButton(Bundle.BTN_DeleteSession());
        deleteBtn.setEnabled(connection.supportsDeleteSession());
        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.add(resumeBtn);
        buttonRow.add(Box.createHorizontalStrut(8));
        buttonRow.add(deleteBtn);
        browserPanel.add(buttonRow, BorderLayout.SOUTH);

        DialogDescriptor dd = new DialogDescriptor(browserPanel, Bundle.TTL_SessionBrowser(),
                true, new Object[]{NotifyDescriptor.CANCEL_OPTION}, NotifyDescriptor.CANCEL_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        resumeBtn.addActionListener(ev -> {
            AcpSessionInfo selected = sessionList.getSelectedValue();
            if (selected != null) {
                dialog.dispose();
                onResumeSession(selected, connection);
            }
        });

        deleteBtn.addActionListener(ev -> {
            AcpSessionInfo selected = sessionList.getSelectedValue();
            if (selected != null) {
                onDeleteSession(selected, listModel, connection, dialog);
            }
        });

        dialog.setVisible(true);
    }

    private JList<AcpSessionInfo> buildSessionList(List<AcpSessionInfo> sessions,
            DefaultListModel<AcpSessionInfo> listModel, String agentName) {
        for (AcpSessionInfo si : sessions) {
            listModel.addElement(si);
        }
        JList<AcpSessionInfo> sessionList = new JList<>(listModel);
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionList.setCellRenderer(new SessionListCellRenderer(agentName));
        sessionList.setSelectedIndex(0);
        return sessionList;
    }

    private void onResumeSession(AcpSessionInfo session, AcpConnection connection) {
        if (connection != currentConnection || sessionTransition != null || promptInProgress || cancellationRequested) {
            return;
        }
        SessionTransition transition = new SessionTransition(session.sessionId());
        sessionTransition = transition;
        statusLabel.setText(Bundle.STS_ResumingSession());
        updateInputState();
        String mainDir = getMainProjectDirectory();
        List<String> openDirs = getOpenProjectDirectories();
        RP.post(() -> {
            try {
                String sessionId = connection.resumeSession(session.sessionId(), mainDir, openDirs);
                SwingUtilities.invokeLater(() -> finishSession(connection, transition, sessionId, null));
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to resume session", ex);
                SwingUtilities.invokeLater(() -> {
                    if (connection == currentConnection && transition == sessionTransition) {
                        sessionTransition = null;
                        statusLabel.setText(Bundle.STS_Error());
                        updateInputState();
                        DialogDisplayer.getDefault().notifyLater(
                                new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                    }
                });
            }
        });
    }

    private void onDeleteSession(AcpSessionInfo session,
            DefaultListModel<AcpSessionInfo> listModel,
            AcpConnection connection, Dialog dialog) {
        if (connection != currentConnection || promptInProgress || cancellationRequested || sessionTransition != null) {
            return;
        }
        RP.post(() -> {
            try {
                connection.deleteSession(session.sessionId());
                SwingUtilities.invokeLater(() -> {
                    listModel.removeElement(session);
                    if (listModel.isEmpty()) {
                        dialog.dispose();
                    }
                    if (connection == currentConnection) {
                        if (session.sessionId().equals(currentSessionId)) {
                            currentSessionId = null;
                            pendingProjectContext = null;
                            inputArea.setText("");
                            resetSessionControls();
                            updateInputState();
                        }
                        appendSystemMessage(Bundle.MSG_SessionDeleted());
                    }
                });
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to delete session", ex);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(Bundle.STS_Error());
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(ex.getMessage(),
                                    NotifyDescriptor.ERROR_MESSAGE));
                });
            }
        });
    }

    private static final class SessionListCellRenderer extends DefaultListCellRenderer {

        private final String agentName;

        SessionListCellRenderer(String agentName) {
            this.agentName = agentName;
        }

        private static final java.time.format.DateTimeFormatter DISPLAY_FORMAT =
                java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AcpSessionInfo si) {
                String title = si.title() != null && !si.title().isBlank() ? si.title() : si.sessionId();
                String label = Bundle.LBL_SessionEntry(agentName, title);
                if (si.updatedAt() != null) {
                    label += "  (" + formatDate(si.updatedAt()) + ")";
                }
                setText(label);
            }
            return this;
        }

        private static String formatDate(String iso) {
            try {
                return java.time.ZonedDateTime.parse(iso)
                        .withZoneSameInstant(java.time.ZoneId.systemDefault())
                        .format(DISPLAY_FORMAT);
            } catch (java.time.format.DateTimeParseException ex) {
                try {
                    return java.time.LocalDateTime.parse(iso).format(DISPLAY_FORMAT);
                } catch (java.time.format.DateTimeParseException ex2) {
                    return iso;
                }
            }
        }
    }

    private void flushMessageBuffers() {
        String thought = currentThoughtBuffer.toString();
        String message = currentMessageBuffer.toString();
        currentThoughtBuffer.setLength(0);
        currentMessageBuffer.setLength(0);
        boolean changed = false;
        if (!thought.isEmpty()) {
            chatHtml.append("<div class='thought'>")
                    .append(HtmlUtils.escapeHtml(thought))
                    .append("</div>");
            changed = true;
        }
        if (!message.isEmpty()) {
            String rendered = AcpMarkdownRenderer.renderToHtml(message);
            if (inAgentMessage && agentMsgDivEndPos >= 0
                    && chatHtml.length() == agentMsgDivEndPos + "</div>".length()) {
                chatHtml.setLength(agentMsgDivEndPos);
                chatHtml.append(rendered).append("</div>");
                renderedLength = 0;
            } else {
                chatHtml.append("<div class='agent-msg'><span class='agent-label'>")
                        .append(Bundle.LBL_AgentSays()).append("</span><br/>")
                        .append(rendered).append("</div>");
                inAgentMessage = true;
            }
            agentMsgDivEndPos = chatHtml.length() - "</div>".length();
            changed = true;
        }
        if (changed) {
            updateChatDisplay();
        }
    }

    private void appendToolCall(AcpToolCall toolCall) {
        flushMessageBuffers();
        inAgentMessage = false;
        agentMsgDivEndPos = -1;
        renderToolCallCard(toolCall);
    }

    private void appendToolCallUpdate(AcpToolCall toolCall) {
        flushMessageBuffers();
        inAgentMessage = false;
        agentMsgDivEndPos = -1;
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
                .append(kindIcon).append(" <b>").append(HtmlUtils.escapeHtml(toolCall.getTitle())).append("</b>")
                .append(" <span class='tc-kind'>[").append(HtmlUtils.escapeHtml(toolCall.getKind())).append("]</span>")
                .append(" ").append(statusIcon)
                .append(" <span class='").append(statusClass).append("'>")
                .append(HtmlUtils.escapeHtml(toolCall.getStatus())).append("</span>");

        if (toolCall.getName() != null && !toolCall.getName().equals(toolCall.getTitle())) {
            chatHtml.append("<br/><i>").append(HtmlUtils.escapeHtml(toolCall.getName())).append("</i>");
        }

        if (!toolCall.getLocations().isEmpty()) {
            chatHtml.append("<br/>");
            for (AcpToolCall.Location loc : toolCall.getLocations()) {
                String navUrl = "acp-nav:" + loc.path()
                        + (loc.line() != null ? ":" + loc.line() : "");
                String display = HtmlUtils.escapeHtml(loc.path())
                        + (loc.line() != null ? ":" + loc.line() : "");
                chatHtml.append("<a class='tc-location' href='")
                        .append(navUrl).append("'>")
                        .append(display)
                        .append("</a> ");
            }
        }

        if (!toolCall.getDiffs().isEmpty()) {
            for (AcpToolCall.DiffContent diff : toolCall.getDiffs()) {
                String diffId = "diff-" + (diffCounter++);
                diffStore.put(diffId, diff);

                chatHtml.append("<br/>");
                if (diff.path() != null) {
                    chatHtml.append("<b>").append(HtmlUtils.escapeHtml(diff.path())).append("</b><br/>");
                }
                appendDiffPreview(diff);
                chatHtml.append("<br/><a class='diff-link' href='acp-diff:")
                        .append(diffId).append("'>").append(Bundle.LBL_ViewFullDiff()).append("</a>");
            }
        }

        if (toolCall.getTextContent() != null && !toolCall.getTextContent().isEmpty()) {
            String preview = toolCall.getTextContent();
            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }
            chatHtml.append("<br/><code>").append(HtmlUtils.escapeHtml(preview)).append("</code>");
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
                    .append(HtmlUtils.escapeHtml(line)).append("</span><br/>");
            previewLines++;
        }
        previewLines = 0;
        for (String line : newLines) {
            if (previewLines >= maxPreview) {
                chatHtml.append("<span class='diff-add'>...</span><br/>");
                break;
            }
            chatHtml.append("<span class='diff-add'>+ ")
                    .append(HtmlUtils.escapeHtml(line)).append("</span><br/>");
            previewLines++;
        }
    }

    private void appendPlan(List<AcpPlanEntry> entries) {
        chatHtml.append("<div class='plan'><b>").append(Bundle.LBL_Plan()).append("</b><ul>");
        for (AcpPlanEntry entry : entries) {
            String icon = switch (entry.status()) {
                case "completed" -> "&#x2705;";
                case "in_progress" -> "&#x23F3;";
                default -> "&#x25CB;";
            };
            chatHtml.append("<li>").append(icon).append(" ")
                    .append(HtmlUtils.escapeHtml(entry.content()))
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
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, chatPane);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationResponse handleElicitation(
            com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationRequest request) {
        java.util.concurrent.atomic.AtomicReference<com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationResponse> result =
                new java.util.concurrent.atomic.AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                JPanel formPanel = new JPanel();
                formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
                formPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                if (request.message() != null && !request.message().isEmpty()) {
                    JLabel msgLabel = new JLabel("<html><p style='width:350px'>"
                            + HtmlUtils.escapeHtml(request.message()) + "</p></html>");
                    formPanel.add(msgLabel);
                    formPanel.add(Box.createVerticalStrut(8));
                }

                Map<String, javax.swing.JComponent> fieldComponents = new java.util.LinkedHashMap<>();
                com.agentclientprotocol.sdk.spec.AcpSchema.ElicitationSchema schema = request.requestedSchema();
                if (schema != null && schema.properties() != null) {
                    for (Map.Entry<String, com.agentclientprotocol.sdk.spec.AcpSchema.ElicitationPropertySchema> entry
                            : schema.properties().entrySet()) {
                        String fieldName = entry.getKey();
                        com.agentclientprotocol.sdk.spec.AcpSchema.ElicitationPropertySchema prop = entry.getValue();

                        if (prop instanceof com.agentclientprotocol.sdk.spec.AcpSchema.StringPropertySchema sp) {
                            String label = sp.title() != null ? sp.title() : fieldName;
                            formPanel.add(new JLabel(label + ":"));
                            if (sp.enumValues() != null && !sp.enumValues().isEmpty()) {
                                JComboBox<String> combo = new JComboBox<>(sp.enumValues().toArray(new String[0]));
                                if (sp.defaultValue() != null) {
                                    combo.setSelectedItem(sp.defaultValue());
                                }
                                fieldComponents.put(fieldName, combo);
                                formPanel.add(combo);
                            } else if (sp.oneOf() != null && !sp.oneOf().isEmpty()) {
                                List<String> values = new ArrayList<>();
                                for (com.agentclientprotocol.sdk.spec.AcpSchema.EnumOption eo : sp.oneOf()) {
                                    values.add(eo.constValue());
                                }
                                JComboBox<String> combo = new JComboBox<>(values.toArray(new String[0]));
                                if (sp.defaultValue() != null) {
                                    combo.setSelectedItem(sp.defaultValue());
                                }
                                fieldComponents.put(fieldName, combo);
                                formPanel.add(combo);
                            } else {
                                JTextField tf = new JTextField(25);
                                if (sp.defaultValue() != null) {
                                    tf.setText(sp.defaultValue());
                                }
                                fieldComponents.put(fieldName, tf);
                                formPanel.add(tf);
                            }
                        } else if (prop instanceof com.agentclientprotocol.sdk.spec.AcpSchema.BooleanPropertySchema bp) {
                            String label = bp.title() != null ? bp.title() : fieldName;
                            JCheckBox cb = new JCheckBox(label);
                            if (bp.defaultValue() != null) {
                                cb.setSelected(bp.defaultValue());
                            }
                            fieldComponents.put(fieldName, cb);
                            formPanel.add(cb);
                        } else {
                            formPanel.add(new JLabel(fieldName + ":"));
                            JTextField tf = new JTextField(25);
                            fieldComponents.put(fieldName, tf);
                            formPanel.add(tf);
                        }
                        formPanel.add(Box.createVerticalStrut(4));
                    }
                }

                String title = schema != null && schema.title() != null
                        ? schema.title() : Bundle.TTL_Elicitation();
                Object[] options = {Bundle.BTN_Accept(), Bundle.BTN_Decline()};
                DialogDescriptor dd = new DialogDescriptor(
                        new JScrollPane(formPanel), title,
                        true, options, Bundle.BTN_Accept(),
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                Object chosen = DialogDisplayer.getDefault().notify(dd);

                if (Bundle.BTN_Accept().equals(chosen)) {
                    Map<String, Object> content = new java.util.LinkedHashMap<>();
                    for (Map.Entry<String, javax.swing.JComponent> fe : fieldComponents.entrySet()) {
                        javax.swing.JComponent comp = fe.getValue();
                        if (comp instanceof JTextField tf) {
                            content.put(fe.getKey(), tf.getText());
                        } else if (comp instanceof JComboBox<?> cb) {
                            content.put(fe.getKey(), cb.getSelectedItem());
                        } else if (comp instanceof JCheckBox chk) {
                            content.put(fe.getKey(), chk.isSelected());
                        }
                    }
                    result.set(com.agentclientprotocol.sdk.spec.AcpSchema
                            .CreateElicitationResponse.accept(content));
                } else {
                    result.set(com.agentclientprotocol.sdk.spec.AcpSchema
                            .CreateElicitationResponse.decline());
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Elicitation dialog error", ex);
            result.set(com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationResponse.decline());
        }
        return result.get();
    }

    private static String getMainProjectDirectory() {
        Project project = getContextProject();
        return project != null ? project.getProjectDirectory().getPath() : null;
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
            dirs.add(p.getProjectDirectory().getPath());
        }
        return dirs;
    }
}
