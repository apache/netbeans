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
package org.netbeans.modules.acp.ui;

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Shows a permission dialog when an ACP agent requests permission
 * to perform a tool call.
 */
@NbBundle.Messages({
    "TTL_PermissionRequest=Agent Permission Request",
    "LBL_ToolName=Tool:",
    "LBL_ToolTitle=Action:",
    "LBL_ChooseOption=Choose an option:"
})
public final class AcpPermissionDialog {

    private AcpPermissionDialog() {
    }

    public static AcpSchema.RequestPermissionResponse showPermissionRequest(
            AcpSchema.RequestPermissionRequest request) {

        AcpSchema.ToolCallUpdate toolCall = request.toolCall();
        List<AcpSchema.PermissionOption> options = request.options();

        if (options == null || options.isEmpty()) {
            return new AcpSchema.RequestPermissionResponse(new AcpSchema.PermissionCancelled());
        }

        AtomicReference<AcpSchema.RequestPermissionResponse> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Runnable showDialog = () -> {
            JPanel panel = new JPanel(new BorderLayout(12, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
            JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
            iconLabel.setVerticalAlignment(JLabel.TOP);
            headerPanel.add(iconLabel, BorderLayout.WEST);

            JPanel infoPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 2, 2, 8);

            if (toolCall != null && toolCall.name() != null) {
                gbc.gridx = 0;
                gbc.gridy = 0;
                infoPanel.add(new JLabel(Bundle.LBL_ToolName()), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                infoPanel.add(new JLabel("<html><b>" + escapeHtml(toolCall.name()) + "</b></html>"), gbc);
            }
            if (toolCall != null && toolCall.title() != null) {
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                infoPanel.add(new JLabel(Bundle.LBL_ToolTitle()), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                infoPanel.add(new JLabel(escapeHtml(toolCall.title())), gbc);
            }
            headerPanel.add(infoPanel, BorderLayout.CENTER);
            panel.add(headerPanel, BorderLayout.NORTH);

            if (toolCall != null && toolCall.rawInput() != null) {
                JTextArea inputArea = new JTextArea(toolCall.rawInput().toString(), 6, 50);
                inputArea.setEditable(false);
                inputArea.setLineWrap(true);
                inputArea.setWrapStyleWord(true);
                JScrollPane sp = new JScrollPane(inputArea);
                sp.setBorder(BorderFactory.createTitledBorder("Details"));
                panel.add(sp, BorderLayout.CENTER);
            }

            JPanel optionsPanel = new JPanel(new GridBagLayout());
            optionsPanel.setBorder(BorderFactory.createTitledBorder(Bundle.LBL_ChooseOption()));
            ButtonGroup group = new ButtonGroup();
            JRadioButton[] buttons = new JRadioButton[options.size()];
            for (int i = 0; i < options.size(); i++) {
                AcpSchema.PermissionOption opt = options.get(i);
                String label = opt.name() != null ? opt.name() : opt.optionId();
                buttons[i] = new JRadioButton(label);
                group.add(buttons[i]);
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(2, 4, 2, 4);
                optionsPanel.add(buttons[i], gbc);
            }
            buttons[0].setSelected(true);
            panel.add(optionsPanel, BorderLayout.SOUTH);

            DialogDescriptor dd = new DialogDescriptor(panel,
                    Bundle.TTL_PermissionRequest());

            Object choice = DialogDisplayer.getDefault().notify(dd);
            if (choice == NotifyDescriptor.OK_OPTION) {
                for (int i = 0; i < buttons.length; i++) {
                    if (buttons[i].isSelected()) {
                        result.set(new AcpSchema.RequestPermissionResponse(
                                new AcpSchema.PermissionSelected(options.get(i).optionId())));
                        break;
                    }
                }
            }
            if (result.get() == null) {
                result.set(new AcpSchema.RequestPermissionResponse(
                        new AcpSchema.PermissionCancelled()));
            }
            latch.countDown();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            SwingUtilities.invokeLater(showDialog);
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return new AcpSchema.RequestPermissionResponse(
                        new AcpSchema.PermissionCancelled());
            }
        }

        return result.get();
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
