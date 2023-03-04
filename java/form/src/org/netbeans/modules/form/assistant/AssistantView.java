/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.form.assistant;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.*;

import org.netbeans.modules.form.FormLoaderSettings;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Assistant view.
 *
 * @author Jan Stola
 */
public class AssistantView extends JPanel {
    private JLabel messageLabel;
    private AssistantModel model;
    
    public AssistantView(AssistantModel model) {
        this.model = model;

        Listener listener = new Listener();
        model.addPropertyChangeListener(listener);

        setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getForeground()));
        setToolTipText(NbBundle.getMessage(getClass(), "TOOLTIP_HelpBar")); // NOI18N

        // Message label
        messageLabel = new JLabel();
        messageLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/lightbulb.gif", true)); // NOI18N

        // Close button
        JButton closeButton = new JButton("x"); // NOI18N
        closeButton.setFont(Font.getFont("SansSerif")); // NOI18N
        closeButton.setOpaque(false);
        closeButton.setFocusPainted(false);
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(listener);
        // Workaround for GroupLayout.BASELINE == GroupLayout.CENTER bug
        JPanel panel = new JPanel(new BorderLayout(0,0));
        panel.setOpaque(false);
        panel.add(closeButton);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(12)
                .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(12));
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(2)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(messageLabel, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(2));
    }

    private class Listener implements ActionListener, PropertyChangeListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FormLoaderSettings.getInstance().setAssistantShown(false);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String[] messages = model.getMessages();
            String message = null;
            if (messages != null) {
                int index = (int)(Math.random()*messages.length);
                message = messages[index];
            }
            if (model.getAdditionalContext() != null) {
                messages = model.getAdditionalMessages();
                if (messages != null) {
                    int index = (int)(Math.random()*messages.length);
                    message = "<html>" + message + "<br>" + messages[index]; // NOI18N
                }
            }
            Object[] params = model.getParameters();
            if (params != null) {
                message = MessageFormat.format(message, params);
            }
            messageLabel.setText(message);
        }

    }

}
