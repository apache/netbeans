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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;

/**
 *
 * @author jpeska
 */
public class EmptyContentNode extends LeafNode {

    private final LinkButton linkButton;
    private final String message;
    private final Object LOCK = new Object();
    private JPanel panel;
    private TreeLabel lblMessage;

    public EmptyContentNode(TreeListNode parent, String message, LinkButton linkButton) {
        super(parent);
        this.message = message;
        this.linkButton = linkButton;
    }

    public EmptyContentNode(TreeListNode parent, String message) {
        this(parent, message, null);
    }

    public EmptyContentNode(TreeListNode parent, LinkButton linkButton) {
        this(parent, "", linkButton);
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (LOCK) {
            if (null == panel) {
                panel = new JPanel(new GridBagLayout());
                panel.setBorder(new EmptyBorder(0, 0, 0, 0));
                panel.setOpaque(false);
                if (message != null && !message.isEmpty()) {
                    lblMessage = new TreeLabel(message);
                    lblMessage.setBorder(new EmptyBorder(0, 0, 0, 5));
                    panel.add(lblMessage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                }
                if (linkButton != null) {
                    panel.add(linkButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                }
                panel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 30), 0, 0));
            }
        }
        if (lblMessage != null) {
            lblMessage.setForeground(isSelected ? foreground : ColorManager.getDefault().getDisabledColor());
        }
        if (linkButton != null) {
            linkButton.setForeground(foreground, isSelected);
        }
        return panel;
    }
}
