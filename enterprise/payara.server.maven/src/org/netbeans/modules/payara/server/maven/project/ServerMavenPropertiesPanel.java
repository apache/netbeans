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
package org.netbeans.modules.payara.server.maven.project;

import java.awt.BorderLayout;
import java.io.File;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.*;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Project Properties panel for the Payara Server Maven Plugin,
 * organised into collapsible sections.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public class ServerMavenPropertiesPanel extends JPanel implements Scrollable {

    private final Preferences pref;

    private static final RequestProcessor RP = new RequestProcessor(ServerMavenPropertiesPanel.class);

    // Server Configuration
    private final JComboBox<String> serverVersionCombo = new JComboBox<>();
    private final JTextField serverPathField     = new JTextField();
    private final JTextField domainNameField     = new JTextField();
    private final JTextField contextRootField    = new JTextField();
    private final JTextField instanceNameField   = new JTextField();

    // Remote Connection
    private final JCheckBox  remoteCheckBox      = new JCheckBox();
    private final JTextField hostNameField       = new JTextField();
    private final JTextField adminPortField      = new JTextField();
    private final JTextField adminUserField      = new JTextField();

    // Dev Mode
    private final JCheckBox  explodedCheckBox    = new JCheckBox();
    private final JCheckBox  hotDeployCheckBox   = new JCheckBox();
    private final JCheckBox  autoDeployCheckBox  = new JCheckBox();
    private final JCheckBox  liveReloadCheckBox  = new JCheckBox();
    private final JCheckBox  keepStateCheckBox   = new JCheckBox();
    private final JCheckBox  trimLogCheckBox     = new JCheckBox();
    private final JCheckBox  ignoreTestCheckBox  = new JCheckBox();

    // AI Agent
    private final JCheckBox  aiAgentCheckBox       = new JCheckBox();
    private final JTextField aiApiKeyField         = new JTextField();
    private final JComboBox<String> aiProviderCombo = new JComboBox<>();
    private final JTextField aiProviderLocField    = new JTextField();
    private final JComboBox<String> aiModelCombo   = new JComboBox<>();

    public ServerMavenPropertiesPanel(ModelHandle2 handle, Project project) {
        pref = getPreferences(project, ServerMavenApplication.class, true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        remoteCheckBox.addActionListener(e -> updateRemoteEnabled());

        // ── Section 1: Server Configuration ──────────────────────────────────
        CollapsibleSection serverSection = new CollapsibleSection(msg("section.server"), true);
        serverVersionCombo.setEditable(true);
        serverVersionCombo.addItem("Loading...");
        serverSection.addComboRow(msg("serverVersionLabel.text"), serverVersionCombo);

        JButton serverPathBrowseButton = new JButton("...");
        serverPathBrowseButton.setToolTipText(msg("serverPathBrowse.tooltip"));
        serverPathBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String current = serverPathField.getText().trim();
            if (!current.isEmpty()) {
                chooser.setCurrentDirectory(new File(current));
            }
            if (chooser.showOpenDialog(ServerMavenPropertiesPanel.this) == JFileChooser.APPROVE_OPTION) {
                serverPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        JPanel serverPathPanel = new JPanel(new BorderLayout(4, 0));
        serverPathPanel.setOpaque(false);
        serverPathPanel.add(serverPathField,        BorderLayout.CENTER);
        serverPathPanel.add(serverPathBrowseButton, BorderLayout.EAST);
        serverSection.addComponentRow(msg("serverPathLabel.text"), serverPathPanel);
        serverSection.addRow(msg("domainNameLabel.text"),    domainNameField);
        serverSection.addRow(msg("contextRootLabel.text"),   contextRootField);
        serverSection.addRow(msg("instanceNameLabel.text"),  instanceNameField);

        // ── Section 2: Remote Connection ──────────────────────────────────────
        CollapsibleSection remoteSection = new CollapsibleSection(msg("section.remote"), false);
        remoteSection.addCheckRow(msg("remoteLabel.text"),    remoteCheckBox);
        remoteSection.addRow(msg("hostNameLabel.text"),       hostNameField);
        remoteSection.addRow(msg("adminPortLabel.text"),      adminPortField);
        remoteSection.addRow(msg("adminUserLabel.text"),      adminUserField);

        // ── Section 3: Dev Mode ───────────────────────────────────────────────
        CollapsibleSection devSection = new CollapsibleSection(msg("section.devmode"), true);
        devSection.addCheckRow(msg("explodedLabel.text"),     explodedCheckBox);
        devSection.addCheckRow(msg("hotDeployLabel.text"),    hotDeployCheckBox);
        devSection.addCheckRow(msg("autoDeployLabel.text"),   autoDeployCheckBox);
        devSection.addCheckRow(msg("liveReloadLabel.text"),   liveReloadCheckBox);
        devSection.addCheckRow(msg("keepStateLabel.text"),    keepStateCheckBox);
        devSection.addCheckRow(msg("trimLogLabel.text"),      trimLogCheckBox);
        devSection.addCheckRow(msg("ignoreTestLabel.text"),   ignoreTestCheckBox);

        // ── Section 4: AI Agent ───────────────────────────────────────────────
        CollapsibleSection aiSection = new CollapsibleSection(msg("section.ai"), false);
        aiSection.addCheckRow(msg("aiAgentLabel.text"),       aiAgentCheckBox);
        aiSection.addRow(msg("aiApiKeyLabel.text"),            aiApiKeyField);
        for (String p : AI_PROVIDERS) aiProviderCombo.addItem(p);
        aiProviderCombo.setEditable(true);
        aiSection.addComboRow(msg("aiProviderLabel.text"),     aiProviderCombo);
        aiSection.addRow(msg("aiProviderLocLabel.text"),       aiProviderLocField);
        aiModelCombo.setEditable(true);
        aiModelCombo.addItem("");
        aiSection.addComboRow(msg("aiModelLabel.text"),        aiModelCombo);

        add(serverSection);
        add(Box.createVerticalStrut(6));
        add(remoteSection);
        add(Box.createVerticalStrut(6));
        add(devSection);
        add(Box.createVerticalStrut(6));
        add(aiSection);
        add(Box.createVerticalGlue());

        loadPreferences();
        updateRemoteEnabled();
    }

    private void loadPreferences() {
        loadServerVersions(pref.get(PREF_SERVER_VERSION, ""));
        serverPathField.setText(pref.get(PREF_SERVER_PATH, ""));
        domainNameField.setText(pref.get(PREF_DOMAIN_NAME, ""));
        contextRootField.setText(pref.get(PREF_CONTEXT_ROOT, ""));
        instanceNameField.setText(pref.get(PREF_INSTANCE_NAME, ""));

        remoteCheckBox.setSelected(pref.getBoolean(PREF_REMOTE, false));
        hostNameField.setText(pref.get(PREF_HOST_NAME, ""));
        adminPortField.setText(pref.get(PREF_ADMIN_PORT, ""));
        adminUserField.setText(pref.get(PREF_ADMIN_USER, ""));

        explodedCheckBox.setSelected(pref.getBoolean(PREF_EXPLODED,    true));
        hotDeployCheckBox.setSelected(pref.getBoolean(PREF_HOT_DEPLOY,  false));
        autoDeployCheckBox.setSelected(pref.getBoolean(PREF_AUTO_DEPLOY, true));
        liveReloadCheckBox.setSelected(pref.getBoolean(PREF_LIVE_RELOAD, true));
        keepStateCheckBox.setSelected(pref.getBoolean(PREF_KEEP_STATE,   true));
        trimLogCheckBox.setSelected(pref.getBoolean(PREF_TRIM_LOG,       true));
        ignoreTestCheckBox.setSelected(pref.getBoolean(PREF_IGNORE_TEST,  true));
        aiAgentCheckBox.setSelected(pref.getBoolean(PREF_AI_AGENT, false));
        String storedKey = pref.get(PREF_AI_API_KEY, "");
        aiApiKeyField.setText(storedKey);
        String envKey = System.getenv(ENV_AI_API_KEY);
        if (envKey != null && !envKey.isEmpty()) {
            String tip = storedKey.isEmpty()
                    ? "Using $" + ENV_AI_API_KEY + " env var (enter a key here to override it)"
                    : "This key overrides the $" + ENV_AI_API_KEY + " env var";
            aiApiKeyField.setToolTipText(tip);
        }
        String savedProvider = pref.get(PREF_AI_PROVIDER, "");
        if (!savedProvider.isEmpty()) aiProviderCombo.setSelectedItem(savedProvider);
        // Listen to provider changes to update model list
        aiProviderCombo.addActionListener(e -> {
            Object prov  = aiProviderCombo.getSelectedItem();
            Object model = aiModelCombo.getSelectedItem();
            loadModelsForProvider(
                    prov  != null ? prov.toString()  : "",
                    model != null ? model.toString() : "");
        });
        loadModelsForProvider(savedProvider, pref.get(PREF_AI_MODEL, ""));
        aiProviderLocField.setText(pref.get(PREF_AI_PROVIDER_LOC, ""));
    }

    public void applyChanges() {
        Object selVer = serverVersionCombo.getSelectedItem();
        pref.put(PREF_SERVER_VERSION, selVer != null ? selVer.toString().trim() : "");
        pref.put(PREF_SERVER_PATH,    serverPathField.getText().trim());
        pref.put(PREF_DOMAIN_NAME,    domainNameField.getText().trim());
        pref.put(PREF_CONTEXT_ROOT,   contextRootField.getText().trim());
        pref.put(PREF_INSTANCE_NAME,  instanceNameField.getText().trim());

        pref.putBoolean(PREF_REMOTE,   remoteCheckBox.isSelected());
        pref.put(PREF_HOST_NAME,  hostNameField.getText().trim());
        pref.put(PREF_ADMIN_PORT, adminPortField.getText().trim());
        pref.put(PREF_ADMIN_USER, adminUserField.getText().trim());

        pref.putBoolean(PREF_EXPLODED,    explodedCheckBox.isSelected());
        pref.putBoolean(PREF_HOT_DEPLOY,  hotDeployCheckBox.isSelected());
        pref.putBoolean(PREF_AUTO_DEPLOY, autoDeployCheckBox.isSelected());
        pref.putBoolean(PREF_LIVE_RELOAD, liveReloadCheckBox.isSelected());
        pref.putBoolean(PREF_KEEP_STATE,  keepStateCheckBox.isSelected());
        pref.putBoolean(PREF_TRIM_LOG,    trimLogCheckBox.isSelected());
        pref.putBoolean(PREF_IGNORE_TEST, ignoreTestCheckBox.isSelected());
        pref.putBoolean(PREF_AI_AGENT,    aiAgentCheckBox.isSelected());
        pref.put(PREF_AI_API_KEY,         aiApiKeyField.getText().trim());
        Object selProv = aiProviderCombo.getSelectedItem();
        pref.put(PREF_AI_PROVIDER, selProv != null ? selProv.toString().trim() : "");
        Object selModel = aiModelCombo.getSelectedItem();
        pref.put(PREF_AI_MODEL,    selModel != null ? selModel.toString().trim() : "");
        pref.put(PREF_AI_PROVIDER_LOC,    aiProviderLocField.getText().trim());
    }

    private void updateRemoteEnabled() {
        boolean remote = remoteCheckBox.isSelected();
        hostNameField.setEnabled(remote);
        adminPortField.setEnabled(remote);
        adminUserField.setEnabled(remote);
    }

    /** GenAIProvider enum values from ecosystem-ai (user can type custom values). */
    private static final List<String> AI_PROVIDERS = Arrays.asList(
            "", "OPEN_AI", "ANTHROPIC", "GOOGLE", "MISTRAL", "GROQ",
            "DEEPSEEK", "DEEPINFRA", "OLLAMA", "LM_STUDIO", "GPT4ALL", "CUSTOM_OPEN_AI"
    );

    /**
     * Populates the model combobox for the selected provider.
     * Local providers use static lists; cloud providers fetch filtered models from OpenRouter.
     */
    private void loadModelsForProvider(String provider, String savedModel) {
        RP.post(() -> {
            List<String> models = new ArrayList<>();
            models.add("");
            if (provider != null && PayaraAIModelRegistry.LOCAL_PROVIDERS.contains(provider)) {
                models.addAll(PayaraAIModelRegistry.getLocalModels(
                        provider, aiProviderLocField.getText().trim()));
            } else if (provider != null && !provider.isEmpty()) {
                // Cloud provider: fetch provider-filtered models from OpenRouter
                models.addAll(PayaraAIModelRegistry.getModelsForProvider(provider));
            }
            SwingUtilities.invokeLater(() -> {
                Object current = aiModelCombo.getSelectedItem();
                String keep = (savedModel != null && !savedModel.isEmpty())
                        ? savedModel : (current != null ? current.toString() : "");
                aiModelCombo.removeAllItems();
                models.forEach(aiModelCombo::addItem);
                if (!keep.isEmpty()) {
                    aiModelCombo.setSelectedItem(keep);
                    if (!keep.equals(aiModelCombo.getSelectedItem())) {
                        aiModelCombo.addItem(keep);
                        aiModelCombo.setSelectedItem(keep);
                    }
                }
            });
        });
    }

    /** Loads Payara Server versions from the tooling API on a background thread. */
    private void loadServerVersions(String saved) {
        RP.post(() -> {
            List<String> versions = new ArrayList<>();
            versions.add("");               // blank = "let the plugin decide"
            try {
                List<PayaraPlatformVersionAPI> fetched = PayaraPlatformVersion.getVersions();
                fetched.stream()
                       .sorted(Collections.reverseOrder())
                       .forEach(v -> versions.add(v.toString()));
            } catch (Exception ex) {
                // network unavailable — fall through with empty list
            }
            SwingUtilities.invokeLater(() -> {
                serverVersionCombo.removeAllItems();
                versions.forEach(serverVersionCombo::addItem);
                if (!saved.isEmpty()) {
                    serverVersionCombo.setSelectedItem(saved);
                    if (!saved.equals(serverVersionCombo.getSelectedItem())) {
                        // version not in list (e.g. custom) — add and select it
                        serverVersionCombo.addItem(saved);
                        serverVersionCombo.setSelectedItem(saved);
                    }
                }
            });
        });
    }

    private static String msg(String key) {
        return NbBundle.getMessage(ServerMavenPropertiesPanel.class,
                "ServerMavenPropertiesPanel." + key);
    }

    // ── Scrollable — tracks viewport width so no horizontal bar appears ────────

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth()  { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    // ── Collapsible section widget ────────────────────────────────────────────

    private static final class CollapsibleSection extends JPanel {

        private static final String EXPANDED_ARROW  = "▼ ";
        private static final String COLLAPSED_ARROW = "► ";

        private boolean expanded;
        private final String title;
        private final JButton toggle;
        private final JPanel content;
        private final GridBagConstraints gbc;
        private int rowIndex;

        CollapsibleSection(String title, boolean expanded) {
            this.title    = title;
            this.expanded = expanded;
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            // ── Header ─────────────────────────────────────────────────────
            toggle = new JButton((expanded ? EXPANDED_ARROW : COLLAPSED_ARROW) + title);
            toggle.setHorizontalAlignment(SwingConstants.LEFT);
            toggle.setFont(toggle.getFont().deriveFont(Font.BOLD));
            toggle.setBorderPainted(false);
            toggle.setContentAreaFilled(false);
            toggle.setFocusPainted(false);
            toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            toggle.addActionListener(e -> toggleExpanded());

            JPanel header = new JPanel(new BorderLayout(4, 0));
            header.setOpaque(false);
            header.add(toggle, BorderLayout.WEST);
            header.add(new JSeparator(), BorderLayout.CENTER);

            // ── Content ────────────────────────────────────────────────────
            content = new JPanel(new GridBagLayout());
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 0));

            gbc = new GridBagConstraints();
            gbc.fill   = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(3, 0, 3, 8);
            gbc.gridy  = 0;

            add(header, BorderLayout.NORTH);
            if (expanded) {
                add(content, BorderLayout.CENTER);
            }
        }

        void addComponentRow(String labelText, JPanel component) {
            gbc.gridy = rowIndex++;
            gbc.gridx   = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
            content.add(new JLabel(labelText), gbc);
            gbc.gridx   = 1; gbc.weightx = 1.0;
            content.add(component, gbc);
        }

        void addRow(String labelText, JTextField field) {
            gbc.gridy = rowIndex++;
            gbc.gridx   = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
            content.add(new JLabel(labelText), gbc);
            gbc.gridx   = 1; gbc.weightx = 1.0;
            content.add(field, gbc);
        }

        void addComboRow(String labelText, JComboBox<?> combo) {
            gbc.gridy = rowIndex++;
            gbc.gridx   = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
            content.add(new JLabel(labelText), gbc);
            gbc.gridx   = 1; gbc.weightx = 1.0;
            content.add(combo, gbc);
        }

        void addCheckRow(String labelText, JCheckBox check) {
            gbc.gridy = rowIndex++;
            gbc.gridx   = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
            content.add(new JLabel(labelText), gbc);
            gbc.gridx   = 1; gbc.weightx = 1.0;
            content.add(check, gbc);
        }

        private void toggleExpanded() {
            expanded = !expanded;
            toggle.setText((expanded ? EXPANDED_ARROW : COLLAPSED_ARROW) + title);
            if (expanded) {
                add(content, BorderLayout.CENTER);
            } else {
                remove(content);
            }
            revalidate();
            repaint();
            // propagate layout change up to the containing scroll pane / panel
            java.awt.Container p = getParent();
            if (p != null) {
                p.revalidate();
                p.repaint();
            }
        }
    }
}
