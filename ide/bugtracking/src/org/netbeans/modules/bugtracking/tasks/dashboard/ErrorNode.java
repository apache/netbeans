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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import org.netbeans.modules.team.commons.treelist.LinkButton;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Error message visualization in TreeList
 *
 * @author S. Aubrecht
 */
public class ErrorNode extends LeafNode {

    private JPanel panel;
    private final JLabel lblMessage;
    private final LinkButton btnRefresh;
    private final Action defaultAction;

    public ErrorNode(String text, Action refreshAction) {
        super(null);
        this.defaultAction = refreshAction;
        btnRefresh = new LinkButton(NbBundle.getMessage(ErrorNode.class, "LBL_Retry"), refreshAction); //NOI18N
        lblMessage = new TreeLabel(text);
        lblMessage.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/error.png", false)); //NOI18N
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if (null == panel) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);

            panel.add(lblMessage, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
            panel.add(btnRefresh, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }

        if (isSelected) {
            lblMessage.setForeground(foreground);
        } else {
            lblMessage.setForeground(ColorManager.getDefault().getErrorColor());
        }
        btnRefresh.setForeground(foreground, isSelected);
        return panel;
    }

    @Override
    protected Action getDefaultAction() {
        return defaultAction;
    }
}
