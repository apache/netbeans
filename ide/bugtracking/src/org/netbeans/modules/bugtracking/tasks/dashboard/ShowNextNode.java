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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class ShowNextNode extends LeafNode {

    private final int count;
    private JPanel panel;
    private LinkButton btnName;
    private Action showNextAction;

    public ShowNextNode(TaskContainerNode parent, int count) {
        super(parent);
        this.count = count;
        this.showNextAction = new ShowNextAction();
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            btnName = new LinkButton(getCountString(), showNextAction);
            panel.add(btnName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0));
            panel.add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }
        btnName.setForeground(foreground, isSelected);
        return panel;
    }

    private String getCountString() {
        return NbBundle.getMessage(ShowNextNode.class, "LBL_ShowNext", count);
    }

    private class ShowNextAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            ((TaskContainerNode) getParent()).showAdditionalPage();
        }
    }
}
