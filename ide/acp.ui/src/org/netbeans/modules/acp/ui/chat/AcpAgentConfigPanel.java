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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentDescriptor.TransportType;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.McpToolRegistry;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "LBL_AgentName=Name:",
    "LBL_Transport=Transport:",
    "LBL_Command=Command:",
    "LBL_Url=URL:",
    "LBL_WorkingDir=Working Directory:",
    "LBL_EnvVars=Environment Variables:",
    "LBL_EnvVarsHint=(one KEY=VALUE per line)",
    "LBL_Browse=Browse...",
    "LBL_Tools=Exposed Tools:"
})
public final class AcpAgentConfigPanel extends JPanel {

    private final JTextField nameField;
    private final JComboBox<TransportType> transportCombo;
    private final JTextField commandField;
    private final JLabel commandLabel;
    private final JTextField urlField;
    private final JLabel urlLabel;
    private final JTextField workingDirField;
    private final JLabel workingDirLabel;
    private final JButton browseButton;
    private final JTextArea envVarsArea;
    private final JLabel envVarsLabel;
    private final List<JCheckBox> toolCheckBoxes = new ArrayList<>();

    public AcpAgentConfigPanel() {
        this(null);
    }

    public AcpAgentConfigPanel(AcpAgentDescriptor existing) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel(Bundle.LBL_AgentName()), gbc);
        nameField = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(nameField, gbc);

        // Transport
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel(Bundle.LBL_Transport()), gbc);
        transportCombo = new JComboBox<>(TransportType.values());
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(transportCombo, gbc);

        // Command (for STDIO)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        commandLabel = new JLabel(Bundle.LBL_Command());
        add(commandLabel, gbc);
        commandField = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(commandField, gbc);

        // URL (for WEBSOCKET)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        urlLabel = new JLabel(Bundle.LBL_Url());
        add(urlLabel, gbc);
        urlField = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(urlField, gbc);

        // Working Directory
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        workingDirLabel = new JLabel(Bundle.LBL_WorkingDir());
        add(workingDirLabel, gbc);
        workingDirField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(workingDirField, gbc);
        browseButton = new JButton(Bundle.LBL_Browse());
        browseButton.addActionListener(e -> browseWorkingDir());
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(browseButton, gbc);

        // Environment Variables
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        envVarsLabel = new JLabel(Bundle.LBL_EnvVars());
        add(envVarsLabel, gbc);
        envVarsArea = new JTextArea(4, 25);
        envVarsArea.setToolTipText(Bundle.LBL_EnvVarsHint());
        JScrollPane envScroll = new JScrollPane(envVarsArea);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(envScroll, gbc);
        row++;
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel(Bundle.LBL_EnvVarsHint()), gbc);

        // Tools
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel(Bundle.LBL_Tools()), gbc);
        JPanel toolsPanel = new JPanel();
        toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.Y_AXIS));
        McpToolRegistry registry = McpToolRegistry.getInstance();
        Set<String> existingEnabled = existing != null ? existing.getEnabledTools() : null;
        Map<String, List<McpToolProvider.McpToolDefinition>> toolsByProvider =
                registry.getToolsByProvider();
        for (Map.Entry<String, List<McpToolProvider.McpToolDefinition>> group
                : toolsByProvider.entrySet()) {
            JLabel groupLabel = new JLabel(group.getKey());
            groupLabel.setFont(groupLabel.getFont().deriveFont(java.awt.Font.BOLD));
            groupLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
            toolsPanel.add(groupLabel);
            for (McpToolProvider.McpToolDefinition tool : group.getValue()) {
                JCheckBox cb = new JCheckBox(tool.name());
                cb.setToolTipText(tool.description());
                if (existingEnabled != null) {
                    cb.setSelected(existingEnabled.contains(tool.name()));
                } else {
                    cb.setSelected(registry.isToolEnabledByDefault(tool.name()));
                }
                toolCheckBoxes.add(cb);
                toolsPanel.add(cb);
            }
        }
        JScrollPane toolsScroll = new JScrollPane(toolsPanel);
        toolsScroll.setPreferredSize(new java.awt.Dimension(0, 120));
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(toolsScroll, gbc);

        transportCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateTransportVisibility();
            }
        });

        if (existing != null) {
            nameField.setText(existing.getName());
            nameField.setEditable(false);
            transportCombo.setSelectedItem(existing.getTransportType());
            if (!existing.getCommand().isEmpty()) {
                commandField.setText(String.join(" ", existing.getCommand()));
            }
            urlField.setText(existing.getUrl() != null ? existing.getUrl() : "");
            workingDirField.setText(existing.getWorkingDirectory() != null ? existing.getWorkingDirectory() : "");
            StringBuilder envText = new StringBuilder();
            for (Map.Entry<String, String> entry : existing.getEnvironmentVariables().entrySet()) {
                envText.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
            }
            envVarsArea.setText(envText.toString().trim());
        }

        updateTransportVisibility();
    }

    private void updateTransportVisibility() {
        boolean stdio = transportCombo.getSelectedItem() == TransportType.STDIO;
        commandLabel.setVisible(stdio);
        commandField.setVisible(stdio);
        workingDirLabel.setVisible(stdio);
        workingDirField.setVisible(stdio);
        browseButton.setVisible(stdio);
        urlLabel.setVisible(!stdio);
        urlField.setVisible(!stdio);
    }

    private void browseWorkingDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String current = workingDirField.getText().trim();
        if (!current.isEmpty()) {
            File dir = new File(current);
            if (dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            workingDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public AcpAgentDescriptor buildDescriptor() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            return null;
        }
        TransportType transport = (TransportType) transportCombo.getSelectedItem();
        AcpAgentDescriptor.Builder builder = new AcpAgentDescriptor.Builder(name, transport);

        if (transport == TransportType.STDIO) {
            String cmd = commandField.getText().trim();
            if (cmd.isEmpty()) {
                return null;
            }
            builder.command(Arrays.asList(cmd.split("\\s+")));
            String workDir = workingDirField.getText().trim();
            if (!workDir.isEmpty()) {
                builder.workingDirectory(workDir);
            }
        } else {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                return null;
            }
            builder.url(url);
        }

        String envText = envVarsArea.getText().trim();
        if (!envText.isEmpty()) {
            Map<String, String> envVars = new LinkedHashMap<>();
            for (String line : envText.split("\n")) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    envVars.put(line.substring(0, eqIdx), line.substring(eqIdx + 1));
                }
            }
            if (!envVars.isEmpty()) {
                builder.environmentVariables(envVars);
            }
        }

        Set<String> enabledTools = new LinkedHashSet<>();
        for (JCheckBox cb : toolCheckBoxes) {
            if (cb.isSelected()) {
                enabledTools.add(cb.getText());
            }
        }
        builder.enabledTools(enabledTools);

        return builder.build();
    }
}
