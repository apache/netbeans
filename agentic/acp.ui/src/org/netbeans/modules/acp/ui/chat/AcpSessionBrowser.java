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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.acp.api.AcpConnection;
import org.netbeans.modules.acp.api.AcpSessionInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "TTL_SessionBrowser=Past Sessions",
    "LBL_SessionEntry={0} — {1}",
    "BTN_ResumeSession=Resume",
    "BTN_DeleteSession=Delete"
})
final class AcpSessionBrowser {

    private static final int SESSION_BROWSER_WIDTH = 450;
    private static final int SESSION_BROWSER_HEIGHT = 250;

    private AcpSessionBrowser() {
    }

    static void show(List<AcpSessionInfo> sessions, AcpConnection connection,
            Consumer<AcpSessionInfo> resume, BiConsumer<AcpSessionInfo, Runnable> delete) {
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
                resume.accept(selected);
            }
        });

        deleteBtn.addActionListener(ev -> {
            AcpSessionInfo selected = sessionList.getSelectedValue();
            if (selected != null) {
                delete.accept(selected, () -> {
                    listModel.removeElement(selected);
                    if (listModel.isEmpty()) {
                        dialog.dispose();
                    }
                });
            }
        });

        dialog.setVisible(true);
    }

    private static JList<AcpSessionInfo> buildSessionList(List<AcpSessionInfo> sessions,
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

    private static final class SessionListCellRenderer extends DefaultListCellRenderer {

        private final String agentName;

        SessionListCellRenderer(String agentName) {
            this.agentName = agentName;
        }

        private static final DateTimeFormatter DISPLAY_FORMAT =
                DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

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
                return ZonedDateTime.parse(iso)
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .format(DISPLAY_FORMAT);
            } catch (DateTimeParseException ex) {
                try {
                    return LocalDateTime.parse(iso).format(DISPLAY_FORMAT);
                } catch (DateTimeParseException ex2) {
                    return iso;
                }
            }
        }
    }
}
