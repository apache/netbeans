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
package org.netbeans.modules.acp.ui.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.acp.api.AcpAgentDescriptor;
import org.netbeans.modules.acp.api.AcpAgentManager;
import org.netbeans.modules.acp.ui.chat.AcpAgentConfigPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "BTN_AddAgent=Add...",
    "BTN_EditAgent=Edit...",
    "BTN_RemoveAgent=Remove",
    "TTL_AddAgent=Add ACP Agent",
    "TTL_EditAgent=Edit ACP Agent",
    "MSG_ConfirmRemoveAgent=Are you sure you want to remove agent \"{0}\"?"
})
final class AcpOptionsPanel extends JPanel {

    private static final int AGENT_LIST_WIDTH = 300;
    private static final int AGENT_LIST_HEIGHT = 200;

    private final AcpOptionsPanelController controller;
    private final DefaultListModel<AcpAgentDescriptor> listModel = new DefaultListModel<>();
    private final JList<AcpAgentDescriptor> agentList;
    private final JButton editButton;
    private final JButton removeButton;

    private boolean dirty;

    AcpOptionsPanel(AcpOptionsPanelController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        agentList = new JList<>(listModel);
        agentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentList.setCellRenderer(new AgentListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(agentList);
        scrollPane.setPreferredSize(new Dimension(AGENT_LIST_WIDTH, AGENT_LIST_HEIGHT));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton addButton = new JButton(Bundle.BTN_AddAgent());
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
        addButton.addActionListener(e -> onAdd());
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(4));

        editButton = new JButton(Bundle.BTN_EditAgent());
        editButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, editButton.getPreferredSize().height));
        editButton.addActionListener(e -> onEdit());
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createVerticalStrut(4));

        removeButton = new JButton(Bundle.BTN_RemoveAgent());
        removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeButton.getPreferredSize().height));
        removeButton.addActionListener(e -> onRemove());
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalGlue());
        add(buttonPanel, BorderLayout.EAST);

        agentList.addListSelectionListener(e -> updateButtonState());
        updateButtonState();
    }

    void load() {
        listModel.clear();
        for (AcpAgentDescriptor agent : AcpAgentManager.getDefault().getAgents()) {
            listModel.addElement(agent);
        }
        dirty = false;
        updateButtonState();
    }

    void store() {
        if (!dirty) {
            return;
        }
        AcpAgentManager.getDefault().setAgents(
                Collections.list(listModel.elements()));
        dirty = false;
    }

    private void onAdd() {
        AcpAgentConfigPanel configPanel = new AcpAgentConfigPanel();
        DialogDescriptor dd = new DialogDescriptor(configPanel, Bundle.TTL_AddAgent());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            AcpAgentDescriptor descriptor = configPanel.buildDescriptor();
            if (descriptor != null) {
                listModel.addElement(descriptor);
                agentList.setSelectedIndex(listModel.size() - 1);
                markDirty();
            }
        }
    }

    private void onEdit() {
        int idx = agentList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        AcpAgentDescriptor existing = listModel.get(idx);
        AcpAgentConfigPanel configPanel = new AcpAgentConfigPanel(existing);
        DialogDescriptor dd = new DialogDescriptor(configPanel, Bundle.TTL_EditAgent());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            AcpAgentDescriptor updated = configPanel.buildDescriptor();
            if (updated != null) {
                listModel.set(idx, updated);
                markDirty();
            }
        }
    }

    private void onRemove() {
        int idx = agentList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        AcpAgentDescriptor agent = listModel.get(idx);
        NotifyDescriptor confirm = new NotifyDescriptor.Confirmation(
                Bundle.MSG_ConfirmRemoveAgent(agent.getName()),
                NotifyDescriptor.YES_NO_OPTION
        );
        if (DialogDisplayer.getDefault().notify(confirm) == NotifyDescriptor.YES_OPTION) {
            listModel.remove(idx);
            markDirty();
        }
    }

    private void markDirty() {
        dirty = true;
        controller.changed();
    }

    private void updateButtonState() {
        boolean hasSelection = agentList.getSelectedIndex() >= 0;
        editButton.setEnabled(hasSelection);
        removeButton.setEnabled(hasSelection);
    }

    private static final class AgentListCellRenderer extends javax.swing.DefaultListCellRenderer {

        @Override
        public java.awt.Component getListCellRendererComponent(
                javax.swing.JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AcpAgentDescriptor agent) {
                setText(agent.getName() + "  [" + agent.getTransportType() + "]");
            }
            return this;
        }
    }
}
