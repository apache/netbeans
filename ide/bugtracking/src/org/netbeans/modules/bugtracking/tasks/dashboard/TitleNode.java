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

import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.ProgressLabel;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.NbBundle;

/**
 * Category Node. E.g. My Projects, Open Projects
 *
 * @author Jan Becicka
 */
public class TitleNode extends LeafNode {

    private JPanel panel;
    private JLabel lblName;
    private final String titleName;
    private final LinkButton[] buttons;
    private final Object LOCK = new Object();
    private final ProgressLabel lblProgress;

    public TitleNode(String titleName, LinkButton... buttons) {
        super(null);
        this.titleName = titleName;
        this.buttons = buttons;
        lblProgress = createProgressLabel(NbBundle.getMessage(TitleNode.class, "LBL_LoadingInProgress"));
        lblProgress.setForeground(ColorManager.getDefault().getDefaultBackground());
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (LOCK) {
            if (null == panel) {
                panel = new JPanel(new GridBagLayout());
                panel.setBorder(new EmptyBorder(0, 0, 0, 0));
                panel.setOpaque(false);
                lblName = new TreeLabel(titleName);
                lblName.setBorder(new EmptyBorder(0, 0, 0, 5));
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
                panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                panel.add(lblProgress, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                panel.add(new JLabel(), new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0));


                if (buttons != null) {
                    for (int i = 0; i < buttons.length; i++) {
                        LinkButton linkButton = buttons[i];
                        panel.add(linkButton, new GridBagConstraints(5 + i, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
                    }
                }
            }
            lblName.setForeground(foreground);
        }
        return panel;
    }

    void setProgressVisible(final boolean visible) {
        if (panel != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    lblProgress.setVisible(visible);
                    panel.repaint();
                    panel.revalidate();
                    fireContentChanged();
                }
            });
        }
    }

    @Override
    protected Type getType() {
        return Type.TITLE;
    }
}
